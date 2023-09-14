package com.songs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;


@Entity
@Table(name="songs")
public class Song {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String artist;
    private String label;
    private int released;
    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @JoinTable(name = "songlist_song",
            joinColumns = {@JoinColumn( name = "songid", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn( name = "songlistid", referencedColumnName = "id")})
    private Set<SongList> songLists;

    public Song (String title, String artist, String label, int released) {
        this.title = title;
        this.artist = artist;
        this.label = label;
        this.released = released;
    }

    public Song() {
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLabel() {
        return label;
    }

    public int getReleased() {
        return released;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setReleased(int released) {
        this.released = released;
    }
    @JsonIgnore
    public Set<SongList> getSongLists() {
        return songLists;
    }

    public void setSongLists(Set<SongList> songLists) {
        this.songLists = songLists;
    }

}