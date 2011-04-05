//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;

public class AssignmentsCounter extends BodyTransformer implements ICounter<Long>, IResettable {

	private static AssignmentsCounter instance = null;

	private AssignmentsCounter() {
	}

	// ignore assignments that have $tempN on the LHS.
	private boolean ignoreTemp = true;

	public static AssignmentsCounter v() {
		if (instance == null)
			instance = new AssignmentsCounter();
		return instance;

	}

	private long counter = 0;

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