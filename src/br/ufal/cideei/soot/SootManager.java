package br.ufal.cideei.soot;

import java.util.Collection;

import br.ufal.cideei.algorithms.BaseAlgorithm;
import br.ufal.cideei.algorithms.coa.ChainOfAssignmentAlgorithm;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentor;
import soot.G;
import soot.Pack;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;

public class SootManager {
	public static void configure(String classpath){		
		Options.v().set_keep_line_number(true);
		Options.v().set_src_prec(Options.src_prec_java);
		PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().set_soot_classpath(classpath);
		Options.v().set_prepend_classpath(true);
		PhaseOptions.v().setPhaseOption("tag.ln", "on");
//		PhaseOptions.v().setPhaseOption("gb", "off");
//		PhaseOptions.v().setPhaseOption("bb", "off");
//		PhaseOptions.v().setPhaseOption("db", "off");
	}
	
	public static void runPacks(IFeatureExtracter extracter){
		Transform t = new Transform("jtp.featmodelinst", FeatureModelInstrumentor.v(extracter));
		PackManager.v().getPack("jtp").add(t);
		PackManager.v().runBodyPacks();
	}
	
	public static void reset(){
		G.reset();
	}
	
	public static Scene getScene(){
		return Scene.v();
	}
	
	public static SootClass loadAndSupport(String className){
		SootClass sootClass = Scene.v().loadClassAndSupport(className);
		sootClass.setApplicationClass();	
		Scene.v().loadNecessaryClasses();
		return sootClass;
	}
	
	public static SootMethod getMethodByName(String className, String methodIdentifier){
		if (!Scene.v().containsClass(className)){
			loadAndSupport(className);
		}
		return Scene.v().getSootClass(className).getMethodByName(methodIdentifier);
	}
	
	public static SootMethod getMethodBySignature(String className,String signature){
		if (!Scene.v().containsClass(className)){
			loadAndSupport(className);
		}
		return Scene.v().getSootClass(className).getMethod(signature);
	}

}
