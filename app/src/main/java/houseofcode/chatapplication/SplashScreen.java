package houseofcode.chatapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            Intent chatRoomsIntent = new Intent(SplashScreen.this, ChatRoomsActivity.class);
            startActivity(chatRoomsIntent);
            finish();
        } else {
            Intent loginIntent = new Intent(SplashScreen.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

    }
}
