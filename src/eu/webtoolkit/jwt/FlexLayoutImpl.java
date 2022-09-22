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

class FlexLayoutImpl extends StdLayoutImpl {
  private static Logger logger = LoggerFactory.getLogger(FlexLayoutImpl.class);

  public FlexLayoutImpl(WLayout layout, final Grid grid) {
    super(layout);
    this.grid_ = grid;
    this.addedItems_ = new ArrayList<WLayoutItem>();
    this.removedItems_ = new ArrayList<String>();
    this.elId_ = "";
    String THIS_JS = "js/FlexLayoutImpl.js";
    WApplication app = WApplication.getInstance();
    if (!app.isJavaScriptLoaded(THIS_JS)) {
      app.loadJavaScript(THIS_JS, wtjs1());
    }
    WContainerWidget c = this.getContainer();
    if (c != null) {
      c.setFlexBox(true);
    }
  }

  public int getMinimumWidth() {
    final int colCount = this.grid_.columns_.size();
    int total = 0;
    for (int i = 0; i < colCount; ++i) {
      total += this.minimumWidthForColumn(i);
    }
    return total + (colCount - 1) * this.grid_.horizontalSpacing_;
  }

  public int getMinimumHeight() {
    final int rowCount = this.grid_.rows_.size();
    int total = 0;
    for (int i = 0; i < rowCount; ++i) {
      total += this.minimumHeightForRow(i);
    }
    return total + (rowCount - 1) * this.grid_.verticalSpacing_;
  }

  public void itemAdded(WLayoutItem item) {
    this.addedItems_.add(item);
    this.update();
  }

  public void itemRemoved(WLayoutItem item) {
    this.addedItems_.remove(item);
    this.removedItems_.add(getImpl(item).getId());
    this.update();
  }

  public void updateDom(final DomElement parent) {
    WApplication app = WApplication.getInstance();
    DomElement div = DomElement.getForUpdate(this.elId_, DomElementType.DIV);
    Orientation orientation = this.getOrientation();
    List<Integer> orderedInserts = new ArrayList<Integer>();
    for (int i = 0; i < this.addedItems_.size(); ++i) {
      orderedInserts.add(this.indexOf(this.addedItems_.get(i), orientation));
    }
    Collections.sort(orderedInserts);
    int totalStretch = this.getTotalStretch(orientation);
    for (int i = 0; i < orderedInserts.size(); ++i) {
      int pos = orderedInserts.get(i);
      DomElement el = this.createElement(orientation, pos, totalStretch, app);
      div.insertChildAt(el, pos);
    }
    this.addedItems_.clear();
    for (int i = 0; i < this.removedItems_.size(); ++i) {
      div.callJavaScript("Wt4_8_1.remove('" + this.removedItems_.get(i) + "');", true);
    }
    this.removedItems_.clear();
    StringBuilder js = new StringBuilder();
    js.append("layout.adjust(").append(this.grid_.horizontalSpacing_).append(")");
    div.callMethod(js.toString());
    parent.addChild(div);
  }

  public void update() {
    WContainerWidget c = this.getContainer();
    if (c != null) {
      c.layoutChanged(false);
    }
  }

