package br.ufal.cideei.soot.analyses;

import java.util.List;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class SimpleReachingDefinitionsRmk extends ForwardFlowAnalysis<Unit, FlowSet> {

	private FlowSet emptySet;

	public SimpleReachingDefinitionsRmk(DirectedGraph<Unit> graph) {
		super(graph);
		this.emptySet = new ArraySparseSet();
		super.doAnalysis();
	}

	@Override
	protected void copy(FlowSet source, FlowSet dest) {
		source.copy(dest);
	}

	@Override
	protected void merge(FlowSet source1, FlowSet source2, FlowSet dest) {
		source1.union(source2, dest);
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
	protected void flowThrough(FlowSet source, Unit unit, FlowSet dest) {
		kill(source, unit, dest);
		gen(dest, unit);
	}

	private void kill(FlowSet src, Unit unit, FlowSet dest) {
		FlowSet kills = emptySet.clone();
		if (unit instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) unit;
			for (Object earlierAssignment : src.toList()) {
				if (earlierAssignment instanceof AssignStmt) {
					AssignStmt stmt = (AssignStmt) earlierAssignment;
					if (stmt.getLeftOp().equivTo(assignStmt.getLeftOp())) {
						kills.add(earlierAssignment);
					}
				}
			}
		}
		src.difference(kills, dest);
	}

	private void gen(FlowSet dest, Unit unit) {
		if (unit instanceof AssignStmt) {
			AssignStmt stmt = (AssignStmt) unit;
			Local local = (Local) stmt.getLeftOp();
			String name = local.getName();
//			if (name.indexOf("$") == -1) {
				dest.add(unit);
//			}
		}
	}
}
