package su.spotifyuploader.models;

public class Song {
    private Integer id;
    private String title;
    private String artist;
    private String label;
    private int released;

    public Song(Integer id, String title, String artist, String label, int released) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.label = label;
        this.released = released;
    }

    public Song(String title, String artist, String label, int released) {
        this.id = null;
        this.title = title;
        this.artist = artist;
        this.label = label;
        this.released = released;
    }

    public String getArtist() {
        return artist;
    }

    public String getLabel() {
        return label;
    }

    public Integer getId() {
        return id;
    }

    public int getReleased() {
        return released;
    }

    public String getTitle() {
        return title;
    }

}