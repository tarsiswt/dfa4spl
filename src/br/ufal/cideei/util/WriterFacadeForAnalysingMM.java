package br.ufal.cideei.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ui.dialogs.ListSelectionDialog;

/*
 * TODO: Make this a general approach to making row-by-row tables that we do not know the order in which information is provided.
 *     0			1				2				3			4                5        			6				7				  8                  9
 * [método] [nº de features] [nº features int.] [nº locals] [nº assigments] [rd lifted time] [uv lifted time] [rd runner time] [uv runner time] [instrumentation]
 * 
 */
public class WriterFacadeForAnalysingMM {
	private static final short MAX_COLUMN = 11;

	/*
	 * Make this a dynamic-growing list(or map)
	 */
	public static final int METHOD_COLUMN = 0;
	public static final int NO_OF_FEATURES_COLUMN = 1;
	public static final int FEAT_INT_COLUMN = 2;
	public static final int LOCAL_COLUMN = 3;
	public static final int ASSIGNMENT_COLUMN = 4;
	public static final int RD_LIFTED_COLUMN = 5;
	public static final int UV_LIFTED_COLUMN = 6;
	public static final int RD_RUNNER_COLUMN = 7;
	public static final int UV_RUNNER_COLUMN = 8;
	public static final int INSTRUMENTATION_COLUMN = 9;
	public static final int INSTRUMENTATION_UNITTOASTNODE_COLUMN = 10;

	private static PrintWriter writer = null;
	private static File file = null;
	private static List<String> list = null;

	static {
		try {
			list = new ArrayList<String>(MAX_COLUMN);

			for (int i = 0; i < MAX_COLUMN; i++) {
				list.add(i, null);
			}

			file = new File("method_grain_results.csv");
			if (file.exists()) {
				file.delete();
			}
			writer = new PrintWriter(new FileWriter(file));

			/*
			 * This println must reflect the information stored dynamically
			 */
			// [método] [nº de features] [nº features int.] [nº locals] [nº
			// assigments] [rd lifted time] [uv lifted time] [rd runner time]
			// [uv runner time]
			writer
					.println("method;no of features;features interactions;locals;assignments;rd lifted time;uv liftedtime;rd runner time;uv runner time;instrumentation;unit to ast");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void write(int i, String content) throws IOException {

		list.set(i, content);

		/*
		 * Store this counter as a class member and avoid this loop.
		 */
		short notNullCounter = 0;
		for (String str : list) {
			if (str != null) {
				notNullCounter++;
			}
		}
		if (notNullCounter == MAX_COLUMN) {
			dump();
		}
	}

	private static void dump() throws IOException {
		System.out.println("Dumping");
		/*
		 * Use a StringBuilder to build a string instead of calling #print in a
		 * loop.
		 */
		for (String str : list) {
			writer.print(str);
			writer.print(';');
		}
		writer.print('\n');
		writer.flush();
		list.clear();

		/*
		 * Find a better approach to reset a list.
		 */
		// Collections.<String>fill(list, null);
		for (int i = 0; i < MAX_COLUMN; i++) {
			list.add(i, null);
		}
	}

	public static void close() throws IOException {
		writer.close();
	}

	public static void renew() throws IOException {
		writer.close();
		if (file.exists()) {
			file.delete();
		}
		writer = new PrintWriter(new FileWriter(new File("method_grain_results.csv")));
		/*
		 * This println must reflect the information stored dynamically
		 */
		writer
				.println("method;no of features;features interactions;locals;assignments;rd lifted time;uv liftedtime;rd runner time;uv runner time;instrumentation;unit to ast");
	}

}
