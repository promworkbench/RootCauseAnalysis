package org.processmining.rootcauseanalysis.utils;

import java.util.Date;
import java.util.Set;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

import com.google.common.collect.Sets;

public class TimeSeriesUtils {

	public static RegularTimePeriod getMCSR(TimeSeriesCollection collection) {

		Set<RegularTimePeriod> timeperiods = Sets.newHashSet();

		for (int i = 0; i < collection.getSeriesCount(); i++) {
			TimeSeries series = collection.getSeries(i);
			timeperiods.add(series.getTimePeriod(0));
		}

		RegularTimePeriod max = null;
		for (RegularTimePeriod period : timeperiods) {
			max = max == null ? period
					: ((period.getEnd().getTime() - period.getStart().getTime()) > (max.getEnd().getTime()
							- max.getStart().getTime())) ? period : max;
		}

		return null;

	}

	public static TimeSeriesCollection resampleTimeSeries(TimeSeriesCollection collection,
			RegularTimePeriod periodClass) {
		TimeSeriesCollection resampledCollection = new TimeSeriesCollection();

		for (int t = 0; t < collection.getSeriesCount(); t++) {
			TimeSeries resampledSeries = resampleTimeSeries(collection.getSeries(t), periodClass);
			if (resampledSeries != null)
				resampledCollection.addSeries(resampledSeries);
		}

		return resampledCollection;
	}

	public static TimeSeries resampleTimeSeries(TimeSeries series, RegularTimePeriod periodClass) {
		TimeSeries resampledSeries = new TimeSeries(series.getDescription() + " (resampled)");
		resampledSeries.setDescription(series.getDescription() + " (resampled)");

		// Keep track of measurements per period
		UnifiedMap<RegularTimePeriod, Set<Number>> measurements = new UnifiedMap<RegularTimePeriod, Set<Number>>();

		// Fill map of new periods
		for (int m = 0; m < series.getItemCount(); m++) {
			Date measurementDate = series.getDataItem(m).getPeriod().getStart();
			Number value = series.getDataItem(m).getValue();

			RegularTimePeriod period = null;

			if (periodClass instanceof Second)
				period = new Second(measurementDate);
			if (periodClass instanceof Hour)
				period = new Hour(measurementDate);
			if (periodClass instanceof Day)
				period = new Day(measurementDate);
			if (periodClass instanceof Week)
				period = new Week(measurementDate);
			if (periodClass instanceof Month)
				period = new Month(measurementDate);
			if (periodClass instanceof Year)
				period = new Year(measurementDate);

			if (!measurements.containsKey(period))
				measurements.put(period, new UnifiedSet<Number>());
			measurements.get(period).add(value);
		}

		// Take average values for periods with multiple measurements
		for (RegularTimePeriod period : measurements.keySet()) {
			long sum = 0l;
			for (Number measurement : measurements.get(period))
				sum += measurement.longValue();
			resampledSeries.add(period, sum / measurements.get(period).size());
		}

		// Impute missing values by copying last known values for unknown values
		int imputedValues = 0;
		RegularTimePeriod nextPeriod = resampledSeries.getTimePeriod(0);
		while (startsBefore(nextPeriod, resampledSeries.getTimePeriod(resampledSeries.getItemCount() - 1))
				&& imputedValues < series.getItemCount()) {
			if (resampledSeries.getDataItem(nextPeriod) == null) {
				resampledSeries.add(nextPeriod, resampledSeries.getValue(nextPeriod.previous()));
				imputedValues++;
			}
			nextPeriod = nextPeriod.next();
		}

		if (imputedValues == series.getItemCount()) {
			System.out.println("Cannot resample " + series.getDescription());
			return null;
		} else
			return resampledSeries;
	}

	public static boolean startsBefore(RegularTimePeriod x, RegularTimePeriod y) {
		return x.getStart().compareTo(y.getStart()) < 0 ? true : false;
	}

	public static boolean startsBefore(TimeSeries x, TimeSeries y) {
		return startsBefore(x.getDataItem(0).getPeriod(), y.getDataItem(0).getPeriod());
	}

	public static boolean endsBefore(RegularTimePeriod x, RegularTimePeriod y) {
		return x.getEnd().compareTo(y.getEnd()) < 0 ? true : false;
	}

	public static boolean endsBefore(TimeSeries x, TimeSeries y) {
		return endsBefore(x.getDataItem(x.getItemCount() - 1).getPeriod(),
				y.getDataItem(y.getItemCount() - 1).getPeriod());
	}

}
