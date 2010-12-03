package br.ufal.cideei.soot.count;

//#ifdef METRICS
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.AssignStmt;

public class AssignmentsCounter extends BodyTransformer implements ICounter<Long> {

	private static AssignmentsCounter instance = null;

	private AssignmentsCounter() {
	}

	public static AssignmentsCounter v() {
		if (instance == null)
			instance = new AssignmentsCounter();
		return instance;

	}

	private long counter = 0;

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		PatchingChain<Unit> units = body.getUnits();
		for (Unit unit : units) {
			if (unit instanceof AssignStmt) {
				counter++;
			}
		}
	}

	public Long getCount() {
		return counter;
	}

	public void reset() {
		counter = 0;
	}

}
// #endif