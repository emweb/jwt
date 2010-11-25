/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * A widget that displays a google map.
 * <p>
 * 
 * This widget uses the online Google Maps server to display a map. It exposes a
 * part of the google maps API.
 * <p>
 * To use the map on a public server you will need to obtain a key. The widget
 * will look for this key as the configuration property
 * <code>&quot;google_api_key&quot;</code>. If this configuration property has
 * not been set, it will use a key that is suitable for localhost.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * Styling through CSS is not applicable.
 * <p>
 * Contributed by: Richard Ulrich.
 */
public class WGoogleMap extends WCompositeWidget {
	/**
	 * MapTypeControl.
	 */
	public enum MapTypeControl {
		/**
		 * Show no maptype control.
		 */
		NoControl,
		/**
		 * Show the default maptype control.
		 */
		DefaultControl,
		/**
		 * Show the menu maptype control.
		 */
		MenuControl,
		/**
		 * Show the hierarchical maptype control.
		 */
		HierarchicalControl;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * A geographical coordinate (latitude/longitude).
	 */
	public static class Coordinate {
		/**
		 * Default constructor.
		 */
		public Coordinate() {
			this.lat_ = 0;
			this.lon_ = 0;
		}

		/**
		 * Creates with given latitude and longitude.
		 */
		public Coordinate(double lat, double lon) {
			this.setLatitude(lat);
			this.setLongitude(lon);
		}

		/**
		 * Sets the latitude.
		 */
		public void setLatitude(double latitude) {
			if (latitude < -90.0 || latitude > 90.0) {
				throw new RuntimeException("invalid latitude: "
						+ String.valueOf(latitude));
			}
			this.lat_ = latitude;
		}

		/**
		 * Sets the longitude.
		 */
		public void setLongitude(double longitude) {
			if (longitude < -180.0 || longitude > 180.0) {
				throw new RuntimeException("invalid longitude: "
						+ String.valueOf(longitude));
			}
			this.lon_ = longitude;
		}

		/**
		 * Returns the latitude.
		 */
		public double getLatitude() {
			return this.lat_;
		}

		/**
		 * Returns the longitude.
		 */
		public double getLongitude() {
			return this.lon_;
		}

		/**
		 * Calculates the distance between two points in km (approximate).
		 * <p>
		 * The calculation uses a sphere. Results can be out by 0.3%.
		 */
		public double distanceTo(WGoogleMap.Coordinate rhs) {
			final double lat1 = this.lat_ * 3.14159265358979323846 / 180.0;
			final double lat2 = rhs.getLatitude() * 3.14159265358979323846 / 180.0;
			final double deltaLong = (rhs.getLongitude() - this.lon_) * 3.14159265358979323846 / 180.0;
			final double angle = Math.sin(lat1) * Math.sin(lat2)
					+ Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLong);
			final double earthRadius = 6371.0;
			final double dist = earthRadius * Math.acos(angle);
			return dist;
		}

		private double lat_;
		private double lon_;
	}

	/**
	 * Creates a map widget with optional parent.
	 */
	public WGoogleMap(WContainerWidget parent) {
		super();
		this.clicked_ = new JSignal1<WGoogleMap.Coordinate>(this, "click") {
		};
		this.doubleClicked_ = new JSignal1<WGoogleMap.Coordinate>(this,
				"dblclick") {
		};
		this.mouseMoved_ = new JSignal1<WGoogleMap.Coordinate>(this,
				"mousemove") {
		};
		this.additions_ = new ArrayList<String>();
		this.setImplementation(new WContainerWidget());
		WApplication app = WApplication.getInstance();
		String googlekey = localhost_key;
		googlekey = WApplication.readConfigurationProperty("google_api_key",
				googlekey);
		final String gmuri = "http://www.google.com/jsapi?key=" + googlekey;
		app.require(gmuri, "google");
		if (parent != null) {
			parent.addWidget(this);
		}
	}

	/**
	 * Creates a map widget with optional parent.
	 * <p>
	 * Calls {@link #WGoogleMap(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WGoogleMap() {
		this((WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		super.remove();
	}

	/**
	 * Adds a marker overlay to the map.
	 */
	public void addMarker(WGoogleMap.Coordinate pos) {
		StringWriter strm = new StringWriter();
		strm.append(
				"var marker = new google.maps.Marker(new google.maps.LatLng(")
				.append(String.valueOf(pos.getLatitude())).append(", ").append(
						String.valueOf(pos.getLongitude())).append("));")
				.append(this.getJsRef()).append(".map.addOverlay(marker);");
		this.doGmJavaScript(strm.toString(), false);
	}

