package com.ncc.neon.query.transform.bucketing

public class DoublePoint {

	double x
	double y

	DoublePoint(double x, double y) {
		this.x =x
		this.y = y
	}

	@Override
	public String toString() {
		return String.format("%.2f, %.2f", x, y)
	}

}
