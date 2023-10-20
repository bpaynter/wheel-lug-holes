/**
 * LinearPatternSet.java
 * Jul 29, 2011
 */
package problem;

import heuristics.SetPartition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import util.IntegerUtils;
import util.MersenneTwisterFast;
import util.Permutation;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Jul 29, 2011
 *
 */
public class LinearPatternSet implements PatternSet {
	private LinearPattern[] patterns;
	private int[] permutation;
	private long[][] gcd;
	private FitSolution solution;
	
	public LinearPatternSet(LinearPattern[] patterns) {
		this.patterns = patterns;
		gcd = new long[patterns.length][patterns.length];
		permutation = new int[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			gcd[i][i] = patterns[i].period;
			permutation[i] = i;
			for (int j = i+1; j < patterns.length; j++) {
				gcd[i][j] = IntegerUtils.gcd(patterns[i].period, patterns[j].period);
				gcd[j][i] = gcd[i][j];
			}
		}
		solution = new FitSolution(patterns.length);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @param gcd
	 */
	private LinearPatternSet(LinearPattern[] patterns, long[][] gcd) {
		this.patterns = patterns;
		this.gcd = gcd;
		// Set the initial permutation (the identity permutation)
		this.permutation = new int[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			permutation[i] = i;
		}
		solution = new FitSolution(patterns.length);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param numPatterns
	 * @param pMax
	 * @param random
	 * @return
	 */
	public static LinearPatternSet randomKorstPatternSet(int numPatterns, int pMax, MersenneTwisterFast random) {
		int[] p = new int[numPatterns];
		LinearPattern[] patterns = new LinearPattern[numPatterns];
		for (int i = 0; i < numPatterns; i++) {
			p[i] = random.nextInt(pMax) + 1;
			patterns[i] = new LinearPattern(p[i], random.nextInt(p[i]) + 1);
		}
		return new LinearPatternSet(patterns);
	}
	

	/**
	 * TODO
	 * 
	 * @return
	 */
	public Iterator<Pattern> iterator() {
		ArrayList<Pattern> patternList = new ArrayList<Pattern>(Arrays.asList(patterns));
		return patternList.iterator();
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @return
	 */
	public LinearPattern getPattern(int i) {
		return patterns[permutation[i]];
	}

	/**
	 * TODO
	 * 
	 * @param indices
	 * @return
	 */
	public LinearPatternSet subset(int[] indices) throws IllegalArgumentException {
		if (indices.length > patterns.length)
			throw new IllegalArgumentException("Number of indices exceeds number of patterns in this set.");
		for (int i = 0; i < indices.length; i++)
			for (int j = i+1; j < indices.length; j++)
				if (indices[i] == indices[j])
					throw new IllegalArgumentException("Duplicate Indices: " + i + " & " + j);
		LinearPattern[] newPatterns = new LinearPattern[indices.length];
		long[][] newGCDs = new long[indices.length][indices.length];
		for (int i = 0; i < indices.length; i++) {
			newPatterns[i] = patterns[permutation[indices[i]]];
			for (int j = 0; j < indices.length; j++)
				newGCDs[i][j] = gcd[permutation[indices[i]]][permutation[indices[j]]];
		}
		return new LinearPatternSet(newPatterns, newGCDs);
	}

	/**
	 * TODO
	 * 
	 * @param p
	 * @param position
	 */
	public void addPattern(Pattern p, int position)  throws IllegalArgumentException {
		if (p == null)
			throw new IllegalArgumentException("Null patterns are not allowed.");
		if ((position < 0) || (position > patterns.length))
			throw new IllegalArgumentException("Invalid insertion position.");
		if (p instanceof LinearPattern) {
			LinearPattern pattern = (LinearPattern)p;
			LinearPattern[] newPatterns = new LinearPattern[patterns.length + 1];
			long[][] newGcd = new long[patterns.length+1][patterns.length+1];
			int[] newPermutation = new int[patterns.length+1];
			solution = null;
			for (int i = 0; i < patterns.length; i++) {
				newPatterns[i] = patterns[i];
				for (int j = 0; j < patterns.length; j++)
					newGcd[i][j] = gcd[i][j];
				newGcd[i][patterns.length] = IntegerUtils.gcd(patterns[i].period, pattern.period);
				newGcd[patterns.length][i] = newGcd[i][patterns.length];
			}
			newPatterns[patterns.length] = pattern;
			newGcd[patterns.length][patterns.length] = pattern.period;
			for (int i = 0; i < position; i++)
				newPermutation[i] = permutation[i];
			newPermutation[position] = patterns.length;
			for (int i = position+1; i <= patterns.length; i++)
				newPermutation[i] = permutation[i-1];
			patterns = newPatterns;
			gcd = newGcd;
			permutation = newPermutation;
		} else
			throw new IllegalArgumentException("This set can only accept Linear Patterns");
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public int numPatterns() {
		return patterns.length;
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public double getB(int i, int j) {
		return gcd[permutation[i]][permutation[j]];
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public double getBB(int i, int j) {
		return gcd[permutation[i]][permutation[j]] -  patterns[permutation[i]].innerDiameter - patterns[permutation[j]].innerDiameter;
	}

	/**
	 * 
	 * TODO
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public double getD(int i, int j) {
		return getPattern(j).innerDiameter;
	}
	
	/**
	 * TODO
	 * 
	 * @param ks
	 */
	public void setSolution(long[][] ks) {
		setSolution(this.permutation, ks);
	}

	/**
	 * TODO
	 * 
	 * @param permutation
	 * @param ks
	 */
	public void setSolution(int[] permutation, long[][] ks) throws IllegalArgumentException {
		if ((permutation.length != patterns.length) 
				&& (!Permutation.correctPermutation(permutation)))
			throw new IllegalArgumentException("Permutation invalid.");
		if (ks.length != patterns.length)
			throw new IllegalArgumentException("Invalid K matrix");
		long[][] ksclone = new long[ks.length][];
		for (int i = 0; i < ks.length; i++)
			ksclone[i] = ks[i].clone();
		int[] newPermutation = new int[patterns.length];
		for (int i = 0; i < patterns.length; i++)
			newPermutation[i] = this.permutation[permutation[i]];
		solution = new FitSolution(patterns.length, newPermutation, ksclone);
	}

	/**
	 * 
	 * TODO
	 * 
	 * @param permutation
	 * @throws IllegalArgumentException
	 */
	public void setZeroSolution(int[] permutation) throws IllegalArgumentException {
		if ((permutation.length == patterns.length) 
					&& (Permutation.correctPermutation(permutation))) {
			long[][] ks = new long[patterns.length][patterns.length];
			for (long[] currentRow : ks) 
				Arrays.fill(currentRow, 0);
			int[] newPermutation = new int[patterns.length];
			for (int i = 0; i < patterns.length; i++)
				newPermutation[i] = this.permutation[permutation[i]];
			solution = new FitSolution(patterns.length, newPermutation, ks);
		} else
			throw new IllegalArgumentException("Permutation invalid.");
	}
	
	/**
	 * Sets the solution to the current permutation and k_{i,j}=0 for all i,j.
	 * This is used when a zero-feasible solution has been found.
	 */
	public void setZeroSolution() {
		long[][] ks = new long[patterns.length][patterns.length];
		for (long[] currentRow : ks) 
			Arrays.fill(currentRow, 0);
		solution = new FitSolution(patterns.length, permutation.clone(), ks);
	}

	/**
	 * This method permutes the patterns in the set. The argument is an array of indices
	 * in the permuted order. The indices refer to the original permutation of the patterns 
	 * (i.e. the order they were in when the set was created)
	 * 
	 * @param permutation The new permutation to be applied to the set.
	 * @throws IllegalArgumentException Thrown if the permutation is not the same
	 * 				length as the number of patterns in this set or if the permutation
	 * 				is not a valid permutation
	 */
	public void setPermutation(int[] permutation) throws IllegalArgumentException {
		// Check that the permutation is the correct length
		if ((permutation.length == patterns.length) 
					&& (Permutation.correctPermutation(permutation))) {
			this.permutation = permutation;
		} else {
			throw new IllegalArgumentException("Permutation invalid.");
		}
	}
	
	/**
	 * This method permutes the patterns in the set. The argument is an array of indices
	 * in the permuted order. The indices refer to the current permutation of the patterns.
	 * 
	 * @param permutation The new permutation to be applied to the set.
	 * @throws IllegalArgumentException Thrown if the permutation is not the same
	 * 				length as the number of patterns in this set or if the permutation
	 * 				is not a valid permutation
	 */
	public void permute(int[] permutation) throws IllegalArgumentException {
		// Check that the permutation is the correct length
		if ((permutation.length == patterns.length) 
					&& (Permutation.correctPermutation(permutation))) {
			int[] newPermutation = new int[patterns.length];
			for (int i = 0; i < patterns.length; i++)
				newPermutation[i] = this.permutation[permutation[i]];
			this.permutation = newPermutation;
		} else {
			throw new IllegalArgumentException("Permutation invalid.");
		}
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public FitSolution getSolution() {
		return solution;
	}

	/**
	 * Sets the start value for a given pattern
	 * 
	 * @param patternNumber The number of the pattern to be updated
	 * @param start The start value to be assigned to the pattern
	 * @throws IllegalArgumentException Thrown if the patternNumber given 
	 * 				does not correspond to a pattern in this set.
	 */
	public void setPatternStart(int patternNumber, double start) 
									throws IllegalArgumentException {
		if ((patternNumber < 0) || (patternNumber > patterns.length - 1))
			throw new IllegalArgumentException("Pattern index out of bounds: " + patternNumber);
		else {
			patterns[permutation[patternNumber]].startingPosition = start;
		}
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public long getKLowerBound(int i, int j) {
		if (i <= j)
			return -getPattern(i).period / gcd(i, j);
		else
			return -getPattern(j).period / gcd(i, j);
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public long getKUpperBound(int i, int j) {
		if (i <= j)
			return getPattern(j).period / gcd(i, j) - 1;
		else
			return getPattern(i).period / gcd(i, j) - 1;
	}
	
	/**
	 * Returns the greatest common divisor of p_i and p_j
	 * 
	 * @param i The index of the first pattern (in the current permutation)
	 * @param j The index of the second pattern (in the current permutation)
	 * @return The GCD of n_i and n_j
	 */
	public long gcd(int i, int j) {
		return gcd[permutation[i]][permutation[j]];
	}
	
	/**
	 * Returns the least common multiple of p_i and p_j
	 * 
	 * @param i The index of the first pattern (in the current permutation)
	 * @param j The index of the second pattern (in the current permutation)
	 * @return The LCM of n_i and n_j
	 */
	public long lcm(int i, int j) {
		// Recall that gcd(a,b)*lcm(a,b) = a*b
		return (patterns[permutation[i]].period / this.gcd(i, j)) * patterns[permutation[j]].period;
	}
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public int densityBinLowerBound() {
		double bin = 0;
		for (LinearPattern p : patterns)
			bin += (1d * p.innerDiameter) / p.period;
		return (int)Math.ceil(bin);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int korstBinLowerBound() {
		int[][] partition = SetPartition.indexPartition(this);
		int lowerBound = 0;
		for (int[] set : partition) {
			double currentBound = 0d;
			for (int i : set) {
				LinearPattern p = getPattern(i);
				currentBound += (1d * p.innerDiameter) / p.period;
			}
			lowerBound += (int)Math.ceil(currentBound);
		}
		return lowerBound;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	@Override
	public LinearPatternSet clone() {
		LinearPattern[] newPatterns = new LinearPattern[patterns.length];
		long[][] newGCD = new long[patterns.length][patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			newPatterns[i] = patterns[i].clone();
			newGCD[i] = gcd[i].clone();
		}
		LinearPatternSet outputSet = new LinearPatternSet(newPatterns, newGCD);
		outputSet.setPermutation(permutation.clone());
		outputSet.solution = solution.clone();
		return outputSet;
	}


	/**
	 * Generates a human-readable, formatted version of the current solution.
	 * 
	 * @return A String representation of the current solution. This is null if no
	 * 			solution has yet been found.
	 */
	public String printSolution() {
		String endln = System.getProperty("line.separator");
		String s = "";
		int[] permutation = solution.getPermutation();
		if (permutation != null) {
			long[][] ks = solution.getKs();
			for (int i = 0; i < patterns.length; i++)
				s += "P_" + i + " = " + patterns[permutation[i]] + endln;
			s += "      ";
			for (int j = 0; j < patterns.length; j++)
				s += String.format("%1$6s", "P_" + j);
			s += endln;
			for (int i = 0; i < patterns.length; i++) {
				s += String.format("%1$6s", "P_" + i);
				for (int j = 0; j < i + 1; j++)
					s += "      ";
				for (int j = i+1; j < patterns.length; j++)
					s += String.format("%1$6s", ks[i][j]);
				s += endln;
			}
		} else
			s = "No Solution Found";
		return s;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		String endln = System.getProperty("line.separator");
		String s = "";
		for (LinearPattern p : patterns)
			s += p + endln;
		return s;
	}
	

}
