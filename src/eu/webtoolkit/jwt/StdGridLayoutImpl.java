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

class StdGridLayoutImpl extends StdLayoutImpl {
	public StdGridLayoutImpl(WLayout layout, Grid grid) {
		super(layout);
		this.grid_ = grid;
		this.useFixedLayout_ = false;
		this.forceUpdate_ = false;
		String THIS_JS = "js/StdGridLayoutImpl.js";
		WApplication app = WApplication.getInstance();
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.getStyleSheet().addRule("table.Wt-hcenter",
					"margin: 0px auto;position: relative");
			app.doJavaScript(wtjs1(app), false);
			app.doJavaScript(appjs1(app), false);
			app.setJavaScriptLoaded(THIS_JS);
			app.addAutoJavaScript(app.getJavaScriptClass()
					+ ".layouts.adjust();");
		}
	}

	public int getMinimumHeight() {
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		int total = 0;
		for (int i = 0; i < rowCount; ++i) {
			int minHeight = 0;
			for (int j = 0; j < colCount; ++j) {
				WLayoutItem item = this.grid_.items_.get(i).get(j).item_;
				if (item != null) {
					minHeight = Math.max(minHeight, getImpl(item)
							.getMinimumHeight());
				}
			}
			total += minHeight;
		}
		return total * (rowCount - 1) * this.grid_.verticalSpacing_;
	}

	public DomElement createDomElement(boolean fitWidth, boolean fitHeight,
			WApplication app) {
		this.forceUpdate_ = false;
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		boolean hasResizeHandles = false;
		for (int i = 0; i < colCount; ++i) {
			if (this.grid_.columns_.get(i).resizable_) {
				hasResizeHandles = true;
				break;
			}
		}
		if (!hasResizeHandles) {
			for (int i = 0; i < rowCount; ++i) {
				if (this.grid_.rows_.get(i).resizable_) {
					hasResizeHandles = true;
					break;
				}
			}
		}
		if (hasResizeHandles
				&& !app.isJavaScriptLoaded("js/StdGridLayoutImpl-resize.js")) {
			SizeHandle.loadJavaScript(app);
			app.doJavaScript(wtjs2(app), false);
			app.setJavaScriptLoaded("js/StdGridLayoutImpl-resize.js");
		}
		int totalColStretch = 0;
		for (int col = 0; col < colCount; ++col) {
			totalColStretch += Math.max(0,
					this.grid_.columns_.get(col).stretch_);
		}
		int totalRowStretch = 0;
		for (int row = 0; row < rowCount; ++row) {
			totalRowStretch += Math.max(0, this.grid_.rows_.get(row).stretch_);
		}
		int[] margin = { 0, 0, 0, 0 };
		if (this.getLayout().getParentLayout() == null) {
			margin[3] = this.getLayout().getContentsMargin(Side.Left);
			margin[0] = this.getLayout().getContentsMargin(Side.Top);
			margin[1] = this.getLayout().getContentsMargin(Side.Right);
			margin[2] = this.getLayout().getContentsMargin(Side.Bottom);
		}
		DomElement div = DomElement.createNew(DomElementType.DomElement_DIV);
		div.setId(this.getId());
		div.setProperty(Property.PropertyStylePosition, "relative");
		String divStyle = "";
		if (fitHeight && !app.getEnvironment().agentIsIElt(9)) {
			divStyle += "height: 100%;";
		}
		if (app.getEnvironment().agentIsIElt(9)) {
			divStyle += "zoom: 1;";
		}
		if (divStyle.length() != 0) {
			div.setProperty(Property.PropertyStyle, divStyle);
		}
		DomElement table = DomElement
				.createNew(DomElementType.DomElement_TABLE);
		{
			String style = "";
			if (fitWidth) {
				if (this.useFixedLayout_) {
					style = "table-layout: fixed;";
				}
				style += "width: 100%;";
			}
			if (fitHeight) {
				style += "height: 100%;";
			}
			table.setProperty(Property.PropertyStyle, style);
		}
		DomElement tbody = DomElement
				.createNew(DomElementType.DomElement_TBODY);
		if (fitWidth) {
			for (int col = 0; col < colCount; ++col) {
				DomElement c = DomElement
						.createNew(DomElementType.DomElement_COL);
				int stretch = Math
						.max(0, this.grid_.columns_.get(col).stretch_);
				if (stretch != 0 || fitWidth && totalColStretch == 0) {
					int pct = totalColStretch == 0 ? 100 / colCount : 100
							* stretch / totalColStretch;
					c.setProperty(Property.PropertyStyle, "width:"
							+ String.valueOf(pct) + "%;");
				}
				table.addChild(c);
				boolean resizeHandleRight = col < colCount - 1
						&& this.grid_.columns_.get(col).resizable_;
				if (resizeHandleRight) {
					c = DomElement.createNew(DomElementType.DomElement_COL);
					c.setProperty(Property.PropertyStyleWidth, String
							.valueOf(this.grid_.horizontalSpacing_)
							+ "px");
					c.setProperty(Property.PropertyClass, "Wt-vrh");
					table.addChild(c);
				}
			}
		}
		List<Boolean> overSpanned = new ArrayList<Boolean>();
		{
			int insertPos = 0;
			for (int ii = 0; ii < colCount * rowCount; ++ii)
				overSpanned.add(insertPos + ii, false);
		}
		;
		boolean resizeHandleAbove = false;
		for (int row = 0; row < rowCount; ++row) {
			boolean resizeHandleBelow = row < rowCount - 1
					&& this.grid_.rows_.get(row).resizable_;
			DomElement tr = DomElement.createNew(DomElementType.DomElement_TR);
			String heightPct = "";
			int stretch = Math.max(0, this.grid_.rows_.get(row).stretch_);
			if (stretch != 0 || fitHeight && totalRowStretch == 0) {
				int pct = totalRowStretch == 0 ? 100 / rowCount : 100 * stretch
						/ totalRowStretch;
				StringWriter style = new StringWriter();
				style.append("height: ").append(String.valueOf(pct)).append(
						"%;");
				heightPct = style.toString();
				tr.setProperty(Property.PropertyStyle, heightPct);
			}
			int[] padding = { 0, 0, 0, 0 };
			if (row == 0) {
				padding[0] = margin[0];
			} else {
				if (!resizeHandleAbove) {
					padding[0] = (this.grid_.verticalSpacing_ + 1) / 2;
				}
			}
			if (row + 1 == rowCount) {
				padding[2] = margin[2];
			} else {
				if (!resizeHandleBelow) {
					padding[2] = this.grid_.verticalSpacing_ / 2;
				}
			}
			boolean resizeHandleLeft = false;
			for (int col = 0; col < colCount; ++col) {
				boolean resizeHandleRight = col < colCount - 1
						&& this.grid_.columns_.get(col - 1
								+ this.grid_.items_.get(row).get(col).colSpan_).resizable_;
				if (!overSpanned.get(row * colCount + col)) {
					Grid.Item item = this.grid_.items_.get(row).get(col);
					boolean itemFitWidth = item.colSpan_ == (int) colCount
							|| totalColStretch == 0;
					boolean itemFitHeight = item.rowSpan_ == (int) rowCount
							|| totalRowStretch == 0;
					int colSpan = 0;
					for (int i = 0; i < item.rowSpan_; ++i) {
						if (this.grid_.rows_.get(row + i).stretch_ != 0) {
							itemFitHeight = true;
						} else {
							if (!(stretch != 0)) {
								itemFitHeight = false;
							}
						}
						colSpan = item.colSpan_;
						for (int j = 0; j < item.colSpan_; ++j) {
							if (this.grid_.columns_.get(col + j).stretch_ != 0) {
								itemFitWidth = true;
							}
							if (i + j > 0) {
								overSpanned.set((row + i) * colCount + col + j,
										true);
							}
							if (j + 1 < item.colSpan_
									&& this.grid_.columns_.get(col + j).resizable_) {
								++colSpan;
							}
						}
					}
					AlignmentFlag hAlign = EnumUtils.enumFromSet(EnumUtils
							.mask(item.alignment_,
									AlignmentFlag.AlignHorizontalMask));
					AlignmentFlag vAlign = EnumUtils.enumFromSet(EnumUtils
							.mask(item.alignment_,
									AlignmentFlag.AlignVerticalMask));
					if (hAlign != null && hAlign != AlignmentFlag.AlignJustify) {
						itemFitWidth = false;
					}
					if (vAlign != null) {
						itemFitHeight = false;
					}
					padding[1] = padding[3] = 0;
					if (col == 0) {
						padding[3] = margin[3];
					} else {
						if (!resizeHandleLeft) {
							padding[3] = (this.grid_.horizontalSpacing_ + 1) / 2;
						}
					}
					if (col + item.colSpan_ == colCount) {
						padding[1] = margin[1];
					} else {
						if (!resizeHandleRight) {
							padding[1] = this.grid_.horizontalSpacing_ / 2;
						}
					}
					DomElement td = DomElement
							.createNew(DomElementType.DomElement_TD);
					if (app.getEnvironment().agentIsIElt(9)) {
						td.setProperty(Property.PropertyStylePosition,
								"relative");
					}
					if (item.item_ != null) {
						DomElement c = getImpl(item.item_).createDomElement(
								itemFitWidth, itemFitHeight, app);
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
								td.setProperty(Property.PropertyStyleTextAlign,
										"right");
							}
							break;
						case AlignLeft:
							if (!c.isDefaultInline()) {
								c.setProperty(Property.PropertyStyleFloat,
										"left");
							} else {
								td.setProperty(Property.PropertyStyleTextAlign,
										"left");
							}
							break;
						case AlignJustify:
							if (c.getProperty(Property.PropertyStyleWidth)
									.length() == 0
									&& this.useFixedLayout_
									&& !app.getEnvironment().agentIsWebKit()
									&& !app.getEnvironment().agentIsGecko()
									&& !c.isDefaultInline()) {
								c.setProperty(Property.PropertyStyleWidth,
										"100%");
							}
							break;
						default:
							break;
						}
						td.addChild(c);
					}
					{
						String style = "";
						if (vAlign == null) {
							style += heightPct;
						}
						if (padding[0] == padding[1]
								&& padding[0] == padding[2]
								&& padding[0] == padding[3]) {
							if (padding[0] != 0) {
								style += "padding:"
										+ String.valueOf(padding[0]) + "px;";
							}
						} else {
							style += "padding:" + String.valueOf(padding[0])
									+ "px " + String.valueOf(padding[1])
									+ "px " + String.valueOf(padding[2])
									+ "px " + String.valueOf(padding[3])
									+ "px;";
						}
						if (vAlign != null) {
							switch (vAlign) {
							case AlignTop:
								style += "vertical-align:top;";
								break;
							case AlignMiddle:
								style += "vertical-align:middle;";
								break;
							case AlignBottom:
								style += "vertical-align:bottom;";
							default:
								break;
							}
						}
						if (style.length() != 0) {
							td.setProperty(Property.PropertyStyle, style);
						}
					}
					if (item.rowSpan_ != 1) {
						td.setProperty(Property.PropertyRowSpan, String
								.valueOf(item.rowSpan_));
					}
					if (colSpan != 1) {
						td.setProperty(Property.PropertyColSpan, String
								.valueOf(colSpan));
					}
					td.setProperty(Property.PropertyStyleOverflowX, "hidden");
					tr.addChild(td);
					if (resizeHandleRight) {
						td = DomElement.createNew(DomElementType.DomElement_TD);
						td.setProperty(Property.PropertyClass, "Wt-vrh");
						String style = "padding:" + String.valueOf(padding[0])
								+ "px 0px" + String.valueOf(padding[2]) + "px;";
						td.setProperty(Property.PropertyStyle, style);
						DomElement div2 = DomElement
								.createNew(DomElementType.DomElement_DIV);
						div2.setProperty(Property.PropertyStyleWidth, String
								.valueOf(this.grid_.horizontalSpacing_)
								+ "px");
						td.addChild(div2);
						tr.addChild(td);
					}
				}
				resizeHandleLeft = resizeHandleRight;
			}
			tbody.addChild(tr);
			if (resizeHandleBelow) {
				tr = DomElement.createNew(DomElementType.DomElement_TR);
				tr.setProperty(Property.PropertyClass, "Wt-hrh");
				String height = String.valueOf(this.grid_.verticalSpacing_)
						+ "px";
				tr.setProperty(Property.PropertyStyleHeight, height);
				DomElement td = DomElement
						.createNew(DomElementType.DomElement_TD);
				td.setProperty(Property.PropertyColSpan, String
						.valueOf(colCount));
				String style2 = "padding: 0px" + String.valueOf(margin[1])
						+ "px 0px" + String.valueOf(margin[3]) + "px;";
				td.setProperty(Property.PropertyStyleHeight, style2);
				DomElement div2 = DomElement
						.createNew(DomElementType.DomElement_DIV);
				div2.setProperty(Property.PropertyStyleHeight, height);
				td.addChild(div2);
				tr.addChild(td);
				tbody.addChild(tr);
			}
			resizeHandleAbove = resizeHandleBelow;
		}
		table.addChild(tbody);
		if (fitHeight) {
			StringWriter layoutAdd = new StringWriter();
			layoutAdd.append(app.getJavaScriptClass()).append(
					".layouts.add(new Wt3_1_7.StdLayout( Wt3_1_7, '").append(
					div.getId()).append("', { stretch: [");
			for (int i = 0; i < rowCount; ++i) {
				if (i != 0) {
					layoutAdd.append(",");
				}
				int stretch = 0;
				if (totalRowStretch == 0 && fitHeight) {
					stretch = 1;
				} else {
					stretch = this.grid_.rows_.get(i).stretch_;
				}
				layoutAdd.append(String.valueOf(stretch));
			}
			layoutAdd.append("], minheight: [");
			for (int i = 0; i < rowCount; ++i) {
				if (i != 0) {
					layoutAdd.append(",");
				}
				int minHeight = 0;
				for (int j = 0; j < colCount; ++j) {
					WLayoutItem item = this.grid_.items_.get(i).get(j).item_;
					if (item != null) {
						minHeight = Math.max(minHeight, getImpl(item)
								.getMinimumHeight());
					}
				}
				if (i == 0) {
					minHeight += margin[0];
				} else {
					minHeight += this.grid_.verticalSpacing_;
				}
				if (i == rowCount - 1) {
					minHeight += margin[2];
				}
				layoutAdd.append(String.valueOf(minHeight));
			}
			layoutAdd.append("]}));");
			table.callJavaScript(layoutAdd.toString());
		}
		div.addChild(table);
		return div;
	}

	public void updateDom() {
		if (this.forceUpdate_) {
			this.forceUpdate_ = false;
			WApplication app = WApplication.getInstance();
			app.doJavaScript(app.getJavaScriptClass() + ".layouts.adjust('"
					+ this.getId() + "');");
		}
	}

	public void setHint(String name, String value) {
		if (name.equals("table-layout")) {
			if (value.equals("fixed")) {
				this.useFixedLayout_ = true;
			} else {
				if (value.equals("auto")) {
					this.useFixedLayout_ = false;
				} else {
					WApplication.getInstance().log("error").append(
							"WGridLayout: unrecognized hint value '").append(
							value).append("' for '").append(name).append("'");
				}
			}
		} else {
			WApplication.getInstance().log("error").append(
					"WGridLayout: unrecognized hint '").append(name)
					.append("'");
		}
	}

	public static String getChildrenResizeJS() {
		String THIS_JS = "js/WtResize.js";
		WApplication app = WApplication.getInstance();
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.doJavaScript(wtjs10(app), false);
			app.setJavaScriptLoaded(THIS_JS);
		}
		return "Wt3_1_7.ChildrenResize";
	}

	public boolean itemResized(WLayoutItem item) {
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		for (int row = 0; row < rowCount; ++row) {
			if (this.grid_.rows_.get(row).stretch_ <= 0) {
				for (int col = 0; col < colCount; ++col) {
					if (this.grid_.items_.get(row).get(col).item_ == item) {
						this.forceUpdate_ = true;
						return true;
					}
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
	private boolean useFixedLayout_;
	private boolean forceUpdate_;

	static String wtjs1(WApplication app) {
		return "Wt3_1_7.StdLayout = function(a,s,i){var o=this;this.getId=function(){return s};this.WT=a;this.marginH=function(b){var f=b.parentNode,g=a.px(b,\"marginLeft\");g+=a.px(b,\"marginRight\");g+=a.px(b,\"borderLeftWidth\");g+=a.px(b,\"borderRightWidth\");g+=a.px(b,\"paddingLeft\");g+=a.px(b,\"paddingRight\");g+=a.pxself(f,\"paddingLeft\");g+=a.pxself(f,\"paddingRight\");return g};this.marginV=function(b){var f=a.px(b,\"marginTop\");f+=a.px(b,\"marginBottom\");f+=a.px(b,\"borderTopWidth\");f+=a.px(b,\"borderBottomWidth\"); f+=a.px(b,\"paddingTop\");f+=a.px(b,\"paddingBottom\");return f};this.getColumn=function(b){var f,g,j,h=a.getElement(s).firstChild.childNodes;g=f=0;for(j=h.length;g<j;g++){var q=h[g];if(a.hasTag(q,\"COLGROUP\")){g=-1;h=q.childNodes;j=h.length}if(a.hasTag(q,\"COL\"))if(q.className!=\"Wt-vrh\")if(f==b)return q;else++f}return null};this.adjustRow=function(b,f){if(b.style.height!=f+\"px\")b.style.height=f+\"px\";b=b.childNodes;var g,j,h,q;g=0;q=-1;for(j=b.length;g<j;++g){h=b[g];var d=f;d-=a.pxself(h,\"paddingTop\"); d-=a.pxself(h,\"paddingBottom\");if(d<=0)d=0;h.className!=\"Wt-vrh\"&&++q;h.style.height=d+\"px\";if(!(h.style.verticalAlign||h.childNodes.length==0)){var c=h.childNodes[0];if(d<=0)d=0;if(c.className==\"Wt-hcenter\"){c.style.height=d+\"px\";c=c.firstChild.firstChild;if(!a.hasTag(c,\"TD\"))c=c.firstChild;if(c.style.height!=d+\"px\")c.style.height=d+\"px\";c=c.firstChild}if(h.childNodes.length==1)d-=this.marginV(c);if(d<=0)d=0;if(!a.hasTag(c,\"TABLE\"))if(c.wtResize){h=c.parentNode.offsetWidth-o.marginH(c);if(o.getColumn(q).style.width!= \"\"){c.style.position=\"absolute\";c.style.width=h+\"px\"}c.wtResize(c,h,d)}else if(c.style.height!=d+\"px\"){c.style.height=d+\"px\";if(c.className==\"Wt-wrapdiv\")if(a.isIE&&a.hasTag(c.firstChild,\"TEXTAREA\"))c.firstChild.style.height=d-a.pxself(c,\"marginBottom\")+\"px\"}}}};this.adjust=function(){var b=a.getElement(s);if(!b)return false;o.initResize&&o.initResize(a,s,i);if(a.isHidden(b))return true;var f=b.firstChild,g=b.parentNode;if(f.style.height!=\"\")f.style.height=\"\";if(!(b.dirty||f.w!=g.clientWidth||f.h!= g.clientHeight))return true;b.dirty=null;var j=a.pxself(g,\"height\");if(j==0){j=g.clientHeight;j-=a.px(g,\"paddingTop\");j-=a.px(g,\"paddingBottom\")}j-=a.px(b,\"marginTop\");j-=a.px(b,\"marginBottom\");var h,q;if(g.children){h=0;for(q=g.children.length;h<q;++h){var d=g.children[h];if(d!=b)j-=$(d).outerHeight()}}var c=b=0,l,t;l=h=0;for(q=f.rows.length;h<q;h++){d=f.rows[h];if(d.className==\"Wt-hrh\")j-=d.offsetHeight;else{c+=i.minheight[l];if(i.stretch[l]<=0)j-=d.offsetHeight;else b+=i.stretch[l];++l}}j=j>c? j:c;if(b!=0&&j>0){c=j;var p;l=h=0;for(q=f.rows.length;h<q;h++)if(f.rows[h].className!=\"Wt-hrh\"){d=f.rows[h];if(i.stretch[l]!=0){if(i.stretch[l]!=-1){p=j*i.stretch[l]/b;p=c>p?p:c;p=Math.round(i.minheight[l]>p?i.minheight[l]:p);c-=p}else p=d.offsetHeight;this.adjustRow(d,p)}++l}}f.w=g.clientWidth;f.h=g.clientHeight;if(f.style.tableLayout!=\"fixed\")return true;b=0;l=f.childNodes;g=0;for(j=l.length;g<j;g++){c=l[g];var y,A,z;if(a.hasTag(c,\"COLGROUP\")){g=-1;l=c.childNodes;j=l.length}if(a.hasTag(c,\"COL\")){if(a.pctself(c, \"width\")==0){h=p=0;for(q=f.rows.length;h<q;h++){d=f.rows[h];d=d.childNodes;A=y=0;for(z=d.length;A<z;A++){t=d[A];if(t.colSpan==1&&y==b&&t.childNodes.length==1){d=t.firstChild;d=d.offsetWidth+o.marginH(d);p=Math.max(p,d);break}y+=t.colSpan;if(y>b)break}}if(p>0&&a.pxself(c,\"width\")!=p)c.style.width=p+\"px\"}++b}}return true};this.contains=function(b){var f=a.getElement(s);b=a.getElement(b.getId());return a.contains(f,b)};this.adjust()};";
	}

	static String wtjs2(WApplication app) {
		return "Wt3_1_7.StdLayout.prototype.initResize = function(a,s,i){function o(e,k){if(e.offsetWidth>0)return e.offsetWidth;else{e=l.firstChild.rows[0];var m,n,r,u;r=n=0;for(u=e.childNodes.length;n<u;++n){m=e.childNodes[n];if(m.className!=\"Wt-vrh\"){if(r==k)return m.offsetWidth;r+=m.colSpan}}return 0}}function b(e,k){var m=a.getElement(s).firstChild;c(e).style.width=k+\"px\";var n,r,u,v;r=n=0;for(u=m.rows.length;n<u;n++){v=m.rows[n];if(v.className!=\"Wt-hrh\"){var w,B,D,F;D=B=0;for(F=v.childNodes.length;B< F;++B){w=v.childNodes[B];if(w.className!=\"Wt-vrh\"){if(w.colSpan==1&&D==e&&w.childNodes.length==1){v=w.firstChild;w=k-d.marginH(v);v.style.width=w+\"px\";break}D+=w.colSpan}}++r}}}function f(e,k,m){var n=e.firstChild;new a.SizeHandle(a,\"v\",n.offsetHeight,n.offsetWidth,-e.parentNode.previousSibling.offsetHeight,e.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(r){j(e,k,r)},n,l,m,0,0)}function g(e,k,m){var n=-e.previousSibling.offsetWidth,r=e.nextSibling.offsetWidth,u=e.firstChild,v=a.pxself(t.rows[0].childNodes[0], \"paddingTop\"),w=a.pxself(t.rows[t.rows.length-1].childNodes[0],\"paddingBottom\");new a.SizeHandle(a,\"h\",u.offsetWidth,t.offsetHeight-v-w,n,r,\"Wt-hsh\",function(B){q(e,k,B)},u,l,m,0,-e.offsetTop+v-a.pxself(e,\"paddingTop\"))}function j(e,k,m){var n=e.parentNode.previousSibling;e=e.parentNode.nextSibling;var r=n.offsetHeight,u=e.offsetHeight;if(i.stretch[k]>0&&i.stretch[k+1]>0)i.stretch[k]=-1;if(i.stretch[k+1]==0)i.stretch[k+1]=-1;i.stretch[k]<=0&&d.adjustRow(n,r+m);i.stretch[k+1]<=0&&d.adjustRow(e,u-m); a.getElement(s).dirty=true;window.onresize()}function h(){var e,k=0;for(e=0;;++e){var m=c(e);if(m)k+=a.pctself(m,\"width\");else break}if(k!=0)for(e=0;;++e)if(m=c(e)){var n=a.pctself(m,\"width\");if(n)m.style.width=n*100/k+\"%\"}else break}function q(e,k,m){e=c(k);var n=o(e,k),r=c(k+1),u=o(r,k+1);if(a.pctself(e,\"width\")>0&&a.pctself(r,\"width\")>0){e.style.width=\"\";h()}a.pctself(e,\"width\")==0&&b(k,n+m);a.pctself(r,\"width\")==0&&b(k+1,u-m);window.onresize()}var d=this,c=d.getColumn,l=a.getElement(s);if(l)if(!d.resizeInitialized){var t= l.firstChild,p,y,A,z;y=p=0;for(A=t.rows.length;p<A;p++){z=t.rows[p];if(z.className==\"Wt-hrh\"){var x=z.firstChild;x.ri=y-1;x.onmousedown=x.ontouchstart=function(e){f(this,this.ri,e||window.event)}}else{var C,E,G;E=C=0;for(G=z.childNodes.length;C<G;++C){x=z.childNodes[C];if(x.className==\"Wt-vrh\"){x.ci=E-1;x.onmousedown=x.ontouchstart=function(e){g(this,this.ci,e||window.event)}}else E+=x.colSpan}++y}}d.resizeInitialized=true}};";
	}

	static String appjs1(WApplication app) {
		return app.getJavaScriptClass()
				+ ".layouts = new (function(){var a=[],s=false;this.add=function(i){var o,b;o=0;for(b=a.length;o<b;++o){var f=a[o];if(f.getId()==i.getId()){a[o]=i;return}else if(i.contains(f)){a.splice(o,0,i);return}}a.push(i)};this.adjust=function(i){if(i){if(i=$(\"#\"+i).get(0))i.dirty=true}else if(!s){s=true;for(var o=0;o<a.length;++o){i=a[o];if(!i.adjust()){i.WT.arrayRemove(a,o);--o}}s=false}}});";
	}

	static String wtjs10(WApplication app) {
		return "Wt3_1_7.ChildrenResize = function(b,f,d){var c,e,a;b.style.height=d+\"px\";c=0;for(e=b.childNodes.length;c<e;++c){a=b.childNodes[c];if(a.nodeType==1)if(a.wtResize)a.wtResize(a,f,d);else if(a.style.height!=b.style.height)a.style.height=b.style.height}};";
	}
}
