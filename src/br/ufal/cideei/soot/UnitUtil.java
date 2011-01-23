package br.ufal.cideei.soot;

import java.io.File;
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

// TODO: Auto-generated Javadoc
/**
 * The Class UnitUtil contains convenience methods to serialize information from
 * memory to help with visualisation and debugging.
 */
public class UnitUtil {

	/**
	 * This is a utility class. There's no need for a constructor since all
	 * methods are static.
	 */
	private UnitUtil() {
	}

	/**
	 * Serialize a SootMethod Body in the Jimple IR into a provided file path.
	 * 
	 * @param body
	 *            the body
	 * @param fileName
	 *            the file name, or null to use the default Soot output folder.
	 * @return 
	 */
	public static File serializeBody(Body body, String fileName) {
		/*
		 * prints .jimple file
		 */
		if (fileName == null) {
			fileName = SourceLocator.v().getFileNameFor(body.getMethod().getDeclaringClass(), Options.output_format_jimple);
		}
		OutputStream streamOut;
		try {
			streamOut = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
		Printer.v().printTo(body, writerOut);
		writerOut.flush();
		try {
			streamOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return new File(fileName);
	}

	/**
	 * Serialize a Unit graph in the DOT format into a provided file path.
	 * 
	 * @param body
	 *            the body
	 * @param fileName
	 *            the file name, or null to use the default Soot output folder.
	 */
	public static File serializeGraph(Body body, String fileName) {
		CFGGraphType graphType = CFGGraphType.BRIEF_UNIT_GRAPH;
		DirectedGraph<Unit> graph = graphType.buildGraph(body);
		SootUnitGraphSerializer drawer = new SootUnitGraphSerializer();
		DotGraph canvas = graphType.drawGraph(drawer, graph, body);
		String methodName = body.getMethod().getSubSignature();

		if (fileName == null) {
			fileName = soot.SourceLocator.v().getOutputDir();
			if (fileName.length() > 0) {
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + methodName.replace(java.io.File.separatorChar, '.') + DotGraph.DOT_EXTENSION;
		}

		canvas.plot(fileName);
		return new File(fileName);
	}

}
