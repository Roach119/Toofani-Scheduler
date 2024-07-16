package com.example.toofanischeduler;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditMySchedule extends AppCompatActivity {

    private TextView dayhead;
    private EditText startTimetxt, endTimetxt, worktxt;
    private Button btnadd;
    private ListView timeListView2;
    private FirebaseFirestore db;
    private ArrayAdapter<String> itemsAdapter;

    private String selectedDay = "";
    private String userUid = "";

    private List<String> allIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_schedule);

        // Initialize views
        dayhead = findViewById(R.id.dayhead);
        timeListView2 = findViewById(R.id.timeListView2);
        startTimetxt = findViewById(R.id.startTimetxt);
        endTimetxt = findViewById(R.id.endTimetxt);
        worktxt = findViewById(R.id.worktxt);
        btnadd = findViewById(R.id.addbtn);

        // Get selected day from intent extras
        Bundle b = getIntent().getExtras();
        if (b != null) {
            selectedDay = b.getString("selectedDay");
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize adapter for ListView
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        timeListView2.setAdapter(itemsAdapter);

        // Set day header
        dayhead.setText(selectedDay);

        // Set click listener for add button
        btnadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSchedule();
            }
        });

        // Set item click listener for ListView
        timeListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(i);
                showOptionsDialog(selectedItem);
            }
        });

        // Load schedules for the selected day
        Refresh();
    }

    // Method to add a new schedule
    private void addSchedule() {
        String startTime = startTimetxt.getText().toString().trim();
        String endTime = endTimetxt.getText().toString().trim();
        String work = worktxt.getText().toString().trim();

        // Validate input fields
        if (startTime.isEmpty() || endTime.isEmpty() || work.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter start time, end time, and work", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getApplicationContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set user UID
        userUid = currentUser.getUid();

        // Create schedule map
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("day", selectedDay);
        schedule.put("startTime", startTime);
        schedule.put("endTime", endTime);
        schedule.put("work", work);
        schedule.put("userUid", userUid);

        // Add schedule to Firestore
        db.collection("schedules").add(schedule)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Schedule Added", Toast.LENGTH_SHORT).show();
                            Refresh();
                            startTimetxt.setText("");
                            worktxt.setText("");
                            endTimetxt.setText("");// Refresh schedule list
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to add schedule", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to show options dialog (Edit/Delete)
    private void showOptionsDialog(final String selectedItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option:");
        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                showEditDialog(selectedItem);
            }
        });

        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                deleteSchedule(selectedItem);
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    // Method to show edit dialog for a selected schedule item
    private void showEditDialog(final String selectedItem) {
        AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(this);
        editDialogBuilder.setTitle("Edit Item");

        View dialogLayout = LayoutInflater.from(this).inflate(R.layout.edit_dialog_layout, null);
        editDialogBuilder.setView(dialogLayout);

        final EditText startTimeEditText = dialogLayout.findViewById(R.id.startTimeEditText);
        final EditText endTimeEditText = dialogLayout.findViewById(R.id.endTimeEditText);
        final EditText workEditText = dialogLayout.findViewById(R.id.workEditText);

        try {
            // Parse selected item to pre-fill edit fields
            String[] parts = selectedItem.split(" - ");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid format");
            }
            String startTime = parts[0];
            String[] timeAndWork = parts[1].split(" > ");
            if (timeAndWork.length != 2) {
                throw new IllegalArgumentException("Invalid format");
            }
            String endTime = timeAndWork[0];
            String work = timeAndWork[1];

            startTimeEditText.setText(startTime);
            endTimeEditText.setText(endTime);
            workEditText.setText(work);

            // Set save and cancel buttons
            editDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    String editedStartTime = startTimeEditText.getText().toString();
                    String editedEndTime = endTimeEditText.getText().toString();
                    String editedWork = workEditText.getText().toString();

                    updateSchedule(selectedItem, editedStartTime, editedEndTime, editedWork);
                }
            });

            editDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.dismiss();
                }
            });

            editDialogBuilder.show();

        } catch (IllegalArgumentException e) {
            Toast.makeText(EditMySchedule.this, "Invalid selection format", Toast.LENGTH_SHORT).show();
            Log.e("EditMySchedule", "Error parsing selected item: " + selectedItem, e);
        }
    }



    // Method to delete a schedule
    private void deleteSchedule(String selectedItem) {
        int selectedIndex = itemsAdapter.getPosition(selectedItem);
        if (selectedIndex == -1 || selectedIndex >= allIds.size()) {
            Toast.makeText(EditMySchedule.this, "Invalid selection", Toast.LENGTH_SHORT).show();
            return;
        }

        String documentId = allIds.get(selectedIndex);

        // Delete schedule from Firestore
        db.collection("schedules").document(documentId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditMySchedule.this, "Schedule deleted successfully", Toast.LENGTH_SHORT).show();
                            Refresh(); // Refresh schedule list
                        } else {
                            Toast.makeText(EditMySchedule.this, "Failed to delete schedule", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    // Method to update a schedule
    private void updateSchedule(String selectedItem, String editedStartTime, String editedEndTime, String editedWork) {
        int selectedIndex = itemsAdapter.getPosition(selectedItem);
        if (selectedIndex == -1 || selectedIndex >= allIds.size()) {
            Toast.makeText(EditMySchedule.this, "Invalid selection", Toast.LENGTH_SHORT).show();
            return;
        }

        String documentId = allIds.get(selectedIndex);

        // Create updates map
        Map<String, Object> updates = new HashMap<>();
        updates.put("startTime", editedStartTime);
        updates.put("endTime", editedEndTime);
        updates.put("work", editedWork);

        // Update schedule in Firestore
        db.collection("schedules").document(documentId)
                .update(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditMySchedule.this, "Schedule updated successfully", Toast.LENGTH_SHORT).show();
                            Refresh(); // Refresh schedule list
                        } else {
                            Toast.makeText(EditMySchedule.this, "Failed to update schedule", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    // Method to refresh schedule list
    // Method to refresh schedule list
    public void Refresh() {
        itemsAdapter.clear();
        allIds.clear();

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getApplicationContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            // Redirect to login activity or handle accordingly
            return;
        }

        // Set user UID
        userUid = currentUser.getUid();

        // Query Firestore for schedules of the logged-in user on the selected day
        db.collection("schedules")
                .whereEqualTo("day", selectedDay)
                .whereEqualTo("userUid", userUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String startTime = document.getString("startTime");
                                String endTime = document.getString("endTime");
                                String work = document.getString("work");

                                String scheduleItem = startTime + " - " + endTime + " > " + work;
                                itemsAdapter.add(scheduleItem);
                                allIds.add(document.getId());
                            }
                            itemsAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(EditMySchedule.this, "Failed to fetch schedules: " + task.getException(), Toast.LENGTH_SHORT).show();
                            // Log the error for debugging purposes
                            Log.e("Firestore", "Error fetching schedules", task.getException());
                        }
                    }
                });
    }

}
