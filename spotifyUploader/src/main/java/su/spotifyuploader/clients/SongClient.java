package su.spotifyuploader.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import su.spotifyuploader.models.Song;

@Service
public class SongClient {

    private final RestTemplate restTemplate;
    private final String songServiceUrl;
    public SongClient(RestTemplate restTemplate,
                      @Value("${songs.service.url}") String songServiceUrl) {
        this.restTemplate = restTemplate;
        this.songServiceUrl = songServiceUrl;
    }

    public boolean createSong(Song song, String authorization) {

        String jsonPayload = "{\"title\":\"" + song.getTitle() + "\","
                + "\"artist\":\"" + song.getArtist() + "\","
                + "\"label\":\"" + song.getLabel() + "\","
                + "\"released\":" + song.getReleased() + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request entity with the Song object and headers
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    songServiceUrl + "/songs/songs",
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            return responseEntity.getStatusCode() == HttpStatus.CREATED;
        } catch (HttpClientErrorException e) {
            return e instanceof HttpClientErrorException.Conflict;
        }



    }
}
