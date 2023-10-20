/**
 * SplayTree.java
 * Nov 26, 2009
 */
package util;

import java.util.Arrays;
import java.util.Iterator;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Nov 26, 2009
 *
 */
public class SplayTree<T extends Comparable<T>> implements Iterable<T> {

	/**
	 * 
	 */
	private TreeNode root;
	/**
	 * 
	 */
	private int size;
	
	/**
	 * 
	 * TODO
	 * 
	 * Constructor for objects of type
	 */
	public SplayTree() {
		root = new TreeNode();
		size = 0;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * Constructor for objects of type 
	 * @param x
	 * @param size
	 */
	private SplayTree(TreeNode x) {
		this();
		this.size = x.subtreeSize;
		this.root.left = x;
		x.up = this.root;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param o
	 * @return
	 */
	public boolean add(T o) {
		if (root.left == null) {
			root.left = new TreeNode(o);
			root.left.up = root;
			size++;
			return true;
		} else {
			TreeNode newNode = new TreeNode(o);
			try {
				this.insert(root.left, newNode);
			} catch (IllegalAccessException e) {
				return false;
			}
			splay(newNode);
			size++;
			return true;
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 * @param newNode
	 * @throws IllegalAccessException
	 */
	private void insert(TreeNode x, TreeNode newNode) throws IllegalAccessException {
		if (x.data.compareTo(newNode.data) == 0)
			throw new IllegalAccessException("An element equal to this one already exists in this structure.");
		else if (x.data.compareTo(newNode.data) > 0) {
			newNode.succ = x;
			if (x.left == null) {
				x.left = newNode;
				newNode.up = x;
				if (newNode.pred != null)
					newNode.pred.succ = newNode;
				if (newNode.succ != null)
					newNode.succ.pred = newNode;
			} else
				insert(x.left, newNode);
			x.subtreeSize++;
		} else { 
			newNode.pred = x;
			if (x.right == null) {
				x.right = newNode;
				newNode.up = x;
				if (newNode.pred != null)
					newNode.pred.succ = newNode;
				if (newNode.succ != null)
					newNode.succ.pred = newNode;
			} else
				insert(x.right, newNode);
			x.subtreeSize++;
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 * @param o
	 * @return
	 */
	private TreeNode find(TreeNode x, T o) {
		if (x == null)
			return null;
		else if (x.data.compareTo(o) > 0) {
			if (x.left != null)
				return find(x.left, o);
			else {
				splay(x.pred);
				return x.pred;
			}
		} else if (x.data.compareTo(o) == 0) {
			splay(x);
			return x;
		} else {
			if (x.right != null)
				return find(x.right, o);
			else {
				splay(x);
				return x;
			}
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 * @param k
	 * @return
	 */
	private TreeNode find(TreeNode x, int k) {
		if (x == null)
			throw new IllegalArgumentException("This node is null!");
		if ((k >= x.subtreeSize) || (k < 0))
			throw new IllegalArgumentException("There are not that many items in this splay tree.");
		if (x.left != null) {
			if (x.left.subtreeSize == k) {
				splay(x);
				return x;
			} else if (x.left.subtreeSize > k)
				return find(x.left, k);
			else if (x.right != null)
				return find(x.right, k - x.left.subtreeSize - 1);
			else
				throw new RuntimeException("This should never happen - Something is wrong with the data structure!");
		} else {
			if (k == 0) {
				splay(x);
				return x;
			} else if (x.right != null)
				return find(x.right, k - 1);
			else
				throw new RuntimeException("This should never happen - Something is wrong with the data structure!");
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 */
	private void splay(TreeNode x) {
		while ((x != null) && (x.up != root))
			doubleRotation(x);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 */
	private void doubleRotation(TreeNode x) {
		if (x.up != root) {
			if (x == x.up.left) {
				if ((x.up.up != root) && (x.up == x.up.up.left)) {
					rotateRight(x.up.up);
				}
				rotateRight(x.up);
			} else {
				if ((x.up.up != root) && (x.up == x.up.up.right)) {
					rotateLeft(x.up.up);
				}
				rotateLeft(x.up);
			}
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 * @return
	 */
	private TreeNode rotateLeft(TreeNode x) {
		if (x.right != null) {
			TreeNode right = x.right;
			if (x == x.up.left)
				x.up.left = right;
			else
				x.up.right = right;
			right.up = x.up;
			x.right = right.left;
			if (right.left != null)
				right.left.up = x;
			right.left = x;
			x.up = right;
			if (right.right != null)
				x.subtreeSize = x.subtreeSize - right.right.subtreeSize;
			x.subtreeSize--;
			if (x.left != null)
				right.subtreeSize = right.subtreeSize + x.left.subtreeSize;
			right.subtreeSize++;
			return right;
		} else
			return x;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 * @return
	 */
	private TreeNode rotateRight(TreeNode x) {
		if (x.left != null) {
			TreeNode left = x.left;
			if (x == x.up.left)
				x.up.left = left;
			else
				x.up.right = left;
			left.up = x.up;
			x.left = left.right;
			if (left.right != null)
				left.right.up = x;
			left.right = x;
			x.up = left;
			if (left.left != null)
				x.subtreeSize = x.subtreeSize - left.left.subtreeSize;
			x.subtreeSize--;
			if (x.right != null)
				left.subtreeSize = left.subtreeSize + x.right.subtreeSize;
			left.subtreeSize++;
			return left;
		} else
			return x;
	}
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public Iterator<T> iterator() {
		return new TreeIterator(this);
	}

	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 */
	private void clear(TreeNode x) {
		if (x.left != null)
			clear(x.left);
		if (x.right != null)
			clear(x.right);
		x.delete();
	}
	
	/**
	 * TODO
	 * 
	 */
	public void clear() {
		if (root.left != null) {
			clear(root.left);
			root.left = null;
		}
		size = 0;
	}

	/**
	 * TODO
	 * 
	 * @param o
	 * @return
	 */
	public boolean contains(T o) {
		if (find(root.left, o).data.compareTo(o) == 0)
			return true;
		else
			return false;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param newTree
	 */
	public void join(SplayTree<T> newTree) {
		if ((newTree == null) || (newTree.size() < 1))
			return;
		if (root.left == null) {
			TreeNode y = newTree.firstNode();
			newTree.splay(y);
			root.left = y;
			y.up = root;
			newTree.root.delete();
			newTree.root = null;
			size = y.subtreeSize;
			newTree = null;
		} else {
			TreeNode x = lastNode();
			TreeNode y = newTree.firstNode();
			splay(x);
			newTree.splay(y);
			if (x.data.compareTo(y.data) > -1)
				throw new IllegalArgumentException("The passed tree must be strictly greater than this one.");
			else {
				x.right = y;
				x.succ = y;
				y.up = x;
				y.pred = x;
				x.subtreeSize += y.subtreeSize;
				size += y.subtreeSize;
				newTree.root.delete();
				newTree.root = null;
				newTree = null;
			}
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 * @return
	 */
	private SplayTree<T> split(TreeNode x) {
		if (x == null)
			return null;
		else if (x.succ == null)
			return new SplayTree<T>();
		else {
			splay(x);
			TreeNode newRoot = x.right;
			x.right = null;
			x.succ = null;
			x.subtreeSize -= newRoot.subtreeSize;
			size -= newRoot.subtreeSize;
			SplayTree<T> outTree = new SplayTree<T>(newRoot);
			outTree.firstNode().pred = null;
			return outTree;
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param o
	 * @return
	 */
	public SplayTree<T> split(T o) {
		TreeNode here = find(root.left, o);
		return split(here);
	}

	/**
	 * 
	 * TODO
	 * 
	 * @param k
	 * @return
	 */
	public SplayTree<T> split(int k) {
		TreeNode here = find(root.left, k);
		return split(here);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	private TreeNode firstNode() {
		TreeNode here = root.left;
		if (here == null)
			return null;
		else {
			while (here.left != null)
				here = here.left;
			return here;
		}
	}
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public T first() {
		TreeNode first = firstNode();
		if (first == null)
			return null;
		else
			return first.data;
	}

	/**
	 * TODO
	 * 
	 * @param o
	 * @return
	 */
	public T get(T o) {
		TreeNode here = find(root.left, o);
		if (here.data.compareTo(o) == 0)
			return here.data;
		else
			return null;
	}

	/**
	 * TODO
	 * 
	 * @param k
	 * @return
	 */
	public T get(int k) {
		return find(root.left, k).data;
	}

	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	private TreeNode lastNode() {
		TreeNode here = root.left;
		if (here == null)
			return null;
		else {
			while (here.right != null)
				here = here.right;
			return here;
		}
	}
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public T last() {
		TreeNode last = lastNode();
		if (last == null)
			return null;
		else
			return last.data;
	}

	/**
	 * TODO
	 * 
	 * @param o
	 * @return
	 */
	public T pred(T o) {
		TreeNode here = find(root.left, o);
		if (here.data.compareTo(o) == 0)
			return here.pred.data;
		else
			return here.data;
	}

	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 */
	private void remove(TreeNode x) {
		SplayTree<T> newTree = split(x);
		if (x.pred == null)
			clear();
		else {
			SplayTree<T> deleted = split(x.pred);
			deleted.clear();
		}
		join(newTree);
	}
	
	/**
	 * TODO
	 * 
	 * @param o
	 * @return
	 */
	public boolean remove(T o) {
		TreeNode x = find(root.left, o);
		if ((x != null) && (x.data.compareTo(o) == 0)) {
			remove(x);
			return true;
		} else
			return false;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param k
	 * @return
	 */
	public void remove(int k) {
		TreeNode x = find(root.left, k);
		remove(x);
	}

	/**
	 * TODO
	 * 
	 * @param o
	 * @return
	 */
	public T succ(T o) {
		return find(root.left, o).succ.data;
	}
	

	/**
	 * TODO
	 * 
	 * @return
	 */
	public int size() {
		return size;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 * @return
	 */
	private String texArcsInOrderTraversal(TreeNode x) {
		String s = "";
		if (x != null) {
			if (x.left != null) {
				s += texArcsInOrderTraversal(x.left);
				s += "\\draw (" + x.data + ") -- (" + x.left.data + ");" + System.getProperty("line.separator");
			}
			if (x.right != null) {
				s += texArcsInOrderTraversal(x.right);
				s += "\\draw (" + x.data + ") -- (" + x.right.data + ");" + System.getProperty("line.separator");
			}
		}
		return s;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 * @param rows
	 * @param row
	 */
	private void texNodesInOrderTraversal(TreeNode x, String[] rows, int row) {
		if (x != null) {
			texNodesInOrderTraversal(x.left, rows, row + 1);
			for (int i = 0; i < rows.length; i++) {
				if (i == row)
					rows[i] = rows[i] + "\\node[circle, draw, minimum size=10mm]	(" + x.data + ")		{" + x.data + "};" + System.getProperty("line.separator") + "&";
				else
					rows[i] = rows[i] + "&";
			}
			texNodesInOrderTraversal(x.right, rows, row + 1);
		}
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public String tex() {
		String endln = System.getProperty("line.separator");
		String s = "\\begin{figure}" + endln + "\\begin{tikzpicture}" + endln;
		if (root.left != null) {
			s += "\\matrix[row sep = 0.5cm, column sep = 0cm] {" + endln;
			String[] rows = new String[getDepth(root.left)];
			Arrays.fill(rows, "&");
			texNodesInOrderTraversal(root.left, rows, 0);
			for (int i = 0; i < rows.length; i++)
				s += rows[i] + "\\\\" + endln;
			s += "};" + endln;
			s += texArcsInOrderTraversal(root.left);
		}	
		s += "\\end{tikzpicture}" + endln + "\\end{figure}" + endln;
		return s;
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 * @param rows
	 * @param row
	 */
	private void printInOrderTraversal(TreeNode x, String[] rows, int row) {
		if (x != null) {
			printInOrderTraversal(x.left, rows, row + 1);
			for (int i = 0; i < rows.length; i++) {
				if (i == row)
					rows[i] = rows[i] + String.format("%5s", x.data);
				else
					rows[i] = rows[i] + String.format("%5s", " ");
			}
			printInOrderTraversal(x.right, rows, row + 1);
		}
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @param x
	 * @return
	 */
	private int getDepth(TreeNode x) {
		int leftDepth = 0;
		int rightDepth = 0;
		if (x.left != null)
			leftDepth = getDepth(x.left);
		if (x.right != null)
			rightDepth = getDepth(x.right);
		return Math.max(leftDepth + 1, rightDepth + 1);
	}
	
	/**
	 * 
	 * TODO
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		String endln = System.getProperty("line.separator");
		String s = "Tree:" + endln;
		if (root.left != null) {
			String[] rows = new String[getDepth(root.left)];
			Arrays.fill(rows, "");
			printInOrderTraversal(root.left, rows, 0);
			for (int i = 0; i < rows.length; i++)
				s = s + rows[i] + endln;
			s += "Successor Chain: ";
			TreeNode x = firstNode();
			while (x != null) {
				s += x.data + "-";
				x = x.succ;
			}
			s += endln + "Predecessor Chain: ";
			x = lastNode();
			while (x != null) {
				s += x.data + "-";
				x = x.pred;
			}
			s += endln;
			s += "Size: " + size;
		} else
			s = null;
		return s;
	}
	
	/**
	 * 
	 * TODO
	 *
	 * @author Brad Paynter
	 * @version Nov 30, 2009
	 *
	 */
	private class TreeNode {
		/**
		 * 
		 */
		public T data;
		/**
		 * 
		 */
		public TreeNode left;
		/**
		 * 
		 */
		public TreeNode right;
		/**
		 * 
		 */
		public TreeNode up;
		/**
		 * 
		 */
		public TreeNode pred;
		/**
		 * 
		 */
		public TreeNode succ;
		/**
		 * 
		 */
		public int subtreeSize;
		
		/**
		 * 
		 * TODO
		 * 
		 * Constructor for objects of type 
		 * @param data
		 */
		public TreeNode(T data) {
			this.data = data;
			left = null;
			right = null;
			up = null;
			pred = null;
			succ = null;
			subtreeSize = 1;
		}
		
		/**
		 * 
		 * TODO
		 * 
		 * Constructor for objects of type
		 */
		public TreeNode() {
			this(null);
		}
		
		/**
		 * 
		 * TODO
		 *
		 */
		public void delete() {
			data = null;
			left = null;
			right = null;
			up = null;
			pred = null;
			succ = null;
		}
	}
	
	/**
	 * 
	 * TODO
	 *
	 * @author Brad Paynter
	 * @version Nov 30, 2009
	 *
	 */
	private class TreeIterator implements Iterator<T> {

		/**
		 * 
		 */
		private TreeNode currentNode;
		
		/**
		 * 
		 * TODO
		 * 
		 * Constructor for objects of type 
		 * @param me
		 */
		public TreeIterator(SplayTree<T> me) {
			currentNode = me.firstNode();
		}
		
		/**
		 * TODO
		 * 
		 * @return
		 */
		public boolean hasNext() {
			if (currentNode == null)
				return false;
			else
				return true;
		}

		/**
		 * TODO
		 * 
		 * @return
		 */
		public T next() {
			if (hasNext()) {
				T out = currentNode.data;
				currentNode = currentNode.succ;
				return out;
			} else
				return null;
		}

		/**
		 * TODO
		 * 
		 */
		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}


}
