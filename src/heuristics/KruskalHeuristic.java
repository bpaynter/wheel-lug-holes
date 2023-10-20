/**
 * KruskalHeuristic.java
 * Jun 10, 2010
 */
package heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import exact.FeasibilityTest;
import exact.FitNetwork;

import problem.*;
import util.IntegerUtils;
import util.MersenneTwisterFast;

/**
 * This code is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * This class enables the user to run the Kruskal Heuristic on a set of patterns to
 * partition the patterns into templates. The heuristic tries to minimize the total number
 * of templates available
 *
 * @author Brad Paynter
 * @version Jun 10, 2010
 *
 */
public class KruskalHeuristic {
	/**
	 * The input set of patterns
	 */
	private PatternSet patterns;
	/**
	 * A set of link elements, one for each pattern
	 */
	private Element[] elements;
	/**
	 * A matrix telling whether any given pair of patterns has
	 * been checked
	 */
	private boolean[][] checked;
	/**
	 * This flag is to determine the level of output for the heuristic.
	 * It defaults to false and must be set manually if more output
	 * is required.
	 */
	public boolean verbose;
	
	public int feasibilityType;
	public static final int zeroFeasibleOrdering = 0;
	public static final int contiguousOrdering = 1;
	
	/**
	 * Constructs an object with the given patterns that can use
	 * the Kruskal Heuristic to partition them into templates.
	 * 
	 * @param patterns The patterns to be partitioned into templates
	 */
	public KruskalHeuristic(PatternSet patterns) {
		// Store the patterns
		this.patterns = patterns;
		// Create an element for each pattern
		this.elements = new Element[patterns.numPatterns()];
		for (int i = 0; i < elements.length; i++) {
			elements[i] = new Element(i);
		}
		// Allocate space for the check matrix
		checked = new boolean[patterns.numPatterns()][patterns.numPatterns()];
		verbose = false;
		feasibilityType = zeroFeasibleOrdering;
	}
	
