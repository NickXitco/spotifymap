import com.wrapper.spotify.model_objects.specification.Artist;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * More robust writing class with specific formatting options.
 */
public class Writer {

    private BufferedWriter bfWriter;

    Writer(String outputFile) throws IOException {
        File file = new File(outputFile);
        this.bfWriter = new BufferedWriter(new FileWriter(file, true));
    }

    public void writeLine(Artist headArtist, Artist[] relatedArtists) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(this.formatArtist(headArtist));

        for (Artist relatedArtist : relatedArtists) {
            sb.append("|");
            sb.append(this.formatArtist(relatedArtist));
        }
        sb.append("\n");
        this.bfWriter.write(sb.toString());
    }

    public void closeWriter() throws IOException {
        this.bfWriter.close();
    }

    public void flushWriter() throws IOException {
        this.bfWriter.flush();
    }

    /**
     * Changes any found occurrences of our delimiter to a different but similar
     * character.
     * @param artist The artist to format.
     * @return A formatted string to be printed.
     */
    private String formatArtist(Artist artist) {
        if (artist.getName().contains("|")) {
            return artist.getName().replaceAll("\\|", "‖") + "|" + artist.getId();
        }
        return artist.getName() + "|" + artist.getId();
    }
}
