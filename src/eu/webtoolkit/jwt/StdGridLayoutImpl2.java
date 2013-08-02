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
		this.needRemeasure_ = false;
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
			WApplication.getInstance().addAutoJavaScript(
					app.getJavaScriptClass() + ".layouts2.adjustNow();");
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
		this.needAdjust_ = this.needConfigUpdate_ = this.needRemeasure_ = false;
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
		if (this.needRemeasure_) {
			this.needRemeasure_ = false;
			StringBuilder js = new StringBuilder();
			js.append(app.getJavaScriptClass()).append(".layouts2.setDirty('")
					.append(this.getId()).append("');");
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
				if (this.grid_.items_.get(row).get(col).item_ == item
						&& !this.grid_.items_.get(row).get(col).update_) {
					this.grid_.items_.get(row).get(col).update_ = true;
					this.needAdjust_ = true;
					return true;
				}
			}
		}
		return false;
	}

	public boolean isParentResized() {
		if (!this.needRemeasure_) {
			this.needRemeasure_ = true;
			return true;
		} else {
			return false;
		}
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
	private boolean needRemeasure_;
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
					js.append("dirty:2,id:'").append(id).append("'")
							.append("}");
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
				"function(B,G,J,N,O,L,T,u,H,z,x){function y(a,c,b,g){function h(m){return m==\"visible\"||m==\"none\"}var j=t[c],k=c?a.scrollHeight:a.scrollWidth,s,d;if(c==0&&k+e.pxself(a,j.left)>=g.clientWidth){s=a.style[j.left];v(a,j.left,\"-1000000px\");k=c?a.scrollHeight:a.scrollWidth}g=c?a.clientHeight:a.clientWidth;if(e.isGecko&&c==0&&h(e.css(a,\"overflow\"))){d=a.style[j.size];v(a,j.size,\"\")}c=c?a.offsetHeight:a.offsetWidth;s&&v(a,j.left,s);d&&v(a,j.size,d); if(g>=1E6)g-=1E6;if(k>=1E6)k-=1E6;if(c>=1E6)c-=1E6;if(k===0){k=e.pxself(a,j.size);if(k!==0&&!e.isOpera&&!e.isGecko)k-=e.px(a,\"border\"+j.Left+\"Width\")+e.px(a,\"border\"+j.Right+\"Width\")}if(e.isIE&&(e.hasTag(a,\"BUTTON\")||e.hasTag(a,\"TEXTAREA\")||e.hasTag(a,\"INPUT\")||e.hasTag(a,\"SELECT\")))k=g;if(k>c)if(e.pxself(a,j.size)==0)k=0;else{var n=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!==\"none\")n=true});if(n)k=0}if(b)return k;e.isOpera||(k+=e.px(a,\"border\"+j.Left+\"Width\")+e.px(a,\"border\"+ j.Right+\"Width\"));k+=e.px(a,\"margin\"+j.Left)+e.px(a,\"margin\"+j.Right);if(!e.boxSizing(a)&&!e.isIE)k+=e.px(a,\"padding\"+j.Left)+e.px(a,\"padding\"+j.Right);if(k<c)k=c;a=e.px(a,\"max\"+j.Size);if(a>0)k=Math.min(a,k);return Math.round(k)}function C(a,c){c=t[c];if(a.style.display===\"none\")return 0;else if(a[\"layoutMin\"+c.Size])return a[\"layoutMin\"+c.Size];else{var b=e.px(a,\"min\"+c.Size);e.boxSizing(a)||(b+=e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right));return b}}function w(a,c){c=t[c];var b=e.px(a,\"margin\"+ c.Left)+e.px(a,\"margin\"+c.Right);if(!e.boxSizing(a)&&!(e.isIE&&!e.isIElt9&&e.hasTag(a,\"BUTTON\")))b+=e.px(a,\"border\"+c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\")+e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right);return b}function K(a,c){c=t[c];return e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right)}function I(a,c){if(e.boxSizing(a)){c=t[c];return e.px(a,\"border\"+c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\")+e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right)}else return 0}function U(a,c){c= t[c];return Math.round(e.px(a,\"border\"+c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\")+e.px(a,\"margin\"+c.Left)+e.px(a,\"margin\"+c.Right)+e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right))}function P(a,c,b){a.dirty=Math.max(a.dirty,c);V=true;b&&B.layouts2.scheduleAdjust()}function v(a,c,b){if(a.style[c]!==b){a.style[c]=b;return true}else return false}function da(a,c,b){var g=t[a],h=g.config.length,j=t[a^1].config.length,k=g.measures.slice();if(k.length==5){k[0]=k[0].slice();k[1]=k[1].slice()}if(V){if(b&& typeof g.minSize==\"undefined\"){g.minSize=e.px(b,\"min\"+g.Size);if(g.minSize>0)g.minSize-=I(b,a)}var s=[],d=[],n=0,m=0,i,D,q=false;for(i=0;i<h;++i){var l=0,r=g.config[i][2],p=true;for(D=0;D<j;++D){var f=g.getItem(i,D);if(f){if(!f.w||a==0&&f.dirty>1){var o=$(\"#\"+f.id),A=o.get(0);if(!A){g.setItem(i,D,null);continue}if(A!=f.w){f.w=A;o.find(\"img\").add(o.filter(\"img\")).bind(\"load\",{item:f},function(ea){P(ea.data.item,1,true)})}}if(!L&&f.w.style.position!=\"absolute\"){f.w.style.position=\"absolute\";f.w.style.visibility= \"hidden\"}if(!f.ps)f.ps=[];if(!f.ms)f.ms=[];if(!f.size)f.size=[];if(!f.psize)f.psize=[];if(!f.fs)f.fs=[];if(!f.margin)f.margin=[];if($(f.w).hasClass(\"Wt-hidden\"))f.ps[a]=f.ms[a]=0;else{o=!f.set;if(!f.set)f.set=[false,false];if(f.w){if(e.isIE)f.w.style.visibility=\"\";if(f.dirty){var E;if(f.dirty>1){C(f.w,a);f.ms[a]=E}else E=f.ms[a];if(E>r)r=E;if(f.dirty>1)f.margin[a]=w(f.w,a);if(!f.set[a])if(a==0||!o){o=e.pxself(f.w,g.size);f.fs[a]=o?o+f.margin[a]:0}else{o=Math.round(e.px(f.w,g.size));f.fs[a]=o>Math.max(I(f.w, a),E)?o+f.margin[a]:0}o=f.fs[a];if(f.layout){if(o==0)o=f.ps[a]}else{if(f.wasLayout){f.wasLayout=false;f.set=[false,false];f.ps=[];f.w.wtResize&&f.w.wtResize(f.w,-1,-1,true);v(f.w,t[1].size,\"\")}A=y(f.w,a,false,c);var M=f.set[a];if(M)if(f.psize[a]>8)M=A>=f.psize[a]-4&&A<=f.psize[a]+4;var Z=typeof f.ps[a]!==\"undefined\"&&g.config[i][0]>0&&f.set[a];o=M||Z?Math.max(o,f.ps[a]):Math.max(o,A)}f.ps[a]=o;if(!f.span||f.span[a]==1){if(o>l)l=o}else q=true}else if(!f.span||f.span[a]==1){if(f.ps[a]>l)l=f.ps[a];if(f.ms[a]> r)r=f.ms[a]}else q=true;if(!(f.w.style.display===\"none\"&&!f.w.ed)&&(!f.span||f.span[a]==1))p=false}}}}if(p)r=l=-1;else if(r>l)l=r;s[i]=l;d[i]=r;if(r>-1){n+=l;m+=r}}if(q)for(i=0;i<h;++i)for(D=0;D<j;++D)if((f=g.getItem(i,D))&&f.span&&f.span[a]>1){c=f.ps[a];for(si=l=q=0;si<f.span[a];++si){r=s[i+si];if(r!=-1){c-=r;++q;if(g.config[i+si][0]>0)l+=g.config[i+si][0]}}if(c>0)if(q>0){if(l>0)q=l;for(si=0;si<f.span[a];++si){r=s[i+si];if(r!=-1){r=l>0?g.config[i+si][0]:1;if(r>0){p=Math.round(c/r);c-=p;q-=r;s[i+ si]+=p}}}}else s[i]=c}j=0;o=true;D=false;for(i=0;i<h;++i)if(d[i]>-1){if(o){j+=g.margins[1];o=false}else{j+=g.margins[0];if(D)j+=4}D=g.config[i][1]!==0}o||(j+=g.margins[2]);n+=j;m+=j;g.measures=[s,d,n,m,j]}if(W||k[2]!=g.measures[2])Q.updateSizeInParent(a);b&&g.minSize==0&&k[3]!=g.measures[3]&&b.parentNode.className!=\"Wt-domRoot\"&&v(b,\"min\"+g.Size,g.measures[3]+\"px\")&&Q.ancestor&&Q.ancestor.setContentsDirty(b);b&&a==0&&b&&e.hasTag(b,\"TD\")&&v(b,g.size,g.measures[2]+\"px\")}function fa(a,c,b){a=t[a];if(X)b= -b;if(a.config[c][0]>0&&a.config[c+1][0]==0){++c;b=-b}a.fixedSize[c]=a.sizes[c]+b;B.layouts2.scheduleAdjust()}function ga(a,c,b){var g=c.di,h=t[a],j=t[a^1],k,s=e.getElement(G),d;for(d=g-1;d>=0;--d)if(h.sizes[d]>=0){k=-(h.sizes[d]-h.measures[1][d]);break}g=h.sizes[g]-h.measures[1][g];if(X){var n=k;k=-g;g=-n}new e.SizeHandle(e,h.resizeDir,e.pxself(c,h.size),e.pxself(c,j.size),k,g,h.resizerClass,function(m){fa(a,d,m)},c,s,b,0,0)}function ha(a,c){var b=t[a],g=t[a^1],h=b.measures,j=0,k=false,s=false,d= false,n=aa?c.parentNode:null;if(b.maxSize===0)if(n){var m=e.css(n,\"position\");if(m===\"absolute\")j=e.pxself(n,b.size);if(j===0){if(!b.initialized){if(a===0&&(m===\"absolute\"||m===\"fixed\")){n.style.display=\"none\";j=n.clientWidth;n.style.display=\"\"}j=a?n.clientHeight:n.clientWidth;k=true;if(a==0&&j==0&&e.isIElt9){j=n.offsetWidth;k=false}var i;if((e.hasTag(n,\"TD\")||e.hasTag(n,\"TH\"))&&!(e.isIE&&!e.isIElt9)){d=0;i=1}else{d=b.minSize?b.minSize:h[3];i=0}function D(M,Z){return Math.abs(M-Z)<1}if(e.isIElt9&& D(j,i)||D(j,d+K(n,a)))b.maxSize=999999}if(j===0&&b.maxSize===0){j=a?n.clientHeight:n.clientWidth;k=true}}}else{j=e.pxself(c,b.size);s=true}else if(b.sizeSet){j=e.pxself(n,b.size);s=true}var q=0;if(n&&n.wtGetPS&&a==1)q=n.wtGetPS(n,c,a,0);d=h[2];if(d<b.minSize)d=b.minSize;if(b.maxSize&&!b.sizeSet){j=Math.min(d,b.maxSize)+q;v(n,b.size,j+I(n,a)+\"px\")&&B.layouts2.remeasure();j=j;d=s=true}b.cSize=j;if(a==1&&n&&n.wtResize){i=g.cSize;d=b.cSize;n.wtResize(n,Math.round(i),Math.round(d),true)}j-=q;if(!s){s= 0;if(typeof b.cPadding===\"undefined\"){s=k?K(n,a):I(n,a);b.cPadding=s}else s=b.cPadding;j-=s}b.initialized=true;if(!(n&&j<=0)){if(j<h[3]-q)j=h[3]-q;k=[];n=b.config.length;s=g.config.length;for(d=0;d<n;++d)b.stretched[d]=false;if(j>=h[3]-q){q=j-h[4];i=[];var l=[0,0],r=[0,0],p=0;for(d=0;d<n;++d)if(h[1][d]>-1){m=-1;if(typeof b.fixedSize[d]!==\"undefined\"&&(d+1==n||h[1][d+1]>-1))m=b.fixedSize[d];else if(b.config[d][1]!==0&&b.config[d][1][0]>=0){m=b.config[d][1][0];if(b.config[d][1][1])m=(j-h[4])*m/100}if(m>= 0){i[d]=-1;k[d]=m;q-=k[d]}else{if(b.config[d][0]>0){m=1;i[d]=b.config[d][0];p+=i[d]}else{m=0;i[d]=0}l[m]+=h[1][d];r[m]+=h[0][d];k[d]=h[0][d]}}else{i[d]=-2;k[d]=-1}if(p==0){for(d=0;d<n;++d)if(i[d]==0){i[d]=1;++p}r[1]=r[0];l[1]=l[0];r[0]=0;l[0]=0}if(q>r[0]+l[1]){q-=r[0];if(q>r[1]){if(b.fitSize){q-=r[1];q=q/p;for(d=0;d<n;++d)if(i[d]>0){k[d]+=Math.round(i[d]*q);b.stretched[d]=true}}}else{m=1;if(q<l[m])q=l[m];q=r[m]-l[m]>0?(q-l[m])/(r[m]-l[m]):0;for(d=0;d<n;++d)if(i[d]>0){l=h[0][d]-h[1][d];k[d]=h[1][d]+ Math.round(l*q)}}}else{for(d=0;d<n;++d)if(i[d]>0)k[d]=h[1][d];q-=l[1];m=0;if(q<l[m])q=l[m];q=r[m]-l[m]>0?(q-l[m])/(r[m]-l[m]):0;for(d=0;d<n;++d)if(i[d]==0){l=h[0][d]-h[1][d];k[d]=h[1][d]+Math.round(l*q)}}}else k=h[1];b.sizes=k;h=b.margins[1];q=true;l=false;for(d=0;d<n;++d){if(k[d]>-1){var f=l;if(l){i=G+\"-rs\"+a+\"-\"+d;l=e.getElement(i);if(!l){l=document.createElement(\"div\");l.setAttribute(\"id\",i);l.di=d;l.style.position=\"absolute\";l.style[g.left]=g.margins[1]+\"px\";l.style[b.size]=b.margins[0]+\"px\"; if(g.cSize)l.style[g.size]=g.cSize-g.margins[2]-g.margins[1]+\"px\";l.className=b.handleClass;c.insertBefore(l,c.firstChild);l.onmousedown=l.ontouchstart=function(M){ga(a,this,M||window.event)}}h+=2;v(l,b.left,h+\"px\");h+=2}l=b.config[d][1]!==0;if(q)q=false;else h+=b.margins[0]}for(r=0;r<s;++r)if((p=b.getItem(d,r))&&p.w){i=p.w;m=Math.max(k[d],0);if(p.span){var o,A=l;for(o=1;o<p.span[a];++o){if(d+o>=k.length)break;if(A)m+=4;A=b.config[d+o][1]!==0;if(k[d+o-1]>-1&&k[d+o]>-1)m+=b.margins[0];m+=k[d+o]}}var E; v(i,\"visibility\",\"\");A=p.align>>b.alignBits&15;o=p.ps[a];if(m<o)A=0;if(A){switch(A){case 1:E=h;break;case 4:E=h+(m-o)/2;break;case 2:E=h+(m-o);break}o-=p.margin[a];if(p.layout){v(i,b.size,o+\"px\")&&P(p,1);p.set[a]=true}else if(m>=o&&p.set[a]){v(i,b.size,o+\"px\")&&P(p,1);p.set[a]=false}p.size[a]=o;p.psize[a]=o}else{A=Math.max(0,m-p.margin[a]);if(!e.isIE&&e.hasTag(i,\"TEXTAREA\"))A=m;E=false;if(e.isIE&&e.hasTag(i,\"BUTTON\"))E=true;if(!(i.style.display===\"none\"&&!i.ed)&&(E||m!=o||p.layout)){v(i,b.size,A+ \"px\")&&P(p,1);p.set[a]=true}else if(p.fs[a])a==0&&v(i,b.size,p.fs[a]+\"px\");else{v(i,b.size,\"\")&&P(p,1);p.set[a]=false}E=h;p.size[a]=A;p.psize[a]=m}if(L)if(f){v(i,b.left,\"4px\");m=e.css(i,\"position\");if(m!==\"absolute\")i.style.position=\"relative\"}else v(i,b.left,\"0px\");else v(i,b.left,E+\"px\");if(a==1){if(i.wtResize)i.wtResize(i,p.set[0]?Math.round(p.size[0]):-1,p.set[1]?Math.round(p.size[1]):-1,true);p.dirty=0}}if(k[d]>-1)h+=k[d]}$(c).children(\".\"+g.handleClass).css(b.size,j-b.margins[2]-b.margins[1]+ \"px\")}}var e=B.WT;this.ancestor=null;this.descendants=[];var Q=this,F=x,V=true,W=true,aa=false,Y=null,R=null,ba=false,S=[],ca=false,X=$(document.body).hasClass(\"Wt-rtl\"),t=[{initialized:false,config:F.cols,margins:H,maxSize:T,measures:[],sizes:[],stretched:[],fixedSize:[],Left:X?\"Right\":\"Left\",left:X?\"right\":\"left\",Right:X?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,c){return F.items[c*t[0].config.length+a]},setItem:function(a,c,b){F.items[c*t[0].config.length+a]=b},handleClass:\"Wt-vrh2\", resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:N},{initialized:false,config:F.rows,margins:z,maxSize:u,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,c){return F.items[a*t[0].config.length+c]},setItem:function(a,c,b){F.items[a*t[0].config.length+c]=b},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:O}];jQuery.data(document.getElementById(G),\"layout\",this);this.updateSizeInParent=function(a){if(Y){var c= t[a],b=c.measures[2];if(c.maxSize>0)b=Math.min(c.maxSize,b);if(ca){c=e.getElement(G);if(!c)return;for(var g=c,h=g.parentNode;;){if(h.wtGetPS)b=h.wtGetPS(h,g,a,b);b+=U(h,a);if(h==R)break;if(a==1&&h==c.parentNode&&!h.lh&&h.offsetHeight>b)b=h.offsetHeight;g=h;h=g.parentNode}}else b+=S[a];Y.setChildSize(R,a,b)}};this.setConfig=function(a){var c=F;F=a;t[0].config=F.cols;t[1].config=F.rows;t[0].stretched=[];t[1].stretched=[];var b;a=0;for(b=c.items.length;a<b;++a){var g=c.items[a];if(g){if(g.set){g.set[0]&& v(g.w,t[0].size,\"\");g.set[1]&&v(g.w,t[1].size,\"\")}if(g.layout){Q.setChildSize(g.w,0,g.ps[0]);Q.setChildSize(g.w,1,g.ps[1])}}}W=true;B.layouts2.scheduleAdjust()};this.getId=function(){return G};this.setItemsDirty=function(a){var c,b,g=t[0].config.length;c=0;for(b=a.length;c<b;++c){var h=F.items[a[c][0]*g+a[c][1]];h.dirty=2;if(h.layout){h.layout=false;h.wasLayout=true;B.layouts2.setChildLayoutsDirty(Q,h.w)}}V=true};this.setDirty=function(){W=true};this.setChildSize=function(a,c,b){var g=t[0].config.length, h=t[c],j,k;j=0;for(k=F.items.length;j<k;++j){var s=F.items[j];if(s&&s.id==a.id){a=c===0?j%g:j/g;if(s.align>>h.alignBits&15||!h.stretched[a]){if(!s.ps)s.ps=[];s.ps[c]=b}s.layout=true;P(s,1);break}}};this.measure=function(a){var c=e.getElement(G);if(c)if(!e.isHidden(c)){if(!ba){ba=true;if(aa=J==null){var b=c;b=b.parentNode;for(S=[0,0];;){S[0]+=U(b,0);S[1]+=U(b,1);if(b.wtGetPS)ca=true;var g=jQuery.data(b.parentNode,\"layout\");if(g){Y=g;R=b;break}b=b;b=b.parentNode;if(b.childNodes.length!=1&&!b.wtGetPS)break}b= c.parentNode;for(g=0;g<2;++g)t[g].sizeSet=e.pxself(b,t[g].size)!=0}else{Y=jQuery.data(document.getElementById(J),\"layout\");R=c;S[0]=U(R,0);S[1]=U(R,1)}}if(V||W){b=aa?c.parentNode:null;da(a,c,b)}if(a==1)V=W=false}};this.setMaxSize=function(a,c){t[0].maxSize=a;t[1].maxSize=c};this.apply=function(a){var c=e.getElement(G);if(!c)return false;if(e.isHidden(c))return true;ha(a,c);return true};this.contains=function(a){var c=e.getElement(G);a=e.getElement(a.getId());return c&&a?e.contains(c,a):false};this.WT= e}");
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
				"new (function(){var B=[],G=false,J=this,N=false;this.find=function(u){return jQuery.data(document.getElementById(u),\"layout\")};this.setDirty=function(u){if(u=this.find(u)){u.setDirty();J.scheduleAdjust()}};this.setChildLayoutsDirty=function(u,H){var z,x;z=0;for(x=u.descendants.length;z<x;++z){var y=u.descendants[z];if(H){var C=u.WT.getElement(y.getId());if(C&&!u.WT.contains(H,C))continue}y.setDirty()}};this.add=function(u){function H(z,x){var y, C;y=0;for(C=z.length;y<C;++y){var w=z[y];if(w.getId()==x.getId()){z[y]=x;x.descendants=w.descendants;return}else if(w.contains(x)){H(w.descendants,x);return}else if(x.contains(w)){x.descendants.push(w);z.splice(y,1);--y;--C}}z.push(x)}H(B,u);J.scheduleAdjust()};var O=false,L=false;this.scheduleAdjust=function(u){if(u)N=true;if(!O){O=true;setTimeout(function(){J.adjust()},0)}};this.adjust=function(u,H){function z(y,C){var w,K;w=0;for(K=y.length;w<K;++w){var I=y[w];z(I.descendants,C);C==1&&N&&I.setDirty(); I.measure(C)}}function x(y,C){var w,K;w=0;for(K=y.length;w<K;++w){var I=y[w];if(I.apply(C))x(I.descendants,C);else{y.splice(w,1);--w;--K}}}if(u){(u=this.find(u))&&u.setItemsDirty(H);J.scheduleAdjust()}else{O=false;if(!G){G=true;L=false;z(B,0);x(B,0);z(B,1);x(B,1);if(L){z(B,0);x(B,0);z(B,1);x(B,1)}N=L=G=false}}};this.updateConfig=function(u,H){(u=this.find(u))&&u.setConfig(H)};this.remeasure=function(){L=true};this.adjustNow=function(){O&&J.adjust()};var T=null;window.onresize=function(){clearTimeout(T); T=setTimeout(function(){T=null;J.scheduleAdjust(true)},20)};window.onshow=function(){N=true;J.adjust()}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(a,e,c,d){function i(j){var k=b.px(j,\"marginTop\");k+=b.px(j,\"marginBottom\");if(!b.boxSizing(j)){k+=b.px(j,\"borderTopWidth\");k+=b.px(j,\"borderBottomWidth\");k+=b.px(j,\"paddingTop\");k+=b.px(j,\"paddingBottom\")}return k}var b=this,h=c>=0;a.lh=h&&d;a.style.height=h?c+\"px\":\"\";if(b.boxSizing(a)){c-=b.px(a,\"marginTop\");c-=b.px(a,\"marginBottom\");c-=b.px(a,\"borderTopWidth\");c-=b.px(a,\"borderBottomWidth\");c-=b.px(a,\"paddingTop\");c-=b.px(a,\"paddingBottom\"); e-=b.px(a,\"marginLeft\");e-=b.px(a,\"marginRight\");e-=b.px(a,\"borderLeftWidth\");e-=b.px(a,\"borderRightWidth\");e-=b.px(a,\"paddingLeft\");e-=b.px(a,\"paddingRight\")}var g,m,f;g=0;for(m=a.childNodes.length;g<m;++g){f=a.childNodes[g];if(f.nodeType==1)if(h){var l=c-i(f);if(l>0)if(f.wtResize)f.wtResize(f,e,l,d);else{l=l+\"px\";if(f.style.height!=l){f.style.height=l;f.lh=d}}}else if(f.wtResize)f.wtResize(f,e,-1);else{f.style.height=\"\";f.lh=false}}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "ChildrenGetPS",
				"function(a,e,c,d){return d}");
	}

	static WJavaScriptPreamble wtjs12() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastResize",
				"function(a,e,c,d){var i=this,b=c>=0;a.lh=b&&d;a.style.height=b?c+\"px\":\"\";a=a.lastChild;var h=a.previousSibling;if(b){c-=h.offsetHeight+i.px(h,\"marginTop\")+i.px(h,\"marginBottom\");if(c>0)if(a.wtResize)a.wtResize(a,e,c,d);else{a.style.height=c+\"px\";a.lh=d}}else if(a.wtResize)a.wtResize(a,-1,-1);else{a.style.height=\"\";a.lh=false}}");
	}

	static WJavaScriptPreamble wtjs13() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastGetPS",
				"function(a,e,c,d){var i=this,b,h;if(e.wtGetPS)return d;b=0;for(h=a.childNodes.length;b<h;++b){var g=a.childNodes[b];if(g!=e){var m=i.css(g,\"position\");if(m!=\"absolute\"&&m!=\"fixed\")if(c===0)d=Math.max(d,g.offsetWidth);else d+=g.offsetHeight+i.px(g,\"marginTop\")+i.px(g,\"marginBottom\")}}return d}");
	}
}
