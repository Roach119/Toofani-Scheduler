package com.example.toofanischeduler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogoActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        mAuth = FirebaseAuth.getInstance();
        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // User is already logged in, redirect to main activity
                    Intent intent = new Intent(LogoActivity.this, test.class);
                    startActivity(intent);
                } else {
                    // User is not logged in, redirect to login activity
                    Intent intent = new Intent(LogoActivity.this, LoginPage.class);
                    startActivity(intent);
                }
                finish(); // Close this activity
            }
        }, 3000); // 3 seconds delay
    }
}
