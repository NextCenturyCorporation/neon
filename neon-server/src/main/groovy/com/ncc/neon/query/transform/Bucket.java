package bucketing;

/**
 * Simple container for passing bucket data between the MapTree java class and the BucketingTransformer
 * 	groovy class.  
 * 
 * @author chill
 */
public class Bucket {

	private double l;
	private double r;
	private double t;
	private double b;

	private int xIdx; //x-index on grid
	private int yIdx; //y-index on grid

	private double xCen; //x coord of centroid
	private double yCen; //y coord of centroid
	
	private long count;
	
	
	public Bucket(double left, double right, double top, double bottom,
				  double xC, double yC, 
				  long c) {
		l = left;
		r = right;
		t = top;
		b = bottom;
		// xIdx = xI;//x index
		// yIdx = yI;
		xCen = xC;//x centroid
		yCen = yC;
		count = c;
	}
	
	public double l() {
		return l;
	}
	
	public double r() {
		return r;
	}
	
	public double t() {
		return t;
	}
	
	public double b() {
		return b;
	}
	
	public double xCen() {
		return xCen;
	}
	
	public double yCen() {
		return yCen;
	}
	
	// public double xIdx() {
	// 	return xIdx;
	// }
	
	// public double yIdx() {
	// 	return yIdx;
	// }
	
	public long count() {
		return count;
	}
	
	@Override
	public String toString() {
		return "lrtb | cenXY | count: " + l + ",\t" + r + ",\t" + t + ",\t" + b 
				+ "\t | " + xCen + ",\t" + yCen
				+ "\t | " + count;
	}
}




