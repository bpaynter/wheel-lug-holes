/**
 * fitSolution.java
 * Jun 10, 2010
 */
package problem;

/**
 * This code is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * This class contains a fit solution for a feasible set of patterns. It
 * contains the number of patterns in the set, and a permutation and it's
 * associated k_{ij} values that, when used with a shortest path network,
 * can be used to find valid starting values for the patterns. 
 *
 * @author Brad Paynter
 * @version Jun 10, 2010
 *
 */
public class FitSolution {
	/**
	 * The number of patterns in this solution
	 */
	private int size;
	/**
	 * A permutation of the patterns in the set
	 */
	private int[] permutation;
	/**
	 * The k_{ij} values corresponding to the permutation.
	 */
	private long[][] ks;
	
	/**
	 * Constructs a valid solution with the given parameters. It clones
	 * the arrays passed to it in order to store the current information.
	 * A FitSolution object with a null permutation is indicative of no
	 * solution.
	 * 
	 * @param size The number of patterns
	 * @param permutation A permutation of the patterns
	 * @param ks Valid k_{ij}'s corresponding to the permutation above.
	 */
	public FitSolution(int size, int[] permutation, long[][] ks) {
		this.size = size;
		if (permutation != null)
			this.permutation = permutation.clone();
		else
			this.permutation = null;
		if (ks != null)
			this.ks = ks;
		else
			this.ks = null;
	}
	
	/**
	 * Null constructor. This is used of no solution has yet been found
	 * (or if no solution exists)
	 * 
	 * @param size The number of patterns in the set
	 */
	public FitSolution(int size) {
		this(size, null, null);
	}

	/**
	 * Returns the permutation in this solution
	 * 
	 * @return The stored permutation
	 */
	public int[] getPermutation() {
		return permutation;
	}

	/**
	 * Returns the k_{ij} matrix
	 * 
	 * @return The k_{ij} matrix
	 */
	public long[][] getKs() {
		return ks;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public FitSolution clone() {
		int[] newPermutation = permutation.clone();
		long[][] newKs = new long[size][size];
		for (int i = 0; i < size; i++)
			newKs[i] = ks[i].clone();
		return new FitSolution(size, newPermutation, newKs);
	}

	/**
	 * Returns the size of the set
	 * 
	 * @return The size
	 */
	public int getSize() {
		return size;
	}
	
	
}
