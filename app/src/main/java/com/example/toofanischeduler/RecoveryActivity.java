package com.example.toofanischeduler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RecoveryActivity extends AppCompatActivity {

    Button recover;
    EditText recmail;
    TextView notfound, showpass;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Authentication

        recover = findViewById(R.id.recover);
        recmail = findViewById(R.id.recmail);
        notfound = findViewById(R.id.notfound);
        showpass = findViewById(R.id.showpass);

        recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the email from the input field
                String mail = recmail.getText().toString().trim();

                // Check if the email is empty and prompt the user if it is
                if (mail.isEmpty()) {
                    notfound.setText("Please enter a valid email address.");
                    showpass.setText("");
                    return;
                }

                // Use Firebase Authentication to send a password reset email
                mAuth.sendPasswordResetEmail(mail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                                if (task.isSuccessful()) {
                                    notfound.setText("Password reset email sent.");
                                    showpass.setText("");
                                } else {
                                    notfound.setText("Email not found or error occurred.");
                                    showpass.setText("");
                                }
                            }
                        });
            }
        });
    }
}
