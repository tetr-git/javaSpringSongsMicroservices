package su.spotifyuploader.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthorizationClient {
    private final RestTemplate restTemplate;
    private final String authServiceUrl;
    public AuthorizationClient(RestTemplate restTemplate, @Value("${service.url}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    public boolean isAuthorizationValid(String token) {
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
                return true;
            } else {
                System.out.println("Response Status Code: " + response.getStatusCode());
                System.out.println("Response Body: " + response.getBody());
                return false;
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUserId(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    authServiceUrl + "/auth/get-userid",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.out.println("Response Status Code: " + response.getStatusCode());
                System.out.println("Response Body: " + response.getBody());
                return null;
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            e.printStackTrace();
            return null;
        }
    }
}

