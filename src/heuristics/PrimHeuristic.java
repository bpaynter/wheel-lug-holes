/**
 * PrimHeuristic.java
 * Jun 10, 2010
 */
package heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TreeSet;

import exact.FeasibilityTest;
import exact.FitNetwork;

import problem.LinearPatternSet;
import problem.Pattern;
import problem.PatternSet;
import util.IntegerUtils;
import util.MersenneTwisterFast;

/**
 * This code is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * TODO
 *
 * @author Brad Paynter
 * @version Jul 2, 2010
 *
 */
public class PrimHeuristic implements Bin {
	/**
	 * TODO
	 */
	private static final boolean left = true;
	/**
	 * TODO
	 */
	private static final boolean right = false;
	/**
	 * Sets the verbosity of the heuristic. If set to <code>false</code>
	 * there will be no output.
	 */
	public boolean verbose;
	/**
	 * The leftmost element in the current solution
	 */
	private Element leftMost;
	/**
	 * The rightmost element in the current solution
	 */
	private Element rightMost;
	/**
	 * The patterns to be used by this heuristic
	 */
	private PatternSet patterns;
	
	public int feasibilityType;
	
	public static final int zeroFeasibleOrdering = 0;
	public static final int contiguousOrdering = 1;
	
	private int complexityLevel;
	
	public static final int zeroFeasibleOrdering_EndInsertion = 0;
	public static final int zeroFeasibleOrdering_AllPosition = 1;
	public static final int contiguousOrdering_EndInsertion = 2;
	public static final int contiguousOrdering_AllPosition = 3;
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @param startPattern
	 * @throws IllegalArgumentException
	 */
	public PrimHeuristic(PatternSet patterns, int startPattern) throws IllegalArgumentException {
		this(patterns);
		// Check if the startPattern index is valid (getPattern() will
		// throw an Exception if the index is invalid)
		patterns.getPattern(startPattern);
		leftMost = new Element(startPattern);
		rightMost = leftMost;
		setLevel(0);
	}
	
	/**
	 * Constructs a PrimHeuristic object to run on the given
	 * set of patterns
	 * 
	 * @param patterns The given patterns
	 */
	public PrimHeuristic(PatternSet patterns) {
		this.patterns = patterns;
		leftMost = null;
		rightMost = null;
		verbose = false;
		setLevel(0);
	}
	
	/**
	 * Sets the heuristic to start with a random pattern
	 * 
	 * @param random A random number generator
	 */
	public void randomStart(MersenneTwisterFast random) {
		leftMost = new Element(random.nextInt(patterns.numPatterns()));
		rightMost = leftMost;
	}
	
