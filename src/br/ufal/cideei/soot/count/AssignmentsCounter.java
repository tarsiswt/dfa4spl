//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;

import br.ufal.cideei.util.count.AbstractMetricsSink;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;

public class AssignmentsCounter extends BodyTransformer implements ICounter<Long>, IResettable {

	private static final String PROPERTY_NAME = "assignments";

	// Ignore assignments that have $tempN on the left hand side?
	private boolean ignoreTemp;

	private long counter = 0;

	private AbstractMetricsSink sink;

	public AssignmentsCounter(AbstractMetricsSink sink, boolean ignoreTemp) {
		this.ignoreTemp = ignoreTemp;
		this.sink = sink;
	}

	public AssignmentsCounter(boolean ignoreTemp) {
		this.ignoreTemp = ignoreTemp;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		PatchingChain<Unit> units = body.getUnits();
		int counterChunk = 0;
		for (Unit unit : units) {
			if (unit instanceof AssignStmt) {
				if (ignoreTemp) {
					AssignStmt assignment = (AssignStmt) unit;
					Value leftOp = assignment.getLeftOp();
					if (leftOp instanceof Local) {
						Local assignee = (Local) leftOp;
						if (!assignee.getName().contains("$")) {
							counterChunk++;
						}
					}
				} else {
					counterChunk++;
				}
			}
		}
		sink.flow(body, AssignmentsCounter.PROPERTY_NAME, counterChunk);
		counter += counterChunk;
	}

	public Long getCount() {
		return counter;
	}

	public void reset() {
		counter = 0;
	}

}
// #endif