	/**
	 * Adds a polyline overlay to the map.
	 * <p>
	 * specify a value between 0.0 and 1.0 for the opacity.
	 */
	public void addPolyline(List<WGoogleMap.Coordinate> points, WColor color,
			int width, double opacity) {
		opacity = Math.max(Math.min(opacity, 1.0), 0.0);
		StringWriter strm = new StringWriter();
		strm.append("var waypoints = [];");
		for (int i = 0; i < points.size(); ++i) {
			strm.append("waypoints[").append(String.valueOf(i)).append(
					"] = new google.maps.LatLng(").append(
					String.valueOf(points.get(i).getLatitude())).append(", ")
					.append(String.valueOf(points.get(i).getLongitude()))
					.append(");");
		}
		strm.append("var poly = new google.maps.Polyline(waypoints, \"")
				.append(color.getCssText()).append("\", ").append(
						String.valueOf(width)).append(", ").append(
						String.valueOf(opacity)).append(");").append(
						this.getJsRef()).append(".map.addOverlay(poly);");
		this.doGmJavaScript(strm.toString(), true);
	}

	/**
	 * Adds a polyline overlay to the map.
	 * <p>
	 * Calls
	 * {@link #addPolyline(List points, WColor color, int width, double opacity)
	 * addPolyline(points, WColor.red, 2, 1.0)}
	 */
	public final void addPolyline(List<WGoogleMap.Coordinate> points) {
		addPolyline(points, WColor.red, 2, 1.0);
	}

	/**
	 * Adds a polyline overlay to the map.
	 * <p>
	 * Calls
	 * {@link #addPolyline(List points, WColor color, int width, double opacity)
	 * addPolyline(points, color, 2, 1.0)}
	 */
	public final void addPolyline(List<WGoogleMap.Coordinate> points,
			WColor color) {
		addPolyline(points, color, 2, 1.0);
	}

	/**
	 * Adds a polyline overlay to the map.
	 * <p>
	 * Calls
	 * {@link #addPolyline(List points, WColor color, int width, double opacity)
	 * addPolyline(points, color, width, 1.0)}
	 */
	public final void addPolyline(List<WGoogleMap.Coordinate> points,
			WColor color, int width) {
		addPolyline(points, color, width, 1.0);
	}

	/**
	 * Removes all overlays from the map.
	 */
	public void clearOverlays() {
		this.doGmJavaScript(this.getJsRef() + ".map.clearOverlays();", false);
	}

	/**
	 * Opens a text bubble with html text at a specific location.
	 */
	public void openInfoWindow(WGoogleMap.Coordinate pos, CharSequence myHtml) {
		StringWriter strm = new StringWriter();
		strm.append(this.getJsRef()).append(
				".map.openInfoWindow(new google.maps.LatLng(").append(
				String.valueOf(pos.getLatitude())).append(", ").append(
				String.valueOf(pos.getLongitude())).append("), ").append(
				WWebWidget.jsStringLiteral(myHtml)).append(");");
		this.doGmJavaScript(strm.toString(), false);
	}

	/**
	 * Sets the map view to the given center.
	 */
	public void setCenter(WGoogleMap.Coordinate center) {
		StringWriter strm = new StringWriter();
		strm.append(this.getJsRef()).append(
				".map.setCenter(new google.maps.LatLng(").append(
				String.valueOf(center.getLatitude())).append(", ").append(
				String.valueOf(center.getLongitude())).append("));");
		this.doGmJavaScript(strm.toString(), false);
	}

	/**
	 * Sets the map view to the given center and zoom level.
	 */
	public void setCenter(WGoogleMap.Coordinate center, int zoom) {
		StringWriter strm = new StringWriter();
		strm.append(this.getJsRef()).append(
				".map.setCenter(new google.maps.LatLng(").append(
				String.valueOf(center.getLatitude())).append(", ").append(
				String.valueOf(center.getLongitude())).append("), ").append(
				String.valueOf(zoom)).append(");");
		this.doGmJavaScript(strm.toString(), false);
	}

	/**
	 * Changes the center point of the map to the given point.
	 * <p>
	 * If the point is already visible in the current map view, change the
	 * center in a smooth animation.
	 */
	public void panTo(WGoogleMap.Coordinate center) {
		StringWriter strm = new StringWriter();
		strm.append(this.getJsRef()).append(
				".map.panTo(new google.maps.LatLng(").append(
				String.valueOf(center.getLatitude())).append(", ").append(
				String.valueOf(center.getLongitude())).append("));");
		this.doGmJavaScript(strm.toString(), false);
	}

