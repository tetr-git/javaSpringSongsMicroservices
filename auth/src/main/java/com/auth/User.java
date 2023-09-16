package com.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;
/*
CREATE TABLE UserTable (
    id serial PRIMARY KEY,
    user_id VARCHAR ( 50 ) NOT NULL,
    firstName VARCHAR ( 50 ) NOT NULL,
    lastName VARCHAR ( 50 ) NOT NULL,
    email VARCHAR ( 255 ) NOT NULL,
);
 */


@Entity
@Table(name = "usertable")
public class User {

    @Id
    private String userId;

    @Column(name = "perm_id", columnDefinition = "UUID")
    private UUID permId;

    private String password;

    private String firstName;

    private String lastName;

    public User() {
    }
    /*@JsonIgnore
    @OneToMany(mappedBy="userId",
            cascade=CascadeType.ALL, orphanRemoval=true, fetch =
            FetchType.EAGER)
    Set<SongList> songLists;

     */

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

    public UUID getPermId() {
        return permId;
    }

    @Override
    public String toString() {
        return "User [ userId=" + userId + "password=" + password + ", firstName=" + firstName + ", lastName=" + lastName + "]";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    /*
    public Set<SongList> getSongLists() {
        return songLists;
    }

    public void setSongLists(Set<SongList> songLists) {
        this.songLists = songLists;
    }
     */
}
