/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget that displays a leaflet map.
 */
public class WLeafletMap extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WLeafletMap.class);

	/**
	 * A geographical coordinate (latitude/longitude).
	 */
	public static class Coordinate {
		private static Logger logger = LoggerFactory
				.getLogger(Coordinate.class);

		/**
		 * Default constructor.
		 * <p>
		 * Constructs a coordinate with latitude and longitude set to 0.
		 */
		public Coordinate() {
			this.lat_ = 0.0;
			this.lng_ = 0.0;
		}

		/**
		 * Create a coordinate (latitude, longitude).
		 */
		public Coordinate(double latitude, double longitude) {
			this.lat_ = latitude;
			this.lng_ = longitude;
		}

		/**
		 * Set the latitude.
		 */
		public void setLatitude(double latitude) {
			this.lat_ = latitude;
		}

		/**
		 * Get the latitude.
		 */
		public double getLatitude() {
			return this.lat_;
		}

		/**
		 * Set the longitude.
		 */
		public void setLongitude(double longitude) {
			this.lng_ = longitude;
		}

		/**
		 * Get the longitude.
		 */
		public double getLongitude() {
			return this.lng_;
		}

		/**
		 * Equality comparison operator.
		 */
		public boolean equals(final WLeafletMap.Coordinate other) {
			return this.lat_ == other.lat_ && this.lng_ == other.lng_;
		}

		private double lat_;
		private double lng_;
	}

	/**
	 * An abstract marker.
	 * <p>
	 * 
	 * This marker can be placed on a {@link WLeafletMap} at certain
	 * coordinates.
	 */
	public static abstract class Marker {
		private static Logger logger = LoggerFactory.getLogger(Marker.class);

		/**
		 * Move the marker.
		 * <p>
		 * If this marker belongs to a map, this will trigger an update of the
		 * {@link WLeafletMap} to move the marker. If it doesn&apos;t belong to
		 * a map, the position is merely updated.
		 */
		public void move(final WLeafletMap.Coordinate pos) {
			this.pos_ = pos;
			if (this.map_ != null) {
				this.moved_ = true;
				this.map_.scheduleRender();
			}
		}

		/**
		 * Get the current position.
		 * <p>
		 * 
		 * @see WLeafletMap.Marker#move(WLeafletMap.Coordinate pos)
		 */
		public WLeafletMap.Coordinate getPosition() {
			return this.pos_;
		}

		protected Marker(final WLeafletMap.Coordinate pos) {
			this.pos_ = pos;
			this.map_ = null;
			this.moved_ = false;
		}

		protected void setMap(WLeafletMap map) {
			this.map_ = map;
		}

		protected abstract void createMarkerJS(final StringBuilder ss,
				final StringBuilder postJS);

		private WLeafletMap.Coordinate pos_;
		private WLeafletMap map_;
		private boolean moved_;
		// private Marker(final WLeafletMap.Marker anon1) ;
	}

	/**
	 * A marker rendered with a widget.
	 * <p>
	 * 
	 * This can be used to place arbitrary widgets on the map.
	 * <p>
	 * The widgets will stay the same size regardless of the zoom level of the
	 * map.
	 */
	public static class WidgetMarker extends WLeafletMap.Marker {
		private static Logger logger = LoggerFactory
				.getLogger(WidgetMarker.class);

		/**
		 * Create a new {@link WidgetMarker} at the given position with the
		 * given widget.
		 * <p>
		 * The widget should have a predetermined width and height to be
		 * rendered correctly, with <code>pos</code> in the center of the
		 * widget.
		 */
		public WidgetMarker(final WLeafletMap.Coordinate pos, WWidget widget) {
			super(pos);
			this.container_ = new WContainerWidget();
			this.container_.setJavaScriptMember("wtReparentBarrier", "true");
			this.container_.addWidget(widget);
		}

		/**
		 * Get the widget.
		 */
		public WWidget getWidget() {
			if (this.container_ != null && this.container_.getCount() > 0) {
				return this.container_.getWidget(0);
			}
			return null;
		}

		protected void setMap(WLeafletMap map) {
			super.setMap(map);
			if (this.container_ != null) {
				this.container_.setParentWidget(map);
			}
		}

		protected void createMarkerJS(final StringBuilder ss,
				final StringBuilder postJS) {
			DomElement element = this.container_.createSDomElement(WApplication
					.getInstance());
			List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
			EscapeOStream js = new EscapeOStream(postJS);
			js.append("var o=")
					.append(this.container_.getJsRef())
					.append(";if(o){o.addEventListener('pointerdown',function(e){e.stopPropagation();},false);}");
			EscapeOStream es = new EscapeOStream(ss);
			char[] buf = new char[30];
			es.append("(function(){");
			es.append("var wIcon=L.divIcon({className:'',");
			if (!this.getWidget().getWidth().isAuto()
					&& !this.getWidget().getHeight().isAuto()) {
				es.append("iconSize:[");
				es.append(
						MathUtils.roundJs(this.getWidget().getWidth()
								.toPixels(), 16)).append(',');
				es.append(
						MathUtils.roundJs(this.getWidget().getHeight()
								.toPixels(), 16)).append("],");
			}
			es.append("html:'");
			es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
			element.asHTML(es, js, timeouts);
			es.popEscape();
			es.append("'});");
			es.append("return L.marker([");
			es.append(MathUtils.roundJs(this.getPosition().getLatitude(), 16))
					.append(",");
			es.append(MathUtils.roundJs(this.getPosition().getLongitude(), 16))
					.append("],");
			es.append("{icon:wIcon,keyboard:false});})()");
			;
		}

		private WContainerWidget container_;
		// private WidgetMarker(final WLeafletMap.WidgetMarker anon1) ;
	}

	/**
	 * A standard leaflet marker.
	 * <p>
	 * 
	 * See <a
	 * href="https://leafletjs.com/reference.html#marker">https://leafletjs
	 * .com/reference.html#marker</a>
	 */
	public static class LeafletMarker extends WLeafletMap.Marker {
		private static Logger logger = LoggerFactory
				.getLogger(LeafletMarker.class);

		/**
		 * Create a new marker at the given position.
		 */
		public LeafletMarker(final WLeafletMap.Coordinate pos) {
			super(pos);
		}

		protected void createMarkerJS(final StringBuilder ss,
				final StringBuilder anon2) {
			ss.append("L.marker([");
			char[] buf = new char[30];
			ss.append(MathUtils.roundJs(this.getPosition().getLatitude(), 16))
					.append(",");
			ss.append(MathUtils.roundJs(this.getPosition().getLongitude(), 16))
					.append("])");
		}
		// private LeafletMarker(final WLeafletMap.LeafletMarker anon1) ;
	}

	/**
	 * Create a new {@link WLeafletMap}.
	 */
	public WLeafletMap(WContainerWidget parent) {
		super(parent);
		this.impl_ = new WLeafletMap.Impl();
		this.options_ = new com.google.gson.JsonObject();
		this.flags_ = new BitSet();
		this.zoomLevelChanged_ = new JSignal1<Integer>(this, "zoomLevelChanged") {
		};
		this.panChanged_ = new JSignal2<Double, Double>(this, "panChanged") {
		};
		this.position_ = new WLeafletMap.Coordinate();
		this.zoomLevel_ = 13;
		this.nextMarkerId_ = 0;
		this.tileLayers_ = new ArrayList<WLeafletMap.TileLayer>();
		this.renderedTileLayersSize_ = 0;
		this.overlays_ = new ArrayList<WLeafletMap.Overlay>();
		this.renderedOverlaysSize_ = 0;
		this.markers_ = new ArrayList<WLeafletMap.MarkerEntry>();
		this.setup();
	}

	/**
	 * Create a new {@link WLeafletMap}.
	 * <p>
	 * Calls {@link #WLeafletMap(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WLeafletMap() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a new {@link WLeafletMap} with the given options.
	 */
	public WLeafletMap(final com.google.gson.JsonObject options,
			WContainerWidget parent) {
		super(parent);
		this.impl_ = new WLeafletMap.Impl();
		this.options_ = options;
		this.flags_ = new BitSet();
		this.zoomLevelChanged_ = new JSignal1<Integer>(this, "zoomLevelChanged") {
		};
		this.panChanged_ = new JSignal2<Double, Double>(this, "panChanged") {
		};
		this.position_ = new WLeafletMap.Coordinate();
		this.zoomLevel_ = 13;
		this.nextMarkerId_ = 0;
		this.tileLayers_ = new ArrayList<WLeafletMap.TileLayer>();
		this.renderedTileLayersSize_ = 0;
		this.overlays_ = new ArrayList<WLeafletMap.Overlay>();
		this.renderedOverlaysSize_ = 0;
		this.markers_ = new ArrayList<WLeafletMap.MarkerEntry>();
		this.setup();
	}

	/**
	 * Create a new {@link WLeafletMap} with the given options.
	 * <p>
	 * Calls
	 * {@link #WLeafletMap(com.google.gson.JsonObject options, WContainerWidget parent)
	 * this(options, (WContainerWidget)null)}
	 */
	public WLeafletMap(final com.google.gson.JsonObject options) {
		this(options, (WContainerWidget) null);
	}

	public void remove() {
		for (int i = 0; i < this.overlays_.size(); ++i) {
			;
		}
		this.overlays_.clear();
		for (int i = 0; i < this.markers_.size(); ++i) {
			if (!this.markers_.get(i).flags.get(MarkerEntry.BIT_REMOVED)) {
				;
			}
		}
		this.markers_.clear();
		super.remove();
	}

	/**
	 * Add a new tile layer.
	 * <p>
	 * See <a
	 * href="https://leafletjs.com/reference.html#tilelayer">https://leafletjs
	 * .com/reference.html#tilelayer</a>
	 */
	public void addTileLayer(final String urlTemplate,
			final com.google.gson.JsonObject options) {
		WLeafletMap.TileLayer layer = new WLeafletMap.TileLayer();
		layer.urlTemplate = urlTemplate;
		layer.options = options;
		this.tileLayers_.add(layer);
		this.scheduleRender();
	}

	/**
	 * Add the given marker.
	 */
	public void addMarker(WLeafletMap.Marker marker) {
		if (marker.map_ == this) {
			return;
		}
		if (marker.map_ != null) {
			marker.map_.removeMarker(marker);
		}
		marker.setMap(this);
		for (int i = 0; i < this.markers_.size(); ++i) {
			if (this.markers_.get(i).marker == marker
					&& this.markers_.get(i).flags.get(MarkerEntry.BIT_REMOVED)) {
				this.markers_.get(i).flags.clear(MarkerEntry.BIT_REMOVED);
				return;
			}
		}
		WLeafletMap.MarkerEntry entry = new WLeafletMap.MarkerEntry();
		entry.marker = marker;
		entry.flags.set(MarkerEntry.BIT_ADDED);
		entry.id = this.nextMarkerId_;
		++this.nextMarkerId_;
		this.markers_.add(entry);
		this.scheduleRender();
	}

	/**
	 * Remove the given marker.
	 */
	public void removeMarker(WLeafletMap.Marker marker) {
		for (int i = 0; i < this.markers_.size(); ++i) {
			if (this.markers_.get(i).marker == marker) {
				marker.setMap((WLeafletMap) null);
				if (this.markers_.get(i).flags.get(MarkerEntry.BIT_ADDED)) {
					this.markers_.remove(0 + i);
					return;
				}
				this.markers_.get(i).flags.set(MarkerEntry.BIT_REMOVED);
				this.scheduleRender();
				return;
			}
		}
	}

	/**
	 * Add a polyline.
	 * <p>
	 * This will draw a polyline on the map going through the given list of
	 * coordinates, with the given pen.
	 * <p>
	 * See <a
	 * href="https://leafletjs.com/reference.html#polyline">https://leafletjs
	 * .com/reference.html#polyline</a>
	 */
	public void addPolyline(final List<WLeafletMap.Coordinate> points,
			final WPen pen) {
		WLeafletMap.Polyline polyline = new WLeafletMap.Polyline(points, pen);
		this.overlays_.add(polyline);
		this.scheduleRender();
	}

	/**
	 * Add a circle.
	 * <p>
	 * This will draw a circle on the map centered at <code>center</code>, with
	 * the given <code>radius</code> (in meters), drawn with the given
	 * <code>stroke</code> and <code>fill</code>.
	 */
	public void addCircle(final WLeafletMap.Coordinate center, double radius,
			final WPen stroke, final WBrush fill) {
		WLeafletMap.Circle circle = new WLeafletMap.Circle(center, radius,
				stroke, fill);
		this.overlays_.add(circle);
		this.scheduleRender();
	}

	/**
	 * Set the current zoom level.
	 */
	public void setZoomLevel(int level) {
		this.zoomLevel_ = level;
		this.flags_.set(BIT_ZOOM_CHANGED);
		this.scheduleRender();
	}

	/**
	 * Get the current zoom level.
	 */
	public int getZoomLevel() {
		return this.zoomLevel_;
	}

	/**
	 * Pan to the given coordinate.
	 */
	public void panTo(final WLeafletMap.Coordinate center) {
		this.position_ = center;
		this.flags_.set(BIT_PAN_CHANGED);
		this.scheduleRender();
	}

	/**
	 * Get the current position.
	 */
	public WLeafletMap.Coordinate getPosition() {
		return this.position_;
	}

	/**
	 * {@link Signal} emitted when the user has changed the zoom level of the
	 * map.
	 */
	public JSignal1<Integer> zoomLevelChanged() {
		return this.zoomLevelChanged_;
	}

	/**
	 * {@link Signal} emitted when the user has panned the map.
	 */
	public JSignal2<Double, Double> panChanged() {
		return this.panChanged_;
	}

	/**
	 * Returns a JavaScript expression to the Leaflet map object.
	 * <p>
	 * You may want to use this in conjunction with {@link JSlot} or
	 * {@link WCompositeWidget#doJavaScript(String js)
	 * WCompositeWidget#doJavaScript()} in custom JavaScript code, e.g. to
	 * access features not built-in to WLeafletMap.
	 */
	public String getMapJsRef() {
		return "((function(){var o=" + this.getJsRef()
				+ ";if(o&&o.wtObj){return o.wtObj.map;}return null;})())";
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
			this.renderedTileLayersSize_ = 0;
			this.renderedOverlaysSize_ = 0;
			this.flags_.clear(BIT_PAN_CHANGED);
			this.flags_.clear(BIT_ZOOM_CHANGED);
		}
		StringBuilder ss = new StringBuilder();
		if (this.flags_.get(BIT_PAN_CHANGED)) {
			this.panToJS(ss, this.position_);
			this.flags_.clear(BIT_PAN_CHANGED);
		}
		if (this.flags_.get(BIT_ZOOM_CHANGED)) {
			this.zoomJS(ss, this.zoomLevel_);
			this.flags_.clear(BIT_ZOOM_CHANGED);
		}
		for (int i = this.renderedTileLayersSize_; i < this.tileLayers_.size(); ++i) {
			this.addTileLayerJS(ss, this.tileLayers_.get(i));
		}
		this.renderedTileLayersSize_ = this.tileLayers_.size();
		for (int i = this.renderedOverlaysSize_; i < this.overlays_.size(); ++i) {
			this.overlays_.get(i).addJS(ss, this);
		}
		this.renderedOverlaysSize_ = this.overlays_.size();
		for (int i = 0; i < this.markers_.size();) {
			if (this.markers_.get(i).flags.get(MarkerEntry.BIT_REMOVED)) {
				this.removeMarkerJS(ss, this.markers_.get(i).id);
				this.markers_.remove(0 + i);
			} else {
				++i;
			}
		}
		for (int i = 0; i < this.markers_.size(); ++i) {
			if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()
					|| this.markers_.get(i).flags.get(MarkerEntry.BIT_ADDED)) {
				this.addMarkerJS(ss, this.markers_.get(i).id,
						this.markers_.get(i).marker);
				this.markers_.get(i).flags.clear(MarkerEntry.BIT_ADDED);
			} else {
				if (this.markers_.get(i).marker.moved_) {
					this.moveMarkerJS(ss, this.markers_.get(i).id,
							this.markers_.get(i).marker.getPosition());
				}
			}
			this.markers_.get(i).marker.moved_ = false;
		}
		if (!(ss.length() == 0)) {
			this.doJavaScript(ss.toString());
		}
		super.render(flags);
	}

	private static final int BIT_ZOOM_CHANGED = 0;
	private static final int BIT_PAN_CHANGED = 1;
	private WLeafletMap.Impl impl_;
	private com.google.gson.JsonObject options_;
	private BitSet flags_;
	private JSignal1<Integer> zoomLevelChanged_;
	private JSignal2<Double, Double> panChanged_;
	private WLeafletMap.Coordinate position_;
	private int zoomLevel_;
	private long nextMarkerId_;

	static class TileLayer {
		private static Logger logger = LoggerFactory.getLogger(TileLayer.class);

		public String urlTemplate;
		public com.google.gson.JsonObject options;
	}

	private List<WLeafletMap.TileLayer> tileLayers_;
	private int renderedTileLayersSize_;
	private List<WLeafletMap.Overlay> overlays_;
	private int renderedOverlaysSize_;

	static class MarkerEntry {
		private static Logger logger = LoggerFactory
				.getLogger(MarkerEntry.class);

		public static final int BIT_ADDED = 0;
		public static final int BIT_REMOVED = 1;

		public MarkerEntry() {
			this.marker = null;
			this.id = -1;
			this.flags = new BitSet();
		}

		public WLeafletMap.Marker marker;
		public long id;
		public BitSet flags;
	}

	private List<WLeafletMap.MarkerEntry> markers_;

	private void setup() {
		this.setImplementation(this.impl_);
		this.zoomLevelChanged().addListener(this,
				new Signal1.Listener<Integer>() {
					public void trigger(Integer e1) {
						WLeafletMap.this.handleZoomLevelChanged(e1);
					}
				});
		this.panChanged().addListener(this,
				new Signal2.Listener<Double, Double>() {
					public void trigger(Double e1, Double e2) {
						WLeafletMap.this.handlePanChanged(e1, e2);
					}
				});
		WApplication app = WApplication.getInstance();
		if (app != null) {
			String leafletJSURL = "";
			String leafletCSSURL = "";
			leafletJSURL = WApplication.readConfigurationProperty(
					"leafletJSURL", leafletJSURL);
			leafletCSSURL = WApplication.readConfigurationProperty(
					"leafletCSSURL", leafletCSSURL);
			if (leafletJSURL.length() != 0 && leafletCSSURL.length() != 0) {
				app.require(leafletJSURL);
				app.useStyleSheet(new WLink(leafletCSSURL));
			} else {
				throw new WException(
						"Trying to create a WLeafletMap, but the leafletJSURL and/or leafletCSSURL properties are not configured");
			}
		} else {
			throw new WException(
					"Trying to create a WLeafletMap without an active WApplication");
		}
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WLeafletMap.js", wtjs1());
		String optionsStr = this.options_.toString();
		StringBuilder ss = new StringBuilder();
		EscapeOStream es = new EscapeOStream(ss);
		es.append("new Wt3_3_12.WLeafletMap(").append(app.getJavaScriptClass())
				.append(",").append(this.getJsRef()).append(",'");
		es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
		es.append(optionsStr);
		es.popEscape();
		es.append("',");
		char[] buf = new char[30];
		es.append(MathUtils.roundJs(this.position_.getLatitude(), 16)).append(
				",");
		es.append(MathUtils.roundJs(this.position_.getLongitude(), 16)).append(
				",");
		es.append(MathUtils.roundJs(this.zoomLevel_, 16)).append(");");
		this.setJavaScriptMember(" WLeafletMap", ss.toString());
		this.setJavaScriptMember(WT_RESIZE_JS, this.getJsRef()
				+ ".wtObj.wtResize");
	}

	private void addTileLayerJS(final StringBuilder ss,
			final WLeafletMap.TileLayer layer) {
		String optionsStr = layer.options.toString();
		EscapeOStream es = new EscapeOStream(ss);
		es.append("var o=").append(this.getJsRef())
				.append(";if(o && o.wtObj){o.wtObj.addTileLayer('");
		es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
		es.append(layer.urlTemplate);
		es.popEscape();
		es.append("','");
		es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
		es.append(optionsStr);
		es.popEscape();
		es.append("');}");
	}

	private void panToJS(final StringBuilder ss,
			final WLeafletMap.Coordinate position) {
		ss.append("var o=").append(this.getJsRef())
				.append(";if(o && o.wtObj){o.wtObj.panTo(");
		char[] buf = new char[30];
		ss.append(MathUtils.roundJs(position.getLatitude(), 16)).append(",");
		ss.append(MathUtils.roundJs(position.getLongitude(), 16)).append(");}");
	}

	private void zoomJS(final StringBuilder ss, int level) {
		ss.append("var o=").append(this.getJsRef())
				.append(";if(o && o.wtObj){o.wtObj.zoom(").append(level)
				.append(");}");
	}

	private void addMarkerJS(final StringBuilder ss, long id,
			WLeafletMap.Marker marker) {
		StringBuilder js = new StringBuilder();
		ss.append("var o=").append(this.getJsRef())
				.append(";if(o && o.wtObj){o.wtObj.addMarker(").append(id)
				.append(',');
		marker.createMarkerJS(ss, js);
		ss.append(");");
		ss.append(js.toString());
		ss.append("}");
	}

	private void removeMarkerJS(final StringBuilder ss, long id) {
		ss.append("var o=").append(this.getJsRef())
				.append(";if(o && o.wtObj){").append("o.wtObj.removeMarker(")
				.append(id).append(");}");
	}

	private void moveMarkerJS(final StringBuilder ss, long id,
			final WLeafletMap.Coordinate position) {
		ss.append("var o=").append(this.getJsRef())
				.append(";if(o && o.wtObj){").append("o.wtObj.moveMarker(")
				.append(id).append(",[");
		char[] buf = new char[30];
		ss.append(MathUtils.roundJs(position.getLatitude(), 16)).append(",");
		ss.append(MathUtils.roundJs(position.getLongitude(), 16)).append("]);");
		ss.append("}");
	}

	private void handlePanChanged(double latitude, double longitude) {
		this.position_ = new WLeafletMap.Coordinate(latitude, longitude);
	}

	private void handleZoomLevelChanged(int zoomLevel) {
		this.zoomLevel_ = zoomLevel;
	}

	private static void addPathOptions(
			final com.google.gson.JsonObject options, final WPen stroke,
			final WBrush fill) {
		if (stroke.getStyle() != PenStyle.NoPen) {
			options.add("stroke", (new com.google.gson.JsonPrimitive(true)));
			options.add("color", (new com.google.gson.JsonPrimitive(stroke
					.getColor().getCssText(false))));
			options.add("opacity", (new com.google.gson.JsonPrimitive(stroke
					.getColor().getAlpha() / 255.0)));
			double weight = stroke.getWidth().toPixels();
			weight = weight == 0 ? 1.0 : weight;
			options.add("weight", (new com.google.gson.JsonPrimitive(weight)));
			String capStyle = "";
			switch (stroke.getCapStyle()) {
			case FlatCap:
				capStyle = "butt";
				break;
			case SquareCap:
				capStyle = "square";
				break;
			case RoundCap:
				capStyle = "round";
			}
			options.add("lineCap",
					(new com.google.gson.JsonPrimitive(capStyle)));
			String joinStyle = "";
			switch (stroke.getJoinStyle()) {
			case BevelJoin:
				joinStyle = "bevel";
				break;
			case MiterJoin:
				joinStyle = "miter";
				break;
			case RoundJoin:
				joinStyle = "round";
			}
			options.add("lineJoin", (new com.google.gson.JsonPrimitive(
					joinStyle)));
		} else {
			options.add("stroke", (new com.google.gson.JsonPrimitive(false)));
		}
		if (fill.getStyle() != BrushStyle.NoBrush) {
			options.add("fill", (new com.google.gson.JsonPrimitive(true)));
			options.add("fillColor", (new com.google.gson.JsonPrimitive(fill
					.getColor().getCssText(false))));
			options.add("fillOpacity", (new com.google.gson.JsonPrimitive(fill
					.getColor().getAlpha() / 255.0)));
		} else {
			options.add("fill", (new com.google.gson.JsonPrimitive(false)));
		}
	}

	// private WLeafletMap(final WLeafletMap anon1) ;
	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WLeafletMap",
				"function(i,e,m,j,k,l){e.wtObj=this;var c=this;this.map=null;var f={},h=l,g=[j,k];this.addTileLayer=function(a,b){b=JSON.parse(b);L.tileLayer(a,b).addTo(c.map)};this.zoom=function(a){h=a;c.map.setZoom(a)};this.panTo=function(a,b){g=[a,b];c.map.panTo([a,b])};this.addPolyline=function(a,b){b=JSON.parse(b);L.polyline(a,b).addTo(c.map)};this.addCircle=function(a,b){b=JSON.parse(b);L.circle(a,b).addTo(c.map)};this.addMarker=function(a,b){b.addTo(c.map); f[a]=b};this.removeMarker=function(a){var b=f[a];if(b){c.map.removeLayer(b);delete f[a]}};this.moveMarker=function(a,b){(a=f[a])&&a.setLatLng(b)};this.wtResize=function(){c.map.invalidateSize()};e.wtEncodeValue=function(){var a=c.map.getCenter();a=[a.lat,a.lng];var b=c.map.getZoom();return JSON.stringify({position:a,zoom:b})};this.init=function(a,b,n){a=JSON.parse(a);a.center=b;a.zoom=n;c.map=L.map(e,a);c.map.on(\"zoomend\",function(){var d=c.map.getZoom();if(d!=h){i.emit(e,\"zoomLevelChanged\",d);h= d}});c.map.on(\"moveend\",function(){var d=c.map.getCenter();if(d.lat!=g[0]||d.lng!=g[1]){i.emit(e,\"panChanged\",d.lat,d.lng);g=[d.lat,d.lng]}})};this.init(m,[j,k],l)}");
	}

	static class Impl extends WWebWidget {
		private static Logger logger = LoggerFactory.getLogger(Impl.class);

		Impl() {
			super();
			this.setInline(false);
			this.setIgnoreChildRemoves(true);
		}

		DomElementType getDomElementType() {
			return DomElementType.DomElement_DIV;
		}
	}

	static abstract class Overlay {
		private static Logger logger = LoggerFactory.getLogger(Overlay.class);

		abstract void addJS(final StringBuilder ss, WLeafletMap map);

		Overlay() {
		}
		// private Overlay(final WLeafletMap.Overlay anon1) ;
	}

	static class Polyline extends WLeafletMap.Overlay {
		private static Logger logger = LoggerFactory.getLogger(Polyline.class);

		public List<WLeafletMap.Coordinate> points;
		public WPen pen;

		Polyline(final List<WLeafletMap.Coordinate> points, final WPen pen) {
			super();
			this.points = points;
			this.pen = pen;
		}

		void addJS(final StringBuilder ss, WLeafletMap map) {
			if (this.pen.getStyle() == PenStyle.NoPen) {
				return;
			}
			com.google.gson.JsonObject options = new com.google.gson.JsonObject();
			addPathOptions(options, this.pen, new WBrush(BrushStyle.NoBrush));
			String optionsStr = options.toString();
			EscapeOStream es = new EscapeOStream(ss);
			es.append("var o=").append(map.getJsRef())
					.append(";if(o && o.wtObj){o.wtObj.addPolyline(");
			es.append("[");
			for (int i = 0; i < this.points.size(); ++i) {
				if (i != 0) {
					es.append(',');
				}
				es.append("[");
				char[] buf = new char[30];
				es.append(
						MathUtils.roundJs(this.points.get(i).getLatitude(), 16))
						.append(",");
				es.append(MathUtils.roundJs(this.points.get(i).getLongitude(),
						16));
				es.append("]");
			}
			es.append("],'");
			es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
			es.append(optionsStr);
			es.popEscape();
			es.append("');}");
		}
		// private Polyline(final WLeafletMap.Polyline anon1) ;
	}

	static class Circle extends WLeafletMap.Overlay {
		private static Logger logger = LoggerFactory.getLogger(Circle.class);

		public WLeafletMap.Coordinate center;
		public double radius;
		public WPen stroke;
		public WBrush fill;

		Circle(final WLeafletMap.Coordinate center, double radius,
				final WPen stroke, final WBrush fill) {
			super();
			this.center = center;
			this.radius = radius;
			this.stroke = stroke;
			this.fill = fill;
		}

		void addJS(final StringBuilder ss, WLeafletMap map) {
			com.google.gson.JsonObject options = new com.google.gson.JsonObject();
			options.add("radius", (new com.google.gson.JsonPrimitive(
					this.radius)));
			addPathOptions(options, this.stroke, this.fill);
			String optionsStr = options.toString();
			EscapeOStream es = new EscapeOStream(ss);
			es.append("var o=").append(map.getJsRef())
					.append(";if(o && o.wtObj){").append("o.wtObj.addCircle([");
			char[] buf = new char[30];
			es.append(MathUtils.roundJs(this.center.getLatitude(), 16)).append(
					",");
			es.append(MathUtils.roundJs(this.center.getLongitude(), 16))
					.append("],'");
			es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
			es.append(optionsStr);
			es.popEscape();
			es.append("');}");
		}
		// private Circle(final WLeafletMap.Circle anon1) ;
	}
}
