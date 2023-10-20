/**
 * HeuristicTester.java
 * Jul 31, 2010
 */
package heuristics;

import java.io.IOException;
import java.util.Iterator;

import problem.CircularPatternSet;
import exact.FitNetwork;
import exact.GurobiFitSolver;
import exact.FeasibilityTest;
import util.BottleneckMonge;
import util.IntegerUtils;
import util.MersenneTwisterFast;
import util.Permutation;

/**
 * This code is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * This class is for testing the heuristics. It generates examples and then tests the
 * heuristics on them 
 *
 * @author Brad Paynter
 * @version Jul 31, 2010
 *
 */
public class HeuristicTester {
	/**
	 * The Kruskal Heuristic
	 */
	private KruskalHeuristic kruskal;
	/**
	 * The Prim Heuristic
	 */
	private PrimHeuristic prim;
	/**
	 * The Gurobi Solver
	 */
	private GurobiFitSolver gurobi;
	/**
	 * The Random Number Generator
	 */
	private MersenneTwisterFast random;
	/**
	 * The factorization structure
	 */
	private IntegerUtils integers;
	
	/**
	 * Constructs an empty HeuristicTester object
	 *
	 */
	public HeuristicTester() {
		integers = new IntegerUtils(100);
	}
	
	/**
	 * Generates a set of patterns of size <code>numTests</code>, then tests
	 * each of the heuristics. If no solution is found it then tries to brute force
	 * a zero-ordering. Finally, if no zero-feasible solution exists, it will use 
	 * Gurobi to try and find a non-zero solution.
	 * 
	 * @param numTests
	 * @param numPatterns
	 */
	public void runHeuristicTester(int numTests, int numPatterns, long index) throws Exception {
		String endln = System.getProperty("line.separator");
		long seed = (index / 130768l) + 1;
		FitNetwork net = new FitNetwork(numPatterns);
		// Initialize the random number generator
		random = new MersenneTwisterFast(64783678397689l * numPatterns * seed);
		// Initialize the counters
		int primBasicSolutions = 0;
		int primModifiedSolutions = 0;
		int kruskalSolutions = 0;
		int zeroSolutions = 0;
		int totalSolutions = 0;
		int results = 0;
		int bottleneckMonge = 0;
		
		//int[] kruskalDist = new int[numPatterns];
		//int[] basicPrimDist = new int[numPatterns];
		//int[] modifiedPrimDist = new int[numPatterns];
		//Arrays.fill(kruskalDist, 0);
		//Arrays.fill(basicPrimDist, 0);
		//Arrays.fill(modifiedPrimDist, 0);
		
		for (int i = 0; i < numTests; i++) {
			/*
			System.out.println("##############################################");
			System.out.println("Number of patterns: " + numPatterns);
			System.out.println("Test Number: " + i + " out of " + numTests);
			*/
			// Get a set of patterns
			CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 1, 1, 5, 3, random, integers);
			boolean valid = false;
			while (!valid) {
				valid = true;
				try {
					patterns = CircularPatternSet.randomTightPatternSet(numPatterns, 1, 1, 100, 3, random, integers);
				} catch (IllegalArgumentException e) {
					valid = false;
				}
			}
			//System.out.println(patterns);
			boolean zeroSolution = false;
			// Run Kruskal Heuristic
			
			kruskal = new KruskalHeuristic(patterns);
			kruskal.runHeuristic();
			//kruskalDist[kruskal.outputSolution().length - 1]++;
			//System.out.print("Kruskal Solution? ");
			if (kruskal.outputSolution().length == 1) {
				//System.out.println("Yes");
				//System.out.println(kruskal.printSolution());
				kruskalSolutions++;
				zeroSolution = true;
			} //else
				//System.out.println("No");
			
			// Run Basic Prim Heuristic
			prim = new PrimHeuristic(patterns);
			prim.smallestStart();
			prim.runBasicHeuristic();
			// basicPrimDist[prim.outputSolution().length - 1]++;
			//System.out.print("Basic Prim Solution? ");
			
			if (prim.outputSolution().length == patterns.numPatterns()) {
				primBasicSolutions++;
				zeroSolution = true;
				//System.out.println("Yes");
				//System.out.println(prim.printSolution());
			} //else
				//System.out.println("No");
			// Run Modified Prim Heuristic
			//Pattern[] primSolution = new Pattern[0];
			int j = 0;
			boolean done = false;
			while ((j < numPatterns) && (!done)) {
				prim.givenStart(j++);
				prim.runModifiedHeuristic();
				if (prim.outputSolution().length == numPatterns) {
					zeroSolution = true;
					done = true;
					primModifiedSolutions++;
				}
				/*
				Pattern[] solution = prim.outputSolution();
				if (solution.length > primSolution.length)
					primSolution = solution;
				if (primSolution.length == numPatterns)
					zeroSolution = true;
					*/
			}
			done = false;
			Permutation perm = new Permutation(patterns.numPatterns());
			Iterator<int[]> iter = perm.iterator();
			while (iter.hasNext() && !done) {
				patterns.setPermutation(iter.next());
				// Check if this ordering is zero-feasible
				if (BottleneckMonge.bottleneckMonge(patterns)) { 
					System.out.println(patterns.printBs());
					//System.in.read();
					done = true;
					if (FeasibilityTest.checkZeroOrdering(patterns)) {
						zeroSolution = true;
						bottleneckMonge++;
					}
				}
			}
			
			if (!zeroSolution) {
				perm = new Permutation(patterns.numPatterns());
				iter = perm.iterator();
				while (iter.hasNext() && !zeroSolution) {
					patterns.setPermutation(iter.next());
					// Check if this ordering is zero-feasible 
					if (FeasibilityTest.checkZeroOrdering(patterns)) {
						//System.out.println(patterns.printBs());
						zeroSolutions++;
						zeroSolution = true;
					}
				}
			} else
				zeroSolutions++;
			
			/*
			if (zeroSolution)
				modifiedPrimDist[primSolution.length - 1]++;
			*/
			
			/*
			if (primSolution.length < numPatterns) {
				Permutation perm = new Permutation(patterns.numPatterns());
				for (int[] currentPerm : perm) {
					patterns.permute(currentPerm);
					// Check if this ordering is zero-feasible 
					if (ZeroOrdering.checkZeroOrdering(patterns)) {
						if (!zeroSolution) {
							zeroSolutions++;
							System.out.println("##############################################");
							System.out.println("Prim Heuristic Solution:");
							for (Pattern p : primSolution)
								System.out.println(p);
						}
						zeroSolution = true;
						// If so, set the PatternSet's solution to the one just found
						System.out.println("Zero Solution:");
						System.out.println(patterns);
					}
				}
			} else {
				zeroSolutions++;
				primModifiedSolutions++;
			}
			*/
			/*
			if (primSolution.length == numPatterns - 2) {
				i++;
				System.out.println(patterns);
				*/
				/*
				zeroSolution = false;
				j = 0;
				Iterator<int[]> permutationIterator = Permutation.iterator(numPatterns, index % 130768l * 10000000l);
				while ((j < 10000000) && !zeroSolution && permutationIterator.hasNext()) {
					j++;
					patterns.permute(permutationIterator.next());
					// patterns.permute(Permutation.randomPermutation(numPatterns, random));
					// Check if this ordering is zero-feasible 
					if (ZeroOrdering.checkZeroOrdering(patterns)) {
						if (!zeroSolution) {
							zeroSolutions++;
							System.out.println("##############################################");
							System.out.println("Prim Heuristic Solution:");
							for (Pattern p : primSolution)
								System.out.println(p);
						}
						results++;
						zeroSolution = true;
						// If so, set the PatternSet's solution to the one just found
						System.out.println("Zero Solution:");
						System.out.println(patterns);
					}
				}
				
			}
			*/
			//modifiedPrimDist[prim.outputSolution().length - 1]++;
			/*
			System.out.print("Modified Prim Solution? ");
			if (prim.outputSolution().length == patterns.numPatterns()) {
				primModifiedSolutions++;
				zeroSolution = true;
				System.out.println("Yes");
				System.out.println(prim.printSolution());
			} else
				System.out.println("No");
			*/
			// If any of the heuristics found a solution, we're done
			/*
			System.out.print("Zero Solution? ");
			if (zeroSolution) {
				zeroSolutions++;
				totalSolutions++;
				//patterns.setZeroSolution();
				//net.testSolution(patterns);
				System.out.println("Yes");
				System.out.println("Solution? Yes");
			// Otherwise, check for a zero-feasible ordering the heuristics
			// may have missed
			} else if (ZeroOrdering.findZeroOrdering(patterns)) {
				zeroSolutions++;
				totalSolutions++;
				zeroSolution = true;
				net.testSolution(patterns);
				System.out.println("Yes");
				System.out.println("Solution? Yes");
			// Else, use Gurobi to find a solution
			} else {
				System.out.println("No");
				gurobi = new GurobiFitSolver(patterns);
				if (gurobi.solve()) {
					if (!gurobi.checkSolution()) {
						System.out.print("Solution? No");
						System.out.println(gurobi.printSolution());
						gurobi.saveModel("ScrewUp.lp");
						System.out.println(patterns.printSolution());
						//throw new Exception("Gurobi screwed up!");
					} else {
						System.out.println("Solution? Yes");
						totalSolutions++;
					}
				} else
					System.out.println("Solution? No");
			}
			System.out.println(patterns);
			*/
		}
		// Output results
		/*
		System.out.println("******************************************");
		System.out.print("Kruskal: \t");
		for (int i : kruskalDist)
			System.out.print(i + "\t");
		System.out.println();
		System.out.print("Basic Prim: \t");
		for (int i : basicPrimDist)
			System.out.print(i + "\t");
		System.out.println();
		System.out.print("Modified Prim: \t");
		for (int i : modifiedPrimDist)
			System.out.print(i + "\t");
		System.out.println();
		*/
		
