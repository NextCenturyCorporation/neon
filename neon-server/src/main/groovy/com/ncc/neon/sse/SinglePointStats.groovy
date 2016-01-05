package com.ncc.neon.sse

class SinglePointStats {
	// The total value of the "slice" this corresponds to across all iterations so far.
	double totalMean

	// The total value of the square of the "slice" this corresponds to across all iterations so far.
	double totalVar

	// The error of the "slice" this corresponds to.
	double error
}