package bucketing;

public class Region {
	double w;
	double s;
	double e;
	double n;
	double WEcen;
	double SNcen;
	
	Region(double w, double e, double s, double n) {
		this.w = w;
		this.e = e;
		this.s = s;
		this.n = n;
		this.WEcen = (w+e)/2;
		this.SNcen = (s+n)/2;
	}
	
	Region quad(int id) {
		switch (id) {
		case 0:
			return nwQuad();
		case 1:
			return neQuad();
		case 2:
			return swQuad();
		case 3:
			return seQuad();
		default:
			return null;
		}
	}
	
	Region nwQuad() {
		return new Region(w, WEcen, SNcen, n);
	}
	Region neQuad() {
		return new Region(WEcen, e, SNcen, n);
	}
	Region swQuad() {
		return new Region(w, WEcen, s, SNcen);
	}
	Region seQuad() {
		return new Region(WEcen, e, s, SNcen);
	}
	
	
	boolean contains(double w, double e, double s, double n) {
		return this.w <= w && this.e >= e && this.s <= s && this.n >= n;
	}
	boolean contains(Region r) {
		return this.contains(r.w, r.e, r.s, r.n);
	}
	boolean contains(DoublePoint p) {
		return this.contains(p.x, p.x, p.y, p.y);
	}
	
	boolean intersects(Region r) {
		return !((r.e < this.w) || (this.e < r.w) || (r.n < this.s) || (this.n < r.s));
	}
	
	@Override
	public String toString() {
		return String.format("%.3f   %.3f   %.3f   %.3f   %.3f   %.3f", w, e, s, n, WEcen, SNcen);
	}
}
