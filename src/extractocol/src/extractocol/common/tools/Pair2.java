package extractocol.common.tools;

public class Pair2<M,N> {
	M e1;
	N e2;
	
	public Pair2(M _e1, N _e2) {
		this.e1 = _e1;
		this.e2 = _e2;
	}
	
	public M getE1() { return this.e1; }
	public void setE1(M _e1) { this.e1 = _e1; }
	
	public N getE2() { return this.e2; }
	public void setE2(N _e2) { this.e2 = _e2; }
	
	public void clear() {
		this.e1 = null;
		this.e2 = null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Pair2<M,N> other = (Pair2<M,N>) obj;
		if (e1 == null) {
			if (other.e1 != null)
				return false;
		} else if (!e1.equals(other.e1))
			return false;
		
		if (e2 == null) {
			if (other.e2 != null)
				return false;
		} else if (!e2.equals(other.e2))
			return false;
		
		return true;
	}
}
