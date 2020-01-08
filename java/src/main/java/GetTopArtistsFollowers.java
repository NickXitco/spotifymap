import java.io.*;
import java.util.*;

public class GetTopArtistsFollowers {
    private static final String inputFile = "top_artists_sorted.txt";
    private static final String outputFile = "top_artists_followers_sorted.csv";

    public static void main(String[] args) throws IOException {
        BufferedReader bfReader = new BufferedReader(new FileReader(new File(inputFile)));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile), false));

        String topArtistLine;


        while ((topArtistLine = bfReader.readLine()) != null) {
            String[] split = topArtistLine.split("\\|");
            bfWriter.write(split[3] + "\n");
        }
        bfReader.close();
        bfWriter.close();
    }
}
