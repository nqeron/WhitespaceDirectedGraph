#Speedy Loop - Directed Graph Queries
Assessment for Whitespace
Developer: Noah Fields, noah@fields.name

## Runnable:
WhitespaceDirectedGraph-1.1-jar-with-dependencies.jar

## To Run:
`java -jar WhitespaceDirectedGraph-1.1-jar-with-dependencies.jar graph.json requests.json`

### graph.json
This is a file that contains the network/graph that is being worked with:
all the edges and their corresponding weights.

Format:
`[{"from": "A", "to": "B", "weight":  5},
{"from": "B", "to": "C", "weight":  4},]`

Each edge has a "from" (String) , a "to" (String), and a weight (Integer).
The upper bound of the number of edges is correlated with the memory available.

### requests.json
This is a file that contains the 'requests' made to the network, which will result in a response for each request
in the order in which they are submitted.

There are three types of requests:

- "dist" - find the distance of a given route
    - inputs:
        - List of Nodes: "A", "B", "C" ...
    - output: distance of route or error message if no such route is found
- "routes": find the number of routes that starts at one node and ends in another, given a minimum and maximum number of
    distance or trips
    - inputs: 
        - start node
        - end node
        - minimum
        - maximum
        - trips/distance
- "minDist" - total distance of shortest trip between two nodes
    - inputs
        - start node
        - end node
    
Examples:

` [{"type":  "dist", "args":  {"route":  ["A", "B", "C"]}},
{"type": "routes", "args":  {"from":  "C", "to":  "C", "min": 0, "max": 3, "type":  "trips"}},
{"type": "minDist", "args":  {"from":  "A", "to":  "C"}}]`
