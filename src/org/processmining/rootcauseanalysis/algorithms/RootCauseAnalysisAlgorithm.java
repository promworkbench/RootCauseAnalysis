package org.processmining.rootcauseanalysis.algorithms;

import java.util.Date;

import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.processmining.contextawareperformance.models.ContextResult;
import org.processmining.contextawareperformance.models.EventCollection;
import org.processmining.contextawareperformance.models.PerformanceMeasurement;
import org.processmining.contextawareperformance.models.eventcollectionentities.EventCollectionEntity;
import org.processmining.contextawareperformance.models.eventcollectionviews.EventCollectionViewType;
import org.processmining.contextawareperformance.models.functions.context.Context;
import org.processmining.contextawareperformance.models.functions.performance.Performance;
//import org.processmining.contextawareperformance.models.preprocessors.xlog.activityinstance.assign.AssignActivityInstanceXLogPreprocessor;
import org.processmining.contextawareperformance.models.preprocessors.xlog.event.remove.RemoveEventsWithoutTimestampXLogPreprocessor;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.rootcauseanalysis.algorithms.causality.GrangerCausalityAlgorithm;
import org.processmining.rootcauseanalysis.models.RootCauseAnalysisOutput;
import org.processmining.rootcauseanalysis.models.graph.Edge;
import org.processmining.rootcauseanalysis.models.graph.Graph;
import org.processmining.rootcauseanalysis.models.graph.Node;
import org.processmining.rootcauseanalysis.models.graph.Properties;
import org.processmining.rootcauseanalysis.parameters.RootCauseAnalysisParameters;
import org.processmining.rootcauseanalysis.utils.TimeSeriesUtils;

public class RootCauseAnalysisAlgorithm {

