import java.io.*;

public class GetArtists {
    private static final String inputFile = "databases/database.txt";
    private static final String outputFile = "databases/artists.txt";

    public static void main(String[] args) throws IOException {
        BufferedReader bfReader = new BufferedReader(new FileReader(new File(inputFile)));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile)));
        int c = 1;
        String line;
        while ((line = bfReader.readLine()) != null) {
            String[] split = line.split("\\|");
            bfWriter.write(split[0] + "|" + split[1] + "\n");
            if (c % 100000 == 0) {
                System.out.println(c + " lines read.");
            }
            c++;
        }
        bfReader.close();
        bfWriter.close();
    }
}
