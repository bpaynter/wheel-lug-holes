package networks;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import util.Permutation;
import util.TSPInstance;


/**
 * This class represents a network (directed graph). It has methods for adding
 * and removing arcs, for converting to and from various network representations
 * and methods for finding single source/sink shortest paths in the network.
 *
 * @author Brad Paynter
 * @version Jun 10, 2010
 *
 */
public class Network implements Iterable<SimpleArc>, CycleFinder, TSPInstance {
	/**
	 * The number of nodes in the network
	 */
    private int size;
    /**
     * The nodes in the network
     */
    private Node[] nodes;
    /**
     * 
     */
    private SimpleArc[] negativeCycle;
    
    /**
     * Constructs a network with no arcs of size <code>size</code>
     * 
     * @param size The number of nodes that the network will contain.
     */
    public Network(int size) {
        this.size = size;
        this.nodes = new Node[size];
        for (int i = 0; i < size; i++) {
            nodes[i] = new Node(i);
        }
        negativeCycle = null;
    }
    
    /**
     * Clears all existing arcs and generates a complete network
     */
    public void generateCompleteArcs() {
    	deleteAllArcs();
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (i != j)
                    addArc(new Arc(nodes[i], nodes[j]));
            }
        }
    }
    
    /**
     * Clears all existing arcs from the network and generates new ones based
     * on the given adjacency matrix representation of the network. For each
     * M[i][j] in the matrix, the arc (i,j) is created in the network with weight
     * equal to the M[i][j] entry value. If the entry is zero then no arc will be
     * created. 
     * 
     * @param weights An adjacency matrix representation of a network
     * @throws IllegalArgumentException Thrown if <code>weights</code> is the wrong size
     */
    public void generateArcs(double[][] weights) throws IllegalArgumentException {
    	int[] permutation = new int[size];
		for (int i = 0; i < size; i++)
			permutation[i] = i;
		generateArcs(weights, permutation);
    }
    
    
    /**
     * 
     * TODO
     * 
     * @param weights
     * @param permutation
     * @throws IllegalArgumentException
     */
    public void generateArcs(double[][] weights, int[] permutation) throws IllegalArgumentException {
    	if (permutation.length != size)
    		throw new IllegalArgumentException("Permutation is incorrect size.");
    	if (!Permutation.correctPermutation(permutation))
    		throw new IllegalArgumentException("Invalid Permutation.");
    	deleteAllArcs();
    	for (int from = 0; from < weights.length; from++) {
    		if (from >= size)
    			throw new IllegalArgumentException("Matrix is too big");
    		for (int to = 0; to < weights[permutation[from]].length; to++) {
    			if (to >= size)
    				throw new IllegalArgumentException("Matrix is too big");
    			if (to != from)
    				addArc(new Arc(nodes[permutation[from]], nodes[permutation[to]], weights[permutation[from]][permutation[to]]));
    		}
    	}
    }
    
    /**
     * Deletes all arcs in the network
     */
    public void deleteAllArcs() {
    	negativeCycle = null;
    	for (Node n : nodes) {
    		Arc a = n.firstFromArc;
    		while (a != null) {
    			Arc next = a.nextFromArc;
    			a.clear();
    			a = next;
    		}
    		n.firstFromArc = null;
    		n.firstToArc = null;
    	}
    }
    
    /**
     * Outputs the current node distance labels. These will only have meaningful
     * values after a shortest path algorithm has been run.
     * 
     * @return An array of the current node distance labels
     */
    public double[] getDistanceLabels() {
    	double[] dist = new double[size];
    	for (int i = 0; i < size; i++)
    		dist[i] = nodes[i].dist;
    	return dist;
    }
    
    /**
     * Sets the arc weights of the network to the values given in an
     * adjacency matrix representation of the network. Note that this
     * only sets the weight for arcs already in the network. If you have not
     * yet created the arcs, use the <code>generateArcs(double[][])</code> method
     * instead.
     * 
     * @param weights An Adjacency Matrix representation of the network
     * @throws IllegalArgumentException Thrown if <code>weights</code> is not the correct size
     */
    public void setArcWeights(double[][] weights) throws IllegalArgumentException {
    	for (Node from : nodes) {
    		for (Arc arc : from) {
    			try {
    				arc.weight = weights[from.number][arc.toNode.number];
    			} catch (ArrayIndexOutOfBoundsException e) {
    				throw new IllegalArgumentException("Array is the wrong size: " + e);
    			}
    		}
    	}
    }
    
    /**
     * 
     * TODO
     * 
     * @param i
     * @param j
     * @param w
     */
    public void setArcWeight(int i, int j, double w) throws IllegalArgumentException {
    	if ((i < 0) || (i >= size))
    		throw new IllegalArgumentException("Node index out of bounds: i=" + i);
    	if ((j < 0) || (j >= size))
    		throw new IllegalArgumentException("Node index out of bounds: j=" + j);
    	boolean found = false;
    	Iterator<Arc> iterator = nodes[i].fromIterator();
    	while (!found && iterator.hasNext()) {
    		Arc a = iterator.next();
    		if (a.toNode.number == j) {
    			a.weight = w;
    			found = true;
    		}
    	}
    	if (!found)
    		addArc(new Arc(nodes[i], nodes[j], w));
    }
    
    /**
     * 
     * TODO
     * 
     * @param i
     * @param j
     * @throws IllegalArgumentException
     */
    public void deleteArc(int i, int j) throws IllegalArgumentException {
    	if ((i < 0) || (i >= size))
    		throw new IllegalArgumentException("Node index out of bounds: i=" + i);
    	if ((j < 0) || (j >= size))
    		throw new IllegalArgumentException("Node index out of bounds: j=" + j);
    	boolean found = false;
    	Arc a = nodes[i].firstFromArc;
    	Arc b = nodes[i].firstFromArc;
    	if (a.toNode.number == j) {
    		nodes[i].firstFromArc = a.nextFromArc;
    		found = true;
    	} else
    		a = a.nextFromArc;
    	while (!found && (a != null)) {
    		if (a.toNode.number == j) {
    			b.nextFromArc = a.nextFromArc;
    			found = true;
    		}
    	}
    	found = false;
    	a = nodes[j].firstToArc;
    	b = nodes[j].firstToArc;
    	if (a.fromNode.number == i) {
    		nodes[j].firstToArc = a.nextToArc;
    		found = true;
    	} else
    		a = a.nextToArc;
    	while (!found && (a != null)) {
    		if (a.fromNode.number == i) {
    			b.nextToArc = a.nextToArc;
    			found = true;
    		}
    	}
    }
   
    /**
     * Finds the shortest path from the given root node to each node in the network.
     * This method uses a FIFO algorithm to find shortest paths and checks for 
     * negative cycles.
     * 
     * @param rootNode The index of the node to find the shortest paths from.
     * @return <code>true</code> if valid shortest path distances were found,
     * 			<code>false</code> if the network contains a negative cycle.
     * @throws IllegalArgumentException Thrown if the <code>rootNode</code> given is
     * 			invalid 
     */
    public boolean findShortestPathsFrom(int rootNode) throws IllegalArgumentException {
    	// Ensure that the root node index is valid
    	if ((rootNode < 0) || (rootNode > size - 1))
    		throw new IllegalArgumentException("Root must be a node in the network.");
    	// Initialize the distance labels and predecessor information
    	for (Node n : nodes) {
    		n.dist = Double.POSITIVE_INFINITY;
    		n.pred = n;
    	}
    	// Set the root distance label to zero
    	nodes[rootNode].dist = 0;
    	// Initialize a queue for nodes to be processed
    	LinkedList<Node> queue = new LinkedList<Node>();
    	// Add the root node to the queue
    	queue.add(nodes[rootNode]);
    	// Set the negative cycle flag to false
    	boolean negCycle = false;
    	// While there are still nodes to be processed and a negative cycle has
    	// not been found
    	while ((queue.size() > 0) && (!negCycle)) {
    		// Get the next node from the queue
    		Node currentNode = queue.remove();
    		// For each arc leaving the current Node
    		for (Arc a : currentNode) {
    			// Check the shortest path optimality conditions
    			if (currentNode.dist + a.getWeight() < a.head().dist) {
    				// If they are violated update the distance label
    				a.head().dist = currentNode.dist + a.getWeight();
    				// Update the predecessor information
    				a.head().pred = currentNode;
    				// Add the updated node to the queue to be processed
    				if (!queue.contains(a.head()))
    					queue.add(a.head());
    			}
    		}
    		// Check for negative cycle
			Node cycleCheckNode = currentNode.pred;
    		// Follow the predecessor path until we cycle or reach the root node
			while ((cycleCheckNode != currentNode) 
						&& (cycleCheckNode != nodes[rootNode]))
				cycleCheckNode = cycleCheckNode.pred;
			// If we cycled then set the negative cyclc flag
			if (cycleCheckNode == nodes[rootNode]) {
				if (nodes[rootNode].pred != nodes[rootNode])
					negCycle = true;
			} else if (cycleCheckNode == currentNode)
				negCycle = true;
    	}
    	// Return the negative cycle flag
    	return !negCycle;
    }
    
    /**
     * Finds the shortest path from each node in the network to the given root node.
     * This method uses a FIFO algorithm to find shortest paths and checks for 
     * negative cycles.
     * 
     * @param rootNode The index of the node to find the shortest paths to.
     * @return <code>true</code> if valid shortest path distances were found,
     * 			<code>false</code> if the network contains a negative cycle.
     * @throws IllegalArgumentException Thrown if the <code>rootNode</code> given is
     * 			invalid 
     */
    public boolean findShortestPathsTo(int rootNode) throws IllegalArgumentException {
    	// Ensure that the root node index is valid
    	if ((rootNode < 0) || (rootNode > size - 1))
    		throw new IllegalArgumentException("Root must be a node in the network.");
    	// Initialize the distance labels and predecessor information
    	for (Node n : nodes) {
    		n.dist = Double.POSITIVE_INFINITY;
    		n.pred = n;
    	}
    	// Set the root distance label to zero
    	nodes[rootNode].dist = 0;
    	// Initialize a queue for nodes to be processed
    	LinkedList<Node> queue = new LinkedList<Node>();
    	// Add the root node to the queue
    	queue.add(nodes[rootNode]);
    	// Set the negative cycle flag to false
    	boolean negCycle = false;
    	// While there are still nodes to be processed and a negative cycle has
    	// not been found
    	while ((queue.size() > 0) && (!negCycle)) {
    		// Get the next node from the queue
    		Node currentNode = queue.remove();
    		// Iterate over the arcs TO the current node
    		for (Iterator<Arc> toIterator = currentNode.toIterator(); toIterator.hasNext(); ) {
    			// Get the next arc
    			Arc a = toIterator.next();
    			// Check the shortest path optimality constraints
    			if (currentNode.dist + a.getWeight() < a.tail().dist) {
    				// If they are violated, update the distance label
    				a.tail().dist = currentNode.dist + a.getWeight();
    				// Update the predecessor information
    				a.tail().pred = currentNode;
    				// Add the updated node to the queue
    				if (!queue.contains(a.tail()))
    					queue.add(a.tail());
    			}
    		}
    		int negCycleLength = 1;
    		// Check for negative cycles
			Node cycleCheckNode = currentNode.pred;
			// Follow the predecessor path until we cycle or reach the root
			while ((cycleCheckNode != currentNode) && (cycleCheckNode != nodes[rootNode])) {
				negCycleLength++;
				cycleCheckNode = cycleCheckNode.pred;
			}
			// If we cycled then we found a negative cycle so set the flag
			if (cycleCheckNode == nodes[rootNode]) {
				if (nodes[rootNode].pred != nodes[rootNode]) {
					negCycle = true;
					negCycleLength++;
				}
			} else if (cycleCheckNode == currentNode)
				negCycle = true;
			if (negCycle) {
				negativeCycle = new SimpleArc[negCycleLength];
				negCycleLength = 0;
				negativeCycle[0] = new SimpleArc(currentNode.number, currentNode.pred.number);
				cycleCheckNode = currentNode.pred;
				// Follow the predecessor path until we cycle or reach the root
				while ((cycleCheckNode != currentNode) && (cycleCheckNode != nodes[rootNode])) {
					negCycleLength++;
					negativeCycle[negCycleLength] = new SimpleArc(cycleCheckNode.number, cycleCheckNode.pred.number);
					cycleCheckNode = cycleCheckNode.pred;
				}
				if (cycleCheckNode == nodes[rootNode])
					negativeCycle[negCycleLength+1] = new SimpleArc(cycleCheckNode.number, cycleCheckNode.pred.number);
			}
    	}
    	// Return the negative cycle flag
    	return !negCycle;
    }
    
    /**
     * Adds a weighted arc in this network from node <code>i</code> to node 
     * <code>j</code> of weight <code>weight</code>
     * 
     * @param i The index of the tail node of the new arc
     * @param j The index of the head node of the new arc
     * @param weight The weight of the new arc
     * @throws IllegalArgumentException Thrown if either index is negative or larger
     * 			than the number of nodes in the network.
     */
    public void addArc(int i, int j, double weight) throws IllegalArgumentException {
    	if ((i < 0) || (i >= this.size) || (j < 0) || (j >= this.size))
    		throw new IllegalArgumentException("Invalid Node number");
    	else
    		addArc(new Arc(nodes[i], nodes[j], weight));
    }
    
    /**
     * Adds an unweighted arc in this network from node <code>i</code> to node 
     * <code>j</code>.
     * 
     * @param i The index of the tail node of the new arc
     * @param j The index of the head node of the new arc
     * @throws IllegalArgumentException Thrown if either index is negative or larger
     * 			than the number of nodes in the network.
     */
    public void addArc(int i, int j) throws IllegalArgumentException {
    	if ((i < 0) || (i >= this.size) || (j < 0) || (j >= this.size))
    		throw new IllegalArgumentException("Invalid Node number");
    	else
    		addArc(new Arc(nodes[i], nodes[j]));
    }
    
    /**
     * Adds an arc to a network. It does this given an existing Arc object. </br>
     * This method will throw an exception if the Arc does not already know 
     * its head or tail node
     * 
     * @param a An arc to be added to a network
     * @throws IllegalArgumentException Thrown if the Arc passed has a null
     * 				head or tail node
     */ 
    private void addArc(Arc a) throws IllegalArgumentException {
    	if ((a.head() == null) || (a.tail() == null))
    		throw new IllegalArgumentException("This arc does not contain enough nodes.");
	    a.setNextFrom(a.tail().firstFromArc);
        a.tail().firstFromArc = a;
        a.setNextTo(a.head().firstToArc);
        a.head().firstToArc = a;
    }
    
    /**
     * Outputs an adjacency matrix representation of this network.
     * i.e. Outputs a matrix M, such that M[i][j] is the weight of
     * the arc from node i to node j. M[i][j] is zero if no such
     * arc exists
     * 
     * @return An adjacency matrix representation of this network.
     */
    public double[][] outputAdjacencyMatrix() {
    	double[][] weights = new double[size][size];
    	for (Node n : nodes) {
    		Arrays.fill(weights[n.number], 0);
    		for (Arc a : n)
    			weights[n.number][a.toNode.number] = a.weight;
    	}
    	return weights;
    }
    
    
    
    /**
     * Gives the number of nodes in this network
     * 
     * @return The number of nodes in this network
     */
    public int getSize() {
    	return size;
    }
    
    /**
	 * Returns an iterator over this network
	 * 
	 * @return An iterator for this network
	 */
	public Iterator<SimpleArc> iterator() {
		return new NetworkIterator(this);
	}
	
	/**
	 * Not Implemented
	 * 
	 * @param set
	 */
	public void verbose(boolean set) {
		// Not Implemented
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param weights
	 * @param permutation
	 * @return
	 */
	public SimpleArc[] getNegativeCycle(double[][] weights, int[] permutation) {
		generateArcs(weights, permutation);
		findShortestPathsTo(permutation[size - 1]);
		if (negativeCycle == null)
			return null;
		int[] inversePerm = Permutation.inversePerm(permutation);
		SimpleArc[] outputCycle = new SimpleArc[negativeCycle.length];
		for (int i = 0; i < negativeCycle.length; i++)
			outputCycle[i] = new SimpleArc(inversePerm[negativeCycle[i].from], inversePerm[negativeCycle[i].to]);
		return outputCycle;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param weights
	 * @return
	 */
	public SimpleArc[] getNegativeCycle(double[][] weights) {
		generateArcs(weights);
		findShortestPathsTo(size - 1);
		return negativeCycle;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param node
	 * @return
	 */
	protected Iterator<SimpleArc> nodeIterator(int node) {
		return new NodeSimpleArcIterator(nodes[node]);
	}
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public int numCities() {
		return size;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public double getWeight(int i, int j) {
		for (Arc a : nodes[i])
			if (a.toNode.number == j)
				return a.weight;
		return Double.POSITIVE_INFINITY;
	}

	/**
	 * TODO
	 * 
	 * @param cityOne
	 * @param cityTwo
	 * @return
	 */
	public double distance(int cityOne, int cityTwo) {
		return getWeight(cityOne, cityTwo);
	}
	
	/**
	 * Returns a human-readable string representation of this network.
	 * This representation is in edge-list form i.e. "(i,j): weight"
	 * 
	 * @return A string representation of the network
	 */
	@Override
	public String toString() {
		String endln = System.getProperty("line.separator");
		String s = "";
		for (Node from : nodes)
			for (Arc arc : from)
				s += "(" + from.number + ", " + arc.toNode.number + "): " 
							+ arc.weight + endln;
		return s;
	}
    
    /**
     * This main method is used only for testing this class
     * 
     * @param args Not used
     */
    public static void main(String[] args) {
    	/*
    	Pattern[] patterns = new Pattern[3];
    	patterns[0] = new Pattern(40, 3, 1);
    	patterns[1] = new Pattern(40, 4, 1);
    	patterns[2] = new Pattern(40, 5, 1);
    	Network net = new Network(patterns);
    	System.out.println("Zero Ks? " + net.zeroKs());
    	Ks k = new Ks(3);
    	k.set(0,1,0);
    	k.set(0, 2, 1);
    	k.set(1, 2, 1);
    	boolean NegCycle = net.findShortestPaths(k);
    	System.out.println(NegCycle);
    	for (int i = 0; i < 3; i++) 
    		System.out.println(net.nodes[i].dist + "; ");
    	
    	patterns = new Pattern[4];
    	
    	Permutation perm = new Permutation(4);
    	net = new Network(patterns);
    	k = new Ks(4);
    	net.generateWeights(k);
    	System.out.println(perm + "-Zero Ks? " + net.zeroKs());
    	while (perm.nextPerm()) {
    		Pattern[] temp = new Pattern[4];
    		int[] currentPerm = perm.getPerm();
    		for (int i = 0; i < currentPerm.length; i++) {
    			temp[i] = patterns[currentPerm[i]];
    			System.out.print(temp[i].numberOfHoles + "-");
    		}
	    	net = new Network(temp);
	    	System.out.println("Zero Ks? " + net.zeroKs());
    	}
    	*/
    }
    
    /**
     * Iterator for Networks. This iterator steps over the arcs in the
     * given network, outputting them as SimpleArcs, an (i,j) pair and
     * the edge weight.
     *
     * @author Brad Paynter
     * @version Jun 14, 2010
     *
     */
    private class NetworkIterator implements Iterator<SimpleArc> {
    	/**
    	 * The network that this iterator is over
    	 */
    	private Network myNetwork;
    	/**
    	 * The node that the next arc will come from
    	 */
    	private int currentNode;
    	/**
    	 * The next arc that this iterator will output
    	 */
    	private Arc currentArc;

    	/**
    	 * Constructs an Iterator over the arcs of the given network
    	 * 
    	 * @param network The network to be iterated over
    	 */
    	public NetworkIterator(Network network) {
    		this.myNetwork = network;
    		currentNode = 0;
    		currentArc = myNetwork.nodes[currentNode].firstFromArc;
    		// Find a node with an outgoing arc
    		while ((currentArc == null) && (currentNode < myNetwork.size - 1)) {
    			currentNode++;
    			currentArc = myNetwork.nodes[currentNode].firstFromArc;
    		}
    	}
    	
		/**
		 * Determines whether this iterator has any more arcs to return
		 * 
		 * @return <code>true</code> if there are more arcs to return, <code>
		 * 			false</code> else. 
		 */
		public boolean hasNext() {
			 return (currentArc != null) ? true : false;
		}

		/**
		 * Returns the next arc in the network and moves the iterator to
		 * the next arc. Returns <code>null</code> if there are no more arcs
		 * to return
		 * 
		 * @return The next arc in the network
		 */
		public SimpleArc next() {
			if (hasNext()) {
				// Store the current arc
				SimpleArc next = new SimpleArc(currentNode, 
										currentArc.toNode.number, currentArc.weight);
				// Move to the next arc from the currentNode
				currentArc = currentArc.nextFromArc;
				// Ensure that there is another arc from this node
				while ((currentArc == null) && (currentNode < myNetwork.size - 1)) {
					// If not, move to the next node
	    			currentNode++;
	    			// Grab the first arc from this new node
	    			currentArc = myNetwork.nodes[currentNode].firstFromArc;
	    		}
				return next;
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
            throw new 
            	UnsupportedOperationException("remove() is not supported by this interator");
        }
    	
    }
    
    private class NodeSimpleArcIterator implements Iterator<SimpleArc> {
    	private Iterator<Arc> arcIterator;
    	
    	public NodeSimpleArcIterator(Node n) {
    		this.arcIterator = n.iterator();
    	}

		/**
		 * TODO
		 * 
		 * @return
		 */
		public boolean hasNext() {
			return arcIterator.hasNext();
		}

		/**
		 * TODO
		 * 
		 * @return
		 */
		public SimpleArc next() {
			Arc a = arcIterator.next();
			return new SimpleArc(a.fromNode.number, a.toNode.number, a.weight);
		}

		/**
		 * TODO
		 * 
		 */
		public void remove() {
			arcIterator.remove();
		}
    	
    }
    
    /**
     * This class represents a node in an Adjacency list representation
     * of a network. It contains the index of this node and pointers to 
     * linked lists containing the arcs from this node and the arcs to 
     * this node. It also contains a distance label and a predecessor 
     * pointer (to be used by shortest path algorithms).
     *
     * @author Brad Paynter
     * @version Jun 10, 2010
     *
     */
    private class Node implements Iterable<Arc> {
    	/**
    	 * The first arc in the linked list of arcs from this node
    	 */
        private Arc firstFromArc;
        /**
         * The first arc in the linked list of arcs to this node
         */
        private Arc firstToArc;
        /**
         * The index of this node
         */
        private int number;
        /**
         * The distance label to be used by shortest path algorithms
         */
        public double dist;
        /**
         * The predecessor pointer to be used by shortest path algorithms
         */
        public Node pred;

        /**
         * Constructs a node with no arcs of index <code>n</code>.
         * 
         * @param n The index of the new node
         */
        public Node(int n) {
            this.number = n;
            this.firstFromArc = null;
            this.firstToArc = null;
            this.dist = 0;
            this.pred = this;
        }
        
        /**
         * Returns an iterator over the arcs from this node
         * 
         * @return An iterator over the arcs from this node
         */
        public Iterator<Arc> iterator() {
            return this.fromIterator();
        }
        
        /**
         * Returns an iterator over the arcs from this node
         * 
         * @return An iterator over the arcs from this node
         */
        public Iterator<Arc> fromIterator() {
        	return new NodeIterator(this, true);
        }
        
        /**
         * Returns an iterator over the arcs to this node
         * 
         * @return An iterator over the arcs to this node
         */
        public Iterator<Arc> toIterator() {
        	return new NodeIterator(this, false);
        }
        
        /**
         * Iterator for Nodes. It iterates along the linked list of arcs.
         * It will either return the list of arcs from the given node, or
         * the list of arcs to the given node. This behavior is governed
         * by the <code>from</code> flag.
         *
         * @author Brad Paynter
         * @version Sep 21, 2009
         *
         */
        private class NodeIterator implements Iterator<Arc> {
        	/**
        	 * The arc that this iterator will return next
        	 */
            private Arc currentArc;
            /**
             * The set of arcs that this iterator is over. If <code>true</code>
             * then this iterator will be over the arcs from a node, if <code>false</code>
             * then it will iterate over the arcs to a node.
             */
            private boolean from;
            
            /**
             * Constructs an iterator over the arcs in node <code>n</code>.
             * If <code>from</code> is set to <code>true</code>
             * then this iterator will be over the arcs from node <code>n</code>, 
             * if <code>false</code> then it will iterate over the arcs 
             * to node <code>n</code>.
             * 
             * @param n The node to get the arcs from
             * @param from Sets whether this iterator is over the from or to
             * 			arcs of this node
             */
            public NodeIterator(Node n, boolean from) {
            	this.from = from;
            	this.currentArc = (from) ? n.firstFromArc : n.firstToArc;
            }
            
            /**
             * Determines whether this iterator has any more arcs to return
             * 
             * @return <code>true</code> if this iterator has more arcs to return,
             * 			<code>false</code> if this iterator has reached the end of
             * 			the linked list.
             */
            public boolean hasNext() {
                return (currentArc == null) ? false : true;
            }
            
            /**
             * Returns the next arc in the linked list and moves the iterator 
             * one position.
             * 
             * @return The next arc in the linked list
             */
            public Arc next() {
                Arc a = currentArc;
                currentArc = (from) ? currentArc.nextFrom() : currentArc.nextTo();
                return a;
            }
            
            /**
             * This operation is not supported by this iterator and this method
             * will throw an UnsupportedOperationException if invoked.
             *
             * @throws UnsupportedOperationException Always thrown, since this
             *			method is not implemented for this iterator
             */
            public void remove() throws UnsupportedOperationException {
                throw(new UnsupportedOperationException("remove() is not supported for Node"));
            }
        }

    }
    
    /**
     * This class represents a directed arc used in an adjacency list format. It
     * contains the tail and head nodes of the arc, the weight of the arc, and
     * pointers to the next arc in the linked list of arcs from a node and the next
     * arc in the linked list of arcs to a node.
     *
     * @author Brad Paynter
     * @version Jun 10, 2010
     *
     */
    private class Arc {
    	/**
    	 * The tail node of the arc
    	 */
    	private Node fromNode;
    	/**
    	 * The head node of the arc
    	 */
        private Node toNode;
        /**
         * The weight of the arc
         */
        private double weight;
        /**
         * The next arc in the linked list of arcs from a node
         */
        private Arc nextFromArc;
        /**
         * The next arc in the linked list of arcs to a node
         */
        private Arc nextToArc;
        
        /**
         * Default constructor for a weighted arc, given all information.
         * 
         * @param from The tail node of the arc
         * @param to The head node of the arc
         * @param weight The weight of the arc
         * @param nextFrom The next arc in the list of arcs from a node
         * @param nextTo The next arc in the list of arcs to a node
         */
        public Arc(Node from, Node to, double weight, Arc nextFrom, Arc nextTo) {
        	this.fromNode = from;
            this.toNode = to;
            this.weight = weight;
            this.nextFromArc = nextFrom;
            this.nextToArc = nextTo;
        }
        
        /**
         * Constructs a weighted arc with no next arc
         * 
         * @param from The tail node of this arc
         * @param to The head node of this arc
         * @param weight The weight of this arc
         */
        public Arc(Node from, Node to, double weight) {
        	this(from, to, weight, null, null);
        }
        
        /**
         * Constructs an unweighted arc (weight 0) with no next arc
         * 
         * @param from The tail node of this arc
         * @param to The head node of this arc
         */
        public Arc(Node from, Node to) {
            this(from, to, 0, null, null);
        }
        
        /**
         * Sets the next arc pointer for the linked list of arcs from a node
         * 
         * @param a The arc to be set as the pointer
         */
        public void setNextFrom(Arc a) {
            this.nextFromArc = a;
        }
        
        /**
         * Sets the next arc pointer for the linked list of arcs from a node
         * 
         * @param a The arc to be set as the pointer
         */
        public void setNextTo(Arc a) {
        	this.nextToArc = a;
        }
        
        /**
         * Gets the next arc from the tail node of this arc
         * 
         * @return The next arc from this arc's tail node
         */
        public Arc nextFrom() {
            return nextFromArc;
        }
        
        /**
         * Gets the next arc to the head node of this arc
         * 
         * @return The next arc to this arc's head node
         */
        public Arc nextTo() {
            return nextToArc;
        }
        
        /**
         * Clears all data in the arc
         */
        public void clear() {
        	fromNode = null;
        	toNode = null;
        	nextFromArc = null;
        	nextToArc = null;
        }
        
        /**
         * Gets the weight of this arc
         * 
         * @return The weight of this arc
         */
        public double getWeight() {
            return this.weight;
        }
        
        /**
         * Gets the head node of this arc
         * 
         * @return The head node of this arc
         */
        public Node head() {
            return toNode;
        }
        
        /**
         * Gets the tail node of this arc
         * 
         * @return The tail node of this arc
         */
        public Node tail() {
        	return fromNode;
        }
        
    }

	
    
}
