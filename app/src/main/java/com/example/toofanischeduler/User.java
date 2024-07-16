package com.example.toofanischeduler;

public class User {
    private String email;
    private String name;
    private String profilePicUrl; // Add this line

    // Parameterized constructor
    public User(String email, String name, String profilePicUrl) {
        this.email = email;
        this.name = name;
        this.profilePicUrl = profilePicUrl;
    }

    // Default constructor
    public User() {
        // Default constructor with no arguments
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}
