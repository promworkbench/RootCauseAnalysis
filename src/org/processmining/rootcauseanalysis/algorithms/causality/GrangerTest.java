/*
 * Copyright (c) 2011, Sergey Edunov. All Rights Reserved.
 *
 * This file is part of JQuant library.
 * https://github.com/Edunov/JQuant/tree/master/modules/jquant-core/src/main/
 * java/ru/algorithmist/jquant/math JQuant library is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * JQuant is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JQuant. If not, see <http://www.gnu.org/licenses/>.
 */
package org.processmining.rootcauseanalysis.algorithms.causality;

import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.processmining.rootcauseanalysis.models.GrangerTestResult;

/**
 * Implementation of Granger causality test in pure Java. It is based on linear
 * regression implementation in Apache Commons Math.
 * 
 * @author Sergey Edunov Date: 06.01.11
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl> (modified code to work with
 *         Apache commons-math v3+ (tested with v3.6.1) rather than v2.2)
 *
 */
public class GrangerTest {

	/**
	 * Returns p-value for Granger causality test.
	 *
	 * @param y
	 *            - predictable variable
	 * @param x
	 *            - predictor
	 * @param L
	 *            - lag, should be 1 or greater.
	 * @return p-value of Granger causality
	 */
	public static GrangerTestResult granger(double[] y, double[] x, int L) {
		OLSMultipleLinearRegression h0 = new OLSMultipleLinearRegression();
		OLSMultipleLinearRegression h1 = new OLSMultipleLinearRegression();

		double[][] laggedY = createLaggedSide(L, y);

		double[][] laggedXY = createLaggedSide(L, x, y);

		int n = laggedY.length;

		h0.newSampleData(strip(L, y), laggedY);
		h1.newSampleData(strip(L, y), laggedXY);

		double rs0[] = h0.estimateResiduals();
		double rs1[] = h1.estimateResiduals();

		double TSS1 = tss(strip(L, y));

		double RSS0 = sqrSum(rs0);
		double RSS1 = sqrSum(rs1);

		double ftest = ((RSS0 - RSS1) / L) / (RSS1 / (n - 2 * L - 1));

		FDistribution fDist = new FDistribution(L, n - 2 * L - 1);

		double pValue = 1.0 - fDist.cumulativeProbability(ftest);
		return new GrangerTestResult(ftest, (1 - RSS1 / TSS1), pValue);

	}

	private static double tss(double[] y) {
		double res = 0;
		double avg = StatUtils.mean(y);
		for (double yi : y) {
			res += (yi - avg) * (yi - avg);
		}
		return res;
	}

	private static double[][] createLaggedSide(int L, double[]... a) {
		int n = a[0].length - L;
		double[][] res = new double[n][L * a.length + 1];
		for (int i = 0; i < a.length; i++) {
			double[] ai = a[i];
			for (int l = 0; l < L; l++) {
				for (int j = 0; j < n; j++) {
					res[j][i * L + l] = ai[l + j];
				}
			}
		}
		for (int i = 0; i < n; i++) {
			res[i][L * a.length] = 1;
		}
		return res;
	}

	public static double sqrSum(double[] a) {
		double res = 0;
		for (double v : a) {
			res += v * v;
		}
		return res;
	}

	// Returns the suffix from array a starting from index l.
	public static double[] strip(int l, double[] a) {
		double[] res = new double[a.length - l];
		System.arraycopy(a, l, res, 0, res.length);
		return res;
	}

}