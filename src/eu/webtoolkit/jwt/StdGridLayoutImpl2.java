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
				"function(z,C,J,M,R,K,s,E,x,v,w){function A(a,b){var d=q[b],g=b?a.scrollHeight:a.scrollWidth,h=b?a.clientHeight:a.clientWidth;b=b?a.offsetHeight:a.offsetWidth;if(h>=1E6)h-=1E6;if(g>=1E6)g-=1E6;if(b>=1E6)b-=1E6;if(g===0)g=f.pxself(a,d.size);if(f.isIE&&(f.hasTag(a,\"BUTTON\")||f.hasTag(a,\"TEXTAREA\")||f.hasTag(a,\"INPUT\")||f.hasTag(a,\"SELECT\")))g=h;if(g>b)if(f.pxself(a,d.size)==0)g=0;else{var o=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!= \"none\")o=true});if(o)g=0}if(!f.isOpera&&!f.isGecko)g+=f.px(a,\"border\"+d.Left+\"Width\")+f.px(a,\"border\"+d.Right+\"Width\");g+=f.px(a,\"margin\"+d.Left)+f.px(a,\"margin\"+d.Right);if(!f.boxSizing(a)&&!f.isIE)g+=f.px(a,\"padding\"+d.Left)+f.px(a,\"padding\"+d.Right);if(g<b)g=b;return Math.round(g)}function u(a,b){b=q[b];if(a.style.display==\"none\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var d=f.px(a,\"min\"+b.Size);f.boxSizing(a)||(d+=f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)); return d}}function H(a,b){b=q[b];var d=f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right);if(!f.boxSizing(a)&&!(f.isIE&&!f.isIElt9&&f.hasTag(a,\"BUTTON\")))d+=f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right);return d}function I(a,b){b=q[b];return f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}function V(a,b){if(f.boxSizing(a)){b=q[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a, \"padding\"+b.Right)}else return 0}function S(a,b){b=q[b];return Math.round(f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right)+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right))}function N(a,b){T=a.dirty=true;b&&z.layouts2.scheduleAdjust()}function y(a,b,d){if(a.style[b]!==d){a.style[b]=d;return true}else return false}function ba(a,b,d){b=q[a];var g=q[a^1],h=b.measures,o=b.config.length,m=g.config.length;if(T||L){if(d&&typeof b.minSize== \"undefined\"){b.minSize=f.px(d,\"min\"+b.Size);if(b.minSize>0)b.minSize-=V(d,a)}h=h.slice();if(h.length==5){h[0]=h[0].slice();h[1]=h[1].slice()}var B=[],e=[],j=0,t=0,i,n;for(i=0;i<o;++i){var p=0,l=b.config[i][2],F=true;for(n=0;n<m;++n){var c=b.getItem(i,n);if(c){if(!c.w||a==0&&c.dirty){var k=$(\"#\"+c.id),r=k.get(0);if(r!=c.w){c.w=r;k.find(\"img\").add(k.filter(\"img\")).bind(\"load\",{item:c},function(X){N(X.data.item,true)});c.w.style[b.left]=c.w.style[g.left]=\"-1000000px\"}}if(!K&&c.w.style.position!=\"absolute\"){c.w.style.position= \"absolute\";c.w.style.visibility=\"hidden\";if(!c.w.wtResize&&!f.isIElt9){c.w.style.boxSizing=\"border-box\";if(k=f.cssPrefix(\"BoxSizing\"))c.w.style[k+\"BoxSizing\"]=\"border-box\"}}if(!c.ps)c.ps=[];if(!c.ms)c.ms=[];if(!c.size)c.size=[];if(!c.psize)c.psize=[];if(!c.fs)c.fs=[];if($(c.w).hasClass(\"Wt-hidden\"))c.ps[a]=c.ms[a]=0;else{k=!c.set;if(!c.set)c.set=[false,false];if(c.w){if(f.isIE)c.w.style.visibility=\"\";if(c.dirty||L){r=u(c.w,a);if(r>l)l=r;c.ms[a]=r;if(!c.set[a])if(a==0||!k){k=f.pxself(c.w,b.size);c.fs[a]= k?k+H(c.w,a):0}else{k=Math.round(f.px(c.w,b.size));c.fs[a]=k>Math.max(V(c.w,a),r)?k+H(c.w,a):0}k=c.fs[a];if(c.layout){if(k==0)k=c.ps[a]}else{if(c.wasLayout){c.wasLayout=false;c.set=[false,false];y(c.w,q[0].size,\"\");y(c.w,q[1].size,\"\")}r=A(c.w,a);var D=typeof c.ps[a]!==\"undefined\"&&b.config[i][0]>0&&c.set[a];k=c.set[a]&&r>=c.psize[a]-4&&r<=c.psize[a]+4||D?Math.max(k,c.ps[a]):Math.max(k,r)}c.ps[a]=k;if(!c.span||c.span[a]==1)if(k>p)p=k}else if(!c.span||c.span[a]==1){if(c.ps[a]>p)p=c.ps[a];if(c.ms[a]> l)l=c.ms[a]}if(c.w.style.display!==\"none\"||f.hasTag(c.w,\"TEXTAREA\")&&c.w.wtResize)F=false}}}}if(F)l=p=-1;else if(l>p)p=l;B[i]=p;e[i]=l;if(l>-1){j+=p;t+=l}}g=0;k=true;m=false;for(i=0;i<o;++i)if(e[i]>-1){if(k){g+=b.margins[1];k=false}else{g+=b.margins[0];if(m)g+=4}m=b.config[i][1]!==0}k||(g+=b.margins[2]);j+=g;t+=g;b.measures=[B,e,j,t,g];if(L||h[2]!=b.measures[2])O.updateSizeInParent(a);d&&b.minSize==0&&h[3]!=b.measures[3]&&d.parentNode.className!=\"Wt-domRoot\"&&y(d,\"min\"+b.Size,b.measures[3]+\"px\")&& O.ancestor&&O.ancestor.setContentsDirty(d);d&&a==0&&d&&f.hasTag(d,\"TD\")&&y(d,b.size,b.measures[2]+\"px\")}}function ca(a,b,d){a=q[a];if(U)d=-d;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;d=-d}a.fixedSize[b]=a.sizes[b]+d;z.layouts2.scheduleAdjust()}function da(a,b,d){var g=b.di,h=q[a],o=q[a^1],m,B=f.getElement(C),e;for(e=g-1;e>=0;--e)if(h.sizes[e]>=0){m=-(h.sizes[e]-h.measures[1][e]);break}g=h.sizes[g]-h.measures[1][g];if(U){var j=m;m=-g;g=-j}new f.SizeHandle(f,h.resizeDir,f.pxself(b,h.size),f.pxself(b, o.size),m,g,h.resizerClass,function(t){ca(a,e,t)},b,B,d,0,0)}function ea(a,b){var d=q[a],g=q[a^1],h=d.measures,o=0,m=false,B=false,e=false,j=Y?b.parentNode:null;if(d.maxSize===0)if(j){var t=f.css(j,\"position\");if(t===\"absolute\")o=f.pxself(j,d.size);if(o===0){if(!d.initialized){if(a===0&&(t===\"absolute\"||t===\"fixed\")){j.style.display=\"none\";o=j.clientWidth;j.style.display=\"\"}o=a?j.clientHeight:j.clientWidth;m=true;if(a==0&&o==0&&f.isIElt9){o=j.offsetWidth;m=false}var i;if((f.hasTag(j,\"TD\")||f.hasTag(j, \"TH\"))&&!(f.isIE&&!f.isIElt9)){e=0;i=1}else{e=d.minSize?d.minSize:h[3];i=0}if(f.isIElt9&&o==i||o==e+I(j,a))d.maxSize=999999}if(o===0&&d.maxSize===0){o=a?j.clientHeight:j.clientWidth;m=true}}}else{o=f.pxself(b,d.size);B=true}var n=0;if(j&&j.wtGetPS&&a==1)n=j.wtGetPS(j,b,a,0);e=h[2];if(e<d.minSize)e=d.minSize;if(d.maxSize)if(e+n<d.maxSize){y(j,d.size,e+n+V(j,a)+\"px\")&&z.layouts2.remeasure();o=e+n;e=B=true}else{o=d.maxSize;m=false}d.cSize=o;if(a==1&&j&&j.wtResize){i=g.cSize;e=d.cSize;j.wtResize(j,Math.round(i), Math.round(e))}o-=n;if(!B){B=0;if(typeof d.cPadding===\"undefined\"){B=m?I(j,a):V(j,a);d.cPadding=B}else B=d.cPadding;o-=B}d.initialized=true;if(!(j&&o<=0)){if(o<h[3]-n)o=h[3]-n;m=[];j=d.config.length;B=g.config.length;for(e=0;e<j;++e)d.stretched[e]=false;if(o>=h[3]-n){n=o-h[4];i=[];var p=[0,0],l=[0,0],F=0;for(e=0;e<j;++e)if(h[1][e]>-1){var c=-1;if(typeof d.fixedSize[e]!==\"undefined\")c=d.fixedSize[e];else if(d.config[e][1]!==0&&d.config[e][1][0]>=0){c=d.config[e][1][0];if(d.config[e][1][1])c=(o-h[4])* c/100}if(c>=0){i[e]=-1;m[e]=c;n-=m[e]}else{if(d.config[e][0]>0){c=1;i[e]=d.config[e][0];F+=i[e]}else{c=0;i[e]=0}p[c]+=h[1][e];l[c]+=h[0][e];m[e]=h[0][e]}}else i[e]=-2;if(F==0){for(e=0;e<j;++e)if(i[e]==0){i[e]=1;++F}l[1]=l[0];p[1]=p[0];l[0]=0;p[0]=0}if(n>l[0]+p[1]){n-=l[0];if(n>l[1]){if(d.fitSize){n-=l[1];n=n/F;for(e=0;e<j;++e)if(i[e]>0){m[e]+=Math.round(i[e]*n);d.stretched[e]=true}}}else{c=1;if(n<p[c])n=p[c];n=l[c]-p[c]>0?(n-p[c])/(l[c]-p[c]):0;for(e=0;e<j;++e)if(i[e]>0){p=h[0][e]-h[1][e];m[e]=h[1][e]+ Math.round(p*n)}}}else{for(e=0;e<j;++e)if(i[e]>0)m[e]=h[1][e];n-=p[1];c=0;if(n<p[c])n=p[c];n=l[c]-p[c]>0?(n-p[c])/(l[c]-p[c]):0;for(e=0;e<j;++e)if(i[e]==0){p=h[0][e]-h[1][e];m[e]=h[1][e]+Math.round(p*n)}}}else m=h[1];d.sizes=m;h=0;n=true;l=false;for(e=0;e<j;++e)if(m[e]>-1){if(p=l){i=C+\"-rs\"+a+\"-\"+e;l=f.getElement(i);if(!l){l=document.createElement(\"div\");l.setAttribute(\"id\",i);l.di=e;l.style.position=\"absolute\";l.style[g.left]=g.margins[1]+\"px\";l.style[d.size]=d.margins[0]+\"px\";l.className=d.handleClass; b.insertBefore(l,b.firstChild);l.onmousedown=l.ontouchstart=function(X){da(a,this,X||window.event)}}h+=2;y(l,d.left,h+\"px\");h+=2}l=d.config[e][1]!==0;if(n){h+=d.margins[1];n=false}else h+=d.margins[0];for(F=0;F<B;++F)if((c=d.getItem(e,F))&&c.w){i=c.w;t=m[e];if(c.span){var k,r=l;for(k=1;k<c.span[a];++k){if(r)t+=4;r=d.config[e+r][1]!==0;t+=d.margins[0];t+=m[e+k]}}var D;y(i,\"visibility\",\"\");r=c.align>>d.alignBits&15;k=c.ps[a];if(t<k)r=0;if(r){switch(r){case 1:D=h;break;case 4:D=h+(t-k)/2;break;case 2:D= h+(t-k);break}if(c.layout){y(i,d.size,k+\"px\")&&N(c);c.set[a]=true}else if(t>=k&&c.set[a]){y(i,d.size,k+\"px\")&&N(c);c.set[a]=false}c.size[a]=k;c.psize[a]=k}else{D=H(c.w,a);r=t;if(f.isIElt9||!f.hasTag(i,\"BUTTON\")&&!f.hasTag(i,\"INPUT\")&&!f.hasTag(i,\"SELECT\")&&!f.hasTag(i,\"TEXTAREA\"))r=Math.max(0,r-D);D=false;if(f.isIE&&f.hasTag(i,\"BUTTON\"))D=true;if(D||t!=k||c.layout){y(i,d.size,r+\"px\")&&N(c);c.set[a]=true}else if(c.fs[a])a==0&&y(i,d.size,c.fs[a]+\"px\");else{y(i,d.size,\"\")&&N(c);c.set[a]=false}D=h;c.size[a]= r;c.psize[a]=t}if(K)if(p){y(i,d.left,\"4px\");t=f.css(i,\"position\");if(t!==\"absolute\")i.style.position=\"relative\"}else y(i,d.left,\"0px\");else y(i,d.left,D+\"px\");if(a==1){i.wtResize&&i.wtResize(i,Math.round(c.size[0]),Math.round(c.size[1]),c);c.dirty=false}}h+=m[e]}$(b).children(\".\"+g.handleClass).css(d.size,o-d.margins[2]-d.margins[1]+\"px\")}}var f=z.WT;this.ancestor=null;this.descendants=[];var O=this,G=w,T=false,L=true,Y=false,W=null,P=null,Z=false,Q=[],aa=false,U=$(document.body).hasClass(\"Wt-rtl\"), q=[{initialized:false,config:G.cols,margins:x,maxSize:s,measures:[],sizes:[],stretched:[],fixedSize:[],Left:U?\"Right\":\"Left\",left:U?\"right\":\"left\",Right:U?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return G.items[b*q[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:M},{initialized:false,config:G.rows,margins:v,maxSize:E,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\", alignBits:4,getItem:function(a,b){return G.items[a*q[0].config.length+b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:R}];jQuery.data(document.getElementById(C),\"layout\",this);this.updateSizeInParent=function(a){if(W){var b=q[a].measures[2];if(aa){var d=f.getElement(C);if(!d)return;d=d;for(var g=d.parentNode;;){if(g.wtGetPS)b=g.wtGetPS(g,d,a,b);b+=S(g,a);if(g==P)break;d=g;g=d.parentNode}}else b+=Q[a];W.setChildSize(P,a,b)}};this.setConfig=function(a){var b=G;G=a;q[0].config= G.cols;q[1].config=G.rows;q[0].stretched=[];q[1].stretched=[];var d;a=0;for(d=b.items.length;a<d;++a){var g=b.items[a];if(g){if(g.set){g.set[0]&&y(g.w,q[0].size,\"\");g.set[1]&&y(g.w,q[1].size,\"\")}if(g.layout){O.setChildSize(g.w,0,g.ps[0]);O.setChildSize(g.w,1,g.ps[1])}}}L=true;z.layouts2.scheduleAdjust()};this.getId=function(){return C};this.setItemsDirty=function(a){var b,d,g=q[0].config.length;b=0;for(d=a.length;b<d;++b){var h=G.items[a[b][0]*g+a[b][1]];h.dirty=true;if(h.layout){h.layout=false;h.wasLayout= true;z.layouts2.setChildLayoutsDirty(O,h.w)}}T=true};this.setDirty=function(){L=true};this.setChildSize=function(a,b,d){var g,h,o=q[0].config.length;g=0;for(h=G.items.length;g<h;++g){var m=G.items[g];if(m&&m.id==a.id){if(!q[b].stretched[b===0?g%o:g/o]){if(!m.ps)m.ps=[];m.ps[b]=d}m.layout=true;N(m);break}}};this.measure=function(a){var b=f.getElement(C);if(b)if(!f.isHidden(b)){if(!Z){Z=true;if(Y=J==null){var d=b;d=d.parentNode;for(Q=[0,0];;){Q[0]+=S(d,0);Q[1]+=S(d,1);if(d.wtGetPS)aa=true;var g=jQuery.data(d.parentNode, \"layout\");if(g){W=g;P=d;break}d=d;d=d.parentNode;if(d.childNodes.length!=1&&!d.wtGetPS)break}}else{W=jQuery.data(document.getElementById(J),\"layout\");P=b;Q[0]=S(P,0);Q[1]=S(P,1)}}if(T||L)ba(a,b,Y?b.parentNode:null);if(a==1)T=L=false}};this.setMaxSize=function(a,b){q[0].maxSize=a;q[1].maxSize=b};this.apply=function(a){var b=f.getElement(C);if(!b)return false;if(f.isHidden(b))return true;ea(a,b);return true};this.contains=function(a){var b=f.getElement(C);a=f.getElement(a.getId());return b&&a?f.contains(b, a):false};this.WT=f}");
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
				"new (function(){var z=[],C=false,J=this,M=false;this.find=function(s){return jQuery.data(document.getElementById(s),\"layout\")};this.setDirty=function(s){(s=jQuery.data(s,\"layout\"))&&s.setDirty()};this.setChildLayoutsDirty=function(s,E){var x,v;x=0;for(v=s.descendants.length;x<v;++x){var w=s.descendants[x];if(E){var A=s.WT.getElement(w.getId());if(A&&!s.WT.contains(E,A))continue}w.setDirty()}};this.add=function(s){function E(x,v){var w,A;w=0;for(A= x.length;w<A;++w){var u=x[w];if(u.getId()==v.getId()){x[w]=v;v.descendants=u.descendants;return}else if(u.contains(v)){E(u.descendants,v);return}else if(v.contains(u)){v.descendants.push(u);x.splice(w,1);--w;--A}}x.push(v)}E(z,s);J.scheduleAdjust()};var R=false,K=false;this.scheduleAdjust=function(){if(!R){R=true;setTimeout(function(){J.adjust()},0)}};this.adjust=function(s,E){function x(w,A){var u,H;u=0;for(H=w.length;u<H;++u){var I=w[u];x(I.descendants,A);A==1&&M&&I.setDirty();I.measure(A)}}function v(w, A){var u,H;u=0;for(H=w.length;u<H;++u){var I=w[u];if(I.apply(A))v(I.descendants,A);else{w.splice(u,1);--u;--H}}}if(s){(s=this.find(s))&&s.setItemsDirty(E);J.scheduleAdjust()}else{R=false;if(!C){C=true;K=false;x(z,0);v(z,0);x(z,1);v(z,1);if(K){x(z,0);v(z,0);x(z,1);v(z,1)}M=K=C=false}}};this.updateConfig=function(s,E){(s=this.find(s))&&s.setConfig(E)};this.remeasure=function(){K=true};window.onresize=function(){M=true;J.scheduleAdjust()};window.onshow=function(){M=true;J.adjust()}})");
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
