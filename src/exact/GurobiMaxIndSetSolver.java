/**
 * GurobiMaxIndSetSolver.java
 * Nov 30, 2011
 */
package exact;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import problem.PatternSet;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Nov 30, 2011
 *
 */
public class GurobiMaxIndSetSolver {
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
	private GRBVar[] nodeVariables;
	
	private PatternSet patterns;
	
	/**
	 * Creates an empty model
	 */
	public GurobiMaxIndSetSolver(PatternSet patterns) {
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
			//environment.set(GRB.IntParam.OutputFlag, 0);
			//environment.set(GRB.DoubleParam.TimeLimit, 600.0);
			model = new GRBModel(environment);
			model.set(GRB.IntAttr.ModelSense, -1);
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
	
	private void generateModel() {
		try {
			// Create a matrix for the k_{ij} values
			nodeVariables = new GRBVar[patterns.numPatterns()];
			for (int i = 0; i < patterns.numPatterns(); i++)
					nodeVariables[i] = model.addVar(0, 1, 1, GRB.BINARY, "Node: " + i);
			model.update();
			for (int i = 0; i < patterns.numPatterns(); i++) {
				for (int j = i+1; j < patterns.numPatterns(); j++) {
					if (patterns.getBB(i, j) >= 0) {
						GRBLinExpr lhs = new GRBLinExpr();
						lhs.addTerm(1.0, nodeVariables[j]);
						lhs.addTerm(1.0, nodeVariables[i]);
						GRBLinExpr rhs = new GRBLinExpr();
						rhs.addConstant(1);
						model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "Constraint ");
					}
				}
			}
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
	
	public int[] outputSolution() {
		int[] solutionIndices = null;
		try {
			int solutionLength = 0;
			for (int i = 0; i < patterns.numPatterns(); i++)
				if (nodeVariables[i].get(GRB.DoubleAttr.X) == 1d)
					solutionLength++;
			solutionIndices = new int[solutionLength];
			int index = 0;
			for (int i = 0; i < patterns.numPatterns(); i++)
				if (nodeVariables[i].get(GRB.DoubleAttr.X) == 1d)
					solutionIndices[index++] = i;
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
			solutionIndices = null;
		}
		return solutionIndices;
	}
	
	public void clear() {
		try {
			model.dispose();
			model = new GRBModel(environment);
			model.set(GRB.IntAttr.ModelSense, -1);
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
		nodeVariables = null;
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
		nodeVariables = null;
	}
	
	/**
	 * 
	 * TODO
	 *
	 */
	public void solve() {
		try {
			model.optimize();
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

}
