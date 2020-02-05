import java.io.*;
import java.util.*;

public class ZZero {
    private static final String inputDatabase = "databases/database_sorted.txt";
    private static final String inputArtists = "databases/artists_with_genres_sorted.txt";
    private static final String outputFile = "layers/z0.txt";
    private static final int TEMP_ARTIST_CAP = 100000;

    public static void main(String[] args) throws IOException {
        BufferedReader bfDatabase = new BufferedReader(new FileReader(new File(inputDatabase)));
        BufferedReader bfArtists = new BufferedReader(new FileReader(new File(inputArtists)));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile), false));

        System.out.println("Getting artists from database...");
        LinkedList<Artist> artists = getArtists(bfDatabase, bfArtists);
        System.out.println("Creating clusters...");
        HashSet<Artist> artistsInMap = completeZ0(artists);
        printArtists(artistsInMap, bfWriter);

    }

    private static void printArtists(HashSet<Artist> artistsInMap, BufferedWriter bfWriter) {
    }

    private static HashSet<Artist> completeZ0(LinkedList<Artist> artists) {
        HashSet<Artist> artistsInMap = new HashSet<>();
        HashMap<Artist, LinkedList<ConnectedComponent>> guestList = new HashMap<>();
        LinkedList<ConnectedComponent> connectedComponents = new LinkedList<>();

        for (int i = 0; i < TEMP_ARTIST_CAP; i++) { //artists.size() && (i < 500 || !(connectedComponents.size() == 1 && artistsInMap.size() > 1))
            Artist artist = artists.get(i);
            artistsInMap.add(artist);
            LinkedList<ConnectedComponent> invitedClusters = guestList.get(artist);
            guestList.remove(artist);
            if (invitedClusters != null) {
                mergeClusters(artist, invitedClusters);
            }

            LinkedList<ConnectedComponent> relatedClusters = new LinkedList<>();
            LinkedList<Artist> newGuests = new LinkedList<>();
            for (Artist a : artist.relatedArtists) {
                if (artistsInMap.contains(a)) {
                    if (a.cc.beneficiary != artist.cc && a.cc.beneficiary != artist.cc.beneficiary) {
                        relatedClusters.add(a.cc);
                    }
                } else {
                    newGuests.add(a);
                }
            }

            if (relatedClusters.size() > 0) {
                mergeClusters(artist, relatedClusters);
            }

            if (artist.cc == null) {
                artist.cc = new ConnectedComponent(artist);
                connectedComponents.add(artist.cc);
            }

            for (Artist newGuest : newGuests) {
                LinkedList<ConnectedComponent> invitations = guestList.get(newGuest);
                if (invitations == null) {
                    LinkedList<ConnectedComponent> l = new LinkedList<>();
                    l.add(artist.cc);
                    guestList.put(newGuest, l);
                } else {
                    invitations.add(artist.cc);
                }
            }

            if ((i + 1) % (TEMP_ARTIST_CAP / 100) == 0) {
                System.out.println(((i + 1) / (TEMP_ARTIST_CAP / 100)) + "% complete!");
            }
        }


        //TODO find the minimum spanning tree

        //TODO count the leaves in the tree. If there are less than (artistsInMap.size() - 500) leaves, then add more artists, maybe another 500? idk man lol

        //TODO sort those leaves by followers and remove them from the map in ascending follower order until we have 500 artists that are connected :) yay


        return artistsInMap;
    }

    private static void mergeClusters(Artist artist, LinkedList<ConnectedComponent> invitedClusters) {
        ConnectedComponent largest = invitedClusters.getFirst();
        for (ConnectedComponent cc : invitedClusters) {
            if (cc.members.size() > largest.members.size()) {
                largest = cc;
            }
        }

        largest.members.add(artist);
        artist.cc = largest;

        for (ConnectedComponent cc : invitedClusters) {
            if (cc != largest) {
                cc.beneficiary = largest;
            }
        }
    }

    private static LinkedList<Artist> getArtists(BufferedReader bfDatabase, BufferedReader bfArtists) throws IOException {
        HashMap<String, Artist> artists = new HashMap<>();

        String line;


        while (((line = bfArtists.readLine()) != null)) {
            String[] split = line.split("\\|");

            String name = split[0];
            String id = split[1];
            int followers = Integer.parseInt(split[2]);
            int popularity = Integer.parseInt(split[3]);


            LinkedList<String> genres = new LinkedList<>();
            genres.addAll(Arrays.asList(split).subList(4, split.length));

            artists.put(id, new Artist(name, id, followers, popularity, genres));
        }

        while ((line = bfDatabase.readLine()) != null) {
            String[] split = line.split("\\|");
            String id = split[1];

            Artist a = artists.get(id);

            if (a == null) {
                System.err.println("Null key for artist " + split[0] + "|" + split[1]);
                continue;
            }

            for (int i = 3; i < split.length; i +=2) {
                Artist b = artists.get(split[i]);
                if (b != null) {
                    a.relatedArtists.add(b);
                } else {
                    b = new Artist(split[i - 1], split[i], 0, 0, null);
                    a.relatedArtists.add(b);
                    artists.put(split[i], b);
                }
            }
        }

        LinkedList<Artist> artistLinkedList = new LinkedList<>(artists.values());
        artistLinkedList.sort(Comparator.comparingInt((Artist a) -> a.followers).reversed());

        //DEBUG START
        LinkedList<Artist> smallerArtistLinkedList = new LinkedList<>();
        for (Artist a : artistLinkedList) {
            smallerArtistLinkedList.add(a);
            if (smallerArtistLinkedList.size() > TEMP_ARTIST_CAP) break;
        }
        smallerArtistLinkedList.sort(Comparator.comparingInt((Artist a) -> a.followers).reversed());
        return smallerArtistLinkedList;
        //DEBUG END

        //return artistLinkedList;
    }

    private static class Artist {
        String name;
        String id;
        int followers;
        int popularity;
        LinkedList<String> genres;
        LinkedList<Artist> relatedArtists;
        ConnectedComponent cc;

        Artist(String name, String id, int followers, int popularity, LinkedList<String> genres) {
            this.name = name;
            this.id = id;
            this.followers = followers;
            this.popularity = popularity;
            this.genres = genres;
            this.relatedArtists = new LinkedList<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            Artist artist = (Artist) o;
            return Objects.equals(this.id, artist.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id);
        }
    }


    private static class ConnectedComponent {
        HashSet<Artist> members;
        ConnectedComponent beneficiary; //The cc that this component gives its members to once it's been merged. This is done to massively speed up computation and keep the algorithm running in linear time.

        ConnectedComponent(Artist head) {
            this.members = new HashSet<>();
            this.members.add(head);
        }
    }

}
