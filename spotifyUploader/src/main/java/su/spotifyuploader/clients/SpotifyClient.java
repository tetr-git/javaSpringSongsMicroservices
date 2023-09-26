package su.spotifyuploader.clients;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import su.spotifyuploader.configs.RestTemplateConfig;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@Component
public class SpotifyClient {

    private final String clientId;
    private final URI redirectUri;
    private final SpotifyApi spotifyApi;
    private final RestTemplateConfig restTemplate;

    public SpotifyClient(@Value("${spotify.client.id}") String clientId,
                         @Value("${spotify.client.secret}") String clientSecret,
                         @Value("${spotify.api.url}") URI spotifyApiUrl,
                         @Value("${spotify.redirect.uri}") URI redirectUri,
                         RestTemplateConfig restTemplate)
    {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.restTemplate = restTemplate;

        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(spotifyApiUrl) // You can set this to your microservice URL
                .build();
    }

    public String searchTrackAndGetId(String accessToken, String title, String artist, Integer releaseYear) {
        spotifyApi.setAccessToken(accessToken);

        StringBuilder queryBuilder = new StringBuilder();
        if (title != null && !title.isEmpty()) {
            queryBuilder.append("track:").append(title).append(" ");
        }
        if (artist != null && !artist.isEmpty()) {
            queryBuilder.append("artist:").append(artist).append(" ");
        }
        if (releaseYear != null) {
            queryBuilder.append("year:").append(releaseYear).append(" ");
        }

        String queryString = queryBuilder.toString().trim();
        SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(queryString).build();

        try {
            List<Track> tracks = List.of(searchTracksRequest.execute().getItems());

            if (!tracks.isEmpty()) {
                return tracks.get(0).getId();
            } else {
                return null;
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Playlist createPlaylist(String accessToken, String userId, String playlistName, boolean isPublic) throws IOException, SpotifyWebApiException, ParseException, SpotifyWebApiException {
        spotifyApi.setAccessToken(accessToken);

        return spotifyApi
                .createPlaylist(userId, playlistName)
                .public_(isPublic)
                .build()
                .execute();
    }

    public String addTracksToPlaylist(String accessToken, String playlistId, List<String> trackUris) throws IOException, SpotifyWebApiException, ParseException {
        spotifyApi.setAccessToken(accessToken);

        trackUris.replaceAll(s -> "spotify:track:" + s);
        URI url = URI.create("https://api.spotify.com/v1/playlists/" + playlistId + "/tracks");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<List<String>> requestEntity = new HttpEntity<>(trackUris, headers);
        ResponseEntity<SnapshotResult> response = restTemplate.postForEntity(url, requestEntity, SnapshotResult.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return Objects.requireNonNull(response.getBody()).getSnapshotId();
        } else {
            return null;
        }
    }

    public String buildAuthorizeUrl(String state)   {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setRedirectUri(redirectUri)
                .build();

        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("user-library-read user-library-modify playlist-modify-public")
                .state(state)
                .build();

        URI authorizationUri = authorizationCodeUriRequest.execute();
        return authorizationUri.toString();
    }
}

