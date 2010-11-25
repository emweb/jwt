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
			app.doJavaScript(layoutAdd.toString());
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
		return "Wt3_1_7.StdLayout = function(a,s,i){var p=this;this.getId=function(){return s};this.WT=a;this.marginH=function(c){var h=c.parentNode,g=a.px(c,\"marginLeft\");g+=a.px(c,\"marginRight\");g+=a.px(c,\"borderLeftWidth\");g+=a.px(c,\"borderRightWidth\");g+=a.px(h,\"paddingLeft\");g+=a.px(h,\"paddingRight\");return g};this.marginV=function(c){var h=a.px(c,\"marginTop\");h+=a.px(c,\"marginBottom\");h+=a.px(c,\"borderTopWidth\");h+=a.px(c,\"borderBottomWidth\");h+=a.px(c,\"paddingTop\");h+=a.px(c,\"paddingBottom\"); return h};this.getColumn=function(c){var h,g,j,f=a.getElement(s).firstChild.childNodes;g=h=0;for(j=f.length;g<j;g++){var q=f[g];if(a.hasTag(q,\"COLGROUP\")){g=-1;f=q.childNodes;j=f.length}if(a.hasTag(q,\"COL\"))if(q.className!=\"Wt-vrh\")if(h==c)return q;else++h}return null};this.adjustRow=function(c,h){if(c.style.height!=h+\"px\")c.style.height=h+\"px\";c=c.childNodes;var g,j,f,q;g=0;q=-1;for(j=c.length;g<j;++g){f=c[g];var d=h;d-=a.pxself(f,\"paddingTop\");d-=a.pxself(f,\"paddingBottom\");if(d<=0)d=0;f.className!= \"Wt-vrh\"&&++q;f.style.height=d+\"px\";if(!(f.style.verticalAlign||f.childNodes.length==0)){var b=f.childNodes[0];if(d<=0)d=0;if(b.className==\"Wt-hcenter\"){b.style.height=d+\"px\";b=b.firstChild.firstChild;if(!a.hasTag(b,\"TD\"))b=b.firstChild;if(b.style.height!=d+\"px\")b.style.height=d+\"px\";b=b.firstChild}if(f.childNodes.length==1)d-=this.marginV(b);if(d<=0)d=0;if(!a.hasTag(b,\"TABLE\"))if(b.wtResize){f=b.parentNode.offsetWidth-p.marginH(b);if(p.getColumn(q).style.width!=\"\"){b.style.position=\"absolute\";b.style.width= f+\"px\"}b.wtResize(b,f,d)}else if(b.style.height!=d+\"px\"){b.style.height=d+\"px\";if(b.className==\"Wt-wrapdiv\")if(a.isIE&&a.hasTag(b.firstChild,\"TEXTAREA\"))b.firstChild.style.height=d-a.pxself(b,\"marginBottom\")+\"px\"}}}};this.adjust=function(){var c=a.getElement(s);if(!c)return false;p.initResize&&p.initResize(a,s,i);if(a.isHidden(c))return true;var h=c.firstChild,g=c.parentNode;if(h.style.height!=\"\")h.style.height=\"\";if(!(c.dirty||h.w!=g.clientWidth||h.h!=g.clientHeight))return true;c.dirty=null;var j= a.pxself(g,\"height\");if(j==0){j=g.clientHeight;j-=a.px(g,\"paddingTop\");j-=a.px(g,\"paddingBottom\")}j-=a.px(c,\"marginTop\");j-=a.px(c,\"marginBottom\");var f,q;if(g.children){f=0;for(q=g.children.length;f<q;++f){var d=g.children[f];if(d!=c)j-=$(d).outerHeight()}}var b=c=0,l,t;l=f=0;for(q=h.rows.length;f<q;f++){d=h.rows[f];if(d.className==\"Wt-hrh\")j-=d.offsetHeight;else{b+=i.minheight[l];if(i.stretch[l]<=0)j-=d.offsetHeight;else c+=i.stretch[l];++l}}j=j>b?j:b;if(c!=0&&j>0){b=j;var o;l=f=0;for(q=h.rows.length;f< q;f++)if(h.rows[f].className!=\"Wt-hrh\"){d=h.rows[f];if(i.stretch[l]!=0){if(i.stretch[l]!=-1){o=j*i.stretch[l]/c;o=b>o?o:b;o=Math.round(i.minheight[l]>o?i.minheight[l]:o);b-=o}else o=d.offsetHeight;this.adjustRow(d,o)}++l}}h.w=g.clientWidth;h.h=g.clientHeight;if(h.style.tableLayout!=\"fixed\")return true;c=0;l=h.childNodes;g=0;for(j=l.length;g<j;g++){b=l[g];var y,A,z;if(a.hasTag(b,\"COLGROUP\")){g=-1;l=b.childNodes;j=l.length}if(a.hasTag(b,\"COL\")){if(a.pctself(b,\"width\")==0){f=o=0;for(q=h.rows.length;f< q;f++){d=h.rows[f];d=d.childNodes;A=y=0;for(z=d.length;A<z;A++){t=d[A];if(t.colSpan==1&&y==c&&t.childNodes.length==1){d=t.firstChild;d=d.offsetWidth+p.marginH(d);o=Math.max(o,d);break}y+=t.colSpan;if(y>c)break}}if(o>0&&a.pxself(b,\"width\")!=o)b.style.width=o+\"px\"}++c}}return true};this.adjust()};";
	}

	static String wtjs2(WApplication app) {
		return "Wt3_1_7.StdLayout.prototype.initResize = function(a,s,i){function p(e,k){if(e.offsetWidth>0)return e.offsetWidth;else{e=l.firstChild.rows[0];var m,n,r,u;r=n=0;for(u=e.childNodes.length;n<u;++n){m=e.childNodes[n];if(m.className!=\"Wt-vrh\"){if(r==k)return m.offsetWidth;r+=m.colSpan}}return 0}}function c(e,k){var m=a.getElement(s).firstChild;b(e).style.width=k+\"px\";var n,r,u,v;r=n=0;for(u=m.rows.length;n<u;n++){v=m.rows[n];if(v.className!=\"Wt-hrh\"){var w,B,D,F;D=B=0;for(F=v.childNodes.length;B< F;++B){w=v.childNodes[B];if(w.className!=\"Wt-vrh\"){if(w.colSpan==1&&D==e&&w.childNodes.length==1){v=w.firstChild;w=k-d.marginH(v);v.style.width=w+\"px\";break}D+=w.colSpan}}++r}}}function h(e,k,m){var n=e.firstChild;new a.SizeHandle(a,\"v\",n.offsetHeight,n.offsetWidth,-e.parentNode.previousSibling.offsetHeight,e.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(r){j(e,k,r)},n,l,m,0,0)}function g(e,k,m){var n=-e.previousSibling.offsetWidth,r=e.nextSibling.offsetWidth,u=e.firstChild,v=a.pxself(t.rows[0].childNodes[0], \"paddingTop\"),w=a.pxself(t.rows[t.rows.length-1].childNodes[0],\"paddingBottom\");new a.SizeHandle(a,\"h\",u.offsetWidth,t.offsetHeight-v-w,n,r,\"Wt-hsh\",function(B){q(e,k,B)},u,l,m,0,-e.offsetTop+v-a.pxself(e,\"paddingTop\"))}function j(e,k,m){var n=e.parentNode.previousSibling;e=e.parentNode.nextSibling;var r=n.offsetHeight,u=e.offsetHeight;if(i.stretch[k]>0&&i.stretch[k+1]>0)i.stretch[k]=-1;if(i.stretch[k+1]==0)i.stretch[k+1]=-1;i.stretch[k]<=0&&d.adjustRow(n,r+m);i.stretch[k+1]<=0&&d.adjustRow(e,u-m); a.getElement(s).dirty=true;window.onresize()}function f(){var e,k=0;for(e=0;;++e){var m=b(e);if(m)k+=a.pctself(m,\"width\");else break}if(k!=0)for(e=0;;++e)if(m=b(e)){var n=a.pctself(m,\"width\");if(n)m.style.width=n*100/k+\"%\"}else break}function q(e,k,m){e=b(k);var n=p(e,k),r=b(k+1),u=p(r,k+1);if(a.pctself(e,\"width\")>0&&a.pctself(r,\"width\")>0){e.style.width=\"\";f()}a.pctself(e,\"width\")==0&&c(k,n+m);a.pctself(r,\"width\")==0&&c(k+1,u-m);window.onresize()}var d=this,b=d.getColumn,l=a.getElement(s);if(l)if(!d.resizeInitialized){var t= l.firstChild,o,y,A,z;y=o=0;for(A=t.rows.length;o<A;o++){z=t.rows[o];if(z.className==\"Wt-hrh\"){var x=z.firstChild;x.ri=y-1;x.onmousedown=x.ontouchstart=function(e){h(this,this.ri,e||window.event)}}else{var C,E,G;E=C=0;for(G=z.childNodes.length;C<G;++C){x=z.childNodes[C];if(x.className==\"Wt-vrh\"){x.ci=E-1;x.onmousedown=x.ontouchstart=function(e){g(this,this.ci,e||window.event)}}else E+=x.colSpan}++y}}d.resizeInitialized=true}};";
	}

	static String appjs1(WApplication app) {
		return app.getJavaScriptClass()
				+ ".layouts = new (function(){var a=[],s=false;this.add=function(i){var p,c;p=0;for(c=a.length;p<c;++p)if(a[p].getId()==i.getId()){a[p]=i;return}a.push(i)};this.adjust=function(i){if(i){if(i=$(\"#\"+i).get(0))i.dirty=true}else if(!s){s=true;for(var p=0;p<a.length;++p){i=a[p];if(!i.adjust()){i.WT.arrayRemove(a,p);--p}}s=false}}});";
	}

	static String wtjs10(WApplication app) {
		return "Wt3_1_7.ChildrenResize = function(b,f,d){var c,e,a;b.style.height=d+\"px\";c=0;for(e=b.childNodes.length;c<e;++c){a=b.childNodes[c];if(a.nodeType==1)if(a.wtResize)a.wtResize(a,f,d);else if(a.style.height!=b.style.height)a.style.height=b.style.height}};";
	}
}
