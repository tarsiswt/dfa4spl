package br.ufal.cideei.util.count;

public interface IPropertiesSink<ID, P, V> {
	public void flow(ID id, P property, V value);
}
