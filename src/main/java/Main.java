import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.json.*;

public class Main {

    public static void main(String[] args) throws Exception {
        //Read graph file
        if(args.length < 2){
            System.out.println("Not Enough Arguments - must contain graph and request files");
            throw new Exception("Not Enough Arguments");
        }

        Path graphFile = Paths.get(args[0]);
        Path requestFile = Paths.get(args[1]);
        String graphString = Files.lines(graphFile).collect(Collectors.joining());

        //load graph file in from json
        JSONArray edges = new JSONArray(graphString);
        Graph graph = new Graph();
        //for each edge
        for(int i = 0; i < edges.length(); i++){
            JSONObject edge = edges.getJSONObject(i);
            String from = edge.getString("from");
            String to = edge.getString("to");
            Integer weight = edge.getInt("weight");
            //add the edge to the graph
            graph.addEdge(from, to, weight);
        }

        //load requests:
        String requestString = Files.lines(requestFile).collect(Collectors.joining());
        //handle requests
        JSONArray requests = new JSONArray(requestString);
        List<String> results = new ArrayList<>();
        for (int i = 0; i < requests.length(); i++){
            JSONObject request = requests.getJSONObject(i);
            String type = request.getString("type");
            JSONObject cArgs = request.getJSONObject("args");
            if("dist".equals(type)){
                //calculate distance of route
                String res = String.format("CMD: %s, Args: %s, result: %s", type, cArgs,calcDist(graph, cArgs) );
                results.add(res);
            } else if("routes".equals(type)){
                //calculate # routes
                String res = String.format("CMD: %s, Args: %s, result: %s", type, cArgs,calcRoutes(graph, cArgs));
                results.add(res);
            } else if ("minDist".equals(type)){
                //calculate min distance between points
                String res = String.format("CMD: %s, Args: %s, result: %s", type, cArgs, calcShortestPath(graph, cArgs));
                results.add(res);
            } else {
                results.add(String.format("CMD: {} => I don't recognize that command", type));
            }
        }

        //print results
        results.stream().forEach(System.out::println);


    }

    //Calculates distance of specific route
    //Details are given in json, in form : {"route": ["X", "Y" ...]}
    //Returns a string that contains an integer
    private static String calcDist(Graph graph, JSONObject cArgs) {
        //args are names of nodes in route, e.g. "["A", "B", "C"]

        Integer sum = 0;
        JSONArray route = cArgs.getJSONArray("route");
        for (int i = 0; i< route.length() - 1; i++){
            Node curNode = graph.getNode(route.getString(i));
            Node nextNode = graph.getNode(route.getString(i+1));
            Integer weight = curNode.getAdjacentNodes().getOrDefault(nextNode, null);
            if (weight == null){
                return "Invalid Route!";
            }else {
                sum += weight;
            }

        }

        return ""+sum;
    }

    //Calculates the shortest path between two nodes in a graph
    //As argument takes in a json object: {"from": "X", "to": "Y"}
    // Returns: String (int) the shortest distance between the two points
    private static String calcShortestPath(Graph graph, JSONObject cArgs) {
        //get the source and target node
        Node source = graph.getNode(cArgs.getString("from"));
        Node toNode = graph.getNode(cArgs.getString("to"));

        if(source == null || toNode == null){
            return "Invalid nodes";
        }

        //The rest of this is basically Dijkstra's algorithm
        graph.resetDistances();
        source.setDistance(0);

        Set<Node> visited = new HashSet<>();
        Queue<Node> unvisited = new PriorityQueue<>(); //use a priority queue to optimize insert, manage, and extracting lowest distance

        unvisited.add(source);

        while(!unvisited.isEmpty()){
            Node curNode = unvisited.poll();
            unvisited.remove(curNode);

            if(curNode.equals(toNode) && curNode.getDistance() > 0){ //If we've hit the target node, we can stop generating the graph
                break;
            }

            for(Map.Entry<Node, Integer> adjacenctPair: curNode.getAdjacentNodes().entrySet()){
                Node adjacentNode = adjacenctPair.getKey();
                Integer adjacentWeight = adjacenctPair.getValue();
                if(!visited.contains(adjacentNode)){
                    calculateMinimumDistance(adjacentNode, adjacentWeight, curNode);
                    unvisited.add(adjacentNode);
                }
            }

            if(!(curNode.equals(source) && curNode.getDistance() <= 0)){
                visited.add(curNode);
            }
        }
        return String.format("%d", toNode.getDistance());
    }

    //Supports Dikstra's Algorithm
    //Calculates the minimum distance between a given node and the adjacent one.
    //Updates the internal distance values -- always updates 0 values
    //Returns: no return value
    private static void calculateMinimumDistance(Node adjacentNode, Integer weight, Node curNode) {
        Integer sourceDist = curNode.getDistance();
        if((sourceDist + weight < adjacentNode.getDistance()) || adjacentNode.getDistance() == 0){//allows backtracking to original node
            adjacentNode.setDistance(sourceDist + weight);
            List<Node> shortestPath = curNode.getShortestPath();
            shortestPath.add(curNode);
            adjacentNode.setShortestPath(shortestPath);
        }
    }