	/**
	 * Sets the heuristic to start with the pair of patterns with the
	 * smallest non-zero B'_{ij}
	 */
	public void smallestStart() {
		int pattern = 0;
		double minBB = Double.POSITIVE_INFINITY;
		for (int i = 0; i < patterns.numPatterns(); i++) {
			for (int j = i + 1; j < patterns.numPatterns(); j++)
				if ((patterns.getBB(i, j) < minBB) && (patterns.getBB(i,j) >= 0)) {
					minBB = patterns.getBB(i,j);
					pattern = i;
				}
		}
		leftMost = new Element(pattern);
		rightMost = leftMost;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param startPattern
	 * @throws IllegalArgumentException
	 */
	public void givenStart(int startPattern) throws IllegalArgumentException {
		if ((startPattern < 0) || (startPattern >= patterns.numPatterns()))
			throw new IllegalArgumentException("startPattern is out of range: " + startPattern);
		leftMost = new Element(startPattern);
		rightMost = leftMost;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[][] runIterativeBasicHeuristic() {
		ArrayList<int[]> solution = new ArrayList<int[]>();
		HashSet<Integer> remainingPatterns = new HashSet<Integer>();
		for (int p = 0; p < patterns.numPatterns(); p++)
			remainingPatterns.add(p);
		while (!remainingPatterns.isEmpty()) {
			int[] remainingIndices = new int[remainingPatterns.size()];
			Iterator<Integer> iterator = remainingPatterns.iterator();
			for (int i = 0; i < remainingPatterns.size(); i++)
				remainingIndices[i] = iterator.next();
			PrimHeuristic heuristic = new PrimHeuristic(patterns.subset(remainingIndices));
			heuristic.smallestStart();
			heuristic.runBasicHeuristic();
			int[] template = heuristic.outputSolutionIndices();
			for (Integer p : template)
				remainingPatterns.remove(p);
			solution.add(template);
		}
		return solution.toArray(new int[0][]);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[][] runIterativeModifiedHeuristic() {
		ArrayList<int[]> solution = new ArrayList<int[]>();
		boolean[] remaining = new boolean[patterns.numPatterns()];
		Arrays.fill(remaining, true);
		int numRemainingPatterns = patterns.numPatterns();
		while (numRemainingPatterns > 0) {
			int[] patternIndices = new int[numRemainingPatterns];
			int currentPatternIndex = 0;
			for (int p = 0; p < patterns.numPatterns(); p++) {
				if (remaining[p]) {
					patternIndices[currentPatternIndex] = p;
					currentPatternIndex++;
				}
			}
			PatternSet currentSet = patterns.subset(patternIndices);
			PrimHeuristic heuristic = new PrimHeuristic(currentSet);
			heuristic.runModifiedAllStart();
			//System.out.println(heuristic.printSolution());
			int[] template = heuristic.outputSolutionIndices();
			int[] outTemplate = new int[template.length];
			//System.out.println(template);
			for (int p = 0; p < template.length; p++) {
				remaining[patternIndices[template[p]]] = false;
				numRemainingPatterns--;
				outTemplate[p] = patternIndices[template[p]];
				//System.out.println("Removing pattern " + p + ": " + currentSet.getPattern(p));
			}
			solution.add(outTemplate);
		}
		return solution.toArray(new int[1][]);
	}
	
	/**
	 * 
	 * TODO
	 *
	 */
	public void runBasicHeuristic() {
		boolean done = false;
		while (!done) {
			done = true;
			TreeSet<Integer> tBar = new TreeSet<Integer>();
			for (int i = 0; i < patterns.numPatterns(); i++)
				tBar.add(i);
			if (leftMost == null) {
				leftMost = new Element(0);
				rightMost = leftMost;
			}
			Element currentElement = leftMost;
			while (currentElement != null) {
				tBar.remove(currentElement.index);
				currentElement = currentElement.right;
			}
			PriorityQueue<PatternPair> priorityQueue = new PriorityQueue<PatternPair>();
			while (!tBar.isEmpty()) {
				//System.out.println(this);
				//System.out.println("Filling Priority Queue.");
				for (int i : tBar) {
					if (patterns.getBB(i, leftMost.index) >= 0)
						priorityQueue.add(new PatternPair(leftMost, i));
					if (patterns.getBB(i, rightMost.index) >= 0)
						priorityQueue.add(new PatternPair(rightMost, i));
				}
				boolean completed = false;
				while (!priorityQueue.isEmpty() && !completed) {
					completed = false;
					PatternPair currentPair = priorityQueue.poll();
					tBar.remove(currentPair.patternTwo);
					if (verbose)
						System.out.println("Got Pair " + currentPair.patternOne.index + "-" + currentPair.patternTwo);
					if (currentPair.patternOne == leftMost) {
						if (verbose)
							System.out.println("Checking left");
						if (checkNewConstraints(currentPair.patternTwo, leftMost, left)) {
							if (verbose)
								System.out.println("Inserting left");
							addPattern(currentPair.patternTwo, leftMost, left);
							completed = true;
							done = false;
							if (verbose)
								System.out.println(this);
						}
					} else {
						if (verbose)
							System.out.println("Checking right");
						if (checkNewConstraints(currentPair.patternTwo, rightMost, right)) {
							if (verbose)
								System.out.println("Inserting right");
							addPattern(currentPair.patternTwo, rightMost, right);
							completed = true;
							done = false;
							if (verbose)
								System.out.println(this);
						}
					}
				}
				priorityQueue.clear();
			}
			tBar.clear();
			if (feasibilityType == zeroFeasibleOrdering)
				done = true;
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int runModifiedAllStart() {
		int bestValue = 0;
		int bestStart = -1;
		for (int i = 0; i < patterns.numPatterns(); i++) {
			givenStart(i);
			int currentValue = runModifiedHeuristic();
			if (currentValue > bestValue) {
				bestValue = currentValue;
				bestStart = i;
			}
		}
		givenStart(bestStart);
		runModifiedHeuristic();
		return bestValue;
	}
	
	/**
	 * 
	 * TODO
	 *
	 */
	public int runModifiedHeuristic() {
		boolean done = false;
		while (!done) {
			done = true;
			TreeSet<Integer> tBar = new TreeSet<Integer>();
			for (int i = 0; i < patterns.numPatterns(); i++)
				tBar.add(i);
			if (leftMost == null) {
				leftMost = new Element(0);
				rightMost = leftMost;
			}
			Element currentElement = leftMost;
			while (currentElement != null) {
				tBar.remove(currentElement.index);
				currentElement = currentElement.right;
			}
			
			PatternPair[] queueElements = new PatternPair[patterns.numPatterns()];
			PriorityQueue<PatternPair> priorityQueue = new PriorityQueue<PatternPair>();
			for (int t : tBar) {
				queueElements[t] = new PatternPair(leftMost, t);
				currentElement = leftMost.right;
				while (currentElement != null) {
					if (patterns.getBB(t, currentElement.index) < queueElements[t].BBij)
						queueElements[t] = new PatternPair(currentElement, t);
					currentElement = currentElement.right;
				}
				priorityQueue.add(queueElements[t]);
			}
			while (!priorityQueue.isEmpty()) {
				PatternPair currentPair = priorityQueue.poll();
				if (verbose)
					System.out.println("Got Pair " + currentPair.patternOne.index + "-" + currentPair.patternTwo);
				int t = currentPair.patternTwo;
				queueElements[t] = null;
				if (currentPair.BBij >= 0) {
					Element tryLeft = currentPair.patternOne;
					Element tryRight = currentPair.patternOne;
					Element newElement = null;
					while ((tryLeft != null) || (tryRight != null)) {
						if (tryLeft != null) {
							if (verbose)
								System.out.println("Checking to the left of " + tryLeft.index);
							if (checkExistingConstraints(t, tryLeft, left) && checkNewConstraints(t, tryLeft, left)) {
								newElement = addPattern(t, tryLeft, left);
								tryRight = null;
								tryLeft = null;
							} else
								tryLeft = tryLeft.left;
						}
						if (tryRight != null) {
							if (verbose)
								System.out.println("Checking to the right of " + tryRight.index);
							if (checkExistingConstraints(t, tryRight, right) && checkNewConstraints(t, tryRight, right)) {
								newElement = addPattern(t, tryRight, right);
								tryLeft = null;
								tryRight = null;
							} else
								tryRight = tryRight.right;
						}
					}
					if (newElement != null) {
						done = false;
						for (int i = 0; i < queueElements.length; i++) {
							if ((queueElements[i] != null) && (queueElements[i].BBij > patterns.getBB(t, queueElements[i].patternTwo))) {
								priorityQueue.remove(queueElements[i]);
								queueElements[i] = new PatternPair(newElement, i);
								priorityQueue.add(queueElements[i]);
							}
						}
					}
				}
			}
			priorityQueue.clear();
			tBar.clear();
			if (feasibilityType == zeroFeasibleOrdering)
				done = true;
		}
		int solutionLength = 1;
		Element currentElement = leftMost;
		while (currentElement.right != null) {
			solutionLength++;
			currentElement = currentElement.right;
		}
		return solutionLength;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param pattern
	 * @param position
	 * @param direction
	 * @throws IllegalArgumentException
	 */
	private Element addPattern(int pattern, Element position, boolean direction) throws IllegalArgumentException {
		if (position == null)
			throw new IllegalArgumentException("Position cannot be null.");
		Element t = new Element(pattern);
		if (direction == left) {
			t.left = position.left;
			t.right = position;
		} else {
			t.left = position;
			t.right = position.right;
		}
		if (t.left != null) {
			t.left.right = t;
			t.leftSum = t.left.leftSum + patterns.getPattern(t.left.index).getInnerDiameter();
		} else
			leftMost = t;
		if (t.right != null) {
			t.right.left = t;
			t.rightSum = t.right.rightSum + patterns.getPattern(t.right.index).getInnerDiameter();
		} else
			rightMost = t;
		Element currentElement = t.left;
		while (currentElement != null) {
			currentElement.rightSum += patterns.getPattern(t.index).getInnerDiameter();
			currentElement = currentElement.left;
		}
		currentElement = t.right;
		while (currentElement != null) {
			currentElement.leftSum += patterns.getPattern(t.index).getInnerDiameter();
			currentElement = currentElement.right;
		}
		
		if (verbose)
			System.out.println("Updating Min-Slack");
		t.minSlack = (t.right != null ? t.right.minSlack : Double.POSITIVE_INFINITY);
		double currentSlack = Double.POSITIVE_INFINITY;
		Element i = leftMost;
		Element j;
		while (i != t) {
			j = t;
			while (j != null) {
				if (verbose)
					System.out.println("Checking B'_{" + i.index + ", " + j.index + "}");
				if (patterns.getBB(i.index, j.index) - (right(i) - right(j.left)) < currentSlack)
					currentSlack = patterns.getBB(i.index, j.index) - (right(i) - right(j.left));
				j = j.right;
			}
			if (currentSlack < i.right.minSlack)
				i.right.minSlack = currentSlack; 
			i = i.right;
		}
		currentSlack = Double.POSITIVE_INFINITY;
		j = rightMost;
		while (j != t) {
			i = t;
			while (i != null) {
				if (verbose)
					System.out.println("Checking B'_{" + i.index + ", " + j.index + "}");
				if (patterns.getBB(i.index, j.index) - (left(j) - left(i.right)) < currentSlack)
					currentSlack = patterns.getBB(i.index, j.index) - (left(j) - left(i.right));
				i = i.left;
			}
			if (currentSlack < j.minSlack)
				j.minSlack = currentSlack; 
			j = j.left;
		}
		if (verbose)
			System.out.println(this);
		return t;
	}
	
	
	/**
	 * 
	 * TODO
	 * 
	 * @param pattern
	 * @param position
	 * @param direction
	 * @return
	 */
	private boolean checkNewConstraints(int pattern, Element position, boolean direction) {
		// Establish a marker for the element we are currently processing
		Element currentElement;
		// If we want to add the new pattern to the left of the position element
		if (direction == left) {
			// Then q = position and p = position.left
			// We need to check those patterns to the left of the insertion point
			currentElement = position.left;
			// As long as we can move left
			while (currentElement != null) {
				// Check that the new constraint is not violated
				if (verbose) {
					System.out.print("Checking: B'_{" + currentElement.index + ", " + pattern + "} = ");
					System.out.print(patterns.getBB(currentElement.index, pattern) + " >= "  + right(currentElement));
					System.out.print(" - " + right(position.left) + " is ");
				}
				if (feasibilityType == zeroFeasibleOrdering) {
					if (patterns.getBB(currentElement.index, pattern) < right(currentElement) - right(position.left)) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else if (feasibilityType == contiguousOrdering) {
					if (patterns.getBB(currentElement.index, pattern) < ((right(currentElement) - right(position.left)) % patterns.getB(currentElement.index, pattern))) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else
					throw new RuntimeException("feasibilityType flag incorrectly set");
				// Move to the left
				currentElement = currentElement.left;
			}
			// Now we need to check those patterns to the right of the insertion point
			currentElement = position;
			// As long as we can move right
			while (currentElement != null) {
				// Check that the new constraint is not violated
				if (verbose) {
					System.out.print("Checking: B'_{" + currentElement.index + ", " + pattern + "} = ");
					System.out.print(patterns.getBB(currentElement.index, pattern) + " >= "  + left(currentElement));
					System.out.print(" - " + left(position) + " is ");
				}
				if (feasibilityType == zeroFeasibleOrdering) {
					if (patterns.getBB(pattern, currentElement.index) < left(currentElement) - left(position)) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else if (feasibilityType == contiguousOrdering) {
					if (patterns.getBB(pattern, currentElement.index) < ((left(currentElement) - left(position)) % patterns.getB(pattern, currentElement.index))) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else
					throw new RuntimeException("feasibilityType flag incorrectly set");
				// Move to the right
				currentElement = currentElement.right;
			}
		// Otherwise, we want to add the new pattern to the right of position
		} else {
			// Thus p = position and q = position.right
			// Now we need to check the patterns to the left of the insertion point
			currentElement = position;
			while (currentElement != null) {
				// Check that the new constraint is not violated
				if (verbose) {
					System.out.print("Checking: B'_{" + currentElement.index + ", " + pattern + "} = ");
					System.out.print(patterns.getBB(currentElement.index, pattern) + " >= "  + right(currentElement));
					System.out.print(" - " + right(position) + " is ");
				}
				if (feasibilityType == zeroFeasibleOrdering) {
					if (patterns.getBB(currentElement.index, pattern) < right(currentElement) - right(position)) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else if (feasibilityType == contiguousOrdering) {
					if (patterns.getBB(currentElement.index, pattern) < ((right(currentElement) - right(position)) % patterns.getB(currentElement.index, pattern))) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else
					throw new RuntimeException("feasibilityType flag incorrectly set");
				// Move to the left
				currentElement = currentElement.left;
			}
			// Now check the patterns to the right of the insertion point
			currentElement = position.right;
			while (currentElement != null) {
				// Check the new constraint
				if (verbose) {
					System.out.print("Checking: B'_{" + currentElement.index + ", " + pattern + "} = ");
					System.out.print(patterns.getBB(currentElement.index, pattern) + " >= "  + left(currentElement));
					System.out.print(" - " + left(position.right) + " is ");
				}
				if (feasibilityType == zeroFeasibleOrdering) {
					if (patterns.getBB(pattern, currentElement.index) < left(currentElement) - left(position.right)) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else if (feasibilityType == contiguousOrdering) {
					if (patterns.getBB(pattern, currentElement.index) < ((left(currentElement) - left(position.right)) % patterns.getB(pattern, currentElement.index))) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else
					throw new RuntimeException("feasibilityType flag incorrectly set");
				// Move to the right
				currentElement = currentElement.right;
			}
		}
		// If no constraint was violated, then the insertion would be zero-feasible
		return true;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param pattern
	 * @param position
	 * @param direction
	 * @return
	 */
	/*
	private boolean checkNewConstraints(Pattern pattern, Element position, boolean direction) {
		// Establish a marker for the element we are currently processing
		Element currentElement;
		// If we want to add the new pattern to the left of the position element
		if (direction == left) {
			// Then q = position and p = position.left
			// We need to check those patterns to the left of the insertion point
			currentElement = position.left;
			// As long as we can move left
			while (currentElement != null) {
				// Check that the new constraint is not violated
				if (verbose) {
					System.out.print("Checking: B'_{" + currentElement.index + ", " + pattern + "} = ");
					System.out.print(patterns.getPattern(currentElement.index).BB(pattern) + " >= "  + right(currentElement));
					System.out.print(" - " + right(position.left) + " is ");
				}
				if (feasibilityType == zeroFeasibleOrdering) {
					if (patterns.getPattern(currentElement.index).BB(pattern) < right(currentElement) - right(position.left)) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else if (feasibilityType == contiguousOrdering) {
					if (patterns.getPattern(currentElement.index).BB(pattern) < ((right(currentElement) - right(position.left)) % patterns.getPattern(currentElement.index).B(pattern))) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else
					throw new RuntimeException("feasibilityType flag incorrectly set");
				// Move to the left
				currentElement = currentElement.left;
			}
			// Now we need to check those patterns to the right of the insertion point
			currentElement = position;
			// As long as we can move right
			while (currentElement != null) {
				// Check that the new constraint is not violated
				if (verbose) {
					System.out.print("Checking: B'_{" + currentElement.index + ", " + pattern + "} = ");
					System.out.print(patterns.getPattern(currentElement.index).BB(pattern) + " >= "  + left(currentElement));
					System.out.print(" - " + left(position) + " is ");
				}
				if (feasibilityType == zeroFeasibleOrdering) {
					if (patterns.getPattern(currentElement.index).BB(pattern) < left(currentElement) - left(position)) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else if (feasibilityType == contiguousOrdering) {
					if (patterns.getPattern(currentElement.index).BB(pattern) < ((left(currentElement) - left(position)) % patterns.getPattern(currentElement.index).B(pattern))) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else
					throw new RuntimeException("feasibilityType flag incorrectly set");
				// Move to the right
				currentElement = currentElement.right;
			}
		// Otherwise, we want to add the new pattern to the right of position
		} else {
			// Thus p = position and q = position.right
			// Now we need to check the patterns to the left of the insertion point
			currentElement = position;
			while (currentElement != null) {
				// Check that the new constraint is not violated
				if (verbose) {
					System.out.print("Checking: B'_{" + currentElement.index + ", " + pattern + "} = ");
					System.out.print(patterns.getPattern(currentElement.index).BB(pattern) + " >= "  + right(currentElement));
					System.out.print(" - " + right(position) + " is ");
				}
				if (feasibilityType == zeroFeasibleOrdering) {
					if (patterns.getPattern(currentElement.index).BB(pattern) < right(currentElement) - right(position)) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else if (feasibilityType == contiguousOrdering) {
					if (patterns.getPattern(currentElement.index).BB(pattern) < ((right(currentElement) - right(position)) % patterns.getPattern(currentElement.index).B(pattern))) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else
					throw new RuntimeException("feasibilityType flag incorrectly set");
				// Move to the left
				currentElement = currentElement.left;
			}
			// Now check the patterns to the right of the insertion point
			currentElement = position.right;
			while (currentElement != null) {
				// Check the new constraint
				if (verbose) {
					System.out.print("Checking: B'_{" + currentElement.index + ", " + pattern + "} = ");
					System.out.print(patterns.getPattern(currentElement.index).BB(pattern) + " >= "  + left(currentElement));
					System.out.print(" - " + left(position.right) + " is ");
				}
				if (feasibilityType == zeroFeasibleOrdering) {
					if (patterns.getPattern(currentElement.index).BB(pattern) < left(currentElement) - left(position.right)) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else if (feasibilityType == contiguousOrdering) {
					if (patterns.getPattern(currentElement.index).BB(pattern) < ((left(currentElement) - left(position.right)) % patterns.getPattern(currentElement.index).B(pattern))) {
						if (verbose)
							System.out.println("false");
						return false;
					} else if (verbose)
						System.out.println("true");
				} else
					throw new RuntimeException("feasibilityType flag incorrectly set");
				// Move to the right
				currentElement = currentElement.right;
			}
		}
		// If no constraint was violated, then the insertion would be zero-feasible
		return true;
	}
	*/
	
	/**
	 * 
	 * TODO
	 * 
	 * @param pattern
	 * @param position
	 * @param direction
	 * @return
	 */
	private boolean checkExistingConstraints(int pattern, Element position, boolean direction) {
		if (feasibilityType == zeroFeasibleOrdering) {
			if (direction == left) {
				// We check the MIN-SLACK in the gap to the left of pattern
				if (position.minSlack < patterns.getPattern(pattern).getInnerDiameter())
					return false;
			} else {
				// First we need to check the minSlack of the gap to the right of position
				if ((position.right != null) && (position.right.minSlack < patterns.getPattern(pattern).getInnerDiameter()))
					return false;
			}
			return true;
		} else if (feasibilityType == contiguousOrdering) {
			Element leftElement;
			Element rightElement;
			if (direction == left) {
				leftElement = position.left;
				rightElement = position;
			} else {
				rightElement = position.right;
				leftElement = position;
			}
			while (leftElement != null) {
				while (rightElement != null) {
					int i = leftElement.index;
					int j = rightElement.index;
					double sumDs = left(rightElement) - left(leftElement) - patterns.getPattern(i).getInnerDiameter() + patterns.getPattern(pattern).getInnerDiameter();
					if (verbose) {
						System.out.print("Checking : B'_{" + i + ", " + j + "} = " + patterns.getBB(i,j));
						System.out.print(" >= (" + sumDs + ") mod B_{" + i + ", " + j + "}" );
					}
					if (patterns.getBB(i, j) < (sumDs % patterns.getB(i, j))) {
						if (verbose)
							System.out.println(" is false");
						return false;
					} else if (verbose)
						System.out.println(" is true");
					rightElement = rightElement.right;
				}
				if (direction == left)
					rightElement = position;
				else
					rightElement = position.right;
				leftElement = leftElement.left;
			}
			return true;
		} else
			throw new RuntimeException("feasibilityType flag incorrectly set");
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param pattern
	 * @param position
	 * @param direction
	 * @return
	 */
	/*
	private boolean checkExistingConstraints(Pattern pattern, Element position, boolean direction) {
		if (feasibilityType == zeroFeasibleOrdering) {
			if (direction == left) {
				// We check the MIN-SLACK in the gap to the left of pattern
				if (position.minSlack < pattern.getInnerDiameter())
					return false;
			} else {
				// First we need to check the minSlack of the gap to the right of position
				if ((position.right != null) && (position.right.minSlack < pattern.getInnerDiameter()))
					return false;
			}
			return true;
		} else if (feasibilityType == contiguousOrdering) {
			Element leftElement;
			Element rightElement;
			if (direction == left) {
				leftElement = position.left;
				rightElement = position;
			} else {
				rightElement = position.right;
				leftElement = position;
			}
			while (leftElement != null) {
				while (rightElement != null) {
					int i = leftElement.index;
					int j = rightElement.index;
					double sumDs = left(rightElement) - left(leftElement) - patterns.getPattern(i).getInnerDiameter() + pattern.getInnerDiameter();
					if (verbose) {
						System.out.print("Checking : B'_{" + i + ", " + j + "} = " + patterns.getBB(i,j));
						System.out.print(" >= (" + sumDs + ") mod B_{" + i + ", " + j + "}" );
					}
					if (patterns.getBB(i, j) < (sumDs % patterns.getB(i, j))) {
						if (verbose)
							System.out.println(" is false");
						return false;
					} else if (verbose)
						System.out.println(" is true");
					rightElement = rightElement.right;
				}
				if (direction == left)
					rightElement = position;
				else
					rightElement = position.right;
				leftElement = leftElement.left;
			}
			return true;
		} else
			throw new RuntimeException("feasibilityType flag incorrectly set");
	}
	*/
	
	/**
	 * 
	 * TODO
	 * 
	 * @param e
	 * @return
	 */
	private double left(Element e) {
		return (e == null) ? 0 : e.leftSum;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param e
	 * @return
	 */
	private double right(Element e) {
		return (e == null) ? 0 : e.rightSum;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public Pattern[] outputSolution() {
		ArrayList<Pattern> output = new ArrayList<Pattern>();
		Element currentElement = leftMost;
		while (currentElement != null) {
			output.add(patterns.getPattern(currentElement.index));
			currentElement = currentElement.right;
		}
		return output.toArray(new Pattern[1]);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[] outputSolutionIndices() {
		Element currentElement = leftMost;
		int numPatterns = 0;
		while (currentElement != null) {
			numPatterns++;
			currentElement = currentElement.right;
		}
		int[] solution = new int[numPatterns];
		currentElement = leftMost;
		int currentPattern = 0;
		while (currentElement != null) {
			solution[currentPattern] = currentElement.index;
			currentPattern++;
			currentElement = currentElement.right;
		}
		return solution;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public String printSolution() {
		PatternSet solution = patterns.subset(outputSolutionIndices());
		//System.out.println(solution);
		solution.setZeroSolution();
		FitNetwork net = new FitNetwork(solution.numPatterns());
		net.testSolution(solution);
		return solution.toString();
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public boolean checkSolution() {
		PatternSet solution = patterns.subset(outputSolutionIndices());
		//System.out.println(solution);
		FitNetwork net = new FitNetwork(solution.numPatterns());
		if (feasibilityType == zeroFeasibleOrdering)
			solution.setZeroSolution();
		else {
			int sumD = 0;
			for (int i = 0; i < solution.numPatterns(); i++) {
				solution.getPattern(i).setStartingPosition(sumD);
				sumD += solution.getPattern(i).getInnerDiameter();
			}
			FeasibilityTest.findKsFromStartingPositions(solution);
		}
		return net.testSolution(solution);
	}
	
	/*
	 * The following methods implement the Bin<Pattern> Interface
	 */
	
	public void setVerbosity(boolean v) {
		verbose = v;
	}
	
	/**
	 * TODO
	 * 
	 * @param bin
	 * @param pattern
	 * @return
	 */
	public boolean fit(int pattern) throws IllegalArgumentException {
		if ((pattern < 0) || (pattern >= patterns.numPatterns()))
			throw new IllegalArgumentException("Index out of bounds.");
		if (!this.checkSolution()) {
			this.runModifiedAllStart();
			if (!this.checkSolution())
				throw new IllegalArgumentException("This bin is not currently feasible.");
		}
		if ((complexityLevel % 2) == 0) {
			if (checkNewConstraints(pattern, leftMost, left))
				return true;
			if (checkNewConstraints(pattern, rightMost, right))
				return true;
		} else {
			double smallestBB = Double.POSITIVE_INFINITY;
			Element smallestBBElement = null;
			Element currentElement = leftMost;
			while (currentElement != null) {
				if (patterns.getBB(currentElement.index, pattern) < smallestBB)
					smallestBBElement = currentElement;
				currentElement = currentElement.right;
			}
			Element tryLeft = smallestBBElement;
			Element tryRight = smallestBBElement;
			while ((tryLeft != null) || (tryRight != null)) {
				if (tryLeft != null) {
					if (verbose)
						System.out.println("Checking to the left of " + tryLeft.index);
					if (checkExistingConstraints(pattern, tryLeft, left) && checkNewConstraints(pattern, tryLeft, left)) {
						return true;
					} else
						tryLeft = tryLeft.left;
				}
				if (tryRight != null) {
					if (verbose)
						System.out.println("Checking to the right of " + tryRight.index);
					if (checkExistingConstraints(pattern, tryRight, right) && checkNewConstraints(pattern, tryRight, right)) {
						return true;
					} else
						tryRight = tryRight.right;
				}
			}
		}
		return false;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[] outputIndices() {
		return this.outputSolutionIndices();
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param pattern
	 * @return
	 * @throws IllegalArgumentException
	 */
	public boolean insert(int pattern) {
		if ((pattern < 0) || (pattern >= patterns.numPatterns()))
			throw new IllegalArgumentException("Index out of bounds.");
		if (!this.checkSolution()) {
			this.runModifiedAllStart();
			if (!this.checkSolution())
				throw new IllegalArgumentException("This bin is not currently feasible.");
		}
		if ((complexityLevel % 2) == 0) {
			if (checkNewConstraints(pattern, leftMost, left)) {
				addPattern(pattern, leftMost, left);
				return true;
			}
			if (checkNewConstraints(pattern, rightMost, right)) {
				addPattern(pattern, rightMost, right);
				return true;
			}
		} else {
			double smallestBB = Double.POSITIVE_INFINITY;
			Element smallestBBElement = null;
			Element currentElement = leftMost;
			while (currentElement != null) {
				if (patterns.getBB(pattern, currentElement.index) < smallestBB)
					smallestBBElement = currentElement;
				currentElement = currentElement.right;
			}
			Element tryLeft = smallestBBElement;
			Element tryRight = smallestBBElement;
			while ((tryLeft != null) || (tryRight != null)) {
				if (tryLeft != null) {
					if (verbose)
						System.out.println("Checking to the left of " + tryLeft.index);
					if (checkExistingConstraints(pattern, tryLeft, left) && checkNewConstraints(pattern, tryLeft, left)) {
						addPattern(pattern, tryLeft, left);
						return true;
					} else
						tryLeft = tryLeft.left;
				}
				if (tryRight != null) {
					if (verbose)
						System.out.println("Checking to the right of " + tryRight.index);
					if (checkExistingConstraints(pattern, tryRight, right) && checkNewConstraints(pattern, tryRight, right)) {
						addPattern(pattern, tryRight, right);
						return true;
					} else
						tryRight = tryRight.right;
				}
			}
		}
		return false;
	}
	
	/**
	 * TODO
	 * 
	 */
	public boolean reSolve() {
		throw new UnsupportedOperationException("The reSolve method has not het been implemented for PrimHeuristic.");
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param level
	 */
	public void setLevel(int level) throws IllegalArgumentException {
		if ((level > numLevels()) || (level < 0))
			throw new IllegalArgumentException("Level number out of range.");
		this.complexityLevel = level;
		feasibilityType = (complexityLevel > zeroFeasibleOrdering_AllPosition) ? contiguousOrdering : zeroFeasibleOrdering;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int numLevels() {
		return contiguousOrdering_AllPosition;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param pattern
	 * @return
	 */
	public PrimHeuristic newBin(int pattern) {
		PrimHeuristic newBin = new PrimHeuristic(patterns, pattern);
		newBin.setLevel(this.complexityLevel);
		return newBin;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int binSize() {
		Element currentElement = leftMost;
		int numPatterns = 0;
		while (currentElement != null) {
			numPatterns++;
			currentElement = currentElement.right;
		}
		return numPatterns;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public Iterator<Integer> iterator() {
		return new PrimIterator(this);
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
		Element currentElement = leftMost;
		String s = "=======================" + endln;
		while (currentElement != null) {
			s += currentElement.index + ": " + patterns.getPattern(currentElement.index) + "[" + currentElement.minSlack + "]" + endln;
			currentElement = currentElement.right;
		}
		s += "=======================" + endln;
		return s;
	}
	
	/**
	 * This main method is only used for testing this class 
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		int numPatterns = 10;
		int numTests = 100;
		MersenneTwisterFast random = new MersenneTwisterFast(5859468024l);
		PrimHeuristic heuristic;
		//IntegerUtils integers = new IntegerUtils(1000);
		int templateSum = 0;
		int badSetCounter = 0;
		for (int i = 0; i < numTests; i++) {
			System.out.println("======================================================");
			System.out.println("Set #" + i + ":");
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
			}*/
			LinearPatternSet patterns = LinearPatternSet.randomKorstPatternSet(numPatterns, 10, random);
			heuristic = new PrimHeuristic(patterns);
			heuristic.feasibilityType = contiguousOrdering;
			heuristic.runModifiedAllStart();
			int[] solution = heuristic.outputSolutionIndices();
			LinearPatternSet solutionSet = patterns.subset(solution);
			solutionSet.setZeroSolution();
			if (!FeasibilityTest.testInLineOrdering(solutionSet))
				System.out.println("Invalid Inline Ordering.");
			else
				System.out.println("Solution: " + solution.length + " patterns.");
			//heuristic.verbose = true;
			/*
			if (heuristic.runModifiedAllStart() == numPatterns) {
				patterns.setPermutation(heuristic.outputSolutionIndices());
				if (!FeasibilityTest.testInLineOrdering(patterns))
					System.out.println("Invalid Inline Ordering.");
				else
					System.out.println("Valid");
			}
			*/
		}
		System.out.println("Bad Sets: " + badSetCounter);
	}
	
	/**
	 * 
	 * TODO
	 *
	 * @author Brad Paynter
	 * @version Jul 2, 2010
	 *
	 */
	private class Element {
		/**
		 * TODO
		 */
		public int index;
		/**
		 * TODO
		 */
		public Element left;
		/**
		 * TODO
		 */
		public Element right;
		/**
		 * TODO
		 */
		public double leftSum;
		/**
		 * TODO
		 */
		public double rightSum;
		/**
		 * Holds the MIN-SLACK value for the gap to the left of this element
		 */
		public double minSlack;
		
		/**
		 * 
		 * TODO
		 * 
		 * @param index
		 */
		public Element(int index) {
			this.index = index;
			left = null;
			right = null;
			leftSum = 0;
			rightSum = 0;
			minSlack = Double.POSITIVE_INFINITY;
		}
	}
	
	/**
	 * 
	 * TODO
	 *
	 * @author Brad Paynter
	 * @version Jul 29, 2011
	 *
	 */
	private class PrimIterator implements Iterator<Integer> {
		private Element currentPosition;
		
		public PrimIterator(PrimHeuristic heuristic) {
			this.currentPosition = heuristic.leftMost;
		}
		
		public boolean hasNext() {
			return (currentPosition != null);
		}
		
		public Integer next() {
			Integer next = currentPosition.index;
			currentPosition = currentPosition.right;
			return next;
		}
		
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Remove is not implemented for this iterator.");
		}
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
	private class PatternPair implements Comparable<PatternPair> {
		/**
		 * The first pattern in the pair (i)
		 */
		public Element patternOne;
		/**
		 * The second pattern in the pair (j)
		 */
		public int patternTwo;
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
		public PatternPair(Element i, int j) {
			patternOne = i;
			patternTwo = j;
			BBij = patterns.getBB(i.index, j);
		}
		
		/**
		 * Compares two PatternPair objects according to their B'_{ij} values
		 * 
		 * @param o The PatternPair to compare to this one
		 * @return Returns -1,0,1 if this PatternPair is less than, equal to or 
		 * 			greater than the given PatternPair respectively.
		 */
		public int compareTo(PatternPair o) {
			if (this.BBij < o.BBij)
					return -1;
			else if ((this.patternOne == o.patternOne) && (this.patternTwo == o.patternTwo))
				return 0;
			else
				return 1;
		}
	}

	
}
