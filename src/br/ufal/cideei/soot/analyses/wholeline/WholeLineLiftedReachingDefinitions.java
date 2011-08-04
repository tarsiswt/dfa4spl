package br.ufal.cideei.soot.analyses.wholeline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DirectedMultigraph;

//#ifdef METRICS
import profiling.ProfilingTag;
import br.ufal.cideei.soot.count.AssignmentsCounter;
//#endif

//#ifdef LAZY
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.soot.analyses.reachingdefs.LazyLiftedReachingDefinitions;

//#endif

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import br.ufal.cideei.soot.analyses.FlowSetUtils;
import br.ufal.cideei.soot.analyses.MapLiftedFlowSet;
import br.ufal.cideei.soot.analyses.reachingdefs.LiftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.reachingdefs.SimpleReachingDefinitions;

import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;
import br.ufal.cideei.util.ConfigurationEdgeFactory;
import br.ufal.cideei.util.ConfigurationEdgeNameProvider;
import br.ufal.cideei.util.ValueContainerEdge;
import br.ufal.cideei.util.count.AbstractMetricsSink;
import br.ufal.cideei.util.graph.VertexLineNameProvider;

//TODO: can this class structure could be replaced by an abstract factory?
public class WholeLineLiftedReachingDefinitions extends BodyTransformer {

	private static WholeLineLiftedReachingDefinitions instance = new WholeLineLiftedReachingDefinitions();

	private WholeLineLiftedReachingDefinitions() {
	}

	public static WholeLineLiftedReachingDefinitions v() {
		return instance;
	}

	// #ifdef METRICS
	private static final String RD_LIFTED_FLOWTHROUGH_COUNTER = "RD A3 flowthrough";
	private static final String RD_LIFTED_FLOWSET_MEM = "RD A3 mem";
	private static final String RD_LIFTED_FLOWTHROUGH_TIME = "RD A3 flowthrough time";
	private AbstractMetricsSink sink;

	public WholeLineLiftedReachingDefinitions setMetricsSink(AbstractMetricsSink sink) {
		this.sink = sink;
		return this;
	}

	// #endif

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		UnitGraph bodyGraph = new BriefUnitGraph(body);
		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);

		boolean wentHybrid = false;
		LiftedReachingDefinitions liftedReachingDefinitions = null;

		// #ifdef METRICS
		AssignmentsCounter assignmentsCounter = new AssignmentsCounter(sink, true);
		long noOfAssigments = assignmentsCounter.getCount();
		long startAnalysis = System.nanoTime();
		// #endif
		
		// #ifdef HYBRID
		if (configTag.size() == 1) {
			wentHybrid = true;
			SimpleReachingDefinitions simpleReachingDefinitions = new SimpleReachingDefinitions(bodyGraph);
		} else {
			//#endif
			liftedReachingDefinitions = new LiftedReachingDefinitions(bodyGraph, configTag.getConfigReps());
			liftedReachingDefinitions.execute();
			// #ifdef HYBRID
		}
		//#endif

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();

		if (!wentHybrid) {
			this.sink.flow(body, RD_LIFTED_FLOWTHROUGH_TIME, liftedReachingDefinitions.getFlowThroughTime());
			this.sink.flow(body, RD_LIFTED_FLOWSET_MEM, FlowSetUtils.liftedMemoryUnits(body, liftedReachingDefinitions, false, 1));
			this.sink.flow(body, RD_LIFTED_FLOWTHROUGH_COUNTER, LiftedReachingDefinitions.getFlowThroughCounter());
			LiftedReachingDefinitions.reset();
		}

