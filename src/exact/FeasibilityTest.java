/**
 * ZeroOrdering.java
 * Jun 10, 2010
 */
package exact;

import java.io.*;
import java.util.Iterator;
import java.util.regex.Matcher;

import problem.CircularPattern;
import problem.CircularPatternSet;
import problem.Pattern;
import problem.PatternSet;
import util.Permutation;

/**
 * This code is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * This class contains static methods for finding and testing feasibility for a 
 * set of patterns
 *
 * @author Brad Paynter
 * @version Jun 10, 2010
 *
 */
public class FeasibilityTest {
	
	public static boolean testStartingPositions(PatternSet patterns) {
		return testStartingPositions(patterns, false);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @return
	 */
	public static boolean testStartingPositions(PatternSet patterns, boolean verbose) {
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i + 1; j < patterns.numPatterns(); j++) {
				Pattern a = patterns.getPattern(i);
				Pattern b = patterns.getPattern(j);
				if (a.getStartingPosition() > b.getStartingPosition()) {
					Pattern temp = a;
					a = b;
					b = temp;
				}
				double sij = Math.abs(b.getStartingPosition() - a.getStartingPosition()) % patterns.getB(i, j);
				if (verbose) {
					System.out.println("A: " + a + ", B: " + b);
					System.out.println(patterns.getD(j, i) + " <= " + sij + "<= " + (patterns.getB(i, j) - patterns.getD(i, j)));
				}
				if (sij + 0.00000000001d < patterns.getD(j, i))
					return false;
				if (sij - 0.00000000001d > patterns.getB(i, j) - patterns.getD(i, j))
					return false;
			}
		return true;
	}
	/*
	public static boolean testStartingPositions(PatternSet patterns) {
		double minBprime = Double.MAX_VALUE;
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++) {
				minBprime = Math.min(minBprime, patterns.getBB(i, j));
			}
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++) {
				//Pattern a = (patterns.getPattern(i).startingPosition < patterns.getPattern(j).startingPosition ? patterns.getPattern(i) : patterns.getPattern(j));
				//Pattern b = (patterns.getPattern(i).startingPosition < patterns.getPattern(j).startingPosition ? patterns.getPattern(j) : patterns.getPattern(i));
				Pattern a = patterns.getPattern(i);
				Pattern b = patterns.getPattern(j);
				double rhs = 0.0 + b.startingPosition - a.startingPosition - a.innerDiameter + minBprime;
				rhs = rhs / patterns.getB(i,j);
				rhs = Math.floor(rhs);
				double lhs = 0.0 + b.startingPosition - a.startingPosition + b.innerDiameter;
				lhs = lhs / patterns.getB(i,j);
				lhs = lhs - 1;
				//System.out.println(a);
				//System.out.println(b);
				//System.out.println(lhs + " - " + rhs);
				if (rhs < lhs) {
					rhs = 0.0 + a.startingPosition - b.startingPosition - b.innerDiameter + minBprime;
					rhs = rhs / patterns.getB(i,j);
					rhs = Math.floor(rhs);
					lhs = 0.0 + a.startingPosition - b.startingPosition + a.innerDiameter;
					lhs = lhs / patterns.getB(i,j);
					lhs = lhs - 1;
					if (rhs < lhs)
						return false;
				}
			}
		return true;
	}
	*/
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 */
	public static void findKsFromStartingPositions(PatternSet patterns) {
		long[][] ks = new long[patterns.numPatterns()][patterns.numPatterns()];
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++) {
				//Pattern a = (patterns.getPattern(i).startingPosition < patterns.getPattern(j).startingPosition ? patterns.getPattern(i) : patterns.getPattern(j));
				//Pattern b = (patterns.getPattern(i).startingPosition < patterns.getPattern(j).startingPosition ? patterns.getPattern(j) : patterns.getPattern(i));
				Pattern a = patterns.getPattern(i);
				Pattern b = patterns.getPattern(j);
				//double rhs = 0.0 + b.getStartingPosition() - a.getStartingPosition() - patterns.getD(j, i);
				double rhs = 0.0 + b.getStartingPosition() - a.getStartingPosition();
				rhs = rhs / patterns.getB(i,j);
				ks[i][j] = (long)Math.floor(rhs);
				ks[j][i] = (long)Math.floor(rhs);
			}
		patterns.setSolution(ks);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @return
	 */
	public static boolean testInLineOrdering(PatternSet patterns) {
		int dSum = 0;
		int[] leftSum = new int[patterns.numPatterns()];
		for (int i = 0; i < patterns.numPatterns(); i++) {
			leftSum[i] = dSum;
			patterns.setPatternStart(i, dSum);
			dSum += patterns.getPattern(i).getInnerDiameter();
		}
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++) {
				double lhs = 0d + leftSum[j] - leftSum[i] - patterns.getPattern(i).getInnerDiameter();
				lhs = lhs % patterns.getB(i, j);
				if (lhs > patterns.getBB(i, j))
					return false;
			}
		return true;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @return
	 */
	public static boolean findInLineOrdering(PatternSet patterns) {
		// Set the flag to false
		boolean found = false;
		// Create a permutation object (for iterating over all permutations)
		Permutation perm = new Permutation(patterns.numPatterns());
		// Grab an iterator from the permutation object
		Iterator<int[]> iterator = perm.iterator(); 
		// While we still have permutations and we have not yet found a solution
		while ((iterator.hasNext()) && (!found)) {
			// Get the next permutation
			int[] currentPerm = iterator.next();
			// Permute the patterns to the current permutation
			patterns.setPermutation(currentPerm);
			// Check if this ordering is Inline-feasible 
			if (testInLineOrdering(patterns)) {
				found = true;
				// If so, set the PatternSet's solution to the one just found
				patterns.setZeroSolution();
			}
		}
		//if (found)
			//System.out.println("Number of permutations until inline-feasible:" + i);
		// Return the flag
		return found;
	}
	
