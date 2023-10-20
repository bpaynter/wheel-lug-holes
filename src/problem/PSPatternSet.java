/**
 * PSPatternSet.java
 * Nov 24, 2011
 */
package problem;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Nov 24, 2011
 *
 */
public interface PSPatternSet extends PatternSet {
	public long gcd(int i, int j);
	public long lcm(int i, int j);
	public PSPattern getPattern(int i) throws IllegalArgumentException;
	public PSPatternSet subset(int[] indices);
	public PSPatternSet clone();
}
