package su.spotifyuploader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import su.spotifyuploader.clients.SpotifyClient;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;


@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    private final SpotifyClient spotifyClient;
    private final Environment environment;

    private String spotifyState;

    public SpotifyController(SpotifyClient spotifyClient, Environment environment) {
        this.spotifyClient = spotifyClient;
        this.environment = environment;
    }

    //todo: create route to create playlist from internal microservice
    //  this get the userId from the database
    //  should return link to spotify playlist on
    //  in description auto time
    //todo right service for spotifyuserdatabase access
    //todo: authorzation check
    //todo: authorise route save to db

    @PostMapping("/create_playlist")
    public ResponseEntity<?> createPlaylist(
            @RequestParam("accessToken") String accessToken,
            @RequestParam("userId") String userId,
            @RequestParam("playlistName") String playlistName,
            @RequestParam("description") String description,
            @RequestParam("isPublic") boolean isPublic
    ) {
        try {
            // Call the SpotifyClient to create a playlist on Spotify
            Playlist createdPlaylist = spotifyClient.createPlaylist(accessToken, userId, playlistName, description, isPublic);

            // You can return the created playlist information or just a success message
            return ResponseEntity.status(HttpStatus.CREATED).body("Playlist created successfully. Playlist ID: " + createdPlaylist.getId());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create a playlist on Spotify.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request or Spotify API credentials.");
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> spotifyCallback(
            @RequestParam("code") String authorizationCode,
            @RequestParam("state") String state,
            ServerHttpRequest request) {
        try {
            // Verify that the 'state' parameter from the callback matches the stored 'state'
            if (!state.equals(spotifyState)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid 'state' parameter.");
            }

            // Handle the Spotify callback by exchanging the authorization code for tokens
            // Use the Spotify API to make a POST request to obtain access and refresh tokens

            // Read Spotify client credentials and redirect URI from application.properties
            String clientId = environment.getProperty("spotify.clientId");
            String clientSecret = environment.getProperty("spotify.clientSecret");
            URI redirectUri = URI.create(Objects.requireNonNull(environment.getProperty("spotify.redirect.uri")));

            SpotifyApi spotifyApi = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRedirectUri(redirectUri)
                    .build();

            // Use the Spotify API to obtain access and refresh tokens
            AuthorizationCodeCredentials authorizationCodeCredentials = spotifyApi.authorizationCode(authorizationCode)
                    .build()
                    .execute();

            // Store tokens in application.properties
            PropertiesConfiguration config = new PropertiesConfiguration("application.properties");
            config.setProperty("spotify.access.token", authorizationCodeCredentials.getAccessToken());
            config.setProperty("spotify.refresh.token", authorizationCodeCredentials.getRefreshToken());
            config.save();

            // You can return a success message or redirect to another page
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Authorization successful. Access Token: " + authorizationCodeCredentials.getAccessToken());

        } catch (IOException | SpotifyWebApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to obtain tokens from Spotify.");
        } catch (ConfigurationException | org.apache.hc.core5.http.ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/authorize")
    public String authorizeSpotify() {
        // Build the Spotify authorization URL
        spotifyState = UUID.randomUUID().toString(); // Generate and store the state
        String spotifyAuthorizeUrl = spotifyClient.buildAuthorizeUrl(spotifyState);

        // Redirect the user to the Spotify authorization page
        return "redirect:" + spotifyAuthorizeUrl;
    }
}
