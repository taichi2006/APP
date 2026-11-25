package com.example.model;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String imagePath;

    public User() {
        this("", "", "", "");
    }

    public User(String firstName, String lastName, String email) {
        this(firstName, lastName, email, "");
    }

    public User(String firstName, String lastName, String email, String imagePath) {
        this.firstName = firstName != null ? firstName : "";
        this.lastName = lastName != null ? lastName : "";
        this.email = email != null ? email : "";
        this.imagePath = imagePath != null ? imagePath : "";
    }

    public User copy(String imagePath) {
        return new User(this.firstName, this.lastName, this.email, imagePath);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
