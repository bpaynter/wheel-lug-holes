/**
 * TwoVIPAlgorithm.java
 * Nov 7, 2011
 */
package util;

import java.util.ArrayDeque;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Nov 7, 2011
 *
 */
public class TwoVIPAlgorithm {
	
	/**
	 * 
	 * TODO
	 * 
	 * @param bounds
	 * @param constraint
	 * @return
	 */
	private static Bounds oneOnOneImpact(TwoVIPInstance instance, Bounds bounds, TwoVIPConstraint constraint) {
		if (constraint.coefficiantTwo > 0) {
			int lbTemp;
			if (constraint.coefficiantOne > 0)
				lbTemp = (int)Math.ceil((0d + constraint.rightHandSide - constraint.coefficiantOne * bounds.ub[constraint.variableIndexOne]) / constraint.coefficiantTwo);
			else
				lbTemp = (int)Math.ceil((0d + constraint.rightHandSide - constraint.coefficiantOne * bounds.lb[constraint.variableIndexOne]) / constraint.coefficiantTwo);
			if (bounds.lb[constraint.variableIndexTwo] < lbTemp) {
				bounds.lb[constraint.variableIndexTwo] = lbTemp;
				bounds.result = true;
			} else
				bounds.result = false;
		} else {
			int ubTemp;
			if (constraint.coefficiantOne > 0)
				ubTemp = (int)Math.floor((0d + constraint.rightHandSide - constraint.coefficiantOne * bounds.lb[constraint.variableIndexOne]) / constraint.coefficiantTwo);
			else
				ubTemp = (int)Math.floor((0d + constraint.rightHandSide - constraint.coefficiantOne * bounds.ub[constraint.variableIndexOne]) / constraint.coefficiantTwo);
			if (bounds.ub[constraint.variableIndexTwo] > ubTemp) {
				bounds.ub[constraint.variableIndexTwo] = ubTemp;
				bounds.result = true;
			} else
				bounds.result = false;
		}
		return bounds;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param bounds
	 * @param t
	 * @return
	 */
	private static Bounds oneOnAllImpact(TwoVIPInstance instance, Bounds bounds, int t) {
		ArrayDeque<Integer> stack = new ArrayDeque<Integer>();
		stack.push(t);
		while (!stack.isEmpty()) {
			int i = stack.pop();
			for (int k = 0; k < instance.numConstraints(); k++) {
				TwoVIPConstraint constraint = instance.getConstraint(k);
				if (constraint.variableIndexTwo == i) {
					constraint = constraint.mirrorConstraint();
				}
				if (constraint.variableIndexOne == i) {
					bounds = oneOnOneImpact(instance, bounds, constraint);
					if (bounds.ub[constraint.variableIndexTwo] < bounds.lb[constraint.variableIndexTwo]) {
						bounds.result = false;
						return bounds;
					}
					if (bounds.result)
						stack.push(constraint.variableIndexTwo);
				}
			}
		}
		bounds.result = true;
		return bounds;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public static long[] feasibility(TwoVIPInstance instance) {
		long[] ub = new long[instance.numVariables()];
		long[] lb = new long[instance.numVariables()];
		for (int i = 0; i < instance.numVariables(); i++) {
			ub[i] = instance.upperBound(i);
			lb[i] = instance.lowerBound(i);
		}
		TwoVIPAlgorithm dummy = new TwoVIPAlgorithm();
		Bounds bounds = dummy.new Bounds(lb, ub);
		boolean done = false;
		while (!done) {
			int i = 0;
			int t = -1;
			while ((i < instance.numVariables()) && (t < 0)) {
				if (bounds.lb[i] < bounds.ub[i])
					t = i;
				else
					i++;
			}
			if (t < 0)
				done = true;
			else {
				int alpha = (int)Math.floor(0.5d * (bounds.lb[t] + bounds.ub[t]));
				long[] ubLeft = bounds.ub.clone();
				ubLeft[t] = alpha;
				Bounds left = dummy.new Bounds(bounds.lb.clone(), ubLeft);
				left = oneOnAllImpact(instance, left, t);
				if (left.result)
					bounds = left;
				else {
					long[] lbRight = bounds.lb.clone();
					lbRight[t] = alpha + 1;
					Bounds right = dummy.new Bounds(lbRight, bounds.ub.clone());
					right = oneOnAllImpact(instance, right, t);
					if (right.result)
						bounds = right;
					else
						return null;
				}
			}
		}
		for (int k = 0; k < instance.numConstraints(); k++) {
			TwoVIPConstraint constraint = instance.getConstraint(k);
			long term1 = constraint.coefficiantOne * bounds.lb[constraint.variableIndexOne];
			long term2 = constraint.coefficiantTwo * bounds.lb[constraint.variableIndexTwo];
			if (term1 + term2 < constraint.rightHandSide)
				return null;
		}
		return bounds.lb;
	}
	
	
	
	private class Bounds {
		public boolean result;
		public long[] lb;
		public long[] ub;
		
		public Bounds(long[] lb, long[] ub) {
			this.lb = lb;
			this.ub = ub;
		}
	}

}
