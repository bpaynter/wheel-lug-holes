/**
 * FloydHeuristic.java
 * Dec 1, 2010
 */
package heuristics;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import networks.CycleFinder;
import networks.FloydWarshall;
import networks.MinMeanCycle;
import networks.Network;
import networks.SimpleArc;

import exact.FeasibilityTest;
import exact.FitNetwork;
import exact.GurobiFitSolver;
import exact.GurobiMaxTemplateSolver;
import problem.CircularPatternSet;
import problem.LinearPatternSet;
import problem.Pattern;
import problem.PatternSet;
import problem.PolarPatternSet;
import util.IntegerUtils;
import util.MersenneTwisterFast;
import util.Permutation;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Dec 1, 2010
 *
 */
public class CycleHeuristic implements Bin {
	private PatternSet patterns;
	private double[][] w;
	private LinkedList<LinkedList<Long>> k;
	private long[][] ub;
	private long[][] lb;
	private LinkedList<Integer> currentTemplate;
	
	public int type;
	public static final int floydWarshall = 0;
	public static final int minMeanCycle = 1;
	public static final int fifoShortestPath = 2;
	
	private int complexityLevel;
	
	public static final int NoSortedAddition_NoAllPosition = 0;
	public static final int SortedAddition_NoAllPosition = 1;
	public static final int NoSortedAddition_AllPosition = 2;
	public static final int SortedAddition_AllPosition = 3;
	
	public boolean sortedAddition;
	public boolean allPositions;
	public boolean allPositionOrder;
	public static final boolean correct = true;
	public static final boolean alternate = false;
	
	public boolean verbose;
	
