import java.io.*;

public class TopArtistInGenre {
    private static final String genres = "databases/genre_stats.txt";
    private static final String artists = "databases/artists_with_genres.txt";
    private static final String outputFile = "databases/top_artists.txt";

    public static void main(String[] args) throws IOException {
        BufferedReader genresReader = new BufferedReader(new FileReader(new File(genres)));
        BufferedReader artistsReader = new BufferedReader(new FileReader(new File(artists)));

        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile), false));


        String genreLine;
        String genre;
        String artistLine;

        while ((genreLine = genresReader.readLine()) != null) {
            String top = "";
            int topFollowers = 0;
            String[] genreSplit = genreLine.split("\\|");
            genre = genreSplit[0];
            bfWriter.write(genre + "|");
            while ((artistLine = artistsReader.readLine()) != null) {
                String[] artistSplit = artistLine.split("\\|");
                if (artistSplit.length >= 5 && artistSplit[4].equals(genre)) {
                    if (topFollowers < Integer.parseInt(artistSplit[2])) {
                        topFollowers = Integer.parseInt(artistSplit[2]);
                        top = artistLine;

                    }
                }

            }
            artistsReader = new BufferedReader(new FileReader(new File(artists)));
            bfWriter.write(top);
            bfWriter.newLine();
            bfWriter.flush();
        }

        bfWriter.close();
        genresReader.close();
        artistsReader.close();
    }
}
