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
        parent.callJavaScript("Wt4_8_1.remove('" + this.removedItems_.get(i) + "');", true);
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
            (ObjectUtils.cast(nested.getImpl(), StdLayoutImpl.class)).updateDom(parent);
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
        .append(".layouts2.add(new Wt4_8_1.StdLayout2(")
        .append(app.getJavaScriptClass())
        .append(",'")
        .append(this.getId())
        .append("',");
    if (this.getLayout().getParentLayout() != null
        && ObjectUtils.cast(getImpl(this.getLayout().getParentLayout()), StdGridLayoutImpl2.class)
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
        "(function(e,i,t,s,n,r,a,f,o,l,d){var u=e.WT;this.descendants=[];var h,c,g,p,m,z,v=1e6,x=this,y=d,S=!0,w=!0,E=!1,W=$(document.body).hasClass(\"Wt-rtl\"),b=[{initialized:!1,config:y.cols,margins:o,maxSize:a,measures:[],sizes:[],stretched:[],fixedSize:[],Left:W?\"Right\":\"Left\",left:W?\"right\":\"left\",Right:W?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(e,i){return y.items[i*b[0].config.length+e]},setItem:function(e,i,t){y.items[i*b[0].config.length+e]=t},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:s,resizeHandles:[]},{initialized:!1,config:y.rows,margins:l,maxSize:f,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(e,i){return y.items[e*b[0].config.length+i]},setItem:function(e,i,t){y.items[e*b[0].config.length+i]=t},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:n,resizeHandles:[]}];document.getElementById(i).wtLayout=this;function L(e){var i,t;for(i=0,t=y.items.length;i<t;++i){var s=y.items[i];if(s&&s.id==e)return s}return null}function R(e,i,t,s){var n,r,a=b[i],f=i?e.scrollHeight:e.scrollWidth;if(0==i){var o=u.pxself(e,a.left);if(f+o>s.clientWidth||f+o==s.clientWidth&&u.isGecko&&\"hidden\"===s.parentNode.parentNode.style.visibility){n=e.style[a.left];D(e,a.left,\"-1000000px\");f=i?e.scrollHeight:e.scrollWidth}}var l,d=i?e.clientHeight:e.clientWidth;if(u.isGecko&&!e.style[a.size]&&0==i&&(\"visible\"==(l=u.css(e,\"overflow\"))||\"none\"==l)){r=e.style[a.size];D(e,a.size,\"\")}var h=i?e.offsetHeight:e.offsetWidth;n&&D(e,a.left,n);r&&D(e,a.size,r);d>=v&&(d-=v);f>=v&&(f-=v);h>=v&&(h-=v);0===f&&(0===(f=u.pxself(e,a.size))||u.isOpera||u.isGecko||(f-=u.px(e,\"border\"+a.Left+\"Width\")+u.px(e,\"border\"+a.Right+\"Width\")));u.isIE&&(u.hasTag(e,\"BUTTON\")||u.hasTag(e,\"TEXTAREA\")||u.hasTag(e,\"INPUT\")||u.hasTag(e,\"SELECT\"))&&(f=d);if(f>h)if(0==u.pxself(e,a.size))f=d;else{var c=!1;$(e).find(\".Wt-popup\").each((function(e){\"none\"!==this.style.display&&(c=!0)}));c&&(f=d)}var g=u.px(e,\"border\"+a.Left+\"Width\")+u.px(e,\"border\"+a.Right+\"Width\"),p=h-(d+g)!=0;if(t)return[f,scrollBar];(u.isGecko||u.isWebKit)&&0==i&&e.getBoundingClientRect().width!=Math.ceil(e.getBoundingClientRect().width)&&(f+=1);u.boxSizing(e)||u.isOpera||(f+=g);f+=u.px(e,\"margin\"+a.Left)+u.px(e,\"margin\"+a.Right);u.boxSizing(e)||u.isIE||(f+=u.px(e,\"padding\"+a.Left)+u.px(e,\"padding\"+a.Right));(f+=h-(d+g))<h&&(f=h);var m=u.px(e,\"max\"+a.Size);m>0&&(f=Math.min(m,f));return[Math.round(f),p]}function M(e,i){var t=b[i];if(\"none\"===e.style.display)return 0;if(e[\"layoutMin\"+t.Size])return e[\"layoutMin\"+t.Size];var s=u.px(e,\"min\"+t.Size);u.boxSizing(e)||(s+=u.px(e,\"padding\"+t.Left)+u.px(e,\"padding\"+t.Right));return s}function I(e,i){var t=b[i],s=u.px(e,\"margin\"+t.Left)+u.px(e,\"margin\"+t.Right);u.boxSizing(e)||u.isIE&&!u.isIElt9&&u.hasTag(e,\"BUTTON\")||(s+=u.px(e,\"border\"+t.Left+\"Width\")+u.px(e,\"border\"+t.Right+\"Width\")+u.px(e,\"padding\"+t.Left)+u.px(e,\"padding\"+t.Right));return s}function T(e,i){var t=b[i];return u.px(e,\"padding\"+t.Left)+u.px(e,\"padding\"+t.Right)}function C(e,i){if(u.boxSizing(e)){var t=b[i];return u.px(e,\"border\"+t.Left+\"Width\")+u.px(e,\"border\"+t.Right+\"Width\")+u.px(e,\"padding\"+t.Left)+u.px(e,\"padding\"+t.Right)}return 0}function H(e,i){var t=b[i];return Math.round(u.px(e,\"border\"+t.Left+\"Width\")+u.px(e,\"border\"+t.Right+\"Width\")+u.px(e,\"margin\"+t.Left)+u.px(e,\"margin\"+t.Right)+u.px(e,\"padding\"+t.Left)+u.px(e,\"padding\"+t.Right))}function N(i,t,s){i.dirty=Math.max(i.dirty,t);S=!0;s&&e.layouts2.scheduleAdjust()}function D(e,i,t){if(e.style[i]!==t){e.style[i]=t;return!0}return!1}function A(e){return\"none\"===e.style.display&&!e.ed||$(e).hasClass(\"Wt-hidden\")}this.updateSizeInParent=function(e){if(c&&g.id){var t=u.$(g.id);t?g!=t&&((c=t.parentNode.wtLayout)?g=t:P()):P()}if(c&&p){var s=b[e],n=s.measures[2];s.maxSize>0&&(n=Math.min(s.maxSize,n));if(z){var r=u.getElement(i);if(!r)return;for(var a=r,f=a.parentNode;;){f.wtGetPS&&(n=f.wtGetPS(f,a,e,n));n+=H(f,e);if(f==g)break;1==e&&f==r.parentNode&&!f.lh&&f.offsetHeight>n&&(n=f.offsetHeight);f=(a=f).parentNode}}else n+=m[e];c.setChildSize(g,e,n)}};function B(t,s,n){var r,a,f,o=s.di,l=b[t],d=b[1^t],h=u.getElement(i);for(f=o-1;f>=0;--f)if(l.sizes[f]>=0){r=-(l.sizes[f]-l.measures[1][f]);break}a=l.sizes[o]-l.measures[1][o];if(W){var c=r;r=-a;a=-c}new u.SizeHandle(u,l.resizeDir,u.pxself(s,l.size),u.pxself(s,d.size),r,a,l.resizerClass,(function(i){!function(i,t,s){var n=b[i];W&&(s=-s);if(n.config[t][0]>0&&0==n.config[t+1][0]){++t;s=-s}n.fixedSize[t]=n.sizes[t]+s;e.layouts2.scheduleAdjust()}(t,f,i)}),s,h,n,0,0)}function j(e,i){var t,s=e.config.length;if(0!==e.config[i][1])for(t=i+1;t<s;++t)if(e.measures[1][t]>-1)return!0;for(t=i-1;t>=0;--t)if(e.measures[1][t]>-1)return 0!==e.config[t][1];return!1}this.setConfig=function(i){var t,s,n=y;y=i;b[0].config=y.cols;b[1].config=y.rows;b[0].stretched=[];b[1].stretched=[];for(t=0,s=n.items.length;t<s;++t){var r=n.items[t];if(r){var a=L(r.id);if(a){a.ps=r.ps;a.sc=r.sc;a.ms=r.ms;a.size=r.size;a.psize=r.psize;a.fs=r.fs;a.margin=r.margin;a.set=r.set}else if(r.set){r.set[0]&&D(r.w,b[0].size,\"\");r.set[1]&&D(r.w,b[1].size,\"\")}}}w=!0;S=!0;e.layouts2.scheduleAdjust()};this.getId=function(){return i};this.setElDirty=function(i){var t=L(i.id);if(t){t.dirty=2;S=!0;e.layouts2.scheduleAdjust()}};this.setItemsDirty=function(i){var t,s,n=b[0].config.length;for(t=0,s=i.length;t<s;++t){var r=i[t][0],a=i[t][1],f=y.items[r*n+a];if(f){f.dirty=2;if(f.layout){f.layout=!1;f.wasLayout=!0;e.layouts2.setChildLayoutsDirty(x,f.w)}}}S=!0};this.setDirty=function(){w=!0};this.setAllDirty=function(){var e,i;for(e=0,i=y.items.length;e<i;++e){var t=y.items[e];t&&(t.dirty=2)}S=!0};this.setChildSize=function(e,i,t){var s,n=b[0].config.length,r=b[i],a=L(e.id);if(a){var f=0===i?s%n:s/n;if(a.align>>r.alignBits&15||!r.stretched[f]){a.ps||(a.ps=[]);a.ps[i]=t}a.layout=!0;N(a,1)}};function P(){var e=u.getElement(i);c=null;g=null;p=!0;E=!0;m=[];z=!1;if(h=null==t){var s=e,n=s.parentNode;m=[0,0];for(;n!=document&&!$(s).hasClass(\"wt-reparented\");){m[0]+=H(n,0);m[1]+=H(n,1);n.wtGetPS&&(z=!0);var r=n.parentNode.wtLayout;if(r){g=n;c=r;break}1==(n=(s=n).parentNode).childNodes.length||n.wtGetPS||(p=!1)}for(var a=e.parentNode,f=0;f<2;++f)b[f].sizeSet=0!=u.pxself(a,b[f].size)}else{c=document.getElementById(t).wtLayout;g=e;m[0]=H(g,0);m[1]=H(g,1)}}this.measure=function(e){var t=u.getElement(i);if(t&&!u.isHidden(t)){E||P();if(S||w){!function(e,i,t){var s=b[e],n=b[1^e],a=s.measures,f=s.config.length,o=n.config.length,l=a.slice();if(5==l.length){l[0]=l[0].slice();l[1]=l[1].slice()}if(S){if(t&&void 0===s.minSize){s.minSize=u.px(t,\"min\"+s.Size);s.minSize>0&&(s.minSize-=C(t,e))}var d,h,c=[],g=[],p=!1;for(d=0;d<f;++d){var m=0,z=s.config[d][2],v=!0;for(h=0;h<o;++h){var y=s.getItem(d,h);if(y){if(!y.w||0==e&&y.dirty>1){var E=$(\"#\"+y.id),W=E.get(0);if(!W){s.setItem(d,h,null);continue}if(W!=y.w){y.w=W;E.find(\"img\").add(E.filter(\"img\")).bind(\"load\",{item:y},(function(e){N(e.data.item,1,!0)}))}}if(!r&&\"absolute\"!=y.w.style.position){y.w.style.position=\"absolute\";y.w.style.visibility=\"hidden\"}y.ps||(y.ps=[]);y.sc||(y.sc=[]);y.ms||(y.ms=[]);y.size||(y.size=[]);y.psize||(y.psize=[]);y.fs||(y.fs=[]);y.margin||(y.margin=[]);var L=!y.set;y.set||(y.set=[!1,!1]);if(A(y.w)){y.ps[e]=y.ms[e]=0;continue}if(y.w){u.isIE&&(y.w.style.visibility=\"\");if(y.dirty){var T;if(y.dirty>1){T=M(y.w,e);y.ms[e]=T}else T=y.ms[e];y.dirty>1&&(y.margin[e]=I(y.w,e));if(!y.set[e])if(0!=e&&L)(H=Math.round(u.px(y.w,s.size)))>Math.max(C(y.w,e),T)?y.fs[e]=H+y.margin[e]:y.fs[e]=0;else{var H=u.pxself(y.w,s.size);y.fs[e]=H?H+y.margin[e]:0}y.align,s.alignBits;var B=y.fs[e];if(y.layout){0==B&&(B=y.ps[e]);y.ps[e]=B}else{if(y.wasLayout){y.wasLayout=!1;y.set=[!1,!1];y.ps=[];y.w.wtResize&&y.w.wtResize(y.w,-1,-1,!0);D(y.w,b[1].size,\"\")}var j=R(y.w,e,!1,i),P=j[0],G=y.set[e];G&&y.psize[e]>8&&(G=P>=y.psize[e]-4&&P<=y.psize[e]+4);var k=void 0!==y.ps[e]&&s.config[d][0]>0&&y.set[e];B=G||k?Math.max(B,y.ps[e]):Math.max(B,P);y.ps[e]=B;y.sc[e]=j[1]}if(y.span&&1!=y.span[e])p=!0;else{B>m&&(m=B);T>z&&(z=T)}}else if(y.span&&1!=y.span[e])p=!0;else{y.ps[e]>m&&(m=y.ps[e]);y.ms[e]>z&&(z=y.ms[e])}A(y.w)||y.span&&1!=y.span[e]||(v=!1)}}}v?z=m=-1:z>m&&(m=z);c[d]=m;g[d]=z;if(z>-1){O+=m;U+=z}}if(p){function _(i,t){for(d=f-1;d>=0;--d)for(h=0;h<o;++h){var n=s.getItem(d,h);if(n&&n.span&&n.span[e]>1){var r,a=i(n),l=0,u=0;for(r=0;r<n.span[e];++r)if(-1!=(c=t[d+r])){a-=c;++l;s.config[d+r][0]>0&&(u+=s.config[d+r][0]);0!=r&&(a-=s.margins[0])}if(a>=0)if(l>0){u>0&&(l=u);for(r=0;r<n.span[e];++r){var c;if(-1!=(c=t[d+r])){var g;if((g=u>0?s.config[d+r][0]:1)>0){var p=Math.round(a*(g/l));a-=p;l-=g;t[d+r]+=p}}}}else t[d]=a}}}_((function(i){return i.ps[e]}),c);_((function(i){return i.ms[e]}),g)}var O=0,U=0;for(d=0;d<f;++d){g[d]>c[d]&&(c[d]=g[d]);if(g[d]>-1){O+=c[d];U+=g[d]}}var J=0,K=(L=!0,!1);for(d=0;d<f;++d)if(g[d]>-1){if(L){J+=s.margins[1];L=!1}else{J+=s.margins[0];K&&(J+=4)}K=0!==s.config[d][1]}L||(J+=s.margins[2]);O+=J;U+=J;s.measures=[c,g,O,U,J]}(w||l[2]!=s.measures[2])&&x.updateSizeInParent(e);if(t&&0==s.minSize&&l[3]!=s.measures[3]&&\"Wt-domRoot\"!=t.parentNode.className){var X=s.measures[3]+\"px\";D(t,\"min\"+s.Size,X)}t&&0==e&&t&&u.hasTag(t,\"TD\")&&D(t,s.size,s.measures[2]+\"px\")}(e,t,h?t.parentNode:null)}1==e&&(S=w=!1)}};this.setMaxSize=function(e,i){b[0].maxSize=e;b[1].maxSize=i};this.apply=function(e){var t=u.getElement(i);if(!t)return!1;if(u.isHidden(t))return!0;!function(e,t){var s=b[e],n=b[1^e],a=s.measures,f=0,o=!1,l=!1,d=h?t.parentNode:null;h&&d.parentNode;if(0===s.maxSize)if(d){\"absolute\"===(le=u.css(d,\"position\"))&&(f=u.pxself(d,s.size));if(0===f){if(!s.initialized){if(0===e&&(\"absolute\"===le||\"fixed\"===le)){d.style.display=\"none\";f=d.clientWidth;d.style.display=\"\"}f=e?d.clientHeight:d.clientWidth;o=!0;if(0==e&&0==f&&u.isIElt9){f=d.offsetWidth;o=!1}var p,m;if(!(u.hasTag(d,\"TD\")||u.hasTag(d,\"TH\")||$(d.parentNode).hasClass(\"Wt-domRoot\"))||u.isIE&&!u.isIElt9){p=s.minSize?s.minSize:a[3];m=0}else{p=0;m=1}function z(e,i){return e-i<=1}(u.isIElt9&&z(f,m)||z(f,p+T(d,e)))&&(s.maxSize=999999)}if(0===f&&0===s.maxSize){f=e?d.clientHeight:d.clientWidth;o=!0}}}else{f=u.pxself(t,s.size);l=!0}else if(s.sizeSet){f=u.pxself(d,s.size);l=!0}var v=0;d&&d.wtGetPS&&1==e&&(v=d.wtGetPS(d,t,e,0));var x=a[2];x<s.minSize&&(x=s.minSize);if(s.maxSize&&!s.sizeSet){var y=Math.min(x,s.maxSize)+v;D(d,s.size,y+C(d,e)+\"px\")&&c&&c.setElDirty(g);f=y;l=!0}s.cSize=f;if(1==e&&d&&d.wtResize){var S=n.cSize,w=s.cSize;d.wtResize(d,Math.round(S),Math.round(w),!0)}f-=v;if(!l){var E=0;if(void 0===s.cPadding){E=o?T(d,e):C(d,e);s.cPadding=E}else E=s.cPadding;f-=E}s.initialized=!0;if(!(d&&f<=0)){f<a[3]-v&&(f=a[3]-v);var W=[],L=s.config.length,R=n.config.length;for(k=0;k<L;++k)s.stretched[k]=!1;if(f>=a[3]-v){for(var M=f-a[4],I=[],H=[0,0],P=[0,0],G=0,k=0;k<L;++k)if(a[1][k]>-1){var _=-1;j(s,k)||(s.fixedSize[k]=void 0);if(void 0!==s.fixedSize[k]&&(k+1==L||a[1][k+1]>-1))_=s.fixedSize[k];else if(j(s,k)&&0!==s.config[k][1]&&s.config[k][1][0]>=0){_=s.config[k][1][0];s.config[k][1][1]&&(_=(f-a[4])*_/100)}if(_>=0){I[k]=-1;W[k]=_;M-=W[k]}else{if(s.config[k][0]>0){K=1;I[k]=s.config[k][0];G+=I[k]}else{K=0;I[k]=0}H[K]+=a[1][k];P[K]+=a[0][k];W[k]=a[0][k]}}else{I[k]=-2;W[k]=-1}s.fixedSize.length>L&&(s.fixedSize.length=L);if(0==G){for(k=0;k<L;++k)if(0==I[k]){I[k]=1;++G}P[1]=P[0];H[1]=H[0];P[0]=0;H[0]=0}if(M>P[0]+H[1])if((M-=P[0])>P[1]){if(s.fitSize){var O=(M-=P[1])/G,U=0;for(k=0;k<L;++k)if(I[k]>0){var J=U;U+=I[k]*O;W[k]+=Math.round(U)-Math.round(J);s.stretched[k]=!0}}}else{M<H[K=1]&&(M=H[K]);O=P[K]-H[K]>0?(M-H[K])/(P[K]-H[K]):0;U=0;for(k=0;k<L;++k)if(I[k]>0){J=U;U+=(a[0][k]-a[1][k])*O;W[k]=a[1][k]+Math.round(U)-Math.round(J)}}else{for(k=0;k<L;++k)I[k]>0&&(W[k]=a[1][k]);var K;(M-=H[1])<H[K=0]&&(M=H[K]);O=P[K]-H[K]>0?(M-H[K])/(P[K]-H[K]):0;U=0;for(k=0;k<L;++k)if(0==I[k]){J=U;U+=(a[0][k]-a[1][k])*O;W[k]=a[1][k]+Math.round(U)-Math.round(J)}}}else W=a[1];s.sizes=W;var X,q=s.margins[1],F=!0,Q=!1;for(k=0;k<L;++k){if(W[k]>-1){var V=Q;if(Q){var Y=i+\"-rs\"+e+\"-\"+k;if(!(ue=u.getElement(Y))){s.resizeHandles[k]=Y;(ue=document.createElement(\"div\")).setAttribute(\"id\",Y);ue.di=k;ue.style.position=\"absolute\";ue.style[n.left]=n.margins[1]+\"px\";ue.style[s.size]=s.margins[0]+\"px\";n.cSize&&(ue.style[n.size]=n.cSize-n.margins[2]-n.margins[1]+\"px\");ue.className=s.handleClass;t.insertBefore(ue,t.firstChild);ue.onmousedown=ue.ontouchstart=function(i){var t=i||window.event;B(e,this,t)}}q+=2;D(ue,s.left,q+\"px\");q+=2}else if(s.resizeHandles[k]){(ue=u.getElement(s.resizeHandles[k])).parentNode.removeChild(ue);s.resizeHandles[k]=void 0}Q=0!==s.config[k][1];F?F=!1:q+=s.margins[0]}else if(s.resizeHandles[k]){(ue=u.getElement(s.resizeHandles[k])).parentNode.removeChild(ue);s.resizeHandles[k]=void 0}for(X=0;X<R;++X){var Z=s.getItem(k,X);if(Z&&Z.w){S=Z.w;var ee,ie=Math.max(W[k],0);if(Z.span){var te,se=Q;for(te=1;te<Z.span[e]&&!(k+te>=W.length);++te){se&&(ie+=4);se=0!==s.config[k+te][1];W[k+te-1]>-1&&W[k+te]>-1&&(ie+=s.margins[0]);ie+=W[k+te]}}D(S,\"visibility\",\"\");var ne=Z.align>>s.alignBits&15,re=Z.ps[e];ie<re&&(ne=0);if(ne){switch(ne){case 1:ee=q;break;case 4:ee=q+(ie-re)/2;break;case 2:ee=q+(ie-re)}re-=Z.margin[e];if(Z.layout){D(S,s.size,re+\"px\")&&N(Z,1);Z.set[e]=!0}else if(ie>=re&&Z.set[e]){D(S,s.size,re+\"px\")&&N(Z,1);Z.set[e]=!1}Z.size[e]=re;Z.psize[e]=re}else{var ae=Z.margin[e],fe=Math.max(0,ie-ae),oe=0==e&&Z.sc[e];if(A(S)||!oe&&ie==re&&!Z.layout)if(Z.fs[e])0==e&&D(S,s.size,Z.fs[e]-ae+\"px\");else{D(S,s.size,\"\")&&N(Z,1);Z.set&&(Z.set[e]=!1)}else if(D(S,s.size,fe+\"px\")){N(Z,1);Z.set[e]=!0}ee=q;Z.size[e]=fe;Z.psize[e]=ie}if(r)if(V){D(S,s.left,\"4px\");var le;\"absolute\"!==(le=u.css(S,\"position\"))&&(S.style.position=\"relative\")}else D(S,s.left,\"0px\");else D(S,s.left,ee+\"px\");if(1==e){S.wtResize&&S.wtResize(S,Z.set[0]?Math.round(Z.size[0]):-1,Z.set[1]?Math.round(Z.size[1]):-1,!0);Z.dirty=0}}}W[k]>-1&&(q+=W[k])}if(s.resizeHandles.length>L){for(var de=L;de<s.resizeHandles.length;de++)if(s.resizeHandles[de]){var ue;(ue=u.getElement(s.resizeHandles[de])).parentNode.removeChild(ue)}s.resizeHandles.length=L}$(t).children(\".\"+n.handleClass).css(s.size,f-s.margins[2]-s.margins[1]+\"px\")}}(e,t);return!0};this.contains=function(e){var t=u.getElement(i),s=u.getElement(e.getId());return!(!t||!s)&&u.contains(t,s)};this.WT=u})");
  }

  static WJavaScriptPreamble appjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.ApplicationScope,
        JavaScriptObjectType.JavaScriptObject,
        "layouts2",
        "new function(){var e=[],i=!1,t=this,s=!1;this.find=function(e){var i=document.getElementById(e);return i?i.wtLayout:null};this.setDirty=function(e){var i=this.find(e);if(i){i.setDirty();t.scheduleAdjust()}};this.setElementDirty=function(e){var i=e;e=e.parentNode;for(;e&&e!=document.body;){var t=e.wtLayout;t&&t.setElDirty(i);i=e;e=e.parentNode}};this.setChildLayoutsDirty=function(e,i){var t,s;for(t=0,s=e.descendants.length;t<s;++t){var n=e.descendants[t];if(i){var r=e.WT.getElement(n.getId());if(r&&!e.WT.contains(i,r))continue}n.setDirty()}};this.add=function(i){!function e(i,t){var s,n;for(s=0,n=i.length;s<n;++s){var r=i[s];if(r.getId()==t.getId()){i[s]=t;t.descendants=r.descendants;return}if(r.contains(t)){e(r.descendants,t);return}if(t.contains(r)){t.descendants.push(r);i.splice(s,1);--s;--n}}i.push(t)}(e,i);t.scheduleAdjust()};var n=!1,r=0;this.scheduleAdjust=function(e){e&&(s=!0);if(!n){i?++r:r=0;if(!(r>=6)){n=!0;setTimeout((function(){t.adjust()}),0)}}};this.adjust=function(r,a){if(r){var f=this.find(r);f&&f.setItemsDirty(a);t.scheduleAdjust()}else{n=!1;if(!i){i=!0;var o=0,l=1;d(e,o);u(e,o);d(e,l);u(e,l);i=!1;s=!1}}function d(e,i){var t,n;for(t=0,n=e.length;t<n;++t){var r=e[t];d(r.descendants,i);i==l&&s?r.setDirty():i==o&&r.setAllDirty();r.measure(i)}}function u(e,i){var t,s;for(t=0,s=e.length;t<s;++t){var n=e[t];if(n.apply(i))u(n.descendants,i);else{e.splice(t,1);--t;--s}}}};this.updateConfig=function(e,i){var t=this.find(e);t&&t.setConfig(i)};this.adjustNow=function(){n&&t.adjust()};var a=null;window.onresize=function(){clearTimeout(a);a=setTimeout((function(){a=null;t.scheduleAdjust(!0)}),20)};window.onshow=function(){s=!0;t.adjust()}}");
  }

  static WJavaScriptPreamble wtjs10() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "ChildrenResize",
        "(function(e,i,t,o){var s,r,h,p=this,n=t>=0;if(o)if(n){e.style.height=t+\"px\";e.lh=!0}else{e.style.height=\"\";e.lh=!1}else e.lh=!1;if(p.boxSizing(e)){t-=p.px(e,\"marginTop\");t-=p.px(e,\"marginBottom\");t-=p.px(e,\"borderTopWidth\");t-=p.px(e,\"borderBottomWidth\");t-=p.px(e,\"paddingTop\");t-=p.px(e,\"paddingBottom\");i-=p.px(e,\"marginLeft\");i-=p.px(e,\"marginRight\");i-=p.px(e,\"borderLeftWidth\");i-=p.px(e,\"borderRightWidth\");i-=p.px(e,\"paddingLeft\");i-=p.px(e,\"paddingRight\")}function a(e){var i=p.px(e,\"marginTop\");i+=p.px(e,\"marginBottom\");if(!p.boxSizing(e)){i+=p.px(e,\"borderTopWidth\");i+=p.px(e,\"borderBottomWidth\");i+=p.px(e,\"paddingTop\");i+=p.px(e,\"paddingBottom\")}return i}for(s=0,r=e.childNodes.length;s<r;++s)if(1==(h=e.childNodes[s]).nodeType&&!$(h).hasClass(\"wt-reparented\"))if(n){var l=t-a(h);if(l>0){if(h.offsetTop>0){var d=p.css(h,\"overflow\");\"visible\"!==d&&\"\"!==d||(h.style.overflow=\"auto\")}if(h.wtResize)h.wtResize(h,i,l,!0);else{var f=l+\"px\";if(h.style.height!=f){h.style.height=f;h.lh=!0}}}}else if(h.wtResize)h.wtResize(h,i,-1,!0);else{h.style.height=\"\";h.lh=!1}})");
  }

  static WJavaScriptPreamble wtjs11() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "ChildrenGetPS",
        "(function(e,i,t,o){return o})");
  }

  static WJavaScriptPreamble wtjs12() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastResize",
        "(function(e,i,t,o){var s=t>=0;if(o)if(s){e.style.height=t+\"px\";e.lh=!0}else{e.style.height=\"\";e.lh=!1}else e.lh=!1;for(var r=e.lastChild;r&&1==r.nodeType&&($(r).hasClass(\"wt-reparented\")||$(r).hasClass(\"resize-sensor\"));)r=r.previousSibling;if(r){var h=r.previousSibling;if(s){if((t-=h.offsetHeight+this.px(h,\"marginTop\")+this.px(h,\"marginBottom\"))>0)if(r.wtResize)r.wtResize(r,i,t,!0);else{r.style.height=t+\"px\";r.lh=!0}}else if(r.wtResize)r.wtResize(r,-1,-1,!0);else{r.style.height=\"\";r.lh=!1}}})");
  }

  static WJavaScriptPreamble wtjs13() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastGetPS",
        "(function(e,i,t,o){var s,r,h=this;for(s=0,r=e.childNodes.length;s<r;++s){var p=e.childNodes[s];if(p!=i){var n=h.css(p,\"position\");\"absolute\"!=n&&\"fixed\"!=n&&(0===t?o=Math.max(o,p.offsetWidth):o+=p.offsetHeight+h.px(p,\"marginTop\")+h.px(p,\"marginBottom\"))}}return o})");
  }
}