  public DomElement createDomElement(
      DomElement parent, boolean fitWidth, boolean fitHeight, WApplication app) {
    this.addedItems_.clear();
    this.removedItems_.clear();
    int[] margin = {0, 0, 0, 0};
    DomElement result;
    if (this.getLayout().getParentLayout() == null) {
      if (this.getContainer() == app.getRoot()) {
        app.setBodyClass(app.getBodyClass() + " Wt-layout");
        app.setHtmlClass(app.getHtmlClass() + " Wt-layout");
        parent.setProperty(Property.StyleBoxSizing, "border-box");
      }
      margin[3] = this.getLayout().getContentsMargin(Side.Left);
      margin[0] = this.getLayout().getContentsMargin(Side.Top);
      margin[1] = this.getLayout().getContentsMargin(Side.Right);
      margin[2] = this.getLayout().getContentsMargin(Side.Bottom);
      Orientation orientation = this.getOrientation();
      if (orientation == Orientation.Horizontal) {
        margin[3] = Math.max(0, margin[3] - this.grid_.horizontalSpacing_ / 2);
        margin[1] = Math.max(0, margin[1] - (this.grid_.horizontalSpacing_ + 1) / 2);
      } else {
        margin[0] = Math.max(0, margin[0] - this.grid_.verticalSpacing_ / 2);
        margin[2] = Math.max(0, margin[2] - (this.grid_.horizontalSpacing_ + 1) / 2);
      }
      ResizeSensor.applyIfNeeded(this.getContainer());
      result = parent;
      this.elId_ = this.getContainer().getId();
    } else {
      result = DomElement.createNew(DomElementType.DIV);
      this.elId_ = this.getId();
      result.setId(this.elId_);
      result.setProperty(Property.StyleDisplay, this.getStyleDisplay());
    }
    if (margin[0] != 0 || margin[1] != 0 || margin[2] != 0 || margin[3] != 0) {
      StringBuilder paddingProperty = new StringBuilder();
      paddingProperty
          .append(margin[0])
          .append("px ")
          .append(margin[1])
          .append("px ")
          .append(margin[2])
          .append("px ")
          .append(margin[3])
          .append("px");
      result.setProperty(Property.StylePadding, paddingProperty.toString());
    }
    result.setProperty(Property.StyleFlexFlow, this.getStyleFlex());
    Orientation orientation = this.getOrientation();
    int c = this.count(orientation);
    int totalStretch = this.getTotalStretch(orientation);
    for (int i = 0; i < c; ++i) {
      DomElement el = this.createElement(orientation, i, totalStretch, app);
      result.addChild(el);
    }
    StringBuilder js = new StringBuilder();
    js.append("layout=new Wt4_8_1.FlexLayout(")
        .append(app.getJavaScriptClass())
        .append(",'")
        .append(this.elId_)
        .append("');");
    result.callMethod(js.toString());
    return result;
  }

  public boolean itemResized(WLayoutItem item) {
    return true;
  }

  public boolean isParentResized() {
    return false;
  }

  private final Grid grid_;
  private List<WLayoutItem> addedItems_;
  private List<String> removedItems_;
  private String elId_;

  private int minimumHeightForRow(int row) {
    int minHeight = 0;
    final int colCount = this.grid_.columns_.size();
    for (int j = 0; j < colCount; ++j) {
      WLayoutItem item = this.grid_.items_.get(row).get(j).item_;
      if (item != null) {
        minHeight = Math.max(minHeight, getImpl(item).getMinimumHeight());
      }
    }
    return minHeight;
  }

  private int minimumWidthForColumn(int col) {
    int minWidth = 0;
    final int rowCount = this.grid_.rows_.size();
    for (int i = 0; i < rowCount; ++i) {
      WLayoutItem item = this.grid_.items_.get(i).get(col).item_;
      if (item != null) {
        minWidth = Math.max(minWidth, getImpl(item).getMinimumWidth());
      }
    }
    return minWidth;
  }

