package io.github.coderodde.finance.loan.simplifcation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class implements a loan graph node.
 *
 * @author Rodion "coderodde" Efremov
 * @version 1.61 (Sep 2, 2021)
 * @since 1.6
 */
public class FinancialGraphNode implements Iterable<FinancialGraphNode> {

    /**
     * This is the name of a node. Also, this is treated as the ID of a 
     * node. (Two nodes <code>u</code>, <code>v</code> are considered as 
     * equal if and only if <code>u.name.equals(v.name)</code>.
     */
    private final String name;

    /**
     * This is the map from lender to loan amount.
     */
    private final Map<FinancialGraphNode, Long> in = new HashMap<>();

    /**
     * This is the map from borrower to resources lent.
     */
    private final Map<FinancialGraphNode, Long> out = new HashMap<>();

    /**
     * The graph owning this node, if any.
     */
    protected FinancialGraph ownerGraph;

    /**
     * If equity is below 0, this node owes, and, vice versa, if positive,
     * is eligible to receive cash.
     */
    private long equity;

    /**
     * Constructs a new node.
     * 
     * @param name the name of the new node.
     */
    public FinancialGraphNode(String name) {
        this.name = name;
    }

    /**
     * Copy-constructs a node.
     * 
     * @param copy the node to share the identity with.
     */
    public FinancialGraphNode(FinancialGraphNode copy) {
        this(copy.name);
    }

    /**
     * Gets the name of this node.
     * 
     * @return the name of this node.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the number of borrowers.
     * 
     * @return the number of borrowers.
     */
    public int getNumberOfBorrowers() {
        return this.out.size();
    }

    /**
     * Returns the number of lenders.
     * 
     * @return the number of lenders. 
     */
    public int getNumberOfLenders() {
        return this.in.size();
    }

    /**
     * Returns the weight of the directed arc {@code (this, borrower)}.
     * 
     * @param borrower the head node of the arc.
     * @return the arc weight.
     */
    public long getWeightTo(FinancialGraphNode borrower) {
        checkBorrowerNotNull(borrower);
        checkBorrowerBelongsToThisGraph(borrower);
        checkBorrowerExists(borrower);
        return this.out.get(borrower);
    }

    /**
     * Sets the weight of the directed arc {@code (this, borrower)}.
     * 
     * @param borrower the head node of the arc.
     * @param weight the arc weight.
     */
    public void setWeightTo(FinancialGraphNode borrower, long weight) {
        checkBorrowerNotNull(borrower);
        checkBorrowerBelongsToThisGraph(borrower);
        checkBorrowerExists(borrower);

        long oldWeight = this.out.get(borrower);

        this.out.put(borrower, weight);
        borrower.in.put(this, weight);
        //            50 = 100 - 50
        long weightDelta = weight - oldWeight;
        ownerGraph.flow += weightDelta;
        equity += weightDelta;
        borrower.equity -= weightDelta;
    }

    /**
     * Connects this node to a borrower.
     * 
     * @param borrower the borrower.
     */
    public void connectToBorrower(FinancialGraphNode borrower) {
        checkBorrowerNotNull(borrower);
        checkBorrowerBelongsToThisGraph(borrower);

        if (out.containsKey(borrower)) {
            return;
        }

        if (borrower.ownerGraph != this.ownerGraph) {
            borrower.ownerGraph = this.ownerGraph;
            borrower.clear();
        } 

        out.put(borrower, 0L);
        borrower.in.put(this, 0L);
        ownerGraph.edgeAmount++;
    }

    public boolean isConnectedTo(FinancialGraphNode borrower) {
        return out.containsKey(borrower);
    }

    /**
     * Remove the borrower of this node.
     * 
     * @param borrower the borrower to remove.
     */
    public void removeBorrower(final FinancialGraphNode borrower) {
        checkBorrowerNotNull(borrower);

        if (borrower.ownerGraph != this.ownerGraph) {
            return;
        }

        if (out.containsKey(borrower)) {
            long w = out.get(borrower);
            out.remove(borrower);
            borrower.in.remove(this);
            this.equity -= w;
            borrower.equity += w;
            ownerGraph.edgeAmount--;
            ownerGraph.flow -= w;
        }
    }