	/**
	 * Zooms the map to a region defined by a bounding box.
	 */
	public void zoomWindow(WGoogleMap.Coordinate topLeft,
			WGoogleMap.Coordinate rightBottom) {
		WGoogleMap.Coordinate topLeftC = topLeft;
		WGoogleMap.Coordinate rightBottomC = rightBottom;
		final WGoogleMap.Coordinate center = new WGoogleMap.Coordinate(
				(topLeftC.getLatitude() + rightBottomC.getLatitude()) / 2.0,
				(topLeftC.getLongitude() + rightBottomC.getLongitude()) / 2.0);
		topLeftC = new WGoogleMap.Coordinate(Math.min(topLeftC.getLatitude(),
				rightBottomC.getLatitude()), Math.min(topLeftC.getLongitude(),
				rightBottomC.getLongitude()));
		rightBottomC = new WGoogleMap.Coordinate(Math.max(topLeftC
				.getLatitude(), rightBottomC.getLatitude()), Math.max(topLeftC
				.getLongitude(), rightBottomC.getLongitude()));
		StringWriter strm = new StringWriter();
		strm
				.append(
						"var bbox = new google.maps.LatLngBounds(new google.maps.LatLng(")
				.append(String.valueOf(topLeftC.getLatitude())).append(", ")
				.append(String.valueOf(topLeftC.getLongitude())).append("), ")
				.append("new google.maps.LatLng(").append(
						String.valueOf(rightBottomC.getLatitude()))
				.append(", ").append(
						String.valueOf(rightBottomC.getLongitude())).append(
						"));").append("var zooml = ").append(this.getJsRef())
				.append(".map.getBoundsZoomLevel(bbox);").append(
						this.getJsRef()).append(
						".map.setCenter(new google.maps.LatLng(").append(
						String.valueOf(center.getLatitude())).append(", ")
				.append(String.valueOf(center.getLongitude())).append(
						"), zooml);");
		this.doGmJavaScript(strm.toString(), true);
	}

	/**
	 * Sets the zoom level to the given new value.
	 */
	public void setZoom(int level) {
		this.doGmJavaScript(this.getJsRef() + ".map.setZoom("
				+ String.valueOf(level) + ");", false);
	}

	/**
	 * Increments zoom level by one.
	 */
	public void zoomIn() {
		this.doGmJavaScript(this.getJsRef() + ".map.zoomIn();", false);
	}

	/**
	 * Decrements zoom level by one.
	 */
	public void zoomOut() {
		this.doGmJavaScript(this.getJsRef() + ".map.zoomOut();", false);
	}

	/**
	 * Stores the current map position and zoom level.
	 * <p>
	 * You can later restore this position using
	 * {@link WGoogleMap#returnToSavedPosition() returnToSavedPosition()}.
	 */
	public void savePosition() {
		this.doGmJavaScript(this.getJsRef() + ".map.savePosition();", false);
	}

	/**
	 * Restores the map view that was saved by {@link WGoogleMap#savePosition()
	 * savePosition()}.
	 */
	public void returnToSavedPosition() {
		this.doGmJavaScript(this.getJsRef() + ".map.returnToSavedPosition();",
				false);
	}

	/**
	 * Notifies the map of a change of the size of its container.
	 * <p>
	 * Call this method after the size of the container DOM object has changed,
	 * so that the map can adjust itself to fit the new size.
	 */
	public void checkResize() {
		this.doGmJavaScript(this.getJsRef() + ".map.checkResize();", false);
	}

	/**
	 * Enables the dragging of the map (enabled by default).
	 */
	public void enableDragging() {
		this.doGmJavaScript(this.getJsRef() + ".map.enableDragging();", false);
	}

	/**
	 * Disables the dragging of the map.
	 */
	public void disableDragging() {
		this.doGmJavaScript(this.getJsRef() + ".map.disableDragging();", false);
	}

	/**
	 * Enables double click to zoom in and out (enabled by default).
	 */
	public void enableDoubleClickZoom() {
		this.doGmJavaScript(this.getJsRef() + ".map.enableDoubleClickZoom();",
				false);
	}

	/**
	 * Disables double click to zoom in and out.
	 */
	public void disableDoubleClickZoom() {
		this.doGmJavaScript(this.getJsRef() + ".map.disableDoubleClickZoom();",
				false);
	}

	/**
	 * Enables the GoogleBar, an integrated search control, on the map.
	 * <p>
	 * When enabled, this control takes the place of the default Powered By
	 * Google logo.
	 * <p>
	 * This control is initially disabled.
	 */
	public void enableGoogleBar() {
		this.doGmJavaScript(this.getJsRef() + ".map.enableGoogleBar();", false);
	}

