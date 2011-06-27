package br.ufal.cideei.util.count;

import soot.Body;

public abstract class AbstractMetricsSink implements IPropertiesSink<Body, String, String> {
	@Override
	public void flow(Body id, String property, String value) {
		handle(id, property, value);
	}
	
	public void flow(Body id, String property, double value) {
		handle(id, property, value);
	}

	protected abstract void handle(Body id, String property, String value);
	
	protected abstract void handle(Body id, String property, double value);
}
