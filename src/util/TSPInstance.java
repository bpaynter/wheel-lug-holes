/**
 * TSPInstance.java
 * Feb 7, 2011
 */
package util;

/**
 * TODO
 *
 * @author Brad Paynter
 * @version Feb 7, 2011
 *
 */
public interface TSPInstance {
	int numCities();
	double distance(int cityOne, int cityTwo);

}
