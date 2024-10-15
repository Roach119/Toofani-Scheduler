package com.example.toofanischeduler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    FirebaseStorage storage;
    StorageReference storageRef;
    String userUid;

    TextView setemail;
    EditText setname, setpass;
    ImageView setimg;
    Button saveset, delacc;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("profile_pictures");

        setemail = findViewById(R.id.setemail);
        setname = findViewById(R.id.setname);
        setpass = findViewById(R.id.setpass);
        setimg = findViewById(R.id.setimg);
        saveset = findViewById(R.id.saveset);
        delacc = findViewById(R.id.delacc);

        // Get the current user and UID
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userUid = currentUser.getUid();
            setemail.setText(currentUser.getEmail());

            // Load user data and profile image
            db.collection("users").document(userUid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            setname.setText(name);

                            // Load existing profile picture if available
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Picasso.get().load(profileImageUrl).into(setimg);
                            } else {
                                // Load default profile image if no custom image found
                                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b").into(setimg);
                            }
                        }
                    });
        }

        // Set click listener to choose image from gallery
        setimg.setOnClickListener(view -> openFileChooser());

        // Set long click listener to delete profile picture
        setimg.setOnLongClickListener(view -> {
            deleteProfileImage();
            return true; // Consume the long click
        });

        saveset.setOnClickListener(view -> {
            String name = setname.getText().toString();
            String pass = setpass.getText().toString();

            if (currentUser != null) {
                currentUser.updateEmail(name);

                if (!pass.isEmpty()) {
                    currentUser.updatePassword(pass)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AccountSettingsActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AccountSettingsActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            // Update user details in Firestore
            db.collection("users").document(userUid)
                    .update("name", name)
                    .addOnSuccessListener(aVoid -> {
                        if (imageUri != null) {
                            uploadImageToFirebaseStorage();
                        } else {
                            Toast.makeText(AccountSettingsActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                            navigateToTestActivity(); // Move to the next activity
                        }
                    });
        });

        delacc.setOnClickListener(view -> {
            if (currentUser != null) {
                String password = setpass.getText().toString().trim();

                if (password.isEmpty()) {
                    Toast.makeText(AccountSettingsActivity.this, "Please enter your password to delete your account", Toast.LENGTH_SHORT).show();
                    return; // Exit the method if password is not provided
                }

                // Prompt user to re-authenticate
                reauthenticateUser(currentUser, password, new ReauthenticationCallback() {
                    @Override
                    public void onReauthenticationSuccess() {
                        // Check if profile picture exists in Firebase Storage
                        StorageReference profileImageRef = storageRef.child(userUid + ".jpg");
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Profile picture exists, proceed with deletion
                            profileImageRef.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Update Firestore to remove profileImageUrl field
                                        db.collection("users").document(userUid)
                                                .update("profileImageUrl", "")
                                                .addOnSuccessListener(aVoid1 -> {
                                                    deleteUserData();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(AccountSettingsActivity.this, "Failed to update profile picture URL in Firestore", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AccountSettingsActivity.this, "Failed to delete profile picture from Firebase Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }).addOnFailureListener(exception -> {
                            // Profile picture doesn't exist, proceed to delete user data
                            deleteUserData();
                        });
                    }

                    @Override
                    public void onReauthenticationFailure(String errorMessage) {
                        Toast.makeText(AccountSettingsActivity.this, "Reauthentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            setimg.setImageURI(imageUri);
        }
    }

    private void uploadImageToFirebaseStorage() {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(userUid + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            fileReference.getDownloadUrl()
                                    .addOnCompleteListener(uriTask -> {
                                        if (uriTask.isSuccessful()) {
                                            String downloadUri = uriTask.getResult().toString();
                                            db.collection("users").document(userUid)
                                                    .update("profileImageUrl", downloadUri)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(AccountSettingsActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                                                        navigateToTestActivity(); // Move to the next activity
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(AccountSettingsActivity.this, "Failed to update profile picture URL", Toast.LENGTH_SHORT).show());
                                        }
                                    });
                        } else {
                            Toast.makeText(AccountSettingsActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void deleteProfileImage() {
        if (currentUser != null) {
            String userUid = currentUser.getUid();

            // Check if profile picture exists in Firebase Storage
            StorageReference profileImageRef = storageRef.child(userUid + ".jpg");
            profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Profile picture exists, proceed with deletion
                profileImageRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            // Update Firestore to remove profileImageUrl field
                            db.collection("users").document(userUid)
                                    .update("profileImageUrl", "")
                                    .addOnSuccessListener(aVoid1 -> {
                                        Toast.makeText(AccountSettingsActivity.this, "Profile picture deleted successfully", Toast.LENGTH_SHORT).show();
                                        // Load default profile image
                                        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b").into(setimg);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AccountSettingsActivity.this, "Failed to update profile picture URL in Firestore", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AccountSettingsActivity.this, "Failed to delete profile picture from Firebase Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }).addOnFailureListener(exception -> {
                // Profile picture doesn't exist, update Firestore and UI accordingly
                db.collection("users").document(userUid)
                        .update("profileImageUrl", "")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AccountSettingsActivity.this, "No profile picture to delete", Toast.LENGTH_SHORT).show();
                            // Load default profile image
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b").into(setimg);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AccountSettingsActivity.this, "Failed to update profile picture URL in Firestore", Toast.LENGTH_SHORT).show();
                        });
            });
        }
    }

    private void deleteUserData() {
        // Step 1: Query and delete user's schedules
        db.collection("schedules")
                .whereEqualTo("userUid", userUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<Void>> deletionTasks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            deletionTasks.add(document.getReference().delete());
                        }

                        // Step 2: Wait for all schedule deletions to complete
                        Tasks.whenAll(deletionTasks)
                                .addOnCompleteListener(scheduleDeletionTask -> {
                                    if (scheduleDeletionTask.isSuccessful()) {
                                        // Step 3: Delete Firestore document
                                        db.collection("users").document(userUid)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    // Step 4: Delete Firebase Authentication user
                                                    currentUser.delete()
                                                            .addOnCompleteListener(authTask -> {
                                                                if (authTask.isSuccessful()) {
                                                                    Toast.makeText(AccountSettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                                    navigateToLoginPage();
                                                                    // Move to login page after deletion
                                                                } else {
                                                                    Toast.makeText(AccountSettingsActivity.this, "Failed to delete account from Firebase Authentication: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(AccountSettingsActivity.this, "Failed to delete user data from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(AccountSettingsActivity.this, "Failed to delete schedule data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(AccountSettingsActivity.this, "Error fetching schedule data to delete", Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private void navigateToTestActivity() {
        Intent intent = new Intent(AccountSettingsActivity.this, test.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLoginPage() {
        Intent intent = new Intent(AccountSettingsActivity.this, LoginPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private interface ReauthenticationCallback {
        void onReauthenticationSuccess();
        void onReauthenticationFailure(String errorMessage);
    }

    private void reauthenticateUser(FirebaseUser user, String password, ReauthenticationCallback callback) {
        // Reauthenticate user with their current credentials before sensitive operations
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    callback.onReauthenticationSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onReauthenticationFailure(e.getMessage());
                });
    }

}