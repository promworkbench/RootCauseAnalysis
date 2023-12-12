package org.processmining.rootcauseanalysis.algorithms.causality;

import org.apache.commons.math3.linear.SingularMatrixException;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.processmining.rootcauseanalysis.models.GrangerTestResult;
import org.processmining.rootcauseanalysis.models.graph.Edge;
import org.processmining.rootcauseanalysis.models.graph.Graph;
import org.processmining.rootcauseanalysis.models.graph.Node;
import org.processmining.rootcauseanalysis.models.graph.Properties;
import org.processmining.rootcauseanalysis.parameters.GrangerCausalityParameters;
import org.processmining.rootcauseanalysis.utils.TimeSeriesUtils;

public class GrangerCausalityAlgorithm extends CausalityAlgorithm {

	public static final String DESCRIPTION;

	static {
		DESCRIPTION = "Granger causality";
	}

	// FIELDS

	private GrangerCausalityParameters parameters;

	// CONSTRUCTORS

	public GrangerCausalityAlgorithm() {
		super(DESCRIPTION);
		setParameters(new GrangerCausalityParameters());
	}

	// GETTERS AND SETTERS

	public GrangerCausalityParameters getParameters() {
		return parameters;
	}

	public void setParameters(GrangerCausalityParameters parameters) {
		this.parameters = parameters;
	}

	// METHODS

	@Override
	public Graph computeCausalGraph(Graph inclusionGraph) {
		Graph causalityGraph = new Graph();

		// Add all nodes from the inclusion graph
		for (Node node : inclusionGraph.getNodes())
			causalityGraph.addNode(node);

		// Add edge between causally related nodes
		for (Edge edge : inclusionGraph.getEdges()) {
			TimeSeries tsX = (TimeSeries) edge.getSource().getProperty(Properties.KEY_TIMESERIES);
			TimeSeries tsY = (TimeSeries) edge.getTarget().getProperty(Properties.KEY_TIMESERIES);

			if (performGrangerTest(tsX, tsY))
				causalityGraph.addEdge(new Edge(edge.getSource(), edge.getTarget()));
		}

		// Remove unconnected nodes
		for (Node node : inclusionGraph.getNodes())
			if (causalityGraph.getEdges(node).isEmpty())
				causalityGraph.getNodes().remove(node);

		return causalityGraph;
	}