	protected static RootCauseAnalysisOutput apply(PluginContext pluginContext, XLog eventlog,
			RootCauseAnalysisParameters parameters) {

		/******************************************************************************************
		 * 
		 * OUTPUT
		 * 
		 ******************************************************************************************/

		Graph decompositionGraph = new Graph();
		Graph inclusionGraph = new Graph();
		Graph causalityGraph = new Graph();
		RootCauseAnalysisOutput output = new RootCauseAnalysisOutput();

		/******************************************************************************************
		 * 
		 * VARIABLES
		 * 
		 ******************************************************************************************/

		XLog eventlogout;
		EventCollection eventCollection;

		/******************************************************************************************
		 * 
		 * PRE-PROCESS THE EVENT LOG
		 * 
		 * The event log is pre-processed by removing events without a timestamp
		 * and adding activity instance ids.
		 * 
		 ******************************************************************************************/

		RemoveEventsWithoutTimestampXLogPreprocessor removeEventsWithoutTimestampPreprocessor = new RemoveEventsWithoutTimestampXLogPreprocessor();
		eventlogout = removeEventsWithoutTimestampPreprocessor.preprocess(eventlog);

//		AssignActivityInstanceXLogPreprocessor activityInstancePreprocessor = new AssignActivityInstanceXLogPreprocessor();
//		eventlogout = activityInstancePreprocessor.preprocess(eventlogout);

		// Create event collection from XLog
		eventCollection = new EventCollection(eventlogout);

		/******************************************************************************************
		 * 
		 * CREATE TIME SERIES AND DECOMPOSITION GRAPH
		 * 
		 * The decomposition graph is the graph where parent nodes represent the
		 * base KPI time series and child nodes represent some decomposed
		 * version of parent nodes.
		 * 
		 ******************************************************************************************/

		// Create basic performance function time series objects and decomposition graph
		for (EventCollectionViewType eventCollectionViewType : parameters.getEventCollectionViewTypesToUse()) {

			performanceFunction: for (Performance<?> performanceFunction : parameters.getPerformanceFunctionsToUse()) {

				if (!performanceFunction.getType().getApplicableViewTypes().contains(eventCollectionViewType))
					continue performanceFunction;

				String lblPerformance = performanceFunction.getType().getDescription();
				Node performanceNode = new Node(lblPerformance);
				TimeSeries tsPerformance = new TimeSeries(lblPerformance);
				tsPerformance.setDescription(lblPerformance);
				performanceNode.setProperty(Properties.KEY_TIMESERIES, tsPerformance);
				decompositionGraph.addNode(performanceNode);

				contextFunction: for (Context<?> contextFunction : parameters.getContextFunctionsToUse()) {

					if (!contextFunction.getType().getApplicableViewTypes().contains(eventCollectionViewType))
						continue contextFunction;

					String lblPerformanceContext = lblPerformance + ", " + contextFunction.getType().getDescription();

					UnifiedMap<String, Node> mapContextResultNode = new UnifiedMap<String, Node>();

					for (EventCollectionEntity eventCollectionEntity : eventCollection.viewAs(eventCollectionViewType)
							.keySet()) {

						PerformanceMeasurement<?> performanceMeasurement = performanceFunction
								.mapToPerformance(eventCollectionEntity, eventCollection);

						ContextResult<?> contextResult = contextFunction.mapToContext(eventCollectionEntity,
								eventCollection);

						Long result = Long.parseLong(performanceMeasurement.getResult().toString());
						Date measurementDate = performanceMeasurement.getMeasurementDate();
						RegularTimePeriod measurementTimePeriod = new FixedMillisecond(measurementDate);

						if (!mapContextResultNode.containsKey(contextResult.getResult().toString())) {

							String lblPerformanceContextContextResult = lblPerformanceContext + ", activity "
									+ eventCollectionEntity.toString() + ", " + contextResult.getResult().toString();
							Node performanceContextContextResultNode = new Node(lblPerformanceContextContextResult);
							TimeSeries tsContextResult = new TimeSeries(lblPerformanceContextContextResult);
							tsContextResult.setDescription(lblPerformanceContextContextResult);
							performanceContextContextResultNode.setProperty(Properties.KEY_TIMESERIES, tsContextResult);

							mapContextResultNode.put(contextResult.getResult().toString(),
									performanceContextContextResultNode);

							decompositionGraph.addNode(performanceContextContextResultNode);
							decompositionGraph.addEdge(new Edge(performanceNode, performanceContextContextResultNode));
						}

						((TimeSeries) performanceNode.getProperty(Properties.KEY_TIMESERIES))
								.addOrUpdate(measurementTimePeriod, result);

						((TimeSeries) mapContextResultNode.get(contextResult.getResult().toString())
								.getProperty(Properties.KEY_TIMESERIES)).addOrUpdate(measurementTimePeriod, result);

					}

				}

			}

		}

		if (decompositionGraph.getNodes().size() == 0) {
			parameters.displayMessage("No time series (nodes), returning empty result.");
			return null;
		}

		/******************************************************************************************
		 * 
		 * CREATE INCLUSION GRAPH
		 * 
		 * The inclusion graph is the fully connected copy of the decomposition
		 * graph where edges are removed between nodes if they have an ancestry
		 * relationship.
		 * 
		 ******************************************************************************************/

		for (Node node : decompositionGraph.getNodes()) {
			inclusionGraph.addNode(node.clone());
		}

		for (Node nodeA : inclusionGraph.getNodes()) {
			for (Node nodeB : inclusionGraph.getNodes()) {
				if (!(nodeA.equals(nodeB) || decompositionGraph.ancestor(nodeA, nodeB)
						|| decompositionGraph.ancestor(nodeB, nodeA))) {

					inclusionGraph.addEdge(new Edge(nodeA, nodeB));
					inclusionGraph.addEdge(new Edge(nodeB, nodeA));

				}
			}
		}

		/******************************************************************************************
		 * 
		 * RE-SAMPLE TIME SERIES
		 * 
		 * All time series in the collection are resampled to the same regular
		 * time period (sampling rate).
		 * 
		 ******************************************************************************************/

		for (Node node : inclusionGraph.getNodes()) {

			TimeSeries nodeSeries = (TimeSeries) node.getProperty(Properties.KEY_TIMESERIES);
			nodeSeries = TimeSeriesUtils.resampleTimeSeries(nodeSeries, parameters.getTimePeriod());
			node.setProperty(Properties.KEY_TIMESERIES, nodeSeries);

		}

		/******************************************************************************************
		 * 
		 * CREATE CAUSALITY GRAPH
		 * 
		 * The causality graph holds all nodes from the inclusion graph for
		 * which a causal relation was found. Only causality between nodes is
		 * checked for which an edge exists in the inclusion graph.
		 * 
		 ******************************************************************************************/

		causalityGraph = new GrangerCausalityAlgorithm().computeCausalGraph(inclusionGraph);
		parameters.displayMessage("We found " + causalityGraph.getEdges().size() + " causal relations.");

		/******************************************************************************************
		 * 
		 * RETURNING OUTPUT
		 * 
		 ******************************************************************************************/

		output.setDecompositionGraph(decompositionGraph);
		output.setInclusionGraph(inclusionGraph);
		output.setCausalityGraph(causalityGraph);

		return output;
	}

}
