/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

class PlotException extends RuntimeException {
	PlotException(String message) {
		super(message);
	}

	String what() {
		return this.getMessage();
	}
}
