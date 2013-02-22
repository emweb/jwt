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
				"function(z,D,J,N,S,K,r,E,y,w,x){function B(a,b,d){var h=p[b],f=b?a.scrollHeight:a.scrollWidth,n=b?a.clientHeight:a.clientWidth;b=b?a.offsetHeight:a.offsetWidth;if(n>=1E6)n-=1E6;if(f>=1E6)f-=1E6;if(b>=1E6)b-=1E6;if(f===0){f=g.pxself(a,h.size);if(f!==0&&!g.isOpera&&!g.isGecko)f-=g.px(a,\"border\"+h.Left+\"Width\")+g.px(a,\"border\"+h.Right+\"Width\")}if(g.isIE&&(g.hasTag(a,\"BUTTON\")||g.hasTag(a,\"TEXTAREA\")||g.hasTag(a,\"INPUT\")||g.hasTag(a,\"SELECT\")))f= n;if(f>b)if(g.pxself(a,h.size)==0)f=0;else{var o=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!=\"none\")o=true});if(o)f=0}if(d)return f;g.isOpera||(f+=g.px(a,\"border\"+h.Left+\"Width\")+g.px(a,\"border\"+h.Right+\"Width\"));f+=g.px(a,\"margin\"+h.Left)+g.px(a,\"margin\"+h.Right);if(!g.boxSizing(a)&&!g.isIE)f+=g.px(a,\"padding\"+h.Left)+g.px(a,\"padding\"+h.Right);if(f<b)f=b;a=g.px(a,\"max\"+h.Size);if(a>0)f=Math.min(a,f);return Math.round(f)}function v(a,b){b=p[b];if(a.style.display==\"none\")return 0; else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var d=g.px(a,\"min\"+b.Size);g.boxSizing(a)||(d+=g.px(a,\"padding\"+b.Left)+g.px(a,\"padding\"+b.Right));return d}}function F(a,b){b=p[b];var d=g.px(a,\"margin\"+b.Left)+g.px(a,\"margin\"+b.Right);if(!g.boxSizing(a)&&!(g.isIE&&!g.isIElt9&&g.hasTag(a,\"BUTTON\")))d+=g.px(a,\"border\"+b.Left+\"Width\")+g.px(a,\"border\"+b.Right+\"Width\")+g.px(a,\"padding\"+b.Left)+g.px(a,\"padding\"+b.Right);return d}function I(a,b){b=p[b];return g.px(a,\"padding\"+b.Left)+g.px(a, \"padding\"+b.Right)}function X(a,b){if(g.boxSizing(a)){b=p[b];return g.px(a,\"border\"+b.Left+\"Width\")+g.px(a,\"border\"+b.Right+\"Width\")+g.px(a,\"padding\"+b.Left)+g.px(a,\"padding\"+b.Right)}else return 0}function T(a,b){b=p[b];return Math.round(g.px(a,\"border\"+b.Left+\"Width\")+g.px(a,\"border\"+b.Right+\"Width\")+g.px(a,\"margin\"+b.Left)+g.px(a,\"margin\"+b.Right)+g.px(a,\"padding\"+b.Left)+g.px(a,\"padding\"+b.Right))}function O(a,b){U=a.dirty=true;b&&z.layouts2.scheduleAdjust()}function A(a,b,d){if(a.style[b]!== d){a.style[b]=d;return true}else return false}function da(a,b,d){b=p[a];var h=p[a^1],f=b.measures,n=b.config.length,o=h.config.length;if(U||L){if(d&&typeof b.minSize==\"undefined\"){b.minSize=g.px(d,\"min\"+b.Size);if(b.minSize>0)b.minSize-=X(d,a)}f=f.slice();if(f.length==5){f[0]=f[0].slice();f[1]=f[1].slice()}var s=[],e=[],k=0,t=0,i,M;for(i=0;i<n;++i){var l=0,q=0,m=b.config[i][2],G=true;for(M=0;M<o;++M){var c=b.getItem(i,M);if(c){if(!c.w||a==0&&c.dirty){var j=$(\"#\"+c.id),u=j.get(0);if(u!=c.w){c.w=u; j.find(\"img\").add(j.filter(\"img\")).bind(\"load\",{item:c},function(Z){O(Z.data.item,true)});c.w.style[b.left]=c.w.style[h.left]=\"-1000000px\"}}if(!K&&c.w.style.position!=\"absolute\"){c.w.style.position=\"absolute\";c.w.style.visibility=\"hidden\";if(!c.w.wtResize&&!g.isIE){c.w.style.boxSizing=\"border-box\";if(j=g.cssPrefix(\"BoxSizing\"))c.w.style[j+\"BoxSizing\"]=\"border-box\"}}if(!c.ps)c.ps=[];if(!c.ms)c.ms=[];if(!c.size)c.size=[];if(!c.psize)c.psize=[];if(!c.fs)c.fs=[];if($(c.w).hasClass(\"Wt-hidden\"))c.ps[a]= c.ms[a]=0;else{j=!c.set;if(!c.set)c.set=[false,false];if(c.w){if(g.isIE)c.w.style.visibility=\"\";if(c.dirty||L){u=v(c.w,a);if(u>m)m=u;c.ms[a]=u;if(!c.set[a])if(a==0||!j){j=g.pxself(c.w,b.size);c.fs[a]=j?j+F(c.w,a):0}else{j=Math.round(g.px(c.w,b.size));c.fs[a]=j>Math.max(X(c.w,a),u)?j+F(c.w,a):0}j=c.fs[a];if(c.layout){if(j==0)j=c.ps[a]}else{if(c.wasLayout){c.wasLayout=false;c.set=[false,false];c.ps-=0.1;c.w.wtResize&&c.w.wtResize(c.w,-1,-1,true);A(c.w,p[1].size,\"\")}u=B(c.w,a,false);var C=c.set[a];if(C)if(c.psize[a]> 8)C=u>=c.psize[a]-2&&u<=c.psize[a]+2;var V=typeof c.ps[a]!==\"undefined\"&&b.config[i][0]>0&&c.set[a];j=C||V?Math.max(j,c.ps[a]):Math.max(j,u)}c.ps[a]=j;if(!c.span||c.span[a]==1){if(j>l)l=j}else if(j>q)q=j}else if(!c.span||c.span[a]==1){if(c.ps[a]>l)l=c.ps[a];if(c.ms[a]>m)m=c.ms[a]}else if(c.ps[a]>q)q=c.ps[a];if(c.w.style.display!==\"none\"||g.hasTag(c.w,\"TEXTAREA\")&&c.w.wtResize)G=false}}}}if(G)m=l=-1;else{if(l==0)l=q;if(m>l)l=m}s[i]=l;e[i]=m;if(m>-1){k+=l;t+=m}}h=0;j=true;o=false;for(i=0;i<n;++i)if(e[i]> -1){if(j){h+=b.margins[1];j=false}else{h+=b.margins[0];if(o)h+=4}o=b.config[i][1]!==0}j||(h+=b.margins[2]);k+=h;t+=h;b.measures=[s,e,k,t,h];if(L||f[2]!=b.measures[2])P.updateSizeInParent(a);d&&b.minSize==0&&f[3]!=b.measures[3]&&d.parentNode.className!=\"Wt-domRoot\"&&A(d,\"min\"+b.Size,b.measures[3]+\"px\")&&P.ancestor&&P.ancestor.setContentsDirty(d);d&&a==0&&d&&g.hasTag(d,\"TD\")&&A(d,b.size,b.measures[2]+\"px\")}}function ea(a,b,d){a=p[a];if(W)d=-d;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;d=-d}a.fixedSize[b]= a.sizes[b]+d;z.layouts2.scheduleAdjust()}function fa(a,b,d){var h=b.di,f=p[a],n=p[a^1],o,s=g.getElement(D),e;for(e=h-1;e>=0;--e)if(f.sizes[e]>=0){o=-(f.sizes[e]-f.measures[1][e]);break}h=f.sizes[h]-f.measures[1][h];if(W){var k=o;o=-h;h=-k}new g.SizeHandle(g,f.resizeDir,g.pxself(b,f.size),g.pxself(b,n.size),o,h,f.resizerClass,function(t){ea(a,e,t)},b,s,d,0,0)}function ga(a,b){var d=p[a],h=p[a^1],f=d.measures,n=0,o=false,s=false,e=false,k=aa?b.parentNode:null;if(d.maxSize===0)if(k){var t=g.css(k,\"position\"); if(t===\"absolute\")n=g.pxself(k,d.size);if(n===0){if(!d.initialized){if(a===0&&(t===\"absolute\"||t===\"fixed\")){k.style.display=\"none\";n=k.clientWidth;k.style.display=\"\"}n=a?k.clientHeight:k.clientWidth;o=true;if(a==0&&n==0&&g.isIElt9){n=k.offsetWidth;o=false}var i;if((g.hasTag(k,\"TD\")||g.hasTag(k,\"TH\"))&&!(g.isIE&&!g.isIElt9)){e=0;i=1}else{e=d.minSize?d.minSize:f[3];i=0}function M(V,Z){return Math.abs(V-Z)<1}if(g.isIElt9&&M(n,i)||M(n,e+I(k,a)))d.maxSize=999999}if(n===0&&d.maxSize===0){n=a?k.clientHeight: k.clientWidth;o=true}}}else{n=g.pxself(b,d.size);s=true}else if(d.sizeSet){n=g.pxself(k,d.size);s=true}var l=0;if(k&&k.wtGetPS&&a==1)l=k.wtGetPS(k,b,a,0);e=f[2];if(e<d.minSize)e=d.minSize;if(d.maxSize&&!d.sizeSet)if(e+l<d.maxSize){A(k,d.size,e+l+X(k,a)+\"px\")&&z.layouts2.remeasure();n=e+l;e=s=true}else{n=d.maxSize;o=false}d.cSize=n;if(a==1&&k&&k.wtResize){i=h.cSize;e=d.cSize;k.wtResize(k,Math.round(i),Math.round(e),true)}n-=l;if(!s){s=0;if(typeof d.cPadding===\"undefined\"){s=o?I(k,a):X(k,a);d.cPadding= s}else s=d.cPadding;n-=s}d.initialized=true;if(!(k&&n<=0)){if(n<f[3]-l)n=f[3]-l;o=[];k=d.config.length;s=h.config.length;for(e=0;e<k;++e)d.stretched[e]=false;if(n>=f[3]-l){l=n-f[4];i=[];var q=[0,0],m=[0,0],G=0;for(e=0;e<k;++e)if(f[1][e]>-1){var c=-1;if(typeof d.fixedSize[e]!==\"undefined\"&&(e+1==k||f[1][e+1]>-1))c=d.fixedSize[e];else if(d.config[e][1]!==0&&d.config[e][1][0]>=0){c=d.config[e][1][0];if(d.config[e][1][1])c=(n-f[4])*c/100}if(c>=0){i[e]=-1;o[e]=c;l-=o[e]}else{if(d.config[e][0]>0){c=1;i[e]= d.config[e][0];G+=i[e]}else{c=0;i[e]=0}q[c]+=f[1][e];m[c]+=f[0][e];o[e]=f[0][e]}}else i[e]=-2;if(G==0){for(e=0;e<k;++e)if(i[e]==0){i[e]=1;++G}m[1]=m[0];q[1]=q[0];m[0]=0;q[0]=0}if(l>m[0]+q[1]){l-=m[0];if(l>m[1]){if(d.fitSize){l-=m[1];l=l/G;for(e=0;e<k;++e)if(i[e]>0){o[e]+=Math.round(i[e]*l);d.stretched[e]=true}}}else{c=1;if(l<q[c])l=q[c];l=m[c]-q[c]>0?(l-q[c])/(m[c]-q[c]):0;for(e=0;e<k;++e)if(i[e]>0){q=f[0][e]-f[1][e];o[e]=f[1][e]+Math.round(q*l)}}}else{for(e=0;e<k;++e)if(i[e]>0)o[e]=f[1][e];l-=q[1]; c=0;if(l<q[c])l=q[c];l=m[c]-q[c]>0?(l-q[c])/(m[c]-q[c]):0;for(e=0;e<k;++e)if(i[e]==0){q=f[0][e]-f[1][e];o[e]=f[1][e]+Math.round(q*l)}}}else o=f[1];d.sizes=o;f=0;l=true;m=false;for(e=0;e<k;++e)if(o[e]>-1){if(q=m){i=D+\"-rs\"+a+\"-\"+e;m=g.getElement(i);if(!m){m=document.createElement(\"div\");m.setAttribute(\"id\",i);m.di=e;m.style.position=\"absolute\";m.style[h.left]=h.margins[1]+\"px\";m.style[d.size]=d.margins[0]+\"px\";if(h.cSize)m.style[h.size]=h.cSize-h.margins[2]-h.margins[1]+\"px\";m.className=d.handleClass; b.insertBefore(m,b.firstChild);m.onmousedown=m.ontouchstart=function(V){fa(a,this,V||window.event)}}f+=2;A(m,d.left,f+\"px\");f+=2}m=d.config[e][1]!==0;if(l){f+=d.margins[1];l=false}else f+=d.margins[0];for(G=0;G<s;++G)if((c=d.getItem(e,G))&&c.w){i=c.w;t=o[e];if(c.span){var j,u=m;for(j=1;j<c.span[a];++j){if(e+j>=o.length)break;if(u)t+=4;u=d.config[e+j][1]!==0;t+=d.margins[0];t+=o[e+j]}}var C;A(i,\"visibility\",\"\");u=c.align>>d.alignBits&15;j=c.ps[a];if(t<j)u=0;if(u){switch(u){case 1:C=f;break;case 4:C= f+(t-j)/2;break;case 2:C=f+(t-j);break}j-=F(c.w,a);if(c.layout){A(i,d.size,j+\"px\")&&O(c);c.set[a]=true}else if(t>=j&&c.set[a]){A(i,d.size,j+\"px\")&&O(c);c.set[a]=false}c.size[a]=j;c.psize[a]=j}else{C=F(c.w,a);u=Math.max(0,t-C);if(!g.isIE&&g.hasTag(i,\"TEXTAREA\"))u=t;C=false;if(g.isIE&&g.hasTag(i,\"BUTTON\"))C=true;if(C||t!=j||c.layout){A(i,d.size,u+\"px\")&&O(c);c.set[a]=true}else if(c.fs[a])a==0&&A(i,d.size,c.fs[a]+\"px\");else{A(i,d.size,\"\")&&O(c);c.set[a]=false}C=f;c.size[a]=u;c.psize[a]=t}if(K)if(q){A(i, d.left,\"4px\");t=g.css(i,\"position\");if(t!==\"absolute\")i.style.position=\"relative\"}else A(i,d.left,\"0px\");else A(i,d.left,C+\"px\");if(a==1){if(i.wtResize)i.wtResize(i,c.set[0]?Math.round(c.size[0]):-1,c.set[1]?Math.round(c.size[1]):-1,true);c.dirty=false}}f+=o[e]}$(b).children(\".\"+h.handleClass).css(d.size,n-d.margins[2]-d.margins[1]+\"px\")}}var g=z.WT;this.ancestor=null;this.descendants=[];var P=this,H=x,U=false,L=true,aa=false,Y=null,Q=null,ba=false,R=[],ca=false,W=$(document.body).hasClass(\"Wt-rtl\"), p=[{initialized:false,config:H.cols,margins:y,maxSize:r,measures:[],sizes:[],stretched:[],fixedSize:[],Left:W?\"Right\":\"Left\",left:W?\"right\":\"left\",Right:W?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return H.items[b*p[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:N},{initialized:false,config:H.rows,margins:w,maxSize:E,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\", alignBits:4,getItem:function(a,b){return H.items[a*p[0].config.length+b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:S}];jQuery.data(document.getElementById(D),\"layout\",this);this.updateSizeInParent=function(a){if(Y){var b=p[a],d=b.measures[2];if(b.maxSize>0)d=Math.min(b.maxSize,d);if(ca){b=g.getElement(D);if(!b)return;for(var h=b,f=h.parentNode;;){if(f.wtGetPS)d=f.wtGetPS(f,h,a,d);d+=T(f,a);if(f==Q)break;if(f==b.parentNode&&!f.lh&&f.offsetHeight>d)d=f.offsetHeight;h=f;f=h.parentNode}}else d+= R[a];Y.setChildSize(Q,a,d)}};this.setConfig=function(a){var b=H;H=a;p[0].config=H.cols;p[1].config=H.rows;p[0].stretched=[];p[1].stretched=[];var d;a=0;for(d=b.items.length;a<d;++a){var h=b.items[a];if(h){if(h.set){h.set[0]&&A(h.w,p[0].size,\"\");h.set[1]&&A(h.w,p[1].size,\"\")}if(h.layout){P.setChildSize(h.w,0,h.ps[0]);P.setChildSize(h.w,1,h.ps[1])}}}L=true;z.layouts2.scheduleAdjust()};this.getId=function(){return D};this.setItemsDirty=function(a){var b,d,h=p[0].config.length;b=0;for(d=a.length;b<d;++b){var f= H.items[a[b][0]*h+a[b][1]];f.dirty=true;if(f.layout){f.layout=false;f.wasLayout=true;z.layouts2.setChildLayoutsDirty(P,f.w)}}U=true};this.setDirty=function(){L=true};this.setChildSize=function(a,b,d){var h=p[0].config.length,f=p[b],n,o;n=0;for(o=H.items.length;n<o;++n){var s=H.items[n];if(s&&s.id==a.id){a=b===0?n%h:n/h;if(s.align>>f.alignBits&15||!f.stretched[a]){if(!s.ps)s.ps=[];s.ps[b]=d}s.layout=true;O(s);break}}};this.measure=function(a){var b=g.getElement(D);if(b)if(!g.isHidden(b)){if(!ba){ba= true;if(aa=J==null){var d=b;d=d.parentNode;for(R=[0,0];;){R[0]+=T(d,0);R[1]+=T(d,1);if(d.wtGetPS)ca=true;var h=jQuery.data(d.parentNode,\"layout\");if(h){Y=h;Q=d;break}d=d;d=d.parentNode;if(d.childNodes.length!=1&&!d.wtGetPS)break}d=b.parentNode;for(h=0;h<2;++h)p[h].sizeSet=g.pxself(d,p[h].size)!=0}else{Y=jQuery.data(document.getElementById(J),\"layout\");Q=b;R[0]=T(Q,0);R[1]=T(Q,1)}}if(U||L){d=aa?b.parentNode:null;da(a,b,d)}if(a==1)U=L=false}};this.setMaxSize=function(a,b){p[0].maxSize=a;p[1].maxSize= b};this.apply=function(a){var b=g.getElement(D);if(!b)return false;if(g.isHidden(b))return true;ga(a,b);return true};this.contains=function(a){var b=g.getElement(D);a=g.getElement(a.getId());return b&&a?g.contains(b,a):false};this.WT=g}");
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
				"new (function(){var z=[],D=false,J=this,N=false;this.find=function(r){return jQuery.data(document.getElementById(r),\"layout\")};this.setDirty=function(r){(r=jQuery.data(r,\"layout\"))&&r.setDirty()};this.setChildLayoutsDirty=function(r,E){var y,w;y=0;for(w=r.descendants.length;y<w;++y){var x=r.descendants[y];if(E){var B=r.WT.getElement(x.getId());if(B&&!r.WT.contains(E,B))continue}x.setDirty()}};this.add=function(r){function E(y,w){var x,B;x=0;for(B= y.length;x<B;++x){var v=y[x];if(v.getId()==w.getId()){y[x]=w;w.descendants=v.descendants;return}else if(v.contains(w)){E(v.descendants,w);return}else if(w.contains(v)){w.descendants.push(v);y.splice(x,1);--x;--B}}y.push(w)}E(z,r);J.scheduleAdjust()};var S=false,K=false;this.scheduleAdjust=function(r){if(r)N=true;if(!S){S=true;setTimeout(function(){J.adjust()},0)}};this.adjust=function(r,E){function y(x,B){var v,F;v=0;for(F=x.length;v<F;++v){var I=x[v];y(I.descendants,B);B==1&&N&&I.setDirty();I.measure(B)}} function w(x,B){var v,F;v=0;for(F=x.length;v<F;++v){var I=x[v];if(I.apply(B))w(I.descendants,B);else{x.splice(v,1);--v;--F}}}if(r){(r=this.find(r))&&r.setItemsDirty(E);J.scheduleAdjust()}else{S=false;if(!D){D=true;K=false;y(z,0);w(z,0);y(z,1);w(z,1);if(K){y(z,0);w(z,0);y(z,1);w(z,1)}N=K=D=false}}};this.updateConfig=function(r,E){(r=this.find(r))&&r.setConfig(E)};this.remeasure=function(){K=true};window.onresize=function(){J.scheduleAdjust(true)};window.onshow=function(){N=true;J.adjust()}})");
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
