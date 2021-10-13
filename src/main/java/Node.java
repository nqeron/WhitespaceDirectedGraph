import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Node implements Comparable<Node> {

    private String name;

    private List<Node> shortestPath = new LinkedList<>();

    private Integer distance = Integer.MAX_VALUE;

    private HashMap<Node, Integer> adjacentNodes;

    public Node( String name){
        this.name = name;
        this.adjacentNodes = new HashMap<>();
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(List<Node> shortestPath) {
        this.shortestPath = shortestPath;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public void add(Node node, Integer weight){
        adjacentNodes.put(node, weight);
    }

    public HashMap<Node, Integer> getAdjacentNodes() {
        return adjacentNodes;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(name, node.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Node o) {
        return this.distance.compareTo(o.getDistance());
    }
}
