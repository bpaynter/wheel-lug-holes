/**
 * PatternSet.java
 * Jul 19, 2011
 */
package problem;


/**
 * TODO
 *
 * @author Brad Paynter
 * @version Jul 19, 2011
 *
 */
public interface PatternSet extends Iterable<Pattern> {
	public Pattern getPattern(int i) throws IllegalArgumentException;
	public PatternSet subset(int[] indices);
	public void addPattern(Pattern p, int position);
	public int numPatterns();
	public double getB(int i, int j);
	public double getBB(int i, int j);
	public double getD(int i, int j);
	public void setSolution(long[][] ks);
	public void setSolution(int[] permutation, long[][] ks);
	public void setZeroSolution();
	public void setZeroSolution(int[] permutation);
	public void permute(int[] permutation);
	public void setPermutation(int[] permutation);
	public FitSolution getSolution();
	public void setPatternStart(int pattern, double start);
	public long getKLowerBound(int i, int j);
	public long getKUpperBound(int i, int j);
	public int densityBinLowerBound();
	public int korstBinLowerBound();
	public PatternSet clone();
	public String printSolution();
}
