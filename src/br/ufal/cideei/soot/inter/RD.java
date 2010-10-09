package br.ufal.cideei.soot.inter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.ufal.cideei.soot.analyses.FeatureSensitiveAnalysisRunner;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachingDefinitions;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;
import br.ufal.cideei.util.SetUtil;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.util.Chain;
import soot.util.queue.QueueReader;

public class RD extends SceneTransformer {

	// Maps every body to its analysis runner object
	private Map<Body, FeatureSensitiveAnalysisRunner> bodyRunnerMap = new HashMap<Body, FeatureSensitiveAnalysisRunner>();

	// Soot-styled singleton's instance
	private static RD instance = new RD();

	public Map<Body, FeatureSensitiveAnalysisRunner> getBodyRunnerMap() {
		return bodyRunnerMap;
	}

	public static RD v() {
		return instance;
	}

	private RD() {
	}

	@Override
	protected void internalTransform(String arg0, Map arg1) {

		/*
		 * 1st iteration. Iterate over all application classes and and
		 * instrument with the feature model;
		 * 
		 * Iterates only over application classes.
		 * 
		 * TODO move this transformation to another SceneTransformer
		 * (FeatureModelInstrumentor?)
		 */
		Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
		Iterator<SootClass> iterator = applicationClasses.iterator();
		while (iterator.hasNext()) {
			SootClass sootClass = (SootClass) iterator.next();
			List<SootMethod> methods = sootClass.getMethods();
			Iterator<SootMethod> iterator2 = methods.iterator();
			while (iterator2.hasNext()) {
				SootMethod sootMethod = (SootMethod) iterator2.next();
				Body activeBody = sootMethod.retrieveActiveBody();
				FeatureModelInstrumentorTransformer.v().transform2(activeBody);
			}
		}

		/*
		 * 2nd iteration. Iterate over the call graph(specifically over it's
		 * edges) and run the actual RD analysis through the Feature-sensitive
		 * analysis runner.
		 * 
		 * Analysis are only ran on application classes.
		 * 
		 * TODO: the behaviour of this iteration needs to have a more
		 * well-define behaviour. i.e:
		 */
		CallGraph callGraph = Scene.v().getCallGraph();
		QueueReader<Edge> listener = callGraph.listener();
		while (listener.hasNext()) {
			Edge eachEdge = (Edge) listener.next();

			MethodOrMethodContext src = eachEdge.src();
			SootMethod srcMethod = src.method();
			SootClass srcDeclaringClass = srcMethod.getDeclaringClass();

			if (srcDeclaringClass.isApplicationClass()) {
				/*
				 * Here the analysis is ran and stored in the body-runner map.
				 */
				Body srcActiveBody = srcMethod.getActiveBody();
				FeatureSensitiveAnalysisRunner runner = new FeatureSensitiveAnalysisRunner(new BriefUnitGraph(srcActiveBody), SetUtil.tstconfig(),
						FeatureSensitiveReachingDefinitions.class, new HashMap<Object, Object>());
				bodyRunnerMap.put(srcActiveBody, runner);

				try {
					runner.execute();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				continue;
			}

			MethodOrMethodContext tgt = eachEdge.tgt();
			SootMethod tgtMethod = tgt.method();
			SootClass tgtDeclaringClass = tgtMethod.getDeclaringClass();

			if (tgtDeclaringClass.isApplicationClass()) {
				System.out.println("Method call: " + src + " -> " + tgt);
				Stmt srcStmt = eachEdge.srcStmt();
				InvokeExpr invokeExpr = srcStmt.getInvokeExpr();
				Body tgtActiveBody = tgtMethod.getActiveBody();

				if (invokeExpr.getArgCount() > 0) {
					FeatureSensitiveAnalysisRunner runner = new FeatureSensitiveAnalysisRunner(new BriefUnitGraph(tgtActiveBody), SetUtil.tstconfig(),
							FeatureSensitiveReachingDefinitions.class, new HashMap<Object, Object>());
					bodyRunnerMap.put(tgtActiveBody, runner);
					try {
						runner.execute();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

}
