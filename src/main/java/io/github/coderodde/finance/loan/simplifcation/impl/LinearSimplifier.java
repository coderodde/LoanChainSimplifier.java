package io.github.coderodde.finance.loan.simplifcation.impl;

import io.github.coderodde.finance.loan.simplifcation.FinancialGraph;
import io.github.coderodde.finance.loan.simplifcation.FinancialGraphNode;
import io.github.coderodde.finance.loan.simplifcation.LoanChainSimplifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This simplifier runs in linear time. First, it ignores the nodes that have
 * zero equity. Then it gathers all nodes with positive equity and all the nodes
 * with negative equity. After that, it marches through the both of them 
 * matching the positive equity nodes with negative equity nodes. Effectively,
 * this algorithm cannot return more than {@code N - 1} arcs.
 */
public final class LinearSimplifier implements LoanChainSimplifier {

    @Override
    public FinancialGraph simplify(FinancialGraph g) {
        Objects.requireNonNull(g, "The source graph is null.");
        
        if (g.size() < 2) {
            // Trial graph, nothing to do:
            return new FinancialGraph(g);
        }
    
        // Copy the graph:
        FinancialGraph r = new FinancialGraph();
        
        Map<String, FinancialGraphNode> m = getIdToNodeMap(g);
        
        final int N = g.size();
        
        List<FinancialGraphNode> positiveEquityNodes = new ArrayList<>(N);
        List<FinancialGraphNode> negativeEquityNodes = new ArrayList<>(N);
        List<FinancialGraphNode> zeroEquityNodes     = new ArrayList<>(N);
        
        for (FinancialGraphNode node : g) {
            if (node.getEquity() > 0L) {
                positiveEquityNodes.add(node);
            } else if (node.getEquity() < 0L) {
                negativeEquityNodes.add(node);
            } else {
                zeroEquityNodes.add(node);
            }
        }
        
        final int POS_LIMIT = positiveEquityNodes.size();
        final int NEG_LIMIT = negativeEquityNodes.size();
        
        long[] positiveNodeEquities = new long[POS_LIMIT];
        long[] negativeNodeEquities = new long[NEG_LIMIT];
        
        List<FinancialGraphNode> newPositiveEquityNodes;
        List<FinancialGraphNode> newNegativeEquityNodes;
        
        newPositiveEquityNodes = new ArrayList<>(POS_LIMIT);
        newNegativeEquityNodes = new ArrayList<>(NEG_LIMIT);
        
        int pi = 0;
        int ni = 0;
        
        // Build equity counters:
        for (FinancialGraphNode node : positiveEquityNodes) {
            newPositiveEquityNodes.add(
                    new FinancialGraphNode(positiveEquityNodes.get(pi)));
        
            positiveNodeEquities[pi++] = node.getEquity();
        }
        
        for (FinancialGraphNode node : negativeEquityNodes) {
            newNegativeEquityNodes.add(
                    new FinancialGraphNode(negativeEquityNodes.get(ni)));
        
            negativeNodeEquities[ni++] = -node.getEquity();
        }
        
        pi = 0;
        ni = 0;
        
        while (pi < POS_LIMIT && ni < NEG_LIMIT) {
            if (positiveNodeEquities[pi] > negativeNodeEquities[ni]) {
                
                FinancialGraphNode lender1 = newPositiveEquityNodes.get(pi);
                FinancialGraphNode debtor1 = newNegativeEquityNodes.get(ni);
                
                FinancialGraphNode lender2 = m.get(lender1.getName());
                FinancialGraphNode debtor2 = m.get(debtor1.getName());
                
                r.add(lender2);
                r.add(debtor2);
                
                lender2.connectToBorrower(debtor2);
                lender2.setWeightTo(debtor2, negativeNodeEquities[ni]);
                
                positiveNodeEquities[pi] -= negativeNodeEquities[ni];
                ++ni;
            } else if (positiveNodeEquities[pi] < negativeNodeEquities[ni]) {
                
                FinancialGraphNode lender1 = newPositiveEquityNodes.get(pi);
                FinancialGraphNode debtor1 = newNegativeEquityNodes.get(ni);
                
                FinancialGraphNode lender2 = m.get(lender1.getName());
                FinancialGraphNode debtor2 = m.get(debtor1.getName());
                
                r.add(lender2);
                r.add(debtor2);
                
                lender2.connectToBorrower(debtor2);
                lender2.setWeightTo(debtor2, positiveNodeEquities[pi]);
                
                negativeNodeEquities[ni] -= positiveNodeEquities[pi];
                ++pi;
            } else {
                
                FinancialGraphNode lender1 = newPositiveEquityNodes.get(pi);
                FinancialGraphNode debtor1 = newNegativeEquityNodes.get(ni);
                
                FinancialGraphNode lender2 = m.get(lender1.getName());
                FinancialGraphNode debtor2 = m.get(debtor1.getName());
                
                r.add(lender2);
                r.add(debtor2);
                
                lender2.connectToBorrower(debtor2);
                lender2.setWeightTo(debtor2, positiveNodeEquities[pi]);
                
                ++pi;
                ++ni;
            }
        }
        
        for (FinancialGraphNode node : 
                loadZeroEquityNodeNames(zeroEquityNodes)) { 
            r.add(node);
        }
        
        return r;
    }

    private Map<String, FinancialGraphNode> getIdToNodeMap(FinancialGraph g) {
        Map<String, FinancialGraphNode> m = new HashMap<>(g.size());
        
        for (FinancialGraphNode node : g) {
            m.put(node.getName(), new FinancialGraphNode(node.getName()));
        }
        
        return m;
    }

    private static List<FinancialGraphNode> loadZeroEquityNodeNames(
            List<FinancialGraphNode> zeroEquityNodes) {
    
        List<FinancialGraphNode> copy = new ArrayList<>(zeroEquityNodes.size());
        
        for (FinancialGraphNode node : zeroEquityNodes) {
            FinancialGraphNode clone = new FinancialGraphNode(node.getName());
            copy.add(clone);
        }
        
        return copy;
    }
}
    