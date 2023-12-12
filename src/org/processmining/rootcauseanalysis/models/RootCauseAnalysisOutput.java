package org.processmining.rootcauseanalysis.models;

import org.processmining.rootcauseanalysis.models.graph.Graph;

public class RootCauseAnalysisOutput {

	// FIELDS

	private Graph decompositionGraph;
	private Graph inclusionGraph;
	private Graph causalityGraph;

	// CONSTRUCTORS

	public Graph getDecompositionGraph() {
		return decompositionGraph;
	}

	// GETTERS AND SETTERS

	public void setDecompositionGraph(Graph decompositionGraph) {
		this.decompositionGraph = decompositionGraph;
	}

	public Graph getInclusionGraph() {
		return inclusionGraph;
	}

	public void setInclusionGraph(Graph inclusionGraph) {
		this.inclusionGraph = inclusionGraph;
	}

	public Graph getCausalityGraph() {
		return causalityGraph;
	}

	public void setCausalityGraph(Graph causalityGraph) {
		this.causalityGraph = causalityGraph;
	}

}
