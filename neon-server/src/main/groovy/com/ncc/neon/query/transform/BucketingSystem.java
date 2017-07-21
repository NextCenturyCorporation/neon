package bucketing;

import java.util.ArrayList;

// import java.io.BufferedWriter;
// import java.io.FileOutputStream;
// import java.io.OutputStreamWriter;
// import java.io.UnsupportedEncodingException;
// import java.io.Writer;
// try (Writer writer = new BufferedWriter(new OutputStreamWriter(
//			new FileOutputStream("mapTreeLog.txt"), "utf-8"))) {


//		writer.write(zoomLevel + " " + numDivs.x + " " + numDivs.y + "\n");
//		for (int i = 0; i < dims.length; i++) {
//		writer.write("\t" + dims[i] + "\t" + intervalList.get(i) + "\n");
//	}
//	} catch (Exception e) {
//	;
//	}
//	return null;


/*
 * All children nodes are labelled going from 0 to numNodes-1, starting at upper left and going across
 * and then down.
 * So if you say the first interval should be 90, then the top level would be:
 * 			---------
 * 			|       |
 * 			|   0   | 
 * 			|       |
 * 			---------
 * And the next:
 * 			---------
 * 			|1|2|3|4|
 * 			|-|-|-|-|
 * 			|5|6|7|8|
 * 			---------
 */
public class BucketingSystem {

	static int MAXLEVEL;
	
	TreeNode[] nodeArray;
	TwoVar[] dims;
	
	/**
	 * 
	 * @param intervalList an ordered ArrayList of ints, each specifying the width of a bucket at 
	 * 		  that level. (e.g. intervalList[3] == 45 means that buckets at level 3 should be
	 * 		  45 units wide and tall.)
	 */
	BucketingSystem(ArrayList<Double> intervalList) {

		// ArrayList<Double> intervalList = intervalSanitizer(inputIntervalList);

		MAXLEVEL = intervalList.size()-1;

		dims = new TwoVar[MAXLEVEL+1];
		
		for (int i = 0; i < intervalList.size(); i++) {
			intervalList.set(i, (Double)(intervalList.get(i).doubleValue()));

			double xDivs = 360.0/intervalList.get(i).doubleValue();
			double yDivs = 180.0/intervalList.get(i).doubleValue();
			dims[i] = new TwoVar((int)xDivs, (int)yDivs);
		}

		int arrlen = getLevelStartIndex(MAXLEVEL+1);

		nodeArray = new TreeNode[arrlen];
		for (int i = 0; i < arrlen; i++) {
			nodeArray[i] = null;
		}
		nodeArray[0] = new TreeNode(-180, 180, -90, 90, 0);
		
	}

	// ArrayList<Double> intervalSanitizer(ArrayList<Double> intervals) {
	// 	Collections.sort(intervals, Collections.reverseOrder());

	// 	ArrayList<Double> sanitizedIntervals = new ArrayList<Double>();
	// 	double prevInterval = 0;
	// 	for (int i = 0; i < intervals.size(); i++) {
	// 		if (intervals.get(i) < 0.05 || intervals.get(i) > 90) {
	// 			continue;
	// 		}

	// 		double curInterval = intervals.get(i);
	// 		if (90 % curInterval != 0) {
	// 			//set curInterval to the next-highest value which evenly divides into 90
	// 			curInterval = 90.0/(int)(90./curInterval);
	// 		}
	// 		if (curInterval != prevInterval) {
	// 			sanitizedIntervals.add(curInterval);
	// 			prevInterval = curInterval;
	// 		}
	// 	}
	// 	return sanitizedIntervals;
	// }
	
	/**
	 * 
	 * @return the index in the main array of the first node on level '$level' of the tree.
	 */
	int getLevelStartIndex(int level) {
		if (level == 0) {
			return 0;
		}
		int index = 1;
		for (int i = 0; i < level-1; i++) {
			index += dims[i].area;
		}
		
		return index;
	}
	
	
	/**
	 * @param zoomLevel
	 * @return an array of all non-empty buckets
	 */
	Bucket[] getBucketsAtLevel(int zoomLevel) {

		TwoVar numDivs = getDivsAtLevel(zoomLevel);


		Bucket buckets[] = new Bucket[numDivs.x*numDivs.y];
		
		int i0 = getLevelStartIndex(zoomLevel);
		int bucketIndex = 0;
		
		for (int i = 0; i < numDivs.x*numDivs.y; i++) {

			TreeNode curNode = nodeArray[i+i0];
			if (curNode != null) {
				buckets[bucketIndex] = curNode.toBucket();
				bucketIndex++;
			}
		}
		
		return buckets;
	}

	
	TwoVar getDivsAtLevel(int level) {
		if (level == 0) {
			return new TwoVar(1,1);
		}
		return dims[level-1];
	}
	
