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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SongListsClient {

    private final RestTemplate restTemplate;
    private final String serviceUrl;

    public SongListsClient(RestTemplate restTemplate,
                           @Value("${service.url}") String serviceUrl) {
        this.restTemplate = restTemplate;
        this.serviceUrl = serviceUrl;
    }

    public PlaylistBuild getPlaylistById(int internalId, String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);

        URI url = UriComponentsBuilder
                .fromUriString(serviceUrl)
                .path("/song_lists/song_lists/{id}")
                .buildAndExpand(internalId)
                .toUri();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Object.class);

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
        } else {
            return null;
        }
    }

    public boolean createPlaylistFromPlaylistBuild(PlaylistBuild playlistBuild, String authorization) {

        try {
            // Extract playlist details from the PlaylistBuild object
            boolean isPrivate = playlistBuild.getPrivate();
            String name = playlistBuild.getName();
            List<Song> songs = playlistBuild.getSongs();


            // Create a payload in the required format for the SongLists microservice
            Map<String, Object> songListPayload = new HashMap<>();
            songListPayload.put("isPrivate", isPrivate);
            songListPayload.put("name", name);

            List<Map<String, Object>> songsPayload = new ArrayList<>();
            for (Song song : songs) {
                Map<String, Object> songPayload = new HashMap<>();
                songPayload.put("title", song.getTitle());
                songPayload.put("artist", song.getArtist());
                songPayload.put("label", song.getLabel());
                songPayload.put("released", song.getReleased());
                songsPayload.add(songPayload);
            }

            songListPayload.put("songList", songsPayload);

            // Build the URL to the microservice endpoint
            URI url = UriComponentsBuilder
                    .fromUriString(serviceUrl)
                    .path("/song_lists/song_lists")
                    .build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorization);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create an HTTP entity with the payload and headers
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(songListPayload, headers);

            // Make an HTTP POST request to the microservice to create the playlist
            ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Object.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                return true;
            } else {
                // Handle other HTTP status codes as needed
                // You can throw an exception or return an error response
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


}