  private DomElement createElement(
      Orientation orientation, int index, int totalStretch, WApplication app) {
    final Grid.Item it = this.item(orientation, index);
    final Grid.Section s = this.section(orientation, index);
    DomElement el = getImpl(it.item_).createDomElement((DomElement) null, true, true, app);
    if (ObjectUtils.cast(getImpl(it.item_), StdGridLayoutImpl2.class) != null) {
      DomElement wrapEl = DomElement.createNew(DomElementType.DIV);
      wrapEl.addChild(el);
      el = wrapEl;
    }
    int[] m = {0, 0, 0, 0};
    FlexLayoutImpl flexImpl = ObjectUtils.cast(getImpl(it.item_), FlexLayoutImpl.class);
    if (flexImpl != null) {
      Orientation elOrientation = flexImpl.getOrientation();
      if (elOrientation == Orientation.Horizontal) {
        m[3] -= flexImpl.grid_.horizontalSpacing_ / 2;
        m[1] -= (flexImpl.grid_.horizontalSpacing_ + 1) / 2;
      } else {
        m[0] -= flexImpl.grid_.verticalSpacing_ / 2;
        m[2] -= (flexImpl.grid_.horizontalSpacing_ + 1) / 2;
      }
    }
    AlignmentFlag hAlign =
        EnumUtils.enumFromSet(EnumUtils.mask(it.alignment_, AlignmentFlag.AlignHorizontalMask));
    AlignmentFlag vAlign =
        EnumUtils.enumFromSet(EnumUtils.mask(it.alignment_, AlignmentFlag.AlignVerticalMask));
    if (orientation == Orientation.Horizontal) {
      if (hAlign != null) {
        el.setProperty(Property.StyleFlex, "0 0 auto");
        DomElement wrap = DomElement.createNew(DomElementType.DIV);
        wrap.setId("w" + el.getId());
        wrap.setProperty(Property.StyleDisplay, this.getStyleDisplay());
        wrap.setProperty(Property.StyleFlexFlow, this.getStyleFlex());
        wrap.addChild(el);
        el = wrap;
        switch (hAlign) {
          case Left:
            el.setProperty(Property.StyleJustifyContent, "flex-start");
            break;
          case Center:
            el.setProperty(Property.StyleJustifyContent, "center");
            break;
          case Right:
            el.setProperty(Property.StyleJustifyContent, "flex-end");
          default:
            break;
        }
      }
      if (vAlign != null) {
        switch (vAlign) {
          case Top:
            el.setProperty(Property.StyleAlignSelf, "flex-start");
            break;
          case Middle:
            el.setProperty(Property.StyleAlignSelf, "center");
            break;
          case Bottom:
            el.setProperty(Property.StyleAlignSelf, "flex-end");
            break;
          case Baseline:
            el.setProperty(Property.StyleAlignSelf, "baseline");
          default:
            break;
        }
      }
    } else {
      if (vAlign != null) {
        el.setProperty(Property.StyleFlex, "0 0 auto");
        DomElement wrap = DomElement.createNew(DomElementType.DIV);
        wrap.setId("w" + el.getId());
        wrap.setProperty(Property.StyleDisplay, this.getStyleDisplay());
        wrap.setProperty(Property.StyleFlexFlow, this.getStyleFlex());
        wrap.addChild(el);
        el = wrap;
        switch (vAlign) {
          case Top:
            el.setProperty(Property.StyleJustifyContent, "flex-start");
            break;
          case Middle:
            el.setProperty(Property.StyleJustifyContent, "center");
            break;
          case Bottom:
            el.setProperty(Property.StyleJustifyContent, "flex-end");
          default:
            break;
        }
      }
      if (hAlign != null) {
        switch (hAlign) {
          case Left:
            el.setProperty(Property.StyleAlignSelf, "flex-start");
            break;
          case Center:
            el.setProperty(Property.StyleAlignSelf, "center");
            break;
          case Right:
            el.setProperty(Property.StyleAlignSelf, "flex-end");
            break;
          default:
            break;
        }
      }
    }
    {
      StringBuilder flexProperty = new StringBuilder();
      int stretch = Math.max(0, s.stretch_);
      int flexGrow = totalStretch == 0 ? 1 : stretch;
      int flexShrink = totalStretch == 0 ? 1 : stretch == 0 ? 0 : 1;
      flexProperty
          .append(flexGrow)
          .append(' ')
          .append(flexShrink)
          .append(' ')
          .append(s.initialSize_.getCssText());
      if (stretch == 0) {
        el.setAttribute("flg", "0");
      }
      el.setProperty(Property.StyleFlex, flexProperty.toString());
    }
    switch (this.getDirection()) {
      case LeftToRight:
        m[3] += (this.grid_.horizontalSpacing_ + 1) / 2;
        m[1] += this.grid_.horizontalSpacing_ / 2;
        break;
      case RightToLeft:
        m[1] += (this.grid_.horizontalSpacing_ + 1) / 2;
        m[3] += this.grid_.horizontalSpacing_ / 2;
        break;
      case TopToBottom:
        m[0] += (this.grid_.horizontalSpacing_ + 1) / 2;
        m[2] += this.grid_.horizontalSpacing_ / 2;
        break;
      case BottomToTop:
        m[2] += (this.grid_.horizontalSpacing_ + 1) / 2;
        m[0] += this.grid_.horizontalSpacing_ / 2;
        break;
    }
    if (m[0] != 0 || m[1] != 0 || m[2] != 0 || m[3] != 0) {
      StringBuilder marginProperty = new StringBuilder();
      marginProperty
          .append(m[0])
          .append("px ")
          .append(m[1])
          .append("px ")
          .append(m[2])
          .append("px ")
          .append(m[3])
          .append("px");
      el.setProperty(Property.StyleMargin, marginProperty.toString());
    }
    return el;
  }

