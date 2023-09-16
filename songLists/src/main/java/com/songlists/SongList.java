package com.songlists;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.UUID;
import java.util.Set;

@Entity
@Table(name = "songLists")
public class SongList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "userId", columnDefinition = "UUID")
    private UUID userId;

    private String name;

    private boolean isPrivate;


    @JsonIgnore
    @ManyToMany (cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @JoinTable(name = "songlist_song",
            joinColumns = {@JoinColumn( name = "songlistid", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn( name = "songid", referencedColumnName = "id")})
    private Set<Song> songs;


    public SongList(Integer id, UUID userId, String name, boolean isPrivate, Set<Song> songs) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.isPrivate = isPrivate;
        this.songs = songs;
    }

    public SongList(Boolean isPrivate, String name, UUID userId, Set<Song> songs) {
        this.userId = userId;
        this.name = name;
        this.isPrivate = isPrivate;
        this.songs = songs;
    }

    public SongList() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    @JsonIgnore
    public Set<Song> getSongs() {
        return songs;
    }

    public void setSongs(Set<Song> songs) {
        this.songs = songs;
    }

    public void addSong(Song song) {
        if (songs == null) {
            songs = new HashSet<>();
        }
        //songs.getSongLists().add(this);
        songs.add(song);
    }
}