package br.ufal.cideei.soot.count;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.util.Chain;

public class LocalCounter extends BodyTransformer implements ICounter<Long> {

	private static LocalCounter instance = null;

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
	private boolean excludeTemp = false;

	public Long getCount() {
		return counter;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		if (excludeTemp ) {
			excludeTmp(body);
		} else {
			counter += body.getLocalCount();
		}
	}
	
	private void excludeTmp(Body body) {
		Chain<Local> locals = body.getLocals();
		for (Local local : locals) {
			if (local.getName().indexOf("$") != -1) {
				counter++;
			}
		}
		
	}

}
