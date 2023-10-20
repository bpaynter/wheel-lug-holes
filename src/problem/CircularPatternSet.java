/**
 * PatternSet.java
 * Nov 2, 2009
 */
package problem;

import heuristics.CycleHeuristic;
import heuristics.SetPartition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Matcher;


import util.Matrix;
import util.MersenneTwisterFast;
import util.IntegerUtils;
import util.Permutation;
import util.SortUtils;
import util.SplayTree;
import util.TSPInstance;

/**
 * This code is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * This class holds a set of patterns and various information pertaining to
 * that set including the GCD's of all possible pairs. This is calculated
 * at the time of construction to speed up operations elsewhere.
 *
 * @author Brad Paynter
 * @version June 6, 2010
 *
 */
public class CircularPatternSet implements Iterable<Pattern>, TSPInstance, Matrix<Double>, PSPatternSet {
	/**
	 * The patterns in this set
	 */
	private CircularPattern[] patterns;
	/**
	 * The gcd of n_i and n_j for all i,j in <code>patterns</code>
	 */
	private long[][] gcd;
	/**
	 * The current permutation of the patterns
	 */
	private int[] permutation;
	/**
	 * An ordering and a set of k_{ij}'s that lead to a feasible 
	 * solution for this set of patterns
	 */
	private FitSolution solution;
	
