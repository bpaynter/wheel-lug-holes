/**
 * MatrixQuadTree.java
 * Sep 8, 2010
 */
package util;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Sep 8, 2010
 *
 */
public class MatrixQuadTree {
	private int dataSize;
	private double[] data;
	private QuadTreeNode root;
	
	private static final int topRight = 0;
	private static final int topLeft = 1;
	private static final int bottomRight = 2;
	private static final int bottomLeft = 3;
	private static final int x1 = 0;
	private static final int y1 = 1;
	private static final int x2 = 2;
	private static final int y2 = 3;
	

	
	public MatrixQuadTree(int dataSize, double[] data) throws IllegalArgumentException {
		this.dataSize = dataSize;
		this.data = data;
		if (data.length != StrictUTMatrix.length(dataSize))
			throw new IllegalArgumentException("Data matrix is the wrong size.");
		int[] coordinates = new int[4];
		coordinates[x1] = 1;
		coordinates[y1] = 0;
		coordinates[x2] = dataSize;
		coordinates[y2] = dataSize - 1;
		root = nodeFactory(null, data, coordinates);
	}
	
	private QuadTreeNode nodeFactory(QuadTreeNode parent, double[] data, int[] coordinates) {
		if ((coordinates[x1] + 1 > coordinates[x2]) || (coordinates[y1] + 1 > coordinates[y2]))
			return null;
		else {
			QuadTreeNode outNode = new QuadTreeNode(parent, coordinates);
			if ((coordinates[x1] + 1 == coordinates[x2]) && (coordinates[y1] + 1 == coordinates[y2])) {
				for (int i = 0; i < 4; i++)
					outNode.children[i] = null;
				outNode.dataRow = coordinates[y1];
				outNode.dataCol = coordinates[x1];
				outNode.subTreeMin = data[StrictUTMatrix.index(dataSize, outNode.dataRow, outNode.dataCol)];
			} else {
				outNode.subTreeMin = Double.POSITIVE_INFINITY;
				if ((coordinates[x1] == coordinates[y1] + 1) && (coordinates[x2] == coordinates[y2] + 1)) {
					outNode.xmid = (coordinates[x1] + (coordinates[x2] - coordinates[x1]) / 2);
					outNode.ymid = outNode.xmid;
					outNode.children[bottomLeft] = null;
					int[] topLeftCoordinates = {coordinates[x1], coordinates[y1], outNode.xmid, outNode.ymid - 1};
					outNode.children[topLeft] = nodeFactory(outNode, data, topLeftCoordinates);
					int[] topRightCoordinates = {outNode.xmid, coordinates[y1], coordinates[x2], outNode.ymid};
					outNode.children[topRight] = nodeFactory(outNode, data, topRightCoordinates);
					int[] bottomRightCoordinates = {outNode.xmid + 1, outNode.ymid, coordinates[x2], coordinates[y2]};
					outNode.children[bottomRight] = nodeFactory(outNode, data, bottomRightCoordinates);
				} else {
					outNode.xmid = (coordinates[x1] + (coordinates[x2] - coordinates[x1] + 1) / 2);
					outNode.ymid = (coordinates[y1] + (coordinates[y2] - coordinates[y1] + 1) / 2);
					int[] bottomLeftCoordinates = {coordinates[x1], outNode.ymid, outNode.xmid, coordinates[y2]};
					outNode.children[bottomLeft] = nodeFactory(outNode, data, bottomLeftCoordinates);
					int[] topLeftCoordinates = {coordinates[x1], coordinates[y1], outNode.xmid, outNode.ymid};
					outNode.children[topLeft] = nodeFactory(outNode, data, topLeftCoordinates);
					int[] topRightCoordinates = {outNode.xmid, coordinates[y1], coordinates[x2], outNode.ymid};
					outNode.children[topRight] = nodeFactory(outNode, data, topRightCoordinates);
					int[] bottomRightCoordinates = {outNode.xmid, outNode.ymid, coordinates[x2], coordinates[y2]};
					outNode.children[bottomRight] = nodeFactory(outNode, data, bottomRightCoordinates);
				}
				for (int i = 0; i < 4; i ++)
					if (outNode.children[i] != null)
						outNode.subTreeMin = Math.min(outNode.subTreeMin, outNode.children[i].subTreeMin);
			}
			return outNode;
		}
	}
	
	public double findMin(int index) {
		int[] searchBox = {index, 0, dataSize, index};
		return findMin(root, searchBox);
	}
	
	private double findMin(QuadTreeNode node, int[] searchBox) {
		if (node == null)
			return Double.POSITIVE_INFINITY;
		if ((searchBox[x1] > node.coordinates[x2]) || (searchBox[x2] < node.coordinates[x1]))
			return Double.POSITIVE_INFINITY;
		if ((searchBox[y1] > node.coordinates[y2]) || (searchBox[y2] < node.coordinates[y1]))
			return Double.POSITIVE_INFINITY;
		if ((searchBox[x1] <= node.coordinates[x1]) && (searchBox[x2] >= node.coordinates[x2]))
			if ((searchBox[y1] <= node.coordinates[y1]) && (searchBox[y2] >= node.coordinates[y2]))
				return node.subTreeMin;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < 4; i++)
			min = Math.min(min, findMin(node.children[i], searchBox));
		return min;
	}
	
	public static void main(String[] args) {
		int dataSize = 11;
		double[] data = new double[StrictUTMatrix.length(dataSize)];
		for (int i = 0; i < data.length; i++)
			data[i] = i;
		MatrixQuadTree tree = new MatrixQuadTree(dataSize, data);
		System.out.println(tree.findMin(7));
	}
	
	
	private class QuadTreeNode {
		public double subTreeMin;
		public int[] coordinates;
		public int xmid;
		public int ymid;
		public int dataRow;
		public int dataCol;
		public QuadTreeNode[] children;
		public QuadTreeNode parent;
		
		private QuadTreeNode(QuadTreeNode parent, int[] coordinates) {
			this.parent = parent;
			this.coordinates = coordinates;
			children = new QuadTreeNode[4];
			dataRow = -1;
			dataCol = -1;
		}
	}
}
