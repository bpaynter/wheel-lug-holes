package util;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Permutation.java
 */

/**
 * This class represents a permutation. It can either store a permutation
 * (which can be modified by various methods) or, through the Iterable interface
 * it can iterate over all possible permutations of a given length. The iterator
 * runs in constant amortized time.
 *
 * @author Brad Paynter
 * @version June 10, 2010
 *
 */
public class Permutation implements Iterable<int[]> {
	
	/**
	 * The current permutation. This is simply a vector of indices in 
	 * their permutation order
	 */
	private int[] perm;
	/**
	 * The size of the set being permuted
	 */
	private int size;
	
	public static final long[] factorial = {1l,							//0!
											1l,							//1!
											2l,							//2!
											6l,							//3!
											24l,						//4!
											120l,						//5!
											720l,						//6!
											5040l,						//7!
											40320l,						//8!
											362880l,					//9!
											3628800l,					//10!
											39916800l,					//11!
											479001600l,					//12!
											6227020800l,				//13!
											87178291200l,				//14!
											1307674368000l,				//15!
											20922789888000l,			//16!
											355687428096000l,			//17!
											6402373705728000l,			//18!
											121645100408832000l,		//19!
											2432902008176640000l,		//20!
											Long.MAX_VALUE};
	
	/**
	 * Constructs an identity Permutation object of size <code>size</code> 
	 * @param size The size of the set to be permuted
	 */
	public Permutation(int size) {
		this.size = size;
		perm = new int[size];
		for (int i = 0; i < size; i++)
			perm[i] = i;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param perm
	 * @throws IllegalArgumentException
	 */
	public Permutation(int[] perm) throws IllegalArgumentException {
		if (correctPermutation(perm)) {
			this.size = perm.length;
			this.perm = perm.clone();
		} else
			throw new IllegalArgumentException("Not a valid Permutation.");
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param size
	 * @param random
	 * @return
	 */
	public static int[] randomPermutation(int size, MersenneTwisterFast random) {
		int[] permutation = new int[size];
		int[] indices = new int[size];
		for (int i = 0; i < size; i++)
			indices[i] = i;
		for (int i = 0; i < size - 1; i++) {
			int index = random.nextInt(size - i - 1);
			permutation[i] = indices[index];
			indices[index] = indices[size - 1 - i];
		}
		permutation[size - 1] = indices[0];
		return permutation;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param index
	 * @return
	 */
	public static int[] getPerm(int size, long index) throws IllegalArgumentException {
		if ((size <=0 ) || (size > 20))
			throw new IllegalArgumentException("This method only supports permutations of length 0 < l < 21.");
		if (index < 0)
			throw new IllegalArgumentException("Index must be non-negative.");
		if (index >= factorial[size])
			throw new IllegalArgumentException("Index must be less than (size)!.");
		int[] perm = new int[size];
		perm[size - 1] = 0;
		for (int j = 1; j < size; j++) {
			Long d = (index % factorial[j + 1]) / factorial[j];
			index = index - d * factorial[j];
			perm[size - j - 1] = d.intValue();
			for (int i = size - j; i < size; i++)
				if (perm[i] >= d)
					perm[i]++;
		}
		return perm;
	}
	
	/**
	 * Outputs the current permutation as an array of integer indices
	 * 
	 * @return The current permutation
	 */
	public int[] getPerm() {
		return perm.clone();
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param perm
	 * @throws IllegalArgumentException
	 */
	public void setPerm(int[] perm) throws IllegalArgumentException {
		if (correctPermutation(perm)) {
			this.size = perm.length;
			this.perm = perm.clone();
		} else
			throw new IllegalArgumentException("Not a valid Permutation.");
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param size
	 * @param index
	 * @throws IllegalArgumentException
	 */
	public void setPerm(int size, long index) throws IllegalArgumentException {
		this.size = size;
		this.perm = getPerm(size, index);
	}
	
	/**
	 * Resets the permutation back to the identity
	 */
	public void reset() {
		for (int i = 0; i < size; i++)
			perm[i] = i;
	}
	
	/**
	 * Changes this permutation to the next permutation in lexicographical order.
	 * This operation runs in constant amortized time.
	 * 
	 * @return True if the permutation has changed, false if the permutation was
	 * 				already the last permutation in lexicographical order
	 */
	public boolean nextPerm() {
		int i = size - 1;
		boolean exit = false;
		while (!exit) {
			if (i > 0) {
				if (perm[i-1] > perm[i])
					i--;
				else
					exit = true;
			} else
				exit = true;
		} 
		i--;
		if (i>-1) {
			int[] temp = new int[size - i];
			for (int k = size - 1; k > i; k--) {
				temp[k-i] = perm[k];
			}
			int j = size - 1;
			while (perm[j] < perm[i])
				j--;
			temp[0] = perm[j];
			temp[j-i] = perm[i];
			perm[i] = temp[0];
			for (int k = 1; k < size - i; k++) {
				perm[i + k] = temp[temp.length - k];
			}
			return true;
		} else
			return false;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param permutation
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static String printPerm(int[] permutation) throws IllegalArgumentException {
		if (!Permutation.correctPermutation(permutation))
			throw new IllegalArgumentException("Invalid Permutation.");
		String s = String.valueOf(permutation[0]);
		for (int i = 1; i < permutation.length; i++)
			s = s + "-" + permutation[i];
		return s;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param permutation
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static int[] inversePerm(int[] permutation) throws IllegalArgumentException {
		if (!Permutation.correctPermutation(permutation))
			throw new IllegalArgumentException("Invalid Permutation.");
		int[] inverse = new int[permutation.length];
		for (int i = 0; i < permutation.length; i++)
			inverse[permutation[i]] = i;
		return inverse;
	}
	
	/**
	 * Outputs the current permutation in string format
	 * 
	 * @return A String representation of the current permutation 
	 */
	public String toString() {
		return Permutation.printPerm(this.perm);
	}
	

	/**
	 * Returns an iterator over all permutations of the size of this Permutation object.
	 * The iterator returns the permutations in lexicographic order and runs in 
	 * constant amortized time.
	 * 
	 * @return An iterator over all permutations
	 */
	public Iterator<int[]> iterator() {
		return new PermutationIterator(size);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param index
	 * @return
	 */
	public static Iterator<int[]> iterator(int size, long index) {
		return iterator(getPerm(size, index));
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param perm
	 * @return
	 */
	public static Iterator<int[]> iterator(int[] perm) {
		Permutation permutation = new Permutation(0);
		return permutation.new PermutationIterator(perm);
	}
	
	/**
	 * Checks an array to verify that it is a correct permutation.
	 * 
	 * @param permutation The array to be tested
	 * @return true if the array represents a permutation, false else
	 */
	public static boolean correctPermutation(int[] permutation) {
		boolean[] check = new boolean[permutation.length];
		Arrays.fill(check, false);
		for (int i = 0; i < permutation.length; i++) {
			// Check that the indices are valid
			if ((permutation[i] < 0) || (permutation[i] > permutation.length - 1))
				return false;
			else if (!check[permutation[i]]) // Check that no index is used twice
				check[permutation[i]] = true;
			else
				return false;
		}
		return true;
		
	}

	/**
	 * This main method is used for testing this class
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		Permutation perm = new Permutation(4);
		System.out.println(perm);
		while (perm.nextPerm()) {
			System.out.println(perm);
		}
		System.out.println("=====================");
		Iterator<int[]> permIterator = Permutation.iterator(4, 15l);
		while (permIterator.hasNext()) {
			int[] currentPerm = permIterator.next();
			for (int i = 0; i < currentPerm.length; i++)
				System.out.print(currentPerm[i] + "-");
			System.out.println();
		}
			
	}
	
	/**
	 * This class is for iterating over permutations. It contains its own
	 * Permutation object which it initializes at the identity permutation
	 * and then increments in lexicographic order
	 *
	 * @author Brad Paynter
	 * @version Jun 10, 2010
	 *
	 */
	private class PermutationIterator implements Iterator<int[]> {
		/**
		 * The current permutation
		 */
		private Permutation perm;
		/**
		 * A flag determining whether the iterator has reached
		 * the end of the ordering of permutations
		 */
		private boolean hasNext;
		
		/**
		 * Constructs a PermutationIterator object of size <code>size</code>	
		 * 
		 * @param size The size of the set being permuted
		 */
		public PermutationIterator(int size) {
			perm = new Permutation(size);
			hasNext = true;
		}
		
		public PermutationIterator(int[] perm) throws IllegalArgumentException {
			this.perm = new Permutation(perm);
			hasNext = true;
		}
		
		/**
		 * Determines whether this iterator has reached the end of the permutations
		 * 
		 * @return True if another permutation exists, false else
		 */
		public boolean hasNext() {
			return hasNext;
		}

		/**
		 * Returns the next permutation in the lexicographic order
		 * 
		 * @return The next permutation
		 */
		public int[] next() {
			if (hasNext) {
				int[] outPerm = perm.getPerm();
				hasNext = perm.nextPerm();
				return outPerm;
			} else
				return null;
		}

		/**
		 * This method is not implemented since the set of all possible 
		 * permutations is immutable.
		 * 
		 * @throws UnsupportedOperationException Always thrown since this method 
		 * 		is not supported by this iterator
		 */
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Remove is not supported by this iterator.");
		}
		
	}
}
