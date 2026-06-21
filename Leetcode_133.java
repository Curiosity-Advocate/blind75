import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Leetcode_133 {

    public Node cloneGraph(Node node) {

        if (node == null)
            return null;

        Queue<Node> nodesToConsider = new LinkedList<>();
        HashMap<Integer, Node> graph = new HashMap<>();

        Node root = new Node(node.val);
        graph.put(root.val, root);
        nodesToConsider.add(node);

        while (nodesToConsider.size() > 0) {
            Node currNode = nodesToConsider.poll();
            Node newCurrNode = graph.get(currNode.val);

            if (currNode.neighbors != null) {
                newCurrNode.neighbors = new ArrayList<Node>();

                for (Node neighbour : currNode.neighbors) {

                    Node newNeighbour;
                    if (!graph.containsKey(neighbour.val)) {
                        newNeighbour = new Node(neighbour.val);
                        graph.put(newNeighbour.val, newNeighbour);
                    }
                    newCurrNode.neighbors.add(graph.get(neighbour.val));
                }
            }
        }
        return root;
    }

    class Node {
        public int val;
        public List<Node> neighbors;

        public Node() {
            val = 0;
            neighbors = new ArrayList<Node>();
        }

        public Node(int _val) {
            val = _val;
            neighbors = new ArrayList<Node>();
        }

        public Node(int _val, ArrayList<Node> _neighbors) {
            val = _val;
            neighbors = _neighbors;
        }
    }
}
