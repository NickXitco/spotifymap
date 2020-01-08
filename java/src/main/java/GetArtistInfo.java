import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.BadGatewayException;
import com.wrapper.spotify.exceptions.detailed.ServiceUnavailableException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class GetArtistInfo {
    private static final String inputFile = "artists.txt";
    private static final String outputFile = "artists_with_genres.txt";
    private static final int TOKEN_REFRESH_RATE = 10000;

    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedReader bfReader = new BufferedReader(new FileReader(new File(inputFile)));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile), false));

        LinkedList<String[]> artists = new LinkedList<String[]>();

        String line;
        while ((line = bfReader.readLine()) != null) {
            String[] split = line.split("\\|");
            String[] pair = new String[2];
            pair[0] = split[0];
            pair[1] = split[1];
            artists.add(pair);
        }
        getArtists(artists, bfWriter);
        bfReader.close();
        bfWriter.close();
    }

    private static void getArtists(LinkedList<String[]> artists, BufferedWriter bfWriter) throws InterruptedException, IOException {
        CrawlerApi api = new CrawlerApi();
        String[] artistIDs = new String[artists.size()];
        int numArtists = artists.size();

        int a = 0;
        while (artists.size() > 0) {
            artistIDs[a] = artists.removeFirst()[1];
            a++;
        }

        int i = 0;
        while (i < numArtists) {
            String[] idSubset = Arrays.copyOfRange(artistIDs, i, i + 20);
            Artist[] severalArtists = getSeveralArtists(idSubset, api);
            if (severalArtists != null) {
                for (Artist artist: severalArtists) {
                    if (artist != null) {
                        bfWriter.write(artist.getName()   + "|" + artist.getId());
                        bfWriter.write("|" + artist.getFollowers().getTotal());
                        bfWriter.write("|" + artist.getPopularity());
                        for (String genre: artist.getGenres()) {
                            bfWriter.write("|" + genre);
                        }
                        bfWriter.write("\n");
                    }
                    printProgress(numArtists, i);
                    i++;
                    if (i % 100000 == 0) {
                        api = new CrawlerApi(); //Token Refresher
                    }
                }
            }
        }
    }

    private static void printProgress(int startingSize, int count) {
        if (count % 1000 == 0) {
            System.out.print(count + " of " + startingSize + " (");
            System.out.println((float) (count) / (startingSize) * 100 + "%) Completed");
        }
    }

    private static Artist[] getSeveralArtists(String[] artistIDs, CrawlerApi crawlerApi) throws InterruptedException {
        GetSeveralArtistsRequest artistsRequest = crawlerApi.spotifyApi.getSeveralArtists(artistIDs).build();
        try {
            return artistsRequest.execute();
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            return null;
        } catch (SpotifyWebApiException e) {
            if (handleError(e)) return getSeveralArtists(artistIDs, crawlerApi);
            return null;
        }
    }

    /**
     * Handles errors thrown by API requests.
     * @param e The API error to be handled.
     * @return true if the error has been handled, false otherwise.
     * @throws InterruptedException For handling interruptions during sleep time.
     */
    private static boolean handleError(SpotifyWebApiException e) throws InterruptedException {
        if (e instanceof TooManyRequestsException) {
            int r = ((TooManyRequestsException) e).getRetryAfter();
            TimeUnit.SECONDS.sleep(2 * r);
        } else if (e instanceof ServiceUnavailableException) {
            System.err.println("ServiceUnavailableException. Retrying...");
        } else if (e instanceof BadGatewayException) {
            System.err.println("BadGatewayException. Retrying in 1 minute...");
            TimeUnit.SECONDS.sleep(60);
        } else {
            System.err.println("Unhandled Error: " + e.getMessage() + e.getClass());
            return false;
        }
        return true;
    }

}
