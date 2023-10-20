/**
 * IntegerUtils.java
 * Jun 9, 2010
 */
package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


/**
 * This class contains various utilities for dealing with integers.
 * This includes prime factorizations, greatest common divisors, 
 * least common multiples, etc.
 *
 * @author Brad Paynter
 * @version Jun 9, 2010
 *
 */
public class IntegerUtils {
	/**
	 * An array of primes less than or equal to <code> size </code>
	 */
	private int[] primes;
	/**
	 * An array containing the prime factors of all integers less 
	 * than or equal to <code> size </code>. It stores this as the 
	 * powers of the primes in the array <code> primes</code>.<br />
	 * i.e. if $j$ is a positive integer then<br />
	 * \[ j = prod_{i=0..}{primes[i]^factors[j-1][i]} \]
	 */
	private int[][] factors;
	/**
	 * The largest integer that this object can factor
	 */
	private int size;
	
	/**
	 * Constructs an IntegerUtils object capable of factoring integers 
	 * less than or equal to <code> size </code> into prime factors.
	 *  
	 * @param size The largest integer this object can factor
	 * @throws IllegalArgumentException Thrown if size is non-positive
	 */
	public IntegerUtils(int size) throws IllegalArgumentException {
		if (size < 1)
			throw new IllegalArgumentException("Size must be positive.");
		this.size = size;
		// Find all primes less than or equal to size
		this.primes = getPrimes(size);
		// Find prime factors of all integers less than or equal to size.
		generatePrimeFactors();
	}
	
