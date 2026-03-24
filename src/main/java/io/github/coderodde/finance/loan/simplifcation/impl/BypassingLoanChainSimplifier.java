package io.github.coderodde.finance.loan.simplifcation.impl;

import io.github.coderodde.finance.loan.simplifcation.LoanChainSimplifier;
import io.github.coderodde.finance.loan.simplifcation.FinancialGraph;
import io.github.coderodde.finance.loan.simplifcation.FinancialGraphNode;
import java.util.List;

/**
 * This class implements the cycle purge/bypass simplifier. In cycle purge
 * technique, we search for directed cycles, choose the minimum weight 
 * {@code w}, subtract {@code w} from the weight of each arc in the cycle, and,
 * finally, remove all those arcs whose weight becomes zero.
 * <p>
 * In bypass technique, in order to lower the total flow, we choose two 
 * connected arcs {@code a_1 = (n_1, n_2), a_2 = (n_2, n_3)}, then we choose
 * the minimum weight {@code w} of the {@code a_1} and {@code a_2}, subtract
 * {@code w} from both the arcs, and rearrange the arcs such that the total flow
 * is lowered.
 */
public final class BypassingLoanChainSimplifier implements LoanChainSimplifier {

    @Override
    public FinancialGraph simplify(FinancialGraph g) {
        FinancialGraph resultFinancialGraph = new FinancialGraph(g);

        if (g.size() < 2) {
            return resultFinancialGraph;
        }

        RecursiveDepthFirstSearch rdfs = new RecursiveDepthFirstSearch();
        List<FinancialGraphNode> cycle;

        while ((cycle = rdfs.findCycle(resultFinancialGraph)) != null) {
            resolveCycle(cycle);
        }

        Triple<FinancialGraphNode> arcChainToBypass;

        while ((arcChainToBypass = findArcChain(resultFinancialGraph)) 
                != null) {
            
            resolveArcChain(arcChainToBypass.first,
                            arcChainToBypass.second,
                            arcChainToBypass.third);
        }

        return resultFinancialGraph;
    }

    private static void resolveCycle(List<FinancialGraphNode> cycle) {
        long minimumWeight = Long.MAX_VALUE;

        for (int i = 0; i < cycle.size(); i++) {
            FinancialGraphNode lender = cycle.get(i);
            FinancialGraphNode borrower = cycle.get((i + 1) % cycle.size());
            long arcWeight = lender.getWeightTo(borrower);
            minimumWeight = Math.min(minimumWeight, arcWeight);
        }

        for (int i = 0; i < cycle.size(); i++) {
            FinancialGraphNode lender = cycle.get(i);
            FinancialGraphNode borrower = cycle.get((i + 1) % cycle.size());
            long arcWeight = lender.getWeightTo(borrower);

            if (arcWeight == minimumWeight) {
                // Remove the minimum weight arc:
                lender.removeBorrower(borrower);
            } else {
                // Subtract 'minimumWeight' from the '(lender, borrower)' arc
                // weight:
                lender.setWeightTo(borrower, 
                        lender.getWeightTo(borrower) - minimumWeight);
            }
        }
    }

    private static Triple<FinancialGraphNode> 
        findArcChain(FinancialGraph graph) {
            
        for (FinancialGraphNode root : graph) {
            return findArcChainImpl(root);
        }

        return null;
    }

    private static Triple<FinancialGraphNode> 
        findArcChainImpl(FinancialGraphNode root) {
            
        for (FinancialGraphNode child : root) {
            for (FinancialGraphNode grandChild : child) {
                return new Triple<>(root, child, grandChild);
            }
        }

        return null;
    }

    private static void resolveArcChain(FinancialGraphNode n1,
                                        FinancialGraphNode n2, 
                                        FinancialGraphNode n3) {
        
        long weightN1N2 = n1.getWeightTo(n2);
        long weightN2N3 = n2.getWeightTo(n3);
        long minimumWeight = Math.min(weightN1N2, weightN2N3);

        n1.setWeightTo(n2, n1.getWeightTo(n2) - minimumWeight);
        n2.setWeightTo(n3, n2.getWeightTo(n3) - minimumWeight);

        if (n1.getWeightTo(n2) == 0L) {
            n1.removeBorrower(n2);
        }

        if (n2.getWeightTo(n3) == 0L) {
            n2.removeBorrower(n3);
        }

        if (n1.isConnectedTo(n3)) {
            n1.setWeightTo(n3, n1.getWeightTo(n3) + minimumWeight);
        } else {
            n1.connectToBorrower(n3);
            n1.setWeightTo(n3, minimumWeight);
        }
    }
    
    private static final class Triple<T> {
        final T first;
        final T second;
        final T third;
        
        Triple(T first, T second, T third) {
            this.first  = first;
            this.second = second;
            this.third  = third;
        }
    }
}
