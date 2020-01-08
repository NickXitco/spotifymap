import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RemoveDuplicates {
    public static void main(String args[]) throws IOException {
        BufferedReader bfReader = new BufferedReader(new FileReader(new File("database.txt")));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File("trimmed_database.txt"), true));

        String lastUniqueID = "";

        int c = 0;

        String line;
        while ((line = bfReader.readLine()) != null) {
            String[] split = line.split("\\|");
            if (!lastUniqueID.equals(split[1])) {
                lastUniqueID = split[1];
                bfWriter.write(line + "\n");
            } else {
                c++;
            }
        }
        System.out.println(c);
    }
}
