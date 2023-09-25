package su.spotifyuploader.models;

import java.util.List;

public class PlaylistBuild {
    private Integer internalId;

    private Boolean isPrivate;

    private String internalOwnerId;

    private String name;

    private List<Song> songs;

    public PlaylistBuild(Integer internalId, Boolean isPrivate, String internalOwnerId, String name, List<Song> songs) {
        this.internalId = internalId;
        this.isPrivate = isPrivate;
        this.internalOwnerId = internalOwnerId;
        this.name = name;
        this.songs = songs;
    }

    public Integer getInternalId() {
        return internalId;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public String getInternalOwnerId() {
        return internalOwnerId;
    }

    public String getName() {
        return name;
    }

    public List<Song> getSongs() {
        return songs;
    }
}
