package br.ufal.cideei.soot.count;

import java.io.IOException;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.util.Chain;
import br.ufal.cideei.util.WriterFacadeForAnalysingMM;

public class LocalCounter extends BodyTransformer implements ICounter<Long>, IResettable {

	private static LocalCounter instance = null;

	public static LocalCounter v() {
		if (instance == null)
			instance = new LocalCounter();
		return instance;

	}
	
	private LocalCounter() {
	}

	private LocalCounter(boolean excludeTemp) {
		this.excludeTemp = excludeTemp;
	}

	public static LocalCounter v(boolean excludeTemp) {
		if (instance == null)
			instance = new LocalCounter(excludeTemp);
		return instance;
	}

	private long counter = 0;
	private boolean excludeTemp = true;

	public Long getCount() {
		return counter;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		if (excludeTemp) {
			excludeTmp(body);
		} else {
			long counterChunk = body.getLocalCount();
			counter += counterChunk;
			
			// #ifdef METRICS
			try {
				WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.LOCAL_COLUMN, Long.toString(counterChunk));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// #endif
		}
	}

	private void excludeTmp(Body body) {
		Chain<Local> locals = body.getLocals();
		int counterChunk = 0; 
		for (Local local : locals) {
			String name = local.getName();
			if (name != "this" && name.indexOf("$") == -1) {
				counterChunk++;
			}
		}
		counter += counterChunk;
		
		// #ifdef METRICS
		try {
			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.LOCAL_COLUMN, Integer.toString(counterChunk));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// #endif

	}

	@Override
	public void reset() {
		counter = 0;		
	}

}
