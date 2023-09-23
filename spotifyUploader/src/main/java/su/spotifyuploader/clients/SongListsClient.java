package su.spotifyuploader.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import su.spotifyuploader.models.Playlist;
import su.spotifyuploader.models.Song;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SongListsClient {

    private final RestTemplate restTemplate;

    private final String authServiceUrl;

    private final String songplaylistsServiceUrl;

    public SongListsClient(RestTemplate restTemplate,
                           @Value("${auth.service.url}") String authServiceUrl,
    @Value("${songplaylists.service.url}") String songplaylistsServiceUrl){
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
        this.songplaylistsServiceUrl = songplaylistsServiceUrl;
    }

    public Playlist getPlaylistById(int internalId, String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);

        // Build the URL to the microservice endpoint
        URI url = UriComponentsBuilder
                .fromUriString(songplaylistsServiceUrl)
                .path("/song_lists/{id}")
                .buildAndExpand(internalId)
                .toUri();

        // Make an HTTP GET request to the microservice
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            // Assuming the response body is a Map<String, Object> as shown in your example
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

            // Extract the relevant data from the response
            Integer id = (Integer) responseBody.get("id");
            Boolean isPrivate = (Boolean) responseBody.get("isPrivate");
            String ownerId = (String) responseBody.get("ownerId");
            String name = (String) responseBody.get("name");

            // Extract the songList array
            List<Map<String, Object>> songListData = (List<Map<String, Object>>) responseBody.get("songList");
            List<Song> songs = new ArrayList<>();

            // Map the songListData to Song objects
            for (Map<String, Object> songData : songListData) {
                Integer songId = (Integer) songData.get("id");
                String title = (String) songData.get("title");
                String artist = (String) songData.get("artist");
                String label = (String) songData.get("label");
                Integer released = (Integer) songData.get("released");
                // Assuming you have a constructor for Song that takes these values
                Song song = new Song(songId, title, artist, label, released);
                songs.add(song);
            }

            // Create a Playlist object and return it
            return new Playlist(id, isPrivate, ownerId, name, songs);
        } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            // Handle unauthorized error
            // You can throw an exception or return an error response
        } else {
            // Handle other HTTP status codes as needed
            // You can throw an exception or return an error response
        }

        // Return null or an appropriate response in case of errors
        return null;
    }

}
