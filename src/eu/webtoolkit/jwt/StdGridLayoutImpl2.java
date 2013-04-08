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
				".layouts2.add(new Wt3_3_0.StdLayout2(").append(
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
				parent.callJavaScript("Wt3_3_0.remove('"
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
		c.setProperty(Property.PropertyStyleVisibility, "hidden");
		return c;
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"StdLayout2",
				"function(B,F,K,N,S,L,u,G,z,x,y){function C(a,b,c){var g=t[b],h=a.style[g.left];A(a,g.left,\"-1000000px\");var j=b?a.scrollHeight:a.scrollWidth,m=b?a.clientHeight:a.clientWidth;b=b?a.offsetHeight:a.offsetWidth;A(a,g.left,h);if(m>=1E6)m-=1E6;if(j>=1E6)j-=1E6;if(b>=1E6)b-=1E6;if(j===0){j=e.pxself(a,g.size);if(j!==0&&!e.isOpera&&!e.isGecko)j-=e.px(a,\"border\"+g.Left+\"Width\")+e.px(a,\"border\"+g.Right+\"Width\")}if(e.isIE&&(e.hasTag(a,\"BUTTON\")||e.hasTag(a, \"TEXTAREA\")||e.hasTag(a,\"INPUT\")||e.hasTag(a,\"SELECT\")))j=m;if(j>b)if(e.pxself(a,g.size)==0)j=0;else{var s=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!=\"none\")s=true});if(s)j=0}if(c)return j;e.isOpera||(j+=e.px(a,\"border\"+g.Left+\"Width\")+e.px(a,\"border\"+g.Right+\"Width\"));j+=e.px(a,\"margin\"+g.Left)+e.px(a,\"margin\"+g.Right);if(!e.boxSizing(a)&&!e.isIE)j+=e.px(a,\"padding\"+g.Left)+e.px(a,\"padding\"+g.Right);if(j<b)j=b;a=e.px(a,\"max\"+g.Size);if(a>0)j=Math.min(a,j);return Math.round(j)} function w(a,b){b=t[b];if(a.style.display==\"none\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=e.px(a,\"min\"+b.Size);e.boxSizing(a)||(c+=e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right));return c}}function H(a,b){b=t[b];var c=e.px(a,\"margin\"+b.Left)+e.px(a,\"margin\"+b.Right);if(!e.boxSizing(a)&&!(e.isIE&&!e.isIElt9&&e.hasTag(a,\"BUTTON\")))c+=e.px(a,\"border\"+b.Left+\"Width\")+e.px(a,\"border\"+b.Right+\"Width\")+e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right);return c} function J(a,b){b=t[b];return e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right)}function X(a,b){if(e.boxSizing(a)){b=t[b];return e.px(a,\"border\"+b.Left+\"Width\")+e.px(a,\"border\"+b.Right+\"Width\")+e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right)}else return 0}function T(a,b){b=t[b];return Math.round(e.px(a,\"border\"+b.Left+\"Width\")+e.px(a,\"border\"+b.Right+\"Width\")+e.px(a,\"margin\"+b.Left)+e.px(a,\"margin\"+b.Right)+e.px(a,\"padding\"+b.Left)+e.px(a,\"padding\"+b.Right))}function O(a,b){U=a.dirty=true;b&& B.layouts2.scheduleAdjust()}function A(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function da(a,b,c){b=t[a];var g=t[a^1],h=b.measures,j=b.config.length,m=g.config.length;if(U||M){if(c&&typeof b.minSize==\"undefined\"){b.minSize=e.px(c,\"min\"+b.Size);if(b.minSize>0)b.minSize-=X(c,a)}h=h.slice();if(h.length==5){h[0]=h[0].slice();h[1]=h[1].slice()}var s=[],d=[],o=0,l=0,i,D,q=false;for(i=0;i<j;++i){var k=0,r=b.config[i][2],p=true;for(D=0;D<m;++D){var f=b.getItem(i,D);if(f){if(!f.w|| a==0&&f.dirty){var n=$(\"#\"+f.id),v=n.get(0);if(v!=f.w){f.w=v;n.find(\"img\").add(n.filter(\"img\")).bind(\"load\",{item:f},function(Z){O(Z.data.item,true)});f.w.style[b.left]=f.w.style[g.left]=\"-1000000px\"}}if(!L&&f.w.style.position!=\"absolute\"){f.w.style.position=\"absolute\";f.w.style.visibility=\"hidden\";if(!f.w.wtResize&&!e.isIE){f.w.style.boxSizing=\"border-box\";if(n=e.cssPrefix(\"BoxSizing\"))f.w.style[n+\"BoxSizing\"]=\"border-box\"}}if(!f.ps)f.ps=[];if(!f.ms)f.ms=[];if(!f.size)f.size=[];if(!f.psize)f.psize= [];if(!f.fs)f.fs=[];if($(f.w).hasClass(\"Wt-hidden\"))f.ps[a]=f.ms[a]=0;else{n=!f.set;if(!f.set)f.set=[false,false];if(f.w){if(e.isIE)f.w.style.visibility=\"\";if(f.dirty||M){v=w(f.w,a);if(v>r)r=v;f.ms[a]=v;if(!f.set[a])if(a==0||!n){n=e.pxself(f.w,b.size);f.fs[a]=n?n+H(f.w,a):0}else{n=Math.round(e.px(f.w,b.size));f.fs[a]=n>Math.max(X(f.w,a),v)?n+H(f.w,a):0}n=f.fs[a];if(f.layout){if(n==0)n=f.ps[a]}else{if(f.wasLayout){f.wasLayout=false;f.set=[false,false];f.ps-=0.1;f.w.wtResize&&f.w.wtResize(f.w,-1,-1, true);A(f.w,t[1].size,\"\")}v=C(f.w,a,false);var E=f.set[a];if(E)if(f.psize[a]>8)E=v>=f.psize[a]-2&&v<=f.psize[a]+2;var V=typeof f.ps[a]!==\"undefined\"&&b.config[i][0]>0&&f.set[a];n=E||V?Math.max(n,f.ps[a]):Math.max(n,v)}f.ps[a]=n;if(!f.span||f.span[a]==1){if(n>k)k=n}else q=true}else if(!f.span||f.span[a]==1){if(f.ps[a]>k)k=f.ps[a];if(f.ms[a]>r)r=f.ms[a]}else q=true;if((f.w.style.display!==\"none\"||e.hasTag(f.w,\"TEXTAREA\")&&f.w.wtResize)&&(!f.span||f.span[a]==1))p=false}}}}if(p)r=k=-1;else if(r>k)k=r; s[i]=k;d[i]=r;if(r>-1){o+=k;l+=r}}if(q)for(i=0;i<j;++i)for(D=0;D<m;++D)if((f=b.getItem(i,D))&&f.span&&f.span[a]>1){g=f.ps[a];for(si=k=q=0;si<f.span[a];++si){r=s[i+si];if(r!=-1){g-=r;++q;if(b.config[i+si][0]>0)k+=b.config[i+si][0]}}if(g>0)if(q>0){if(k>0)q=k;for(si=0;si<f.span[a];++si){r=s[i+si];if(r!=-1){r=k>0?b.config[i+si][0]:1;if(r>0){p=Math.round(g/r);g-=p;q-=r;s[i+si]+=p}}}}else s[i]=g}m=0;n=true;D=false;for(i=0;i<j;++i)if(d[i]>-1){if(n){m+=b.margins[1];n=false}else{m+=b.margins[0];if(D)m+=4}D= b.config[i][1]!==0}n||(m+=b.margins[2]);o+=m;l+=m;b.measures=[s,d,o,l,m];if(M||h[2]!=b.measures[2])P.updateSizeInParent(a);c&&b.minSize==0&&h[3]!=b.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&&A(c,\"min\"+b.Size,b.measures[3]+\"px\")&&P.ancestor&&P.ancestor.setContentsDirty(c);c&&a==0&&c&&e.hasTag(c,\"TD\")&&A(c,b.size,b.measures[2]+\"px\")}}function ea(a,b,c){a=t[a];if(W)c=-c;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;c=-c}a.fixedSize[b]=a.sizes[b]+c;B.layouts2.scheduleAdjust()}function fa(a,b, c){var g=b.di,h=t[a],j=t[a^1],m,s=e.getElement(F),d;for(d=g-1;d>=0;--d)if(h.sizes[d]>=0){m=-(h.sizes[d]-h.measures[1][d]);break}g=h.sizes[g]-h.measures[1][g];if(W){var o=m;m=-g;g=-o}new e.SizeHandle(e,h.resizeDir,e.pxself(b,h.size),e.pxself(b,j.size),m,g,h.resizerClass,function(l){ea(a,d,l)},b,s,c,0,0)}function ga(a,b){var c=t[a],g=t[a^1],h=c.measures,j=0,m=false,s=false,d=false,o=aa?b.parentNode:null;if(c.maxSize===0)if(o){var l=e.css(o,\"position\");if(l===\"absolute\")j=e.pxself(o,c.size);if(j===0){if(!c.initialized){if(a=== 0&&(l===\"absolute\"||l===\"fixed\")){o.style.display=\"none\";j=o.clientWidth;o.style.display=\"\"}j=a?o.clientHeight:o.clientWidth;m=true;if(a==0&&j==0&&e.isIElt9){j=o.offsetWidth;m=false}var i;if((e.hasTag(o,\"TD\")||e.hasTag(o,\"TH\"))&&!(e.isIE&&!e.isIElt9)){d=0;i=1}else{d=c.minSize?c.minSize:h[3];i=0}function D(V,Z){return Math.abs(V-Z)<1}if(e.isIElt9&&D(j,i)||D(j,d+J(o,a)))c.maxSize=999999}if(j===0&&c.maxSize===0){j=a?o.clientHeight:o.clientWidth;m=true}}}else{j=e.pxself(b,c.size);s=true}else if(c.sizeSet){j= e.pxself(o,c.size);s=true}var q=0;if(o&&o.wtGetPS&&a==1)q=o.wtGetPS(o,b,a,0);d=h[2];if(d<c.minSize)d=c.minSize;if(c.maxSize&&!c.sizeSet)if(d+q<c.maxSize){A(o,c.size,d+q+X(o,a)+\"px\")&&B.layouts2.remeasure();j=d+q;d=s=true}else{j=c.maxSize;m=false}c.cSize=j;if(a==1&&o&&o.wtResize){i=g.cSize;d=c.cSize;o.wtResize(o,Math.round(i),Math.round(d),true)}j-=q;if(!s){s=0;if(typeof c.cPadding===\"undefined\"){s=m?J(o,a):X(o,a);c.cPadding=s}else s=c.cPadding;j-=s}c.initialized=true;if(!(o&&j<=0)){if(j<h[3]-q)j= h[3]-q;m=[];o=c.config.length;s=g.config.length;for(d=0;d<o;++d)c.stretched[d]=false;if(j>=h[3]-q){q=j-h[4];i=[];var k=[0,0],r=[0,0],p=0;for(d=0;d<o;++d)if(h[1][d]>-1){l=-1;if(typeof c.fixedSize[d]!==\"undefined\"&&(d+1==o||h[1][d+1]>-1))l=c.fixedSize[d];else if(c.config[d][1]!==0&&c.config[d][1][0]>=0){l=c.config[d][1][0];if(c.config[d][1][1])l=(j-h[4])*l/100}if(l>=0){i[d]=-1;m[d]=l;q-=m[d]}else{if(c.config[d][0]>0){l=1;i[d]=c.config[d][0];p+=i[d]}else{l=0;i[d]=0}k[l]+=h[1][d];r[l]+=h[0][d];m[d]=h[0][d]}}else{i[d]= -2;m[d]=-1}if(p==0){for(d=0;d<o;++d)if(i[d]==0){i[d]=1;++p}r[1]=r[0];k[1]=k[0];r[0]=0;k[0]=0}if(q>r[0]+k[1]){q-=r[0];if(q>r[1]){if(c.fitSize){q-=r[1];q=q/p;for(d=0;d<o;++d)if(i[d]>0){m[d]+=Math.round(i[d]*q);c.stretched[d]=true}}}else{l=1;if(q<k[l])q=k[l];q=r[l]-k[l]>0?(q-k[l])/(r[l]-k[l]):0;for(d=0;d<o;++d)if(i[d]>0){k=h[0][d]-h[1][d];m[d]=h[1][d]+Math.round(k*q)}}}else{for(d=0;d<o;++d)if(i[d]>0)m[d]=h[1][d];q-=k[1];l=0;if(q<k[l])q=k[l];q=r[l]-k[l]>0?(q-k[l])/(r[l]-k[l]):0;for(d=0;d<o;++d)if(i[d]== 0){k=h[0][d]-h[1][d];m[d]=h[1][d]+Math.round(k*q)}}}else m=h[1];c.sizes=m;h=c.margins[1];q=true;k=false;for(d=0;d<o;++d){if(m[d]>-1){var f=k;if(k){i=F+\"-rs\"+a+\"-\"+d;k=e.getElement(i);if(!k){k=document.createElement(\"div\");k.setAttribute(\"id\",i);k.di=d;k.style.position=\"absolute\";k.style[g.left]=g.margins[1]+\"px\";k.style[c.size]=c.margins[0]+\"px\";if(g.cSize)k.style[g.size]=g.cSize-g.margins[2]-g.margins[1]+\"px\";k.className=c.handleClass;b.insertBefore(k,b.firstChild);k.onmousedown=k.ontouchstart=function(V){fa(a, this,V||window.event)}}h+=2;A(k,c.left,h+\"px\");h+=2}k=c.config[d][1]!==0;if(q)q=false;else h+=c.margins[0]}for(r=0;r<s;++r)if((p=c.getItem(d,r))&&p.w){i=p.w;l=Math.max(m[d],0);if(p.span){var n,v=k;for(n=1;n<p.span[a];++n){if(d+n>=m.length)break;if(v)l+=4;v=c.config[d+n][1]!==0;if(m[d+n-1]>-1&&m[d+n]>-1)l+=c.margins[0];l+=m[d+n]}}var E;A(i,\"visibility\",\"\");v=p.align>>c.alignBits&15;n=p.ps[a];if(l<n)v=0;if(v){switch(v){case 1:E=h;break;case 4:E=h+(l-n)/2;break;case 2:E=h+(l-n);break}n-=H(p.w,a);if(p.layout){A(i, c.size,n+\"px\")&&O(p);p.set[a]=true}else if(l>=n&&p.set[a]){A(i,c.size,n+\"px\")&&O(p);p.set[a]=false}p.size[a]=n;p.psize[a]=n}else{E=H(p.w,a);v=Math.max(0,l-E);if(!e.isIE&&e.hasTag(i,\"TEXTAREA\"))v=l;E=false;if(e.isIE&&e.hasTag(i,\"BUTTON\"))E=true;if(E||l!=n||p.layout){A(i,c.size,v+\"px\")&&O(p);p.set[a]=true}else if(p.fs[a])a==0&&A(i,c.size,p.fs[a]+\"px\");else{A(i,c.size,\"\")&&O(p);p.set[a]=false}E=h;p.size[a]=v;p.psize[a]=l}if(L)if(f){A(i,c.left,\"4px\");l=e.css(i,\"position\");if(l!==\"absolute\")i.style.position= \"relative\"}else A(i,c.left,\"0px\");else A(i,c.left,E+\"px\");if(a==1){if(i.wtResize)i.wtResize(i,p.set[0]?Math.round(p.size[0]):-1,p.set[1]?Math.round(p.size[1]):-1,true);p.dirty=false}}if(m[d]>-1)h+=m[d]}$(b).children(\".\"+g.handleClass).css(c.size,j-c.margins[2]-c.margins[1]+\"px\")}}var e=B.WT;this.ancestor=null;this.descendants=[];var P=this,I=y,U=false,M=true,aa=false,Y=null,Q=null,ba=false,R=[],ca=false,W=$(document.body).hasClass(\"Wt-rtl\"),t=[{initialized:false,config:I.cols,margins:z,maxSize:u, measures:[],sizes:[],stretched:[],fixedSize:[],Left:W?\"Right\":\"Left\",left:W?\"right\":\"left\",Right:W?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return I.items[b*t[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:N},{initialized:false,config:I.rows,margins:x,maxSize:G,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,b){return I.items[a*t[0].config.length+ b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:S}];jQuery.data(document.getElementById(F),\"layout\",this);this.updateSizeInParent=function(a){if(Y){var b=t[a],c=b.measures[2];if(b.maxSize>0)c=Math.min(b.maxSize,c);if(ca){b=e.getElement(F);if(!b)return;for(var g=b,h=g.parentNode;;){if(h.wtGetPS)c=h.wtGetPS(h,g,a,c);c+=T(h,a);if(h==Q)break;if(h==b.parentNode&&!h.lh&&h.offsetHeight>c)c=h.offsetHeight;g=h;h=g.parentNode}}else c+=R[a];Y.setChildSize(Q,a,c)}};this.setConfig=function(a){var b= I;I=a;t[0].config=I.cols;t[1].config=I.rows;t[0].stretched=[];t[1].stretched=[];var c;a=0;for(c=b.items.length;a<c;++a){var g=b.items[a];if(g){if(g.set){g.set[0]&&A(g.w,t[0].size,\"\");g.set[1]&&A(g.w,t[1].size,\"\")}if(g.layout){P.setChildSize(g.w,0,g.ps[0]);P.setChildSize(g.w,1,g.ps[1])}}}M=true;B.layouts2.scheduleAdjust()};this.getId=function(){return F};this.setItemsDirty=function(a){var b,c,g=t[0].config.length;b=0;for(c=a.length;b<c;++b){var h=I.items[a[b][0]*g+a[b][1]];h.dirty=true;if(h.layout){h.layout= false;h.wasLayout=true;B.layouts2.setChildLayoutsDirty(P,h.w)}}U=true};this.setDirty=function(){M=true};this.setChildSize=function(a,b,c){var g=t[0].config.length,h=t[b],j,m;j=0;for(m=I.items.length;j<m;++j){var s=I.items[j];if(s&&s.id==a.id){a=b===0?j%g:j/g;if(s.align>>h.alignBits&15||!h.stretched[a]){if(!s.ps)s.ps=[];s.ps[b]=c}s.layout=true;O(s);break}}};this.measure=function(a){var b=e.getElement(F);if(b)if(!e.isHidden(b)){if(!ba){ba=true;if(aa=K==null){var c=b;c=c.parentNode;for(R=[0,0];;){R[0]+= T(c,0);R[1]+=T(c,1);if(c.wtGetPS)ca=true;var g=jQuery.data(c.parentNode,\"layout\");if(g){Y=g;Q=c;break}c=c;c=c.parentNode;if(c.childNodes.length!=1&&!c.wtGetPS)break}c=b.parentNode;for(g=0;g<2;++g)t[g].sizeSet=e.pxself(c,t[g].size)!=0}else{Y=jQuery.data(document.getElementById(K),\"layout\");Q=b;R[0]=T(Q,0);R[1]=T(Q,1)}}if(U||M){c=aa?b.parentNode:null;da(a,b,c)}if(a==1)U=M=false}};this.setMaxSize=function(a,b){t[0].maxSize=a;t[1].maxSize=b};this.apply=function(a){var b=e.getElement(F);if(!b)return false; if(e.isHidden(b))return true;ga(a,b);return true};this.contains=function(a){var b=e.getElement(F);a=e.getElement(a.getId());return b&&a?e.contains(b,a):false};this.WT=e}");
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
				"new (function(){var B=[],F=false,K=this,N=false;this.find=function(u){return jQuery.data(document.getElementById(u),\"layout\")};this.setDirty=function(u){(u=jQuery.data(u,\"layout\"))&&u.setDirty()};this.setChildLayoutsDirty=function(u,G){var z,x;z=0;for(x=u.descendants.length;z<x;++z){var y=u.descendants[z];if(G){var C=u.WT.getElement(y.getId());if(C&&!u.WT.contains(G,C))continue}y.setDirty()}};this.add=function(u){function G(z,x){var y,C;y=0;for(C= z.length;y<C;++y){var w=z[y];if(w.getId()==x.getId()){z[y]=x;x.descendants=w.descendants;return}else if(w.contains(x)){G(w.descendants,x);return}else if(x.contains(w)){x.descendants.push(w);z.splice(y,1);--y;--C}}z.push(x)}G(B,u);K.scheduleAdjust()};var S=false,L=false;this.scheduleAdjust=function(u){if(u)N=true;if(!S){S=true;setTimeout(function(){K.adjust()},0)}};this.adjust=function(u,G){function z(y,C){var w,H;w=0;for(H=y.length;w<H;++w){var J=y[w];z(J.descendants,C);C==1&&N&&J.setDirty();J.measure(C)}} function x(y,C){var w,H;w=0;for(H=y.length;w<H;++w){var J=y[w];if(J.apply(C))x(J.descendants,C);else{y.splice(w,1);--w;--H}}}if(u){(u=this.find(u))&&u.setItemsDirty(G);K.scheduleAdjust()}else{S=false;if(!F){F=true;L=false;z(B,0);x(B,0);z(B,1);x(B,1);if(L){z(B,0);x(B,0);z(B,1);x(B,1)}N=L=F=false}}};this.updateConfig=function(u,G){(u=this.find(u))&&u.setConfig(G)};this.remeasure=function(){L=true};window.onresize=function(){K.scheduleAdjust(true)};window.onshow=function(){N=true;K.adjust()}})");
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
