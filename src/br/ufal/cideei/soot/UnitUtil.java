package br.ufal.cideei.soot;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import soot.Body;
import soot.Printer;
import soot.SourceLocator;
import soot.Unit;
import soot.options.Options;
import soot.toolkits.graph.DirectedGraph;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.dot.DotGraph;

public class UnitUtil {
	private UnitUtil() {
	}

	public static void serializeBody(Body body, String fileName) {
		/*
		 * prints .jimple file
		 */
		{
			if (fileName == null) {
				fileName = SourceLocator.v().getFileNameFor(body.getMethod().getDeclaringClass(), Options.output_format_jimple);
			}
			OutputStream streamOut;
			try {
				streamOut = new FileOutputStream(fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
			Printer.v().printTo(body, writerOut);
			writerOut.flush();
			try {
				streamOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}

	public static void serializeGraph(Body body, String fileName) {
		CFGGraphType graphtype = CFGGraphType.BRIEF_UNIT_GRAPH;
		DirectedGraph<Unit> graph = graphtype.buildGraph(body);
		SootUnitGraphSerializer drawer = new SootUnitGraphSerializer();
		DotGraph canvas = graphtype.drawGraph(drawer, graph, body);

		String methodname = body.getMethod().getSubSignature();

		if (fileName == null) {
			fileName = soot.SourceLocator.v().getOutputDir();
			if (fileName.length() > 0) {
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + methodname.replace(java.io.File.separatorChar, '.') + DotGraph.DOT_EXTENSION;
		}

		System.out.println("Generate dot file in " + fileName);
		canvas.plot(fileName);

	}

}
