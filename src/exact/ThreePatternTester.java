/**
 * ThreePatternTester.java
 * Aug 18, 2010
 */
package exact;

import problem.CircularPatternSet;
import util.IntegerUtils;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Aug 18, 2010
 *
 */
public class ThreePatternTester {
	
	/**
	 * 
	 * TODO
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static boolean testThreePatterns(CircularPatternSet patterns, int a, int b, int c) {
		double lhs = 0.0;
		double rhs = 0.0;
		lhs = lhs + patterns.getPattern(a).innerDiameter + patterns.getPattern(b).innerDiameter + patterns.getPattern(c).innerDiameter;
		lhs = lhs / patterns.getPattern(a).outerCircumference;
		rhs = -lhs;
		// System.out.println(lhs + ", " + rhs);
		lhs = lhs - (1.0 / patterns.lcm(a, c));
		rhs = rhs + (1.0 / patterns.lcm(a, b));
		rhs = rhs + (1.0 / patterns.lcm(b, c));
		long[] nums = new long[3];
		nums[0] = patterns.getPattern(a).numberOfHoles * patterns.getPattern(b).numberOfHoles;
		nums[1] = patterns.getPattern(b).numberOfHoles * patterns.getPattern(c).numberOfHoles;
		nums[2] = patterns.getPattern(a).numberOfHoles * patterns.getPattern(c).numberOfHoles;
		long lcm = 1;
		lcm = lcm * patterns.getPattern(a).numberOfHoles;
		lcm = lcm * patterns.getPattern(b).numberOfHoles;
		lcm = lcm * patterns.getPattern(c).numberOfHoles;
		lcm = lcm / IntegerUtils.gcd(nums);
		// System.out.println(lhs + ", " + rhs + ", " + lcm);
		lhs = Math.ceil(lhs * lcm);
		rhs = Math.floor(rhs * lcm);
		// System.out.println(lhs + " <= " + rhs);
		return (lhs <= rhs);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public static boolean findBadTriple(CircularPatternSet patterns) {
		for (int a = 0; a < patterns.numPatterns() - 2; a++)
			for (int b = a + 1; b < patterns.numPatterns() - 1; b++)
				for (int c = b + 1; c < patterns.numPatterns(); c++)
					if (!testThreePatterns(patterns, a, b, c)) {
						System.out.println(a + ", " + b + ", " + c);
						return true;
					}
		return false;
	}
	
	/**
	 * TODO
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		CircularPatternSet patterns = new CircularPatternSet("(234872,91,14,0.0)(234872,85,16,0.0)(234872,66,20,0.0)(234872,45,3,0.0)" +
				"(234872,77,4,0.0)(234872,89,15,0.0)(234872,67,24,0.0)(234872,60,21,0.0)");
		System.out.println(patterns);
		System.out.println(ThreePatternTester.findBadTriple(patterns));
	}

}
