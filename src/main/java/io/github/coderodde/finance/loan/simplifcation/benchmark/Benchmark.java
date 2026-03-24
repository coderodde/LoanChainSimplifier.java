package io.github.coderodde.finance.loan.simplifcation.benchmark;

import io.github.coderodde.finance.loan.simplifcation.FinancialGraph;
import io.github.coderodde.finance.loan.simplifcation.FinancialGraphNode;
import io.github.coderodde.finance.loan.simplifcation.impl.BypassingLoanChainSimplifier;
import io.github.coderodde.finance.loan.simplifcation.impl.LinearSimplifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Benchmark {
    
    private static final int NUMBER_OF_NODES = 10_000;
    private static final int NUMBER_OF_ARCS  = 100_000;
    private static final int STRING_ID_LENGTH = 4;
    private static final long MAXIMUM_LOAN = 10L;
    
    public static void main(String[] args) {
        long seed = System.currentTimeMillis();
        
        System.out.println("Seed: " + seed);
        
        Random random = new Random(seed);
        FinancialGraph source = getRandomFinancialGraph(random);
        
        printStatistics(source, "Source");
        
        long ta = System.currentTimeMillis();
        
        FinancialGraph result1 = 
                new BypassingLoanChainSimplifier().simplify(source);
        
        long tb = System.currentTimeMillis();
        
        printStatistics(result1, "Bypassing");
        System.out.println("    Duration = " + (tb - ta) + " milliseconds.");
        
        
        ta = System.currentTimeMillis();
        
        FinancialGraph result2 = new LinearSimplifier().simplify(source);
        
        tb = System.currentTimeMillis();
        
        printStatistics(result2, "Linear");
        System.out.println("    Duration = " + (tb - ta) + " milliseconds.");
        
        boolean success = source.isEquivalentTo(result1) &&
                          result1.isEquivalentTo(result2);
        
        System.out.println("Algorithms agree: " + success);
    }
    
    private static void printStatistics(FinancialGraph graph, String name) {
        String fmt = 
                """
                Graph (%s): 
                    edges = %d
                    flow  = %d
                """;
        
        System.out.printf(fmt, 
                          name, 
                          graph.getEdgeAmount(), 
                          graph.getTotalFlow());
    }
    
    private static FinancialGraph getRandomFinancialGraph(Random random) {
        FinancialGraph g = new FinancialGraph();
        
        Set<String>  stringFilter = new HashSet<>(NUMBER_OF_NODES);
        
        while (stringFilter.size() < NUMBER_OF_NODES) {
            String id = getRandomString(random);
            stringFilter.add(id);
        }
        
        List<String> ids = new ArrayList<>(stringFilter);
        List<FinancialGraphNode> nodes = convertIdsToNodes(ids, g);
        
        while (g.getEdgeAmount() < NUMBER_OF_ARCS) {
            FinancialGraphNode lender = nodes.get(random.nextInt(nodes.size()));
            FinancialGraphNode debtor = nodes.get(random.nextInt(nodes.size()));
            
            if (lender.equals(debtor)) {
                continue;
            }
            
            lender.connectToBorrower(debtor);
            lender.setWeightTo(debtor, getRandomLoan(random));
        }
        
        return g;
    }
    
    private static String getRandomString(Random random) {
        StringBuilder sb = new StringBuilder(STRING_ID_LENGTH);
        
        for (int i = 0; i < STRING_ID_LENGTH; ++i) {
            sb.append('a' + random.nextInt(26));
        }
        
        return sb.toString();
    }

    private static long getRandomLoan(Random random) {
        return 1L + random.nextLong(MAXIMUM_LOAN);
    }

    private static List<FinancialGraphNode> 
        convertIdsToNodes(List<String> ids, FinancialGraph graph) {
        
            List<FinancialGraphNode> nodes = new ArrayList<>(ids.size());
    
        for (String id : ids) {
            FinancialGraphNode node = new FinancialGraphNode(id);
            graph.add(node);
            nodes.add(node);
        }
        
        return nodes; 
    }
}
