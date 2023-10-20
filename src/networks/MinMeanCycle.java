/**
 * MinMeanCycle.java
 * Feb 13, 2011
 */
package networks;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Feb 13, 2011
 *
 */
public class MinMeanCycle implements CycleFinder {

	private int size;
	private double[][] d;
	private int[][] pred;
	private int optK;
	private int optJ;
	private double optMean;
	public boolean verbose;
	
	/**
	 * 
	 * TODO
	 * 
	 * @param size
	 */
	public MinMeanCycle(int size) {
		this.size = size;
		d = new double[size + 1][size];
		pred = new int[size + 1][size];
		verbose = false;
	}
	

	
	/**
	 * 
	 * TODO
	 * 
	 * @param weights
	 * @return
	 */
	public double findMinMeanCycleValue(double[][] weights) {
		optK = -1;
		optJ = -1;
		optMean = Double.POSITIVE_INFINITY;
		d[0][0] = 0.0d;
		for (int i = 1; i < size; i++)
			d[0][i] = Double.POSITIVE_INFINITY;
		pred[0][0] = 0;
		for (int i = 1; i < size; i++)
			pred[0][i] = -1;
		for (int k = 1; k <= size; k++) {
			for (int j = 0; j < size; j++) {
				d[k][j] = Double.POSITIVE_INFINITY;
				pred[k][j] = -1;
				for (int i = 0; i < size; i++) {
					if (d[k-1][i] + weights[i][j] < d[k][j]) {
						d[k][j] = d[k-1][i] + weights[i][j];
						pred[k][j] = i;
					}
				}
			}
		}
		for (int j = 0; j < size; j++) {
			double candidateMean = Double.NEGATIVE_INFINITY;
			int candidateK = -1;
			for (int k = size - 1; k >= 0; k--) {
				double mean = (d[size][j] - d[k][j]) / (size - k);
				if (mean > candidateMean) {
					candidateMean = mean;
					candidateK = k;
				}
			}
			if (candidateMean < optMean) {
				optMean = candidateMean;
				optK = candidateK;
				optJ = j;
			}
		}
		return optMean;
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
	 * @param permutation
	 * @return
	 */
	public SimpleArc[] getNegativeCycle(double[][] weights, int[] permutation) {
		return getNegativeCycle(weights);
	}
	
	/**
	 * TODO
	 * 
	 * @param weights
	 * @return
	 */
	public SimpleArc[] getNegativeCycle(double[][] weights) {
		findMinMeanCycleValue(weights);
		if (optMean < 0) {
			int j = optJ;
			int i = -1;
			SimpleArc[] negativeCycle = new SimpleArc[size - optK];
			for (int k = size; k > optK; k--) {
				i = pred[k][j];
				negativeCycle[k - optK - 1] = new SimpleArc(i, j, 0);
				j = i;
			}
			return negativeCycle;
		} else
			return null;
	}

}
