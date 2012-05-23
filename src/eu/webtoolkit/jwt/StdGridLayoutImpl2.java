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
			maxWidth = (int) this.getContainer().getMaximumWidth().toPixels();
			maxHeight = (int) this.getContainer().getMaximumHeight().toPixels();
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
						int align = hAlign.getValue();
						if (align == AlignmentFlag.AlignJustify.getValue()) {
							align = 0;
						}
						if (vAlign != null) {
							switch (vAlign) {
							case AlignTop:
								align |= AlignmentFlag.AlignLeft.getValue() << 8;
								break;
							case AlignMiddle:
								align |= AlignmentFlag.AlignCenter.getValue() << 8;
								break;
							case AlignBottom:
								align |= AlignmentFlag.AlignRight.getValue() << 8;
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
		if (!app.getEnvironment().agentIsIE()) {
			c.setProperty(Property.PropertyStyleBoxSizing, "border-box");
		}
		return c;
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"StdLayout2",
				"function(D,y,J,K,L,u,F,B,z,v,x){function t(a,b){var c=r[b],j=b?a.scrollHeight:a.scrollWidth;b=b?a.clientHeight:a.clientWidth;if(b>1E6){b-=1E6;if(j>1E6)j-=1E6}if(d.isIE&&(d.hasTag(a,\"BUTTON\")||d.hasTag(a,\"TEXTAREA\")||d.hasTag(a,\"INPUT\")||d.hasTag(a,\"SELECT\")))j=b;if(!d.isOpera&&!d.isGecko)j+=d.px(a,\"border\"+c.Left+\"Width\")+d.px(a,\"border\"+c.Right+\"Width\");j+=d.px(a,\"margin\"+c.Left)+d.px(a,\"margin\"+c.Right);if(!d.boxSizing(a)&&!d.isIE)j+=d.px(a, \"padding\"+c.Left)+d.px(a,\"padding\"+c.Right);return j}function G(a,b){b=r[b];if(a.style.display==\"none\"||a.style.visibility==\"hidden\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=d.px(a,\"min\"+b.Size);d.boxSizing(a)||(c+=d.px(a,\"padding\"+b.Left)+d.px(a,\"padding\"+b.Right));return c}}function C(a,b){b=r[b];var c=d.px(a,\"margin\"+b.Left)+d.px(a,\"margin\"+b.Right);if(!d.boxSizing(a)&&!(d.isIE&&!d.isIElt9&&d.hasTag(a,\"BUTTON\")))c+=d.px(a,\"border\"+b.Left+\"Width\")+d.px(a,\"border\"+ b.Right+\"Width\")+d.px(a,\"padding\"+b.Left)+d.px(a,\"padding\"+b.Right);return c}function Q(a,b){b=r[b];return d.px(a,\"padding\"+b.Left)+d.px(a,\"padding\"+b.Right)}function R(a,b){if(d.boxSizing(a)){b=r[b];return d.px(a,\"border\"+b.Left+\"Width\")+d.px(a,\"border\"+b.Right+\"Width\")+d.px(a,\"padding\"+b.Left)+d.px(a,\"padding\"+b.Right)}else return 0}function T(a,b){b=r[b];return d.px(a,\"border\"+b.Left+\"Width\")+d.px(a,\"border\"+b.Right+\"Width\")+d.px(a,\"margin\"+b.Left)+d.px(a,\"margin\"+b.Right)+d.px(a,\"padding\"+b.Left)+ d.px(a,\"padding\"+b.Right)}function U(a){H=a.dirty=true;D.layouts2.scheduleAdjust()}function V(a,b,c){b=r[a];var j=b.config.length,h=r[a^1].config.length;if(H||I){var g=b.measures.slice();if(g.length==5){g[0]=g[0].slice();g[1]=g[1].slice()}var q=[],w=[],i=0,n=0,l,k;for(l=0;l<j;++l){var f=0,o=b.config[l][2],E=true;for(k=0;k<h;++k){var e=b.getItem(l,k);if(e){if(!e.w){var p=$(\"#\"+e.id);e.w=p.get(0);(function(){p.find(\"img\").load(function(){U(e)})})();e.w.style.left=e.w.style.top=\"-1000000px\"}if(!u&&e.w.style.position!= \"absolute\"){e.w.style.position=\"absolute\";e.w.style.visibility=\"hidden\";e.w.style.boxSizing=\"border-box\";var m=d.cssPrefix(\"BoxSizing\");if(m)e.w.style[m+\"BoxSizing\"]=\"border-box\"}if(!e.ps)e.ps=[];if(!e.ms)e.ms=[];if(!e.size)e.size=[];if(!e.fs)e.fs=[];if(!e.set)e.set=[false,false];if(e.w){if(d.isIE)e.w.style.visibility=\"\";if(e.dirty||I){m=G(e.w,a);if(m>o)o=m;e.ms[a]=m;if(!e.set[a])if(a==0){var s=d.pxself(e.w,b.size);e.fs[a]=s?s+C(e.w,a):0}else{s=d.px(e.w,b.size);e.fs[a]=s>Math.max(R(e.w,a),m)?s+C(e.w, a):0}m=e.fs[a];if(e.layout)m=Math.max(m,e.ps[a]);else{s=t(e.w,a);m=!e.set[a]||s!=e.size[a]?Math.max(m,s):Math.max(m,e.ps[a])}e.ps[a]=m;if(!e.span||e.span[a]==1)if(m>f)f=m;if(a==1)e.dirty=false}else if(!e.span||e.span[a]==1){if(e.ps[a]>f)f=e.ps[a];if(e.ms[a]>o)o=e.ms[a]}if(e.w.style.display!==\"none\")E=false}}}if(E)o=f=-1;else if(o>f)f=o;q[l]=f;w[l]=o;if(o>-1){i+=f;n+=o}}h=0;k=true;f=false;for(l=0;l<j;++l)if(w[l]>-1){if(k){h+=b.margins[1];k=false}else{h+=b.margins[0];if(f)h+=4}f=b.config[l][1]}k||(h+= b.margins[2]);i+=h;n+=h;b.measures=[q,w,i,n];if(M)if(g[2]!=b.measures[2]){j=T(N,a);M.setChildSize(N,a,b.measures[2]+j)}if(c&&g[3]!=b.measures[3]){g=b.measures[3]+\"px\";if(c.style[\"min\"+b.Size]!=g){c.style[\"min\"+b.Size]=g;O.ancestor&&O.ancestor.setContentsDirty(c)}}if(c)if(a==0&&c&&d.hasTag(c,\"TD\"))c.style[b.size]=b.measures[2]+\"px\"}}function W(a,b,c){a=r[a];a.fixedSize[b]=a.sizes[b]+c;D.layouts2.scheduleAdjust()}function X(a,b,c){var j=b.di,h=r[a],g=r[a^1],q,w=d.getElement(y),i;for(i=j-1;i>=0;--i)if(h.sizes[i]>= 0){q=-(h.sizes[i]-h.measures[1][i]);break}j=h.sizes[j]-h.measures[1][j];new d.SizeHandle(d,h.resizeDir,d.pxself(b,h.size),d.pxself(b,g.size),q,j,h.resizerClass,function(n){W(a,i,n)},b,w,c,0,0)}function Y(a,b){var c=r[a],j=r[a^1],h=c.measures,g=0,q=false,w=false,i=false,n=P?b.parentNode:null;if(c.maxSize===0)if(n){var l=d.css(n,\"position\");if(l===\"absolute\")g=d.pxself(n,c.size);if(g===0){if(!c.initialized)if(l!==\"absolute\"){g=a?n.clientHeight:n.clientWidth;q=true;if(a==0&&g==0&&d.isIE6){g=n.offsetWidth; q=false}if(d.isIE6&&g==0||g==h[3]+Q(n,a)){debugger;c.maxSize=999999}}if(g===0&&c.maxSize===0){g=a?n.clientHeight:n.clientWidth;q=true}}}else{g=d.pxself(b,c.size);w=true}c.initialized=true;if(c.maxSize)if(h[2]<c.maxSize){n.style[c.size]=h[2]+\"px\";g=h[2];i=w=true}else{g=c.maxSize;q=false}c.cSize=g;if(a==1&&(c.maxSize||j.maxSize)){var k=j.cSize,f=c.cSize;n.wtResize&&n.wtResize(n,k,f)}w||(g-=q?Q(n,a):R(n,a));if(!(n&&g<=0)){if(g<h[3])g=h[3];$(b).children(\".\"+j.handleClass).css(c.size,g-c.margins[2]-c.margins[1]+ \"px\");q=h[1].slice();w=c.config.length;n=j.config.length;if(c.fixedSize.length==0&&g==h[2])q=h[0];else if(g>h[3]){var o=0;var E=k=0,e=0;for(f=0;f<w;++f)if(h[1][f]>-1)if(typeof c.fixedSize[f]!==\"undefined\"){E+=c.fixedSize[f];q[f]=c.fixedSize[f]}else{++e;if(c.config[f][0]<=0)o+=h[0][f]-h[1][f];else k+=c.config[f][0]}g=g-h[3]-E;if(c.fitSize&&!i&&k==0)o=0;if(o){i=g>o?o:g;o=i/o;for(f=0;f<w;++f)if(h[1][f]>-1)if(typeof c.fixedSize[f]===\"undefined\"&&c.config[f][0]<=0)q[f]+=o*(h[0][f]-h[1][f]);g-=i}if(c.fitSize&& g>0){i=k;if(k==0)i=e;i=g/i;for(f=0;f<w;++f)if(h[1][f]>-1)if(typeof c.fixedSize[f]===\"undefined\"){g=k==0?1:c.config[f][0];if(g>0)q[f]+=g*i}}}c.sizes=q;h=0;g=true;o=false;for(f=0;f<w;++f)if(q[f]>-1){if(e=o){k=y+\"-rs\"+a+\"-\"+f;i=d.getElement(k);if(!i){i=document.createElement(\"div\");i.setAttribute(\"id\",k);i.di=f;i.style.position=\"absolute\";i.style[j.left]=j.margins[1]+\"px\";i.style[c.size]=c.margins[0]+\"px\";i.className=c.handleClass;b.insertBefore(i,b.firstChild);i.onmousedown=i.ontouchstart=function(Z){X(a, this,Z||window.event)}}h+=2;i.style[c.left]=h+\"px\";h+=2}o=c.config[f][1];if(g){h+=c.margins[1];g=false}else h+=c.margins[0];for(E=0;E<n;++E){var p=c.getItem(f,E);if(p&&p.w){k=p.w;i=q[f];if(p.span){var m=o;for(l=1;l<p.span[a];++l){if(m)i+=4;m=c.config[f+m][1];i+=c.margins[0];i+=q[f+l]}}var s;k.style.visibility=\"\";m=p.align>>c.alignBits&15;l=p.ps[a];if(i<l)m=0;if(m){switch(m){case 1:s=h;break;case 4:s=h+(i-l)/2;break;case 2:s=h+(i-l);break}if(p.layout){k.style[c.size]=l+\"px\";p.set[a]=true}else if(i>= l&&p.set[a]){k.style[c.size]=\"\";p.set[a]=false}p.size[a]=l}else{s=C(k,a);m=i;if(d.isIElt9||!d.hasTag(k,\"BUTTON\")&&!d.hasTag(k,\"INPUT\")&&!d.hasTag(k,\"SELECT\"))m=Math.max(0,m-s);s=false;if(d.isIE&&d.hasTag(k,\"BUTTON\"))s=true;if(s||i!=l||p.layout){k.style[c.size]=m+\"px\";p.set[a]=true}else if(!p.fs[a]&&k.style[c.size]!=\"\"){k.style[c.size]=\"\";p.set[a]=false}s=h;p.size[a]=m}if(u)if(e){k.style[c.left]=\"4px\";l=d.css(k,\"position\");if(l!==\"absolute\")k.style.position=\"relative\"}else k.style[c.left]=\"0px\";else k.style[c.left]= s+\"px\";a==1&&k.wtResize&&k.wtResize(k,p.size[0],p.size[1])}}h+=q[f]}}}var d=D.WT;this.ancestor=null;this.descendants=[];var O=this,A=x,H=false,I=true,P=false,M=null,N=null,S=false,r=[{initialized:false,config:A.cols,margins:z,maxSize:F,measures:[],sizes:[],fixedSize:[],Left:\"Left\",left:\"left\",Right:\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return A.items[b*r[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:K},{initialized:false,config:A.rows, margins:v,maxSize:B,measures:[],sizes:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:8,getItem:function(a,b){return A.items[a*r[0].config.length+b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:L}];jQuery.data(document.getElementById(y),\"layout\",this);this.setConfig=function(a){var b=A;A=a;r[0].config=A.cols;r[1].config=A.rows;var c;a=0;for(c=b.items.length;a<c;++a){var j=b.items[a];if(j){if(j.set[0])j.w.style[r[0].size]=\"\";if(j.set[1])j.w.style[r[1].size]= \"\";if(j.layout){O.setChildSize(j.w,0,j.ps[0]);O.setChildSize(j.w,1,j.ps[1])}}}I=true};this.getId=function(){return y};this.setItemsDirty=function(a){var b,c,j=r[0].config.length;b=0;for(c=a.length;b<c;++b)A.items[a[b][0]*j+a[b][1]].dirty=true;H=true};this.setDirty=function(){I=true};this.setChildSize=function(a,b,c){var j,h;j=0;for(h=A.items.length;j<h;++j){var g=A.items[j];if(g&&g.id==a.id){if(!g.ps)g.ps=[];g.ps[b]=c;g.layout=true;break}}H=true};this.measure=function(a){var b=d.getElement(y);if(b){if(!S){S= true;if(P=J==null){if(M=jQuery.data(b.parentNode.parentNode,\"layout\"))N=b.parentNode}else{M=jQuery.data(document.getElementById(J),\"layout\");N=b}}if(H||I)V(a,b,P?b.parentNode:null);if(a==1)H=I=false}};this.apply=function(a){var b=d.getElement(y);if(!b)return false;Y(a,b);return true};this.contains=function(a){var b=d.getElement(y);a=d.getElement(a.getId());return b&&a?d.contains(b,a):false}}");
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
				"new (function(){var D=[],y=false,J=this,K=false;this.find=function(u){return jQuery.data(document.getElementById(u),\"layout\")};this.add=function(u){function F(B,z){var v,x;v=0;for(x=B.length;v<x;++v){var t=B[v];if(t.contains(z)){F(t.descendants,z);return}else if(z.contains(t)){z.descendants.push(t);B.splice(v,1);--v;--x}}B.push(z)}F(D,u)};var L=false;this.scheduleAdjust=function(){if(!L){L=true;setTimeout(function(){J.adjust()},0)}};this.adjust= function(u,F){function B(v,x){var t,G;t=0;for(G=v.length;t<G;++t){var C=v[t];B(C.descendants,x);x==1&&K&&C.setDirty();C.measure(x)}}function z(v,x){var t,G;t=0;for(G=v.length;t<G;++t){var C=v[t];if(C.apply(x))z(C.descendants,x);else{v.splice(t,1);--t;--G}}}if(u)(u=this.find(u))&&u.setItemsDirty(F);else{L=false;if(!y){y=true;B(D,0);z(D,0);B(D,1);z(D,1);K=y=false}}};this.updateConfig=function(u,F){(u=this.find(u))&&u.setConfig(F)};window.onresize=function(){K=true;J.scheduleAdjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(b,e,c){function k(f){var g=a.px(f,\"marginTop\");g+=a.px(f,\"marginBottom\");if(!a.boxSizing(f)){g+=a.px(f,\"borderTopWidth\");g+=a.px(f,\"borderBottomWidth\");g+=a.px(f,\"paddingTop\");g+=a.px(f,\"paddingBottom\")}return g}var i,j,d,a=this;b.style.height=c+\"px\";if(a.boxSizing(b)){c-=a.px(b,\"marginTop\");c-=a.px(b,\"marginBottom\");c-=a.px(b,\"borderTopWidth\");c-=a.px(b,\"borderBottomWidth\");c-=a.px(b,\"paddingTop\");c-=a.px(b,\"paddingBottom\");e-= a.px(b,\"marginLeft\");e-=a.px(b,\"marginRight\");e-=a.px(b,\"borderLeftWidth\");e-=a.px(b,\"borderRightWidth\");e-=a.px(b,\"paddingLeft\");e-=a.px(b,\"paddingRight\")}i=0;for(j=b.childNodes.length;i<j;++i){d=b.childNodes[i];if(d.nodeType==1){var h=c-k(d);if(h>0)if(d.wtResize)d.wtResize(d,e,h);else{h=h+\"px\";if(d.style.height!=h)d.style.height=h}}}}");
	}
}