	/**
	 * Returns the largest integer this object can factor 
	 * 
	 * @return The largest integer this object can factor
	 */
	public int size() {
		return size;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param i
	 * @return
	 */
	public static boolean isInteger(double i) {
		return (i == Math.floor(i));
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean divides(long a, long b) {
		double d = 1.0 * b / a;
		return (Math.floor(d) == d);
	}
	
	/**
	 * Generates an array of all prime integers less than or equal to 
	 * <code> size </code>. It does this using the sieve of Eratosthenes.
	 * 
	 * @param size The upper bound on primes returned
	 * @return An array containing all prime integers less than or equal 
	 * 			to <code> size </code>.
	 */
	public static int[] getPrimes(int size) {
		// Create an array of all integers from 1 to size
		int[] integers = new int[size];
		for (int i = 1; i <= size; i++)
			integers[i-1] = i;
		// Create an ArrayList to contain the primes
		ArrayList<Integer> primes = new ArrayList<Integer>();
		// For each integer greater than 2
		for (int i = 2; i <= size; i++) {
			// If the integer has not yet been eliminated by the sieve then
			// it must be prime
			if (integers[i-1] == i) {
				primes.add(i);
				// Eliminate all multiples of the new prime from the 
				// list of integers
				int j = i + i;
				while (j <= size) {
					integers[j-1] = -1;
					j = j + i;
				}
			}
		}
		// Output the ArrayList to an array
		int[] outPrimes = new int[primes.size()];
		for (int i = 0; i < primes.size(); i++)
			outPrimes[i] = primes.get(i);
		// Return the result
		return outPrimes;
	}
	
	/**
	 * This method generates all the prime factors of all positive integers
	 * less than or equal to <code> size </code> and stores them in the internal
	 * array <code> factors </code>
	 *
	 */
	private void generatePrimeFactors() {
		// Initialize the factors array
		factors = new int[size][];
		// The factors of 1 are null
		factors[0] = null;
		// For each integer from 2 to size
		for (int i = 2; i <= size; i++) {
			// Initialize an array for the prime factors
			factors[i-1] = new int[primes.length];
			Arrays.fill(factors[i-1], 0);
			// Set the current remainder
			int remainder = i;
			// Set the current prime
			int primeIndex = 0;
			// While factors still remain
			while (remainder > 1) {
				// Check if the current prime divides the remainder
				if (remainder % primes[primeIndex] == 0) {
					factors[i-1][primeIndex]++;
					remainder = remainder / primes[primeIndex];
				} else
					primeIndex++;
			}
		}
	}
	
	/**
	 * This method returns an array containing the prime factors of a given integer.
	 * 
	 * @param number The integer to be factored
	 * @return An array of the prime factors of <code> number </code>
	 * @throws IllegalArgumentException Thrown if the argument is non-positive 
	 * 			or larger than <code>size</code>
	 */
	public int[] getPrimeFactors(int number) throws IllegalArgumentException {
		// Check that this object can factor this number
		if ((number > size) || (number < 1))
			throw new IllegalArgumentException
				("Number must be a positive integer less than or equal to " + size);
		// Create an ArrayList to contain all prime factors of the number
		ArrayList<Integer> primeFactors = new ArrayList<Integer>();
		// For each prime
		for (int i = 0; i < primes.length; i++) {
			// Add the number of that prime necessary
			for (int j = 0; j < factors[number-1][i]; j++)
				primeFactors.add(primes[i]);
		}
		// Convert the ArrayList to an array
		int[] outFactors = new int[primeFactors.size()];
		for (int i = 0; i < primeFactors.size(); i++)
			outFactors[i] = primeFactors.get(i);
		// Output the result
		return outFactors;
	}
	
	/**
	 * This method is used by the <code> getFactors(int)</code> method. It is
	 * used to keep track of which combination of prime factors have been used.
	 * It does this by treating the array <code> primeArray</code> as a number
	 * where each column has base equal to the number of times that prime divides
	 * <code>number</code>. 
	 * 
	 * @param number
	 * @param primeArray
	 * @return
	 */
	private boolean increment(int number, int[] primeArray) {
		int index = 0;
		// moving along the array of primes
		while (index < primes.length) {
			// if we can add another of primes[index] then do so and exit
			if (primeArray[index] < factors[number - 1][index]) {
				primeArray[index]++;
				return true;
			} else { // if not, then blank this column and try to add one to the next column
				primeArray[index] = 0;
				index++;
			}
		}
		// we've run out of prime factors
		return false;
	}
	
	/**
	 * This method returns all factors of a given integer
	 * 
	 * @param number A positive integer less than or equal to 
	 * 		<code>size</code>
	 * @return An array containing all positive integer 
	 * 		factors of <code>number</code>
	 * @throws IllegalArgumentException Thrown if the argument is 
	 * 		non-positive or larger than <code>size</code>
	 */
	public int[] getFactors(int number) throws IllegalArgumentException {
		// Check that this object can factor this number
		if ((number > size) || (number < 1))
			throw new IllegalArgumentException
				("Number must be a positive integer less than or equal to " + size);
		// Create an index array of all primes known to this object
		int[] primeArray = new int[primes.length];
		// Set the array to zero
		Arrays.fill(primeArray, 0);
		// Create an ArrayList to hold the factors
		ArrayList<Integer> factorsOfNumber = new ArrayList<Integer>();
		// Add 1 to the list of factors
		factorsOfNumber.add(1);
		// While we still have primes to consider
		while (increment(number, primeArray)) {
			// Multiply all currently active primes to obtain the current factor
			int factor = 1;
			for (int i = 0; i < primeArray.length; i++) {
				for (int j = 0; j < primeArray[i]; j++)
					factor = factor * primes[i];
			}
			// Add the factor to the ArrayList
			factorsOfNumber.add(factor);
		}
		// Convert the ArrayList to an array
		int[] outFactors = new int[factorsOfNumber.size()];
		for (int i = 0; i < factorsOfNumber.size(); i++)
			outFactors[i] = factorsOfNumber.get(i);
		// Sort the array of factors
		Arrays.sort(outFactors);
		// Return the array
		return outFactors;
	}
	
	/**
	 * This method calculates the greatest common divisor of a set of integers.
	 * It uses the Extended Euclidian Algorithm to do so.
	 * 
	 * @param num An array of integers
	 * @return The greatest common divisor of the integers contained in <code>num</code>
	 */
	public static long gcd(long[] num) {
		// Get the number of integers
	    int size = num.length;
	    // Create a matrix (square plus one column)
	    long[][] matrix = new long[size][size + 1];
	    // Fill the matrix with zeros except for 1 on the diagonals
	    for (int i = 0; i < size; i++) {
	    	for (int j = 0; j < size; j++) {
	    		if (j == i) {
	    			// If any of the integers are negative, multiply the row by -1
	    			if (num[i] < 0) {
	    				num[i] = -num[i];
	    				matrix[i][j] = -1;
	    			} else
	    				matrix[i][j] = 1;
	    		}
	    		else
	    			matrix[i][j] = 0;
	    	}
	    	// Place the integers themselves in the last column
	    	matrix[i][size] = num[i];
	    }
	    // Create a comparator object
	    GCDVectorComparator comparator = new GCDVectorComparator();
	    // Sort the matrix so that any zero rows move to the bottom, 
	    // then sorted by increasing absolute value
	    Arrays.sort(matrix, comparator);
	    while (matrix[1][size] != 0) {
	    	// For each row in the matrix
	    	for (int i = 1; i < size; i++) {
	    		// subtract multiples of the first row from the other rows so that
	    		// each row has the remainder in the last column
	    		int mult = (int)Math.floor(matrix[i][size]/matrix[0][size]);
	    		for (int j = 0; j < size + 1; j++) {
	    			matrix[i][j] = matrix[i][j] - matrix[0][j] * mult;
	    		}
	    	}
	    	// Sort the array again
		    Arrays.sort(matrix, comparator);
	    }
	    // Return the GCD (which ends up in the top right of the matrix)
	    return matrix[0][size];
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static long gcd(long a, long b) {
		long[] n = {a, b};
		return IntegerUtils.gcd(n);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static long lcm(long a, long b) {
		long lcm = (a / IntegerUtils.gcd(a, b)) * b;
		/*
		if (lcm > Long.MAX_VALUE)
			throw new IllegalArgumentException("LCM out of bounds.");
		*/
		return lcm;
	}
	
	/**
	 * This method overrides the toString method inherited from Object.
	 * It outputs the internal data of this class in a human-readable format.
	 * 
	 * @return A human-readable String representation of this object's internal data
	 */
	@Override
	public String toString() {
		String endLn = System.getProperty("line.separator");
		String s = "";
		for (int i = 2; i <= size; i++) {
			s += i + " = ";
			boolean first = true;
			for (int j = 0; j < primes.length; j++) {
				if (factors[i-1][j] != 0) {
					if (!first)
						s += " * ";
					else
						first = false;
					s += primes[j] + "^" + factors[i-1][j];
				}
			}
			s += endLn;
		}
		return s;
	}
	
	/**
	 * This main method is used for testing this class.
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		IntegerUtils factors = new IntegerUtils(1000);
		//System.out.print(factors);
		
		int[] primeFactors = factors.getPrimeFactors(840);
		for (int i : primeFactors)
			System.out.print(i + "-");
		System.out.println();
		/*
		primeFactors = factors.getPrimeFactors(542);
		for (int i : primeFactors)
			System.out.print(i + "-");
		System.out.println();
		primeFactors = factors.getPrimeFactors(34);
		for (int i : primeFactors)
			System.out.print(i + "-");
		System.out.println();
		*/
		int[] allFactors = factors.getFactors(840);
		for (int i : allFactors)
			System.out.print(i + "-");
		System.out.println();
	}
	
	/**
	 * This class is used in the GCD calculation, enabling the use of the 
	 * Arrays.sort() method.
	 *
	 * @author Brad Paynter
	 * @version Jun 9, 2010
	 *
	 */
	private static class GCDVectorComparator implements Comparator<long[]> {

		/**
		 * Null constructor
		 */
		public GCDVectorComparator() {
			
		}
		
		/**
		 * This method compares to vectors used in the GCD calculation. It fulfills
		 * the Comparator interface.
		 * 
		 * @param o1 The first vector to be compared
		 * @param o2 The second vector to be compared
		 * @return Returns -1, 0 or 1 if the first vector is smaller than, 
		 * 		equal to or greater than the second
		 * @throws IllegalArgumentException Thrown if the two vectors to be 
		 * 		compared are not the same length
		 */
		public int compare(long[] o1, long[] o2) throws IllegalArgumentException {
			if (o1.length != o2.length) 
				throw new IllegalArgumentException("Array lengths must match.");
			int size = o1.length;
			if (Math.abs(o1[size - 1]) == Math.abs(o2[size - 1]))
				return 0;
			else if (o1[size - 1] == 0)
				return 1;
			else if (o2[size - 1] == 0)
				return -1;
			else if (Math.abs(o1[size - 1]) < Math.abs(o2[size - 1]))
				return -1;
			else
				return 1;
		}
		
	}
}
