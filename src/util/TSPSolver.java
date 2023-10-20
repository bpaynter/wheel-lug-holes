/**
 * TSPSolver.java
 * Feb 7, 2011
 */
package util;

import java.util.Iterator;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Feb 7, 2011
 *
 */
public class TSPSolver {
	private TSPInstance instance;
	public boolean type;
	public static final boolean cycle = true;
	public static final boolean path = false;
	
	public TSPSolver(TSPInstance instance) {
		this.instance = instance;
		type = cycle;
	}
	
	public int[] bruteForceSolver() {
		return bruteForceSolver(false);
	}
	
	public int[] bruteForceSolver(boolean verbose) {
		long start = System.nanoTime();
		Iterator<int[]> permutationIterator = Permutation.iterator(instance.numCities(), 0l);
		int[] currentTour;
		int[] optimalTour = null;
		int numberOfIterations = 0;
		double optimalTourLength = Double.POSITIVE_INFINITY;
		while (permutationIterator.hasNext()) {
			numberOfIterations++;
			currentTour = permutationIterator.next();
			int currentTourLength = 0;
			for (int i = 0; i < instance.numCities() - 1; i++)
				currentTourLength += instance.distance(currentTour[i], currentTour[i+1]);
			if (type = cycle)
				currentTourLength += instance.distance(currentTour[instance.numCities() - 1], currentTour[0]);
			if (currentTourLength < optimalTourLength) {
				optimalTour = currentTour;
				optimalTourLength = currentTourLength;
			}
			if (verbose && (1.0 * numberOfIterations / 10000000 == numberOfIterations / 10000000)) {
				long time = (System.nanoTime() - start) / 1000000000;
				System.out.println(numberOfIterations + " iterations complete. Current best: " + optimalTourLength 
								+ ". Total Time elapsed: " + time + "sec. Iterations per second = " + numberOfIterations / time);
			}
		}
		return optimalTour;
	}
}
