import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.BadGatewayException;
import com.wrapper.spotify.exceptions.detailed.ServiceUnavailableException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsRelatedArtistsRequest;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Spotify Web API crawler that creates a partial network of all connected
 * Spotify artists.
 *
 * I made the decision when making this for the second time that, instead of
 * making this a sort of, loop system, where the found artists are printed to
 * the file and immediately added back into the queue that I would change it to
 * more of a, each run of this program will dive one layer deeper in the
 * breadth-first search style network that this is creating.
 *
 * This allows you to sort of see how the network expands, and it allows means
 * that instead of the program taking 72 straight hours to run, it's more like
 * 5 or 6 sessions of 8-12 hours.
 */
public class Crawler {
    //My conjecture is that as long as this first artist isn't an absolute
    //nobody, this first node shouldn't matter, and that the major spotify
    //artist network as a whole is fully connected.
    private static final String firstArtist = "0LcJLqbBmaGUft1e9Mm8HV"; //ABBA
    private static final String inputFile = "databases/database.txt";
    private static final String outputFile = "databases/database.txt";
    private static final int PROGRESS_STEP = 100;
    private static final int TOKEN_REFRESH_RATE = 10000;


    /**
     * Synchronized getRelatedArtists request. Silently handles known exceptions
     * that we can deal with seamlessly.
     * @return An array of artist objects related to the head artist
     * @throws InterruptedException For handling interruptions during sleep time.
     */
    private static Artist[] getRelated(String headArtistID, CrawlerApi crawlerApi) throws InterruptedException {
        GetArtistsRelatedArtistsRequest relatedRequest = crawlerApi.spotifyApi.getArtistsRelatedArtists(headArtistID).build();
        try {
            return relatedRequest.execute();
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            return null;
        } catch (SpotifyWebApiException e) {
            if (handleError(e)) return getRelated(headArtistID, crawlerApi);
            return null;
        }
    }

    /**
     * Synchronized getArtist request. Used to verify ID is legitimate as well
     * as get the name of the artist to properly output to our file.
     * @return An artist object based on the given ID.
     * @throws InterruptedException For handling interruptions during sleep time.
     */
    private static Artist getArtist(String headArtistID, CrawlerApi crawlerApi) throws InterruptedException {
        GetArtistRequest artistRequest = crawlerApi.spotifyApi.getArtist(headArtistID).build();
        try {
            return artistRequest.execute();
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            return null;
        } catch (SpotifyWebApiException e) {
            if (handleError(e)) return getArtist(headArtistID, crawlerApi);
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

    /**
     * Main function. Sets up writer and reader and runs loops.
     * @param args Unused.
     * @throws IOException For file not found.
     * @throws InterruptedException For handling interruptions during sleep time.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        CrawlerApi crawlerApi = new CrawlerApi();
        Reader reader = new Reader(inputFile);

        System.out.println("Starting reader...");
        Queue<String> idQueue = reader.readInUnexploredArtists();
        reader.closeReader();

        if (idQueue == null) {
            //If we've encountered a bad line in reading, quit the program.
            return;
        }

        if (idQueue.isEmpty()) {
            //If we have no artists to explore, explore the preset first artist.
            idQueue.add(firstArtist);
        }

        int startingSize = idQueue.size();
        System.out.println("Queue Size: " + startingSize);

        Writer writer = new Writer(outputFile);
        while (!idQueue.isEmpty()) {
            if (addLine(idQueue, crawlerApi, writer)) {
                printProgress(startingSize, idQueue);
            } else {
                writer.closeWriter();
                System.err.println("Unhandled Error! Quitting...");
                return;
            }

            //Get a new token every 10000 cycles, which should be under an hour
            if ((startingSize - idQueue.size()) % TOKEN_REFRESH_RATE == 0) {
                crawlerApi = new CrawlerApi();
            }
        }
        writer.closeWriter();
    }

    /**
     * Gets required information from web request and prints to file.
     * @param idQueue The queue of artist IDs from input file.
     * @param crawlerApi The API we're using to grab requests.
     * @param writer The writer pointing to the output file.
     * @return False if an error that we cannot handle occurs.
     * @throws InterruptedException For handling interruptions during sleep time.
     * @throws IOException For file not found.
     */
    private static boolean addLine(Queue<String> idQueue, CrawlerApi crawlerApi, Writer writer) throws InterruptedException, IOException {
        String headArtistID = idQueue.poll();

        Artist headArtist = getArtist(headArtistID, crawlerApi);
        if (headArtist == null) {
            System.err.println(headArtistID);
            return false;
        }

        Artist[] relatedArtists = getRelated(headArtistID, crawlerApi);
        if (relatedArtists == null) {
            System.err.println(headArtistID);
            return false;
        }

        writer.writeLine(headArtist, relatedArtists);
        return true;
    }

    /**
     * Prints the progress every 100 writes.
     * @param startingSize The initial size of the idQueue.
     * @param idQueue The current queue of artist IDs.
     */
    private static void printProgress(int startingSize, Queue<String> idQueue) {
        int count = startingSize - idQueue.size();
        if (count % PROGRESS_STEP == 0) {
            System.out.print(count + " of " + startingSize + " (");
            System.out.println((float) (count) / (startingSize) * PROGRESS_STEP + "%) Completed");
        }
    }
}
