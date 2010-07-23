package br.ufal.cideei.soot.analyses;

import java.util.List;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class SimpleReachingDefinitionsAnalysis extends ForwardFlowAnalysis<Unit, FlowSet> {

	private FlowSet emptySet;
	
	public SimpleReachingDefinitionsAnalysis(DirectedGraph<Unit> _graph) {
		super(_graph);
		
		this.emptySet = new ArraySparseSet();
		
		super.doAnalysis();
	}

	// ForwardFlowAnalysis implementation --------------------------
	
	@Override
	protected void copy(FlowSet _source, FlowSet _dest) {
//		System.out.println("[copy] source set -> " + _source);
		_source.copy(_dest);
//		System.out.println("[copy] dest set -> " + _dest);
	}

	@Override
	protected void merge(FlowSet _source1, FlowSet _source2, FlowSet _dest) {
//		System.out.println("[merge] ------------------------------");
//		System.out.println("[merge] source set1 -> " + _source1);
//		System.out.println("[merge] source set2 -> " + _source2);
		_source1.union(_source2, _dest);
//		System.out.println("[merge] dest set -> " + _dest);
//		System.out.println("[merge] ------------------------------");
	}

	@Override
	protected FlowSet entryInitialFlow() {
		return this.emptySet.clone();
	}
	
	@Override
	protected FlowSet newInitialFlow() {
		return this.emptySet.clone();
	}

	@Override
	protected void flowThrough(FlowSet _source, Unit _unit, FlowSet _dest)
	{
//		System.out.println("[flowThrough] ------------------------------");
//		System.out.println("[flowThrough] unit -> " + _unit.toString());
//		System.out.println("[flowThrough] in -> " + _source.toString());
//		System.out.println("[flowThrough] out -> " + _dest.toString());
		kill(_source, _unit, _dest);
		gen(_dest, _unit);
//		System.out.println("[flowThrough] in -> " + _source.toString());
//		System.out.println("[flowThrough] out -> " + _dest.toString());
//		System.out.println("[flowThrough] ------------------------------");
	}
	
	private void kill(FlowSet _source, Unit _unit, FlowSet _dest)
	{
		FlowSet kills = this.emptySet.clone();
		List<Definition> sourceList = _source.toList(); 

		for (ValueBox valueBox: _unit.getDefBoxes())
		{
			Value value = valueBox.getValue();
			
			if (value instanceof Local)
			//if (value instanceof FieldRef)
			{
				for (Definition definition: sourceList)
				{
					if (definition.definesLocal((Local)value))
						kills.add(definition);
				}
			}
		}
		
		_source.difference(kills,_dest);
	}
	
	private void gen(FlowSet _dest, Unit _unit)
	{
		for (ValueBox valueBox: _unit.getDefBoxes())
		{
			Value value = valueBox.getValue();

//			if (value instanceof Local)
			if (value instanceof Local)
			{
				_dest.add(new Definition(((Local)value),_unit));
			}
		}
	}

}
