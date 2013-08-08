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
				"function(B,H,I,N,O,L,U,s,F,v,y){function z(a,c,b,e){function h(m){return m==\"visible\"||m==\"none\"}var j=u[c],k=c?a.scrollHeight:a.scrollWidth,t,d;if(c==0&&k+f.pxself(a,j.left)>=e.clientWidth){t=a.style[j.left];w(a,j.left,\"-1000000px\");k=c?a.scrollHeight:a.scrollWidth}e=c?a.clientHeight:a.clientWidth;if(f.isGecko&&c==0&&h(f.css(a,\"overflow\"))){d=a.style[j.size];w(a,j.size,\"\")}c=c?a.offsetHeight:a.offsetWidth;t&&w(a,j.left,t);d&&w(a,j.size,d); if(e>=1E6)e-=1E6;if(k>=1E6)k-=1E6;if(c>=1E6)c-=1E6;if(k===0){k=f.pxself(a,j.size);if(k!==0&&!f.isOpera&&!f.isGecko)k-=f.px(a,\"border\"+j.Left+\"Width\")+f.px(a,\"border\"+j.Right+\"Width\")}if(f.isIE&&(f.hasTag(a,\"BUTTON\")||f.hasTag(a,\"TEXTAREA\")||f.hasTag(a,\"INPUT\")||f.hasTag(a,\"SELECT\")))k=e;if(k>c)if(f.pxself(a,j.size)==0)k=0;else{var n=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!==\"none\")n=true});if(n)k=0}if(b)return k;f.isOpera||(k+=f.px(a,\"border\"+j.Left+\"Width\")+f.px(a,\"border\"+ j.Right+\"Width\"));k+=f.px(a,\"margin\"+j.Left)+f.px(a,\"margin\"+j.Right);if(!f.boxSizing(a)&&!f.isIE)k+=f.px(a,\"padding\"+j.Left)+f.px(a,\"padding\"+j.Right);if(k<c)k=c;a=f.px(a,\"max\"+j.Size);if(a>0)k=Math.min(a,k);return Math.round(k)}function C(a,c){c=u[c];if(a.style.display===\"none\")return 0;else if(a[\"layoutMin\"+c.Size])return a[\"layoutMin\"+c.Size];else{var b=f.px(a,\"min\"+c.Size);f.boxSizing(a)||(b+=f.px(a,\"padding\"+c.Left)+f.px(a,\"padding\"+c.Right));return b}}function x(a,c){c=u[c];var b=f.px(a,\"margin\"+ c.Left)+f.px(a,\"margin\"+c.Right);if(!f.boxSizing(a)&&!(f.isIE&&!f.isIElt9&&f.hasTag(a,\"BUTTON\")))b+=f.px(a,\"border\"+c.Left+\"Width\")+f.px(a,\"border\"+c.Right+\"Width\")+f.px(a,\"padding\"+c.Left)+f.px(a,\"padding\"+c.Right);return b}function K(a,c){c=u[c];return f.px(a,\"padding\"+c.Left)+f.px(a,\"padding\"+c.Right)}function J(a,c){if(f.boxSizing(a)){c=u[c];return f.px(a,\"border\"+c.Left+\"Width\")+f.px(a,\"border\"+c.Right+\"Width\")+f.px(a,\"padding\"+c.Left)+f.px(a,\"padding\"+c.Right)}else return 0}function V(a,c){c= u[c];return Math.round(f.px(a,\"border\"+c.Left+\"Width\")+f.px(a,\"border\"+c.Right+\"Width\")+f.px(a,\"margin\"+c.Left)+f.px(a,\"margin\"+c.Right)+f.px(a,\"padding\"+c.Left)+f.px(a,\"padding\"+c.Right))}function P(a,c,b){a.dirty=Math.max(a.dirty,c);Q=true;b&&B.layouts2.scheduleAdjust()}function w(a,c,b){if(a.style[c]!==b){a.style[c]=b;return true}else return false}function da(a,c,b){var e=u[a],h=e.config.length,j=u[a^1].config.length,k=e.measures.slice();if(k.length==5){k[0]=k[0].slice();k[1]=k[1].slice()}if(Q){if(b&& typeof e.minSize==\"undefined\"){e.minSize=f.px(b,\"min\"+e.Size);if(e.minSize>0)e.minSize-=J(b,a)}var t=[],d=[],n=0,m=0,i,D,q=false;for(i=0;i<h;++i){var l=0,r=e.config[i][2],p=true;for(D=0;D<j;++D){var g=e.getItem(i,D);if(g){if(!g.w||a==0&&g.dirty>1){var o=$(\"#\"+g.id),A=o.get(0);if(!A){e.setItem(i,D,null);continue}if(A!=g.w){g.w=A;o.find(\"img\").add(o.filter(\"img\")).bind(\"load\",{item:g},function(ea){P(ea.data.item,1,true)})}}if(!L&&g.w.style.position!=\"absolute\"){g.w.style.position=\"absolute\";g.w.style.visibility= \"hidden\"}if(!g.ps)g.ps=[];if(!g.ms)g.ms=[];if(!g.size)g.size=[];if(!g.psize)g.psize=[];if(!g.fs)g.fs=[];if(!g.margin)g.margin=[];if($(g.w).hasClass(\"Wt-hidden\"))g.ps[a]=g.ms[a]=0;else{o=!g.set;if(!g.set)g.set=[false,false];if(g.w){if(f.isIE)g.w.style.visibility=\"\";if(g.dirty){var G;if(g.dirty>1){C(g.w,a);g.ms[a]=G}else G=g.ms[a];if(G>r)r=G;if(g.dirty>1)g.margin[a]=x(g.w,a);if(!g.set[a])if(a==0||!o){o=f.pxself(g.w,e.size);g.fs[a]=o?o+g.margin[a]:0}else{o=Math.round(f.px(g.w,e.size));g.fs[a]=o>Math.max(J(g.w, a),G)?o+g.margin[a]:0}o=g.fs[a];if(g.layout){if(o==0)o=g.ps[a]}else{if(g.wasLayout){g.wasLayout=false;g.set=[false,false];g.ps=[];g.w.wtResize&&g.w.wtResize(g.w,-1,-1,true);w(g.w,u[1].size,\"\")}A=z(g.w,a,false,c);var M=g.set[a];if(M)if(g.psize[a]>8)M=A>=g.psize[a]-4&&A<=g.psize[a]+4;var Z=typeof g.ps[a]!==\"undefined\"&&e.config[i][0]>0&&g.set[a];o=M||Z?Math.max(o,g.ps[a]):Math.max(o,A)}g.ps[a]=o;if(!g.span||g.span[a]==1){if(o>l)l=o}else q=true}else if(!g.span||g.span[a]==1){if(g.ps[a]>l)l=g.ps[a];if(g.ms[a]> r)r=g.ms[a]}else q=true;if(!(g.w.style.display===\"none\"&&!g.w.ed)&&(!g.span||g.span[a]==1))p=false}}}}if(p)r=l=-1;else if(r>l)l=r;t[i]=l;d[i]=r;if(r>-1){n+=l;m+=r}}if(q)for(i=0;i<h;++i)for(D=0;D<j;++D)if((g=e.getItem(i,D))&&g.span&&g.span[a]>1){c=g.ps[a];for(si=l=q=0;si<g.span[a];++si){r=t[i+si];if(r!=-1){c-=r;++q;if(e.config[i+si][0]>0)l+=e.config[i+si][0]}}if(c>0)if(q>0){if(l>0)q=l;for(si=0;si<g.span[a];++si){r=t[i+si];if(r!=-1){r=l>0?e.config[i+si][0]:1;if(r>0){p=Math.round(c/r);c-=p;q-=r;t[i+ si]+=p}}}}else t[i]=c}j=0;o=true;D=false;for(i=0;i<h;++i)if(d[i]>-1){if(o){j+=e.margins[1];o=false}else{j+=e.margins[0];if(D)j+=4}D=e.config[i][1]!==0}o||(j+=e.margins[2]);n+=j;m+=j;e.measures=[t,d,n,m,j]}if(W||k[2]!=e.measures[2])R.updateSizeInParent(a);b&&e.minSize==0&&k[3]!=e.measures[3]&&b.parentNode.className!=\"Wt-domRoot\"&&w(b,\"min\"+e.Size,e.measures[3]+\"px\")&&R.ancestor&&R.ancestor.setContentsDirty(b);b&&a==0&&b&&f.hasTag(b,\"TD\")&&w(b,e.size,e.measures[2]+\"px\")}function fa(a,c,b){a=u[a];if(X)b= -b;if(a.config[c][0]>0&&a.config[c+1][0]==0){++c;b=-b}a.fixedSize[c]=a.sizes[c]+b;B.layouts2.scheduleAdjust()}function ga(a,c,b){var e=c.di,h=u[a],j=u[a^1],k,t=f.getElement(H),d;for(d=e-1;d>=0;--d)if(h.sizes[d]>=0){k=-(h.sizes[d]-h.measures[1][d]);break}e=h.sizes[e]-h.measures[1][e];if(X){var n=k;k=-e;e=-n}new f.SizeHandle(f,h.resizeDir,f.pxself(c,h.size),f.pxself(c,j.size),k,e,h.resizerClass,function(m){fa(a,d,m)},c,t,b,0,0)}function ha(a,c){var b=u[a],e=u[a^1],h=b.measures,j=0,k=false,t=false,d= false,n=aa?c.parentNode:null;if(b.maxSize===0)if(n){var m=f.css(n,\"position\");if(m===\"absolute\")j=f.pxself(n,b.size);if(j===0){if(!b.initialized){if(a===0&&(m===\"absolute\"||m===\"fixed\")){n.style.display=\"none\";j=n.clientWidth;n.style.display=\"\"}j=a?n.clientHeight:n.clientWidth;k=true;if(a==0&&j==0&&f.isIElt9){j=n.offsetWidth;k=false}var i;if((f.hasTag(n,\"TD\")||f.hasTag(n,\"TH\"))&&!(f.isIE&&!f.isIElt9)){d=0;i=1}else{d=b.minSize?b.minSize:h[3];i=0}function D(M,Z){return Math.abs(M-Z)<1}if(f.isIElt9&& D(j,i)||D(j,d+K(n,a)))b.maxSize=999999}if(j===0&&b.maxSize===0){j=a?n.clientHeight:n.clientWidth;k=true}}}else{j=f.pxself(c,b.size);t=true}else if(b.sizeSet){j=f.pxself(n,b.size);t=true}var q=0;if(n&&n.wtGetPS&&a==1)q=n.wtGetPS(n,c,a,0);d=h[2];if(d<b.minSize)d=b.minSize;if(b.maxSize&&!b.sizeSet){j=Math.min(d,b.maxSize)+q;w(n,b.size,j+J(n,a)+\"px\")&&B.layouts2.remeasure();j=j;d=t=true}b.cSize=j;if(a==1&&n&&n.wtResize){i=e.cSize;d=b.cSize;n.wtResize(n,Math.round(i),Math.round(d),true)}j-=q;if(!t){t= 0;if(typeof b.cPadding===\"undefined\"){t=k?K(n,a):J(n,a);b.cPadding=t}else t=b.cPadding;j-=t}b.initialized=true;if(!(n&&j<=0)){if(j<h[3]-q)j=h[3]-q;k=[];n=b.config.length;t=e.config.length;for(d=0;d<n;++d)b.stretched[d]=false;if(j>=h[3]-q){q=j-h[4];i=[];var l=[0,0],r=[0,0],p=0;for(d=0;d<n;++d)if(h[1][d]>-1){m=-1;if(typeof b.fixedSize[d]!==\"undefined\"&&(d+1==n||h[1][d+1]>-1))m=b.fixedSize[d];else if(b.config[d][1]!==0&&b.config[d][1][0]>=0){m=b.config[d][1][0];if(b.config[d][1][1])m=(j-h[4])*m/100}if(m>= 0){i[d]=-1;k[d]=m;q-=k[d]}else{if(b.config[d][0]>0){m=1;i[d]=b.config[d][0];p+=i[d]}else{m=0;i[d]=0}l[m]+=h[1][d];r[m]+=h[0][d];k[d]=h[0][d]}}else{i[d]=-2;k[d]=-1}if(p==0){for(d=0;d<n;++d)if(i[d]==0){i[d]=1;++p}r[1]=r[0];l[1]=l[0];r[0]=0;l[0]=0}if(q>r[0]+l[1]){q-=r[0];if(q>r[1]){if(b.fitSize){q-=r[1];q=q/p;for(d=0;d<n;++d)if(i[d]>0){k[d]+=Math.round(i[d]*q);b.stretched[d]=true}}}else{m=1;if(q<l[m])q=l[m];q=r[m]-l[m]>0?(q-l[m])/(r[m]-l[m]):0;for(d=0;d<n;++d)if(i[d]>0){l=h[0][d]-h[1][d];k[d]=h[1][d]+ Math.round(l*q)}}}else{for(d=0;d<n;++d)if(i[d]>0)k[d]=h[1][d];q-=l[1];m=0;if(q<l[m])q=l[m];q=r[m]-l[m]>0?(q-l[m])/(r[m]-l[m]):0;for(d=0;d<n;++d)if(i[d]==0){l=h[0][d]-h[1][d];k[d]=h[1][d]+Math.round(l*q)}}}else k=h[1];b.sizes=k;h=b.margins[1];q=true;l=false;for(d=0;d<n;++d){if(k[d]>-1){var g=l;if(l){i=H+\"-rs\"+a+\"-\"+d;l=f.getElement(i);if(!l){l=document.createElement(\"div\");l.setAttribute(\"id\",i);l.di=d;l.style.position=\"absolute\";l.style[e.left]=e.margins[1]+\"px\";l.style[b.size]=b.margins[0]+\"px\"; if(e.cSize)l.style[e.size]=e.cSize-e.margins[2]-e.margins[1]+\"px\";l.className=b.handleClass;c.insertBefore(l,c.firstChild);l.onmousedown=l.ontouchstart=function(M){ga(a,this,M||window.event)}}h+=2;w(l,b.left,h+\"px\");h+=2}l=b.config[d][1]!==0;if(q)q=false;else h+=b.margins[0]}for(r=0;r<t;++r)if((p=b.getItem(d,r))&&p.w){i=p.w;m=Math.max(k[d],0);if(p.span){var o,A=l;for(o=1;o<p.span[a];++o){if(d+o>=k.length)break;if(A)m+=4;A=b.config[d+o][1]!==0;if(k[d+o-1]>-1&&k[d+o]>-1)m+=b.margins[0];m+=k[d+o]}}var G; w(i,\"visibility\",\"\");A=p.align>>b.alignBits&15;o=p.ps[a];if(m<o)A=0;if(A){switch(A){case 1:G=h;break;case 4:G=h+(m-o)/2;break;case 2:G=h+(m-o);break}o-=p.margin[a];if(p.layout){w(i,b.size,o+\"px\")&&P(p,1);p.set[a]=true}else if(m>=o&&p.set[a]){w(i,b.size,o+\"px\")&&P(p,1);p.set[a]=false}p.size[a]=o;p.psize[a]=o}else{A=Math.max(0,m-p.margin[a]);if(!f.isIE&&f.hasTag(i,\"TEXTAREA\"))A=m;G=false;if(f.isIE&&f.hasTag(i,\"BUTTON\"))G=true;if(!(i.style.display===\"none\"&&!i.ed)&&(G||m!=o||p.layout)){w(i,b.size,A+ \"px\")&&P(p,1);p.set[a]=true}else if(p.fs[a])a==0&&w(i,b.size,p.fs[a]+\"px\");else{w(i,b.size,\"\")&&P(p,1);p.set[a]=false}G=h;p.size[a]=A;p.psize[a]=m}if(L)if(g){w(i,b.left,\"4px\");m=f.css(i,\"position\");if(m!==\"absolute\")i.style.position=\"relative\"}else w(i,b.left,\"0px\");else w(i,b.left,G+\"px\");if(a==1){if(i.wtResize)i.wtResize(i,p.set[0]?Math.round(p.size[0]):-1,p.set[1]?Math.round(p.size[1]):-1,true);p.dirty=0}}if(k[d]>-1)h+=k[d]}$(c).children(\".\"+e.handleClass).css(b.size,j-b.margins[2]-b.margins[1]+ \"px\")}}var f=B.WT;this.ancestor=null;this.descendants=[];var R=this,E=y,Q=true,W=true,aa=false,Y=null,S=null,ba=false,T=[],ca=false,X=$(document.body).hasClass(\"Wt-rtl\"),u=[{initialized:false,config:E.cols,margins:F,maxSize:U,measures:[],sizes:[],stretched:[],fixedSize:[],Left:X?\"Right\":\"Left\",left:X?\"right\":\"left\",Right:X?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,c){return E.items[c*u[0].config.length+a]},setItem:function(a,c,b){E.items[c*u[0].config.length+a]=b},handleClass:\"Wt-vrh2\", resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:N},{initialized:false,config:E.rows,margins:v,maxSize:s,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\",Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,c){return E.items[a*u[0].config.length+c]},setItem:function(a,c,b){E.items[a*u[0].config.length+c]=b},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:O}];jQuery.data(document.getElementById(H),\"layout\",this);this.updateSizeInParent=function(a){if(Y){var c= u[a],b=c.measures[2];if(c.maxSize>0)b=Math.min(c.maxSize,b);if(ca){c=f.getElement(H);if(!c)return;for(var e=c,h=e.parentNode;;){if(h.wtGetPS)b=h.wtGetPS(h,e,a,b);b+=V(h,a);if(h==S)break;if(a==1&&h==c.parentNode&&!h.lh&&h.offsetHeight>b)b=h.offsetHeight;e=h;h=e.parentNode}}else b+=T[a];Y.setChildSize(S,a,b)}};this.setConfig=function(a){var c=E;E=a;u[0].config=E.cols;u[1].config=E.rows;u[0].stretched=[];u[1].stretched=[];var b;a=0;for(b=c.items.length;a<b;++a){var e=c.items[a];if(e){if(e.set){e.set[0]&& w(e.w,u[0].size,\"\");e.set[1]&&w(e.w,u[1].size,\"\")}if(e.layout){R.setChildSize(e.w,0,e.ps[0]);R.setChildSize(e.w,1,e.ps[1])}}}W=true;B.layouts2.scheduleAdjust()};this.getId=function(){return H};this.setElDirty=function(a){var c,b;c=0;for(b=E.items.length;c<b;++c){var e=E.items[c];if(e&&e.id==a.id){e.dirty=2;Q=true;return}}};this.setItemsDirty=function(a){var c,b,e=u[0].config.length;c=0;for(b=a.length;c<b;++c){var h=E.items[a[c][0]*e+a[c][1]];h.dirty=2;if(h.layout){h.layout=false;h.wasLayout=true; B.layouts2.setChildLayoutsDirty(R,h.w)}}Q=true};this.setDirty=function(){W=true};this.setChildSize=function(a,c,b){var e=u[0].config.length,h=u[c],j,k;j=0;for(k=E.items.length;j<k;++j){var t=E.items[j];if(t&&t.id==a.id){a=c===0?j%e:j/e;if(t.align>>h.alignBits&15||!h.stretched[a]){if(!t.ps)t.ps=[];t.ps[c]=b}t.layout=true;P(t,1);break}}};this.measure=function(a){var c=f.getElement(H);if(c)if(!f.isHidden(c)){if(!ba){ba=true;if(aa=I==null){var b=c;b=b.parentNode;for(T=[0,0];;){T[0]+=V(b,0);T[1]+=V(b, 1);if(b.wtGetPS)ca=true;var e=jQuery.data(b.parentNode,\"layout\");if(e){Y=e;S=b;break}b=b;b=b.parentNode;if(b.childNodes.length!=1&&!b.wtGetPS)break}b=c.parentNode;for(e=0;e<2;++e)u[e].sizeSet=f.pxself(b,u[e].size)!=0}else{Y=jQuery.data(document.getElementById(I),\"layout\");S=c;T[0]=V(S,0);T[1]=V(S,1)}}if(Q||W){b=aa?c.parentNode:null;da(a,c,b)}if(a==1)Q=W=false}};this.setMaxSize=function(a,c){u[0].maxSize=a;u[1].maxSize=c};this.apply=function(a){var c=f.getElement(H);if(!c)return false;if(f.isHidden(c))return true; ha(a,c);return true};this.contains=function(a){var c=f.getElement(H);a=f.getElement(a.getId());return c&&a?f.contains(c,a):false};this.WT=f}");
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
				"new (function(){var B=[],H=false,I=this,N=false;this.find=function(s){return jQuery.data(document.getElementById(s),\"layout\")};this.setDirty=function(s){if(s=this.find(s)){s.setDirty();I.scheduleAdjust()}};this.setElementDirty=function(s){var F=s;for(s=s.parentNode;s&&s!=document.body;){var v=jQuery.data(s,\"layout\");if(v){v.setElDirty(F);I.scheduleAdjust()}F=s;s=s.parentNode}};this.setChildLayoutsDirty=function(s,F){var v,y;v=0;for(y=s.descendants.length;v< y;++v){var z=s.descendants[v];if(F){var C=s.WT.getElement(z.getId());if(C&&!s.WT.contains(F,C))continue}z.setDirty()}};this.add=function(s){function F(v,y){var z,C;z=0;for(C=v.length;z<C;++z){var x=v[z];if(x.getId()==y.getId()){v[z]=y;y.descendants=x.descendants;return}else if(x.contains(y)){F(x.descendants,y);return}else if(y.contains(x)){y.descendants.push(x);v.splice(z,1);--z;--C}}v.push(y)}F(B,s);I.scheduleAdjust()};var O=false,L=false;this.scheduleAdjust=function(s){if(s)N=true;if(!O){O=true; setTimeout(function(){I.adjust()},0)}};this.adjust=function(s,F){function v(z,C){var x,K;x=0;for(K=z.length;x<K;++x){var J=z[x];v(J.descendants,C);C==1&&N&&J.setDirty();J.measure(C)}}function y(z,C){var x,K;x=0;for(K=z.length;x<K;++x){var J=z[x];if(J.apply(C))y(J.descendants,C);else{z.splice(x,1);--x;--K}}}if(s){(s=this.find(s))&&s.setItemsDirty(F);I.scheduleAdjust()}else{O=false;if(!H){H=true;L=false;v(B,0);y(B,0);v(B,1);y(B,1);if(L){v(B,0);y(B,0);v(B,1);y(B,1)}N=L=H=false}}};this.updateConfig=function(s, F){(s=this.find(s))&&s.setConfig(F)};this.remeasure=function(){L=true};this.adjustNow=function(){O&&I.adjust()};var U=null;window.onresize=function(){clearTimeout(U);U=setTimeout(function(){U=null;I.scheduleAdjust(true)},20)};window.onshow=function(){N=true;I.adjust()}})");
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