//		if (body.getMethod().getSignature().contains("simple3")) {
//			System.out.println(body.getTag(ConfigTag.CONFIG_TAG_NAME));
//			for (Unit unit : body.getUnits()) {
//				System.out.println(unit + " [[" + unit.getTag(FeatureTag.FEAT_TAG_NAME) + "]]");
//				System.out.println(liftedReachingDefinitions.getFlowAfter(unit));
//			}
//			DOTExporter<Unit, ValueContainerEdge<IConfigRep>> exporter = new DOTExporter<Unit, ValueContainerEdge<IConfigRep>>(new VertexLineNameProvider<Unit>(null), null, new
//
//			ConfigurationEdgeNameProvider<ValueContainerEdge<IConfigRep>>());
//			try {
//				exporter.export(new FileWriter(System.getProperty("user.home") + File.separator + "REACHES DATA" + ".dot"), createProvidesGraph(body.getUnits(), liftedReachingDefinitions, body));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setRdAnalysisTime2(endAnalysis - startAnalysis);
		// #endif
	}

	private DirectedMultigraph<Unit, ValueContainerEdge<IConfigRep>> createProvidesGraph(Collection<Unit> unitsInSelection, LiftedReachingDefinitions reachingDefinitions, Body body) {
		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);

		DirectedMultigraph<Unit, ValueContainerEdge<IConfigRep>> reachesData = new DirectedMultigraph<Unit, ValueContainerEdge<IConfigRep>>(ConfigurationEdgeFactory.getInstance());

		// for every unit in the selection...
		for (Unit unitFromSelection : unitsInSelection) {
			if (unitFromSelection instanceof DefinitionStmt) {
				/*
				 * exclude definitions when it's $temp on the leftOp.
				 */
				DefinitionStmt definition = (DefinitionStmt) unitFromSelection;

				Value leftOp = (Value) definition.getLeftOp();
				if (leftOp instanceof Local) {
					if (((Local) leftOp).getName().contains("$")) {
						continue;
					}
				}

				IFeatureRep featuresThatUseDefinition = null;

				// for every unit in the body...
				Iterator<Unit> iterator = body.getUnits().snapshotIterator();
				while (iterator.hasNext()) {
					Unit nextUnit = iterator.next();
					FeatureTag nextUnitTag = (FeatureTag) nextUnit.getTag("FeatureTag");

					List useAndDefBoxes = nextUnit.getUseAndDefBoxes();
					for (Object object : useAndDefBoxes) {
						ValueBox vbox = (ValueBox) object;
						if (vbox.getValue().equivTo(leftOp)) {
							if (featuresThatUseDefinition == null) {
								featuresThatUseDefinition = nextUnitTag.getFeatureRep().clone();
							} else {
								featuresThatUseDefinition.addAll(nextUnitTag.getFeatureRep());
							}
						}
					}

					MapLiftedFlowSet liftedFlowAfter = (MapLiftedFlowSet) reachingDefinitions.getFlowAfter(nextUnit);
					Collection<IConfigRep> configurations = liftedFlowAfter.getConfigurations();
					for (IConfigRep currConfiguration : configurations) {
						FlowSet flowSet = liftedFlowAfter.getLattice(currConfiguration);

						// if the unit belongs to the current configuration...
						if (!nextUnitTag.getFeatureRep().belongsToConfiguration(currConfiguration)) {
							continue;
						}

						// if the definition reaches this unit...
						if (flowSet.contains(definition)) {
							List<ValueBox> useBoxes = nextUnit.getUseBoxes();
							for (ValueBox vbox : useBoxes) {
								/*
								 * and the definition is used, add to the map (graph)...
								 */
								if (vbox.getValue().equivTo(leftOp)) {
									if (!reachesData.containsVertex(definition)) {
										reachesData.addVertex(definition);
									}
									if (!reachesData.containsVertex(nextUnit)) {
										reachesData.addVertex(nextUnit);
									}

									Set<ValueContainerEdge<IConfigRep>> allEdges = reachesData.getAllEdges(definition, nextUnit);
									if (allEdges.size() >= 1) {
										int diffCounter = 0;
										Iterator<ValueContainerEdge<IConfigRep>> edgesIterator = allEdges.iterator();
										Set<ValueContainerEdge<IConfigRep>> edgeRemovalSchedule = new HashSet<ValueContainerEdge<IConfigRep>>();
										while (edgesIterator.hasNext()) {
											ValueContainerEdge<IConfigRep> valueContainerEdge = (ValueContainerEdge<IConfigRep>) edgesIterator.next();
											IConfigRep valueConfiguration = valueContainerEdge.getValue();
											FlowSet flowSetFromOtherReached = liftedFlowAfter.getLattice(valueConfiguration);

											if (flowSetFromOtherReached.equals(flowSet)) {
												/*
												 * Se a configura��o que estiver "querendo" entrar for menor, ent�o ela
												 * expulsar� os maiores.
												 */
												if (valueConfiguration.size() > currConfiguration.size() && featuresThatUseDefinition.belongsToConfiguration(currConfiguration)) {
													edgeRemovalSchedule.add(valueContainerEdge);
													ValueContainerEdge<IConfigRep> addEdge = reachesData.addEdge(definition, nextUnit);
													addEdge.setValue(currConfiguration);
													continue;
												}
											} else {
												diffCounter++;
											}
										}
										if (diffCounter == allEdges.size() && featuresThatUseDefinition.belongsToConfiguration(currConfiguration)) {
											ValueContainerEdge<IConfigRep> addEdge = reachesData.addEdge(definition, nextUnit);
											addEdge.setValue(currConfiguration);
										}
										reachesData.removeAllEdges(edgeRemovalSchedule);
									} else {
										ValueContainerEdge<IConfigRep> addEdge = reachesData.addEdge(definition, nextUnit);
										addEdge.setValue(currConfiguration);
									}
								}
							}
						}
					}
				}
			}
		}
		return reachesData;
	}

}
