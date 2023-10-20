/**
 * DissertationResults.java
 * Mar 27, 2012
 */
package heuristics;

import java.util.ArrayList;
import java.util.Arrays;

import problem.CircularPatternSet;
import problem.LinearPatternSet;
import problem.PatternSet;
import problem.PolarPatternSet;
import util.IntegerUtils;
import util.MersenneTwisterFast;
import util.Permutation;
import exact.FeasibilityTest;
import exact.FitNetwork;
import exact.GurobiFitSolver;
import exact.GurobiMinTemplatesSolver;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Mar 27, 2012
 *
 */
public class DissertationResults {
	public static void kruskalPrim() {
		int numTests = 10000;
		for (int numPatterns = 3; numPatterns <= 10; numPatterns++) {
			MersenneTwisterFast random = new MersenneTwisterFast(2950420534l * numPatterns * 2); // When finding publication numbers Add multiplier to seed
			//MersenneTwisterFast permutationRandomizer = new MersenneTwisterFast(548257483758l * numPermutations);
			CycleHeuristic heuristic;
//			GurobiFitSolver solver;
			int successes = 0;
			int solutions = 0;
			int zeroSolutions = 0;
			int inLineSolutions = 0;
			int kruskalCounter1 = 0;
			int kruskalCounter2 = 0;
			int primCounter1 = 0;
			int primCounter2 = 0;
			int primCounter3 = 0;
			IntegerUtils integers = new IntegerUtils(1000);
			FitNetwork net = new FitNetwork(numPatterns);
			for (int i = 0; i < numTests; i++) {
				//System.out.println("Test #" + i);
				CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers); //was d\in[1,30]
				boolean valid = false;
				while (!valid) {
					valid = true;
					try {
						patterns = CircularPatternSet.randomTightPatternSet(numPatterns, 30, 1, 100, 3, random, integers); //was d\in[1,30] n\in[3,100]
					} catch (IllegalArgumentException e) {
						valid = false;
					}
				}
				KruskalHeuristic kruskalZero = new KruskalHeuristic(patterns);
				kruskalZero.feasibilityType = KruskalHeuristic.zeroFeasibleOrdering;
				KruskalHeuristic kruskalContiguous = new KruskalHeuristic(patterns);
				kruskalContiguous.feasibilityType = KruskalHeuristic.contiguousOrdering;
				PrimHeuristic primZero = new PrimHeuristic(patterns);
				primZero.feasibilityType = PrimHeuristic.zeroFeasibleOrdering;
				PrimHeuristic primContiguous = new PrimHeuristic(patterns);
				primContiguous.feasibilityType = PrimHeuristic.contiguousOrdering;
				kruskalZero.runHeuristic();
				int[][] kruskalZeroSolution = kruskalZero.outputSolutionIndices();
				if (kruskalZeroSolution.length == 1)
					kruskalCounter1++;
				kruskalContiguous.runHeuristic();
				int[][] kruskalContiguousSolution = kruskalContiguous.outputSolutionIndices();
				if (kruskalContiguousSolution.length == 1)
					kruskalCounter2++;
				if (primZero.runModifiedAllStart() == patterns.numPatterns())
					primCounter1++;
				if (primContiguous.runModifiedAllStart() == patterns.numPatterns())
					primCounter2++;
//				if (FeasibilityTest.findZeroOrdering(patterns))
//					zeroSolutions++;
//				if (FeasibilityTest.findInLineOrdering(patterns))
//					inLineSolutions++;
			}
			System.out.println("With " + numPatterns + " patterns per set, ");
			//System.out.println("CycleFinder succeeded " + successes + " times out of "+ numTests + " tests.");
			System.out.println("Prim found " + primCounter1 + " out of " + zeroSolutions + " zero-feasible solutions, and " + primCounter2 + " out of " + inLineSolutions + " inline-feasible solutions out of "+ numTests + " tests.");
			System.out.println("Kruskal found " + kruskalCounter1 + " out of " + zeroSolutions + " zero-feasible solutions, and " + kruskalCounter2 + " out of " + inLineSolutions + " inline-feasible solutions out of "+ numTests + " tests.");
			//System.out.println("Kruskal succeeded " + kruskalCounter1 + " times, Basic prim " + primCounter1 + " times, Modified Prim " + primCounter2 + " times and AllStart Modified Prim " + primCounter3 + " times.");
			
		}
	}
	
	public static void prim() {
		int numTests = 10000;
		for (int numPatterns = 3; numPatterns <= 10; numPatterns++) {
			MersenneTwisterFast random = new MersenneTwisterFast(54756378683l * numPatterns * 2); // When finding publication numbers Add multiplier to seed
			MersenneTwisterFast random2 = new MersenneTwisterFast(67840289052l * numPatterns * 2);
			//CycleHeuristic heuristic;
//			GurobiFitSolver solver;
			int successes = 0;
			int solutions = 0;
			int zeroSolutions = 0;
			int inLineSolutions = 0;
			int kruskalCounter1 = 0;
			int kruskalCounter2 = 0;
			int primCounter1 = 0;
			int primCounter2 = 0;
			int primCounter3 = 0;
			IntegerUtils integers = new IntegerUtils(1000);
			FitNetwork net = new FitNetwork(numPatterns);
			for (int i = 0; i < numTests; i++) {
				//System.out.println("Test #" + i);
				CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers); //was d\in[1,30]
				boolean valid = false;
				while (!valid) {
					valid = true;
					try {
						patterns = CircularPatternSet.randomTightPatternSet(numPatterns, 30, 1, 100, 3, random, integers); //was d\in[1,30] n\in[3,100]
					} catch (IllegalArgumentException e) {
						valid = false;
					}
				}
				PrimHeuristic primSmall = new PrimHeuristic(patterns);
				primSmall.feasibilityType = PrimHeuristic.contiguousOrdering;
				primSmall.smallestStart();
				PrimHeuristic primRandom = new PrimHeuristic(patterns);
				primRandom.feasibilityType = PrimHeuristic.contiguousOrdering;
				primRandom.randomStart(random2);
				PrimHeuristic primAll = new PrimHeuristic(patterns);
				primAll.feasibilityType = PrimHeuristic.contiguousOrdering;
				if (primSmall.runModifiedHeuristic() == patterns.numPatterns())
					primCounter1++;
				if (primRandom.runModifiedHeuristic() == patterns.numPatterns())
					primCounter2++;
				if (primAll.runModifiedAllStart() == patterns.numPatterns())
					primCounter3++;
				if (FeasibilityTest.findInLineOrdering(patterns))
					inLineSolutions++;
			}
			System.out.println("With " + numPatterns + " patterns per set, ");
			//System.out.println("CycleFinder succeeded " + successes + " times out of "+ numTests + " tests.");
			System.out.println("Prim (Smallest Start) found " + primCounter1 + " out of " + inLineSolutions + " inline-feasible solutions out of "+ numTests + " tests.");
			System.out.println("Prim (Random Start) found " + primCounter2 + " out of " + inLineSolutions + " inline-feasible solutions out of "+ numTests + " tests.");
			System.out.println("Prim (All Start) found " + primCounter3 + " out of " + inLineSolutions + " inline-feasible solutions out of "+ numTests + " tests.");
			//System.out.println("Kruskal found " + kruskalCounter1 + " out of " + zeroSolutions + " zero-feasible solutions, and " + kruskalCounter2 + " out of " + inLineSolutions + " inline-feasible solutions out of "+ numTests + " tests.");
			//System.out.println("Kruskal succeeded " + kruskalCounter1 + " times, Basic prim " + primCounter1 + " times, Modified Prim " + primCounter2 + " times and AllStart Modified Prim " + primCounter3 + " times.");
			
		}
	}
	
	public static void cycleFinders() {
		int numTests = 10000;
		for (int numPatterns = 3; numPatterns <= 10; numPatterns++) {
			MersenneTwisterFast random = new MersenneTwisterFast(39452899l * numPatterns * 2); // When finding publication numbers Add multiplier to seed
			//MersenneTwisterFast random2 = new MersenneTwisterFast(67840289052l * numPatterns * 2);
			//CycleHeuristic heuristic;
//			GurobiFitSolver solver;
			int successes = 0;
			int solutions = 0;
			int zeroSolutions = 0;
			int inLineSolutions = 0;
			int kruskalCounter1 = 0;
			int kruskalCounter2 = 0;
			int cycleCounter1 = 0;
			int cycleCounter2 = 0;
			int cycleCounter3 = 0;
			IntegerUtils integers = new IntegerUtils(1000);
			FitNetwork net = new FitNetwork(numPatterns);
			for (int i = 0; i < numTests; i++) {
				//System.out.println("Test #" + i);
				CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers); //was d\in[1,30]
				boolean valid = false;
				while (!valid) {
					valid = true;
					try {
						patterns = CircularPatternSet.randomTightPatternSet(numPatterns, 30, 1, 100, 3, random, integers); //was d\in[1,30] n\in[3,100]
					} catch (IllegalArgumentException e) {
						valid = false;
					}
				}
				CycleHeuristic cycleFloyd = new CycleHeuristic(patterns);
				cycleFloyd.sortedAddition = false;
				cycleFloyd.allPositions = false;
				cycleFloyd.type = CycleHeuristic.floydWarshall;
				CycleHeuristic cycleMinMean = new CycleHeuristic(patterns);
				cycleMinMean.sortedAddition = false;
				cycleMinMean.allPositions = false;
				cycleMinMean.type = CycleHeuristic.minMeanCycle;
				CycleHeuristic cycleFIFO = new CycleHeuristic(patterns);
				cycleFIFO.sortedAddition = false;
				cycleFIFO.allPositions = false;
				cycleFIFO.type = CycleHeuristic.fifoShortestPath;
				int[] cycleFloydSolution = cycleFloyd.runIterativeHeuristic();
				if (cycleFloydSolution.length == patterns.numPatterns())
					cycleCounter1++;
				int[] cycleMinMeanSolution = cycleMinMean.runIterativeHeuristic();
				if (cycleMinMeanSolution.length == patterns.numPatterns())
					cycleCounter2++;
				int[] cycleFIFOSolution = cycleFIFO.runIterativeHeuristic();
				if (cycleFIFOSolution.length == patterns.numPatterns())
					cycleCounter3++;
				
			}
			System.out.println("With " + numPatterns + " patterns per set, ");
			//System.out.println("CycleFinder succeeded " + successes + " times out of "+ numTests + " tests.");
			System.out.println("Cycle (Floyd-Warshall) found " + cycleCounter1 + " out of " + numTests + " tests.");
			System.out.println("Cycle (Min Mean Cycle) found " + cycleCounter2 + " out of " + numTests + " tests.");
			System.out.println("Cycle (FIFO Shortest Path) found " + cycleCounter3 + " out of " + numTests + " tests.");
			//System.out.println("Kruskal found " + kruskalCounter1 + " out of " + zeroSolutions + " zero-feasible solutions, and " + kruskalCounter2 + " out of " + inLineSolutions + " inline-feasible solutions out of "+ numTests + " tests.");
			//System.out.println("Kruskal succeeded " + kruskalCounter1 + " times, Basic prim " + primCounter1 + " times, Modified Prim " + primCounter2 + " times and AllStart Modified Prim " + primCounter3 + " times.");
			
		}
	}
	
	public static void insertionOrders() {
		int numTests = 10000;
		for (int numPatterns = 3; numPatterns <= 10; numPatterns++) {
			MersenneTwisterFast random = new MersenneTwisterFast(8457258942l * numPatterns * 2); // When finding publication numbers Add multiplier to seed
			MersenneTwisterFast random2 = new MersenneTwisterFast(925843953l * numPatterns * 2);
			//CycleHeuristic heuristic;
//			GurobiFitSolver solver;
			int successes = 0;
			int solutions = 0;
			int zeroSolutions = 0;
			int inLineSolutions = 0;
			int kruskalCounter1 = 0;
			int kruskalCounter2 = 0;
			int cycleCounter1 = 0;
			int cycleCounter2 = 0;
			int cycleCounter3 = 0;
			int cycleCounter4 = 0;
			int cycleCounter5 = 0;
			IntegerUtils integers = new IntegerUtils(1000);
			FitNetwork net = new FitNetwork(numPatterns);
			for (int i = 0; i < numTests; i++) {
				//System.out.println("Test #" + i);
				CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers); //was d\in[1,30]
				boolean valid = false;
				while (!valid) {
					valid = true;
					try {
						patterns = CircularPatternSet.randomTightPatternSet(numPatterns, 30, 1, 100, 3, random, integers); //was d\in[1,30] n\in[3,100]
					} catch (IllegalArgumentException e) {
						valid = false;
					}
				}
				patterns.sortByIncreasingSize();
				CycleHeuristic cycleIncreasingSize = new CycleHeuristic(patterns);
				cycleIncreasingSize.sortedAddition = false;
				cycleIncreasingSize.allPositions = false;
				cycleIncreasingSize.type = CycleHeuristic.fifoShortestPath;
				
				int[] cycleIncreasingSizeSolution = cycleIncreasingSize.runIterativeHeuristic();
				if (cycleIncreasingSizeSolution.length == patterns.numPatterns())
					cycleCounter1++;
				patterns.sortByDecreasingSize();
				CycleHeuristic cycleDecreasingSize = new CycleHeuristic(patterns);
				cycleDecreasingSize.sortedAddition = false;
				cycleDecreasingSize.allPositions = false;
				cycleDecreasingSize.type = CycleHeuristic.fifoShortestPath;
				int[] cycleDecreasingSizeSolution = cycleDecreasingSize.runIterativeHeuristic();
				if (cycleDecreasingSizeSolution.length == patterns.numPatterns())
					cycleCounter2++;
				
				patterns.setPermutation(Permutation.randomPermutation(patterns.numPatterns(), random2));
				CycleHeuristic cycleRandomOrder = new CycleHeuristic(patterns);
				cycleRandomOrder.sortedAddition = false;
				cycleRandomOrder.allPositions = false;
				cycleRandomOrder.type = CycleHeuristic.fifoShortestPath;
				CycleHeuristic cyclePrimOrder = new CycleHeuristic(patterns);
				cyclePrimOrder.sortedAddition = true;
				cyclePrimOrder.allPositions = false;
				cyclePrimOrder.type = CycleHeuristic.fifoShortestPath;
				
				int[] cycleRandomOrderSolution = cycleRandomOrder.runIterativeHeuristic();
				if (cycleRandomOrderSolution.length == patterns.numPatterns())
					cycleCounter3++;
				cyclePrimOrder.smallestStart();
				int[] cyclePrimOrderSolution = cyclePrimOrder.runIterativeHeuristic();
				if (cyclePrimOrderSolution.length == patterns.numPatterns())
					cycleCounter4++;
				//cyclePrimOrder.smallestStart();
				cyclePrimOrderSolution = cyclePrimOrder.runIterativeHeuristicSmallestStart();
				if (cyclePrimOrderSolution.length == patterns.numPatterns())
					cycleCounter5++;
				
				
				
				
			}
			System.out.println("With " + numPatterns + " patterns per set, ");
			//System.out.println("CycleFinder succeeded " + successes + " times out of "+ numTests + " tests.");
			System.out.println("Cycle (Increasing Size) found " + cycleCounter1 + " out of " + numTests + " tests.");
			System.out.println("Cycle (Decreasing Size) found " + cycleCounter2 + " out of " + numTests + " tests.");
			System.out.println("Cycle (Random Order) found " + cycleCounter3 + " out of " + numTests + " tests.");
			System.out.println("Cycle (Prim Order) found " + cycleCounter4 + " out of " + numTests + " tests.");
			System.out.println("Cycle (Prim Order+) found " + cycleCounter5 + " out of " + numTests + " tests.");
			//System.out.println("Kruskal found " + kruskalCounter1 + " out of " + zeroSolutions + " zero-feasible solutions, and " + kruskalCounter2 + " out of " + inLineSolutions + " inline-feasible solutions out of "+ numTests + " tests.");
			//System.out.println("Kruskal succeeded " + kruskalCounter1 + " times, Basic prim " + primCounter1 + " times, Modified Prim " + primCounter2 + " times and AllStart Modified Prim " + primCounter3 + " times.");
			
		}
	}
	
	public static void fit() {
		int numTests = 10000;
		for (int numPatterns = 3; numPatterns <= 10; numPatterns++) {
			MersenneTwisterFast random = new MersenneTwisterFast(7548396893l * numPatterns * 2); // When finding publication numbers Add multiplier to seed
			MersenneTwisterFast random2 = new MersenneTwisterFast(925843953l * numPatterns * 2);
			//CycleHeuristic heuristic;
//			GurobiFitSolver solver;
			int kruskalCounter = 0;
			int primCounter = 0;
			int cycleCounter = 0;
			int gurobiCounter = 0;
			int esCounter = 0;
			int gurobiCorrectCounter = 0;
			
			IntegerUtils integers = new IntegerUtils(1000);
			FitNetwork net = new FitNetwork(numPatterns);
			for (int i = 0; i < numTests; i++) {
				//System.out.println("Test #" + i);
				CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers); //was d\in[1,30]
				boolean valid = false;
				while (!valid) {
					valid = true;
					try {
						patterns = CircularPatternSet.randomTightPatternSet(numPatterns, 30, 1, 100, 3, random, integers); //was d\in[1,30] n\in[3,100]
					} catch (IllegalArgumentException e) {
						valid = false;
					}
				}
				int[] permutation = Permutation.randomPermutation(patterns.numPatterns(), random2);
				
				/*
				patterns.setPermutation(permutation);
				PrimHeuristic prim = new PrimHeuristic(patterns);
				prim.feasibilityType = PrimHeuristic.contiguousOrdering;
				if (prim.runModifiedAllStart() == patterns.numPatterns())
					primCounter++;
				
				patterns.setPermutation(permutation);
				KruskalHeuristic kruskal = new KruskalHeuristic(patterns);
				kruskal.feasibilityType = KruskalHeuristic.contiguousOrdering;
				kruskal.runHeuristic();
				if (kruskal.outputSolutionIndices().length == 1)
					kruskalCounter++;
				
				patterns.setPermutation(permutation);
				CycleHeuristic cycle = new CycleHeuristic(patterns);
				cycle.type = CycleHeuristic.fifoShortestPath;
				cycle.sortedAddition = true;
				cycle.allPositions = false;
				if (cycle.runIterativeHeuristicSmallestStart().length == patterns.numPatterns())
					cycleCounter++;
				*/
				
				patterns.sortByDecreasingSize();
				EmptySpaceHeuristic es = new EmptySpaceHeuristic(patterns);
				if (es.runHeuristic().length == patterns.numPatterns())
					esCounter++;
				
				/*
				patterns.setPermutation(permutation);
				GurobiFitSolver gurobi = new GurobiFitSolver(patterns);
				if (gurobi.solve()) {
					gurobiCounter++;
					//System.out.println(gurobi.printSolution());
					if (gurobi.checkSolution())
						gurobiCorrectCounter++;
				}
				
				gurobi.dispose();
				*/
				
			}
			System.out.println("With " + numPatterns + " patterns per set, ");
			//System.out.println("CycleFinder succeeded " + successes + " times out of "+ numTests + " tests.");
			//System.out.println("Prim (Contiguous Order) (Smallest Start) found " + primCounter + " out of " + numTests + " tests.");
			//System.out.println("Kruskal (Contiguous Order) found " + kruskalCounter + " out of " + numTests + " tests.");
			//System.out.println("Cycle (Prim Order) (Both Smallest Starts) found " + cycleCounter + " out of " + numTests + " tests.");
			//System.out.println("Gurobi (10 min Limit) found " + gurobiCorrectCounter + " correct out of " + gurobiCounter + " solutions out of " + numTests + " tests.");
			System.out.println("EmptySpace found " + esCounter + " out of " + numTests + " tests.");
			//System.out.println("Kruskal found " + kruskalCounter1 + " out of " + zeroSolutions + " zero-feasible solutions, and " + kruskalCounter2 + " out of " + inLineSolutions + " inline-feasible solutions out of "+ numTests + " tests.");
			//System.out.println("Kruskal succeeded " + kruskalCounter1 + " times, Basic prim " + primCounter1 + " times, Modified Prim " + primCounter2 + " times and AllStart Modified Prim " + primCounter3 + " times.");
			
		}
	}
	
	public static void maxTemplate() {
		int kruskalTemplateSum = 0;
		long kruskalTimeSum = 0l;
		int primTemplateSum = 0;
		long primTimeSum = 0l;
		int cycleTemplateSum = 0;
		long cycleTimeSum = 0l;
		for (int numPatterns = 25; numPatterns <= 250; numPatterns += 25) {
			System.out.println("Sets of " + numPatterns + " patterns:");
			int numTests = 10;
			//int numPatterns = 250;
			MersenneTwisterFast random = new MersenneTwisterFast(56724969353l*(long)Math.floor(numPatterns / 25));
			//MersenneTwisterFast startRandomizer = new MersenneTwisterFast(5752846548926l);
			//CycleHeuristic heuristic;
			KruskalHeuristic kruskal;
			PrimHeuristic prim;
			CycleHeuristic cycle;
			IntegerUtils integers = new IntegerUtils(1000);
			kruskalTemplateSum = 0;
			kruskalTimeSum = 0l;
			primTemplateSum = 0;
			primTimeSum = 0l;
			cycleTemplateSum = 0;
			cycleTimeSum = 0l;
			long startTime;
			long endTime;
			int badSetCounter = 0;
			for (int i = 0; i < numTests; i++) {
				// Create Set
				System.out.println("Set #" + i);
				CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers);
				boolean valid = false;
				while (!valid) {
					valid = true;
					try {
						patterns = CircularPatternSet.randomTightPatternSet(numPatterns, 30, 1, 1000, 3, random, integers);
					} catch (IllegalArgumentException e) {
						badSetCounter++;
						if (1.0 * badSetCounter / 100 == Math.floor(1.0 * badSetCounter / 100))
							System.out.println(badSetCounter);
						valid = false;
					}
				}
				System.out.println(patterns);
				
				/*
				// Kruskal
				System.gc();
				startTime = System.nanoTime();
				kruskal = new KruskalHeuristic(patterns);
				kruskal.feasibilityType = KruskalHeuristic.inlineFeasible;
				kruskal.runHeuristic();
				int[][] kruskalSolution = kruskal.outputSolutionIndices();
				int kruskalSolutionSize = 0;
				for (int[] template : kruskalSolution)
					if (template.length > kruskalSolutionSize)
						kruskalSolutionSize = template.length;
				kruskal = null;
				System.gc();
				endTime = System.nanoTime();
				kruskalTemplateSum += kruskalSolutionSize;
				long kruskalTime = endTime - startTime;
				kruskalTimeSum += kruskalTime;
				System.out.println("Kruskal (Inline Feasible) fitted " + kruskalSolutionSize + " patterns in " + kruskalTime + " nanoseconds.");
				*/
				// Prim
				System.gc();
				startTime = System.nanoTime();
				prim = new PrimHeuristic(patterns);
				prim.feasibilityType = PrimHeuristic.contiguousOrdering;
				int primSolution = prim.runModifiedAllStart();
				prim = null;
				System.gc();
				endTime = System.nanoTime();
				primTemplateSum += primSolution;
				long primTime = endTime - startTime;
				primTimeSum += primTime;
				System.out.println("Modified Prim (ZeroFeasibility) (All Start) fitted " + primSolution + " patterns in " + primTime + " nanoseconds.");
				
				/*
				// Cycle Heuristic - fifoShortestPath
				System.gc();
				startTime = System.nanoTime();
				cycle = new CycleHeuristic(patterns);
				cycle.allPositions = false;
				cycle.sortedAddition = false;
				//cycle.allPositionOrder = CycleHeuristic.alternate;
				cycle.type = CycleHeuristic.fifoShortestPath;
				int cycleSolution = cycle.runIterativeHeuristicLargestStart().length;
				cycle = null;
				System.gc();
				endTime = System.nanoTime();
				cycleTemplateSum += cycleSolution;
				long cycleTime = endTime - startTime;
				cycleTimeSum += cycleTime;
				System.out.println("fifoShortestPath (Largest Start) fitted " + cycleSolution + " patterns in " + cycleTime + " nanoseconds.");
				
				// Cycle Heuristic - fifoShortestPath
				System.gc();
				startTime = System.nanoTime();
				cycle = new CycleHeuristic(patterns);
				cycle.allPositions = false;
				cycle.sortedAddition = true;
				//cycle.allPositionOrder = CycleHeuristic.alternate;
				cycle.type = CycleHeuristic.fifoShortestPath;
				int cycleSolution = cycle.runIterativeHeuristicSmallestStart().length;
				cycle = null;
				System.gc();
				endTime = System.nanoTime();
				primTemplateSum += cycleSolution;
				long cycleTime = endTime - startTime;
				primTimeSum += cycleTime;
				System.out.println("fifoShortestPath (Sorted Addition) (Prim Start+) fitted " + cycleSolution + " patterns in " + cycleTime + " nanoseconds.");
				*/
				
				System.out.println("=========================================================================================================");
				
			}
			//System.out.println("Kruskal fit an average of " + (kruskalTemplateSum / numTests) + " in an average of " + (kruskalTimeSum / (1000000000l * numTests)) + " seconds.");
			//System.out.println("Cycle fit an average of " + (cycleTemplateSum / numTests) + " in an average of " + (cycleTimeSum / (1000000000l * numTests)) + " seconds.");
			System.out.println("Prim fit an average of " + (primTemplateSum / numTests) + " in an average of " + (primTimeSum / (1000000000l * numTests)) + " seconds.");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
	}
		
	public static void minTemplates() {
		// Min Templates Test - Tight
		System.out.println("Solution Key: <NumPatterns:Set#:Heuristic:NumOfTemplates:RelativeDifferenceInLowerBound:Templates{0,0,0}:Time(nanosec)>");
		
		int gurobiTemplateSum = 0;
		long gurobiTimeSum = 0l;
		double gurobiLowerBoundDifferenceSum = 0d;
		int kruskalTemplateSum = 0;
		long kruskalTimeSum = 0l;
		double kruskalLowerBoundDifferenceSum = 0d;
		
		double iterativePrimLowerBoundDifferenceSum = 0d;
		for (int numPatterns = 75; numPatterns <= 250; numPatterns += 25) {
			System.out.println("Sets of " + numPatterns + " patterns:");
			int numTests = 10;
			//int numPatterns = 250;
			MersenneTwisterFast random = new MersenneTwisterFast(56724969353l*(long)Math.floor(numPatterns / 25));
			//MersenneTwisterFast startRandomizer = new MersenneTwisterFast(5752846548926l);
			//CycleHeuristic heuristic;
			KruskalHeuristic kruskal;
			//PrimHeuristic prim;
			//BinPackingHeuristic binPacker;
			GurobiMinTemplatesSolver gurobi;
			IntegerUtils integers = new IntegerUtils(1000);
			gurobiTemplateSum = 0;
			gurobiTimeSum = 0l;
			gurobiLowerBoundDifferenceSum = 0d;
			kruskalTemplateSum = 0;
			kruskalTimeSum = 0l;
			kruskalLowerBoundDifferenceSum = 0d;
			long startTime;
			long endTime;
			int badSetCounter = 0;
			for (int i = 0; i < numTests; i++) {
				// Create Set
//				System.out.println("Set #" + i);
				
				
				CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers);
				boolean valid = false;
				while (!valid) {
					valid = true;
					try {
						patterns = CircularPatternSet.randomTightPatternSet(numPatterns, 30, 1, 1000, 3, random, integers);
					} catch (IllegalArgumentException e) {
						badSetCounter++;
						if (1.0 * badSetCounter / 100 == Math.floor(1.0 * badSetCounter / 100))
							System.out.println(badSetCounter);
						valid = false;
					}
				}
				int lowerBound = patterns.korstBinLowerBound();
				
//				System.out.println(patterns);
//				System.out.println("Lower Bound: " + lowerBound);
				
				
				
				// Kruskal
				System.gc();
				startTime = System.nanoTime();
				kruskal = new KruskalHeuristic(patterns);
				kruskal.feasibilityType = KruskalHeuristic.contiguousOrdering;
				kruskal.runHeuristic();
				int[][] kruskalSolution = kruskal.outputSolutionIndices();
				kruskal = null;
				System.gc();
				endTime = System.nanoTime();
				kruskalTemplateSum += kruskalSolution.length;
				long kruskalTime = endTime - startTime;
				kruskalTimeSum += kruskalTime;
				double kruskalLowerBoundDifference = (0d + kruskalSolution.length - lowerBound) / lowerBound;
				kruskalLowerBoundDifferenceSum += kruskalLowerBoundDifference;
				System.out.print("<" + numPatterns + ":" + i + ":Kruskal:" + kruskalSolution.length + ":" + kruskalLowerBoundDifference + ":{" + kruskalSolution[0].length);
				for (int j = 1; j < kruskalSolution.length; j++)
					System.out.print("," + kruskalSolution[j].length);
				System.out.println("}:" + kruskalTime + ">");
//				System.out.println("Kruskal (Contiguous Ordering) fitted " + numPatterns + " on " + kruskalSolutionlength + " templates in " + kruskalTime + " nanoseconds.");
				
//				
//				kruskal = new KruskalHeuristic(patterns);
//				kruskal.feasibilityType = KruskalHeuristic.contiguousOrdering;
//				kruskal.runHeuristic();
//				int kruskalResult = kruskal.outputSolutionIndices().length;
//				
				// Gurobi
				System.gc();
				startTime = System.nanoTime();
				gurobi = new GurobiMinTemplatesSolver();
				gurobi.setPatterns(patterns, kruskalSolution.length + (int)Math.floor(numPatterns / 25));
				gurobi.solve();
				int[][] gurobiSolution = gurobi.outputSolution();
				gurobi.dispose();
				gurobi = null;
				System.gc();
				endTime = System.nanoTime();
				if (gurobiSolution == null)
					gurobiSolution = new int[0][0];
				gurobiTemplateSum += gurobiSolution.length;
				long gurobiTime = endTime - startTime;
				gurobiTimeSum += gurobiTime;
				double gurobiLowerBoundDifference = (0d + gurobiSolution.length - lowerBound) / lowerBound;
				gurobiLowerBoundDifferenceSum += gurobiLowerBoundDifference;
				System.out.print("<" + numPatterns + ":" + i + ":Gurobi (60min Limit):" + gurobiSolution.length + ":" + gurobiLowerBoundDifference +  ":{" + gurobiSolution[0].length);
				for (int j = 1; j < gurobiSolution.length; j++)
					System.out.print("," + gurobiSolution[j].length);
				System.out.println("}:" + gurobiTime + ">");
//					System.out.println("Bin Packer (Empty Space) (Level 0) fitted " + numPatterns + " on " + esSolution.length + " templates in " + esTime + " nanoseconds.");				
				
			
				
				System.out.println("=========================================================================================================");
				
			}
			System.out.println("Gurobi (60min Limit) fit " + numPatterns + " patterns on an average of " + (1d * gurobiTemplateSum / numTests) + " templates in an average of " + (gurobiTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (gurobiLowerBoundDifferenceSum * 100 / numTests) + "%.");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
	}
	
	public static void minTemplatesPolar() {
		// Min Templates Test - Polar
		System.out.println("Solution Key: <NumPatterns:Set#:Heuristic:NumOfTemplates:Templates{0,0,0}:Time(nanosec)>");
		int fifoTemplateSum = 0;
		long fifoTimeSum = 0l;
		int floydTemplateSum = 0;
		long floydTimeSum = 0l;
		int gurobiTemplateSum = 0;
		long gurobiTimeSum = 0l;
		for (int numPatterns = 25; numPatterns <= 250; numPatterns += 25) {
			System.out.println("Sets of " + numPatterns + " patterns:");
			int numTests = 10;
			//int numPatterns = 250;
			MersenneTwisterFast random = new MersenneTwisterFast(5780593202071l*(long)Math.floor(numPatterns / 25));
			//MersenneTwisterFast startRandomizer = new MersenneTwisterFast(5752846548926l);
			//CycleHeuristic heuristic;
			BinPackingHeuristic binPacker;
			GurobiMinTemplatesSolver solver;
			fifoTemplateSum = 0;
			fifoTimeSum = 0l;
			floydTemplateSum = 0;
			floydTimeSum = 0l;
			gurobiTemplateSum = 0;
			gurobiTimeSum = 0l;
			long startTime;
			long endTime;
			for (int i = 0; i < numTests; i++) {
				// Create Set
	//			System.out.println("Set #" + i);
				
				
	//			System.out.println(patterns);
				
				PolarPatternSet patterns = PolarPatternSet.randomPatternSet(numPatterns, 50, 200, 3, 80, 5, 10, random);
				
				System.out.println("Lower Bound: " + SetPartition.indexPartition(patterns).length);
				
				// Bin Packing - fifoShortestPath
				System.gc();
				startTime = System.nanoTime();
				binPacker = new BinPackingHeuristic(patterns);
				binPacker.binType = BinPackingHeuristic.fifoShortestPath;
				binPacker.setComplexity(0);
				//binPacker.setVerbosity(true);
				binPacker.runFirstFitHeuristic();
				int[][] fifoSolution = binPacker.outputSolutionIndices();
				if (!binPacker.checkSolution())
					throw new RuntimeException("Fifo Screwed Up!");
				//System.out.println(binPacker.printSolution());
				binPacker = null;
				System.gc();
				endTime = System.nanoTime();
				fifoTemplateSum += fifoSolution.length;
				long fifoTime = endTime - startTime;
				fifoTimeSum += fifoTime;
				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (Fifo) (Level 0):" + fifoSolution.length + ":{" + fifoSolution[0].length);
				for (int j = 1; j < fifoSolution.length; j++)
					System.out.print("," + fifoSolution[j].length);
				System.out.println("}:" + fifoTime + ">");
	//			System.out.println("Bin Packer (fifo) (Level 0) fitted " + numPatterns + " on " + fifoSolution.length + " templates in " + fifoTime + " nanoseconds.");				
				
				// Bin Packing - Floyd
				System.gc();
				startTime = System.nanoTime();
				binPacker = new BinPackingHeuristic(patterns);
				binPacker.binType = BinPackingHeuristic.floydWarshall;
				binPacker.setComplexity(0);
				binPacker.runFirstFitHeuristic();
				int[][] floydSolution = binPacker.outputSolutionIndices();
				if (!binPacker.checkSolution())
					throw new RuntimeException("Floyd Screwed Up!");
				//System.out.println(binPacker.printSolution());
				binPacker = null;
				System.gc();
				endTime = System.nanoTime();
				floydTemplateSum += floydSolution.length;
				long floydTime = endTime - startTime;
				floydTimeSum += floydTime;
				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (Floyd) (Level 0):" + floydSolution.length + ":{" + floydSolution[0].length);
				for (int j = 1; j < floydSolution.length; j++)
					System.out.print("," + floydSolution[j].length);
				System.out.println("}:" + floydTime + ">");
	//			System.out.println("Bin Packer (Floyd) (Level 0) fitted " + numPatterns + " on " + floydSolution.length + " templates in " + floydTime + " nanoseconds.");				
				
				// Gurobi
				
				System.gc();
				startTime = System.nanoTime();
				solver = new GurobiMinTemplatesSolver();
				solver.setPatterns(patterns, floydSolution.length);
				solver.solve();
				int[][] gurobiSolution = solver.outputSolution();
				if ((gurobiSolution != null) && (!solver.checkSolution())) {
					System.out.println("Gurobi Screwed Up!");
					gurobiSolution = null;
				}
				//System.out.println(binPacker.printSolution());
				solver = null;
				System.gc();
				endTime = System.nanoTime();
				gurobiTemplateSum += (gurobiSolution != null ? gurobiSolution.length : numPatterns);
				long gurobiTime = endTime - startTime;
				gurobiTimeSum += gurobiTime;
				System.out.print("<" + numPatterns + ":" + i + ":Gurobi (Level 0):");
				if (gurobiSolution != null) {
					System.out.print(gurobiSolution.length + ":{" + gurobiSolution[0].length);
				for (int j = 1; j < gurobiSolution.length; j++)
					System.out.print("," + gurobiSolution[j].length);
				} else
					System.out.print("-:{-");
				System.out.println("}:" + gurobiTime + ">");
				
				
				
				System.out.println("=========================================================================================================");
				
			}
			System.out.println("Bin Packer (fifo) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * fifoTemplateSum / numTests) + " templates in an average of " + (fifoTimeSum / (1000000000l * numTests)) + " seconds.");
			System.out.println("Bin Packer (Floyd) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * floydTemplateSum / numTests) + " templates in an average of " + (floydTimeSum / (1000000000l * numTests)) + " seconds.");
			System.out.println("Gurobi fit " + numPatterns + " patterns on an average of " + (1d * gurobiTemplateSum / numTests) + " templates in an average of " + (gurobiTimeSum / (1000000000l * numTests)) + " seconds.");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
	}
	
	public static void korst() {
		//System.out.println("Solution Key: <NumPatterns:Set#:Heuristic:NumOfTemplates:RelativeDifferenceInLowerBound:Templates{0,0,0}:Time(nanosec)>");
		for (int pMax = 10; pMax <= 100; pMax += 10) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("pMax = " + pMax);
			int kruskalTemplateSum = 0;
			long kruskalTimeSum = 0l;
			double kruskalLowerBoundDifferenceSum = 0d;
			int primTemplateSum = 0;
			long primTimeSum = 0l;
			double primLowerBoundDifferenceSum = 0d;
			int fifoTemplateSum = 0;
			long fifoTimeSum = 0l;
			double fifoLowerBoundDifferenceSum = 0d;
			int floydTemplateSum = 0;
			long floydTimeSum = 0l;
			double floydLowerBoundDifferenceSum = 0d;
			int esTemplateSum = 0;
			long esTimeSum = 0l;
			double esLowerBoundDifferenceSum = 0d;
			int iterativePrimTemplateSum = 0;
			long iterativePrimTimeSum = 0l;
			double iterativePrimLowerBoundDifferenceSum = 0d;
			for (int numPatterns = 10; numPatterns <= 200; numPatterns += 10) {
				System.out.println("Sets of " + numPatterns + " patterns:");
				int numTests = 100;
				//int numPatterns = 250;
				MersenneTwisterFast random = new MersenneTwisterFast(348753881l*(long)Math.floor(numPatterns / 10) + pMax);
				//MersenneTwisterFast startRandomizer = new MersenneTwisterFast(5752846548926l);
				//CycleHeuristic heuristic;
				KruskalHeuristic kruskal;
				PrimHeuristic prim;
				BinPackingHeuristic binPacker;
				IntegerUtils integers = new IntegerUtils(1000);
				kruskalTemplateSum = 0;
				kruskalTimeSum = 0l;
				kruskalLowerBoundDifferenceSum = 0d;
				primTemplateSum = 0;
				primTimeSum = 0l;
				primLowerBoundDifferenceSum = 0d;
				fifoTemplateSum = 0;
				fifoTimeSum = 0l;
				fifoLowerBoundDifferenceSum = 0d;
				floydTemplateSum = 0;
				floydTimeSum = 0l;
				floydLowerBoundDifferenceSum = 0d;
				esTemplateSum = 0;
				esTimeSum = 0l;
				esLowerBoundDifferenceSum = 0d;
				iterativePrimTemplateSum = 0;
				iterativePrimTimeSum = 0l;
				iterativePrimLowerBoundDifferenceSum = 0d;
				long startTime;
				long endTime;
				int badSetCounter = 0;
				for (int i = 0; i < numTests; i++) {
					// Create Set
	//				System.out.println("Set #" + i);
					LinearPatternSet patterns = LinearPatternSet.randomKorstPatternSet(numPatterns, pMax, random);
					int lowerBound = patterns.korstBinLowerBound();
					
	//				System.out.println(patterns);
	//				System.out.println("Lower Bound: " + lowerBound);
					
					int[][] partition = SetPartition.indexPartition(patterns);
					PatternSet[] partitionedSets = new PatternSet[partition.length];
					for (int j = 0; j < partition.length; j++)
						partitionedSets[j] = patterns.subset(partition[j]);
					
					// Kruskal
					System.gc();
					startTime = System.nanoTime();
					ArrayList<int[]> kruskalSolution = new ArrayList<int[]>();
					int kruskalSolutionSize = 0;
					for (PatternSet p : partitionedSets) {
						kruskal = new KruskalHeuristic(p);
						kruskal.feasibilityType = KruskalHeuristic.contiguousOrdering;
						kruskal.runHeuristic();
						int[][] currentSolution = kruskal.outputSolutionIndices();
						kruskalSolution.addAll(Arrays.asList(currentSolution));
						kruskalSolutionSize += currentSolution.length;
						kruskal = null;
					}
					System.gc();
					endTime = System.nanoTime();
					kruskalTemplateSum += kruskalSolutionSize;
					long kruskalTime = endTime - startTime;
					kruskalTimeSum += kruskalTime;
					double kruskalLowerBoundDifference = (0d + kruskalSolutionSize - lowerBound) / lowerBound;
					kruskalLowerBoundDifferenceSum += kruskalLowerBoundDifference;
	//				System.out.print("<" + numPatterns + ":" + i + ":Kruskal:" + kruskalSolutionSize + ":" + kruskalLowerBoundDifference + ":{" + kruskalSolution.get(0).length);
	//				for (int j = 1; j < kruskalSolution.size(); j++)
	//					System.out.print("," + kruskalSolution.get(j).length);
	//				System.out.println("}:" + kruskalTime + ">");
	//				System.out.println("Kruskal (Contiguous Ordering) fitted " + numPatterns + " on " + kruskalSolutionSize + " templates in " + kruskalTime + " nanoseconds.");
					
					// Iterative Prim
					System.gc();
					startTime = System.nanoTime();
					ArrayList<int[]> iterativePrimSolution = new ArrayList<int[]>();
					int iterativePrimSolutionSize = 0;
					for (PatternSet p : partitionedSets) {
						prim = new PrimHeuristic(p);
						prim.feasibilityType = PrimHeuristic.contiguousOrdering;
						int[][] currentSolution = prim.runIterativeModifiedHeuristic();
						iterativePrimSolutionSize += currentSolution.length;
						iterativePrimSolution.addAll(Arrays.asList(currentSolution));
						prim = null;
					}
					System.gc();
					endTime = System.nanoTime();
					iterativePrimTemplateSum += iterativePrimSolutionSize;
					long iterativePrimTime = endTime - startTime;
					iterativePrimTimeSum += iterativePrimTime;
					double iterativePrimLowerBoundDifference = (0d + iterativePrimSolutionSize - lowerBound) / lowerBound;
					iterativePrimLowerBoundDifferenceSum += iterativePrimLowerBoundDifference;
	//				System.out.print("<" + numPatterns + ":" + i + ":Iterative Prim:" + iterativePrimSolutionSize + ":" + iterativePrimLowerBoundDifference +  ":{" + iterativePrimSolution.get(0).length);
	//				for (int j = 1; j < iterativePrimSolution.size(); j++)
	//					System.out.print("," + iterativePrimSolution.get(j).length);
	//				System.out.println("}:" + iterativePrimTime + ">");
	//				System.out.println("Iterative Prim (Contiguous Ordering) fitted " + numPatterns + " on " + iterativePrimSolutionSize + " templates in " + iterativePrimTime + " nanoseconds.");				
					
					// Bin Packing - Modified Prim
					System.gc();
					startTime = System.nanoTime();
					ArrayList<int[]> primSolution = new ArrayList<int[]>();
					int primSolutionSize = 0;
					for (PatternSet p : partitionedSets) {
						binPacker = new BinPackingHeuristic(p);
						binPacker.binType = BinPackingHeuristic.prim;
						binPacker.setComplexity(3);
						binPacker.runFirstFitHeuristic();
						int[][] currentSolution = binPacker.outputSolutionIndices();
						primSolutionSize += currentSolution.length;
						primSolution.addAll(Arrays.asList(currentSolution));
						binPacker = null;
					}
					System.gc();
					endTime = System.nanoTime();
					primTemplateSum += primSolutionSize;
					long primTime = endTime - startTime;
					primTimeSum += primTime;
					double primLowerBoundDifference = (0d + primSolutionSize - lowerBound) / lowerBound;
					primLowerBoundDifferenceSum += primLowerBoundDifference;
	//				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (Prim) (Level 3):" + primSolutionSize + ":" + primLowerBoundDifference +  ":{" + primSolution.get(0).length);
	//				for (int j = 1; j < primSolution.size(); j++)
	//					System.out.print("," + primSolution.get(j).length);
	//				System.out.println("}:" + primTime + ">");
	//				System.out.println("Bin Packer (Prim) (Level 3) fitted " + numPatterns + " on " + primSolutionSize + " templates in " + primTime + " nanoseconds.");				
					
					// Bin Packing - fifoShortestPath
					System.gc();
					startTime = System.nanoTime();
					ArrayList<int[]> fifoSolution = new ArrayList<int[]>();
					int fifoSolutionSize = 0;
					for (PatternSet p : partitionedSets) {
						binPacker = new BinPackingHeuristic(p);
						binPacker.binType = BinPackingHeuristic.fifoShortestPath;
						binPacker.setComplexity(0);
						binPacker.runFirstFitHeuristic();
						int[][] currentSolution = binPacker.outputSolutionIndices();
						fifoSolutionSize += currentSolution.length;
						fifoSolution.addAll(Arrays.asList(currentSolution));
						binPacker = null;
					}
					System.gc();
					endTime = System.nanoTime();
					fifoTemplateSum += fifoSolutionSize;
					long fifoTime = endTime - startTime;
					fifoTimeSum += fifoTime;
					double fifoLowerBoundDifference = (0d + fifoSolutionSize - lowerBound) / lowerBound;
					fifoLowerBoundDifferenceSum += fifoLowerBoundDifference;
	//				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (Fifo) (Level 0):" + fifoSolutionSize + ":" + fifoLowerBoundDifference +  ":{" + fifoSolution.get(0).length);
	//				for (int j = 1; j < fifoSolution.size(); j++)
	//					System.out.print("," + fifoSolution.get(j).length);
	//				System.out.println("}:" + fifoTime + ">");
	//				System.out.println("Bin Packer (fifo) (Level 0) fitted " + numPatterns + " on " + fifoSolutionSize + " templates in " + fifoTime + " nanoseconds.");				
					
				
					// Bin Packing - EmptySpace
					System.gc();
					startTime = System.nanoTime();
					ArrayList<int[]> esSolution = new ArrayList<int[]>();
					int esSolutionSize = 0;
					for (PatternSet p : partitionedSets) {
						binPacker = new BinPackingHeuristic(p);
						binPacker.binType = BinPackingHeuristic.emptySpace;
						binPacker.setComplexity(0);
						binPacker.runFirstFitHeuristic();
						int[][] currentSolution = binPacker.outputSolutionIndices();
						esSolutionSize += currentSolution.length;
						esSolution.addAll(Arrays.asList(currentSolution));
						binPacker = null;
					}
					System.gc();
					endTime = System.nanoTime();
					esTemplateSum += esSolutionSize;
					long esTime = endTime - startTime;
					esTimeSum += esTime;
					double esLowerBoundDifference = (0d + esSolutionSize - lowerBound) / lowerBound;
					esLowerBoundDifferenceSum += esLowerBoundDifference;
	//				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (Empty Space) (Level 0):" + esSolutionSize + ":" + esLowerBoundDifference +  ":{" + esSolution.get(0).length);
	//				for (int j = 1; j < esSolution.size(); j++)
	//					System.out.print("," + esSolution.get(j).length);
	//				System.out.println("}:" + esTime + ">");
	//				System.out.println("Bin Packer (Empty Space) (Level 0) fitted " + numPatterns + " on " + esSolutionSize + " templates in " + esTime + " nanoseconds.");				
					
					
	//				System.out.println("=========================================================================================================");
					
				}
				System.out.println("Kruskal fit " + numPatterns + " patterns on an average of " + (1d * kruskalTemplateSum / numTests) + " templates in an average of " + (kruskalTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (kruskalLowerBoundDifferenceSum * 100 / numTests) + "%.");
				System.out.println("Iterative Prim fit " + numPatterns + " patterns on an average of " + (1d * iterativePrimTemplateSum / numTests) + " templates in an average of " + (iterativePrimTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (iterativePrimLowerBoundDifferenceSum * 100 / numTests) + "%.");
				System.out.println("Bin Packer (Prim) (Level 3) fit " + numPatterns + " patterns on an average of " + (1d * primTemplateSum / numTests) + " templates in an average of " + (primTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (primLowerBoundDifferenceSum * 100 / numTests) + "%.");
				System.out.println("Bin Packer (fifo) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * fifoTemplateSum / numTests) + " templates in an average of " + (fifoTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (fifoLowerBoundDifferenceSum * 100 / numTests) + "%.");
				//System.out.println("Bin Packer (Floyd) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * floydTemplateSum / numTests) + " templates in an average of " + (floydTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (floydLowerBoundDifferenceSum * 100 / numTests) + "%.");
				System.out.println("Bin Packer (Empty Space) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * esTemplateSum / numTests) + " templates in an average of " + (esTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (esLowerBoundDifferenceSum * 100 / numTests) + "%.");
				System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			}
		}
	}
	
	
	public static void main(String[] args) {
		//kruskalPrim();
		//prim();
		//cycleFinders();
		//insertionOrders();
		//fit();
		//maxTemplate();
		//minTemplates();
		minTemplatesPolar();
		//korst();
	}
}
