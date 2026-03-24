package io.github.coderodde.finance.loan.simplifcation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * This class models a financial loan graph.
 */
public class FinancialGraph implements Iterable<FinancialGraphNode> {

    /**
     * This map maps name of the nodes to respective node objects.
     */
    private final Map<String, FinancialGraphNode> nodeMap = new HashMap<>();

    /**
     * This list contains all the nodes currently stored in this graph.
     */
    private final List<FinancialGraphNode> nodeList = new ArrayList<>();

    /**
     * This variable caches the amount of edges in this graph.
     */
    protected int edgeAmount;

    /**
     * This variable caches the total flow of this graph
     * (sum of edge weights).
     */
    protected long flow;

    /**
     * Constructs an empty graph.
     */
    public FinancialGraph() {}

    /**
     * Constructs a graph with the same amount of nodes as in
     * <code>copy</code> with the same node names. Edges are copied as well,
     * and their respective arc weights are set correspondingly.
     *
     * @param copy the graph to copy.
     */
    public FinancialGraph(FinancialGraph copy) {
        Map<FinancialGraphNode, FinancialGraphNode> map = new HashMap<>(nodeList.size());

        for (FinancialGraphNode node : copy) {
            FinancialGraphNode newNode = new FinancialGraphNode(node);
            map.put(node, newNode);
            add(newNode);
        }

        for (FinancialGraphNode node : copy) {
            FinancialGraphNode tail = map.get(node);

            for (FinancialGraphNode borrower : node) {
                FinancialGraphNode head = map.get(borrower);
                tail.connectToBorrower(head);
                tail.setWeightTo(head, node.getWeightTo(borrower));
            }
        }

        this.edgeAmount = copy.edgeAmount;
    }

    public FinancialGraph copyWithoutArcs() {
        FinancialGraph result = new FinancialGraph();

        for (FinancialGraphNode node : this) {
            result.add(new FinancialGraphNode(node));
        }

        return result;
    }

    @Override
    public String toString() {
        return "[" + nodeList.size() + " nodes, " + edgeAmount + " edges, " + 
               flow + " flow]";
    }

    public String toDetailedString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (FinancialGraphNode node : this) {
            FinancialGraph.this.toDetailedString(node, stringBuilder);
        }

        return stringBuilder.toString();
    }

    private static void toDetailedString(
            FinancialGraphNode node, 
            StringBuilder stringBuilder) {
        stringBuilder.append(node).append("\n");

        for (FinancialGraphNode child : node) {
            stringBuilder.append("    Node ")
                         .append(child.getName())
                         .append(", w = ")
                         .append(node.getWeightTo(child))
                         .append("\n");
        }
    }

    /**
     * Adds a node to this graph if not already in this graph.
     *
     * @param node the node to add.
     */
    public void add(FinancialGraphNode node) {
        Objects.requireNonNull(node, "The input node is null.");

        if (node.ownerGraph != this && node.ownerGraph != null) {
            throw new IllegalArgumentException(
                    "The input node belongs to some another graph.");
        }

        if (nodeMap.containsKey(node.getName())) {
            // Already in this graph.
            return;
        }

        node.clear();
        node.ownerGraph = this;
        nodeMap.put(node.getName(), node);
        nodeList.add(node);
    }

    /**
     * Checks whether a node is included in this graph.
     *
     * @param node the node to query.
     * @return <code>true</code> if this graph contains the query node;
     * <code>false</code> otherwise.
     */
    public boolean contains(FinancialGraphNode node) {
        Objects.requireNonNull(node, "The input node is null.");

        if (node.ownerGraph != this) {
            return false;
        }

        return nodeMap.containsKey(node.getName());
    }

    /**
     * Returns a node with index <code>index</code>.
     *
     * @param index the node index.
     * @return the node at index <code>index</code>.
     */
    public FinancialGraphNode get(int index) {
        return nodeList.get(index);
    }

    /**
     * Returns a node with name <code>name</code>.
     *
     * @param name the name of the query node.
     * @return the node with name <code>name</code>; <code>null</code>
     * otherwise.
     */
    public FinancialGraphNode get(String name) {
        return nodeMap.get(name);
    }

    /**
     * Removes a node from this graph if present.
     *
     * @param node the node to remove.
     */
    public void remove(FinancialGraphNode node) {
        if (node.ownerGraph != this) {
            throw new IllegalArgumentException(
                    "The input node does not belong to this graph.");
        }

        if (nodeMap.containsKey(node.getName())) {
            nodeMap.remove(node.getName());
            nodeList.remove(node);
            node.clear();
            node.ownerGraph = null;
        }
    }

    /**
     * Returns the amount of nodes in this graph.
     *
     * @return the amount of nodes in this graph.
     */
    public int size() {
        return nodeList.size();
    }

    /**
     * Returns the amount of edges in this graph.
     *
     * @return the amount of edges in this graph.
     */
    public int getEdgeAmount() {
        return edgeAmount;
    }

    /**
     * Returns the total flow (sum of all edge weights) of this graph.
     *
     * @return the total flow of this graph.
     */
    public long getTotalFlow() {
        return flow;
    }

    /**
     * Returns an iterator over this graph's nodes.
     *
     * @return an iterator over this graph's nodes.
     */
    @Override
    public Iterator<FinancialGraphNode> iterator() {
        return new NodeIterator();
    }

    public boolean isEquivalentTo(FinancialGraph g) {
        if (this.size() != g.size()) {
            return false;
        }

        for (FinancialGraphNode node : this) {
            FinancialGraphNode tmp = g.get(node.getName());

            if (tmp == null) {
                return false;
            }

            if (node.getEquity() != tmp.getEquity()) {
                return false;
            }
        }

        return true;
    }

    /**
     * This class implements the iterators over this graph's nodes.
     */
    private class NodeIterator implements Iterator<FinancialGraphNode> {

        /**
         * The actual iterator.
         */
        private Iterator<FinancialGraphNode> iterator = nodeList.iterator();

        /**
         * The last returned node.
         */
        private FinancialGraphNode lastReturned;

        /**
         * Returns <code>true</code> if and only if there is more
         * nodes to iterate.
         *
         * @return <code>true</code> if and only if there is more nodes to
         * iterate.
         */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Returns the next node or throws
         * <code>NoSuchElementException</code> if there is no more
         * nodes to iterate.
         *
         * @return the next node.
         */
        @Override
        public FinancialGraphNode next() {
            return (lastReturned = iterator.next());
        }

        /**
         * Removes the current node from this graph.
         */
        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new NoSuchElementException(
                        "There is no current node to remove.");
            }

            iterator.remove();
            nodeMap.remove(lastReturned.getName());
            lastReturned.clear();
            lastReturned = null;
        }
    }

    private void checkNodeBelongsToThisGraph(FinancialGraphNode node) {
        if (node.ownerGraph != this) {
            throw new IllegalArgumentException(
                    "The input node " + node + 
                            " does not belong to this graph.");
        }
    }
}
