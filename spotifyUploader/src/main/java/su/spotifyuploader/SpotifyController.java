package su.spotifyuploader;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import su.spotifyuploader.clients.AuthorizationClient;
import su.spotifyuploader.clients.SongListsClient;
import su.spotifyuploader.clients.SpotifyClient;
import su.spotifyuploader.models.PlaylistBuild;
import su.spotifyuploader.models.SpotifyUser;
import su.spotifyuploader.repositories.SpotifyRepository;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;


@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    private final SpotifyClient spotifyClient;
    private final SongListsClient songListsClient;
    private final AuthorizationClient authorizationClient;
    private final Environment environment;
    private final SpotifyRepository spotifyRepository;

    private String authorizationCodeRenewal;
    private String spotifyState;

    public SpotifyController(SpotifyClient spotifyClient, Environment environment, SongListsClient songListsClient, AuthorizationClient authorizationClient, SpotifyRepository spotifyRepository) {
        this.spotifyClient = spotifyClient;
        this.songListsClient = songListsClient;
        this.authorizationClient = authorizationClient;
        this.spotifyRepository = spotifyRepository;
        this.environment = environment;
    }

    //todo: create route to create playlist from internal microservice
    //  this get the userId from the database
    //  should return link to spotify playlist on
    //  in description auto time
    //todo right service for spotifyuserdatabase access
    //todo: authorzation check
    //todo: authorise route save to db

    @GetMapping("/get_playlist/{internalId}")
    public ResponseEntity<PlaylistBuild> getPlaylistById(
            @PathVariable(value = "internalId") int internalId,
            @RequestHeader("Authorization") String authorization) {
        if (!authorizationClient.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PlaylistBuild playlist = songListsClient.getPlaylistById(internalId, authorization);

        if (playlist != null) {
            return ResponseEntity.ok(playlist);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/create_playlist")
    public ResponseEntity<?> createPlaylist(
            @RequestParam("accessToken") String accessToken,
            @RequestParam("userId") String userId,
            @RequestParam("playlistName") String playlistName,
            @RequestParam("description") String description,
            @RequestParam("isPublic") boolean isPublic,
            @RequestHeader("Authorization") String authorization
    ) {
        if (!authorizationClient.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
            @RequestParam("state") String state) {
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

            AuthorizationCodeCredentials authorizationCodeCredentials = spotifyApi.authorizationCode(authorizationCode)
                    .build()
                    .execute();

            String userid;
            try {
                userid = authorizationClient.getUserId(authorizationCodeRenewal);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed internal autorisation.");            }

            String accessToken = authorizationCodeCredentials.getAccessToken();
            spotifyRepository.findAll().forEach(spotifyUser -> {
                if (spotifyUser.getUserId().equals(userid)) {
                    spotifyRepository.delete(spotifyUser);
                }
            });
            spotifyRepository.save(new SpotifyUser(userid, accessToken));

            return ResponseEntity.status(HttpStatus.OK)
                    .body("Authorization successful. Access Token: " + authorizationCodeCredentials.getAccessToken());
        } catch (IOException | SpotifyWebApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to obtain tokens from Spotify.");
        } catch (org.apache.hc.core5.http.ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/authorize")
    public String authorizeSpotify(@RequestHeader("Authorization") String authorization) {
        if (!authorizationClient.isAuthorizationValid(authorization)) {
            return "redirect:/";
        }
        authorizationCodeRenewal = authorization;
        spotifyState = UUID.randomUUID().toString(); // Generate and store the state
        String spotifyAuthorizeUrl = spotifyClient.buildAuthorizeUrl(spotifyState);

        // Redirect the user to the Spotify authorization page
        return "redirect:" + spotifyAuthorizeUrl;
    }

    private String getUserId(){
        return spotifyRepository.findSpotifyUserByUserId("userId").get(0).getUserId();
    }
}
