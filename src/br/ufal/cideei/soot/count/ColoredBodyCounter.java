//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;

import soot.Body;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class ColoredBodyCounter extends BodyCounter {

	private long coloredCounter = 0;

	public Long getColoredCount() {
		return coloredCounter;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		super.internalTransform(body, phase, opt);
		FeatureTag tag = (FeatureTag) body.getTag(FeatureTag.FEAT_TAG_NAME);
		if (tag.getFeatures().size() > 1)
			coloredCounter++;
	}
}
// #endif