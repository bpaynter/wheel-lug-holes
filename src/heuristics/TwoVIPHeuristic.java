/**
 * TwoVIPHeuristic.java
 * Nov 24, 2011
 */
package heuristics;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import networks.FloydWarshall;
import problem.PSPatternSet;
import util.TwoVIPAlgorithm;
import util.TwoVIPConstraint;
import util.TwoVIPInstance;
import exact.FitNetwork;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Nov 24, 2011
 *
 */
public class TwoVIPHeuristic implements Bin {
	private PSPatternSet patterns;
	private LinkedList<LinkedList<Long>> k;
	private long[][] ub;
	private long[][] lb;
	private LinkedList<Integer> currentTemplate;
	
	private int complexityLevel;
	
	public static final int NoSortedAddition_NoAllPosition = 0;
	public static final int SortedAddition_NoAllPosition = 1;
	public static final int NoSortedAddition_AllPosition = 2;
	public static final int SortedAddition_AllPosition = 3;
	
	public boolean sortedAddition;
	
	public boolean verbose;
	
	public TwoVIPHeuristic(PSPatternSet patterns) {
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
		sortedAddition = false;
		complexityLevel = 0;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @param startPattern
	 */
	public TwoVIPHeuristic(PSPatternSet patterns, int startPattern) {
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
		for (int i = 0; i < patterns.numPatterns(); i++) {
			givenStart(i);
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
			newKs = tryPattern(candidatePattern);
			if (newKs != null) {
				LinkedList<Long> newRow = new LinkedList<Long>();
				for (int i = 0; i < newKs.length; i++) {
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
						if (currentPattern.BBij < patterns.getBB(candidatePattern, currentPattern.pattern))
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
	private long[] tryPattern(int newPattern) {
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
		TwoVIP twoVIP = new TwoVIP(patterns, currentTemplate, k, newPattern);
		return TwoVIPAlgorithm.feasibility(twoVIP);
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
		newKs = tryPattern(item);
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
		newKs = tryPattern(item);
		if (newKs != null) {
			LinkedList<Long> newRow = new LinkedList<Long>();
			for (int i = 0; i < newKs.length; i++) {
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
		TwoVIPHeuristic newBin = new TwoVIPHeuristic(patterns);
		newBin.givenStart(item);
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
			PSPatternSet solution = patterns.subset(template);
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
			PSPatternSet solution = patterns.subset(template);
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
			else if (this.BBij > o.BBij)
				return -1;
			else
				return 1;
		}
	}	
	
	private class TwoVIP implements TwoVIPInstance {
		private PSPatternSet patterns;
		private LinkedList<Integer> currentTemplate;
		private int newPattern;
		private double[][] d;
		
		public TwoVIP(PSPatternSet patterns, LinkedList<Integer> currentTemplate, LinkedList<LinkedList<Long>> ks, int newPattern) {
			this.patterns = patterns;
			this.currentTemplate = currentTemplate;
			this.newPattern = newPattern;
			FloydWarshall floyd = new FloydWarshall(currentTemplate.size());
			double[][] w = new double[currentTemplate.size()][currentTemplate.size()];
			for (int i = 0; i < currentTemplate.size(); i++) {
				w[i][i] = 0;
				for (int j = i+1; j < currentTemplate.size(); j++) {
					w[i][j] = (ks.get(i).get(j) + 1)*patterns.getB(currentTemplate.get(i), currentTemplate.get(j)) - patterns.getD(currentTemplate.get(i), currentTemplate.get(j));
					w[j][i] = -ks.get(i).get(j)*patterns.getB(currentTemplate.get(j), currentTemplate.get(i)) - patterns.getD(currentTemplate.get(j), currentTemplate.get(i));
				}
			}
			d = floyd.runFloydWarshall(w);
		}

		/**
		 * TODO
		 * 
		 * @return
		 */
		public int numVariables() {
			return currentTemplate.size();
		}

		/**
		 * TODO
		 * 
		 * @return
		 */
		public int numConstraints() {
			return currentTemplate.size() * (currentTemplate.size() - 1);
		}

		/**
		 * TODO
		 * 
		 * @param i
		 * @return
		 * @throws IllegalArgumentException
		 */
		public long lowerBound(int i) throws IllegalArgumentException {
			return 0;
		}

		/**
		 * TODO
		 * 
		 * @param i
		 * @return
		 * @throws IllegalArgumentException
		 */
		public long upperBound(int i) throws IllegalArgumentException {
			return patterns.getKUpperBound(currentTemplate.get(i), newPattern) - patterns.getKLowerBound(currentTemplate.get(i), newPattern);
		}

		/**
		 * TODO
		 * 
		 * @param i
		 * @return
		 * @throws IllegalArgumentException
		 */
		public long weight(int i) throws IllegalArgumentException {
			return 0;
		}

		/**
		 * TODO
		 * 
		 * @param k
		 * @return
		 */
		public TwoVIPConstraint getConstraint(int k) {
			int i = (int)Math.floor(k / (currentTemplate.size() - 1));
			int j = (k % (currentTemplate.size() - 1));
			if (j >= i)
				j++;
			long a = patterns.gcd(currentTemplate.get(i), newPattern);
			a *= patterns.getPattern(currentTemplate.get(j)).numberOfHoles();
			long b = patterns.gcd(currentTemplate.get(j), newPattern);
			b *= -patterns.getPattern(currentTemplate.get(i)).numberOfHoles();
			double c = patterns.getD(currentTemplate.get(i), currentTemplate.get(j)) + patterns.getD(currentTemplate.get(j), currentTemplate.get(i)) - d[i][j];
			c /= patterns.getB(currentTemplate.get(i), newPattern);
			c -= 1;
			c *= a;
			long cBar = (long)Math.ceil(c);
			return new TwoVIPConstraint(i, j, a, b, cBar);
		}
		
	}

}
