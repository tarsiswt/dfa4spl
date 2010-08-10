package br.ufal.cideei.soot;

import soot.G;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

public class SootManager {
	public static void configure(String classpath){
//		PhaseOptions.v().setPhaseOption("jb.ls", "enabled:false");
//		PhaseOptions.v().setPhaseOption("jb.lp", "unsplit-original-locals:true");
		
		Options.v().set_keep_line_number(true);
		Options.v().set_src_prec(Options.src_prec_java);
		PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().set_soot_classpath(classpath);
		Options.v().set_prepend_classpath(true);
		PhaseOptions.v().setPhaseOption("tag.ln", "on");
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
//		System.out.println(Scene.v().getSootClass(className).getMethods());
		return Scene.v().getSootClass(className).getMethod(signature);
	}

}
