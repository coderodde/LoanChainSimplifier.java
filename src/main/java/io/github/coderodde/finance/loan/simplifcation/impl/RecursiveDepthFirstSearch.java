package io.github.coderodde.finance.loan.simplifcation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.github.coderodde.finance.loan.simplifcation.FinancialGraphNode;
import io.github.coderodde.finance.loan.simplifcation.FinancialGraph;

/**
 * This class implements a recursive depth-first search variant returning a 
 * directed cycle.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Sep 4, 2021)
 * @since 1.6 (Sep 4, 2021)
 */
class RecursiveDepthFirstSearch {

    private final Set<FinancialGraphNode> marked = new HashSet<>();
    private final Set<FinancialGraphNode> stack = new HashSet<>();
    private final Map<FinancialGraphNode, FinancialGraphNode> parents = new HashMap<>();
    
    public List<FinancialGraphNode> findCycle(FinancialGraph graph) {
        for (FinancialGraphNode root : graph) {
            if (!marked.contains(root)) {
                parents.put(root, null);
                
                List<FinancialGraphNode> cycle = findCycleImpl(root);

                if (cycle != null) {
                    clearDataStructures();
                    return cycle;
                }
            }
        }
        
        clearDataStructures();
        return null;
    }
    
    private void clearDataStructures() {
        marked.clear();
        stack.clear();
        parents.clear();
    }
    
    private List<FinancialGraphNode> findCycleImpl(FinancialGraphNode root) {
        if (marked.contains(root)) {
            return null;
        }
        
        if (stack.contains(root)) {
            List<FinancialGraphNode> cycle = new ArrayList<>();
            FinancialGraphNode currentFinancialGraphNode = parents.get(root);
            
            while (currentFinancialGraphNode != root) {
                cycle.add(currentFinancialGraphNode);
                currentFinancialGraphNode = parents.get(currentFinancialGraphNode);
            }
            
            cycle.add(root);
            Collections.<FinancialGraphNode>reverse(cycle);
            return cycle;
        }
        
        stack.add(root);
        
        for (FinancialGraphNode child : root) {
            parents.put(child, root);
            List<FinancialGraphNode> cycleCandidate = findCycleImpl(child);
            
            if (cycleCandidate != null) {
                return cycleCandidate;
            }
        }
        
        stack.remove(root);
        marked.add(root);
        return null;
    }
}
