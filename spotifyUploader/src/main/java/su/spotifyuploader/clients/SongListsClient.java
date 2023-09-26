package su.spotifyuploader.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import su.spotifyuploader.models.PlaylistBuild;
import su.spotifyuploader.models.Song;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SongListsClient {

    private final RestTemplate restTemplate;
    private final String authServiceUrl;
    private final String songPlaylistsServiceUrl;

    public SongListsClient(RestTemplate restTemplate,
                           @Value("${auth.service.url}") String authServiceUrl,
    @Value("${songplaylists.service.url}") String songPlaylistsServiceUrl){
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
        this.songPlaylistsServiceUrl = songPlaylistsServiceUrl;
    }

    public PlaylistBuild getPlaylistById(int internalId, String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);

        // Build the URL to the microservice endpoint
        URI url = UriComponentsBuilder
                .fromUriString(songPlaylistsServiceUrl)
                .path("/songms/song_lists/{id}")
                .buildAndExpand(internalId)
                .toUri();
        // Pass the HttpHeaders with the Authorization header in the requestEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        // Make an HTTP GET request to the microservice
        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Object.class);
        //ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

            Integer id = (Integer) responseBody.get("id");
            Boolean isPrivate = (Boolean) responseBody.get("isPrivate");
            String ownerId = (String) responseBody.get("ownerId");
            String name = (String) responseBody.get("name");

            List<Map<String, Object>> songListData = (List<Map<String, Object>>) responseBody.get("songList");
            List<Song> songs = new ArrayList<>();

            for (Map<String, Object> songData : songListData) {
                Integer songId = (Integer) songData.get("id");
                String title = (String) songData.get("title");
                String artist = (String) songData.get("artist");
                String label = (String) songData.get("label");
                Integer released = (Integer) songData.get("released");
                Song song = new Song(songId, title, artist, label, released);
                songs.add(song);
            }

            return new PlaylistBuild(id, isPrivate, ownerId, name, songs);
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
