/**
 * FloydWarshall.java
 * Feb 13, 2011
 */
package networks;

import util.Permutation;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Feb 13, 2011
 *
 */
public class FloydWarshall implements CycleFinder {
	private int size;
	private double[][] d;
	private int[][] next;
	public boolean verbose;
	
	public FloydWarshall(int size) {
		this.size = size;
		d = new double[size][size];
		next = new int[size][size];
		verbose = false;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param weights
	 * @return
	 * @throws IllegalArgumentException
	 */
	public double[][] runFloydWarshall(double[][] weights) throws IllegalArgumentException {
		int[] permutation = new int[size];
		for (int i = 0; i < size; i++)
			permutation[i] = i;
		return runFloydWarshall(weights, permutation);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param weights
	 * @param permutation
	 * @return
	 * @throws IllegalArgumentException
	 */
	public double[][] runFloydWarshall(double[][] weights, int[] permutation) throws IllegalArgumentException {
		if (permutation.length != size)
			throw new IllegalArgumentException("Permutation is the wrong size.");
		if (!Permutation.correctPermutation(permutation))
			throw new IllegalArgumentException("Invalid Permutation.");
		if (weights.length != size)
			throw new IllegalArgumentException("Weights matrix is the wrong size.");
		for (int i = 0; i < weights.length; i++) {
			next[permutation[i]][permutation[i]] = 0;
			d[permutation[i]][permutation[i]] = 0;
			if (weights.length != weights[permutation[i]].length)
				throw new IllegalArgumentException("Weights Matrix is not square.");
			for (int j = i+1; j < weights[permutation[i]].length; j++) {
				d[permutation[i]][permutation[j]] = weights[permutation[i]][permutation[j]];
				d[permutation[j]][permutation[i]] = weights[permutation[j]][permutation[i]];
				next[permutation[i]][permutation[j]] = j;
				next[permutation[j]][permutation[i]] = i;
			}
		}
		boolean negCycle = false;
		int iteration = 0;
		while (!negCycle && (iteration < weights.length)) {
			for (int i = 0; i < weights.length; i++)
				for (int j = 0; j < weights.length; j++) {
					if (d[permutation[i]][permutation[j]] > d[permutation[i]][permutation[iteration]] + d[permutation[iteration]][permutation[j]]) {
						d[permutation[i]][permutation[j]] = d[permutation[i]][permutation[iteration]] + d[permutation[iteration]][permutation[j]];
						next[permutation[i]][permutation[j]] = next[permutation[i]][permutation[iteration]];
						if (i == j)
							negCycle = true;
					}
				}
			iteration++;
		}
		if (verbose) {
			String endln = System.getProperty("line.separator");
			String s = "";
			s += "Weight";
			for (int j = 0; j < size; j++)
				s += String.format("%1$9s", "<" + j + ">");
			s += endln;
			for (int i = 0; i < size; i++) {
				s += String.format("%1$9s", "<" + i + ">");
				for (int j = 0; j < size; j++)
					s += String.format("%1$9.2f", weights[permutation[i]][permutation[j]]);
				s += endln;
			}
			s += "Dist: ";
			for (int j = 0; j < size; j++)
				s += String.format("%1$9s", "<" + j + ">");
			s += endln;
			for (int i = 0; i < size; i++) {
				s += String.format("%1$9s", "<" + i + ">");
				for (int j = 0; j < size; j++)
					s += String.format("%1$9.2f", d[permutation[i]][permutation[j]]);
				s += endln;
			}
			s += "Next: ";
			for (int j = 0; j < size; j++)
				s += String.format("%1$6s", "<" + j + ">");
			s += endln;
			for (int i = 0; i < size; i++) {
				s += String.format("%1$6s", "<" + i + ">");
				for (int j = 0; j < size; j++)
					s += String.format("%1$6s", next[permutation[i]][permutation[j]]);
				s += endln;
			}
			System.out.print(s);
		}
		return d;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param set
	 */
	public void verbose(boolean set) {
		this.verbose = set;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param weights
	 * @return
	 */
	public SimpleArc[] getNegativeCycle(double[][] weights) {
		int[] permutation = new int[size];
		for (int i = 0; i < size; i++)
			permutation[i] = i;
		return getNegativeCycle(weights, permutation);
	}

	/**
	 * TODO
	 * 
	 * @param weights
	 * @return
	 */
	public SimpleArc[] getNegativeCycle(double[][] weights, int[] permutation) {
		runFloydWarshall(weights, permutation);
		for (int i = 0; i < size; i++)
			if (d[permutation[i]][permutation[i]] < 0) {
				int j = i;
				int cycleLength = 0;
				do {
					cycleLength++;
					j = next[permutation[j]][permutation[i]];
				} while (j != i);
				SimpleArc[] negativeCycle = new SimpleArc[cycleLength];
				j = i;
				cycleLength = 0;
				do {
					negativeCycle[cycleLength] = new SimpleArc(j, next[permutation[j]][permutation[i]], weights[permutation[j]][next[permutation[j]][permutation[i]]]);
					cycleLength++;
					j = next[permutation[j]][permutation[i]];
				} while (j != i);
				return negativeCycle;
			}
		return null;
	}
	
	
	public static void main(String[] args) {
		FloydWarshall floyd = new FloydWarshall(5);
		double[][] weights = {	{0, 1, 1, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
								{1, 0, 1, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
								{1, 1, 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
								{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0, 1},
								{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1, 0}};
		floyd.verbose = true;
		floyd.runFloydWarshall(weights);
	}


}
