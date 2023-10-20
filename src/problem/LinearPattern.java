/**
 * LinearPattern.java
 * Jul 19, 2011
 */
package problem;

import util.IntegerUtils;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Jul 19, 2011
 *
 */
public class LinearPattern implements Pattern {
	public long period;
	public double innerDiameter;
	public double startingPosition;
	
	/**
	 * 
	 * TODO
	 * 
	 * @param period
	 * @param innerDiameter
	 * @param startingPosition
	 */
	public LinearPattern(long period, double innerDiameter, double startingPosition) {
		this.period = period;
		this.innerDiameter = innerDiameter;
		this.startingPosition = startingPosition;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param period
	 * @param innerDiameter
	 */
	public LinearPattern(long period, double innerDiameter) {
		this(period, innerDiameter, 0d);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	@Override
	public LinearPattern clone() {
		return new LinearPattern(period, innerDiameter, startingPosition);
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @return
	 * @throws IllegalArgumentException
	 */
	public double BB(Pattern i) throws IllegalArgumentException {
		return this.B(i) - this.innerDiameter - i.getInnerDiameter();
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @return
	 * @throws IllegalArgumentException
	 */
	public double B(Pattern i) throws IllegalArgumentException {
		if (i instanceof LinearPattern) {
			LinearPattern a = (LinearPattern)i;
			return IntegerUtils.gcd(this.period, a.period);
		} else if (i.getPeriod() == Math.floor(i.getPeriod()))
			return IntegerUtils.gcd(this.period, (long)i.getPeriod());
		else
			throw new IllegalArgumentException("Cannot compare different kinds of pattern");
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public double getStartingPosition() {
		return this.startingPosition;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public double getInnerDiameter() {
		return this.innerDiameter;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public double getPeriod() {
		return this.period;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public LinearPattern inversePattern() {
		return new LinearPattern(period, period - innerDiameter, startingPosition + innerDiameter);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public double density() {
		return 1d * period / innerDiameter;
	}

	/**
	 * TODO
	 * 
	 * @param start
	 */
	public void setStartingPosition(double start) {
		this.startingPosition = start;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param i
	 * @return
	 * @throws IllegalArgumentException
	 */
	public long Nij(Pattern i) throws IllegalArgumentException {
		if (i instanceof LinearPattern) {
			LinearPattern a = (LinearPattern)i;
			return (long)(this.period / IntegerUtils.gcd(this.period, a.period));
		} else if (IntegerUtils.isInteger(i.getPeriod()))
				return this.period / IntegerUtils.gcd(this.period, (long)i.getPeriod());
		else
			throw new IllegalArgumentException("Cannot compare different patterns.");
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param d
	 * @param s
	 * @return
	 */
	public LinearPattern companionPattern(double d, double s) {
		return new LinearPattern(period, d, s);
	}
	
	public LinearPatternSet set() {
		LinearPattern[] patterns = {this};
		return new LinearPatternSet(patterns);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param p
	 * @param d
	 * @param s
	 * @return
	 * @throws IllegalArgumentException
	 */
	public LinearPattern lcmPattern(Pattern p, double d, double s) throws IllegalArgumentException {
		if (p instanceof LinearPattern) {
			LinearPattern a = (LinearPattern)p;
			return new LinearPattern(IntegerUtils.gcd(a.period, this.period), d, s);
		} else
			throw new IllegalArgumentException("Cannot compare different pattern types.");
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public String toString() {
		return "(" + period + ", " + innerDiameter + ", " + startingPosition + ")";
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LinearPattern a = new LinearPattern(5, 3);
		CircularPattern b = new CircularPattern(20, 6, 1);
		System.out.print(b.B(a) + " & ");
		System.out.println(a.B(b));
		System.out.print(a.BB(b) + " & ");
		System.out.println(b.BB(a));
	}

}
