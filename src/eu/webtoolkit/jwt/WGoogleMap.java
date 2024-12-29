/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget that displays a google map.
 *
 * <p>This widget uses the online Google Maps server to display a map. It exposes a part of the
 * google maps API.
 *
 * <p>This widget supports both version 2 and version 3 of the Google Maps API. The version 2 API is
 * used by default, to enable the version 3 API, use the constructor&apos;s version argument.
 *
 * <p>To use the map on a public server you will need to obtain a key. The widget will look for this
 * key as the configuration property <code>&quot;google_api_key&quot;</code>. If this configuration
 * property has not been set, it will use a key that is suitable for localhost.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>Styling through CSS is not applicable.
 *
 * <p>Contributed by: Richard Ulrich.
 */
public class WGoogleMap extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WGoogleMap.class);

  /** A geographical coordinate (latitude/longitude) */
  public static class Coordinate {
    private static Logger logger = LoggerFactory.getLogger(Coordinate.class);

    /** Default constructor. */
    public Coordinate() {
      this.lat_ = 0;
      this.lon_ = 0;
    }
    /** Creates with given latitude and longitude. */
    public Coordinate(double lat, double lon) {
      this.setLatitude(lat);
      this.setLongitude(lon);
    }
    /** Sets the latitude. */
    public void setLatitude(double latitude) {
      if (latitude < -90.0 || latitude > 90.0) {
        throw new RuntimeException("invalid latitude: " + String.valueOf(latitude));
      }
      this.lat_ = latitude;
    }
    /** Sets the longitude. */
    public void setLongitude(double longitude) {
      if (longitude < -180.0 || longitude > 180.0) {
        throw new RuntimeException("invalid longitude: " + String.valueOf(longitude));
      }
      this.lon_ = longitude;
    }
    /** Returns the latitude. */
    public double getLatitude() {
      return this.lat_;
    }
    /** Returns the longitude. */
    public double getLongitude() {
      return this.lon_;
    }
    /**
     * Calculates the distance between two points in km (approximate).
     *
     * <p>The calculation uses a sphere. Results can be out by 0.3%.
     */
    public double distanceTo(final WGoogleMap.Coordinate rhs) {
      final double lat1 = this.lat_ * 3.14159265358979323846 / 180.0;
      final double lat2 = rhs.getLatitude() * 3.14159265358979323846 / 180.0;
      final double deltaLong = (rhs.getLongitude() - this.lon_) * 3.14159265358979323846 / 180.0;
      final double angle =
          Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLong);
      final double earthRadius = 6371.0;
      final double dist = earthRadius * Math.acos(angle);
      return dist;
    }

    private double lat_;
    private double lon_;
  }
  /** Creates a map widget with a version. */
  public WGoogleMap(GoogleMapsVersion version, WContainerWidget parentContainer) {
    super();
    this.clicked_ = new JSignal1<WGoogleMap.Coordinate>(this, "click") {};
    this.doubleClicked_ = new JSignal1<WGoogleMap.Coordinate>(this, "dblclick") {};
    this.mouseMoved_ = null;
    this.googlekey_ = "";
    this.additions_ = new ArrayList<String>();
    this.apiVersion_ = version;
    this.setImplementation(new WContainerWidget());
    WApplication app = WApplication.getInstance();
    this.googlekey_ = localhost_key;
    this.googlekey_ = WApplication.readConfigurationProperty("google_api_key", this.googlekey_);
    final String gmuri = "//www.google.com/jsapi?key=" + this.googlekey_;
    app.require(gmuri, "google");
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a map widget with a version.
   *
   * <p>Calls {@link #WGoogleMap(GoogleMapsVersion version, WContainerWidget parentContainer)
   * this(GoogleMapsVersion.v3, (WContainerWidget)null)}
   */
  public WGoogleMap() {
    this(GoogleMapsVersion.v3, (WContainerWidget) null);
  }
  /**
   * Creates a map widget with a version.
   *
   * <p>Calls {@link #WGoogleMap(GoogleMapsVersion version, WContainerWidget parentContainer)
   * this(version, (WContainerWidget)null)}
   */
  public WGoogleMap(GoogleMapsVersion version) {
    this(version, (WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {

    super.remove();
  }
  /** Adds a marker overlay to the map. */
  public void addMarker(final WGoogleMap.Coordinate pos) {
    StringWriter strm = new StringWriter();
    strm.append("var position = ");
    write(strm, pos);
    strm.append(";")
        .append("var marker = new google.maps.Marker({")
        .append("position: position,")
        .append("map: ")
        .append(this.getJsRef())
        .append(".map")
        .append("});")
        .append(this.getJsRef())
        .append(".map.overlays.push(marker);");
    this.doGmJavaScript(strm.toString());
  }
  /**
   * Adds a polyline overlay to the map.
   *
   * <p>Specify a value between 0.0 and 1.0 for the opacity or set the alpha value in the color.
   */
  public void addPolyline(
      final List<WGoogleMap.Coordinate> points, final WColor color, int width, double opacity) {
    if (opacity == 1.0) {
      opacity = color.getAlpha() / 255.0;
    }
    opacity = Math.max(Math.min(opacity, 1.0), 0.0);
    StringWriter strm = new StringWriter();
    strm.append("var waypoints = [];");
    for (int i = 0; i < points.size(); ++i) {
      strm.append("waypoints[").append(String.valueOf(i)).append("] = ");
      write(strm, points.get(i));
      strm.append(";");
    }
    strm.append("var poly = new google.maps.Polyline({path: waypoints,strokeColor: \"")
        .append(color.getCssText(false))
        .append("\",")
        .append("strokeOpacity: ")
        .append(String.valueOf(opacity))
        .append(",")
        .append("strokeWeight: ")
        .append(String.valueOf(width))
        .append("});")
        .append("poly.setMap(")
        .append(this.getJsRef())
        .append(".map);")
        .append(this.getJsRef())
        .append(".map.overlays.push(poly);");
    this.doGmJavaScript(strm.toString());
  }
  /**
   * Adds a polyline overlay to the map.
   *
   * <p>Calls {@link #addPolyline(List points, WColor color, int width, double opacity)
   * addPolyline(points, new WColor(StandardColor.Red), 2, 1.0)}
   */
  public final void addPolyline(final List<WGoogleMap.Coordinate> points) {
    addPolyline(points, new WColor(StandardColor.Red), 2, 1.0);
  }
  /**
   * Adds a polyline overlay to the map.
   *
   * <p>Calls {@link #addPolyline(List points, WColor color, int width, double opacity)
   * addPolyline(points, color, 2, 1.0)}
   */
  public final void addPolyline(final List<WGoogleMap.Coordinate> points, final WColor color) {
    addPolyline(points, color, 2, 1.0);
  }
  /**
   * Adds a polyline overlay to the map.
   *
   * <p>Calls {@link #addPolyline(List points, WColor color, int width, double opacity)
   * addPolyline(points, color, width, 1.0)}
   */
  public final void addPolyline(
      final List<WGoogleMap.Coordinate> points, final WColor color, int width) {
    addPolyline(points, color, width, 1.0);
  }
  /**
   * Adds a circle to the map.
   *
   * <p>The stroke and fill opacity can be configured respectively in the strokeColor and fillColor.
   * This feature is only supported by the Google Maps API version 3.
   */
  public void addCircle(
      final WGoogleMap.Coordinate center,
      double radius,
      final WColor strokeColor,
      int strokeWidth,
      final WColor fillColor) {
    StringWriter strm = new StringWriter();
    double strokeOpacity = strokeColor.getAlpha() / 255.0;
    double fillOpacity = fillColor.getAlpha() / 255.0;
    strm.append("var mapLocal = ").append(this.getJsRef() + ".map;").append("var latLng = ");
    write(strm, center);
    strm.append(";")
        .append("var circle = new google.maps.Circle( {   map: mapLocal,   radius: ")
        .append(String.valueOf(radius))
        .append(",   center:  latLng  ,  fillOpacity: \"")
        .append(String.valueOf(fillOpacity))
        .append("\",  fillColor: \"")
        .append(fillColor.getCssText(false))
        .append("\",  strokeWeight: ")
        .append(String.valueOf(strokeWidth))
        .append(",  strokeColor:\"")
        .append(strokeColor.getCssText(false))
        .append("\",  strokeOpacity: ")
        .append(String.valueOf(strokeOpacity))
        .append("} );")
        .append(this.getJsRef())
        .append(".map.overlays.push(circle);");
    this.doGmJavaScript(strm.toString());
  }
  /**
   * Adds a circle to the map.
   *
   * <p>Calls {@link #addCircle(WGoogleMap.Coordinate center, double radius, WColor strokeColor, int
   * strokeWidth, WColor fillColor) addCircle(center, radius, strokeColor, strokeWidth, new
   * WColor())}
   */
  public final void addCircle(
      final WGoogleMap.Coordinate center,
      double radius,
      final WColor strokeColor,
      int strokeWidth) {
    addCircle(center, radius, strokeColor, strokeWidth, new WColor());
  }
  /** Adds a icon marker overlay to the map. */
  public void addIconMarker(final WGoogleMap.Coordinate pos, final String iconURL) {
    StringWriter strm = new StringWriter();
    strm.append("var position = ");
    write(strm, pos);
    strm.append(";")
        .append("var marker = new google.maps.Marker({")
        .append("position: position,")
        .append("icon: \"")
        .append(iconURL)
        .append("\",")
        .append("map: ")
        .append(this.getJsRef())
        .append(".map")
        .append("});")
        .append(this.getJsRef())
        .append(".map.overlays.push(marker);");
    this.doGmJavaScript(strm.toString());
  }
  /** Removes all overlays from the map. */
  public void clearOverlays() {
    StringWriter strm = new StringWriter();
    strm.append("var mapLocal = ")
        .append(this.getJsRef() + ".map, i;\n")
        .append("if (mapLocal.overlays) {\n")
        .append("for (i in mapLocal.overlays) {\n")
        .append("mapLocal.overlays[i].setMap(null);\n")
        .append("}\n")
        .append("mapLocal.overlays.length = 0;\n")
        .append("}\n")
        .append("if (mapLocal.infowindows) {\n")
        .append("for (i in mapLocal.infowindows) {\n")
        .append("mapLocal.infowindows[i].close();\n")
        .append("}\n")
        .append("mapLocal.infowindows.length = 0;\n")
        .append("}\n");
    this.doGmJavaScript(strm.toString());
  }
  /** Opens a text bubble with html text at a specific location. */
  public void openInfoWindow(final WGoogleMap.Coordinate pos, final CharSequence myHtml) {
    StringWriter strm = new StringWriter();
    strm.append("var pos = ");
    write(strm, pos);
    strm.append(";");
    strm.append("var infowindow = new google.maps.InfoWindow({content: ")
        .append(WWebWidget.jsStringLiteral(myHtml))
        .append(",")
        .append("position: pos});infowindow.open(")
        .append(this.getJsRef())
        .append(".map);")
        .append(this.getJsRef())
        .append(".map.infowindows.push(infowindow);");
    this.doGmJavaScript(strm.toString());
  }
  /** Sets the map view to the given center. */
  public void setCenter(final WGoogleMap.Coordinate center) {
    StringWriter strm = new StringWriter();
    strm.append(this.getJsRef()).append(".map.setCenter(");
    write(strm, center);
    strm.append(");");
    this.doGmJavaScript(strm.toString());
  }
  /** Sets the map view to the given center and zoom level. */
  public void setCenter(final WGoogleMap.Coordinate center, int zoom) {
    StringWriter strm = new StringWriter();
    strm.append(this.getJsRef()).append(".map.setCenter(");
    write(strm, center);
    strm.append("); ")
        .append(this.getJsRef())
        .append(".map.setZoom(")
        .append(String.valueOf(zoom))
        .append(");");
    this.doGmJavaScript(strm.toString());
  }
  /**
   * Changes the center point of the map to the given point.
   *
   * <p>If the point is already visible in the current map view, change the center in a smooth
   * animation.
   */
  public void panTo(final WGoogleMap.Coordinate center) {
    StringWriter strm = new StringWriter();
    strm.append(this.getJsRef()).append(".map.panTo(");
    write(strm, center);
    strm.append(");");
    this.doGmJavaScript(strm.toString());
  }
  /** Zooms the map to a region defined by a bounding box. */
  public void zoomWindow(
      final WGoogleMap.Coordinate topLeft, final WGoogleMap.Coordinate rightBottom) {
    final WGoogleMap.Coordinate center =
        new WGoogleMap.Coordinate(
            (topLeft.getLatitude() + rightBottom.getLatitude()) / 2.0,
            (topLeft.getLongitude() + rightBottom.getLongitude()) / 2.0);
    WGoogleMap.Coordinate topLeftC =
        new WGoogleMap.Coordinate(
            Math.min(topLeft.getLatitude(), rightBottom.getLatitude()),
            Math.min(topLeft.getLongitude(), rightBottom.getLongitude()));
    WGoogleMap.Coordinate rightBottomC =
        new WGoogleMap.Coordinate(
            Math.max(topLeft.getLatitude(), rightBottom.getLatitude()),
            Math.max(topLeft.getLongitude(), rightBottom.getLongitude()));
    StringWriter strm = new StringWriter();
    strm.append("var bbox = new google.maps.LatLngBounds(");
    write(strm, topLeftC);
    strm.append(", ");
    write(strm, rightBottomC);
    strm.append(");");
    strm.append(this.getJsRef()).append(".map.fitBounds(bbox);");
    this.doGmJavaScript(strm.toString());
  }
  /** Sets the zoom level to the given new value. */
  public void setZoom(int level) {
    this.doGmJavaScript(this.getJsRef() + ".map.setZoom(" + String.valueOf(level) + ");");
  }
  /** Increments zoom level by one. */
  public void zoomIn() {
    StringWriter strm = new StringWriter();
    strm.append("var zoom = ")
        .append(this.getJsRef())
        .append(".map.getZoom();")
        .append(this.getJsRef())
        .append(".map.setZoom(zoom + 1);");
    this.doGmJavaScript(strm.toString());
  }
  /** Decrements zoom level by one. */
  public void zoomOut() {
    StringWriter strm = new StringWriter();
    strm.append("var zoom = ")
        .append(this.getJsRef())
        .append(".map.getZoom();")
        .append(this.getJsRef())
        .append(".map.setZoom(zoom - 1);");
    this.doGmJavaScript(strm.toString());
  }
  /**
   * Stores the current map position and zoom level.
   *
   * <p>You can later restore this position using {@link WGoogleMap#returnToSavedPosition()
   * returnToSavedPosition()}.
   */
  public void savePosition() {
    StringWriter strm = new StringWriter();
    strm.append(this.getJsRef())
        .append(".map.savedZoom = ")
        .append(this.getJsRef())
        .append(".map.getZoom();")
        .append(this.getJsRef())
        .append(".map.savedPosition = ")
        .append(this.getJsRef())
        .append(".map.getCenter();");
    this.doGmJavaScript(strm.toString());
  }
  /** Restores the map view that was saved by {@link WGoogleMap#savePosition() savePosition()}. */
  public void returnToSavedPosition() {
    StringWriter strm = new StringWriter();
    strm.append(this.getJsRef())
        .append(".map.setZoom(")
        .append(this.getJsRef())
        .append(".map.savedZoom);")
        .append(this.getJsRef())
        .append(".map.setCenter(")
        .append(this.getJsRef())
        .append(".map.savedPosition);");
    this.doGmJavaScript(strm.toString());
  }
  /** Enables the dragging of the map (enabled by default). */
  public void enableDragging() {
    this.setMapOption("draggable", "true");
  }
  /** Disables the dragging of the map. */
  public void disableDragging() {
    this.setMapOption("draggable", "false");
  }
  /** Enables double click to zoom in and out (enabled by default). */
  public void enableDoubleClickZoom() {
    this.setMapOption("disableDoubleClickZoom", "false");
  }
  /** Disables double click to zoom in and out. */
  public void disableDoubleClickZoom() {
    this.setMapOption("disableDoubleClickZoom", "true");
  }
  /**
   * Enables the GoogleBar, an integrated search control, on the map.
   *
   * <p>When enabled, this control takes the place of the default Powered By Google logo.
   *
   * <p>This control is initially disabled.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This functionality is no longer available in the Google Maps API v3. </i>
   */
  public void enableGoogleBar() {
    throw new UnsupportedOperationException(
        "WGoogleMap::enableGoogleBar is not supported in the Google Maps API v3.");
  }
  /**
   * Disables the GoogleBar integrated search control.
   *
   * <p>When disabled, the default Powered by Google logo occupies the position formerly containing
   * this control. Note that this control is already disabled by default.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This functionality is no longer available in the Google Maps API v3. </i>
   */
  public void disableGoogleBar() {
    throw new UnsupportedOperationException(
        "WGoogleMap::disableGoogleBar is not supported in the Google Maps API v3.");
  }
  /**
   * Enables zooming using a mouse&apos;s scroll wheel.
   *
   * <p>Scroll wheel zoom is disabled by default.
   */
  public void enableScrollWheelZoom() {
    this.setMapOption("scrollwheel", "true");
  }
  /**
   * Disables zooming using a mouse&apos;s scroll wheel.
   *
   * <p>Scroll wheel zoom is disabled by default.
   */
  public void disableScrollWheelZoom() {
    this.setMapOption("scrollwheel", "false");
  }
  /**
   * Sets the map type control.
   *
   * <p>The control allows selecting and switching between supported map types via buttons.
   */
  public void setMapTypeControl(MapTypeControl type) {
    StringWriter strm = new StringWriter();
    String control = "";
    switch (type) {
      case Default:
        control = "DEFAULT";
        break;
      case Menu:
        control = "DROPDOWN_MENU";
        break;
      case HorizontalBar:
        control = "HORIZONTAL_BAR";
        break;
      case Hierarchical:
        throw new UnsupportedOperationException(
            "WGoogleMap::setMapTypeControl: HierarchicalControl is not supported when using Google Maps API v3.");
      default:
        control = "";
    }
    strm.append("var options = {")
        .append("disableDefaultUI: ")
        .append(control.equals("") ? "true" : "false")
        .append(",")
        .append("mapTypeControlOptions: {");
    if (!control.equals("")) {
      strm.append("style: google.maps.MapTypeControlStyle.").append(control);
    }
    strm.append("}").append("};").append(this.getJsRef()).append(".map.setOptions(options);");
    this.doGmJavaScript(strm.toString());
  }
  /**
   * The click event.
   *
   * <p>This event is fired when the user clicks on the map with the mouse.
   */
  public JSignal1<WGoogleMap.Coordinate> clicked() {
    return this.clicked_;
  }
  /**
   * The double click event.
   *
   * <p>This event is fired when a double click is done on the map.
   */
  public JSignal1<WGoogleMap.Coordinate> doubleClicked() {
    return this.doubleClicked_;
  }
  /** This event is fired when the user moves the mouse inside the map. */
  public JSignal1<WGoogleMap.Coordinate> mouseMoved() {
    if (!(this.mouseMoved_ != null)) {
      this.mouseMoved_ = new JSignal1<WGoogleMap.Coordinate>(this, "mousemove") {};
    }
    return this.mouseMoved_;
  }
  /** Return the used Google Maps API version. */
  public GoogleMapsVersion getApiVersion() {
    return this.apiVersion_;
  }

  private JSignal1<WGoogleMap.Coordinate> clicked_;
  private JSignal1<WGoogleMap.Coordinate> doubleClicked_;
  private JSignal1<WGoogleMap.Coordinate> mouseMoved_;
  private String googlekey_;

  protected void render(EnumSet<RenderFlag> flags) {
    if (flags.contains(RenderFlag.Full)) {
      WApplication app = WApplication.getInstance();
      String initFunction = app.getJavaScriptClass() + ".init_google_maps_" + this.getId();
      StringBuilder strm = new StringBuilder();
      strm.append("{ ")
          .append(initFunction)
          .append(" = function() {var self = ")
          .append(this.getJsRef())
          .append(";if (!self) { setTimeout(")
          .append(initFunction)
          .append(", 0);return;}");
      strm.append(
          "var latlng = new google.maps.LatLng(47.01887777, 8.651888);var myOptions = {zoom: 13,center: latlng,mapTypeId: google.maps.MapTypeId.ROADMAP};var map = new google.maps.Map(self, myOptions);map.overlays = [];map.infowindows = [];");
      this.setJavaScriptMember(
          WT_RESIZE_JS,
          "function(self, w, h) {if (w >= 0) self.style.width=w + 'px';if (h >= 0) self.style.height=h + 'px';if (self.map) google.maps.event.trigger(self.map, 'resize');}");
      strm.append("self.map = map;");
      this.streamJSListener(this.clicked_, "click", strm);
      this.streamJSListener(this.doubleClicked_, "dblclick", strm);
      if (this.mouseMoved_ != null) {
        this.streamJSListener(this.mouseMoved_, "mousemove", strm);
      }
      for (int i = 0; i < this.additions_.size(); i++) {
        strm.append(this.additions_.get(i));
      }
      strm.append("setTimeout(function(){ delete ")
          .append(initFunction)
          .append(";}, 0)};")
          .append(app.getJavaScriptClass())
          .append("._p_.loadGoogleMaps('")
          .append('3')
          .append("',")
          .append(WWebWidget.jsStringLiteral(this.googlekey_))
          .append(",")
          .append(initFunction)
          .append(");")
          .append("}");
      this.additions_.clear();
      app.doJavaScript(strm.toString(), true);
    }
    super.render(flags);
  }
  /**
   * Execute a piece of JavaScript that manipulates the map.
   *
   * <p>This is like {@link WCompositeWidget#doJavaScript(String js)
   * WCompositeWidget#doJavaScript()} but delays the javascript until the map has been loaded.
   */
  protected void doGmJavaScript(final String jscode) {
    if (this.isRendered()) {
      this.doJavaScript(jscode);
    } else {
      this.additions_.add(jscode);
    }
  }

  private List<String> additions_;

  private void streamJSListener(
      final JSignal1<WGoogleMap.Coordinate> signal, String signalName, final StringBuilder strm) {
    strm.append("google.maps.event.addListener(map, \"")
        .append(signalName)
        .append("\", function(event) {if (event && event.latLng) {")
        .append(signal.createCall("event.latLng.lat() +' '+ event.latLng.lng()"))
        .append(";}});");
  }

  private void setMapOption(final String option, final String value) {
    StringWriter strm = new StringWriter();
    strm.append("var option = {")
        .append(option)
        .append(" :")
        .append(value)
        .append("};")
        .append(this.getJsRef())
        .append(".map.setOptions(option);");
    this.doGmJavaScript(strm.toString());
  }

  private GoogleMapsVersion apiVersion_;
  private static final String localhost_key =
      "ABQIAAAAWqrN5o4-ISwj0Up_depYvhTwM0brOpm-All5BF6PoaKBxRWWERS-S9gPtCri-B6BZeXV8KpT4F80DQ";

  static void write(final StringWriter os, final WGoogleMap.Coordinate c) {
    char[] b1 = new char[35];
    char[] b2 = new char[35];
    os.append("new google.maps.LatLng(")
        .append(MathUtils.roundJs(c.getLatitude(), 15))
        .append(",")
        .append(MathUtils.roundJs(c.getLongitude(), 15))
        .append(")");
  }
}