		System.out.println("Kruskal Heuristic found " + kruskalSolutions + " solutions,");
		System.out.println("Basic Prim Heuristic found " + primBasicSolutions + " solutions,");
		System.out.println("Modified Prim Heuristic found " + primModifiedSolutions + " solutions");
		System.out.println("out of a total of " + zeroSolutions + " zero-feasible solutions");
		System.out.println("with " + bottleneckMonge + " bottleneck Monge solutions.");
		//System.out.println("out of a total of " + totalSolutions + " solutions");
		/*
		System.out.println(results + " results");
		System.out.println("out of a total of " + numTests + " tests.");
		*/
		//for (int i = 0; i < numPatterns; i++)
			//System.out.println(i + ": " + modifiedPrimDist[i]);
	}
	
	/**
	 * User interface for this class. Asks the user for the parameters of
	 * the runHeuristicTester method and then runs the method
	 * 
	 * @param args Not used
	 * @throws IOException Thrown if an IO error occurs in the Input Stream
	 */
	public static void main(String[] args) throws Exception {
		int numTests = Integer.parseInt(args[0]);
		int numPatterns = Integer.parseInt(args[1]);
		long index = Long.parseLong(args[2]);
		HeuristicTester tester = new HeuristicTester();
		tester.runHeuristicTester(numTests, numPatterns, index);
	}
}
