package de.htwb.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "songLists")
public class SongList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User userId;

    private String name;

    private boolean isPrivate;
    @JsonIgnore
    @ManyToMany (cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @JoinTable(name = "songlist_song",
            joinColumns = {@JoinColumn( name = "songlistid", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn( name = "songid", referencedColumnName = "id")})
    private Set<Song> songs;

    public SongList(Integer id, User userId, String name, boolean isPrivate, Set<Song> songs) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.isPrivate = isPrivate;
        this.songs = songs;
    }

    public SongList(Boolean isPrivate, String name, User userId, Set<Song> songs) {
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

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
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