  private Orientation getOrientation() {
    switch (this.getDirection()) {
      case LeftToRight:
      case RightToLeft:
        return Orientation.Horizontal;
      case TopToBottom:
      case BottomToTop:
        return Orientation.Vertical;
    }
    return Orientation.Horizontal;
  }

  private LayoutDirection getDirection() {
    WBoxLayout boxLayout = ObjectUtils.cast(this.getLayout(), WBoxLayout.class);
    if (boxLayout != null) {
      return boxLayout.getDirection();
    } else {
      return LayoutDirection.LeftToRight;
    }
  }

  private String getStyleDisplay() {
    return this.getContainer().isInline() ? "inline-flex" : "flex";
  }

  private String getStyleFlex() {
    switch (this.getDirection()) {
      case LeftToRight:
        return "row";
      case RightToLeft:
        return "row-reverse";
      case TopToBottom:
        return "column";
      case BottomToTop:
        return "column-reverse";
    }
    return "";
  }

  private int count(Orientation orientation) {
    return this.grid_.rows_.size() * this.grid_.columns_.size();
  }

  private int indexOf(WLayoutItem it, Orientation orientation) {
    int c = this.count(orientation);
    for (int i = 0; i < c; ++i) {
      if (this.item(orientation, i).item_ == it) {
        return i;
      }
    }
    return -1;
  }

  private int getTotalStretch(Orientation orientation) {
    int totalStretch = 0;
    int c = this.count(orientation);
    for (int i = 0; i < c; ++i) {
      final Grid.Section s = this.section(orientation, i);
      final Grid.Item it = this.item(orientation, i);
      if (!(it.item_.getWidget() != null) || !it.item_.getWidget().isHidden()) {
        totalStretch += Math.max(0, s.stretch_);
      }
    }
    return totalStretch;
  }

  private Grid.Item item(Orientation orientation, int i) {
    if (orientation == Orientation.Horizontal) {
      return this.grid_.items_.get(0).get(i);
    } else {
      return this.grid_.items_.get(i).get(0);
    }
  }

  private Grid.Section section(Orientation orientation, int i) {
    if (orientation == Orientation.Horizontal) {
      return this.grid_.columns_.get(i);
    } else {
      return this.grid_.rows_.get(i);
    }
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "FlexLayout",
        "function(i,h){function j(){var b=f.getElement(h);if(b){b=b.childNodes;for(var g=0;g<b.length;++g){var e=b[g];if(!(e.style.display==\"none\"||$(e).hasClass(\"out\")||e.className==\"resize-sensor\")){var c=f.css(e,\"overflow\");if(c===\"visible\"||c===\"\")e.style.overflow=\"hidden\"}}}}var f=i.WT;setTimeout(j,0);this.adjust=function(){setTimeout(function(){var b=f.getElement(h);if(b){b=b.childNodes;for(var g=0,e=f.styleAttribute(\"flex-grow\"),c=0;c<b.length;++c){var a= b[c];if(!(a.style.display==\"none\"||$(a).hasClass(\"out\")||a.className==\"resize-sensor\")){var d=a.getAttribute(\"flg\");if(d!==\"0\"){d=f.css(a,e);g+=parseFloat(d)}}}for(c=0;c<b.length;++c){a=b[c];if(!(a.style.display==\"none\"||$(a).hasClass(\"out\")||a.className==\"resize-sensor\")){a.resizeSensor&&a.resizeSensor.trigger();if(g===0)d=1;else{d=a.getAttribute(\"flg\");if(d===\"0\")d=0;else d=d=f.css(a,e)}a.style[e]=d}}}},0)}}");
  }
}
