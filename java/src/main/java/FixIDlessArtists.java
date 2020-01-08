import java.io.*;

public class FixIDlessArtists {
    public static void main(String args[]) throws IOException {
        BufferedReader bfReader = new BufferedReader(new FileReader(new File("databases/artists_with_genres.txt")));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File("databases/artists_with_genres_delimiter_fixed.txt"), false));

        String line;
        while ((line = bfReader.readLine()) != null) {
            String[] split = line.split("\\|");
            if (split.length >= 2) {
                bfWriter.write(line + "\n");
            }
        }
    }
}
