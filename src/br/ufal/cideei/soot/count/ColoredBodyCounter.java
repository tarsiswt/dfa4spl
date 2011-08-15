//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;

import soot.Body;
import br.ufal.cideei.soot.instrument.ConfigTag;

public class ColoredBodyCounter extends BodyCounter {

	private long coloredCounter = 0;

	public Long getColoredCount() {
		return coloredCounter;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		super.internalTransform(body, phase, opt);
		ConfigTag tag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
		//XXX check size consistency. Depends on instrumentation.
		if (tag.size() > 1)
			coloredCounter++;
	}
}
// #endif
