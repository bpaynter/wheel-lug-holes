/**
 * Pattern.java
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
public interface Pattern extends Cloneable {
	public Pattern clone();
	//public double BB(Pattern i) throws IllegalArgumentException;
	public double B(Pattern i) throws IllegalArgumentException;
	public long Nij(Pattern i) throws IllegalArgumentException;
	public Pattern companionPattern(double d, double s);
	public Pattern lcmPattern(Pattern p, double d, double s) throws IllegalArgumentException;
	public double getStartingPosition();
	public double getInnerDiameter();
	public double getPeriod();
	public void setStartingPosition(double start);
	public Pattern inversePattern();
	public double density();
	public PatternSet set();
}