	/**
	 * Runs the Kruskal Heuristic
	 */
	public void runHeuristic() {
		// Initialize the check matrix to false
		int iterationNumber = 0;
		for (boolean[] array : checked)
			Arrays.fill(array, false);
		// Create a priority queue for the pairs of patterns
		PriorityQueue<PatternPair> priorityQueue = new PriorityQueue<PatternPair>();
		// Add all pairs of patterns into the priority queue
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i + 1; j < patterns.numPatterns(); j++)
				priorityQueue.add(new PatternPair(i, j));
		// While there are patterns in the priority queue
		while (!priorityQueue.isEmpty()) {
			// Get the next pair of patterns
			PatternPair min = priorityQueue.poll();
			// See if these two patterns have previously been checked
			boolean joined = false;
			if (!checked[min.patternOne][min.patternTwo]) {
				// Check to see if we can join(i,j)
				if (checkFeasibility
							(elements[min.patternOne], elements[min.patternTwo])) {
					joinSet(elements[min.patternOne], elements[min.patternTwo]);
					joined = true;
				// Check to see if we can join(j,i)
				} else if (checkFeasibility
							(elements[min.patternTwo], elements[min.patternOne])) {
					joinSet(elements[min.patternTwo], elements[min.patternOne]);
					joined = true;
				// If neither worked then reverse one set
				} else {
					findSet(elements[min.patternOne]).reverse();
					if (checkFeasibility
								(elements[min.patternOne], elements[min.patternTwo])) {
						joinSet(elements[min.patternOne], elements[min.patternTwo]);
						joined = true;
					// Check to see if we can join(j,i)
					} else if (checkFeasibility
								(elements[min.patternTwo], elements[min.patternOne])) {
						joinSet(elements[min.patternTwo], elements[min.patternOne]);
						joined = true;
					}
				}
			}
			
			if (joined && verbose) {
				int[] bins = binSizes();
				System.out.print("Iteration #" + iterationNumber + " - Current Bins: " + bins[0]);
				for (int i = 1; i < bins.length; i++)
					System.out.print("," + bins[i]);
				System.out.println();
			}
	
			iterationNumber++;
		}
	}
	
	/**
	 * Checks whether the set containing element <code>p</code> can
	 * be joined to the left of the set containing element <code>q</code>
	 * while maintaining zero-feasibility. It also updates the 
	 * check matrix
	 * 
	 * @param p An element in the left set
	 * @param q An element in the right set
	 * @return <code>true</code> if joining the two sets would maintain
	 * 			zero-feasibility, <code>false</code> else.
	 */
	private boolean checkFeasibility(Element p, Element q) {
		// Set the flag for feasibility
		boolean feasible = true;
		// Find the sets containing p and q
		Set a = findSet(p);
		Set b = findSet(q);
		// Check all pairs of i in a and j in b
		for (Element i : a)
			for (Element j : b) {
				// If any pair violates the zero-feasibility constraints then the sets
				// cannot be joined and the flag is set to false
				if (verbose) {
					System.out.print("Checking B'_{" + i.index + ", " + j.index + "} = ");
					System.out.print(patterns.getBB(i.index, j.index) + " >= ");
					System.out.print(a.rightSum(i) + " + " + b.leftSum(j) + " : ");
				}
				if (feasibilityType == zeroFeasibleOrdering) {
					if (patterns.getBB(i.index, j.index) < a.rightSum(i) + b.leftSum(j)) {
						feasible = false;
						if (verbose)
							System.out.println("false");
					} else if (verbose)
						System.out.println("true");
				} else if (feasibilityType == contiguousOrdering) {
					if (patterns.getBB(i.index, j.index) < ((a.rightSum(i) + b.leftSum(j)) % patterns.getB(i.index, j.index))) {
						feasible = false;
						if (verbose)
							System.out.println("false");
					} else if (verbose)
						System.out.println("true");
				} else
					throw new RuntimeException("feasibilityType flag incorrectly set.");
				// Store the fact that this pair has been checked 
				checked[i.index][j.index] = true;
				checked[j.index][i.index] = true; 
			}
		// Return the flag
		return feasible;
	}
	
	/**
	 * Joins the set containing element <code>i</code> and the set
	 * containing element <code>j</code> It assumes that these sets are
	 * different. This method updates all relevent data.
	 * 
	 * @param i An element in the first set
	 * @param j An element in the second set
	 */
	private void joinSet(Element i, Element j) {
		// Determing which sets the two elements are in
		Set a = findSet(i);
		Set b = findSet(j);
		// If one set is reversed and the other is not...
		if (a.reverse != b.reverse)
			// Then reverse the smaller one.
			if (a.size < b.size)
				a.reverseSet();
			else
				b.reverseSet();
		// If the two sets are not reversed then join b to the 
		// right of a
		if (!a.reverse) {
			// Update the Left/Right sums
			for (Element e : a)
				e.rightSum += b.setSum;
			for (Element e : b)
				e.leftSum += a.setSum;
			// Update the linked-list links
			a.rightmost.right = b.leftmost;
			b.leftmost.left = a.rightmost;
			a.rightmost = b.rightmost;
			b.leftmost = a.leftmost;
			
		// If the two sets are reversed then join b to the
		// left of a
		} else {
			// Update the Left/Right sums
			for (Element e : a)
				e.leftSum += b.setSum;
			for (Element e : b)
				e.rightSum += a.setSum;
			// Update the linked-list links
			b.rightmost.right = a.leftmost;
			a.leftmost.left = b.rightmost;
			b.rightmost = a.rightmost;
			a.leftmost = b.leftmost;
			
		}
		// Correct the set totals
		a.setSum += b.setSum;
		b.setSum = a.setSum;
		// Correct the sizes
		a.size += b.size;
		b.size = a.size;
		// Union the sets by rank (Update the find-set links)
		if (a.rank < b.rank) 
			b.joinSet(a);
		else
			a.joinSet(b);
		if (verbose) {
			System.out.println(this);
			System.out.println("=====================");
		}
		
		
	}
	
	/**
	 * Finds the set containing the given element. This method
	 * performs path-halving on the find-set tree as it goes,
	 * thus the running time of this operation is inverse Ackerman
	 * 
	 * @param p The element
	 * @return The set containing the element <code>p</code>
	 */
	private Set findSet(Element p) {
		Element current = p;
		// Walk up the find-set tree, performing path-halving as we go
		while (current.set == null) {
			Element top = current.up.up;
			current.up = top;
			current = top;
		}
		return current.set;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[][] outputSolutionIndices() {
		Set[] sets = getSets();
		int[][] output = new int[sets.length][];
		for (int i = 0; i < sets.length; i++) {
			int setLength = 0;
			for (@SuppressWarnings("unused") Element e : sets[i])
				setLength++;
			output[i] = new int[setLength];
			int setIndex = 0;
			for (Element e : sets[i])
				output[i][setIndex++] = e.index;
		}
		return output;
	}
	
	/**
	 * Outputs an array of arrays of patterns, where each array represents
	 * a template formed by this heuristic. If the runHeuristic() method has not
	 * yet been run, this method will return a separate array for each pattern
	 * 
	 * @return The solution formed by the heuristic
	 */
	public Pattern[][] outputSolution() {
		Set[] sets = getSets();
		Pattern[][] output = new Pattern[sets.length][];
		for (int i = 0; i < sets.length; i++) {
			ArrayList<Pattern> currentSet = new ArrayList<Pattern>();
			for (Element e : sets[i])
				currentSet.add(patterns.getPattern(e.index));
			output[i] = currentSet.toArray(new Pattern[1]);
		}
		return output;
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
		boolean validSolution = true;
		// Get the current solution
		int[][] solution = outputSolutionIndices();
		// For each template in the solution
		for (int[] set : solution) {
			// Create a PatternSet for each set
			PatternSet currentSet = patterns.subset(set);
			// Create a network to test the solution
			FitNetwork net = new FitNetwork(set.length);
			if (feasibilityType == zeroFeasibleOrdering)
				currentSet.setZeroSolution();
			else {
				int sumD = 0;
				for (int i = 0; i < currentSet.numPatterns(); i++) {
					currentSet.getPattern(i).setStartingPosition(sumD);
					sumD += currentSet.getPattern(i).getInnerDiameter();
				}
				FeasibilityTest.findKsFromStartingPositions(currentSet);
			}
			// Test the solution
			if (!net.testSolution(currentSet))
				validSolution = false;
		}
		return validSolution;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int numberOfBins() {
		return getSets().length;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[] binSizes() {
		int[][] solution = outputSolutionIndices();
		int[] output = new int[solution.length];
		for (int i = 0; i < solution.length; i++)
			output[i] = solution[i].length;
		return output;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public String printSolution() {
		String endln = System.getProperty("line.separator");
		int[][] solution = outputSolutionIndices();
		PatternSet currentSet = patterns.subset(solution[0]);
		// Set the PatternSet to the zero-solution in the 
		// current ordering
		currentSet.setZeroSolution();
		// Create a network to test the solution
		FitNetwork net = new FitNetwork(solution[0].length);
		// Test the solution
		net.testSolution(currentSet);
		// For each template in the solution
		String s = currentSet.toString();
		for (int i = 1; i < solution.length; i++) {
			s += endln;
			// Create a PatternSet for each set
			currentSet = patterns.subset(solution[i]);
			// Set the PatternSet to the zero-solution in the 
			// current ordering
			currentSet.setZeroSolution();
			// Create a network to test the solution
			net = new FitNetwork(solution[i].length);
			// Test the solution
			net.testSolution(currentSet);
			s += currentSet.toString();
		}
		return s;
	}
	
	/**
	 * Returns the sets formed by the heuristic
	 * 
	 * @return An array of Sets formed by the heuristic
	 */
	private Set[] getSets() {
		HashSet<Set> sets = new HashSet<Set>();
		for (Element e : elements)
			sets.add(findSet(e));
		return sets.toArray(new Set[1]);
	}
	
	/**
	 * Creates a human-readable version of the current partition
	 * of the patterns into templates
	 * 
	 * @return A String representation of the current partition.
	 */
	@Override
	public String toString() {
		String endln = System.getProperty("line.separator");
		String output = "";
		Set[] sets = getSets();
		for (Set s : sets) {
			output += "------------------" + endln;
			output += s + endln;
		}
		output += "------------------" + endln;
		return output;
	}
	
	/**
	 * This main method is only used for testing this class 
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		/*
		Pattern[] patterns = new Pattern[10];
		patterns[0] = new Pattern(193876, 81, 6);
		patterns[1] = new Pattern(193876, 84, 19);
		patterns[2] = new Pattern(193876, 48, 2);
		patterns[3] = new Pattern(193876, 38, 7);
		patterns[4] = new Pattern(193876, 60, 19);
		patterns[5] = new Pattern(193876, 70, 10);
		patterns[6] = new Pattern(193876, 47, 28);
		patterns[7] = new Pattern(193876, 23, 13);
		patterns[8] = new Pattern(193876, 75, 27);
		patterns[9] = new Pattern(193876, 53, 11);
		
		PatternSet example = new PatternSet(patterns);
		//System.out.print(example);
		KruskalHeuristic heuristic = new KruskalHeuristic(example);
		heuristic.runHeuristic();
		//System.out.println(heuristic.outputSolution().length);
		if (!heuristic.checkSolution()) {
			System.out.println(heuristic);
			System.out.println("MISTAKE!");
		}
		*/
		
		KruskalHeuristic heuristic;
		int numPatterns = 100;
		int numTests = 10;
		MersenneTwisterFast random = new MersenneTwisterFast(57482967240l);
		IntegerUtils integers = new IntegerUtils(200);
		for (int i = 0; i < numTests; i++) {
			System.out.println("=====================================================================");
			System.out.println("Set #" + i);
			CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers);
			boolean valid = false;
			while (!valid) {
				valid = true;
				try {
					patterns = CircularPatternSet.randomTightPatternSet(numPatterns, 30, 1, 200, 3, random, integers);
				} catch (IllegalArgumentException e) {
					valid = false;
				}
			}
			random.nextInt(patterns.numPatterns());
			heuristic = new KruskalHeuristic(patterns);
			heuristic.runHeuristic();
			/*
			Pattern[][] solution = heuristic.outputSolution();
			//System.out.println(solution.length);
			
			int largestTemplate = 0;
			for (int j = 0; j < solution.length; j++)
				if (solution[j].length > largestTemplate)
					largestTemplate = solution[j].length;
			System.out.println(largestTemplate);
			*/
			
			if (!heuristic.checkSolution()) {
				System.out.println(patterns);
				System.out.println("MISTAKE!");
			}

		}
		
		/*
		int numTests = 100;
		//int numPatterns = 10;
		int pMax = 30;
		MersenneTwisterFast random = new MersenneTwisterFast(5859468024l * pMax);
		for (int numPatterns = 10; numPatterns <= 200; numPatterns += 10) {
			double totalDifference = 0;
			double totalDifference2 = 0;
			for (int i = 0; i < numTests; i++) {
				PatternSet patterns = CircularPatternSet.randomKorstPatternSet(numPatterns, pMax, random);
				//System.out.println("Got Set.");
				heuristic = new KruskalHeuristic(patterns);
				heuristic.runHeuristic();
				if (!heuristic.checkSolution())
					System.out.println("Kruskal screwed up!!!");
				int[][] solution = heuristic.outputSolutionIndices();
				//System.out.println("Kruskal Done.");
				PatternSet[] partition = SetPartition.setPartition(patterns);
				//System.out.println("Partitioned.");
				int lowerBound = 0;
				int kruskal2 = 0;
				for (PatternSet s : partition) {
					lowerBound += s.densityBinLowerBound();
					heuristic = new KruskalHeuristic(s);
					heuristic.runHeuristic();
					kruskal2 += heuristic.outputSolution().length;
				}
				//System.out.println("Set #" + i + ": Lower bound = " + lowerBound + ", Kruskal Solution 1 = " + solution.length + ", Kruskal Solution 2 = " + kruskal2);
				double difference = (solution.length - lowerBound) * 100d / lowerBound;
				totalDifference += difference;
				totalDifference2 += (kruskal2 - lowerBound) * 100d / lowerBound;
			}
			System.out.println("Average difference for " + numPatterns + " patterns : " + (totalDifference / numTests) + " & " + (totalDifference2 / numTests));
		}
		*/
	}
	
	/**
	 * This class represents a set of Elements. Contains classes for finding
	 * the beginning and end of the linked list it contains. It also allows
	 * for lazy reverses.
	 *
	 * @author Brad Paynter
	 * @version Jun 19, 2010
	 *
	 */
	private class Set implements Iterable<Element> {
		/**
		 * The height of this set's find-set tree
		 */
		public int rank;
		/**
		 * The number of elements (patterns) in this set 
		 */
		public int size;
		/**
		 * Whether this set as been reversed or not
		 */
		private boolean reverse;
		/**
		 * The leftmost element of this chain
		 */
		private Element leftmost;
		/**
		 * The rightmost element of this chain
		 */
		private Element rightmost;
		/**
		 * The root of the find-set tree
		 */
		public Element root;
		/**
		 * The sum of the pattern inner diameters in this set
		 */
		public double setSum;
		
		/**
		 * Constructs a set containing only the given element
		 *  
		 * @param i The element that this set will contain
		 */
		public Set(Element i) {
			reverse = false;
			leftmost = i;
			rightmost = i;
			size = 1;
			root = i;
			rank = 0;
			setSum = patterns.getPattern(i.index).getInnerDiameter();
		}
		
		/**
		 * Gets the first element in this set
		 * 
		 * @return The first element in this set
		 */
		public Element getFirst() {
			if (!reverse)
				return leftmost;
			else
				return rightmost;
		}
		
		/**
		 * Merges the set <code>s</code> into this set. It updates the find-set
		 * tree but does NOT update the linked lists or any of the stored data.
		 * It also erases set <code>s</code> as all of its elements are now
		 * part of this set.
		 * See <code>joinSet(Element, Element)</code> in the main class.
		 * 
		 * @param s The set to be merged into this one
		 * @throws IllegalArgumentException
		 */
		public void joinSet(Set s) throws IllegalArgumentException {
			if (s.rank > this.rank)
				throw new IllegalArgumentException
							("Must merge smaller rank set into larger rank set.");
			else if (s.rank == this.rank)
				this.rank++;
			s.root.set = null;
			s.root.up = this.root;
			s.clear();
		}
		
		/**
		 * Reverses the linked list order of this set (this method does not
		 * actually reverse the set but sets the <code>reverse</code> flag)
		 */
		public void reverse() {
			reverse = !reverse;
		}
		
		/**
		 * Gets the LEFT sum for an element in this set
		 * 
		 * @param i The element we need to find the LEFT sum for
		 * @return The LEFT sum of element <code>i</code>
		 * @throws IllegalArgumentException Thrown if the element <code>i</code> is not
		 * 			in this set
		 */
		public int leftSum(Element i) throws IllegalArgumentException {
			if (findSet(i) != this)
				throw new IllegalArgumentException("This Element is not in this Set.");
			if (!reverse)
				return i.leftSum;
			else
				return i.rightSum;
		}
		
		/**
		 * Gets the RIGHT sum for an element in this set
		 * 
		 * @param i The element we need to find the RIGHT sum for
		 * @return The RIGHT sum of element <code>i</code>
		 * @throws IllegalArgumentException Thrown if the element <code>i</code> is not
		 * 			in this set
		 */
		public int rightSum(Element i) throws IllegalArgumentException {
			if (findSet(i) != this)
				throw new IllegalArgumentException("This Element is not in this Set.");
			if (!reverse)
				return i.rightSum;
			else
				return i.leftSum;
		}
		
		/**
		 * Actually reverses the set. This method reverses the set
		 * internally instead of just flipping the <code>reverse</code> flag
		 */
		private void reverseSet() {
			// Update the flag
			reverse = !reverse;
			// Switch the leftmost and rightmost elements
			Element current = leftmost;
			leftmost = rightmost;
			rightmost = current;
			// Walk the list correcting the links as we go
			Element last = null;
			while (current != null) {
				int sum = current.leftSum;
				current.leftSum = current.rightSum;
				current.rightSum = sum;
				Element next = current.right;
				current.right = last;
				current.left = next;
				last = current;
				current = next;
			}
		}
		
		/**
		 * Clears all pointers in the set. This method does NOT
		 * affect the elements of the set in any way, it just enables
		 * the set object to be ready for Garbage Collection.
		 */
		private void clear() {
			leftmost = null;
			rightmost = null;
		}
		
		/**
		 * Generates an iterator over the elements of this set
		 * 
		 * @return An iterator over this set
		 */
		public Iterator<Element> iterator() {
			return new SetIterator(this);
		}
		
		/**
		 * Generates a human-readable version of this set. It comprises of a
		 * list of the indices of the elements in this set in their set order
		 * 
		 * @return A String representation of this set. 
		 */
		@Override
		public String toString() {
			String endln = System.getProperty("line.separator");
			String s = "";
			for (Element e : this) {
				s += e.index + ": " + patterns.getPattern(e.index) + endln;
			}
			return s;
		}
		
		/**
		 * An iterator over this Set object.
		 *
		 * @author Brad Paynter
		 * @version Jun 19, 2010
		 *
		 */
		private class SetIterator implements Iterator<Element> {
			/**
			 * The set we are iterating over
			 */
			private Set mySet;
			/**
			 * The next element to be returned by this iterator
			 */
			private Element currentElement;
			
			/**
			 * Creates an iterator over the given set
			 * 
			 * @param s The set to iterate over
			 */
			public SetIterator(Set s) {
				mySet = s;
				currentElement = mySet.getFirst();
			}

			/**
			 * Determines whether this iterator has more elements to return
			 * 
			 * @return <code>false</code> if this iterator has reached the end of the set
			 * 			<code>true</code> else
			 */
			public boolean hasNext() {
				return (currentElement != null);
			}

			/**
			 * Returns the next element in the set and moves
			 * the iterator forward.
			 * 
			 * @return The next element in the set.
			 */
			public Element next() {
				if (this.hasNext()) {
					// Store the current element
					Element returnElement = currentElement;
					// Move to the next element
					if (!mySet.reverse)
						currentElement = currentElement.right;
					else
						currentElement = currentElement.left;
					// Return the stored element
					return returnElement;
				} else
					return null;
			}

			/**
             * This operation is not supported by this iterator and this method
             * will throw an UnsupportedOperationException if invoked.
             *
             * @throws UnsupportedOperationException Always thrown, since this
             *			method is not implemented for this iterator
             */
			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException
								("This method is not supported by this iterator.");				
			}
		}
	}
	
	/**
	 * This class is a wrapper for a Pattern. It contains all the
	 * links needed to be part of the set including a doubly-linked
	 * chain and a find-set tree. It also contains LEFT and RIGHT data
	 *
	 * @author Brad Paynter
	 * @version Jun 19, 2010
	 *
	 */
	private class Element {
		/**
		 * The number of the pattern that this element represents
		 */
		public int index;
		/**
		 * The Element to the left of this one in the chain
		 */
		public Element left;
		/**
		 * The Element to the right of this one in the chain
		 */
		public Element right;
		/**
		 * The Element above this one in the find-set tree 
		 */
		public Element up;
		/**
		 * The Set that this element is contained in.
		 * This pointer will be null unless this element is the
		 * root of its find-set tree 
		 */
		public Set set;
		/**
		 * The sum of all d_i's to the left of this pattern
		 */
		public int leftSum;
		/**
		 * The sum of all d_i's to the right of this pattern
		 */
		public int rightSum;
		
		/**
		 * Constructs an element with index <code>i</code> with no
		 * pointers, and a Set containing only this Element.
		 * 
		 * @param index The index of the Element
		 */
		public Element(int index) {
			this.index = index;
			left = null;
			right = null;
			up = this;
			set = new Set(this); 
			leftSum = 0;
			rightSum = 0;
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
		public int patternOne;
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
		public PatternPair(int i, int j) {
			patternOne = i;
			patternTwo = j;
			BBij = patterns.getBB(i, j);
		}
		
		/**
		 * Compares two PatternPair objects according to their B'_{ij} values
		 * 
		 * @param o The PatternPair to campare to this one
		 * @return Returns -1,0,1 if this PatternPair is less than, equal to or 
		 * 			greater than the given PatternPair respectively.
		 */
		public int compareTo(PatternPair o) {
			if (this.BBij < o.BBij)
				return -1;
			else if (this.BBij == o.BBij)
				return 0;
			else
				return 1;
		}
		
	}
}
