package org.processmining.rootcauseanalysis.algorithms.causality;

import org.processmining.rootcauseanalysis.models.graph.Graph;

public interface ICausalityAlgorithm {

	Graph computeCausalGraph(Graph inclusionGraph);

}
