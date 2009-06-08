package eu.webtoolkit.jwt.chart;

public class PlotException extends RuntimeException {
	public PlotException(String message) {
		super(message);
	}

	public String what() {
		return this.getMessage();
	}
}
