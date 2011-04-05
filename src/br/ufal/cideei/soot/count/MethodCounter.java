//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;

public class MethodCounter extends BodyTransformer implements ICounter<Integer>, IResettable {

	private static MethodCounter instance = null;
	
	private Set<SootMethod> methodContainer = new HashSet<SootMethod>();

	private MethodCounter() {
	}

	public static MethodCounter v() {
		if (instance == null)
			instance = new MethodCounter();
		return instance;
	}
	
	public Collection<SootMethod> getMethods(){
		return Collections.unmodifiableCollection(methodContainer);
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		SootMethod method = body.getMethod();
		if (!methodContainer.contains(method)) {
			methodContainer.add(method);
		}
	}

	@Override
	public Integer getCount() {
		return methodContainer.size();
	}

	@Override
	public void reset() {
		methodContainer.clear();
	}
}
//#endif