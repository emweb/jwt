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

class StdGridLayoutImpl2 extends StdLayoutImpl {
  private static Logger logger = LoggerFactory.getLogger(StdGridLayoutImpl2.class);

  public StdGridLayoutImpl2(WLayout layout, final Grid grid) {
    super(layout);
    this.grid_ = grid;
    this.needAdjust_ = false;
    this.needRemeasure_ = false;
    this.needConfigUpdate_ = false;
    this.addedItems_ = new ArrayList<WLayoutItem>();
    this.removedItems_ = new ArrayList<String>();
    String THIS_JS = "js/StdGridLayoutImpl2.js";
    WApplication app = WApplication.getInstance();
    if (!app.isJavaScriptLoaded(THIS_JS)) {
      app.getStyleSheet().addRule("table.Wt-hcenter", "margin: 0px auto;position: relative");
      app.loadJavaScript(THIS_JS, wtjs1());
      app.loadJavaScript(THIS_JS, appjs1());
      app.doJavaScript(app.getJavaScriptClass() + ".layouts2.scheduleAdjust();");
      app.doJavaScript(
          "(function(){var f=function(){"
              + app.getJavaScriptClass()
              + ".layouts2.scheduleAdjust();};if($().jquery.indexOf('1.') === 0)$(window).load(f);else $(window).on('load',f);})();");
      WApplication.getInstance()
          .addAutoJavaScript(
              "if("
                  + app.getJavaScriptClass()
                  + ".layouts2) "
                  + app.getJavaScriptClass()
                  + ".layouts2.adjustNow();");
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
    if (this.needConfigUpdate_) {
      this.needConfigUpdate_ = false;
      DomElement div = DomElement.getForUpdate(this, DomElementType.DIV);
      for (int i = 0; i < this.addedItems_.size(); ++i) {
        WLayoutItem item = this.addedItems_.get(i);
        DomElement c = this.createElement(item, app);
        div.addChild(c);
      }
      this.addedItems_.clear();
      for (int i = 0; i < this.removedItems_.size(); ++i) {
        parent.callJavaScript("Wt4_6_1.remove('" + this.removedItems_.get(i) + "');", true);
      }
      this.removedItems_.clear();
      parent.addChild(div);
      StringBuilder js = new StringBuilder();
      js.append(app.getJavaScriptClass())
          .append(".layouts2.updateConfig('")
          .append(this.getId())
          .append("',");
      this.streamConfig(js, app);
      js.append(");");
      app.doJavaScript(js.toString());
      this.needRemeasure_ = false;
      this.needAdjust_ = false;
    }
    if (this.needRemeasure_) {
      this.needRemeasure_ = false;
      StringBuilder js = new StringBuilder();
      js.append(app.getJavaScriptClass())
          .append(".layouts2.setDirty('")
          .append(this.getId())
          .append("');");
      app.doJavaScript(js.toString());
    }
    if (this.needAdjust_) {
      this.needAdjust_ = false;
      StringBuilder js = new StringBuilder();
      js.append(app.getJavaScriptClass())
          .append(".layouts2.adjust('")
          .append(this.getId())
          .append("', [");
      boolean first = true;
      final int colCount = this.grid_.columns_.size();
      final int rowCount = this.grid_.rows_.size();
      for (int row = 0; row < rowCount; ++row) {
        for (int col = 0; col < colCount; ++col) {
          if (this.grid_.items_.get(row).get(col).update_) {
            this.grid_.items_.get(row).get(col).update_ = false;
            if (!first) {
              js.append(",");
            }
            first = false;
            js.append("[").append((int) row).append(",").append((int) col).append("]");
          }
        }
      }
      js.append("]);");
      app.doJavaScript(js.toString());
    }
    final int colCount = this.grid_.columns_.size();
    final int rowCount = this.grid_.rows_.size();
    for (int i = 0; i < rowCount; ++i) {
      for (int j = 0; j < colCount; ++j) {
        WLayoutItem item = this.grid_.items_.get(i).get(j).item_;
        if (item != null) {
          WLayout nested = item.getLayout();
          if (nested != null) {
            (((nested.getImpl()) instanceof StdLayoutImpl
                    ? (StdLayoutImpl) (nested.getImpl())
                    : null))
                .updateDom(parent);
          }
        }
      }
    }
  }

  public void update() {
    WContainerWidget c = this.getContainer();
    if (c != null) {
      c.layoutChanged(false);
    }
    this.needConfigUpdate_ = true;
  }

  public DomElement createDomElement(
      DomElement parent, boolean fitWidth, boolean fitHeight, WApplication app) {
    this.needAdjust_ = this.needConfigUpdate_ = this.needRemeasure_ = false;
    this.addedItems_.clear();
    this.removedItems_.clear();
    final int colCount = this.grid_.columns_.size();
    final int rowCount = this.grid_.rows_.size();
    int[] margin = {0, 0, 0, 0};
    int maxWidth = 0;
    int maxHeight = 0;
    if (this.getLayout().getParentLayout() == null) {
      if (this.getContainer() == app.getRoot()) {
        app.setBodyClass(app.getBodyClass() + " Wt-layout");
        app.setHtmlClass(app.getHtmlClass() + " Wt-layout");
      }
      margin[3] = this.getLayout().getContentsMargin(Side.Left);
      margin[0] = this.getLayout().getContentsMargin(Side.Top);
      margin[1] = this.getLayout().getContentsMargin(Side.Right);
      margin[2] = this.getLayout().getContentsMargin(Side.Bottom);
      maxWidth = pixelSize(this.getContainer().getMaximumWidth());
      maxHeight = pixelSize(this.getContainer().getMaximumHeight());
    }
    StringBuilder js = new StringBuilder();
    js.append(app.getJavaScriptClass())
        .append(".layouts2.add(new Wt4_6_1.StdLayout2(")
        .append(app.getJavaScriptClass())
        .append(",'")
        .append(this.getId())
        .append("',");
    if (this.getLayout().getParentLayout() != null
        && ((getImpl(this.getLayout().getParentLayout())) instanceof StdGridLayoutImpl2
                ? (StdGridLayoutImpl2) (getImpl(this.getLayout().getParentLayout()))
                : null)
            != null) {
      js.append("'").append(getImpl(this.getLayout().getParentLayout()).getId()).append("',");
    } else {
      js.append("null,");
    }
    boolean progressive = !app.getEnvironment().hasAjax();
    js.append(fitWidth ? '1' : '0')
        .append(",")
        .append(fitHeight ? '1' : '0')
        .append(",")
        .append(progressive ? '1' : '0')
        .append(",");
    js.append(maxWidth)
        .append(",")
        .append(maxHeight)
        .append(",[")
        .append(this.grid_.horizontalSpacing_)
        .append(",")
        .append(margin[3])
        .append(",")
        .append(margin[1])
        .append("],[")
        .append(this.grid_.verticalSpacing_)
        .append(",")
        .append(margin[0])
        .append(",")
        .append(margin[2])
        .append("],");
    this.streamConfig(js, app);
    DomElement div = DomElement.createNew(DomElementType.DIV);
    div.setId(this.getId());
    div.setProperty(Property.StylePosition, "relative");
    DomElement table = null;
    DomElement tbody = null;
    DomElement tr = null;
    if (progressive) {
      table = DomElement.createNew(DomElementType.TABLE);
      StringBuilder style = new StringBuilder();
      if (maxWidth != 0) {
        style.append("max-width: ").append(maxWidth).append("px;");
      }
      if (maxHeight != 0) {
        style.append("max-height: ").append(maxHeight).append("px;");
      }
      style.append("width: 100%;");
      table.setProperty(Property.Style, style.toString());
      int totalColStretch = 0;
      for (int col = 0; col < colCount; ++col) {
        totalColStretch += Math.max(0, this.grid_.columns_.get(col).stretch_);
      }
      for (int col = 0; col < colCount; ++col) {
        DomElement c = DomElement.createNew(DomElementType.COL);
        int stretch = Math.max(0, this.grid_.columns_.get(col).stretch_);
        if (stretch != 0 || totalColStretch == 0) {
          char[] buf = new char[30];
          double pct = totalColStretch == 0 ? 100.0 / colCount : 100.0 * stretch / totalColStretch;
          StringBuilder ss = new StringBuilder();
          ss.append("width:").append(MathUtils.roundCss(pct, 2)).append("%;");
          c.setProperty(Property.Style, ss.toString());
        }
        table.addChild(c);
      }
      tbody = DomElement.createNew(DomElementType.TBODY);
    }
    List<Boolean> overSpanned = new ArrayList<Boolean>();
    {
      int insertPos = 0;
      for (int ii = 0; ii < (colCount * rowCount); ++ii) overSpanned.add(insertPos + ii, false);
    }
    ;
    int prevRowWithItem = -1;
    for (int row = 0; row < rowCount; ++row) {
      if (table != null) {
        tr = DomElement.createNew(DomElementType.TR);
      }
      boolean rowVisible = false;
      int prevColumnWithItem = -1;
      for (int col = 0; col < colCount; ++col) {
        final Grid.Item item = this.grid_.items_.get(row).get(col);
        if (!overSpanned.get(row * colCount + col)) {
          for (int i = 0; i < item.rowSpan_; ++i) {
            for (int j = 0; j < item.colSpan_; ++j) {
              if (i + j > 0) {
                overSpanned.set((row + i) * colCount + col + j, true);
              }
            }
          }
          AlignmentFlag hAlign =
              EnumUtils.enumFromSet(
                  EnumUtils.mask(item.alignment_, AlignmentFlag.AlignHorizontalMask));
          AlignmentFlag vAlign =
              EnumUtils.enumFromSet(
                  EnumUtils.mask(item.alignment_, AlignmentFlag.AlignVerticalMask));
          DomElement td = null;
          if (table != null) {
            boolean itemVisible = this.hasItem(row, col);
            rowVisible = rowVisible || itemVisible;
            td = DomElement.createNew(DomElementType.TD);
            if (itemVisible) {
              int[] padding = {0, 0, 0, 0};
              int nextRow = this.nextRowWithItem(row, col);
              int prevRow = prevRowWithItem;
              int nextCol = this.nextColumnWithItem(row, col);
              int prevCol = prevColumnWithItem;
              if (prevRow == -1) {
                padding[0] = margin[0];
              } else {
                padding[0] = (this.grid_.verticalSpacing_ + 1) / 2;
              }
              if (nextRow == (int) rowCount) {
                padding[2] = margin[2];
              } else {
                padding[2] = this.grid_.verticalSpacing_ / 2;
              }
              if (prevCol == -1) {
                padding[3] = margin[3];
              } else {
                padding[3] = (this.grid_.horizontalSpacing_ + 1) / 2;
              }
              if (nextCol == (int) colCount) {
                padding[1] = margin[1];
              } else {
                padding[1] = this.grid_.horizontalSpacing_ / 2;
              }
              StringBuilder style = new StringBuilder();
              if (app.getLayoutDirection() == LayoutDirection.RightToLeft) {
                int tmp = padding[1];
                padding[1] = padding[3];
                padding[3] = tmp;
              }
              if (padding[0] == padding[1]
                  && padding[0] == padding[2]
                  && padding[0] == padding[3]) {
                if (padding[0] != 0) {
                  style.append("padding:").append(padding[0]).append("px;");
                }
              } else {
                style
                    .append("padding:")
                    .append(padding[0])
                    .append("px ")
                    .append(padding[1])
                    .append("px ")
                    .append(padding[2])
                    .append("px ")
                    .append(padding[3])
                    .append("px;");
              }
              if ((int) vAlign.getValue() != 0) {
                switch (vAlign) {
                  case Top:
                    style.append("vertical-align:top;");
                    break;
                  case Middle:
                    style.append("vertical-align:middle;");
                    break;
                  case Bottom:
                    style.append("vertical-align:bottom;");
                  default:
                    break;
                }
              }
              td.setProperty(Property.Style, style.toString());
              if (item.rowSpan_ != 1) {
                td.setProperty(Property.RowSpan, String.valueOf(item.rowSpan_));
              }
              if (item.colSpan_ != 1) {
                td.setProperty(Property.ColSpan, String.valueOf(item.colSpan_));
              }
              prevColumnWithItem = col;
            }
          }
          DomElement c = null;
          if (!(table != null)) {
            if (item.item_ != null) {
              c = this.createElement(item.item_, app);
              div.addChild(c);
            }
          } else {
            if (item.item_ != null) {
              c = getImpl(item.item_).createDomElement((DomElement) null, true, true, app);
            }
          }
          if (table != null) {
            if (c != null) {
              if (!app.getEnvironment().agentIsIElt(9)) {
                c.setProperty(Property.StyleBoxSizing, "border-box");
              }
              if ((int) hAlign.getValue() == 0) {
                hAlign = AlignmentFlag.Justify;
              }
              switch (hAlign) {
                case Center:
                  {
                    DomElement itable = DomElement.createNew(DomElementType.TABLE);
                    itable.setProperty(Property.Class, "Wt-hcenter");
                    if ((int) vAlign.getValue() == 0) {
                      itable.setProperty(Property.Style, "height:100%;");
                    }
                    DomElement irow = DomElement.createNew(DomElementType.TR);
                    DomElement itd = DomElement.createNew(DomElementType.TD);
                    if ((int) vAlign.getValue() == 0) {
                      itd.setProperty(Property.Style, "height:100%;");
                    }
                    boolean haveMinWidth = c.getProperty(Property.StyleMinWidth).length() != 0;
                    itd.addChild(c);
                    if (app.getEnvironment().agentIsIElt(9)) {
                      if (haveMinWidth) {
                        DomElement spacer = DomElement.createNew(DomElementType.DIV);
                        spacer.setProperty(
                            Property.StyleWidth, c.getProperty(Property.StyleMinWidth));
                        spacer.setProperty(Property.StyleHeight, "1px");
                        itd.addChild(spacer);
                      }
                    }
                    irow.addChild(itd);
                    itable.addChild(irow);
                    c = itable;
                    break;
                  }
                case Right:
                  if (!c.isDefaultInline()) {
                    c.setProperty(Property.StyleFloat, "right");
                  } else {
                    td.setProperty(Property.StyleTextAlign, "right");
                  }
                  break;
                case Left:
                  if (!c.isDefaultInline()) {
                    c.setProperty(Property.StyleFloat, "left");
                  } else {
                    td.setProperty(Property.StyleTextAlign, "left");
                  }
                  break;
                default:
                  break;
              }
              boolean haveMinWidth = c.getProperty(Property.StyleMinWidth).length() != 0;
              td.addChild(c);
              if (app.getEnvironment().agentIsIElt(9)) {
                if (haveMinWidth) {
                  DomElement spacer = DomElement.createNew(DomElementType.DIV);
                  spacer.setProperty(Property.StyleWidth, c.getProperty(Property.StyleMinWidth));
                  spacer.setProperty(Property.StyleHeight, "1px");
                  td.addChild(spacer);
                }
              }
            }
            tr.addChild(td);
          }
        }
      }
      if (tr != null) {
        if (!rowVisible) {
          tr.setProperty(Property.StyleDisplay, "hidden");
        } else {
          prevRowWithItem = row;
        }
        tbody.addChild(tr);
      }
    }
    js.append("));");
    if (table != null) {
      table.addChild(tbody);
      div.addChild(table);
    }
    div.callJavaScript(js.toString());
    if (this.getLayout().getParentLayout() == null) {
      WContainerWidget c = this.getContainer();
      if (c.getPositionScheme() == PositionScheme.Relative
          || c.getPositionScheme() == PositionScheme.Absolute) {
        div.setProperty(Property.StylePosition, "absolute");
        div.setProperty(Property.StyleLeft, "0");
        div.setProperty(Property.StyleRight, "0");
      } else {
        if (app.getEnvironment().agentIsIE()) {
          if (app.getEnvironment().agentIsIE()
              && c.getParent().getPositionScheme() != PositionScheme.Static) {
            parent.setProperty(Property.StylePosition, "relative");
          }
        }
      }
      AlignmentFlag hAlign =
          EnumUtils.enumFromSet(
              EnumUtils.mask(c.getContentAlignment(), AlignmentFlag.AlignHorizontalMask));
      switch (hAlign) {
        case Center:
          {
            DomElement itable = DomElement.createNew(DomElementType.TABLE);
            itable.setProperty(Property.Class, "Wt-hcenter");
            if (fitHeight) {
              itable.setProperty(Property.Style, "height:100%;");
            }
            DomElement irow = DomElement.createNew(DomElementType.TR);
            DomElement itd = DomElement.createNew(DomElementType.TD);
            if (fitHeight) {
              itd.setProperty(Property.Style, "height:100%;");
            }
            itd.addChild(div);
            irow.addChild(itd);
            itable.addChild(irow);
            itable.setId(this.getId() + "l");
            div = itable;
            break;
          }
        case Left:
          break;
        case Right:
          div.setProperty(Property.StyleFloat, "right");
          break;
        default:
          break;
      }
    }
    return div;
  }

  public boolean itemResized(WLayoutItem item) {
    final int colCount = this.grid_.columns_.size();
    final int rowCount = this.grid_.rows_.size();
    for (int row = 0; row < rowCount; ++row) {
      for (int col = 0; col < colCount; ++col) {
        if (this.grid_.items_.get(row).get(col).item_ == item
            && !this.grid_.items_.get(row).get(col).update_) {
          this.grid_.items_.get(row).get(col).update_ = true;
          this.needAdjust_ = true;
          return true;
        }
      }
    }
    return false;
  }

  public boolean isParentResized() {
    if (!this.needRemeasure_) {
      this.needRemeasure_ = true;
      return true;
    } else {
      return false;
    }
  }

  private final Grid grid_;
  private boolean needAdjust_;
  private boolean needRemeasure_;
  private boolean needConfigUpdate_;
  private List<WLayoutItem> addedItems_;
  private List<String> removedItems_;

  private int nextRowWithItem(int row, int c) {
    for (row += this.grid_.items_.get(row).get(c).rowSpan_;
        row < (int) this.grid_.rows_.size();
        ++row) {
      for (int col = 0;
          col < this.grid_.columns_.size();
          col += this.grid_.items_.get(row).get(col).colSpan_) {
        if (this.hasItem(row, col)) {
          return row;
        }
      }
    }
    return this.grid_.rows_.size();
  }

  private int nextColumnWithItem(int row, int col) {
    for (; ; ) {
      col = col + this.grid_.items_.get(row).get(col).colSpan_;
      if (col < (int) this.grid_.columns_.size()) {
        for (int i = 0; i < this.grid_.rows_.size(); ++i) {
          if (this.hasItem(i, col)) {
            return col;
          }
        }
      } else {
        return this.grid_.columns_.size();
      }
    }
  }

  private boolean hasItem(int row, int col) {
    WLayoutItem item = this.grid_.items_.get(row).get(col).item_;
    if (item != null) {
      WWidget w = item.getWidget();
      return !(w != null) || !w.isHidden();
    } else {
      return false;
    }
  }

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

  private static int pixelSize(final WLength size) {
    if (size.getUnit() == LengthUnit.Percentage) {
      return 0;
    } else {
      return (int) size.toPixels();
    }
  }

  private void streamConfig(
      final StringBuilder js, final List<Grid.Section> sections, boolean rows, WApplication app) {
    js.append("[");
    for (int i = 0; i < sections.size(); ++i) {
      if (i != 0) {
        js.append(",");
      }
      js.append("[").append(sections.get(i).stretch_).append(",");
      if (sections.get(i).resizable_) {
        SizeHandle.loadJavaScript(app);
        js.append("[");
        final WLength size = sections.get(i).initialSize_;
        if (size.isAuto()) {
          js.append("-1");
        } else {
          if (size.getUnit() == LengthUnit.Percentage) {
            js.append(size.getValue()).append(",1");
          } else {
            js.append(size.toPixels());
          }
        }
        js.append("],");
      } else {
        js.append("0,");
      }
      if (rows) {
        js.append(this.minimumHeightForRow(i));
      } else {
        js.append(this.minimumWidthForColumn(i));
      }
      js.append("]");
    }
    js.append("]");
  }

  private void streamConfig(final StringBuilder js, WApplication app) {
    js.append("{ rows:");
    this.streamConfig(js, this.grid_.rows_, true, app);
    js.append(", cols:");
    this.streamConfig(js, this.grid_.columns_, false, app);
    js.append(", items: [");
    final int colCount = this.grid_.columns_.size();
    final int rowCount = this.grid_.rows_.size();
    for (int row = 0; row < rowCount; ++row) {
      for (int col = 0; col < colCount; ++col) {
        final Grid.Item item = this.grid_.items_.get(row).get(col);
        AlignmentFlag hAlign =
            EnumUtils.enumFromSet(
                EnumUtils.mask(item.alignment_, AlignmentFlag.AlignHorizontalMask));
        AlignmentFlag vAlign =
            EnumUtils.enumFromSet(EnumUtils.mask(item.alignment_, AlignmentFlag.AlignVerticalMask));
        if (row + col != 0) {
          js.append(",");
        }
        if (item.item_ != null) {
          String id = getImpl(item.item_).getId();
          js.append("{");
          if (item.colSpan_ != 1 || item.rowSpan_ != 1) {
            js.append("span: [")
                .append(item.colSpan_)
                .append(",")
                .append(item.rowSpan_)
                .append("],");
          }
          if (EnumUtils.valueOf(item.alignment_) != 0) {
            int align = 0;
            if (hAlign != null) {
              switch (hAlign) {
                case Left:
                  align |= 0x1;
                  break;
                case Right:
                  align |= 0x2;
                  break;
                case Center:
                  align |= 0x4;
                  break;
                default:
                  break;
              }
            }
            if (vAlign != null) {
              switch (vAlign) {
                case Top:
                  align |= 0x10;
                  break;
                case Bottom:
                  align |= 0x20;
                  break;
                case Middle:
                  align |= 0x40;
                  break;
                default:
                  break;
              }
            }
            js.append("align:").append((int) align).append(",");
          }
          js.append("dirty:")
              .append(this.grid_.items_.get(row).get(col).update_ ? 2 : 0)
              .append(",id:'")
              .append(id)
              .append("'")
              .append("}");
          this.grid_.items_.get(row).get(col).update_ = 0 != 0;
        } else {
          js.append("null");
        }
      }
    }
    js.append("]}");
  }

  private DomElement createElement(WLayoutItem item, WApplication app) {
    DomElement c = getImpl(item).createDomElement((DomElement) null, true, true, app);
    c.setProperty(Property.StyleVisibility, "hidden");
    return c;
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "StdLayout2",
        "function(G,C,K,U,V,W,ca,q,D,y,z){function v(a){var c,b;c=0;for(b=B.items.length;c<b;++c){var f=B.items[c];if(f&&f.id==a)return f}return null}function A(a,c,b,f){function i(j){return j==\"visible\"||j==\"none\"}var m=r[c],l=c?a.scrollHeight:a.scrollWidth,t,d;if(c==0){var g=h.pxself(a,m.left);if(l+g>f.clientWidth||l+g==f.clientWidth&&h.isGecko&&f.parentNode.parentNode.style.visibility===\"hidden\"){t=a.style[m.left];w(a,m.left,\"-1000000px\");l=c?a.scrollHeight: a.scrollWidth}}f=c?a.clientHeight:a.clientWidth;if(h.isGecko&&!a.style[m.size]&&c==0&&i(h.css(a,\"overflow\"))){d=a.style[m.size];w(a,m.size,\"\")}g=c?a.offsetHeight:a.offsetWidth;t&&w(a,m.left,t);d&&w(a,m.size,d);if(f>=1E6)f-=1E6;if(l>=1E6)l-=1E6;if(g>=1E6)g-=1E6;if(l===0){l=h.pxself(a,m.size);if(l!==0&&!h.isOpera&&!h.isGecko)l-=h.px(a,\"border\"+m.Left+\"Width\")+h.px(a,\"border\"+m.Right+\"Width\")}if(h.isIE&&(h.hasTag(a,\"BUTTON\")||h.hasTag(a,\"TEXTAREA\")||h.hasTag(a,\"INPUT\")||h.hasTag(a,\"SELECT\")))l=f;if(l> g)if(h.pxself(a,m.size)==0)l=f;else{var k=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!==\"none\")k=true});if(k)l=f}d=h.px(a,\"border\"+m.Left+\"Width\")+h.px(a,\"border\"+m.Right+\"Width\");t=g-(f+d)!=0;if(b)return[l,scrollBar];if((h.isGecko||h.isWebKit)&&c==0&&a.getBoundingClientRect().width!=Math.ceil(a.getBoundingClientRect().width))l+=1;if(!h.boxSizing(a)&&!h.isOpera)l+=d;l+=h.px(a,\"margin\"+m.Left)+h.px(a,\"margin\"+m.Right);if(!h.boxSizing(a)&&!h.isIE)l+=h.px(a,\"padding\"+m.Left)+h.px(a, \"padding\"+m.Right);l+=g-(f+d);if(l<g)l=g;a=h.px(a,\"max\"+m.Size);if(a>0)l=Math.min(a,l);return[Math.round(l),t]}function x(a,c){c=r[c];if(a.style.display===\"none\")return 0;else if(a[\"layoutMin\"+c.Size])return a[\"layoutMin\"+c.Size];else{var b=h.px(a,\"min\"+c.Size);h.boxSizing(a)||(b+=h.px(a,\"padding\"+c.Left)+h.px(a,\"padding\"+c.Right));return b}}function O(a,c){c=r[c];var b=h.px(a,\"margin\"+c.Left)+h.px(a,\"margin\"+c.Right);if(!h.boxSizing(a)&&!(h.isIE&&!h.isIElt9&&h.hasTag(a,\"BUTTON\")))b+=h.px(a,\"border\"+ c.Left+\"Width\")+h.px(a,\"border\"+c.Right+\"Width\")+h.px(a,\"padding\"+c.Left)+h.px(a,\"padding\"+c.Right);return b}function M(a,c){c=r[c];return h.px(a,\"padding\"+c.Left)+h.px(a,\"padding\"+c.Right)}function ha(a,c){if(h.boxSizing(a)){c=r[c];return h.px(a,\"border\"+c.Left+\"Width\")+h.px(a,\"border\"+c.Right+\"Width\")+h.px(a,\"padding\"+c.Left)+h.px(a,\"padding\"+c.Right)}else return 0}function da(a,c){c=r[c];return Math.round(h.px(a,\"border\"+c.Left+\"Width\")+h.px(a,\"border\"+c.Right+\"Width\")+h.px(a,\"margin\"+c.Left)+ h.px(a,\"margin\"+c.Right)+h.px(a,\"padding\"+c.Left)+h.px(a,\"padding\"+c.Right))}function X(a,c,b){a.dirty=Math.max(a.dirty,c);P=true;b&&G.layouts2.scheduleAdjust()}function w(a,c,b){if(a.style[c]!==b){a.style[c]=b;return true}else return false}function ma(a){return a.style.display===\"none\"&&!a.ed||$(a).hasClass(\"Wt-hidden\")}function va(a,c,b){var f=r[a],i=f.config.length,m=r[a^1].config.length,l=f.measures.slice();if(l.length==5){l[0]=l[0].slice();l[1]=l[1].slice()}if(P){if(b&&typeof f.minSize==\"undefined\"){f.minSize= h.px(b,\"min\"+f.Size);if(f.minSize>0)f.minSize-=ha(b,a)}var t=[],d=[],g,k,j=false;for(g=0;g<i;++g){var H=0,o=f.config[g][2],p=true;for(k=0;k<m;++k){var e=f.getItem(g,k);if(e){if(!e.w||a==0&&e.dirty>1){var n=$(\"#\"+e.id),s=n.get(0);if(!s){f.setItem(g,k,null);continue}if(s!=e.w){e.w=s;n.find(\"img\").add(n.filter(\"img\")).bind(\"load\",{item:e},function(S){X(S.data.item,1,true)})}}if(!W&&e.w.style.position!=\"absolute\"){e.w.style.position=\"absolute\";e.w.style.visibility=\"hidden\"}if(!e.ps)e.ps=[];if(!e.sc)e.sc= [];if(!e.ms)e.ms=[];if(!e.size)e.size=[];if(!e.psize)e.psize=[];if(!e.fs)e.fs=[];if(!e.margin)e.margin=[];s=!e.set;if(!e.set)e.set=[false,false];if(ma(e.w))e.ps[a]=e.ms[a]=0;else if(e.w){if(h.isIE)e.w.style.visibility=\"\";if(e.dirty){if(e.dirty>1){n=x(e.w,a);e.ms[a]=n}else n=e.ms[a];if(e.dirty>1)e.margin[a]=O(e.w,a);if(!e.set[a])if(a==0||!s){s=h.pxself(e.w,f.size);e.fs[a]=s?s+e.margin[a]:0}else{s=Math.round(h.px(e.w,f.size));e.fs[a]=s>Math.max(ha(e.w,a),n)?s+e.margin[a]:0}s=e.fs[a];if(e.layout){if(s== 0)s=e.ps[a];e.ps[a]=s}else{if(e.wasLayout){e.wasLayout=false;e.set=[false,false];e.ps=[];e.w.wtResize&&e.w.wtResize(e.w,-1,-1,true);w(e.w,r[1].size,\"\")}var u=A(e.w,a,false,c),E=u[0],I=e.set[a];if(I)if(e.psize[a]>8)I=E>=e.psize[a]-4&&E<=e.psize[a]+4;var na=typeof e.ps[a]!==\"undefined\"&&f.config[g][0]>0&&e.set[a];s=I||na?Math.max(s,e.ps[a]):Math.max(s,E);e.ps[a]=s;e.sc[a]=u[1]}if(!e.span||e.span[a]==1){if(s>H)H=s;if(n>o)o=n}else j=true}else if(!e.span||e.span[a]==1){if(e.ps[a]>H)H=e.ps[a];if(e.ms[a]> o)o=e.ms[a]}else j=true;if(!ma(e.w)&&(!e.span||e.span[a]==1))p=false}}}if(p)o=H=-1;else if(o>H)H=o;t[g]=H;d[g]=o;if(o>-1){Y+=H;ia+=o}}if(j){function Z(S,ja){for(g=i-1;g>=0;--g)for(k=0;k<m;++k){var aa=f.getItem(g,k);if(aa&&aa.span&&aa.span[a]>1){var ba=S(aa),ea=0,ka=0,F;for(F=0;F<aa.span[a];++F){var Q=ja[g+F];if(Q!=-1){ba-=Q;++ea;if(f.config[g+F][0]>0)ka+=f.config[g+F][0];if(F!=0)ba-=f.margins[0]}}if(ba>=0)if(ea>0){if(ka>0)ea=ka;for(F=0;F<aa.span[a];++F){Q=ja[g+F];if(Q!=-1){Q=ka>0?f.config[g+F][0]: 1;if(Q>0){var ra=Math.round(ba*(Q/ea));ba-=ra;ea-=Q;ja[g+F]+=ra}}}}else ja[g]=ba}}}Z(function(S){return S.ps[a]},t);Z(function(S){return S.ms[a]},d)}var Y=0,ia=0;for(g=0;g<i;++g){if(d[g]>t[g])t[g]=d[g];if(d[g]>-1){Y+=t[g];ia+=d[g]}}c=0;s=true;j=false;for(g=0;g<i;++g)if(d[g]>-1){if(s){c+=f.margins[1];s=false}else{c+=f.margins[0];if(j)c+=4}j=f.config[g][1]!==0}s||(c+=f.margins[2]);Y+=c;ia+=c;f.measures=[t,d,Y,ia,c]}if(fa||l[2]!=f.measures[2])sa.updateSizeInParent(a);b&&f.minSize==0&&l[3]!=f.measures[3]&& b.parentNode.className!=\"Wt-domRoot\"&&w(b,\"min\"+f.Size,f.measures[3]+\"px\");b&&a==0&&b&&h.hasTag(b,\"TD\")&&w(b,f.size,f.measures[2]+\"px\")}function wa(a,c,b){a=r[a];if(ga)b=-b;if(a.config[c][0]>0&&a.config[c+1][0]==0){++c;b=-b}a.fixedSize[c]=a.sizes[c]+b;G.layouts2.scheduleAdjust()}function xa(a,c,b){var f=c.di,i=r[a],m=r[a^1],l,t=h.getElement(C),d;for(d=f-1;d>=0;--d)if(i.sizes[d]>=0){l=-(i.sizes[d]-i.measures[1][d]);break}f=i.sizes[f]-i.measures[1][f];if(ga){var g=l;l=-f;f=-g}new h.SizeHandle(h,i.resizeDir, h.pxself(c,i.size),h.pxself(c,m.size),l,f,i.resizerClass,function(k){wa(a,d,k)},c,t,b,0,0)}function ta(a,c){var b=a.config.length,f;if(a.config[c][1]!==0)for(f=c+1;f<b;++f)if(a.measures[1][f]>-1)return true;for(f=c-1;f>=0;--f)if(a.measures[1][f]>-1)return a.config[f][1]!==0;return false}function ya(a,c){var b=r[a],f=r[a^1],i=b.measures,m=0,l=false,t=false,d=false,g=la?c.parentNode:null;if(b.maxSize===0)if(g){var k=h.css(g,\"position\");if(k===\"absolute\")m=h.pxself(g,b.size);if(m===0){if(!b.initialized){if(a=== 0&&(k===\"absolute\"||k===\"fixed\")){g.style.display=\"none\";m=g.clientWidth;g.style.display=\"\"}m=a?g.clientHeight:g.clientWidth;l=true;if(a==0&&m==0&&h.isIElt9){m=g.offsetWidth;l=false}var j;if((h.hasTag(g,\"TD\")||h.hasTag(g,\"TH\")||$(g.parentNode).hasClass(\"Wt-domRoot\"))&&!(h.isIE&&!h.isIElt9)){d=0;j=1}else{d=b.minSize?b.minSize:i[3];j=0}function H(Z,Y){return Z-Y<=1}if(h.isIElt9&&H(m,j)||H(m,d+M(g,a)))b.maxSize=999999}if(m===0&&b.maxSize===0){m=a?g.clientHeight:g.clientWidth;l=true}}}else{m=h.pxself(c, b.size);t=true}else if(b.sizeSet){m=h.pxself(g,b.size);t=true}var o=0;if(g&&g.wtGetPS&&a==1)o=g.wtGetPS(g,c,a,0);d=i[2];if(d<b.minSize)d=b.minSize;if(b.maxSize&&!b.sizeSet){m=Math.min(d,b.maxSize)+o;w(g,b.size,m+ha(g,a)+\"px\")&&N&&N.setElDirty(J);m=m;d=t=true}b.cSize=m;if(a==1&&g&&g.wtResize){j=f.cSize;d=b.cSize;g.wtResize(g,Math.round(j),Math.round(d),true)}m-=o;if(!t){t=0;if(typeof b.cPadding===\"undefined\"){t=l?M(g,a):ha(g,a);b.cPadding=t}else t=b.cPadding;m-=t}b.initialized=true;if(!(g&&m<=0)){if(m< i[3]-o)m=i[3]-o;g=[];l=b.config.length;t=f.config.length;for(d=0;d<l;++d)b.stretched[d]=false;if(m>=i[3]-o){o=m-i[4];j=[];var p=[0,0],e=[0,0],n=0;for(d=0;d<l;++d)if(i[1][d]>-1){k=-1;ta(b,d)||(b.fixedSize[d]=undefined);if(typeof b.fixedSize[d]!==\"undefined\"&&(d+1==l||i[1][d+1]>-1))k=b.fixedSize[d];else if(ta(b,d)&&b.config[d][1]!==0&&b.config[d][1][0]>=0){k=b.config[d][1][0];if(b.config[d][1][1])k=(m-i[4])*k/100}if(k>=0){j[d]=-1;g[d]=k;o-=g[d]}else{if(b.config[d][0]>0){k=1;j[d]=b.config[d][0];n+=j[d]}else{k= 0;j[d]=0}p[k]+=i[1][d];e[k]+=i[0][d];g[d]=i[0][d]}}else{j[d]=-2;g[d]=-1}if(b.fixedSize.length>l)b.fixedSize.length=l;if(n==0){for(d=0;d<l;++d)if(j[d]==0){j[d]=1;++n}e[1]=e[0];p[1]=p[0];e[0]=0;p[0]=0}if(o>e[0]+p[1]){o-=e[0];if(o>e[1]){if(b.fitSize){o-=e[1];o=o/n;for(d=p=0;d<l;++d)if(j[d]>0){e=p;p+=j[d]*o;g[d]+=Math.round(p)-Math.round(e);b.stretched[d]=true}}}else{k=1;if(o<p[k])o=p[k];o=e[k]-p[k]>0?(o-p[k])/(e[k]-p[k]):0;for(d=p=0;d<l;++d)if(j[d]>0){n=i[0][d]-i[1][d];e=p;p+=n*o;g[d]=i[1][d]+Math.round(p)- Math.round(e)}}}else{for(d=0;d<l;++d)if(j[d]>0)g[d]=i[1][d];o-=p[1];k=0;if(o<p[k])o=p[k];o=e[k]-p[k]>0?(o-p[k])/(e[k]-p[k]):0;for(d=p=0;d<l;++d)if(j[d]==0){n=i[0][d]-i[1][d];e=p;p+=n*o;g[d]=i[1][d]+Math.round(p)-Math.round(e)}}}else g=i[1];b.sizes=g;i=b.margins[1];o=true;p=false;for(d=0;d<l;++d){if(g[d]>-1){var s=p;if(p){p=C+\"-rs\"+a+\"-\"+d;j=h.getElement(p);if(!j){b.resizeHandles[d]=p;j=document.createElement(\"div\");j.setAttribute(\"id\",p);j.di=d;j.style.position=\"absolute\";j.style[f.left]=f.margins[1]+ \"px\";j.style[b.size]=b.margins[0]+\"px\";if(f.cSize)j.style[f.size]=f.cSize-f.margins[2]-f.margins[1]+\"px\";j.className=b.handleClass;c.insertBefore(j,c.firstChild);j.onmousedown=j.ontouchstart=function(Z){xa(a,this,Z||window.event)}}i+=2;w(j,b.left,i+\"px\");i+=2}else if(b.resizeHandles[d]){j=h.getElement(b.resizeHandles[d]);j.parentNode.removeChild(j);b.resizeHandles[d]=undefined}p=b.config[d][1]!==0;if(o)o=false;else i+=b.margins[0]}else if(b.resizeHandles[d]){j=h.getElement(b.resizeHandles[d]);j.parentNode.removeChild(j); b.resizeHandles[d]=undefined}for(e=0;e<t;++e)if((n=b.getItem(d,e))&&n.w){j=n.w;k=Math.max(g[d],0);if(n.span){var u,E=p;for(u=1;u<n.span[a];++u){if(d+u>=g.length)break;if(E)k+=4;E=b.config[d+u][1]!==0;if(g[d+u-1]>-1&&g[d+u]>-1)k+=b.margins[0];k+=g[d+u]}}var I;w(j,\"visibility\",\"\");E=n.align>>b.alignBits&15;u=n.ps[a];if(k<u)E=0;if(E){switch(E){case 1:I=i;break;case 4:I=i+(k-u)/2;break;case 2:I=i+(k-u);break}u-=n.margin[a];if(n.layout){w(j,b.size,u+\"px\")&&X(n,1);n.set[a]=true}else if(k>=u&&n.set[a]){w(j, b.size,u+\"px\")&&X(n,1);n.set[a]=false}n.size[a]=u;n.psize[a]=u}else{I=n.margin[a];E=Math.max(0,k-I);var na=a==0&&n.sc[a];if(!ma(j)&&(na||k!=u||n.layout)){if(w(j,b.size,E+\"px\")){X(n,1);n.set[a]=true}}else if(n.fs[a])a==0&&w(j,b.size,n.fs[a]-I+\"px\");else{w(j,b.size,\"\")&&X(n,1);if(n.set)n.set[a]=false}I=i;n.size[a]=E;n.psize[a]=k}if(W)if(s){w(j,b.left,\"4px\");k=h.css(j,\"position\");if(k!==\"absolute\")j.style.position=\"relative\"}else w(j,b.left,\"0px\");else w(j,b.left,I+\"px\");if(a==1){if(j.wtResize)j.wtResize(j, n.set[0]?Math.round(n.size[0]):-1,n.set[1]?Math.round(n.size[1]):-1,true);n.dirty=0}}if(g[d]>-1)i+=g[d]}if(b.resizeHandles.length>l){for(s=l;s<b.resizeHandles.length;s++)if(b.resizeHandles[s]){j=h.getElement(b.resizeHandles[s]);j.parentNode.removeChild(j)}b.resizeHandles.length=l}$(c).children(\".\"+f.handleClass).css(b.size,m-b.margins[2]-b.margins[1]+\"px\")}}function oa(){var a=h.getElement(C);la=K==null;J=N=null;ua=pa=true;T=[];qa=false;if(la){var c=a,b=c.parentNode;for(T=[0,0];b!=document;){if($(c).hasClass(\"wt-reparented\"))break; T[0]+=da(b,0);T[1]+=da(b,1);if(b.wtGetPS)qa=true;if(c=b.parentNode.wtLayout){J=b;N=c;break}c=b;b=c.parentNode;if(b.childNodes.length!=1&&!b.wtGetPS)pa=false}a=a.parentNode;for(b=0;b<2;++b)r[b].sizeSet=h.pxself(a,r[b].size)!=0}else{N=document.getElementById(K).wtLayout;J=a;T[0]=da(J,0);T[1]=da(J,1)}}var h=G.WT;this.descendants=[];var sa=this,B=z,P=true,fa=true,la,N,J,pa,ua=false,T,qa,ga=$(document.body).hasClass(\"Wt-rtl\"),r=[{initialized:false,config:B.cols,margins:D,maxSize:ca,measures:[],sizes:[], stretched:[],fixedSize:[],Left:ga?\"Right\":\"Left\",left:ga?\"right\":\"left\",Right:ga?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,c){return B.items[c*r[0].config.length+a]},setItem:function(a,c,b){B.items[c*r[0].config.length+a]=b},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:U,resizeHandles:[]},{initialized:false,config:B.rows,margins:y,maxSize:q,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\", alignBits:4,getItem:function(a,c){return B.items[a*r[0].config.length+c]},setItem:function(a,c,b){B.items[a*r[0].config.length+c]=b},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:V,resizeHandles:[]}];document.getElementById(C).wtLayout=this;this.updateSizeInParent=function(a){if(N&&J.id){var c=h.$(J.id);if(c){if(J!=c)if(N=c.parentNode.wtLayout)J=c;else oa()}else oa()}if(N)if(pa){var b=r[a];c=b.measures[2];if(b.maxSize>0)c=Math.min(b.maxSize,c);if(qa){b=h.getElement(C);if(!b)return; for(var f=b,i=f.parentNode;;){if(i.wtGetPS)c=i.wtGetPS(i,f,a,c);c+=da(i,a);if(i==J)break;if(a==1&&i==b.parentNode&&!i.lh&&i.offsetHeight>c)c=i.offsetHeight;f=i;i=f.parentNode}}else c+=T[a];N.setChildSize(J,a,c)}};this.setConfig=function(a){var c=B;B=a;r[0].config=B.cols;r[1].config=B.rows;r[0].stretched=[];r[1].stretched=[];var b;a=0;for(b=c.items.length;a<b;++a){var f=c.items[a];if(f){var i=v(f.id);if(i){i.ps=f.ps;i.sc=f.sc;i.ms=f.ms;i.size=f.size;i.psize=f.psize;i.fs=f.fs;i.margin=f.margin;i.set= f.set}else if(f.set){f.set[0]&&w(f.w,r[0].size,\"\");f.set[1]&&w(f.w,r[1].size,\"\")}}}P=fa=true;G.layouts2.scheduleAdjust()};this.getId=function(){return C};this.setElDirty=function(a){if(a=v(a.id)){a.dirty=2;P=true;G.layouts2.scheduleAdjust()}};this.setItemsDirty=function(a){var c,b,f=r[0].config.length;c=0;for(b=a.length;c<b;++c){var i=B.items[a[c][0]*f+a[c][1]];if(i){i.dirty=2;if(i.layout){i.layout=false;i.wasLayout=true;G.layouts2.setChildLayoutsDirty(sa,i.w)}}}P=true};this.setDirty=function(){fa= true};this.setAllDirty=function(){var a,c;a=0;for(c=B.items.length;a<c;++a){var b=B.items[a];if(b)b.dirty=2}P=true};this.setChildSize=function(a,c,b){var f=r[0].config.length,i=r[c],m;if(a=v(a.id)){f=c===0?m%f:m/f;if(a.align>>i.alignBits&15||!i.stretched[f]){if(!a.ps)a.ps=[];a.ps[c]=b}a.layout=true;X(a,1)}};this.measure=function(a){var c=h.getElement(C);if(c)if(!h.isHidden(c)){ua||oa();if(P||fa)va(a,c,la?c.parentNode:null);if(a==1)P=fa=false}};this.setMaxSize=function(a,c){r[0].maxSize=a;r[1].maxSize= c};this.apply=function(a){var c=h.getElement(C);if(!c)return false;if(h.isHidden(c))return true;ya(a,c);return true};this.contains=function(a){var c=h.getElement(C);a=h.getElement(a.getId());return c&&a?h.contains(c,a):false};this.WT=h}");
  }

