package org.processmining.rootcauseanalysis.plugins;

import java.util.Collection;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.rootcauseanalysis.algorithms.RootCauseAnalysisAlgorithm;
import org.processmining.rootcauseanalysis.connections.RootCauseAnalysisXLogConnection;
import org.processmining.rootcauseanalysis.constants.AuthorConstants;
import org.processmining.rootcauseanalysis.help.RootCauseAnalysisHelp;
import org.processmining.rootcauseanalysis.models.RootCauseAnalysisOutput;
import org.processmining.rootcauseanalysis.parameters.RootCauseAnalysisParameters;

@Plugin(
		name = "Discover Root Causes for Process Performance",
		parameterLabels = { "Event log", "Parameters" },
		returnLabels = { "Output" },
		returnTypes = { RootCauseAnalysisOutput.class },
		help = RootCauseAnalysisHelp.TEXT)

public class RootCauseAnalysisPlugin extends RootCauseAnalysisAlgorithm {

	@UITopiaVariant(
			author = AuthorConstants.NAME,
			email = AuthorConstants.EMAIL,
			affiliation = AuthorConstants.AFFILIATION)
	@PluginVariant(
			variantLabel = "Discover Root Causes for Process Performance, Log only, UI",
			requiredParameterLabels = { 0 })
	public RootCauseAnalysisOutput runUI(UIPluginContext pluginContext, XLog eventlog) {
		RootCauseAnalysisParameters parameters = new RootCauseAnalysisParameters();
		//TODO set parameters using a GUI
		return runConnectionsXLog(pluginContext, eventlog, parameters);
	}

	@PluginVariant(
			variantLabel = "Discover Root Causes for Process Performance, Parameters",
			requiredParameterLabels = { 0, 1 })
	public RootCauseAnalysisOutput run(PluginContext pluginContext, XLog eventlog,
			RootCauseAnalysisParameters parameters) {
		return runConnectionsXLog(pluginContext, eventlog, parameters);
	}

	private RootCauseAnalysisOutput runConnectionsXLog(PluginContext pluginContext, XLog eventlog,
			RootCauseAnalysisParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<RootCauseAnalysisXLogConnection> connections;
			try {
				connections = pluginContext.getConnectionManager().getConnections(RootCauseAnalysisXLogConnection.class,
						pluginContext, eventlog);
				for (RootCauseAnalysisXLogConnection connection : connections) {
					if (connection.getObjectWithRole(RootCauseAnalysisXLogConnection.EVENTLOG).equals(eventlog)
							&& connection.getParameters().equals(parameters)) {
						parameters.displayMessage("Connection found, returning the previously calculated output.");
						return connection.getObjectWithRole(RootCauseAnalysisXLogConnection.OUTPUT);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
				parameters.displayMessage("No connection found, have to calculate now.");
			}
		}

		RootCauseAnalysisOutput output = runPrivate(pluginContext, eventlog, parameters);

		if (parameters.isTryConnections()) {
			pluginContext.getConnectionManager()
					.addConnection(new RootCauseAnalysisXLogConnection(eventlog, output, parameters));
		}

		return output;
	}

	private RootCauseAnalysisOutput runPrivate(PluginContext pluginContext, XLog eventlog,
			RootCauseAnalysisParameters parameters) {
		long time = -System.currentTimeMillis();
		parameters.displayMessage("[RootCauseAnalysisPlugin] Start");
		parameters.displayMessage("[RootCauseAnalysisPlugin] Parameters: " + parameters.toString());

		RootCauseAnalysisOutput output;
		output = apply(pluginContext, eventlog, parameters);

		time += System.currentTimeMillis();
		parameters.displayMessage(
				"[RootCauseAnalysisPlugin] End (took " + DurationFormatUtils.formatDurationHMS(time) + ").");
		return output;
	}

}
