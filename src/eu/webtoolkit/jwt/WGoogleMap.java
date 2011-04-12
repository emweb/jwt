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

/**
 * A widget that displays a google map.
 * <p>
 * 
 * This widget uses the online Google Maps server to display a map. It exposes a
 * part of the google maps API.
 * <p>
 * This widget supports both version 2 and version 3 of the Google Maps API. The
 * version 2 API is used by default, to enable the version 3 API, use the
 * constructor&apos;s version argument.
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
	 * ApiVersion.
	 */
	public enum ApiVersion {
		/**
		 * API Version 2.x.
		 */
		Version2,
		/**
		 * API Version 3.x.
		 */
		Version3;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

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
		 * Show the dropdown menu maptype control.
		 */
		MenuControl,
		/**
		 * Show the hierarchical maptype control.
		 */
		HierarchicalControl,
		/**
		 * Show the horizontal bar maptype control.
		 */
		HorizontalBarControl;

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
	 * Creates a map widget with a version and optionally a parent argument.
	 */
	public WGoogleMap(WGoogleMap.ApiVersion version, WContainerWidget parent) {
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
		this.apiVersion_ = version;
		this.setImplementation(new WContainerWidget());
		if (parent != null) {
			parent.addWidget(this);
		}
	}

	/**
	 * Creates a map widget with a version and optionally a parent argument.
	 * <p>
	 * Calls
	 * {@link #WGoogleMap(WGoogleMap.ApiVersion version, WContainerWidget parent)
	 * this(version, (WContainerWidget)null)}
	 */
	public WGoogleMap(WGoogleMap.ApiVersion version) {
		this(version, (WContainerWidget) null);
	}

	/**
	 * Creates a map widget with optionally a parent argument.
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
		this.apiVersion_ = WGoogleMap.ApiVersion.Version2;
		this.setImplementation(new WContainerWidget());
		if (parent != null) {
			parent.addWidget(this);
		}
	}

	/**
	 * Creates a map widget with optionally a parent argument.
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
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			strm
					.append(
							"var marker = new google.maps.Marker(new google.maps.LatLng(")
					.append(String.valueOf(pos.getLatitude())).append(", ")
					.append(String.valueOf(pos.getLongitude())).append("));")
					.append(this.getJsRef()).append(".map.addOverlay(marker);");
		} else {
			strm.append("var position = new google.maps.LatLng(").append(
					String.valueOf(pos.getLatitude())).append(", ").append(
					String.valueOf(pos.getLongitude())).append(");").append(
					"var marker = new google.maps.Marker({").append(
					"position: position,").append("map: ").append(
					this.getJsRef()).append(".map").append("});").append(
					this.getJsRef()).append(".map.overlays.push(marker);");
		}
		this.doGmJavaScript(strm.toString(), false);
	}

	/**
	 * Adds a polyline overlay to the map.
	 * <p>
	 * Specify a value between 0.0 and 1.0 for the opacity or set the alpha
	 * value in the color.
	 */
	public void addPolyline(List<WGoogleMap.Coordinate> points, WColor color,
			int width, double opacity) {
		if (opacity == 1.0) {
			opacity = color.getAlpha() / 255.0;
		}
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
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			strm.append("var poly = new google.maps.Polyline(waypoints, \"")
					.append(color.getCssText()).append("\", ").append(
							String.valueOf(width)).append(", ").append(
							String.valueOf(opacity)).append(");").append(
							this.getJsRef()).append(".map.addOverlay(poly);");
		} else {
			strm
					.append(
							"var poly = new google.maps.Polyline({path: waypoints,strokeColor: \"")
					.append(color.getCssText()).append("\",").append(
							"strokeOpacity: ").append(String.valueOf(opacity))
					.append(",").append("strokeWeight: ").append(
							String.valueOf(width)).append("});").append(
							"poly.setMap(").append(this.getJsRef()).append(
							".map);").append(this.getJsRef()).append(
							".map.overlays.push(poly);");
		}
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
	 * Adds a circle to the map.
	 * <p>
	 * The stroke and fill opacity can be configured respectively in the
	 * strokeColor and fillColor. This feature is only supported by the Google
	 * Maps API version 3.
	 */
	public void addCircle(WGoogleMap.Coordinate center, double radius,
			WColor strokeColor, int strokeWidth, WColor fillColor) {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			throw new UnsupportedOperationException(
					"WGoogleMap::addCircle is not supported in the Google Maps API v2.");
		} else {
			StringWriter strm = new StringWriter();
			double strokeOpacity = strokeColor.getAlpha() / 255.0;
			double fillOpacity = fillColor.getAlpha() / 255.0;
			strm
					.append("var mapLocal = ")
					.append(this.getJsRef() + ".map;")
					.append("var latLng  = new google.maps.LatLng(")
					.append(String.valueOf(center.getLatitude()))
					.append(",")
					.append(String.valueOf(center.getLongitude()))
					.append(");")
					.append(
							"var circle = new google.maps.Circle( {   map: mapLocal,   radius: ")
					.append(String.valueOf(radius)).append(
							",   center:  latLng  ,  fillOpacity: \"").append(
							String.valueOf(fillOpacity)).append(
							"\",  fillColor: \"")
					.append(fillColor.getCssText()).append(
							"\",  strokeWeight: ").append(
							String.valueOf(strokeWidth)).append(
							",  strokeColor:\"").append(
							strokeColor.getCssText()).append(
							"\",  strokeOpacity: ").append(
							String.valueOf(strokeOpacity)).append("} );");
			this.doGmJavaScript(strm.toString(), false);
		}
	}

	/**
	 * Adds a circle to the map.
	 * <p>
	 * Calls
	 * {@link #addCircle(WGoogleMap.Coordinate center, double radius, WColor strokeColor, int strokeWidth, WColor fillColor)
	 * addCircle(center, radius, strokeColor, strokeWidth, new WColor())}
	 */
	public final void addCircle(WGoogleMap.Coordinate center, double radius,
			WColor strokeColor, int strokeWidth) {
		addCircle(center, radius, strokeColor, strokeWidth, new WColor());
	}

	/**
	 * Removes all overlays from the map.
	 */
	public void clearOverlays() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this.doGmJavaScript(this.getJsRef() + ".map.clearOverlays();",
					false);
		} else {
			StringWriter strm = new StringWriter();
			strm.append("var mapLocal = ").append(this.getJsRef() + ".map;\n")
					.append("if (mapLocal.overlays) {\n").append(
							"  for (i in mapLocal.overlays) {\n").append(
							"    mapLocal.overlays[i].setMap(null);\n").append(
							"  }\n")
					.append("  mapLocal.overlays.length = 0;\n").append("}\n")
					.append("if (mapLocal.infowindows) {\n").append(
							"  for (i in mapLocal.infowindows) {\n").append(
							"    mapLocal.infowindows[i].close();\n").append(
							"  }\n").append(
							"  mapLocal.infowindows.length = 0;\n").append(
							"}\n");
			this.doGmJavaScript(strm.toString(), false);
		}
	}

	/**
	 * Opens a text bubble with html text at a specific location.
	 */
	public void openInfoWindow(WGoogleMap.Coordinate pos, CharSequence myHtml) {
		StringWriter strm = new StringWriter();
		strm.append("var pos = new google.maps.LatLng(").append(
				String.valueOf(pos.getLatitude())).append(", ").append(
				String.valueOf(pos.getLongitude())).append(");");
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			strm.append(this.getJsRef()).append(".map.openInfoWindow(pos, ")
					.append(WWebWidget.jsStringLiteral(myHtml)).append(");");
		} else {
			strm.append(
					"var infowindow = new google.maps.InfoWindow({content: ")
					.append(WWebWidget.jsStringLiteral(myHtml)).append(",")
					.append("position: pos});infowindow.open(").append(
							this.getJsRef()).append(".map);").append(
							this.getJsRef()).append(
							".map.infowindows.push(infowindow);");
		}
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
				String.valueOf(center.getLongitude())).append(")); ").append(
				this.getJsRef()).append(".map.setZoom(").append(
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
						"));");
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			strm.append("var zooml = ").append(this.getJsRef()).append(
					".map.getBoundsZoomLevel(bbox);").append(this.getJsRef())
					.append(".map.setCenter(new google.maps.LatLng(").append(
							String.valueOf(center.getLatitude())).append(", ")
					.append(String.valueOf(center.getLongitude())).append(
							"), zooml);");
		} else {
			strm.append(this.getJsRef()).append(".map.fitBounds(bbox);");
		}
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
		StringWriter strm = new StringWriter();
		strm.append("var zoom = ").append(this.getJsRef()).append(
				".map.getZoom();").append(this.getJsRef()).append(
				".map.setZoom(zoom + 1);");
		this.doGmJavaScript(strm.toString(), false);
	}

	/**
	 * Decrements zoom level by one.
	 */
	public void zoomOut() {
		StringWriter strm = new StringWriter();
		strm.append("var zoom = ").append(this.getJsRef()).append(
				".map.getZoom();").append(this.getJsRef()).append(
				".map.setZoom(zoom - 1);");
		this.doGmJavaScript(strm.toString(), false);
	}

	/**
	 * Stores the current map position and zoom level.
	 * <p>
	 * You can later restore this position using
	 * {@link WGoogleMap#returnToSavedPosition() returnToSavedPosition()}.
	 */
	public void savePosition() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this
					.doGmJavaScript(this.getJsRef() + ".map.savePosition();",
							false);
		} else {
			StringWriter strm = new StringWriter();
			strm.append(this.getJsRef()).append(".map.savedZoom = ").append(
					this.getJsRef()).append(".map.getZoom();").append(
					this.getJsRef()).append(".map.savedPosition = ").append(
					this.getJsRef()).append(".map.getCenter();");
			this.doGmJavaScript(strm.toString(), false);
		}
	}

	/**
	 * Restores the map view that was saved by {@link WGoogleMap#savePosition()
	 * savePosition()}.
	 */
	public void returnToSavedPosition() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this.doGmJavaScript(this.getJsRef()
					+ ".map.returnToSavedPosition();", false);
		} else {
			StringWriter strm = new StringWriter();
			strm.append(this.getJsRef()).append(".map.setZoom(").append(
					this.getJsRef()).append(".map.savedZoom);").append(
					this.getJsRef()).append(".map.setCenter(").append(
					this.getJsRef()).append(".map.savedPosition);");
			this.doGmJavaScript(strm.toString(), false);
		}
	}

	/**
	 * Notifies the map of a change of the size of its container.
	 * <p>
	 * Call this method after the size of the container DOM object has changed,
	 * so that the map can adjust itself to fit the new size.
	 * <p>
	 * 
	 * @deprecated the map is resized automatically when necessary
	 */
	public void checkResize() {
		this.doGmJavaScript(this.getJsRef() + ".map.checkResize();", false);
	}

	/**
	 * Enables the dragging of the map (enabled by default).
	 */
	public void enableDragging() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this.doGmJavaScript(this.getJsRef() + ".map.enableDragging();",
					false);
		} else {
			this.setMapOption("draggable", "true");
		}
	}

	/**
	 * Disables the dragging of the map.
	 */
	public void disableDragging() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this.doGmJavaScript(this.getJsRef() + ".map.disableDragging();",
					false);
		} else {
			this.setMapOption("draggable", "false");
		}
	}

	/**
	 * Enables double click to zoom in and out (enabled by default).
	 */
	public void enableDoubleClickZoom() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this.doGmJavaScript(this.getJsRef()
					+ ".map.enableDoubleClickZoom();", false);
		} else {
			this.setMapOption("disableDoubleClickZoom", "false");
		}
	}

	/**
	 * Disables double click to zoom in and out.
	 */
	public void disableDoubleClickZoom() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this.doGmJavaScript(this.getJsRef()
					+ ".map.disableDoubleClickZoom();", false);
		} else {
			this.setMapOption("disableDoubleClickZoom", "true");
		}
	}

	/**
	 * Enables the GoogleBar, an integrated search control, on the map.
	 * <p>
	 * When enabled, this control takes the place of the default Powered By
	 * Google logo.
	 * <p>
	 * This control is initially disabled.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>This functionality is no longer available in the Google
	 * Maps API v3. </i>
	 * </p>
	 */
	public void enableGoogleBar() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this.doGmJavaScript(this.getJsRef() + ".map.enableGoogleBar();",
					false);
		} else {
			throw new UnsupportedOperationException(
					"WGoogleMap::enableGoogleBar is not supported in the Google Maps API v3.");
		}
	}

	/**
	 * Disables the GoogleBar integrated search control.
	 * <p>
	 * When disabled, the default Powered by Google logo occupies the position
	 * formerly containing this control. Note that this control is already
	 * disabled by default.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>This functionality is no longer available in the Google
	 * Maps API v3. </i>
	 * </p>
	 */
	public void disableGoogleBar() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this.doGmJavaScript(this.getJsRef() + ".map.disableGoogleBar();",
					false);
		} else {
			throw new UnsupportedOperationException(
					"WGoogleMap::disableGoogleBar is not supported in the Google Maps API v3.");
		}
	}

	/**
	 * Enables zooming using a mouse&apos;s scroll wheel.
	 * <p>
	 * Scroll wheel zoom is disabled by default.
	 */
	public void enableScrollWheelZoom() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this.doGmJavaScript(this.getJsRef()
					+ ".map.enableScrollWheelZoom();", false);
		} else {
			this.setMapOption("scrollwheel", "true");
		}
	}

	/**
	 * Disables zooming using a mouse&apos;s scroll wheel.
	 * <p>
	 * Scroll wheel zoom is disabled by default.
	 */
	public void disableScrollWheelZoom() {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			this.doGmJavaScript(this.getJsRef()
					+ ".map.disableScrollWheelZoom();", false);
		} else {
			this.setMapOption("scrollwheel", "false");
		}
	}

	/**
	 * Sets the map type control.
	 * <p>
	 * The control allows selecting and switching between supported map types
	 * via buttons.
	 */
	public void setMapTypeControl(WGoogleMap.MapTypeControl type) {
		StringWriter strm = new StringWriter();
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
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
			case HorizontalBarControl:
				throw new UnsupportedOperationException(
						"WGoogleMap::setMapTypeControl: HorizontalBarControl is not supported when using Google Maps API v2.");
			default:
				control = "";
			}
			strm.append(this.getJsRef()).append(".map.removeControl(").append(
					this.getJsRef()).append(".mtc);");
			if (!control.equals("")) {
				strm.append("var mtc = new ").append(control).append("();")
						.append(this.getJsRef()).append(".mtc = mtc;").append(
								this.getJsRef())
						.append(".map.addControl(mtc);");
			}
		} else {
			String control = "";
			switch (type) {
			case DefaultControl:
				control = "DEFAULT";
				break;
			case MenuControl:
				control = "DROPDOWN_MENU";
				break;
			case HorizontalBarControl:
				control = "HORIZONTAL_BAR";
				break;
			case HierarchicalControl:
				throw new UnsupportedOperationException(
						"WGoogleMap::setMapTypeControl: HierarchicalControl is not supported when using Google Maps API v3.");
			default:
				control = "";
			}
			strm.append("var options = {").append("disableDefaultUI: ").append(
					control.equals("") ? "true" : "false").append(",").append(
					"  mapTypeControlOptions: {");
			if (!control.equals("")) {
				strm.append("style: google.maps.MapTypeControlStyle.").append(
						control);
			}
			strm.append("  }").append("};").append(this.getJsRef()).append(
					".map.setOptions(options);");
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

	/**
	 * Return the used Google Maps API version.
	 */
	public WGoogleMap.ApiVersion getApiVersion() {
		return this.apiVersion_;
	}

	private JSignal1<WGoogleMap.Coordinate> clicked_;
	private JSignal1<WGoogleMap.Coordinate> doubleClicked_;
	private JSignal1<WGoogleMap.Coordinate> mouseMoved_;

	void render(EnumSet<RenderFlag> flags) {
		try {
			if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
				WApplication app = WApplication.getInstance();
				if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
					String googlekey = localhost_key;
					googlekey = WApplication.readConfigurationProperty(
							"google_api_key", googlekey);
					final String gmuri = "http://www.google.com/jsapi?key="
							+ googlekey;
					app.require(gmuri, "google");
				}
				String initFunction = app.getJavaScriptClass()
						+ ".init_google_maps_" + this.getId();
				StringWriter strm = new StringWriter();
				strm.append("{ ").append(initFunction).append(
						" = function() {var self = ").append(this.getJsRef())
						.append(";if (!self) { setTimeout(").append(
								initFunction).append(", 0);return;}");
				if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
					strm
							.append("var map = new google.maps.Map(self);map.setCenter(new google.maps.LatLng(47.01887777, 8.651888), 13);");
					this
							.setJavaScriptMember(
									"wtResize",
									"function(self, w, h) {self.style.width=w + 'px';self.style.height=h + 'px';if (self.map)  self.map.checkResize();}");
				} else {
					strm
							.append("var latlng = new google.maps.LatLng(47.01887777, 8.651888);var myOptions = {zoom: 13,center: latlng,mapTypeId: google.maps.MapTypeId.ROADMAP};var map = new google.maps.Map(self, myOptions);map.overlays = [];map.infowindows = [];");
					this
							.setJavaScriptMember(
									"wtResize",
									"function(self, w, h) {self.style.width=w + 'px';self.style.height=h + 'px';if (self.map) google.maps.event.trigger(self.map, 'resize');}");
				}
				strm.append("self.map = map;");
				this.streamJSListener(this.clicked_, "click", strm);
				this.streamJSListener(this.doubleClicked_, "dblclick", strm);
				this.streamJSListener(this.mouseMoved_, "mousemove", strm);
				for (int i = 0; i < this.additions_.size(); i++) {
					strm.append(this.additions_.get(i));
				}
				strm.append("setTimeout(function(){ delete ").append(
						initFunction).append(";}, 0)").append("};");
				if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
					strm
							.append(
									"google.load(\"maps\", \"2\", {other_params:\"sensor=false\", callback: ")
							.append(
									app.getJavaScriptClass()
											+ ".init_google_maps_"
											+ this.getId()).append("});");
				}
				strm.append("}");
				this.additions_.clear();
				app.doJavaScript(strm.toString(),
						this.apiVersion_ == WGoogleMap.ApiVersion.Version2);
				if (this.apiVersion_ == WGoogleMap.ApiVersion.Version3) {
					String uri = "";
					if (app.getEnvironment().hasAjax()) {
						uri = "http://maps.google.com/maps/api/js?sensor=false&callback=";
						uri += app.getJavaScriptClass() + ".init_google_maps_"
								+ this.getId();
					} else {
						uri = "http://maps.google.com/maps/api/js?sensor=false";
					}
					app.require(uri);
				}
			}
			super.render(flags);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
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

	private void streamJSListener(JSignal1<WGoogleMap.Coordinate> signal,
			String signalName, Writer strm) throws IOException {
		if (this.apiVersion_ == WGoogleMap.ApiVersion.Version2) {
			strm.append("google.maps.Event.addListener(map, \"").append(
					signalName).append(
					"\", function(overlay, latlng) {if (latlng) {").append(
					signal.createCall("latlng.lat() +' '+ latlng.lng()"))
					.append(";}});");
		} else {
			strm
					.append("google.maps.event.addListener(map, \"")
					.append(signalName)
					.append("\", function(event) {if (event && event.latLng) {")
					.append(
							signal
									.createCall("event.latLng.lat() +' '+ event.latLng.lng()"))
					.append(";}});");
		}
	}

	private void setMapOption(String option, String value) {
		StringWriter strm = new StringWriter();
		strm.append("var option = {").append(option).append(" :").append(value)
				.append("};").append(this.getJsRef()).append(
						".map.setOptions(option);");
		this.doGmJavaScript(strm.toString(), false);
	}

	private WGoogleMap.ApiVersion apiVersion_;
	static final String localhost_key = "ABQIAAAAWqrN5o4-ISwj0Up_depYvhTwM0brOpm-All5BF6PoaKBxRWWERS-S9gPtCri-B6BZeXV8KpT4F80DQ";
}
