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
				"function(z,C,J,N,S,K,r,E,y,w,x){function B(a,b){var c=p[b],g=b?a.scrollHeight:a.scrollWidth,h=b?a.clientHeight:a.clientWidth;b=b?a.offsetHeight:a.offsetWidth;if(h>=1E6)h-=1E6;if(g>=1E6)g-=1E6;if(b>=1E6)b-=1E6;if(g===0){g=f.pxself(a,c.size);if(g!==0&&!f.isOpera&&!f.isGecko)g-=f.px(a,\"border\"+c.Left+\"Width\")+f.px(a,\"border\"+c.Right+\"Width\")}if(f.isIE&&(f.hasTag(a,\"BUTTON\")||f.hasTag(a,\"TEXTAREA\")||f.hasTag(a,\"INPUT\")||f.hasTag(a,\"SELECT\")))g= h;if(g>b)if(f.pxself(a,c.size)==0)g=0;else{var n=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!=\"none\")n=true});if(n)g=0}f.isOpera||(g+=f.px(a,\"border\"+c.Left+\"Width\")+f.px(a,\"border\"+c.Right+\"Width\"));g+=f.px(a,\"margin\"+c.Left)+f.px(a,\"margin\"+c.Right);if(!f.boxSizing(a)&&!f.isIE)g+=f.px(a,\"padding\"+c.Left)+f.px(a,\"padding\"+c.Right);if(g<b)g=b;a=f.px(a,\"max\"+c.Size);if(a>0)g=Math.min(a,g);return Math.round(g)}function v(a,b){b=p[b];if(a.style.display==\"none\")return 0;else if(a[\"layoutMin\"+ b.Size])return a[\"layoutMin\"+b.Size];else{var c=f.px(a,\"min\"+b.Size);f.boxSizing(a)||(c+=f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right));return c}}function F(a,b){b=p[b];var c=f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right);if(!f.boxSizing(a)&&!(f.isIE&&!f.isIElt9&&f.hasTag(a,\"BUTTON\")))c+=f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right);return c}function I(a,b){b=p[b];return f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)} function X(a,b){if(f.boxSizing(a)){b=p[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}else return 0}function T(a,b){b=p[b];return Math.round(f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right)+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right))}function O(a,b){U=a.dirty=true;b&&z.layouts2.scheduleAdjust()}function A(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false} function ca(a,b,c){b=p[a];var g=p[a^1],h=b.measures,n=b.config.length,o=g.config.length;if(U||L){if(c&&typeof b.minSize==\"undefined\"){b.minSize=f.px(c,\"min\"+b.Size);if(b.minSize>0)b.minSize-=X(c,a)}h=h.slice();if(h.length==5){h[0]=h[0].slice();h[1]=h[1].slice()}var s=[],e=[],k=0,t=0,i,M;for(i=0;i<n;++i){var l=0,q=0,m=b.config[i][2],G=true;for(M=0;M<o;++M){var d=b.getItem(i,M);if(d){if(!d.w||a==0&&d.dirty){var j=$(\"#\"+d.id),u=j.get(0);if(u!=d.w){d.w=u;j.find(\"img\").add(j.filter(\"img\")).bind(\"load\", {item:d},function(V){O(V.data.item,true)});d.w.style[b.left]=d.w.style[g.left]=\"-1000000px\"}}if(!K&&d.w.style.position!=\"absolute\"){d.w.style.position=\"absolute\";d.w.style.visibility=\"hidden\";if(!d.w.wtResize&&!f.isIE){d.w.style.boxSizing=\"border-box\";if(j=f.cssPrefix(\"BoxSizing\"))d.w.style[j+\"BoxSizing\"]=\"border-box\"}}if(!d.ps)d.ps=[];if(!d.ms)d.ms=[];if(!d.size)d.size=[];if(!d.psize)d.psize=[];if(!d.fs)d.fs=[];if($(d.w).hasClass(\"Wt-hidden\"))d.ps[a]=d.ms[a]=0;else{j=!d.set;if(!d.set)d.set=[false, false];if(d.w){if(f.isIE)d.w.style.visibility=\"\";if(d.dirty||L){u=v(d.w,a);if(u>m)m=u;d.ms[a]=u;if(!d.set[a])if(a==0||!j){j=f.pxself(d.w,b.size);d.fs[a]=j?j+F(d.w,a):0}else{j=Math.round(f.px(d.w,b.size));d.fs[a]=j>Math.max(X(d.w,a),u)?j+F(d.w,a):0}j=d.fs[a];if(d.layout){if(j==0)j=d.ps[a]}else{if(d.wasLayout){d.wasLayout=false;d.set=[false,false];d.ps-=0.1;d.w.wtResize&&d.w.wtResize(d.w,-1,-1,true);A(d.w,p[1].size,\"\")}u=B(d.w,a);var D=typeof d.ps[a]!==\"undefined\"&&b.config[i][0]>0&&d.set[a];j=d.set[a]&& u>=d.psize[a]-4&&u<=d.psize[a]+4||D?Math.max(j,d.ps[a]):Math.max(j,u)}d.ps[a]=j;if(!d.span||d.span[a]==1){if(j>l)l=j}else if(j>q)q=j}else if(!d.span||d.span[a]==1){if(d.ps[a]>l)l=d.ps[a];if(d.ms[a]>m)m=d.ms[a]}else if(d.ps[a]>q)q=d.ps[a];if(d.w.style.display!==\"none\"||f.hasTag(d.w,\"TEXTAREA\")&&d.w.wtResize)G=false}}}}if(G)m=l=-1;else{if(l==0)l=q;if(m>l)l=m}s[i]=l;e[i]=m;if(m>-1){k+=l;t+=m}}g=0;j=true;o=false;for(i=0;i<n;++i)if(e[i]>-1){if(j){g+=b.margins[1];j=false}else{g+=b.margins[0];if(o)g+=4}o= b.config[i][1]!==0}j||(g+=b.margins[2]);k+=g;t+=g;b.measures=[s,e,k,t,g];if(L||h[2]!=b.measures[2])P.updateSizeInParent(a);c&&b.minSize==0&&h[3]!=b.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&&A(c,\"min\"+b.Size,b.measures[3]+\"px\")&&P.ancestor&&P.ancestor.setContentsDirty(c);c&&a==0&&c&&f.hasTag(c,\"TD\")&&A(c,b.size,b.measures[2]+\"px\")}}function da(a,b,c){a=p[a];if(W)c=-c;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;c=-c}a.fixedSize[b]=a.sizes[b]+c;z.layouts2.scheduleAdjust()}function ea(a,b, c){var g=b.di,h=p[a],n=p[a^1],o,s=f.getElement(C),e;for(e=g-1;e>=0;--e)if(h.sizes[e]>=0){o=-(h.sizes[e]-h.measures[1][e]);break}g=h.sizes[g]-h.measures[1][g];if(W){var k=o;o=-g;g=-k}new f.SizeHandle(f,h.resizeDir,f.pxself(b,h.size),f.pxself(b,n.size),o,g,h.resizerClass,function(t){da(a,e,t)},b,s,c,0,0)}function fa(a,b){var c=p[a],g=p[a^1],h=c.measures,n=0,o=false,s=false,e=false,k=Z?b.parentNode:null;if(c.maxSize===0)if(k){var t=f.css(k,\"position\");if(t===\"absolute\")n=f.pxself(k,c.size);if(n===0){if(!c.initialized){if(a=== 0&&(t===\"absolute\"||t===\"fixed\")){k.style.display=\"none\";n=k.clientWidth;k.style.display=\"\"}n=a?k.clientHeight:k.clientWidth;o=true;if(a==0&&n==0&&f.isIElt9){n=k.offsetWidth;o=false}var i;if((f.hasTag(k,\"TD\")||f.hasTag(k,\"TH\"))&&!(f.isIE&&!f.isIElt9)){e=0;i=1}else{e=c.minSize?c.minSize:h[3];i=0}function M(V,ga){return Math.abs(V-ga)<1}if(f.isIElt9&&M(n,i)||M(n,e+I(k,a)))c.maxSize=999999}if(n===0&&c.maxSize===0){n=a?k.clientHeight:k.clientWidth;o=true}}}else{n=f.pxself(b,c.size);s=true}else if(c.sizeSet){n= f.pxself(k,c.size);s=true}var l=0;if(k&&k.wtGetPS&&a==1)l=k.wtGetPS(k,b,a,0);e=h[2];if(e<c.minSize)e=c.minSize;if(c.maxSize&&!c.sizeSet)if(e+l<c.maxSize){A(k,c.size,e+l+X(k,a)+\"px\")&&z.layouts2.remeasure();n=e+l;e=s=true}else{n=c.maxSize;o=false}c.cSize=n;if(a==1&&k&&k.wtResize){i=g.cSize;e=c.cSize;k.wtResize(k,Math.round(i),Math.round(e),true)}n-=l;if(!s){s=0;if(typeof c.cPadding===\"undefined\"){s=o?I(k,a):X(k,a);c.cPadding=s}else s=c.cPadding;n-=s}c.initialized=true;if(!(k&&n<=0)){if(n<h[3]-l)n= h[3]-l;o=[];k=c.config.length;s=g.config.length;for(e=0;e<k;++e)c.stretched[e]=false;if(n>=h[3]-l){l=n-h[4];i=[];var q=[0,0],m=[0,0],G=0;for(e=0;e<k;++e)if(h[1][e]>-1){var d=-1;if(typeof c.fixedSize[e]!==\"undefined\"&&(e+1==k||h[1][e+1]>-1))d=c.fixedSize[e];else if(c.config[e][1]!==0&&c.config[e][1][0]>=0){d=c.config[e][1][0];if(c.config[e][1][1])d=(n-h[4])*d/100}if(d>=0){i[e]=-1;o[e]=d;l-=o[e]}else{if(c.config[e][0]>0){d=1;i[e]=c.config[e][0];G+=i[e]}else{d=0;i[e]=0}q[d]+=h[1][e];m[d]+=h[0][e];o[e]= h[0][e]}}else i[e]=-2;if(G==0){for(e=0;e<k;++e)if(i[e]==0){i[e]=1;++G}m[1]=m[0];q[1]=q[0];m[0]=0;q[0]=0}if(l>m[0]+q[1]){l-=m[0];if(l>m[1]){if(c.fitSize){l-=m[1];l=l/G;for(e=0;e<k;++e)if(i[e]>0){o[e]+=Math.round(i[e]*l);c.stretched[e]=true}}}else{d=1;if(l<q[d])l=q[d];l=m[d]-q[d]>0?(l-q[d])/(m[d]-q[d]):0;for(e=0;e<k;++e)if(i[e]>0){q=h[0][e]-h[1][e];o[e]=h[1][e]+Math.round(q*l)}}}else{for(e=0;e<k;++e)if(i[e]>0)o[e]=h[1][e];l-=q[1];d=0;if(l<q[d])l=q[d];l=m[d]-q[d]>0?(l-q[d])/(m[d]-q[d]):0;for(e=0;e<k;++e)if(i[e]== 0){q=h[0][e]-h[1][e];o[e]=h[1][e]+Math.round(q*l)}}}else o=h[1];c.sizes=o;h=0;l=true;m=false;for(e=0;e<k;++e)if(o[e]>-1){if(q=m){i=C+\"-rs\"+a+\"-\"+e;m=f.getElement(i);if(!m){m=document.createElement(\"div\");m.setAttribute(\"id\",i);m.di=e;m.style.position=\"absolute\";m.style[g.left]=g.margins[1]+\"px\";m.style[c.size]=c.margins[0]+\"px\";if(g.cSize)m.style[g.size]=g.cSize-g.margins[2]-g.margins[1]+\"px\";m.className=c.handleClass;b.insertBefore(m,b.firstChild);m.onmousedown=m.ontouchstart=function(V){ea(a,this, V||window.event)}}h+=2;A(m,c.left,h+\"px\");h+=2}m=c.config[e][1]!==0;if(l){h+=c.margins[1];l=false}else h+=c.margins[0];for(G=0;G<s;++G)if((d=c.getItem(e,G))&&d.w){i=d.w;t=o[e];if(d.span){var j,u=m;for(j=1;j<d.span[a];++j){if(e+j>=o.length)break;if(u)t+=4;u=c.config[e+j][1]!==0;t+=c.margins[0];t+=o[e+j]}}var D;A(i,\"visibility\",\"\");u=d.align>>c.alignBits&15;j=d.ps[a];if(t<j)u=0;if(u){switch(u){case 1:D=h;break;case 4:D=h+(t-j)/2;break;case 2:D=h+(t-j);break}j-=F(d.w,a);if(d.layout){A(i,c.size,j+\"px\")&& O(d);d.set[a]=true}else if(t>=j&&d.set[a]){A(i,c.size,j+\"px\")&&O(d);d.set[a]=false}d.size[a]=j;d.psize[a]=j}else{D=F(d.w,a);u=Math.max(0,t-D);D=false;if(f.isIE&&f.hasTag(i,\"BUTTON\"))D=true;if(D||t!=j||d.layout){A(i,c.size,u+\"px\")&&O(d);d.set[a]=true}else if(d.fs[a])a==0&&A(i,c.size,d.fs[a]+\"px\");else{A(i,c.size,\"\")&&O(d);d.set[a]=false}D=h;d.size[a]=u;d.psize[a]=t}if(K)if(q){A(i,c.left,\"4px\");t=f.css(i,\"position\");if(t!==\"absolute\")i.style.position=\"relative\"}else A(i,c.left,\"0px\");else A(i,c.left, D+\"px\");if(a==1){if(i.wtResize)i.wtResize(i,d.set[0]?Math.round(d.size[0]):-1,d.set[1]?Math.round(d.size[1]):-1,true);d.dirty=false}}h+=o[e]}$(b).children(\".\"+g.handleClass).css(c.size,n-c.margins[2]-c.margins[1]+\"px\")}}var f=z.WT;this.ancestor=null;this.descendants=[];var P=this,H=x,U=false,L=true,Z=false,Y=null,Q=null,aa=false,R=[],ba=false,W=$(document.body).hasClass(\"Wt-rtl\"),p=[{initialized:false,config:H.cols,margins:y,maxSize:r,measures:[],sizes:[],stretched:[],fixedSize:[],Left:W?\"Right\": \"Left\",left:W?\"right\":\"left\",Right:W?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return H.items[b*p[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:N},{initialized:false,config:H.rows,margins:w,maxSize:E,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,b){return H.items[a*p[0].config.length+b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\", fitSize:S}];jQuery.data(document.getElementById(C),\"layout\",this);this.updateSizeInParent=function(a){if(Y){var b=p[a],c=b.measures[2];if(b.maxSize>0)c=Math.min(b.maxSize,c);if(ba){b=f.getElement(C);if(!b)return;for(var g=b,h=g.parentNode;;){if(h.wtGetPS)c=h.wtGetPS(h,g,a,c);c+=T(h,a);if(h==Q)break;if(h==b.parentNode&&!h.lh&&h.offsetHeight>c)c=h.offsetHeight;g=h;h=g.parentNode}}else c+=R[a];Y.setChildSize(Q,a,c)}};this.setConfig=function(a){var b=H;H=a;p[0].config=H.cols;p[1].config=H.rows;p[0].stretched= [];p[1].stretched=[];var c;a=0;for(c=b.items.length;a<c;++a){var g=b.items[a];if(g){if(g.set){g.set[0]&&A(g.w,p[0].size,\"\");g.set[1]&&A(g.w,p[1].size,\"\")}if(g.layout){P.setChildSize(g.w,0,g.ps[0]);P.setChildSize(g.w,1,g.ps[1])}}}L=true;z.layouts2.scheduleAdjust()};this.getId=function(){return C};this.setItemsDirty=function(a){var b,c,g=p[0].config.length;b=0;for(c=a.length;b<c;++b){var h=H.items[a[b][0]*g+a[b][1]];h.dirty=true;if(h.layout){h.layout=false;h.wasLayout=true;z.layouts2.setChildLayoutsDirty(P, h.w)}}U=true};this.setDirty=function(){L=true};this.setChildSize=function(a,b,c){var g=p[0].config.length,h=p[b],n,o;n=0;for(o=H.items.length;n<o;++n){var s=H.items[n];if(s&&s.id==a.id){a=b===0?n%g:n/g;if(s.align>>h.alignBits&15||!h.stretched[a]){if(!s.ps)s.ps=[];s.ps[b]=c}s.layout=true;O(s);break}}};this.measure=function(a){var b=f.getElement(C);if(b)if(!f.isHidden(b)){if(!aa){aa=true;if(Z=J==null){var c=b;c=c.parentNode;for(R=[0,0];;){R[0]+=T(c,0);R[1]+=T(c,1);if(c.wtGetPS)ba=true;var g=jQuery.data(c.parentNode, \"layout\");if(g){Y=g;Q=c;break}c=c;c=c.parentNode;if(c.childNodes.length!=1&&!c.wtGetPS)break}c=b.parentNode;for(g=0;g<2;++g)p[g].sizeSet=f.pxself(c,p[g].size)!=0}else{Y=jQuery.data(document.getElementById(J),\"layout\");Q=b;R[0]=T(Q,0);R[1]=T(Q,1)}}if(U||L){c=Z?b.parentNode:null;ca(a,b,c)}if(a==1)U=L=false}};this.setMaxSize=function(a,b){p[0].maxSize=a;p[1].maxSize=b};this.apply=function(a){var b=f.getElement(C);if(!b)return false;if(f.isHidden(b))return true;fa(a,b);return true};this.contains=function(a){var b= f.getElement(C);a=f.getElement(a.getId());return b&&a?f.contains(b,a):false};this.WT=f}");
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
				"new (function(){var z=[],C=false,J=this,N=false;this.find=function(r){return jQuery.data(document.getElementById(r),\"layout\")};this.setDirty=function(r){(r=jQuery.data(r,\"layout\"))&&r.setDirty()};this.setChildLayoutsDirty=function(r,E){var y,w;y=0;for(w=r.descendants.length;y<w;++y){var x=r.descendants[y];if(E){var B=r.WT.getElement(x.getId());if(B&&!r.WT.contains(E,B))continue}x.setDirty()}};this.add=function(r){function E(y,w){var x,B;x=0;for(B= y.length;x<B;++x){var v=y[x];if(v.getId()==w.getId()){y[x]=w;w.descendants=v.descendants;return}else if(v.contains(w)){E(v.descendants,w);return}else if(w.contains(v)){w.descendants.push(v);y.splice(x,1);--x;--B}}y.push(w)}E(z,r);J.scheduleAdjust()};var S=false,K=false;this.scheduleAdjust=function(r){if(r)N=true;if(!S){S=true;setTimeout(function(){J.adjust()},0)}};this.adjust=function(r,E){function y(x,B){var v,F;v=0;for(F=x.length;v<F;++v){var I=x[v];y(I.descendants,B);B==1&&N&&I.setDirty();I.measure(B)}} function w(x,B){var v,F;v=0;for(F=x.length;v<F;++v){var I=x[v];if(I.apply(B))w(I.descendants,B);else{x.splice(v,1);--v;--F}}}if(r){(r=this.find(r))&&r.setItemsDirty(E);J.scheduleAdjust()}else{S=false;if(!C){C=true;K=false;y(z,0);w(z,0);y(z,1);w(z,1);if(K){y(z,0);w(z,0);y(z,1);w(z,1)}N=K=C=false}}};this.updateConfig=function(r,E){(r=this.find(r))&&r.setConfig(E)};this.remeasure=function(){K=true};window.onresize=function(){J.scheduleAdjust(true)};window.onshow=function(){N=true;J.adjust()}})");
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
