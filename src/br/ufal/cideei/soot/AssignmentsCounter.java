package br.ufal.cideei.soot;

//#ifdef METRICS
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.AssignStmt;

public class AssignmentsCounter extends BodyTransformer {

	private static AssignmentsCounter instance = null;

	private AssignmentsCounter() {
	}

	public static AssignmentsCounter v() {
		if (instance == null)
			instance = new AssignmentsCounter();
		return instance;

	}

	private static long counter = 0;

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		PatchingChain<Unit> units = body.getUnits();
		for (Unit unit : units) {
			if (unit instanceof AssignStmt) {
				AssignmentsCounter.counter++;
			}
		}
	}

	public static long getCounter() {
		return AssignmentsCounter.counter;
	}

	public static void reset() {
		AssignmentsCounter.counter = 0;
	}

}
// #endif