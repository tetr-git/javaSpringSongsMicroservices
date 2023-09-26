package com.songlists.clients;

import com.songlists.models.Song;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
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

    public Song getSongByUuid(String songUuid, String authToken) {
        String jsonRequestBody = "{\"songUuid\": \"" + songUuid + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Set content type to JSON
        headers.set("Authorization", authToken); // Set the Authorization header

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);

        try {
            ResponseEntity<Song> response = restTemplate.exchange(
                    songsServiceUrl + "/songms/songs/uuid/" + songUuid,
                    HttpMethod.GET,
                    requestEntity,
                    Song.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.out.println("Response Status Code: " + response.getStatusCodeValue());
                System.out.println("Response Body: " + response.getBody());
                return null;
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Song getSongByDetails(
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam("label") String label,
                @RequestParam("releaseYear") int releaseYear,
            @RequestHeader("Authorization") String authToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken); // Set the Authorization header

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Song> response = restTemplate.exchange(
                    songsServiceUrl + "/songms/songs/find?title=" + title +
                            "&artist=" + artist +
                            "&label=" + label +
                            "&releaseYear=" + releaseYear,
                    HttpMethod.GET,
                    requestEntity,
                    Song.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.out.println("Response Status Code: " + response.getStatusCodeValue());
                System.out.println("Response Body: " + response.getBody());
                return null;
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            ex.printStackTrace();
            return null;
        }
    }




}
