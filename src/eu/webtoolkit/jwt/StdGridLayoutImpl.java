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
		String THIS_JS = "js/StdGridLayoutImpl.js";
		WApplication app = WApplication.getInstance();
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.getStyleSheet().addRule("table.Wt-hcenter",
					"margin: 0px auto;position: relative");
			app.doJavaScript(wtjs1(app), false);
			app.doJavaScript(appjs1(app), false);
			app.doJavaScript(appjs2(app), false);
			app.setJavaScriptLoaded(THIS_JS);
			app.addAutoJavaScript(app.getJavaScriptClass()
					+ ".layoutsAdjust();");
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
		if (fitHeight && !app.getEnvironment().agentIsIE()) {
			divStyle += "height: 100%;";
		}
		if (app.getEnvironment().agentIsIE()) {
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
					".layouts.push(new Wt3_1_4.StdLayout( Wt3_1_4, '").append(
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
					if (app.getEnvironment().agentIsIE()) {
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
		return "Wt3_1_4.ChildrenResize";
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
			if (app.getEnvironment().agentIsIE()) {
				container.setOverflow(WContainerWidget.Overflow.OverflowHidden);
			}
		}
	}

	private Grid grid_;
	private boolean useFixedLayout_;

	static String wtjs1(WApplication app) {
		return "Wt3_1_4.StdLayout = function(a,z,q){var x=this;this.marginH=function(b){var l=b.parentNode;return a.px(b,\"marginLeft\")+a.px(b,\"marginRight\")+a.px(b,\"borderLeftWidth\")+a.px(b,\"borderRightWidth\")+a.px(l,\"paddingLeft\")+a.px(l,\"paddingRight\")};this.marginV=function(b){return a.px(b,\"marginTop\")+a.px(b,\"marginBottom\")+a.px(b,\"borderTopWidth\")+a.px(b,\"borderBottomWidth\")+a.px(b,\"paddingTop\")+a.px(b,\"paddingBottom\")};this.adjustRow=function(b,l){if(b.style.height!=l+\"px\")b.style.height=l+ \"px\";b=b.childNodes;var g,j,m;g=0;for(j=b.length;g<j;++g){m=b[g];var e=l-a.pxself(m,\"paddingTop\")-a.pxself(m,\"paddingBottom\");if(e<=0)e=0;m.style.height=e+\"px\";if(!(m.style.verticalAlign||m.childNodes.length==0)){var c=m.childNodes[0];if(e<=0)e=0;if(c.className==\"Wt-hcenter\"){c.style.height=e+\"px\";c=c.firstChild.firstChild;if(!a.hasTag(c,\"TD\"))c=c.firstChild;if(c.style.height!=e+\"px\")c.style.height=e+\"px\";c=c.firstChild}if(m.childNodes.length==1)e+=-this.marginV(c);if(e<=0)e=0;if(!a.hasTag(c,\"TABLE\"))if(c.wtResize){m= c.parentNode.offsetWidth-x.marginH(c);c.wtResize(c,m,e)}else if(c.style.height!=e+\"px\"){c.style.height=e+\"px\";if(c.className==\"Wt-wrapdiv\")if(a.isIE&&a.hasTag(c.firstChild,\"TEXTAREA\"))c.firstChild.style.height=e-a.pxself(c,\"marginBottom\")+\"px\"}}}};this.adjust=function(){var b=a.getElement(z);if(!b)return false;x.initResize&&x.initResize(a,z,q);if(a.isHidden(b))return true;var l=b.firstChild;if(l.style.height!=\"\")l.style.height=\"\";var g=a.pxself(b.parentNode,\"height\");if(g==0){g=b.parentNode.clientHeight; g+=-a.px(b.parentNode,\"paddingTop\")-a.px(b.parentNode,\"paddingBottom\")}g+=-a.px(b,\"marginTop\")-a.px(b,\"marginBottom\");var j,m;if(b.parentNode.children){j=0;for(m=b.parentNode.children.length;j<m;++j){var e=b.parentNode.children[j];if(e!=b)g-=$(e).outerHeight()}}var c=b=0,n,r;n=j=0;for(m=l.rows.length;j<m;j++){e=l.rows[j];if(e.className==\"Wt-hrh\")g-=e.offsetHeight;else{c+=q.minheight[n];if(q.stretch[n]<=0)g-=e.offsetHeight;else b+=q.stretch[n];++n}}g=g>c?g:c;if(b!=0&&g>0){c=g;var k;n=j=0;for(m=l.rows.length;j< m;j++)if(l.rows[j].className!=\"Wt-hrh\"){e=l.rows[j];if(q.stretch[n]!=0){if(q.stretch[n]!=-1){k=g*q.stretch[n]/b;k=c>k?k:c;k=Math.round(q.minheight[n]>k?q.minheight[n]:k);c-=k}else k=e.offsetHeight;this.adjustRow(e,k)}++n}}if(l.style.tableLayout!=\"fixed\")return true;n=0;c=l.childNodes;g=0;for(b=c.length;g<b;g++){k=c[g];var s,y,t,u;if(a.hasTag(k,\"COLGROUP\")){g=-1;c=k.childNodes;b=c.length}if(a.hasTag(k,\"COL\")){if(a.pctself(k,\"width\")==0){j=s=0;for(m=l.rows.length;j<m;j++){e=l.rows[j];e=e.childNodes; t=y=0;for(u=e.length;t<u;t++){r=e[t];if(r.colSpan==1&&y==n&&r.childNodes.length==1){e=r.firstChild;e=e.offsetWidth+x.marginH(e);s=Math.max(s,e);break}y+=r.colSpan;if(y>n)break}}if(s>0&&a.pxself(k,\"width\")!=s)k.style.width=s+\"px\"}++n}}return true}};";
	}

	static String wtjs2(WApplication app) {
		return "Wt3_1_4.StdLayout.prototype.initResize = function(a,z,q){function x(d){var f,i,h,o=a.getElement(z).firstChild.childNodes;i=f=0;for(h=o.length;i<h;i++){var p=o[i];if(a.hasTag(p,\"COLGROUP\")){i=-1;o=p.childNodes;h=o.length}if(a.hasTag(p,\"COL\"))if(p.className!=\"Wt-vrh\")if(f==d)return p;else++f}return null}function b(d,f){if(d.offsetWidth>0)return d.offsetWidth;else{d=d.parentNode.rows[0];var i,h,o,p;o=h=0;for(p=d.childNodes.length;h<p;++h){i=d.childNodes[h];if(i.className!=\"Wt-vrh\"){if(o== f)return i.offsetWidth;o+=i.colSpan}}return 0}}function l(d,f){var i=a.getElement(z).firstChild;x(d).style.width=f+\"px\";var h,o,p,v;o=h=0;for(p=i.rows.length;h<p;h++){v=i.rows[h];if(v.className!=\"Wt-hrh\"){var w,A,C,E;C=A=0;for(E=v.childNodes.length;A<E;++A){w=v.childNodes[A];if(w.className!=\"Wt-vrh\"){if(w.colSpan==1&&C==d&&w.childNodes.length==1){v=w.firstChild;w=f-c.marginH(v);v.style.width=w+\"px\";break}C+=w.colSpan}}++o}}}function g(d,f,i){var h=d.firstChild;new a.SizeHandle(a,\"v\",h.offsetHeight, h.offsetWidth,-d.parentNode.previousSibling.offsetHeight,d.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(o){m(d,f,o)},h,n,i,0,0)}function j(d,f,i){var h=-d.previousSibling.offsetWidth,o=d.nextSibling.offsetWidth,p=d.firstChild,v=a.pxself(r.rows[0].childNodes[0],\"paddingTop\"),w=a.pxself(r.rows[r.rows.length-1].childNodes[0],\"paddingBottom\");new a.SizeHandle(a,\"h\",p.offsetWidth,r.offsetHeight-v-w,h,o,\"Wt-hsh\",function(A){e(d,f,A)},p,n,i,0,-d.offsetTop+v-a.pxself(d,\"paddingTop\"))}function m(d, f,i){var h=d.parentNode.previousSibling;d=d.parentNode.nextSibling;var o=h.offsetHeight,p=d.offsetHeight;if(q.stretch[f]>0&&q.stretch[f+1]>0)q.stretch[f]=-1;if(q.stretch[f+1]==0)q.stretch[f+1]=-1;q.stretch[f]<=0&&c.adjustRow(h,o+i);q.stretch[f+1]<=0&&c.adjustRow(d,p-i);window.onresize()}function e(d,f,i){d=x(f);var h=b(d,f),o=x(f+1),p=b(o,f+1);if(a.pctself(d,\"width\")>0&&a.pctself(o,\"width\")>0)d.style.width=\"\";a.pctself(d,\"width\")==0&&l(f,h+i);a.pctself(o,\"width\")==0&&l(f+1,p-i);window.onresize()} var c=this,n=a.getElement(z);if(n)if(!c.resizeInitialized){var r=n.firstChild,k,s,y,t;s=k=0;for(y=r.rows.length;k<y;k++){t=r.rows[k];if(t.className==\"Wt-hrh\"){var u=t.firstChild;u.ri=s-1;u.onmousedown=function(d){g(this,this.ri,d||window.event)}}else{var B,D,F;D=B=0;for(F=t.childNodes.length;B<F;++B){u=t.childNodes[B];if(u.className==\"Wt-vrh\"){u.ci=D-1;u.onmousedown=function(d){j(this,this.ci,d||window.event)}}else D+=u.colSpan}++s}}c.resizeInitialized=true}};";
	}

	static String appjs1(WApplication app) {
		return app.getJavaScriptClass() + ".layouts = [];";
	}

	static String appjs2(WApplication app) {
		return app.getJavaScriptClass()
				+ ".layoutsAdjust = function(){if(!this.adjusting){this.adjusting=true;var a;for(a=0;a<this.layouts.length;++a)if(!this.layouts[a].adjust()){this.WT.arrayRemove(this.layouts,a);--a}this.adjusting=false}};";
	}

	static String wtjs10(WApplication app) {
		return "Wt3_1_4.ChildrenResize = function(b,f,d){var c,e,a;b.style.height=d+\"px\";c=0;for(e=b.childNodes.length;c<e;++c){a=b.childNodes[c];if(a.nodeType==1)if(a.wtResize)a.wtResize(a,f,d);else if(a.style.height!=b.style.height)a.style.height=b.style.height}};";
	}
}
