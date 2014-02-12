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
					app.getJavaScriptClass() + ".layouts2.adjustNow();");
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
		js.append(app.getJavaScriptClass()).append(
				".layouts2.add(new Wt3_3_2.StdLayout2(").append(
				app.getJavaScriptClass()).append(",'").append(this.getId())
				.append("',");
		if (this.getLayout().getParentLayout() != null) {
			js.append("'").append(
					getImpl(this.getLayout().getParentLayout()).getId())
					.append("',");
		} else {
			js.append("null,");
		}
		boolean progressive = !app.getEnvironment().hasAjax();
		js.append(fitWidth ? '1' : '0').append(",").append(
				fitHeight ? '1' : '0').append(",").append(
				progressive ? '1' : '0').append(",");
		js.append(maxWidth).append(",").append(maxHeight).append(",[").append(
				this.grid_.horizontalSpacing_).append(",").append(margin[3])
				.append(",").append(margin[1]).append("],[").append(
						this.grid_.verticalSpacing_).append(",").append(
						margin[0]).append(",").append(margin[2]).append("],");
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
			for (int ii = 0; ii < colCount * rowCount; ++ii)
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
								;
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
							td.setProperty(Property.PropertyStyle, style
									.toString());
							if (item.rowSpan_ != 1) {
								td.setProperty(Property.PropertyRowSpan, String
										.valueOf(item.rowSpan_));
							}
							if (item.colSpan_ != 1) {
								td.setProperty(Property.PropertyColSpan, String
										.valueOf(item.colSpan_));
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
										spacer
												.setProperty(
														Property.PropertyStyleWidth,
														c
																.getProperty(Property.PropertyStyleMinWidth));
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
									spacer
											.setProperty(
													Property.PropertyStyleWidth,
													c
															.getProperty(Property.PropertyStyleMinWidth));
									spacer
											.setProperty(
													Property.PropertyStyleHeight,
													"1px");
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
				parent.callJavaScript("Wt3_3_2.remove('"
						+ this.removedItems_.get(i) + "');", true);
			}
			this.removedItems_.clear();
			parent.addChild(div);
			StringBuilder js = new StringBuilder();
			js.append(app.getJavaScriptClass()).append(
					".layouts2.updateConfig('").append(this.getId()).append(
					"',");
			this.streamConfig(js, app);
			js.append(");");
			app.doJavaScript(js.toString());
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
						js.append("[").append((int) row).append(",").append(
								(int) col).append("]");
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
								.getImpl())
								: null)).updateDom(parent);
					}
				}
			}
		}
	}

	public void setHint(final String name, final String value) {
		logger.error(new StringWriter().append("unrecognized hint '").append(
				name).append("'").toString());
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
					js.append("dirty:2,id:'").append(id).append("'")
							.append("}");
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
				"function(G,H,I,P,Q,S,s,E,x,z,y){function B(a,b,c,e){function i(m){return m==\"visible\"||m==\"none\"}var j=u[b],k=b?a.scrollHeight:a.scrollWidth,t,d;if(b==0&&k+g.pxself(a,j.left)>=e.clientWidth){t=a.style[j.left];v(a,j.left,\"-1000000px\");k=b?a.scrollHeight:a.scrollWidth}e=b?a.clientHeight:a.clientWidth;if(g.isGecko&&!a.style[j.size]&&b==0&&i(g.css(a,\"overflow\"))){d=a.style[j.size];v(a,j.size,\"\")}b=b?a.offsetHeight:a.offsetWidth;t&&v(a,j.left,t); d&&v(a,j.size,d);if(e>=1E6)e-=1E6;if(k>=1E6)k-=1E6;if(b>=1E6)b-=1E6;if(k===0){k=g.pxself(a,j.size);if(k!==0&&!g.isOpera&&!g.isGecko)k-=g.px(a,\"border\"+j.Left+\"Width\")+g.px(a,\"border\"+j.Right+\"Width\")}if(g.isIE&&(g.hasTag(a,\"BUTTON\")||g.hasTag(a,\"TEXTAREA\")||g.hasTag(a,\"INPUT\")||g.hasTag(a,\"SELECT\")))k=e;if(k>b)if(g.pxself(a,j.size)==0)k=e;else{var o=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!==\"none\")o=true});if(o)k=e}d=g.px(a,\"border\"+j.Left+\"Width\")+g.px(a,\"border\"+j.Right+ \"Width\");t=b-(e+d)!=0;if(c)return[k,scrollBar];if(!g.boxSizing(a)&&!g.isOpera)k+=d;k+=g.px(a,\"margin\"+j.Left)+g.px(a,\"margin\"+j.Right);if(!g.boxSizing(a)&&!g.isIE)k+=g.px(a,\"padding\"+j.Left)+g.px(a,\"padding\"+j.Right);k+=b-(e+d);if(k<b)k=b;a=g.px(a,\"max\"+j.Size);if(a>0)k=Math.min(a,k);return[Math.round(k),t]}function w(a,b){b=u[b];if(a.style.display===\"none\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=g.px(a,\"min\"+b.Size);g.boxSizing(a)||(c+=g.px(a,\"padding\"+b.Left)+ g.px(a,\"padding\"+b.Right));return c}}function K(a,b){b=u[b];var c=g.px(a,\"margin\"+b.Left)+g.px(a,\"margin\"+b.Right);if(!g.boxSizing(a)&&!(g.isIE&&!g.isIElt9&&g.hasTag(a,\"BUTTON\")))c+=g.px(a,\"border\"+b.Left+\"Width\")+g.px(a,\"border\"+b.Right+\"Width\")+g.px(a,\"padding\"+b.Left)+g.px(a,\"padding\"+b.Right);return c}function J(a,b){b=u[b];return g.px(a,\"padding\"+b.Left)+g.px(a,\"padding\"+b.Right)}function aa(a,b){if(g.boxSizing(a)){b=u[b];return g.px(a,\"border\"+b.Left+\"Width\")+g.px(a,\"border\"+b.Right+\"Width\")+ g.px(a,\"padding\"+b.Left)+g.px(a,\"padding\"+b.Right)}else return 0}function W(a,b){b=u[b];return Math.round(g.px(a,\"border\"+b.Left+\"Width\")+g.px(a,\"border\"+b.Right+\"Width\")+g.px(a,\"margin\"+b.Left)+g.px(a,\"margin\"+b.Right)+g.px(a,\"padding\"+b.Left)+g.px(a,\"padding\"+b.Right))}function T(a,b,c){a.dirty=Math.max(a.dirty,b);M=true;c&&G.layouts2.scheduleAdjust()}function v(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function ga(a,b,c){var e=u[a],i=e.config.length,j=u[a^1].config.length, k=e.measures.slice();if(k.length==5){k[0]=k[0].slice();k[1]=k[1].slice()}if(M){if(c&&typeof e.minSize==\"undefined\"){e.minSize=g.px(c,\"min\"+e.Size);if(e.minSize>0)e.minSize-=aa(c,a)}var t=[],d=[],o=0,m=0,h,F,r=false;for(h=0;h<i;++h){var l=0,q=e.config[h][2],n=true;for(F=0;F<j;++F){var f=e.getItem(h,F);if(f){if(!f.w||a==0&&f.dirty>1){var p=$(\"#\"+f.id),C=p.get(0);if(!C){e.setItem(h,F,null);continue}if(C!=f.w){f.w=C;p.find(\"img\").add(p.filter(\"img\")).bind(\"load\",{item:f},function(ha){T(ha.data.item,1, true)})}}if(!S&&f.w.style.position!=\"absolute\"){f.w.style.position=\"absolute\";f.w.style.visibility=\"hidden\"}if(!f.ps)f.ps=[];if(!f.sc)f.sc=[];if(!f.ms)f.ms=[];if(!f.size)f.size=[];if(!f.psize)f.psize=[];if(!f.fs)f.fs=[];if(!f.margin)f.margin=[];if($(f.w).hasClass(\"Wt-hidden\"))f.ps[a]=f.ms[a]=0;else{p=!f.set;if(!f.set)f.set=[false,false];if(f.w){if(g.isIE)f.w.style.visibility=\"\";if(f.dirty){var D;if(f.dirty>1){w(f.w,a);f.ms[a]=D}else D=f.ms[a];if(D>q)q=D;if(f.dirty>1)f.margin[a]=K(f.w,a);if(!f.set[a])if(a== 0||!p){p=g.pxself(f.w,e.size);f.fs[a]=p?p+f.margin[a]:0}else{p=Math.round(g.px(f.w,e.size));f.fs[a]=p>Math.max(aa(f.w,a),D)?p+f.margin[a]:0}p=f.fs[a];if(f.layout){if(p==0)p=f.ps[a];f.ps[a]=p}else{if(f.wasLayout){f.wasLayout=false;f.set=[false,false];f.ps=[];f.w.wtResize&&f.w.wtResize(f.w,-1,-1,true);v(f.w,u[1].size,\"\")}C=B(f.w,a,false,b);var N=C[0],X=f.set[a];if(X)if(f.psize[a]>8)X=N>=f.psize[a]-4&&N<=f.psize[a]+4;var ia=typeof f.ps[a]!==\"undefined\"&&e.config[h][0]>0&&f.set[a];p=X||ia?Math.max(p, f.ps[a]):Math.max(p,N);f.ps[a]=p;f.sc[a]=C[1]}if(!f.span||f.span[a]==1){if(p>l)l=p}else r=true}else if(!f.span||f.span[a]==1){if(f.ps[a]>l)l=f.ps[a];if(f.ms[a]>q)q=f.ms[a]}else r=true;if(!(f.w.style.display===\"none\"&&!f.w.ed)&&(!f.span||f.span[a]==1))n=false}}}}if(n)q=l=-1;else if(q>l)l=q;t[h]=l;d[h]=q;if(q>-1){o+=l;m+=q}}if(r)for(h=0;h<i;++h)for(F=0;F<j;++F)if((f=e.getItem(h,F))&&f.span&&f.span[a]>1){b=f.ps[a];for(q=l=r=0;q<f.span[a];++q){n=t[h+q];if(n!=-1){b-=n;++r;if(e.config[h+q][0]>0)l+=e.config[h+ q][0]}}if(b>0)if(r>0){if(l>0)r=l;for(q=0;q<f.span[a];++q){n=t[h+q];if(n!=-1){n=l>0?e.config[h+q][0]:1;if(n>0){D=Math.round(b/n);b-=D;r-=n;t[h+q]+=D}}}}else t[h]=b}j=0;p=true;F=false;for(h=0;h<i;++h)if(d[h]>-1){if(p){j+=e.margins[1];p=false}else{j+=e.margins[0];if(F)j+=4}F=e.config[h][1]!==0}p||(j+=e.margins[2]);o+=j;m+=j;e.measures=[t,d,o,m,j]}if(Y||k[2]!=e.measures[2])ba.updateSizeInParent(a);c&&e.minSize==0&&k[3]!=e.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&&v(c,\"min\"+e.Size,e.measures[3]+ \"px\");c&&a==0&&c&&g.hasTag(c,\"TD\")&&v(c,e.size,e.measures[2]+\"px\")}function ja(a,b,c){a=u[a];if(Z)c=-c;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;c=-c}a.fixedSize[b]=a.sizes[b]+c;G.layouts2.scheduleAdjust()}function ka(a,b,c){var e=b.di,i=u[a],j=u[a^1],k,t=g.getElement(H),d;for(d=e-1;d>=0;--d)if(i.sizes[d]>=0){k=-(i.sizes[d]-i.measures[1][d]);break}e=i.sizes[e]-i.measures[1][e];if(Z){var o=k;k=-e;e=-o}new g.SizeHandle(g,i.resizeDir,g.pxself(b,i.size),g.pxself(b,j.size),k,e,i.resizerClass,function(m){ja(a, d,m)},b,t,c,0,0)}function la(a,b){var c=u[a],e=u[a^1],i=c.measures,j=0,k=false,t=false,d=false,o=ca?b.parentNode:null;if(c.maxSize===0)if(o){var m=g.css(o,\"position\");if(m===\"absolute\")j=g.pxself(o,c.size);if(j===0){if(!c.initialized){if(a===0&&(m===\"absolute\"||m===\"fixed\")){o.style.display=\"none\";j=o.clientWidth;o.style.display=\"\"}j=a?o.clientHeight:o.clientWidth;k=true;if(a==0&&j==0&&g.isIElt9){j=o.offsetWidth;k=false}var h;if((g.hasTag(o,\"TD\")||g.hasTag(o,\"TH\"))&&!(g.isIE&&!g.isIElt9)){d=0;h=1}else{d= c.minSize?c.minSize:i[3];h=0}function F(N,X){return N-X<=1}if(g.isIElt9&&epsEqual(j,h)||F(j,d+J(o,a)))c.maxSize=999999}if(j===0&&c.maxSize===0){j=a?o.clientHeight:o.clientWidth;k=true}}}else{j=g.pxself(b,c.size);t=true}else if(c.sizeSet){j=g.pxself(o,c.size);t=true}var r=0;if(o&&o.wtGetPS&&a==1)r=o.wtGetPS(o,b,a,0);d=i[2];if(d<c.minSize)d=c.minSize;if(c.maxSize&&!c.sizeSet){j=Math.min(d,c.maxSize)+r;v(o,c.size,j+aa(o,a)+\"px\")&&U&&U.setElDirty(O);j=j;d=t=true}c.cSize=j;if(a==1&&o&&o.wtResize){h=e.cSize; d=c.cSize;o.wtResize(o,Math.round(h),Math.round(d),true)}j-=r;if(!t){t=0;if(typeof c.cPadding===\"undefined\"){t=k?J(o,a):aa(o,a);c.cPadding=t}else t=c.cPadding;j-=t}c.initialized=true;if(!(o&&j<=0)){if(j<i[3]-r)j=i[3]-r;k=[];o=c.config.length;t=e.config.length;for(d=0;d<o;++d)c.stretched[d]=false;if(j>=i[3]-r){r=j-i[4];h=[];var l=[0,0],q=[0,0],n=0;for(d=0;d<o;++d)if(i[1][d]>-1){m=-1;if(typeof c.fixedSize[d]!==\"undefined\"&&(d+1==o||i[1][d+1]>-1))m=c.fixedSize[d];else if(c.config[d][1]!==0&&c.config[d][1][0]>= 0){m=c.config[d][1][0];if(c.config[d][1][1])m=(j-i[4])*m/100}if(m>=0){h[d]=-1;k[d]=m;r-=k[d]}else{if(c.config[d][0]>0){m=1;h[d]=c.config[d][0];n+=h[d]}else{m=0;h[d]=0}l[m]+=i[1][d];q[m]+=i[0][d];k[d]=i[0][d]}}else{h[d]=-2;k[d]=-1}if(n==0){for(d=0;d<o;++d)if(h[d]==0){h[d]=1;++n}q[1]=q[0];l[1]=l[0];q[0]=0;l[0]=0}if(r>q[0]+l[1]){r-=q[0];if(r>q[1]){if(c.fitSize){r-=q[1];r=r/n;for(d=0;d<o;++d)if(h[d]>0){k[d]+=Math.round(h[d]*r);c.stretched[d]=true}}}else{m=1;if(r<l[m])r=l[m];r=q[m]-l[m]>0?(r-l[m])/(q[m]- l[m]):0;for(d=0;d<o;++d)if(h[d]>0){l=i[0][d]-i[1][d];k[d]=i[1][d]+Math.round(l*r)}}}else{for(d=0;d<o;++d)if(h[d]>0)k[d]=i[1][d];r-=l[1];m=0;if(r<l[m])r=l[m];r=q[m]-l[m]>0?(r-l[m])/(q[m]-l[m]):0;for(d=0;d<o;++d)if(h[d]==0){l=i[0][d]-i[1][d];k[d]=i[1][d]+Math.round(l*r)}}}else k=i[1];c.sizes=k;i=c.margins[1];r=true;l=false;for(d=0;d<o;++d){if(k[d]>-1){var f=l;if(l){h=H+\"-rs\"+a+\"-\"+d;l=g.getElement(h);if(!l){l=document.createElement(\"div\");l.setAttribute(\"id\",h);l.di=d;l.style.position=\"absolute\";l.style[e.left]= e.margins[1]+\"px\";l.style[c.size]=c.margins[0]+\"px\";if(e.cSize)l.style[e.size]=e.cSize-e.margins[2]-e.margins[1]+\"px\";l.className=c.handleClass;b.insertBefore(l,b.firstChild);l.onmousedown=l.ontouchstart=function(N){ka(a,this,N||window.event)}}i+=2;v(l,c.left,i+\"px\");i+=2}l=c.config[d][1]!==0;if(r)r=false;else i+=c.margins[0]}for(q=0;q<t;++q)if((n=c.getItem(d,q))&&n.w){h=n.w;m=Math.max(k[d],0);if(n.span){var p,C=l;for(p=1;p<n.span[a];++p){if(d+p>=k.length)break;if(C)m+=4;C=c.config[d+p][1]!==0;if(k[d+ p-1]>-1&&k[d+p]>-1)m+=c.margins[0];m+=k[d+p]}}var D;v(h,\"visibility\",\"\");C=n.align>>c.alignBits&15;p=n.ps[a];if(m<p)C=0;if(C){switch(C){case 1:D=i;break;case 4:D=i+(m-p)/2;break;case 2:D=i+(m-p);break}p-=n.margin[a];if(n.layout){v(h,c.size,p+\"px\")&&T(n,1);n.set[a]=true}else if(m>=p&&n.set[a]){v(h,c.size,p+\"px\")&&T(n,1);n.set[a]=false}n.size[a]=p;n.psize[a]=p}else{C=Math.max(0,m-n.margin[a]);D=a==0&&n.sc[a];if(!(h.style.display===\"none\"&&!h.ed)&&(D||m!=p||n.layout)){if(v(h,c.size,C+\"px\")){if(!g.isIE&& (g.hasTag(h,\"TEXTAREA\")||g.hasTag(h,\"INPUT\"))){v(h,\"margin-\"+c.left,n.margin[a]/2+\"px\");v(h,\"margin-\"+e.left,n.margin[!a]/2+\"px\")}T(n,1);n.set[a]=true}}else if(n.fs[a])a==0&&v(h,c.size,n.fs[a]+\"px\");else{v(h,c.size,\"\")&&T(n,1);n.set[a]=false}D=i;n.size[a]=C;n.psize[a]=m}if(S)if(f){v(h,c.left,\"4px\");m=g.css(h,\"position\");if(m!==\"absolute\")h.style.position=\"relative\"}else v(h,c.left,\"0px\");else v(h,c.left,D+\"px\");if(a==1){if(h.wtResize)h.wtResize(h,n.set[0]?Math.round(n.size[0]):-1,n.set[1]?Math.round(n.size[1]): -1,true);n.dirty=0}}if(k[d]>-1)i+=k[d]}$(b).children(\".\"+e.handleClass).css(c.size,j-c.margins[2]-c.margins[1]+\"px\")}}var g=G.WT;this.descendants=[];var ba=this,A=y,M=true,Y=true,ca=false,U=null,O=null,da=false,ea=false,V=[],fa=false,Z=$(document.body).hasClass(\"Wt-rtl\"),u=[{initialized:false,config:A.cols,margins:x,maxSize:s,measures:[],sizes:[],stretched:[],fixedSize:[],Left:Z?\"Right\":\"Left\",left:Z?\"right\":\"left\",Right:Z?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return A.items[b* u[0].config.length+a]},setItem:function(a,b,c){A.items[b*u[0].config.length+a]=c},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:P},{initialized:false,config:A.rows,margins:z,maxSize:E,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,b){return A.items[a*u[0].config.length+b]},setItem:function(a,b,c){A.items[a*u[0].config.length+b]=c},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\", fitSize:Q}];jQuery.data(document.getElementById(H),\"layout\",this);this.updateSizeInParent=function(a){if(U)if(da){var b=u[a],c=b.measures[2];if(b.maxSize>0)c=Math.min(b.maxSize,c);if(fa){b=g.getElement(H);if(!b)return;for(var e=b,i=e.parentNode;;){if(i.wtGetPS)c=i.wtGetPS(i,e,a,c);c+=W(i,a);if(i==O)break;if(a==1&&i==b.parentNode&&!i.lh&&i.offsetHeight>c)c=i.offsetHeight;e=i;i=e.parentNode}}else c+=V[a];U.setChildSize(O,a,c)}};this.setConfig=function(a){var b=A;A=a;u[0].config=A.cols;u[1].config=A.rows; u[0].stretched=[];u[1].stretched=[];var c;a=0;for(c=b.items.length;a<c;++a){var e=b.items[a];if(e){if(e.set){e.set[0]&&v(e.w,u[0].size,\"\");e.set[1]&&v(e.w,u[1].size,\"\")}if(e.layout){ba.setChildSize(e.w,0,e.ps[0]);ba.setChildSize(e.w,1,e.ps[1])}}}M=Y=true;G.layouts2.scheduleAdjust()};this.getId=function(){return H};this.setElDirty=function(a){var b,c;b=0;for(c=A.items.length;b<c;++b){var e=A.items[b];if(e&&e.id==a.id){e.dirty=2;M=true;G.layouts2.scheduleAdjust();return}}};this.setItemsDirty=function(a){var b, c,e=u[0].config.length;b=0;for(c=a.length;b<c;++b){var i=A.items[a[b][0]*e+a[b][1]];i.dirty=2;if(i.layout){i.layout=false;i.wasLayout=true;G.layouts2.setChildLayoutsDirty(ba,i.w)}}M=true};this.setDirty=function(){Y=true};this.setAllDirty=function(){var a,b;a=0;for(b=A.items.length;a<b;++a){var c=A.items[a];if(c)c.dirty=2}M=true};this.setChildSize=function(a,b,c){var e=u[0].config.length,i=u[b],j,k;j=0;for(k=A.items.length;j<k;++j){var t=A.items[j];if(t&&t.id==a.id){a=b===0?j%e:j/e;if(t.align>>i.alignBits& 15||!i.stretched[a]){if(!t.ps)t.ps=[];t.ps[b]=c}t.layout=true;T(t,1);break}}};this.measure=function(a){var b=g.getElement(H);if(b)if(!g.isHidden(b)){if(!ea){ea=true;ca=I==null;da=true;if(ca){var c=b;c=c.parentNode;for(V=[0,0];c!=document;){V[0]+=W(c,0);V[1]+=W(c,1);if(c.wtGetPS)fa=true;var e=jQuery.data(c.parentNode,\"layout\");if(e){U=e;O=c;break}c=c;c=c.parentNode;if(c.childNodes.length!=1&&!c.wtGetPS)da=false}c=b.parentNode;for(e=0;e<2;++e)u[e].sizeSet=g.pxself(c,u[e].size)!=0}else{U=jQuery.data(document.getElementById(I), \"layout\");O=b;V[0]=W(O,0);V[1]=W(O,1)}}if(M||Y){c=ca?b.parentNode:null;ga(a,b,c)}if(a==1)M=Y=false}};this.setMaxSize=function(a,b){u[0].maxSize=a;u[1].maxSize=b};this.apply=function(a){var b=g.getElement(H);if(!b)return false;if(g.isHidden(b))return true;la(a,b);return true};this.contains=function(a){var b=g.getElement(H);a=g.getElement(a.getId());return b&&a?g.contains(b,a):false};this.WT=g}");
	}

	static WJavaScriptPreamble appjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.ApplicationScope,
				JavaScriptObjectType.JavaScriptObject,
				"layouts2",
				"new (function(){var G=[],H=false,I=this,P=false;this.find=function(s){return jQuery.data(document.getElementById(s),\"layout\")};this.setDirty=function(s){if(s=this.find(s)){s.setDirty();I.scheduleAdjust()}};this.setElementDirty=function(s){var E=s;for(s=s.parentNode;s&&s!=document.body;){var x=jQuery.data(s,\"layout\");x&&x.setElDirty(E);E=s;s=s.parentNode}};this.setChildLayoutsDirty=function(s,E){var x,z;x=0;for(z=s.descendants.length;x<z;++x){var y= s.descendants[x];if(E){var B=s.WT.getElement(y.getId());if(B&&!s.WT.contains(E,B))continue}y.setDirty()}};this.add=function(s){function E(x,z){var y,B;y=0;for(B=x.length;y<B;++y){var w=x[y];if(w.getId()==z.getId()){x[y]=z;z.descendants=w.descendants;return}else if(w.contains(z)){E(w.descendants,z);return}else if(z.contains(w)){z.descendants.push(w);x.splice(y,1);--y;--B}}x.push(z)}E(G,s);I.scheduleAdjust()};var Q=false;this.scheduleAdjust=function(s){if(s)P=true;if(!Q){Q=true;setTimeout(function(){I.adjust()}, 0)}};this.adjust=function(s,E){function x(y,B){var w,K;w=0;for(K=y.length;w<K;++w){var J=y[w];x(J.descendants,B);if(B==1&&P)J.setDirty();else B==0&&J.setAllDirty();J.measure(B)}}function z(y,B){var w,K;w=0;for(K=y.length;w<K;++w){var J=y[w];if(J.apply(B))z(J.descendants,B);else{y.splice(w,1);--w;--K}}}if(s){(s=this.find(s))&&s.setItemsDirty(E);I.scheduleAdjust()}else{Q=false;if(!H){H=true;x(G,0);z(G,0);x(G,1);z(G,1);P=H=false}}};this.updateConfig=function(s,E){(s=this.find(s))&&s.setConfig(E)};this.adjustNow= function(){Q&&I.adjust()};var S=null;window.onresize=function(){clearTimeout(S);S=setTimeout(function(){S=null;I.scheduleAdjust(true)},20)};window.onshow=function(){P=true;I.adjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(a,f,c,d){function i(j){var k=b.px(j,\"marginTop\");k+=b.px(j,\"marginBottom\");if(!b.boxSizing(j)){k+=b.px(j,\"borderTopWidth\");k+=b.px(j,\"borderBottomWidth\");k+=b.px(j,\"paddingTop\");k+=b.px(j,\"paddingBottom\")}return k}var b=this,h=c>=0;a.lh=h&&d;a.style.height=h?c+\"px\":\"\";if(b.boxSizing(a)){c-=b.px(a,\"marginTop\");c-=b.px(a,\"marginBottom\");c-=b.px(a,\"borderTopWidth\");c-=b.px(a,\"borderBottomWidth\");c-=b.px(a,\"paddingTop\");c-=b.px(a,\"paddingBottom\"); f-=b.px(a,\"marginLeft\");f-=b.px(a,\"marginRight\");f-=b.px(a,\"borderLeftWidth\");f-=b.px(a,\"borderRightWidth\");f-=b.px(a,\"paddingLeft\");f-=b.px(a,\"paddingRight\")}var g,m,e;g=0;for(m=a.childNodes.length;g<m;++g){e=a.childNodes[g];if(e.nodeType==1)if(h){var l=c-i(e);if(l>0)if(e.wtResize)e.wtResize(e,f,l,d);else{l=l+\"px\";if(e.style.height!=l){e.style.height=l;e.lh=d}}}else if(e.wtResize)e.wtResize(e,f,-1);else{e.style.height=\"\";e.lh=false}}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "ChildrenGetPS",
				"function(a,f,c,d){return d}");
	}

	static WJavaScriptPreamble wtjs12() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastResize",
				"function(a,f,c,d){var i=this,b=c>=0;a.lh=b&&d;a.style.height=b?c+\"px\":\"\";a=a.lastChild;var h=a.previousSibling;if(b){c-=h.offsetHeight+i.px(h,\"marginTop\")+i.px(h,\"marginBottom\");if(c>0)if(a.wtResize)a.wtResize(a,f,c,d);else{a.style.height=c+\"px\";a.lh=d}}else if(a.wtResize)a.wtResize(a,-1,-1);else{a.style.height=\"\";a.lh=false}}");
	}

	static WJavaScriptPreamble wtjs13() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastGetPS",
				"function(a,f,c,d){var i=this,b,h;b=0;for(h=a.childNodes.length;b<h;++b){var g=a.childNodes[b];if(g!=f){var m=i.css(g,\"position\");if(m!=\"absolute\"&&m!=\"fixed\")if(c===0)d=Math.max(d,g.offsetWidth);else d+=g.offsetHeight+i.px(g,\"marginTop\")+i.px(g,\"marginBottom\")}}return d}");
	}
}
