package br.ufal.cideei.soot.count;

import java.io.IOException;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.util.WriterFacadeForAnalysingMM;

public class ColoredBodyCounter extends BodyTransformer implements ICounter<Long>, IResettable {

	private static ColoredBodyCounter instance = null;

	private ColoredBodyCounter() {
	}

	public static ColoredBodyCounter v() {
		if (instance == null)
			instance = new ColoredBodyCounter();
		return instance;

	}

	private long counter = 0;
	private long coloredCounter = 0;

	public Long getCount() {
		return counter;
	}
	
	public Long getColoredCount() {
		return coloredCounter;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		counter++;
		FeatureTag tag = (FeatureTag) body.getTag("FeatureTag");
		if (tag.getFeatures().size() > 1)
			coloredCounter++;
		
		// #ifdef METRICS
//		try {
//			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.NO_OF_FEATURES_COLUMN, Integer.toString(tag.getFeatures().size()));
//			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.FEAT_INT_COLUMN, "0");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//#endif

	}

	@Override
	public void reset() {
		counter = 0;
		coloredCounter = 0;
	}
	
	public String toString() {
		return "(colored) body count: " + coloredCounter +" of " + counter;
	}
}
