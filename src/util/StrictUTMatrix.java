/**
 * UpperTriangularMatrix.java
 * Nov 2, 2009
 */
package util;

/**
 * This class provides static methods for storing a strict upper triangular matrix
 * in a one dimensional array.
 *
 * @author Brad Paynter
 * @version Nov 2, 2009
 *
 */
public class StrictUTMatrix {

	/**
	 * Calculates the index in the vector that corresponds to a given position
	 * in the upper triangular matrix
	 * 
	 * @param size The number of rows (or columns) in the matrix
	 * @param i A row in the matrix
	 * @param j A column in the matrix
	 * @return The position in the vector of the data from position (i,j) of the matrix
	 */
	public static int index(int size, int i, int j) {
		if (i < j) {
			return size*i + j - i*(i+1)/2 - i - 1;
		} else 
			throw new IllegalArgumentException("This matrix is strictly upper triangular");
	}
	
	/**
	 * Calculates the size of vector needed to store a square, strictly upper
	 * triangular matrix.
	 * 
	 * @param size The number of rows (or columns) in the matrix
	 * @return The size of the vector required.
	 */
	public static int length(int size) {
		return (size * (size - 1)) / 2;
	}
}
