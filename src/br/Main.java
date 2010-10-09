package br;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import soot.ArrayType;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.dava.toolkits.base.AST.structuredAnalysis.ReachingDefs;
import soot.jimple.Constant;
import soot.jimple.JasminClass;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.StringConstant;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ExplicitEdgesPred;
import soot.jimple.toolkits.callgraph.Filter;
import soot.jimple.toolkits.callgraph.Sources;
import soot.jimple.toolkits.callgraph.TransitiveTargets;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.InitAnalysis;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.util.Chain;
import soot.util.JasminOutputStream;
import soot.util.queue.QueueReader;

public class Main {

	public static void main(String[] args) throws IOException {

		Options.v().set_whole_program(true);
		Options.v().set_verbose(false);

		Scene scene = Scene.v();
		SootClass sClass;
		SootMethod method;

		// Carregar dependências e a raiz Object
		SootClass objClass = scene.loadClassAndSupport("java.lang.Object");
		scene.loadClassAndSupport("java.lang.System");

		// Declarar a classe como public
		sClass = new SootClass("HelloWorld", Modifier.PUBLIC);

		/*
		 * É obrigatório definir a superclasse, pois quando ela não é definida,
		 * o compilador se encarrega de fazê-lo, mas nesse caso é obrigatório
		 * explicitá-la.
		 */
		sClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		scene.addClass(sClass);

		/*
		 * Criar a assinatura do método(nome, parâmetros e retorno):
		 * 
		 * public static void main(String[])
		 * 
		 * o corpo será definido mais abaixo
		 */
		method = new SootMethod("main", Arrays.asList(new Type[] { ArrayType.v(RefType.v("java.lang.String"), 1) }), VoidType.v(), Modifier.PUBLIC
				| Modifier.STATIC);

		sClass.addMethod(method);

		// Este bloco é utilizado para definir o corpo do método
		{
			// Um Body só deve ser instanciado relacionando-o diretamente a uma
			// IR
			JimpleBody body = Jimple.v().newBody(method);

			method.setActiveBody(body);
			Chain<Local> locals = body.getLocals();
			Chain<Unit> units = body.getUnits();
			Local arg, tmpRef;

			// Add some locals, java.lang.String l0
			arg = Jimple.v().newLocal("l0", ArrayType.v(RefType.v("java.lang.String"), 1));
			locals.add(arg);

			// Add locals, java.io.printStream tmpRef
			tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
			locals.add(tmpRef);

			// add "l0 = @parameter0"
			units.add(Jimple.v().newIdentityStmt(arg, Jimple.v().newParameterRef(ArrayType.v(RefType.v("java.lang.String"), 1), 0)));

			// add "tmpRef = java.lang.System.out"
			Unit tmpRefAssignUnit = Jimple.v().newAssignStmt(tmpRef,
					Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef()));
			units.add(tmpRefAssignUnit);

			// insert "tmpRef.println("Hello world!")"
			{
				SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>");
				units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), StringConstant.v("Hello world!"))));
			}

			// insert "return"
			units.add(Jimple.v().newReturnVoidStmt());

			{
				UnitGraph unitGraph = new BriefUnitGraph(body);
				SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);

				SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);
				List localUsesOfTmpRefValueBoxPair = localUses.getUsesOf(tmpRefAssignUnit);

				SimpleLiveLocals liveLocals = new SimpleLiveLocals(unitGraph);
				List liveLocalsBefore = liveLocals.getLiveLocalsAfter(tmpRefAssignUnit);

				scene.setMainClass(sClass);
				scene.loadNecessaryClasses();

				InitAnalysis init = new InitAnalysis(unitGraph);
				for (Unit unit : units) {
					System.out.println(init.getFlowAfter(unit));
				}

				// CHATransformer.v().transform();
				// CallGraph cg = Scene.v().getCallGraph();

				// ReachingDefs reachingDefs = new ReachingDefs(unitGraph);
				// System.out.println(reachingDefs);

			}
		}

		// String fileName = SourceLocator.v().getFileNameFor(sClass,
		// Options.output_format_class);
		String fileName = SourceLocator.v().getSourceForClass(sClass.getName());
		System.out.println(fileName);
		OutputStream streamOut = new JasminOutputStream(new FileOutputStream(fileName));
		PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
		JasminClass jasminClass = new soot.jimple.JasminClass(sClass);
		jasminClass.print(writerOut);
		writerOut.flush();
		streamOut.close();

	}

}