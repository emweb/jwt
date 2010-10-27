/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;

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
					".layouts.add(new Wt3_1_6.StdLayout( Wt3_1_6, '").append(
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
		return "Wt3_1_6.ChildrenResize";
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
		return "Wt3_1_6.StdLayout = function(a,t,g){var n=this;this.getId=function(){return t};this.WT=a;this.marginH=function(d){var k=d.parentNode;return a.px(d,\"marginLeft\")+a.px(d,\"marginRight\")+a.px(d,\"borderLeftWidth\")+a.px(d,\"borderRightWidth\")+a.px(k,\"paddingLeft\")+a.px(k,\"paddingRight\")};this.marginV=function(d){return a.px(d,\"marginTop\")+a.px(d,\"marginBottom\")+a.px(d,\"borderTopWidth\")+a.px(d,\"borderBottomWidth\")+a.px(d,\"paddingTop\")+a.px(d,\"paddingBottom\")};this.adjustRow=function(d,k){if(d.style.height!= k+\"px\")d.style.height=k+\"px\";d=d.childNodes;var l,o,f;l=0;for(o=d.length;l<o;++l){f=d[l];var j=k-a.pxself(f,\"paddingTop\")-a.pxself(f,\"paddingBottom\");if(j<=0)j=0;f.style.height=j+\"px\";if(!(f.style.verticalAlign||f.childNodes.length==0)){var b=f.childNodes[0];if(j<=0)j=0;if(b.className==\"Wt-hcenter\"){b.style.height=j+\"px\";b=b.firstChild.firstChild;if(!a.hasTag(b,\"TD\"))b=b.firstChild;if(b.style.height!=j+\"px\")b.style.height=j+\"px\";b=b.firstChild}if(f.childNodes.length==1)j+=-this.marginV(b);if(j<=0)j= 0;if(!a.hasTag(b,\"TABLE\"))if(b.wtResize){f=b.parentNode.offsetWidth-n.marginH(b);b.wtResize(b,f,j)}else if(b.style.height!=j+\"px\"){b.style.height=j+\"px\";if(b.className==\"Wt-wrapdiv\")if(a.isIE&&a.hasTag(b.firstChild,\"TEXTAREA\"))b.firstChild.style.height=j-a.pxself(b,\"marginBottom\")+\"px\"}}}};this.adjust=function(){var d=a.getElement(t);if(!d)return false;n.initResize&&n.initResize(a,t,g);if(a.isHidden(d))return true;var k=d.firstChild,l=d.parentNode;if(k.style.height!=\"\")k.style.height=\"\";if(!(d.dirty|| k.w!=l.clientWidth||k.h!=l.clientHeight))return true;d.dirty=null;var o=a.pxself(l,\"height\");if(o==0){o=l.clientHeight;o+=-a.px(l,\"paddingTop\")-a.px(l,\"paddingBottom\")}o+=-a.px(d,\"marginTop\")-a.px(d,\"marginBottom\");var f,j;if(l.children){f=0;for(j=l.children.length;f<j;++f){var b=l.children[f];if(b!=d)o-=$(b).outerHeight()}}var r=d=0,m,u;m=f=0;for(j=k.rows.length;f<j;f++){b=k.rows[f];if(b.className==\"Wt-hrh\")o-=b.offsetHeight;else{r+=g.minheight[m];if(g.stretch[m]<=0)o-=b.offsetHeight;else d+=g.stretch[m]; ++m}}o=o>r?o:r;if(d!=0&&o>0){r=o;var p;m=f=0;for(j=k.rows.length;f<j;f++)if(k.rows[f].className!=\"Wt-hrh\"){b=k.rows[f];if(g.stretch[m]!=0){if(g.stretch[m]!=-1){p=o*g.stretch[m]/d;p=r>p?p:r;p=Math.round(g.minheight[m]>p?g.minheight[m]:p);r-=p}else p=b.offsetHeight;this.adjustRow(b,p)}++m}}k.w=l.clientWidth;k.h=l.clientHeight;if(k.style.tableLayout!=\"fixed\")return true;d=0;m=k.childNodes;l=0;for(o=m.length;l<o;l++){r=m[l];var x,z,y;if(a.hasTag(r,\"COLGROUP\")){l=-1;m=r.childNodes;o=m.length}if(a.hasTag(r, \"COL\")){if(a.pctself(r,\"width\")==0){f=p=0;for(j=k.rows.length;f<j;f++){b=k.rows[f];b=b.childNodes;z=x=0;for(y=b.length;z<y;z++){u=b[z];if(u.colSpan==1&&x==d&&u.childNodes.length==1){b=u.firstChild;b=b.offsetWidth+n.marginH(b);p=Math.max(p,b);break}x+=u.colSpan;if(x>d)break}}if(p>0&&a.pxself(r,\"width\")!=p)r.style.width=p+\"px\"}++d}}return true};this.adjust()};";
	}

	static String wtjs2(WApplication app) {
		return "Wt3_1_6.StdLayout.prototype.initResize = function(a,t,g){function n(c){var e,h,i,q=a.getElement(t).firstChild.childNodes;h=e=0;for(i=q.length;h<i;h++){var s=q[h];if(a.hasTag(s,\"COLGROUP\")){h=-1;q=s.childNodes;i=q.length}if(a.hasTag(s,\"COL\"))if(s.className!=\"Wt-vrh\")if(e==c)return s;else++e}return null}function d(c,e){if(c.offsetWidth>0)return c.offsetWidth;else{c=m.firstChild.rows[0];var h,i,q,s;q=i=0;for(s=c.childNodes.length;i<s;++i){h=c.childNodes[i];if(h.className!=\"Wt-vrh\"){if(q== e)return h.offsetWidth;q+=h.colSpan}}return 0}}function k(c,e){var h=a.getElement(t).firstChild;n(c).style.width=e+\"px\";var i,q,s,v;q=i=0;for(s=h.rows.length;i<s;i++){v=h.rows[i];if(v.className!=\"Wt-hrh\"){var w,B,D,F;D=B=0;for(F=v.childNodes.length;B<F;++B){w=v.childNodes[B];if(w.className!=\"Wt-vrh\"){if(w.colSpan==1&&D==c&&w.childNodes.length==1){v=w.firstChild;w=e-r.marginH(v);v.style.width=w+\"px\";break}D+=w.colSpan}}++q}}}function l(c,e,h){var i=c.firstChild;new a.SizeHandle(a,\"v\",i.offsetHeight, i.offsetWidth,-c.parentNode.previousSibling.offsetHeight,c.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(q){f(c,e,q)},i,m,h,0,0)}function o(c,e,h){var i=-c.previousSibling.offsetWidth,q=c.nextSibling.offsetWidth,s=c.firstChild,v=a.pxself(u.rows[0].childNodes[0],\"paddingTop\"),w=a.pxself(u.rows[u.rows.length-1].childNodes[0],\"paddingBottom\");new a.SizeHandle(a,\"h\",s.offsetWidth,u.offsetHeight-v-w,i,q,\"Wt-hsh\",function(B){b(c,e,B)},s,m,h,0,-c.offsetTop+v-a.pxself(c,\"paddingTop\"))}function f(c, e,h){var i=c.parentNode.previousSibling;c=c.parentNode.nextSibling;var q=i.offsetHeight,s=c.offsetHeight;if(g.stretch[e]>0&&g.stretch[e+1]>0)g.stretch[e]=-1;if(g.stretch[e+1]==0)g.stretch[e+1]=-1;g.stretch[e]<=0&&r.adjustRow(i,q+h);g.stretch[e+1]<=0&&r.adjustRow(c,s-h);a.getElement(t).dirty=true;window.onresize()}function j(){var c,e=0;for(c=0;;++c){var h=n(c);if(h)e+=a.pctself(h,\"width\");else break}if(e!=0)for(c=0;;++c)if(h=n(c)){var i=a.pctself(h,\"width\");if(i)h.style.width=i*100/e+\"%\"}else break} function b(c,e,h){c=n(e);var i=d(c,e),q=n(e+1),s=d(q,e+1);if(a.pctself(c,\"width\")>0&&a.pctself(q,\"width\")>0){c.style.width=\"\";j()}a.pctself(c,\"width\")==0&&k(e,i+h);a.pctself(q,\"width\")==0&&k(e+1,s-h);window.onresize()}var r=this,m=a.getElement(t);if(m)if(!r.resizeInitialized){var u=m.firstChild,p,x,z,y;x=p=0;for(z=u.rows.length;p<z;p++){y=u.rows[p];if(y.className==\"Wt-hrh\"){var A=y.firstChild;A.ri=x-1;A.onmousedown=function(c){l(this,this.ri,c||window.event)}}else{var C,E,G;E=C=0;for(G=y.childNodes.length;C< G;++C){A=y.childNodes[C];if(A.className==\"Wt-vrh\"){A.ci=E-1;A.onmousedown=function(c){o(this,this.ci,c||window.event)}}else E+=A.colSpan}++x}}r.resizeInitialized=true}};";
	}

	static String appjs1(WApplication app) {
		return app.getJavaScriptClass()
				+ ".layouts = new (function(){var a=[],t=false;this.add=function(g){var n,d;n=0;for(d=a.length;n<d;++n)if(a[n].getId()==g.getId()){a[n]=g;return}a.push(g)};this.adjust=function(g){if(g){if(g=$(\"#\"+g).get(0))g.dirty=true}else if(!t){t=true;for(var n=0;n<a.length;++n){g=a[n];if(!g.adjust()){g.WT.arrayRemove(a,n);--n}}t=false}}});";
	}

	static String wtjs10(WApplication app) {
		return "Wt3_1_6.ChildrenResize = function(b,f,d){var c,e,a;b.style.height=d+\"px\";c=0;for(e=b.childNodes.length;c<e;++c){a=b.childNodes[c];if(a.nodeType==1)if(a.wtResize)a.wtResize(a,f,d);else if(a.style.height!=b.style.height)a.style.height=b.style.height}};";
	}
}
