package io.github.coderodde.finance.loan.simplifcation;

/**
 * This interface defines the API for loan chain simplifier algorithms.
 */
public interface LoanChainSimplifier {

    /**
     * Attempts to reduce the number of arcs in the source graph. Is not optimal
     * in general.
     * 
     * @param source the source graph to simplify.
     * 
     * @return a possibly simpler graph. 
     */
    public FinancialGraph simplify(FinancialGraph source);
}
