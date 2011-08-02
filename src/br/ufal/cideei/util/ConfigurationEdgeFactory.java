package br.ufal.cideei.util;

import java.util.Set;

import org.jgrapht.EdgeFactory;

import soot.Unit;
import br.ufal.cideei.soot.instrument.IConfigRep;

public class ConfigurationEdgeFactory implements EdgeFactory<Unit, ValueContainerEdge<IConfigRep>> {

	static ConfigurationEdgeFactory instance = null;

	private ConfigurationEdgeFactory() {
	}

	public static ConfigurationEdgeFactory getInstance() {
		if (ConfigurationEdgeFactory.instance == null) {
			instance = new ConfigurationEdgeFactory();
		}
		return ConfigurationEdgeFactory.instance;
	}

	@Override
	public ValueContainerEdge<IConfigRep> createEdge(Unit source, Unit target) {
		return new ValueContainerEdge<IConfigRep>();
	}
}
