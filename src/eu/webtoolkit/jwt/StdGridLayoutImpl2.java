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
		c.setProperty(Property.PropertyStyleVisibility, "hidden");
		return c;
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"StdLayout2",
				"function(B,E,J,M,R,K,t,F,A,x,y){function D(a,b){var c=r[b],h=b?a.scrollHeight:a.scrollWidth,g=b?a.clientHeight:a.clientWidth;b=b?a.offsetHeight:a.offsetWidth;if(g>=1E6)g-=1E6;if(h>=1E6)h-=1E6;if(b>=1E6)b-=1E6;if(h===0)h=f.pxself(a,c.size);if(f.isIE&&(f.hasTag(a,\"BUTTON\")||f.hasTag(a,\"TEXTAREA\")||f.hasTag(a,\"INPUT\")||f.hasTag(a,\"SELECT\")))h=g;if(h>b)if(f.pxself(a,c.size)==0)h=0;else{var l=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!= \"none\")l=true});if(l)h=0}f.isOpera||(h+=f.px(a,\"border\"+c.Left+\"Width\")+f.px(a,\"border\"+c.Right+\"Width\"));h+=f.px(a,\"margin\"+c.Left)+f.px(a,\"margin\"+c.Right);if(!f.boxSizing(a)&&!f.isIE)h+=f.px(a,\"padding\"+c.Left)+f.px(a,\"padding\"+c.Right);if(h<b)h=b;a=f.px(a,\"max\"+c.Size);if(a>0)h=Math.min(a,h);return Math.round(h)}function w(a,b){b=r[b];if(a.style.display==\"none\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=f.px(a,\"min\"+b.Size);f.boxSizing(a)||(c+=f.px(a,\"padding\"+ b.Left)+f.px(a,\"padding\"+b.Right));return c}}function G(a,b){b=r[b];var c=f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right);if(!f.boxSizing(a)&&!(f.isIE&&!f.isIElt9&&f.hasTag(a,\"BUTTON\")))c+=f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right);return c}function I(a,b){b=r[b];return f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}function V(a,b){if(f.boxSizing(a)){b=r[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+ \"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}else return 0}function S(a,b){b=r[b];return Math.round(f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right)+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right))}function N(a,b){T=a.dirty=true;b&&B.layouts2.scheduleAdjust()}function C(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function ba(a,b,c){b=r[a];var h=r[a^1],g=b.measures,l=b.config.length,q=h.config.length; if(T||L){if(c&&typeof b.minSize==\"undefined\"){b.minSize=f.px(c,\"min\"+b.Size);if(b.minSize>0)b.minSize-=V(c,a)}g=g.slice();if(g.length==5){g[0]=g[0].slice();g[1]=g[1].slice()}var s=[],d=[],k=0,u=0,j,n;for(j=0;j<l;++j){var p=0,o=0,z=b.config[j][2],i=true;for(n=0;n<q;++n){var e=b.getItem(j,n);if(e){if(!e.w||a==0&&e.dirty){var m=$(\"#\"+e.id),v=m.get(0);if(v!=e.w){e.w=v;m.find(\"img\").add(m.filter(\"img\")).bind(\"load\",{item:e},function(ca){N(ca.data.item,true)});e.w.style[b.left]=e.w.style[h.left]=\"-1000000px\"}}if(!K&& e.w.style.position!=\"absolute\"){e.w.style.position=\"absolute\";e.w.style.visibility=\"hidden\";if(!e.w.wtResize&&!f.isIE){e.w.style.boxSizing=\"border-box\";if(m=f.cssPrefix(\"BoxSizing\"))e.w.style[m+\"BoxSizing\"]=\"border-box\"}}if(!e.ps)e.ps=[];if(!e.ms)e.ms=[];if(!e.size)e.size=[];if(!e.psize)e.psize=[];if(!e.fs)e.fs=[];if($(e.w).hasClass(\"Wt-hidden\"))e.ps[a]=e.ms[a]=0;else{m=!e.set;if(!e.set)e.set=[false,false];if(e.w){if(f.isIE)e.w.style.visibility=\"\";if(e.dirty||L){v=w(e.w,a);if(v>z)z=v;e.ms[a]=v;if(!e.set[a])if(a== 0||!m){m=f.pxself(e.w,b.size);e.fs[a]=m?m+G(e.w,a):0}else{m=Math.round(f.px(e.w,b.size));e.fs[a]=m>Math.max(V(e.w,a),v)?m+G(e.w,a):0}m=e.fs[a];if(e.layout){if(m==0)m=e.ps[a]}else{if(e.wasLayout){e.wasLayout=false;e.set=[false,false];e.ps-=0.1;e.w.wtResize&&e.w.wtResize(e.w,-1,-1,true);C(e.w,r[1].size,\"\")}v=D(e.w,a);var X=typeof e.ps[a]!==\"undefined\"&&b.config[j][0]>0&&e.set[a];m=e.set[a]&&v>=e.psize[a]-4&&v<=e.psize[a]+4||X?Math.max(m,e.ps[a]):Math.max(m,v)}e.ps[a]=m;if(!e.span||e.span[a]==1){if(m> p)p=m}else if(m>o)o=m}else if(!e.span||e.span[a]==1){if(e.ps[a]>p)p=e.ps[a];if(e.ms[a]>z)z=e.ms[a]}else if(e.ps[a]>o)o=e.ps[a];if(e.w.style.display!==\"none\"||f.hasTag(e.w,\"TEXTAREA\")&&e.w.wtResize)i=false}}}}if(i)z=p=-1;else{if(p==0)p=o;if(z>p)p=z}s[j]=p;d[j]=z;if(z>-1){k+=p;u+=z}}h=0;m=true;q=false;for(j=0;j<l;++j)if(d[j]>-1){if(m){h+=b.margins[1];m=false}else{h+=b.margins[0];if(q)h+=4}q=b.config[j][1]!==0}m||(h+=b.margins[2]);k+=h;u+=h;b.measures=[s,d,k,u,h];if(L||g[2]!=b.measures[2])O.updateSizeInParent(a); c&&b.minSize==0&&g[3]!=b.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&&C(c,\"min\"+b.Size,b.measures[3]+\"px\")&&O.ancestor&&O.ancestor.setContentsDirty(c);c&&a==0&&c&&f.hasTag(c,\"TD\")&&C(c,b.size,b.measures[2]+\"px\")}}function da(a,b,c){a=r[a];if(U)c=-c;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;c=-c}a.fixedSize[b]=a.sizes[b]+c;B.layouts2.scheduleAdjust()}function ea(a,b,c){var h=b.di,g=r[a],l=r[a^1],q,s=f.getElement(E),d;for(d=h-1;d>=0;--d)if(g.sizes[d]>=0){q=-(g.sizes[d]-g.measures[1][d]);break}h= g.sizes[h]-g.measures[1][h];if(U){var k=q;q=-h;h=-k}new f.SizeHandle(f,g.resizeDir,f.pxself(b,g.size),f.pxself(b,l.size),q,h,g.resizerClass,function(u){da(a,d,u)},b,s,c,0,0)}function fa(a,b){var c=r[a],h=r[a^1],g=c.measures,l=0,q=false,s=false,d=false,k=Y?b.parentNode:null;if(c.maxSize===0)if(k){var u=f.css(k,\"position\");if(u===\"absolute\")l=f.pxself(k,c.size);if(l===0){if(!c.initialized){if(a===0&&(u===\"absolute\"||u===\"fixed\")){k.style.display=\"none\";l=k.clientWidth;k.style.display=\"\"}l=a?k.clientHeight: k.clientWidth;q=true;if(a==0&&l==0&&f.isIElt9){l=k.offsetWidth;q=false}var j;if((f.hasTag(k,\"TD\")||f.hasTag(k,\"TH\"))&&!(f.isIE&&!f.isIElt9)){d=0;j=1}else{d=c.minSize?c.minSize:g[3];j=0}if(f.isIElt9&&l==j||l==d+I(k,a))c.maxSize=999999}if(l===0&&c.maxSize===0){l=a?k.clientHeight:k.clientWidth;q=true}}}else{l=f.pxself(b,c.size);s=true}var n=0;if(k&&k.wtGetPS&&a==1)n=k.wtGetPS(k,b,a,0);d=g[2];if(d<c.minSize)d=c.minSize;if(c.maxSize)if(d+n<c.maxSize){C(k,c.size,d+n+V(k,a)+\"px\")&&B.layouts2.remeasure(); l=d+n;d=s=true}else{l=c.maxSize;q=false}c.cSize=l;if(a==1&&k&&k.wtResize){j=h.cSize;d=c.cSize;k.wtResize(k,Math.round(j),Math.round(d),true)}l-=n;if(!s){s=0;if(typeof c.cPadding===\"undefined\"){s=q?I(k,a):V(k,a);c.cPadding=s}else s=c.cPadding;l-=s}c.initialized=true;if(!(k&&l<=0)){if(l<g[3]-n)l=g[3]-n;q=[];k=c.config.length;s=h.config.length;for(d=0;d<k;++d)c.stretched[d]=false;if(l>=g[3]-n){n=l-g[4];j=[];var p=[0,0],o=[0,0],z=0;for(d=0;d<k;++d)if(g[1][d]>-1){var i=-1;if(typeof c.fixedSize[d]!==\"undefined\"&& (d+1==k||g[1][d+1]>-1))i=c.fixedSize[d];else if(c.config[d][1]!==0&&c.config[d][1][0]>=0){i=c.config[d][1][0];if(c.config[d][1][1])i=(l-g[4])*i/100}if(i>=0){j[d]=-1;q[d]=i;n-=q[d]}else{if(c.config[d][0]>0){i=1;j[d]=c.config[d][0];z+=j[d]}else{i=0;j[d]=0}p[i]+=g[1][d];o[i]+=g[0][d];q[d]=g[0][d]}}else j[d]=-2;if(z==0){for(d=0;d<k;++d)if(j[d]==0){j[d]=1;++z}o[1]=o[0];p[1]=p[0];o[0]=0;p[0]=0}if(n>o[0]+p[1]){n-=o[0];if(n>o[1]){if(c.fitSize){n-=o[1];n=n/z;for(d=0;d<k;++d)if(j[d]>0){q[d]+=Math.round(j[d]* n);c.stretched[d]=true}}}else{i=1;if(n<p[i])n=p[i];n=o[i]-p[i]>0?(n-p[i])/(o[i]-p[i]):0;for(d=0;d<k;++d)if(j[d]>0){p=g[0][d]-g[1][d];q[d]=g[1][d]+Math.round(p*n)}}}else{for(d=0;d<k;++d)if(j[d]>0)q[d]=g[1][d];n-=p[1];i=0;if(n<p[i])n=p[i];n=o[i]-p[i]>0?(n-p[i])/(o[i]-p[i]):0;for(d=0;d<k;++d)if(j[d]==0){p=g[0][d]-g[1][d];q[d]=g[1][d]+Math.round(p*n)}}}else q=g[1];c.sizes=q;g=0;n=true;o=false;for(d=0;d<k;++d)if(q[d]>-1){if(p=o){j=E+\"-rs\"+a+\"-\"+d;o=f.getElement(j);if(!o){o=document.createElement(\"div\"); o.setAttribute(\"id\",j);o.di=d;o.style.position=\"absolute\";o.style[h.left]=h.margins[1]+\"px\";o.style[c.size]=c.margins[0]+\"px\";if(h.cSize)o.style[h.size]=h.cSize-h.margins[2]-h.margins[1]+\"px\";o.className=c.handleClass;b.insertBefore(o,b.firstChild);o.onmousedown=o.ontouchstart=function(X){ea(a,this,X||window.event)}}g+=2;C(o,c.left,g+\"px\");g+=2}o=c.config[d][1]!==0;if(n){g+=c.margins[1];n=false}else g+=c.margins[0];for(z=0;z<s;++z)if((i=c.getItem(d,z))&&i.w){j=i.w;u=q[d];if(i.span){var e,m=o;for(e= 1;e<i.span[a];++e){if(d+e>=q.length)break;if(m)u+=4;m=c.config[d+e][1]!==0;u+=c.margins[0];u+=q[d+e]}}var v;C(j,\"visibility\",\"\");m=i.align>>c.alignBits&15;e=i.ps[a];if(u<e)m=0;if(m){switch(m){case 1:v=g;break;case 4:v=g+(u-e)/2;break;case 2:v=g+(u-e);break}e-=G(i.w,a);if(i.layout){C(j,c.size,e+\"px\")&&N(i);i.set[a]=true}else if(u>=e&&i.set[a]){C(j,c.size,e+\"px\")&&N(i);i.set[a]=false}i.size[a]=e;i.psize[a]=e}else{v=G(i.w,a);m=Math.max(0,u-v);v=false;if(f.isIE&&f.hasTag(j,\"BUTTON\"))v=true;if(v||u!=e|| i.layout){C(j,c.size,m+\"px\")&&N(i);i.set[a]=true}else if(i.fs[a])a==0&&C(j,c.size,i.fs[a]+\"px\");else{C(j,c.size,\"\")&&N(i);i.set[a]=false}v=g;i.size[a]=m;i.psize[a]=u}if(K)if(p){C(j,c.left,\"4px\");u=f.css(j,\"position\");if(u!==\"absolute\")j.style.position=\"relative\"}else C(j,c.left,\"0px\");else C(j,c.left,v+\"px\");if(a==1){if(j.wtResize)j.wtResize(j,i.set[0]?Math.round(i.size[0]):-1,i.set[1]?Math.round(i.size[1]):-1,true);i.dirty=false}}g+=q[d]}$(b).children(\".\"+h.handleClass).css(c.size,l-c.margins[2]- c.margins[1]+\"px\")}}var f=B.WT;this.ancestor=null;this.descendants=[];var O=this,H=y,T=false,L=true,Y=false,W=null,P=null,Z=false,Q=[],aa=false,U=$(document.body).hasClass(\"Wt-rtl\"),r=[{initialized:false,config:H.cols,margins:A,maxSize:t,measures:[],sizes:[],stretched:[],fixedSize:[],Left:U?\"Right\":\"Left\",left:U?\"right\":\"left\",Right:U?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return H.items[b*r[0].config.length+a]},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\", fitSize:M},{initialized:false,config:H.rows,margins:x,maxSize:F,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,b){return H.items[a*r[0].config.length+b]},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:R}];jQuery.data(document.getElementById(E),\"layout\",this);this.updateSizeInParent=function(a){if(W){var b=r[a],c=b.measures[2];if(b.maxSize>0)c=Math.min(b.maxSize,c);if(aa){b=f.getElement(E); if(!b)return;for(var h=b,g=h.parentNode;;){if(g.wtGetPS)c=g.wtGetPS(g,h,a,c);c+=S(g,a);if(g==P)break;if(g==b.parentNode&&!g.lh&&g.offsetHeight>c)c=g.offsetHeight;h=g;g=h.parentNode}}else c+=Q[a];W.setChildSize(P,a,c)}};this.setConfig=function(a){var b=H;H=a;r[0].config=H.cols;r[1].config=H.rows;r[0].stretched=[];r[1].stretched=[];var c;a=0;for(c=b.items.length;a<c;++a){var h=b.items[a];if(h){if(h.set){h.set[0]&&C(h.w,r[0].size,\"\");h.set[1]&&C(h.w,r[1].size,\"\")}if(h.layout){O.setChildSize(h.w,0,h.ps[0]); O.setChildSize(h.w,1,h.ps[1])}}}L=true;B.layouts2.scheduleAdjust()};this.getId=function(){return E};this.setItemsDirty=function(a){var b,c,h=r[0].config.length;b=0;for(c=a.length;b<c;++b){var g=H.items[a[b][0]*h+a[b][1]];g.dirty=true;if(g.layout){g.layout=false;g.wasLayout=true;B.layouts2.setChildLayoutsDirty(O,g.w)}}T=true};this.setDirty=function(){L=true};this.setChildSize=function(a,b,c){var h=r[0].config.length,g=r[b],l,q;l=0;for(q=H.items.length;l<q;++l){var s=H.items[l];if(s&&s.id==a.id){a= b===0?l%h:l/h;if(s.align>>g.alignBits&15||!g.stretched[a]){if(!s.ps)s.ps=[];s.ps[b]=c}s.layout=true;N(s);break}}};this.measure=function(a){var b=f.getElement(E);if(b)if(!f.isHidden(b)){if(!Z){Z=true;if(Y=J==null){var c=b;c=c.parentNode;for(Q=[0,0];;){Q[0]+=S(c,0);Q[1]+=S(c,1);if(c.wtGetPS)aa=true;var h=jQuery.data(c.parentNode,\"layout\");if(h){W=h;P=c;break}c=c;c=c.parentNode;if(c.childNodes.length!=1&&!c.wtGetPS)break}}else{W=jQuery.data(document.getElementById(J),\"layout\");P=b;Q[0]=S(P,0);Q[1]=S(P, 1)}}if(T||L)ba(a,b,Y?b.parentNode:null);if(a==1)T=L=false}};this.setMaxSize=function(a,b){r[0].maxSize=a;r[1].maxSize=b};this.apply=function(a){var b=f.getElement(E);if(!b)return false;if(f.isHidden(b))return true;fa(a,b);return true};this.contains=function(a){var b=f.getElement(E);a=f.getElement(a.getId());return b&&a?f.contains(b,a):false};this.WT=f}");
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
				"new (function(){var B=[],E=false,J=this,M=false;this.find=function(t){return jQuery.data(document.getElementById(t),\"layout\")};this.setDirty=function(t){(t=jQuery.data(t,\"layout\"))&&t.setDirty()};this.setChildLayoutsDirty=function(t,F){var A,x;A=0;for(x=t.descendants.length;A<x;++A){var y=t.descendants[A];if(F){var D=t.WT.getElement(y.getId());if(D&&!t.WT.contains(F,D))continue}y.setDirty()}};this.add=function(t){function F(A,x){var y,D;y=0;for(D= A.length;y<D;++y){var w=A[y];if(w.getId()==x.getId()){A[y]=x;x.descendants=w.descendants;return}else if(w.contains(x)){F(w.descendants,x);return}else if(x.contains(w)){x.descendants.push(w);A.splice(y,1);--y;--D}}A.push(x)}F(B,t);J.scheduleAdjust()};var R=false,K=false;this.scheduleAdjust=function(){if(!R){R=true;setTimeout(function(){J.adjust()},0)}};this.adjust=function(t,F){function A(y,D){var w,G;w=0;for(G=y.length;w<G;++w){var I=y[w];A(I.descendants,D);D==1&&M&&I.setDirty();I.measure(D)}}function x(y, D){var w,G;w=0;for(G=y.length;w<G;++w){var I=y[w];if(I.apply(D))x(I.descendants,D);else{y.splice(w,1);--w;--G}}}if(t){(t=this.find(t))&&t.setItemsDirty(F);J.scheduleAdjust()}else{R=false;if(!E){E=true;K=false;A(B,0);x(B,0);A(B,1);x(B,1);if(K){A(B,0);x(B,0);A(B,1);x(B,1)}M=K=E=false}}};this.updateConfig=function(t,F){(t=this.find(t))&&t.setConfig(F)};this.remeasure=function(){K=true};window.onresize=function(){M=true;J.scheduleAdjust()};window.onshow=function(){M=true;J.adjust()}})");
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
