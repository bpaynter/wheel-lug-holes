/**
 * CycleFinder.java
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
public interface CycleFinder {
	public void verbose(boolean set);
	public SimpleArc[] getNegativeCycle(double[][] weights);
	public SimpleArc[] getNegativeCycle(double[][] weights, int[] permutation);
}
