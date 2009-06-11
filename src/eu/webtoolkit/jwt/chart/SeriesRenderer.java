package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.WPointF;

abstract class SeriesRenderer {
	public void destroy() {
	}

	public abstract void addValue(double x, double y, double stacky);

	public abstract void paint();

	protected WChart2DRenderer renderer_;
	protected WDataSeries series_;

	protected SeriesRenderer(WChart2DRenderer renderer, WDataSeries series,
			SeriesRenderIterator it) {
		this.renderer_ = renderer;
		this.series_ = series;
		this.it_ = it;
	}

	protected static double crisp(double u) {
		return Math.floor(u) + 0.5;
	}

	protected WPointF hv(WPointF p) {
		return this.renderer_.hv(p);
	}

	protected WPointF hv(double x, double y) {
		return this.renderer_.hv(x, y);
	}

	protected SeriesRenderIterator it_;
}
