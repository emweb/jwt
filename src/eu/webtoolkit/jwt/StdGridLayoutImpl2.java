/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StdGridLayoutImpl2 extends StdLayoutImpl {
	private static Logger logger = LoggerFactory
			.getLogger(StdGridLayoutImpl2.class);

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
			app.getStyleSheet().addRule("table.Wt-hcenter",
					"margin: 0px auto;position: relative");
			app.loadJavaScript(THIS_JS, wtjs1());
			app.loadJavaScript(THIS_JS, appjs1());
			app.doJavaScript(app.getJavaScriptClass()
					+ ".layouts2.scheduleAdjust();");
			app.doJavaScript("$(window).load(function() { "
					+ app.getJavaScriptClass() + ".layouts2.scheduleAdjust();"
					+ "});");
			WApplication.getInstance().addAutoJavaScript(
					"if(" + app.getJavaScriptClass() + ".layouts2) "
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

	public void updateAddItem(WLayoutItem item) {
		super.updateAddItem(item);
		this.addedItems_.add(item);
	}

	public void updateRemoveItem(WLayoutItem item) {
		super.updateRemoveItem(item);
		this.addedItems_.remove(item);
		this.removedItems_.add(getImpl(item).getId());
	}

	public void update(WLayoutItem item) {
		WContainerWidget c = this.getContainer();
		if (c != null) {
			c.layoutChanged(false, false);
		}
		this.needConfigUpdate_ = true;
	}

	public DomElement createDomElement(boolean fitWidth, boolean fitHeight,
			WApplication app) {
		this.needAdjust_ = this.needConfigUpdate_ = this.needRemeasure_ = false;
		this.addedItems_.clear();
		this.removedItems_.clear();
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		int[] margin = { 0, 0, 0, 0 };
		int maxWidth = 0;
		int maxHeight = 0;
		if (this.getLayout().getParentLayout() == null) {
			margin[3] = this.getLayout().getContentsMargin(Side.Left);
			margin[0] = this.getLayout().getContentsMargin(Side.Top);
			margin[1] = this.getLayout().getContentsMargin(Side.Right);
			margin[2] = this.getLayout().getContentsMargin(Side.Bottom);
			maxWidth = pixelSize(this.getContainer().getMaximumWidth());
			maxHeight = pixelSize(this.getContainer().getMaximumHeight());
		}
		StringBuilder js = new StringBuilder();
		js.append(app.getJavaScriptClass())
				.append(".layouts2.add(new Wt3_3_5.StdLayout2(")
				.append(app.getJavaScriptClass()).append(",'")
				.append(this.getId()).append("',");
		if (this.getLayout().getParentLayout() != null) {
			js.append("'")
					.append(getImpl(this.getLayout().getParentLayout()).getId())
					.append("',");
		} else {
			js.append("null,");
		}
		boolean progressive = !app.getEnvironment().hasAjax();
		js.append(fitWidth ? '1' : '0').append(",")
				.append(fitHeight ? '1' : '0').append(",")
				.append(progressive ? '1' : '0').append(",");
		js.append(maxWidth).append(",").append(maxHeight).append(",[")
				.append(this.grid_.horizontalSpacing_).append(",")
				.append(margin[3]).append(",").append(margin[1]).append("],[")
				.append(this.grid_.verticalSpacing_).append(",")
				.append(margin[0]).append(",").append(margin[2]).append("],");
		this.streamConfig(js, app);
		DomElement div = DomElement.createNew(DomElementType.DomElement_DIV);
		div.setId(this.getId());
		div.setProperty(Property.PropertyStylePosition, "relative");
		DomElement table = null;
		DomElement tbody = null;
		DomElement tr = null;
		if (progressive) {
			table = DomElement.createNew(DomElementType.DomElement_TABLE);
			StringBuilder style = new StringBuilder();
			if (maxWidth != 0) {
				style.append("max-width: ").append(maxWidth).append("px;");
			}
			if (maxHeight != 0) {
				style.append("max-height: ").append(maxHeight).append("px;");
			}
			style.append("width: 100%;");
			table.setProperty(Property.PropertyStyle, style.toString());
			int totalColStretch = 0;
			for (int col = 0; col < colCount; ++col) {
				totalColStretch += Math.max(0,
						this.grid_.columns_.get(col).stretch_);
			}
			for (int col = 0; col < colCount; ++col) {
				DomElement c = DomElement
						.createNew(DomElementType.DomElement_COL);
				int stretch = Math
						.max(0, this.grid_.columns_.get(col).stretch_);
				if (stretch != 0 || totalColStretch == 0) {
					char[] buf = new char[30];
					double pct = totalColStretch == 0 ? 100.0 / colCount
							: 100.0 * stretch / totalColStretch;
					StringBuilder ss = new StringBuilder();
					ss.append("width:").append(MathUtils.roundCss(pct, 2))
							.append("%;");
					c.setProperty(Property.PropertyStyle, ss.toString());
				}
				table.addChild(c);
			}
			tbody = DomElement.createNew(DomElementType.DomElement_TBODY);
		}
		List<Boolean> overSpanned = new ArrayList<Boolean>();
		{
			int insertPos = 0;
			for (int ii = 0; ii < (colCount * rowCount); ++ii)
				overSpanned.add(insertPos + ii, false);
		}
		;
		int prevRowWithItem = -1;
		for (int row = 0; row < rowCount; ++row) {
			if (table != null) {
				tr = DomElement.createNew(DomElementType.DomElement_TR);
			}
			boolean rowVisible = false;
			int prevColumnWithItem = -1;
			for (int col = 0; col < colCount; ++col) {
				final Grid.Item item = this.grid_.items_.get(row).get(col);
				if (!overSpanned.get(row * colCount + col)) {
					for (int i = 0; i < item.rowSpan_; ++i) {
						for (int j = 0; j < item.colSpan_; ++j) {
							if (i + j > 0) {
								overSpanned.set((row + i) * colCount + col + j,
										true);
							}
						}
					}
					AlignmentFlag hAlign = EnumUtils.enumFromSet(EnumUtils
							.mask(item.alignment_,
									AlignmentFlag.AlignHorizontalMask));
					AlignmentFlag vAlign = EnumUtils.enumFromSet(EnumUtils
							.mask(item.alignment_,
									AlignmentFlag.AlignVerticalMask));
					DomElement td = null;
					if (table != null) {
						boolean itemVisible = this.hasItem(row, col);
						rowVisible = rowVisible || itemVisible;
						td = DomElement.createNew(DomElementType.DomElement_TD);
						if (itemVisible) {
							int[] padding = { 0, 0, 0, 0 };
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
									style.append("padding:").append(padding[0])
											.append("px;");
								}
							} else {
								style.append("padding:").append(padding[0])
										.append("px ").append(padding[1])
										.append("px ").append(padding[2])
										.append("px ").append(padding[3])
										.append("px;");
							}
							if (vAlign != null) {
								switch (vAlign) {
								case AlignTop:
									style.append("vertical-align:top;");
									break;
								case AlignMiddle:
									style.append("vertical-align:middle;");
									break;
								case AlignBottom:
									style.append("vertical-align:bottom;");
								default:
									break;
								}
							}
							td.setProperty(Property.PropertyStyle,
									style.toString());
							if (item.rowSpan_ != 1) {
								td.setProperty(Property.PropertyRowSpan,
										String.valueOf(item.rowSpan_));
							}
							if (item.colSpan_ != 1) {
								td.setProperty(Property.PropertyColSpan,
										String.valueOf(item.colSpan_));
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
							c = getImpl(item.item_).createDomElement(true,
									true, app);
						}
					}
					if (table != null) {
						if (c != null) {
							if (!app.getEnvironment().agentIsIElt(9)) {
								c.setProperty(Property.PropertyStyleBoxSizing,
										"border-box");
							}
							if (hAlign == null) {
								hAlign = AlignmentFlag.AlignJustify;
							}
							switch (hAlign) {
							case AlignCenter: {
								DomElement itable = DomElement
										.createNew(DomElementType.DomElement_TABLE);
								itable.setProperty(Property.PropertyClass,
										"Wt-hcenter");
								if (vAlign == null) {
									itable.setProperty(Property.PropertyStyle,
											"height:100%;");
								}
								DomElement irow = DomElement
										.createNew(DomElementType.DomElement_TR);
								DomElement itd = DomElement
										.createNew(DomElementType.DomElement_TD);
								if (vAlign == null) {
									itd.setProperty(Property.PropertyStyle,
											"height:100%;");
								}
								boolean haveMinWidth = c.getProperty(
										Property.PropertyStyleMinWidth)
										.length() != 0;
								itd.addChild(c);
								if (app.getEnvironment().agentIsIElt(9)) {
									if (haveMinWidth) {
										DomElement spacer = DomElement
												.createNew(DomElementType.DomElement_DIV);
										spacer.setProperty(
												Property.PropertyStyleWidth,
												c.getProperty(Property.PropertyStyleMinWidth));
										spacer.setProperty(
												Property.PropertyStyleHeight,
												"1px");
										itd.addChild(spacer);
									}
								}
								irow.addChild(itd);
								itable.addChild(irow);
								c = itable;
								break;
							}
							case AlignRight:
								if (!c.isDefaultInline()) {
									c.setProperty(Property.PropertyStyleFloat,
											"right");
								} else {
									td.setProperty(
											Property.PropertyStyleTextAlign,
											"right");
								}
								break;
							case AlignLeft:
								if (!c.isDefaultInline()) {
									c.setProperty(Property.PropertyStyleFloat,
											"left");
								} else {
									td.setProperty(
											Property.PropertyStyleTextAlign,
											"left");
								}
								break;
							default:
								break;
							}
							boolean haveMinWidth = c.getProperty(
									Property.PropertyStyleMinWidth).length() != 0;
							td.addChild(c);
							if (app.getEnvironment().agentIsIElt(9)) {
								if (haveMinWidth) {
									DomElement spacer = DomElement
											.createNew(DomElementType.DomElement_DIV);
									spacer.setProperty(
											Property.PropertyStyleWidth,
											c.getProperty(Property.PropertyStyleMinWidth));
									spacer.setProperty(
											Property.PropertyStyleHeight, "1px");
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
					tr.setProperty(Property.PropertyStyleDisplay, "hidden");
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
		return div;
	}

	public void updateDom(final DomElement parent) {
		WApplication app = WApplication.getInstance();
		if (this.needConfigUpdate_) {
			this.needConfigUpdate_ = false;
			DomElement div = DomElement.getForUpdate(this,
					DomElementType.DomElement_DIV);
			for (int i = 0; i < this.addedItems_.size(); ++i) {
				WLayoutItem item = this.addedItems_.get(i);
				DomElement c = this.createElement(item, app);
				div.addChild(c);
			}
			this.addedItems_.clear();
			for (int i = 0; i < this.removedItems_.size(); ++i) {
				parent.callJavaScript(
						"Wt3_3_5.remove('" + this.removedItems_.get(i) + "');",
						true);
			}
			this.removedItems_.clear();
			parent.addChild(div);
			StringBuilder js = new StringBuilder();
			js.append(app.getJavaScriptClass())
					.append(".layouts2.updateConfig('").append(this.getId())
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
			js.append(app.getJavaScriptClass()).append(".layouts2.setDirty('")
					.append(this.getId()).append("');");
			app.doJavaScript(js.toString());
		}
		if (this.needAdjust_) {
			this.needAdjust_ = false;
			StringBuilder js = new StringBuilder();
			js.append(app.getJavaScriptClass()).append(".layouts2.adjust('")
					.append(this.getId()).append("', [");
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
						js.append("[").append((int) row).append(",")
								.append((int) col).append("]");
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
						(((nested.getImpl()) instanceof StdLayoutImpl ? (StdLayoutImpl) (nested
								.getImpl()) : null)).updateDom(parent);
					}
				}
			}
		}
	}

	public void setHint(final String name, final String value) {
		logger.error(new StringWriter().append("unrecognized hint '")
				.append(name).append("'").toString());
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

	void containerAddWidgets(WContainerWidget container) {
		super.containerAddWidgets(container);
		if (!(container != null)) {
			return;
		}
		WApplication app = WApplication.getInstance();
		if (this.getParentLayoutImpl() == null) {
			if (container == app.getRoot()) {
				app.setBodyClass(app.getBodyClass() + " Wt-layout");
				app.setHtmlClass(app.getHtmlClass() + " Wt-layout");
			}
		}
	}

	private final Grid grid_;
	private boolean needAdjust_;
	private boolean needRemeasure_;
	private boolean needConfigUpdate_;
	private List<WLayoutItem> addedItems_;
	private List<String> removedItems_;

	private int nextRowWithItem(int row, int c) {
		for (row += this.grid_.items_.get(row).get(c).rowSpan_; row < (int) this.grid_.rows_
				.size(); ++row) {
			for (int col = 0; col < this.grid_.columns_.size(); col += this.grid_.items_
					.get(row).get(col).colSpan_) {
				if (this.hasItem(row, col)) {
					return row;
				}
			}
		}
		return this.grid_.rows_.size();
	}

	private int nextColumnWithItem(int row, int col) {
		for (;;) {
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
				minHeight = Math.max(minHeight, getImpl(item)
						.getMinimumHeight());
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
		if (size.getUnit() == WLength.Unit.Percentage) {
			return 0;
		} else {
			return (int) size.toPixels();
		}
	}

	private void streamConfig(final StringBuilder js,
			final List<Grid.Section> sections, boolean rows, WApplication app) {
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
					if (size.getUnit() == WLength.Unit.Percentage) {
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
				AlignmentFlag hAlign = EnumUtils.enumFromSet(EnumUtils.mask(
						item.alignment_, AlignmentFlag.AlignHorizontalMask));
				AlignmentFlag vAlign = EnumUtils.enumFromSet(EnumUtils.mask(
						item.alignment_, AlignmentFlag.AlignVerticalMask));
				if (row + col != 0) {
					js.append(",");
				}
				if (item.item_ != null) {
					String id = getImpl(item.item_).getId();
					js.append("{");
					if (item.colSpan_ != 1 || item.rowSpan_ != 1) {
						js.append("span: [").append(item.colSpan_).append(",")
								.append(item.rowSpan_).append("],");
					}
					if (!item.alignment_.isEmpty()) {
						int align = 0;
						if (hAlign != null) {
							switch (hAlign) {
							case AlignLeft:
								align |= 0x1;
								break;
							case AlignRight:
								align |= 0x2;
								break;
							case AlignCenter:
								align |= 0x4;
								break;
							default:
								break;
							}
						}
						if (vAlign != null) {
							switch (vAlign) {
							case AlignTop:
								align |= 0x10;
								break;
							case AlignBottom:
								align |= 0x20;
								break;
							case AlignMiddle:
								align |= 0x40;
								break;
							default:
								break;
							}
						}
						js.append("align:").append((int) align).append(",");
					}
					js.append("dirty:")
							.append(this.grid_.items_.get(row).get(col).update_ ? 2
									: 0).append(",id:'").append(id).append("'")
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
		DomElement c = getImpl(item).createDomElement(true, true, app);
		c.setProperty(Property.PropertyStyleVisibility, "hidden");
		return c;
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"StdLayout2",
				"function(G,C,J,V,W,X,ca,p,D,x,y){function u(a){var b,c;b=0;for(c=B.items.length;b<c;++b){var e=B.items[b];if(e&&e.id==a)return e}return null}function A(a,b,c,e){function i(j){return j==\"visible\"||j==\"none\"}var n=q[b],m=b?a.scrollHeight:a.scrollWidth,s,d;if(b==0){var f=h.pxself(a,n.left);if(m+f>e.clientWidth||m+f==e.clientWidth&&h.isGecko&&e.parentNode.parentNode.style.visibility===\"hidden\"){s=a.style[n.left];v(a,n.left,\"-1000000px\");m=b?a.scrollHeight: a.scrollWidth}}e=b?a.clientHeight:a.clientWidth;if(h.isGecko&&!a.style[n.size]&&b==0&&i(h.css(a,\"overflow\"))){d=a.style[n.size];v(a,n.size,\"\")}f=b?a.offsetHeight:a.offsetWidth;s&&v(a,n.left,s);d&&v(a,n.size,d);if(e>=1E6)e-=1E6;if(m>=1E6)m-=1E6;if(f>=1E6)f-=1E6;if(m===0){m=h.pxself(a,n.size);if(m!==0&&!h.isOpera&&!h.isGecko)m-=h.px(a,\"border\"+n.Left+\"Width\")+h.px(a,\"border\"+n.Right+\"Width\")}if(h.isIE&&(h.hasTag(a,\"BUTTON\")||h.hasTag(a,\"TEXTAREA\")||h.hasTag(a,\"INPUT\")||h.hasTag(a,\"SELECT\")))m=e;if(m> f)if(h.pxself(a,n.size)==0)m=e;else{var l=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!==\"none\")l=true});if(l)m=e}d=h.px(a,\"border\"+n.Left+\"Width\")+h.px(a,\"border\"+n.Right+\"Width\");s=f-(e+d)!=0;if(c)return[m,scrollBar];if(h.isGecko&&b==0&&a.getBoundingClientRect().width!=Math.ceil(a.getBoundingClientRect().width))m+=1;if(!h.boxSizing(a)&&!h.isOpera)m+=d;m+=h.px(a,\"margin\"+n.Left)+h.px(a,\"margin\"+n.Right);if(!h.boxSizing(a)&&!h.isIE)m+=h.px(a,\"padding\"+n.Left)+h.px(a,\"padding\"+ n.Right);m+=f-(e+d);if(m<f)m=f;a=h.px(a,\"max\"+n.Size);if(a>0)m=Math.min(a,m);return[Math.round(m),s]}function w(a,b){b=q[b];if(a.style.display===\"none\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=h.px(a,\"min\"+b.Size);h.boxSizing(a)||(c+=h.px(a,\"padding\"+b.Left)+h.px(a,\"padding\"+b.Right));return c}}function O(a,b){b=q[b];var c=h.px(a,\"margin\"+b.Left)+h.px(a,\"margin\"+b.Right);if(!h.boxSizing(a)&&!(h.isIE&&!h.isIElt9&&h.hasTag(a,\"BUTTON\")))c+=h.px(a,\"border\"+b.Left+ \"Width\")+h.px(a,\"border\"+b.Right+\"Width\")+h.px(a,\"padding\"+b.Left)+h.px(a,\"padding\"+b.Right);return c}function K(a,b){b=q[b];return h.px(a,\"padding\"+b.Left)+h.px(a,\"padding\"+b.Right)}function ha(a,b){if(h.boxSizing(a)){b=q[b];return h.px(a,\"border\"+b.Left+\"Width\")+h.px(a,\"border\"+b.Right+\"Width\")+h.px(a,\"padding\"+b.Left)+h.px(a,\"padding\"+b.Right)}else return 0}function da(a,b){b=q[b];return Math.round(h.px(a,\"border\"+b.Left+\"Width\")+h.px(a,\"border\"+b.Right+\"Width\")+h.px(a,\"margin\"+b.Left)+h.px(a, \"margin\"+b.Right)+h.px(a,\"padding\"+b.Left)+h.px(a,\"padding\"+b.Right))}function Y(a,b,c){a.dirty=Math.max(a.dirty,b);P=true;c&&G.layouts2.scheduleAdjust()}function v(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function ma(a){return a.style.display===\"none\"&&!a.ed||$(a).hasClass(\"Wt-hidden\")}function ua(a,b,c){var e=q[a],i=e.config.length,n=q[a^1].config.length,m=e.measures.slice();if(m.length==5){m[0]=m[0].slice();m[1]=m[1].slice()}if(P){if(c&&typeof e.minSize==\"undefined\"){e.minSize= h.px(c,\"min\"+e.Size);if(e.minSize>0)e.minSize-=ha(c,a)}var s=[],d=[],f,l,j=false;for(f=0;f<i;++f){var H=0,o=e.config[f][2],r=true;for(l=0;l<n;++l){var g=e.getItem(f,l);if(g){if(!g.w||a==0&&g.dirty>1){var k=$(\"#\"+g.id),z=k.get(0);if(!z){e.setItem(f,l,null);continue}if(z!=g.w){g.w=z;k.find(\"img\").add(k.filter(\"img\")).bind(\"load\",{item:g},function(T){Y(T.data.item,1,true)})}}if(!X&&g.w.style.position!=\"absolute\"){g.w.style.position=\"absolute\";g.w.style.visibility=\"hidden\"}if(!g.ps)g.ps=[];if(!g.sc)g.sc= [];if(!g.ms)g.ms=[];if(!g.size)g.size=[];if(!g.psize)g.psize=[];if(!g.fs)g.fs=[];if(!g.margin)g.margin=[];k=!g.set;if(!g.set)g.set=[false,false];if(ma(g.w))g.ps[a]=g.ms[a]=0;else if(g.w){if(h.isIE)g.w.style.visibility=\"\";if(g.dirty){if(g.dirty>1){z=w(g.w,a);g.ms[a]=z}else z=g.ms[a];if(g.dirty>1)g.margin[a]=O(g.w,a);if(!g.set[a])if(a==0||!k){k=h.pxself(g.w,e.size);g.fs[a]=k?k+g.margin[a]:0}else{k=Math.round(h.px(g.w,e.size));g.fs[a]=k>Math.max(ha(g.w,a),z)?k+g.margin[a]:0}k=g.fs[a];if(g.layout){if(k== 0)k=g.ps[a];g.ps[a]=k}else{if(g.wasLayout){g.wasLayout=false;g.set=[false,false];g.ps=[];g.w.wtResize&&g.w.wtResize(g.w,-1,-1,true);v(g.w,q[1].size,\"\")}z=A(g.w,a,false,b);var t=z[0],E=g.set[a];if(E)if(g.psize[a]>8)E=t>=g.psize[a]-4&&t<=g.psize[a]+4;var M=typeof g.ps[a]!==\"undefined\"&&e.config[f][0]>0&&g.set[a];k=E||M?Math.max(k,g.ps[a]):Math.max(k,t);g.ps[a]=k;g.sc[a]=z[1]}if(!g.span||g.span[a]==1){if(k>H)H=k}else j=true}else if(!g.span||g.span[a]==1){if(g.ps[a]>H)H=g.ps[a];if(g.ms[a]>o)o=g.ms[a]}else j= true;if(!ma(g.w)&&(!g.span||g.span[a]==1))r=false}}}if(r)o=H=-1;else if(o>H)H=o;s[f]=H;d[f]=o;if(o>-1){Q+=H;Z+=o}}if(j){function ia(T,ja){for(f=0;f<i;++f)for(l=0;l<n;++l){var aa=e.getItem(f,l);if(aa&&aa.span&&aa.span[a]>1){var ba=T(aa),ea=0,ka=0,F;for(F=0;F<aa.span[a];++F){var S=ja[f+F];if(S!=-1){ba-=S;++ea;if(e.config[f+F][0]>0)ka+=e.config[f+F][0];if(F!=0)ba-=e.margins[0]}}if(ba>=0)if(ea>0){if(ka>0)ea=ka;for(F=0;F<aa.span[a];++F){S=ja[f+F];if(S!=-1){S=ka>0?e.config[f+F][0]:1;if(S>0){var qa=Math.round(ba* (S/ea));ba-=qa;ea-=S;ja[f+F]+=qa}}}}else ja[f]=ba}}}ia(function(T){return T.ps[a]},s);ia(function(T){return T.ms[a]},d)}var Q=0,Z=0;for(f=0;f<i;++f){if(d[f]>s[f])s[f]=d[f];if(d[f]>-1){Q+=s[f];Z+=d[f]}}b=0;k=true;j=false;for(f=0;f<i;++f)if(d[f]>-1){if(k){b+=e.margins[1];k=false}else{b+=e.margins[0];if(j)b+=4}j=e.config[f][1]!==0}k||(b+=e.margins[2]);Q+=b;Z+=b;e.measures=[s,d,Q,Z,b]}if(fa||m[2]!=e.measures[2])ra.updateSizeInParent(a);c&&e.minSize==0&&m[3]!=e.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&& v(c,\"min\"+e.Size,e.measures[3]+\"px\");c&&a==0&&c&&h.hasTag(c,\"TD\")&&v(c,e.size,e.measures[2]+\"px\")}function va(a,b,c){a=q[a];if(ga)c=-c;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;c=-c}a.fixedSize[b]=a.sizes[b]+c;G.layouts2.scheduleAdjust()}function wa(a,b,c){var e=b.di,i=q[a],n=q[a^1],m,s=h.getElement(C),d;for(d=e-1;d>=0;--d)if(i.sizes[d]>=0){m=-(i.sizes[d]-i.measures[1][d]);break}e=i.sizes[e]-i.measures[1][e];if(ga){var f=m;m=-e;e=-f}new h.SizeHandle(h,i.resizeDir,h.pxself(b,i.size),h.pxself(b, n.size),m,e,i.resizerClass,function(l){va(a,d,l)},b,s,c,0,0)}function sa(a,b){var c=a.config.length,e;if(a.config[b][1]!==0)for(e=b+1;e<c;++e)if(a.measures[1][e]>-1)return true;for(e=b-1;e>=0;--e)if(a.measures[1][e]>-1)return a.config[e][1]!==0;return false}function xa(a,b){var c=q[a],e=q[a^1],i=c.measures,n=0,m=false,s=false,d=false,f=la?b.parentNode:null;if(c.maxSize===0)if(f){var l=h.css(f,\"position\");d=a==1&&typeof f.nativeHeight!==\"undefined\";if(l===\"absolute\")n=d?h.parsePx(f.nativeHeight):h.pxself(f, c.size);if(n===0){if(!c.initialized){if(a===0&&(l===\"absolute\"||l===\"fixed\")){f.style.display=\"none\";n=f.clientWidth;f.style.display=\"\"}d||(n=a?f.clientHeight:f.clientWidth);m=true;if(a==0&&n==0&&h.isIElt9){n=f.offsetWidth;m=false}var j;if((h.hasTag(f,\"TD\")||h.hasTag(f,\"TH\")||$(f.parentNode).hasClass(\"Wt-domRoot\"))&&!(h.isIE&&!h.isIElt9)){d=0;j=1}else{d=c.minSize?c.minSize:i[3];j=0}function H(Q,Z){return Q-Z<=1}if(h.isIElt9&&H(n,j)||H(n,d+K(f,a)))c.maxSize=999999}if(n===0&&c.maxSize===0){n=a?f.clientHeight: f.clientWidth;m=true}}}else{n=h.pxself(b,c.size);s=true}else if(c.sizeSet){n=h.pxself(f,c.size);s=true}var o=0;if(f&&f.wtGetPS&&a==1)o=f.wtGetPS(f,b,a,0);d=i[2];if(d<c.minSize)d=c.minSize;if(c.maxSize&&!c.sizeSet){n=Math.min(d,c.maxSize)+o;v(f,c.size,n+ha(f,a)+\"px\")&&N&&N.setElDirty(I);n=n;d=s=true}c.cSize=n;if(a==1&&f&&f.wtResize){j=e.cSize;d=c.cSize;f.wtResize(f,Math.round(j),Math.round(d),true)}n-=o;if(!s){s=0;if(typeof c.cPadding===\"undefined\"){s=m?K(f,a):ha(f,a);c.cPadding=s}else s=c.cPadding; n-=s}c.initialized=true;if(!(f&&n<=0)){if(n<i[3]-o)n=i[3]-o;f=[];m=c.config.length;s=e.config.length;for(d=0;d<m;++d)c.stretched[d]=false;if(n>=i[3]-o){o=n-i[4];j=[];var r=[0,0],g=[0,0],k=0;for(d=0;d<m;++d)if(i[1][d]>-1){l=-1;sa(c,d)||(c.fixedSize[d]=undefined);if(typeof c.fixedSize[d]!==\"undefined\"&&(d+1==m||i[1][d+1]>-1))l=c.fixedSize[d];else if(sa(c,d)&&c.config[d][1]!==0&&c.config[d][1][0]>=0){l=c.config[d][1][0];if(c.config[d][1][1])l=(n-i[4])*l/100}if(l>=0){j[d]=-1;f[d]=l;o-=f[d]}else{if(c.config[d][0]> 0){l=1;j[d]=c.config[d][0];k+=j[d]}else{l=0;j[d]=0}r[l]+=i[1][d];g[l]+=i[0][d];f[d]=i[0][d]}}else{j[d]=-2;f[d]=-1}if(c.fixedSize.length>m)c.fixedSize.length=m;if(k==0){for(d=0;d<m;++d)if(j[d]==0){j[d]=1;++k}g[1]=g[0];r[1]=r[0];g[0]=0;r[0]=0}if(o>g[0]+r[1]){o-=g[0];if(o>g[1]){if(c.fitSize){o-=g[1];o=o/k;for(d=0;d<m;++d)if(j[d]>0){f[d]+=Math.round(j[d]*o);c.stretched[d]=true}}}else{l=1;if(o<r[l])o=r[l];o=g[l]-r[l]>0?(o-r[l])/(g[l]-r[l]):0;for(d=0;d<m;++d)if(j[d]>0){r=i[0][d]-i[1][d];f[d]=i[1][d]+Math.round(r* o)}}}else{for(d=0;d<m;++d)if(j[d]>0)f[d]=i[1][d];o-=r[1];l=0;if(o<r[l])o=r[l];o=g[l]-r[l]>0?(o-r[l])/(g[l]-r[l]):0;for(d=0;d<m;++d)if(j[d]==0){r=i[0][d]-i[1][d];f[d]=i[1][d]+Math.round(r*o)}}}else f=i[1];c.sizes=f;i=c.margins[1];o=true;r=false;for(d=0;d<m;++d){if(f[d]>-1){var z=r;if(r){r=C+\"-rs\"+a+\"-\"+d;j=h.getElement(r);if(!j){c.resizeHandles[d]=r;j=document.createElement(\"div\");j.setAttribute(\"id\",r);j.di=d;j.style.position=\"absolute\";j.style[e.left]=e.margins[1]+\"px\";j.style[c.size]=c.margins[0]+ \"px\";if(e.cSize)j.style[e.size]=e.cSize-e.margins[2]-e.margins[1]+\"px\";j.className=c.handleClass;b.insertBefore(j,b.firstChild);j.onmousedown=j.ontouchstart=function(Q){wa(a,this,Q||window.event)}}i+=2;v(j,c.left,i+\"px\");i+=2}else if(c.resizeHandles[d]){j=h.getElement(c.resizeHandles[d]);j.parentNode.removeChild(j);c.resizeHandles[d]=undefined}r=c.config[d][1]!==0;if(o)o=false;else i+=c.margins[0]}else if(c.resizeHandles[d]){j=h.getElement(c.resizeHandles[d]);j.parentNode.removeChild(j);c.resizeHandles[d]= undefined}for(g=0;g<s;++g)if((k=c.getItem(d,g))&&k.w){j=k.w;l=Math.max(f[d],0);if(k.span){var t,E=r;for(t=1;t<k.span[a];++t){if(d+t>=f.length)break;if(E)l+=4;E=c.config[d+t][1]!==0;if(f[d+t-1]>-1&&f[d+t]>-1)l+=c.margins[0];l+=f[d+t]}}var M;v(j,\"visibility\",\"\");E=k.align>>c.alignBits&15;t=k.ps[a];if(l<t)E=0;if(E){switch(E){case 1:M=i;break;case 4:M=i+(l-t)/2;break;case 2:M=i+(l-t);break}t-=k.margin[a];if(k.layout){v(j,c.size,t+\"px\")&&Y(k,1);k.set[a]=true}else if(l>=t&&k.set[a]){v(j,c.size,t+\"px\")&& Y(k,1);k.set[a]=false}k.size[a]=t;k.psize[a]=t}else{M=k.margin[a];E=Math.max(0,l-M);var ia=a==0&&k.sc[a];if(!ma(j)&&(ia||l!=t||k.layout)){if(v(j,c.size,E+\"px\")){Y(k,1);k.set[a]=true}}else if(k.fs[a])a==0&&v(j,c.size,k.fs[a]-M+\"px\");else{v(j,c.size,\"\")&&Y(k,1);if(k.set)k.set[a]=false}M=i;k.size[a]=E;k.psize[a]=l}if(X)if(z){v(j,c.left,\"4px\");l=h.css(j,\"position\");if(l!==\"absolute\")j.style.position=\"relative\"}else v(j,c.left,\"0px\");else v(j,c.left,M+\"px\");if(a==1){if(j.wtResize)j.wtResize(j,k.set[0]? Math.round(k.size[0]):-1,k.set[1]?Math.round(k.size[1]):-1,true);k.dirty=0}}if(f[d]>-1)i+=f[d]}if(c.resizeHandles.length>m){for(z=m;z<c.resizeHandles.length;z++)if(c.resizeHandles[z]){j=h.getElement(c.resizeHandles[z]);j.parentNode.removeChild(j)}c.resizeHandles.length=m}$(b).children(\".\"+e.handleClass).css(c.size,n-c.margins[2]-c.margins[1]+\"px\")}}function na(){var a=h.getElement(C);la=J==null;I=N=null;ta=oa=true;U=[];pa=false;if(la){var b=a;b=b.parentNode;for(U=[0,0];b!=document;){U[0]+=da(b,0); U[1]+=da(b,1);if(b.wtGetPS)pa=true;var c=jQuery.data(b.parentNode,\"layout\");if(c){I=b;N=c;break}b=b;b=b.parentNode;if(b.childNodes.length!=1&&!b.wtGetPS)oa=false}a=a.parentNode;for(b=0;b<2;++b)q[b].sizeSet=b==1&&typeof a.nativeHeight!==\"undefined\"?h.parsePx(a.nativeHeight)!=0:h.pxself(a,q[b].size)!=0}else{N=jQuery.data(document.getElementById(J),\"layout\");I=a;U[0]=da(I,0);U[1]=da(I,1)}}var h=G.WT;this.descendants=[];var ra=this,B=y,P=true,fa=true,la,N,I,oa,ta=false,U,pa,ga=$(document.body).hasClass(\"Wt-rtl\"), q=[{initialized:false,config:B.cols,margins:D,maxSize:ca,measures:[],sizes:[],stretched:[],fixedSize:[],Left:ga?\"Right\":\"Left\",left:ga?\"right\":\"left\",Right:ga?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return B.items[b*q[0].config.length+a]},setItem:function(a,b,c){B.items[b*q[0].config.length+a]=c},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:V,resizeHandles:[]},{initialized:false,config:B.rows,margins:x,maxSize:p,measures:[],sizes:[],stretched:[], fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,b){return B.items[a*q[0].config.length+b]},setItem:function(a,b,c){B.items[a*q[0].config.length+b]=c},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:W,resizeHandles:[]}];jQuery.data(document.getElementById(C),\"layout\",this);this.updateSizeInParent=function(a){if(N&&I.id){var b=h.$(I.id);if(b){if(I!=b)if(N=jQuery.data(b.parentNode,\"layout\"))I=b;else na()}else na()}if(N)if(oa){var c= q[a];b=c.measures[2];if(c.maxSize>0)b=Math.min(c.maxSize,b);if(pa){c=h.getElement(C);if(!c)return;for(var e=c,i=e.parentNode;;){if(i.wtGetPS)b=i.wtGetPS(i,e,a,b);b+=da(i,a);if(i==I)break;if(a==1&&i==c.parentNode&&!i.lh&&i.offsetHeight>b)b=i.offsetHeight;e=i;i=e.parentNode}}else b+=U[a];N.setChildSize(I,a,b)}};this.setConfig=function(a){var b=B;B=a;q[0].config=B.cols;q[1].config=B.rows;q[0].stretched=[];q[1].stretched=[];var c;a=0;for(c=b.items.length;a<c;++a){var e=b.items[a];if(e){var i=u(e.id); if(i){i.ps=e.ps;i.sc=e.sc;i.ms=e.ms;i.size=e.size;i.psize=e.psize;i.fs=e.fs;i.margin=e.margin;i.set=e.set}else if(e.set){e.set[0]&&v(e.w,q[0].size,\"\");e.set[1]&&v(e.w,q[1].size,\"\")}}}P=fa=true;G.layouts2.scheduleAdjust()};this.getId=function(){return C};this.setElDirty=function(a){if(a=u(a.id)){a.dirty=2;P=true;G.layouts2.scheduleAdjust()}};this.setItemsDirty=function(a){var b,c,e=q[0].config.length;b=0;for(c=a.length;b<c;++b){var i=B.items[a[b][0]*e+a[b][1]];if(i){i.dirty=2;if(i.layout){i.layout= false;i.wasLayout=true;G.layouts2.setChildLayoutsDirty(ra,i.w)}}}P=true};this.setDirty=function(){fa=true};this.setAllDirty=function(){var a,b;a=0;for(b=B.items.length;a<b;++a){var c=B.items[a];if(c)c.dirty=2}P=true};this.setChildSize=function(a,b,c){var e=q[0].config.length,i=q[b],n;if(a=u(a.id)){e=b===0?n%e:n/e;if(a.align>>i.alignBits&15||!i.stretched[e]){if(!a.ps)a.ps=[];a.ps[b]=c}a.layout=true;Y(a,1)}};this.measure=function(a){var b=h.getElement(C);if(b)if(!h.isHidden(b)){ta||na();if(P||fa)ua(a, b,la?b.parentNode:null);if(a==1)P=fa=false}};this.setMaxSize=function(a,b){q[0].maxSize=a;q[1].maxSize=b};this.apply=function(a){var b=h.getElement(C);if(!b)return false;if(h.isHidden(b))return true;xa(a,b);return true};this.contains=function(a){var b=h.getElement(C);a=h.getElement(a.getId());return b&&a?h.contains(b,a):false};this.WT=h}");
	}

	static WJavaScriptPreamble appjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.ApplicationScope,
				JavaScriptObjectType.JavaScriptObject,
				"layouts2",
				"new (function(){var G=[],C=false,J=this,V=false;this.find=function(p){return(p=document.getElementById(p))?jQuery.data(p,\"layout\"):null};this.setDirty=function(p){if(p=this.find(p)){p.setDirty();J.scheduleAdjust()}};this.setElementDirty=function(p){var D=p;for(p=p.parentNode;p&&p!=document.body;){var x=jQuery.data(p,\"layout\");x&&x.setElDirty(D);D=p;p=p.parentNode}};this.setChildLayoutsDirty=function(p,D){var x,y;x=0;for(y=p.descendants.length;x< y;++x){var u=p.descendants[x];if(D){var A=p.WT.getElement(u.getId());if(A&&!p.WT.contains(D,A))continue}u.setDirty()}};this.add=function(p){function D(x,y){var u,A;u=0;for(A=x.length;u<A;++u){var w=x[u];if(w.getId()==y.getId()){x[u]=y;y.descendants=w.descendants;return}else if(w.contains(y)){D(w.descendants,y);return}else if(y.contains(w)){y.descendants.push(w);x.splice(u,1);--u;--A}}x.push(y)}D(G,p);J.scheduleAdjust()};var W=false,X=0;this.scheduleAdjust=function(p){if(p)V=true;if(!W){if(C)++X;else X= 0;if(!(X>=6)){W=true;setTimeout(function(){J.adjust()},0)}}};this.adjust=function(p,D){function x(u,A){var w,O;w=0;for(O=u.length;w<O;++w){var K=u[w];x(K.descendants,A);if(A==1&&V)K.setDirty();else A==0&&K.setAllDirty();K.measure(A)}}function y(u,A){var w,O;w=0;for(O=u.length;w<O;++w){var K=u[w];if(K.apply(A))y(K.descendants,A);else{u.splice(w,1);--w;--O}}}if(p){(p=this.find(p))&&p.setItemsDirty(D);J.scheduleAdjust()}else{W=false;if(!C){C=true;x(G,0);y(G,0);x(G,1);y(G,1);V=C=false}}};this.updateConfig= function(p,D){(p=this.find(p))&&p.setConfig(D)};this.adjustNow=function(){W&&J.adjust()};var ca=null;window.onresize=function(){clearTimeout(ca);ca=setTimeout(function(){ca=null;J.scheduleAdjust(true)},20)};window.onshow=function(){V=true;J.adjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(a,f,c,e){function i(j){var k=b.px(j,\"marginTop\");k+=b.px(j,\"marginBottom\");if(!b.boxSizing(j)){k+=b.px(j,\"borderTopWidth\");k+=b.px(j,\"borderBottomWidth\");k+=b.px(j,\"paddingTop\");k+=b.px(j,\"paddingBottom\")}return k}var b=this,h=c>=0;a.lh=h&&e;a.style.height=h?c+\"px\":\"\";if(b.boxSizing(a)){c-=b.px(a,\"marginTop\");c-=b.px(a,\"marginBottom\");c-=b.px(a,\"borderTopWidth\");c-=b.px(a,\"borderBottomWidth\");c-=b.px(a,\"paddingTop\");c-=b.px(a,\"paddingBottom\"); f-=b.px(a,\"marginLeft\");f-=b.px(a,\"marginRight\");f-=b.px(a,\"borderLeftWidth\");f-=b.px(a,\"borderRightWidth\");f-=b.px(a,\"paddingLeft\");f-=b.px(a,\"paddingRight\")}var g,m,d;g=0;for(m=a.childNodes.length;g<m;++g){d=a.childNodes[g];if(d.nodeType==1)if(h){var l=c-i(d);if(l>0){if(d.offsetTop>0){var n=b.css(d,\"overflow\");if(n===\"visible\"||n===\"\")d.style.overflow=\"auto\"}if(d.wtResize)d.wtResize(d,f,l,e);else{l=l+\"px\";if(d.style.height!=l){d.style.height=l;d.lh=e}}}}else if(d.wtResize)d.wtResize(d,f,-1);else{d.style.height= \"\";d.lh=false}}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "ChildrenGetPS",
				"function(a,f,c,e){return e}");
	}

	static WJavaScriptPreamble wtjs12() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastResize",
				"function(a,f,c,e){var i=this,b=c>=0;a.lh=b&&e;a.style.height=b?c+\"px\":\"\";a=a.lastChild;var h=a.previousSibling;if(b){c-=h.offsetHeight+i.px(h,\"marginTop\")+i.px(h,\"marginBottom\");if(c>0)if(a.wtResize)a.wtResize(a,f,c,e);else{a.style.height=c+\"px\";a.lh=e}}else if(a.wtResize)a.wtResize(a,-1,-1);else{a.style.height=\"\";a.lh=false}}");
	}

	static WJavaScriptPreamble wtjs13() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastGetPS",
				"function(a,f,c,e){var i=this,b,h;b=0;for(h=a.childNodes.length;b<h;++b){var g=a.childNodes[b];if(g!=f){var m=i.css(g,\"position\");if(m!=\"absolute\"&&m!=\"fixed\")if(c===0)e=Math.max(e,g.offsetWidth);else e+=g.offsetHeight+i.px(g,\"marginTop\")+i.px(g,\"marginBottom\")}}return e}");
	}
}
