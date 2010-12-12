package br.ufal.cideei.soot.count;

import java.util.Map;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.util.Chain;

public class FieldCounter extends SceneTransformer implements ICounter<Long> {

	private static FieldCounter instance = null;

	private FieldCounter() {
	}

	public static FieldCounter v() {
		if (instance == null)
			instance = new FieldCounter();
		return instance;

	}

	private long counter = 0;

	public Long getCount() {
		return counter;
	}

	@Override
	protected void internalTransform(String phase, Map opt) {
		Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
		for (SootClass sootClass : applicationClasses) {
			counter += sootClass.getFieldCount();
		}
	}
}