	private boolean performGrangerTest(TimeSeries tsx, TimeSeries tsy) {
		try {
			TimeSeries tsX = tsx.createCopy(0, tsx.getItemCount() - 1);
			TimeSeries tsY = tsy.createCopy(0, tsy.getItemCount() - 1);

			RegularTimePeriod Xs = tsX.getDataItem(0).getPeriod();
			RegularTimePeriod Ye = tsY.getDataItem(tsY.getItemCount() - 1).getPeriod();

			/******************************************************************************************
			 * 
			 * TIMESERIES PRE-PROCESSING
			 * 
			 * We can only check Granger causality between time series X->Y when
			 * Y doesn't end before X starts. Also, we cannot take the following
			 * points into account: points of X that occur after Y has ended;
			 * points of Y that occur before X has started.
			 * 
			 * Therefore, we modify the series in a pre-processing step. If the
			 * pre-processed series do not satisfy the minimum sample size
			 * requirements, then X->Y is considered not to be causal (we cannot
			 * test).
			 * 
			 ******************************************************************************************/

			if (TimeSeriesUtils.endsBefore(Ye, Xs))
				return false;

			if (TimeSeriesUtils.endsBefore(tsY, tsX))
				tsX.delete(tsX.getIndex(Ye), tsX.getItemCount() - 1);

			if (tsX.getItemCount() < parameters.getMinimumSampleSize())
				return false;

			if (TimeSeriesUtils.startsBefore(tsY, tsX))
				tsY.delete(0, tsY.getIndex(Xs));

			if (tsY.getItemCount() < parameters.getMinimumSampleSize())
				return false;

			/******************************************************************************************
			 * 
			 * RE-BASE TIME SERIES
			 * 
			 * H = max. shift. is defined as the number of time periods that X
			 * is bigger than Y or Y is bigger than X.
			 * 
			 * We need two time series of the same size as input for the Granger
			 * test. Therefore, we shift one of the time series with a certain h
			 * value time units.
			 * 
			 * As soon as we find causality for any given value for h, we
			 * return.
			 * 
			 ******************************************************************************************/

			int H = tsX.getItemCount() - tsY.getItemCount();
			int maxH = Math.min(parameters.getMaxH(), Math.abs(H));
			//			System.out.println("Max. H: " + maxH);

			if (H > 0) {
				// |tsX| > |tsY|
				// Loop through all possible shift values
				h: for (int h = maxH; h >= 0; h--) {
					// Modify tsX to have the same length of tsY, by shifting h periods and dropping the rest.					
					try {
						TimeSeries tsxMod = tsx.createCopy(h, h + tsy.getItemCount() - 1);
						//					System.out.println("h: " + h);
						if (performShiftedGrangerTest(tsy, tsxMod))
							return true;
					} catch (CloneNotSupportedException e) {
						System.out.println("Could not compare pair for h=" + h + ". Error");
						e.printStackTrace();
						continue h;
					}
				}
			} else if (H < 0) {
				// |tsY| > |tsX|
				// Loop through all possible shift values
				h: for (int h = maxH; h >= 0; h--) {
					// Modify tsY to have the same length of tsx, by shifting h periods and dropping the rest.					
					try {
						TimeSeries tsyMod = tsy.createCopy(h, h + tsx.getItemCount() - 1);
						//					System.out.println("h: " + h);
						if (performShiftedGrangerTest(tsyMod, tsx))
							return true;
					} catch (CloneNotSupportedException e) {
						System.out.println("Could not compare pair for h=" + h + ". Error");
						e.printStackTrace();
						continue h;
					}
				}
			} else {
				// |tsx| == |txy|
				return performShiftedGrangerTest(tsY, tsX);
			}

			// No causality
			return false;

		} catch (CloneNotSupportedException e) {
			System.out.println("Could not compare pair. Error: ");
			e.printStackTrace();

			return false;
		}
	}

	private boolean performShiftedGrangerTest(TimeSeries tsX, TimeSeries tsY) {
		/******************************************************************************************
		 * 
		 * PERFORM GRANGER TEST
		 * 
		 * Different lag values are tested. The max. lag is (|Y|-beta)) /
		 * (2*beta+1). Beta is the factor of data points we should have more
		 * than predictors.
		 * 
		 ******************************************************************************************/

		int maxLAG = (tsX.getItemCount() - parameters.getBeta()) / (2 * parameters.getBeta() + 1);

		// Loop through all possible lag values
		for (int l = 1; l < maxLAG; l++) {

			//			System.out.println("Granger-test " + tsx.getDescription() + " - " + tsy.getDescription() + " lag:" + l);

			double[] x = getTimeSeriesDataAsDoubleArray(tsX);
			double[] y = getTimeSeriesDataAsDoubleArray(tsY);

			try {
				GrangerTestResult r = GrangerTest.granger(y, x, l);
				//				grangerPValues.put(pair, r.getPValue());

				if (r.getPValue() < 0.05d) {
					//					System.out.println("CAUSALITY!");
					return true;
				}

			} catch (SingularMatrixException ex) {
				//				System.out.println("Could not invert matrix (singular), for pair: <" + tsX.getDescription() + ">-><"
				//						+ tsY.getDescription() + ">, with lag: " + l);
			}
		}

		return false;
	}

	private static double[] getTimeSeriesDataAsDoubleArray(TimeSeries serie) {
		double[] data = new double[serie.getItemCount()];
		for (int i = 0; i < data.length; i++)
			data[i] = serie.getDataItem(i).getValue().doubleValue();
		return data;
	}
}
