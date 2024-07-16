package com.example.toofanischeduler;

public class Schedule {
    private String id; // Change id type to String
    private String day, startTime, endTime, work, email;

    public Schedule(String day, String startTime, String endTime, String work, String email) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.work = work;
        this.email = email;
    }

    public Schedule() {
        // Default constructor required by Firestore
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
