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
 * A widget that displays a leaflet map.
 *
 * <p>This is a simple wrapper around the <a href="https://leafletjs.com/">Leaflet</a> JavaScript
 * library.
 *
 * <p>Leaflet itself does not provide maps. It is a JavaScript library that enables you to use any
 * &quot;tile server&quot;, like OpenStreetMap. If you just create a WLeafletMap (and give it a
 * size), then you will be presented with an empty map. You can then add tile layers to the map
 * using {@link WLeafletMap#addTileLayer(String urlTemplate, com.google.gson.JsonObject options)
 * addTileLayer()}.
 *
 * <p>WLeafletMap is not exhaustive in its support for Leaflet features. It supports a subset out of
 * the box. One of these features is markers, which come in two flavors: standard leaflet markers
 * ({@link LeafletMarker}) and widget markers ({@link WidgetMarker}). Using a widget marker, you can
 * place arbitrary widgets on the map.
 *
 * <p>If you need direct access to the leaflet map in your own custom JavaScript, you can use {@link
 * WLeafletMap#getMapJsRef() getMapJsRef()}.
 *
 * <p>Leaflet itself is not bundled with JWt. Use the <code>leafletJSURL</code> and <code>
 * leafletCSSURL</code> properties to configure where the JavaScript and CSS of Leaflet should be
 * loaded from.
 */
public class WLeafletMap extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WLeafletMap.class);

  static class OrderedAction {
    private static Logger logger = LoggerFactory.getLogger(OrderedAction.class);

    enum Type {
      None,
      Add,
      MoveFront,
      MoveBack;

      /** Returns the numerical representation of this enum. */
      public int getValue() {
        return ordinal();
      }
    }

    public OrderedAction(WLeafletMap.OrderedAction.Type type) {
      this.type = type;
      this.sequenceNumber = 0;
      this.itemEntry = null;
    }

    public OrderedAction() {
      this(WLeafletMap.OrderedAction.Type.None);
    }

    public WLeafletMap.OrderedAction.Type type;
    public int sequenceNumber;
    public WLeafletMap.ItemEntry itemEntry;
  }
  /** A geographical coordinate (latitude/longitude) */
  public static class Coordinate {
    private static Logger logger = LoggerFactory.getLogger(Coordinate.class);

    /**
     * Default constructor.
     *
     * <p>Constructs a coordinate with latitude and longitude set to 0.
     */
    public Coordinate() {
      this.lat_ = 0.0;
      this.lng_ = 0.0;
    }
    /** Create a coordinate (latitude, longitude) */
    public Coordinate(double latitude, double longitude) {
      this.lat_ = latitude;
      this.lng_ = longitude;
    }
    /** Set the latitude. */
    public void setLatitude(double latitude) {
      this.lat_ = latitude;
    }
    /** Get the latitude. */
    public double getLatitude() {
      return this.lat_;
    }
    /** Set the longitude. */
    public void setLongitude(double longitude) {
      this.lng_ = longitude;
    }
    /** Get the longitude. */
    public double getLongitude() {
      return this.lng_;
    }
    /** Equality comparison operator. */
    public boolean equals(final WLeafletMap.Coordinate other) {
      return this.lat_ == other.lat_ && this.lng_ == other.lng_;
    }

    private double lat_;
    private double lng_;
  }
  /**
   * An abstract map item.
   *
   * <p>This is the base class for all items that can be added to a {@link WLeafletMap}. As this is
   * an abstract class, it should not be added directly to a map as is. Instead, you should use one
   * of the subclasses.
   */
  public abstract static class AbstractMapItem extends WObject {
    private static Logger logger = LoggerFactory.getLogger(AbstractMapItem.class);

    /**
     * Move the map item.
     *
     * <p>If this map item belongs to a map, this will trigger an update of the {@link WLeafletMap}
     * to move the map item. If it doesn&apos;t belong to a map, the position is merely updated.
     */
    public void move(final WLeafletMap.Coordinate pos) {
      this.pos_ = pos;
      if (this.map_ != null) {
        this.flags_.set(BIT_MOVED);
        this.map_.scheduleRender();
      }
    }
    /**
     * Get the current position.
     *
     * <p>
     *
     * @see WLeafletMap.AbstractMapItem#move(WLeafletMap.Coordinate pos)
     */
    public WLeafletMap.Coordinate getPosition() {
      return this.pos_;
    }
    /**
     * {@link Signal} emitted when the user clicks on the map item.
     *
     * <p>
     *
     * <p><i><b>Note: </b>For {@link AbstractOverlayItem}, this signal is only triggered if their
     * interactive option is set to true (by default, it is set to false). </i>
     *
     * @see WLeafletMap#setOptions(com.google.gson.JsonObject options)
     */
    public Signal clicked() {
      return this.clicked_;
    }
    /**
     * {@link Signal} emitted when the user double-clicks on the map item.
     *
     * <p>
     *
     * <p><i><b>Note: </b>Double-clicking on a marker may trigger the doubleClickZoom from leaflet
     * as well, which does zoom on the map, centered on where the click occurred. </i>
     *
     * <p><i><b>Note: </b>For {@link AbstractOverlayItem}, this signal is only triggered if their
     * interactive option is set to true (by default, it is set to false). </i>
     *
     * @see WLeafletMap#setOptions(com.google.gson.JsonObject options)
     */
    public Signal doubleClicked() {
      return this.dblclicked_;
    }
    /**
     * {@link Signal} emitted when the user holds the mouse click on the map item.
     *
     * <p>
     *
     * <p><i><b>Note: </b>For {@link AbstractOverlayItem}, this signal is only triggered if their
     * interactive option is set to true (by default, it is set to false). </i>
     *
     * @see WLeafletMap#setOptions(com.google.gson.JsonObject options)
     */
    public Signal mouseWentDown() {
      return this.mousedown_;
    }
    /**
     * {@link Signal} emitted when the user releases the mouse click on the map item.
     *
     * <p>
     *
     * <p><i><b>Note: </b>For {@link AbstractOverlayItem}, this signal is only triggered if their
     * interactive option is set to true (by default, it is set to false). </i>
     *
     * @see WLeafletMap#setOptions(com.google.gson.JsonObject options)
     */
    public Signal mouseWentUp() {
      return this.mouseup_;
    }
    /**
     * {@link Signal} emitted when the user&apos;s mouse enters the map item.
     *
     * <p>
     *
     * <p><i><b>Note: </b>For {@link AbstractOverlayItem}, this signal is only triggered if their
     * interactive option is set to true (by default, it is set to false). </i>
     *
     * @see WLeafletMap#setOptions(com.google.gson.JsonObject options)
     */
    public Signal mouseWentOver() {
      return this.mouseover_;
    }
    /**
     * {@link Signal} emitted when the user&apos;s mouse leaves the map item.
     *
     * <p>
     *
     * <p><i><b>Note: </b>For {@link AbstractOverlayItem}, this signal is only triggered if their
     * interactive option is set to true (by default, it is set to false). </i>
     *
     * @see WLeafletMap#setOptions(com.google.gson.JsonObject options)
     */
    public Signal mouseWentOut() {
      return this.mouseout_;
    }
    /**
     * Constructor.
     *
     * <p>Since this is an abstract class, it should not be used directly.
     */
    protected AbstractMapItem(final WLeafletMap.Coordinate pos) {
      super();
      this.flags_ = new BitSet();
      this.map_ = null;
      this.pos_ = pos;
      this.orderedAction_ = new WLeafletMap.OrderedAction();
      this.clicked_ = new Signal();
      this.dblclicked_ = new Signal();
      this.mousedown_ = new Signal();
      this.mouseup_ = new Signal();
      this.mouseover_ = new Signal();
      this.mouseout_ = new Signal();
    }
    /**
     * Writes the JS code to create this item to the stream.
     *
     * <p>This method should write in <code>ss</code> the JS code that creates the item. The <code>
     * postJS</code> stream can be used to write JS code that should be executed after the item has
     * been created.
     */
    protected abstract void createItemJS(
        final StringBuilder ss, final StringBuilder postJS, long id);
    /**
     * Unrender the item.
     *
     * <p>This is called when the map needs to be recreated. You can override this function in case
     * you need to do some cleanup before the map is recreated.
     *
     * <p>By default, this does nothing.
     *
     * <p>
     *
     * @see WLeafletMap#setOptions(com.google.gson.JsonObject options)
     */
    protected void unrender() {}
    /**
     * Return whether this item needs to be updated.
     *
     * <p>This is called when the map is rendered. If this returns true, {@link
     * WLeafletMap.AbstractMapItem#update(StringBuilder js) update()} will be used to update the
     * item.
     */
    protected boolean needsUpdate() {
      return false;
    }
    /** Returns the map this item belongs to. */
    protected WLeafletMap getMap() {
      return this.map_;
    }
    /**
     * Writes the JS to update this item to the stream.
     *
     * <p>This is called when the map is rendered if {@link
     * WLeafletMap.AbstractMapItem#needsUpdate() needsUpdate()} returns true.
     */
    protected void update(final StringBuilder js) {}
    /**
     * Set the map this item belongs to.
     *
     * <p>This is called to set the map the item belongs to. You can override this function if you
     * need to do something when the item is added to a map.
     *
     * <p>You should not call this function directly.
     *
     * <p>
     *
     * @see WLeafletMap.AbstractMapItem#getMap()
     */
    protected void setMap(WLeafletMap map) {
      this.map_ = map;
    }

    private static final int BIT_MOVED = 0;
    private BitSet flags_;
    WLeafletMap map_;
    WLeafletMap.Coordinate pos_;
    WLeafletMap.OrderedAction orderedAction_;
    private Signal clicked_;
    private Signal dblclicked_;
    private Signal mousedown_;
    private Signal mouseup_;
    private Signal mouseover_;
    private Signal mouseout_;
    /**
     * This method should write the JS code needed to update the item. The JS code written can use
     * o.wtObj to refer to the {@link WLeafletMap} JS object.
     */
    void applyChangeJS(final StringBuilder ss, long id) {
      if (this.flags_.get(BIT_MOVED)) {
        ss.append("o.wtObj.moveMapItem(").append(id).append(",[");
        char[] buf = new char[30];
        ss.append(MathUtils.roundJs(this.getPosition().getLatitude(), 16)).append(",");
        ss.append(MathUtils.roundJs(this.getPosition().getLongitude(), 16)).append("]);");
        this.flags_.clear(BIT_MOVED);
      }
    }

    private boolean isChanged() {
      return !this.flags_.isEmpty();
    }
    /** The name of the JS function that adds the item to the map. */
    String getAddFunctionJs() {
      return "addMapItem";
    }

    private WLeafletMap.OrderedAction getOrderedAction() {
      return this.orderedAction_;
    }

    private void resetOrderedAction() {
      this.orderedAction_ = new WLeafletMap.OrderedAction();
    }
  }
  /**
   * An abstract map item with text.
   *
   * <p>This is the base class for all AbstractMapItems that can be added to other AbstractMapItems.
   * This is an abstract class, so it should not be used directly.
   */
  public abstract static class AbstractOverlayItem extends WLeafletMap.AbstractMapItem {
    private static Logger logger = LoggerFactory.getLogger(AbstractOverlayItem.class);

    /**
     * Set the options of the {@link AbstractOverlayItem}.
     *
     * <p>Set the options that will be passed to the {@link WLeafletMap} {@link AbstractOverlayItem}
     * at construction.
     *
     * <p>See <a
     * href="https://leafletjs.com/reference.html">https://leafletjs.com/reference.html</a> for the
     * list of options.
     *
     * <p>
     *
     * <p><i><b>Note: </b>Modifying the options after the {@link AbstractOverlayItem} has been
     * loaded by the user will not work as the {@link AbstractOverlayItem} was already constructed.
     * Some option like &apos;content&apos; can be changed after load using the appropriate
     * function. </i>
     *
     * @see WLeafletMap.AbstractOverlayItem#setContent(WWidget content)
     */
    public void setOptions(final com.google.gson.JsonObject options) {
      this.options_ = options;
    }
    /** Set the content. */
    public void setContent(WWidget content) {
      if (this.content_ != null) {
        {
          WWidget toRemove = this.content_.removeFromParent();
          if (toRemove != null) toRemove.remove();
        }
      }
      this.content_ = content;
      if (this.content_ != null) {
        this.content_.setParentWidget(this.getMap());
      }
      this.flags_.set(BIT_CONTENT_CHANGED);
      if (this.getMap() != null) {
        this.getMap().scheduleRender();
      }
    }
    /** Set the content. */
    public void setContent(final CharSequence content) {
      this.setContent(new WText(WString.toWString(content)));
    }
    /**
     * Get the content.
     *
     * <p>
     *
     * @see WLeafletMap.AbstractOverlayItem#setContent(WWidget content)
     */
    public WWidget getContent() {
      return this.content_;
    }
    /**
     * Opens the {@link AbstractOverlayItem}.
     *
     * <p>
     *
     * @see WLeafletMap.AbstractOverlayItem#close()
     * @see WLeafletMap.AbstractOverlayItem#toggle()
     */
    public void open() {
      if (!this.open_) {
        this.toggle();
      }
    }
    /**
     * Closes the {@link AbstractOverlayItem}.
     *
     * <p>
     *
     * @see WLeafletMap.AbstractOverlayItem#open()
     * @see WLeafletMap.AbstractOverlayItem#toggle()
     */
    public void close() {
      if (this.open_) {
        this.toggle();
      }
    }
    /**
     * Opens or closes the {@link AbstractOverlayItem}.
     *
     * <p>
     *
     * @see WLeafletMap.AbstractOverlayItem#open()
     * @see WLeafletMap.AbstractOverlayItem#close()
     */
    public void toggle() {
      this.open_ = !this.open_;
      if (this.getMap() != null) {
        this.flags_.set(BIT_OPEN_CHANGED);
        this.getMap().scheduleRender();
      }
      if (this.open_) {
        this.opened().trigger();
      } else {
        this.closed().trigger();
      }
    }
    /**
     * Returns whether the {@link AbstractOverlayItem} is open.
     *
     * <p>
     *
     * @see WLeafletMap.AbstractOverlayItem#close()
     * @see WLeafletMap.AbstractOverlayItem#toggle()
     */
    public boolean isOpen() {
      return this.open_;
    }
    /**
     * {@link Signal} emited after the {@link AbstractOverlayItem} was opened.
     *
     * <p>
     *
     * @see WLeafletMap.AbstractOverlayItem#closed()
     */
    public Signal opened() {
      return this.opened_;
    }
    /**
     * {@link Signal} emited after the {@link AbstractOverlayItem} was closed.
     *
     * <p>
     *
     * @see WLeafletMap.AbstractOverlayItem#opened()
     */
    public Signal closed() {
      return this.closed_;
    }
    /**
     * Brings this {@link AbstractOverlayItem} to the front.
     *
     * <p>This brings this {@link AbstractOverlayItem} to the front of other {@link
     * AbstractOverlayItem} of the same type.
     *
     * <p>
     *
     * <p><i><b>Warning: </b>This function only works after the {@link AbstractOverlayItem} is added
     * to the map. </i>
     *
     * @see WLeafletMap.AbstractOverlayItem#bringToBack()
     */
    public void bringToFront() {
      if (this.getMap() != null) {
        this.orderedAction_.type = WLeafletMap.OrderedAction.Type.MoveFront;
        this.orderedAction_.sequenceNumber = this.getMap().getNextActionSequenceNumber();
        this.getMap().scheduleRender();
      }
    }
    /**
     * Brings this {@link AbstractOverlayItem} to the back.
     *
     * <p>This brings this {@link AbstractOverlayItem} to the back of other {@link
     * AbstractOverlayItem} of the same type.
     *
     * <p>
     *
     * <p><i><b>Warning: </b>This function only works after the {@link AbstractOverlayItem} is added
     * to the map. </i>
     *
     * @see WLeafletMap.AbstractOverlayItem#bringToFront()
     */
    public void bringToBack() {
      if (this.getMap() != null) {
        this.orderedAction_.type = WLeafletMap.OrderedAction.Type.MoveBack;
        this.orderedAction_.sequenceNumber = this.getMap().getNextActionSequenceNumber();
        this.getMap().scheduleRender();
      }
    }
    /**
     * Constructor.
     *
     * <p>Creates a new {@link AbstractOverlayItem} that has the given coordinates.
     *
     * <p>Since this is an abstract class, this should not be used directly.
     */
    protected AbstractOverlayItem(final WLeafletMap.Coordinate pos) {
      super(pos);
      this.flags_ = new BitSet();
      this.content_ = null;
      this.options_ = new com.google.gson.JsonObject();
      this.opened_ = new Signal();
      this.closed_ = new Signal();
      this.init();
    }
    /**
     * Constructor.
     *
     * <p>Creates a new {@link AbstractOverlayItem} with the given content and that has the given
     * coordinates.
     *
     * <p>Since this is an abstract class, this should not be used directly.
     */
    protected AbstractOverlayItem(final WLeafletMap.Coordinate pos, WWidget content) {
      super(pos);
      this.flags_ = new BitSet();
      this.content_ = null;
      this.options_ = new com.google.gson.JsonObject();
      this.opened_ = new Signal();
      this.closed_ = new Signal();
      this.init();
      this.setContent(content);
    }

    protected void setMap(WLeafletMap map) {
      super.setMap(map);
      if (this.content_ != null) {
        this.content_.setParentWidget(map);
      }
    }

    static final int BIT_CONTENT_CHANGED = 0;
    static final int BIT_OPEN_CHANGED = 1;
    BitSet flags_;
    WWidget content_;
    private boolean open_;
    com.google.gson.JsonObject options_;
    private Signal opened_;
    private Signal closed_;

    private boolean isChanged() {
      return !this.flags_.isEmpty() || super.isChanged();
    }

    void applyChangeJS(final StringBuilder ss, long id) {
      if (this.flags_.get(BIT_CONTENT_CHANGED)) {
        DomElement element = this.content_.createSDomElement(WApplication.getInstance());
        List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
        EscapeOStream delayedJS = new EscapeOStream();
        EscapeOStream content = new EscapeOStream();
        content.append("o.wtObj.setOverlayItemContent(").append(String.valueOf(id)).append(",'");
        content.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
        element.asHTML(content, delayedJS, timeouts);
        content.popEscape();
        content.append("','");
        content.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
        content.append(element.getId());
        content.popEscape();
        content.append("');");
        ss.append("o.wtObj.delayedJS = function() {").append(delayedJS.toString()).append("};");
        ss.append(content.toString());
        this.flags_.clear(BIT_CONTENT_CHANGED);
      }
      if (this.flags_.get(BIT_OPEN_CHANGED)) {
        ss.append("o.wtObj.toggleOverlayItem(").append(id).append(",");
        ss.append(this.open_).append(");");
        this.flags_.clear(BIT_OPEN_CHANGED);
      }
      super.applyChangeJS(ss, id);
    }

    private void init() {
      this.open_ = true;
    }

    String getAddFunctionJs() {
      return "addOverlayItem";
    }
  }
  /**
   * A popup that can be added to the {@link WLeafletMap}.
   *
   * <p>Popups are interactive windows that can be opened on the map, typically linked to a map
   * location or a {@link Marker}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Multiple popups can be added to a map (using coordinates), but only one
   * popup at the time can be linked to each {@link Marker}. </i>
   *
   * @see WLeafletMap#addPopup(WLeafletMap.Popup popup)
   * @see WLeafletMap.Marker#addPopup(WLeafletMap.Popup popup)
   */
  public static class Popup extends WLeafletMap.AbstractOverlayItem {
    private static Logger logger = LoggerFactory.getLogger(Popup.class);

    /** Create a popup with the given coordinates. */
    public Popup(final WLeafletMap.Coordinate pos) {
      super(pos);
    }
    /**
     * Create a popup with the given coordinates.
     *
     * <p>Calls {@link #Popup(WLeafletMap.Coordinate pos) this(new WLeafletMap.Coordinate(0, 0))}
     */
    public Popup() {
      this(new WLeafletMap.Coordinate(0, 0));
    }
    /** Create a popup with the given content. */
    public Popup(WWidget content) {
      super(new WLeafletMap.Coordinate(0, 0), content);
    }
    /**
     * Create a popup with the given content.
     *
     * <p>This is a shortcut for creating a popup with a {@link WText} widget as content.
     */
    public Popup(final CharSequence content) {
      super(new WLeafletMap.Coordinate(0, 0), new WText(WString.toWString(content)));
    }
    /** Create a popup with the given content and coordinates. */
    public Popup(final WLeafletMap.Coordinate pos, WWidget content) {
      super(pos, content);
    }
    /**
     * Create a popup with the given content and coordinates.
     *
     * <p>This is a shortcut for creating a popup with a {@link WText} widget as content.
     */
    public Popup(final WLeafletMap.Coordinate pos, final CharSequence content) {
      super(pos, new WText(WString.toWString(content)));
    }

    protected void createItemJS(final StringBuilder ss, final StringBuilder postJS, long id) {
      EscapeOStream es = new EscapeOStream(ss);
      String optionsStr = this.options_.toString();
      es.append("L.popup(");
      if (!this.options_.entrySet().isEmpty()) {
        es.append("JSON.parse('");
        es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
        es.append(optionsStr);
        es.popEscape();
        es.append("')");
      }
      es.append(").setLatLng(L.latLng(");
      char[] buf = new char[30];
      es.append(MathUtils.roundJs(this.getPosition().getLatitude(), 16)).append(",");
      es.append(MathUtils.roundJs(this.getPosition().getLongitude(), 16)).append("))");
      this.applyChangeJS(postJS, id);
    }

    String getAddFunctionJs() {
      return "addPopup";
    }
  }
  /**
   * A {@link Tooltip} that can be added to the {@link WLeafletMap}.
   *
   * <p>Tooltips are interactive windows that can be opened on the map, typically linked to a map
   * location or a {@link Marker}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Multiple tooltips can be added to a map (using coordinates), but only one
   * tooltip at the time can be linked to each {@link Marker}. </i>
   *
   * @see WLeafletMap#addTooltip(WLeafletMap.Tooltip tooltip)
   * @see WLeafletMap.Marker#addTooltip(WLeafletMap.Tooltip tooltip)
   */
  public static class Tooltip extends WLeafletMap.AbstractOverlayItem {
    private static Logger logger = LoggerFactory.getLogger(Tooltip.class);

    /** Create a tooltip with the given coordinates. */
    public Tooltip(final WLeafletMap.Coordinate pos) {
      super(pos);
    }
    /**
     * Create a tooltip with the given coordinates.
     *
     * <p>Calls {@link #Tooltip(WLeafletMap.Coordinate pos) this(new WLeafletMap.Coordinate(0, 0))}
     */
    public Tooltip() {
      this(new WLeafletMap.Coordinate(0, 0));
    }
    /** Create a tooltip with the given content. */
    public Tooltip(WWidget content) {
      super(new WLeafletMap.Coordinate(0, 0), content);
    }
    /**
     * Create a tooltip with the given content.
     *
     * <p>This is a shortcut for creating a tooltip with a {@link WText} widget as content.
     */
    public Tooltip(final CharSequence content) {
      super(new WLeafletMap.Coordinate(0, 0), new WText(WString.toWString(content)));
    }
    /** Create a tooltip with the given content and coordinates. */
    public Tooltip(final WLeafletMap.Coordinate pos, WWidget content) {
      super(pos, content);
    }
    /**
     * Create a tooltip with the given content and coordinates.
     *
     * <p>This is a shortcut for creating a tooltip with a {@link WText} widget as content.
     */
    public Tooltip(final WLeafletMap.Coordinate pos, final CharSequence content) {
      super(pos, new WText(WString.toWString(content)));
    }

    protected void createItemJS(final StringBuilder ss, final StringBuilder postJS, long id) {
      EscapeOStream es = new EscapeOStream(ss);
      String optionsStr = this.options_.toString();
      es.append("L.tooltip(");
      if (!this.options_.entrySet().isEmpty()) {
        es.append("JSON.parse('");
        es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
        es.append(optionsStr);
        es.popEscape();
        es.append("')");
      }
      es.append(").setLatLng(L.latLng(");
      char[] buf = new char[30];
      es.append(MathUtils.roundJs(this.getPosition().getLatitude(), 16)).append(",");
      es.append(MathUtils.roundJs(this.getPosition().getLongitude(), 16)).append("))");
      this.applyChangeJS(postJS, id);
    }

    String getAddFunctionJs() {
      return "addTooltip";
    }
  }
  /**
   * An abstract marker.
   *
   * <p>This marker can be placed on a {@link WLeafletMap} at certain coordinates.
   */
  public abstract static class Marker extends WLeafletMap.AbstractMapItem {
    private static Logger logger = LoggerFactory.getLogger(Marker.class);

    /**
     * Add the popup to the {@link Marker}.
     *
     * <p>Add the popup to the {@link Marker}. This will remove any popup previously added to this
     * {@link Marker}.
     *
     * <p>A popup added to a {@link Marker} will have it&apos;s coordinate set to the coordinate of
     * the {@link Marker} and will be closed by default. If the {@link Marker}&apos;s interactive
     * option is true, the popup will switch between closed and open when the {@link Marker} is
     * clicked.
     *
     * <p>
     *
     * @see WLeafletMap.Marker#getRemovePopup()
     */
    public void addPopup(WLeafletMap.Popup popup) {
      if (popup != null) {
        if (this.map_ != null) {
          if (this.popup_ != null) {
            this.map_.removePopup(this.popup_, this);
          }
          this.popup_ = popup;
          this.map_.addItem(popup, this);
        } else {
          this.popup_ = popup;
          this.popupBuffer_ = popup;
        }
        this.popup_.pos_ = this.pos_;
        if (!this.popup_.flags_.get(AbstractOverlayItem.BIT_OPEN_CHANGED)) {
          this.popup_.close();
        }
      }
    }
    /**
     * Removes the popup from the {@link Marker}.
     *
     * <p>
     *
     * @see WLeafletMap.Marker#addPopup(WLeafletMap.Popup popup)
     */
    public WLeafletMap.Popup getRemovePopup() {
      if (!(this.popup_ != null)) {
        return null;
      }
      WLeafletMap.Popup popup = this.popup_;
      this.popup_ = null;
      if (this.map_ != null) {
        return this.map_.removePopup(popup, this);
      }
      WLeafletMap.Popup result = this.popupBuffer_;
      this.popupBuffer_ = null;
      return result;
    }
    /**
     * Return the popup added to the {@link Marker}.
     *
     * <p>
     *
     * @see WLeafletMap.Marker#addPopup(WLeafletMap.Popup popup)
     */
    public WLeafletMap.Popup getPopup() {
      return this.popup_;
    }
    /**
     * Add the tooltip to the {@link Marker}.
     *
     * <p>Add the tooltip to the {@link Marker}. This will remove any tooltip previously added to
     * this {@link Marker}.
     *
     * <p>A tooltip added to a {@link Marker} will have it&apos;s coordinate set to the coordinate
     * of the {@link Marker}. If the {@link Marker}&apos;s option interactive is true, the tooltip
     * will switch between closed and open when the mouse hover over the {@link Marker}.
     *
     * <p>
     *
     * @see WLeafletMap.Marker#getRemoveTooltip()
     */
    public void addTooltip(WLeafletMap.Tooltip tooltip) {
      if (tooltip != null) {
        if (this.map_ != null) {
          if (this.tooltip_ != null) {
            this.map_.removeTooltip(this.tooltip_, this);
          }
          this.tooltip_ = tooltip;
          this.map_.addItem(tooltip, this);
        } else {
          this.tooltip_ = tooltip;
          this.tooltipBuffer_ = tooltip;
        }
        this.tooltip_.pos_ = this.pos_;
        if (!this.tooltip_.flags_.get(AbstractOverlayItem.BIT_OPEN_CHANGED)) {
          this.tooltip_.close();
        }
      }
    }
    /**
     * Removes the tooltip from the {@link Marker}.
     *
     * <p>
     *
     * @see WLeafletMap.Marker#addTooltip(WLeafletMap.Tooltip tooltip)
     */
    public WLeafletMap.Tooltip getRemoveTooltip() {
      if (!(this.tooltip_ != null)) {
        return null;
      }
      WLeafletMap.Tooltip tooltip = this.tooltip_;
      this.tooltip_ = null;
      if (this.map_ != null) {
        return this.map_.removeTooltip(tooltip, this);
      }
      WLeafletMap.Tooltip result = this.tooltipBuffer_;
      this.tooltipBuffer_ = null;
      return result;
    }
    /**
     * Return the tooltip added to the {@link Marker}.
     *
     * <p>
     *
     * @see WLeafletMap.Marker#addTooltip(WLeafletMap.Tooltip tooltip)
     */
    public WLeafletMap.Tooltip getTooltip() {
      return this.tooltip_;
    }

    protected Marker(final WLeafletMap.Coordinate pos) {
      super(pos);
      this.popup_ = null;
      this.popupBuffer_ = (WLeafletMap.Popup) null;
      this.tooltip_ = null;
      this.tooltipBuffer_ = (WLeafletMap.Tooltip) null;
    }

    protected void setMap(WLeafletMap map) {
      if (this.map_ != null) {
        if (this.popup_ != null) {
          WLeafletMap.AbstractMapItem popup = this.map_.removeItem(this.popup_, this);
          if (popup != null) {
            this.popupBuffer_ = this.popup_;
          }
        }
        if (this.tooltip_ != null) {
          WLeafletMap.AbstractMapItem tooltip = this.map_.removeItem(this.tooltip_, this);
          if (tooltip != null) {
            this.tooltipBuffer_ = this.tooltip_;
          }
        }
      }
      super.setMap(map);
      if (this.map_ != null) {
        if (this.popup_ != null) {
          this.map_.addItem(this.popupBuffer_, this);
        }
        if (this.tooltip_ != null) {
          this.map_.addItem(this.tooltipBuffer_, this);
        }
      }
    }

    private WLeafletMap.Popup popup_;
    private WLeafletMap.Popup popupBuffer_;
    private WLeafletMap.Tooltip tooltip_;
    private WLeafletMap.Tooltip tooltipBuffer_;
  }
  /**
   * A marker rendered with a widget.
   *
   * <p>This can be used to place arbitrary widgets on the map.
   *
   * <p>The widgets will stay the same size regardless of the zoom level of the map.
   */
  public static class WidgetMarker extends WLeafletMap.Marker {
    private static Logger logger = LoggerFactory.getLogger(WidgetMarker.class);

    /** Create a new {@link WidgetMarker} at the given position with the given widget. */
    public WidgetMarker(final WLeafletMap.Coordinate pos, WWidget widget) {
      super(pos);
      this.container_ = (WContainerWidget) null;
      this.anchorX_ = -1;
      this.anchorY_ = -1;
      this.anchorPointChanged_ = false;
      this.createContainer();
      this.container_.addWidget(widget);
    }
    /** Get the widget. */
    public WWidget getWidget() {
      if (this.container_ != null && this.container_.getCount() > 0) {
        return this.container_.getWidget(0);
      }
      return null;
    }
    /**
     * Set the anchor point of the marker.
     *
     * <p>This determines the &quot;tip&quot; of the marker (relative to its top left corner). The
     * marker will be aligned so that this point is at the marker&apos;s geographical location.
     *
     * <p>If x is negative, the anchor point is in the horizontal center of the widget. If y is
     * negative, the anchor point is in the vertical center of the widget.
     *
     * <p>By default the anchor point is in the middle (horizontal and vertical center).
     */
    public void setAnchorPoint(double x, double y) {
      this.anchorX_ = x;
      this.anchorY_ = y;
      if (this.getMap() != null && this.getMap().isRendered()) {
        this.anchorPointChanged_ = true;
        this.getMap().scheduleRender();
      }
    }

    protected void setMap(WLeafletMap map) {
      super.setMap(map);
      if (this.container_ != null) {
        this.container_.setParentWidget(map);
      }
    }

    protected void createItemJS(final StringBuilder ss, final StringBuilder postJS, long id) {
      DomElement element = this.container_.createSDomElement(WApplication.getInstance());
      List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
      char[] buf = new char[30];
      if (this.anchorX_ >= 0 || this.anchorY_ >= 0) {
        this.updateAnchorJS(postJS);
      }
      EscapeOStream js = new EscapeOStream(postJS);
      EscapeOStream es = new EscapeOStream(ss);
      es.append("(function(){");
      es.append("var wIcon=L.divIcon({className:'',iconSize:null,iconAnchor:null,");
      es.append("html:'");
      es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
      element.asHTML(es, js, timeouts);
      es.popEscape();
      es.append("'});");
      es.append("return L.marker([");
      es.append(MathUtils.roundJs(this.getPosition().getLatitude(), 16)).append(",");
      es.append(MathUtils.roundJs(this.getPosition().getLongitude(), 16)).append("],");
      es.append("{icon:wIcon,keyboard:false});})()");
    }

    protected void unrender() {
      WWidget w = this.getWidget();
      WWidget uW = null;
      if (w != null) {
        uW = this.container_.removeWidget(w);
      }
      this.container_ = null;
      this.createContainer();
      if (uW != null) {
        this.container_.addWidget(uW);
      }
    }

    protected boolean needsUpdate() {
      return this.anchorPointChanged_;
    }

    protected void update(final StringBuilder js) {
      if (this.anchorPointChanged_) {
        this.updateAnchorJS(js);
        this.anchorPointChanged_ = false;
      }
    }

    private WContainerWidget container_;
    private double anchorX_;
    private double anchorY_;
    private boolean anchorPointChanged_;

    private void createContainer() {
      this.container_ = new WContainerWidget();
      this.container_.addStyleClass("Wt-leaflet-widgetmarker-container");
      this.container_.setJavaScriptMember("wtReparentBarrier", "true");
      WLeafletMap m = this.getMap();
      if (m != null) {
        this.container_.setParentWidget(m);
      }
    }

    private void updateAnchorJS(final StringBuilder js) {
      char[] buf = new char[30];
      js.append("var o=")
          .append(this.container_.getJsRef())
          .append(";if(o){o.style.transform='translate(");
      if (this.anchorX_ >= 0) {
        js.append(MathUtils.roundJs(-this.anchorX_, 16)).append("px");
      } else {
        js.append("-50%");
      }
      js.append(',');
      if (this.anchorY_ >= 0) {
        js.append(MathUtils.roundJs(-this.anchorY_, 16)).append("px");
      } else {
        js.append("-50%");
      }
      js.append(")';}");
    }
  }
  /**
   * A standard leaflet marker.
   *
   * <p>See <a
   * href="https://leafletjs.com/reference.html#marker">https://leafletjs.com/reference.html#marker</a>
   */
  public static class LeafletMarker extends WLeafletMap.Marker {
    private static Logger logger = LoggerFactory.getLogger(LeafletMarker.class);

    /** Create a new marker at the given position. */
    public LeafletMarker(final WLeafletMap.Coordinate pos) {
      super(pos);
      this.options_ = new com.google.gson.JsonObject();
    }
    /**
     * Set the options of the marker.
     *
     * <p>Set the options that will be passed to the {@link WLeafletMap} marker.
     *
     * <p>See <a
     * href="https://leafletjs.com/reference.html#marker">https://leafletjs.com/reference.html#marker</a>
     * for the list of options.
     */
    public void setOptions(final com.google.gson.JsonObject options) {
      this.options_ = options;
    }

    protected void createItemJS(final StringBuilder ss, final StringBuilder anon2, long id) {
      String optionsStr = this.options_.toString();
      EscapeOStream es = new EscapeOStream(ss);
      es.append("L.marker([");
      char[] buf = new char[30];
      es.append(MathUtils.roundJs(this.getPosition().getLatitude(), 16)).append(",");
      es.append(MathUtils.roundJs(this.getPosition().getLongitude(), 16)).append("],");
      if (!this.options_.entrySet().isEmpty()) {
        es.append("JSON.parse('");
        es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
        es.append(optionsStr);
        es.popEscape();
        es.append("')");
      }
      ss.append(")");
    }

    private com.google.gson.JsonObject options_;
  }
  /** Create a new {@link WLeafletMap}. */
  public WLeafletMap(WContainerWidget parentContainer) {
    super();
    this.impl_ = null;
    this.options_ = new com.google.gson.JsonObject();
    this.flags_ = new BitSet();
    this.zoomLevelChanged_ = new JSignal1<Integer>(this, "zoomLevelChanged") {};
    this.panChanged_ = new JSignal2<Double, Double>(this, "panChanged") {};
    this.overlayItemToggled_ = new JSignal2<Long, Boolean>(this, "overlayItemToggled") {};
    this.position_ = new WLeafletMap.Coordinate();
    this.zoomLevel_ = 13;
    this.nextMarkerId_ = 0;
    this.nextActionSequenceNumber_ = 0;
    this.itemClicked_ = new JSignal1<Long>(this, "itemClicked") {};
    this.itemDblclicked_ = new JSignal1<Long>(this, "itemDblClicked") {};
    this.itemMousedown_ = new JSignal1<Long>(this, "itemMousedown") {};
    this.itemMouseup_ = new JSignal1<Long>(this, "itemMouseup") {};
    this.itemMouseover_ = new JSignal1<Long>(this, "itemMouseover") {};
    this.itemMouseout_ = new JSignal1<Long>(this, "itemMouseout") {};
    this.tileLayers_ = new ArrayList<WLeafletMap.TileLayer>();
    this.renderedTileLayersSize_ = 0;
    this.overlays_ = new ArrayList<WLeafletMap.Overlay>();
    this.renderedOverlaysSize_ = 0;
    this.mapItems_ = new ArrayList<WLeafletMap.ItemEntry>();
    this.setup();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Create a new {@link WLeafletMap}.
   *
   * <p>Calls {@link #WLeafletMap(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WLeafletMap() {
    this((WContainerWidget) null);
  }
  /**
   * Create a new {@link WLeafletMap} with the given options.
   *
   * <p>
   *
   * @see WLeafletMap#setOptions(com.google.gson.JsonObject options)
   */
  public WLeafletMap(final com.google.gson.JsonObject options, WContainerWidget parentContainer) {
    super();
    this.options_ = options;
    this.flags_ = new BitSet();
    this.zoomLevelChanged_ = new JSignal1<Integer>(this, "zoomLevelChanged") {};
    this.panChanged_ = new JSignal2<Double, Double>(this, "panChanged") {};
    this.overlayItemToggled_ = new JSignal2<Long, Boolean>(this, "overlayItemToggled") {};
    this.position_ = new WLeafletMap.Coordinate();
    this.zoomLevel_ = 13;
    this.nextMarkerId_ = 0;
    this.nextActionSequenceNumber_ = 0;
    this.itemClicked_ = new JSignal1<Long>(this, "itemClicked") {};
    this.itemDblclicked_ = new JSignal1<Long>(this, "itemDblClicked") {};
    this.itemMousedown_ = new JSignal1<Long>(this, "itemMousedown") {};
    this.itemMouseup_ = new JSignal1<Long>(this, "itemMouseup") {};
    this.itemMouseover_ = new JSignal1<Long>(this, "itemMouseover") {};
    this.itemMouseout_ = new JSignal1<Long>(this, "itemMouseout") {};
    this.tileLayers_ = new ArrayList<WLeafletMap.TileLayer>();
    this.renderedTileLayersSize_ = 0;
    this.overlays_ = new ArrayList<WLeafletMap.Overlay>();
    this.renderedOverlaysSize_ = 0;
    this.mapItems_ = new ArrayList<WLeafletMap.ItemEntry>();
    this.setup();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Create a new {@link WLeafletMap} with the given options.
   *
   * <p>Calls {@link #WLeafletMap(com.google.gson.JsonObject options, WContainerWidget
   * parentContainer) this(options, (WContainerWidget)null)}
   */
  public WLeafletMap(final com.google.gson.JsonObject options) {
    this(options, (WContainerWidget) null);
  }

  public void remove() {
    super.remove();
  }
  /**
   * Change the options of the WLeafletMap.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This fully rerenders the map, because it creates a new Leaflet map, so any
   * custom JavaScript modifying the map with e.g. {@link WCompositeWidget#doJavaScript(String js)
   * WCompositeWidget#doJavaScript()} is undone, much like reloading the page when <code>
   * reload-is-new-session</code> is set to false. </i> See <a
   * href="https://leafletjs.com/reference.html#map">https://leafletjs.com/reference.html#map</a>
   * for a list of options.
   */
  public void setOptions(final com.google.gson.JsonObject options) {
    this.options_ = options;
    this.flags_.set(BIT_OPTIONS_CHANGED);
    if (this.isRendered()) {
      for (int i = 0; i < this.mapItems_.size(); ++i) {
        if (!this.mapItems_.get(i).flags.get(ItemEntry.BIT_ADDED)
            && !this.mapItems_.get(i).flags.get(ItemEntry.BIT_REMOVED)) {
          this.mapItems_.get(i).mapItem.unrender();
        }
      }
    }
    this.scheduleRender();
  }
  /**
   * Add a new tile layer.
   *
   * <p>See <a
   * href="https://leafletjs.com/reference.html#tilelayer">https://leafletjs.com/reference.html#tilelayer</a>
   */
  public void addTileLayer(final String urlTemplate, final com.google.gson.JsonObject options) {
    WLeafletMap.TileLayer layer = new WLeafletMap.TileLayer();
    layer.urlTemplate = urlTemplate;
    layer.options = options;
    this.tileLayers_.add(layer);
    this.scheduleRender();
  }
  /** Add the given popup. */
  public void addPopup(WLeafletMap.Popup popup) {
    this.addItem(popup);
  }
  /** Remove the given popup. */
  public WLeafletMap.Popup removePopup(WLeafletMap.Popup popup) {
    return this.removePopup(popup, (WLeafletMap.Marker) null);
  }
  /** Add the given tooltip. */
  public void addTooltip(WLeafletMap.Tooltip tooltip) {
    this.addItem(tooltip);
  }
  /** Remove the given tooltip. */
  public WLeafletMap.Tooltip removeTooltip(WLeafletMap.Tooltip tooltip) {
    return this.removeTooltip(tooltip, (WLeafletMap.Marker) null);
  }
  /** Add the given marker. */
  public void addMarker(WLeafletMap.Marker marker) {
    this.addItem(marker);
  }
  /** Remove the given marker. */
  public WLeafletMap.Marker removeMarker(WLeafletMap.Marker marker) {
    return ObjectUtils.cast(this.removeItem(marker), Marker.class);
  }
  /**
   * Add a polyline.
   *
   * <p>This will draw a polyline on the map going through the given list of coordinates, with the
   * given pen.
   *
   * <p>See <a
   * href="https://leafletjs.com/reference.html#polyline">https://leafletjs.com/reference.html#polyline</a>
   */
  public void addPolyline(final List<WLeafletMap.Coordinate> points, final WPen pen) {
    this.overlays_.add(new Polyline(points, pen));
    this.scheduleRender();
  }
  /**
   * Add a circle.
   *
   * <p>This will draw a circle on the map centered at <code>center</code>, with the given <code>
   * radius</code> (in meters), drawn with the given <code>stroke</code> and <code>fill</code>.
   */
  public void addCircle(
      final WLeafletMap.Coordinate center, double radius, final WPen stroke, final WBrush fill) {
    this.overlays_.add(new Circle(center, radius, stroke, fill));
    this.scheduleRender();
  }
  /** Set the current zoom level. */
  public void setZoomLevel(int level) {
    this.zoomLevel_ = level;
    this.flags_.set(BIT_ZOOM_CHANGED);
    this.scheduleRender();
  }
  /** Get the current zoom level. */
  public int getZoomLevel() {
    return this.zoomLevel_;
  }
  /** Pan to the given coordinate. */
  public void panTo(final WLeafletMap.Coordinate center) {
    this.position_ = center;
    this.flags_.set(BIT_PAN_CHANGED);
    this.scheduleRender();
  }
  /** Get the current position. */
  public WLeafletMap.Coordinate getPosition() {
    return this.position_;
  }
  /** {@link Signal} emitted when the user has changed the zoom level of the map. */
  public JSignal1<Integer> zoomLevelChanged() {
    return this.zoomLevelChanged_;
  }
  /** {@link Signal} emitted when the user has panned the map. */
  public JSignal2<Double, Double> panChanged() {
    return this.panChanged_;
  }
  /**
   * Returns a JavaScript expression to the Leaflet map object.
   *
   * <p>You may want to use this in conjunction with {@link JSlot} or {@link
   * WCompositeWidget#doJavaScript(String js) WCompositeWidget#doJavaScript()} in custom JavaScript
   * code, e.g. to access features not built-in to WLeafletMap.
   */
  public String getMapJsRef() {
    return "((function(){var o="
        + this.getJsRef()
        + ";if(o&&o.wtObj){return o.wtObj.map;}return null;})())";
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (flags.contains(RenderFlag.Full) || this.flags_.get(BIT_OPTIONS_CHANGED)) {
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
    for (int i = 0; i < this.mapItems_.size(); ) {
      if (this.mapItems_.get(i).flags.get(ItemEntry.BIT_REMOVED)) {
        if (!this.flags_.get(BIT_OPTIONS_CHANGED)) {
          this.removeItemJS(ss, this.mapItems_.get(i).id);
        }
        this.mapItems_.remove(0 + i);
      } else {
        ++i;
      }
    }
    List<WLeafletMap.OrderedAction> orderedActions = new ArrayList<WLeafletMap.OrderedAction>();
    for (int add = 0; add < (this.nextActionSequenceNumber_); ++add) {
      orderedActions.add(new WLeafletMap.OrderedAction());
    }
    ;
    for (int i = 0; i < this.mapItems_.size(); ++i) {
      WLeafletMap.OrderedAction mapItemAction = this.mapItems_.get(i).mapItem.getOrderedAction();
      this.mapItems_.get(i).mapItem.resetOrderedAction();
      if (flags.contains(RenderFlag.Full)
          || this.flags_.get(BIT_OPTIONS_CHANGED)
          || this.mapItems_.get(i).flags.get(ItemEntry.BIT_ADDED)) {
        if (mapItemAction.type != WLeafletMap.OrderedAction.Type.Add) {
          this.addItemJS(ss, this.mapItems_.get(i));
        }
        this.mapItems_.get(i).flags.clear(ItemEntry.BIT_ADDED);
      } else {
        if (this.mapItems_.get(i).mapItem.isChanged()) {
          this.updateItemJS(ss, this.mapItems_.get(i));
        }
        if (this.mapItems_.get(i).mapItem.needsUpdate()) {
          this.mapItems_.get(i).mapItem.update(ss);
        }
      }
      if (mapItemAction.type != WLeafletMap.OrderedAction.Type.None) {
        mapItemAction.itemEntry = this.mapItems_.get(i);
        orderedActions.set(mapItemAction.sequenceNumber, mapItemAction);
      }
    }
    for (int i = 0; i < this.nextActionSequenceNumber_; ++i) {
      switch (orderedActions.get(i).type) {
        case Add:
          this.addItemJS(ss, orderedActions.get(i).itemEntry);
          break;
        case MoveFront:
          this.updateItemJS(ss, orderedActions.get(i).itemEntry, "moveOverlayItemToFront");
          break;
        case MoveBack:
          this.updateItemJS(ss, orderedActions.get(i).itemEntry, "moveOverlayItemToBack");
          break;
        default:
          break;
      }
    }
    if (!(ss.length() == 0)) {
      this.doJavaScript(ss.toString());
    }
    this.flags_.clear(BIT_OPTIONS_CHANGED);
    this.nextActionSequenceNumber_ = 0;
    super.render(flags);
  }

  private static final int BIT_ZOOM_CHANGED = 0;
  private static final int BIT_PAN_CHANGED = 1;
  private static final int BIT_OPTIONS_CHANGED = 2;
  private static final String WIDGETMARKER_CONTAINER_RULENAME =
      "WLeafletMap::WidgetMarker::container";
  private static final String WIDGETMARKER_CONTAINER_CHILDREN_RULENAME =
      "WLeafletMap::WidgetMarker::container-children";
  private WLeafletMap.Impl impl_;
  private com.google.gson.JsonObject options_;
  private BitSet flags_;
  private JSignal1<Integer> zoomLevelChanged_;
  private JSignal2<Double, Double> panChanged_;
  private JSignal2<Long, Boolean> overlayItemToggled_;
  private WLeafletMap.Coordinate position_;
  private int zoomLevel_;
  private long nextMarkerId_;
  private int nextActionSequenceNumber_;
  private JSignal1<Long> itemClicked_;
  private JSignal1<Long> itemDblclicked_;
  private JSignal1<Long> itemMousedown_;
  private JSignal1<Long> itemMouseup_;
  private JSignal1<Long> itemMouseover_;
  private JSignal1<Long> itemMouseout_;

  static class TileLayer {
    private static Logger logger = LoggerFactory.getLogger(TileLayer.class);

    public String urlTemplate;
    public com.google.gson.JsonObject options;
  }

  private List<WLeafletMap.TileLayer> tileLayers_;
  private int renderedTileLayersSize_;
  private List<WLeafletMap.Overlay> overlays_;
  private int renderedOverlaysSize_;

  static class ItemEntry {
    private static Logger logger = LoggerFactory.getLogger(ItemEntry.class);

    public static final int BIT_ADDED = 0;
    public static final int BIT_REMOVED = 1;

    public ItemEntry() {
      this.uMapItem = (WLeafletMap.AbstractMapItem) null;
      this.mapItem = null;
      this.parent = null;
      this.id = -1;
      this.flags = new BitSet();
    }

    public WLeafletMap.AbstractMapItem uMapItem;
    public WLeafletMap.AbstractMapItem mapItem;
    public WLeafletMap.ItemEntry parent;
    public long id;
    public BitSet flags;
  }

  private List<WLeafletMap.ItemEntry> mapItems_;

  private void setup() {
    this.setImplementation(this.impl_ = new WLeafletMap.Impl());
    this.zoomLevelChanged()
        .addListener(
            this,
            (Integer e1) -> {
              WLeafletMap.this.handleZoomLevelChanged(e1);
            });
    this.panChanged()
        .addListener(
            this,
            (Double e1, Double e2) -> {
              WLeafletMap.this.handlePanChanged(e1, e2);
            });
    this.overlayItemToggled_.addListener(
        this,
        (Long e1, Boolean e2) -> {
          WLeafletMap.this.handleOverlayItemToggled(e1, e2);
        });
    this.itemClicked_.addListener(
        this,
        (Long e1) -> {
          WLeafletMap.this.handleItemClicked(e1);
        });
    this.itemDblclicked_.addListener(
        this,
        (Long e1) -> {
          WLeafletMap.this.handleItemDblClicked(e1);
        });
    this.itemMousedown_.addListener(
        this,
        (Long e1) -> {
          WLeafletMap.this.handleItemMousedown(e1);
        });
    this.itemMouseup_.addListener(
        this,
        (Long e1) -> {
          WLeafletMap.this.handleItemMouseup(e1);
        });
    this.itemMouseover_.addListener(
        this,
        (Long e1) -> {
          WLeafletMap.this.handleItemMouseover(e1);
        });
    this.itemMouseout_.addListener(
        this,
        (Long e1) -> {
          WLeafletMap.this.handleItemMouseout(e1);
        });
    WApplication app = WApplication.getInstance();
    if (app != null) {
      if (!app.getStyleSheet().isDefined(WIDGETMARKER_CONTAINER_RULENAME)) {
        app.getStyleSheet()
            .addRule(
                ".Wt-leaflet-widgetmarker-container",
                "transform: translate(-50%, -50%);",
                WIDGETMARKER_CONTAINER_RULENAME);
      }
      if (!app.getStyleSheet().isDefined(WIDGETMARKER_CONTAINER_CHILDREN_RULENAME)) {
        app.getStyleSheet()
            .addRule(
                ".Wt-leaflet-widgetmarker-container > *",
                "pointer-events: auto;",
                WIDGETMARKER_CONTAINER_CHILDREN_RULENAME);
      }
      String leafletJSURL = "";
      String leafletCSSURL = "";
      leafletJSURL = WApplication.readConfigurationProperty("leafletJSURL", leafletJSURL);
      leafletCSSURL = WApplication.readConfigurationProperty("leafletCSSURL", leafletCSSURL);
      if (leafletJSURL.length() != 0 && leafletCSSURL.length() != 0) {
        app.require(leafletJSURL);
        app.useStyleSheet(new WLink(leafletCSSURL));
      } else {
        throw new WException(
            "Trying to create a WLeafletMap, but the leafletJSURL and/or leafletCSSURL properties are not configured");
      }
    } else {
      throw new WException("Trying to create a WLeafletMap without an active WApplication");
    }
  }

  private void defineJavaScript() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WLeafletMap.js", wtjs1());
    String optionsStr = this.options_.toString();
    StringBuilder ss = new StringBuilder();
    EscapeOStream es = new EscapeOStream(ss);
    es.append("new Wt4_12_1.WLeafletMap(")
        .append(app.getJavaScriptClass())
        .append(",")
        .append(this.getJsRef())
        .append(",'");
    es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
    es.append(optionsStr);
    es.popEscape();
    es.append("',");
    char[] buf = new char[30];
    es.append(MathUtils.roundJs(this.position_.getLatitude(), 16)).append(",");
    es.append(MathUtils.roundJs(this.position_.getLongitude(), 16)).append(",");
    es.append(MathUtils.roundJs(this.zoomLevel_, 16)).append(",");
    es.append(app.getEnvironment().getServer().getConfiguration().getDoubleClickTimeout())
        .append(");");
    this.setJavaScriptMember(" WLeafletMap", ss.toString());
    this.setJavaScriptMember(WT_RESIZE_JS, this.getJsRef() + ".wtObj.wtResize");
  }

  private void addTileLayerJS(final StringBuilder ss, final WLeafletMap.TileLayer layer) {
    String optionsStr = layer.options.toString();
    EscapeOStream es = new EscapeOStream(ss);
    es.append("var o=").append(this.getJsRef()).append(";if(o && o.wtObj){o.wtObj.addTileLayer('");
    es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
    es.append(layer.urlTemplate);
    es.popEscape();
    es.append("','");
    es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
    es.append(optionsStr);
    es.popEscape();
    es.append("');}");
  }

  private void panToJS(final StringBuilder ss, final WLeafletMap.Coordinate position) {
    ss.append("var o=").append(this.getJsRef()).append(";if(o && o.wtObj){o.wtObj.panTo(");
    char[] buf = new char[30];
    ss.append(MathUtils.roundJs(position.getLatitude(), 16)).append(",");
    ss.append(MathUtils.roundJs(position.getLongitude(), 16)).append(");}");
  }

  private void zoomJS(final StringBuilder ss, int level) {
    ss.append("var o=")
        .append(this.getJsRef())
        .append(";if(o && o.wtObj){o.wtObj.zoom(")
        .append(level)
        .append(");}");
  }

  private WLeafletMap.AbstractMapItem getItem(long id) {
    for (int i = 0; i < this.mapItems_.size(); ++i) {
      if (this.mapItems_.get(i).id == id) {
        return this.mapItems_.get(i).mapItem;
      }
    }
    return null;
  }

  private void addItem(WLeafletMap.AbstractMapItem mapItem, WLeafletMap.Marker parent) {
    WLeafletMap.ItemEntry parentEntry = null;
    for (int i = 0; i < this.mapItems_.size(); ++i) {
      if (this.mapItems_.get(i).uMapItem == mapItem
          && this.mapItems_.get(i).flags.get(ItemEntry.BIT_REMOVED)
          && (!(this.mapItems_.get(i).parent != null || parent != null)
              || this.mapItems_.get(i).parent != null
                  && this.mapItems_.get(i).parent.uMapItem == parent)) {
        this.mapItems_.get(i).uMapItem = mapItem;
        this.mapItems_.get(i).flags.clear(ItemEntry.BIT_REMOVED);
        this.mapItems_.get(i).uMapItem.setMap(this);
        return;
      } else {
        if (this.mapItems_.get(i).mapItem == parent) {
          parentEntry = this.mapItems_.get(i);
        }
      }
    }
    WLeafletMap.ItemEntry entry = new ItemEntry();
    entry.uMapItem = mapItem;
    entry.mapItem = entry.uMapItem;
    entry.parent = parentEntry;
    entry.flags.set(ItemEntry.BIT_ADDED);
    entry.id = this.nextMarkerId_;
    ++this.nextMarkerId_;
    entry.mapItem.orderedAction_.type = WLeafletMap.OrderedAction.Type.Add;
    entry.mapItem.orderedAction_.sequenceNumber = this.getNextActionSequenceNumber();
    WLeafletMap.ItemEntry entryPtr = entry;
    this.mapItems_.add(entry);
    entryPtr.mapItem.setMap(this);
    this.scheduleRender();
  }

  private final void addItem(WLeafletMap.AbstractMapItem mapItem) {
    addItem(mapItem, (WLeafletMap.Marker) null);
  }

  private WLeafletMap.AbstractMapItem removeItem(
      WLeafletMap.AbstractMapItem mapItem, WLeafletMap.Marker parent) {
    for (int i = 0; i < this.mapItems_.size(); ++i) {
      if (this.mapItems_.get(i).uMapItem == mapItem && this.mapItems_.get(i).mapItem == mapItem) {
        if ((this.mapItems_.get(i).parent != null || parent != null)
            && (!(this.mapItems_.get(i).parent != null)
                || this.mapItems_.get(i).parent.mapItem != parent)) {
          return null;
        }
        mapItem.setMap((WLeafletMap) null);
        WLeafletMap.AbstractMapItem result = this.mapItems_.get(i).uMapItem;
        if (this.mapItems_.get(i).flags.get(ItemEntry.BIT_ADDED)) {
          this.mapItems_.remove(0 + i);
          return result;
        }
        this.mapItems_.get(i).flags.set(ItemEntry.BIT_REMOVED);
        this.scheduleRender();
        return result;
      }
    }
    return null;
  }

  private final WLeafletMap.AbstractMapItem removeItem(WLeafletMap.AbstractMapItem mapItem) {
    return removeItem(mapItem, (WLeafletMap.Marker) null);
  }

  private WLeafletMap.Popup removePopup(WLeafletMap.Popup popup, WLeafletMap.Marker parent) {
    return ObjectUtils.cast(this.removeItem(popup, parent), Popup.class);
  }

  private WLeafletMap.Tooltip removeTooltip(
      WLeafletMap.Tooltip tooltip, WLeafletMap.Marker parent) {
    return ObjectUtils.cast(this.removeItem(tooltip, parent), Tooltip.class);
  }

  private void addItemJS(final StringBuilder ss, final WLeafletMap.ItemEntry entry) {
    StringBuilder js = new StringBuilder();
    ss.append("var o=")
        .append(this.getJsRef())
        .append(";if(o && o.wtObj){o.wtObj.")
        .append(entry.mapItem.getAddFunctionJs());
    ss.append("(").append(entry.id).append(",");
    if (entry.parent != null) {
      ss.append(entry.parent.id).append(",");
    } else {
      ss.append(-1).append(",");
    }
    entry.mapItem.createItemJS(ss, js, entry.id);
    ss.append(");");
    ss.append(js.toString());
    ss.append("}");
  }

  private void removeItemJS(final StringBuilder ss, long id) {
    ss.append("var o=")
        .append(this.getJsRef())
        .append(";if(o && o.wtObj){")
        .append("o.wtObj.removeMapItem(")
        .append(id)
        .append(");}");
  }

  private void updateItemJS(final StringBuilder ss, final WLeafletMap.ItemEntry entry) {
    ss.append("var o=").append(this.getJsRef()).append(";if(o && o.wtObj){");
    entry.mapItem.applyChangeJS(ss, entry.id);
    ss.append("}");
  }

  private void updateItemJS(
      final StringBuilder ss, final WLeafletMap.ItemEntry entry, final String fname) {
    ss.append("var o=").append(this.getJsRef()).append(";if(o && o.wtObj){");
    ss.append("o.wtObj.").append(fname).append("(").append(entry.id).append(");");
    ss.append("}");
  }

  private void handlePanChanged(double latitude, double longitude) {
    this.position_ = new WLeafletMap.Coordinate(latitude, longitude);
  }

  private void handleZoomLevelChanged(int zoomLevel) {
    this.zoomLevel_ = zoomLevel;
  }

  private void handleOverlayItemToggled(long id, boolean open) {
    WLeafletMap.AbstractOverlayItem overlayItem =
        ObjectUtils.cast(this.getItem(id), WLeafletMap.AbstractOverlayItem.class);
    if (overlayItem != null && overlayItem.open_ != open) {
      overlayItem.open_ = open;
      if (open) {
        overlayItem.opened().trigger();
      } else {
        overlayItem.closed().trigger();
      }
    }
  }

  private int getNextActionSequenceNumber() {
    int result = this.nextActionSequenceNumber_;
    this.nextActionSequenceNumber_++;
    return result;
  }

  private void handleItemClicked(long id) {
    WLeafletMap.AbstractMapItem item = this.getItem(id);
    if (item != null) {
      item.clicked().trigger();
    }
  }

  private void handleItemDblClicked(long id) {
    WLeafletMap.AbstractMapItem item = this.getItem(id);
    if (item != null) {
      item.doubleClicked().trigger();
    }
  }

  private void handleItemMousedown(long id) {
    WLeafletMap.AbstractMapItem item = this.getItem(id);
    if (item != null) {
      item.mouseWentDown().trigger();
    }
  }

  private void handleItemMouseup(long id) {
    WLeafletMap.AbstractMapItem item = this.getItem(id);
    if (item != null) {
      item.mouseWentUp().trigger();
    }
  }

  private void handleItemMouseover(long id) {
    WLeafletMap.AbstractMapItem item = this.getItem(id);
    if (item != null) {
      item.mouseWentOver().trigger();
    }
  }

  private void handleItemMouseout(long id) {
    WLeafletMap.AbstractMapItem item = this.getItem(id);
    if (item != null) {
      item.mouseWentOut().trigger();
    }
  }

  private static void addPathOptions(
      final com.google.gson.JsonObject options, final WPen stroke, final WBrush fill) {
    if (stroke.getStyle() != PenStyle.None) {
      options.add("stroke", (new com.google.gson.JsonPrimitive(true)));
      options.add(
          "color", (new com.google.gson.JsonPrimitive(stroke.getColor().getCssText(false))));
      options.add(
          "opacity", (new com.google.gson.JsonPrimitive(stroke.getColor().getAlpha() / 255.0)));
      double weight = stroke.getWidth().toPixels();
      weight = weight == 0 ? 1.0 : weight;
      options.add("weight", (new com.google.gson.JsonPrimitive(weight)));
      String capStyle = "";
      switch (stroke.getCapStyle()) {
        case Flat:
          capStyle = "butt";
          break;
        case Square:
          capStyle = "square";
          break;
        case Round:
          capStyle = "round";
      }
      options.add("lineCap", (new com.google.gson.JsonPrimitive(capStyle)));
      String joinStyle = "";
      switch (stroke.getJoinStyle()) {
        case Bevel:
          joinStyle = "bevel";
          break;
        case Miter:
          joinStyle = "miter";
          break;
        case Round:
          joinStyle = "round";
      }
      options.add("lineJoin", (new com.google.gson.JsonPrimitive(joinStyle)));
    } else {
      options.add("stroke", (new com.google.gson.JsonPrimitive(false)));
    }
    if (fill.getStyle() != BrushStyle.None) {
      options.add("fill", (new com.google.gson.JsonPrimitive(true)));
      options.add(
          "fillColor", (new com.google.gson.JsonPrimitive(fill.getColor().getCssText(false))));
      options.add(
          "fillOpacity", (new com.google.gson.JsonPrimitive(fill.getColor().getAlpha() / 255.0)));
    } else {
      options.add("fill", (new com.google.gson.JsonPrimitive(false)));
    }
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WLeafletMap",
        "(function(t,e,o,n,i,p,l){e.wtObj&&e.wtObj.map.remove();e.wtObj=this;const a=this;this.map=null;const m={};let d=p,c=[n,i];this.addTileLayer=function(t,e){const o=JSON.parse(e);L.tileLayer(t,o).addTo(a.map)};this.zoom=function(t){d=t;a.map.setZoom(t)};this.panTo=function(t,e){c=[t,e];a.map.panTo([t,e])};this.addPolyline=function(t,e){const o=JSON.parse(e);L.polyline(t,o).addTo(a.map)};this.addCircle=function(t,e){const o=JSON.parse(e);L.circle(t,o).addTo(a.map)};function s(o,n){n.doubleClickTimeout=null;n.on(\"click\",(function(){!function(o,n){if(n.doubleClickTimeout){clearTimeout(n.doubleClickTimeout);n.doubleClickTimeout=null;t.emit(e,\"itemDblClicked\",o)}else n.doubleClickTimeout=setTimeout((function(){n.doubleClickTimeout=null;t.emit(e,\"itemClicked\",o)}),l)}(o,n)}));n.on(\"mousedown\",(function(){t.emit(e,\"itemMousedown\",o)}));n.on(\"mouseup\",(function(){t.emit(e,\"itemMouseup\",o)}));n.on(\"mouseover\",(function(){t.emit(e,\"itemMouseover\",o)}));n.on(\"mouseout\",(function(){t.emit(e,\"itemMouseout\",o)}))}this.addPopup=function(t,e,o){o.item_type=1;a.addOverlayItem(t,e,o)};this.addTooltip=function(t,e,o){o.item_type=0;a.addOverlayItem(t,e,o)};this.addOverlayItem=function(t,o,n){s(t,n);n.contentHolder=document.createElement(\"div\");n.contentHolder.style.cssText=\"visibility: hidden;\";e.appendChild(n.contentHolder);n.item_id=t;n.parent_id=o;m[t]=n;a.toggleOverlayItem(t,!0)};this.addMapItem=function(t,e,o){s(t,o);o.addTo(a.map);m[t]=o};this.removeMapItem=function(t){const e=m[t];if(e){const o=e.parent_id;if((o||0===o)&&-1!==o){const e=m[o];e&&(e.getPopup()&&e.getPopup().item_id===t?e.unbindPopup():e.getTooltip()&&e.getTooltip().item_id===t&&e.unbindTooltip())}e.contentHolder&&e.contentHolder.remove();a.map.removeLayer(e);delete m[t]}};this.moveMapItem=function(t,e){const o=m[t];o&&o.setLatLng(e)};this.setOverlayItemContent=function(t,e,o){const n=m[t];if(n){n.contentId=o;n.contentHolder.innerHTML=e;if(n.isOpen()){const t=n.contentHolder.firstChild;n.setContent(n.contentHolder.removeChild(t))}a.delayedJS()}};this.moveOverlayItemToFront=function(t){const e=m[t];e&&e.bringToFront()};this.moveOverlayItemToBack=function(t){const e=m[t];e&&e.bringToBack()};this.toggleOverlayItem=function(t,e){const o=m[t];if(o)if(e&&!o.isOpen()){const t=o.parent_id;if(-1===t)o.openOn(this.map);else{const e=m[t];e&&(1===o.item_type?e.bindPopup(o).openPopup():0===o.item_type&&e.bindTooltip(o).openTooltip())}}else!e&&o.isOpen()&&o.close()};this.wtResize=function(){a.map.invalidateSize()};e.wtEncodeValue=function(){const t=a.map.getCenter(),e=[t.lat,t.lng],o=a.map.getZoom();return JSON.stringify({position:e,zoom:o})};this.init=function(o,n,i){const p=JSON.parse(o);p.center=n;p.zoom=i;a.map=L.map(e,p);const l=parseInt(function(){let t=e.parentNode;for(;t;){if(t.wtPopup)return t.style.zIndex;t=t.parentNode}return 0}(),10);if(l>0){a.map.getPane(\"tilePane\").style.zIndex=l+200;a.map.getPane(\"overlayPane\").style.zIndex=l+400;a.map.getPane(\"shadowPane\").style.zIndex=l+500;a.map.getPane(\"markerPane\").style.zIndex=l+600;a.map.getPane(\"tooltipPane\").style.zIndex=l+650;a.map.getPane(\"popupPane\").style.zIndex=l+700}a.map.on(\"zoomend\",(function(){const o=a.map.getZoom();if(o!==d){t.emit(e,\"zoomLevelChanged\",o);d=o}}));a.map.on(\"moveend\",(function(){const o=a.map.getCenter();if(o.lat!==c[0]||o.lng!==c[1]){t.emit(e,\"panChanged\",o.lat,o.lng);c=[o.lat,o.lng]}}));a.map.on(\"popupclose\",(function(o){const n=o.popup.getContent();n.remove();o.popup.contentHolder.appendChild(n);t.emit(e,\"overlayItemToggled\",o.popup.item_id,!1)}));a.map.on(\"popupopen\",(function(o){const n=o.popup.contentHolder.firstChild;n&&o.popup.setContent(o.popup.contentHolder.removeChild(n));t.emit(e,\"overlayItemToggled\",o.popup.item_id,!0)}));a.map.on(\"tooltipclose\",(function(o){const n=o.tooltip.getContent();n.remove();o.tooltip.contentHolder.appendChild(n);t.emit(e,\"overlayItemToggled\",o.tooltip.item_id,!1)}));a.map.on(\"tooltipopen\",(function(o){const n=o.tooltip.contentHolder.firstChild;n&&o.tooltip.setContent(o.tooltip.contentHolder.removeChild(n));t.emit(e,\"overlayItemToggled\",o.tooltip.item_id,!0)}))};this.init(o,[n,i],p)})");
  }

  static class Impl extends WWebWidget {
    private static Logger logger = LoggerFactory.getLogger(Impl.class);

    Impl(WContainerWidget parentContainer) {
      super();
      this.setInline(false);
      if (parentContainer != null) parentContainer.addWidget(this);
    }

    public Impl() {
      this((WContainerWidget) null);
    }

    DomElementType getDomElementType() {
      return DomElementType.DIV;
    }
  }

  abstract static class Overlay {
    private static Logger logger = LoggerFactory.getLogger(Overlay.class);

    abstract void addJS(final StringBuilder ss, WLeafletMap map);

    Overlay() {}
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
      if (this.pen.getStyle() == PenStyle.None) {
        return;
      }
      com.google.gson.JsonObject options = new com.google.gson.JsonObject();
      addPathOptions(options, this.pen, new WBrush(BrushStyle.None));
      String optionsStr = options.toString();
      EscapeOStream es = new EscapeOStream(ss);
      es.append("var o=").append(map.getJsRef()).append(";if(o && o.wtObj){o.wtObj.addPolyline(");
      es.append("[");
      for (int i = 0; i < this.points.size(); ++i) {
        if (i != 0) {
          es.append(',');
        }
        es.append("[");
        char[] buf = new char[30];
        es.append(MathUtils.roundJs(this.points.get(i).getLatitude(), 16)).append(",");
        es.append(MathUtils.roundJs(this.points.get(i).getLongitude(), 16));
        es.append("]");
      }
      es.append("],'");
      es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
      es.append(optionsStr);
      es.popEscape();
      es.append("');}");
    }
  }

  static class Circle extends WLeafletMap.Overlay {
    private static Logger logger = LoggerFactory.getLogger(Circle.class);

    public WLeafletMap.Coordinate center;
    public double radius;
    public WPen stroke;
    public WBrush fill;

    Circle(
        final WLeafletMap.Coordinate center, double radius, final WPen stroke, final WBrush fill) {
      super();
      this.center = center;
      this.radius = radius;
      this.stroke = stroke;
      this.fill = fill;
    }

    void addJS(final StringBuilder ss, WLeafletMap map) {
      com.google.gson.JsonObject options = new com.google.gson.JsonObject();
      options.add("radius", (new com.google.gson.JsonPrimitive(this.radius)));
      addPathOptions(options, this.stroke, this.fill);
      String optionsStr = options.toString();
      EscapeOStream es = new EscapeOStream(ss);
      es.append("var o=")
          .append(map.getJsRef())
          .append(";if(o && o.wtObj){")
          .append("o.wtObj.addCircle([");
      char[] buf = new char[30];
      es.append(MathUtils.roundJs(this.center.getLatitude(), 16)).append(",");
      es.append(MathUtils.roundJs(this.center.getLongitude(), 16)).append("],'");
      es.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
      es.append(optionsStr);
      es.popEscape();
      es.append("');}");
    }
  }
}
