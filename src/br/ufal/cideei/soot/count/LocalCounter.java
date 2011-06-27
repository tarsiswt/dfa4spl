//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;

import br.ufal.cideei.util.count.AbstractMetricsSink;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.util.Chain;

public class LocalCounter extends BodyTransformer implements ICounter<Long>, IResettable {

	private static final String LOCALS = "locals";
	private long counter = 0;
	private boolean excludeTemp = true;
	private AbstractMetricsSink sink;

	public LocalCounter(AbstractMetricsSink sink, boolean excludeTemp) {
		this.excludeTemp = excludeTemp;
		this.sink = sink;
	}

	public Long getCount() {
		return counter;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		int counterChunk = 0;
		if (excludeTemp) {
			Chain<Local> locals = body.getLocals();
			for (Local local : locals) {
				String name = local.getName();
				if (name != "this" && name.indexOf("$") == -1) {
					counterChunk++;
				}
			}
		} else {
			counterChunk = body.getLocalCount();
		}
		if (sink != null) {
			sink.flow(body, LOCALS, counterChunk);
		}
		counter += counterChunk;
	}

	@Override
	public void reset() {
		counter = 0;
	}

}
// #endif