package com.example.toofanischeduler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserReg extends AppCompatActivity {

    EditText regmail, regname, regpass;
    Button submit;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String defaultAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reg);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Authentication
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        regmail = findViewById(R.id.regmail);
        regname = findViewById(R.id.regname);
        regpass = findViewById(R.id.regpass);
        submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = regmail.getText().toString().trim();
                String name = regname.getText().toString().trim();
                String password = regpass.getText().toString().trim();

                if (email.isEmpty() || name.isEmpty() || password.isEmpty()) {
                    Toast.makeText(UserReg.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Create user in Firebase Authentication
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(Task task) {
                                    if (task.isSuccessful()) {
                                        // User registered successfully in Authentication
                                        Toast.makeText(UserReg.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                                        // Get the authenticated user
                                        if (mAuth.getCurrentUser() != null) {
                                            String userId = mAuth.getCurrentUser().getUid();

                                            // Store additional user data in Firestore
                                            User user = new User(email, name, defaultAvatarUrl); // Include the default avatar URL
                                            db.collection("users")
                                                    .document(userId)
                                                    .set(user)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(UserReg.this, "User data stored successfully", Toast.LENGTH_SHORT).show();
                                                                regmail.setText("");
                                                                regname.setText("");
                                                                regpass.setText("");
                                                                finish(); // Finish activity or navigate to another screen
                                                            } else {
                                                                Toast.makeText(UserReg.this, "Failed to store user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(UserReg.this, "Failed to get current user", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Registration failed
                                        Toast.makeText(UserReg.this, "Failed to register user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}
