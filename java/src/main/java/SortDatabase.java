import java.io.*;
import java.util.*;

public class SortDatabase {
    private static final String inputFileSorted = "databases/artists_with_genres_sorted.txt";
    private static final String inputFileToBeSorted = "databases/database.txt";
    private static final String outputFile = "databases/database_sorted.txt";

    public static void main(String[] args) throws IOException {
        BufferedReader bfReaderSorted = new BufferedReader(new FileReader(new File(inputFileSorted)));
        BufferedReader bfReaderToBeSorted = new BufferedReader(new FileReader(new File(inputFileToBeSorted)));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile), false));


        HashMap<String, String> idToLine = new HashMap<>();

        String line;
        while ((line = bfReaderToBeSorted.readLine()) != null) {
            String[] split = line.split("\\|");
            String id = split[1];
            idToLine.put(id, line);
        }

        bfReaderToBeSorted.close();

        while ((line = bfReaderSorted.readLine()) != null) {
            String[] split = line.split("\\|");
            String id = split[1];
            bfWriter.write(idToLine.get(id) + "\n");
        }

        bfReaderSorted.close();
        bfWriter.close();
    }
}
