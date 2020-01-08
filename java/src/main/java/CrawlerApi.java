import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;

public class CrawlerApi {
    private static final String clientId = "5b00769425ca43019b6072c9fe842472";
    private static final String clientSecret = "e2d374f53a964ba9afd170626289bfa3";

    public final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();
    private final ClientCredentialsRequest clientCredentialsRequest = this.spotifyApi.clientCredentials()
            .build();

    CrawlerApi() {
        this.clientCredentials_Sync();
    }

    public void clientCredentials_Sync() {
        try {
            final ClientCredentials clientCredentials = this.clientCredentialsRequest.execute();

            // Set access token for further "spotifyApi" object usage
            this.spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            System.out.println("Token Received! Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
