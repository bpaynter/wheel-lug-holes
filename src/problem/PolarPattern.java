/**
 * PolarPattern.java
 * Oct 24, 2011
 */
package problem;

import java.util.Scanner;

import util.IntegerUtils;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Oct 24, 2011
 *
 */
public class PolarPattern implements PSPattern {
	
	public double outerCircleRadius;
	public int numberOfHoles;
	public double innerCircleRadius;
	public double startingAngle;
	
	public PolarPattern(double R, int n, double rho, double phi) {
		outerCircleRadius = R;
		numberOfHoles = n;
		innerCircleRadius = rho;
		startingAngle = phi;
	}
	
	public PolarPattern(double R, int n, double rho) {
		this(R, n, rho, 0.0);
	}
	
	/**
	 * Creates a Pattern object from a string listing of the pattern. The
	 * pattern should be input as "(B,n,d,s)".
	 * 
	 * @param patternString
	 * @throws IllegalArgumentException
	 */
	public PolarPattern(String patternString) throws IllegalArgumentException {
		Scanner input = new Scanner(patternString).useDelimiter("[(,)]");
		if (input.hasNextDouble())
			outerCircleRadius = input.nextDouble();
		else
			throw new IllegalArgumentException("Invalid Pattern String.");
		if (input.hasNextInt())
			numberOfHoles = input.nextInt();
		else
			throw new IllegalArgumentException("Invalid Pattern String.");
		if (input.hasNextDouble())
			innerCircleRadius = input.nextDouble();
		else
			throw new IllegalArgumentException("Invalid Pattern String.");
		if (input.hasNextDouble())
			startingAngle = input.nextDouble();
		else
			startingAngle = 0d;
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @return
	 * @throws IllegalArgumentException
	 */
	public double B(Pattern i) throws IllegalArgumentException {
		if ((i instanceof PolarPattern)) {
			PolarPattern b = (PolarPattern)i;
			return 2.0 * Math.PI / IntegerUtils.lcm(this.numberOfHoles, b.numberOfHoles);
		} else
			throw new IllegalArgumentException("Cannot compare different kinds of patterns");
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @return
	 * @throws IllegalArgumentException
	 */
	public long Nij(Pattern i) throws IllegalArgumentException {
		if (i instanceof PolarPattern) {
			PolarPattern a = (PolarPattern)i;
			return IntegerUtils.lcm(numberOfHoles, a.numberOfHoles) / numberOfHoles;
		} else
			throw new IllegalArgumentException("Cannot compare different kinds of patterns.");
	}

	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public long numberOfHoles() {
		return numberOfHoles;
	}
	
	/**
	 * TODO
	 * 
	 * @param d
	 * @param s
	 * @return
	 */
	public Pattern companionPattern(double d, double s) {
		throw new UnsupportedOperationException("This operation is undefined for PolarPatterns.");
	}

	/**
	 * TODO
	 * 
	 * @param p
	 * @param d
	 * @param s
	 * @return
	 * @throws IllegalArgumentException
	 */
	public Pattern lcmPattern(Pattern p, double d, double s) throws IllegalArgumentException {
		throw new UnsupportedOperationException("This operation is undefined for PolarPatterns.");
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public double getStartingPosition() {
		return startingAngle;
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @return
	 * @throws IllegalArgumentException
	 */
	/*
	public double getInnerDiameter(Pattern i) throws IllegalArgumentException {
		if (i instanceof PolarPattern) {
			PolarPattern p = (PolarPattern)i;
			if (Math.abs(outerCircleRadius - p.outerCircleRadius) >= (innerCircleRadius + p.innerCircleRadius))
				return 0d;
			double numerator = (this.innerCircleRadius + p.innerCircleRadius) * (this.innerCircleRadius + p.innerCircleRadius);
			numerator -= (this.outerCircleRadius - p.outerCircleRadius) * (this.outerCircleRadius - p.outerCircleRadius);
			double denomenator = 4d * this.outerCircleRadius * p.outerCircleRadius;
			double ratio = numerator / denomenator;
			double squareRoot = Math.sqrt(ratio);
			double arcSine = Math.asin(squareRoot);
			return 2d * arcSine;
		} else
			throw new IllegalArgumentException("Cannot compare different kinds of patterns.");
	}
	*/
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public double getInnerDiameter() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("This operation is undefined for PolarPatterns.");
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public double getPeriod() {
		return 2.0 * Math.PI * this.outerCircleRadius / this.numberOfHoles;
	}

	/**
	 * TODO
	 * 
	 * @param start
	 */
	public void setStartingPosition(double start) {
		this.startingAngle = start;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public Pattern inversePattern() {
		throw new UnsupportedOperationException("Inverse Pattern is not defined for Polar Patterns.");
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public PolarPattern clone() {
		return new PolarPattern(outerCircleRadius, numberOfHoles, innerCircleRadius, startingAngle);
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public double density() {
		return this.getPeriod() - this.innerCircleRadius;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public PolarPatternSet set() {
		PolarPattern[] set = {this};
		return new PolarPatternSet(set);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		String s = "(";
		s += outerCircleRadius + ", ";
		s += numberOfHoles + ", ";
		s += innerCircleRadius + ", ";
		s += startingAngle + ")";
		return s;
	}

}
