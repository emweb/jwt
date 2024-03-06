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
              + ".layouts2.scheduleAdjust();};window.addEventListener('load',f);})();");
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
        parent.callJavaScript("Wt4_10_4.remove('" + this.removedItems_.get(i) + "');", true);
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
        .append(".layouts2.add(new Wt4_10_4.StdLayout2(")
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
        "(function(e,t,i,s,n,o,l,f,r,c,a){const d=e.WT;this.descendants=[];const u=1e6,g=\"-\"+u+\"px\",h=this;let p,m,z,x,y,S,w=a,L=!0,W=!0,b=!1;const E=document.body.classList.contains(\"Wt-rtl\"),M=[{initialized:!1,config:w.cols,margins:r,maxSize:l,measures:[],sizes:[],stretched:[],fixedSize:[],Left:E?\"Right\":\"Left\",left:E?\"right\":\"left\",Right:E?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(e,t){return w.items[t*M[0].config.length+e]},setItem:function(e,t,i){w.items[t*M[0].config.length+e]=i},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:s,resizeHandles:[]},{initialized:!1,config:w.rows,margins:c,maxSize:f,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(e,t){return w.items[e*M[0].config.length+t]},setItem:function(e,t,i){w.items[e*M[0].config.length+t]=i},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:n,resizeHandles:[]}];document.getElementById(t).wtLayout=this;function R(e){for(const t of w.items)if(t&&t.id===e)return t;return null}function H(e,t,i,s){const n=M[t];let o,l,f=t?e.scrollHeight:e.scrollWidth;if(0===t){const i=d.pxself(e,n.left);if(f+i>s.clientWidth||f+i===s.clientWidth&&d.isGecko&&\"hidden\"===s.parentNode.parentNode.style.visibility){o=e.style[n.left];T(e,n.left,g);f=t?e.scrollHeight:e.scrollWidth}}let r=t?e.clientHeight:e.clientWidth;if(d.isGecko&&!e.style[n.size]&&0===t&&(\"visible\"===(c=d.css(e,\"overflow\"))||\"none\"===c)){l=e.style[n.size];T(e,n.size,\"\")}var c;let a=t?e.offsetHeight:e.offsetWidth;o&&T(e,n.left,o);l&&T(e,n.size,l);r>=u&&(r-=u);f>=u&&(f-=u);a>=u&&(a-=u);if(0===f){f=d.pxself(e,n.size);0===f||d.isOpera||d.isGecko||(f-=d.px(e,\"border\"+n.Left+\"Width\")+d.px(e,\"border\"+n.Right+\"Width\"))}if(f>a)if(0===d.pxself(e,n.size))f=r;else{let t=!1;e.querySelectorAll(\".Wt-popup\").forEach((function(e){\"none\"!==e.style.display&&(t=!0)}));t&&(f=r)}const h=d.px(e,\"border\"+n.Left+\"Width\")+d.px(e,\"border\"+n.Right+\"Width\"),p=a-(r+h)!=0;if(i)return[f,p];(d.isGecko||d.isWebKit)&&0===t&&e.getBoundingClientRect().width!==Math.ceil(e.getBoundingClientRect().width)&&(f+=1);d.boxSizing(e)||d.isOpera||(f+=h);f+=d.px(e,\"margin\"+n.Left)+d.px(e,\"margin\"+n.Right);d.boxSizing(e)||(f+=d.px(e,\"padding\"+n.Left)+d.px(e,\"padding\"+n.Right));f+=a-(r+h);f<a&&(f=a);const m=d.px(e,\"max\"+n.Size);m>0&&(f=Math.min(m,f));return[Math.round(f),p]}function N(e,t){const i=M[t];if(\"none\"===e.style.display)return 0;if(e[\"layoutMin\"+i.Size])return e[\"layoutMin\"+i.Size];{let t=d.px(e,\"min\"+i.Size);d.boxSizing(e)||(t+=d.px(e,\"padding\"+i.Left)+d.px(e,\"padding\"+i.Right));return t}}function C(e,t){const i=M[t];let s=d.px(e,\"margin\"+i.Left)+d.px(e,\"margin\"+i.Right);d.boxSizing(e)||(s+=d.px(e,\"border\"+i.Left+\"Width\")+d.px(e,\"border\"+i.Right+\"Width\")+d.px(e,\"padding\"+i.Left)+d.px(e,\"padding\"+i.Right));return s}function v(e,t){const i=M[t];return d.px(e,\"padding\"+i.Left)+d.px(e,\"padding\"+i.Right)}function D(e,t){if(d.boxSizing(e)){const i=M[t];return d.px(e,\"border\"+i.Left+\"Width\")+d.px(e,\"border\"+i.Right+\"Width\")+d.px(e,\"padding\"+i.Left)+d.px(e,\"padding\"+i.Right)}return 0}function I(e,t){const i=M[t];return Math.round(d.px(e,\"border\"+i.Left+\"Width\")+d.px(e,\"border\"+i.Right+\"Width\")+d.px(e,\"margin\"+i.Left)+d.px(e,\"margin\"+i.Right)+d.px(e,\"padding\"+i.Left)+d.px(e,\"padding\"+i.Right))}function A(t,i,s){t.dirty=Math.max(t.dirty,i);L=!0;s&&e.layouts2.scheduleAdjust()}function T(e,t,i){if(e.style[t]!==i){e.style[t]=i;return!0}return!1}function j(e){return\"none\"===e.style.display&&!e.ed||e.classList.contains(\"Wt-hidden\")}this.updateSizeInParent=function(e){if(m&&z.id){const e=d.$(z.id);if(e){if(z!==e){m=e.parentNode.wtLayout;m?z=e:G()}}else G()}if(m&&x){const i=M[e];let s=i.measures[2];i.maxSize>0&&(s=Math.min(i.maxSize,s));if(S){const i=d.getElement(t);if(!i)return;let n=i,o=n.parentNode;for(;;){o.wtGetPS&&(s=o.wtGetPS(o,n,e,s));s+=I(o,e);if(o===z)break;1===e&&o===i.parentNode&&!o.lh&&o.offsetHeight>s&&(s=o.offsetHeight);n=o;o=n.parentNode}}else s+=y[e];m.setChildSize(z,e,s)}};function B(i,s,n){const o=s.di,l=M[i],f=M[1^i],r=d.getElement(t);let c,a,u;for(u=o-1;u>=0;--u)if(l.sizes[u]>=0){c=-(l.sizes[u]-l.measures[1][u]);break}a=l.sizes[o]-l.measures[1][o];E&&([c,a]=[-a,-c]);new d.SizeHandle(d,l.resizeDir,d.pxself(s,l.size),d.pxself(s,f.size),c,a,l.resizerClass,(function(t){!function(t,i,s){const n=M[t];E&&(s=-s);if(n.config[i][0]>0&&0===n.config[i+1][0]){++i;s=-s}n.fixedSize[i]=n.sizes[i]+s;e.layouts2.scheduleAdjust()}(i,u,t)}),s,r,n,0,0)}function P(e,t){const i=e.config.length;if(0!==e.config[t][1])for(let s=t+1;s<i;++s)if(e.measures[1][s]>-1)return!0;for(let i=t-1;i>=0;--i)if(e.measures[1][i]>-1)return 0!==e.config[i][1];return!1}this.setConfig=function(t){const i=w;w=t;M[0].config=w.cols;M[1].config=w.rows;M[0].stretched=[];M[1].stretched=[];for(const e of i.items)if(e){const t=R(e.id);if(t){t.ps=e.ps;t.sc=e.sc;t.ms=e.ms;t.size=e.size;t.psize=e.psize;t.fs=e.fs;t.margin=e.margin;t.set=e.set}else if(e.set){e.set[0]&&T(e.w,M[0].size,\"\");e.set[1]&&T(e.w,M[1].size,\"\")}}W=!0;L=!0;e.layouts2.scheduleAdjust()};this.getId=function(){return t};this.setElDirty=function(t){const i=R(t.id);if(i){i.dirty=2;L=!0;e.layouts2.scheduleAdjust()}};this.setItemsDirty=function(t){const i=M[0].config.length;for(const[s,n]of t){const t=w.items[s*i+n];if(t){t.dirty=2;if(t.layout){t.layout=!1;t.wasLayout=!0;e.layouts2.setChildLayoutsDirty(h,t.w)}}}L=!0};this.setDirty=function(){W=!0};this.setAllDirty=function(){for(const e of w.items)e&&(e.dirty=2);L=!0};this.setChildSize=function(e,t,i){const s=M[0].config.length,n=M[t];let o;const l=R(e.id);if(l){const e=0===t?o%s:o/s;if(l.align>>n.alignBits&15||!n.stretched[e]){l.ps||(l.ps=[]);l.ps[t]=i}l.layout=!0;A(l,1)}};function G(){const e=d.getElement(t);p=null===i;m=null;z=null;x=!0;b=!0;y=[];S=!1;if(p){let t=e,i=t.parentNode;y=[0,0];for(;i!==document&&!t.classList.contains(\"wt-reparented\");){y[0]+=I(i,0);y[1]+=I(i,1);i.wtGetPS&&(S=!0);const e=i.parentNode.wtLayout;if(e){z=i;m=e;break}t=i;i=t.parentNode;1===i.childNodes.length||i.wtGetPS||(x=!1)}const s=e.parentNode;for(let e=0;e<2;++e)M[e].sizeSet=0!==d.pxself(s,M[e].size)}else{m=document.getElementById(i).wtLayout;z=e;y[0]=I(z,0);y[1]=I(z,1)}}this.measure=function(e){const i=d.getElement(t);if(i&&!d.isHidden(i)){b||G();if(L||W){!function(e,t,i){const s=M[e],n=M[1^e],l=s.measures,f=s.config.length,r=n.config.length,c=l.slice();if(5===c.length){c[0]=c[0].slice();c[1]=c[1].slice()}if(L){if(i&&void 0===s.minSize){s.minSize=d.px(i,\"min\"+s.Size);s.minSize>0&&(s.minSize-=D(i,e))}const u=[],g=[];let p,m;const z=!0;let x=!1;for(let b=0;b<f;++b){let E=0,R=s.config[b][2],v=!0;for(let I=0;I<r;++I){const B=s.getItem(b,I);if(B){if(!B.w||0===e&&B.dirty>1){const G=d.$(B.id);if(!G){s.setItem(b,I,null);continue}if(G!==B.w){B.w=G;const k=B;[G,...G.querySelectorAll(\"img\")].filter((e=>\"IMG\"===e.tagName)).forEach((function(e){e.addEventListener(\"load\",(function(){A(k,1,!0)}))}))}}if(!o&&\"absolute\"!==B.w.style.position){B.w.style.position=\"absolute\";B.w.style.visibility=\"hidden\"}B.ps||(B.ps=[]);B.sc||(B.sc=[]);B.ms||(B.ms=[]);B.size||(B.size=[]);B.psize||(B.psize=[]);B.fs||(B.fs=[]);B.margin||(B.margin=[]);const P=!B.set;B.set||(B.set=[!1,!1]);if(j(B.w)){B.ps[e]=B.ms[e]=0;continue}if(B.w){if(B.dirty){let _;if(B.dirty>1){_=N(B.w,e);B.ms[e]=_}else _=B.ms[e];B.dirty>1&&(B.margin[e]=C(B.w,e));if(!B.set[e])if(0!==e&&P){const $=Math.round(d.px(B.w,s.size));$>Math.max(D(B.w,e),_)?B.fs[e]=$+B.margin[e]:B.fs[e]=0}else{const J=d.pxself(B.w,s.size);B.fs[e]=J?J+B.margin[e]:0}const q=B.align>>s.alignBits&15;let O=B.fs[e];if(q||z||s.config[b][0]<=0)if(B.layout){0===O&&(O=B.ps[e]);B.ps[e]=O}else{if(B.wasLayout){B.wasLayout=!1;B.set=[!1,!1];B.ps=[];B.w.wtResize&&B.w.wtResize(B.w,-1,-1,!0);T(B.w,M[1].size,\"\")}const K=H(B.w,e,!1,t),F=K[0];let Q=B.set[e];Q&&B.psize[e]>8&&(Q=F>=B.psize[e]-4&&F<=B.psize[e]+4);const U=void 0!==B.ps[e]&&s.config[b][0]>0&&B.set[e];O=Q||U?Math.max(O,B.ps[e]):Math.max(O,F);B.ps[e]=O;B.sc[e]=K[1]}else B.layout&&0===O&&(O=B.ps[e]);if(B.span&&1!==B.span[e])x=!0;else{O>E&&(E=O);_>R&&(R=_)}}else if(B.span&&1!==B.span[e])x=!0;else{B.ps[e]>E&&(E=B.ps[e]);B.ms[e]>R&&(R=B.ms[e])}j(B.w)||B.span&&1!==B.span[e]||(v=!1)}}}v?R=E=-1:R>E&&(E=R);u[b]=E;g[b]=R;if(R>-1){p+=E;m+=R}}if(x){function a(t,i){for(let n=f-1;n>=0;--n)for(let o=0;o<r;++o){const l=s.getItem(n,o);if(l&&l.span&&l.span[e]>1){let o,f=t(l),r=0,c=0;for(o=0;o<l.span[e];++o){const e=i[n+o];if(-1!==e){f-=e;++r;s.config[n+o][0]>0&&(c+=s.config[n+o][0]);0!==o&&(f-=s.margins[0])}}if(f>=0)if(r>0){c>0&&(r=c);for(o=0;o<l.span[e];++o)if(-1!==i[n+o]){let e;e=c>0?s.config[n+o][0]:1;if(e>0){const t=Math.round(f*(e/r));f-=t;r-=e;i[n+o]+=t}}}else i[n]=f}}}a((function(t){return t.ps[e]}),u);a((function(t){return t.ms[e]}),g)}p=0;m=0;for(let V=0;V<f;++V){g[V]>u[V]&&(u[V]=g[V]);if(g[V]>-1){p+=u[V];m+=g[V]}}let y=0,S=!0,w=!1;for(let X=0;X<f;++X)if(g[X]>-1){if(S){y+=s.margins[1];S=!1}else{y+=s.margins[0];w&&(y+=4)}w=0!==s.config[X][1]}S||(y+=s.margins[2]);p+=y;m+=y;s.measures=[u,g,p,m,y]}(W||c[2]!==s.measures[2])&&h.updateSizeInParent(e);if(i&&0===s.minSize&&c[3]!==s.measures[3]&&\"Wt-domRoot\"!==i.parentNode.className){const Y=s.measures[3]+\"px\";T(i,\"min\"+s.Size,Y)}i&&0===e&&i&&d.hasTag(i,\"TD\")&&T(i,s.size,s.measures[2]+\"px\")}(e,i,p?i.parentNode:null)}1===e&&(L=W=!1)}};this.setMaxSize=function(e,t){M[0].maxSize=e;M[1].maxSize=t};this.apply=function(e){const i=d.getElement(t);if(!i)return!1;if(d.isHidden(i))return!0;!function(e,i){const s=M[e],n=M[1^e],l=s.measures;let f=0,r=!1,c=!1;const a=p?i.parentNode:null;if(0===s.maxSize)if(a){const t=d.css(a,\"position\");\"absolute\"===t&&(f=d.pxself(a,s.size));if(0===f){if(!s.initialized){if(0===e&&(\"absolute\"===t||\"fixed\"===t)){a.style.display=\"none\";f=a.clientWidth;a.style.display=\"\"}f=e?a.clientHeight:a.clientWidth;r=!0;let i,n;if(d.hasTag(a,\"TD\")||d.hasTag(a,\"TH\")||a.parentNode.classList.contains(\"Wt-domRoot\")){i=0;n=1}else{i=s.minSize?s.minSize:l[3];n=0}f-(i+v(a,e))<=1&&(s.maxSize=999999)}if(0===f&&0===s.maxSize){f=e?a.clientHeight:a.clientWidth;r=!0}}}else{f=d.pxself(i,s.size);c=!0}else if(s.sizeSet){f=d.pxself(a,s.size);c=!0}let u=0;a&&a.wtGetPS&&1===e&&(u=a.wtGetPS(a,i,e,0));let g=l[2];g<s.minSize&&(g=s.minSize);if(s.maxSize&&!s.sizeSet){const t=Math.min(g,s.maxSize)+u;T(a,s.size,t+D(a,e)+\"px\")&&m&&m.setElDirty(z);f=t;c=!0}s.cSize=f;if(1===e&&a&&a.wtResize){const e=n.cSize,t=s.cSize;a.wtResize(a,Math.round(e),Math.round(t),!0)}f-=u;if(!c){let t=0;if(void 0===s.cPadding){t=r?v(a,e):D(a,e);s.cPadding=t}else t=s.cPadding;f-=t}s.initialized=!0;if(a&&f<=0)return;f<l[3]-u&&(f=l[3]-u);let h=[];const x=s.config.length,y=n.config.length;let S;for(S=0;S<x;++S)s.stretched[S]=!1;if(f>=l[3]-u){const e=-1,t=-2,i=0,n=1;let o=f-l[4];const r=[],c=[0,0],a=[0,0];let d=0;for(let u=0;u<x;++u)if(l[1][u]>-1){let t=-1;P(s,u)||delete s.fixedSize[u];if(void 0!==s.fixedSize[u]&&(u+1===x||l[1][u+1]>-1))t=s.fixedSize[u];else if(P(s,u)&&0!==s.config[u][1]&&s.config[u][1][0]>=0){t=s.config[u][1][0];s.config[u][1][1]&&(t=(f-l[4])*t/100)}if(t>=0){r[u]=e;h[u]=t;o-=h[u]}else{let e;if(s.config[u][0]>0){e=n;r[u]=s.config[u][0];d+=r[u]}else{e=i;r[u]=0}c[e]+=l[1][u];a[e]+=l[0][u];h[u]=l[0][u]}}else{r[u]=t;h[u]=-1}s.fixedSize.length>x&&(s.fixedSize.length=x);if(0===d){for(let e=0;e<x;++e)if(0===r[e]){r[e]=1;++d}a[n]=a[i];c[n]=c[i];a[i]=0;c[i]=0}if(o>a[i]+c[n]){o-=a[i];if(o>a[n]){if(s.fitSize){o-=a[n];const e=o/d;let t=0;for(let i=0;i<x;++i)if(r[i]>0){const n=t;t+=r[i]*e;h[i]+=Math.round(t)-Math.round(n);s.stretched[i]=!0}}}else{const e=n;o<c[e]&&(o=c[e]);let t;t=a[e]-c[e]>0?(o-c[e])/(a[e]-c[e]):0;let i=0;for(let e=0;e<x;++e)if(r[e]>0){const s=i;i+=(l[0][e]-l[1][e])*t;h[e]=l[1][e]+Math.round(i)-Math.round(s)}}}else{for(let e=0;e<x;++e)r[e]>0&&(h[e]=l[1][e]);o-=c[n];const e=i;o<c[e]&&(o=c[e]);let t;t=a[e]-c[e]>0?(o-c[e])/(a[e]-c[e]):0;let s=0;for(let e=0;e<x;++e)if(0===r[e]){const i=s;s+=(l[0][e]-l[1][e])*t;h[e]=l[1][e]+Math.round(s)-Math.round(i)}}}else h=l[1];s.sizes=h;let w=s.margins[1],L=!0,W=!1;const b=W;for(let l=0;l<x;++l){if(h[l]>-1){if(W){const o=t+\"-rs\"+e+\"-\"+l;let f=d.getElement(o);if(!f){s.resizeHandles[l]=o;f=document.createElement(\"div\");f.setAttribute(\"id\",o);f.di=l;f.style.position=\"absolute\";f.style[n.left]=n.margins[1]+\"px\";f.style[s.size]=s.margins[0]+\"px\";n.cSize&&(f.style[n.size]=n.cSize-n.margins[2]-n.margins[1]+\"px\");f.className=s.handleClass;i.insertBefore(f,i.firstChild);f.onmousedown=f.ontouchstart=function(t){const i=t||window.event;B(e,this,i)}}w+=2;T(f,s.left,w+\"px\");w+=2}else if(s.resizeHandles[l]){const e=d.getElement(s.resizeHandles[l]);e.parentNode.removeChild(e);delete s.resizeHandles[l]}W=0!==s.config[l][1];L?L=!1:w+=s.margins[0]}else if(s.resizeHandles[l]){const e=d.getElement(s.resizeHandles[l]);e.parentNode.removeChild(e);delete s.resizeHandles[l]}for(let t=0;t<y;++t){const i=s.getItem(l,t);if(i&&i.w){const t=i.w;let n,f=Math.max(h[l],0);if(i.span){let t,n=W;for(t=1;t<i.span[e]&&!(l+t>=h.length);++t){n&&(f+=4);n=0!==s.config[l+t][1];h[l+t-1]>-1&&h[l+t]>-1&&(f+=s.margins[0]);f+=h[l+t]}}T(t,\"visibility\",\"\");let r=i.align>>s.alignBits&15,c=i.ps[e];f<c&&(r=0);if(r){switch(r){case 1:n=w;break;case 4:n=w+(f-c)/2;break;case 2:n=w+(f-c)}c-=i.margin[e];if(i.layout){T(t,s.size,c+\"px\")&&A(i,1);i.set[e]=!0}else if(f>=c&&i.set[e]){T(t,s.size,c+\"px\")&&A(i,1);i.set[e]=!1}i.size[e]=c;i.psize[e]=c}else{const o=i.margin[e],l=Math.max(0,f-o),r=0===e&&i.sc[e];if(j(t)||!r&&f===c&&!i.layout)if(i.fs[e])0===e&&T(t,s.size,i.fs[e]-o+\"px\");else{T(t,s.size,\"\")&&A(i,1);i.set&&(i.set[e]=!1)}else if(T(t,s.size,l+\"px\")){A(i,1);i.set[e]=!0}n=w;i.size[e]=l;i.psize[e]=f}if(o)if(b){T(t,s.left,\"4px\");\"absolute\"!==d.css(t,\"position\")&&(t.style.position=\"relative\")}else T(t,s.left,\"0px\");else T(t,s.left,n+\"px\");if(1===e){t.wtResize&&t.wtResize(t,i.set[0]?Math.round(i.size[0]):-1,i.set[1]?Math.round(i.size[1]):-1,!0);i.dirty=0}}}h[l]>-1&&(w+=h[l])}if(s.resizeHandles.length>x){for(const e of s.resizeHandles)if(e){const t=d.getElement(e);t.parentNode.removeChild(t)}s.resizeHandles.length=x}i.querySelectorAll(`:scope > .${n.handleClass}`).forEach((function(e){e.style[s.size]=f-s.margins[2]-s.margins[1]+\"px\"}))}(e,i);return!0};this.contains=function(e){const i=d.getElement(t),s=d.getElement(e.getId());return!(!i||!s)&&d.contains(i,s)};this.WT=d})");
  }

  static WJavaScriptPreamble appjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.ApplicationScope,
        JavaScriptObjectType.JavaScriptObject,
        "layouts2",
        "new function(){const e=[];let t=!1;const i=this;let s=!1;this.find=function(e){const t=document.getElementById(e);return t?t.wtLayout:null};this.setDirty=function(e){const t=this.find(e);if(t){t.setDirty();i.scheduleAdjust()}};this.setElementDirty=function(e){let t=e;e=e.parentNode;for(;e&&e!==document.body;){const i=e.wtLayout;i&&i.setElDirty(t);t=e;e=e.parentNode}};this.setChildLayoutsDirty=function(e,t){for(const i of e.descendants){if(t){const s=e.WT.getElement(i.getId());if(s&&!e.WT.contains(t,s))continue}i.setDirty()}};this.add=function(t){!function e(t,i){for(let s=0,n=t.length;s<n;++s){const o=t[s];if(o.getId()===i.getId()){t[s]=i;i.descendants=o.descendants;return}if(o.contains(i)){e(o.descendants,i);return}if(i.contains(o)){i.descendants.push(o);t.splice(s,1);--s;--n}}t.push(i)}(e,t);i.scheduleAdjust()};let n=!1,o=0;this.scheduleAdjust=function(e){e&&(s=!0);if(!n){t?++o:o=0;if(!(o>=6)){n=!0;setTimeout((function(){i.adjust()}),0)}}};this.adjust=function(o,l){if(o){const e=this.find(o);e&&e.setItemsDirty(l);i.scheduleAdjust()}else{n=!1;if(!t){t=!0;f(e,0);r(e,0);f(e,1);r(e,1);t=!1;s=!1}}function f(e,t){for(const i of e){f(i.descendants,t);1===t&&s?i.setDirty():0===t&&i.setAllDirty();i.measure(t)}}function r(e,t){for(let i=0,s=e.length;i<s;++i){const n=e[i];if(n.apply(t))r(n.descendants,t);else{e.splice(i,1);--i;--s}}}};this.updateConfig=function(e,t){const i=this.find(e);i&&i.setConfig(t)};this.adjustNow=function(){n&&i.adjust()};let l=null;window.onresize=function(){clearTimeout(l);l=setTimeout((function(){l=null;i.scheduleAdjust(!0)}),20)};window.onshow=function(){s=!0;i.adjust()}}");
  }

  static WJavaScriptPreamble wtjs10() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "ChildrenResize",
        "(function(t,i,e,o){const s=this,n=e>=0;if(o)if(n){t.style.height=e+\"px\";t.lh=!0}else{t.style.height=\"\";t.lh=!1}else t.lh=!1;if(s.boxSizing(t)){e-=s.px(t,\"marginTop\");e-=s.px(t,\"marginBottom\");e-=s.px(t,\"borderTopWidth\");e-=s.px(t,\"borderBottomWidth\");e-=s.px(t,\"paddingTop\");e-=s.px(t,\"paddingBottom\");i-=s.px(t,\"marginLeft\");i-=s.px(t,\"marginRight\");i-=s.px(t,\"borderLeftWidth\");i-=s.px(t,\"borderRightWidth\");i-=s.px(t,\"paddingLeft\");i-=s.px(t,\"paddingRight\")}function p(t){let i=s.px(t,\"marginTop\");i+=s.px(t,\"marginBottom\");if(!s.boxSizing(t)){i+=s.px(t,\"borderTopWidth\");i+=s.px(t,\"borderBottomWidth\");i+=s.px(t,\"paddingTop\");i+=s.px(t,\"paddingBottom\")}return i}for(const o of t.childNodes)if(1===o.nodeType&&!o.classList.contains(\"wt-reparented\"))if(n){const t=e-p(o);if(t>0){if(o.offsetTop>0){const t=s.css(o,\"overflow\");\"visible\"!==t&&\"\"!==t||(o.style.overflow=\"auto\")}if(o.wtResize)o.wtResize(o,i,t,!0);else{const i=t+\"px\";if(o.style.height!==i){o.style.height=i;o.lh=!0}}}}else if(o.wtResize)o.wtResize(o,i,-1,!0);else{o.style.height=\"\";o.lh=!1}})");
  }

  static WJavaScriptPreamble wtjs11() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "ChildrenGetPS",
        "(function(t,i,e,o){return o})");
  }

  static WJavaScriptPreamble wtjs12() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastResize",
        "(function(t,i,e,o){const s=this,n=e>=0;if(o)if(n){t.style.height=e+\"px\";t.lh=!0}else{t.style.height=\"\";t.lh=!1}else t.lh=!1;let p=t.lastChild;for(;p&&1===p.nodeType&&(p.classList.contains(\"wt-reparented\")||p.classList.contains(\"resize-sensor\"));)p=p.previousSibling;if(!p)return;const h=p.previousSibling;if(n){if((e-=h.offsetHeight+s.px(h,\"marginTop\")+s.px(h,\"marginBottom\"))>0)if(p.wtResize)p.wtResize(p,i,e,!0);else{p.style.height=e+\"px\";p.lh=!0}}else if(p.wtResize)p.wtResize(p,-1,-1,!0);else{p.style.height=\"\";p.lh=!1}})");
  }

  static WJavaScriptPreamble wtjs13() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastGetPS",
        "(function(t,i,e,o){const s=this;for(const n of t.childNodes)if(n!==i){const t=s.css(n,\"position\");\"absolute\"!==t&&\"fixed\"!==t&&(0===e?o=Math.max(o,n.offsetWidth):o+=n.offsetHeight+s.px(n,\"marginTop\")+s.px(n,\"marginBottom\"))}return o})");
  }
}
