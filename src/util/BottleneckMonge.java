/**
 * MongeMatrix.java
 * Feb 17, 2011
 */
package util;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Feb 17, 2011
 *
 */
public class BottleneckMonge {
	public static <T extends Comparable<? super T>> T max(T o1, T o2) {
		if (o1.compareTo(o2) < 0)
			return o2;
		else
			return o1;
	}
	
	public static <T extends Comparable<? super T>> boolean bottleneckMonge(Matrix<T> m) {
		for (int i = 0; i < m.getRows(); i++)
			for (int r = i+1; r < m.getRows(); r++)
				for (int j = 0; j < m.getColumns(); j++)
					for (int s = j+1; s < m.getColumns(); s++) {
						//System.out.println("c(" + i + ", " + j + ") = " + m.get(i, j) + ", c(" + r + ", " + s + ") = " + m.get(r, s));
						//System.out.println("c(" + i + ", " + s + ") = " + m.get(i, s) + ", c(" + r + ", " + j + ") = " + m.get(r, j));
						//System.out.println("max{c(" + i + ", " + j + "), c(" + r + ", " + s + ")} = " + max(m.get(i, j),m.get(r, s)));
						//System.out.println("max{c(" + i + ", " + s + "), c(" + r + ", " + j + ")} = " + max(m.get(i, s),m.get(r, j)));
						if (max(m.get(i, j),m.get(r, s)).compareTo(max(m.get(i, s), m.get(r, j))) > 0) {
							//System.out.println("FALSE!");
							return false;
						} //else
							//System.out.println("TRUE");
					}
		return true;
	}
}
