package extractocol.frontend.basic.pairs;

public class Hexa<M, D, N, T> {
	M callee;
	D d3;
	N n;
	D d1;
	D d2;
	T tre;
	
	int hashCode;
	
	public Hexa(M _m, D _d3, N _n, D _d1, D _d2, T _tre){
		this.callee = _m;
		this.d3 = _d3;
		this.n = _n;
		this.d1 = _d1;
		this.d2 = _d2;
		this.tre = _tre;
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callee == null) ? 0 : callee.hashCode());
		result = prime * result + ((d3 == null) ? 0 : d3.hashCode());
		result = prime * result + ((n == null) ? 0 : n.hashCode());
		result = prime * result + ((d1 == null) ? 0 : d1.hashCode());
		result = prime * result + ((d2 == null) ? 0 : d2.hashCode());
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
		Hexa other = (Hexa) obj;
		
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
		
		if (d1 == null) {
			if (other.d1 != null)
				return false;
		} else if (!d1.equals(other.d1))
			return false;
		
		if (d2 == null) {
			if (other.d2 != null)
				return false;
		} else if (!d2.equals(other.d2))
			return false;
		
		if (tre == null) {
			if (other.tre != null)
				return false;
		} else if (!tre.equals(other.tre))
			return false;
		
		return true;
	}
}
