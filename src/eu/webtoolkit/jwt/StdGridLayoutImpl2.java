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
				"function(C,G,J,O,P,L,U,u,H,A,x){function y(a,c,b,g){function h(m){return m==\"visible\"||m==\"none\"}var k=s[c],j=c?a.scrollHeight:a.scrollWidth,r,d;if(c==0&&j+e.pxself(a,k.left)>=g.clientWidth){r=a.style[k.left];v(a,k.left,\"-1000000px\");j=c?a.scrollHeight:a.scrollWidth}g=c?a.clientHeight:a.clientWidth;if(e.isGecko&&c==0&&h(e.css(a,\"overflow\"))){d=a.style[k.size];v(a,k.size,\"\")}c=c?a.offsetHeight:a.offsetWidth;r&&v(a,k.left,r);d&&v(a,k.size,d); if(g>=1E6)g-=1E6;if(j>=1E6)j-=1E6;if(c>=1E6)c-=1E6;if(j===0){j=e.pxself(a,k.size);if(j!==0&&!e.isOpera&&!e.isGecko)j-=e.px(a,\"border\"+k.Left+\"Width\")+e.px(a,\"border\"+k.Right+\"Width\")}if(e.isIE&&(e.hasTag(a,\"BUTTON\")||e.hasTag(a,\"TEXTAREA\")||e.hasTag(a,\"INPUT\")||e.hasTag(a,\"SELECT\")))j=g;if(j>c)if(e.pxself(a,k.size)==0)j=0;else{var n=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!==\"none\")n=true});if(n)j=0}if(b)return j;e.isOpera||(j+=e.px(a,\"border\"+k.Left+\"Width\")+e.px(a,\"border\"+ k.Right+\"Width\"));j+=e.px(a,\"margin\"+k.Left)+e.px(a,\"margin\"+k.Right);if(!e.boxSizing(a)&&!e.isIE)j+=e.px(a,\"padding\"+k.Left)+e.px(a,\"padding\"+k.Right);if(j<c)j=c;a=e.px(a,\"max\"+k.Size);if(a>0)j=Math.min(a,j);return Math.round(j)}function D(a,c){c=s[c];if(a.style.display===\"none\")return 0;else if(a[\"layoutMin\"+c.Size])return a[\"layoutMin\"+c.Size];else{var b=e.px(a,\"min\"+c.Size);e.boxSizing(a)||(b+=e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right));return b}}function w(a,c){c=s[c];var b=e.px(a,\"margin\"+ c.Left)+e.px(a,\"margin\"+c.Right);if(!e.boxSizing(a)&&!(e.isIE&&!e.isIElt9&&e.hasTag(a,\"BUTTON\")))b+=e.px(a,\"border\"+c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\")+e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right);return b}function K(a,c){c=s[c];return e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right)}function I(a,c){if(e.boxSizing(a)){c=s[c];return e.px(a,\"border\"+c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\")+e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right)}else return 0}function V(a,c){c= s[c];return Math.round(e.px(a,\"border\"+c.Left+\"Width\")+e.px(a,\"border\"+c.Right+\"Width\")+e.px(a,\"margin\"+c.Left)+e.px(a,\"margin\"+c.Right)+e.px(a,\"padding\"+c.Left)+e.px(a,\"padding\"+c.Right))}function Q(a,c,b){a.dirty=Math.max(a.dirty,c);W=true;b&&C.layouts2.scheduleAdjust()}function v(a,c,b){if(a.style[c]!==b){a.style[c]=b;return true}else return false}function da(a,c,b){var g=s[a],h=g.measures,k=g.config.length,j=s[a^1].config.length;if(W||M){if(b&&typeof g.minSize==\"undefined\"){g.minSize=e.px(b,\"min\"+ g.Size);if(g.minSize>0)g.minSize-=I(b,a)}h=h.slice();if(h.length==5){h[0]=h[0].slice();h[1]=h[1].slice()}var r=[],d=[],n=0,m=0,i,E,p=false;for(i=0;i<k;++i){var l=0,t=g.config[i][2],q=true;for(E=0;E<j;++E){var f=g.getItem(i,E);if(f){if(!f.w||a==0&&f.dirty>1){var o=$(\"#\"+f.id),B=o.get(0);if(!B){g.setItem(i,E,null);continue}if(B!=f.w){f.w=B;o.find(\"img\").add(o.filter(\"img\")).bind(\"load\",{item:f},function(ea){Q(ea.data.item,1,true)})}}if(!L&&f.w.style.position!=\"absolute\"){f.w.style.position=\"absolute\"; f.w.style.visibility=\"hidden\"}if(!f.ps)f.ps=[];if(!f.ms)f.ms=[];if(!f.size)f.size=[];if(!f.psize)f.psize=[];if(!f.fs)f.fs=[];if(!f.margin)f.margin=[];if($(f.w).hasClass(\"Wt-hidden\"))f.ps[a]=f.ms[a]=0;else{o=!f.set;if(!f.set)f.set=[false,false];if(f.w){if(e.isIE)f.w.style.visibility=\"\";if(f.dirty||M){var z;if(f.dirty>1){D(f.w,a);f.ms[a]=z}else z=f.ms[a];if(z>t)t=z;if(f.dirty>1)f.margin[a]=w(f.w,a);if(!f.set[a])if(a==0||!o){o=e.pxself(f.w,g.size);f.fs[a]=o?o+f.margin[a]:0}else{o=Math.round(e.px(f.w, g.size));f.fs[a]=o>Math.max(I(f.w,a),z)?o+f.margin[a]:0}o=f.fs[a];if(f.layout){if(o==0)o=f.ps[a]}else{if(f.wasLayout){f.wasLayout=false;f.set=[false,false];f.ps=[];f.w.wtResize&&f.w.wtResize(f.w,-1,-1,true);v(f.w,s[1].size,\"\")}B=y(f.w,a,false,c);var N=f.set[a];if(N)if(f.psize[a]>8)N=B>=f.psize[a]-4&&B<=f.psize[a]+4;var Z=typeof f.ps[a]!==\"undefined\"&&g.config[i][0]>0&&f.set[a];o=N||Z?Math.max(o,f.ps[a]):Math.max(o,B)}f.ps[a]=o;if(!f.span||f.span[a]==1){if(o>l)l=o}else p=true}else if(!f.span||f.span[a]== 1){if(f.ps[a]>l)l=f.ps[a];if(f.ms[a]>t)t=f.ms[a]}else p=true;if(!(f.w.style.display===\"none\"&&!f.w.ed)&&(!f.span||f.span[a]==1))q=false}}}}if(q)t=l=-1;else if(t>l)l=t;r[i]=l;d[i]=t;if(t>-1){n+=l;m+=t}}if(p)for(i=0;i<k;++i)for(E=0;E<j;++E)if((f=g.getItem(i,E))&&f.span&&f.span[a]>1){c=f.ps[a];for(si=p=z=0;si<f.span[a];++si){l=r[i+si];if(l!=-1){c-=l;++z;if(g.config[i+si][0]>0)p+=g.config[i+si][0]}}if(c>0)if(z>0){if(p>0)z=p;for(si=0;si<f.span[a];++si){l=r[i+si];if(l!=-1){l=p>0?g.config[i+si][0]:1;if(l> 0){t=Math.round(c/l);c-=t;z-=l;r[i+si]+=t}}}}else r[i]=c}j=0;o=true;E=false;for(i=0;i<k;++i)if(d[i]>-1){if(o){j+=g.margins[1];o=false}else{j+=g.margins[0];if(E)j+=4}E=g.config[i][1]!==0}o||(j+=g.margins[2]);n+=j;m+=j;g.measures=[r,d,n,m,j];if(M||h[2]!=g.measures[2])R.updateSizeInParent(a);b&&g.minSize==0&&h[3]!=g.measures[3]&&b.parentNode.className!=\"Wt-domRoot\"&&v(b,\"min\"+g.Size,g.measures[3]+\"px\")&&R.ancestor&&R.ancestor.setContentsDirty(b);b&&a==0&&b&&e.hasTag(b,\"TD\")&&v(b,g.size,g.measures[2]+ \"px\")}}function fa(a,c,b){a=s[a];if(X)b=-b;if(a.config[c][0]>0&&a.config[c+1][0]==0){++c;b=-b}a.fixedSize[c]=a.sizes[c]+b;C.layouts2.scheduleAdjust()}function ga(a,c,b){var g=c.di,h=s[a],k=s[a^1],j,r=e.getElement(G),d;for(d=g-1;d>=0;--d)if(h.sizes[d]>=0){j=-(h.sizes[d]-h.measures[1][d]);break}g=h.sizes[g]-h.measures[1][g];if(X){var n=j;j=-g;g=-n}new e.SizeHandle(e,h.resizeDir,e.pxself(c,h.size),e.pxself(c,k.size),j,g,h.resizerClass,function(m){fa(a,d,m)},c,r,b,0,0)}function ha(a,c){var b=s[a],g=s[a^ 1],h=b.measures,k=0,j=false,r=false,d=false,n=aa?c.parentNode:null;if(b.maxSize===0)if(n){var m=e.css(n,\"position\");if(m===\"absolute\")k=e.pxself(n,b.size);if(k===0){if(!b.initialized){if(a===0&&(m===\"absolute\"||m===\"fixed\")){n.style.display=\"none\";k=n.clientWidth;n.style.display=\"\"}k=a?n.clientHeight:n.clientWidth;j=true;if(a==0&&k==0&&e.isIElt9){k=n.offsetWidth;j=false}var i;if((e.hasTag(n,\"TD\")||e.hasTag(n,\"TH\"))&&!(e.isIE&&!e.isIElt9)){d=0;i=1}else{d=b.minSize?b.minSize:h[3];i=0}function E(N,Z){return Math.abs(N- Z)<1}if(e.isIElt9&&E(k,i)||E(k,d+K(n,a)))b.maxSize=999999}if(k===0&&b.maxSize===0){k=a?n.clientHeight:n.clientWidth;j=true}}}else{k=e.pxself(c,b.size);r=true}else if(b.sizeSet){k=e.pxself(n,b.size);r=true}var p=0;if(n&&n.wtGetPS&&a==1)p=n.wtGetPS(n,c,a,0);d=h[2];if(d<b.minSize)d=b.minSize;if(b.maxSize&&!b.sizeSet){k=Math.min(d,b.maxSize)+p;v(n,b.size,k+I(n,a)+\"px\")&&C.layouts2.remeasure();k=k;d=r=true}b.cSize=k;if(a==1&&n&&n.wtResize){i=g.cSize;d=b.cSize;n.wtResize(n,Math.round(i),Math.round(d),true)}k-= p;if(!r){r=0;if(typeof b.cPadding===\"undefined\"){r=j?K(n,a):I(n,a);b.cPadding=r}else r=b.cPadding;k-=r}b.initialized=true;if(!(n&&k<=0)){if(k<h[3]-p)k=h[3]-p;j=[];n=b.config.length;r=g.config.length;for(d=0;d<n;++d)b.stretched[d]=false;if(k>=h[3]-p){p=k-h[4];i=[];var l=[0,0],t=[0,0],q=0;for(d=0;d<n;++d)if(h[1][d]>-1){m=-1;if(typeof b.fixedSize[d]!==\"undefined\"&&(d+1==n||h[1][d+1]>-1))m=b.fixedSize[d];else if(b.config[d][1]!==0&&b.config[d][1][0]>=0){m=b.config[d][1][0];if(b.config[d][1][1])m=(k-h[4])* m/100}if(m>=0){i[d]=-1;j[d]=m;p-=j[d]}else{if(b.config[d][0]>0){m=1;i[d]=b.config[d][0];q+=i[d]}else{m=0;i[d]=0}l[m]+=h[1][d];t[m]+=h[0][d];j[d]=h[0][d]}}else{i[d]=-2;j[d]=-1}if(q==0){for(d=0;d<n;++d)if(i[d]==0){i[d]=1;++q}t[1]=t[0];l[1]=l[0];t[0]=0;l[0]=0}if(p>t[0]+l[1]){p-=t[0];if(p>t[1]){if(b.fitSize){p-=t[1];p=p/q;for(d=0;d<n;++d)if(i[d]>0){j[d]+=Math.round(i[d]*p);b.stretched[d]=true}}}else{m=1;if(p<l[m])p=l[m];p=t[m]-l[m]>0?(p-l[m])/(t[m]-l[m]):0;for(d=0;d<n;++d)if(i[d]>0){l=h[0][d]-h[1][d]; j[d]=h[1][d]+Math.round(l*p)}}}else{for(d=0;d<n;++d)if(i[d]>0)j[d]=h[1][d];p-=l[1];m=0;if(p<l[m])p=l[m];p=t[m]-l[m]>0?(p-l[m])/(t[m]-l[m]):0;for(d=0;d<n;++d)if(i[d]==0){l=h[0][d]-h[1][d];j[d]=h[1][d]+Math.round(l*p)}}}else j=h[1];b.sizes=j;h=b.margins[1];p=true;l=false;for(d=0;d<n;++d){if(j[d]>-1){var f=l;if(l){i=G+\"-rs\"+a+\"-\"+d;l=e.getElement(i);if(!l){l=document.createElement(\"div\");l.setAttribute(\"id\",i);l.di=d;l.style.position=\"absolute\";l.style[g.left]=g.margins[1]+\"px\";l.style[b.size]=b.margins[0]+ \"px\";if(g.cSize)l.style[g.size]=g.cSize-g.margins[2]-g.margins[1]+\"px\";l.className=b.handleClass;c.insertBefore(l,c.firstChild);l.onmousedown=l.ontouchstart=function(N){ga(a,this,N||window.event)}}h+=2;v(l,b.left,h+\"px\");h+=2}l=b.config[d][1]!==0;if(p)p=false;else h+=b.margins[0]}for(t=0;t<r;++t)if((q=b.getItem(d,t))&&q.w){i=q.w;m=Math.max(j[d],0);if(q.span){var o,B=l;for(o=1;o<q.span[a];++o){if(d+o>=j.length)break;if(B)m+=4;B=b.config[d+o][1]!==0;if(j[d+o-1]>-1&&j[d+o]>-1)m+=b.margins[0];m+=j[d+ o]}}var z;v(i,\"visibility\",\"\");B=q.align>>b.alignBits&15;o=q.ps[a];if(m<o)B=0;if(B){switch(B){case 1:z=h;break;case 4:z=h+(m-o)/2;break;case 2:z=h+(m-o);break}o-=q.margin[a];if(q.layout){v(i,b.size,o+\"px\")&&Q(q,1);q.set[a]=true}else if(m>=o&&q.set[a]){v(i,b.size,o+\"px\")&&Q(q,1);q.set[a]=false}q.size[a]=o;q.psize[a]=o}else{B=Math.max(0,m-q.margin[a]);if(!e.isIE&&e.hasTag(i,\"TEXTAREA\"))B=m;z=false;if(e.isIE&&e.hasTag(i,\"BUTTON\"))z=true;if(!(i.style.display===\"none\"&&!i.ed)&&(z||m!=o||q.layout)){v(i, b.size,B+\"px\")&&Q(q,1);q.set[a]=true}else if(q.fs[a])a==0&&v(i,b.size,q.fs[a]+\"px\");else{v(i,b.size,\"\")&&Q(q,1);q.set[a]=false}z=h;q.size[a]=B;q.psize[a]=m}if(L)if(f){v(i,b.left,\"4px\");m=e.css(i,\"position\");if(m!==\"absolute\")i.style.position=\"relative\"}else v(i,b.left,\"0px\");else v(i,b.left,z+\"px\");if(a==1){if(i.wtResize)i.wtResize(i,q.set[0]?Math.round(q.size[0]):-1,q.set[1]?Math.round(q.size[1]):-1,true);q.dirty=0}}if(j[d]>-1)h+=j[d]}$(c).children(\".\"+g.handleClass).css(b.size,k-b.margins[2]-b.margins[1]+ \"px\")}}var e=C.WT;this.ancestor=null;this.descendants=[];var R=this,F=x,W=false,M=true,aa=false,Y=null,S=null,ba=false,T=[],ca=false,X=$(document.body).hasClass(\"Wt-rtl\"),s=[{initialized:false,config:F.cols,margins:H,maxSize:U,measures:[],sizes:[],stretched:[],fixedSize:[],Left:X?\"Right\":\"Left\",left:X?\"right\":\"left\",Right:X?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,c){return F.items[c*s[0].config.length+a]},setItem:function(a,c,b){F.items[c*s[0].config.length+a]=b},handleClass:\"Wt-vrh2\", resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:O},{initialized:false,config:F.rows,margins:A,maxSize:u,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,c){return F.items[a*s[0].config.length+c]},setItem:function(a,c,b){F.items[a*s[0].config.length+c]=b},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:P}];jQuery.data(document.getElementById(G),\"layout\",this);this.updateSizeInParent=function(a){if(Y){var c= s[a],b=c.measures[2];if(c.maxSize>0)b=Math.min(c.maxSize,b);if(ca){c=e.getElement(G);if(!c)return;for(var g=c,h=g.parentNode;;){if(h.wtGetPS)b=h.wtGetPS(h,g,a,b);b+=V(h,a);if(h==S)break;if(a==1&&h==c.parentNode&&!h.lh&&h.offsetHeight>b)b=h.offsetHeight;g=h;h=g.parentNode}}else b+=T[a];Y.setChildSize(S,a,b)}};this.setConfig=function(a){var c=F;F=a;s[0].config=F.cols;s[1].config=F.rows;s[0].stretched=[];s[1].stretched=[];var b;a=0;for(b=c.items.length;a<b;++a){var g=c.items[a];if(g){if(g.set){g.set[0]&& v(g.w,s[0].size,\"\");g.set[1]&&v(g.w,s[1].size,\"\")}if(g.layout){R.setChildSize(g.w,0,g.ps[0]);R.setChildSize(g.w,1,g.ps[1])}}}M=true;C.layouts2.scheduleAdjust()};this.getId=function(){return G};this.setItemsDirty=function(a){var c,b,g=s[0].config.length;c=0;for(b=a.length;c<b;++c){var h=F.items[a[c][0]*g+a[c][1]];h.dirty=2;if(h.layout){h.layout=false;h.wasLayout=true;C.layouts2.setChildLayoutsDirty(R,h.w)}}W=true};this.setDirty=function(){M=true};this.setChildSize=function(a,c,b){var g=s[0].config.length, h=s[c],k,j;k=0;for(j=F.items.length;k<j;++k){var r=F.items[k];if(r&&r.id==a.id){a=c===0?k%g:k/g;if(r.align>>h.alignBits&15||!h.stretched[a]){if(!r.ps)r.ps=[];r.ps[c]=b}r.layout=true;Q(r,1);break}}};this.measure=function(a){var c=e.getElement(G);if(c)if(!e.isHidden(c)){if(!ba){ba=true;if(aa=J==null){var b=c;b=b.parentNode;for(T=[0,0];;){T[0]+=V(b,0);T[1]+=V(b,1);if(b.wtGetPS)ca=true;var g=jQuery.data(b.parentNode,\"layout\");if(g){Y=g;S=b;break}b=b;b=b.parentNode;if(b.childNodes.length!=1&&!b.wtGetPS)break}b= c.parentNode;for(g=0;g<2;++g)s[g].sizeSet=e.pxself(b,s[g].size)!=0}else{Y=jQuery.data(document.getElementById(J),\"layout\");S=c;T[0]=V(S,0);T[1]=V(S,1)}}if(W||M){b=aa?c.parentNode:null;da(a,c,b)}if(a==1)W=M=false}};this.setMaxSize=function(a,c){s[0].maxSize=a;s[1].maxSize=c};this.apply=function(a){var c=e.getElement(G);if(!c)return false;if(e.isHidden(c))return true;ha(a,c);return true};this.contains=function(a){var c=e.getElement(G);a=e.getElement(a.getId());return c&&a?e.contains(c,a):false};this.WT= e}");
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
				"new (function(){var C=[],G=false,J=this,O=false;this.find=function(u){return jQuery.data(document.getElementById(u),\"layout\")};this.setDirty=function(u){(u=jQuery.data(u,\"layout\"))&&u.setDirty()};this.setChildLayoutsDirty=function(u,H){var A,x;A=0;for(x=u.descendants.length;A<x;++A){var y=u.descendants[A];if(H){var D=u.WT.getElement(y.getId());if(D&&!u.WT.contains(H,D))continue}y.setDirty()}};this.add=function(u){function H(A,x){var y,D;y=0;for(D= A.length;y<D;++y){var w=A[y];if(w.getId()==x.getId()){A[y]=x;x.descendants=w.descendants;return}else if(w.contains(x)){H(w.descendants,x);return}else if(x.contains(w)){x.descendants.push(w);A.splice(y,1);--y;--D}}A.push(x)}H(C,u);J.scheduleAdjust()};var P=false,L=false;this.scheduleAdjust=function(u){if(u)O=true;if(!P){P=true;setTimeout(function(){J.adjust()},0)}};this.adjust=function(u,H){function A(y,D){var w,K;w=0;for(K=y.length;w<K;++w){var I=y[w];A(I.descendants,D);D==1&&O&&I.setDirty();I.measure(D)}} function x(y,D){var w,K;w=0;for(K=y.length;w<K;++w){var I=y[w];if(I.apply(D))x(I.descendants,D);else{y.splice(w,1);--w;--K}}}if(u){(u=this.find(u))&&u.setItemsDirty(H);J.scheduleAdjust()}else{P=false;if(!G){G=true;L=false;A(C,0);x(C,0);A(C,1);x(C,1);if(L){A(C,0);x(C,0);A(C,1);x(C,1)}O=L=G=false}}};this.updateConfig=function(u,H){(u=this.find(u))&&u.setConfig(H)};this.remeasure=function(){L=true};this.adjustNow=function(){P&&J.adjust()};var U=null;window.onresize=function(){clearTimeout(U);U=setTimeout(function(){U= null;J.scheduleAdjust(true)},20)};window.onshow=function(){O=true;J.adjust()}})");
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
