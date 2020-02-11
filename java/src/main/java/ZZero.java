import java.io.*;
import java.util.*;

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
        HashMap<Artist, ConnectedComponent> artistsInMap = completeZ0(artists);
        printArtists(artistsInMap, bfWriter);

    }

    private static void printArtists(HashMap<Artist, ConnectedComponent> artistsInMap, BufferedWriter bfWriter) {
    }

    private static HashMap<Artist, ConnectedComponent> completeZ0(LinkedList<Artist> artists) {
        HashMap<Artist, ConnectedComponent> artistsInMap = new HashMap<>();
        HashMap<Artist, LinkedList<ConnectedComponent>> guestList = new HashMap<>();
        HashSet<ConnectedComponent> connectedComponents = new HashSet<>();

        int noArtists = artists.size();

        int i = 0;
        for (Artist artist : artists) {//artists.size() && (i < 500 || !(connectedComponents.size() == 1 && artistsInMap.size() > 1))
            ConnectedComponent cc = new ConnectedComponent(artist);
            artistsInMap.put(artist, cc);
            LinkedList<ConnectedComponent> invitations = guestList.get(artist);
            guestList.remove(artist);
            connectedComponents.add(cc);

            if (invitations != null) {
                for (ConnectedComponent k : invitations) {
                    ConnectedComponent topK = bubbleUp(k);
                    if (topK != cc) {
                        cc.members.merge(topK.members);
                        topK.dead = true;
                        topK.beneficiary = cc;
                        connectedComponents.remove(topK);
                    }
                }
            }

            for (Artist r : artist.relatedArtists) {
                ConnectedComponent rCC = artistsInMap.get(r);
                if (rCC != null) {
                    ConnectedComponent topRCC = bubbleUp(rCC);
                    if (topRCC != cc) {
                        artistsInMap.replace(r, cc);
                        cc.members.merge(topRCC.members);
                        topRCC.dead = true;
                        topRCC.beneficiary = cc;
                        connectedComponents.remove(topRCC);
                    }
                } else {
                    if (guestList.containsKey(r)) {
                        guestList.get(r).add(cc);
                    } else {
                        LinkedList<ConnectedComponent> l = new LinkedList<>();
                        l.add(cc);
                        guestList.put(r, l);
                    }
                }
            }

            i++;
            if ((i) % (noArtists/ 100) == 0) {
                System.out.println(((i) / (noArtists / 100)) + "% complete! (" + i + "/" + noArtists + ")");
            }
        }

        ConnectedComponent largest = connectedComponents.iterator().next();
        for (ConnectedComponent cc : connectedComponents) {
            cc.cementedList = cc.members.getList();
            if (cc.cementedList.size() > largest.cementedList.size()) {
                largest = cc;
            }
        }


        //TODO find the minimum spanning tree
        //TODO count the leaves in the tree. If there are less than (artistsInMap.size() - 500) leaves, then add more artists, maybe another 500? idk man lol
        //TODO sort those leaves by followers and remove them from the map in ascending follower order until we have 500 artists that are connected :) yay
        return artistsInMap;
    }

    private static ConnectedComponent bubbleUp(ConnectedComponent k) {
        while (k.beneficiary != null) {
            k = k.beneficiary;
        }
        return k;
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
        Artist next;
        Artist prev;

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
        MembersList members;
        ConnectedComponent beneficiary; //The cc that this component gives its members to once it's been merged. This is done to massively speed up computation and keep the algorithm running in linear time.
        boolean dead = false;
        LinkedList<Artist> cementedList;

        ConnectedComponent(Artist head) {
            this.members = new MembersList(head);
        }
    }

    private static class MembersList {
        Artist head;
        Artist tail;

        MembersList(Artist a) {
            this.head = a;
            this.tail = a;
        }

        LinkedList<Artist> getList() {
            LinkedList<Artist> list = new LinkedList<>();
            Artist current = this.head;
            while (current != null) {
                list.add(current);
                current = current.next;
            }
            return list;
        }

        void add(Artist a) {
            this.tail.next = a;
            a.prev = this.tail;
            this.tail = a;
        }

        void merge(MembersList toBeMerged) {
            toBeMerged.head.prev = this.tail;
            this.tail.next = toBeMerged.head;
            this.tail = toBeMerged.tail;
            toBeMerged.head = null;
            toBeMerged.tail = null;
        }
    }

}
