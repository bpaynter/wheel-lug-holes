/**
 * SetPartition.java
 * Jul 11, 2011
 */
package heuristics;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import networks.FloydWarshall;

import problem.PatternSet;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Jul 11, 2011
 *
 */
public class SetPartition {
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @return
	 */
	public static int[][] indexPartition(PatternSet patterns) {
		int size = patterns.numPatterns();
		ArrayList<int[]> sets = new ArrayList<int[]>();
		double[][] weights = new double[size][size];
		for (int i = 0; i < size; i++) {
			weights[i][i] = 0;
			for (int j = i + 1; j < size; j++) {
				weights[i][j] = (patterns.getBB(i, j) < 0) ? Double.POSITIVE_INFINITY : 1;
				weights[j][i] = weights[i][j];
			}
		}
		/*
		String endln = System.getProperty("line.separator");
		String s = "";
		s += "Weight";
		for (int j = 0; j < size; j++)
			s += String.format("%1$9s", "<" + j + ">");
		s += endln;
		for (int i = 0; i < size; i++) {
			s += String.format("%1$9s", "<" + i + ">");
			for (int j = 0; j < size; j++)
				s += String.format("%1$9.2f", weights[i][j]);
			s += endln;
		}
		System.out.println(s);
		*/
		SetPartition.trimLoners(weights);
		/*
		s = "";
		s += "Weight";
		for (int j = 0; j < size; j++)
			s += String.format("%1$9s", "<" + j + ">");
		s += endln;
		for (int i = 0; i < size; i++) {
			s += String.format("%1$9s", "<" + i + ">");
			for (int j = 0; j < size; j++)
				s += String.format("%1$9.2f", weights[i][j]);
			s += endln;
		}
		System.out.println(s);
		*/
		FloydWarshall floyd = new FloydWarshall(patterns.numPatterns());
		//floyd.verbose = true;
		double[][] dist = floyd.runFloydWarshall(weights);
		boolean[] used = new boolean[patterns.numPatterns()];
		Arrays.fill(used, false);
		int rootNode = 0;
		while (rootNode >= 0) {
			int numInSet = 0;
			for (int i = 0; i < patterns.numPatterns(); i++)
				if (dist[rootNode][i] < patterns.numPatterns())
					numInSet++;
			int[] template = new int[numInSet];
			numInSet = 0;
			for (int i = 0; i < patterns.numPatterns(); i++)
				if (dist[rootNode][i] < patterns.numPatterns()) {
					template[numInSet++] = i;
					used[i] = true;
				}
			sets.add(template);
			rootNode = -1;
			for (int i = 0; i < patterns.numPatterns(); i++)
				if (!used[i])
					rootNode = i;
		}
		return sets.toArray(new int[0][]);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param weights
	 * @return
	 */
	private static double[][] trimLoners(double[][] weights) {
		for (int i = 0; i < weights.length; i++) {
			int numNeighbors = 0;
			int lastNeighbor = -1;
			for (int j = 0; j < weights[i].length; j++)
				if (weights[i][j] == 1d) {
					numNeighbors++;
					lastNeighbor = j;
				}
			if (numNeighbors == 1)
				for (int k = 0; k < weights.length; k++)
					if ((k != i) && (k != lastNeighbor)) {
						weights[lastNeighbor][k] = Double.POSITIVE_INFINITY;
						weights[k][lastNeighbor] = Double.POSITIVE_INFINITY;
					}
				
		}
		return weights;
	}
	
	public static void generateIncidenceGraph(PatternSet patterns, File f) throws IllegalArgumentException {
		ArrayList<int[]> edgeArray = new ArrayList<int[]>();
		for (int i = 0; i < patterns.numPatterns(); i++)
			for (int j = i+1; j < patterns.numPatterns(); j++)
				if (patterns.getBB(i, j) < 0) {
					int[] edge = {i+1, j+1};
					edgeArray.add(edge);
				}
		PrintWriter fileOut = null;
		try {
			fileOut = new PrintWriter(new FileWriter(f));
		} catch (IOException e) {
			System.out.println("File Creation Error <" + e + ">");
			throw new IllegalArgumentException("Invalid File: " + f);
		}
		int headerLength = (int)Math.ceil(Math.log10(patterns.numPatterns())) + (int)Math.ceil(Math.log10(edgeArray.size())) + 9;
		fileOut.print(headerLength + "\n");
		fileOut.print("p edge " + patterns.numPatterns() + " " + edgeArray.size() + "\n");
		for (int i = 1; i < patterns.numPatterns()+1; i++)
			fileOut.print("n " + i + "\n");
		// Write the array to the file
		for (int[] edge : edgeArray) {
			fileOut.print("e " + edge[0] + " " + edge[1] + "\n");
		}
		// Close the file
		fileOut.close();
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @return
	 */
	public static PatternSet[] setPartition(PatternSet patterns) {
		int[][] partition = indexPartition(patterns);
		PatternSet[] setPartition = new PatternSet[partition.length];
		for (int i = 0; i < partition.length; i++)
			setPartition[i] = patterns.subset(partition[i]);
		return setPartition;
	}
}
