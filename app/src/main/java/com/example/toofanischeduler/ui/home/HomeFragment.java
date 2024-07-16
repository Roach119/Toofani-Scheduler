package com.example.toofanischeduler.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.toofanischeduler.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {

    TextView homeusername;
    ImageView showhomeimage;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homeusername = view.findViewById(R.id.homeusername);
        showhomeimage = view.findViewById(R.id.showhomeimage);

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get current user from Firebase Authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Get the user's UID
            String userUid = currentUser.getUid();

            // Fetch user's name from Firestore
            db.collection("users").document(userUid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                homeusername.setText("Welcome " + name + "!");
                            } else {
                                homeusername.setText("Welcome!");
                            }

                            // Load profile image if available
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Picasso.get().load(profileImageUrl).into(showhomeimage);
                            } else {
                                // Load default image if profile image is not available
                                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b").into(showhomeimage);
                            }
                        } else {
                            homeusername.setText("Welcome!");
                            // Load default image if profile document does not exist
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b").into(showhomeimage);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                        homeusername.setText("Welcome!");
                        // Load default image on failure
                        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b").into(showhomeimage);
                    });
        } else {
            homeusername.setText("Welcome!");
            // Load default image if no user is logged in
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b").into(showhomeimage);
        }

        return view;
    }
}
