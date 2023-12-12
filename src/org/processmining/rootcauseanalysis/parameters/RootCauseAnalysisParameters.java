package org.processmining.rootcauseanalysis.parameters;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Week;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.contextawareperformance.models.eventcollectionviews.EventCollectionViewType;
import org.processmining.contextawareperformance.models.functions.context.Context;
import org.processmining.contextawareperformance.models.functions.context.event.EventExecutingResourceContext;
import org.processmining.contextawareperformance.models.functions.context.event.prefix.ActivityNameEventPrefixContext;
import org.processmining.contextawareperformance.models.functions.performance.Performance;
import org.processmining.contextawareperformance.models.functions.performance.activityinstance.duration.ActivityInstanceDurationPerformance;
import org.processmining.contextawareperformance.models.functions.performance.trace.duration.CaseDurationPerformance;

import com.google.common.collect.Sets;

public class RootCauseAnalysisParameters extends PluginParametersImpl {

	private static final EnumSet<EventCollectionViewType> DEFAULT_EVENT_COLLECTION_VIEW_TYPES;
	private static final Set<? extends Context<?>> DEFAULT_CONTEXT_FUNCTIONS;
	private static final Set<? extends Performance<?>> DEFAULT_PERFORMANCE_FUNCTIONS;
	private static final boolean DEFAULT_CLONE;
	private static RegularTimePeriod DEFAULT_TIMEPERIOD;

	//@formatter:off
	static {
		DEFAULT_EVENT_COLLECTION_VIEW_TYPES = EnumSet.of(
				EventCollectionViewType.ACTIVITYINSTANCE, 
				EventCollectionViewType.CASE);
		DEFAULT_CONTEXT_FUNCTIONS = Sets.<Context<?>>newHashSet(
				new ActivityNameEventPrefixContext(), 
				new EventExecutingResourceContext());
		DEFAULT_PERFORMANCE_FUNCTIONS = Sets.<Performance<?>>newHashSet(
				new ActivityInstanceDurationPerformance(),
				new CaseDurationPerformance());
		DEFAULT_TIMEPERIOD = new Week();
		DEFAULT_CLONE = false;
	}
	//@formatter:on

	// FIELDS

	private Set<EventCollectionViewType> eventCollectionViewTypesToUse;
	private Set<? extends Context<?>> contextFunctionsToUse;
	private Set<? extends Performance<?>> performanceFunctionsToUse;
	private RegularTimePeriod timePeriod;
	private boolean clone;

	// CONSTRUCTORS

	public RootCauseAnalysisParameters() {
		super();
		setEventCollectionViewTypesToUse(DEFAULT_EVENT_COLLECTION_VIEW_TYPES);
		setContextFunctionsToUse(DEFAULT_CONTEXT_FUNCTIONS);
		setPerformanceFunctionsToUse(DEFAULT_PERFORMANCE_FUNCTIONS);
		setTimePeriod(DEFAULT_TIMEPERIOD);
		setClone(DEFAULT_CLONE);
	}

	public RootCauseAnalysisParameters(Set<EventCollectionViewType> viewTypes, Set<? extends Context<?>> contexts,
			Set<? extends Performance<?>> performanceMeasures, boolean clone) {
		super();
		setEventCollectionViewTypesToUse(viewTypes);
		setContextFunctionsToUse(contexts);
		setPerformanceFunctionsToUse(performanceMeasures);
		setClone(clone);
	}

	// GETTERS AND SETTERS

	public Set<EventCollectionViewType> getEventCollectionViewTypesToUse() {
		return eventCollectionViewTypesToUse;
	}

	public void setEventCollectionViewTypesToUse(Set<EventCollectionViewType> eventCollectionViewTypesToUse) {
		this.eventCollectionViewTypesToUse = eventCollectionViewTypesToUse;
	}

	public Set<? extends Context<?>> getContextFunctionsToUse() {
		return contextFunctionsToUse;
	}

	public void setContextFunctionsToUse(Set<? extends Context<?>> contextFunctionsToUse) {
		this.contextFunctionsToUse = contextFunctionsToUse;
	}

	public Set<? extends Performance<?>> getPerformanceFunctionsToUse() {
		return performanceFunctionsToUse;
	}

	public void setPerformanceFunctionsToUse(Set<? extends Performance<?>> performanceFunctionsToUse) {
		this.performanceFunctionsToUse = performanceFunctionsToUse;
	}

	public RegularTimePeriod getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(RegularTimePeriod timePeriod) {
		this.timePeriod = timePeriod;
	}

	public boolean isClone() {
		return clone;
	}

	public void setClone(boolean clone) {
		this.clone = clone;
	}

	// METHODS

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RootCauseAnalysisParameters))
			return false;

		RootCauseAnalysisParameters parameters = (RootCauseAnalysisParameters) obj;

		return super.equals(parameters)
				&& Objects.equals(eventCollectionViewTypesToUse, parameters.eventCollectionViewTypesToUse)
				&& Objects.equals(contextFunctionsToUse, parameters.contextFunctionsToUse)
				&& Objects.equals(performanceFunctionsToUse, parameters.performanceFunctionsToUse)
				&& Objects.equals(timePeriod, parameters.timePeriod) && Objects.equals(clone, parameters.clone);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), eventCollectionViewTypesToUse, contextFunctionsToUse,
				performanceFunctionsToUse, clone, timePeriod);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(System.lineSeparator());

		builder.append("Used views:" + System.lineSeparator());

		for (EventCollectionViewType viewType : eventCollectionViewTypesToUse)
			builder.append("- " + viewType.toString() + System.lineSeparator());

		builder.append("Used contexts:" + System.lineSeparator());

		for (Context<?> context : contextFunctionsToUse)
			builder.append("- " + context.toString() + System.lineSeparator());

		builder.append("Used performance measures:" + System.lineSeparator());

		for (Performance<?> performance : performanceFunctionsToUse)
			builder.append("- " + performance.toString() + System.lineSeparator());

		builder.append("Time period:" + getTimePeriod().getClass());

		return builder.toString();
	}

}
