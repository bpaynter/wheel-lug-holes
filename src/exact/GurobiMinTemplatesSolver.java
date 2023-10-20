/**
 * GurobiMaxTemplateSolver.java
 * Jul 28, 2010
 */
package exact;

import java.util.Arrays;

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
public class GurobiMinTemplatesSolver {
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
	private int upperBound;
	private GRBVar[] ks;
	private GRBVar[] startingPositions;
	/**
	 * TODO
	 */
	private GRBVar[][] ys;
	private GRBVar[] zs;
	
	/**
	 * 
	 * TODO
	 *
	 */
	public GurobiMinTemplatesSolver() {
		try {
			environment = new GRBEnv("MaxTemplateSolver.log");
			environment.set(GRB.DoubleParam.TimeLimit, 3600d);
			environment.set(GRB.DoubleParam.IntFeasTol, 1e-9d);
			environment.set(GRB.DoubleParam.FeasibilityTol, 1e-9d);
			environment.set(GRB.IntParam.OutputFlag, 0);
			model = new GRBModel(environment);
			model.set(GRB.IntAttr.ModelSense, 1);
		} catch (GRBException e) {
			System.err.println("Error Code: " + e.getErrorCode() + ". " + e.getMessage());
		}
		patterns = null;
		upperBound = -1;
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
	public void setPatterns(PatternSet patterns, int upperBound) {
		this.patterns = patterns;
		this.upperBound = upperBound;
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
			ys = new GRBVar[patterns.numPatterns()][upperBound];
			zs = new GRBVar[upperBound];
			for (int t = 0; t < upperBound; t++) {
				for (int i = 0; i < patterns.numPatterns(); i++)
					ys[i][t] = model.addVar(0, 1, 0.0, GRB.BINARY, "y_{" + i + ", " + t + "}");
				zs[t] = model.addVar(0, 1, 1.0, GRB.CONTINUOUS, "z_" + t);
			}
			model.update();
			for (int i = 0; i < patterns.numPatterns(); i++) {
				for (int j = i+1; j < patterns.numPatterns(); j++)
					for (int t = 0; t < upperBound; t++) {
						GRBLinExpr lhs = new GRBLinExpr();
						lhs.addTerm(1.0, startingPositions[j]);
						lhs.addTerm(-1.0, startingPositions[i]);
						GRBLinExpr rhs = new GRBLinExpr();
						rhs.addTerm(patterns.getB(i, j), ks[StrictUTMatrix.index(patterns.numPatterns(), i, j)]);
						rhs.addTerm(-patterns.getD(i, j), ys[i][t]);
						rhs.addTerm(-patterns.getD(i, j), ys[j][t]);
						rhs.addConstant(patterns.getB(i, j) + patterns.getD(i, j));
						model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "Constraint: " + i + ", " + j);
						lhs = new GRBLinExpr();
						lhs.addTerm(1.0, startingPositions[i]);
						lhs.addTerm(-1.0, startingPositions[j]);
						rhs = new GRBLinExpr();
						rhs.addTerm(-patterns.getB(i, j), ks[StrictUTMatrix.index(patterns.numPatterns(), i, j)]);
						rhs.addTerm(-patterns.getD(j, i), ys[i][t]);
						rhs.addTerm(-patterns.getD(j, i), ys[j][t]);
						rhs.addConstant(patterns.getD(j, i));
						model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "Constraint: " + j + ", " + i);
					}
				GRBLinExpr lhs = new GRBLinExpr();
				for (int t = 0; t < upperBound; t++)
					lhs.addTerm(1.0, ys[i][t]);
				GRBLinExpr rhs = new GRBLinExpr();
				rhs.addConstant(1);
				model.addConstr(lhs, GRB.EQUAL, rhs, "Constraint: " + i);
				for (int t = 0; t < upperBound; t++) {
					lhs = new GRBLinExpr();
					rhs = new GRBLinExpr();
					lhs.addTerm(1.0, ys[i][t]);
					rhs.addTerm(1.0, zs[t]);
					model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "Constraint: " + i + ", " + t);
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
	public long[][][] outputKs() {
		int[][] solution = outputSolution();
		long[][][] outputKs = new long[solution.length][][];
		try {
			for (int t = 0; t < solution.length; t++) {
				outputKs[t] = new long[solution[t].length][solution[t].length];
				for (int i = 0; i < solution[t].length; i++) {
					outputKs[t][i][i] = 0;
					for (int j = i+1; j < solution[t].length; j++) {
						outputKs[t][i][j] = (long)ks[StrictUTMatrix.index(patterns.numPatterns(), solution[t][i], solution[t][j])].get(GRB.DoubleAttr.X);
						outputKs[t][j][i] = outputKs[t][i][j];
					}
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
	public int[][] outputSolution() {
		int[][] solutionIndices = null;
		try {
			int numTemplates = 0;
			for (int t = 0; t < upperBound; t++)
				if (zs[t].get(GRB.DoubleAttr.X) == 1d)
					numTemplates++;
			solutionIndices = new int[numTemplates][];
			int templateIndex = 0;
			for (int t = 0; t < upperBound; t++)
				if (zs[t].get(GRB.DoubleAttr.X) == 1d) {
					int templateSize = 0;
					for (int i = 0; i < patterns.numPatterns(); i++)
						if (ys[i][t].get(GRB.DoubleAttr.X) == 1d)
							templateSize++;
					solutionIndices[templateIndex] = new int[templateSize];
					int index = 0;
					for (int i = 0; i < patterns.numPatterns(); i++)
						if (ys[i][t].get(GRB.DoubleAttr.X) == 1d)
							solutionIndices[templateIndex][index++] = i;
					templateIndex++;
				}
		} catch (Exception e) {
			System.err.println(e.getMessage());
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
		int[][] solution = outputSolution();
		long[][][] ks = outputKs();
		for (int t = 0; t < solution.length; t++) {
			PatternSet solutionSet = patterns.subset(solution[t]);
			solutionSet.setSolution(ks[t]);
			//System.out.println(solution.printSolution());
			FitNetwork net = new FitNetwork(solutionSet.numPatterns());
			if (!net.testSolution(solutionSet))
				return false;
		}
		return true;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public double[] outputStartingPositions() {
		double[] outputS = new double[patterns.numPatterns()];
		try {
			for (int i = 0; i < patterns.numPatterns(); i++)
				outputS[i] = startingPositions[i].get(GRB.DoubleAttr.X);
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
		for (GRBVar[] yRow : ys) {
			for (GRBVar y : yRow)
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
			return Double.NaN;
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
		GurobiMinTemplatesSolver solver = new GurobiMinTemplatesSolver();
		MersenneTwisterFast random = new MersenneTwisterFast(5748367893569853l);
		IntegerUtils integers = new IntegerUtils(100);
		for (int i = 0; i < 1; i++) {
			CircularPatternSet patterns = CircularPatternSet.randomTightPatternSet(1, 30, 1, 5, 3, random, integers);
			boolean valid = false;
			while (!valid) {
				valid = true;
				try {
					patterns = CircularPatternSet.randomTightPatternSet(20, 30, 1, 100, 3, random, integers);
				} catch (IllegalArgumentException e) {
					valid = false;
				}
			}
			solver.setPatterns(patterns, 15);
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
