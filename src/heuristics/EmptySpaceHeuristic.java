/**
 * EmptySpaceHeuristic.java
 * Jul 10, 2011
 */
package heuristics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeSet;

import exact.FeasibilityTest;

import problem.*;
import util.IntegerUtils;
import util.MersenneTwisterFast;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Jul 10, 2011
 *
 */
public class EmptySpaceHeuristic implements Bin {
	
	private PatternSet patterns;
	private TreeSet<Pattern> emptySpace;
	private ArrayList<Integer> currentTemplate;
	public boolean sortedAddition;
	private int complexityLevel;
	public boolean verbose;
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @param startPattern
	 */
	public EmptySpaceHeuristic(PatternSet patterns, int startPattern) {
		this.patterns = patterns;
		emptySpace = new TreeSet<Pattern>(new PatternComparator());
		sortedAddition = false;
		complexityLevel = 0;
		verbose = false;
		setStart(startPattern);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 */
	public EmptySpaceHeuristic(PatternSet patterns) {
		this(patterns, 0);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[] runHeuristic() {
		Queue<Integer> remainingPatterns;
		if (sortedAddition)
			remainingPatterns = new PriorityQueue<Integer>(patterns.numPatterns(), new PatternSorter(patterns));
		else
			remainingPatterns = new LinkedList<Integer>();
		for (int i = 0; i < patterns.numPatterns(); i++)
			remainingPatterns.add(i);
		for (Integer i : currentTemplate)
			remainingPatterns.remove(i);
		return runHeuristic(remainingPatterns);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param startingPattern
	 * @throws IllegalArgumentException
	 */
	public void setStart(int startingPattern) throws IllegalArgumentException {
		Pattern candidatePattern = patterns.getPattern(startingPattern);
		candidatePattern.setStartingPosition(0.0d);
		currentTemplate = new ArrayList<Integer>();
		currentTemplate.add(startingPattern);
		emptySpace = new TreeSet<Pattern>(new PatternComparator());
		emptySpace.add(candidatePattern.inversePattern());
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param remainingPatterns
	 * @return
	 */
	public int[] runHeuristic(Queue<Integer> remainingPatterns) {
		while (!remainingPatterns.isEmpty()) {
			int candidatePatternIndex = remainingPatterns.poll();
			Pattern candidatePattern = patterns.getPattern(candidatePatternIndex);
			if (verbose)
				System.out.println("Trying pattern " + candidatePattern);
			Pattern space = findSpace(candidatePattern);
			if (space != null) {
				insert(candidatePattern, space);
				currentTemplate.add(candidatePatternIndex);
			}
		}
		return outputSolutionIndices();
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[] outputSolutionIndices() {
		int[] outTemplate = new int[currentTemplate.size()];
		Iterator<Integer> templateIterator = currentTemplate.iterator();
		for (int i = 0; i < outTemplate.length; i++)
			outTemplate[i] = templateIterator.next();
		return outTemplate;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public boolean checkSolution() {
		PatternSet template = patterns.subset(outputSolutionIndices());
		return FeasibilityTest.testStartingPositions(template);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param v
	 */
	public void setVerbosity(boolean v) {
		verbose = v;
	}
	
	public int runMinTemplatesHeuristic() {
		Queue<Integer> remainingPatterns;
		if (sortedAddition)
			remainingPatterns = new PriorityQueue<Integer>(patterns.numPatterns(), new PatternSorter(patterns));
		else
			remainingPatterns = new LinkedList<Integer>();
		for (int i = 0; i < patterns.numPatterns(); i++)
			remainingPatterns.add(i);
		Pattern blank = null;
		if (patterns.getPattern(0) instanceof LinearPattern)
			blank = new LinearPattern(10, 10, 0d);
		else if (patterns.getPattern(0) instanceof CircularPattern) {
			CircularPattern pattern = (CircularPattern)patterns.getPattern(0);
			blank = new CircularPattern(pattern.outerCircumference, 1, pattern.outerCircumference, 0d);
		} else
			throw new RuntimeException("Unexpected Pattern type.");
		emptySpace.clear();
		emptySpace.add(blank);
		int numTemplates = 1;
		while (!remainingPatterns.isEmpty()) {
			
		}
		return -1;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param pattern
	 * @return
	 */
	private Pattern findSpace(Pattern pattern) {
		Iterator<Pattern> patternIterator = emptySpace.iterator();
		while (patternIterator.hasNext()) {
			Pattern space = patternIterator.next();
			if (verbose)
				System.out.println("Testing Space: " + space);
			if (space.getPeriod() == space.getInnerDiameter())
				return space;
			double lhs = pattern.getInnerDiameter() + space.getPeriod() - space.getInnerDiameter();
			double rhs = pattern.B(space);
			if (verbose)
				System.out.println(lhs + "<= " + rhs);
			if (lhs <= rhs) {
				try {
					patternIterator.remove();
				} catch (UnsupportedOperationException e) {
					System.err.println(e);
					patternIterator = null;
					emptySpace.remove(space);
				}
				if (verbose)
					System.out.println(pattern + " fits in " + space);
				return space;
			}
			
		}
		return null;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param pattern
	 * @param space
	 */
	private void insert(Pattern pattern, Pattern space) {
		pattern.setStartingPosition(space.getStartingPosition());
		double Bij = pattern.B(space);
		double start = space.getStartingPosition();
		if (verbose)
			System.out.println("Bij = " + Bij);
		/*
		if (Bij == space.getPeriod()) {
			if (Bij == pattern.getPeriod())
				emptySpace.add(space.companionPattern(space.getInnerDiameter() - pattern.getInnerDiameter(), start + pattern.getInnerDiameter()));
			else {
				for (int i = 0; i < pattern.Nij(space) - 1; i++)
					emptySpace.add(pattern.companionPattern(space.getInnerDiameter(), ((i + 1) * Bij) + start));
				emptySpace.add(pattern.companionPattern(space.getInnerDiameter() - pattern.getInnerDiameter(), start + pattern.getInnerDiameter()));
			}
		} else {
			if (Bij == pattern.getPeriod()) {
				for (int i = 0; i < space.Nij(pattern); i++)
					emptySpace.add(space.companionPattern(pattern.getPeriod() - pattern.getInnerDiameter(), i * Bij + start + pattern.getInnerDiameter()));
				emptySpace.add(space.companionPattern(pattern.getPeriod() + space.getInnerDiameter() - space.getPeriod() - pattern.getInnerDiameter(), space.Nij(pattern) * Bij + start + pattern.getInnerDiameter()));
			} else {
				for (int i = 0; i < pattern.Nij(space) - 1; i++)
					emptySpace.add(pattern.companionPattern(pattern.getInnerDiameter(), (i + 1) * Bij + start));
				for (int i = 0; i < space.Nij(pattern) - 1; i++)
					emptySpace.add(space.companionPattern(space.getPeriod() - space.getInnerDiameter(), (i+1) * Bij + start - space.getPeriod() + space.getInnerDiameter()));
				emptySpace.add(pattern.lcmPattern(space, Bij + space.getInnerDiameter()- space.getPeriod() - pattern.getInnerDiameter(), start + pattern.getInnerDiameter()));
			}
		}
		*/
		/*
		for (int i = 0; i < space.Nij(pattern) - 1; i++)
			emptySpace.add(pattern.companionPattern(pattern.getInnerDiameter(), ((i + 1) * Bij) + start));
		for (int i = 0; i < pattern.Nij(space) - 1; i++)
			emptySpace.add(space.companionPattern(space.getPeriod() - space.getInnerDiameter(), ((i + 1) * Bij) + start - space.getPeriod() + space.getInnerDiameter()));
		double usedLength = space.getPeriod() - space.getInnerDiameter() + pattern.getInnerDiameter();
		emptySpace.add(pattern.lcmPattern(space, Bij - usedLength, start + pattern.getInnerDiameter()));
		*/
		for (int i = 0; i < pattern.Nij(space) - 1; i++)
			emptySpace.add(pattern.companionPattern(pattern.getInnerDiameter(), (i + 1) * Bij + start));
		for (int i = 0; i < space.Nij(pattern) - 1; i++)
			emptySpace.add(space.companionPattern(space.getPeriod() - space.getInnerDiameter(), (i+1) * Bij + start - space.getPeriod() + space.getInnerDiameter()));
		emptySpace.add(pattern.lcmPattern(space, Bij + space.getInnerDiameter()- space.getPeriod() - pattern.getInnerDiameter(), start + pattern.getInnerDiameter()));
		
		if (verbose) {
			System.out.println("Empty Space Patterns:");
			for (Pattern p : emptySpace)
				System.out.println(p);
		}
	}
	
	// The following methods implement the Bin interface
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public Iterator<Integer> iterator() {
		return currentTemplate.iterator();
	}

	/**
	 * TODO
	 * 
	 * @param item
	 * @return
	 */
	public boolean fit(int item) throws IllegalArgumentException {
		if ((item < 0) || (item >= patterns.numPatterns()))
			throw new IllegalArgumentException("Index out of range: " + item);
		Pattern space = findSpace(patterns.getPattern(item));
		if (space == null) 
			return false;
		else {
			emptySpace.add(space);
			return true;
		}
	}

	/**
	 * TODO
	 * 
	 * @param item
	 * @return
	 */
	public boolean insert(int item) throws IllegalArgumentException {
		if ((item < 0) || (item >= patterns.numPatterns()))
			throw new IllegalArgumentException("Index out of range: " + item);
		Pattern candidatePattern = patterns.getPattern(item);
		Pattern space = findSpace(candidatePattern);
		if (space != null) {
			insert(candidatePattern, space);
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
		throw new UnsupportedOperationException("This method is not yet implemented.");
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public int[] outputIndices() {
		return outputSolutionIndices();
	}

	/**
	 * TODO
	 * 
	 * @param level
	 * @throws IllegalArgumentException
	 */
	public void setLevel(int level) throws IllegalArgumentException {
		sortedAddition = (level > 1);
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public int numLevels() {
		return 4;
	}

	/**
	 * TODO
	 * 
	 * @param item
	 * @return
	 */
	public Bin newBin(int item) {
		EmptySpaceHeuristic newBin = new EmptySpaceHeuristic(patterns, item);
		newBin.setLevel(this.complexityLevel);
		return newBin;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public String printSolution() {
		String endln = System.getProperty("line.separator");
		String s = "";
		for (Integer p : currentTemplate)
			s += patterns.getPattern(p) + endln;
		return s;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public int binSize() {
		return currentTemplate.size();
	}
	
	/**
	 * This main method is only used for testing this class 
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		int numPatterns = 100;
		int numTests = 100;
		int templateSum = 0;
		int successes = 0;
		MersenneTwisterFast random = new MersenneTwisterFast(5859468024l);
		EmptySpaceHeuristic heuristic;
		IntegerUtils integers = new IntegerUtils(1000);
		int badSetCounter = 0;
		for (int i = 0; i < numTests; i++) {
			/*
			PatternSet patterns = PatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers);
			boolean valid = false;
			while (!valid) {
				valid = true;
				try {
					patterns = PatternSet.randomTightPatternSet(numPatterns, 30, 1, 100, 3, random, integers);
				} catch (IllegalArgumentException e) {
					badSetCounter++;
					if (1.0 * badSetCounter / 100 == Math.floor(1.0 * badSetCounter / 100))
						System.out.println(badSetCounter);
					valid = false;
				}
			}
			*/
			//CircularPatternSet patterns = CircularPatternSet.randomKorstPatternSet(numPatterns, 20, random);
			CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(numPatterns, 100, 1, 1000, 3, random, integers);
			System.out.println("Set #" + i);
			//System.out.println("Lower bound on number of bins: " + patterns.densityBinLowerBound());
			heuristic = new EmptySpaceHeuristic(patterns);
			heuristic.sortedAddition = true;
			//heuristic.verbose = true;
			int[] template = heuristic.runHeuristic();
			if (template.length == numPatterns)
				successes++;
			templateSum += template.length;
			PatternSet templateSet = patterns.subset(template);
			if (!FeasibilityTest.testStartingPositions(templateSet)) {
				heuristic = new EmptySpaceHeuristic(patterns);
				heuristic.sortedAddition = true;
				heuristic.verbose = true;
				template = heuristic.runHeuristic();
				templateSet = patterns.subset(template);
				FeasibilityTest.testStartingPositions(templateSet, true);
				System.out.println("Solution:");
				System.out.println(templateSet);
				System.out.println("EmptySpaceHeuristic screwed up!!!!");
			}
			System.out.println("-----------------------------------------");
		}
		System.out.println("EmptySpaceHeuristic fit an average of " + (1d * templateSum / numTests) + " patterns on one template.");
		System.out.println("Successes: " + successes);
		System.out.println("Bad Sets: " + badSetCounter);
	}
	
	/**
	 * 
	 * TODO
	 *
	 * @author Brad Paynter
	 * @version Jul 10, 2011
	 *
	 */
	private class PatternComparator implements Comparator<Pattern> {

		/**
		 * TODO
		 * 
		 * @param arg0
		 * @param arg1
		 * @return
		 */
		public int compare(Pattern arg0, Pattern arg1) {
			if (arg0.getPeriod() == arg1.getPeriod()) {
				if (arg0.getInnerDiameter() == arg1.getInnerDiameter()) {
					return Double.compare(arg0.getStartingPosition(), arg1.getStartingPosition());
				} else
					return Double.compare(arg0.getInnerDiameter(), arg1.getInnerDiameter());
			} else
				if (arg0.getPeriod() < arg1.getPeriod())
					return 1;
				else
					return -1;
		}
		
	}
	
	/**
	 * 
	 * TODO
	 *
	 * @author Brad Paynter
	 * @version Jul 10, 2011
	 *
	 */
	private class PatternSorter implements Comparator<Integer> {
		private PatternSet patterns;
		
		public PatternSorter(PatternSet patterns) {
			this.patterns = patterns;
		}
		
		public int compare(Integer a, Integer b) {
			double densityA = patterns.getPattern(a).density();
			double densityB = patterns.getPattern(b).density();			
			if (densityA == densityB) {
				if (a.hashCode() != b.hashCode())
					return a.hashCode() - b.hashCode();
				else
					throw new RuntimeException("I hope this never happens.");
			} else 
				return -Double.compare(densityA, densityB);
		}
	}

	
}