  static WJavaScriptPreamble appjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.ApplicationScope,
        JavaScriptObjectType.JavaScriptObject,
        "layouts2",
        "new (function(){var G=[],C=false,K=this,U=false;this.find=function(q){return(q=document.getElementById(q))?q.wtLayout:null};this.setDirty=function(q){if(q=this.find(q)){q.setDirty();K.scheduleAdjust()}};this.setElementDirty=function(q){var D=q;for(q=q.parentNode;q&&q!=document.body;){var y=q.wtLayout;y&&y.setElDirty(D);D=q;q=q.parentNode}};this.setChildLayoutsDirty=function(q,D){var y,z;y=0;for(z=q.descendants.length;y<z;++y){var v=q.descendants[y]; if(D){var A=q.WT.getElement(v.getId());if(A&&!q.WT.contains(D,A))continue}v.setDirty()}};this.add=function(q){function D(y,z){var v,A;v=0;for(A=y.length;v<A;++v){var x=y[v];if(x.getId()==z.getId()){y[v]=z;z.descendants=x.descendants;return}else if(x.contains(z)){D(x.descendants,z);return}else if(z.contains(x)){z.descendants.push(x);y.splice(v,1);--v;--A}}y.push(z)}D(G,q);K.scheduleAdjust()};var V=false,W=0;this.scheduleAdjust=function(q){if(q)U=true;if(!V){if(C)++W;else W=0;if(!(W>=6)){V=true;setTimeout(function(){K.adjust()}, 0)}}};this.adjust=function(q,D){function y(v,A){var x,O;x=0;for(O=v.length;x<O;++x){var M=v[x];y(M.descendants,A);if(A==1&&U)M.setDirty();else A==0&&M.setAllDirty();M.measure(A)}}function z(v,A){var x,O;x=0;for(O=v.length;x<O;++x){var M=v[x];if(M.apply(A))z(M.descendants,A);else{v.splice(x,1);--x;--O}}}if(q){(q=this.find(q))&&q.setItemsDirty(D);K.scheduleAdjust()}else{V=false;if(!C){C=true;y(G,0);z(G,0);y(G,1);z(G,1);U=C=false}}};this.updateConfig=function(q,D){(q=this.find(q))&&q.setConfig(D)};this.adjustNow= function(){V&&K.adjust()};var ca=null;window.onresize=function(){clearTimeout(ca);ca=setTimeout(function(){ca=null;K.scheduleAdjust(true)},20)};window.onshow=function(){U=true;K.adjust()}})");
  }

  static WJavaScriptPreamble wtjs10() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "ChildrenResize",
        "function(a,f,d,e){function h(i){var j=b.px(i,\"marginTop\");j+=b.px(i,\"marginBottom\");if(!b.boxSizing(i)){j+=b.px(i,\"borderTopWidth\");j+=b.px(i,\"borderBottomWidth\");j+=b.px(i,\"paddingTop\");j+=b.px(i,\"paddingBottom\")}return j}var b=this,l=d>=0;if(e)if(l){a.style.height=d+\"px\";a.lh=true}else{a.style.height=\"\";a.lh=false}else a.lh=false;if(b.boxSizing(a)){d-=b.px(a,\"marginTop\");d-=b.px(a,\"marginBottom\");d-=b.px(a,\"borderTopWidth\");d-=b.px(a,\"borderBottomWidth\"); d-=b.px(a,\"paddingTop\");d-=b.px(a,\"paddingBottom\");f-=b.px(a,\"marginLeft\");f-=b.px(a,\"marginRight\");f-=b.px(a,\"borderLeftWidth\");f-=b.px(a,\"borderRightWidth\");f-=b.px(a,\"paddingLeft\");f-=b.px(a,\"paddingRight\")}var g,c;e=0;for(g=a.childNodes.length;e<g;++e){c=a.childNodes[e];if(c.nodeType==1&&!$(c).hasClass(\"wt-reparented\"))if(l){var k=d-h(c);if(k>0){if(c.offsetTop>0){var m=b.css(c,\"overflow\");if(m===\"visible\"||m===\"\")c.style.overflow=\"auto\"}if(c.wtResize)c.wtResize(c,f,k,true);else{k=k+\"px\";if(c.style.height!= k){c.style.height=k;c.lh=true}}}}else if(c.wtResize)c.wtResize(c,f,-1,true);else{c.style.height=\"\";c.lh=false}}}");
  }

  static WJavaScriptPreamble wtjs11() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "ChildrenGetPS",
        "function(a,f,d,e){return e}");
  }

  static WJavaScriptPreamble wtjs12() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastResize",
        "function(a,f,d,e){var h=this,b=d>=0;if(e)if(b){a.style.height=d+\"px\";a.lh=true}else{a.style.height=\"\";a.lh=false}else a.lh=false;for(a=a.lastChild;a&&a.nodeType==1&&($(a).hasClass(\"wt-reparented\")||$(a).hasClass(\"resize-sensor\"));)a=a.previousSibling;if(a){e=a.previousSibling;if(b){d-=e.offsetHeight+h.px(e,\"marginTop\")+h.px(e,\"marginBottom\");if(d>0)if(a.wtResize)a.wtResize(a,f,d,true);else{a.style.height=d+\"px\";a.lh=true}}else if(a.wtResize)a.wtResize(a, -1,-1,true);else{a.style.height=\"\";a.lh=false}}}");
  }

  static WJavaScriptPreamble wtjs13() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastGetPS",
        "function(a,f,d,e){var h=this,b,l;b=0;for(l=a.childNodes.length;b<l;++b){var g=a.childNodes[b];if(g!=f){var c=h.css(g,\"position\");if(c!=\"absolute\"&&c!=\"fixed\")if(d===0)e=Math.max(e,g.offsetWidth);else e+=g.offsetHeight+h.px(g,\"marginTop\")+h.px(g,\"marginBottom\")}}return e}");
  }
}
