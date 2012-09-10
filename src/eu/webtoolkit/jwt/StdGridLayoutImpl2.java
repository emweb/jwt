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
				"function(G,B,J,O,P,t,H,E,C,x,A){function u(a,c){var b=s[c],f=c?a.scrollHeight:a.scrollWidth,i=c?a.clientHeight:a.clientWidth;c=c?a.offsetHeight:a.offsetWidth;if(i>=1E6)i-=1E6;if(f>=1E6)f-=1E6;if(c>=1E6)c-=1E6;if(f===0)f=e.pxself(a,b.size);if(e.isIE&&(e.hasTag(a,\"BUTTON\")||e.hasTag(a,\"TEXTAREA\")||e.hasTag(a,\"INPUT\")||e.hasTag(a,\"SELECT\")))f=i;if(f>c)if(e.pxself(a,b.size)==0)f=0;if(!e.isOpera&&!e.isGecko)f+=e.px(a,\"border\"+b.Left+\"Width\")+e.px(a, \"border\"+b.Right+\"Width\");f+=e.px(a,\"margin\"+b.Left)+e.px(a,\"margin\"+b.Right);if(!e.boxSizing(a)&&!e.isIE)f+=e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right);if(f<c)f=c;return Math.round(f)}function I(a,c){c=s[c];if(a.style.display==\"none\")return 0;else if(a[\"layoutMin\"+c.Size])return a[\"layoutMin\"+c.Size];else{var b=e.px(a,\"min\"+c.Size);e.boxSizing(a)||(b+=e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right));return b}}function F(a,c){c=s[c];var b=e.px(a,\"margin\"+c.Left)+e.px(a,\"margin\"+c.Right); if(!e.boxSizing(a)&&!(e.isIE&&!e.isIElt9&&e.hasTag(a,\"BUTTON\")))b+=e.px(a,\"border\"+c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\")+e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right);return b}function X(a,c){c=s[c];return e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right)}function T(a,c){if(e.boxSizing(a)){c=s[c];return e.px(a,\"border\"+c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\")+e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right)}else return 0}function Q(a,c){c=s[c];return Math.round(e.px(a,\"border\"+ c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\")+e.px(a,\"margin\"+c.Left)+e.px(a,\"margin\"+c.Right)+e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right))}function R(a,c){K=a.dirty=true;c&&G.layouts2.scheduleAdjust()}function y(a,c,b){if(a.style[c]!==b){a.style[c]=b;return true}else return false}function aa(a,c,b){var f=s[a],i=s[a^1],j=f.measures,p=f.config.length,v=i.config.length;if(K||L){if(b&&typeof f.minSize==\"undefined\"){f.minSize=e.px(b,\"min\"+f.Size);if(f.minSize>0)f.minSize-=T(b,a)}j=j.slice(); if(j.length==5){j[0]=j[0].slice();j[1]=j[1].slice()}var h=[],n=[],k=0,d=0,o,m;for(o=0;o<p;++o){var r=0,l=f.config[o][2],w=true;for(m=0;m<v;++m){var g=f.getItem(o,m);if(g){if(!g.w||a==0&&g.dirty){var q=$(\"#\"+g.id),z=q.get(0);if(z!=g.w){g.w=z;q.find(\"img\").add(q.filter(\"img\")).bind(\"load\",{item:g},function(ba){R(ba.data.item,true)});g.w.style[f.left]=g.w.style[i.left]=\"-1000000px\"}}if(!t&&g.w.style.position!=\"absolute\"){g.w.style.position=\"absolute\";g.w.style.visibility=\"hidden\";if(!g.w.wtResize){g.w.style.boxSizing= \"border-box\";if(q=e.cssPrefix(\"BoxSizing\"))g.w.style[q+\"BoxSizing\"]=\"border-box\"}}if(!g.ps)g.ps=[];if(!g.ms)g.ms=[];if(!g.size)g.size=[];if(!g.psize)g.psize=[];if(!g.fs)g.fs=[];if(!g.set)g.set=[false,false];if(g.w){if(e.isIE)g.w.style.visibility=\"\";if(g.dirty||L){q=I(g.w,a);if(q>l)l=q;g.ms[a]=q;if(!g.set[a])if(a==0){z=e.pxself(g.w,f.size);g.fs[a]=z?z+F(g.w,a):0}else{z=Math.round(e.px(g.w,f.size));g.fs[a]=z>Math.max(T(g.w,a),q)?z+F(g.w,a):0}q=g.fs[a];if(g.layout)q=Math.max(q,g.ps[a]);else{z=u(g.w, a);var ca=typeof g.ps[a]!==\"undefined\"&&f.config[o][0]>0&&g.set[a];q=g.set[a]&&z===g.psize[a]||ca?Math.max(q,g.ps[a]):Math.max(q,z)}g.ps[a]=q;if(!g.span||g.span[a]==1)if(q>r)r=q}else if(!g.span||g.span[a]==1){if(g.ps[a]>r)r=g.ps[a];if(g.ms[a]>l)l=g.ms[a]}if(g.w.style.display!==\"none\"||e.hasTag(g.w,\"TEXTAREA\")&&g.w.wtResize)w=false}}}if(w)l=r=-1;else if(l>r)r=l;h[o]=r;n[o]=l;if(l>-1){k+=r;d+=l}}i=0;v=true;m=false;for(o=0;o<p;++o)if(n[o]>-1){if(v){i+=f.margins[1];v=false}else{i+=f.margins[0];if(m)i+= 4}m=f.config[o][1]!==0}v||(i+=f.margins[2]);k+=i;d+=i;f.measures=[h,n,k,d,i];if(U)if(j[2]!=f.measures[2]){p=f.measures[2];if(Y){c=c;for(h=c.parentNode;;){if(h.wtGetPS)p=h.wtGetPS(h,c,a,p);p+=Q(h,a);if(h==M)break;c=h;h=c.parentNode}}else p+=N[a];U.setChildSize(M,a,p)}b&&f.minSize==0&&j[3]!=f.measures[3]&&b.parentNode.className!=\"Wt-domRoot\"&&y(b,\"min\"+f.Size,f.measures[3]+\"px\")&&V.ancestor&&V.ancestor.setContentsDirty(b);b&&a==0&&b&&e.hasTag(b,\"TD\")&&y(b,f.size,f.measures[2]+\"px\")}}function da(a,c, b){a=s[a];if(S)b=-b;if(a.config[c][0]>0&&a.config[c+1][0]==0){++c;b=-b}a.fixedSize[c]=a.sizes[c]+b;G.layouts2.scheduleAdjust()}function ea(a,c,b){var f=c.di,i=s[a],j=s[a^1],p,v=e.getElement(B),h;for(h=f-1;h>=0;--h)if(i.sizes[h]>=0){p=-(i.sizes[h]-i.measures[1][h]);break}f=i.sizes[f]-i.measures[1][f];if(S){var n=p;p=-f;f=-n}new e.SizeHandle(e,i.resizeDir,e.pxself(c,i.size),e.pxself(c,j.size),p,f,i.resizerClass,function(k){da(a,h,k)},c,v,b,0,0)}function fa(a,c){var b=s[a],f=s[a^1],i=b.measures,j=0, p=false,v=false,h=false,n=W?c.parentNode:null;if(b.maxSize===0)if(n){var k=e.css(n,\"position\");if(k===\"absolute\")j=e.pxself(n,b.size);if(j===0){if(!b.initialized)if(k!==\"absolute\"){j=a?n.clientHeight:n.clientWidth;p=true;if(a==0&&j==0&&e.isIElt9){j=n.offsetWidth;p=false}var d;if((e.hasTag(n,\"TD\")||e.hasTag(n,\"TH\"))&&!(e.isIE&&!e.isIElt9)){d=0;h=1}else{d=b.minSize?b.minSize:i[3];h=0}if(e.isIElt9&&j==h||j==d+X(n,a))b.maxSize=999999}if(j===0&&b.maxSize===0){j=a?n.clientHeight:n.clientWidth;p=true}}}else{j= e.pxself(c,b.size);v=true}d=0;if(n&&n.wtGetPS&&a==1)d=n.wtGetPS(n,c,a,0);h=i[2];if(h<b.minSize)h=b.minSize;if(b.maxSize)if(h+d<b.maxSize){y(n,b.size,h+d+T(n,a)+\"px\");j=h+d;h=v=true}else{j=b.maxSize;p=false}b.cSize=j;if(a==1&&n&&n.wtResize){h=f.cSize;var o=b.cSize;n.wtResize(n,Math.round(h),Math.round(o))}j-=d;if(!v){v=0;if(typeof b.cPadding===\"undefined\"){v=p?X(n,a):T(n,a);b.cPadding=v}else v=b.cPadding;j-=v}b.initialized=true;if(!(n&&j<=0)){if(j<i[3]-d)j=i[3]-d;$(c).children(\".\"+f.handleClass).css(b.size, j-b.margins[2]-b.margins[1]+\"px\");p=[];n=b.config.length;v=f.config.length;if(j>=i[3]-d){o=j-i[4];h=[];var m=[0,0],r=[0,0],l=0;for(d=0;d<n;++d)if(i[1][d]>-1){k=-1;if(typeof b.fixedSize[d]!==\"undefined\")k=b.fixedSize[d];else if(b.config[d][1]!==0&&b.config[d][1][0]>=0){k=b.config[d][1][0];if(b.config[d][1][1])k=(j-i[4])*k/100}if(k>=0){h[d]=-1;p[d]=k;o-=p[d]}else{if(b.config[d][0]>0){k=1;h[d]=b.config[d][0];l+=h[d]}else{k=0;h[d]=0}m[k]+=i[1][d];r[k]+=i[0][d];p[d]=i[0][d]}}else h[d]=-2;if(l==0){for(d= 0;d<n;++d)if(h[d]==0){h[d]=1;++l}r[1]=r[0];m[1]=m[0];r[0]=0;m[0]=0}if(o>r[0]+m[1]){o-=r[0];if(o>r[1]){if(b.fitSize){o-=r[1];j=o/l;for(d=0;d<n;++d)if(h[d]>0)p[d]+=Math.round(h[d]*j)}}else{k=1;if(o<m[k])o=m[k];j=r[k]-m[k]>0?(o-m[k])/(r[k]-m[k]):0;for(d=0;d<n;++d)if(h[d]>0){o=i[0][d]-i[1][d];p[d]=i[1][d]+Math.round(o*j)}}}else{for(d=0;d<n;++d)if(h[d]>0)p[d]=i[1][d];o-=m[1];k=0;if(o<m[k])o=m[k];j=r[k]-m[k]>0?(o-m[k])/(r[k]-m[k]):0;for(d=0;d<n;++d)if(h[d]==0){o=i[0][d]-i[1][d];p[d]=i[1][d]+Math.round(o* j)}}}else p=i[1];b.sizes=p;i=0;j=true;m=false;for(d=0;d<n;++d)if(p[d]>-1){if(o=m){h=B+\"-rs\"+a+\"-\"+d;m=e.getElement(h);if(!m){m=document.createElement(\"div\");m.setAttribute(\"id\",h);m.di=d;m.style.position=\"absolute\";m.style[f.left]=f.margins[1]+\"px\";m.style[b.size]=b.margins[0]+\"px\";m.className=b.handleClass;c.insertBefore(m,c.firstChild);m.onmousedown=m.ontouchstart=function(z){ea(a,this,z||window.event)}}i+=2;y(m,b.left,i+\"px\");i+=2}m=b.config[d][1]!==0;if(j){i+=b.margins[1];j=false}else i+=b.margins[0]; for(r=0;r<v;++r)if((l=b.getItem(d,r))&&l.w){h=l.w;k=p[d];if(l.span){var w,g=m;for(w=1;w<l.span[a];++w){if(g)k+=4;g=b.config[d+g][1]!==0;k+=b.margins[0];k+=p[d+w]}}var q;h.style.visibility=\"\";g=l.align>>b.alignBits&15;w=l.ps[a];if(k<w)g=0;if(g){switch(g){case 1:q=i;break;case 4:q=i+(k-w)/2;break;case 2:q=i+(k-w);break}if(l.layout){y(h,b.size,w+\"px\")&&R(l);l.set[a]=true}else if(k>=w&&l.set[a]){y(h,b.size,w+\"px\")&&R(l);l.set[a]=false}l.size[a]=w;l.psize[a]=w}else{q=F(l.w,a);g=k;if(e.isIElt9||!e.hasTag(h, \"BUTTON\")&&!e.hasTag(h,\"INPUT\")&&!e.hasTag(h,\"SELECT\")&&!e.hasTag(h,\"TEXTAREA\"))g=Math.max(0,g-q);q=false;if(e.isIE&&e.hasTag(h,\"BUTTON\"))q=true;if(q||k!=w||l.layout){y(h,b.size,g+\"px\")&&R(l);l.set[a]=true}else if(l.fs[a])a==0&&y(h,b.size,l.fs[a]+\"px\");else{y(h,b.size,\"\")&&R(l);l.set[a]=false}q=i;l.size[a]=g;l.psize[a]=k}if(t)if(o){y(h,b.left,\"4px\");k=e.css(h,\"position\");if(k!==\"absolute\")h.style.position=\"relative\"}else y(h,b.left,\"0px\");else y(h,b.left,q+\"px\");if(a==1){h.wtResize&&h.wtResize(h, Math.round(l.size[0]),Math.round(l.size[1]));l.dirty=false}}i+=p[d]}}}var e=G.WT;this.ancestor=null;this.descendants=[];var V=this,D=A,K=false,L=true,W=false,U=null,M=null,Z=false,N=[],Y=false,S=$(document.body).hasClass(\"Wt-rtl\"),s=[{initialized:false,config:D.cols,margins:C,maxSize:H,measures:[],sizes:[],fixedSize:[],Left:S?\"Right\":\"Left\",left:S?\"right\":\"left\",Right:S?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,c){return D.items[c*s[0].config.length+a]},handleClass:\"Wt-vrh2\", resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:O},{initialized:false,config:D.rows,margins:x,maxSize:E,measures:[],sizes:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,c){return D.items[a*s[0].config.length+c]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:P}];jQuery.data(document.getElementById(B),\"layout\",this);this.setConfig=function(a){var c=D;D=a;s[0].config=D.cols;s[1].config=D.rows;var b;a=0;for(b=c.items.length;a< b;++a){var f=c.items[a];if(f){f.set[0]&&y(f.w,s[0].size,\"\");f.set[1]&&y(f.w,s[1].size,\"\");if(f.layout){V.setChildSize(f.w,0,f.ps[0]);V.setChildSize(f.w,1,f.ps[1])}}}L=true};this.getId=function(){return B};this.setItemsDirty=function(a){var c,b,f=s[0].config.length;c=0;for(b=a.length;c<b;++c)D.items[a[c][0]*f+a[c][1]].dirty=true;K=true};this.setDirty=function(){L=true};this.setChildSize=function(a,c,b){var f,i;f=0;for(i=D.items.length;f<i;++f){var j=D.items[f];if(j&&j.id==a.id){if(!j.ps)j.ps=[];j.ps[c]= b;j.layout=true;break}}K=true};this.measure=function(a){var c=e.getElement(B);if(c)if(!e.isHidden(c)){if(!Z){Z=true;if(W=J==null){var b=c;b=b.parentNode;for(N=[0,0];;){N[0]+=Q(b,0);N[1]+=Q(b,1);if(b.wtGetPS)Y=true;var f=jQuery.data(b.parentNode,\"layout\");if(f){U=f;M=b;break}b=b;b=b.parentNode;if(b.childNodes.length!=1&&!b.wtGetPS)break}}else{U=jQuery.data(document.getElementById(J),\"layout\");M=c;N[0]=Q(M,0);N[1]=Q(M,1)}}if(K||L)aa(a,c,W?c.parentNode:null);if(a==1)K=L=false}};this.apply=function(a){var c= e.getElement(B);if(!c)return false;if(e.isHidden(c))return true;fa(a,c);return true};this.contains=function(a){var c=e.getElement(B);a=e.getElement(a.getId());return c&&a?e.contains(c,a):false}}");
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
				"new (function(){var G=[],B=false,J=this,O=false;this.find=function(t){return jQuery.data(document.getElementById(t),\"layout\")};this.setDirty=function(t){(t=jQuery.data(t,\"layout\"))&&t.setDirty()};this.add=function(t){function H(E,C){var x,A;x=0;for(A=E.length;x<A;++x){var u=E[x];if(u.contains(C)){H(u.descendants,C);return}else if(C.contains(u)){C.descendants.push(u);E.splice(x,1);--x;--A}}E.push(C)}H(G,t)};var P=false;this.scheduleAdjust=function(){if(!P){P= true;setTimeout(function(){J.adjust()},0)}};this.adjust=function(t,H){function E(x,A){var u,I;u=0;for(I=x.length;u<I;++u){var F=x[u];E(F.descendants,A);A==1&&O&&F.setDirty();F.measure(A)}}function C(x,A){var u,I;u=0;for(I=x.length;u<I;++u){var F=x[u];if(F.apply(A))C(F.descendants,A);else{x.splice(u,1);--u;--I}}}if(t){(t=this.find(t))&&t.setItemsDirty(H);J.scheduleAdjust()}else{P=false;if(!B){B=true;E(G,0);C(G,0);E(G,1);C(G,1);O=B=false}}};this.updateConfig=function(t,H){(t=this.find(t))&&t.setConfig(H)}; window.onresize=function(){O=true;J.scheduleAdjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(a,d,c){function e(h){var j=b.px(h,\"marginTop\");j+=b.px(h,\"marginBottom\");if(!b.boxSizing(h)){j+=b.px(h,\"borderTopWidth\");j+=b.px(h,\"borderBottomWidth\");j+=b.px(h,\"paddingTop\");j+=b.px(h,\"paddingBottom\")}return j}var f,i,g,b=this;a.style.height=c+\"px\";if(b.boxSizing(a)){c-=b.px(a,\"marginTop\");c-=b.px(a,\"marginBottom\");c-=b.px(a,\"borderTopWidth\");c-=b.px(a,\"borderBottomWidth\");c-=b.px(a,\"paddingTop\");c-=b.px(a,\"paddingBottom\");d-= b.px(a,\"marginLeft\");d-=b.px(a,\"marginRight\");d-=b.px(a,\"borderLeftWidth\");d-=b.px(a,\"borderRightWidth\");d-=b.px(a,\"paddingLeft\");d-=b.px(a,\"paddingRight\")}f=0;for(i=a.childNodes.length;f<i;++f){g=a.childNodes[f];if(g.nodeType==1){var k=c-e(g);if(k>0)if(g.wtResize)g.wtResize(g,d,k);else{k=k+\"px\";if(g.style.height!=k)g.style.height=k}}}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "ChildrenGetPS",
				"function(a,d,c,e){return e}");
	}

	static WJavaScriptPreamble wtjs12() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastResize",
				"function(a,d,c){var e=this;a.style.height=c+\"px\";a=a.lastChild;var f=a.previousSibling;c-=f.offsetHeight+e.px(f,\"marginTop\")+e.px(f,\"marginBottom\");if(c>0)if(a.wtResize)a.wtResize(a,d,c);else a.style.height=c+\"px\"}");
	}

	static WJavaScriptPreamble wtjs13() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastGetPS",
				"function(a,d,c,e){var f=this,i,g;i=0;for(g=a.childNodes.length;i<g;++i){var b=a.childNodes[i];if(b!=d)if(c===0)e=Math.max(e,b.offsetWidth);else e+=b.offsetHeight+f.px(b,\"marginTop\")+f.px(b,\"marginBottom\")}return e}");
	}
}
