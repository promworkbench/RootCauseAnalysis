package org.processmining.rootcauseanalysis.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.rootcauseanalysis.models.RootCauseAnalysisOutput;
import org.processmining.rootcauseanalysis.parameters.RootCauseAnalysisParameters;

/**
 * Connection between an event log and performance root cause analysis output
 * calculated based on only that event log.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class RootCauseAnalysisXLogConnection extends AbstractConnection {

	public static final String EVENTLOG;
	public static final String OUTPUT;

	static {
		EVENTLOG = "Event log";
		OUTPUT = "Output";
	}

	// FIELDS

	private RootCauseAnalysisParameters parameters;

	// CONSTRUCTORS

	public RootCauseAnalysisXLogConnection(XLog eventlog, RootCauseAnalysisOutput output,
			RootCauseAnalysisParameters parameters) {
		super("Connection");
		put(EVENTLOG, eventlog);
		put(OUTPUT, output);
		this.parameters = parameters;
	}

	// GETTERS AND SETTERS

	public RootCauseAnalysisParameters getParameters() {
		return parameters;
	}

}
