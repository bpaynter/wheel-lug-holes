/**
 * TwoVIPConstraint.java
 * Nov 24, 2011
 */
package util;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Nov 24, 2011
 *
 */
public class TwoVIPConstraint {
	public long coefficiantOne;
	public long coefficiantTwo;
	public int variableIndexOne;
	public int variableIndexTwo;
	public long rightHandSide;
	
	/**
	 * ax_i + bx_j >= c
	 * 
	 * @param i
	 * @param j
	 * @param a
	 * @param b
	 * @param c
	 */
	public TwoVIPConstraint(int i, int j, long a, long b, long c) {
		coefficiantOne = a;
		coefficiantTwo = b;
		variableIndexOne = i;
		variableIndexTwo = j;
		rightHandSide = c;
	}
	
	public TwoVIPConstraint mirrorConstraint() {
		return new TwoVIPConstraint(variableIndexTwo, variableIndexOne, coefficiantTwo, coefficiantOne, rightHandSide);
	}
}
