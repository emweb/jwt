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
				"function(F,A,L,M,N,s,G,E,B,x,z){function u(a,b){var c=r[b],k=b?a.scrollHeight:a.scrollWidth;b=b?a.clientHeight:a.clientWidth;if(b>=1E6){b-=1E6;if(k>=1E6)k-=1E6}if(f.isIE&&(f.hasTag(a,\"BUTTON\")||f.hasTag(a,\"TEXTAREA\")||f.hasTag(a,\"INPUT\")||f.hasTag(a,\"SELECT\")))k=b;if(!f.isOpera&&!f.isGecko)k+=f.px(a,\"border\"+c.Left+\"Width\")+f.px(a,\"border\"+c.Right+\"Width\");k+=f.px(a,\"margin\"+c.Left)+f.px(a,\"margin\"+c.Right);if(!f.boxSizing(a)&&!f.isIE)k+=f.px(a, \"padding\"+c.Left)+f.px(a,\"padding\"+c.Right);return k}function H(a,b){b=r[b];if(a.style.display==\"none\"||a.style.visibility==\"hidden\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=f.px(a,\"min\"+b.Size);f.boxSizing(a)||(c+=f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right));return c}}function C(a,b){b=r[b];var c=f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right);if(!f.boxSizing(a)&&!(f.isIE&&!f.isIElt9&&f.hasTag(a,\"BUTTON\")))c+=f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+ b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right);return c}function V(a,b){b=r[b];return f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}function T(a,b){if(f.boxSizing(a)){b=r[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}else return 0}function Q(a,b){b=r[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right)+f.px(a,\"padding\"+b.Left)+ f.px(a,\"padding\"+b.Right)}function O(a,b){I=a.dirty=true;b&&F.layouts2.scheduleAdjust()}function y(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function X(a,b,c){b=r[a];var k=b.config.length,h=r[a^1].config.length;if(I||J){var i=b.measures.slice();if(i.length==5){i[0]=i[0].slice();i[1]=i[1].slice()}var p=[],w=[],e=0,m=0,g,j;for(g=0;g<k;++g){var n=0,l=b.config[g][2],t=true;for(j=0;j<h;++j){var d=b.getItem(g,j);if(d){if(!d.w){var v=$(\"#\"+d.id);d.w=v.get(0);(function(){v.find(\"img\").load(function(){O(d)})})(); d.w.style.left=d.w.style.top=\"-1000000px\";var o=C(d.w,0),q=C(d.w,1);d.m=[o,q]}if(!s&&d.w.style.position!=\"absolute\"){d.w.style.position=\"absolute\";d.w.style.visibility=\"hidden\";if(!d.w.wtResize){d.w.style.boxSizing=\"border-box\";if(o=f.cssPrefix(\"BoxSizing\"))d.w.style[o+\"BoxSizing\"]=\"border-box\"}}if(!d.ps)d.ps=[];if(!d.ms)d.ms=[];if(!d.size)d.size=[];if(!d.psize)d.psize=[];if(!d.fs)d.fs=[];if(!d.set)d.set=[false,false];if(d.w){if(f.isIE)d.w.style.visibility=\"\";if(d.dirty||J){o=H(d.w,a);if(o>l)l=o; d.ms[a]=o;if(!d.set[a])if(a==0){q=f.pxself(d.w,b.size);d.fs[a]=q?q+C(d.w,a):0}else{q=f.px(d.w,b.size);d.fs[a]=q>Math.max(T(d.w,a),o)?q+C(d.w,a):0}o=d.fs[a];if(d.layout)o=Math.max(o,d.ps[a]);else{q=u(d.w,a);o=(typeof d.ps[a]===\"undefined\"||b.config[g][0]<=0)&&(!d.set[a]||q!=d.psize[a])?Math.max(o,q):Math.max(o,d.ps[a])}d.ps[a]=o;if(!d.span||d.span[a]==1)if(o>n)n=o}else if(!d.span||d.span[a]==1){if(d.ps[a]>n)n=d.ps[a];if(d.ms[a]>l)l=d.ms[a]}if(d.w.style.display!==\"none\"||f.hasTag(d.w,\"TEXTAREA\")&&d.w.wtResize)t= false}}}if(t)l=n=-1;else if(l>n)n=l;p[g]=n;w[g]=l;if(l>-1){e+=n;m+=l}}h=0;j=true;n=false;for(g=0;g<k;++g)if(w[g]>-1){if(j){h+=b.margins[1];j=false}else{h+=b.margins[0];if(n)h+=4}n=b.config[g][1]!==0}j||(h+=b.margins[2]);e+=h;m+=h;b.measures=[p,w,e,m,h];R&&i[2]!=b.measures[2]&&R.setChildSize(P,a,b.measures[2]+K[a]);c&&i[3]!=b.measures[3]&&y(c,\"min\"+b.Size,b.measures[3]+\"px\")&&S.ancestor&&S.ancestor.setContentsDirty(c);c&&a==0&&c&&f.hasTag(c,\"TD\")&&y(c,b.size,b.measures[2]+\"px\")}}function Y(a,b,c){a= r[a];a.fixedSize[b]=a.sizes[b]+c;F.layouts2.scheduleAdjust()}function Z(a,b,c){var k=b.di,h=r[a],i=r[a^1],p,w=f.getElement(A),e;for(e=k-1;e>=0;--e)if(h.sizes[e]>=0){p=-(h.sizes[e]-h.measures[1][e]);break}k=h.sizes[k]-h.measures[1][k];new f.SizeHandle(f,h.resizeDir,f.pxself(b,h.size),f.pxself(b,i.size),p,k,h.resizerClass,function(m){Y(a,e,m)},b,w,c,0,0)}function aa(a,b){var c=r[a],k=r[a^1],h=c.measures,i=0,p=false,w=false,e=false,m=U?b.parentNode:null;if(c.maxSize===0)if(m){var g=f.css(m,\"position\"); if(g===\"absolute\")i=f.pxself(m,c.size);if(i===0){if(!c.initialized)if(g!==\"absolute\"){i=a?m.clientHeight:m.clientWidth;p=true;if(a==0&&i==0&&f.isIE6){i=m.offsetWidth;p=false}if(f.isIE6&&i==0||i==h[3]+V(m,a))c.maxSize=999999}if(i===0&&c.maxSize===0){i=a?m.clientHeight:m.clientWidth;p=true}}}else{i=f.pxself(b,c.size);w=true}if(c.maxSize)if(h[2]<c.maxSize){y(m,c.size,h[2]+T(m,a)+\"px\");i=h[2];e=w=true}else{i=c.maxSize;p=false}c.cSize=i;if(a==1&&(c.maxSize||k.maxSize)){var j=k.cSize;e=c.cSize;m&&m.wtResize&& m.wtResize(m,j,e)}if(!w){w=0;if(c.initialized)w=c.cPadding;else{w=p?V(m,a):T(m,a);c.cPadding=w}i-=w}c.initialized=true;if(!(m&&i<=0)){if(i<h[3])i=h[3];$(b).children(\".\"+k.handleClass).css(c.size,i-c.margins[2]-c.margins[1]+\"px\");p=[];m=c.config.length;w=k.config.length;if(i>h[3]){var n=i-h[4];j=[];var l=[0,0],t=[0,0],d=0;for(e=0;e<m;++e)if(h[1][e]>-1){g=-1;if(typeof c.fixedSize[e]!==\"undefined\")g=c.fixedSize[e];else if(c.config[e][1]!==0&&c.config[e][1][0]>=0){g=c.config[e][1][0];if(c.config[e][1][1])g= (i-h[4])*g/100}if(g>=0){j[e]=-1;p[e]=g;n-=p[e]}else{if(c.config[e][0]>0){g=1;j[e]=c.config[e][0];d+=j[e]}else{g=0;j[e]=0}l[g]+=h[1][e];t[g]+=h[0][e];p[e]=h[0][e]}}else j[e]=-2;if(d==0){for(e=0;e<m;++e)if(j[e]==0){j[e]=1;++d}t[1]=t[0];l[1]=l[0];t[0]=0;l[0]=0}if(n>t[0]+l[1]){n-=t[0];if(n>t[1]){if(c.fitSize){n-=t[1];i=n/d;for(e=0;e<m;++e)if(j[e]>0)p[e]+=Math.round(j[e]*i)}}else{g=1;if(n<l[g])n=l[g];i=t[g]-l[g]>0?(n-l[g])/(t[g]-l[g]):0;for(e=0;e<m;++e)if(j[e]>0){n=h[0][e]-h[1][e];p[e]=h[1][e]+Math.round(n* i)}}}else{for(e=0;e<m;++e)if(j[e]>0)p[e]=h[1][e];n-=l[1];g=0;if(n<l[g])n=l[g];i=t[g]-l[g]>0?(n-l[g])/(t[g]-l[g]):0;for(e=0;e<m;++e)if(j[e]==0){n=h[0][e]-h[1][e];p[e]=h[1][e]+Math.round(n*i)}}}else p=h[1];c.sizes=p;h=0;i=true;l=false;for(e=0;e<m;++e)if(p[e]>-1){if(n=l){j=A+\"-rs\"+a+\"-\"+e;l=f.getElement(j);if(!l){l=document.createElement(\"div\");l.setAttribute(\"id\",j);l.di=e;l.style.position=\"absolute\";l.style[k.left]=k.margins[1]+\"px\";l.style[c.size]=c.margins[0]+\"px\";l.className=c.handleClass;b.insertBefore(l, b.firstChild);l.onmousedown=l.ontouchstart=function(ba){Z(a,this,ba||window.event)}}h+=2;y(l,c.left,h+\"px\");h+=2}l=c.config[e][1]!==0;if(i){h+=c.margins[1];i=false}else h+=c.margins[0];for(t=0;t<w;++t)if((d=c.getItem(e,t))&&d.w){j=d.w;g=p[e];if(d.span){var v,o=l;for(v=1;v<d.span[a];++v){if(o)g+=4;o=c.config[e+o][1]!==0;g+=c.margins[0];g+=p[e+v]}}var q;j.style.visibility=\"\";o=d.align>>c.alignBits&15;v=d.ps[a];if(g<v)o=0;if(o){switch(o){case 1:q=h;break;case 4:q=h+(g-v)/2;break;case 2:q=h+(g-v);break}if(d.layout){y(j, c.size,v+\"px\")&&O(d);d.set[a]=true}else if(g>=v&&d.set[a]){y(j,c.size,v+\"px\")&&O(d);d.set[a]=false}d.size[a]=v;d.psize[a]=v}else{q=d.m[a];o=g;if(f.isIElt9||!f.hasTag(j,\"BUTTON\")&&!f.hasTag(j,\"INPUT\")&&!f.hasTag(j,\"SELECT\")&&!f.hasTag(j,\"TEXTAREA\"))o=Math.max(0,o-q);q=false;if(f.isIE&&f.hasTag(j,\"BUTTON\"))q=true;if(q||g!=v||d.layout){y(j,c.size,o+\"px\")&&O(d);d.set[a]=true}else if(d.fs[a])a==0&&y(j,c.size,d.fs[a]+\"px\");else{y(j,c.size,\"\")&&O(d);d.set[a]=false}q=h;d.size[a]=o;d.psize[a]=g}if(s)if(n){y(j, c.left,\"4px\");g=f.css(j,\"position\");if(g!==\"absolute\")j.style.position=\"relative\"}else y(j,c.left,\"0px\");else y(j,c.left,q+\"px\");if(a==1){j.wtResize&&j.wtResize(j,d.size[0],d.size[1]);d.dirty=false}}h+=p[e]}}}var f=F.WT;this.ancestor=null;this.descendants=[];var S=this,D=z,I=false,J=true,U=false,R=null,P=null,W=false,K=[],r=[{initialized:false,config:D.cols,margins:B,maxSize:G,measures:[],sizes:[],fixedSize:[],Left:\"Left\",left:\"left\",Right:\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a, b){return D.items[b*r[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:M},{initialized:false,config:D.rows,margins:x,maxSize:E,measures:[],sizes:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,b){return D.items[a*r[0].config.length+b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:N}];jQuery.data(document.getElementById(A),\"layout\",this);this.setConfig=function(a){var b=D;D= a;r[0].config=D.cols;r[1].config=D.rows;var c;a=0;for(c=b.items.length;a<c;++a){var k=b.items[a];if(k){k.set[0]&&y(k.w,r[0].size,\"\");k.set[1]&&y(k.w,r[1].size,\"\");if(k.layout){S.setChildSize(k.w,0,k.ps[0]);S.setChildSize(k.w,1,k.ps[1])}}}J=true};this.getId=function(){return A};this.setItemsDirty=function(a){var b,c,k=r[0].config.length;b=0;for(c=a.length;b<c;++b)D.items[a[b][0]*k+a[b][1]].dirty=true;I=true};this.setDirty=function(){J=true};this.setChildSize=function(a,b,c){var k,h;k=0;for(h=D.items.length;k< h;++k){var i=D.items[k];if(i&&i.id==a.id){if(!i.ps)i.ps=[];i.ps[b]=c;i.layout=true;break}}I=true};this.measure=function(a){var b=f.getElement(A);if(b)if(!f.isHidden(b)){if(!W){W=true;if(U=L==null){var c=b.parentNode;for(K=[0,0];;){K[0]+=Q(c,0);K[1]+=Q(c,1);var k=jQuery.data(c.parentNode,\"layout\");if(k){R=k;P=c;break}c=c.parentNode;if(c.childNodes.length!=1)break}}else{R=jQuery.data(document.getElementById(L),\"layout\");P=b;K[0]=Q(P,0);K[1]=Q(P,1)}}if(I||J)X(a,b,U?b.parentNode:null);if(a==1)I=J=false}}; this.apply=function(a){var b=f.getElement(A);if(!b)return false;if(f.isHidden(b))return true;aa(a,b);return true};this.contains=function(a){var b=f.getElement(A);a=f.getElement(a.getId());return b&&a?f.contains(b,a):false}}");
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
				"new (function(){var F=[],A=false,L=this,M=false;this.find=function(s){return jQuery.data(document.getElementById(s),\"layout\")};this.setDirty=function(s){(s=jQuery.data(s,\"layout\"))&&s.setDirty()};this.add=function(s){function G(E,B){var x,z;x=0;for(z=E.length;x<z;++x){var u=E[x];if(u.contains(B)){G(u.descendants,B);return}else if(B.contains(u)){B.descendants.push(u);E.splice(x,1);--x;--z}}E.push(B)}G(F,s)};var N=false;this.scheduleAdjust=function(){if(!N){N= true;setTimeout(function(){L.adjust()},0)}};this.adjust=function(s,G){function E(x,z){var u,H;u=0;for(H=x.length;u<H;++u){var C=x[u];E(C.descendants,z);z==1&&M&&C.setDirty();C.measure(z)}}function B(x,z){var u,H;u=0;for(H=x.length;u<H;++u){var C=x[u];if(C.apply(z))B(C.descendants,z);else{x.splice(u,1);--u;--H}}}if(s)(s=this.find(s))&&s.setItemsDirty(G);else{N=false;if(!A){A=true;E(F,0);B(F,0);E(F,1);B(F,1);M=A=false}}};this.updateConfig=function(s,G){(s=this.find(s))&&s.setConfig(G)};window.onresize= function(){M=true;L.scheduleAdjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(b,e,c){function k(f){var g=a.px(f,\"marginTop\");g+=a.px(f,\"marginBottom\");if(!a.boxSizing(f)){g+=a.px(f,\"borderTopWidth\");g+=a.px(f,\"borderBottomWidth\");g+=a.px(f,\"paddingTop\");g+=a.px(f,\"paddingBottom\")}return g}var i,j,d,a=this;b.style.height=c+\"px\";if(a.boxSizing(b)){c-=a.px(b,\"marginTop\");c-=a.px(b,\"marginBottom\");c-=a.px(b,\"borderTopWidth\");c-=a.px(b,\"borderBottomWidth\");c-=a.px(b,\"paddingTop\");c-=a.px(b,\"paddingBottom\");e-= a.px(b,\"marginLeft\");e-=a.px(b,\"marginRight\");e-=a.px(b,\"borderLeftWidth\");e-=a.px(b,\"borderRightWidth\");e-=a.px(b,\"paddingLeft\");e-=a.px(b,\"paddingRight\")}i=0;for(j=b.childNodes.length;i<j;++i){d=b.childNodes[i];if(d.nodeType==1){var h=c-k(d);if(h>0)if(d.wtResize)d.wtResize(d,e,h);else{h=h+\"px\";if(d.style.height!=h)d.style.height=h}}}}");
	}
}
