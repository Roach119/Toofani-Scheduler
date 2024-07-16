package com.example.toofanischeduler;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewSchedule extends AppCompatActivity {

    private TextView dayhead2;
    private ListView timeListView;
    private ArrayAdapter<String> itemsAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_schedule);

        dayhead2 = findViewById(R.id.dayhead2);
        timeListView = findViewById(R.id.timeListView);

        db = FirebaseFirestore.getInstance();

        String selectedDay = getIntent().getStringExtra("selectedDay");
        dayhead2.setText(selectedDay);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getApplicationContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userUid = currentUser.getUid();

        // Query Firestore for schedules of the logged-in user on the selected day
        db.collection("schedules")
                .whereEqualTo("day", selectedDay)
                .whereEqualTo("userUid", userUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Schedule> schedules = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Schedule schedule = document.toObject(Schedule.class);
                            schedule.setId(document.getId());
                            schedules.add(schedule);
                        }

                        // Sort schedules by start time
                        Collections.sort(schedules, new Comparator<Schedule>() {
                            @Override
                            public int compare(Schedule s1, Schedule s2) {
                                return convertToMinutes(s1.getStartTime()) - convertToMinutes(s2.getStartTime());
                            }
                        });

                        // Update ListView using an ArrayAdapter
                        itemsAdapter = new ArrayAdapter<>(ViewSchedule.this, android.R.layout.simple_list_item_1);
                        timeListView.setAdapter(itemsAdapter);

                        for (Schedule sobj : schedules) {
                            String scheduleString = sobj.getStartTime() + " - " + sobj.getEndTime() + " > " + sobj.getWork();
                            itemsAdapter.add(scheduleString);
                        }

                        itemsAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(ViewSchedule.this, "Failed to fetch schedules", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Utility method to convert time to minutes
    private int convertToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
}
