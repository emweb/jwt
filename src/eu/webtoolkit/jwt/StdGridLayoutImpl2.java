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
			app.doJavaScript(app.getJavaScriptClass()
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
				".layouts2.add(new Wt3_2_3.StdLayout2(").append(
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
				parent.callJavaScript("Wt3_2_3.remove('"
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
		if (app.getEnvironment().agentIsIElt(9)) {
			c.setProperty(Property.PropertyStylePosition, "absolute");
		}
		c.setProperty(Property.PropertyStyleVisibility, "hidden");
		return c;
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"StdLayout2",
				"function(F,C,I,O,P,t,D,z,x,w,B){function v(a,b){var c=r[b],g=b?a.scrollHeight:a.scrollWidth,h=b?a.clientHeight:a.clientWidth;b=b?a.offsetHeight:a.offsetWidth;if(h>=1E6)h-=1E6;if(g>=1E6)g-=1E6;if(b>=1E6)b-=1E6;if(g===0)g=e.pxself(a,c.size);if(e.isIE&&(e.hasTag(a,\"BUTTON\")||e.hasTag(a,\"TEXTAREA\")||e.hasTag(a,\"INPUT\")||e.hasTag(a,\"SELECT\")))g=h;if(g>b)if(e.pxself(a,c.size)==0)g=0;else{var l=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!= \"none\")l=true});if(l)g=0}if(!e.isOpera&&!e.isGecko)g+=e.px(a,\"border\"+c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\");g+=e.px(a,\"margin\"+c.Left)+e.px(a,\"margin\"+c.Right);if(!e.boxSizing(a)&&!e.isIE)g+=e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right);if(g<b)g=b;return Math.round(g)}function H(a,b){b=r[b];if(a.style.display==\"none\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=e.px(a,\"min\"+b.Size);e.boxSizing(a)||(c+=e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right)); return c}}function G(a,b){b=r[b];var c=e.px(a,\"margin\"+b.Left)+e.px(a,\"margin\"+b.Right);if(!e.boxSizing(a)&&!(e.isIE&&!e.isIElt9&&e.hasTag(a,\"BUTTON\")))c+=e.px(a,\"border\"+b.Left+\"Width\")+e.px(a,\"border\"+b.Right+\"Width\")+e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right);return c}function X(a,b){b=r[b];return e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right)}function T(a,b){if(e.boxSizing(a)){b=r[b];return e.px(a,\"border\"+b.Left+\"Width\")+e.px(a,\"border\"+b.Right+\"Width\")+e.px(a,\"padding\"+b.Left)+e.px(a, \"padding\"+b.Right)}else return 0}function Q(a,b){b=r[b];return Math.round(e.px(a,\"border\"+b.Left+\"Width\")+e.px(a,\"border\"+b.Right+\"Width\")+e.px(a,\"margin\"+b.Left)+e.px(a,\"margin\"+b.Right)+e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right))}function K(a,b){R=a.dirty=true;b&&F.layouts2.scheduleAdjust()}function y(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function aa(a,b,c){b=r[a];var g=r[a^1],h=b.measures,l=b.config.length,o=g.config.length;if(R||J){if(c&&typeof b.minSize== \"undefined\"){b.minSize=e.px(c,\"min\"+b.Size);if(b.minSize>0)b.minSize-=T(c,a)}h=h.slice();if(h.length==5){h[0]=h[0].slice();h[1]=h[1].slice()}var A=[],d=[],m=0,j=0,i,q;for(i=0;i<l;++i){var k=0,s=b.config[i][2],p=true;for(q=0;q<o;++q){var f=b.getItem(i,q);if(f){if(!f.w||a==0&&f.dirty){var n=$(\"#\"+f.id),u=n.get(0);if(u!=f.w){f.w=u;n.find(\"img\").add(n.filter(\"img\")).bind(\"load\",{item:f},function(ba){K(ba.data.item,true)});f.w.style[b.left]=f.w.style[g.left]=\"-1000000px\"}}if(!t&&f.w.style.position!=\"absolute\"){f.w.style.position= \"absolute\";f.w.style.visibility=\"hidden\";if(!f.w.wtResize&&!e.isIElt9){f.w.style.boxSizing=\"border-box\";if(n=e.cssPrefix(\"BoxSizing\"))f.w.style[n+\"BoxSizing\"]=\"border-box\"}}if(!f.ps)f.ps=[];if(!f.ms)f.ms=[];if(!f.size)f.size=[];if(!f.psize)f.psize=[];if(!f.fs)f.fs=[];n=!f.set;if(!f.set)f.set=[false,false];if(f.w){if(e.isIE)f.w.style.visibility=\"\";if(f.dirty||J){u=H(f.w,a);if(u>s)s=u;f.ms[a]=u;if(!f.set[a])if(a==0||!n){n=e.pxself(f.w,b.size);f.fs[a]=n?n+G(f.w,a):0}else{n=Math.round(e.px(f.w,b.size)); f.fs[a]=n>Math.max(T(f.w,a),u)?n+G(f.w,a):0}n=f.fs[a];if(f.layout){if(n==0)n=f.ps[a]}else{if(f.wasLayout){f.wasLayout=false;f.set=[false,false];y(f.w,r[0].size,\"\");y(f.w,r[1].size,\"\")}u=v(f.w,a);var V=typeof f.ps[a]!==\"undefined\"&&b.config[i][0]>0&&f.set[a];n=f.set[a]&&u>=f.psize[a]-4&&u<=f.psize[a]+4||V?Math.max(n,f.ps[a]):Math.max(n,u)}f.ps[a]=n;if(!f.span||f.span[a]==1)if(n>k)k=n}else if(!f.span||f.span[a]==1){if(f.ps[a]>k)k=f.ps[a];if(f.ms[a]>s)s=f.ms[a]}if(f.w.style.display!==\"none\"||e.hasTag(f.w, \"TEXTAREA\")&&f.w.wtResize)p=false}}}if(p)s=k=-1;else if(s>k)k=s;A[i]=k;d[i]=s;if(s>-1){m+=k;j+=s}}g=0;n=true;o=false;for(i=0;i<l;++i)if(d[i]>-1){if(n){g+=b.margins[1];n=false}else{g+=b.margins[0];if(o)g+=4}o=b.config[i][1]!==0}n||(g+=b.margins[2]);m+=g;j+=g;b.measures=[A,d,m,j,g];if(J||h[2]!=b.measures[2])L.updateSizeInParent(a);c&&b.minSize==0&&h[3]!=b.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&&y(c,\"min\"+b.Size,b.measures[3]+\"px\")&&L.ancestor&&L.ancestor.setContentsDirty(c);c&&a==0&&c&&e.hasTag(c, \"TD\")&&y(c,b.size,b.measures[2]+\"px\")}}function ca(a,b,c){a=r[a];if(S)c=-c;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;c=-c}a.fixedSize[b]=a.sizes[b]+c;F.layouts2.scheduleAdjust()}function da(a,b,c){var g=b.di,h=r[a],l=r[a^1],o,A=e.getElement(C),d;for(d=g-1;d>=0;--d)if(h.sizes[d]>=0){o=-(h.sizes[d]-h.measures[1][d]);break}g=h.sizes[g]-h.measures[1][g];if(S){var m=o;o=-g;g=-m}new e.SizeHandle(e,h.resizeDir,e.pxself(b,h.size),e.pxself(b,l.size),o,g,h.resizerClass,function(j){ca(a,d,j)},b,A,c,0,0)} function ea(a,b){var c=r[a],g=r[a^1],h=c.measures,l=0,o=false,A=false,d=false,m=W?b.parentNode:null;if(c.maxSize===0)if(m){var j=e.css(m,\"position\");if(j===\"absolute\")l=e.pxself(m,c.size);if(l===0){if(!c.initialized)if(j!==\"absolute\"){l=a?m.clientHeight:m.clientWidth;o=true;if(a==0&&l==0&&e.isIElt9){l=m.offsetWidth;o=false}var i;if((e.hasTag(m,\"TD\")||e.hasTag(m,\"TH\"))&&!(e.isIE&&!e.isIElt9)){d=0;i=1}else{d=c.minSize?c.minSize:h[3];i=0}if(e.isIElt9&&l==i||l==d+X(m,a))c.maxSize=999999}if(l===0&&c.maxSize=== 0){l=a?m.clientHeight:m.clientWidth;o=true}}}else{l=e.pxself(b,c.size);A=true}var q=0;if(m&&m.wtGetPS&&a==1)q=m.wtGetPS(m,b,a,0);d=h[2];if(d<c.minSize)d=c.minSize;if(c.maxSize)if(d+q<c.maxSize){y(m,c.size,d+q+T(m,a)+\"px\");l=d+q;d=A=true}else{l=c.maxSize;o=false}c.cSize=l;if(a==1&&m&&m.wtResize){i=g.cSize;d=c.cSize;m.wtResize(m,Math.round(i),Math.round(d))}l-=q;if(!A){A=0;if(typeof c.cPadding===\"undefined\"){A=o?X(m,a):T(m,a);c.cPadding=A}else A=c.cPadding;l-=A}c.initialized=true;if(!(m&&l<=0)){if(l< h[3]-q)l=h[3]-q;$(b).children(\".\"+g.handleClass).css(c.size,l-c.margins[2]-c.margins[1]+\"px\");o=[];m=c.config.length;A=g.config.length;for(d=0;d<m;++d)c.stretched[d]=false;if(l>=h[3]-q){q=l-h[4];i=[];var k=[0,0],s=[0,0],p=0;for(d=0;d<m;++d)if(h[1][d]>-1){j=-1;if(typeof c.fixedSize[d]!==\"undefined\")j=c.fixedSize[d];else if(c.config[d][1]!==0&&c.config[d][1][0]>=0){j=c.config[d][1][0];if(c.config[d][1][1])j=(l-h[4])*j/100}if(j>=0){i[d]=-1;o[d]=j;q-=o[d]}else{if(c.config[d][0]>0){j=1;i[d]=c.config[d][0]; p+=i[d]}else{j=0;i[d]=0}k[j]+=h[1][d];s[j]+=h[0][d];o[d]=h[0][d]}}else i[d]=-2;if(p==0){for(d=0;d<m;++d)if(i[d]==0){i[d]=1;++p}s[1]=s[0];k[1]=k[0];s[0]=0;k[0]=0}if(q>s[0]+k[1]){q-=s[0];if(q>s[1]){if(c.fitSize){q-=s[1];l=q/p;for(d=0;d<m;++d)if(i[d]>0){o[d]+=Math.round(i[d]*l);c.stretched[d]=true}}}else{j=1;if(q<k[j])q=k[j];l=s[j]-k[j]>0?(q-k[j])/(s[j]-k[j]):0;for(d=0;d<m;++d)if(i[d]>0){q=h[0][d]-h[1][d];o[d]=h[1][d]+Math.round(q*l)}}}else{for(d=0;d<m;++d)if(i[d]>0)o[d]=h[1][d];q-=k[1];j=0;if(q<k[j])q= k[j];l=s[j]-k[j]>0?(q-k[j])/(s[j]-k[j]):0;for(d=0;d<m;++d)if(i[d]==0){q=h[0][d]-h[1][d];o[d]=h[1][d]+Math.round(q*l)}}}else o=h[1];c.sizes=o;h=0;l=true;k=false;for(d=0;d<m;++d)if(o[d]>-1){if(q=k){i=C+\"-rs\"+a+\"-\"+d;k=e.getElement(i);if(!k){k=document.createElement(\"div\");k.setAttribute(\"id\",i);k.di=d;k.style.position=\"absolute\";k.style[g.left]=g.margins[1]+\"px\";k.style[c.size]=c.margins[0]+\"px\";k.className=c.handleClass;b.insertBefore(k,b.firstChild);k.onmousedown=k.ontouchstart=function(V){da(a,this, V||window.event)}}h+=2;y(k,c.left,h+\"px\");h+=2}k=c.config[d][1]!==0;if(l){h+=c.margins[1];l=false}else h+=c.margins[0];for(s=0;s<A;++s)if((p=c.getItem(d,s))&&p.w){i=p.w;j=o[d];if(p.span){var f,n=k;for(f=1;f<p.span[a];++f){if(n)j+=4;n=c.config[d+n][1]!==0;j+=c.margins[0];j+=o[d+f]}}var u;y(i,\"visibility\",\"\");n=p.align>>c.alignBits&15;f=p.ps[a];if(j<f)n=0;if(n){switch(n){case 1:u=h;break;case 4:u=h+(j-f)/2;break;case 2:u=h+(j-f);break}if(p.layout){y(i,c.size,f+\"px\")&&K(p);p.set[a]=true}else if(j>=f&& p.set[a]){y(i,c.size,f+\"px\")&&K(p);p.set[a]=false}p.size[a]=f;p.psize[a]=f}else{u=G(p.w,a);n=j;if(e.isIElt9||!e.hasTag(i,\"BUTTON\")&&!e.hasTag(i,\"INPUT\")&&!e.hasTag(i,\"SELECT\")&&!e.hasTag(i,\"TEXTAREA\"))n=Math.max(0,n-u);u=false;if(e.isIE&&e.hasTag(i,\"BUTTON\"))u=true;if(u||j!=f||p.layout){y(i,c.size,n+\"px\")&&K(p);p.set[a]=true}else if(p.fs[a])a==0&&y(i,c.size,p.fs[a]+\"px\");else{y(i,c.size,\"\")&&K(p);p.set[a]=false}u=h;p.size[a]=n;p.psize[a]=j}if(t)if(q){y(i,c.left,\"4px\");j=e.css(i,\"position\");if(j!== \"absolute\")i.style.position=\"relative\"}else y(i,c.left,\"0px\");else y(i,c.left,u+\"px\");if(a==1){i.wtResize&&i.wtResize(i,Math.round(p.size[0]),Math.round(p.size[1]),p);p.dirty=false}}h+=o[d]}}}var e=F.WT;this.ancestor=null;this.descendants=[];var L=this,E=B,R=false,J=true,W=false,U=null,M=null,Y=false,N=[],Z=false,S=$(document.body).hasClass(\"Wt-rtl\"),r=[{initialized:false,config:E.cols,margins:x,maxSize:D,measures:[],sizes:[],stretched:[],fixedSize:[],Left:S?\"Right\":\"Left\",left:S?\"right\":\"left\",Right:S? \"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return E.items[b*r[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:O},{initialized:false,config:E.rows,margins:w,maxSize:z,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,b){return E.items[a*r[0].config.length+b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:P}];jQuery.data(document.getElementById(C), \"layout\",this);this.updateSizeInParent=function(a){if(U){var b=r[a].measures[2];if(Z){var c=e.getElement(C);if(!c)return;c=c;for(var g=c.parentNode;;){if(g.wtGetPS)b=g.wtGetPS(g,c,a,b);b+=Q(g,a);if(g==M)break;c=g;g=c.parentNode}}else b+=N[a];U.setChildSize(M,a,b)}};this.setConfig=function(a){var b=E;E=a;r[0].config=E.cols;r[1].config=E.rows;var c;a=0;for(c=b.items.length;a<c;++a){var g=b.items[a];if(g){g.set[0]&&y(g.w,r[0].size,\"\");g.set[1]&&y(g.w,r[1].size,\"\");if(g.layout){L.setChildSize(g.w,0,g.ps[0]); L.setChildSize(g.w,1,g.ps[1])}}}J=true};this.getId=function(){return C};this.setItemsDirty=function(a){var b,c,g=r[0].config.length;b=0;for(c=a.length;b<c;++b){var h=E.items[a[b][0]*g+a[b][1]];h.dirty=true;if(h.layout){h.layout=false;h.wasLayout=true;F.layouts2.setChildLayoutsDirty(L,h.w)}}R=true};this.setDirty=function(){J=true};this.setChildSize=function(a,b,c){var g,h,l=r[0].config.length;g=0;for(h=E.items.length;g<h;++g){var o=E.items[g];if(o&&o.id==a.id){if(!r[b].stretched[b===0?g%l:g/l]){if(!o.ps)o.ps= [];o.ps[b]=c}o.layout=true;K(o);break}}};this.measure=function(a){var b=e.getElement(C);if(b)if(!e.isHidden(b)){if(!Y){Y=true;if(W=I==null){var c=b;c=c.parentNode;for(N=[0,0];;){N[0]+=Q(c,0);N[1]+=Q(c,1);if(c.wtGetPS)Z=true;var g=jQuery.data(c.parentNode,\"layout\");if(g){U=g;M=c;break}c=c;c=c.parentNode;if(c.childNodes.length!=1&&!c.wtGetPS)break}}else{U=jQuery.data(document.getElementById(I),\"layout\");M=b;N[0]=Q(M,0);N[1]=Q(M,1)}}if(R||J)aa(a,b,W?b.parentNode:null);if(a==1)R=J=false}};this.setMaxSize= function(a,b){r[0].maxSize=a;r[1].maxSize=b};this.apply=function(a){var b=e.getElement(C);if(!b)return false;if(e.isHidden(b))return true;ea(a,b);return true};this.contains=function(a){var b=e.getElement(C);a=e.getElement(a.getId());return b&&a?e.contains(b,a):false};this.WT=e}");
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
				"new (function(){var F=[],C=false,I=this,O=false;this.find=function(t){return jQuery.data(document.getElementById(t),\"layout\")};this.setDirty=function(t){(t=jQuery.data(t,\"layout\"))&&t.setDirty()};this.setChildLayoutsDirty=function(t,D){var z,x;z=0;for(x=t.descendants.length;z<x;++z){var w=t.descendants[z];if(D){var B=t.WT.getElement(w.getId());if(!t.WT.contains(D,B))continue}w.setDirty()}};this.add=function(t){function D(z,x){var w,B;w=0;for(B=z.length;w< B;++w){var v=z[w];if(v.getId()==x.getId()){z[w]=x;x.descendants=v.descendants;return}else if(v.contains(x)){D(v.descendants,x);return}else if(x.contains(v)){x.descendants.push(v);z.splice(w,1);--w;--B}}z.push(x)}D(F,t);I.scheduleAdjust()};var P=false;this.scheduleAdjust=function(){if(!P){P=true;setTimeout(function(){I.adjust()},0)}};this.adjust=function(t,D){function z(w,B){var v,H;v=0;for(H=w.length;v<H;++v){var G=w[v];z(G.descendants,B);B==1&&O&&G.setDirty();G.measure(B)}}function x(w,B){var v, H;v=0;for(H=w.length;v<H;++v){var G=w[v];if(G.apply(B))x(G.descendants,B);else{w.splice(v,1);--v;--H}}}if(t){(t=this.find(t))&&t.setItemsDirty(D);I.scheduleAdjust()}else{P=false;if(!C){C=true;z(F,0);x(F,0);z(F,1);x(F,1);O=C=false}}};this.updateConfig=function(t,D){(t=this.find(t))&&t.setConfig(D)};window.onresize=function(){O=true;I.scheduleAdjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(a,e,c){function f(h){var i=b.px(h,\"marginTop\");i+=b.px(h,\"marginBottom\");if(!b.boxSizing(h)){i+=b.px(h,\"borderTopWidth\");i+=b.px(h,\"borderBottomWidth\");i+=b.px(h,\"paddingTop\");i+=b.px(h,\"paddingBottom\")}return i}var b=this;a.style.height=c+\"px\";if(b.boxSizing(a)){c-=b.px(a,\"marginTop\");c-=b.px(a,\"marginBottom\");c-=b.px(a,\"borderTopWidth\");c-=b.px(a,\"borderBottomWidth\");c-=b.px(a,\"paddingTop\");c-=b.px(a,\"paddingBottom\");e-=b.px(a, \"marginLeft\");e-=b.px(a,\"marginRight\");e-=b.px(a,\"borderLeftWidth\");e-=b.px(a,\"borderRightWidth\");e-=b.px(a,\"paddingLeft\");e-=b.px(a,\"paddingRight\")}var g,k,d;g=0;for(k=a.childNodes.length;g<k;++g){d=a.childNodes[g];if(d.nodeType==1){var j=c-f(d);if(j>0)if(d.wtResize)d.wtResize(d,e,j);else{j=j+\"px\";if(d.style.height!=j)d.style.height=j}}}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "ChildrenGetPS",
				"function(a,e,c,f){return f}");
	}

	static WJavaScriptPreamble wtjs12() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastResize",
				"function(a,e,c){var f=this;a.style.height=c+\"px\";a=a.lastChild;var b=a.previousSibling;c-=b.offsetHeight+f.px(b,\"marginTop\")+f.px(b,\"marginBottom\");if(c>0)if(a.wtResize)a.wtResize(a,e,c);else a.style.height=c+\"px\"}");
	}

	static WJavaScriptPreamble wtjs13() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastGetPS",
				"function(a,e,c,f){var b=this,g,k;g=0;for(k=a.childNodes.length;g<k;++g){var d=a.childNodes[g];if(d!=e)if(c===0)f=Math.max(f,d.offsetWidth);else f+=d.offsetHeight+b.px(d,\"marginTop\")+b.px(d,\"marginBottom\")}return f}");
	}
}