	/**
	 * Disables the GoogleBar integrated search control.
	 * <p>
	 * When disabled, the default Powered by Google logo occupies the position
	 * formerly containing this control. Note that this control is already
	 * disabled by default.
	 */
	public void disableGoogleBar() {
		this
				.doGmJavaScript(this.getJsRef() + ".map.disableGoogleBar();",
						false);
	}

	/**
	 * Enables zooming using a mouse&apos;s scroll wheel.
	 * <p>
	 * Scroll wheel zoom is disabled by default.
	 */
	public void enableScrollWheelZoom() {
		this.doGmJavaScript(this.getJsRef() + ".map.enableScrollWheelZoom();",
				false);
	}

	/**
	 * Disables zooming using a mouse&apos;s scroll wheel.
	 * <p>
	 * Scroll wheel zoom is disabled by default.
	 */
	public void disableScrollWheelZoom() {
		this.doGmJavaScript(this.getJsRef() + ".map.disableScrollWheelZoom();",
				false);
	}

	/**
	 * Sets the map type control.
	 * <p>
	 * The control allows selecting and switching between supported map types
	 * via buttons.
	 */
	public void setMapTypeControl(WGoogleMap.MapTypeControl type) {
		String control = "";
		switch (type) {
		case DefaultControl:
			control = "google.maps.MapTypeControl";
			break;
		case MenuControl:
			control = "google.maps.MenuMapTypeControl";
			break;
		case HierarchicalControl:
			control = "google.maps.HierarchicalMapTypeControl";
			break;
		default:
			control = "";
		}
		StringWriter strm = new StringWriter();
		strm.append(this.getJsRef()).append(".map.removeControl(").append(
				this.getJsRef()).append(".mtc);");
		if (!control.equals("")) {
			strm.append("var mtc = new ").append(control).append("();").append(
					this.getJsRef()).append(".mtc = mtc;").append(
					this.getJsRef()).append(".map.addControl(mtc);");
		}
		this.doGmJavaScript(strm.toString(), false);
	}

	/**
	 * The click event.
	 * <p>
	 * This event is fired when the user clicks on the map with the mouse.
	 */
	public JSignal1<WGoogleMap.Coordinate> clicked() {
		return this.clicked_;
	}

	/**
	 * The double click event.
	 * <p>
	 * This event is fired when a double click is done on the map.
	 */
	public JSignal1<WGoogleMap.Coordinate> doubleClicked() {
		return this.doubleClicked_;
	}

	/**
	 * This event is fired when the user moves the mouse inside the map.
	 */
	public JSignal1<WGoogleMap.Coordinate> mouseMoved() {
		return this.mouseMoved_;
	}

	private JSignal1<WGoogleMap.Coordinate> clicked_;
	private JSignal1<WGoogleMap.Coordinate> doubleClicked_;
	private JSignal1<WGoogleMap.Coordinate> mouseMoved_;

	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			StringWriter strm = new StringWriter();
			strm
					.append("{ function initialize() {var self = ")
					.append(this.getJsRef())
					.append(
							";var map = new google.maps.Map2(self);map.setCenter(new google.maps.LatLng(47.01887777, 8.651888), 13);self.map = map;google.maps.Event.addListener(map, \"click\", function(overlay, latlng) {if (latlng) {")
					.append(
							this.clicked_
									.createCall("latlng.lat() +' '+ latlng.lng()"))
					.append(
							";}});google.maps.Event.addListener(map, \"dblclick\", function(overlay, latlng) {if (latlng) {")
					.append(
							this.doubleClicked_
									.createCall("latlng.lat() +' '+ latlng.lng()"))
					.append(
							";}});google.maps.Event.addListener(map, \"mousemove\", function(latlng) {if (latlng) {")
					.append(
							this.mouseMoved_
									.createCall("latlng.lat() +' '+ latlng.lng()"))
					.append(";}});");
			for (int i = 0; i < this.additions_.size(); i++) {
				strm.append(this.additions_.get(i));
			}
			strm
					.append("}google.load(\"maps\", \"2\", {other_params:\"sensor=false\", callback: initialize});}");
			this.additions_.clear();
			WApplication.getInstance().doJavaScript(strm.toString());
		}
		super.render(flags);
	}

	private List<String> additions_;

	private void doGmJavaScript(String jscode, boolean sepScope) {
		String js = jscode;
		if (sepScope) {
			js = "{" + js + "}";
		}
		if (this.isRendered()) {
			WApplication.getInstance().doJavaScript(js);
		} else {
			this.additions_.add(js);
		}
	}

	static final String localhost_key = "ABQIAAAAWqrN5o4-ISwj0Up_depYvhTwM0brOpm-All5BF6PoaKBxRWWERS-S9gPtCri-B6BZeXV8KpT4F80DQ";
}
