package br.ufal.cideei.soot.analyses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import soot.Body;
import soot.PatchingChain;
import soot.Unit;
import soot.tagkit.Tag;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;

public class FlowSetUtils {

	public static int liftedMemoryUnits(Body body, ForwardFlowAnalysis<Unit, MapLiftedFlowSet> analysis, boolean countKeys, int countEmptyAs) {
		int memUnits = 0;
		PatchingChain<Unit> units = body.getUnits();
		for (Unit unit : units) {
			MapLiftedFlowSet flowAfter = analysis.getFlowAfter(unit);
			MapLiftedFlowSet flowBefore = analysis.getFlowBefore(unit);

			Set<Entry<IConfigRep, FlowSet>> entrySet;

			entrySet = flowBefore.getMapping().entrySet();
			for (Entry<IConfigRep, FlowSet> entry : entrySet) {
				FlowSet value = entry.getValue();
				if (value.isEmpty()) {
					memUnits += countEmptyAs;
				} else {
					memUnits += value.size();
				}
				if (countKeys) {
					memUnits++;
				}
			}

			entrySet = flowAfter.getMapping().entrySet();
			for (Entry<IConfigRep, FlowSet> entry : entrySet) {
				FlowSet value = entry.getValue();
				if (value.isEmpty()) {
					memUnits += countEmptyAs;
				} else {
					memUnits += value.size();
				}
				if (countKeys) {
					memUnits++;
				}
			}
		}
		return memUnits;
	}

	public static long unliftedMemoryUnits2(Body body, ForwardFlowAnalysis<Unit, FlowSet> analysis, int countEmptyAs) {
		long memUnits = 0;
		PatchingChain<Unit> units = body.getUnits();
		for (Unit unit : units) {
			int flowBeforeSize = analysis.getFlowBefore(unit).size();
			if (flowBeforeSize == 0) {
				memUnits += countEmptyAs;
			} else {
				memUnits += flowBeforeSize;
			}

			int flowAfterSize = analysis.getFlowAfter(unit).size();
			if (flowAfterSize == 0) {
				memUnits += countEmptyAs;
			} else {
				memUnits += flowAfterSize;
			}
		}
		return memUnits;
	}

	public static double averageSharingDegree(Body body, ForwardFlowAnalysis<Unit, MapLiftedFlowSet> analysis) {
		ConfigTag tag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
		ILazyConfigRep lazyConfig = (ILazyConfigRep) tag.getConfigReps().iterator().next();

		double noOfConfigs = lazyConfig.size();

		List<Double> sharingDegrees = new ArrayList<Double>();
		PatchingChain<Unit> units = body.getUnits();
		for (Unit unit : units) {
			MapLiftedFlowSet flowBefore = analysis.getFlowBefore(unit);
			MapLiftedFlowSet flowAfter = analysis.getFlowAfter(unit);

			sharingDegrees.add(noOfConfigs / flowBefore.size());
			sharingDegrees.add(noOfConfigs / flowAfter.size());
		}

		double accumulator = 0.0;
		for (Double degree : sharingDegrees) {
			accumulator += degree;
		}

		return accumulator / sharingDegrees.size();
	}

	public static double lazyMemoryUnits(Body body, ForwardFlowAnalysis<Unit, MapLiftedFlowSet> analysis, boolean countKeys, int countEmptyAs, int FlocalSize) {
		double memUnits = 0.0;
		PatchingChain<Unit> units = body.getUnits();
		for (Unit unit : units) {
			MapLiftedFlowSet flowBefore = analysis.getFlowBefore(unit);
			MapLiftedFlowSet flowAfter = analysis.getFlowAfter(unit);

			Set<Entry<IConfigRep, FlowSet>> entrySet;

			entrySet = flowBefore.getMapping().entrySet();
			for (Entry<IConfigRep, FlowSet> entry : entrySet) {
				IConfigRep key = entry.getKey();
				FlowSet value = entry.getValue();
				
				if (countKeys) {
					memUnits += FlocalSize / 32;
				}

				memUnits += key.size();
				int flowBeforeSize = value.size();
				if (flowBeforeSize == 0) {
					memUnits += countEmptyAs;
				} else {
					memUnits += flowBeforeSize;
				}
			}
			entrySet = flowAfter.getMapping().entrySet();
			for (Entry<IConfigRep, FlowSet> entry : entrySet) {
				IConfigRep key = entry.getKey();
				FlowSet value = entry.getValue();
				
				if (countKeys){
					memUnits += FlocalSize/32;
				}

				memUnits += key.size();
				int flowAfterSize = value.size();
				if (flowAfterSize == 0) {
					memUnits += countEmptyAs;
				} else {
					memUnits += flowAfterSize;
				}
			}

		}
		return memUnits;
	}

	public static File pbm(Body body, ForwardFlowAnalysis<Unit, MapLiftedFlowSet> analysis, String fileName) {
		ConfigTag tag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
		final int MAX_WIDTH = tag.getConfigReps().iterator().next().size();
		List<LinkedList<Integer>> matrix = createPixMatrix(body, analysis, MAX_WIDTH);

		createPixMatrix(body, analysis, MAX_WIDTH);

		OutputStream streamOut;
		try {
			streamOut = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
		writerOut.println("P2");
		writerOut.println(matrix.get(0).size() + " " + matrix.size());
		writerOut.println("10");
		for (LinkedList<Integer> row : matrix) {
			for (Integer pix : row) {
				writerOut.print(pix);
				writerOut.print(' ');
			}
			writerOut.println("");
		}
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

	private static List<LinkedList<Integer>> createPixMatrix(Body body, ForwardFlowAnalysis<Unit, MapLiftedFlowSet> analysis, final int MAX_WIDTH) {
		List<LinkedList<Integer>> matrix = new ArrayList<LinkedList<Integer>>();
		int index = 0, size = 0;
		PatchingChain<Unit> units = body.getUnits();
		for (Unit unit : units) {
			size = analysis.getFlowAfter(unit).size();

			LinkedList<Integer> row = new LinkedList<Integer>();
			for (index = 0; index < size; index++) {
				row.add(0);
			}
			boolean headTailFlip = true;
			while (row.size() != MAX_WIDTH) {
				if (headTailFlip)
					row.addFirst(9);
				else
					row.addLast(9);

				headTailFlip = !headTailFlip;
			}
			matrix.add(row);
		}
		return matrix;
	}
}