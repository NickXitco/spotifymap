import java.io.*;
import java.util.Comparator;
import java.util.LinkedList;

public class SortArtists {
    private static final String inputFile = "databases/artists_with_genres.txt";
    private static final String outputFile = "databases/artists_with_genres_sorted.txt";

    public static void main(String[] args) throws IOException {
        BufferedReader bfReader = new BufferedReader(new FileReader(new File(inputFile)));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile), false));

        LinkedList<Artist> artists = new LinkedList<Artist>();

        String line;
        while ((line = bfReader.readLine()) != null) {
            String[] split = line.split("\\|");
            int followers = Integer.parseInt(split[2]);
            artists.add(new Artist(followers, line));
        }

        artists.sort(Comparator.comparingInt((Artist a) -> a.followers).reversed());

        for (Artist a:
                artists) {
            bfWriter.write(a.line + "\n");
        }


        bfReader.close();
        bfWriter.close();
    }

    private static class Artist {
        int followers;
        String line;

        Artist(int followers, String line) {
            this.followers = followers;
            this.line = line;
        }
    }
}
