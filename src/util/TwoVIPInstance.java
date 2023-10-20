/**
 * TwoVIPInstance.java
 * Nov 7, 2011
 */
package util;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Nov 7, 2011
 *
 */
public interface TwoVIPInstance {
	public int numVariables();
	public int numConstraints();
	public long lowerBound(int i) throws IllegalArgumentException;
	public long upperBound(int i) throws IllegalArgumentException;
	public long weight(int i) throws IllegalArgumentException;
	public TwoVIPConstraint getConstraint(int k);
}
