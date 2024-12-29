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

class GoogleMapExample extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(GoogleMapExample.class);

  public GoogleMapExample(WContainerWidget parentContainer) {
    super();
    this.mapTypeModel_ = null;
    WHBoxLayout layout = new WHBoxLayout();
    this.setLayout(layout);
    this.setHeight(new WLength(400));
    this.map_ = new WGoogleMap(GoogleMapsVersion.v3);
    layout.addWidget(this.map_, 1);
    this.map_.setMapTypeControl(MapTypeControl.Default);
    this.map_.enableScrollWheelZoom();
    WTemplate controls = new WTemplate(tr("graphics-GoogleMap-controls"));
    layout.addWidget(controls);
    WPushButton zoomIn = new WPushButton("+");
    controls.bindWidget("zoom-in", zoomIn);
    zoomIn.addStyleClass("zoom");
    zoomIn
        .clicked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.map_.zoomIn();
            });
    WPushButton zoomOut = new WPushButton("-");
    controls.bindWidget("zoom-out", zoomOut);
    zoomOut.addStyleClass("zoom");
    zoomOut
        .clicked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.map_.zoomOut();
            });
    String[] cityNames = {"Brussels", "Lisbon", "Paris"};
    WGoogleMap.Coordinate[] cityCoords = {
      new WGoogleMap.Coordinate(50.85034, 4.35171),
      new WGoogleMap.Coordinate(38.703731, -9.135475),
      new WGoogleMap.Coordinate(48.877474, 2.312579)
    };
    for (int i = 0; i < 3; ++i) {
      WPushButton city = new WPushButton(cityNames[i]);
      controls.bindWidget(cityNames[i], city);
      final WGoogleMap.Coordinate coord = cityCoords[i];
      city.clicked()
          .addListener(
              this,
              () -> {
                GoogleMapExample.this.map_.panTo(coord);
              });
    }
    WPushButton reset = new WPushButton("Reset");
    controls.bindWidget("emweb", reset);
    reset
        .clicked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.panToEmWeb();
            });
    WPushButton savePosition = new WPushButton("Save current position");
    controls.bindWidget("save-position", savePosition);
    savePosition
        .clicked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.savePosition();
            });
    this.returnToPosition_ = new WPushButton("Return to saved position");
    controls.bindWidget("return-to-saved-position", this.returnToPosition_);
    this.returnToPosition_.setEnabled(false);
    this.returnToPosition_
        .clicked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.map_.returnToSavedPosition();
            });
    this.mapTypeModel_ = new WStringListModel();
    this.addMapTypeControl("No control", MapTypeControl.None);
    this.addMapTypeControl("Default", MapTypeControl.Default);
    this.addMapTypeControl("Menu", MapTypeControl.Menu);
    if (this.map_.getApiVersion() == GoogleMapsVersion.v3) {
      this.addMapTypeControl("Horizontal bar", MapTypeControl.HorizontalBar);
    }
    WComboBox menuControls = new WComboBox();
    controls.bindWidget("control-menu-combo", menuControls);
    menuControls.setModel(this.mapTypeModel_);
    menuControls.setCurrentIndex(1);
    menuControls
        .activated()
        .addListener(
            this,
            (Integer mapType) -> {
              GoogleMapExample.this.setMapTypeControl(mapType);
            });
    WCheckBox draggingCB = new WCheckBox("Enable dragging");
    controls.bindWidget("dragging-cb", draggingCB);
    draggingCB.setChecked(true);
    this.map_.enableDragging();
    draggingCB
        .checked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.map_.enableDragging();
            });
    draggingCB
        .unChecked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.map_.disableDragging();
            });
    WCheckBox enableDoubleClickZoomCB = new WCheckBox("Enable double click zoom");
    controls.bindWidget("double-click-zoom-cb", enableDoubleClickZoomCB);
    enableDoubleClickZoomCB.setChecked(false);
    this.map_.disableDoubleClickZoom();
    enableDoubleClickZoomCB
        .checked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.map_.enableDoubleClickZoom();
            });
    enableDoubleClickZoomCB
        .unChecked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.map_.disableDoubleClickZoom();
            });
    WCheckBox enableScrollWheelZoomCB = new WCheckBox("Enable scroll wheel zoom");
    controls.bindWidget("scroll-wheel-zoom-cb", enableScrollWheelZoomCB);
    enableScrollWheelZoomCB.setChecked(true);
    this.map_.enableScrollWheelZoom();
    enableScrollWheelZoomCB
        .checked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.map_.enableScrollWheelZoom();
            });
    enableScrollWheelZoomCB
        .unChecked()
        .addListener(
            this,
            () -> {
              GoogleMapExample.this.map_.disableScrollWheelZoom();
            });
    List<WGoogleMap.Coordinate> road = this.getRoadDescription();
    this.map_.addPolyline(road, new WColor(0, 191, 255));
    this.map_.addMarker(new WGoogleMap.Coordinate(50.885069, 4.71958));
    this.map_.setCenter(road.get(road.size() - 1));
    this.map_.openInfoWindow(
        road.get(0),
        "<p><img src=\"https://www.emweb.be/css/emweb_small.png\" /></p><p><strong>Emweb office</strong></p>");
    this.map_
        .clicked()
        .addListener(
            this,
            (WGoogleMap.Coordinate c) -> {
              GoogleMapExample.this.googleMapClicked(c);
            });
    this.map_
        .doubleClicked()
        .addListener(
            this,
            (WGoogleMap.Coordinate c) -> {
              GoogleMapExample.this.googleMapDoubleClicked(c);
            });
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public GoogleMapExample() {
    this((WContainerWidget) null);
  }

  private void panToEmWeb() {
    this.map_.panTo(new WGoogleMap.Coordinate(50.9082, 4.66056));
  }

  private void savePosition() {
    this.returnToPosition_.setEnabled(true);
    this.map_.savePosition();
  }

  private void addMapTypeControl(final CharSequence description, MapTypeControl value) {
    int r = this.mapTypeModel_.getRowCount();
    this.mapTypeModel_.insertRow(r);
    this.mapTypeModel_.setData(r, 0, description);
    this.mapTypeModel_.setData(r, 0, value, ItemDataRole.User);
  }

  private void setMapTypeControl(int row) {
    Object mtc = this.mapTypeModel_.getData(row, 0, ItemDataRole.User);
    this.map_.setMapTypeControl(((MapTypeControl) mtc));
  }

  private List<WGoogleMap.Coordinate> getRoadDescription() {
    List<WGoogleMap.Coordinate> result = new ArrayList<WGoogleMap.Coordinate>();
    result.add(new WGoogleMap.Coordinate(50.9082, 4.66056));
    result.add(new WGoogleMap.Coordinate(50.90901, 4.66426));
    result.add(new WGoogleMap.Coordinate(50.90944, 4.66514));
    result.add(new WGoogleMap.Coordinate(50.90968, 4.66574));
    result.add(new WGoogleMap.Coordinate(50.91021, 4.66541));
    result.add(new WGoogleMap.Coordinate(50.9111, 4.66508));
    result.add(new WGoogleMap.Coordinate(50.9119, 4.66469));
    result.add(new WGoogleMap.Coordinate(50.91224, 4.66463));
    result.add(new WGoogleMap.Coordinate(50.91227, 4.66598));
    result.add(new WGoogleMap.Coordinate(50.9122, 4.66786));
    result.add(new WGoogleMap.Coordinate(50.91199, 4.66962));
    result.add(new WGoogleMap.Coordinate(50.91169, 4.67117));
    result.add(new WGoogleMap.Coordinate(50.91107, 4.67365));
    result.add(new WGoogleMap.Coordinate(50.91061, 4.67515));
    result.add(new WGoogleMap.Coordinate(50.91023, 4.67596));
    result.add(new WGoogleMap.Coordinate(50.9098, 4.67666));
    result.add(new WGoogleMap.Coordinate(50.90953, 4.67691));
    result.add(new WGoogleMap.Coordinate(50.90912, 4.67746));
    result.add(new WGoogleMap.Coordinate(50.90882, 4.67772));
    result.add(new WGoogleMap.Coordinate(50.90838, 4.67801));
    result.add(new WGoogleMap.Coordinate(50.9083, 4.67798));
    result.add(new WGoogleMap.Coordinate(50.90803, 4.67814));
    result.add(new WGoogleMap.Coordinate(50.90742, 4.67836));
    result.add(new WGoogleMap.Coordinate(50.90681, 4.67845));
    result.add(new WGoogleMap.Coordinate(50.90209, 4.67871));
    result.add(new WGoogleMap.Coordinate(50.90134, 4.67893));
    result.add(new WGoogleMap.Coordinate(50.90066, 4.6793));
    result.add(new WGoogleMap.Coordinate(50.90015, 4.67972));
    result.add(new WGoogleMap.Coordinate(50.89945, 4.68059));
    result.add(new WGoogleMap.Coordinate(50.89613, 4.68582));
    result.add(new WGoogleMap.Coordinate(50.8952, 4.68719));
    result.add(new WGoogleMap.Coordinate(50.89464, 4.68764));
    result.add(new WGoogleMap.Coordinate(50.89183, 4.69032));
    result.add(new WGoogleMap.Coordinate(50.89131, 4.69076));
    result.add(new WGoogleMap.Coordinate(50.88916, 4.69189));
    result.add(new WGoogleMap.Coordinate(50.88897, 4.69195));
    result.add(new WGoogleMap.Coordinate(50.88859, 4.69195));
    result.add(new WGoogleMap.Coordinate(50.88813, 4.69193));
    result.add(new WGoogleMap.Coordinate(50.88697, 4.69135));
    result.add(new WGoogleMap.Coordinate(50.88669, 4.6913));
    result.add(new WGoogleMap.Coordinate(50.88531, 4.69155));
    result.add(new WGoogleMap.Coordinate(50.88425, 4.69196));
    result.add(new WGoogleMap.Coordinate(50.88398, 4.69219));
    result.add(new WGoogleMap.Coordinate(50.88391, 4.69226));
    result.add(new WGoogleMap.Coordinate(50.88356, 4.69292));
    result.add(new WGoogleMap.Coordinate(50.88323, 4.69361));
    result.add(new WGoogleMap.Coordinate(50.88067, 4.6934));
    result.add(new WGoogleMap.Coordinate(50.88055, 4.69491));
    result.add(new WGoogleMap.Coordinate(50.88036, 4.69616));
    result.add(new WGoogleMap.Coordinate(50.88009, 4.69755));
    result.add(new WGoogleMap.Coordinate(50.87973, 4.69877));
    result.add(new WGoogleMap.Coordinate(50.87951, 4.69856));
    result.add(new WGoogleMap.Coordinate(50.87933, 4.69831));
    result.add(new WGoogleMap.Coordinate(50.87905, 4.69811));
    result.add(new WGoogleMap.Coordinate(50.879, 4.69793));
    result.add(new WGoogleMap.Coordinate(50.87856, 4.69745));
    result.add(new WGoogleMap.Coordinate(50.87849, 4.69746));
    result.add(new WGoogleMap.Coordinate(50.87843, 4.69758));
    result.add(new WGoogleMap.Coordinate(50.87822, 4.69758));
    result.add(new WGoogleMap.Coordinate(50.87814, 4.69766));
    result.add(new WGoogleMap.Coordinate(50.87813, 4.69788));
    result.add(new WGoogleMap.Coordinate(50.87789, 4.69862));
    return result;
  }

  private void googleMapDoubleClicked(WGoogleMap.Coordinate c) {
    System.err
        .append("Double clicked at coordinate (")
        .append(String.valueOf(c.getLatitude()))
        .append(",")
        .append(String.valueOf(c.getLongitude()))
        .append(")");
  }

  private void googleMapClicked(WGoogleMap.Coordinate c) {
    System.err
        .append("Clicked at coordinate (")
        .append(String.valueOf(c.getLatitude()))
        .append(",")
        .append(String.valueOf(c.getLongitude()))
        .append(")");
  }

  private WGoogleMap map_;
  private WAbstractItemModel mapTypeModel_;
  private WPushButton returnToPosition_;
}
