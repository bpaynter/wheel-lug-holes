/**
 * SimpleArc.java
 * Nov 2, 2009
 */
package networks;

/**
 * This class is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * It represents a simple directed arc in a network, comprising simply of the indices
 * of the tail-node and the head-node of the arc. It also contains an optional arc weight.
 * This simple arc is used for Edge-List network representations.
 *
 * @author Brad Paynter
 * @version Nov 2, 2009
 *
 */
public class SimpleArc {
	/**
	 * The index of the tail-node of this arc
	 */
	public int from;
	/**
	 * The index of the head-node of this arc
	 */
	public int to;
	/**
	 * The weight of this arc
	 */
	public double weight;
	
	/**
	 * Constructor for arcs with a weight value
	 * 
	 * @param from The index of the tail-node of this arc
	 * @param to The index of the head-node of this arc
	 * @param weight The weight of this arc
	 */
	public SimpleArc(int from, int to, double weight) {
		this.from = from;
		this.to = to;
		this.weight = weight;
	}
	
	/**
	 * Constructor for arcs without a weight value. It constructs a simple
	 * arc with weight zero.
	 * 
	 * @param from The index of the tail-node of this arc
	 * @param to The index of the head-node of this arc
	 */
	public SimpleArc(int from, int to) {
		this(from, to, 0.0);
	}
	
	/**
	 * Creates a human-readable text version of this arc. It is formatted as
	 * (i, j): w_{ij} or
	 * (i, j) if the arc has no weight
	 * 
	 * @return A String version of this arc
	 */
	@Override public String toString() {
		String s = "(" + from + ", " + to + ")";
		if (weight != 0)
			s += ": " + weight;
		return s;
	}
}
