/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

public class PlotException extends RuntimeException {
	public PlotException(String message) {
		super(message);
	}

	public String what() {
		return this.getMessage();
	}
}
