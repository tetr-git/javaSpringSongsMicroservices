package su.spotifyuploader.clients;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.io.IOException;
import java.net.URI;

@Component
public class SpotifyClient {

    private final String clientId;

    private final URI redirectUri;

    private final SpotifyApi spotifyApi;

    public SpotifyClient(@Value("${spotify.client.id}") String clientId,
                         @Value("${spotify.client.secret}") String clientSecret,
                         @Value("${spotify.api.url}") URI spotifyApiUrl,
                         @Value("${spotify.redirect.uri}") URI redirectUri)
    {
        this.clientId = clientId;
        this.redirectUri = redirectUri;

        // Initialize Spotify API
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(spotifyApiUrl) // You can set this to your microservice URL
                .build();
    }

    public Playlist createPlaylist(String accessToken, String userId, String playlistName, String description, boolean isPublic) throws IOException, SpotifyWebApiException, ParseException, SpotifyWebApiException {
        // Set the access token obtained from Spotify's OAuth2 flow
        spotifyApi.setAccessToken(accessToken);

        // Create a new playlist

        return spotifyApi
                .createPlaylist(userId, playlistName)
                .public_(isPublic)
                .description(description)
                .build()
                .execute();
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

