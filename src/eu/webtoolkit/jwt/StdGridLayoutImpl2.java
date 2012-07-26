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
					+ app.getJavaScriptClass() + ".layouts2.scheduleAdjust();"
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
				".layouts2.add(new Wt3_2_2.StdLayout2(").append(
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
						if (item.item_ != null) {
							c = getImpl(item.item_).createDomElement(true,
									true, app);
						}
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
				parent.callJavaScript("Wt3_2_2.remove('"
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

	private void streamConfig(StringBuilder js, List<Grid.Section> sections,
			boolean rows, WApplication app) {
		js.append("[");
		for (int i = 0; i < sections.size(); ++i) {
			if (i != 0) {
				js.append(",");
			}
			js.append("[").append(sections.get(i).stretch_).append(",");
			if (sections.get(i).resizable_) {
				SizeHandle.loadJavaScript(app);
				js.append("[");
				WLength size = sections.get(i).initialSize_;
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

	private void streamConfig(StringBuilder js, WApplication app) {
		js.append("{ rows:");
		this.streamConfig(js, this.grid_.rows_, true, app);
		js.append(", cols:");
		this.streamConfig(js, this.grid_.columns_, false, app);
		js.append(", items: [");
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
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
				"function(G,A,M,N,O,t,H,D,B,v,z){function u(a,b){var c=s[b],h=b?a.scrollHeight:a.scrollWidth,i=b?a.clientHeight:a.clientWidth;b=b?a.offsetHeight:a.offsetWidth;if(i>=1E6){i-=1E6;if(h>=1E6)h-=1E6}if(h===0)h=e.pxself(a,c.size);if(e.isIE&&(e.hasTag(a,\"BUTTON\")||e.hasTag(a,\"TEXTAREA\")||e.hasTag(a,\"INPUT\")||e.hasTag(a,\"SELECT\")))h=i;if(!e.isOpera&&!e.isGecko)h+=e.px(a,\"border\"+c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\");h+=e.px(a,\"margin\"+c.Left)+ e.px(a,\"margin\"+c.Right);if(!e.boxSizing(a)&&!e.isIE)h+=e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right);if(h<b)h=b;return h}function I(a,b){b=s[b];if(a.style.display==\"none\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=e.px(a,\"min\"+b.Size);e.boxSizing(a)||(c+=e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right));return c}}function E(a,b){b=s[b];var c=e.px(a,\"margin\"+b.Left)+e.px(a,\"margin\"+b.Right);if(!e.boxSizing(a)&&!(e.isIE&&!e.isIElt9&&e.hasTag(a,\"BUTTON\")))c+= e.px(a,\"border\"+b.Left+\"Width\")+e.px(a,\"border\"+b.Right+\"Width\")+e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right);return c}function X(a,b){b=s[b];return e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right)}function S(a,b){if(e.boxSizing(a)){b=s[b];return e.px(a,\"border\"+b.Left+\"Width\")+e.px(a,\"border\"+b.Right+\"Width\")+e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right)}else return 0}function T(a,b){b=s[b];return e.px(a,\"border\"+b.Left+\"Width\")+e.px(a,\"border\"+b.Right+\"Width\")+e.px(a,\"margin\"+b.Left)+ e.px(a,\"margin\"+b.Right)+e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right)}function P(a,b){J=a.dirty=true;b&&G.layouts2.scheduleAdjust()}function y(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function Z(a,b,c){b=s[a];var h=s[a^1],i=b.measures,k=b.config.length,o=h.config.length;if(J||K){if(c&&typeof b.minSize==\"undefined\"){b.minSize=e.px(c,\"min\"+b.Size);if(b.minSize>0)b.minSize-=S(c,a)}i=i.slice();if(i.length==5){i[0]=i[0].slice();i[1]=i[1].slice()}var w=[],g=[],m=0,j=0, d,p;for(d=0;d<k;++d){var l=0,q=b.config[d][2],n=true;for(p=0;p<o;++p){var f=b.getItem(d,p);if(f){if(!f.w){var x=$(\"#\"+f.id);f.w=x.get(0);(function(){x.find(\"img\").load(function(){P(f)})})();f.w.style[b.left]=f.w.style[h.left]=\"-1000000px\"}if(!t&&f.w.style.position!=\"absolute\"){f.w.style.position=\"absolute\";f.w.style.visibility=\"hidden\";if(!f.w.wtResize){f.w.style.boxSizing=\"border-box\";var r=e.cssPrefix(\"BoxSizing\");if(r)f.w.style[r+\"BoxSizing\"]=\"border-box\"}}if(!f.ps)f.ps=[];if(!f.ms)f.ms=[];if(!f.size)f.size= [];if(!f.psize)f.psize=[];if(!f.fs)f.fs=[];if(!f.set)f.set=[false,false];if(f.w){if(e.isIE)f.w.style.visibility=\"\";if(f.dirty||K){r=I(f.w,a);if(r>q)q=r;f.ms[a]=r;if(!f.set[a])if(a==0){var F=e.pxself(f.w,b.size);f.fs[a]=F?F+E(f.w,a):0}else{F=e.px(f.w,b.size);f.fs[a]=F>Math.max(S(f.w,a),r)?F+E(f.w,a):0}r=f.fs[a];if(f.layout)r=Math.max(r,f.ps[a]);else{F=u(f.w,a);r=(typeof f.ps[a]===\"undefined\"||b.config[d][0]<=0)&&(!f.set[a]||F!=f.psize[a])?Math.max(r,F):Math.max(r,f.ps[a])}f.ps[a]=r;if(!f.span||f.span[a]== 1)if(r>l)l=r}else if(!f.span||f.span[a]==1){if(f.ps[a]>l)l=f.ps[a];if(f.ms[a]>q)q=f.ms[a]}if(f.w.style.display!==\"none\"||e.hasTag(f.w,\"TEXTAREA\")&&f.w.wtResize)n=false}}}if(n)q=l=-1;else if(q>l)l=q;w[d]=l;g[d]=q;if(q>-1){m+=l;j+=q}}h=0;o=true;p=false;for(d=0;d<k;++d)if(g[d]>-1){if(o){h+=b.margins[1];o=false}else{h+=b.margins[0];if(p)h+=4}p=b.config[d][1]!==0}o||(h+=b.margins[2]);m+=h;j+=h;b.measures=[w,g,m,j,h];U&&i[2]!=b.measures[2]&&U.setChildSize(Q,a,b.measures[2]+L[a]);c&&b.minSize==0&&i[3]!= b.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&&y(c,\"min\"+b.Size,b.measures[3]+\"px\")&&V.ancestor&&V.ancestor.setContentsDirty(c);c&&a==0&&c&&e.hasTag(c,\"TD\")&&y(c,b.size,b.measures[2]+\"px\")}}function aa(a,b,c){a=s[a];if(R)c=-c;a.fixedSize[b]=a.sizes[b]+c;G.layouts2.scheduleAdjust()}function ba(a,b,c){var h=b.di,i=s[a],k=s[a^1],o,w=e.getElement(A),g;for(g=h-1;g>=0;--g)if(i.sizes[g]>=0){o=-(i.sizes[g]-i.measures[1][g]);break}h=i.sizes[h]-i.measures[1][h];if(R){var m=o;o=-h;h=-m}new e.SizeHandle(e, i.resizeDir,e.pxself(b,i.size),e.pxself(b,k.size),o,h,i.resizerClass,function(j){aa(a,g,j)},b,w,c,0,0)}function ca(a,b){var c=s[a],h=s[a^1],i=c.measures,k=0,o=false,w=false,g=false,m=W?b.parentNode:null;if(c.maxSize===0)if(m){var j=e.css(m,\"position\");if(j===\"absolute\")k=e.pxself(m,c.size);if(k===0){if(!c.initialized)if(j!==\"absolute\"){k=a?m.clientHeight:m.clientWidth;o=true;if(a==0&&k==0&&e.isIElt9){k=m.offsetWidth;o=false}var d;if((e.hasTag(m,\"TD\")||e.hasTag(m,\"TH\"))&&!(e.isIE&&!e.isIElt9)){d=0; g=1}else{d=c.minSize?c.minSize:i[3];g=0}if(e.isIElt9&&k==g||k==d+X(m,a))c.maxSize=999999}if(k===0&&c.maxSize===0){k=a?m.clientHeight:m.clientWidth;o=true}}}else{k=e.pxself(b,c.size);w=true}d=0;if(m&&a==1&&e.hasTag(m,\"FIELDSET\")&&m.children.length==2)d=m.firstChild.offsetHeight;g=i[2];if(g<c.minSize)g=c.minSize;if(c.maxSize)if(g<c.maxSize){y(m,c.size,g+S(m,a)+\"px\");k=g;g=w=true}else{k=c.maxSize;o=false}c.cSize=k;if(a==1&&m&&m.wtResize){g=h.cSize;m.wtResize(m,g,c.cSize)}k-=d;if(!w){w=0;if(c.initialized)w= c.cPadding;else{w=o?X(m,a):S(m,a);c.cPadding=w}k-=w}c.initialized=true;if(!(m&&k<=0)){if(k<i[3]-d)k=i[3]-d;$(b).children(\".\"+h.handleClass).css(c.size,k-c.margins[2]-c.margins[1]+\"px\");o=[];m=c.config.length;w=h.config.length;if(k>=i[3]-d){var p=k-i[4];g=[];var l=[0,0],q=[0,0],n=0;for(d=0;d<m;++d)if(i[1][d]>-1){j=-1;if(typeof c.fixedSize[d]!==\"undefined\")j=c.fixedSize[d];else if(c.config[d][1]!==0&&c.config[d][1][0]>=0){j=c.config[d][1][0];if(c.config[d][1][1])j=(k-i[4])*j/100}if(j>=0){g[d]=-1;o[d]= j;p-=o[d]}else{if(c.config[d][0]>0){j=1;g[d]=c.config[d][0];n+=g[d]}else{j=0;g[d]=0}l[j]+=i[1][d];q[j]+=i[0][d];o[d]=i[0][d]}}else g[d]=-2;if(n==0){for(d=0;d<m;++d)if(g[d]==0){g[d]=1;++n}q[1]=q[0];l[1]=l[0];q[0]=0;l[0]=0}if(p>q[0]+l[1]){p-=q[0];if(p>q[1]){if(c.fitSize){p-=q[1];k=p/n;for(d=0;d<m;++d)if(g[d]>0)o[d]+=Math.round(g[d]*k)}}else{j=1;if(p<l[j])p=l[j];k=q[j]-l[j]>0?(p-l[j])/(q[j]-l[j]):0;for(d=0;d<m;++d)if(g[d]>0){p=i[0][d]-i[1][d];o[d]=i[1][d]+Math.round(p*k)}}}else{for(d=0;d<m;++d)if(g[d]> 0)o[d]=i[1][d];p-=l[1];j=0;if(p<l[j])p=l[j];k=q[j]-l[j]>0?(p-l[j])/(q[j]-l[j]):0;for(d=0;d<m;++d)if(g[d]==0){p=i[0][d]-i[1][d];o[d]=i[1][d]+Math.round(p*k)}}}else o=i[1];c.sizes=o;i=0;k=true;l=false;for(d=0;d<m;++d)if(o[d]>-1){if(p=l){g=A+\"-rs\"+a+\"-\"+d;l=e.getElement(g);if(!l){l=document.createElement(\"div\");l.setAttribute(\"id\",g);l.di=d;l.style.position=\"absolute\";l.style[h.left]=h.margins[1]+\"px\";l.style[c.size]=c.margins[0]+\"px\";l.className=c.handleClass;b.insertBefore(l,b.firstChild);l.onmousedown= l.ontouchstart=function(F){ba(a,this,F||window.event)}}i+=2;y(l,c.left,i+\"px\");i+=2}l=c.config[d][1]!==0;if(k){i+=c.margins[1];k=false}else i+=c.margins[0];for(q=0;q<w;++q)if((n=c.getItem(d,q))&&n.w){g=n.w;j=o[d];if(n.span){var f,x=l;for(f=1;f<n.span[a];++f){if(x)j+=4;x=c.config[d+x][1]!==0;j+=c.margins[0];j+=o[d+f]}}var r;g.style.visibility=\"\";x=n.align>>c.alignBits&15;f=n.ps[a];if(j<f)x=0;if(x){switch(x){case 1:r=i;break;case 4:r=i+(j-f)/2;break;case 2:r=i+(j-f);break}if(n.layout){y(g,c.size,f+ \"px\")&&P(n);n.set[a]=true}else if(j>=f&&n.set[a]){y(g,c.size,f+\"px\")&&P(n);n.set[a]=false}n.size[a]=f;n.psize[a]=f}else{r=E(n.w,a);x=j;if(e.isIElt9||!e.hasTag(g,\"BUTTON\")&&!e.hasTag(g,\"INPUT\")&&!e.hasTag(g,\"SELECT\")&&!e.hasTag(g,\"TEXTAREA\"))x=Math.max(0,x-r);r=false;if(e.isIE&&e.hasTag(g,\"BUTTON\"))r=true;if(r||j!=f||n.layout){y(g,c.size,x+\"px\")&&P(n);n.set[a]=true}else if(n.fs[a])a==0&&y(g,c.size,n.fs[a]+\"px\");else{y(g,c.size,\"\")&&P(n);n.set[a]=false}r=i;n.size[a]=x;n.psize[a]=j}if(t)if(p){y(g,c.left, \"4px\");j=e.css(g,\"position\");if(j!==\"absolute\")g.style.position=\"relative\"}else y(g,c.left,\"0px\");else y(g,c.left,r+\"px\");if(a==1){g.wtResize&&g.wtResize(g,n.size[0],n.size[1]);n.dirty=false}}i+=o[d]}}}var e=G.WT;this.ancestor=null;this.descendants=[];var V=this,C=z,J=false,K=true,W=false,U=null,Q=null,Y=false,L=[],R=$(document.body).hasClass(\"Wt-rtl\"),s=[{initialized:false,config:C.cols,margins:B,maxSize:H,measures:[],sizes:[],fixedSize:[],Left:R?\"Right\":\"Left\",left:R?\"right\":\"left\",Right:R?\"Left\": \"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return C.items[b*s[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:N},{initialized:false,config:C.rows,margins:v,maxSize:D,measures:[],sizes:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,b){return C.items[a*s[0].config.length+b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:O}];jQuery.data(document.getElementById(A), \"layout\",this);this.setConfig=function(a){var b=C;C=a;s[0].config=C.cols;s[1].config=C.rows;var c;a=0;for(c=b.items.length;a<c;++a){var h=b.items[a];if(h){h.set[0]&&y(h.w,s[0].size,\"\");h.set[1]&&y(h.w,s[1].size,\"\");if(h.layout){V.setChildSize(h.w,0,h.ps[0]);V.setChildSize(h.w,1,h.ps[1])}}}K=true};this.getId=function(){return A};this.setItemsDirty=function(a){var b,c,h=s[0].config.length;b=0;for(c=a.length;b<c;++b)C.items[a[b][0]*h+a[b][1]].dirty=true;J=true};this.setDirty=function(){K=true};this.setChildSize= function(a,b,c){var h,i;h=0;for(i=C.items.length;h<i;++h){var k=C.items[h];if(k&&k.id==a.id){if(!k.ps)k.ps=[];k.ps[b]=c;k.layout=true;break}}J=true};this.measure=function(a){var b=e.getElement(A);if(b)if(!e.isHidden(b)){if(!Y){Y=true;if(W=M==null){var c=b.parentNode;for(L=[0,0];;){L[0]+=T(c,0);L[1]+=T(c,1);var h=jQuery.data(c.parentNode,\"layout\");if(h){U=h;Q=c;break}c=c.parentNode;if(c.childNodes.length!=1)break}}else{U=jQuery.data(document.getElementById(M),\"layout\");Q=b;L[0]=T(Q,0);L[1]=T(Q,1)}}if(J|| K)Z(a,b,W?b.parentNode:null);if(a==1)J=K=false}};this.apply=function(a){var b=e.getElement(A);if(!b)return false;if(e.isHidden(b))return true;ca(a,b);return true};this.contains=function(a){var b=e.getElement(A);a=e.getElement(a.getId());return b&&a?e.contains(b,a):false}}");
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
				"new (function(){var G=[],A=false,M=this,N=false;this.find=function(t){return jQuery.data(document.getElementById(t),\"layout\")};this.setDirty=function(t){(t=jQuery.data(t,\"layout\"))&&t.setDirty()};this.add=function(t){function H(D,B){var v,z;v=0;for(z=D.length;v<z;++v){var u=D[v];if(u.contains(B)){H(u.descendants,B);return}else if(B.contains(u)){B.descendants.push(u);D.splice(v,1);--v;--z}}D.push(B)}H(G,t)};var O=false;this.scheduleAdjust=function(){if(!O){O= true;setTimeout(function(){M.adjust()},0)}};this.adjust=function(t,H){function D(v,z){var u,I;u=0;for(I=v.length;u<I;++u){var E=v[u];D(E.descendants,z);z==1&&N&&E.setDirty();E.measure(z)}}function B(v,z){var u,I;u=0;for(I=v.length;u<I;++u){var E=v[u];if(E.apply(z))B(E.descendants,z);else{v.splice(u,1);--u;--I}}}if(t)(t=this.find(t))&&t.setItemsDirty(H);else{O=false;if(!A){A=true;D(G,0);B(G,0);D(G,1);B(G,1);N=A=false}}};this.updateConfig=function(t,H){(t=this.find(t))&&t.setConfig(H)};window.onresize= function(){N=true;M.scheduleAdjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(b,e,c){function k(f){var g=a.px(f,\"marginTop\");g+=a.px(f,\"marginBottom\");if(!a.boxSizing(f)){g+=a.px(f,\"borderTopWidth\");g+=a.px(f,\"borderBottomWidth\");g+=a.px(f,\"paddingTop\");g+=a.px(f,\"paddingBottom\")}return g}var i,j,d,a=this;b.style.height=c+\"px\";if(a.boxSizing(b)){c-=a.px(b,\"marginTop\");c-=a.px(b,\"marginBottom\");c-=a.px(b,\"borderTopWidth\");c-=a.px(b,\"borderBottomWidth\");c-=a.px(b,\"paddingTop\");c-=a.px(b,\"paddingBottom\");e-= a.px(b,\"marginLeft\");e-=a.px(b,\"marginRight\");e-=a.px(b,\"borderLeftWidth\");e-=a.px(b,\"borderRightWidth\");e-=a.px(b,\"paddingLeft\");e-=a.px(b,\"paddingRight\")}i=0;for(j=b.childNodes.length;i<j;++i){d=b.childNodes[i];if(d.nodeType==1){var h=c-k(d);if(h>0)if(d.wtResize)d.wtResize(d,e,h);else{h=h+\"px\";if(d.style.height!=h)d.style.height=h}}}}");
	}
}
