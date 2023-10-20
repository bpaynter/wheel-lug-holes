/**
 * GurobiFitSolver.java
 * Jul 27, 2010
 */
package exact;

import gurobi.*;

import problem.CircularPattern;
import problem.CircularPatternSet;
import problem.PatternSet;
import problem.Pattern;
import util.StrictUTMatrix;

/**
 * This code is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 * This class uses the Gurobi solver to model the Fit Integer Program. It has methods 
 * to add all variables and constraints, to solve the model and output the solution.
 *
 * @author Brad Paynter
 * @version Jul 27, 2010
 *
 */
public class GurobiFitSolver {
	/**
	 * The enviroment needed for the Gurobi Modeler
	 */
	private GRBEnv environment;
	/**
	 * The Gurobi Model Object
	 */
	private GRBModel model;
	/**
	 * The k_{i,j} variables
	 */
	private GRBVar[] ks;
	private GRBVar[] startingPositions;
	
	private PatternSet patterns;
	
	/**
	 * Creates an empty model
	 */
	public GurobiFitSolver(PatternSet patterns) {
		boolean complete = false;
		while (!complete) {
			try {
				environment = new GRBEnv();
				complete = true;
			} catch (GRBException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException r) {
					r.printStackTrace();
				}
			}
		}
		try {
			environment.set(GRB.IntParam.OutputFlag, 0);
			environment.set(GRB.DoubleParam.TimeLimit, 600.0);
			model = new GRBModel(environment);
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
			System.err.println("Problem in GurobiFitSolver constructor 2.");
		}
		this.patterns = patterns;
		generateModel();
	}
	
	/**
	 * Sets the verbosity level of the solver.
	 * 
	 * @param verbose <code>false</code> to suppress all output, <code>true</code>
	 * 			for Gurobi default
	 */
	public void setVerbosity(boolean verbose) {
		try {
			if (verbose)
				environment.set(GRB.IntParam.OutputFlag, 1);
			else
				environment.set(GRB.IntParam.OutputFlag, 0);
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
	
	/**
	 * This method creates the Integer Program representing the Fit problem for the
	 * given set of patterns. It determines the integer k_{i,j} values with which the
	 * entire set can be fitted onto one template. If the model is infeasible, it implies
	 * that such a template is impossible.
	 * 
	 * @param patterns The set of patterns 
	 */
	private void generateModel() {
		try {
			// Create a matrix for the k_{ij} values
			ks = new GRBVar[StrictUTMatrix.length(patterns.numPatterns())];
			startingPositions = new GRBVar[patterns.numPatterns()];
			for (int i = 0; i < patterns.numPatterns(); i++) {
				for (int j = i+1; j < patterns.numPatterns(); j++) {
					long lbound = patterns.getKLowerBound(i, j);
					long ubound = patterns.getKUpperBound(i, j);
					// Set each integer k_{ij} variable with its upper bound, lower bound and objective co-efficient
					ks[StrictUTMatrix.index(patterns.numPatterns(), i, j)] 
					   	= model.addVar(lbound, ubound, 
					   					0.0, GRB.INTEGER, "k_{" + i + "," + j + "}");
				}
				double ub = patterns.getPattern(i).getPeriod() * patterns.getKUpperBound(0, i);
				double lb = patterns.getPattern(i).getPeriod() * patterns.getKLowerBound(0, i);
				startingPositions[i] = model.addVar(lb, ub, 0.0, GRB.CONTINUOUS, "s_" + i);
			}
			// Update the model to include all the variables we've added
			model.update();
			for (int i = 0; i < patterns.numPatterns(); i++) {
				for (int j = i+1; j < patterns.numPatterns(); j++) {
					GRBLinExpr lhs = new GRBLinExpr();
					lhs.addTerm(1.0, startingPositions[j]);
					lhs.addTerm(-1.0, startingPositions[i]);
					GRBLinExpr rhs = new GRBLinExpr();
					rhs.addTerm(patterns.getB(i, j), ks[StrictUTMatrix.index(patterns.numPatterns(), i, j)]);
					rhs.addConstant(patterns.getB(i, j) - patterns.getPattern(j).getInnerDiameter());
					model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "Constraint :" + i + ", " + j);
					lhs = new GRBLinExpr();
					lhs.addTerm(1.0, startingPositions[i]);
					lhs.addTerm(-1.0, startingPositions[j]);
					rhs = new GRBLinExpr();
					rhs.addTerm(-patterns.getB(i, j), ks[StrictUTMatrix.index(patterns.numPatterns(), i, j)]);
					rhs.addConstant(-patterns.getPattern(i).getInnerDiameter());
					model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "Constraint :" + j + ", " + i);
				}
			}
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
	
	/**
	 * Outputs a string representation of the current decision variable values
	 * 
	 * @return A string of the current k_{ij} values
	 */
	public String printSolution() {
		String endln = System.getProperty("line.separator");
		String s = "Solution" + endln;
		try {
			for (GRBVar k : ks) {
				s += k.get(GRB.StringAttr.VarName) + "=" + k.get(GRB.DoubleAttr.X) + endln;
			}
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
		long[][] intKs = getKs();
		s += "      ";
		for (int j = 0; j < patterns.numPatterns(); j++)
			s += String.format("%1$6s", "P_" + j);
		s += endln;
		for (int i = 0; i < patterns.numPatterns(); i++) {
			s += String.format("%1$6s", "P_" + i);
			for (int j = 0; j < patterns.numPatterns(); j++)
				s += String.format("%1$6s", intKs[i][j]);
			s += endln;
		}
		return s;
	}
	
	/**
	 * Clears the internal model, removing all variables and constraints
	 */
	public void clear() {
		try {
			model.dispose();
			model = new GRBModel(environment);
			model.set(GRB.IntAttr.ModelSense, -1);
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
		ks = null;
	}
	
	/**
	 * 
	 */
	public void dispose() {
		try {
			this.clear();
			environment.dispose();
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
		ks = null;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean saveModel(String fileName) {
		boolean success;
		try {
			model.write(fileName);
			success = true;
		} catch (GRBException e) {
			success = false;
		}
		return success;
	}
	
	/**
	 * Solves the current model
	 * 
	 * @return Returns <code>true</code> if the Solver found an optimal solution
	 * 			<code>false</code> else
	 */
	public boolean solve() {
		try {
			model.optimize();
			if (model.get(GRB.IntAttr.Status) == GRB.OPTIMAL)
				return true;
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
		return false;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public long[][] getKs() {
		long[][] outKs;
		try {
			outKs = new long[patterns.numPatterns()][patterns.numPatterns()];
			for (int i = 0; i < patterns.numPatterns(); i++) {
				outKs[i][i] = 0;
				for (int j = i + 1; j < patterns.numPatterns(); j++) {
					outKs[i][j] = (long)(Math.floor(ks[StrictUTMatrix.index(patterns.numPatterns(), i, j)].get(GRB.DoubleAttr.X) + 0.5));
					outKs[j][i] = outKs[i][j];
				}
			}
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
			outKs = null;
		}
		return outKs;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public boolean checkSolution() {
		long[][] ks = getKs();
		patterns.setSolution(ks);
		FitNetwork net = new FitNetwork(patterns.numPatterns());
		return net.testSolution(patterns);
	}
	
	/**
	 * This main method is used for testing this class
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		// MersenneTwisterFast random = new MersenneTwisterFast(534254525);
		// IntegerUtils integers = new IntegerUtils(100);
		for (int i = 0; i < 1; i++) {
			CircularPattern[] patternArray = new CircularPattern[4];
			
			// Example #1672
			patternArray[0] = new CircularPattern(49281, 25, 19);
			patternArray[1] = new CircularPattern(49281, 39, 10);
			patternArray[2] = new CircularPattern(49281, 55, 8);
			patternArray[3] = new CircularPattern(49281, 64, 6);
			
			/*
			// Example #1695
			patternArray[0] = new CircularPattern(72851, 20, 12);
			patternArray[1] = new CircularPattern(72851, 88, 5);
			patternArray[2] = new CircularPattern(72851, 50, 27);
			patternArray[3] = new CircularPattern(72851, 47, 4);
			*/
			/*
			// Example #1703
			patternArray[0] = new Pattern(115633, 79, 4);
			patternArray[1] = new Pattern(115633, 9, 5);
			patternArray[2] = new Pattern(115633, 73, 16);
			patternArray[3] = new Pattern(115633, 44, 20);
			*/
			/*
			// Example #1708
			patternArray[0] = new Pattern(233929, 81, 30);
			patternArray[1] = new Pattern(233929, 93, 8);
			patternArray[2] = new Pattern(233929, 74, 9);
			patternArray[3] = new Pattern(233929, 76, 8);
			*/
			
			CircularPatternSet patterns = new CircularPatternSet(patternArray);
			/*
			PatternSet patterns = PatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers);
			boolean valid = false;
			while (!valid) {
				valid = true;
				try {
					patterns = PatternSet.randomTightPatternSet(20, 30, 1, 100, 3, random, integers);
				} catch (IllegalArgumentException e) {
					valid = false;
				}
			}
			*/
			GurobiFitSolver solver = new GurobiFitSolver(patterns);
			solver.solve();
			System.out.println(solver.printSolution());
			/*
			int[][] ks = new int[4][4];
			for (int[] j : ks)
				Arrays.fill(j, 0);
			ks[0][3] = 1;
			ks[3][0] = 1;
			ks[2][3] = -1;
			ks[3][2] = -1;
			*/
			patterns.setSolution(solver.getKs());
			FitNetwork net = new FitNetwork(4);
			System.out.println(net.testSolution(patterns));
			System.out.println(patterns.printSolution());
			solver.clear();
			
		}
	}

}
