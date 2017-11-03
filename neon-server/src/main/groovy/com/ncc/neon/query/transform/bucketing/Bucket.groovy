package com.ncc.neon.query.transform.bucketing

/**
 * Simple container for passing bucket data between the MapTree java class and the BucketingTransformer
 * 	groovy class.
 *
 * @author chill
 */
public class Bucket {

    private final double l
    private final double r
    private final double t
    private final double b
    private final double xCen //x coord of centroid
    private final double yCen //y coord of centroid

    private final long count

    //    private int xIdx //x-index on grid
    //    private int yIdx //y-index on grid

    public Bucket(double left, double right, double top, double bottom,
    double xC, double yC, long c) {
        l = left
        r = right
        t = top
        b = bottom
        // xIdx = xI;//x index
        // yIdx = yI;
        xCen = xC//x centroid
        yCen = yC
        count = c
    }

    @Override
    public String toString() {
        return "lrtb | cenXY | count: " + l + ",\t" + r + ",\t" + t + ",\t" + b + "\t | " + xCen + ",\t" + yCen + "\t | " + count
    }
}




