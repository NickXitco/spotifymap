import java.io.*;
import java.util.*;

public class ZZero {
    private static final String inputDatabase = "databases/database_sorted.txt";
    private static final String inputArtists = "databases/artist_with_genres_sorted.txt";
    private static final String outputFile = "layers/z0.txt";

    public static void main(String[] args) throws IOException {
        BufferedReader bfDatabase = new BufferedReader(new FileReader(new File(inputDatabase)));
        BufferedReader bfArtists = new BufferedReader(new FileReader(new File(inputArtists)));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile), false));

        LinkedList<Artist> artists = getArtists(bfDatabase, bfArtists);
        HashMap<Artist, HashSet<Artist>> artistsInMap = completeZ0(artists);
        printArtists(artistsInMap, bfWriter);

    }

    private static void printArtists(HashMap<Artist, HashSet<Artist>> artistsInMap, BufferedWriter bfWriter) {
    }

    private static HashMap<Artist, HashSet<Artist>> completeZ0(LinkedList<Artist> artists) {
        HashMap<Artist, HashSet<Artist>> artistsInMap = new HashMap<>();
        HashSet<HashSet<Artist>> connectedComponents = getAllConnectedComponents(artistsInMap);

        for (int i = 0; (i < 500 || (connectedComponents.size() == 1 && artistsInMap.size() > 1)); i++) {
            Artist artist = artists.get(i);
            Artist connectedArtist = getConnectedArtist(artist, artistsInMap);

            if (connectedArtist == null) {
                HashSet<Artist> cc = new HashSet<>();
                cc.add(artist);
                artistsInMap.put(artist, cc);
                connectedComponents.add(cc);
            } else {
                artistsInMap.put(artist, artistsInMap.get(connectedArtist));
            }
        }


        //TODO find the minimum spanning tree

        //TODO count the leaves in the tree. If there are less than (artistsInMap.size() - 500) leaves, then add more artists, maybe another 500? idk man lol

        //TODO sort those leaves by followers and remove them from the map in ascending follower order until we have 500 artists that are connected :) yay


        return artistsInMap;
    }

    private static HashSet<HashSet<Artist>> getAllConnectedComponents(HashMap<Artist, HashSet<Artist>> artistsInMap) {
        HashSet<HashSet<Artist>> connectedComponents = new HashSet<>();

        for (Artist a : artistsInMap.keySet()) {
            connectedComponents.add(artistsInMap.get(a));
        }

        return connectedComponents;
    }

    private static Artist getConnectedArtist(Artist artist, HashMap<Artist, HashSet<Artist>> artistsInMap) {
        for (Artist a : artist.relatedArtists) {
            if (artistsInMap.containsKey(a)) {
                return a;
            }
        }
        return null;
    }

    private static LinkedList<Artist> getArtists(BufferedReader bfDatabase, BufferedReader bfArtists) throws IOException {
        HashMap<String, Artist> artists = new HashMap<>();

        String line;

        while ((line = bfArtists.readLine()) != null) {
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
        return artistLinkedList;
    }

    private static class Artist {
        String name;
        String id;
        int followers;
        int popularity;
        LinkedList<String> genres;
        LinkedList<Artist> relatedArtists;

        Artist(String name, String id, int followers, int popularity, LinkedList<String> genres) {
            this.name = name;
            this.id = id;
            this.followers = followers;
            this.popularity = popularity;
            this.genres = genres;
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
}
