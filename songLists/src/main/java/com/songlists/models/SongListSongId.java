package com.songlists.models;

import java.io.Serializable;
import java.util.UUID;

public class SongListSongId implements Serializable {
    private Integer songListId;
    private UUID songId;

    //Constructors, getters, and setters

    public SongListSongId() {
    }

    public SongListSongId(Integer songListId, UUID songId) {
        this.songListId = songListId;
        this.songId = songId;
    }

    public Integer getSongListId() {
        return songListId;
    }

    public void setSongListId(Integer songListId) {
        this.songListId = songListId;
    }

    public UUID getSongId() {
        return songId;
    }

    public void setSongId(UUID songId) {
        this.songId = songId;
    }
}
