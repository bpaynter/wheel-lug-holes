/**
 * MinimumSpanningTree.java
 * Jan 22, 2012
 */
package networks;

import java.util.Arrays;
import java.util.Iterator;
import java.util.PriorityQueue;

import util.TSPSolver;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Jan 22, 2012
 *
 */
public class MinSpanningTree extends Network {
	
	public MinSpanningTree(double[][] weights) {
		super(weights.length);
		this.generateArcs(weights);
	}
	
	public SimpleArc[] prim() {
		PriorityQueue<HeapElement> queue = new PriorityQueue<HeapElement>();
		boolean[] inTree = new boolean[getSize()];
		Arrays.fill(inTree, false);
		HeapElement[] heapElements = new HeapElement[getSize() - 1];
		inTree[0] = true;
		for (int i = 1; i < getSize(); i++) {
			heapElements[i-1] = new HeapElement(i, 0, getWeight(0, i));
			queue.add(heapElements[i-1]);
		}
		while (!queue.isEmpty()) {
			HeapElement nextArc = queue.poll();
			inTree[nextArc.getIndex()] = true;
			Iterator<SimpleArc> iterator = nodeIterator(nextArc.getIndex());
			while (iterator.hasNext()) {
				SimpleArc a = iterator.next();
				if (!inTree[a.to] && a.weight < heapElements[a.to-1].getKey())  {
					queue.remove(heapElements[a.to-1]);
					heapElements[a.to-1].setTreeNode(nextArc.getIndex());
					heapElements[a.to-1].setKey(a.weight);
					queue.add(heapElements[a.to-1]);
				}
			}
		}
		return heapElements;
	}
	
	public static void main(String[] args) {
		String[] cities = 	{"Chicago", "Green Bay", "Minneapolis", "Indianapolis", "St Louis",
								"Atlanta", "New York", "Boston", "Miami", "Los Angeles", "Seattle",
								"Denver", "Houston", "Washington", "Pittsburgh", "New Orleans",
								"San Francisco", "Kansas City", "Phoenix", "Dallas"};
		
		double[][] locations = {{-87.6278, 		41.882},
								{-087.989750, 	44.521600},
								{-93.266667, 	44.983333},
								{-86.147685, 	39.790942},
								{-90.2, 		38.63}};
		
		/*
		double[][] locations = {{-87.6278, 		41.882},
								{-087.989750, 	44.521600},
								{-93.266667, 	44.983333},
								{-86.147685, 	39.790942},
								{-90.2, 		38.63},
								{-084.422592, 	33.762900},
								{-073.943849, 	40.669800},
								{-071.017892, 	42.336029},
								{-080.210845, 	25.775667},
								{-118.411201, 	34.112101},
								{-122.350326, 	47.621800},
								{-104.872655, 	39.768035},
								{-095.386728, 	29.768700},
								{-077.016167, 	38.905050},
								{-079.976702, 	40.439207},
								{-089.931355, 	30.065846},
								{-122.554783, 	37.793250},
								{-094.552009, 	39.122312},
								{-112.071399, 	33.542550},
								{-096.765249, 	32.794151}};
		*/
		double[][] weights = new double[locations.length][locations.length];
		for (int i = 0; i < weights.length; i++) {
			weights[i][i] = 0.0;
			for (int j = i+1; j < weights.length; j++) {
				double x = locations[i][0] - locations[j][0];
				double y = locations[i][1] - locations[j][1];
				weights[i][j] = Math.sqrt(x*x + y*y);
				weights[j][i] = weights[i][j];
			}
		}
		long start = System.nanoTime();
		MinSpanningTree t = new MinSpanningTree(weights);
		SimpleArc[] tree = t.prim();
		long end = System.nanoTime();
		System.out.println("Minimum Spanning Tree");
		System.out.println("---------------------");
		for (SimpleArc a : tree)
			System.out.println(cities[a.from] + " to " + cities[a.to]);
		System.out.println();
		System.out.println("Total time elapsed: " + (0.0 + end - start) / 1000000000 + "sec.");
		System.out.println("===================================================================================");
		System.out.println();
		start = System.nanoTime();
		TSPSolver tsp = new TSPSolver(t);
		int[] tour = tsp.bruteForceSolver(true);
		end = System.nanoTime();
		System.out.println("Traveling Salesman Tour");
		System.out.println("-----------------------");
		System.out.println(cities[tour[0]]);
		for (int i = 1; i < tour.length; i++)
			System.out.println("   to " + cities[tour[i]]);
		System.out.println();
		System.out.println("Total time elapsed: " + (0.0 + end - start) / 1000000000 + "sec.");
	}
	
	private class HeapElement extends SimpleArc implements Comparable<HeapElement> {

		
		public HeapElement(int index, int treeNode, double key) {
			super(treeNode, index, key);
		}
		
		public int getIndex() {
			return to;
		}
		
		public void setTreeNode(int n) {
			from = n;
		}
		
		public void setKey(double w) {
			weight = w;
		}
		
		public double getKey() {
			return weight;
		}
		
		public int compareTo(HeapElement o) {
			if (this.to == o.to)
				return 0;
			else
				return Double.compare(this.weight, o.weight);
		}
	}

}
