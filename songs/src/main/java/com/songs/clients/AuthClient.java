package com.songs.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthClient {
    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public AuthClient(RestTemplate restTemplate, @Value("${auth.service.url}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    public boolean isAuthorizationValid(String token) {
        // Create a JSON request body
        String jsonRequestBody = "{\"token\": \"" + token + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Set content type to JSON

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    authServiceUrl + "/auth/validate",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // If the response is OK, the token is valid
                return true;
            } else {
                // Handle different HTTP response status codes here (e.g., 401, 403, etc.)
                // You can log the response body for more details.
                System.out.println("Response Status Code: " + response.getStatusCodeValue());
                System.out.println("Response Body: " + response.getBody());
                return false;
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Handle exceptions, e.g., connection errors or invalid URL
            e.printStackTrace();
            return false;
        }
    }
}

