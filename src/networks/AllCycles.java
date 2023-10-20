/**
 * AllCycles.java
 * Nov 2, 2009
 */
package networks;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * This class contains all cycles on a complete directed network on <code>n</code> nodes.
 * There is no public constructor; all construction should be done using the  
 *
 * @author Brad Paynter
 * @version Nov 2, 2009
 *
 */
public class AllCycles implements Iterable<Cycle> {
	/**
	 * The collection of cycles
	 */
	private ArrayList<ArrayList<Cycle>> cycles;
	/**
	 * The length of the largest cycle in this collection.
	 * Also the number of nodes in the network these cycles are from.
	 */
	private int size;
	
	/**
	 * Constructs an object containing all cycles on a complete,
	 * directed network on <code>numNodes</code> nodes.
	 *  
	 * @param numNodes The length of the longest possible cycle
	 */
	public AllCycles(int numNodes) {
		size = numNodes - 2;
		cycles = new ArrayList<ArrayList<Cycle>>(size + 1);
		for (int i = 0; i < size + 1; i++)
			cycles.add(new ArrayList<Cycle>());
		// We need to create cycles on currentNodes = 2..numNodes nodes
		for (int currentNodes = 2; currentNodes <= numNodes; currentNodes++) {
			// We need to create cycles of length n by adding the newest node 
			// (currentNodes) to the existing cycles of length n-1
			for (int currentCycleLength = currentNodes; 
								currentCycleLength >= 2; currentCycleLength--) {
				// We need to create 2-cycles from scratch
				if (currentCycleLength == 2) {
					// Create a 2-cycle from all existing nodes to the new node
					for (int i = 0; i < currentNodes - 1; i++) {
						int[] twoCycle = {i, currentNodes - 1}; 
						cycles.get(0).add(new Cycle(twoCycle));
					}
				} else {
					// For each existing cycle of length n-1
					for (Cycle cycle : cycles.get((currentCycleLength - 2) - 1)) {
						int[] cycleArray = cycle.getCycle();
						// We can add the new node between any two nodes in the existing cycle
						for (int i = 0; i < cycleArray.length; i++) {
							int[] newCycle = new int[cycleArray.length + 1];
							// Leave the first i+1 nodes
							for (int j = 0; j <= i; j++)
								newCycle[j] = cycleArray[j];
							// Put the new node in position i+1
							newCycle[i+1] = currentNodes - 1;
							// Place the rest of the nodes shifted one down
							for (int j = i+2; j <= cycleArray.length; j++)
								newCycle[j] = cycleArray[j - 1];
							// Add then new cycle to the collection
							cycles.get(newCycle.length - 2).add(new Cycle(newCycle));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Returns the length of the longest cycle in this structure.
	 * Also the number of nodes in the network these cycles are from.
	 * 
	 * @return The length of the longest cycle in this structure
	 */
	public int getMaxCycleLength() {
		return size + 2;
	}
	
	/**
	 * Returns an iterator over all cycles of length <code>cycleLength</code>
	 * stored in this object.
	 * 
	 * @param cycleLength The length of cycle desired.
	 * @return An iterator over all cycles in this structure of length 
	 * 			<code>cycleLength</code>
	 */
	public Iterator<Cycle> iterator(int cycleLength) {
		return cycles.get(cycleLength - 2).iterator();
	}
	
	/**
	 * Gets all cycles in this structure of length <code>cycleLength</code>
	 * and returns them as an array of Cycles.
	 * 
	 * @param cycleLength The length of cycle desired
	 * @return An array of all Cycle objects in this structure of the given length
	 */
	public Cycle[] getCycles(int cycleLength) {
		Cycle[] output = new Cycle[0];
		output = cycles.get(cycleLength - 2).toArray(output);
		return output; 
	}
	
	/**
	 * Returns an iterator over all cycles in this structure.
	 * 
	 * @return An iterator over all cycles in this structure.
	 */
	public Iterator<Cycle> iterator() {
		return new AllCycleIterator(this);
	}
	
	/**
	 * Returns a human-readable list of all cycles in this structure.
	 * 
	 * @return A String list of all cycles in this structure.
	 */
	@Override
	public String toString() {
		String s = "";
		String endln = System.getProperty("line.separator");
		for (Cycle c : this)
			s += c + endln;
		return s;
	}
	
	/**
	 * This main method is only used for testing this class
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		AllCycles c = new AllCycles(9);
		System.out.println(c);
		c = new AllCycles(5);
		System.out.println(c);
	}
	
	/**
	 * Iterator for AllCycle objects. This iterator steps over
	 * all cycles in a complete directed network. It returns the cycles
	 * in non-decreasing order of length, but the order of the cycles
	 * of the same length is random. 
	 *
	 * @author Brad Paynter
	 * @version Nov 2, 2009
	 *
	 */
	private class AllCycleIterator implements Iterator<Cycle> {
		/**
		 * An iterator over the ArrayLists of cycles
		 */
		private Iterator<ArrayList<Cycle>> arrayIterator;
		/**
		 * An iterator over the cycles of the current length
		 */
		private Iterator<Cycle> currentIterator;
		
		/**
		 * Constructs an iterator over all cycles in the AllCycles structure.
		 * This iterator will iterate over all cycles of lenth 2, then all
		 * cycles of length 3 and so on.
		 *  
		 * @param me The AllCycles structure to iterate over.
		 */
		public AllCycleIterator(AllCycles me) {
			arrayIterator = me.cycles.iterator();
			if (this.arrayIterator.hasNext())
				currentIterator = this.arrayIterator.next().iterator();
			else
				currentIterator = null;
		}
		
		/**
		 * Determines whether this iterator has any more cycles to return  
		 * 
		 * @return <code>false</code> if this iterator has run out of cycles,
		 * 			<code>true</code> else.
		 */
		public boolean hasNext() {
			if (currentIterator != null) {
				if (currentIterator.hasNext())
					return true;
				else if (arrayIterator.hasNext()) {
					currentIterator = arrayIterator.next().iterator();
					return this.hasNext();
				} else
					return false;
			} else
				return false;
		}

		/**
		 * Returns the next cycle and advances the iterator.
		 * 
		 * @return The next cycle
		 */
		public Cycle next() {
			if (this.hasNext())
				return currentIterator.next();
			else
				return null;
		}

		/**
		 * This method is not supported by this implementation of <code>Iterator</code>. 
		 * If called this method will throw an UnsupportedOperationException.
		 * 
		 * @throws UnsupportedOperationException Always thrown since this operation is not
		 * 			implemented in this iterator
		 */
		public void remove() {
			throw new 
				UnsupportedOperationException("Remove is not supported for this iterator.");
		}
		
	}
}
