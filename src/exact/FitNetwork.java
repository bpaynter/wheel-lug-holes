/**
 * FitNetwork.java
 * Jun 14, 2010
 */
package exact;

import networks.Network;
import problem.PatternSet;


/**
 * This code is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * This class is a network representation of the fit problem for a given number of 
 * patterns. It can be used to determine whether a given permutation and set of k_{ij}'s 
 * is feasible for a set of patterns. If so, the network can give a valid set of starting 
 * locations for the patterns.
 *
 * @author Brad Paynter
 * @version Jun 14, 2010
 *
 */
public class FitNetwork extends Network {

	/**
	 * Constructs a complete network with <code>size</code> nodes. The network
	 * can be used for problems involving <code>size</code> patterns.
	 * The arcs have zero weight at this point.
	 *  
	 * @param size The number of nodes (patterns) in this network
	 */
	public FitNetwork(int size) {
		super(size);
        this.generateCompleteArcs();
	}
    
    /**
     * Generates the arc weights corresponding to the given patterns and the current 
     * stored solution
     * 
     * @param patterns The set of patterns that the weights correspond to. This PatternSet 
     * 				must have a stored solution. 
     * @throws IllegalArgumentException Thrown if if the pattern set has no valid solution.
     */
    private void generateWeights(PatternSet patterns) throws IllegalArgumentException {
    	// Get the k_{ij} matrix
    	long[][] ks = patterns.getSolution().getKs();
    	// Ensure that the k_{ij} matrix is not null
    	if (ks == null)
    		throw new IllegalArgumentException("Pattern Set has no associated solution.");
    	patterns.setPermutation(patterns.getSolution().getPermutation());
    	// Ensure that the k_{ij} matrix is the right size
    	if (ks.length != getSize())
    		throw new IllegalArgumentException("Incorrect number of k's.");
    	// Set up the adjacency matrix
    	double[][] weights = new double[getSize()][getSize()];
    	// For all possible pairs
    	for (int from = 0; from < getSize(); from++) {
            for (int to = 0; to < getSize(); to++) {
            	// If the arc is a loop, set its weight to zero
            	if (from == to)
                    weights[from][to] = 0;
            	// If the arc is forward
                else if (from < to)
                	weights[from][to] = (ks[from][to] + 1) * patterns.getB(from, to) 
                								- patterns.getD(from, to);
            	// If the arc is backward
                else
                	weights[from][to] = -ks[to][from] * patterns.getB(to, from) 
                								- patterns.getD(from, to);
                //System.out.println("(" + from + "," + to + "): " + weights[from][to]);
            }
        }
    	// Pass the adjacency matrix to the network
    	setArcWeights(weights);
    }
    
    /**
     * Tests a PatternSet and its solution to ensure that the solution is valid.<br/>
     * i.e. it ensures that the network corresponding to the solution has no negative
     * weight cycles.<br/>
     * If the network has no negative-weight cycle, then this method finds the shortest
     * path from all nodes to the node corresponding to the first pattern in the solution
     * order and sets the start values of the patterns in the set to the non-overlapping 
     * starting values found. 
     * 
     * @param patterns The set of patterns to be tested.
     * @return	<code>true</code> if the network corresponding to the patterns in the 
     * 			set and their solution has no negative weight cycle, <code>false</code> 
     * 			if the solution is missing, invalid, or if the network does have a 
     * 			negative weight cycle.
     * @throws IllegalArgumentException Thrown if the number of patterns in the PatternSet
     * 			does not match the number of nodes in this network object.
     */
    public boolean testSolution(PatternSet patterns) throws IllegalArgumentException {
    	// Ensure that the PatternSet and this network are the correct size
    	if (patterns.numPatterns() != getSize())
    		throw new IllegalArgumentException
    					("This network is the wrong size for this set of patterns.");
    	// Ensure that the PatternSet contains a solution
    	if (patterns.getSolution().getPermutation() == null)
    		return false;
    	else {
    		// Permute the patterns to the order in the solution
        	patterns.permute(patterns.getSolution().getPermutation());
        	// Generate the arc weights corresponding to the solution 
    		generateWeights(patterns);
    		// Find the shortest path distances from all nodes to node 0
    		boolean success = findShortestPathsTo(0);
    		// If there is no negative weight cycle
    		if (success) {
    			// Get the valid distance labels from the network
	    		double dist[] = getDistanceLabels();
	    		// Set the starting positions to the negative distance labels
	    		for (int i = 0; i < dist.length; i++)
	    			patterns.setPatternStart(i, -dist[i]);
    		}
    		// Return the status
    		return success;
    	}
    }
}
