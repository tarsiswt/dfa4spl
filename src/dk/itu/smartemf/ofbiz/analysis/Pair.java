package dk.itu.smartemf.ofbiz.analysis;

/**
 * Type-parametric 2-tuple class.
 * 
 * @author <a href="mailto:matt@cis.ksu.edu">Matt Hoosier</a>
 */
public class Pair<E1, E2> {
	public final E1 first;

	public final E2 second;

	Pair(E1 first, E2 second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public int hashCode() {
		return first.hashCode() + second.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if (o instanceof Pair) {
			Pair other = (Pair) o;
			return first.equals(other.first) && second.equals(other.second);
		}
		return false;
	}

	@Override
	public String toString() {
		return "<" + first + ", " + second + ">";
	}
}
