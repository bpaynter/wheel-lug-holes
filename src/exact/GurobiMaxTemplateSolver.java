/**
 * GurobiMaxTemplateSolver.java
 * Jul 28, 2010
 */
package exact;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import problem.PatternSet;
import problem.CircularPatternSet;
import util.IntegerUtils;
import util.MersenneTwisterFast;
import util.StrictUTMatrix;

/**
 * This code is written as part of the research into optimizing lug hole templates done
 * by Bradley Paynter and Dr. D. Shier at Clemson University, Clemson, SC. <br />
 * <br />
 *
 * @author Brad Paynter
 * @version Jul 28, 2010
 *
 */
public class GurobiMaxTemplateSolver {
	/**
	 * TODO
	 */
	private GRBEnv environment;
	/**
	 * TODO
	 */
	private GRBModel model;
	/**
	 * TODO
	 */
	private PatternSet patterns;
	private GRBVar[] ks;
	private GRBVar[] startingPositions;
	/**
	 * TODO
	 */
	private GRBVar[] ys;
	
	/**
	 * 
	 * TODO
	 *
	 */
	public GurobiMaxTemplateSolver() {
		try {
			environment = new GRBEnv("MaxTemplateSolver.log");
			//environment.set(GRB.DoubleParam.TimeLimit, 60d);
			model = new GRBModel(environment);
			model.set(GRB.IntAttr.ModelSense, -1);
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
		patterns = null;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 * @return
	 */
	/*
	private double calculateM() {
		double M = 0.0;
		M += 2 * patterns.getPattern(0).outerCircumference;
		int maxD = 0;
		for (Pattern p : patterns)
			if (p.innerDiameter > maxD)
				maxD = p.innerDiameter;
		M += maxD;
		return M;
	}
	*/
	
	/**
	 * 
	 * TODO
	 * 
	 * @param patterns
	 */
	public void setPatterns(PatternSet patterns) {
		this.patterns = patterns;
		try {
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
			//double M = calculateM();
			ys = new GRBVar[patterns.numPatterns()];
			for (int i = 0; i < patterns.numPatterns(); i++) {
				ys[i] = model.addVar(0, 1, 1.0, GRB.BINARY, "y_" + i);
			}
			model.update();
			for (int i = 0; i < patterns.numPatterns(); i++) {
				for (int j = i+1; j < patterns.numPatterns(); j++) {
					GRBLinExpr lhs = new GRBLinExpr();
					lhs.addTerm(1.0, startingPositions[j]);
					lhs.addTerm(-1.0, startingPositions[i]);
					GRBLinExpr rhs = new GRBLinExpr();
					rhs.addTerm(patterns.getB(i, j), ks[StrictUTMatrix.index(patterns.numPatterns(), i, j)]);
					rhs.addTerm(-patterns.getD(i, j), ys[i]);
					rhs.addTerm(-patterns.getD(i, j), ys[j]);
					rhs.addConstant(patterns.getB(i, j) + patterns.getD(i, j));
					model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "Constraint :" + i + ", " + j);
					lhs = new GRBLinExpr();
					lhs.addTerm(1.0, startingPositions[i]);
					lhs.addTerm(-1.0, startingPositions[j]);
					rhs = new GRBLinExpr();
					rhs.addTerm(-patterns.getB(i, j), ks[StrictUTMatrix.index(patterns.numPatterns(), i, j)]);
					rhs.addTerm(-patterns.getD(j, i), ys[i]);
					rhs.addTerm(-patterns.getD(j, i), ys[j]);
					rhs.addConstant(patterns.getD(j, i));
					model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "Constraint :" + j + ", " + i);
				}
			}
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public long[][] outputKs() {
		int[] solution = outputSolution();
		long[][] outputKs = new long[solution.length][solution.length];
		try {
			for (int i = 0; i < solution.length; i++) {
				outputKs[i][i] = 0;
				for (int j = i+1; j < solution.length; j++) {
					outputKs[i][j] = (long)ks[StrictUTMatrix.index(patterns.numPatterns(), solution[i], solution[j])].get(GRB.DoubleAttr.X);
					outputKs[j][i] = outputKs[i][j];
				}
			}
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
			return null;
		}
		return outputKs;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public int[] outputSolution() {
		int[] solutionIndices = null;
		try {
			int solutionLength = 0;
			for (int i = 0; i < patterns.numPatterns(); i++)
				if (ys[i].get(GRB.DoubleAttr.X) == 1d)
					solutionLength++;
			solutionIndices = new int[solutionLength];
			int index = 0;
			for (int i = 0; i < patterns.numPatterns(); i++)
				if (ys[i].get(GRB.DoubleAttr.X) == 1d)
					solutionIndices[index++] = i;
			
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
			solutionIndices = null;
		}
		return solutionIndices;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public boolean checkSolution() {
		PatternSet solution = patterns.subset(outputSolution());
		solution.setSolution(outputKs());
		//System.out.println(solution.printSolution());
		FitNetwork net = new FitNetwork(solution.numPatterns());
		return net.testSolution(solution);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public double[] outputStartingPositions() {
		int[] solution = outputSolution();
		double[] outputS = new double[solution.length];
		try {
			for (int i = 0; i < solution.length; i++)
				outputS[i] = startingPositions[solution[i]].get(GRB.DoubleAttr.X);
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
			return null;
		}
		return outputS;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 * @throws GRBException
	 */
	public String printSolution() throws GRBException {
		String endln = System.getProperty("line.separator");
		String s = "Solution" + endln;
		for (GRBVar k : ks) {
			s += k.get(GRB.StringAttr.VarName) + "=" + k.get(GRB.DoubleAttr.X) + endln;
		}
		for (GRBVar y : ys) {
			s += y.get(GRB.StringAttr.VarName) + "=" + y.get(GRB.DoubleAttr.X) + endln;
		}
		return s;
	}
	
	/**
	 * 
	 * TODO
	 *
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
	 */
	public void solve() {
		try {
			model.optimize();
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return model.toString();
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public double getObjectiveValue() {
		try {
			return model.get(GRB.DoubleAttr.ObjVal);
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
			return 0.0;
		}
		
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public double getObjectiveBound() {
		try {
			return model.get(GRB.DoubleAttr.ObjBound);
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
			return 0.0;
		}
		
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param args
	 * @throws GRBException 
	 */
	public static void main(String[] args) throws GRBException {
		GurobiMaxTemplateSolver solver = new GurobiMaxTemplateSolver();
		MersenneTwisterFast random = new MersenneTwisterFast(5748367893569853l);
		IntegerUtils integers = new IntegerUtils(100);
		for (int i = 0; i < 1; i++) {
			CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers);
			boolean valid = false;
			while (!valid) {
				valid = true;
				try {
					patterns = CircularPatternSet.randomTightPatternSet(50, 30, 1, 100, 3, random, integers);
				} catch (IllegalArgumentException e) {
					valid = false;
				}
			}
			solver.setPatterns(patterns);
			solver.solve();
			System.out.println(solver.printSolution());
			solver.clear();
			/*
			try {
				cplex = new CplexFitSolver(patterns);
				cplex.exportModel("Cplex.lp");
				System.out.println(cplex.solve());
				double[] solution = cplex.getValues();
    			int[] ks = new int[solution.length];
    			IloIntVar[] cplexVars = cplex.getKs();
    			for (int j = 0; j < solution.length; j++) {
    				ks[j] = (int)Math.round(solution[j]);
    				System.out.println(cplexVars[j].getName() + "=" + ks[i]);
    			}
			} catch (IloException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
	}

}
