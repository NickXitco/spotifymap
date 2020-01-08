import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ArtistStats {
    private static final String inputFile = "artists_with_genres.txt";

    public static void main(String[] args) throws IOException {
        BufferedReader bfReader = new BufferedReader(new FileReader(new File(inputFile)));

        int maxFollowers = -1;
        String maxFollowersLine = "";
        int maxPopularity = -1;
        String maxPopularityLine = "";

        int c = 0;
        String line;
        while ((line = bfReader.readLine()) != null) {
            c++;
            try {
                String[] split = line.split("\\|");
                if (Integer.parseInt(split[2]) > maxFollowers) {
                    maxFollowers = Integer.parseInt(split[2]);
                    maxFollowersLine = line;
                }
                if (Integer.parseInt(split[3]) > maxPopularity) {
                    maxPopularity = Integer.parseInt(split[3]);
                    maxPopularityLine = line;
                }
            } catch (NumberFormatException e) {
                System.err.println("Null Artist: " + c);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Out of Bounds Artist: " + c);
            }
        }
        System.out.println("Most Followers: " + maxFollowersLine);
        System.out.println("Most Popular: " + maxPopularityLine);
    }
}
