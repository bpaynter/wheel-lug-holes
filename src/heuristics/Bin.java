/**
 * BinFitDeterminer.java
 * Jul 7, 2011
 */
package heuristics;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Jul 7, 2011
 *
 */
public interface Bin extends Iterable<Integer> {
	public boolean fit(int item);
	public boolean insert(int item);
	public boolean reSolve();
	public int[] outputIndices();
	public void setLevel(int level) throws IllegalArgumentException;
	public int numLevels();
	public Bin newBin(int item);
	public boolean checkSolution();
	public String printSolution();
	public int binSize();
	public void setVerbosity(boolean v);
}
