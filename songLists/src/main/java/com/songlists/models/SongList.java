package com.songlists.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "songLists")
public class SongList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String userId;

    private String name;

    private boolean isPrivate;

    @JsonIgnore
    @OneToMany(mappedBy = "songList", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SongListSong> songListSongs = new HashSet<>();

    public SongList(Boolean isPrivate, String name, String userId, Set<SongListSong> songListSongs) {
        this.userId = userId;
        this.name = name;
        this.isPrivate = isPrivate;
        this.songListSongs = songListSongs;
    }

    //construtor with empty set
    public SongList(Boolean isPrivate, String name, String userId) {
        this.userId = userId;
        this.name = name;
        this.isPrivate = isPrivate;
    }

    public SongList() {
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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
    public Set<SongListSong> getSongListSongs() {
        return songListSongs;
    }

    public void setSongListSongs(Set<SongListSong> songListSongs) {
        this.songListSongs = songListSongs;
    }

    public Set<UUID> getSongsUuid() {
        Set<UUID> songsUuid = new HashSet<>();
        for (SongListSong songListSong : songListSongs) {
            songsUuid.add(songListSong.getSongId());
        }
        return songsUuid;
    }


}
