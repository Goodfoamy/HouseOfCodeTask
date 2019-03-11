package houseofcode.chatapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ChatRoomsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private DatabaseReference groupRef;

    private ListView roomListView;
    private List<DataItem> dataItemList;
    private CustomAdapter adapter;
    private Button createRoomBtn;
    private List<String> listOfRooms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_rooms);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat Rooms");

        roomListView = (ListView) findViewById(R.id.roomListView);
        createRoomBtn = (Button) findViewById(R.id.createRoomBtn);

        dataItemList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        groupRef = database.getReference().child("Rooms");

        createRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewRoom();
            }
        });

        adapter = new CustomAdapter(this, R.layout.list_row, dataItemList);

        roomListView.setAdapter(adapter);
        populateListView();

        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent chatIntent = new Intent(ChatRoomsActivity.this, ChatActivity.class);
                chatIntent.putExtra("ChatRoom", dataItemList.get(i).roomName);
                startActivity(chatIntent);
            }
        });
    }

    private void populateListView(){

        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Iterator iterator = dataSnapshot.getChildren().iterator();
                listOfRooms = new ArrayList<String>();

                while(iterator.hasNext()){

                    listOfRooms.add(((DataSnapshot)iterator.next()).getKey());
                }

                dataItemList.clear();

                for(int i = 0; i < listOfRooms.size(); i++){
                    dataItemList.add(new DataItem(R.drawable.chevron_sign_left, listOfRooms.get(i)));
                    //Icon made by Dave Gandy from www.flaticon.com
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });
    }

    private void createNewRoom() {

        LayoutInflater layout = LayoutInflater.from(this);

        final View dialogView = layout.inflate(R.layout.alert_dialog_text_views, null);

        final EditText roomName = dialogView.findViewById(R.id.roomName);
        final EditText roomDesc = dialogView.findViewById(R.id.roomDesc);


        AlertDialog.Builder dialog = new AlertDialog.Builder(ChatRoomsActivity.this, R.style.AlertDialog);
        dialog.setTitle("New Chatroom");
        dialog.setView(dialogView);
        dialog.setCancelable(false);

        dialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String chatRoomName = roomName.getText().toString();
                String chatRoomDesc = roomDesc.getText().toString();

                if(TextUtils.isEmpty(chatRoomName)){
                    Toast.makeText(ChatRoomsActivity.this, "Room name is missing", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(chatRoomDesc)){
                    Toast.makeText(ChatRoomsActivity.this, "Room description is missing", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseRoomCreation(chatRoomName, chatRoomDesc);
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        dialog.show();
    }

    private void firebaseRoomCreation(final String roomName, String roomDesc) {

        dbRef.child("Rooms").child(roomName).setValue(roomDesc).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ChatRoomsActivity.this, roomName + " is created", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                Intent loginActivity = new Intent(ChatRoomsActivity.this, LoginActivity.class);
                startActivity(loginActivity);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
