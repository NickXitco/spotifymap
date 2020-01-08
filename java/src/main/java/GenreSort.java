import java.io.*;
import java.util.*;

public class GenreSort {
    private static final String inputFile = "artists_with_genres.txt";
    private static final String outputFile = "genre_stats.txt";

    public static void main(String[] args) throws IOException {
        BufferedReader bfReader = new BufferedReader(new FileReader(new File(inputFile)));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile), false));

        HashMap<String, Integer> genres = new HashMap<String, Integer>();

        String line;
        while ((line = bfReader.readLine()) != null) {
            String[] split = line.split("\\|");
            for (int i = 4; i < split.length; i++) {
                if (genres.containsKey(split[i])) {
                    genres.put(split[i], genres.get(split[i]) + 1);
                } else {
                    genres.put(split[i], 1);
                }
            }
        }

        bfReader.close();

        List<Map.Entry<String, Integer>> genreList = new LinkedList<Map.Entry<String, Integer>>(genres.entrySet());

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
