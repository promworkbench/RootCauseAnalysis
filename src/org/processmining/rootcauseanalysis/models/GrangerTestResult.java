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

package org.processmining.rootcauseanalysis.models;

/**
 * @author "Sergey Edunov"
 * @version 1/13/11
 */
public class GrangerTestResult {

	// FIELDS

	private double fStat;
	private double r2;
	private double pValue;

	// CONSTRUCTORS

	public GrangerTestResult(double fStat, double r2, double pValue) {
		this.fStat = fStat;
		this.r2 = r2;
		this.pValue = pValue;
	}

	// GETTERS AND SETTERS

	public double getFStat() {
		return fStat;
	}

	public double getR2() {
		return r2;
	}

	public double getPValue() {
		return pValue;
	}
}