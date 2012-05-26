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

	public StdGridLayoutImpl2(WLayout layout, Grid grid) {
		super(layout);
		this.grid_ = grid;
		this.needAdjust_ = false;
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
			app.addAutoJavaScript(app.getJavaScriptClass()
					+ ".layouts2.scheduleAdjust();");
			app.doJavaScript("$(window).load(function() { "
					+ app.getJavaScriptClass() + ".layouts2.adjustAll();"
					+ "});");
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
		WContainerWidget c = this.getContainer();
		if (c != null) {
			getImpl(item).containerAddWidgets(c);
			this.addedItems_.add(item);
			this.update(item);
		}
	}

	public void updateRemoveItem(WLayoutItem item) {
		WContainerWidget c = this.getContainer();
		if (c != null) {
			this.update(item);
			this.addedItems_.remove(item);
			this.removedItems_.add(getImpl(item).getId());
			getImpl(item).containerAddWidgets((WContainerWidget) null);
		}
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
		this.needAdjust_ = this.needConfigUpdate_ = false;
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
				".layouts2.add(new Wt3_2_1.StdLayout2(").append(
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
		js.append(fitWidth ? "1" : "0").append(",").append(
				fitHeight ? "1" : "0").append(",").append(
				progressive ? "1" : "0").append(",");
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
					ss.append("width:").append(MathUtils.round(pct, 2)).append(
							"%;");
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
				Grid.Item item = this.grid_.items_.get(row).get(col);
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
						c = getImpl(item.item_).createDomElement(true, true,
								app);
					}
					if (table != null) {
						if (c != null) {
							if (!app.getEnvironment().agentIsIE()) {
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
								itd.addChild(c);
								if (app.getEnvironment().agentIsIElt(9)) {
									if (c.getProperty(
											Property.PropertyStyleMinWidth)
											.length() != 0) {
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
							td.addChild(c);
							if (app.getEnvironment().agentIsIElt(9)) {
								if (c.getProperty(
										Property.PropertyStyleMinWidth)
										.length() != 0) {
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

	public void updateDom(DomElement parent) {
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
				parent.callJavaScript("Wt3_2_1.remove('"
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

	public void setHint(String name, String value) {
		logger.error(new StringWriter().append("unrecognized hint '").append(
				name).append("'").toString());
	}

	public boolean itemResized(WLayoutItem item) {
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		for (int row = 0; row < rowCount; ++row) {
			for (int col = 0; col < colCount; ++col) {
				if (this.grid_.items_.get(row).get(col).item_ == item) {
					this.grid_.items_.get(row).get(col).update_ = true;
					this.needAdjust_ = true;
					return true;
				}
			}
		}
		return false;
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

	private Grid grid_;
	private boolean needAdjust_;
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

	private static int pixelSize(WLength size) {
		if (size.getUnit() == WLength.Unit.Percentage) {
			return 0;
		} else {
			return (int) size.toPixels();
		}
	}

	private void streamConfig(StringBuilder js, WApplication app) {
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		js.append("{ rows: [");
		for (int i = 0; i < rowCount; ++i) {
			if (i != 0) {
				js.append(",");
			}
			js.append("[").append(this.grid_.rows_.get(i).stretch_).append(",")
					.append(this.grid_.rows_.get(i).resizable_ ? 1 : 0).append(
							",").append(this.minimumHeightForRow(i))
					.append("]");
			if (this.grid_.rows_.get(i).resizable_) {
				SizeHandle.loadJavaScript(app);
			}
		}
		js.append("], cols: [");
		for (int i = 0; i < colCount; ++i) {
			if (i != 0) {
				js.append(",");
			}
			js.append("[").append(this.grid_.columns_.get(i).stretch_).append(
					",").append(this.grid_.columns_.get(i).resizable_ ? 1 : 0)
					.append(",").append(this.minimumWidthForColumn(i)).append(
							"]");
			if (this.grid_.columns_.get(i).resizable_) {
				SizeHandle.loadJavaScript(app);
			}
		}
		js.append("], items: [");
		for (int row = 0; row < rowCount; ++row) {
			for (int col = 0; col < colCount; ++col) {
				Grid.Item item = this.grid_.items_.get(row).get(col);
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
							case AlignMiddle:
								align |= 0x20;
								break;
							case AlignBottom:
								align |= 0x40;
								break;
							default:
								break;
							}
						}
						js.append("align:").append((int) align).append(",");
					}
					js.append("id:'").append(id).append("'").append("}");
				} else {
					js.append("null");
				}
			}
		}
		js.append("]}");
	}

	private DomElement createElement(WLayoutItem item, WApplication app) {
		DomElement c = getImpl(item).createDomElement(true, true, app);
		c.setProperty(Property.PropertyStylePosition, "absolute");
		c.setProperty(Property.PropertyStyleVisibility, "hidden");
		return c;
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"StdLayout2",
				"function(E,y,K,L,M,v,F,C,z,w,x){function s(a,b){var e=q[b],i=b?a.scrollHeight:a.scrollWidth;b=b?a.clientHeight:a.clientWidth;if(b>1E6){b-=1E6;if(i>1E6)i-=1E6}if(f.isIE&&(f.hasTag(a,\"BUTTON\")||f.hasTag(a,\"TEXTAREA\")||f.hasTag(a,\"INPUT\")||f.hasTag(a,\"SELECT\")))i=b;if(!f.isOpera&&!f.isGecko)i+=f.px(a,\"border\"+e.Left+\"Width\")+f.px(a,\"border\"+e.Right+\"Width\");i+=f.px(a,\"margin\"+e.Left)+f.px(a,\"margin\"+e.Right);if(!f.boxSizing(a)&&!f.isIE)i+=f.px(a, \"padding\"+e.Left)+f.px(a,\"padding\"+e.Right);return i}function G(a,b){b=q[b];if(a.style.display==\"none\"||a.style.visibility==\"hidden\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var e=f.px(a,\"min\"+b.Size);f.boxSizing(a)||(e+=f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right));return e}}function D(a,b){b=q[b];var e=f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right);if(!f.boxSizing(a)&&!(f.isIE&&!f.isIElt9&&f.hasTag(a,\"BUTTON\")))e+=f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+ b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right);return e}function T(a,b){b=q[b];return f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}function R(a,b){if(f.boxSizing(a)){b=q[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}else return 0}function O(a,b){b=q[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right)+f.px(a,\"padding\"+b.Left)+ f.px(a,\"padding\"+b.Right)}function V(a){H=a.dirty=true;E.layouts2.scheduleAdjust()}function W(a,b,e){b=q[a];var i=b.config.length,g=q[a^1].config.length;if(H||I){var c=b.measures.slice();if(c.length==5){c[0]=c[0].slice();c[1]=c[1].slice()}var p=[],t=[],h=0,o=0,m,l;for(m=0;m<i;++m){var k=0,j=b.config[m][2],A=true;for(l=0;l<g;++l){var d=b.getItem(m,l);if(d){if(!d.w){var u=$(\"#\"+d.id);d.w=u.get(0);(function(){u.find(\"img\").load(function(){V(d)})})();d.w.style.left=d.w.style.top=\"-1000000px\"}if(!v&&d.w.style.position!= \"absolute\"){d.w.style.position=\"absolute\";d.w.style.visibility=\"hidden\";if(!d.w.wtResize){d.w.style.boxSizing=\"border-box\";var n=f.cssPrefix(\"BoxSizing\");if(n)d.w.style[n+\"BoxSizing\"]=\"border-box\"}}if(!d.ps)d.ps=[];if(!d.ms)d.ms=[];if(!d.size)d.size=[];if(!d.psize)d.psize=[];if(!d.fs)d.fs=[];if(!d.set)d.set=[false,false];if(d.w){if(f.isIE)d.w.style.visibility=\"\";if(d.dirty||I){n=G(d.w,a);if(n>j)j=n;d.ms[a]=n;if(!d.set[a])if(a==0){var r=f.pxself(d.w,b.size);d.fs[a]=r?r+D(d.w,a):0}else{r=f.px(d.w,b.size); d.fs[a]=r>Math.max(R(d.w,a),n)?r+D(d.w,a):0}n=d.fs[a];if(d.layout)n=Math.max(n,d.ps[a]);else{r=s(d.w,a);n=(typeof d.ps[a]===\"undefined\"||b.config[m][0]<=0)&&(!d.set[a]||r!=d.psize[a])?Math.max(n,r):Math.max(n,d.ps[a])}d.ps[a]=n;if(!d.span||d.span[a]==1)if(n>k)k=n;if(a==1)d.dirty=false}else if(!d.span||d.span[a]==1){if(d.ps[a]>k)k=d.ps[a];if(d.ms[a]>j)j=d.ms[a]}if(d.w.style.display!==\"none\")A=false}}}if(A)j=k=-1;else if(j>k)k=j;p[m]=k;t[m]=j;if(j>-1){h+=k;o+=j}}g=0;l=true;k=false;for(m=0;m<i;++m)if(t[m]> -1){if(l){g+=b.margins[1];l=false}else{g+=b.margins[0];if(k)g+=4}k=b.config[m][1]}l||(g+=b.margins[2]);h+=g;o+=g;b.measures=[p,t,h,o,g];P&&c[2]!=b.measures[2]&&P.setChildSize(N,a,b.measures[2]+J[a]);if(e&&c[3]!=b.measures[3]){i=b.measures[3]+\"px\";if(e.style[\"min\"+b.Size]!=i){e.style[\"min\"+b.Size]=i;Q.ancestor&&Q.ancestor.setContentsDirty(e)}}if(e)if(a==0&&e&&f.hasTag(e,\"TD\"))e.style[b.size]=b.measures[2]+\"px\"}}function X(a,b,e){a=q[a];a.fixedSize[b]=a.sizes[b]+e;E.layouts2.scheduleAdjust()}function Y(a, b,e){var i=b.di,g=q[a],c=q[a^1],p,t=f.getElement(y),h;for(h=i-1;h>=0;--h)if(g.sizes[h]>=0){p=-(g.sizes[h]-g.measures[1][h]);break}i=g.sizes[i]-g.measures[1][i];new f.SizeHandle(f,g.resizeDir,f.pxself(b,g.size),f.pxself(b,c.size),p,i,g.resizerClass,function(o){X(a,h,o)},b,t,e,0,0)}function Z(a,b){var e=q[a],i=q[a^1],g=e.measures,c=0,p=false,t=false,h=false,o=S?b.parentNode:null;if(e.maxSize===0)if(o){var m=f.css(o,\"position\");if(m===\"absolute\")c=f.pxself(o,e.size);if(c===0){if(!e.initialized)if(m!== \"absolute\"){c=a?o.clientHeight:o.clientWidth;p=true;if(a==0&&c==0&&f.isIE6){c=o.offsetWidth;p=false}if(f.isIE6&&c==0||c==g[3]+T(o,a))e.maxSize=999999}if(c===0&&e.maxSize===0){c=a?o.clientHeight:o.clientWidth;p=true}}}else{c=f.pxself(b,e.size);t=true}e.initialized=true;if(e.maxSize)if(g[2]<e.maxSize){o.style[e.size]=g[2]+R(o,a)+\"px\";c=g[2];h=t=true}else{c=e.maxSize;p=false}e.cSize=c;if(a==1&&(e.maxSize||i.maxSize)){h=i.cSize;var l=e.cSize;o&&o.wtResize&&o.wtResize(o,h,l)}t||(c-=p?T(o,a):R(o,a));if(!(o&& c<=0)){if(c<g[3])c=g[3];$(b).children(\".\"+i.handleClass).css(e.size,c-e.margins[2]-e.margins[1]+\"px\");p=[];t=e.config.length;o=i.config.length;if(c>g[3]){l=c-g[4];h=[];var k=[0,0],j=[0,0],A=0;for(c=0;c<t;++c)if(g[1][c]>-1)if(typeof e.fixedSize[c]!==\"undefined\"){h[c]=-1;p[c]=e.fixedSize[c];l-=p[c]}else{var d;if(e.config[c][0]>0){d=1;h[c]=e.config[c][0];A+=h[c]}else{d=0;h[c]=0}k[d]+=g[1][c];j[d]+=g[0][c];p[c]=g[0][c]}else h[c]=-2;if(A==0){for(c=0;c<t;++c)if(h[c]==0){h[c]=1;++A}j[1]=j[0];k[1]=k[0];j[0]= 0;k[0]=0}if(l>j[0]+k[1]){l-=j[0];if(l>j[1]){if(e.fitSize){l-=j[1];l=l/A;for(c=0;c<t;++c)if(h[c]>0)p[c]+=Math.round(h[c]*l)}}else{d=1;if(l<k[d])l=k[d];l=j[d]-k[d]>0?(l-k[d])/(j[d]-k[d]):0;for(c=0;c<t;++c)if(h[c]>0){k=g[0][c]-g[1][c];p[c]=g[1][c]+Math.round(k*l)}}}else{for(c=0;c<t;++c)if(h[c]>0)p[c]=g[1][c];l-=k[1];d=0;if(l<k[d])l=k[d];l=j[d]-k[d]>0?(l-k[d])/(j[d]-k[d]):0;for(c=0;c<t;++c)if(h[c]==0){k=g[0][c]-g[1][c];p[c]=g[1][c]+Math.round(k*l)}}}else p=g[1];e.sizes=p;g=0;l=true;j=false;for(c=0;c< t;++c)if(p[c]>-1){if(k=j){h=y+\"-rs\"+a+\"-\"+c;j=f.getElement(h);if(!j){j=document.createElement(\"div\");j.setAttribute(\"id\",h);j.di=c;j.style.position=\"absolute\";j.style[i.left]=i.margins[1]+\"px\";j.style[e.size]=e.margins[0]+\"px\";j.className=e.handleClass;b.insertBefore(j,b.firstChild);j.onmousedown=j.ontouchstart=function(aa){Y(a,this,aa||window.event)}}g+=2;j.style[e.left]=g+\"px\";g+=2}j=e.config[c][1];if(l){g+=e.margins[1];l=false}else g+=e.margins[0];for(A=0;A<o;++A)if((d=e.getItem(c,A))&&d.w){h= d.w;m=p[c];if(d.span){var u,n=j;for(u=1;u<d.span[a];++u){if(n)m+=4;n=e.config[c+n][1];m+=e.margins[0];m+=p[c+u]}}var r;h.style.visibility=\"\";n=d.align>>e.alignBits&15;u=d.ps[a];if(m<u)n=0;if(n){switch(n){case 1:r=g;break;case 4:r=g+(m-u)/2;break;case 2:r=g+(m-u);break}if(d.layout){h.style[e.size]=u+\"px\";d.set[a]=true}else if(m>=u&&d.set[a]){h.style[e.size]=\"\";d.set[a]=false}d.size[a]=u;d.psize[a]=u}else{r=D(h,a);n=m;if(f.isIElt9||!f.hasTag(h,\"BUTTON\")&&!f.hasTag(h,\"INPUT\")&&!f.hasTag(h,\"SELECT\"))n= Math.max(0,n-r);r=false;if(f.isIE&&f.hasTag(h,\"BUTTON\"))r=true;if(r||m!=u||d.layout){h.style[e.size]=n+\"px\";d.set[a]=true}else if(!d.fs[a]&&h.style[e.size]!=\"\"){h.style[e.size]=\"\";d.set[a]=false}r=g;d.size[a]=n;d.psize[a]=m}if(v)if(k){h.style[e.left]=\"4px\";m=f.css(h,\"position\");if(m!==\"absolute\")h.style.position=\"relative\"}else h.style[e.left]=\"0px\";else h.style[e.left]=r+\"px\";a==1&&h.wtResize&&h.wtResize(h,d.size[0],d.size[1])}g+=p[c]}}}var f=E.WT;this.ancestor=null;this.descendants=[];var Q=this, B=x,H=false,I=true,S=false,P=null,N=null,U=false,J=[],q=[{initialized:false,config:B.cols,margins:z,maxSize:F,measures:[],sizes:[],fixedSize:[],Left:\"Left\",left:\"left\",Right:\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return B.items[b*q[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:L},{initialized:false,config:B.rows,margins:w,maxSize:C,measures:[],sizes:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\", alignBits:4,getItem:function(a,b){return B.items[a*q[0].config.length+b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:M}];jQuery.data(document.getElementById(y),\"layout\",this);this.setConfig=function(a){var b=B;B=a;q[0].config=B.cols;q[1].config=B.rows;var e;a=0;for(e=b.items.length;a<e;++a){var i=b.items[a];if(i){if(i.set[0])i.w.style[q[0].size]=\"\";if(i.set[1])i.w.style[q[1].size]=\"\";if(i.layout){Q.setChildSize(i.w,0,i.ps[0]);Q.setChildSize(i.w,1,i.ps[1])}}}I=true};this.getId= function(){return y};this.setItemsDirty=function(a){var b,e,i=q[0].config.length;b=0;for(e=a.length;b<e;++b)B.items[a[b][0]*i+a[b][1]].dirty=true;H=true};this.setDirty=function(){I=true};this.setChildSize=function(a,b,e){var i,g;i=0;for(g=B.items.length;i<g;++i){var c=B.items[i];if(c&&c.id==a.id){if(!c.ps)c.ps=[];c.ps[b]=e;c.layout=true;break}}H=true};this.measure=function(a){var b=f.getElement(y);if(b){if(!U){U=true;if(S=K==null){var e=b.parentNode;for(J=[0,0];;){J[0]+=O(e,0);J[1]+=O(e,1);var i= jQuery.data(e.parentNode,\"layout\");if(i){P=i;N=e;break}e=e.parentNode;if(e.childNodes.length!=1)break}}else{P=jQuery.data(document.getElementById(K),\"layout\");N=b;J[0]=O(N,0);J[1]=O(N,1)}}if(H||I)W(a,b,S?b.parentNode:null);if(a==1)H=I=false}};this.apply=function(a){var b=f.getElement(y);if(!b)return false;Z(a,b);return true};this.contains=function(a){var b=f.getElement(y);a=f.getElement(a.getId());return b&&a?f.contains(b,a):false}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"StdLayout2.prototype.initResize",
				"function(){this.resizeInitialized=true}");
	}

	static WJavaScriptPreamble appjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.ApplicationScope,
				JavaScriptObjectType.JavaScriptObject,
				"layouts2",
				"new (function(){var E=[],y=false,K=this,L=false;this.find=function(v){return jQuery.data(document.getElementById(v),\"layout\")};this.add=function(v){function F(C,z){var w,x;w=0;for(x=C.length;w<x;++w){var s=C[w];if(s.contains(z)){F(s.descendants,z);return}else if(z.contains(s)){z.descendants.push(s);C.splice(w,1);--w;--x}}C.push(z)}F(E,v)};var M=false;this.scheduleAdjust=function(){if(!M){M=true;setTimeout(function(){K.adjust()},0)}};this.adjust= function(v,F){function C(w,x){var s,G;s=0;for(G=w.length;s<G;++s){var D=w[s];C(D.descendants,x);x==1&&L&&D.setDirty();D.measure(x)}}function z(w,x){var s,G;s=0;for(G=w.length;s<G;++s){var D=w[s];if(D.apply(x))z(D.descendants,x);else{w.splice(s,1);--s;--G}}}if(v)(v=this.find(v))&&v.setItemsDirty(F);else{M=false;if(!y){y=true;C(E,0);z(E,0);C(E,1);z(E,1);L=y=false}}};this.updateConfig=function(v,F){(v=this.find(v))&&v.setConfig(F)};window.onresize=function(){L=true;K.scheduleAdjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(b,e,c){function k(f){var g=a.px(f,\"marginTop\");g+=a.px(f,\"marginBottom\");if(!a.boxSizing(f)){g+=a.px(f,\"borderTopWidth\");g+=a.px(f,\"borderBottomWidth\");g+=a.px(f,\"paddingTop\");g+=a.px(f,\"paddingBottom\")}return g}var i,j,d,a=this;b.style.height=c+\"px\";if(a.boxSizing(b)){c-=a.px(b,\"marginTop\");c-=a.px(b,\"marginBottom\");c-=a.px(b,\"borderTopWidth\");c-=a.px(b,\"borderBottomWidth\");c-=a.px(b,\"paddingTop\");c-=a.px(b,\"paddingBottom\");e-= a.px(b,\"marginLeft\");e-=a.px(b,\"marginRight\");e-=a.px(b,\"borderLeftWidth\");e-=a.px(b,\"borderRightWidth\");e-=a.px(b,\"paddingLeft\");e-=a.px(b,\"paddingRight\")}i=0;for(j=b.childNodes.length;i<j;++i){d=b.childNodes[i];if(d.nodeType==1){var h=c-k(d);if(h>0)if(d.wtResize)d.wtResize(d,e,h);else{h=h+\"px\";if(d.style.height!=h)d.style.height=h}}}}");
	}
}
