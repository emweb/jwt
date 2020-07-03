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
    if (((this.painter_) instanceof WWidgetCanvasPainter
                ? (WWidgetCanvasPainter) (this.painter_)
                : null)
            != null
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
        "function(W,w){w.wtObj=this;var x=this;this.imagePreloaders=[];this.images=[];this.canvas=document.getElementById(\"c\"+w.id);this.repaint=function(){};this.widget=w;this.cancelPreloaders=function(){for(var y=0;y<x.imagePreloaders.length;++y)x.imagePreloaders[y].cancel();x.imagePreloaders=[]}}");
  }

  static WJavaScriptPreamble wtjs11() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptObject,
        "gfxUtils",
        "function(){function W(){var i=this;this.path_crisp=function(a){return a.map(function(b){return[Math.floor(b[0])+0.5,Math.floor(b[1])+0.5,b[2]]})};this.transform_mult=function(a,b){if(b.length===2){var e=b[0],f=b[1];return[a[w]*e+a[y]*f+a[E],a[x]*e+a[A]*f+a[F]]}if(b.length===3){if(b[2]===J||b[2]===K)return b.slice(0);e=b[0];f=b[1];return[a[w]*e+a[y]*f+a[E],a[x]*e+a[A]*f+a[F],b[2]]}if(b.length===4){var h,d,c,p;d=i.transform_mult(a,[b[0],b[1]]);e=d[0]; h=d[0];f=d[1];d=d[1];for(c=0;c<3;++c){p=i.transform_mult(a,c==0?[i.rect_left(b),i.rect_bottom(b)]:c==1?[i.rect_right(b),i.rect_top(b)]:[i.rect_right(b),i.rect_bottom(b)]);e=Math.min(e,p[0]);h=Math.max(h,p[0]);f=Math.min(f,p[1]);d=Math.max(d,p[1])}return[e,f,h-e,d-f]}if(b.length===6)return[a[w]*b[w]+a[y]*b[x],a[x]*b[w]+a[A]*b[x],a[w]*b[y]+a[y]*b[A],a[x]*b[y]+a[A]*b[A],a[w]*b[E]+a[y]*b[F]+a[E],a[x]*b[E]+a[A]*b[F]+a[F]];return[]};this.transform_apply=function(a,b){var e=i.transform_mult;return b.map(function(f){return e(a, f)})};this.transform_det=function(a){return a[w]*a[A]-a[x]*a[y]};this.transform_adjoint=function(a){var b=a[w],e=a[y],f=a[x],h=a[A],d=a[E];a=a[F];return[h,-e,-f,b,a*f-d*h,-(a*b-d*e)]};this.transform_inverted=function(a){var b=i.transform_det(a);if(b!=0){a=i.transform_adjoint(a);return[a[w]/b,a[y]/b,a[x]/b,a[A]/b,a[E]/b,a[F]/b]}else{console.log(\"inverted(): oops, determinant == 0\");return a}};this.transform_assign=function(a,b){a[0]=b[0];a[1]=b[1];a[2]=b[2];a[3]=b[3];a[4]=b[4];a[5]=b[5]};this.transform_equal= function(a,b){return a[0]==b[0]&&a[1]==b[1]&&a[2]==b[2]&&a[3]==b[3]&&a[4]==b[4]&&a[5]==b[5]};this.css_text=function(a){return\"rgba(\"+a[0]+\",\"+a[1]+\",\"+a[2]+\",\"+a[3]+\")\"};this.arcPosition=function(a,b,e,f,h){h=-h/180*Math.PI;return[a+e*Math.cos(h),b+f*Math.sin(h)]};this.pnpoly=function(a,b){var e=false,f=0,h=0,d=a[0];a=a[1];var c,p,o;for(c=0;c<b.length;++c){p=f;o=h;if(b[c][2]===X){o=i.arcPosition(b[c][0],b[c][1],b[c+1][0],b[c+1][1],b[c+2][0]);p=o[0];o=o[1]}else if(b[c][2]===K){o=i.arcPosition(b[c- 2][0],b[c-2][1],b[c-1][0],b[c-1][1],b[c][0]+b[c][1]);p=o[0];o=o[1]}else if(b[c][2]!==J){p=b[c][0];o=b[c][1]}if(b[c][2]!==H)if(h>a!==o>a&&d<(p-f)*(a-h)/(o-h)+f)e=!e;f=p;h=o}return e};this.rect_intersection=function(a,b){a=i.rect_normalized(a);b=i.rect_normalized(b);var e=i.rect_top,f=i.rect_bottom,h=i.rect_left,d=i.rect_right;h=Math.max(h(a),h(b));d=Math.min(d(a),d(b));e=Math.max(e(a),e(b));a=Math.min(f(a),f(b));return[h,e,d-h,a-e]};this.drawRect=function(a,b,e,f){b=i.rect_normalized(b);var h=i.rect_top(b), d=i.rect_bottom(b),c=i.rect_left(b);b=i.rect_right(b);path=[[c,h,H],[b,h,G],[b,d,G],[c,d,G],[c,h,G]];i.drawPath(a,path,e,f,false)};this.drawPath=function(a,b,e,f,h){function d(m){return m[0]}function c(m){return m[1]}function p(m){return m[2]}var o=0,t=[],q=[],u=[];a.beginPath();b.length>0&&p(b[0])!==H&&a.moveTo(0,0);for(o=0;o<b.length;o++){var k=b[o];switch(p(k)){case H:Math.abs(d(k))<=1048576&&Math.abs(c(k))<=1048576&&a.moveTo(d(k),c(k));break;case G:(function(){var m=o===0?[0,0]:b[o-1];if(!e&& !h&&f&&(Math.abs(d(m))>1048576||Math.abs(c(m))>1048576||Math.abs(d(k))>1048576||Math.abs(c(k))>1048576)){(function(){function r(M,N,O){return(O-M)/N}function s(M,N,O){return M+O*N}var l=a.wtTransform?a.wtTransform:[1,0,0,1,0,0],B=i.transform_inverted(l),g=i.transform_mult(l,m),n=i.transform_mult(l,k),C=d(n)-d(g),D=c(n)-c(g);l=a.canvas.width+50;var z=a.canvas.height+50,j,v=null,I=j=null;j=null;if(d(g)<-50&&d(n)>-50){j=r(d(g),C,-50);v=[-50,s(c(g),D,j),j]}else if(d(g)>l&&d(n)<l){j=r(d(g),C,l);v=[l,s(c(g), D,j),j]}else if(d(g)>-50&&d(g)<l)v=[d(g),c(g),0];else return;if(c(g)<-50&&c(n)>-50){j=r(c(g),D,-50);j=[s(d(g),C,j),-50,j]}else if(c(g)>z&&c(n)<z){j=r(c(g),D,z);j=[s(d(g),C,j),z,j]}else if(c(g)>-50&&c(g)<z)j=[d(g),c(g),0];else return;v=v[2]>j[2]?[v[0],v[1]]:[j[0],j[1]];if(!(d(v)<-50||d(v)>l||c(v)<-50||c(v)>z)){if(d(g)<l&&d(n)>l){j=r(d(g),C,l);I=[l,s(c(g),D,j),j]}else if(d(g)>-50&&d(n)<-50){j=r(d(g),C,-50);I=[-50,s(c(g),D,j),j]}else if(d(n)>-50&&d(n)<l)I=[d(n),c(n),1];else return;if(c(g)<z&&c(n)>z){j= r(c(g),D,z);j=[s(d(g),C,j),z,j]}else if(c(g)>-50&&c(n)<-50){j=r(c(g),D,-50);j=[s(d(g),C,j),-50,j]}else if(d(n)>-50&&c(n)<z)j=[d(n),c(n),1];else return;g=I[2]<j[2]?[I[0],I[1]]:[j[0],j[1]];if(!(d(g)<-50||d(g)>l||c(g)<-50||c(g)>z)){v=i.transform_mult(B,v);g=i.transform_mult(B,g);a.moveTo(v[0],v[1]);a.lineTo(g[0],g[1])}}})();Math.abs(d(k))<=1048576&&Math.abs(c(k))<=1048576&&a.moveTo(d(k),c(k))}else a.lineTo(d(k),c(k))})();break;case aa:t.push(d(k),c(k));break;case ba:t.push(d(k),c(k));break;case P:t.push(d(k), c(k));a.bezierCurveTo.apply(a,t);t=[];break;case X:q.push(d(k),c(k));break;case J:q.push(d(k));break;case K:(function(){function m(n){return n>360?360:n<-360?-360:n}function r(n){n=n%360;return n<0?n+360:n}function s(n){return n*Math.PI/180}var l=d(k),B=c(k),g=s(r(-l));l=B>=360||B<=-360?g-2*Math.PI*(B>0?1:-1):s(r(-l-m(B)));q.push(g,l,B>0);a.arc.apply(a,q);q=[]})();break;case ca:u.push(d(k),c(k));break;case Q:u.push(d(k),c(k));a.quadraticCurveTo.apply(a,u);u=[];break}}e&&a.fill();f&&a.stroke();h&& a.clip()};this.drawStencilAlongPath=function(a,b,e,f,h,d){function c(u){return u[0]}function p(u){return u[1]}function o(u){return u[2]}var t=0;for(t=0;t<e.length;t++){var q=e[t];if(!(d&&a.wtClipPath&&!i.pnpoly(q,i.transform_apply(a.wtClipPathTransform,a.wtClipPath))))if(o(q)==H||o(q)==G||o(q)==Q||o(q)==P){q=i.transform_apply([1,0,0,1,c(q),p(q)],b);i.drawPath(a,q,f,h,false)}}};this.drawText=function(a,b,e,f,h){if(!(h&&a.wtClipPath&&!i.pnpoly(h,i.transform_apply(a.wtClipPathTransform,a.wtClipPath)))){var d= e&S;var c=h=null;switch(e&da){case Y:a.textAlign=\"left\";h=i.rect_left(b);break;case Z:a.textAlign=\"right\";h=i.rect_right(b);break;case $:a.textAlign=\"center\";h=i.rect_center(b).x;break}switch(d){case T:a.textBaseline=\"top\";c=i.rect_top(b);break;case U:a.textBaseline=\"bottom\";c=i.rect_bottom(b);break;case V:a.textBaseline=\"middle\";c=i.rect_center(b).y;break}if(!(h==null||c==null)){b=a.fillStyle;a.fillStyle=a.strokeStyle;a.fillText(f,h,c);a.fillStyle=b}}};this.calcYOffset=function(a,b,e,f){return f=== V?-((b-1)*e/2)+a*e:f===T?a*e:f===U?-(b-1-a)*e:0};this.drawTextOnPath=function(a,b,e,f,h,d,c,p,o){function t(g){return g[0]}function q(g){return g[1]}function u(g){return g[2]}var k=0,m=0;f=i.transform_apply(f,h);for(k=0;k<h.length;k++){if(k>=b.length)break;m=h[k];var r=f[k],s=b[k].split(\"\\n\");if(u(m)==H||u(m)==G||u(m)==Q||u(m)==P)if(d==0)for(m=0;m<s.length;m++){var l=i.calcYOffset(m,s.length,c,p&S);i.drawText(a,[e[0]+t(r),e[1]+q(r)+l,e[2],e[3]],p,s[m],o?[t(r),q(r)]:null)}else{l=d*Math.PI/180;m=Math.cos(-l); l=-Math.sin(-l);var B=-l;a.save();a.transform(m,B,l,m,t(r),q(r));for(m=0;m<s.length;m++){l=i.calcYOffset(m,s.length,c,p&S);i.drawText(a,[e[0],e[1]+l,e[2],e[3]],p,s[m],o?[t(r),q(r)]:null)}a.restore()}}};this.setClipPath=function(a,b,e,f){if(f){a.setTransform.apply(a,e);i.drawPath(a,b,false,false,true);a.setTransform(1,0,0,1,0,0)}a.wtClipPath=b;a.wtClipPathTransform=e};this.removeClipPath=function(a){delete a.wtClipPath;delete a.wtClipPathTransform};this.rect_top=function(a){return a[1]};this.rect_bottom= function(a){return a[1]+a[3]};this.rect_right=function(a){return a[0]+a[2]};this.rect_left=function(a){return a[0]};this.rect_topleft=function(a){return[a[0],a[1]]};this.rect_topright=function(a){return[a[0]+a[2],a[1]]};this.rect_bottomleft=function(a){return[a[0],a[1]+a[3]]};this.rect_bottomright=function(a){return[a[0]+a[2],a[1]+a[3]]};this.rect_center=function(a){return{x:(2*a[0]+a[2])/2,y:(2*a[1]+a[3])/2}};this.rect_normalized=function(a){var b,e,f;if(a[2]>0){b=a[0];f=a[2]}else{b=a[0]+a[2];f= -a[2]}if(a[3]>0){e=a[1];a=a[3]}else{e=a[1]+a[3];a=-a[3]}return[b,e,f,a]};this.drawImage=function(a,b,e,f,h){try{a.drawImage(b,f[0],f[1],f[2],f[3],h[0],h[1],h[2],h[3])}catch(d){a=\"Error while drawing image: '\"+e+\"': \"+d.name;if(d.message)a+=\": \"+d.message;console.error(a)}}}var w=0,x=1,y=2,A=3,E=4,F=5,H=0,G=1,aa=2,ba=3,P=4,ca=5,Q=6,X=7,J=8,K=9,Y=1,Z=2,$=4,T=128,V=512,U=1024,S=112|T|256|V|U|2048,da=Y|Z|$|8;return new W}()");
  }

  static WJavaScriptPreamble wtjs20() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WJavaScriptObjectStorage",
        "function(k,g){function d(b){if(jQuery.isArray(b)){var c=[],a;for(a=0;a<b.length;++a)c.push(d(b[a]));return c}else if(jQuery.isPlainObject(b)){c={};for(a in b)if(b.hasOwnProperty(a))c[a]=d(b[a]);return c}else return b}function e(b,c){if(b===c)return true;if(jQuery.isArray(b)&&jQuery.isArray(c)){if(b.length!==c.length)return false;var a;for(a=0;a<b.length;++a)if(!e(b[a],c[a]))return false;return true}else if(jQuery.isPlainObject(b)&& jQuery.isPlainObject(c)){for(a in b)if(b.hasOwnProperty(a)){if(!c.hasOwnProperty(a))return false;if(!e(b[a],c[a]))return false}for(a in c)if(c.hasOwnProperty(a))if(!b.hasOwnProperty(a))return false;return true}else return false}function h(b){return jQuery.isArray(b)&&b.length>6}function j(){var b={},c,a;for(a=0;a<f.jsValues.length;++a){c=f.jsValues[a];if(!h(c)&&!e(c,i[a]))b[a]=c}return JSON.stringify(b)}g.wtJSObj=this;var f=this,i={};this.jsValues=[];this.setJsValue=function(b,c){h(c)||(i[b]=d(c)); f.jsValues[b]=c};g.wtEncodeValue=j}");
  }
}
