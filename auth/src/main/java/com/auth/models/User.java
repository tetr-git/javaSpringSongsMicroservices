package com.auth.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usertable")
public class User {

    @Id
    private String userId;

    private String password;

    private String firstName;

    private String lastName;

    public User() {
    }

    public User(String userId, String password, String firstName, String lastName) {
        this.userId = userId;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String setFirstName(String firstName) {
        return this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User [ userId=" + userId + "password=" + password + ", firstName=" + firstName + ", lastName=" + lastName + "]";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