	/**
	 * This constructor is used internally when the GCD matrix has 
	 * been precalculated
	 * 
	 * @param patterns An array of the patterns to be contained 
	 * 			in this PatternSet
	 * @param gcd A matrix containing the GCD's of all possible pairs 
	 * 			from <code>patterns</code>
	 */
	private CircularPatternSet(CircularPattern[] patterns, long[][] gcd) {
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
	 * Constructs a PatternSet object containing the Patterns given.
	 * 
	 * @param patterns An array of Pattern objects to be stored in this set.
	 */
	public CircularPatternSet(CircularPattern[] patterns) {
		this.patterns = patterns;
		this.gcd = new long[patterns.length][patterns.length];
		this.permutation = new int[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			// Set the current permutation to the identity
			permutation[i] = i;
			gcd[i][i] = patterns[i].numberOfHoles;
            for (int j = i + 1; j < patterns.length; j++) {
            	// Set the gcd matrix
                gcd[i][j] = IntegerUtils.gcd(patterns[i].numberOfHoles, patterns[j].numberOfHoles);
                gcd[j][i] = gcd[i][j];
            }
        }
		solution = new FitSolution(patterns.length);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int densityBinLowerBound() {
		double bin = 0;
		for (CircularPattern p : patterns)
			bin += (1d * p.innerDiameter * p.numberOfHoles) / p.outerCircumference;
		return (int)Math.ceil(bin);
	}
	
	/**
	 * 
	 * TODO
	 *
	 */
	public void sortByDecreasingSize() {
		permutation = SortUtils.sort(patterns, new decreasingSizeComparator());
	}
	
	/**
	 * 
	 * TODO
	 *
	 */
	public void sortByIncreasingSize() {
		permutation = SortUtils.sort(patterns, new increasingSizeComparator());
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
				CircularPattern p = getPattern(i);
				currentBound += (1d * p.innerDiameter * p.numberOfHoles) / p.outerCircumference;
			}
			lowerBound += (int)Math.ceil(currentBound);
		}
		return lowerBound;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patternString
	 */
	public CircularPatternSet(String patternString) {
		ArrayList<CircularPattern> inputPatterns = new ArrayList<CircularPattern>();
		Matcher m = java.util.regex.Pattern.compile("\\s*(\\d+,\\d+,\\d+)\\s*").matcher(patternString);
		while (m.find())
			inputPatterns.add(new CircularPattern(m.group()));
		this.patterns = inputPatterns.toArray(new CircularPattern[1]);
		this.gcd = new long[patterns.length][patterns.length];
		this.permutation = new int[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			// Set the current permutation to the identity
			permutation[i] = i;
			gcd[i][i] = patterns[i].numberOfHoles;
            for (int j = i + 1; j < patterns.length; j++) {
            	// Set the gcd matrix
                gcd[i][j] = IntegerUtils.gcd(patterns[i].numberOfHoles, patterns[j].numberOfHoles);
                gcd[j][i] = gcd[i][j];
            }
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
	public static CircularPatternSet randomKorstPatternSet(int numPatterns, int pMax, MersenneTwisterFast random) {
		int[] p = new int[numPatterns];
		int[] d = new int[numPatterns];
		boolean[] used = new boolean[pMax];
		Arrays.fill(used, false);
		for (int i = 0; i < numPatterns; i++) {
			p[i] = random.nextInt(pMax) + 1;
			used[p[i] - 1] = true;
			d[i] = random.nextInt(p[i]) + 1;
		}
		long B = 1;
		for (int i = 0; i < pMax; i++)
			if (used[i])
				B = IntegerUtils.lcm(B, i+1);
		CircularPattern[] patterns = new CircularPattern[numPatterns];
		for (int i = 0; i < numPatterns; i++)
			patterns[i] = new CircularPattern(B, B / p[i], d[i]);
		return new CircularPatternSet(patterns);
	}
	
	/**
	 * This method creates a PatternSet containing <code>numPatterns</code> 
	 * Patterns, randomly chosen to fulfill the following requirements:
	 * <UL>
	 * <LI> n_i does not divide n_j for all i,j in <code>patterns</code>
	 * <LI> B'_{ij} > 0 for all i,j in <code>patterns</code>
	 * </UL>
	 * 
	 * @param numPatterns The number of patterns required in this set
	 * @param maxDiameter The largest possible diameter
	 * @param minDiameter The smallest possible diameter
	 * @param maxHoles The largest possible number of holes
	 * @param minHoles The smallest possible number of holes
	 * @param random The randomizer currently in use
	 * @param factors The <code>IntegerUtils</code> object to be used
	 * @return A random <code>PatternSet</code> object fulfilling the requirements above
	 * @throws IllegalArgumentException Thrown if <code>factors</code> is not large
	 * 			enough to factor <code>maxHoles</code>.
	 */
	public static CircularPatternSet randomTightPatternSet(int numPatterns, int maxDiameter, 
													int minDiameter, int maxHoles, int minHoles, 
													MersenneTwisterFast random, IntegerUtils factors) 
													throws IllegalArgumentException {
		// Ensure that the IntegerUtils object is large enough
		if (factors.size() < maxHoles)
			throw new IllegalArgumentException("Must have factors for maxHoles!");
		// Create arrays to contain the d and n values
		int[] d = new int[numPatterns];
		int[] n = new int[numPatterns];
		// Create a structure to contain the n values available to be chosen
		SplayTree<Integer> possibleNs = new SplayTree<Integer>();
		// Add all n's from minHoles to maxHoles
		for (int i = 0; i < maxHoles - minHoles + 1; i++)
			possibleNs.add(minHoles + i);
		// Create the patterns
		for (int i = 0; i < numPatterns; i++) {
			// Choose a random diameter
			d[i] = random.nextInt(maxDiameter - minDiameter + 1) + minDiameter;
			// Choose a random n from those available
			int nIndex = random.nextInt(possibleNs.size());
			n[i] = possibleNs.get(nIndex);
			// Remove the n chosen from the list
			possibleNs.remove(nIndex);
			// Remove all factors of n from the list
			for (int j : factors.getFactors(n[i])) {
				possibleNs.remove(new Integer(j));
			}
			// Remove all multiples of n from the list
			int j = n[i] + n[i];
			while (j <= maxHoles) {
				possibleNs.remove(new Integer(j));
				j += n[i];
			}
		}
		// Clear the list of possible n's to free memory
		possibleNs.clear();
		// Initialize B and the GCD matrix
		int B = 0;
		long[][] gcd = new long[numPatterns][numPatterns];
		// For all possible pairs of patterns
		for (int i = 0; i < numPatterns; i++) {
			gcd[i][i] = n[i];
            for (int j = i + 1; j < numPatterns; j++) {
            	// Find their GCD
                gcd[i][j] = IntegerUtils.gcd(n[i], n[j]);
                gcd[j][i] = gcd[i][j];
                // Ensure that B is large enough so that B'_{ij} > 0
                double Bij = 1.0 * (d[i] + d[j]) * n[i] * n[j] / gcd[i][j];
				if (B < Bij)
					B = (int)Math.ceil(Bij);
            }
        }
		// Add 1 to B to prevent rounding errors.
		B++;
		// Create the patterns from the data
		CircularPattern[] out = new CircularPattern[numPatterns];
		for (int i = 0; i < numPatterns; i++)
			out[i] = new CircularPattern(B, n[i], d[i]);
		// Output the result
		return new CircularPatternSet(out, gcd);
		
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param indices
	 * @return
	 * @throws IllegalArgumentException
	 */
	public CircularPatternSet subset(int[] indices) throws IllegalArgumentException {
		if (indices.length > patterns.length)
			throw new IllegalArgumentException("Number of indices exceeds number of patterns in this set.");
		for (int i = 0; i < indices.length; i++)
			for (int j = i+1; j < indices.length; j++)
				if (indices[i] == indices[j])
					throw new IllegalArgumentException("Duplicate Indices: " + i + " & " + j);
		CircularPattern[] newPatterns = new CircularPattern[indices.length];
		long[][] newGCDs = new long[indices.length][indices.length];
		for (int i = 0; i < indices.length; i++) {
			newPatterns[i] = patterns[permutation[indices[i]]];
			for (int j = 0; j < indices.length; j++)
				newGCDs[i][j] = gcd[permutation[indices[i]]][permutation[indices[j]]];
		}
		return new CircularPatternSet(newPatterns, newGCDs);
	}
	
	/**
	 * Returns the number of patterns in this set
	 * 
	 * @return The number of patterns in this set
	 */
	public int numPatterns() {
		return patterns.length;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int numCities() {
		return this.numPatterns();
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
			throw new IllegalArgumentException("Not that many patterns.");
		else {
			patterns[permutation[patternNumber]].startingPosition = start;
		}
	}
	
	/**
	 * Sets the start value for all patterns
	 * 
	 * @param start An array containing the starting position of all 
	 * 				patterns in their permuted order
	 * @throws IllegalArgumentException Thrown if the array length is not 
	 * 				equal to the number of patterns in this set.
	 */
	public void setPatternStart(double[] start) throws IllegalArgumentException {
		if (start.length != patterns.length)
			throw new IllegalArgumentException("Incorrect number of starting positions.");
		for (int i = 0; i < start.length; i++)
			patterns[permutation[i]].startingPosition = start[i];
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	@Override
	public CircularPatternSet clone() {
		CircularPattern[] newPatterns = new CircularPattern[patterns.length];
		long[][] newGCD = new long[patterns.length][patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			newPatterns[i] = patterns[i].clone();
			newGCD[i] = gcd[i].clone();
		}
		CircularPatternSet outputSet = new CircularPatternSet(newPatterns, newGCD);
		outputSet.setPermutation(permutation.clone());
		outputSet.solution = solution.clone();
		return outputSet;
	}
	
	/**
	 * Returns a given pattern from the set. This method returns the pattern
	 * in position <code>i</code> of the current permutation.
	 * 
	 * @param i The position of the pattern required
	 * @return The pattern in position <code>i</code> of the current permutation
	 * @throws IllegalArgumentException Thrown if the pattern index is negative or greater
	 * 			than or equal to the number of patterns in this set
	 */
	public CircularPattern getPattern(int i) throws IllegalArgumentException {
		CircularPattern outPattern;
		try {
			outPattern = patterns[permutation[i]];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Pattern Index out of bounds.");
		}
		return outPattern;
	}
	
	/**
	 * Sets the current solution for this set to the given permutation 
	 * and k_{ij} = 0 for all i,j 
	 * 
	 * @param permutation A zero-feasible ordering of the patterns in this set.
	 * @throws IllegalArgumentException Thrown if the permutation is invalid
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
		} else {
			throw new IllegalArgumentException("Permutation invalid.");
		}
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
	 * 
	 * TODO
	 * 
	 * @param pattern
	 * @param position
	 * @throws IllegalArgumentException
	 */
	public void addPattern(Pattern pattern, int position) throws IllegalArgumentException {
		if (pattern == null)
			throw new IllegalArgumentException("Null patterns are not allowed.");
		if ((position < 0) || (position > patterns.length))
			throw new IllegalArgumentException("Invalid insertion position.");
		if (pattern instanceof CircularPattern) {
			CircularPattern p = (CircularPattern)pattern;
			CircularPattern[] newPatterns = new CircularPattern[patterns.length + 1];
			long[][] newGcd = new long[patterns.length+1][patterns.length+1];
			int[] newPermutation = new int[patterns.length+1];
			solution = null;
			for (int i = 0; i < patterns.length; i++) {
				newPatterns[i] = patterns[i];
				for (int j = 0; j < patterns.length; j++)
					newGcd[i][j] = gcd[i][j];
				newGcd[i][patterns.length] = IntegerUtils.gcd(patterns[i].numberOfHoles, p.numberOfHoles);
				newGcd[patterns.length][i] = newGcd[i][patterns.length];
			}
			newPatterns[patterns.length] = p;
			newGcd[patterns.length][patterns.length] = p.numberOfHoles;
			for (int i = 0; i < position; i++)
				newPermutation[i] = permutation[i];
			newPermutation[position] = patterns.length;
			for (int i = position+1; i <= patterns.length; i++)
				newPermutation[i] = permutation[i-1];
			patterns = newPatterns;
			gcd = newGcd;
			permutation = newPermutation;
		} else
			throw new IllegalArgumentException("This set can only accept Circular Patterns");
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param position
	 * @throws IllegalArgumentException
	 */
	public void removePattern(int position) throws IllegalArgumentException {
		if ((position < 0) || (position >= patterns.length))
			throw new IllegalArgumentException("Invalid index.");
		int patternNumber = permutation[position];
		CircularPattern[] newPatterns = new CircularPattern[patterns.length-1];
		long[][] newGcd = new long[patterns.length-1][patterns.length-1];
		int[] newPermutation = new int[patterns.length-1];
		for (int i = 0; i < patterns.length-1; i++) {
			if (i < patternNumber) {
				newPatterns[i] = patterns[i];
				for (int j = 0; j < patterns.length-1; j++)
					if (j < patternNumber)
						newGcd[i][j] = gcd[i][j];
					else
						newGcd[i][j] = gcd[i][j+1];
			} else {
				newPatterns[i] = patterns[i+1];
				for (int j = 0; j < patterns.length-1; j++)
					if (j < patternNumber)
						newGcd[i][j] = gcd[i+1][j];
					else
						newGcd[i][j] = gcd[i+1][j+1];
			}
			if (i < position)
				newPermutation[i] = (permutation[i] > patternNumber ? permutation[i] - 1 : permutation[i]);
			else
				newPermutation[i] = (permutation[i+1] > patternNumber ? permutation[i+1] - 1 : permutation[i+1]);
		}
		patterns = newPatterns;
		gcd = newGcd;
		permutation = newPermutation;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param p
	 * @return
	 */
	public boolean removePattern(Pattern p) {
		int position = -1;
		for (int i = 0; i < patterns.length; i++)
			if (p == patterns[permutation[i]])
				position = i;
		if (position < 0)
			return false;
		else
			removePattern(position);
		return true;
	}
	
	/**
	 * Sets the current solution to the given permutation/k_{ij} pair
	 * 
	 * @param permutation An ordering on the patterns
	 * @param ks A matrix of k_{ij} values
	 * @throws IllegalArgumentException Thrown if the permutation is invalid 
	 * 				or if the k_{ij} matrix is the wrong size.
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
	 * @param ks
	 * @throws IllegalArgumentException
	 */
	public void setSolution(long[][] ks) throws IllegalArgumentException {
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
	 * Outputs the current solution for this pattern set. This solution will have a null
	 * permutation if no solution has yet been found.
	 * 
	 * @return The current solution
	 */
	public FitSolution getSolution() {
		return solution;
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
	 * Returns the greatest common divisor of n_i and n_j
	 * 
	 * @param i The index of the first pattern (in the current permutation)
	 * @param j The index of the second pattern (in the current permutation)
	 * @return The GCD of n_i and n_j
	 */
	public long gcd(int i, int j) {
		return gcd[permutation[i]][permutation[j]];
	}
	
	/**
	 * Returns the least common multiple of n_i and n_j
	 * 
	 * @param i The index of the first pattern (in the current permutation)
	 * @param j The index of the second pattern (in the current permutation)
	 * @return The LCM of n_i and n_j
	 */
	public long lcm(int i, int j) {
		// Recall that gcd(a,b)*lcm(a,b) = a*b
		return (patterns[permutation[i]].numberOfHoles / this.gcd(i, j)) * patterns[permutation[j]].numberOfHoles;
	}
	
	/**
	 * Returns the value B_{ij} = B / lcm(n_i, n_j)
	 * 
	 * @param i The index of the first pattern (in the current permutation)
	 * @param j The index of the second pattern (in the current permutation)
	 * @return The value B_{ij}
	 */
	public double getB(int i, int j) {
		return 1.0 * patterns[permutation[i]].outerCircumference / this.lcm(i, j);
	}
	
	/**
	 * Returns the value B'_{ij} = (B / lcm(n_i, n_j)) - (d_i + d_j)
	 * 
	 * @param i The index of the first pattern (in the current permutation)
	 * @param j The index of the second pattern (in the current permutation)
	 * @return The value B'_{ij}
	 */
	public double getBB(int i, int j) {
		return this.getB(i, j) - patterns[permutation[i]].innerDiameter 
								- patterns[permutation[j]].innerDiameter;
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
	 * 
	 * TODO
	 * 
	 * @param cityOne
	 * @param cityTwo
	 * @return
	 */
	public double distance(int cityOne, int cityTwo) {
		return this.getB(cityOne, cityTwo);
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
			return -getPattern(j).numberOfHoles / gcd(i,j);
		else
			return -getPattern(i).numberOfHoles / gcd(i,j);
		/*
		if (i <= j)
			return -getPattern(j).numberOfHoles;
		else
			return -getPattern(i).numberOfHoles;
		*/
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
			return getPattern(i).numberOfHoles / gcd(i,j) - 1;
		else
			return getPattern(j).numberOfHoles / gcd(i,j) - 1;
		/*
		if (i <= j)
			return getPattern(i).numberOfHoles;
		else
			return getPattern(j).numberOfHoles;
		*/
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
		} else {
			s = "No Solution Found";
		}
		return s;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public String printGcds() {
		String endln = System.getProperty("line.separator");
		String s = "";
		for (int i = 0; i < patterns.length; i++)
			s += "P_" + i + " = " + patterns[permutation[i]] + endln;
		s += "      ";
		for (int j = 0; j < patterns.length; j++)
			s += String.format("%1$6s", "P_" + j);
		s += endln;
		for (int i = 0; i < patterns.length; i++) {
			s += String.format("%1$6s", "P_" + i);
			for (int j = 0; j < patterns.length; j++)
				s += String.format("%1$6s", gcd[permutation[i]][permutation[j]]);
			s += endln;
		}
		return s;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public String printBs() {
		String endln = System.getProperty("line.separator");
		String s = "";
		for (int i = 0; i < patterns.length; i++)
			s += "P_" + i + " = " + patterns[permutation[i]] + endln;
		s += "         ";
		for (int j = 0; j < patterns.length; j++)
			s += String.format("%1$9s", "P_" + j);
		s += endln;
		for (int i = 0; i < patterns.length; i++) {
			s += String.format("%1$9s", "P_" + i);
			for (int j = 0; j < patterns.length; j++)
				s += String.format("%1$9.2f", getB(i, j));
			s += endln;
		}
		return s;
	}
	
	/**
	 * Returns an iterator over the patterns in this set. They will iterate in the
	 * order of the current permutation.
	 * 
	 * @return An iterator over the patterns in this set.
	 */
	public Iterator<Pattern> iterator() {
		return new PatternSetIterator(this);
	}
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public int getRows() {
		return this.numPatterns();
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public int getColumns() {
		return this.numPatterns();
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public Double get(int i, int j) {
		if (i == j)
			return new Double(0.0);
		else
			return new Double(this.getB(i, j));
	}
	
	/**
	 * Produces a human-readable output of the contents of this set.
	 * 
	 * @return A string describing the patterns in this set
	 */
	@Override 
	public String toString() {
		String endln = System.getProperty("line.separator");
		String s = "";
		for (int i : permutation)
			s = s + patterns[i] + endln;
		return s;
	}
	
	/**
	 * This main method is only used for testing this class
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		//String s = "(23761,99,8) (23761,88,17,0t5) (23761,72,13)";
		String s = "(420,4,2) (420,15,2) (420,7,2) (420,10,2) (420,6,2)";
		CircularPatternSet q = new CircularPatternSet(s);
		CycleHeuristic heuristic = new CycleHeuristic(q);
		heuristic.verbose = true;
		heuristic.allPositions = false;
		heuristic.sortedAddition = true;
		heuristic.type = CycleHeuristic.fifoShortestPath;
		heuristic.smallestStart();
		int[] output = heuristic.runIterativeHeuristic();
		if (output.length == 5) {
			if (heuristic.checkSolution()) {
				System.out.println(heuristic.printSolution());
			} else
				System.out.println("CycleFinder Screwed Up!!!!");
		}
//		System.out.println(q.printGcds());
//		int[] permutation = {1, 2, 0};
//		q.permute(permutation);
//		System.out.println(q.printGcds());
//		q.addPattern(new CircularPattern(23761, 22, 7, 0), 2);
//		System.out.println(q.printGcds());
//		int[] subset = {3, 2, 0};
//		CircularPatternSet sub = q.subset(subset);
//		System.out.println(sub.printGcds());
//		int[] permutation2 = {2, 3, 1, 0};
//		q.permute(permutation2);
//		System.out.println(q.printGcds());
//		q.removePattern(2);
//		System.out.println(q.printGcds());
//		int[] permutation3 = {2, 0, 1};
//		q.permute(permutation3);
//		System.out.println(q.printGcds());
//		q.removePattern(2);
//		System.out.println(q.printGcds());
	}

	/**
	 * Iterator class for PatternSet Objects. Iterates over the patterns in
	 * the given set
	 *
	 * @author Brad Paynter
	 * @version Jul 28, 2010
	 *
	 */
	private class PatternSetIterator implements Iterator<Pattern> {
		/**
		 * The set to iterate over
		 */
		private CircularPatternSet mySet;
		/**
		 * The current position of the iterator
		 */
		private int currentPattern;
		
		/**
		 * Constructs a PatterSetIterator over the given set
		 * 
		 * @param patterns The PatternSet to iterate over
		 */
		public PatternSetIterator(CircularPatternSet patterns) {
			mySet = patterns;
			currentPattern = 0;
		}
		
		/**
		 * Determines whether this iterator has any more patterns to
		 * return.
		 * 
		 * @return <code>true</code> if the iterator has another pattern
		 * 			to return, <code>false</code> else.
		 */
		public boolean hasNext() {
			if (currentPattern >= mySet.numPatterns())
				return false;
			else
				return true;
		}

		/**
		 * Returns the next pattern in the set and advances the iterator
		 * one position.
		 * 
		 * @return The next pattern in the set. <code>null</code> if no
		 * 			more patterns remain
		 */
		public Pattern next() {
			if (this.hasNext())
				return mySet.getPattern(currentPattern++);
			else
				return null;
		}

		/**
		 * This method is not supported by this implementation of <code>Iterator</code>. 
		 * If called this method will throw an UnsupportedOperationException.
		 * 
		 * @throws UnsupportedOperationException Always thrown since this operation is not
		 * 			implemented in this iterator
		 */
		public void remove() {
			throw new 
				UnsupportedOperationException("Remove is not supported for this iterator.");
		}
		
	}

	private class decreasingSizeComparator implements Comparator<CircularPattern> {

		/**
		 * TODO
		 * 
		 * @param arg0
		 * @param arg1
		 * @return
		 */
		public int compare(CircularPattern arg0, CircularPattern arg1) {
			double useRatio0 = 0.1 * arg0.outerCircumference / (1.0 * arg0.numberOfHoles * arg0.innerDiameter);
			double useRatio1 = 0.1 * arg1.outerCircumference / (1.0 * arg1.numberOfHoles * arg1.innerDiameter);
			return Double.compare(useRatio0, useRatio1);
		}
		
	}
	
	private class increasingSizeComparator implements Comparator<CircularPattern> {

		/**
		 * TODO
		 * 
		 * @param arg0
		 * @param arg1
		 * @return
		 */
		public int compare(CircularPattern arg0, CircularPattern arg1) {
			double useRatio0 = 0.1 * arg0.outerCircumference / (1.0 * arg0.numberOfHoles * arg0.innerDiameter);
			double useRatio1 = 0.1 * arg1.outerCircumference / (1.0 * arg1.numberOfHoles * arg1.innerDiameter);
			return -Double.compare(useRatio0, useRatio1);
		}
		
	}
	
	
}
