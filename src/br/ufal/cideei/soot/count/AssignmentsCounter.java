package br.ufal.cideei.soot.count;

//#ifdef METRICS
import java.io.IOException;
import java.util.Map;

import br.ufal.cideei.util.WriterFacadeForAnalysingMM;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.AssignStmt;

public class AssignmentsCounter extends BodyTransformer implements ICounter<Long>, IResettable {

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
		int counterChunk = 0;
		for (Unit unit : units) {
			if (unit instanceof AssignStmt) {
				counterChunk++;
			}
		}
		
		// #ifdef METRICS
		try {
			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.ASSIGNMENT_COLUMN, Integer.toString(counterChunk));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// #endif
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