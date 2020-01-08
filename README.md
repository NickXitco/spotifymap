# SpotifyWebGame


## [1/31/2019, 1:04PM]

Alright I don't have time to code anything right now, but I'm having a bad day,
and I want to get something done that feels like work. Basically the current plan
for this website is to have a few things:

- A navigable "cloud" "galaxy" thing of the entire Spotify Network. 
    - because of the limitations of modern computers/phones and most web browsers, we can't render all 1.x million nodes and ~20 million edges at once, that's just not feasible and it would probably look dumb too
    - I forget if I ended up completing this statistic or heuristic or whatever you want to call it but the proposed solution for this was to basically define clusters of the entire network. Possibly for each genre, possibly for each "hub" of nodes. That, OR
    
    - We could define effectively a Z layer as well. So instead of having all 1.x million nodes on a single XY plane, we create a few, maybe idk 16 buckets to place each of the nodes in?
    - So let's say that the top 500 artists or so (everything seemed to be able to handle that number fine) are on Z=0. Alright? And then we put maybe, 5000 artists ***THAT ARE CONNECTED TO THE Z=0 LAYER**
        - See that's the rub here, is that we can't just take the top 500 artists
        - Randomly, and then just basically sort everything by follower counts, no  we have to come up with a better heuristic than that, something that takes into account linkages and such, so that when you generate the Z=1 layer, you're seeing deeper connections of what is on the Z=0 layer
        
        - So maybe what we first do is create the clusters, and define maybe 50-100 large groups of nodes, then take the best artist from each of those nodes.
        
        - Defining best here might be something like:
            - What is the artist, from each cluster, that has the most followers AND has a direct connection to another cluster with a large number of followers. We want these clusters to ideally be interconnected, Maybe with some strings of lower-level artists connecting them.
    - So then we take the best nodes from each of the clusters, those are Z=0, Then we continue to cluster down lower and lower, taking the top 50-100 of each Z=n_x layer where n is the layer and x is the cluster and making them into new Z=n+1_x layers.

    - Because, in all reality, if you're looking at a Z=3_x cluster, you're probably not going to want to see *every* Z=3 cluster, so we don't have to render everything at once, all we have to do is render Z=0, then Z=0 + Z=1_x + Z=2_x_y, and so on. We could probably have some of the things derender based on the camera's position in the canvas but that's a later optimization issue.

- A side panel of artist information

- A filter/search system

- A "WikiGame"-style random search game.
    - Basically something like:

    - You get two random artists, probably in the top 75000 or so artists
    
    - You're given the position of the two in the graph, like it zooms out to show it and you basically have to quickly route from one artist to the another.
    
    - After you finish, it shows you your time, steps taken, genres visted, other interesting stats, as well as the best possible path, which (I think, could be calculated by a BFS that starts running client-side at the same time as the client starts.

    - It's a sort of "race-the-CPU" sort of thing. We could also kinda capitalize on that and have you *Actually* race your computer. I'd have to see how long the BFS takes.
    - Maybe I could implement different levels of difficulty, if the BFS isn't too fast.
    
    - Depending on how long it takes, we could probably store best paths in an offline database to just retrieve them on completion? idk that sounds dumb I'll have to see how long everything takes on a normal system though.

    - I think if I did this right, it will be the crown-jewel of this website.
		
anyways, gtg, this felt good to right, I'm excited to get to work, not really sure where to start,
I think a good place would be implementing the graph and search system in Python.
		
## [1/31/2019, 8:57PM]
		
We learned about Steiner Trees today in Approximation Algorithms, which, conviniently, might just be
a great system for handling the Z=0 layer, as I'm calling it now. I'm not quite sure what to define
as an edge's weight right now, but I think it's going to be based on the change in Z value of each
node to the other nodes y'know.

But the Z value itself is going to be based on (I think) two main characterstics:  
* The number of followers the artist has  
* The distance to other nodes on different Z levels  
	
Now that seems like circular logic so let me try to just work my thoughts out here. Let's suppose,
we create our Z=0 layer with a bunch like 500 artists. We'll try to distribute it so that it's not
400 pop/hiphop artists and 1 rock artist or something. We then set up a quota for each subsequent Z layer.
Let's say it's Q(Z=0) = 500, Q(Z=1) = 5000, Q(Z=2) = 50000, Q(Z=3) = 500000, Q(Z=4) = inf. that actually might not
be good logically because we have an issue where a LOT of artists are stragglers, and the most popular artists
are clustered together so it may need to look more like

* Q(Z=2) = 5000  
* Q(Z=3) = 10000  
* Q(Z=4) = 25000  
* Q(Z=5) = 50000  
* Q(Z=6) = 100000  
* Q(Z=7) = 250000  
* Q(Z=8) = 500000  
* Q(Z=9) = inf  

something like that. We'll see. Anyways, basically the algorithm would be, after defining a Z=0, and going through
the list of artists in descending follower-count order:

- While there are still un-layered artists:
    - If the artist is connected to an artist in the layer n-1, add it to layer n
    - Else, add it to another list that we go through before checking never-before-seen artists
    - If the size of n is equal to Q(n), n++
		
That way, while artists towards the top layer also tend to have the most followers, it's more important, and crucial
actually that the artist has a connection up to the top layer. That way, we don't have dangling artists.

So then an edges weight would just be Z_lower - Z_upper + 1, maybe with a term to make artists in lower layers have a
penalty to encourage algorithms to search along top level artists and "drill-down" rather than trying to tunnel through
really low level artists and tunnel back up, but who knows, that could be the fastest method.
				
Once we have our Z=0 layer picked out, we can run a Steiner Tree approximation with the Z=0 nodes as our terminals and
the entire graph as our graph, and what we would get is effectively the best highways connecting these big superclusters

## [1/3/2020]

NEW YEAR NEW ME

Let's finally finish this damn project that's haunted me for years. So.

Step 0. Get all the artists on spotify and their 20 connections.

Step 1. Construct the Z<sub>0</sub> layer and here's (one way) how:

1. The goal of this algorithm is to find the 500 artists that maximize the sum of followers in the layer, while also
maintaining connectivity throughout the layer.

2. Initialize the Z<sub>0</sub> with the 500 most followed (or some other metric) artists.

3. If the graph is connected, then wow this job is a lot easier then I though, however it's likely not, so:

4. Continue to add artists (in descending follower (or other metric) order) until the graph is connected.

5. Find the "redundant vertices" of that set, i.e., the vertices that, if removed, would leave a still-connected graph.
   
6. This can be done by creating the fully spanning forest of the graph and selecting any vertices of degree 1, leaves.

7. Delete redundant vertices until the size of the layer is 500.

8. Edge case is if (Current Layer Size - Redundant Vertices) > 500, would have to add more artists in step 4 to counter.

Step 2. Figure out the rest of the layers (to be thought about more)
	