	public CycleHeuristic(PatternSet patterns) {
		this.patterns = patterns;
		ub = new long[patterns.numPatterns()][patterns.numPatterns()];
		lb = new long[patterns.numPatterns()][patterns.numPatterns()];
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++) {
				lb[i][j] = patterns.getKLowerBound(i, j);
				lb[j][i] = lb[i][j];
				ub[i][j] = patterns.getKUpperBound(i, j);
				ub[j][i] = ub[i][j];
			}
		currentTemplate = null;
		k = null;
		type = fifoShortestPath;
		sortedAddition = false;
		allPositions = false;
		allPositionOrder = alternate;
		complexityLevel = 0;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @param startPattern
	 */
	public CycleHeuristic(PatternSet patterns, int startPattern) {
		this(patterns);
		givenStart(startPattern);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param start
	 */
	public void givenStart(int start) {
		int[] template = {start};
		long[][] ks = {{0}};
		buildTemplate(template, ks);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param startPatterns
	 * @throws IllegalArgumentException
	 */
	public void givenStart(int[] startPatterns) throws IllegalArgumentException {
		PatternSet startPatternSet = patterns.subset(startPatterns);
		CycleHeuristic initializer = new CycleHeuristic(startPatternSet);
		initializer.type = this.type;
		long[][] solution = initializer.runHeuristic();
		if (solution == null)
			throw new IllegalArgumentException("Initial Pattern Set has no solution.");
		buildTemplate(startPatterns, solution);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param startPatterns
	 * @param ks
	 * @throws IllegalArgumentException
	 */
	public void givenStart(int[] startPatterns, long[][] ks) throws IllegalArgumentException {
		PatternSet startPatternSet = patterns.subset(startPatterns);
		startPatternSet.setSolution(ks);
		FitNetwork net = new FitNetwork(startPatterns.length);
		if (!net.testSolution(startPatternSet))
			throw new IllegalArgumentException("Invalid starting solution.");
		else
			buildTemplate(startPatterns, ks);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param template
	 * @param ks
	 */
	private void buildTemplate(int[] template, long[][] ks) throws IllegalArgumentException {
		if (template.length > patterns.numPatterns())
			throw new IllegalArgumentException("Too many patterns already in template.");
		currentTemplate = new LinkedList<Integer>();
		for (int i : template) {
			if ((i < 0) || (i >= patterns.numPatterns()))
				throw new IllegalArgumentException("Pattern Number out of Bounds.");
			currentTemplate.add(i);
		}
		k = new LinkedList<LinkedList<Long>>();
		for (long[] solutionRow : ks) {
			if (solutionRow.length != ks.length)
				throw new RuntimeException("K matrix is not square.");
			LinkedList<Long> row = new LinkedList<Long>();
			for (long i : solutionRow)
				row.add(i);
			k.add(row);
		}
	}
	
	/**
	 * 
	 * TODO
	 *
	 */
	public void firstStart() {
		int[] template = {0,1};
		long[][] ks = {{0,0},{0,0}};
		buildTemplate(template, ks);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public void smallestStart() {
		double minBBij = Double.MAX_VALUE;
		int minI = -1;
		int minJ = -1;
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++)
				if (patterns.getBB(i, j) < minBBij) {
					minBBij = patterns.getBB(i, j);
					minI = i;
					minJ = j;
				}
		int[] startPatterns = {minI, minJ};
		long[][] ks = {{0,0},{0,0}};
		buildTemplate(startPatterns, ks);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[] runIterativeHeuristicSmallestStart() {
		double minBBij = Double.MAX_VALUE;
		int minI = -1;
		int minJ = -1;
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++)
				if (patterns.getBB(i, j) < minBBij) {
					minBBij = patterns.getBB(i, j);
					minI = i;
					minJ = j;
				}
		int[] startPatterns1 = {minI, minJ};
		long[][] ks = {{0,0},{0,0}};
		buildTemplate(startPatterns1, ks);
		int[] solution1 = runIterativeHeuristic();
		int[] startPatterns2 = {minJ, minI};
		buildTemplate(startPatterns2, ks);
		int[] solution2 = runIterativeHeuristic();
		if (solution1.length > solution2.length)
			return solution1;
		else
			return solution2;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public void largestStart() {
		double maxBBij = 0;
		int maxI = -1;
		int maxJ = -1;
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++)
				if (patterns.getBB(i, j) > maxBBij) {
					maxBBij = patterns.getBB(i, j);
					maxI = i;
					maxJ = j;
				}
		int[] startPatterns = {maxI, maxJ};
		long[][] ks = {{0,0},{0,0}};
		buildTemplate(startPatterns, ks);	
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[] runIterativeHeuristicAllStart() {
		int[] bestSolution = {0};
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++) {
				int[] startingPatterns = {i, j};
				givenStart(startingPatterns);
				int[] currentSolution = runIterativeHeuristic();
				if (currentSolution.length == patterns.numPatterns())
					return currentSolution;
				else if (currentSolution.length > bestSolution.length)
					bestSolution = currentSolution;
			}
		return bestSolution;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param startPatterns
	 * @param ks
	 * @return
	 * @throws IllegalArgumentException
	 */
	public int[] runIterativeHeuristic() {
		if (currentTemplate == null)
			firstStart();
		Queue<SortablePattern> remainingPatterns;
		if (!sortedAddition)
			remainingPatterns = new LinkedList<SortablePattern>();
		else
			remainingPatterns = new PriorityQueue<SortablePattern>();
		if (!checkSolution())
			throw new IllegalArgumentException("Initial Pattern Set has invalid solution.");		
		// Populate the remaining patterns array
		for (int i = 0; i < patterns.numPatterns(); i++) {
			double minBB = Double.MAX_VALUE;
			for (int j = 0; j < currentTemplate.size(); j ++) {
				if (i == currentTemplate.get(j))
					minBB = -1d;
				else if (patterns.getBB(i, currentTemplate.get(j)) < minBB)
					minBB = patterns.getBB(i, currentTemplate.get(j));	
			}
			if (minBB >= 0)
				remainingPatterns.add(new SortablePattern(i, minBB));
		}
		while (!remainingPatterns.isEmpty()) {
			int candidatePattern = remainingPatterns.poll().pattern;
			if (verbose) {
				System.out.print("Current Template: " + currentTemplate.get(0));
				for (int i = 1; i < currentTemplate.size(); i++)
					System.out.print("-" + currentTemplate.get(i));
				System.out.println();
				System.out.print("Remaining Patterns: " + candidatePattern);
				for (SortablePattern p : remainingPatterns)
					System.out.print("-" + p.pattern);
				System.out.println();
				System.out.println("Trying to add pattern " + candidatePattern);
			}
			long[] newKs = null;
			if (allPositions)
				newKs = tryPatternAllPositions(candidatePattern);
			else
				newKs = tryPatternEnd(candidatePattern);
			if (newKs != null) {
				LinkedList<Long> newRow = new LinkedList<Long>();
				for (int i = 0; i < newKs.length - 1; i++) {
					newRow.add(newKs[i]);
					k.get(i).add(newKs[i]);
				}
				newRow.add(0l);
				k.add(newRow);
				currentTemplate.add(candidatePattern);
				if (verbose) {
					String endln = System.getProperty("line.separator");
					String s = "";
					s += "      ";
					for (int j = 0; j < currentTemplate.size(); j++)
						s += String.format("%1$6s", "P_" + currentTemplate.get(j));
					s += endln;
					for (int i = 0; i < currentTemplate.size(); i++) {
						s += String.format("%1$6s", "P_" + currentTemplate.get(i));
						for (int j = 0; j < currentTemplate.size(); j++)
							s += String.format("%1$6s", k.get(i).get(j));
						s += endln;
					}
					System.out.println(s);
				}
				if (sortedAddition) {
					PriorityQueue<SortablePattern> newQueue = new PriorityQueue<SortablePattern>();
					while (!remainingPatterns.isEmpty()) {
						SortablePattern currentPattern = remainingPatterns.poll();
						if (currentPattern.BBij > patterns.getBB(candidatePattern, currentPattern.pattern))
							currentPattern.BBij = patterns.getBB(candidatePattern, currentPattern.pattern);
						newQueue.add(currentPattern);
					}
					remainingPatterns = newQueue;
				}
			}
		}
		int[] outTemplate = new int[currentTemplate.size()];
		for (int i = 0; i < currentTemplate.size(); i++)
			outTemplate[i] = currentTemplate.get(i);
		return outTemplate;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param template
	 * @param templateSize
	 * @param newPattern
	 * @param cycleFinder
	 * @return
	 */
	private long[] tryPatternEnd(int newPattern) {
		CycleFinder cycleFinder = null;
		if (type == floydWarshall)
			cycleFinder = new FloydWarshall(currentTemplate.size() + 1);
		else if (type == minMeanCycle)
			cycleFinder = new MinMeanCycle(currentTemplate.size() + 1);
		else if (type == fifoShortestPath)
			cycleFinder = new Network(currentTemplate.size() + 1);
		cycleFinder.verbose(this.verbose);
		long[] newKs = new long[currentTemplate.size() + 1];
		Arrays.fill(newKs, 0);
		boolean[] up = new boolean[currentTemplate.size() + 1];
		Arrays.fill(up, true);
		boolean[] down = new boolean[currentTemplate.size() + 1];
		Arrays.fill(down, true);
		w = new double[currentTemplate.size() + 1][currentTemplate.size() + 1];
		for (int i = 0; i < currentTemplate.size(); i++) {
			w[i][i] = 0;
			w[i][currentTemplate.size()] = patterns.getB(currentTemplate.get(i), newPattern) - patterns.getD(currentTemplate.get(i), newPattern);
			w[currentTemplate.size()][i] = -patterns.getD(newPattern, currentTemplate.get(i));
			for (int j = i+1; j < currentTemplate.size(); j++) {
				w[i][j] = (k.get(i).get(j) + 1)*patterns.getB(currentTemplate.get(i), currentTemplate.get(j)) - patterns.getD(currentTemplate.get(i), currentTemplate.get(j));
				w[j][i] = -k.get(i).get(j)*patterns.getB(currentTemplate.get(j), currentTemplate.get(i)) - patterns.getD(currentTemplate.get(j), currentTemplate.get(i));
			}
		}
		if (verbose) {
			String endln = System.getProperty("line.separator");
			String s = "";
			s += "k_ij  ";
			for (int j = 0; j < currentTemplate.size(); j++)
				s += String.format("%1$6s", "P_" + currentTemplate.get(j));
			s += String.format("%1$6s", "P_" + newPattern);
			s += endln;
			for (int i = 0; i < currentTemplate.size(); i++) {
				s += String.format("%1$6s", "P_" + currentTemplate.get(i));
				for (int j = 0; j < currentTemplate.size(); j++)
					s += String.format("%1$6s", k.get(i).get(j));
				s += String.format("%1$6s", newKs[i]);
				s += endln;
			}
			s += String.format("%1$6s", "P_" + newPattern);
			for (int i = 0; i < currentTemplate.size() + 1; i++)
				s += String.format("%1$6s", newKs[i]);
			s += endln;
			System.out.println(s);
			s = "";
			s += "w_ij  ";
			for (int j = 0; j < currentTemplate.size(); j++)
				s += String.format("%1$6s", "P_" + currentTemplate.get(j));
			s += String.format("%1$6s", "P_" + newPattern);
			s += endln;
			for (int i = 0; i < currentTemplate.size(); i++) {
				s += String.format("%1$6s", "P_" + currentTemplate.get(i));
				for (int j = 0; j < currentTemplate.size(); j++)
					s += String.format("%1$6s", w[i][j]);
				s += String.format("%1$6s", w[i][currentTemplate.size()]);
				s += endln;
			}
			s += String.format("%1$6s", "P_" + newPattern);
			for (int i = 0; i < currentTemplate.size() + 1; i++)
				s += String.format("%1$6s", w[currentTemplate.size()][i]);
			s += endln;
			System.out.println(s);
		}
		boolean positionDone = false;
		while (!positionDone) {
			SimpleArc[] negativeCycle = cycleFinder.getNegativeCycle(w);
			if (negativeCycle != null) {
				if (verbose) {
					System.out.print("Negative cycle found:");
					for (SimpleArc a : negativeCycle) 
						System.out.print(a.from + " -> " + a.to + ", ");
					System.out.println();
				}
				int candidateI = -1;
				int candidateJ = -1;
				for (SimpleArc a : negativeCycle) {
					int i = a.from;
					int j = a.to;
					if ((i == currentTemplate.size()) || (j == currentTemplate.size())) {
						if ((i < j) && (up[i]) && (newKs[i] < ub[currentTemplate.get(i)][newPattern])) {
							if (candidateI == -1) { // || (patterns.getB(currentTemplate.get(i), newPattern) < patterns.getB(candidateI, candidateJ))) {
								candidateI = i;
								candidateJ = j;
							}
						} else if ((i > j) && (down[j]) && (newKs[j] > lb[currentTemplate.get(j)][newPattern])) {
							if (candidateI == -1) { // || (patterns.getB(currentTemplate.get(j), newPattern) < patterns.getB(candidateI, candidateJ))) {
								candidateI = i;
								candidateJ = j;
							}
						}
					}
				}
				if (candidateI == -1) {
					if (verbose)
						System.out.println("No Candidate Found.");
					positionDone = true;
				} else {
					int i = candidateI;
					int j = candidateJ;
					if (i < j) {
						if (verbose)
							System.out.println("Increasing k_{" + i + ", " + j + "}");
						newKs[i]++;
						down[i] = false;
						w[i][j] = (newKs[i] + 1)*patterns.getB(currentTemplate.get(i), newPattern) - patterns.getD(currentTemplate.get(i), newPattern);
						w[j][i] = -newKs[i]*patterns.getB(currentTemplate.get(i), newPattern) - patterns.getD(newPattern, currentTemplate.get(i));
					}
					if (i > j) {
						if (verbose)
							System.out.println("Decreasing k_{" + i + ", " + j + "}");
						newKs[j]--;
						up[j] = false;
						w[j][i] = (newKs[j] + 1)*patterns.getB(currentTemplate.get(j), newPattern) - patterns.getD(currentTemplate.get(j), newPattern);
						w[i][j] = -newKs[j]*patterns.getB(currentTemplate.get(j), newPattern) - patterns.getD(newPattern, currentTemplate.get(j));
					}
					
				}
				if (verbose) {
					String endln = System.getProperty("line.separator");
					String s = "";
					s += "      ";
					for (int j = 0; j < currentTemplate.size(); j++)
						s += String.format("%1$6s", "P_" + currentTemplate.get(j));
					s += String.format("%1$6s", "P_" + newPattern);
					s += endln;
					for (int i = 0; i < currentTemplate.size(); i++) {
						s += String.format("%1$6s", "P_" + currentTemplate.get(i));
						for (int j = 0; j < currentTemplate.size(); j++)
							s += String.format("%1$6s", k.get(i).get(j));
						s += String.format("%1$6s", newKs[i]);
						s += endln;
					}
					s += String.format("%1$6s", "P_" + newPattern);
					for (int i = 0; i < currentTemplate.size() + 1; i++)
						s += String.format("%1$6s", newKs[i]);
					s += endln;
					System.out.println(s);
					s = "";
					s += "w_ij  ";
					for (int j = 0; j < currentTemplate.size(); j++)
						s += String.format("%1$6s", "P_" + currentTemplate.get(j));
					s += String.format("%1$6s", "P_" + newPattern);
					s += endln;
					for (int i = 0; i < currentTemplate.size(); i++) {
						s += String.format("%1$6s", "P_" + currentTemplate.get(i));
						for (int j = 0; j < currentTemplate.size(); j++)
							s += String.format("%1$6s", w[i][j]);
						s += String.format("%1$6s", w[i][currentTemplate.size()]);
						s += endln;
					}
					s += String.format("%1$6s", "P_" + newPattern);
					for (int i = 0; i < currentTemplate.size() + 1; i++)
						s += String.format("%1$6s", w[currentTemplate.size()][i]);
					s += endln;
					System.out.println(s);
				}
			} else
				return newKs;
		}
		return null;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param template
	 * @param templateSize
	 * @param newPattern
	 * @param cycleFinder
	 * @return
	 */
	private long[] tryPatternAllPositions(int newPattern) {
		CycleFinder cycleFinder = null;
		if (type == floydWarshall)
			cycleFinder = new FloydWarshall(currentTemplate.size() + 1);
		else if (type == minMeanCycle)
			cycleFinder = new MinMeanCycle(currentTemplate.size() + 1);
		else if (type == fifoShortestPath)
			cycleFinder = new Network(currentTemplate.size() + 1);
		cycleFinder.verbose(this.verbose);
		long[] newKs = new long[currentTemplate.size() + 1];
		Arrays.fill(newKs, 0);
		boolean[] up = new boolean[currentTemplate.size() + 1];
		Arrays.fill(up, true);
		boolean[] down = new boolean[currentTemplate.size() + 1];
		Arrays.fill(down, true);
		w = new double[currentTemplate.size() + 1][currentTemplate.size() + 1];
		
		int[] permutation = new int[currentTemplate.size() + 1];
		for (int i = 0; i < permutation.length; i++)
			permutation[i] = i;
		for (int position = currentTemplate.size(); position > -1; position--) {
			if (position < currentTemplate.size()) {
				int temp = permutation[position];
				permutation[position] = permutation[position + 1];
				permutation[position + 1] = temp;
			}
			if (verbose) {
				System.out.println("Position " + position);
				System.out.println("Current Permutation: " + Permutation.printPerm(permutation));
			}
			for (int i = 0; i < currentTemplate.size() + 1; i++) {
				if (i >= position)
					newKs[i] = -1;
				else
					newKs[i] = 0;
			}
			if (verbose) {
				String endln = System.getProperty("line.separator");
				String s = "";
				s += "      ";
				for (int j = 0; j < currentTemplate.size(); j++)
					s += String.format("%1$6s", "P_" + currentTemplate.get(j));
				s += String.format("%1$6s", "P_" + newPattern);
				s += endln;
				for (int i = 0; i < currentTemplate.size(); i++) {
					s += String.format("%1$6s", "P_" + currentTemplate.get(i));
					for (int j = 0; j < currentTemplate.size(); j++)
						s += String.format("%1$6s", k.get(i).get(j));
					s += String.format("%1$6s", newKs[i]);
					s += endln;
				}
				s += String.format("%1$6s", "P_" + newPattern);
				for (int i = 0; i < currentTemplate.size() + 1; i++)
					s += String.format("%1$6s", newKs[i]);
				s += endln;
				System.out.println(s);
			}
			for (int i = 0; i < currentTemplate.size(); i++) {
				w[i][i] = 0;
				w[i][currentTemplate.size()] = patterns.getB(currentTemplate.get(i), newPattern) - patterns.getD(currentTemplate.get(i), newPattern);
				w[currentTemplate.size()][i] = -patterns.getD(newPattern, currentTemplate.get(i));
				for (int j = i+1; j < currentTemplate.size(); j++) {
					w[i][j] = (k.get(i).get(j) + 1)*patterns.getB(currentTemplate.get(i), currentTemplate.get(j)) - patterns.getD(currentTemplate.get(i), currentTemplate.get(j));
					w[j][i] = -k.get(i).get(j)*patterns.getB(currentTemplate.get(i), currentTemplate.get(j)) - patterns.getD(currentTemplate.get(j), currentTemplate.get(i));
				}
			}
			boolean positionDone = false;
			while (!positionDone) {
				SimpleArc[] negativeCycle;
				if (allPositionOrder == correct)
					negativeCycle = cycleFinder.getNegativeCycle(w, permutation);
				else
					negativeCycle = cycleFinder.getNegativeCycle(w);				
				if (negativeCycle != null) {
					if (verbose)
						System.out.println("Negative cycle found.");
					int candidateI = -1;
					int candidateJ = -1;
					for (SimpleArc a : negativeCycle) {
						int i = a.from;
						int j = a.to;
						if ((i == currentTemplate.size()) || (j == currentTemplate.size())) {
							if ((i < j) && (up[i]) && (newKs[i] < ub[currentTemplate.get(i)][newPattern])) {
								if (candidateI == -1) { // || (patterns.getB(currentTemplate.get(i), newPattern) < patterns.getB(candidateI, candidateJ))) {
									candidateI = i;
									candidateJ = j;
								}
							} else if ((i > j) && (down[j]) && (newKs[j] > lb[currentTemplate.get(j)][newPattern])) {
								if (candidateI == -1) { // || (patterns.getB(currentTemplate.get(j), newPattern) < patterns.getB(candidateI, candidateJ))) {
									candidateI = i;
									candidateJ = j;
								}
							}
						}
					}
					if (candidateI == -1) {
						if (verbose)
							System.out.println("No Candidate Found.");
						positionDone = true;
					} else {
						int i = candidateI;
						int j = candidateJ;
						if (i < j) {
							if (verbose)
								System.out.println("Increasing k_{" + i + ", " + j + "}");
							newKs[i]++;
							down[i] = false;
							w[i][j] = (newKs[i] + 1)*patterns.getB(currentTemplate.get(i), newPattern) - patterns.getD(currentTemplate.get(i), newPattern);
							w[j][i] = -newKs[i]*patterns.getB(currentTemplate.get(i), newPattern) - patterns.getD(newPattern, currentTemplate.get(i));
						}
						if (i > j) {
							if (verbose)
								System.out.println("Decreasing k_{" + i + ", " + j + "}");
							newKs[j]--;
							up[j] = false;
							w[j][i] = (newKs[j] + 1)*patterns.getB(currentTemplate.get(j), newPattern) - patterns.getD(currentTemplate.get(j), newPattern);
							w[i][j] = -newKs[j]*patterns.getB(currentTemplate.get(j), newPattern) - patterns.getD(newPattern, currentTemplate.get(j));
						}
						
					}
				} else
					return newKs;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	
	public long[][] runHeuristic() {
		w = new double[patterns.numPatterns()][patterns.numPatterns()];
		long[][] k = new long[patterns.numPatterns()][patterns.numPatterns()];
		boolean[][] up = new boolean[patterns.numPatterns()][patterns.numPatterns()];
		for (boolean[] row : up)
			Arrays.fill(row, true);
		boolean[][] down = new boolean[patterns.numPatterns()][patterns.numPatterns()];
		for (boolean[] row : down)
			Arrays.fill(row, true);
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++) {
				k[i][j] = 0;
				k[j][i] = 0;
				w[i][j] = (k[i][j] + 1)*patterns.getB(i, j) - patterns.getD(i, j);
				w[j][i] = -k[i][j]*patterns.getB(i, j) - patterns.getD(j, i);
			}
		boolean done = false;
		CycleFinder cycleFinder = null;
		if (type == floydWarshall)
			cycleFinder = new FloydWarshall(patterns.numPatterns());
		else if (type == minMeanCycle)
			cycleFinder = new MinMeanCycle(patterns.numPatterns());
		else if (type == fifoShortestPath)
			cycleFinder = new Network(patterns.numPatterns());
		while (!done) {
			if (verbose) {
				String endln = System.getProperty("line.separator");
				String s = "";
				s += "      ";
				for (int j = 0; j < patterns.numPatterns(); j++)
					s += String.format("%1$6s", "P_" + j);
				s += endln;
				for (int i = 0; i < patterns.numPatterns(); i++) {
					s += String.format("%1$6s", "P_" + i);
					for (int j = 0; j < patterns.numPatterns(); j++)
						s += String.format("%1$6s", k[i][j]);
					s += endln;
				}
				System.out.println(s);
			}
			SimpleArc[] negativeCycle = cycleFinder.getNegativeCycle(w);
			if (negativeCycle != null) {
				if (verbose)
					System.out.println("Negative cycle found.");
				int candidateI = -1;
				int candidateJ = -1;
				for (SimpleArc a : negativeCycle) {
					int i = a.from;
					int j = a.to;
					if ((i < j) && (up[i][j]) && (k[i][j] < ub[i][j])) {
						if ((candidateI == -1) || (patterns.getB(i, j) < patterns.getB(candidateI, candidateJ))) {
							candidateI = i;
							candidateJ = j;
						}
					} else if ((i > j) && (down[i][j]) && (k[i][j] > lb[i][j])) {
						if ((candidateI == -1) || (patterns.getB(i, j) < patterns.getB(candidateI, candidateJ))) {
							candidateI = i;
							candidateJ = j;
						}
					}
				}
				if (candidateI == -1) {
					if (verbose)
						System.out.println("No Candidate Found.");
					return null;
				} else {
					int i = candidateI;
					int j = candidateJ;
					if (i < j) {
						if (verbose)
							System.out.println("Increasing k_{" + i + ", " + j + "}");
						k[i][j]++;
						k[j][i]++;
						down[i][j] = false;
						down[j][i] = false;
						w[i][j] = (k[i][j] + 1)*patterns.getB(i, j) - patterns.getD(i, j);
						w[j][i] = -k[i][j]*patterns.getB(i, j) - patterns.getD(j, i);
					}
					if (i > j) {
						if (verbose)
							System.out.println("Decreasing k_{" + i + ", " + j + "}");
						k[i][j]--;
						k[j][i]--;
						up[i][j] = false;
						up[j][i] = false;
						w[j][i] = (k[i][j] + 1)*patterns.getB(i, j) - patterns.getD(j, i);
						w[i][j] = -k[i][j]*patterns.getB(i, j) - patterns.getD(i, j);
					}
					
				}
			} else
				return k;
		}
		return k;
	}
	
	
	/*
	 * The following methods implement the Bin interface
	 */
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public Iterator<Integer> iterator() {
		return (currentTemplate != null) ? currentTemplate.iterator() : null;
	}

	/**
	 * TODO
	 * 
	 * @param item
	 * @return
	 */
	public boolean fit(int item) {
		for (Integer i : currentTemplate)
			if (patterns.getBB(i, item) < 0)
				return false;
		long[] newKs = null;
		if (allPositions)
			newKs = tryPatternAllPositions(item);
		else
			newKs = tryPatternEnd(item);
		if (newKs != null)
			return true;
		else
			return false;
	}

	/**
	 * TODO
	 * 
	 * @param item
	 * @return
	 */
	public boolean insert(int item) {
		for (Integer i : currentTemplate)
			if (patterns.getBB(i, item) < 0)
				return false;
		long[] newKs = null;
		if (allPositions)
			newKs = tryPatternAllPositions(item);
		else
			newKs = tryPatternEnd(item);
		if (newKs != null) {
			LinkedList<Long> newRow = new LinkedList<Long>();
			for (int i = 0; i < newKs.length - 1; i++) {
				newRow.add(newKs[i]);
				k.get(i).add(newKs[i]);
			}
			newRow.add(0l);
			k.add(newRow);
			currentTemplate.add(item);
			return true;
		} else
			return false;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public boolean reSolve() {
		throw new UnsupportedOperationException("This method has not yet been implemented.");
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public int[] outputIndices() {
		if (currentTemplate == null)
			return null;
		else {
			int[] template = new int[currentTemplate.size()];
			for (int i = 0; i < currentTemplate.size(); i++)
				template[i] = currentTemplate.get(i);
			return template;
		}
	}

	/**
	 * TODO
	 * 
	 * @param level
	 */
	public void setLevel(int level) throws IllegalArgumentException {
		if ((level < 0) || (level > numLevels()))
			throw new IllegalArgumentException("Argument out of bounds: " + level);
		if (level >= NoSortedAddition_AllPosition)
			allPositions = true;
		else
			allPositions = false;
		if (level % 2 == 0)
			sortedAddition = false;
		else
			sortedAddition = true;
	}
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public int numLevels() {
		return SortedAddition_AllPosition;
	}
	
	public void setVerbosity(boolean v) {
		verbose = v;
	}

	/**
	 * TODO
	 * 
	 * @param item
	 * @return
	 */
	public Bin newBin(int item) {
		CycleHeuristic newBin = new CycleHeuristic(patterns);
		newBin.givenStart(item);
		newBin.type = this.type;
		newBin.setLevel(complexityLevel);
		return newBin;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public boolean checkSolution() {
		if (currentTemplate == null)
			return true;
		else {
			int[] template = new int[currentTemplate.size()];
			for (int i = 0; i < currentTemplate.size(); i++)
				template[i] = currentTemplate.get(i);
			long[][] ks = new long[k.size()][];
			for (int i = 0; i < k.size(); i++) {
				ks[i] = new long[k.get(i).size()];
				for (int j = 0; j < k.get(i).size(); j++)
					ks[i][j] = k.get(i).get(j);
			}
			PatternSet solution = patterns.subset(template);
			solution.setSolution(ks);
			if (verbose)
				System.out.println(solution.printSolution());
			FitNetwork net = new FitNetwork(solution.numPatterns());
			return net.testSolution(solution);
		}
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public String printSolution() {
		if (currentTemplate == null)
			return null;
		else {
			int[] template = new int[currentTemplate.size()];
			for (int i = 0; i < currentTemplate.size(); i++)
				template[i] = currentTemplate.get(i);
			long[][] ks = new long[k.size()][];
			for (int i = 0; i < k.size(); i++) {
				ks[i] = new long[k.get(i).size()];
				for (int j = 0; j < k.get(i).size(); j++)
					ks[i][j] = k.get(i).get(j);
			}
			PatternSet solution = patterns.subset(template);
			solution.setSolution(ks);
			return solution.printSolution();
		}
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public int binSize() {
		return (currentTemplate != null) ? currentTemplate.size() : 0;
	}
	
	
	public static void main(String[] args) {
		
		//int numPatterns = 5;
		int numTests = 11;
		for (int numPatterns = 4; numPatterns <= 4; numPatterns++) {
			MersenneTwisterFast random = new MersenneTwisterFast(957483972l); // When finding publication numbers Add multiplier to seed
			MersenneTwisterFast permutationRandomizer = new MersenneTwisterFast(548257483758l);
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
				
				//System.out.println(patterns);
				/*
				kruskal = new KruskalHeuristic(patterns);
				kruskal.runHeuristic();
				int[][] kruskalSolution = kruskal.outputSolutionIndices();
				boolean[] inSolution = new boolean[patterns.numPatterns()];
				Arrays.fill(inSolution, false);
				int[] startingOrder = new int[patterns.numPatterns()];
				int startingOrderIndex = 0;
				for (int j = 0; j < kruskalSolution.length; j++)
					for (int k = 0; k < kruskalSolution[j].length; k++) {
						startingOrder[startingOrderIndex++] = kruskalSolution[j][k];
						inSolution[kruskalSolution[j][k]] = true;
					}
				for (boolean in : inSolution)
					if (!in)
						throw new RuntimeException("Prim returned bad data.");
				*/
				/*
				boolean ZeroSolution = false;
				PrimHeuristic prim = new PrimHeuristic(patterns);
				prim.runModifiedAllStart();
				int[] primOutput = prim.outputSolutionIndices();
				if (primOutput.length == numPatterns) {
					patterns.setZeroSolution(primOutput);
					successes++;
					ZeroSolution = true;
				} else if (FeasibilityTest.findZeroOrdering(patterns)) {
					ZeroSolution = true;
				}
				if (ZeroSolution) {
					if (!net.testSolution(patterns))
						System.out.println("CycleFinder Screwed Up!!!!");
					else {
						zeroSolutions++;
						inLineSolutions++;
					}
					//System.out.println(patterns);

				} else {
					if (FeasibilityTest.findInLineOrdering(patterns)) {
						inLineSolutions++;
						FeasibilityTest.findKsFromStartingPositions(patterns);
						//System.out.println("Test #" + i);
						//System.out.println(patterns.printSolution());
					}
				}
				*/
				
				/*
				KruskalHeuristic kruskal = new KruskalHeuristic(patterns);
				//PrimHeuristic prim = new PrimHeuristic(patterns);
				kruskal.feasibilityType = KruskalHeuristic.zeroFeasibleOrdering;
				//kruskal.verbose = true;
				kruskal.runHeuristic();
				if (kruskal.outputSolutionIndices()[0].length == patterns.numPatterns()) {
					kruskalCounter1++;
					int[] kruskalOutput = kruskal.outputSolutionIndices()[0];
					patterns.permute(kruskalOutput);
					System.out.println(patterns);
					if (!FeasibilityTest.testInLineOrdering(patterns))
						System.out.println("Kruskal Screwed Up!!!!!");
				}
				*/
				/*
				prim.smallestStart();
				prim.feasibilityType = PrimHeuristic.inlineFeasible;
				prim.runBasicHeuristic();
				if (prim.outputSolution().length == patterns.numPatterns()) {
					primCounter1++;
					int[] primOutput = prim.outputSolutionIndices();
					patterns.permute(primOutput);
					if (!FeasibilityTest.testInLineOrdering(patterns))
						System.out.println("Basic Prim Screwed Up!!!!!");
				}
				prim.smallestStart();
				//prim.verbose = true;
				prim.feasibilityType = PrimHeuristic.inlineFeasible;
				prim.runModifiedHeuristic();
				if (prim.outputSolution().length == patterns.numPatterns()) {
					primCounter2++;
					int[] primOutput = prim.outputSolutionIndices();
					patterns.permute(primOutput);
					if (!FeasibilityTest.testInLineOrdering(patterns))
						System.out.println("Modified Prim Screwed Up!!!!!");
				}
				prim.feasibilityType = PrimHeuristic.inlineFeasible;
				prim.runModifiedAllStart();
				if (prim.outputSolution().length == patterns.numPatterns()) {
					primCounter3++;
					int[] primOutput = prim.outputSolutionIndices();
					patterns.permute(primOutput);
					if (!FeasibilityTest.testInLineOrdering(patterns))
						System.out.println("Modified Prim Screwed Up!!!!!");
				}
				*/
				
				/*
				//System.out.println("=====================================");
				//System.out.println("Set #: " + i);
				//System.out.println(patterns);
				heuristic = new CycleHeuristic(patterns);
				//heuristic.verbose = true;
				heuristic.allPositions = true;
				heuristic.sortedAddition = true;
				heuristic.allPositionOrder = alternate;
				heuristic.type = fifoShortestPath;
				int[] output = heuristic.runIterativeHeuristicAllStart();
				if (output.length == numPatterns) {
					patterns.setSolution(output, heuristic.k);
					//System.out.println(patterns.printSolution());
					if (net.testSolution(patterns)) {
						successes++;
						solutions++;
					} else
						System.out.println("CycleFinder Screwed Up!!!!");
				} else {
					solver = new GurobiFitSolver(patterns);
					//solver.setVerbosity(false);
					if (solver.solve()) {
						if (solver.checkSolution())
							solutions++;
						else
							System.out.println("Gurobi Screwed Up!!!!");
					}
					solver.dispose();
					solver = null;
				}
				*/
				/*
				//System.out.println("=====================================");
				//System.out.println("Set #: " + i);
				//System.out.println(patterns);
				heuristic = new CycleHeuristic(patterns);
				//heuristic.verbose = true;
				heuristic.allPositions = false;
				heuristic.type = fifoShortestPath;
				if (heuristic.runHeuristic()) {
					patterns.setSolution(heuristic.k);
					//System.out.println(patterns.printSolution());
					if (net.testSolution(patterns)) {
						successes++;
						solutions++;
					} else
						System.out.println("CycleFinder Screwed Up!!!!");
				} else {
					heuristic = new CycleHeuristic(patterns);
					heuristic.type = minMeanCycle;
					if (heuristic.runHeuristic()) {
						patterns.setSolution(heuristic.k);
						//System.out.println(patterns.printSolution());
						if (net.testSolution(patterns)) {
							successes++;
							solutions++;
						} else
							System.out.println("CycleFinder Screwed Up!!!!");
					} else {
						heuristic = new CycleHeuristic(patterns);
						heuristic.allPositions = true;
						heuristic.type = floydWarshall;
						int[] output = heuristic.runIterativeHeuristic();
						if (output.length == numPatterns) {
							patterns.setSolution(output, heuristic.k);
							//System.out.println(patterns.printSolution());
							if (net.testSolution(patterns)) {
								successes++;
								solutions++;
							} else
								System.out.println("CycleFinder Screwed Up!!!!");
						}
					}
				}
				*/
				/*
				//System.out.println("=====================================");
				//System.out.println("Set #: " + i);
				//System.out.println(patterns);
				PrimHeuristic prim = new PrimHeuristic(patterns);
				prim.feasibilityType = PrimHeuristic.contiguousOrdering;
				prim.runModifiedAllStart();
				int[] primOutput = prim.outputSolutionIndices();
				if (primOutput.length == numPatterns) {
					if (prim.checkSolution())
						successes++;
					else
						System.out.println("Prim screwed up!!!!!!!!!");
				}
				*/
				
				System.out.println("=====================================");
				System.out.println("Set #: " + i);
				//System.out.println(patterns);
//				PrimHeuristic prim = new PrimHeuristic(patterns);
//				prim.feasibilityType = PrimHeuristic.zeroFeasibleOrdering;
//				prim.runModifiedAllStart();
//				int[] primOutput = prim.outputSolutionIndices();
//				PatternSet primOutputSet = patterns.subset(prim.outputSolutionIndices());
//				if (FeasibilityTest.testInLineOrdering(primOutputSet))
//					FeasibilityTest.findKsFromStartingPositions(primOutputSet);
//				else
//					throw new RuntimeException("This shouldn't happen: Infeasible Contiguous Ordering");
//				FitNetwork smallNet = new FitNetwork(primOutput.length);
//				if (!smallNet.testSolution(primOutputSet))
//					throw new RuntimeException("This shouldn't happen: Bad Ks");
//				long[][] Ks = primOutputSet.getSolution().getKs();
				heuristic = new CycleHeuristic(patterns);
				System.out.println(patterns);
				
				heuristic.verbose = true;
				heuristic.allPositions = false;
				heuristic.sortedAddition = true;
				heuristic.type = fifoShortestPath;
				//heuristic.givenStart(primOutput, Ks);
				//heuristic.smallestStart();
				int[] output = heuristic.runIterativeHeuristicSmallestStart();
				boolean one = false;
				if (output.length == numPatterns)
					one = true;
				System.out.println("Permute.");
				patterns.permute(Permutation.randomPermutation(patterns.numPatterns(), permutationRandomizer));
				System.out.println(patterns);
				//heuristic.smallestStart();
				output = heuristic.runIterativeHeuristicSmallestStart();
				if ((output.length == numPatterns) != one) {
					System.out.println("PROBLEM");
					if (heuristic.checkSolution()) {
						successes++;
						//System.out.println(heuristic.printSolution());
					} else
						System.out.println("CycleFinder Screwed Up!!!!");
				}
				
				/*
				//System.out.println("=====================================");
				//System.out.println("Set #: " + i);
				//System.out.println(patterns);
				PrimHeuristic prim = new PrimHeuristic(patterns);
				prim.runModifiedAllStart();
				int[] primOutput = prim.outputSolutionIndices();
				heuristic = new CycleHeuristic(patterns);
				//heuristic.verbose = true;
				heuristic.allPositions = false;
				heuristic.type = floydWarshall;
				int[] output = heuristic.runIterativeHeuristic(primOutput);
				if (output.length == numPatterns) {
					patterns.setSolution(output, heuristic.k);
					//System.out.println(patterns.printSolution());
					if (net.testSolution(patterns)) {
						successes++;
						solutions++;
					} else
						System.out.println("CycleFinder Screwed Up!!!!");
				} else {
					int[][] ks = heuristic.k;
					heuristic = new CycleHeuristic(patterns);
					//heuristic.verbose = true;
					heuristic.allPositions = false;
					heuristic.type = fifoShortestPath;
					output = heuristic.runIterativeHeuristic(output, ks);
					if (output.length == numPatterns) {
						patterns.setSolution(output, heuristic.k);
						//System.out.println(patterns.printSolution());
						if (net.testSolution(patterns)) {
							successes++;
							solutions++;
						} else
							System.out.println("CycleFinder Screwed Up!!!!");
					}
				}
				*/
				/*
				//System.out.println("=====================================");
				//System.out.println("Set #: " + i);
				//System.out.println(patterns);
				heuristic = new CycleHeuristic(patterns);
				//heuristic.verbose = true;
				heuristic.allPositions = true;
				heuristic.sortedAddition = true;
				heuristic.type) = fifoShortestPath;
				int[] output = heuristic.runIterativeHeuristic();
				//heuristic.verbose = true;
				if (output.length == numPatterns) {
				//System.out.println(patterns.printSolution());
					if (heuristic.checkSolution()) {
						successes++;
						solutions++;
					} else
						throw new RuntimeException("CycleFinder screwed up!!!");
						//System.out.println("CycleFinder Screwed Up!!!!");
				}
				*/
				/*
				//System.out.println(patterns);
				heuristic = new CycleHeuristic(patterns);
				//heuristic.verbose = true;
				heuristic.type = floydWarshall;
				if (heuristic.runHeuristic()) {
					patterns.setSolution(heuristic.k);
					//System.out.println(patterns.printSolution());
					if (net.testSolution(patterns)) {
						successes++;
						solutions++;
						FeasibilityTest.findKsFromStartingPositions(patterns);
						if (!net.testSolution(patterns))
							System.out.println("Starting Position Tester Screwed Up!!!!!!");
					} else
						System.out.println("CycleFinder Screwed Up!!!!");
				}
				*/
				/*
				//System.out.println(patterns);
				PrimHeurisic prim = new PrimHeuristic(patterns);
				prim.runModifiedAllStart();
				int[] primOutput = prim.outputSolutionIndices();
				boolean[] inSolution = new boolean[patterns.numPatterns()];
				Arrays.fill(inSolution, false);
				int index = 0;
				int[] heuristicInput = new int[patterns.numPatterns()];
				for (int j = 0; j < primOutput.length; j++) {
					heuristicInput[index] = primOutput[j];
					index++;
					inSolution[primOutput[j]] = true;
				}
				for (int j = 0; j < patterns.numPatterns(); j++)
					if (!inSolution[j])
						heuristicInput[index++] = j;
				patterns.permute(heuristicInput);
				heuristic = new CycleHeuristic(patterns);
				//heuristic.verbose = true;
				heuristic.type = minMeanCycle;
				if (heuristic.runHeuristic()) {
					patterns.setSolution(heuristic.k);
					//System.out.println(patterns.printSolution());
					if (net.testSolution(patterns)) {
						successes++;
						solutions++;
					} else
						System.out.println("CycleFinder Screwed Up!!!!");
				}
				*/
				/*
				//System.out.println(patterns);
				//KruskalHeuristic kruskal = new KruskalHeuristic(patterns);
				//kruskal.runHeuristic();
				//int[][] kruskalOutput = kruskal.outputSolutionIndices();
				PrimHeuristic kruskal = new PrimHeuristic(patterns);
				int[][] kruskalOutput = kruskal.runIterativeModifiedHeuristic();
				int index = 0;
				int[] heuristicInput = new int[patterns.numPatterns()];
				for (int j = 0; j < kruskalOutput.length; j++)
					for (int k = 0; k < kruskalOutput[j].length; k++)
						heuristicInput[index++] = kruskalOutput[j][k];
				patterns.permute(heuristicInput);
				heuristic = new CycleHeuristic(patterns);
				//heuristic.verbose = true;
				heuristic.type = fifoShortestPath;
				if (heuristic.runHeuristic()) {
					patterns.setSolution(heuristic.k);
					//System.out.println(patterns.printSolution());
					if (net.testSolution(patterns)) {
						successes++;
						solutions++;
					} else
						System.out.println("CycleFinder Screwed Up!!!!");
				}
				*/
				/*
				boolean done = false;
				int counter = 0;
				heuristic = new CycleHeuristic(patterns);
				heuristic.type = minMeanCycle;
				heuristic.allPositions = false;
				while (!done) {
					patterns.setPermutation(Permutation.randomPermutation(patterns.numPatterns(), permutationRandomizer));
					//System.out.println(patterns);
					//heuristic.verbose = true;
					int[] output = heuristic.runIterativeHeuristic();
					if (output.length == numPatterns) {
						patterns.setSolution(output, heuristic.k);
						//System.out.println(patterns.printSolution());
						if (net.testSolution(patterns)) {
							successes++;
							solutions++;
							done = true;
							if (!FeasibilityTest.testStartingPositions(patterns))
								System.out.println("Starting Position Tester Screwed Up!!!!!!");
						} else
							System.out.println("CycleFinder Screwed Up!!!!");
					}
					if (++counter >= numPermutations)
						done = true;
				}
				*/
				/*if (!found) {
					GurobiFitSolver solver = new GurobiFitSolver(patterns);
					if (solver.solve()) {
						if (solver.checkSolution())
							solutions++;
						else
							System.out.println("Gurobi Screwed Up!!!!");
					}
				}*/
			
			}
			System.out.print("With " + numPatterns + " patterns per set, ");
			System.out.println("CycleFinder succeeded " + successes + ", " + solutions + " times out of "+ numTests + " tests.");
			//System.out.println("Prim succeeded " + successes + " times out of " + zeroSolutions + " zero-feasible solutions out of " + inLineSolutions + " inline-feasible solutions out of "+ numTests + " tests.");
			//System.out.println("Kruskal succeeded " + kruskalCounter1 + " times, Basic prim " + primCounter1 + " times, Modified Prim " + primCounter2 + " times and AllStart Modified Prim " + primCounter3 + " times.");
			
		}
		
		/*
		//int numPatterns = 50;
		int numTests = 1000;
		for (int numPatterns = 25; numPatterns <= 250; numPatterns += 25) {
			MersenneTwisterFast random = new MersenneTwisterFast(5859468024l);
			//MersenneTwisterFast startRandomizer = new MersenneTwisterFast(5752846548926l);
			CycleHeuristic heuristic;
			//GurobiMaxTemplateSolver solver = new GurobiMaxTemplateSolver();
			IntegerUtils integers = new IntegerUtils(1000);
			int templateSum = 0;
			int badSetCounter = 0;
			for (int i = 0; i < numTests; i++) {
				if (Math.floor(1.0 * i/10) == 1.0 * i/10)
					System.out.println(i);
				PatternSet patterns = PatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers);
				boolean valid = false;
				while (!valid) {
					valid = true;
					try {
						patterns = PatternSet.randomTightPatternSet(numPatterns, 30, 1, 1000, 3, random, integers);
					} catch (IllegalArgumentException e) {
						badSetCounter++;
						if (1.0 * badSetCounter / 100 == Math.floor(1.0 * badSetCounter / 100))
							System.out.println(badSetCounter);
						valid = false;
					}
				}
				*/
				/*
				solver.clear();
				solver.setPatterns(patterns);
				solver.solve();
				if (!solver.checkSolution())
					System.err.println("Gurobi Screwed Up!");
				*/
				/*
				heuristic = new CycleHeuristic(patterns);
				//heuristic.feasibilityType = PrimHeuristic.inlineFeasible;
				heuristic.type = fifoShortestPath;
				heuristic.allPositions = false;
				//heuristic.verbose = true;
				//heuristic.randomStart(startRandomizer);
				//heuristic.runModifiedAllStart();
				int[] bestSolution = heuristic.runIterativeHeuristicSmallestStart();
				//System.out.println(bestSolution.length);
				templateSum += bestSolution.length;
				//templateSum += solver.getObjectiveValue();
			}
			System.out.print("With " + numPatterns + " patterns per set, ");
			System.out.print("there were " + badSetCounter + " Bad sets and ");
			System.out.println("the Average number of Patterns on Max Template was " + (1.0 * templateSum / numTests));
		}
		*/
		
		/*
		// Max Template Test
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
				PatternSet patterns = PatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers);
				boolean valid = false;
				while (!valid) {
					valid = true;
					try {
						patterns = PatternSet.randomTightPatternSet(numPatterns, 30, 1, 1000, 3, random, integers);
					} catch (IllegalArgumentException e) {
						badSetCounter++;
						if (1.0 * badSetCounter / 100 == Math.floor(1.0 * badSetCounter / 100))
							System.out.println(badSetCounter);
						valid = false;
					}
				}
				System.out.println(patterns);
				*/
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
				
				// Prim
				System.gc();
				startTime = System.nanoTime();
				prim = new PrimHeuristic(patterns);
				prim.feasibilityType = PrimHeuristic.zeroFeasible;
				int primSolution = prim.runModifiedAllStart();
				prim = null;
				System.gc();
				endTime = System.nanoTime();
				primTemplateSum += primSolution;
				long primTime = endTime - startTime;
				primTimeSum += primTime;
				System.out.println("Modified Prim (ZeroFeasibility) (All Start) fitted " + primSolution + " patterns in " + primTime + " nanoseconds.");
				*/
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
				cycleSolution = cycle.runIterativeHeuristicLargestStart().length;
				cycle = null;
				System.gc();
				endTime = System.nanoTime();
				primTemplateSum += cycleSolution;
				cycleTime = endTime - startTime;
				primTimeSum += cycleTime;
				System.out.println("fifoShortestPath (Reverse Sorted Addition) (Largest Start) fitted " + cycleSolution + " patterns in " + cycleTime + " nanoseconds.");
				
				
				System.out.println("=========================================================================================================");
				
			}
			//System.out.println("Kruskal fit an average of " + (kruskalTemplateSum / numTests) + " in an average of " + (kruskalTimeSum / (1000000000l * numTests)) + " seconds.");
			System.out.println("Cycle fit an average of " + (cycleTemplateSum / numTests) + " in an average of " + (cycleTimeSum / (1000000000l * numTests)) + " seconds.");
			System.out.println("Cycle2 fit an average of " + (primTemplateSum / numTests) + " in an average of " + (primTimeSum / (1000000000l * numTests)) + " seconds.");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
		*/
		
		/*
		
		// Min Templates Test - Tight
		System.out.println("Solution Key: <NumPatterns:Set#:Heuristic:NumOfTemplates:RelativeDifferenceInLowerBound:Templates{0,0,0}:Time(nanosec)>");
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
		double twoVIPLowerBoundDifferenceSum = 0d;
		int twoVIPTemplateSum = 0;
		long twoVIPTimeSum = 0l;
		double iterativePrimLowerBoundDifferenceSum = 0d;
		for (int numPatterns = 25; numPatterns <= 250; numPatterns += 25) {
			System.out.println("Sets of " + numPatterns + " patterns:");
			int numTests = 10;
			//int numPatterns = 250;
			MersenneTwisterFast random = new MersenneTwisterFast(56724969353l*(long)Math.floor(numPatterns / 25));
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
			twoVIPTemplateSum = 0;
			twoVIPTimeSum = 0l;
			twoVIPLowerBoundDifferenceSum = 0d;
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
				
				// Iterative Prim
				System.gc();
				startTime = System.nanoTime();
				prim = new PrimHeuristic(patterns);
				prim.feasibilityType = PrimHeuristic.contiguousOrdering;
				int[][] iterativePrimSolution = prim.runIterativeModifiedHeuristic();
				prim = null;
				System.gc();
				endTime = System.nanoTime();
				iterativePrimTemplateSum += iterativePrimSolution.length;
				long iterativePrimTime = endTime - startTime;
				iterativePrimTimeSum += iterativePrimTime;
				double iterativePrimLowerBoundDifference = (0d + iterativePrimSolution.length - lowerBound) / lowerBound;
				iterativePrimLowerBoundDifferenceSum += iterativePrimLowerBoundDifference;
				System.out.print("<" + numPatterns + ":" + i + ":Iterative Prim:" + iterativePrimSolution.length + ":" + iterativePrimLowerBoundDifference +  ":{" + iterativePrimSolution[0].length);
				for (int j = 1; j < iterativePrimSolution.length; j++)
					System.out.print("," + iterativePrimSolution[j].length);
				System.out.println("}:" + iterativePrimTime + ">");
//				System.out.println("Iterative Prim (Contiguous Ordering) fitted " + numPatterns + " on " + iterativePrimSolution.length + " templates in " + iterativePrimTime + " nanoseconds.");				
				
				// Bin Packing - Modified Prim
				System.gc();
				startTime = System.nanoTime();
				binPacker = new BinPackingHeuristic(patterns);
				binPacker.binType = BinPackingHeuristic.prim;
				binPacker.setComplexity(3);
				binPacker.runFirstFitHeuristic();
				int[][] primSolution = binPacker.outputSolutionIndices();
				binPacker = null;
				System.gc();
				endTime = System.nanoTime();
				primTemplateSum += primSolution.length;
				long primTime = endTime - startTime;
				primTimeSum += primTime;
				double primLowerBoundDifference = (0d + primSolution.length - lowerBound) / lowerBound;
				primLowerBoundDifferenceSum += primLowerBoundDifference;
				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (Prim) (Level 3):" + primSolution.length + ":" + primLowerBoundDifference +  ":{" + primSolution[0].length);
				for (int j = 1; j < primSolution.length; j++)
					System.out.print("," + primSolution[j].length);
				System.out.println("}:" + primTime + ">");
//				System.out.println("Bin Packer (Prim) (Level 3) fitted " + numPatterns + " on " + primSolution.length + " templates in " + primTime + " nanoseconds.");				
				
				// Bin Packing - fifoShortestPath
				System.gc();
				startTime = System.nanoTime();
				binPacker = new BinPackingHeuristic(patterns);
				binPacker.binType = BinPackingHeuristic.fifoShortestPath;
				binPacker.setComplexity(0);
				binPacker.runFirstFitHeuristic();
				int[][] fifoSolution = binPacker.outputSolutionIndices();
				//System.out.println(binPacker.printSolution());
				binPacker = null;
				System.gc();
				endTime = System.nanoTime();
				fifoTemplateSum += fifoSolution.length;
				long fifoTime = endTime - startTime;
				fifoTimeSum += fifoTime;
				double fifoLowerBoundDifference = (0d + fifoSolution.length - lowerBound) / lowerBound;
				fifoLowerBoundDifferenceSum += fifoLowerBoundDifference;
				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (Fifo) (Level 0):" + fifoSolution.length + ":" + fifoLowerBoundDifference +  ":{" + fifoSolution[0].length);
				for (int j = 1; j < fifoSolution.length; j++)
					System.out.print("," + fifoSolution[j].length);
				System.out.println("}:" + fifoTime + ">");
//				System.out.println("Bin Packer (fifo) (Level 0) fitted " + numPatterns + " on " + fifoSolution.length + " templates in " + fifoTime + " nanoseconds.");				
				
				// Bin Packing - Floyd
				System.gc();
				startTime = System.nanoTime();
				binPacker = new BinPackingHeuristic(patterns);
				binPacker.binType = BinPackingHeuristic.floydWarshall;
				binPacker.setComplexity(0);
				binPacker.runFirstFitHeuristic();
				int[][] floydSolution = binPacker.outputSolutionIndices();
				binPacker = null;
				System.gc();
				endTime = System.nanoTime();
				floydTemplateSum += floydSolution.length;
				long floydTime = endTime - startTime;
				floydTimeSum += floydTime;
				double floydLowerBoundDifference = (0d + floydSolution.length - lowerBound) / lowerBound;
				floydLowerBoundDifferenceSum += floydLowerBoundDifference;
				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (Floyd) (Level 0):" + floydSolution.length + ":" + floydLowerBoundDifference +  ":{" + floydSolution[0].length);
				for (int j = 1; j < floydSolution.length; j++)
					System.out.print("," + floydSolution[j].length);
				System.out.println("}:" + floydTime + ">");
//				System.out.println("Bin Packer (Floyd) (Level 0) fitted " + numPatterns + " on " + floydSolution.length + " templates in " + floydTime + " nanoseconds.");				
				
				// Bin Packing - TwoVIP
				System.gc();
				startTime = System.nanoTime();
				binPacker = new BinPackingHeuristic(patterns);
				binPacker.binType = BinPackingHeuristic.twoVIP;
				binPacker.setComplexity(0);
				binPacker.setVerbosity(true);
				binPacker.runFirstFitHeuristic();
				int[][] twoVIPSolution = binPacker.outputSolutionIndices();
				if (!binPacker.checkSolution())
					throw new RuntimeException("TwoVIP Screwed Up!!!!!");
				binPacker = null;
				System.gc();
				endTime = System.nanoTime();
				twoVIPTemplateSum += twoVIPSolution.length;
				long twoVIPTime = endTime - startTime;
				twoVIPTimeSum += twoVIPTime;
				double twoVIPLowerBoundDifference = (0d + twoVIPSolution.length - lowerBound) / lowerBound;
				twoVIPLowerBoundDifferenceSum += twoVIPLowerBoundDifference;
				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (TwoVIP) (Level 0):" + twoVIPSolution.length + ":" + twoVIPLowerBoundDifference +  ":{" + twoVIPSolution[0].length);
				for (int j = 1; j < twoVIPSolution.length; j++)
					System.out.print("," + twoVIPSolution[j].length);
				System.out.println("}:" + twoVIPTime + ">");
//				System.out.println("Bin Packer (TwoVIP) (Level 0) fitted " + numPatterns + " on " + twoVIPSolution.length + " templates in " + twoVIPTime + " nanoseconds.");				
				
				
				// Bin Packing - EmptySpace
				System.gc();
				startTime = System.nanoTime();
				binPacker = new BinPackingHeuristic(patterns);
				binPacker.binType = BinPackingHeuristic.emptySpace;
				binPacker.setComplexity(0);
				binPacker.runFirstFitHeuristic();
				int[][] esSolution = binPacker.outputSolutionIndices();
				binPacker = null;
				System.gc();
				endTime = System.nanoTime();
				esTemplateSum += esSolution.length;
				long esTime = endTime - startTime;
				esTimeSum += esTime;
				double esLowerBoundDifference = (0d + esSolution.length - lowerBound) / lowerBound;
				esLowerBoundDifferenceSum += esLowerBoundDifference;
				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (Empty Space) (Level 0):" + esSolution.length + ":" + esLowerBoundDifference +  ":{" + esSolution[0].length);
				for (int j = 1; j < esSolution.length; j++)
					System.out.print("," + esSolution[j].length);
				System.out.println("}:" + esTime + ">");
//				System.out.println("Bin Packer (Empty Space) (Level 0) fitted " + numPatterns + " on " + esSolution.length + " templates in " + esTime + " nanoseconds.");				
				
				
				System.out.println("=========================================================================================================");
				
			}
			System.out.println("Kruskal fit " + numPatterns + " patterns on an average of " + (1d * kruskalTemplateSum / numTests) + " templates in an average of " + (kruskalTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (kruskalLowerBoundDifferenceSum * 100 / numTests) + "%.");
			System.out.println("Iterative Prim fit " + numPatterns + " patterns on an average of " + (1d * iterativePrimTemplateSum / numTests) + " templates in an average of " + (iterativePrimTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (iterativePrimLowerBoundDifferenceSum * 100 / numTests) + "%.");
			System.out.println("Bin Packer (Prim) (Level 3) fit " + numPatterns + " patterns on an average of " + (1d * primTemplateSum / numTests) + " templates in an average of " + (primTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (primLowerBoundDifferenceSum * 100 / numTests) + "%.");
			System.out.println("Bin Packer (fifo) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * fifoTemplateSum / numTests) + " templates in an average of " + (fifoTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (fifoLowerBoundDifferenceSum * 100 / numTests) + "%.");
			System.out.println("Bin Packer (Floyd) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * floydTemplateSum / numTests) + " templates in an average of " + (floydTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (floydLowerBoundDifferenceSum * 100 / numTests) + "%.");
			System.out.println("Bin Packer (Empty Space) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * esTemplateSum / numTests) + " templates in an average of " + (esTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (esLowerBoundDifferenceSum * 100 / numTests) + "%.");
			System.out.println("Bin Packer (TwoVIP) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * twoVIPTemplateSum / numTests) + " templates in an average of " + (twoVIPTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (twoVIPLowerBoundDifferenceSum * 100 / numTests) + "%.");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
		*/
		/*
		// Min Templates Test - Polar
				System.out.println("Solution Key: <NumPatterns:Set#:Heuristic:NumOfTemplates:Templates{0,0,0}:Time(nanosec)>");
				int fifoTemplateSum = 0;
				long fifoTimeSum = 0l;
				int floydTemplateSum = 0;
				long floydTimeSum = 0l;
				for (int numPatterns = 25; numPatterns <= 250; numPatterns += 25) {
					System.out.println("Sets of " + numPatterns + " patterns:");
					int numTests = 10;
					//int numPatterns = 250;
					MersenneTwisterFast random = new MersenneTwisterFast(5780593202071l*(long)Math.floor(numPatterns / 25));
					//MersenneTwisterFast startRandomizer = new MersenneTwisterFast(5752846548926l);
					//CycleHeuristic heuristic;
					BinPackingHeuristic binPacker;
					fifoTemplateSum = 0;
					fifoTimeSum = 0l;
					floydTemplateSum = 0;
					floydTimeSum = 0l;
					long startTime;
					long endTime;
					for (int i = 0; i < numTests; i++) {
						// Create Set
//						System.out.println("Set #" + i);
						
						
//						System.out.println(patterns);
						
						PolarPatternSet patterns = PolarPatternSet.randomPatternSet(numPatterns, 200, 300, 5, 15, 10, 30, random);
						
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
//						System.out.println("Bin Packer (fifo) (Level 0) fitted " + numPatterns + " on " + fifoSolution.length + " templates in " + fifoTime + " nanoseconds.");				
						
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
//						System.out.println("Bin Packer (Floyd) (Level 0) fitted " + numPatterns + " on " + floydSolution.length + " templates in " + floydTime + " nanoseconds.");				
						
						
						
						System.out.println("=========================================================================================================");
						
					}
					System.out.println("Bin Packer (fifo) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * fifoTemplateSum / numTests) + " templates in an average of " + (fifoTimeSum / (1000000000l * numTests)) + " seconds.");
					System.out.println("Bin Packer (Floyd) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * floydTemplateSum / numTests) + " templates in an average of " + (floydTimeSum / (1000000000l * numTests)) + " seconds.");
					System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				}
		*/
		
		// Min Templates Test - Korst
		/*
		System.out.println("Solution Key: <NumPatterns:Set#:Heuristic:NumOfTemplates:RelativeDifferenceInLowerBound:Templates{0,0,0}:Time(nanosec)>");
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
			MersenneTwisterFast random = new MersenneTwisterFast(7482967460764l*(long)Math.floor(numPatterns / 10));
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
				LinearPatternSet patterns = LinearPatternSet.randomKorstPatternSet(numPatterns, 30, random);
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
				
				// Bin Packing - Floyd
				System.gc();
				startTime = System.nanoTime();
				ArrayList<int[]> floydSolution = new ArrayList<int[]>();
				int floydSolutionSize = 0;
				for (PatternSet p : partitionedSets) {
					binPacker = new BinPackingHeuristic(p);
					binPacker.binType = BinPackingHeuristic.floydWarshall;
					binPacker.setComplexity(0);
					binPacker.runFirstFitHeuristic();
					int[][] currentSolution = binPacker.outputSolutionIndices();
					floydSolutionSize += currentSolution.length;
					floydSolution.addAll(Arrays.asList(currentSolution));
					binPacker = null;
				}
				System.gc();
				endTime = System.nanoTime();
				floydTemplateSum += floydSolutionSize;
				long floydTime = endTime - startTime;
				floydTimeSum += floydTime;
				double floydLowerBoundDifference = (0d + floydSolutionSize - lowerBound) / lowerBound;
				floydLowerBoundDifferenceSum += floydLowerBoundDifference;
//				System.out.print("<" + numPatterns + ":" + i + ":Bin Packer (Floyd) (Level 0):" + floydSolutionSize + ":" + floydLowerBoundDifference +  ":{" + floydSolution.get(0).length);
//				for (int j = 1; j < floydSolution.size(); j++)
//					System.out.print("," + floydSolution.get(j).length);
//				System.out.println("}:" + floydTime + ">");
//				System.out.println("Bin Packer (Floyd) (Level 0) fitted " + numPatterns + " on " + floydSolutionSize + " templates in " + floydTime + " nanoseconds.");				
				
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
			System.out.println("Bin Packer (Floyd) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * floydTemplateSum / numTests) + " templates in an average of " + (floydTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (floydLowerBoundDifferenceSum * 100 / numTests) + "%.");
			System.out.println("Bin Packer (Empty Space) (Level 0) fit " + numPatterns + " patterns on an average of " + (1d * esTemplateSum / numTests) + " templates in an average of " + (esTimeSum / (1000000000l * numTests)) + " seconds. It exceeded the lower bound by an average of " + (esLowerBoundDifferenceSum * 100 / numTests) + "%.");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
		*/
	}
	
	/**
	 * This class contains two Pattern indices and their B'_{ij} value.
	 * It exists solely for use in the java.util.PriorityQueue class and
	 * as such implements the java.lang.Comparable interface.
	 *
	 * @author Brad Paynter
	 * @version Jun 19, 2010
	 *
	 */
	private class SortablePattern implements Comparable<SortablePattern> {
		/**
		 * The first pattern in the pair (i)
		 */
		public int pattern;
		
		/**
		 * The value of B'_{ij}
		 */
		public double BBij;
		
		/**
		 * Constructs a PatternPair containing P_i and P_j.
		 * It looks up and stores the value of B'_{ij} from the
		 * PatternSet object
		 *  
		 * @param i The first pattern in this pair
		 * @param j The second pattern in this pair
		 */
		public SortablePattern(int i, double BBij) {
			pattern = i;
			this.BBij = BBij;
		}
		
		/**
		 * Compares two PatternPair objects according to their B'_{ij} values
		 * 
		 * @param o The PatternPair to compare to this one
		 * @return Returns -1,0,1 if this PatternPair is less than, equal to or 
		 * 			greater than the given PatternPair respectively.
		 */
		public int compareTo(SortablePattern o) {
			if (this.pattern == o.pattern)
				return 0;
			else if (this.BBij < o.BBij)
				return -1;
			else
				return 1;
		}
	}

	
}
