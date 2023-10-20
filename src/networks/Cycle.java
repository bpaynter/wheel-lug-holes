/**
 * Cycle.java
 * Nov 2, 2009
 */
package networks;

import java.util.Iterator;


/**
 * This class is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * It is a simple representation of a directed cycle in a network. It stores the cycle
 * as a simple list of node indices. The class contains an iterator which will return
 * the simple (i, j) representations of the arcs in the cycle in SimpleArc form.
 *
 * @author Brad Paynter
 * @version Nov 2, 2009
 *
 */
public class Cycle implements Iterable<SimpleArc> {
	/**
	 * An array of the node indices in the cycle
	 */
	private int[] cycle;

	/**
	 * Constructor, given the array representation of the cycle.
	 * 
	 * @param cycle An array of the node indices in the cycle
	 */
	public Cycle(int[] cycle) {
		this.cycle = cycle;
	}
	
	/**
	 * Returns the array representation of the cycle
	 * 
	 * @return The array representation of the cycle
	 */
	public int[] getCycle() {
		return cycle;
	}
	
	/**
	 * Returns the length of the cycle
	 * 
	 * @return The length of the cycle
	 */
	public int length() {
		return cycle.length;
	}
	
	/**
	 * Returns an iterator that moves through the cycle returning
	 * simple (i,j) arc representations of the cycle.
	 * 
	 * @return A SimpleArc Iterator over this cycle
	 */
	public Iterator<SimpleArc> iterator() {
		return new CycleIterator(cycle);
	}
	
	/**
	 * Returns a human readable version of this cycle. It is simply
	 * and ordered list of the node indices in the cycle.
	 * 
	 * @return A String representation of this cycle
	 */
	@Override public String toString() {
		String s = Integer.toString(cycle[0]);
		for (int i = 1; i < cycle.length; i++)
			s = s + "," + cycle[i];
		return s;
	}
	
	/**
	 * This method is used for testing this class
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		int[] cycle = {5, 3, 4, 1, 2, 0};
		Cycle me = new Cycle(cycle);
		for (SimpleArc a : me)
			System.out.println(a);
	}
	
	/**
	 * This class is the iterator for the Cycle class. It iterates over
	 * the cycle, returning the (i,j) edges it passes as it transits the
	 * cycle. 
	 *
	 * @author Brad Paynter
	 * @version Jun 14, 2010
	 *
	 */
	private class CycleIterator implements Iterator<SimpleArc> {
		/**
		 * The cycle that this iterator is over
		 */
    	private int[] cycle;
    	/**
    	 * The current position in
    	 */
    	private int position;
    	
      	/**
      	 * Creates an iterator over the given cycle
      	 * 
      	 * @param cycle An array containing the ordered indices of this cycle.
      	 */
    	public CycleIterator(int[] cycle) {
    		if (cycle.length > 1) {
	    		this.cycle = cycle;
	    		this.position = 0;
    		} else
    			throw new IllegalArgumentException("Null Cycle");
    	}
    	
		/**
		 * Checks if this cycle has any remaining arcs
		 * 
		 * @return <code>false</code> if the iterator is complete, <code>true</code> else 
		 */
		public boolean hasNext() {
			if (position >= cycle.length)
				return false;
			else
				return true;
		}

		/**
		 * Returns the next arc in the cycle and moves the iterator one step.
		 * 
		 * @return The next arc in the cycle
		 */
		public SimpleArc next() {
			if (this.hasNext()) {
				SimpleArc next = 
					new SimpleArc(cycle[position], cycle[(++position) % cycle.length]);
				return next;
			} else {
				return null;
			}
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
