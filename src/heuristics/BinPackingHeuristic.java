/**
 * BinPackingHeuristic.java
 * Jul 29, 2011
 */
package heuristics;

import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Iterator;


import problem.LinearPatternSet;
import problem.PSPatternSet;
import problem.PatternSet;
import util.IntegerUtils;
import util.MersenneTwisterFast;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Jul 29, 2011
 *
 */
public class BinPackingHeuristic implements Iterable<Bin> {
	private TreeSet<Bin> bins;
	private PatternSet patterns;
	
	private int complexityLevel;
	public boolean verbose;
	
	public int binType;
	
	public static final int prim = 0;
	public static final int floydWarshall = 1;
	public static final int minMeanCycle = 2;
	public static final int fifoShortestPath = 3;
	public static final int emptySpace = 4;
	public static final int twoVIP = 5;
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 */
	public BinPackingHeuristic(PatternSet patterns) {
		this.patterns = patterns;
		this.bins = new TreeSet<Bin>(new BinComparator());
		this.binType = prim;
		this.complexityLevel = 3;
		verbose = false;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param initialPattern
	 */
	public void initialize(int initialPattern) {
		int[] pattern = {initialPattern};
		initialize(pattern);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param initialPatterns
	 */
	public void initialize(int[] initialPatterns) {
		bins.clear();
		for (int i : initialPatterns) {
			if (binType == prim) {
				PrimHeuristic newBin = new PrimHeuristic(patterns, i);
				newBin.setLevel(complexityLevel);
				newBin.setVerbosity(verbose);
				bins.add(newBin);
			} else if (binType == floydWarshall) {
				CycleHeuristic newBin = new CycleHeuristic(patterns, i);
				newBin.type = CycleHeuristic.floydWarshall;
				newBin.setLevel(complexityLevel);
				newBin.setVerbosity(verbose);
				bins.add(newBin);
			} else if (binType == fifoShortestPath) {
				CycleHeuristic newBin = new CycleHeuristic(patterns, i);
				newBin.type = CycleHeuristic.fifoShortestPath;
				newBin.setLevel(complexityLevel);
				newBin.setVerbosity(verbose);
				bins.add(newBin);
			} else if (binType == minMeanCycle) {
				CycleHeuristic newBin = new CycleHeuristic(patterns, i);
				newBin.type = CycleHeuristic.minMeanCycle;
				newBin.setLevel(complexityLevel);
				newBin.setVerbosity(verbose);
				bins.add(newBin);
			} else if (binType == emptySpace) {
				EmptySpaceHeuristic newBin = new EmptySpaceHeuristic(patterns, i);
				newBin.setLevel(complexityLevel);
				newBin.setVerbosity(verbose);
				bins.add(newBin);
			} else if (binType == twoVIP) {
				PSPatternSet newPatterns;
				if (patterns instanceof PSPatternSet)
					newPatterns = (PSPatternSet)patterns;
				else
					throw new IllegalArgumentException("TwoVIP can only handle PSPatterns.");
				TwoVIPHeuristic newBin = new TwoVIPHeuristic(newPatterns, i);
				newBin.setLevel(complexityLevel);
				newBin.setVerbosity(verbose);
				bins.add(newBin);
			} else
				throw new RuntimeException("This type of bin is not yet implemented.");
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param level
	 */
	public void setComplexity(int level) {
		this.complexityLevel = level;
		for (Bin b : bins) 
			b.setLevel(level);
	}
	
	/**
	 * 
	 * TODO
	 *
	 */
	public void runFirstFitHeuristic() {
		PriorityQueue<Integer> patternsLeft = new PriorityQueue<Integer>(patterns.numPatterns(), new PatternComparator());
		if (bins.isEmpty()) {
			for (int i = 0; i < patterns.numPatterns(); i++)
				patternsLeft.add(i);
			initialize(patternsLeft.poll());
		} else {
			int[] remainingPatterns = new int[patterns.numPatterns()];
			int numberOfremainingPatterns = patterns.numPatterns();
			for (int i = 0; i < patterns.numPatterns(); i++)
				remainingPatterns[i] = i;
			for (Bin b : bins)
				for (Integer i : b)
					remainingPatterns[i] = remainingPatterns[--numberOfremainingPatterns];
			for (int i = 0; i < numberOfremainingPatterns; i++)
				patternsLeft.add(remainingPatterns[i]);
		}
		while (!patternsLeft.isEmpty()) {
			int currentPattern = patternsLeft.poll();
			if (verbose) {
				System.out.println("Total patterns left: " + (patternsLeft.size()+1));
				System.out.println("Current bins: ");
				System.out.println(printSolution());
				System.out.println("Finding a bin for pattern: " + patterns.getPattern(currentPattern));
			}
			Iterator<Bin> binIterator = bins.iterator();
			boolean done = false;
			while (!done && binIterator.hasNext()) {
				Bin currentBin = binIterator.next();
				if (currentBin.insert(currentPattern)) {
					if (verbose) {
						System.out.println("Added pattern to bin:");
						System.out.println(currentBin.printSolution());
					}
					binIterator.remove();
					binIterator = null;
					bins.add(currentBin);
					done = true;
				}
			}
			if (!done) {
				if (verbose) {
					System.out.println("Creating new bin");
				}
				Bin newBin = bins.first().newBin(currentPattern);
				newBin.setVerbosity(verbose);
				bins.add(newBin);
			}
		}
	}
	
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[][] outputSolutionIndices() {
		int[][] out = new int[bins.size()][];
		Iterator<Bin> binIterator = bins.iterator();
		for (int i = 0; i < bins.size(); i++)
			out[i] = binIterator.next().outputIndices();
		return out;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int numberOfBins() {
		return bins.size();
	}

	/**
	 * Checks the solution returned by the heuristic to make sure it is
	 * valid. If this method returns false it indicates a bug in this class,
	 * thus it should never happen
	 * 
	 * @return <code>true</code> if the current solution is valid, 
	 * 			<code>false</code> else.
	 */
	public boolean checkSolution() {
		for (Bin b : bins)
			if (!b.checkSolution())
				return false;
		return true;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param v
	 */
	public void setVerbosity(boolean v) {
		verbose = v;
		for (Bin b : bins)
			b.setVerbosity(v);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public Iterator<Bin> iterator() {
		return bins.iterator();
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public String printSolution() {
		String endln = System.getProperty("line.separator");
		String s = "";
		Iterator<Bin> binIterator = bins.iterator();
		while (binIterator.hasNext()) {
			s += "++++++++++++++++++++++++++++++++++++++++" + endln;
			Bin currentBin = binIterator.next();
			s += currentBin.printSolution();
			s += "Bin Size: " + currentBin.binSize();
			s += endln;
		}
		s += "++++++++++++++++++++++++++++++++++++++++" + endln;
		s += "Total number of Bins: " + bins.size();
		return s;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int numTests = 100;
		int numPatterns = 10;
		MersenneTwisterFast random = new MersenneTwisterFast(548527385704250l);
		int[] primCounter = {0,0,0,0};
		long[] primTimeCounter = {0,0,0,0};
		int[] esCounter = {0,0,0,0};
		long[] esTimeCounter = {0,0,0,0};
		int[] fifoCounter = {0,0,0,0};
		long[] fifoTimeCounter = {0,0,0,0};
		int[] floydCounter = {0,0,0,0};
		long[] floydTimeCounter = {0,0,0,0};
		int kruskalBinCounter = 0;
		long kruskalTimeCounter = 0;
		long startTime = 0;
		long endTime = 0;
		BinPackingHeuristic heuristic;
		KruskalHeuristic kruskal;
		PrimHeuristic prim;
		IntegerUtils integers = new IntegerUtils(1000);
		for (int i = 0; i < numTests; i++) {
			/*
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
			*/
			LinearPatternSet patterns = LinearPatternSet.randomKorstPatternSet(numPatterns, 10, random);
			for (int level = 0; level < 4; level ++) {
				/*
				System.gc();
				startTime = System.nanoTime();
				heuristic = new BinPackingHeuristic(patterns);
				heuristic.binType = fifoShortestPath;
				heuristic.setComplexity(level);
				heuristic.initialize(0);
				heuristic.runFirstFitHeuristic();
				endTime = System.nanoTime();
				if (!heuristic.checkSolution())
					System.out.println("Bin Packer screwed up!!!!!!!!!!");
				fifoCounter[level] += heuristic.numberOfBins();
				fifoTimeCounter[level] += (endTime - startTime);
				heuristic = null;
				System.gc();
				*/
				/*
				System.gc();
				startTime = System.nanoTime();
				heuristic = new BinPackingHeuristic(patterns);
				heuristic.binType = emptySpace;
				heuristic.setComplexity(level);
				heuristic.initialize(0);
				heuristic.runFirstFitHeuristic();
				endTime = System.nanoTime();
				if (!heuristic.checkSolution())
					System.out.println("Bin Packer screwed up!!!!!!!!!!");
				esCounter[level] += heuristic.numberOfBins();
				esTimeCounter[level] += (endTime - startTime);
				heuristic = null;
				System.gc();
				*/
				/*
				startTime = System.nanoTime();
				heuristic = new BinPackingHeuristic(patterns);
				heuristic.binType = prim;
				//heuristic.verbose = true;
				heuristic.setComplexity(level);
				heuristic.initialize(0);
				heuristic.runFirstFitHeuristic();
				endTime = System.nanoTime();
				//System.out.println(heuristic.printSolution());
				if (!heuristic.checkSolution())
					System.out.println("Bin Packer screwed up!!!!!!!!!!");
				primCounter[level] += heuristic.numberOfBins();
				primTimeCounter[level] += (endTime - startTime);
				heuristic = null;
				System.gc();
				
				startTime = System.nanoTime();
				heuristic = new BinPackingHeuristic(patterns);
				heuristic.binType = floydWarshall;
				heuristic.setComplexity(level);
				heuristic.initialize(0);
				heuristic.runFirstFitHeuristic();
				endTime = System.nanoTime();
				if (!heuristic.checkSolution())
					System.out.println("Bin Packer screwed up!!!!!!!!!!");
				floydCounter[level] += heuristic.numberOfBins();
				floydTimeCounter[level] += (endTime - startTime);
				heuristic = null;
				System.gc();
				*/
			}
			System.out.println("======================================");
			System.out.println("Set #:" + i);
			System.out.println(patterns);
			System.out.println("Lower Bound: " + patterns.korstBinLowerBound());
			
			System.gc();
			startTime = System.nanoTime();
			heuristic = new BinPackingHeuristic(patterns);
			heuristic.binType = fifoShortestPath;
			heuristic.setComplexity(0);
			//heuristic.setVerbosity(true);
			heuristic.runFirstFitHeuristic();
			endTime = System.nanoTime();
			System.out.println(heuristic.printSolution());
			if (!heuristic.checkSolution())
				System.out.println("Bin Packer screwed up!!!!!!!!!!");
			fifoCounter[0] += heuristic.numberOfBins();
			fifoTimeCounter[0] += (endTime - startTime);
			heuristic = null;
			System.gc();
			
			/*
			startTime = System.nanoTime();
			kruskal = new KruskalHeuristic(patterns);
			kruskal.feasibilityType = KruskalHeuristic.contiguousOrdering;
			kruskal.runHeuristic();
			endTime = System.nanoTime();
			if (!kruskal.checkSolution())
				System.out.println("Kruskal screwed up!!!!!!!!!!");
			kruskalBinCounter += kruskal.numberOfBins();
			kruskalTimeCounter += (endTime - startTime);
			kruskal = null;
			System.gc();
			
			startTime = System.nanoTime();
			prim = new PrimHeuristic(patterns);
			prim.feasibilityType = PrimHeuristic.contiguousOrdering;
			int[][] solution = prim.runIterativeModifiedHeuristic();
			endTime = System.nanoTime();
			for (int[] template : solution) {
				PatternSet templateSet = patterns.subset(template);
				if (!FeasibilityTest.testInLineOrdering(templateSet))
					System.out.println("Prim screwed up!!!!!!!!!!");
			}
			primCounter[0] += solution.length;
			primTimeCounter[0] += (endTime - startTime);
			prim = null;
			System.gc();
			*/
			
			//System.out.println("=============================================");
			//System.out.println("Set #" + i);
			//System.out.println(heuristic.printSolution());
			
		}
		for (int level = 0; level < 4; level++) {
			System.out.print("FirstFit (Prim) (Level " + level + ") fit " + numPatterns + " on an average of " + (1d * primCounter[level] / numTests) + " templates");
			System.out.println(" in an average of " + (1d * primTimeCounter[level] / (numTests * 1000000000)) + " secs.");
			System.out.print("FirstFit (Floyd) (Level " + level + ") fit " + numPatterns + " on an average of " + (1d * floydCounter[level] / numTests) + " templates");
			System.out.println(" in an average of " + (1d * floydTimeCounter[level] / (numTests * 1000000000)) + " secs.");
			System.out.print("FirstFit (Fifo) (Level " + level + ") fit " + numPatterns + " on an average of " + (1d * fifoCounter[level] / numTests) + " templates");
			System.out.println(" in an average of " + (1d * fifoTimeCounter[level] / (numTests * 1000000000)) + " secs.");
			System.out.print("EmptySpace (Level " + level + ") fit " + numPatterns + " on an average of " + (1d * esCounter[level] / numTests) + " templates");
			System.out.println(" in an average of " + (1d * esTimeCounter[level] / (numTests * 1000000000)) + " secs.");
			System.out.println();
		}
		System.out.print("Kruskal fit " + numPatterns + " on an average of " + (1d * kruskalBinCounter / numTests) + " templates.");
		System.out.println("in an average of " + (1d * kruskalTimeCounter / (numTests * 1000000000)) + " secs.");
	}
	
	private class PatternComparator implements Comparator<Integer> {
		/**
		 * TODO
		 * 
		 * @param arg0
		 * @param arg1
		 * @return
		 */
		public int compare(Integer arg0, Integer arg1) {
			double densityA = patterns.getPattern(arg0).density();
			double densityB = patterns.getPattern(arg1).density();			
			if (densityA == densityB) {
				//if (arg0.hashCode() != arg1.hashCode())
					return arg0.hashCode() - arg1.hashCode();
				//else
					//throw new RuntimeException("I hope this never happens.");
			} else 
				return Double.compare(densityA, densityB);
		}
	}
	
	private class BinComparator implements Comparator<Bin> {

		/**
		 * TODO
		 * 
		 * @param arg0
		 * @param arg1
		 * @return
		 */
		public int compare(Bin arg0, Bin arg1) {
			if (arg0.binSize() == arg1.binSize()) {
				//if (arg0.hashCode() != arg1.hashCode())
					return arg0.hashCode() - arg1.hashCode();
				//else
					//throw new RuntimeException("I hope this never happens.");
			} else
				return arg0.binSize() - arg1.binSize();
		}
		
	}
}