	/**
	 * Finds a zero-feasible ordering of the given patterns (if such an order exists).
	 * It does this by checking all permutations of the patterns until a zero-feasible
	 * order is found. If a zero-feasible solution is found then this method stores the
	 * solution in the PatternSet object.
	 * 
	 * @param patterns The set of patterns to be tested
	 * @return <code>true</code> if a zero-feasible was found, <code>false</code> else.
	 */
	public static boolean findZeroOrdering(PatternSet patterns) {
		// Set the flag to false
		boolean found = false;
		// Create a permutation object (for iterating over all permutations)
		Permutation perm = new Permutation(patterns.numPatterns());
		// Grab an iterator from the permutation object
		Iterator<int[]> iterator = perm.iterator(); 
		// While we still have permutations and we have not yet found a solution
		while ((iterator.hasNext()) && (!found)) {
			// Get the next permutation
			int[] currentPerm = iterator.next();
			// Permute the patterns to the current permutation
			patterns.setPermutation(currentPerm);
			// Check if this ordering is zero-feasible 
			if (checkZeroOrdering(patterns)) {
				found = true;
				// If so, set the PatternSet's solution to the one just found
				patterns.setZeroSolution();
			}
		}
		// Return the flag
		return found;
	}
	
	/**
	 * Checks a set of patterns in its given order to see whether or not it 
	 * is zero-feasible
	 * 
	 * @param patterns The patterns in the order to be checked
	 * @return <code>true</code> if the patterns in their given order are 
	 * 			zero-feasible, <code>false</code> else.
	 */
	public static boolean checkZeroOrdering(PatternSet patterns) {
		// For all possible pairs of patterns
		for (int i = 1; i < patterns.numPatterns(); i++) {
    		for (int j = 0; j < i; j++) {
    			double sum = 0;
    			// Get the sum of d_i's between them
    			for (int l = j; l < i + 1; l++) {
    				sum = sum + patterns.getPattern(l).getInnerDiameter();
    			}
    			// Check whether B_{ij}\ge\sum_{i\preceq\ell\preceq j}{d_\ell}
    			if (patterns.getB(i, j) < sum)
    				return false;
    		}
    	}
    	return true;
	}
	
	/**
	 * This main method is only used for testing this class
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) throws IOException {
		int instance = Integer.parseInt(args[0]);
		int numPatterns = Integer.parseInt(args[1]);
		int numSet = Integer.parseInt(args[2]);
		long numPermutations = Long.parseLong(args[3]);
		long numInstances = (Permutation.factorial[numPatterns] / numPermutations) + 1;
		long startPermutation = (instance % numInstances) * numPermutations;
		BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
		java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\s*(\\d*,\\d*,\\d*,\\d*.\\d*)\\s*");
		CircularPattern[] patterns = new CircularPattern[numPatterns];
		for (int i = 0; i <= numSet; i++) {
			for (int j = 0; j < numPatterns;) {
				String patternString = consoleReader.readLine();
				Matcher m = p.matcher(patternString);
				if (m.find()) {
					patterns[j] = new CircularPattern(m.group());
					j++;
				}
			}
		}
		PatternSet set = new CircularPatternSet(patterns);
		System.out.println("Instance Number: " + instance);
		System.out.println("Set Number: " + numSet);
		System.out.println(set);
		System.out.println("Permutations: " + startPermutation + " - " + (startPermutation + numPermutations - 1));
		Iterator<int[]> permutationIterator = Permutation.iterator(numPatterns, startPermutation);
		int permutationNumber = 0;
		long startTime = System.currentTimeMillis();
		while ((permutationNumber < numPermutations) && permutationIterator.hasNext()) {
			permutationNumber++;
			set.permute(permutationIterator.next());
			if (checkZeroOrdering(set)) {
				System.out.println("Zero Ordering Found:");
				System.out.println(set);
				System.out.println("====================");
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Processing Time: " + ((endTime - startTime) / 1000) + "sec");
	}
	
	
}
