import java.io.*;
import java.util.*;
import java.util.function.ToIntFunction;

public class ZZero {
    private static final String inputDatabase = "databases/database_sorted.txt";
    private static final String inputArtists = "databases/artists_with_genres_sorted.txt";
    private static final String outputFile = "layers/z0.txt";

    public static void main(String[] args) throws IOException {
        BufferedReader bfDatabase = new BufferedReader(new FileReader(new File(inputDatabase)));
        BufferedReader bfArtists = new BufferedReader(new FileReader(new File(inputArtists)));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(outputFile), false));

        System.out.println("Getting artists from database...");
        LinkedList<Artist> artists = getArtists(bfDatabase, bfArtists);
        System.out.println("Creating clusters...");
        HashMap<Artist, Element> artistsInMap = completeZ0(artists);
        printArtists(artistsInMap, bfWriter);

    }

    private static void printArtists(HashMap<Artist, Element> artistsInMap, BufferedWriter bfWriter) {
    }

    private static HashMap<Artist, Element> completeZ0(LinkedList<Artist> artists) {
        HashMap<Artist, Element> artistsInMap = new HashMap<>();
        HashMap<Artist, LinkedList<Element>> guestList = new HashMap<>();

        int noArtists = artists.size();
        int i = 0;
        for (Artist artist : artists) {
            //Neat statistics you could try later:
            //The element e added is always going to be the root or child to the root, so you can easily get the size of the component each time
            //You could also try adding in every artist to an element and then unionizing everything together at once instead of inline.
            Element e = artistsInMap.get(artist);
            if (e == null) {
                e = new Element(artist);
                artistsInMap.put(artist, e);
            }

            for (Artist r : artist.relatedArtists) {
                Element er = artistsInMap.get(r);
                if (er != null) {
                    union(e, er);
                } else {
                    LinkedList<Element> rInvites = guestList.get(r);
                    if (rInvites == null) {
                        rInvites = new LinkedList<>();
                        rInvites.add(e);
                        guestList.put(r, rInvites);
                    } else {
                        rInvites.add(e);
                    }
                }
            }

            LinkedList<Element> invitations = guestList.get(artist);
            guestList.remove(artist);
            if (invitations != null) {
                for (Element invite : invitations) {
                    union(e, invite);
                }
            }

            i++;
            if ((i) % (noArtists/ 100) == 0) {
                System.out.println(((i) / (noArtists / 100)) + "% complete! (" + i + "/" + noArtists + ")");
            }
        }

        LinkedList<LinkedList<Artist>> flattenedComponents = flatten(artistsInMap);
        flattenedComponents.sort(Comparator.comparingInt((ToIntFunction<LinkedList>) LinkedList::size).reversed());

        //TODO find the minimum spanning tree
        //TODO count the leaves in the tree. If there are less than (artistsInMap.size() - 500) leaves, then add more artists, maybe another 500? idk man lol
        //TODO sort those leaves by followers and remove them from the map in ascending follower order until we have 500 artists that are connected :) yay
        return artistsInMap;
    }

    private static LinkedList<LinkedList<Artist>> flatten(HashMap<Artist, Element> artistsInMap) {
        HashMap<Artist, LinkedList<Artist>> listify = new HashMap<>();
        for (Element e : artistsInMap.values()) {
            Element root = find(e);
            LinkedList<Artist> list = listify.get(root.artist);
            if (list == null) {
                list = new LinkedList<>();
                list.add(e.artist);
                listify.put(root.artist, list);
            } else {
                list.add(e.artist);
            }
        }
        return new LinkedList<>(listify.values());
    }

    private static Element find(Element e) {
        if (e.parent != e) {
            e.parent = find(e.parent);
        }
        return e.parent;
    }

    private static void union(Element x, Element y) {
        Element xRoot = find(x);
        Element yRoot = find(y);

        if (xRoot == yRoot) return;

        if (xRoot.size < yRoot.size) {
            Element temp1 = xRoot;
            xRoot = yRoot;
            yRoot = temp1;
        }

        yRoot.parent = xRoot;
        xRoot.size = xRoot.size + yRoot.size;
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

    private static class Element {
        Artist artist;
        Element parent;
        int size;

        Element(Artist a) {
            this.artist = a;
            this.parent = this;
            this.size = 1;
        }

    }
}
