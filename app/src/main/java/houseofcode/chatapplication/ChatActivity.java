package houseofcode.chatapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.facebook.internal.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 0;
    private static final int GALLERY_REQUEST = 1;
    private static final int CANCELLED_REQUEST = 2;


    private Toolbar mToolbar;
    private EditText messageInput;
    private ScrollView mScrollView;
    private TextView textDisplay;
    private ImageButton importImage;
    private ImageView chatImage;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, roomRef, messageKeyRef;
    private StorageReference mImageStorage;

    private String roomName, currentUserId, currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        roomName = getIntent().getStringExtra("ChatRoom");

        mToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(roomName);

        messageInput = (EditText) findViewById(R.id.chat_input_message);
        mScrollView = (ScrollView) findViewById(R.id.chat_message_scroll_view);
        textDisplay = (TextView) findViewById(R.id.chat_text_view);
        importImage = (ImageButton) findViewById(R.id.select_image_button);
        chatImage = (ImageView) findViewById(R.id.chat_image);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        roomRef = FirebaseDatabase.getInstance().getReference().child("Rooms").child(roomName);
        mImageStorage = FirebaseStorage.getInstance().getReference();

        getUserData();

        messageInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)){
                    sendMessageToDatabase();
                    messageInput.getText().clear();
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
                return false;
            }
        });

        importImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showSelectorDialog();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        roomRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserData(){
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessageToDatabase(){

        String message = messageInput.getText().toString();
        String messageKey = roomRef.push().getKey();

        if(TextUtils.isEmpty(message)) {
            Toast.makeText(ChatActivity.this, "Write a message" , Toast.LENGTH_SHORT).show();
        } else {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM, dd, yyyy");
            String currentDate = dateFormat.format(calendar.getTime());

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
            String currentTime = timeFormat.format(calendar.getTime());

            HashMap<String, Object> messageKeyMap = new HashMap<>();
            roomRef.updateChildren(messageKeyMap);

            messageKeyRef = roomRef.child(messageKey);

            HashMap<String, Object> messageDataMap = new HashMap<>();
            messageDataMap.put("name", currentUserName);
            messageDataMap.put("date", currentDate);
            messageDataMap.put("time", currentTime);
            messageDataMap.put("message", message);
            messageDataMap.put("image", "");
            messageKeyRef.updateChildren(messageDataMap);


        }
    }

    private void storeImage(Uri image){

        final String randomName = random();

        StorageReference filepath = mImageStorage.child("images").child(randomName+".png");
        filepath.putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){

                    mImageStorage.child("images/"+randomName+".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            String messageKey = roomRef.push().getKey();

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM, dd, yyyy");
                            String currentDate = dateFormat.format(calendar.getTime());

                            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
                            String currentTime = timeFormat.format(calendar.getTime());

                            HashMap<String, Object> messageKeyMap = new HashMap<>();
                            roomRef.updateChildren(messageKeyMap);

                            messageKeyRef = roomRef.child(messageKey);

                            HashMap<String, Object> messageDataMap = new HashMap<>();
                            messageDataMap.put("name", currentUserName);
                            messageDataMap.put("date", currentDate);
                            messageDataMap.put("time", currentTime);
                            messageDataMap.put("message", "");
                            messageDataMap.put("image", url);
                            messageKeyRef.updateChildren(messageDataMap);
                        }
                    });
                } else {
                    Toast.makeText(ChatActivity.this, "Error in uploading" , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(50);
        char tempChar;
        for(int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96)+32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void displayMessages(DataSnapshot dataSnapshot){

        Iterator iterator = dataSnapshot.getChildren().iterator();;

        while(iterator.hasNext()){

            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String imageURL = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();


            //Image loading for chat still not working, could be ImageView problem.
            if(imageURL.equals("")){
                textDisplay.append(chatName + "\n" + chatDate + " " + chatTime + ":\n" + chatMessage + "\n\n");
            }else {
                textDisplay.append(chatName + "\n" + chatDate + " " + chatTime + ":\n");
                Picasso.get().load(imageURL).into(chatImage);
                textDisplay.append("\n\n");
            }



            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }

    private void showSelectorDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this, R.style.AlertDialog);
        dialog.setTitle("Add Image");

        String[] options = {"Camera", "Gallery", "Cancel"};

        dialog.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch (i){
                    case 0:
                        photoWithCamera();
                        break;
                    case 1:
                        choosePhotoFromGallery();
                        break;
                    case 2:
                        break;
                }
            }
        });
        dialog.show();
    }

    private void photoWithCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void choosePhotoFromGallery(){
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null){
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

        } else if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null){

            Uri imageUri = data.getData();
            storeImage(imageUri);
        }
    }
}