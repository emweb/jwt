/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
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

class LeafletMapExample extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(LeafletMapExample.class);

  public LeafletMapExample(WContainerWidget parentContainer) {
    super();
    WHBoxLayout layout = new WHBoxLayout();
    this.setLayout(layout);
    this.setHeight(new WLength(400));
    this.map_ = new WLeafletMap();
    layout.addWidget(this.map_, 1);
    com.google.gson.JsonObject options = new com.google.gson.JsonObject();
    options.add("maxZoom", (new com.google.gson.JsonPrimitive(19)));
    options.add(
        "attribution",
        (new com.google.gson.JsonPrimitive(
            "&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors")));
    this.map_.addTileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", options);
    this.map_.panTo(EMWEB_COORDS);
    this.addEmwebLogoMarker();
    this.addGroteMarktPopup();
    WPen pen = new WPen(new WColor(0, 191, 255));
    pen.setCapStyle(PenCapStyle.Round);
    pen.setJoinStyle(PenJoinStyle.Round);
    pen.setWidth(new WLength(3.0));
    this.map_.addPolyline(this.getRoadDescription(), pen);
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public LeafletMapExample() {
    this((WContainerWidget) null);
  }

  private WLeafletMap map_;

  private void addEmwebLogoMarker() {
    WImage emwebLogo = new WImage(new WLink("https://www.emweb.be/css/emweb_small_filled.png"));
    emwebLogo.setInline(false);
    emwebLogo.resize(new WLength(118), new WLength(32));
    WLeafletMap.WidgetMarker emwebMarker = new WLeafletMap.WidgetMarker(EMWEB_COORDS, emwebLogo);
    emwebMarker.addPopup(new WLeafletMap.Popup("This is where Wt is developed!"));
    this.map_.addMarker(emwebMarker);
  }

  private void addGroteMarktPopup() {
    com.google.gson.JsonObject options = new com.google.gson.JsonObject();
    options.add("autoClose", (new com.google.gson.JsonPrimitive(false)));
    options.add("closeOnClick", (new com.google.gson.JsonPrimitive(false)));
    WLeafletMap.Popup groteMarktPopup = new WLeafletMap.Popup(GROTE_MARKT_COORDS);
    groteMarktPopup.setContent("You should check this place out.");
    groteMarktPopup.setOptions(options);
    this.map_.addPopup(groteMarktPopup);
  }

  private List<WLeafletMap.Coordinate> getRoadDescription() {
    List<WLeafletMap.Coordinate> result = new ArrayList<WLeafletMap.Coordinate>();
    result.add(new WLeafletMap.Coordinate(50.9082, 4.66056));
    result.add(new WLeafletMap.Coordinate(50.90901, 4.66426));
    result.add(new WLeafletMap.Coordinate(50.90944, 4.66514));
    result.add(new WLeafletMap.Coordinate(50.90968, 4.66574));
    result.add(new WLeafletMap.Coordinate(50.91021, 4.66541));
    result.add(new WLeafletMap.Coordinate(50.9111, 4.66508));
    result.add(new WLeafletMap.Coordinate(50.9119, 4.66469));
    result.add(new WLeafletMap.Coordinate(50.91224, 4.66463));
    result.add(new WLeafletMap.Coordinate(50.91227, 4.66598));
    result.add(new WLeafletMap.Coordinate(50.9122, 4.66786));
    result.add(new WLeafletMap.Coordinate(50.91199, 4.66962));
    result.add(new WLeafletMap.Coordinate(50.91169, 4.67117));
    result.add(new WLeafletMap.Coordinate(50.91107, 4.67365));
    result.add(new WLeafletMap.Coordinate(50.91061, 4.67515));
    result.add(new WLeafletMap.Coordinate(50.91023, 4.67596));
    result.add(new WLeafletMap.Coordinate(50.9098, 4.67666));
    result.add(new WLeafletMap.Coordinate(50.90953, 4.67691));
    result.add(new WLeafletMap.Coordinate(50.90912, 4.67746));
    result.add(new WLeafletMap.Coordinate(50.90882, 4.67772));
    result.add(new WLeafletMap.Coordinate(50.90838, 4.67801));
    result.add(new WLeafletMap.Coordinate(50.9083, 4.67798));
    result.add(new WLeafletMap.Coordinate(50.90803, 4.67814));
    result.add(new WLeafletMap.Coordinate(50.90742, 4.67836));
    result.add(new WLeafletMap.Coordinate(50.90681, 4.67845));
    result.add(new WLeafletMap.Coordinate(50.90209, 4.67871));
    result.add(new WLeafletMap.Coordinate(50.90134, 4.67893));
    result.add(new WLeafletMap.Coordinate(50.90066, 4.6793));
    result.add(new WLeafletMap.Coordinate(50.90015, 4.67972));
    result.add(new WLeafletMap.Coordinate(50.89945, 4.68059));
    result.add(new WLeafletMap.Coordinate(50.89613, 4.68582));
    result.add(new WLeafletMap.Coordinate(50.8952, 4.68719));
    result.add(new WLeafletMap.Coordinate(50.89464, 4.68764));
    result.add(new WLeafletMap.Coordinate(50.89183, 4.69032));
    result.add(new WLeafletMap.Coordinate(50.89131, 4.69076));
    result.add(new WLeafletMap.Coordinate(50.88916, 4.69189));
    result.add(new WLeafletMap.Coordinate(50.88897, 4.69195));
    result.add(new WLeafletMap.Coordinate(50.88859, 4.69195));
    result.add(new WLeafletMap.Coordinate(50.88813, 4.69193));
    result.add(new WLeafletMap.Coordinate(50.88697, 4.69135));
    result.add(new WLeafletMap.Coordinate(50.88669, 4.6913));
    result.add(new WLeafletMap.Coordinate(50.88531, 4.69155));
    result.add(new WLeafletMap.Coordinate(50.88425, 4.69196));
    result.add(new WLeafletMap.Coordinate(50.88398, 4.69219));
    result.add(new WLeafletMap.Coordinate(50.88391, 4.69226));
    result.add(new WLeafletMap.Coordinate(50.88356, 4.69292));
    result.add(new WLeafletMap.Coordinate(50.88323, 4.69361));
    result.add(new WLeafletMap.Coordinate(50.88067, 4.6934));
    result.add(new WLeafletMap.Coordinate(50.88055, 4.69491));
    result.add(new WLeafletMap.Coordinate(50.88036, 4.69616));
    result.add(new WLeafletMap.Coordinate(50.88009, 4.69755));
    result.add(new WLeafletMap.Coordinate(50.87973, 4.69877));
    result.add(new WLeafletMap.Coordinate(50.87951, 4.69856));
    result.add(new WLeafletMap.Coordinate(50.87933, 4.69831));
    result.add(new WLeafletMap.Coordinate(50.87905, 4.69811));
    result.add(new WLeafletMap.Coordinate(50.879, 4.69793));
    result.add(new WLeafletMap.Coordinate(50.87856, 4.69745));
    result.add(new WLeafletMap.Coordinate(50.87849, 4.69746));
    result.add(new WLeafletMap.Coordinate(50.87843, 4.69758));
    result.add(new WLeafletMap.Coordinate(50.87822, 4.69758));
    result.add(new WLeafletMap.Coordinate(50.87814, 4.69766));
    result.add(new WLeafletMap.Coordinate(50.87813, 4.69788));
    result.add(new WLeafletMap.Coordinate(50.87789, 4.69862));
    return result;
  }

  private static final WLeafletMap.Coordinate EMWEB_COORDS =
      new WLeafletMap.Coordinate(50.906901, 4.655973);
  private static final WLeafletMap.Coordinate GROTE_MARKT_COORDS =
      new WLeafletMap.Coordinate(50.879161, 4.700751);
}
