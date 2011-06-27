//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;

public class BodyCounter extends BodyTransformer implements ICounter<Long>, IResettable {

	protected long counter = 0;

	public Long getCount() {
		return counter;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		counter++;
	}

	@Override
	public void reset() {
		counter = 0;
	}
}
//#endif
