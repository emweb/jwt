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
				"function(B,G,K,N,S,L,u,H,A,y,z){function C(a,b,c){function i(k){return k==\"visible\"||k==\"none\"}var e=t[b],p=a.style[e.left],n;w(a,e.left,\"-1000000px\");var l=b?a.scrollHeight:a.scrollWidth,d=b?a.clientHeight:a.clientWidth;if(f.isGecko&&b==0&&i(f.css(a,\"overflow\"))){n=a.style[e.size];w(a,e.size,\"\")}b=b?a.offsetHeight:a.offsetWidth;w(a,e.left,p);n&&w(a,e.size,n);if(d>=1E6)d-=1E6;if(l>=1E6)l-=1E6;if(b>=1E6)b-=1E6;if(l===0){l=f.pxself(a,e.size); if(l!==0&&!f.isOpera&&!f.isGecko)l-=f.px(a,\"border\"+e.Left+\"Width\")+f.px(a,\"border\"+e.Right+\"Width\")}if(f.isIE&&(f.hasTag(a,\"BUTTON\")||f.hasTag(a,\"TEXTAREA\")||f.hasTag(a,\"INPUT\")||f.hasTag(a,\"SELECT\")))l=d;if(l>b)if(f.pxself(a,e.size)==0)l=0;else{var m=false;$(a).find(\".Wt-popup\").each(function(){if(this.style.display!==\"none\")m=true});if(m)l=0}if(c)return l;f.isOpera||(l+=f.px(a,\"border\"+e.Left+\"Width\")+f.px(a,\"border\"+e.Right+\"Width\"));l+=f.px(a,\"margin\"+e.Left)+f.px(a,\"margin\"+e.Right);if(!f.boxSizing(a)&& !f.isIE)l+=f.px(a,\"padding\"+e.Left)+f.px(a,\"padding\"+e.Right);if(l<b)l=b;a=f.px(a,\"max\"+e.Size);if(a>0)l=Math.min(a,l);return Math.round(l)}function x(a,b){b=t[b];if(a.style.display===\"none\")return 0;else if(a[\"layoutMin\"+b.Size])return a[\"layoutMin\"+b.Size];else{var c=f.px(a,\"min\"+b.Size);f.boxSizing(a)||(c+=f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right));return c}}function I(a,b){b=t[b];var c=f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right);if(!f.boxSizing(a)&&!(f.isIE&&!f.isIElt9&&f.hasTag(a, \"BUTTON\")))c+=f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right);return c}function J(a,b){b=t[b];return f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}function X(a,b){if(f.boxSizing(a)){b=t[b];return f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right)}else return 0}function T(a,b){b=t[b];return Math.round(f.px(a,\"border\"+b.Left+\"Width\")+f.px(a,\"border\"+b.Right+\"Width\")+ f.px(a,\"margin\"+b.Left)+f.px(a,\"margin\"+b.Right)+f.px(a,\"padding\"+b.Left)+f.px(a,\"padding\"+b.Right))}function O(a,b){U=a.dirty=true;b&&B.layouts2.scheduleAdjust()}function w(a,b,c){if(a.style[b]!==c){a.style[b]=c;return true}else return false}function da(a,b,c){b=t[a];var i=t[a^1],e=b.measures,p=b.config.length,n=i.config.length;if(U||M){if(c&&typeof b.minSize==\"undefined\"){b.minSize=f.px(c,\"min\"+b.Size);if(b.minSize>0)b.minSize-=X(c,a)}e=e.slice();if(e.length==5){e[0]=e[0].slice();e[1]=e[1].slice()}var l= [],d=[],m=0,k=0,h,D,r=false;for(h=0;h<p;++h){var j=0,s=b.config[h][2],q=true;for(D=0;D<n;++D){var g=b.getItem(h,D);if(g){if(!g.w||a==0&&g.dirty){var o=$(\"#\"+g.id),v=o.get(0);if(!v){b.setItem(h,D,null);continue}if(v!=g.w){g.w=v;o.find(\"img\").add(o.filter(\"img\")).bind(\"load\",{item:g},function(Z){O(Z.data.item,true)});g.w.style[b.left]=g.w.style[i.left]=\"-1000000px\"}}if(!L&&g.w.style.position!=\"absolute\"){g.w.style.position=\"absolute\";g.w.style.visibility=\"hidden\"}if(!g.ps)g.ps=[];if(!g.ms)g.ms=[];if(!g.size)g.size= [];if(!g.psize)g.psize=[];if(!g.fs)g.fs=[];if($(g.w).hasClass(\"Wt-hidden\"))g.ps[a]=g.ms[a]=0;else{o=!g.set;if(!g.set)g.set=[false,false];if(g.w){if(f.isIE)g.w.style.visibility=\"\";if(g.dirty||M){v=x(g.w,a);if(v>s)s=v;g.ms[a]=v;if(!g.set[a])if(a==0||!o){o=f.pxself(g.w,b.size);g.fs[a]=o?o+I(g.w,a):0}else{o=Math.round(f.px(g.w,b.size));g.fs[a]=o>Math.max(X(g.w,a),v)?o+I(g.w,a):0}o=g.fs[a];if(g.layout){if(o==0)o=g.ps[a]}else{if(g.wasLayout){g.wasLayout=false;g.set=[false,false];g.ps=[];g.w.wtResize&&g.w.wtResize(g.w, -1,-1,true);w(g.w,t[1].size,\"\")}v=C(g.w,a,false);var E=g.set[a];if(E)if(g.psize[a]>8)E=v>=g.psize[a]-4&&v<=g.psize[a]+4;var V=typeof g.ps[a]!==\"undefined\"&&b.config[h][0]>0&&g.set[a];o=E||V?Math.max(o,g.ps[a]):Math.max(o,v)}g.ps[a]=o;if(!g.span||g.span[a]==1){if(o>j)j=o}else r=true}else if(!g.span||g.span[a]==1){if(g.ps[a]>j)j=g.ps[a];if(g.ms[a]>s)s=g.ms[a]}else r=true;if(!(g.w.style.display===\"none\"&&!g.w.ed)&&(!g.span||g.span[a]==1))q=false}}}}if(q)s=j=-1;else if(s>j)j=s;l[h]=j;d[h]=s;if(s>-1){m+= j;k+=s}}if(r)for(h=0;h<p;++h)for(D=0;D<n;++D)if((g=b.getItem(h,D))&&g.span&&g.span[a]>1){i=g.ps[a];for(si=j=r=0;si<g.span[a];++si){s=l[h+si];if(s!=-1){i-=s;++r;if(b.config[h+si][0]>0)j+=b.config[h+si][0]}}if(i>0)if(r>0){if(j>0)r=j;for(si=0;si<g.span[a];++si){s=l[h+si];if(s!=-1){s=j>0?b.config[h+si][0]:1;if(s>0){q=Math.round(i/s);i-=q;r-=s;l[h+si]+=q}}}}else l[h]=i}n=0;o=true;D=false;for(h=0;h<p;++h)if(d[h]>-1){if(o){n+=b.margins[1];o=false}else{n+=b.margins[0];if(D)n+=4}D=b.config[h][1]!==0}o||(n+= b.margins[2]);m+=n;k+=n;b.measures=[l,d,m,k,n];if(M||e[2]!=b.measures[2])P.updateSizeInParent(a);c&&b.minSize==0&&e[3]!=b.measures[3]&&c.parentNode.className!=\"Wt-domRoot\"&&w(c,\"min\"+b.Size,b.measures[3]+\"px\")&&P.ancestor&&P.ancestor.setContentsDirty(c);c&&a==0&&c&&f.hasTag(c,\"TD\")&&w(c,b.size,b.measures[2]+\"px\")}}function ea(a,b,c){a=t[a];if(W)c=-c;if(a.config[b][0]>0&&a.config[b+1][0]==0){++b;c=-c}a.fixedSize[b]=a.sizes[b]+c;B.layouts2.scheduleAdjust()}function fa(a,b,c){var i=b.di,e=t[a],p=t[a^ 1],n,l=f.getElement(G),d;for(d=i-1;d>=0;--d)if(e.sizes[d]>=0){n=-(e.sizes[d]-e.measures[1][d]);break}i=e.sizes[i]-e.measures[1][i];if(W){var m=n;n=-i;i=-m}new f.SizeHandle(f,e.resizeDir,f.pxself(b,e.size),f.pxself(b,p.size),n,i,e.resizerClass,function(k){ea(a,d,k)},b,l,c,0,0)}function ga(a,b){var c=t[a],i=t[a^1],e=c.measures,p=0,n=false,l=false,d=false,m=aa?b.parentNode:null;if(c.maxSize===0)if(m){var k=f.css(m,\"position\");if(k===\"absolute\")p=f.pxself(m,c.size);if(p===0){if(!c.initialized){if(a=== 0&&(k===\"absolute\"||k===\"fixed\")){m.style.display=\"none\";p=m.clientWidth;m.style.display=\"\"}p=a?m.clientHeight:m.clientWidth;n=true;if(a==0&&p==0&&f.isIElt9){p=m.offsetWidth;n=false}var h;if((f.hasTag(m,\"TD\")||f.hasTag(m,\"TH\"))&&!(f.isIE&&!f.isIElt9)){d=0;h=1}else{d=c.minSize?c.minSize:e[3];h=0}function D(V,Z){return Math.abs(V-Z)<1}if(f.isIElt9&&D(p,h)||D(p,d+J(m,a)))c.maxSize=999999}if(p===0&&c.maxSize===0){p=a?m.clientHeight:m.clientWidth;n=true}}}else{p=f.pxself(b,c.size);l=true}else if(c.sizeSet){p= f.pxself(m,c.size);l=true}var r=0;if(m&&m.wtGetPS&&a==1)r=m.wtGetPS(m,b,a,0);d=e[2];if(d<c.minSize)d=c.minSize;if(c.maxSize&&!c.sizeSet){p=Math.min(d,c.maxSize)+r;w(m,c.size,p+X(m,a)+\"px\")&&B.layouts2.remeasure();p=p;d=l=true}c.cSize=p;if(a==1&&m&&m.wtResize){h=i.cSize;d=c.cSize;m.wtResize(m,Math.round(h),Math.round(d),true)}p-=r;if(!l){l=0;if(typeof c.cPadding===\"undefined\"){l=n?J(m,a):X(m,a);c.cPadding=l}else l=c.cPadding;p-=l}c.initialized=true;if(!(m&&p<=0)){if(p<e[3]-r)p=e[3]-r;n=[];m=c.config.length; l=i.config.length;for(d=0;d<m;++d)c.stretched[d]=false;if(p>=e[3]-r){r=p-e[4];h=[];var j=[0,0],s=[0,0],q=0;for(d=0;d<m;++d)if(e[1][d]>-1){k=-1;if(typeof c.fixedSize[d]!==\"undefined\"&&(d+1==m||e[1][d+1]>-1))k=c.fixedSize[d];else if(c.config[d][1]!==0&&c.config[d][1][0]>=0){k=c.config[d][1][0];if(c.config[d][1][1])k=(p-e[4])*k/100}if(k>=0){h[d]=-1;n[d]=k;r-=n[d]}else{if(c.config[d][0]>0){k=1;h[d]=c.config[d][0];q+=h[d]}else{k=0;h[d]=0}j[k]+=e[1][d];s[k]+=e[0][d];n[d]=e[0][d]}}else{h[d]=-2;n[d]=-1}if(q== 0){for(d=0;d<m;++d)if(h[d]==0){h[d]=1;++q}s[1]=s[0];j[1]=j[0];s[0]=0;j[0]=0}if(r>s[0]+j[1]){r-=s[0];if(r>s[1]){if(c.fitSize){r-=s[1];r=r/q;for(d=0;d<m;++d)if(h[d]>0){n[d]+=Math.round(h[d]*r);c.stretched[d]=true}}}else{k=1;if(r<j[k])r=j[k];r=s[k]-j[k]>0?(r-j[k])/(s[k]-j[k]):0;for(d=0;d<m;++d)if(h[d]>0){j=e[0][d]-e[1][d];n[d]=e[1][d]+Math.round(j*r)}}}else{for(d=0;d<m;++d)if(h[d]>0)n[d]=e[1][d];r-=j[1];k=0;if(r<j[k])r=j[k];r=s[k]-j[k]>0?(r-j[k])/(s[k]-j[k]):0;for(d=0;d<m;++d)if(h[d]==0){j=e[0][d]-e[1][d]; n[d]=e[1][d]+Math.round(j*r)}}}else n=e[1];c.sizes=n;e=c.margins[1];r=true;j=false;for(d=0;d<m;++d){if(n[d]>-1){var g=j;if(j){h=G+\"-rs\"+a+\"-\"+d;j=f.getElement(h);if(!j){j=document.createElement(\"div\");j.setAttribute(\"id\",h);j.di=d;j.style.position=\"absolute\";j.style[i.left]=i.margins[1]+\"px\";j.style[c.size]=c.margins[0]+\"px\";if(i.cSize)j.style[i.size]=i.cSize-i.margins[2]-i.margins[1]+\"px\";j.className=c.handleClass;b.insertBefore(j,b.firstChild);j.onmousedown=j.ontouchstart=function(V){fa(a,this, V||window.event)}}e+=2;w(j,c.left,e+\"px\");e+=2}j=c.config[d][1]!==0;if(r)r=false;else e+=c.margins[0]}for(s=0;s<l;++s)if((q=c.getItem(d,s))&&q.w){h=q.w;k=Math.max(n[d],0);if(q.span){var o,v=j;for(o=1;o<q.span[a];++o){if(d+o>=n.length)break;if(v)k+=4;v=c.config[d+o][1]!==0;if(n[d+o-1]>-1&&n[d+o]>-1)k+=c.margins[0];k+=n[d+o]}}var E;w(h,\"visibility\",\"\");v=q.align>>c.alignBits&15;o=q.ps[a];if(k<o)v=0;if(v){switch(v){case 1:E=e;break;case 4:E=e+(k-o)/2;break;case 2:E=e+(k-o);break}o-=I(q.w,a);if(q.layout){w(h, c.size,o+\"px\")&&O(q);q.set[a]=true}else if(k>=o&&q.set[a]){w(h,c.size,o+\"px\")&&O(q);q.set[a]=false}q.size[a]=o;q.psize[a]=o}else{E=I(q.w,a);v=Math.max(0,k-E);if(!f.isIE&&f.hasTag(h,\"TEXTAREA\"))v=k;E=false;if(f.isIE&&f.hasTag(h,\"BUTTON\"))E=true;if(!(h.style.display===\"none\"&&!h.ed)&&(E||k!=o||q.layout)){w(h,c.size,v+\"px\")&&O(q);q.set[a]=true}else if(q.fs[a])a==0&&w(h,c.size,q.fs[a]+\"px\");else{w(h,c.size,\"\")&&O(q);q.set[a]=false}E=e;q.size[a]=v;q.psize[a]=k}if(L)if(g){w(h,c.left,\"4px\");k=f.css(h,\"position\"); if(k!==\"absolute\")h.style.position=\"relative\"}else w(h,c.left,\"0px\");else w(h,c.left,E+\"px\");if(a==1){if(h.wtResize)h.wtResize(h,q.set[0]?Math.round(q.size[0]):-1,q.set[1]?Math.round(q.size[1]):-1,true);q.dirty=false}}if(n[d]>-1)e+=n[d]}$(b).children(\".\"+i.handleClass).css(c.size,p-c.margins[2]-c.margins[1]+\"px\")}}var f=B.WT;this.ancestor=null;this.descendants=[];var P=this,F=z,U=false,M=true,aa=false,Y=null,Q=null,ba=false,R=[],ca=false,W=$(document.body).hasClass(\"Wt-rtl\"),t=[{initialized:false, config:F.cols,margins:A,maxSize:u,measures:[],sizes:[],stretched:[],fixedSize:[],Left:W?\"Right\":\"Left\",left:W?\"right\":\"left\",Right:W?\"Left\":\"Right\",Size:\"Width\",size:\"width\",alignBits:0,getItem:function(a,b){return F.items[b*t[0].config.length+a]},setItem:function(a,b,c){F.items[b*t[0].config.length+a]=c},handleClass:\"Wt-vrh2\",resizeDir:\"h\",resizerClass:\"Wt-hsh2\",fitSize:N},{initialized:false,config:F.rows,margins:y,maxSize:H,measures:[],sizes:[],stretched:[],fixedSize:[],Left:\"Top\",left:\"top\",Right:\"Bottom\", Size:\"Height\",size:\"height\",alignBits:4,getItem:function(a,b){return F.items[a*t[0].config.length+b]},setItem:function(a,b,c){F.items[a*t[0].config.length+b]=c},handleClass:\"Wt-hrh2\",resizeDir:\"v\",resizerClass:\"Wt-vsh2\",fitSize:S}];jQuery.data(document.getElementById(G),\"layout\",this);this.updateSizeInParent=function(a){if(Y){var b=t[a],c=b.measures[2];if(b.maxSize>0)c=Math.min(b.maxSize,c);if(ca){b=f.getElement(G);if(!b)return;for(var i=b,e=i.parentNode;;){if(e.wtGetPS)c=e.wtGetPS(e,i,a,c);c+=T(e, a);if(e==Q)break;if(a==1&&e==b.parentNode&&!e.lh&&e.offsetHeight>c)c=e.offsetHeight;i=e;e=i.parentNode}}else c+=R[a];Y.setChildSize(Q,a,c)}};this.setConfig=function(a){var b=F;F=a;t[0].config=F.cols;t[1].config=F.rows;t[0].stretched=[];t[1].stretched=[];var c;a=0;for(c=b.items.length;a<c;++a){var i=b.items[a];if(i){if(i.set){i.set[0]&&w(i.w,t[0].size,\"\");i.set[1]&&w(i.w,t[1].size,\"\")}if(i.layout){P.setChildSize(i.w,0,i.ps[0]);P.setChildSize(i.w,1,i.ps[1])}}}M=true;B.layouts2.scheduleAdjust()};this.getId= function(){return G};this.setItemsDirty=function(a){var b,c,i=t[0].config.length;b=0;for(c=a.length;b<c;++b){var e=F.items[a[b][0]*i+a[b][1]];e.dirty=true;if(e.layout){e.layout=false;e.wasLayout=true;B.layouts2.setChildLayoutsDirty(P,e.w)}}U=true};this.setDirty=function(){M=true};this.setChildSize=function(a,b,c){var i=t[0].config.length,e=t[b],p,n;p=0;for(n=F.items.length;p<n;++p){var l=F.items[p];if(l&&l.id==a.id){a=b===0?p%i:p/i;if(l.align>>e.alignBits&15||!e.stretched[a]){if(!l.ps)l.ps=[];l.ps[b]= c}l.layout=true;O(l);break}}};this.measure=function(a){var b=f.getElement(G);if(b)if(!f.isHidden(b)){if(!ba){ba=true;if(aa=K==null){var c=b;c=c.parentNode;for(R=[0,0];;){R[0]+=T(c,0);R[1]+=T(c,1);if(c.wtGetPS)ca=true;var i=jQuery.data(c.parentNode,\"layout\");if(i){Y=i;Q=c;break}c=c;c=c.parentNode;if(c.childNodes.length!=1&&!c.wtGetPS)break}c=b.parentNode;for(i=0;i<2;++i)t[i].sizeSet=f.pxself(c,t[i].size)!=0}else{Y=jQuery.data(document.getElementById(K),\"layout\");Q=b;R[0]=T(Q,0);R[1]=T(Q,1)}}if(U|| M){c=aa?b.parentNode:null;da(a,b,c)}if(a==1)U=M=false}};this.setMaxSize=function(a,b){t[0].maxSize=a;t[1].maxSize=b};this.apply=function(a){var b=f.getElement(G);if(!b)return false;if(f.isHidden(b))return true;ga(a,b);return true};this.contains=function(a){var b=f.getElement(G);a=f.getElement(a.getId());return b&&a?f.contains(b,a):false};this.WT=f}");
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
				"new (function(){var B=[],G=false,K=this,N=false;this.find=function(u){return jQuery.data(document.getElementById(u),\"layout\")};this.setDirty=function(u){(u=jQuery.data(u,\"layout\"))&&u.setDirty()};this.setChildLayoutsDirty=function(u,H){var A,y;A=0;for(y=u.descendants.length;A<y;++A){var z=u.descendants[A];if(H){var C=u.WT.getElement(z.getId());if(C&&!u.WT.contains(H,C))continue}z.setDirty()}};this.add=function(u){function H(A,y){var z,C;z=0;for(C= A.length;z<C;++z){var x=A[z];if(x.getId()==y.getId()){A[z]=y;y.descendants=x.descendants;return}else if(x.contains(y)){H(x.descendants,y);return}else if(y.contains(x)){y.descendants.push(x);A.splice(z,1);--z;--C}}A.push(y)}H(B,u);K.scheduleAdjust()};var S=false,L=false;this.scheduleAdjust=function(u){if(u)N=true;if(!S){S=true;setTimeout(function(){K.adjust()},0)}};this.adjust=function(u,H){function A(z,C){var x,I;x=0;for(I=z.length;x<I;++x){var J=z[x];A(J.descendants,C);C==1&&N&&J.setDirty();J.measure(C)}} function y(z,C){var x,I;x=0;for(I=z.length;x<I;++x){var J=z[x];if(J.apply(C))y(J.descendants,C);else{z.splice(x,1);--x;--I}}}if(u){(u=this.find(u))&&u.setItemsDirty(H);K.scheduleAdjust()}else{S=false;if(!G){G=true;L=false;A(B,0);y(B,0);A(B,1);y(B,1);if(L){A(B,0);y(B,0);A(B,1);y(B,1)}N=L=G=false}}};this.updateConfig=function(u,H){(u=this.find(u))&&u.setConfig(H)};this.remeasure=function(){L=true};window.onresize=function(){K.scheduleAdjust(true)};window.onshow=function(){N=true;K.adjust()}})");
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
