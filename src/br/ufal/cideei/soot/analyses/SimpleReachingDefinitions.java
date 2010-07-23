package br.ufal.cideei.soot.analyses;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class SimpleReachingDefinitions 
{
	private HashMap<Unit,List<Definition>> unitToDefinitionAfter;
	private HashMap<Unit,List<Definition>> unitToDefinitionBefore;
	
	public SimpleReachingDefinitions(DirectedGraph<Unit> graph)
	{
		SimpleReachingDefinitionsAnalysis analysis = new SimpleReachingDefinitionsAnalysis(graph);

		this.unitToDefinitionAfter = new HashMap<Unit,List<Definition>>(graph.size() * 2 + 1, 0.7f);
		this.unitToDefinitionBefore = new HashMap<Unit,List<Definition>>(graph.size() * 2 + 1, 0.7f);

		for (Unit unit: graph)
		{
			FlowSet set = (FlowSet) analysis.getFlowBefore(unit);
			this.unitToDefinitionBefore.put(unit,
					Collections.unmodifiableList(set.toList()));

			set = (FlowSet) analysis.getFlowAfter(unit);
			this.unitToDefinitionAfter.put(unit,
					Collections.unmodifiableList(set.toList()));
		}
	}

	public List<Definition> getReachingDefinitionsAfter(Unit _unit) {
		return this.unitToDefinitionAfter.get(_unit);
	}

	public List<Definition> getReachingDefinitionsBefore(Unit _unit) {
		return this.unitToDefinitionBefore.get(_unit);
	}	

}