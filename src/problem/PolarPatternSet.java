/**
 * PolarPatternSet.java
 * Oct 25, 2011
 */
package problem;

import heuristics.BinPackingHeuristic;
import heuristics.SetPartition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.io.*;

import exact.FitNetwork;
import exact.GurobiMinTemplatesSolver;

import util.IntegerUtils;
import util.MersenneTwisterFast;
import util.Permutation;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Oct 25, 2011
 *
 */
public class PolarPatternSet implements PSPatternSet {
	
	private PolarPattern[] patterns;
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
	private PolarPatternSet(PolarPattern[] patterns, long[][] gcd) {
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
	public PolarPatternSet(PolarPattern[] patterns) {
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
	 * @param patternString
	 */
	public PolarPatternSet(String patternString) {
		ArrayList<PolarPattern> inputPatterns = new ArrayList<PolarPattern>();
		Matcher m = java.util.regex.Pattern.compile("\\s*(\\d*,\\d*,\\d*,\\d*[.]?\\d*)\\s*|\\s*(\\d*,\\d*[.]?\\d*,\\d*)\\s*").matcher(patternString);
		while (m.find())
			inputPatterns.add(new PolarPattern(m.group()));
		this.patterns = inputPatterns.toArray(new PolarPattern[1]);
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
	 * @param minR
	 * @param maxR
	 * @param minN
	 * @param maxN
	 * @param minD
	 * @param maxD
	 * @param random
	 * @return
	 */
	public static PolarPatternSet randomPatternSet(int numPatterns, int minR, int maxR, int minN, int maxN, int minD, int maxD, MersenneTwisterFast random) {
		PolarPattern[] patterns = new PolarPattern[numPatterns];
		for (int i = 0; i < numPatterns; i++) {
			int R = random.nextInt(maxR - minR) + minR;
			int n = random.nextInt(maxN - minN) + minN;
			int d = random.nextInt(maxD - minD) + minD;
			patterns[i] = new PolarPattern(R, n, d);
		}
		return new PolarPatternSet(patterns);
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
	 * @throws IllegalArgumentException
	 */
	public PolarPattern getPattern(int i)  throws IllegalArgumentException {
		PolarPattern outPattern;
		try {
			outPattern = patterns[permutation[i]];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Pattern Index out of bounds.");
		}
		return outPattern;
	}

	/**
	 * TODO
	 * 
	 * @param indices
	 * @return
	 */
	public PolarPatternSet subset(int[] indices)throws IllegalArgumentException {
		if (indices.length > patterns.length)
			throw new IllegalArgumentException("Number of indices exceeds number of patterns in this set.");
		for (int i = 0; i < indices.length; i++)
			for (int j = i+1; j < indices.length; j++)
				if (indices[i] == indices[j])
					throw new IllegalArgumentException("Duplicate Indices: " + i + " & " + j);
		PolarPattern[] newPatterns = new PolarPattern[indices.length];
		long[][] newGCDs = new long[indices.length][indices.length];
		for (int i = 0; i < indices.length; i++) {
			newPatterns[i] = patterns[permutation[indices[i]]];
			for (int j = 0; j < indices.length; j++)
				newGCDs[i][j] = gcd[permutation[indices[i]]][permutation[indices[j]]];
		}
		return new PolarPatternSet(newPatterns, newGCDs);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public PolarPatternSet clone() {
		PolarPattern[] newPatterns = new PolarPattern[patterns.length];
		long[][] newGCD = new long[patterns.length][patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			newPatterns[i] = patterns[i].clone();
			newGCD[i] = gcd[i].clone();
		}
		PolarPatternSet outputSet = new PolarPatternSet(newPatterns, newGCD);
		outputSet.setPermutation(permutation.clone());
		outputSet.solution = solution.clone();
		return outputSet;
	}
	
	/**
	 * TODO
	 * 
	 * @param p
	 * @param position
	 */
	public void addPattern(Pattern pattern, int position)  throws IllegalArgumentException {
		if (pattern == null)
			throw new IllegalArgumentException("Null patterns are not allowed.");
		if ((position < 0) || (position > patterns.length))
			throw new IllegalArgumentException("Invalid insertion position.");
		if (pattern instanceof PolarPattern) {
			PolarPattern p = (PolarPattern)pattern;
			PolarPattern[] newPatterns = new PolarPattern[patterns.length + 1];
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
			throw new IllegalArgumentException("This set can only accept Polar Patterns");
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
	 * TODO
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public double getB(int i, int j) {
		return 2d * Math.PI / lcm(i, j);
	}

	/**
	 * TODO
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public double getBB(int i, int j) {
		return getB(i,j) - 2d * getD(i, j);
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
		PolarPattern a = getPattern(permutation[i]);
		PolarPattern b = getPattern(permutation[j]);
		if (Math.abs(a.outerCircleRadius - b.outerCircleRadius) >= (a.innerCircleRadius + b.innerCircleRadius))
			return 0d;
		double numerator = (a.innerCircleRadius + b.innerCircleRadius) * (a.innerCircleRadius + b.innerCircleRadius);
		numerator -= (a.outerCircleRadius - b.outerCircleRadius) * (a.outerCircleRadius - b.outerCircleRadius);
		double denomenator = 4d * a.outerCircleRadius * b.outerCircleRadius;
		double ratio = numerator / denomenator;
		double squareRoot = Math.sqrt(ratio);
		double arcSine = Math.asin(squareRoot);
		return 2d * arcSine;
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
	 * TODO
	 * 
	 */
	public void setZeroSolution() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Zero-fesibility is undefined for Polar Patterns");
	}

	/**
	 * TODO
	 * 
	 * @param permutation
	 */
	public void setZeroSolution(int[] permutation) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Zero-fesibility is undefined for Polar Patterns");
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
			patterns[permutation[i]].startingAngle = start[i];
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
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public int densityBinLowerBound() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Bin Lower Lound has no definition for Polar Patterns.");
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public int korstBinLowerBound() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Bin Lower Lound has no definition for Polar Patterns.");
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
			String[] colors = {"blue", "green", "red", "orange", "yellow", "red!50!blue", "brown", "pink", "gray", "green!60!black",
								"blue!60!white", "black", "brown!60!white", "yellow!60!black"};
			for (int i = 0; i < patterns.length; i++) {
				PolarPattern p = patterns[i];
				s += "\\polarpattern{" + (int)p.outerCircleRadius + "}{" + p.numberOfHoles + "}{";
				s += (int)p.innerCircleRadius + "}{" + p.startingAngle + "}{" + colors[i] + "};" + endln;
			}
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
	 * TODO
	 * 
	 * @param pattern
	 * @param start
	 */
	public void setPatternStart(int pattern, double start) throws IllegalArgumentException {
		getPattern(pattern).startingAngle = start;
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
		String[] colors = {"blue", "green", "red", "orange", "yellow", "red!50!blue", "brown", "pink", "gray", "green!60!black",
				"blue!60!white", "black", "brown!60!white", "yellow!60!black", null, null, null, null, null, null, null, null, null, null};
		for (int i = 0; i < patterns.length; i++) {
			PolarPattern p = patterns[i];
			s += "\\polarpattern{" + (int)p.outerCircleRadius + "}{" + p.numberOfHoles + "}{";
			s += (int)p.innerCircleRadius + "}{" + p.startingAngle + "}{" + colors[i] + "};" + endln;
		}
		return s;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		MersenneTwisterFast random = new MersenneTwisterFast(5458934583l);
		String endln = System.getProperty("line.separator");
		File f = new File("IndustryExample.txt");
		BufferedReader reader = new BufferedReader(new FileReader(f));
		ArrayList<PolarPattern> patternArray = new ArrayList<PolarPattern>();
		while (reader.ready()) {
			String s = reader.readLine();
			//System.out.println(s);
			PolarPattern p = new PolarPattern(s);
			p.outerCircleRadius = p.outerCircleRadius / 2;
			p.innerCircleRadius = p.innerCircleRadius / 2;
			patternArray.add(p);
		}
		reader.close();
		PolarPatternSet patterns = new PolarPatternSet(patternArray.toArray(new PolarPattern[0]));
		//patterns.setPermutation(Permutation.randomPermutation(patterns.numPatterns(), random));
		//SetPartition.generateIncidenceGraph(patterns, new File("IndustryExampleGraph.clq.b"));
		//System.out.println(patterns);
		System.out.println("Lower Bound: " + SetPartition.indexPartition(patterns).length);
//		long startTime = System.nanoTime();
//		BinPackingHeuristic binPacker = new BinPackingHeuristic(patterns);
//		binPacker.binType = BinPackingHeuristic.fifoShortestPath;
//		binPacker.setComplexity(0);
//		binPacker.runFirstFitHeuristic();
//		long endTime = System.nanoTime();
//		System.out.println("Total Running Time: " + 1.0d * (endTime - startTime) / 1000000000l + " seconds");
//		if (!binPacker.checkSolution())
//			throw new RuntimeException("Heuristic Screwed Up!!!");
//		System.out.println(binPacker.printSolution());
//		
		GurobiMinTemplatesSolver solver = new GurobiMinTemplatesSolver();
		solver.setPatterns(patterns, 6);
		solver.solve();
		int[][] solution = solver.outputSolution();
		String s = "";
		for (int[] template : solution) {
			for (int i : template)
				s += i + "-";
			s += endln;
		}
		patterns.setPatternStart(solver.outputStartingPositions());
		for (int i = 0; i < solution.length; i++) {
			System.out.println("Template #" + i);
			System.out.println(patterns.subset(solution[i]));
			System.out.println("++++++++++++++++++++++++++++++++++++++++");
		}
		
		//System.out.println("Maximum Independent Set: " + s);
		/*
		double epsilon = 0.05;
		PolarPattern[] patternArray = {new PolarPattern(12,3,1+epsilon), new PolarPattern(11.5,4,1+epsilon), new PolarPattern(12.5,5,1+epsilon)};
		PolarPatternSet patterns = new PolarPatternSet(patternArray);
		long[][] ks = {{0,0,1},{0,0,1},{1,1,0}};
		patterns.setSolution(ks);
		FitNetwork fit = new FitNetwork(3);
		fit.testSolution(patterns);
		System.out.println(patterns);
		*/
	}

}
