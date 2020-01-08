import java.io.*;
import java.util.*;

public class SortTopGenreArtists {
    private static final String inputFile = "top_artists.txt";
    private static final String outputFile = "top_artists_sorted.txt";

    public static void main(String[] args) throws IOException {
        BufferedReader bfReader = new BufferedReader(new FileReader(new File(inputFile)));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile), false));

        String topArtistLine;

        HashMap<String, Integer> topArtistsMap = new HashMap<String, Integer>();

        int c = 0;

        while ((topArtistLine = bfReader.readLine()) != null) {
            String[] split = topArtistLine.split("\\|");
            try {
                topArtistsMap.put(topArtistLine, Integer.parseInt(split[3]));
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(topArtistLine);
                System.out.println(c);
            }
            c++;
        }

        bfReader.close();



        List<Map.Entry<String, Integer>> genreList = new LinkedList<Map.Entry<String, Integer>>(topArtistsMap.entrySet());

        Collections.sort(genreList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o1.getValue().compareTo(o2.getValue()));
            }
        });

        Collections.reverse(genreList);

        for (Map.Entry entry: genreList) {
            bfWriter.write(entry.getKey() + "|" + entry.getValue() + "\n");
        }

        bfWriter.close();
    }
}
