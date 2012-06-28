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
				"function(F,A,L,M,N,r,G,D,B,y,w){function u(a,b){var c=q[b],j=b?a.scrollHeight:a.scrollWidth,g=b?a.clientHeight:a.clientWidth;b=b?a.offsetHeight:a.offsetWidth;if(g>=1E6){g-=1E6;if(j>=1E6)j-=1E6}if(j===0)j=f.pxself(a,c.size);if(f.isIE&&(f.hasTag(a,\"BUTTON\")||f.hasTag(a,\"TEXTAREA\")||f.hasTag(a,\"INPUT\")||f.hasTag(a,\"SELECT\")))j=g;if(!f.isOpera&&!f.isGecko)j+=f.px(a,\"border\"+c.Left+\"Width\")+f.px(a,\"border\"+c.Right+\"Width\");j+=f.px(a,\"margin\"+c.Left)+ f.px(a,\"margin\"+c.Right);if(!f.boxSizing(a)&&!f.isIE)j+=f.px(a,\"padding\"+c.Left)+f.px(a,\"padding\"+c.Right);if(j<b)j=b;return j}function H(a,b){b=q[b];if(a.style.display==\"none\"||a.style.visibility==\"hidden\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=f.px(a,\"min\"+b.Size);f.boxSizing(a)||(c+=f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right));return c}}function E(a,b){b=q[b];var c=f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right);if(!f.boxSizing(a)&&!(f.isIE&&!f.isIElt9&& f.hasTag(a,\"BUTTON\")))c+=f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right);return c}function V(a,b){b=q[b];return f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}function T(a,b){if(f.boxSizing(a)){b=q[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}else return 0}function Q(a,b){b=q[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+ f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right)+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}function O(a,b){I=a.dirty=true;b&&F.layouts2.scheduleAdjust()}function z(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function X(a,b,c){b=q[a];var j=b.config.length,g=q[a^1].config.length;if(I||J){var k=b.measures.slice();if(k.length==5){k[0]=k[0].slice();k[1]=k[1].slice()}var p=[],x=[],e=0,m=0,h,i;for(h=0;h<j;++h){var n=0,l=b.config[h][2],s=true;for(i=0;i<g;++i){var d=b.getItem(h, i);if(d){if(!d.w){var v=$(\"#\"+d.id);d.w=v.get(0);(function(){v.find(\"img\").load(function(){O(d)})})();d.w.style.left=d.w.style.top=\"-1000000px\"}if(!r&&d.w.style.position!=\"absolute\"){d.w.style.position=\"absolute\";d.w.style.visibility=\"hidden\";if(!d.w.wtResize){d.w.style.boxSizing=\"border-box\";var o=f.cssPrefix(\"BoxSizing\");if(o)d.w.style[o+\"BoxSizing\"]=\"border-box\"}}if(!d.ps)d.ps=[];if(!d.ms)d.ms=[];if(!d.size)d.size=[];if(!d.psize)d.psize=[];if(!d.fs)d.fs=[];if(!d.set)d.set=[false,false];if(d.w){if(f.isIE)d.w.style.visibility= \"\";if(d.dirty||J){o=H(d.w,a);if(o>l)l=o;d.ms[a]=o;if(!d.set[a])if(a==0){var t=f.pxself(d.w,b.size);d.fs[a]=t?t+E(d.w,a):0}else{t=f.px(d.w,b.size);d.fs[a]=t>Math.max(T(d.w,a),o)?t+E(d.w,a):0}o=d.fs[a];if(d.layout)o=Math.max(o,d.ps[a]);else{t=u(d.w,a);o=(typeof d.ps[a]===\"undefined\"||b.config[h][0]<=0)&&(!d.set[a]||t!=d.psize[a])?Math.max(o,t):Math.max(o,d.ps[a])}d.ps[a]=o;if(!d.span||d.span[a]==1)if(o>n)n=o}else if(!d.span||d.span[a]==1){if(d.ps[a]>n)n=d.ps[a];if(d.ms[a]>l)l=d.ms[a]}if(d.w.style.display!== \"none\"||f.hasTag(d.w,\"TEXTAREA\")&&d.w.wtResize)s=false}}}if(s)l=n=-1;else if(l>n)n=l;p[h]=n;x[h]=l;if(l>-1){e+=n;m+=l}}g=0;i=true;n=false;for(h=0;h<j;++h)if(x[h]>-1){if(i){g+=b.margins[1];i=false}else{g+=b.margins[0];if(n)g+=4}n=b.config[h][1]!==0}i||(g+=b.margins[2]);e+=g;m+=g;b.measures=[p,x,e,m,g];R&&k[2]!=b.measures[2]&&R.setChildSize(P,a,b.measures[2]+K[a]);c&&k[3]!=b.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&&z(c,\"min\"+b.Size,b.measures[3]+\"px\")&&S.ancestor&&S.ancestor.setContentsDirty(c); c&&a==0&&c&&f.hasTag(c,\"TD\")&&z(c,b.size,b.measures[2]+\"px\")}}function Y(a,b,c){a=q[a];a.fixedSize[b]=a.sizes[b]+c;F.layouts2.scheduleAdjust()}function Z(a,b,c){var j=b.di,g=q[a],k=q[a^1],p,x=f.getElement(A),e;for(e=j-1;e>=0;--e)if(g.sizes[e]>=0){p=-(g.sizes[e]-g.measures[1][e]);break}j=g.sizes[j]-g.measures[1][j];new f.SizeHandle(f,g.resizeDir,f.pxself(b,g.size),f.pxself(b,k.size),p,j,g.resizerClass,function(m){Y(a,e,m)},b,x,c,0,0)}function aa(a,b){var c=q[a],j=q[a^1],g=c.measures,k=0,p=false,x= false,e=false,m=U?b.parentNode:null;if(c.maxSize===0)if(m){var h=f.css(m,\"position\");if(h===\"absolute\")k=f.pxself(m,c.size);if(k===0){if(!c.initialized)if(h!==\"absolute\"){k=a?m.clientHeight:m.clientWidth;p=true;if(a==0&&k==0&&f.isIElt9){k=m.offsetWidth;p=false}var i;if((f.hasTag(m,\"TD\")||f.hasTag(m,\"TH\"))&&!(f.isIE&&!f.isIElt9)){e=0;i=1}else{e=g[3];i=0}if(f.isIElt9&&k==i||k==e+V(m,a))c.maxSize=999999}if(k===0&&c.maxSize===0){k=a?m.clientHeight:m.clientWidth;p=true}}}else{k=f.pxself(b,c.size);x=true}if(c.maxSize)if(g[2]< c.maxSize){z(m,c.size,g[2]+T(m,a)+\"px\");k=g[2];e=x=true}else{k=c.maxSize;p=false}c.cSize=k;if(a==1&&m&&m.wtResize){i=j.cSize;m.wtResize(m,i,c.cSize)}if(!x){x=0;if(c.initialized)x=c.cPadding;else{x=p?V(m,a):T(m,a);c.cPadding=x}k-=x}c.initialized=true;if(!(m&&k<=0)){if(k<g[3])k=g[3];$(b).children(\".\"+j.handleClass).css(c.size,k-c.margins[2]-c.margins[1]+\"px\");p=[];m=c.config.length;x=j.config.length;if(k>g[3]){var n=k-g[4];i=[];var l=[0,0],s=[0,0],d=0;for(e=0;e<m;++e)if(g[1][e]>-1){h=-1;if(typeof c.fixedSize[e]!== \"undefined\")h=c.fixedSize[e];else if(c.config[e][1]!==0&&c.config[e][1][0]>=0){h=c.config[e][1][0];if(c.config[e][1][1])h=(k-g[4])*h/100}if(h>=0){i[e]=-1;p[e]=h;n-=p[e]}else{if(c.config[e][0]>0){h=1;i[e]=c.config[e][0];d+=i[e]}else{h=0;i[e]=0}l[h]+=g[1][e];s[h]+=g[0][e];p[e]=g[0][e]}}else i[e]=-2;if(d==0){for(e=0;e<m;++e)if(i[e]==0){i[e]=1;++d}s[1]=s[0];l[1]=l[0];s[0]=0;l[0]=0}if(n>s[0]+l[1]){n-=s[0];if(n>s[1]){if(c.fitSize){n-=s[1];k=n/d;for(e=0;e<m;++e)if(i[e]>0)p[e]+=Math.round(i[e]*k)}}else{h= 1;if(n<l[h])n=l[h];k=s[h]-l[h]>0?(n-l[h])/(s[h]-l[h]):0;for(e=0;e<m;++e)if(i[e]>0){n=g[0][e]-g[1][e];p[e]=g[1][e]+Math.round(n*k)}}}else{for(e=0;e<m;++e)if(i[e]>0)p[e]=g[1][e];n-=l[1];h=0;if(n<l[h])n=l[h];k=s[h]-l[h]>0?(n-l[h])/(s[h]-l[h]):0;for(e=0;e<m;++e)if(i[e]==0){n=g[0][e]-g[1][e];p[e]=g[1][e]+Math.round(n*k)}}}else p=g[1];c.sizes=p;g=0;k=true;l=false;for(e=0;e<m;++e)if(p[e]>-1){if(n=l){i=A+\"-rs\"+a+\"-\"+e;l=f.getElement(i);if(!l){l=document.createElement(\"div\");l.setAttribute(\"id\",i);l.di=e; l.style.position=\"absolute\";l.style[j.left]=j.margins[1]+\"px\";l.style[c.size]=c.margins[0]+\"px\";l.className=c.handleClass;b.insertBefore(l,b.firstChild);l.onmousedown=l.ontouchstart=function(ba){Z(a,this,ba||window.event)}}g+=2;z(l,c.left,g+\"px\");g+=2}l=c.config[e][1]!==0;if(k){g+=c.margins[1];k=false}else g+=c.margins[0];for(s=0;s<x;++s)if((d=c.getItem(e,s))&&d.w){i=d.w;h=p[e];if(d.span){var v,o=l;for(v=1;v<d.span[a];++v){if(o)h+=4;o=c.config[e+o][1]!==0;h+=c.margins[0];h+=p[e+v]}}var t;i.style.visibility= \"\";o=d.align>>c.alignBits&15;v=d.ps[a];if(h<v)o=0;if(o){switch(o){case 1:t=g;break;case 4:t=g+(h-v)/2;break;case 2:t=g+(h-v);break}if(d.layout){z(i,c.size,v+\"px\")&&O(d);d.set[a]=true}else if(h>=v&&d.set[a]){z(i,c.size,v+\"px\")&&O(d);d.set[a]=false}d.size[a]=v;d.psize[a]=v}else{t=E(d.w,a);o=h;if(f.isIElt9||!f.hasTag(i,\"BUTTON\")&&!f.hasTag(i,\"INPUT\")&&!f.hasTag(i,\"SELECT\")&&!f.hasTag(i,\"TEXTAREA\"))o=Math.max(0,o-t);t=false;if(f.isIE&&f.hasTag(i,\"BUTTON\"))t=true;if(t||h!=v||d.layout){z(i,c.size,o+\"px\")&& O(d);d.set[a]=true}else if(d.fs[a])a==0&&z(i,c.size,d.fs[a]+\"px\");else{z(i,c.size,\"\")&&O(d);d.set[a]=false}t=g;d.size[a]=o;d.psize[a]=h}if(r)if(n){z(i,c.left,\"4px\");h=f.css(i,\"position\");if(h!==\"absolute\")i.style.position=\"relative\"}else z(i,c.left,\"0px\");else z(i,c.left,t+\"px\");if(a==1){i.wtResize&&i.wtResize(i,d.size[0],d.size[1]);d.dirty=false}}g+=p[e]}}}var f=F.WT;this.ancestor=null;this.descendants=[];var S=this,C=w,I=false,J=true,U=false,R=null,P=null,W=false,K=[];w=$(document.body).hasClass(\"Wt-rtl\"); var q=[{initialized:false,config:C.cols,margins:B,maxSize:G,measures:[],sizes:[],fixedSize:[],Left:w?\"Right\":\"Left\",left:w?\"right\":\"left\",Right:w?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return C.items[b*q[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:M},{initialized:false,config:C.rows,margins:y,maxSize:D,measures:[],sizes:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a, b){return C.items[a*q[0].config.length+b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:N}];jQuery.data(document.getElementById(A),\"layout\",this);this.setConfig=function(a){var b=C;C=a;q[0].config=C.cols;q[1].config=C.rows;var c;a=0;for(c=b.items.length;a<c;++a){var j=b.items[a];if(j){j.set[0]&&z(j.w,q[0].size,\"\");j.set[1]&&z(j.w,q[1].size,\"\");if(j.layout){S.setChildSize(j.w,0,j.ps[0]);S.setChildSize(j.w,1,j.ps[1])}}}J=true};this.getId=function(){return A};this.setItemsDirty= function(a){var b,c,j=q[0].config.length;b=0;for(c=a.length;b<c;++b)C.items[a[b][0]*j+a[b][1]].dirty=true;I=true};this.setDirty=function(){J=true};this.setChildSize=function(a,b,c){var j,g;j=0;for(g=C.items.length;j<g;++j){var k=C.items[j];if(k&&k.id==a.id){if(!k.ps)k.ps=[];k.ps[b]=c;k.layout=true;break}}I=true};this.measure=function(a){var b=f.getElement(A);if(b)if(!f.isHidden(b)){if(!W){W=true;if(U=L==null){var c=b.parentNode;for(K=[0,0];;){K[0]+=Q(c,0);K[1]+=Q(c,1);var j=jQuery.data(c.parentNode, \"layout\");if(j){R=j;P=c;break}c=c.parentNode;if(c.childNodes.length!=1)break}}else{R=jQuery.data(document.getElementById(L),\"layout\");P=b;K[0]=Q(P,0);K[1]=Q(P,1)}}if(I||J)X(a,b,U?b.parentNode:null);if(a==1)I=J=false}};this.apply=function(a){var b=f.getElement(A);if(!b)return false;if(f.isHidden(b))return true;aa(a,b);return true};this.contains=function(a){var b=f.getElement(A);a=f.getElement(a.getId());return b&&a?f.contains(b,a):false}}");
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
				"new (function(){var F=[],A=false,L=this,M=false;this.find=function(r){return jQuery.data(document.getElementById(r),\"layout\")};this.setDirty=function(r){(r=jQuery.data(r,\"layout\"))&&r.setDirty()};this.add=function(r){function G(D,B){var y,w;y=0;for(w=D.length;y<w;++y){var u=D[y];if(u.contains(B)){G(u.descendants,B);return}else if(B.contains(u)){B.descendants.push(u);D.splice(y,1);--y;--w}}D.push(B)}G(F,r)};var N=false;this.scheduleAdjust=function(){if(!N){N= true;setTimeout(function(){L.adjust()},0)}};this.adjust=function(r,G){function D(y,w){var u,H;u=0;for(H=y.length;u<H;++u){var E=y[u];D(E.descendants,w);w==1&&M&&E.setDirty();E.measure(w)}}function B(y,w){var u,H;u=0;for(H=y.length;u<H;++u){var E=y[u];if(E.apply(w))B(E.descendants,w);else{y.splice(u,1);--u;--H}}}if(r)(r=this.find(r))&&r.setItemsDirty(G);else{N=false;if(!A){A=true;D(F,0);B(F,0);D(F,1);B(F,1);M=A=false}}};this.updateConfig=function(r,G){(r=this.find(r))&&r.setConfig(G)};window.onresize= function(){M=true;L.scheduleAdjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(b,e,c){function k(f){var g=a.px(f,\"marginTop\");g+=a.px(f,\"marginBottom\");if(!a.boxSizing(f)){g+=a.px(f,\"borderTopWidth\");g+=a.px(f,\"borderBottomWidth\");g+=a.px(f,\"paddingTop\");g+=a.px(f,\"paddingBottom\")}return g}var i,j,d,a=this;b.style.height=c+\"px\";if(a.boxSizing(b)){c-=a.px(b,\"marginTop\");c-=a.px(b,\"marginBottom\");c-=a.px(b,\"borderTopWidth\");c-=a.px(b,\"borderBottomWidth\");c-=a.px(b,\"paddingTop\");c-=a.px(b,\"paddingBottom\");e-= a.px(b,\"marginLeft\");e-=a.px(b,\"marginRight\");e-=a.px(b,\"borderLeftWidth\");e-=a.px(b,\"borderRightWidth\");e-=a.px(b,\"paddingLeft\");e-=a.px(b,\"paddingRight\")}i=0;for(j=b.childNodes.length;i<j;++i){d=b.childNodes[i];if(d.nodeType==1){var h=c-k(d);if(h>0)if(d.wtResize)d.wtResize(d,e,h);else{h=h+\"px\";if(d.style.height!=h)d.style.height=h}}}}");
	}
}