    //Calculates number of routes between two nodes, given a minimum and maximum trip or distance limit
    //Handles Json: {"from": "X", "to": "Y", "min": 0, "max": 7, "type": "trips"}
    //min and max are integers, type can be "trips" otherwise it assumes working with distances
    //Returns string of the total number of given routes calculated
    private static String calcRoutes(Graph graph, JSONObject cArgs) {
        //get the nodes in question
        Node fromNode = graph.getNode(cArgs.getString("from"));
        Node toNode = graph.getNode(cArgs.getString("to"));
        if (fromNode == null || toNode == null){
            return "Nodes do not exist";
        }
        //get the minimum and maximum
        int minimum = cArgs.getInt("min");
        int maximum = cArgs.getInt("max");
        if (maximum < 0 || minimum > maximum){
            return "Invalid minimum and maximum values";
        }
        //get the type -- either trips or dist
        String type = cArgs.getString("type");
        boolean trips = "trips".equalsIgnoreCase(type);


        int numRoutes = 0;
        if(trips) {
            numRoutes = calcTrips(fromNode, toNode, minimum, maximum); //calculate # of routes based on trips
        } else {
            //Calculate # of routes based on distance
            numRoutes = calcDistRoutes(fromNode, toNode, minimum, maximum);
        }


        return String.format("%d", numRoutes);
    }

    //Calculates the number of routes from node (fromNode) that terminates in node (toNode) that takes at least a distance
        //of minimum and less than maximum
    //Parameters:
    //  fromNode: Node --> the node the routes are sourced from
    //  toNode: Node --> the terminal node in a given route
    //  minimum: int --> the minimum distance a route needs to take to be counted
    //  maximum: int --> the maximum distance a route needs to take to be counted
    //Returns:
    //  numRoutes: int --> total number of routes calculated
    private static int calcDistRoutes(Node fromNode, Node toNode, int minimum, int maximum) {
        int numRoutes = 0;
        //Sets up a Queue to use for BFS, uses Map.Entry so we can keep track of current distance
        Queue<Map.Entry<Node, Integer>> nodes = new ArrayDeque<>();
        nodes.add(new AbstractMap.SimpleEntry<Node, Integer>(fromNode, 0));

        if(toNode.equals(fromNode)){ numRoutes--;} //don't count source -> source as a route

        while(!nodes.isEmpty()){
            //obtain next node
            Map.Entry<Node, Integer>  curEntry = nodes.poll();
            // get distance
            Integer curDist = curEntry.getValue();
            Node curNode = curEntry.getKey();
            //if the current node is the to node and the distance is appropriate, this is a route!
            if (curNode.equals(toNode) && (minimum <= curDist && curDist < maximum)){
                numRoutes++;
            }
            //For each adjacent node
            for(Map.Entry<Node, Integer> edge: curNode.getAdjacentNodes().entrySet()){
                Integer newDist = edge.getValue() + curDist;
                if (minimum <= newDist && newDist < maximum){ //only add nodes that are within distance parameter
                    Map.Entry<Node, Integer> newEdge = new AbstractMap.SimpleEntry<Node, Integer>(edge.getKey(), newDist);
                    nodes.add(newEdge);
                    //add edge with updated distance
                }
            }
        }
        return numRoutes;
    }

    //Calculate number of routes from one node to another within a certain number of trips
    //Parameters:
    //  fromNode: Node --> node that all routes must be sourced from
    //  toNode: Node --> node that all routes must terminate in
    //  minimum: int --> the minimum # of trips the route must take to be counted
    //  maximum: int --> the maximum # of trips the route must take to be counted
    //Returns:
    //  numRoutes: int --> number of routes calculated that fit the parameters
    private static int calcTrips(Node fromNode, Node toNode, int minimum, int maximum) {
        int numRoutes = 0;
        int curDepth = 0;
        Queue<Node> nodes = new ArrayDeque<>();
        nodes.add(fromNode);
        if (fromNode.equals(toNode)) {
            numRoutes--;
        } //If the initial node is the same as the to node, then prevent it
        //from counting the node itself as a route
        while (!nodes.isEmpty() && curDepth <= maximum) {
            int levelSize = nodes.size();
            while (levelSize-- > 0) { //as long as current tier (depth) has nodes, poll from them
                Node curNode = nodes.poll();

                if (curDepth >= minimum && curNode.equals(toNode)) { //check to see if the node we're visiting terminates
                    //a route in the correct distance
                    numRoutes++;
                }
                curNode.getAdjacentNodes().keySet().stream().forEach(node -> nodes.add(node)); //add adjacent nodes to search
            }
            curDepth++; //increase depth - this is needed to terminate loop
        }
        return numRoutes;
    }
}
