/**
 * Olivet.java
 * Jan 22, 2012
 */
package networks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import util.TSPSolver;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Jan 22, 2012
 *
 */
public class Olivet {
	
	public static void main(String[] args) throws IOException {
		String[] cities = 	{"Chicago", "Green Bay", "Minneapolis", "Indianapolis", "St Louis",
									"Atlanta", "New York", "Boston", "Miami", "Los Angeles", "Seattle",
									"Denver", "Houston", "Washington", "Pittsburgh", "New Orleans",
									"San Francisco", "Kansas City", "Phoenix", "Dallas"};
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
		BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Minimum Spanning Trees and Cycles - Brad Paynter");
		System.out.println("================================================");
		boolean done = false;
		String input;
		int numCities = -1;
		while (!done) {
			System.out.print("Number of Cities? ");
			input = consoleReader.readLine();
			done = true;
			try {
				numCities = Integer.parseInt(input);
			} catch (NumberFormatException e) {
				System.out.println("Invalid number");
				done = false;
			}
			if (done) {
				if ((numCities < 1) || (numCities > 20)) {
					System.out.println("Please enter a number between 1 and 20.");
					done = false;
				} else {
					double[][] weights = new double[numCities][numCities];
					for (int i = 0; i < numCities; i++) {
						weights[i][i] = 0.0;
						for (int j = i+1; j < numCities; j++) {
							double x = locations[i][0] - locations[j][0];
							double y = locations[i][1] - locations[j][1];
							weights[i][j] = Math.sqrt(x*x + y*y);
							weights[j][i] = weights[i][j];
						}
					}
					MinSpanningTree t = new MinSpanningTree(weights);
					done = false;
					while (!done) {
						System.out.print("(M)inimum Spanning Tree, (T)raveling Salesman or (E)xit? ");
						input = consoleReader.readLine();
						if (input.equals("m") || input.equals("M")) {
							long start = System.nanoTime();
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
						} else if (input.equals("t") || input.equals("T")) {
							long start = System.nanoTime();
							TSPSolver tsp = new TSPSolver(t);
							int[] tour = tsp.bruteForceSolver(true);
							long end = System.nanoTime();
							System.out.println("Traveling Salesman Tour");
							System.out.println("-----------------------");
							System.out.println(cities[tour[0]]);
							for (int i = 1; i < tour.length; i++)
								System.out.println("   to " + cities[tour[i]]);
							System.out.println();
							System.out.println("Total time elapsed: " + (0.0 + end - start) / 1000000000 + "sec.");
							System.out.println("===================================================================================");
							System.out.println();
						} else if (input.equals("e") || input.equals("E")) {
							done = true;
						}
					}
					done = false;
				}
			}
		}
		
	}

}
