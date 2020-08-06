package extractocol.frontend.basic.pairs;

public class Quad<M, D, N, T> {
	M callee;
	D d3;
	N n;
	T tre;
	
	int hashCode;
	
	public Quad(M _m, D _d3, N _n, T _tre){
		this.callee = _m;
		this.d3 = _d3;
		this.n = _n;
		this.tre = _tre;
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callee == null) ? 0 : callee.hashCode());
		result = prime * result + ((d3 == null) ? 0 : d3.hashCode());
		result = prime * result + ((n == null) ? 0 : n.hashCode());
		result = prime * result + ((tre == null) ? 0 : tre.hashCode());
		
		this.hashCode = result;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		@SuppressWarnings("rawtypes")
		Quad other = (Quad) obj;
		
		if (callee == null) {
			if (other.callee != null)
				return false;
		} else if (!callee.equals(other.callee))
			return false;
		
		if (d3 == null) {
			if (other.d3 != null)
				return false;
		} else if (!d3.equals(other.d3))
			return false;
		
		if (n == null) {
			if (other.n != null)
				return false;
		} else if (!n.equals(other.n))
			return false;
		
		if (tre == null) {
			if (other.tre != null)
				return false;
		} else if (!tre.equals(other.tre))
			return false;
		
		return true;
	}

}
