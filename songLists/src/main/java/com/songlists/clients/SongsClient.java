package com.songlists.clients;

import com.songlists.models.Song;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
@Component
public class SongsClient {

    private final RestTemplate restTemplate;

    private final String songsServiceUrl;

    public SongsClient(RestTemplate restTemplate, @Value("${songs.service.url}") String songsServiceUrl) {
        this.restTemplate = restTemplate;
        this.songsServiceUrl = songsServiceUrl;
    }

    public Song getSongByUuid(String songUuid) {
        String jsonRequestBody = "{\"songUuid\": \"" + songUuid + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Set content type to JSON

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);

        try {
            ResponseEntity<Song> response = restTemplate.exchange(
                    songsServiceUrl + "/songMS/songs/uuid/" + songUuid,
                    HttpMethod.GET,
                    requestEntity,
                    Song.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // If the response is OK, the token is valid
                return response.getBody();
            } else {
                // Handle different HTTP response status codes here (e.g., 401, 403, etc.)
                // You can log the response body for more details.
                System.out.println("Response Status Code: " + response.getStatusCodeValue());
                System.out.println("Response Body: " + response.getBody());
                return null;
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Handle exceptions here (e.g., if the song microservice is unavailable)
            ex.printStackTrace();
            return null;
        }
    }

    //find song by json with titel,artist,label,release year
    public Song getSongByDetails(String titel, String artist, String label, int releaseYear) {
        String jsonRequestBody = "{\"titel\": \"" + titel + "\", " +
                "\"artist\": \"" + artist + "\", " +
                "\"label\": \"" + label + "\", " +
                "\"releaseYear\": \"" + releaseYear + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Set content type to JSON

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);

        try {
            ResponseEntity<Song> response = restTemplate.exchange(
                    songsServiceUrl + "/songMS/songs/find/",
                    HttpMethod.GET,
                    requestEntity,
                    Song.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // If the response is OK, the token is valid
                return response.getBody();
            } else {
                // Handle different HTTP response status codes here (e.g., 401, 403, etc.)
                // You can log the response body for more details.
                System.out.println("Response Status Code: " + response.getStatusCodeValue());
                System.out.println("Response Body: " + response.getBody());
                return null;
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Handle exceptions here (e.g., if the song microservice is unavailable)
            ex.printStackTrace();
            return null;
        }
    }


}