	/**
	 * This divides the entire globe into a number of boxes associated with a particular zoom level,
	 * and then connects them using a continuous z-order curve. 
	 * Input is the x and y lat/lon coordinates, output is the index along the z-order curve of the box in which that point sits,
	 * at the current zoom level.
	 * 
	 * @return 	the index along the z-order curve of the box in which that point sits,
	 *			at the current zoom level.
	 */
	int getIndexOffsetOfNodeWithPoint(double x, double y, int level) {

		Region worldBounds = nodeArray[0].bounds;
		
		TwoVar divs = getDivsAtLevel(level);

		int childXindex = (int)(divs.x * (x - worldBounds.w) / (worldBounds.e - worldBounds.w));
		int childYindex = (int)(divs.y * (worldBounds.n - y) / (worldBounds.n - worldBounds.s));

		int base = Math.min( divs.x, divs.y);
		if (base == 1) {
			base = Math.max( divs.x, divs.y);
		}

		int indexOffset = interweaveIndices(childXindex, 
											childYindex, 
											base);
		return indexOffset;
	}
	
	
	/**
	 * This converts the x,y coordinates on a grid into a single index along a z-order curve
	 * representing that grid.
	 * https://en.wikipedia.org/wiki/Z-order_curve
	 * z-order usually uses base 2, but since we want to be able to split the grid into
	 * arbitrarily-sized boxes, we need to change it to use any base.
	 * 
	 * @param i1 the x-index of the grid square
	 * @param i2 the y-index of the grid square
	 * @param base the number of children the grid squares parent was split into.
	 * 		So if the grid square is one of a 3x3 group, then the base would be 3.
	 * 		Note that the width and height of the group have to be either equal or one must be 
	 * 			some integer multiple of the other. 
	 * 			E.g., 2x4 works, 3x5 does not. 
	 * 
	 * @return the index in a z-order curve that has been mapped onto the grid at it's current level.
	 * 			On fail, returns null.
	 */
	Integer interweaveIndices( int i1, int i2, int base ) {
		//https://en.wikipedia.org/wiki/Z-order_curve
		//z-order usually uses base 2, but since we want to be able to split the grid into
		//arbitrarily-sized boxes, we need to change it to use any base.
		//Base == min(width, height), where width and height are the  
		
		int iRes = 0;
		long digitNum = 1;
		
		if (base < 2 && !(i1 == 0 && i2 == 0)) {
			return null;
		}
		
		while ( (i1|i2) != 0) {
			iRes += digitNum * (i1 % base);
			iRes += digitNum * base * (i2 % base);
			digitNum *= base * base;
			i1 /= base;
			i2 /= base;
		}
		
		
		return iRes;
	}
	
	TwoVar extricateIndices(int i, int base ) {
		//https://en.wikipedia.org/wiki/Z-order_curve
		//z-order usually uses base 2, but since we want to be able to split the grid into
		//arbitrarily-many boxes, we need to change it to use any base.
		
		int i1 = 0;
		int i2 = 0;
		int digitNum = 1;
		
		if (base < 2 && !(i1 == 0 && i2 == 0)) {
			return null;
		}
		
		while ( i != 0) {
			i1 += digitNum * (i%base);
			i /= base;
			i2 += digitNum * (i%base);
			digitNum *= base;
			i /= base;
		}
		
		
		return new TwoVar(i1, i2);
	}
	
	boolean insert(double x, double y) {
		if (Math.abs(x) == 180) {
			x -= x/Math.abs(x) * 0.01;
		}
		if (Math.abs(y) == 90)  {
			y -= y/Math.abs(y) * 0.01;
		}
		if (Math.abs(x) > 180 || Math.abs(y) > 90)  {
			return false;
		}
		
		TreeNode parentNode = nodeArray[0];

		for (int l = 0; l <= MAXLEVEL; l++) {
			int baseIndex = getLevelStartIndex(l);
			
			int offsetIndex = getIndexOffsetOfNodeWithPoint(x, y, l);
			int index = baseIndex + offsetIndex;
//			System.out.println(l + " " + baseIndex + " " + offsetIndex);
			
			if (nodeArray[index] == null) {
				nodeArray[index] = new TreeNode(parentNode.getRegion(offsetIndex), l);
			}
			nodeArray[index].addPoint(x, y);
			
			parentNode = nodeArray[index];
		}
		
		return true;
	}
	
	
	class TreeNode {
		int level;
		Region bounds;
		double xCen;
		double yCen;
		long count;

		TreeNode(double w, double e, double s, double n, int level) {
			this(new Region(w, e, s, n), level);
		}
		
		TreeNode(Region r, int lev) {
			bounds = r;
			count = 0;
			level = lev;
			xCen = 0;
			yCen = 0;
		}
		
		
		void addPoint(double x, double y) {
			xCen = xCen*count/(count+1) + x/(count+1);
			yCen = yCen*count/(count+1) + y/(count+1);
			count++;
		}
		
		/**
		 * 
		 * @param index the index (along the z-curve) of the desired sub-region within the current node
		 * @return the sub-region corresponding to the given index 
		 */
		Region getRegion(int index) {
			Region worldBounds = nodeArray[0].bounds;
			
			TwoVar divs = getDivsAtLevel(level+1);

			double w = (worldBounds.e - worldBounds.w)/divs.x;
			double h = (worldBounds.n - worldBounds.s)/divs.y;
			

			int base = Math.min( divs.x, divs.y);
			if (base == 1) {
				base = Math.max( divs.x, divs.y);
			}
			
			TwoVar indices = extricateIndices(index, base);

			double xOffset = w * indices.x;
			double yOffset = h * indices.y;

			return new Region(	worldBounds.w + xOffset, worldBounds.w + xOffset + w,
								worldBounds.n - yOffset - h, worldBounds.n - yOffset);
		}
		
		Bucket toBucket() {
			// TwoVar indices = extricateIndices(index, base);
			// return new Bucket(bounds.w, bounds.e, bounds.s, bounds.n, xI, yI, xCen, yCen, count);
			return new Bucket(bounds.w, bounds.e, bounds.s, bounds.n, xCen, yCen, count);
		}
		
		@Override
		public String toString() {
			return "Node: c=" + count + " @ " + bounds + " | cen=" + xCen + " " + yCen;
		}
		
	}
	
	/**
	 * Container for holding two dimensions or variables
	 * @author chill
	 */
	class TwoVar{
		int x;
		int y;
		int area; //for when x and y are dimensions
		
		TwoVar(int x, int y) {
			this.x = x;
			this.y = y;
			this.area = x*y;
		}
		public String toString() {
			return x + " " + y;
		}
	}
}
