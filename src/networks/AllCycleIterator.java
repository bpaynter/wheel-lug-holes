/**
 * AllCycleIterator.java
 * Jul 27, 2010
 */
package networks;

import java.util.Iterator;

/**
 * This class is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * The class is an iterator over all directed cycles in a complete network. It does this
 * recursively and on-the-fly as opposed to the AllCycles class which precomputes and 
 * stores all the cycles needed.<br />
 * The iterator operates as follows:<br />
 * Create all 2-cycles involving the nth node<br />
 * Take each cycle on n-1 nodes (from a recursive iterator)<br />
 * Output the cycle as is<br />
 * Output the cycle with the nth node inserted in each possible position in the cycle
 *
 * @author Brad Paynter
 * @version Jul 27, 2010
 *
 */
public class AllCycleIterator implements Iterator<Cycle> {
	/**
	 * The number of nodes in the largest cycle (n)
	 */
	private int size;
	/**
	 * The current cycle of length n-1
	 */
	private int[] currentCycle;
	/**
	 * An iterator over all directed cycles on n-1 nodes
	 */
	private AllCycleIterator iterator;
	/**
	 * The position in <code>currentCycle</code> 
	 * where we need to place node n 
	 */
	private int position;
	
	/**
	 * Constructs an iterator over all directed cycles in an network of
	 * <code>numNodes</code> nodes
	 * 
	 * @param numNodes The number of nodes in the network
	 */
	public AllCycleIterator(int numNodes) {
		size = numNodes;
		if (numNodes < 2) {
			currentCycle = null;
			iterator = null;
		} else {
			currentCycle = new int[1];
			currentCycle[0] = 0;
			position = 0;
			iterator = new AllCycleIterator(size - 1);
		}
	}

	/**
	 * Determines whether this iterator has any more cycles
	 * to return
	 * 
	 * @return <code>true</code> if this iterator has more cycles to return,
	 * 			<code>false</code> else
	 */
	public boolean hasNext() {
		if (currentCycle == null)
			return false;
		else
			return true;
	}

	/**
	 * Returns the next cycle in this iterator
	 * 
	 * @return The next cycle in this iterator, <code>null</code> if no
	 * 			more remain
	 */
	public Cycle next() {
		// Set a place to store the outgoing cycle
		int[] outCycle;
		// If the current cycle is null, then we're done iterating
		if (currentCycle == null)
			return null;
		// If the current cycle is a singleton, then we need to create 
		// the 2-cycle 
		else if (currentCycle.length == 1) {
			outCycle = new int[2];
			outCycle[0] = currentCycle[0];
			outCycle[1] = size - 1;
			// Add one to the first node
			currentCycle[0]++;
			// If we have created all 2-cycles we get the cycles
			// from the iterator on n-1 nodes
			if (currentCycle[0] >= size - 1) {
				Cycle nextCycle = iterator.next();
				currentCycle = (nextCycle != null) ? nextCycle.getCycle() : null;
			}
		// If we don't have a 2-cycle, then we output the cycle we have
		} else if (position == 0) {
			outCycle = currentCycle.clone();
			position = 1;
		// Else, we've already output the cycle as is,
		// so let's modify it by adding the nth node
		// in each possible position in the cycle
		} else {
			// Create space for the cycle
			outCycle = new int[currentCycle.length + 1];
			// Place nodes 0 through position - 1 from the original cycle
			for (int i = 0; i < position; i++)
				outCycle[i] = currentCycle[i];
			// Place the nth node at position
			outCycle[position] = size - 1;
			// Place nodes position on from the original cycle
			// shifted down one spot
			for (int i = position; i < currentCycle.length; i++)
				outCycle[i + 1] = currentCycle[i];
			// Advance the position for the next cycle
			position++;
			// If we've done everything we can with this cycle, get the next one
			if (position > currentCycle.length) {
				Cycle nextCycle = iterator.next();
				currentCycle = (nextCycle != null) ? nextCycle.getCycle() : null;
				position = 0;
			}
		}
		// Output the cycle
		return new Cycle(outCycle);
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
	
	/**
	 * This method is only used for testing this class
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		for (Iterator<Cycle> iterator = new AllCycleIterator(5); iterator.hasNext(); )
			System.out.println(iterator.next());
	}

}
