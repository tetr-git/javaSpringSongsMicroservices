package su.spotifyuploader.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;

import java.net.URI;
import java.util.List;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    public ResponseEntity<SnapshotResult> postForEntity(URI url, HttpEntity<List<String>> requestEntity, Class<SnapshotResult> snapshotResultClass) {
        // Create an instance of RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Perform the POST request
        ResponseEntity<SnapshotResult> response = restTemplate.postForEntity(url, requestEntity, snapshotResultClass);

        // Return the response
        return response;
    }
}
