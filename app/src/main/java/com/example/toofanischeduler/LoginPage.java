package com.example.toofanischeduler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginPage extends AppCompatActivity {

    EditText user, pass;
    TextView reg, forgot;
    Button log;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Authentication

        user = findViewById(R.id.user);
        pass = findViewById(R.id.pass);
        log = findViewById(R.id.log);
        reg = findViewById(R.id.reg);
        forgot = findViewById(R.id.forgot);

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = user.getText().toString().trim();
                String password = pass.getText().toString().trim();

                if (userEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginPage.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Attempt to sign in with email and password using Firebase Authentication
                    mAuth.signInWithEmailAndPassword(userEmail, password)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginPage.this, "Login successful", Toast.LENGTH_SHORT).show();
                                        LoggedData.email = userEmail;
                                        Intent intent = new Intent(LoginPage.this, test.class);
                                        startActivity(intent);
                                        finish(); // Finish the login activity so user can't go back to it after login
                                    } else {
                                        // Handle login failure
                                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                            Toast.makeText(LoginPage.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                            Toast.makeText(LoginPage.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(LoginPage.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }
            }
        });

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPage.this, UserReg.class);
                startActivity(intent);
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPage.this, RecoveryActivity.class);
                startActivity(intent);
            }
        });
    }
}
