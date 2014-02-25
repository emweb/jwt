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
				"function(E,F,J,T,U,V,r,D,y,A,z){function C(a,b,c,e){function i(l){return l==\"visible\"||l==\"none\"}var k=s[b],j=b?a.scrollHeight:a.scrollWidth,t,d;if(b==0&&j+f.pxself(a,k.left)>=e.clientWidth){t=a.style[k.left];v(a,k.left,\"-1000000px\");j=b?a.scrollHeight:a.scrollWidth}e=b?a.clientHeight:a.clientWidth;if(f.isGecko&&!a.style[k.size]&&b==0&&i(f.css(a,\"overflow\"))){d=a.style[k.size];v(a,k.size,\"\")}b=b?a.offsetHeight:a.offsetWidth;t&&v(a,k.left,t); d&&v(a,k.size,d);if(e>=1E6)e-=1E6;if(j>=1E6)j-=1E6;if(b>=1E6)b-=1E6;if(j===0){j=f.pxself(a,k.size);if(j!==0&&!f.isOpera&&!f.isGecko)j-=f.px(a,\"border\"+k.Left+\"Width\")+f.px(a,\"border\"+k.Right+\"Width\")}if(f.isIE&&(f.hasTag(a,\"BUTTON\")||f.hasTag(a,\"TEXTAREA\")||f.hasTag(a,\"INPUT\")||f.hasTag(a,\"SELECT\")))j=e;if(j>b)if(f.pxself(a,k.size)==0)j=e;else{var n=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!==\"none\")n=true});if(n)j=e}d=f.px(a,\"border\"+k.Left+\"Width\")+f.px(a,\"border\"+k.Right+ \"Width\");t=b-(e+d)!=0;if(c)return[j,scrollBar];if(!f.boxSizing(a)&&!f.isOpera)j+=d;j+=f.px(a,\"margin\"+k.Left)+f.px(a,\"margin\"+k.Right);if(!f.boxSizing(a)&&!f.isIE)j+=f.px(a,\"padding\"+k.Left)+f.px(a,\"padding\"+k.Right);j+=b-(e+d);if(j<b)j=b;a=f.px(a,\"max\"+k.Size);if(a>0)j=Math.min(a,j);return[Math.round(j),t]}function x(a,b){b=s[b];if(a.style.display===\"none\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=f.px(a,\"min\"+b.Size);f.boxSizing(a)||(c+=f.px(a,\"padding\"+b.Left)+ f.px(a,\"padding\"+b.Right));return c}}function M(a,b){b=s[b];var c=f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right);if(!f.boxSizing(a)&&!(f.isIE&&!f.isIElt9&&f.hasTag(a,\"BUTTON\")))c+=f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right);return c}function K(a,b){b=s[b];return f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}function ea(a,b){if(f.boxSizing(a)){b=s[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+ f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}else return 0}function aa(a,b){b=s[b];return Math.round(f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right)+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right))}function W(a,b,c){a.dirty=Math.max(a.dirty,b);N=true;c&&E.layouts2.scheduleAdjust()}function v(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function qa(a,b,c){var e=s[a],i=e.config.length,k=s[a^1].config.length, j=e.measures.slice();if(j.length==5){j[0]=j[0].slice();j[1]=j[1].slice()}if(N){if(c&&typeof e.minSize==\"undefined\"){e.minSize=f.px(c,\"min\"+e.Size);if(e.minSize>0)e.minSize-=ea(c,a)}var t=[],d=[],n=0,l=0,h,H,q=false;for(h=0;h<i;++h){var m=0,u=e.config[h][2],p=true;for(H=0;H<k;++H){var g=e.getItem(h,H);if(g){if(!g.w||a==0&&g.dirty>1){var o=$(\"#\"+g.id),w=o.get(0);if(!w){e.setItem(h,H,null);continue}if(w!=g.w){g.w=w;o.find(\"img\").add(o.filter(\"img\")).bind(\"load\",{item:g},function(P){W(P.data.item,1,true)})}}if(!V&& g.w.style.position!=\"absolute\"){g.w.style.position=\"absolute\";g.w.style.visibility=\"hidden\"}if(!g.ps)g.ps=[];if(!g.sc)g.sc=[];if(!g.ms)g.ms=[];if(!g.size)g.size=[];if(!g.psize)g.psize=[];if(!g.fs)g.fs=[];if(!g.margin)g.margin=[];if($(g.w).hasClass(\"Wt-hidden\"))g.ps[a]=g.ms[a]=0;else{o=!g.set;if(!g.set)g.set=[false,false];if(g.w){if(f.isIE)g.w.style.visibility=\"\";if(g.dirty){if(g.dirty>1){w=x(g.w,a);g.ms[a]=w}else w=g.ms[a];if(w>u)u=w;if(g.dirty>1)g.margin[a]=M(g.w,a);if(!g.set[a])if(a==0||!o){o=f.pxself(g.w, e.size);g.fs[a]=o?o+g.margin[a]:0}else{o=Math.round(f.px(g.w,e.size));g.fs[a]=o>Math.max(ea(g.w,a),w)?o+g.margin[a]:0}o=g.fs[a];if(g.layout){if(o==0)o=g.ps[a];g.ps[a]=o}else{if(g.wasLayout){g.wasLayout=false;g.set=[false,false];g.ps=[];g.w.wtResize&&g.w.wtResize(g.w,-1,-1,true);v(g.w,s[1].size,\"\")}w=C(g.w,a,false,b);var I=w[0],Q=g.set[a];if(Q)if(g.psize[a]>8)Q=I>=g.psize[a]-4&&I<=g.psize[a]+4;var ka=typeof g.ps[a]!==\"undefined\"&&e.config[h][0]>0&&g.set[a];o=Q||ka?Math.max(o,g.ps[a]):Math.max(o,I); g.ps[a]=o;g.sc[a]=w[1]}if(!g.span||g.span[a]==1){if(o>m)m=o}else q=true}else if(!g.span||g.span[a]==1){if(g.ps[a]>m)m=g.ps[a];if(g.ms[a]>u)u=g.ms[a]}else q=true;if(!(g.w.style.display===\"none\"&&!g.w.ed)&&(!g.span||g.span[a]==1))p=false}}}}if(p)u=m=-1;else if(u>m)m=u;t[h]=m;d[h]=u;if(u>-1){n+=m;l+=u}}if(q){function ma(P,fa){for(h=0;h<i;++h)for(H=0;H<k;++H){var X=e.getItem(h,H);if(X&&X.span&&X.span[a]>1){var ba=P(X),ga=0,ha=0,G;for(G=0;G<X.span[a];++G){var O=fa[h+G];if(O!=-1){ba-=O;++ga;if(e.config[h+ G][0]>0)ha+=e.config[h+G][0]}}if(ba>=0)if(ga>0){if(ha>0)ga=ha;for(G=0;G<X.span[a];++G){O=fa[h+G];if(O!=-1){O=ha>0?e.config[h+G][0]:1;if(O>0){var na=Math.round(ba/O);ba-=na;ga-=O;fa[h+G]+=na}}}}else fa[h]=ba}}}ma(function(P){return P.ps[a]},t);ma(function(P){return P.ms[a]},d)}b=0;o=true;q=false;for(h=0;h<i;++h)if(d[h]>-1){if(o){b+=e.margins[1];o=false}else{b+=e.margins[0];if(q)b+=4}q=e.config[h][1]!==0}o||(b+=e.margins[2]);n+=b;l+=b;e.measures=[t,d,n,l,b]}if(ca||j[2]!=e.measures[2])ia.updateSizeInParent(a); c&&e.minSize==0&&j[3]!=e.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&&v(c,\"min\"+e.Size,e.measures[3]+\"px\");c&&a==0&&c&&f.hasTag(c,\"TD\")&&v(c,e.size,e.measures[2]+\"px\")}function ra(a,b,c){a=s[a];if(da)c=-c;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;c=-c}a.fixedSize[b]=a.sizes[b]+c;E.layouts2.scheduleAdjust()}function sa(a,b,c){var e=b.di,i=s[a],k=s[a^1],j,t=f.getElement(F),d;for(d=e-1;d>=0;--d)if(i.sizes[d]>=0){j=-(i.sizes[d]-i.measures[1][d]);break}e=i.sizes[e]-i.measures[1][e];if(da){var n= j;j=-e;e=-n}new f.SizeHandle(f,i.resizeDir,f.pxself(b,i.size),f.pxself(b,k.size),j,e,i.resizerClass,function(l){ra(a,d,l)},b,t,c,0,0)}function ta(a,b){var c=s[a],e=s[a^1],i=c.measures,k=0,j=false,t=false,d=false,n=ja?b.parentNode:null;if(c.maxSize===0)if(n){var l=f.css(n,\"position\");if(l===\"absolute\")k=f.pxself(n,c.size);if(k===0){if(!c.initialized){if(a===0&&(l===\"absolute\"||l===\"fixed\")){n.style.display=\"none\";k=n.clientWidth;n.style.display=\"\"}k=a?n.clientHeight:n.clientWidth;j=true;if(a==0&&k== 0&&f.isIElt9){k=n.offsetWidth;j=false}var h;if((f.hasTag(n,\"TD\")||f.hasTag(n,\"TH\"))&&!(f.isIE&&!f.isIElt9)){d=0;h=1}else{d=c.minSize?c.minSize:i[3];h=0}function H(Q,ka){return Q-ka<=1}if(f.isIElt9&&epsEqual(k,h)||H(k,d+K(n,a)))c.maxSize=999999}if(k===0&&c.maxSize===0){k=a?n.clientHeight:n.clientWidth;j=true}}}else{k=f.pxself(b,c.size);t=true}else if(c.sizeSet){k=f.pxself(n,c.size);t=true}var q=0;if(n&&n.wtGetPS&&a==1)q=n.wtGetPS(n,b,a,0);d=i[2];if(d<c.minSize)d=c.minSize;if(c.maxSize&&!c.sizeSet){k= Math.min(d,c.maxSize)+q;v(n,c.size,k+ea(n,a)+\"px\")&&Y&&Y.setElDirty(S);k=k;d=t=true}c.cSize=k;if(a==1&&n&&n.wtResize){h=e.cSize;d=c.cSize;n.wtResize(n,Math.round(h),Math.round(d),true)}k-=q;if(!t){t=0;if(typeof c.cPadding===\"undefined\"){t=j?K(n,a):ea(n,a);c.cPadding=t}else t=c.cPadding;k-=t}c.initialized=true;if(!(n&&k<=0)){if(k<i[3]-q)k=i[3]-q;j=[];n=c.config.length;t=e.config.length;for(d=0;d<n;++d)c.stretched[d]=false;if(k>=i[3]-q){q=k-i[4];h=[];var m=[0,0],u=[0,0],p=0;for(d=0;d<n;++d)if(i[1][d]> -1){l=-1;if(typeof c.fixedSize[d]!==\"undefined\"&&(d+1==n||i[1][d+1]>-1))l=c.fixedSize[d];else if(c.config[d][1]!==0&&c.config[d][1][0]>=0){l=c.config[d][1][0];if(c.config[d][1][1])l=(k-i[4])*l/100}if(l>=0){h[d]=-1;j[d]=l;q-=j[d]}else{if(c.config[d][0]>0){l=1;h[d]=c.config[d][0];p+=h[d]}else{l=0;h[d]=0}m[l]+=i[1][d];u[l]+=i[0][d];j[d]=i[0][d]}}else{h[d]=-2;j[d]=-1}if(p==0){for(d=0;d<n;++d)if(h[d]==0){h[d]=1;++p}u[1]=u[0];m[1]=m[0];u[0]=0;m[0]=0}if(q>u[0]+m[1]){q-=u[0];if(q>u[1]){if(c.fitSize){q-=u[1]; q=q/p;for(d=0;d<n;++d)if(h[d]>0){j[d]+=Math.round(h[d]*q);c.stretched[d]=true}}}else{l=1;if(q<m[l])q=m[l];q=u[l]-m[l]>0?(q-m[l])/(u[l]-m[l]):0;for(d=0;d<n;++d)if(h[d]>0){m=i[0][d]-i[1][d];j[d]=i[1][d]+Math.round(m*q)}}}else{for(d=0;d<n;++d)if(h[d]>0)j[d]=i[1][d];q-=m[1];l=0;if(q<m[l])q=m[l];q=u[l]-m[l]>0?(q-m[l])/(u[l]-m[l]):0;for(d=0;d<n;++d)if(h[d]==0){m=i[0][d]-i[1][d];j[d]=i[1][d]+Math.round(m*q)}}}else j=i[1];c.sizes=j;i=c.margins[1];q=true;m=false;for(d=0;d<n;++d){if(j[d]>-1){var g=m;if(m){h= F+\"-rs\"+a+\"-\"+d;m=f.getElement(h);if(!m){m=document.createElement(\"div\");m.setAttribute(\"id\",h);m.di=d;m.style.position=\"absolute\";m.style[e.left]=e.margins[1]+\"px\";m.style[c.size]=c.margins[0]+\"px\";if(e.cSize)m.style[e.size]=e.cSize-e.margins[2]-e.margins[1]+\"px\";m.className=c.handleClass;b.insertBefore(m,b.firstChild);m.onmousedown=m.ontouchstart=function(Q){sa(a,this,Q||window.event)}}i+=2;v(m,c.left,i+\"px\");i+=2}m=c.config[d][1]!==0;if(q)q=false;else i+=c.margins[0]}for(u=0;u<t;++u)if((p=c.getItem(d, u))&&p.w){h=p.w;l=Math.max(j[d],0);if(p.span){var o,w=m;for(o=1;o<p.span[a];++o){if(d+o>=j.length)break;if(w)l+=4;w=c.config[d+o][1]!==0;if(j[d+o-1]>-1&&j[d+o]>-1)l+=c.margins[0];l+=j[d+o]}}var I;v(h,\"visibility\",\"\");w=p.align>>c.alignBits&15;o=p.ps[a];if(l<o)w=0;if(w){switch(w){case 1:I=i;break;case 4:I=i+(l-o)/2;break;case 2:I=i+(l-o);break}o-=p.margin[a];if(p.layout){v(h,c.size,o+\"px\")&&W(p,1);p.set[a]=true}else if(l>=o&&p.set[a]){v(h,c.size,o+\"px\")&&W(p,1);p.set[a]=false}p.size[a]=o;p.psize[a]= o}else{w=Math.max(0,l-p.margin[a]);I=a==0&&p.sc[a];if(!(h.style.display===\"none\"&&!h.ed)&&(I||l!=o||p.layout)){if(v(h,c.size,w+\"px\")){if(!f.isIE&&(f.hasTag(h,\"TEXTAREA\")||f.hasTag(h,\"INPUT\"))){v(h,\"margin-\"+c.left,p.margin[a]/2+\"px\");v(h,\"margin-\"+e.left,p.margin[!a]/2+\"px\")}W(p,1);p.set[a]=true}}else if(p.fs[a])a==0&&v(h,c.size,p.fs[a]+\"px\");else{v(h,c.size,\"\")&&W(p,1);p.set[a]=false}I=i;p.size[a]=w;p.psize[a]=l}if(V)if(g){v(h,c.left,\"4px\");l=f.css(h,\"position\");if(l!==\"absolute\")h.style.position= \"relative\"}else v(h,c.left,\"0px\");else v(h,c.left,I+\"px\");if(a==1){if(h.wtResize)h.wtResize(h,p.set[0]?Math.round(p.size[0]):-1,p.set[1]?Math.round(p.size[1]):-1,true);p.dirty=0}}if(j[d]>-1)i+=j[d]}$(b).children(\".\"+e.handleClass).css(c.size,k-c.margins[2]-c.margins[1]+\"px\")}}var f=E.WT;this.descendants=[];var ia=this,B=z,N=true,ca=true,ja=false,Y=null,S=null,la=false,oa=false,Z=[],pa=false,da=$(document.body).hasClass(\"Wt-rtl\"),s=[{initialized:false,config:B.cols,margins:y,maxSize:r,measures:[], sizes:[],stretched:[],fixedSize:[],Left:da?\"Right\":\"Left\",left:da?\"right\":\"left\",Right:da?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return B.items[b*s[0].config.length+a]},setItem:function(a,b,c){B.items[b*s[0].config.length+a]=c},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:T},{initialized:false,config:B.rows,margins:A,maxSize:D,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4, getItem:function(a,b){return B.items[a*s[0].config.length+b]},setItem:function(a,b,c){B.items[a*s[0].config.length+b]=c},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:U}];jQuery.data(document.getElementById(F),\"layout\",this);this.updateSizeInParent=function(a){if(Y)if(la){var b=s[a],c=b.measures[2];if(b.maxSize>0)c=Math.min(b.maxSize,c);if(pa){b=f.getElement(F);if(!b)return;for(var e=b,i=e.parentNode;;){if(i.wtGetPS)c=i.wtGetPS(i,e,a,c);c+=aa(i,a);if(i==S)break;if(a==1&&i==b.parentNode&& !i.lh&&i.offsetHeight>c)c=i.offsetHeight;e=i;i=e.parentNode}}else c+=Z[a];Y.setChildSize(S,a,c)}};this.setConfig=function(a){var b=B;B=a;s[0].config=B.cols;s[1].config=B.rows;s[0].stretched=[];s[1].stretched=[];var c;a=0;for(c=b.items.length;a<c;++a){var e=b.items[a];if(e){if(e.set){e.set[0]&&v(e.w,s[0].size,\"\");e.set[1]&&v(e.w,s[1].size,\"\")}if(e.layout){ia.setChildSize(e.w,0,e.ps[0]);ia.setChildSize(e.w,1,e.ps[1])}}}N=ca=true;E.layouts2.scheduleAdjust()};this.getId=function(){return F};this.setElDirty= function(a){var b,c;b=0;for(c=B.items.length;b<c;++b){var e=B.items[b];if(e&&e.id==a.id){e.dirty=2;N=true;E.layouts2.scheduleAdjust();return}}};this.setItemsDirty=function(a){var b,c,e=s[0].config.length;b=0;for(c=a.length;b<c;++b){var i=B.items[a[b][0]*e+a[b][1]];i.dirty=2;if(i.layout){i.layout=false;i.wasLayout=true;E.layouts2.setChildLayoutsDirty(ia,i.w)}}N=true};this.setDirty=function(){ca=true};this.setAllDirty=function(){var a,b;a=0;for(b=B.items.length;a<b;++a){var c=B.items[a];if(c)c.dirty= 2}N=true};this.setChildSize=function(a,b,c){var e=s[0].config.length,i=s[b],k,j;k=0;for(j=B.items.length;k<j;++k){var t=B.items[k];if(t&&t.id==a.id){a=b===0?k%e:k/e;if(t.align>>i.alignBits&15||!i.stretched[a]){if(!t.ps)t.ps=[];t.ps[b]=c}t.layout=true;W(t,1);break}}};this.measure=function(a){var b=f.getElement(F);if(b)if(!f.isHidden(b)){if(!oa){oa=true;ja=J==null;la=true;if(ja){var c=b;c=c.parentNode;for(Z=[0,0];c!=document;){Z[0]+=aa(c,0);Z[1]+=aa(c,1);if(c.wtGetPS)pa=true;var e=jQuery.data(c.parentNode, \"layout\");if(e){Y=e;S=c;break}c=c;c=c.parentNode;if(c.childNodes.length!=1&&!c.wtGetPS)la=false}c=b.parentNode;for(e=0;e<2;++e)s[e].sizeSet=f.pxself(c,s[e].size)!=0}else{Y=jQuery.data(document.getElementById(J),\"layout\");S=b;Z[0]=aa(S,0);Z[1]=aa(S,1)}}if(N||ca){c=ja?b.parentNode:null;qa(a,b,c)}if(a==1)N=ca=false}};this.setMaxSize=function(a,b){s[0].maxSize=a;s[1].maxSize=b};this.apply=function(a){var b=f.getElement(F);if(!b)return false;if(f.isHidden(b))return true;ta(a,b);return true};this.contains= function(a){var b=f.getElement(F);a=f.getElement(a.getId());return b&&a?f.contains(b,a):false};this.WT=f}");
	}

	static WJavaScriptPreamble appjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.ApplicationScope,
				JavaScriptObjectType.JavaScriptObject,
				"layouts2",
				"new (function(){var E=[],F=false,J=this,T=false;this.find=function(r){return jQuery.data(document.getElementById(r),\"layout\")};this.setDirty=function(r){if(r=this.find(r)){r.setDirty();J.scheduleAdjust()}};this.setElementDirty=function(r){var D=r;for(r=r.parentNode;r&&r!=document.body;){var y=jQuery.data(r,\"layout\");y&&y.setElDirty(D);D=r;r=r.parentNode}};this.setChildLayoutsDirty=function(r,D){var y,A;y=0;for(A=r.descendants.length;y<A;++y){var z= r.descendants[y];if(D){var C=r.WT.getElement(z.getId());if(C&&!r.WT.contains(D,C))continue}z.setDirty()}};this.add=function(r){function D(y,A){var z,C;z=0;for(C=y.length;z<C;++z){var x=y[z];if(x.getId()==A.getId()){y[z]=A;A.descendants=x.descendants;return}else if(x.contains(A)){D(x.descendants,A);return}else if(A.contains(x)){A.descendants.push(x);y.splice(z,1);--z;--C}}y.push(A)}D(E,r);J.scheduleAdjust()};var U=false;this.scheduleAdjust=function(r){if(r)T=true;if(!U){U=true;setTimeout(function(){J.adjust()}, 0)}};this.adjust=function(r,D){function y(z,C){var x,M;x=0;for(M=z.length;x<M;++x){var K=z[x];y(K.descendants,C);if(C==1&&T)K.setDirty();else C==0&&K.setAllDirty();K.measure(C)}}function A(z,C){var x,M;x=0;for(M=z.length;x<M;++x){var K=z[x];if(K.apply(C))A(K.descendants,C);else{z.splice(x,1);--x;--M}}}if(r){(r=this.find(r))&&r.setItemsDirty(D);J.scheduleAdjust()}else{U=false;if(!F){F=true;y(E,0);A(E,0);y(E,1);A(E,1);T=F=false}}};this.updateConfig=function(r,D){(r=this.find(r))&&r.setConfig(D)};this.adjustNow= function(){U&&J.adjust()};var V=null;window.onresize=function(){clearTimeout(V);V=setTimeout(function(){V=null;J.scheduleAdjust(true)},20)};window.onshow=function(){T=true;J.adjust()}})");
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
