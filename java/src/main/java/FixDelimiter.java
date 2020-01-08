import java.io.*;

public class FixDelimiter {
    public static void main(String args[]) throws IOException {
        BufferedReader bfReaderA = new BufferedReader(new FileReader(new File("databases/artists.txt")));
        BufferedReader bfReaderB = new BufferedReader(new FileReader(new File("databases/artists_with_genres.txt")));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File("databases/artists_with_genres_delimiter_fixed.txt"), false));

        String lineA;
        String lineB;
        while ((lineA = bfReaderA.readLine()) != null) {
            lineB = bfReaderB.readLine();
            String[] splitA = lineA.split("\\|", 2);
            String[] splitB = lineB.split("\\|");

            int i = 1;

            while (i < splitB.length) {
                if (splitA[1].equals(splitB[i])) { break; }
                i++;
            }


            String correctName = splitA[0];
            bfWriter.write(correctName + "|");

            while (i < splitB.length) {
                bfWriter.write(splitB[i]);
                if (i != splitB.length - 1) {
                    bfWriter.write("|");
                }
                i++;
            }

            bfWriter.write("\n");
        }






    }
}
