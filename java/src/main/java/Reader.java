import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;



public class Reader {

    private static final int ALPHACODE_LENGTH = 22;
    public static final int MAX_NUM_ELEMENTS = 42;
    private BufferedReader bfReader;

    Reader(String input) throws FileNotFoundException {
        File file = new File(input);
        this.bfReader = new BufferedReader(new FileReader(file));
    }

    public void closeReader() throws IOException {
        this.bfReader.close();
    }

    public Queue<String> readInUnexploredArtists() throws IOException {
        HashSet<String> unexploredHashSet = new HashSet<String>();
        HashSet<String> exploredHashSet = new HashSet<String>();
        Queue<String> unexploredArtists = new LinkedList<String>();

        boolean nullFlag = false;

        String line;
        while ((line = this.bfReader.readLine()) != null) {

            //Splits "ABBA|0LcJLqbBmaGUft1e9Mm8HV" into two strings.
            String[] split = line.split("\\|");

            //Every ID in the first column has already been explored and therefore
            //needs to be kept track of so that we don't look at artists twice.
            exploredHashSet.add(split[1]);

            //If we have a non-even number of elements in split, that would mean
            //that there is some error in that line, since every name must be
            //paired with an id. Similarly, there are at most 1 + 20 artists in
            //each line, and anything longer is wrong.
            if (split.length % 2 != 0 || split.length > MAX_NUM_ELEMENTS) {
                System.err.println("BAD LINE: " + line);
                nullFlag = true;
            }

            //Some explored artists have 0 related artists, so we don't need
            //to check them. Besides, they would throw an error here.

            //ABBA|0LcJLqbBmaGUft1e9Mm8HV|Boney M.|54R6Y0I7jGUCveDTtI21nb|Cher|72OaDtakiy6yFqkt4TsiFt
            //  0             1              2                 3            4            5
            if (split.length > 2) {
                for (int i = 3; i < split.length; i+=2) {
                    //Every Spotify ID is a 22 * 62bit (1364 bit) alphanumeric
                    //code, and anything that isn't 22 in length is wrong.
                    if (split[i].length() != ALPHACODE_LENGTH) {
                        System.err.println("BAD LINE: " + line);
                        nullFlag = true;
                    }
                    unexploredHashSet.add(split[i]);
                }
            }
        }

        if (nullFlag) {
            return null;
        }

        //Remove every first column artist, if it exists, from all the artists
        unexploredHashSet.removeAll(exploredHashSet);
        //Add all of the artists to an actual, ready-to-go queue.
        unexploredArtists.addAll(unexploredHashSet);
        return unexploredArtists;
    }
}
