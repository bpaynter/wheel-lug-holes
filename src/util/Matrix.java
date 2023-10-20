/**
 * Matrix.java
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
public interface Matrix<T extends Comparable<? super T>> {
	public int getRows();
	public int getColumns();
	public T get(int i, int j);
}
