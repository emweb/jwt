/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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
 * A widget that is painted using vector graphics.
 *
 * <p>A painted widget is rendered from basic drawing primitives. Rendering is done not on the
 * server but on the browser, using different rendering methods:
 *
 * <p>
 *
 * <table border="1" cellspacing="3" cellpadding="3">
 * <tr><td><b>Browser</b></td><td><b>Methods</b> </td><td><b>Default method</b> </td></tr>
 * <tr><td>Firefox 1.5+</td><td>HtmlCanvas, InlineSVG, PngImage </td><td>HtmlCanvas </td></tr>
 * <tr><td>Internet Explorer 6.0+</td><td>InlineVML, PngImage </td><td>InlineVML </td></tr>
 * <tr><td>Internet Explorer 9+</td><td>HtmlCanvas, InlineSVG, PngImage </td><td>HtmlCanvas </td></tr>
 * <tr><td>Safari</td><td>HtmlCanvas, InlineSVG, PngImage </td><td>HtmlCanvas </td></tr>
 * <tr><td>Opera</td><td>InlineSVG, HtmlCanvas*, PngImage </td><td>InlineSVG </td></tr>
 * <tr><td>other</td><td>?</td><td>HtmlCanvas, PngImage </td></tr>
 * </table>
 *
 * <p><i>* HtmlCanvas occasionally suffers from rendering artefacts in Opera.</i>
 *
 * <p>The different rendering methods correspond to different {@link WPaintDevice} implementations,
 * from which this widget choses a suitable one depending on the browser capabilities and
 * configuration.
 *
 * <p>If no JavaScript is available, the JavaScript-based HtmlCanvas will not be used, and InlineSVG
 * will be used instead. The method used may be changed by using {@link
 * WPaintedWidget#setPreferredMethod(RenderMethod method) setPreferredMethod()}.
 *
 * <p>In some browsers, InlineSVG requires that the document is rendered as XHTML. This must be
 * enabled in the configuration file using the <code>&lt;send-xhtml-mime-type&gt;</code> option. By
 * default, this option is off. Firefox 4 and Chrome do support svg in normal html mode.
 *
 * <p>The PngImage is the most portable rendering method, and may be the fastest if the painting is
 * of high complexity and/or the image is fairly small.
 *
 * <p>To use a WPaintedWidget, you must derive from it and reimplement {@link
 * WPaintedWidget#paintEvent(WPaintDevice paintDevice) paintEvent()}. To paint on a {@link
 * WPaintDevice}, you will need to use a {@link WPainter}. Repainting is triggered by calling the
 * {@link WPaintedWidget#update(EnumSet flags) update()} method.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>Styling through CSS is not applicable.
 *
 * <p>
 *
 * <p><i><b>Note: </b>A WPaintedWidget requires that it is given a size using {@link
 * WPaintedWidget#resize(WLength width, WLength height) resize()} or by a layout manager.</i>
 *
 * <h3>Client side interaction and repainting</h3>
 *
 * <p>If the widget is drawn as an HTML canvas element, i.e. the {@link WPaintedWidget#getMethod()
 * method} is HtmlCanvas, a WPaintedWidget can expose certain objects to be modified client side.
 *
 * <p>
 *
 * @see WJavaScriptHandle
 * @see WJavaScriptExposableObject
 * @see WImage
 */
public abstract class WPaintedWidget extends WInteractWidget {
  private static Logger logger = LoggerFactory.getLogger(WPaintedWidget.class);

  /** Create a new painted widget. */
  public WPaintedWidget(WContainerWidget parentContainer) {
    super();
    this.preferredMethod_ = RenderMethod.HtmlCanvas;
    this.painter_ = null;
    this.needRepaint_ = false;
    this.sizeChanged_ = false;
    this.areaImageAdded_ = false;
    this.repaintFlags_ = EnumSet.noneOf(PaintFlag.class);
    this.areaImage_ = null;
    this.renderWidth_ = 0;
    this.renderHeight_ = 0;
    this.repaintSlot_ =
        new JSlot("function() {var o=" + this.getObjJsRef() + ";if(o){o.repaint();}}", this);
    this.jsObjects_ = new WJavaScriptObjectStorage(this);
    this.jsDefined_ = false;
    if (WApplication.getInstance() != null) {
      final WEnvironment env = WApplication.getInstance().getEnvironment();
      if (env.agentIsOpera() && env.getUserAgent().indexOf("Mac OS X") == -1) {
        this.preferredMethod_ = RenderMethod.InlineSvgVml;
      }
    }
    this.setInline(false);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Create a new painted widget.
   *
   * <p>Calls {@link #WPaintedWidget(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WPaintedWidget() {
    this((WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {
    {
      WWidget oldWidget = this.areaImage_;
      this.areaImage_ = null;
      {
        WWidget toRemove = this.manageWidget(oldWidget, this.areaImage_);
        if (toRemove != null) toRemove.remove();
      }
    }
    super.remove();
  }
  /**
   * Sets the preferred rendering method.
   *
   * <p>When <code>method</code> is supported by the browser, then it is chosen for rendering.
   */
  public void setPreferredMethod(RenderMethod method) {
    if (this.preferredMethod_ != method) {
      this.painter_ = null;
      this.preferredMethod_ = method;
    }
  }
  /** Returns the preferred rendering method. */
  public RenderMethod getPreferredMethod() {
    return this.preferredMethod_;
  }
  /**
   * Lets the widget repaint itself.
   *
   * <p>Repainting is not immediate, but happens after when the event loop is exited.
   *
   * <p>Unless a {@link PaintFlag#Update} paint flag is set, the widget is first cleared.
   */
  public void update(EnumSet<PaintFlag> flags) {
    this.needRepaint_ = true;
    this.repaintFlags_.addAll(flags);
    this.repaint();
  }
  /**
   * Lets the widget repaint itself.
   *
   * <p>Calls {@link #update(EnumSet flags) update(EnumSet.of(flag, flags))}
   */
  public final void update(PaintFlag flag, PaintFlag... flags) {
    update(EnumSet.of(flag, flags));
  }
  /**
   * Lets the widget repaint itself.
   *
   * <p>Calls {@link #update(EnumSet flags) update(EnumSet.noneOf(PaintFlag.class))}
   */
  public final void update() {
    update(EnumSet.noneOf(PaintFlag.class));
  }

  public void resize(final WLength width, final WLength height) {
    if (!width.isAuto() && !height.isAuto()) {
      this.setLayoutSizeAware(false);
      this.resizeCanvas((int) width.toPixels(), (int) height.toPixels());
    }
    super.resize(width, height);
  }
  /**
   * Adds an interactive area.
   *
   * <p>Adds the <code>area</code> which listens to events in a specific region of the widget. Areas
   * are organized in a list, to which the given <code>area</code> is appended. When areas overlap,
   * the area with the lowest index receives the event.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When defining at least one area, no more events will propagate to the widget
   * itself. As a work-around, you can emulate this by listening for events on a {@link WRectArea}
   * that corresponds to the whole widget, and which is added as the last area (catching all events
   * that were not caught by preceding areas).</i>
   */
  public void addArea(WAbstractArea area) {
    this.createAreaImage();
    this.areaImage_.addArea(area);
  }
  /**
   * Inserts an interactive area.
   *
   * <p>Inserts the <code>area</code> which listens to events in the coresponding area of the
   * widget. Areas are organized in a list, and the <i>area</i> is inserted at index <code>index
   * </code>. When areas overlap, the area with the lowest index receives the event.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When defining at least one area, no more events will propagate to the widget
   * itself. As a work-around, you can emulate this by listening for events on a {@link WRectArea}
   * that corresponds to the whole widget, and which is added as the last area (catching all events
   * that were not caught by preceding areas).</i>
   */
  public void insertArea(int index, WAbstractArea area) {
    this.createAreaImage();
    this.areaImage_.insertArea(index, area);
  }
  /**
   * Removes an interactive area.
   *
   * <p>Removes the <code>area</code> from this widget.
   *
   * <p>
   */
  public WAbstractArea removeArea(WAbstractArea area) {
    this.createAreaImage();
    return this.areaImage_.removeArea(area);
  }
  /**
   * Returns the interactive area at the given index.
   *
   * <p>Returns <code>null</code> if <code>index</code> was invalid.
   *
   * <p>
   */
  public WAbstractArea getArea(int index) {
    return this.areaImage_ != null ? this.areaImage_.getArea(index) : null;
  }
  /**
   * Returns the interactive areas set for this widget.
   *
   * <p>
   *
   * @see WPaintedWidget#addArea(WAbstractArea area)
   */
  public List<WAbstractArea> getAreas() {
    return this.areaImage_ != null
        ? this.areaImage_.getAreas()
        : (List<WAbstractArea>) new ArrayList<WAbstractArea>();
  }
  /**
   * A JavaScript slot that repaints the widget when triggered.
   *
   * <p>This is useful for client-side initiated repaints. You may want to use this if you want to
   * add interaction or animation to your {@link WPaintedWidget}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This feature is currently only supported if the {@link
   * WPaintedWidget#getMethod() method} is HtmlCanvas. This will not cause a server roundtrip.
   * Instead, the resulting JavaScript of {@link WPaintedWidget#paintEvent(WPaintDevice paintDevice)
   * paintEvent()} will be re-executed on the client side.</i>
   *
   * @see WPaintedWidget#getObjJsRef()
   */
  public JSlot getRepaintSlot() {
    return this.repaintSlot_;
  }
  /**
   * Create a {@link WTransform} that is accessible from JavaScript, associated with this
   * WPaintedWidget.
   */
  protected WJavaScriptHandle<WTransform> createJSTransform() {
    return this.jsObjects_.addObject(new WTransform());
  }
  /**
   * Create a {@link WBrush} that is accessible from JavaScript, associated with this
   * WPaintedWidget.
   */
  protected WJavaScriptHandle<WBrush> createJSBrush() {
    return this.jsObjects_.addObject(new WBrush());
  }
  /**
   * Create a {@link WPen} that is accessible from JavaScript, associated with this WPaintedWidget.
   */
  protected WJavaScriptHandle<WPen> createJSPen() {
    return this.jsObjects_.addObject(new WPen());
  }
  /**
   * Create a {@link WPainterPath} that is accessible from JavaScript, associated with this
   * WPaintedWidget.
   */
  protected WJavaScriptHandle<WPainterPath> createJSPainterPath() {
    return this.jsObjects_.addObject(new WPainterPath());
  }
  /**
   * Create a {@link WRectF} that is accessible from JavaScript, associated with this
   * WPaintedWidget.
   */
  protected WJavaScriptHandle<WRectF> createJSRect() {
    return this.jsObjects_.addObject(new WRectF(0, 0, 0, 0));
  }
  /**
   * Create a {@link WPointF} that is accessible from JavaScript, associated with this
   * WPaintedWidget.
   */
  protected WJavaScriptHandle<WPointF> createJSPoint() {
    return this.jsObjects_.addObject(new WPointF(0, 0));
  }

  protected void layoutSizeChanged(int width, int height) {
    this.resizeCanvas(width, height);
  }
  /**
   * Returns the actual method used for rendering.
   *
   * <p>The default method considers browser capabilites and the preferred method to make an actual
   * choice for the implementation.
   *
   * <p>You may want to reimplement this method to override this choice.
   */
  protected RenderMethod getMethod() {
    final WEnvironment env = WApplication.getInstance().getEnvironment();
    RenderMethod method;
    if (this.preferredMethod_ == RenderMethod.PngImage) {
      return RenderMethod.PngImage;
    }
    if (env.agentIsIElt(9)) {
      method =
          this.preferredMethod_ == RenderMethod.InlineSvgVml
              ? RenderMethod.InlineSvgVml
              : RenderMethod.PngImage;
    } else {
      if (!(env.agentIsChrome()
              && (int) env.getAgent().getValue() >= (int) UserAgent.Chrome5.getValue()
          || env.agentIsGecko()
              && (int) env.getAgent().getValue() >= (int) UserAgent.Firefox4_0.getValue())) {
        method = env.hasJavaScript() ? RenderMethod.HtmlCanvas : RenderMethod.PngImage;
      } else {
        if (!env.hasJavaScript()) {
          method = RenderMethod.InlineSvgVml;
        } else {
          boolean oldFirefoxMac =
              (env.getUserAgent().indexOf("Firefox/1.5") != -1
                      || env.getUserAgent().indexOf("Firefox/2.0") != -1)
                  && env.getUserAgent().indexOf("Macintosh") != -1;
          if (oldFirefoxMac) {
            method = RenderMethod.HtmlCanvas;
          } else {
            method =
                this.preferredMethod_ == RenderMethod.PngImage
                    ? RenderMethod.HtmlCanvas
                    : this.preferredMethod_;
          }
          boolean nokia810 =
              env.getUserAgent().indexOf("Linux arm") != -1
                  && env.getUserAgent().indexOf("Tablet browser") != -1
                  && env.getUserAgent().indexOf("Gecko") != -1;
          if (nokia810) {
            method = RenderMethod.HtmlCanvas;
          } else {
            method =
                this.preferredMethod_ == RenderMethod.PngImage
                    ? RenderMethod.HtmlCanvas
                    : this.preferredMethod_;
          }
        }
      }
    }
    return method;
  }
  /**
   * Paints the widget.
   *
   * <p>You should reimplement this method to paint the contents of the widget, using the given
   * paintDevice.
   */
  protected abstract void paintEvent(WPaintDevice paintDevice);
  /**
   * Creates a paint device.
   *
   * <p>Although it&apos;s usually not necessary to call this function, you may want to reimplement
   * this function to customize or specialize the device used for painting the widget.
   */
  protected WPaintDevice getCreatePaintDevice() {
    (this).isCreatePainter();
    if (this.painter_ != null) {
      return this.painter_.createPaintDevice(true);
    } else {
      return null;
    }
  }

  DomElementType getDomElementType() {
    if (this.isInline() && WApplication.getInstance().getEnvironment().agentIsIElt(9)) {
      return DomElementType.SPAN;
    } else {
      return DomElementType.DIV;
    }
  }

  void updateDom(final DomElement element, boolean all) {
    if (all && this.areaImage_ != null || this.areaImageAdded_) {
      element.addChild(this.areaImage_.createSDomElement(WApplication.getInstance()));
      this.areaImageAdded_ = false;
    }
    super.updateDom(element, all);
  }

  protected DomElement createDomElement(WApplication app) {
    if (this.isInLayout()) {
      this.setLayoutSizeAware(true);
      this.setJavaScriptMember(
          WT_RESIZE_JS,
          "function(self, w, h) {var u = $(self).find('canvas, img');if (w >= 0) u.width(w);else u.width('auto');if (h >= 0) u.height(h);else u.height('auto');}");
    }
    this.isCreatePainter();
    DomElement result = DomElement.createNew(this.getDomElementType());
    this.setId(result, app);
    DomElement wrap = result;
    if (this.getWidth().isAuto() && this.getHeight().isAuto()) {
      result.setProperty(Property.StylePosition, "relative");
      wrap = DomElement.createNew(DomElementType.DIV);
      wrap.setProperty(Property.StylePosition, "absolute");
      wrap.setProperty(Property.StyleLeft, "0");
      wrap.setProperty(Property.StyleRight, "0");
    }
    DomElement canvas = DomElement.createNew(DomElementType.DIV);
    if (!app.getEnvironment().agentIsSpiderBot()) {
      canvas.setId('p' + this.getId());
    }
    WPaintDevice device = this.painter_.getPaintDevice(false);
    if (this.painter_.getRenderType() == WWidgetPainter.RenderType.InlineVml && this.isInline()) {
      result.setProperty(Property.Style, "zoom: 1;");
      canvas.setProperty(Property.StyleDisplay, "inline");
      canvas.setProperty(Property.Style, "zoom: 1;");
    }
    if (this.renderWidth_ != 0 && this.renderHeight_ != 0) {
      this.paintEvent(device);
      if (device.getPainter() != null) {
        device.getPainter().end();
      }
    }
    this.painter_.createContents(canvas, device);
    this.needRepaint_ = false;
    wrap.addChild(canvas);
    if (wrap != result) {
      result.addChild(wrap);
    }
    this.updateDom(result, true);
    return result;
  }

  protected void getDomChanges(final List<DomElement> result, WApplication app) {
    DomElement e = DomElement.getForUpdate(this, DomElementType.DIV);
    this.updateDom(e, false);
    result.add(e);
    boolean createdNew = this.isCreatePainter();
    if (this.needRepaint_) {
      WPaintDevice device =
          this.painter_.getPaintDevice(
              this.repaintFlags_.contains(PaintFlag.Update) && !createdNew);
      if (this.renderWidth_ != 0 && this.renderHeight_ != 0) {
        this.paintEvent(device);
        if (device.getPainter() != null) {
          device.getPainter().end();
        }
      }
      if (createdNew) {
        DomElement canvas = DomElement.getForUpdate('p' + this.getId(), DomElementType.DIV);
        canvas.removeAllChildren();
        this.painter_.createContents(canvas, device);
        result.add(canvas);
      } else {
        this.painter_.updateContents(result, device);
      }
      this.needRepaint_ = false;
      this.repaintFlags_ = EnumSet.noneOf(PaintFlag.class);
    }
  }

  void propagateRenderOk(boolean deep) {
    this.needRepaint_ = false;
    super.propagateRenderOk(deep);
  }

  protected void enableAjax() {
    if (ObjectUtils.cast(this.painter_, WWidgetCanvasPainter.class) != null
        && this.renderWidth_ != 0
        && this.renderHeight_ != 0) {
      this.update();
    }
    super.enableAjax();
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (flags.contains(RenderFlag.Full) || !this.jsDefined_) {
      this.defineJavaScript();
    }
    super.render(flags);
  }

  protected void setFormData(final WObject.FormData formData) {
    String[] parVals = formData.values;
    if ((parVals.length == 0)) {
      return;
    }
    if (parVals[0].equals("undefined")) {
      return;
    }
    this.jsObjects_.assignFromJSON(parVals[0]);
  }
  /**
   * Returns a JavaScript reference to the client side representation of the {@link WPaintedWidget}.
   *
   * <p>The client side representation exposes the following interface:
   *
   * <pre>{@code
   * {
   * canvas: exposes the underlying HTML canvas element
   * repaint: a function that, when called, will repaint the widget without a server roundtrip
   * }
   *
   * }</pre>
   *
   * <p>
   *
   * <p><i><b>Note: </b>The {@link WPaintedWidget#getMethod() method} should be HtmlCanvas and there
   * has to be at least one {@link WJavaScriptHandle} associated with this {@link WPaintedWidget} in
   * order for this reference to be valid. </i>
   */
  protected String getObjJsRef() {
    return this.getJsRef() + ".wtObj";
  }

  private void defineJavaScript() {
    WApplication app = WApplication.getInstance();
    if (this.getMethod() == RenderMethod.HtmlCanvas) {
      app.loadJavaScript("js/WPaintedWidget.js", wtjs10());
      app.loadJavaScript("js/WPaintedWidget.js", wtjs11());
      if (this.jsObjects_.size() > 0) {
        this.setFormObject(true);
        app.loadJavaScript("js/WJavaScriptObjectStorage.js", wtjs20());
        this.jsDefined_ = true;
      } else {
        this.jsDefined_ = false;
      }
    }
  }

  private RenderMethod preferredMethod_;
  private WWidgetPainter painter_;
  private boolean needRepaint_;
  boolean sizeChanged_;
  private boolean areaImageAdded_;
  EnumSet<PaintFlag> repaintFlags_;
  WImage areaImage_;
  int renderWidth_;
  int renderHeight_;
  private JSlot repaintSlot_;
  WJavaScriptObjectStorage jsObjects_;
  private boolean jsDefined_;

  private void resizeCanvas(int width, int height) {
    if (this.renderWidth_ == width && this.renderHeight_ == height) {
      return;
    }
    this.renderWidth_ = width;
    this.renderHeight_ = height;
    if (this.areaImage_ != null) {
      this.areaImage_.resize(new WLength(this.renderWidth_), new WLength(this.renderHeight_));
    }
    this.sizeChanged_ = true;
    this.update();
  }

  private boolean isCreatePainter() {
    if (this.painter_ != null) {
      return false;
    }
    final WEnvironment env = WApplication.getInstance().getEnvironment();
    RenderMethod method = this.getMethod();
    if (method == RenderMethod.InlineSvgVml) {
      if (env.agentIsIElt(9)) {
        this.painter_ = new WWidgetVectorPainter(this, WWidgetPainter.RenderType.InlineVml);
      } else {
        this.painter_ = new WWidgetVectorPainter(this, WWidgetPainter.RenderType.InlineSvg);
      }
    } else {
      if (method == RenderMethod.PngImage) {
        this.painter_ = new WWidgetRasterPainter(this);
      } else {
        this.painter_ = new WWidgetCanvasPainter(this);
      }
    }
    return true;
  }

  private void createAreaImage() {
    if (!(this.areaImage_ != null)) {
      this.areaImage_ =
          new WImage(
              new WLink(WApplication.getInstance().getOnePixelGifUrl()), (WContainerWidget) null);
      this.widgetAdded(this.areaImage_);
      if (this.getPositionScheme() == PositionScheme.Static) {
        this.setPositionScheme(PositionScheme.Relative);
      }
      this.areaImage_.setPositionScheme(PositionScheme.Absolute);
      this.areaImage_.setOffsets(new WLength(0), EnumSet.of(Side.Left, Side.Top));
      this.areaImage_.setMargin(new WLength(0), EnumSet.of(Side.Top));
      this.areaImage_.resize(new WLength(this.renderWidth_), new WLength(this.renderHeight_));
      this.areaImageAdded_ = true;
    }
  }

  static WJavaScriptPreamble wtjs10() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WPaintedWidget",
        "(function(t,r){r.wtObj=this;var e=this;this.imagePreloaders=[];this.images=[];this.canvas=document.getElementById(\"c\"+r.id);this.repaint=function(){};this.widget=r;this.cancelPreloaders=function(){for(var t=0;t<e.imagePreloaders.length;++t)e.imagePreloaders[t].cancel();e.imagePreloaders=[]}})");
  }

  static WJavaScriptPreamble wtjs11() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptObject,
        "gfxUtils",
        "function(){var t=1024,r=4080;return new function(){var e=this;this.path_crisp=function(t){return t.map((function(t){return[Math.floor(t[0])+.5,Math.floor(t[1])+.5,t[2]]}))};this.transform_mult=function(t,r){if(2===r.length){var n=r[0],a=r[1];return[t[0]*n+t[2]*a+t[4],t[1]*n+t[3]*a+t[5]]}if(3===r.length){if(8===r[2]||9===r[2])return r.slice(0);n=r[0],a=r[1];return[t[0]*n+t[2]*a+t[4],t[1]*n+t[3]*a+t[5],r[2]]}if(4===r.length){var i,o,s,c,f,l,u=e.transform_mult(t,[r[0],r[1]]);i=u[0];s=u[0];o=u[1];c=u[1];for(f=0;f<3;++f){l=e.transform_mult(t,0==f?[e.rect_left(r),e.rect_bottom(r)]:1==f?[e.rect_right(r),e.rect_top(r)]:[e.rect_right(r),e.rect_bottom(r)]);i=Math.min(i,l[0]);s=Math.max(s,l[0]);o=Math.min(o,l[1]);c=Math.max(c,l[1])}return[i,o,s-i,c-o]}return 6===r.length?[t[0]*r[0]+t[2]*r[1],t[1]*r[0]+t[3]*r[1],t[0]*r[2]+t[2]*r[3],t[1]*r[2]+t[3]*r[3],t[0]*r[4]+t[2]*r[5]+t[4],t[1]*r[4]+t[3]*r[5]+t[5]]:[]};this.transform_apply=function(t,r){var n=e.transform_mult;return r.map((function(r){return n(t,r)}))};this.transform_det=function(t){var r=t[0],e=t[2],n=t[1];return r*t[3]-n*e};this.transform_adjoint=function(t){var r=t[0],e=t[2],n=t[1],a=t[3],i=t[4],o=t[5];return[a,-e,-n,r,o*n-i*a,-(o*r-i*e)]};this.transform_inverted=function(t){var r=e.transform_det(t);if(0!=r){var n=e.transform_adjoint(t);return[n[0]/r,n[2]/r,n[1]/r,n[3]/r,n[4]/r,n[5]/r]}console.log(\"inverted(): oops, determinant == 0\");return t};this.transform_assign=function(t,r){t[0]=r[0];t[1]=r[1];t[2]=r[2];t[3]=r[3];t[4]=r[4];t[5]=r[5]};this.transform_equal=function(t,r){return t[0]==r[0]&&t[1]==r[1]&&t[2]==r[2]&&t[3]==r[3]&&t[4]==r[4]&&t[5]==r[5]};this.css_text=function(t){return\"rgba(\"+t[0]+\",\"+t[1]+\",\"+t[2]+\",\"+t[3]+\")\"};this.arcPosition=function(t,r,e,n,a){var i=-a/180*Math.PI;return[t+e*Math.cos(i),r+n*Math.sin(i)]};this.pnpoly=function(t,r){var n,a,i,o=!1,s=0,c=0,f=t[0],l=t[1];for(n=0;n<r.length;++n){a=s;i=c;if(7===r[n][2]){a=(u=e.arcPosition(r[n][0],r[n][1],r[n+1][0],r[n+1][1],r[n+2][0]))[0];i=u[1]}else if(9===r[n][2]){var u;a=(u=e.arcPosition(r[n-2][0],r[n-2][1],r[n-1][0],r[n-1][1],r[n][0]+r[n][1]))[0];i=u[1]}else if(8!==r[n][2]){a=r[n][0];i=r[n][1]}0!==r[n][2]&&c>l!=i>l&&f<(a-s)*(l-c)/(i-c)+s&&(o=!o);s=a;c=i}return o};this.rect_intersection=function(t,r){t=e.rect_normalized(t);r=e.rect_normalized(r);var n=e.rect_top,a=e.rect_bottom,i=e.rect_left,o=e.rect_right,s=Math.max(i(t),i(r)),c=Math.min(o(t),o(r)),f=Math.max(n(t),n(r));return[s,f,c-s,Math.min(a(t),a(r))-f]};this.drawRect=function(t,r,n,a){r=e.rect_normalized(r);var i=e.rect_top(r),o=e.rect_bottom(r),s=e.rect_left(r),c=e.rect_right(r);path=[[s,i,0],[c,i,1],[c,o,1],[s,o,1],[s,i,1]];e.drawPath(t,path,n,a,!1)};this.drawPath=function(t,r,n,a,i){var o=0,s=[],c=[],f=[],l=1048576;function u(t){return t[0]}function h(t){return t[1]}function m(t){return t[2]}t.beginPath();r.length>0&&0!==m(r[0])&&t.moveTo(0,0);for(o=0;o<r.length;o++){var p=r[o];switch(m(p)){case 0:Math.abs(u(p))<=l&&Math.abs(h(p))<=l&&t.moveTo(u(p),h(p));break;case 1:!function(){var s=0===o?[0,0]:r[o-1];if(!n&&!i&&a&&(Math.abs(u(s))>l||Math.abs(h(s))>l||Math.abs(u(p))>l||Math.abs(h(p))>l)){!function(){var r,n=t.wtTransform?t.wtTransform:[1,0,0,1,0,0],a=e.transform_inverted(n),i=e.transform_mult(n,s),o=e.transform_mult(n,p),c=u(o)-u(i),f=h(o)-h(i),l=-50,m=t.canvas.width+50,_=-50,v=t.canvas.height+50;function g(t,r,e){return(e-t)/r}function d(t,r,e){return t+e*r}var b,w,P=null,M=null,T=null,y=null;if(u(i)<l&&u(o)>l){r=g(u(i),c,l);P=[l,d(h(i),f,r),r]}else if(u(i)>m&&u(o)<m){r=g(u(i),c,m);P=[m,d(h(i),f,r),r]}else{if(!(u(i)>l&&u(i)<m))return;P=[u(i),h(i),0]}if(h(i)<_&&h(o)>_){r=g(h(i),f,_);M=[d(u(i),c,r),_,r]}else if(h(i)>v&&h(o)<v){r=g(h(i),f,v);M=[d(u(i),c,r),v,r]}else{if(!(h(i)>_&&h(i)<v))return;M=[u(i),h(i),0]}if(!(u(b=P[2]>M[2]?[P[0],P[1]]:[M[0],M[1]])<l||u(b)>m||h(b)<_||h(b)>v)){if(u(i)<m&&u(o)>m){r=g(u(i),c,m);T=[m,d(h(i),f,r),r]}else if(u(i)>l&&u(o)<l){r=g(u(i),c,l);T=[l,d(h(i),f,r),r]}else{if(!(u(o)>l&&u(o)<m))return;T=[u(o),h(o),1]}if(h(i)<v&&h(o)>v){r=g(h(i),f,v);y=[d(u(i),c,r),v,r]}else if(h(i)>_&&h(o)<_){r=g(h(i),f,_);y=[d(u(i),c,r),_,r]}else{if(!(u(o)>_&&h(o)<v))return;y=[u(o),h(o),1]}if(!(u(w=T[2]<y[2]?[T[0],T[1]]:[y[0],y[1]])<l||u(w)>m||h(w)<_||h(w)>v)){b=e.transform_mult(a,b);w=e.transform_mult(a,w);t.moveTo(b[0],b[1]);t.lineTo(w[0],w[1])}}}();Math.abs(u(p))<=l&&Math.abs(h(p))<=l&&t.moveTo(u(p),h(p))}else t.lineTo(u(p),h(p))}();break;case 2:case 3:s.push(u(p),h(p));break;case 4:s.push(u(p),h(p));t.bezierCurveTo.apply(t,s);s=[];break;case 7:c.push(u(p),h(p));break;case 8:c.push(u(p));break;case 9:!function(){function r(t){var r=t%360;return r<0?r+360:r}function e(t){return t*Math.PI/180}var n,a,i=u(p),o=h(p),s=e(r(-i));n=o>=360||o<=-360?s-2*Math.PI*(o>0?1:-1):e(r(-i-((a=o)>360?360:a<-360?-360:a)));var f=o>0;c.push(s,n,f);t.arc.apply(t,c);c=[]}();break;case 5:f.push(u(p),h(p));break;case 6:f.push(u(p),h(p));t.quadraticCurveTo.apply(t,f);f=[]}}n&&t.fill();a&&t.stroke();i&&t.clip()};this.drawStencilAlongPath=function(t,r,n,a,i,o){var s,c=0;function f(t){return t[1]}function l(t){return t[2]}for(c=0;c<n.length;c++){var u=n[c];if((!o||!t.wtClipPath||e.pnpoly(u,e.transform_apply(t.wtClipPathTransform,t.wtClipPath)))&&(0==l(u)||1==l(u)||6==l(u)||4==l(u))){var h=e.transform_apply([1,0,0,1,(s=u,s[0]),f(u)],r);e.drawPath(t,h,a,i,!1)}}};this.drawText=function(n,a,i,o,s){if(!s||!n.wtClipPath||e.pnpoly(s,e.transform_apply(n.wtClipPathTransform,n.wtClipPath))){var c=i&r,f=null,l=null;switch(15&i){case 1:n.textAlign=\"left\";f=e.rect_left(a);break;case 2:n.textAlign=\"right\";f=e.rect_right(a);break;case 4:n.textAlign=\"center\";f=e.rect_center(a).x}switch(c){case 128:n.textBaseline=\"top\";l=e.rect_top(a);break;case t:n.textBaseline=\"bottom\";l=e.rect_bottom(a);break;case 512:n.textBaseline=\"middle\";l=e.rect_center(a).y}if(null!=f&&null!=l){var u=n.fillStyle;n.fillStyle=n.strokeStyle;n.fillText(o,f,l);n.fillStyle=u}}};this.calcYOffset=function(r,e,n,a){return 512===a?-(e-1)*n/2+r*n:128===a?r*n:a===t?-(e-1-r)*n:0};this.drawTextOnPath=function(t,n,a,i,o,s,c,f,l){var u=0,h=0;function m(t){return t[0]}function p(t){return t[1]}function _(t){return t[2]}var v=e.transform_apply(i,o);for(u=0;u<o.length&&!(u>=n.length);u++){var g=o[u],d=v[u],b=n[u].split(\"\\n\");if(0==_(g)||1==_(g)||6==_(g)||4==_(g))if(0==s)for(h=0;h<b.length;h++){var w=e.calcYOffset(h,b.length,c,f&r);e.drawText(t,[a[0]+m(d),a[1]+p(d)+w,a[2],a[3]],f,b[h],l?[m(d),p(d)]:null)}else{var P=s*Math.PI/180,M=Math.cos(-P),T=-Math.sin(-P),y=-T,x=M;t.save();t.transform(M,y,T,x,m(d),p(d));for(h=0;h<b.length;h++){w=e.calcYOffset(h,b.length,c,f&r);e.drawText(t,[a[0],a[1]+w,a[2],a[3]],f,b[h],l?[m(d),p(d)]:null)}t.restore()}}};this.setClipPath=function(t,r,n,a){if(a){t.setTransform.apply(t,n);e.drawPath(t,r,!1,!1,!0);t.setTransform(1,0,0,1,0,0)}t.wtClipPath=r;t.wtClipPathTransform=n};this.removeClipPath=function(t){delete t.wtClipPath;delete t.wtClipPathTransform};this.rect_top=function(t){return t[1]};this.rect_bottom=function(t){return t[1]+t[3]};this.rect_right=function(t){return t[0]+t[2]};this.rect_left=function(t){return t[0]};this.rect_topleft=function(t){return[t[0],t[1]]};this.rect_topright=function(t){return[t[0]+t[2],t[1]]};this.rect_bottomleft=function(t){return[t[0],t[1]+t[3]]};this.rect_bottomright=function(t){return[t[0]+t[2],t[1]+t[3]]};this.rect_center=function(t){return{x:(2*t[0]+t[2])/2,y:(2*t[1]+t[3])/2}};this.rect_normalized=function(t){var r,e,n,a;if(t[2]>0){r=t[0];n=t[2]}else{r=t[0]+t[2];n=-t[2]}if(t[3]>0){e=t[1];a=t[3]}else{e=t[1]+t[3];a=-t[3]}return[r,e,n,a]};this.drawImage=function(t,r,e,n,a){try{t.drawImage(r,n[0],n[1],n[2],n[3],a[0],a[1],a[2],a[3])}catch(t){var i=\"Error while drawing image: '\"+e+\"': \"+t.name;t.message&&(i+=\": \"+t.message);console.error(i)}}}}()");
  }

  static WJavaScriptPreamble wtjs20() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WJavaScriptObjectStorage",
        "(function(r,t){t.wtJSObj=this;var n=this;r.WT;function e(r){if(jQuery.isArray(r)){var t,n=[];for(t=0;t<r.length;++t)n.push(e(r[t]));return n}if(jQuery.isPlainObject(r)){var i;n={};for(i in r)r.hasOwnProperty(i)&&(n[i]=e(r[i]));return n}return r}function i(r,t){if(r===t)return!0;if(jQuery.isArray(r)&&jQuery.isArray(t)){if(r.length!==t.length)return!1;var n;for(n=0;n<r.length;++n)if(!i(r[n],t[n]))return!1;return!0}if(jQuery.isPlainObject(r)&&jQuery.isPlainObject(t)){var e;for(e in r)if(r.hasOwnProperty(e)){if(!t.hasOwnProperty(e))return!1;if(!i(r[e],t[e]))return!1}for(e in t)if(t.hasOwnProperty(e)&&!r.hasOwnProperty(e))return!1;return!0}return!1}function u(r){return jQuery.isArray(r)&&r.length>6}var a={};this.jsValues=[];this.setJsValue=function(r,t){u(t)||(a[r]=e(t));n.jsValues[r]=t};t.wtEncodeValue=function(){var r,t,e={};for(t=0;t<n.jsValues.length;++t)u(r=n.jsValues[t])||i(r,a[t])||(e[t]=r);return JSON.stringify(e)}})");
  }
}