    /**
     * Clear this node from all loans coming in and out.
     */
    public void clear() {
        Iterator<FinancialGraphNode> iterator = iterator();

        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        iterator = parentIterable().iterator();

        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    public boolean isOrphan() {
        return ownerGraph == null;
    }

    private void checkBorrowerBelongsToThisGraph(final FinancialGraphNode borrower) {
        if (borrower.ownerGraph != this.ownerGraph) {
            throw new IllegalStateException("The input borrower node " +
                    borrower + " does not belong to this graph.");
        }
    }

    /**
     * Returns the string representation of this node.
     * @return 
     */
    @Override
    public String toString() {
        return "[Node " + name + "; equity: " + getEquity() + "]";
    }

    /**
     * Returns the hash code of this node. Depends only on the name of
     * the node.
     * 
     * @return the hash code of this node. 
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns <code>true</code> if and only if the two nodes have the 
     * same name (identity).
     * 
     * @param o the object to test against.
     * @return <code>true</code> if and only if <code>o</code> is a node
     * and has the same name.
     */
    @Override
    public boolean equals(Object o) {
        return ((FinancialGraphNode) o).name.equals(this.name);
    }

    /**
     * Returns the iterator over this node's borrowers.
     * 
     * @return the iterator over this node's borrowers.
     */
    @Override
    public Iterator<FinancialGraphNode> iterator() {
        return new ChildIterator();
    }

    /**
     * Returns the iterable over this nodes lenders.
     * 
     * @return the iterable over this nodes lenders.
     */
    public Iterable<FinancialGraphNode> parentIterable() {
        return new ParentIterable();
    }

    /**
     * Return the equity of this node.
     * 
     * @return the equity of this node.
     */
    public long getEquity() {
        return this.equity;
    }

    /**
     * Checks that borrower is not null and not this.
     * 
     * @param borrower the borrower to check. 
     */
    private void checkBorrowerNotNull(final FinancialGraphNode borrower) {
        if (borrower == null) {
            throw new NullPointerException("Borrower is null.");
        }
    }

    /**
     * This class implements the iterator over this node's borrowers.
     */
    private class ChildIterator implements Iterator<FinancialGraphNode> {

        /**
         * The actual iterator.
         */
        private Iterator<FinancialGraphNode> iterator = FinancialGraphNode.this.out.keySet().iterator();

        /**
         * Holds the node last returned by <code>next</code>.
         */
        private FinancialGraphNode lastReturned;

        /**
         * Returns <code>true</code> if and only if there is more 
         * nodes to iterate.
         * 
         * @return <code>true</code> if and only if there is more nodes
         * to iterate.
         */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Returns the next node.
         * 
         * @return the next node.
         */
        @Override
        public FinancialGraphNode next() {
            return (lastReturned = iterator.next());
        }

        /**
         * Removes the node from this node's borrowers.
         */
        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new NoSuchElementException(
                        "There is no current element to remove.");
            }

            final long weight = FinancialGraphNode.this.getWeightTo(lastReturned);
            iterator.remove();
            lastReturned.in.remove(FinancialGraphNode.this);
            lastReturned = null;

            if (ownerGraph != null) {
                ownerGraph.edgeAmount--;
                ownerGraph.flow -= weight;
            }
        }
    }

    /**
     * Returns the iterable over this node's lenders.
     */
    private class ParentIterable implements Iterable<FinancialGraphNode> {

        /**
         * Returns the iterator over this node's lenders.
         * 
         * @return the iterator over this node's lenders.
         */
        @Override
        public Iterator<FinancialGraphNode> iterator() {
            return new ParentIterator();
        }
    }

    /**
     * This class implements the iterator over this node's lenders.
     */
    private class ParentIterator implements Iterator<FinancialGraphNode> {

        /**
         * The actual iterator.
         */
        private Iterator<FinancialGraphNode> iterator = FinancialGraphNode.this.in.keySet().iterator();

        /**
         * The node last returned.
         */
        private FinancialGraphNode lastReturned;

        /**
         * Return <code>true</code> if this iterator has more nodes
         * to iterate.
         * 
         * @return <code>true</code> if this iterator has more nodes to
         * iterate.
         */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Returns the next node or throws 
         * <code>NoSuchElementException</code> if there is no more
         * nodes.
         * 
         * @return the next element. 
         */
        @Override
        public FinancialGraphNode next() {
            return (lastReturned = iterator.next());
        }

        /**
         * Removes this lender from this node's lender list.
         */
        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new NoSuchElementException(
                        "There is no current element to remove.");
            }

            final long weight = lastReturned.getWeightTo(FinancialGraphNode.this);

            iterator.remove();
            lastReturned.out.remove(FinancialGraphNode.this);
            lastReturned = null;

            if (ownerGraph != null) {
                ownerGraph.edgeAmount--;
                ownerGraph.flow -= weight;
            }
        }
    }

    private void checkBorrowerExists(FinancialGraphNode borrower) {
        if (!out.containsKey(borrower)) {
            throw new IllegalArgumentException(
                    "No arc (" + this + ", " + borrower + ").");
        }
    }

    private void checkWeightDelta(final FinancialGraphNode borrower, final long weightDelta) {
        if (out.get(borrower) + weightDelta < 0L) {
            throw new IllegalArgumentException(
                    "The weight delta (" + weightDelta + 
                            ") exceeds the arc weight (" + 
                            out.get(borrower) + ").");
        }
    }
}
