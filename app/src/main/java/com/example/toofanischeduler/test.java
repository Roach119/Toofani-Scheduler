package com.example.toofanischeduler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.toofanischeduler.databinding.ActivityTestBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class test extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityTestBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarTest.toolbar);
        binding.appBarTest.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle floating action button click
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Access header views
        View headerView = navigationView.getHeaderView(0);
        TextView headername = headerView.findViewById(R.id.headername);
        TextView headermail = headerView.findViewById(R.id.headermail);
        ImageView slidebarimage = headerView.findViewById(R.id.slidebarimage);

        // Set user information in the navigation drawer header
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            headermail.setText(currentUser.getEmail());

            // Fetch user's name and profile image URL from Firestore
            String userUid = currentUser.getUid();
            db.collection("users").document(userUid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            headername.setText(name);

                            // Load profile image if exists, otherwise load default avatar
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Picasso.get().load(profileImageUrl).into(slidebarimage);
                            } else {
                                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b").into(slidebarimage);
                            }
                        } else {
                            headername.setText("User");
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b").into(slidebarimage);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                        headername.setText("User");
                        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/toofani-scheduler.appspot.com/o/defaultavatar.jpg?alt=media&token=0b5dca6c-ab04-409b-a9f0-f33382ffe43b").into(slidebarimage);
                    });
        }

        // Define top-level destinations for the Navigation Component
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_myschedule, R.id.nav_editschedule)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_test);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            // Log out the user from Firebase Authentication
            mAuth.signOut();

            // Redirect to LoginActivity
            Intent intent = new Intent(this, LoginPage.class);
            startActivity(intent);
            finish(); // Close this activity
            return true;
        } else if (id == R.id.action_settings) {
            // Navigate to AccountSettingsActivity
            Intent intent = new Intent(this, AccountSettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_test);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
