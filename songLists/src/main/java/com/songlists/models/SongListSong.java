package com.songlists.models;

import jakarta.persistence.*;

import java.util.UUID;


import java.io.Serializable;

@Entity
@Table(name = "songlist_song")
@IdClass(SongListSongId.class)
public class SongListSong implements Serializable {
    @Id
    @Column(name = "songListId")
    private Integer songListId;

    @Id
    @Column(name = "songId")
    private UUID songId;

    @ManyToOne
    @JoinColumn(name = "songListId", insertable = false, updatable = false)
    private SongList songList;

    public SongListSong() {
    }

    public SongListSong(Integer songListId, UUID songId) {
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

    public SongList getSongList() {
        return songList;
    }

    public void setSongList(SongList songList) {
        this.songList = songList;
    }
}

