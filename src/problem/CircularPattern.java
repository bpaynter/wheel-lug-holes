package problem;


import java.util.Scanner;

import util.IntegerUtils;


/**
 * This class contains the information about a single pattern of lug holes. <br />
 * It is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * A pattern is a set of equal sized, circular holes evenly spaced around a circle.
 * 
 * @author Brad Paynter
 * @version June 9, 2010
 */
public class CircularPattern implements PSPattern {
	public long outerCircumference; // B
	public long numberOfHoles; // n
	public double innerDiameter; // d
	public double startingPosition; // s
	
	/**
	 * Constructs a Pattern object with the given parameters
	 * 
	 * @param B The circumference of the circle that the holes are arranged on
	 * @param n The number of holes in the pattern
	 * @param d The diameter of the holes
	 * @param s The starting position of the "first" hole
	 */
	public CircularPattern(long B, long n, double d, double s) {
		this.outerCircumference = B;
		this.numberOfHoles = n;
		this.innerDiameter = d;
		this.startingPosition = s;
	}
	
	/**
	 * Creates a Pattern object from a string listing of the pattern. The
	 * pattern should be input as "(B,n,d,s)".
	 * 
	 * @param patternString
	 * @throws IllegalArgumentException
	 */
	public CircularPattern(String patternString) throws IllegalArgumentException {
		Scanner input = new Scanner(patternString).useDelimiter("[(,)]");
		if (input.hasNextInt())
			outerCircumference = input.nextInt();
		else
			throw new IllegalArgumentException("Invalid Pattern String.");
		if (input.hasNextInt())
			numberOfHoles = input.nextInt();
		else
			throw new IllegalArgumentException("Invalid Pattern String.");
		if (input.hasNextInt())
			innerDiameter = input.nextInt();
		else
			throw new IllegalArgumentException("Invalid Pattern String.");
		if (input.hasNextDouble())
			startingPosition = input.nextDouble();
		else
			startingPosition = 0d;
	}
	
	/**
	 * Constructs a Pattern object with a starting position of zero
	 * 
	 * @param B The circumference of the circle that the holes are arranged on
	 * @param n The number of holes in the pattern
	 * @param d The diameter of the holes
	 */
	public CircularPattern(long B, long n, double d) {
		this(B, n, d, 0);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	@Override
	public CircularPattern clone() {
		return new CircularPattern(outerCircumference, numberOfHoles, innerDiameter, startingPosition);
	}
	
	/**
	 * This method calculates the value B_{ij}. It is the Outer Circumference 
	 * value divided by the LCM of n_i and n_j. It does this by calculating 
	 * the GCD of n_i and n_j. If this Pattern is contained in a PatternSet 
	 * then use the equivalent method from PatternSet as it is faster
	 * 
	 * @param i The first Pattern
	 * @param j The second Pattern
	 * @return The B_{ij} value
	 * @throws IllegalArgumentException Thrown if the two patterns 
	 * 			do not have the same B value
	 */
	public double B(Pattern j) throws IllegalArgumentException {
		if ((j instanceof CircularPattern)) {
			CircularPattern b = (CircularPattern)j;
			// Check if the circumferences match
		    if (this.outerCircumference != b.outerCircumference) 
		        throw new IllegalArgumentException("Outer Circumferences must match");
		    double B = 0d;
		    // Calculate the output value
		   	B = 1.0 * this.outerCircumference / IntegerUtils.lcm(this.numberOfHoles, b.numberOfHoles);
		    // System.out.println(B);
		    return B;
		} else if (IntegerUtils.isInteger(this.getPeriod()) && IntegerUtils.isInteger(j.getPeriod()))
			return IntegerUtils.gcd((long)this.getPeriod(), (long)j.getPeriod());
		else
			throw new IllegalArgumentException("Cannot compare different kinds of patterns");
			
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
	 * This method calculates the value B'_{ij}. It is the Outer Circumference value 
	 * divided by the LCM of n_i and n_j minus d_i and d_j. It does this by 
	 * calculating the GCD of n_i and n_j.  If this Pattern is contained in a 
	 * PatternSet then use the equivalent method from PatternSet as it is faster.
	 * 
	 * @param i The first Pattern
	 * @param j The second Pattern
	 * @return The B'_{ij} value
	 * @throws IllegalArgumentException Thrown if the two patterns do not 
	 * 			have the same B value
	 */
	public double BB(Pattern j) throws IllegalArgumentException {
		return this.B(j) - this.innerDiameter - j.getInnerDiameter();
	}
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public double getStartingPosition() {
		return startingPosition;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param start
	 */
	public void setStartingPosition(double start) {
		this.startingPosition = start;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public double getInnerDiameter() {
		return innerDiameter;
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
		if (i instanceof CircularPattern) {
			CircularPattern a = (CircularPattern)i;
			return IntegerUtils.lcm(numberOfHoles, a.numberOfHoles) / numberOfHoles;
		} else if (IntegerUtils.isInteger(this.getPeriod()) && IntegerUtils.isInteger(i.getPeriod()))
			return (long)this.getPeriod() / IntegerUtils.gcd((long)this.getPeriod(), (long)i.getPeriod());
		else
			throw new IllegalArgumentException("Cannot compare different kinds of patterns.");
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param d
	 * @param s
	 * @return
	 */
	public CircularPattern companionPattern(double d, double s) {
		return new CircularPattern(outerCircumference, numberOfHoles, d, Math.abs(s) % outerCircumference);
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
	public CircularPattern lcmPattern(Pattern p, double d, double s) throws IllegalArgumentException {
		if (p instanceof CircularPattern) {
			CircularPattern a = (CircularPattern)p;
			if (a.outerCircumference != this.outerCircumference)
				throw new IllegalArgumentException("Circular patterns cannot have different outer circumferences.");
			return new CircularPattern(a.outerCircumference, IntegerUtils.lcm(a.numberOfHoles, this.numberOfHoles), d, Math.abs(s) % outerCircumference);
		} else
			throw new IllegalArgumentException("Cannot compare different pattern types.");
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public double getPeriod() {
		return 1d * outerCircumference / numberOfHoles;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public CircularPattern inversePattern() {
		double d = (1d * outerCircumference / numberOfHoles) - innerDiameter;
		return new CircularPattern(outerCircumference, numberOfHoles, d, startingPosition + innerDiameter);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public double density() {
		return 1d * outerCircumference / (innerDiameter * numberOfHoles);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public CircularPatternSet set() {
		CircularPattern[] patterns = {this};
		return new CircularPatternSet(patterns);
	}
	
	/**
	 * This method overrides the <code>toString()</code> method from <code>Object</code>. 
	 * It outputs a nicely formatted string describing this Pattern.
	 * 
	 * @return A string description of this pattern.
	 */
	@Override
	public String toString() {
		return "(" + outerCircumference + "," + numberOfHoles + "," 
					+ innerDiameter + "," + startingPosition + ")";
	}
	
	/**
	 * This main method is used for testing
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		CircularPattern p = new CircularPattern("(3243,5,2,0)");
		System.out.println(p);
	}

	
	
}


