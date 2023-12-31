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
import su.spotifyuploader.clients.SongClient;
import su.spotifyuploader.clients.SongListsClient;
import su.spotifyuploader.clients.SpotifyClient;
import su.spotifyuploader.models.PlaylistBuild;
import su.spotifyuploader.models.SpotifyUser;
import su.spotifyuploader.repositories.SpotifyRepository;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    private final SpotifyClient spotifyClient;
    private final SongListsClient songListsClient;
    private final AuthorizationClient authorizationClient;
    private final SongClient songClient;
    private final Environment environment;
    private final SpotifyRepository spotifyRepository;
    private String authorizationCodeRenewal;
    private String spotifyState;

    public SpotifyController(SpotifyClient spotifyClient, SongListsClient songListsClient, AuthorizationClient authorizationClient, Environment environment, SpotifyRepository spotifyRepository, SongClient songClient) {
        this.spotifyClient = spotifyClient;
        this.songClient = songClient;
        this.songListsClient = songListsClient;
        this.authorizationClient = authorizationClient;
        this.environment = environment;
        this.spotifyRepository = spotifyRepository;
    }

    @GetMapping("/get_playlist/{internalId}")
    public ResponseEntity<?> getPlaylistById(
            @PathVariable(value = "internalId") int internalId,
            @RequestHeader("Authorization") String authorization) {
        if (!authorizationClient.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid Authorization Header");
        }
        PlaylistBuild playlist = songListsClient.getPlaylistById(internalId, authorization);

        if (playlist != null) {
            return ResponseEntity.ok(playlist);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Not Found: Playlist with ID " + internalId + " not found");
        }
    }


    @PostMapping("/save_playlist_from_spotify/{spotifyPlaylistUrl}")
    public ResponseEntity<?> savePlaylistFromSpotify(
            @PathVariable(value = "spotifyPlaylistUrl") String playlistUrl,
            @RequestHeader("Authorization") String authorization) {
        if (!authorizationClient.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid Authorization Header");
        }
        try {
            String userId = authorizationClient.getUserId(authorization);
            String accessToken =  spotifyRepository.findSpotifyUserByUserId(userId).get(0).getAccessToken();
            PlaylistBuild playlist = spotifyClient.getPlaylistWithSongs(playlistUrl, accessToken, userId);
            if (playlist != null) {
                for (int i = 0; i < playlist.getSongs().size(); i++) {
                    if (!songClient.createSong(playlist.getSongs().get(i), authorization)) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Internal Server Error: Failed to create song");
                    }
                }
                if (songListsClient.createPlaylistFromPlaylistBuild(playlist, authorization))
                    return ResponseEntity.status(HttpStatus.CREATED).build();
                else
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Internal Server Error: Failed to create playlist");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Not Found: Spotify playlist not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: An unexpected error occurred");
        }
    }

    @PostMapping("/create_playlist_on_spotify/{internalId}")
    public ResponseEntity<?> createPlaylistOnSpotify(
            @PathVariable(value = "internalId") int internalId,
            @RequestHeader("Authorization") String authorization) {
        if (!authorizationClient.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PlaylistBuild playlist;
        try {
            playlist = songListsClient.getPlaylistById(internalId, authorization);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Playlist ID not found.");
        }
        if (playlist != null) {
            try {
                String accessToken = spotifyRepository.findSpotifyUserByUserId(playlist.getInternalOwnerId()).get(0).getAccessToken();
                Playlist createdPlaylist = spotifyClient.createPlaylist(accessToken, playlist.getInternalOwnerId(), playlist.getName(), playlist.getPrivate());
                List<String> trackIds = new ArrayList<>();

                for (int i = 0; i < playlist.getSongs().size(); i++) {
                    String trackId = spotifyClient.searchTrackAndGetId(accessToken, playlist.getSongs().get(i).getTitle(), playlist.getSongs().get(i).getArtist(), playlist.getSongs().get(i).getReleased());
                    if (trackId != null) {
                        trackIds.add(trackId);
                    }
                }

                if (!trackIds.isEmpty()) {
                    spotifyClient.addTracksToPlaylist(accessToken, createdPlaylist.getId(), trackIds);

                    return ResponseEntity.status(HttpStatus.CREATED).body("Playlist created successfully. https://open.spotify.com/playlist/" + createdPlaylist.getId());
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No valid track IDs to add to the playlist.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create a playlist on Spotify.");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request or Spotify API credentials.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/callback")
    public ResponseEntity<?> spotifyCallback(
            @RequestParam("code") String authorizationCode,
            @RequestParam("state") String state) {
        try {
            if (!state.equals(spotifyState)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid 'state' parameter.");
            }

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

        return "redirect:" + spotifyAuthorizeUrl;
    }
}
