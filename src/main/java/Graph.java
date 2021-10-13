import java.util.HashMap;
import java.util.HashSet;

public class Graph {
    private HashMap<String, Node> nodes = new HashMap<>();

    public void addEdge(String from, String to, Integer weight) {
        Node fromNode = nodes.getOrDefault(from, new Node(from));
        Node toNode = nodes.getOrDefault(to, new Node(to));
        fromNode.add(toNode, weight);
        nodes.put(from, fromNode);
        nodes.put(to, toNode);
    }

    public Node getNode(String string) {
        return nodes.get(string);
    }

    public void resetDistances() {
        for (Node n: nodes.values()){
            n.setDistance(Integer.MAX_VALUE);
        }
    }
}
