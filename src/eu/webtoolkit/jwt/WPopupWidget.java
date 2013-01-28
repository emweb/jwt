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

class WPopupWidget extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WPopupWidget.class);

	public WPopupWidget(WWidget impl, WObject parent) {
		super();
		this.anchorWidget_ = null;
		this.orientation_ = Orientation.Vertical;
		this.transient_ = false;
		this.autoHideDelay_ = 0;
		this.hidden_ = new Signal(this);
		this.shown_ = new Signal(this);
		this.jsHidden_ = new JSignal(impl, "hidden");
		this.jsShown_ = new JSignal(impl, "shown");
		this.setImplementation(impl);
		if (parent != null) {
			parent.addChild(this);
		}
		this.hide();
		this.setPopup(true);
		this.setPositionScheme(PositionScheme.Absolute);
		WInteractWidget iw = ((impl) instanceof WInteractWidget ? (WInteractWidget) (impl)
				: null);
		if (iw != null) {
			iw.escapePressed().addListener(this, new Signal.Listener() {
				public void trigger() {
					WPopupWidget.this.hide();
				}
			});
			iw.clicked().preventPropagation();
		}
		this.jsHidden_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WPopupWidget.this.hide();
			}
		});
		this.jsShown_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WPopupWidget.this.show();
			}
		});
		WApplication.getInstance().getDomRoot().addWidget(this);
	}

	public WPopupWidget(WWidget impl) {
		this(impl, (WObject) null);
	}

	public void setAnchorWidget(WWidget anchorWidget, Orientation orientation) {
		this.anchorWidget_ = anchorWidget;
		this.orientation_ = orientation;
	}

	public final void setAnchorWidget(WWidget anchorWidget) {
		setAnchorWidget(anchorWidget, Orientation.Vertical);
	}

	public WWidget getAnchorWidget() {
		return this.anchorWidget_;
	}

	public Orientation getOrientation() {
		return this.orientation_;
	}

	public void setTransient(boolean isTransient, int autoHideDelay) {
		this.transient_ = isTransient;
		this.autoHideDelay_ = autoHideDelay;
	}

	public final void setTransient(boolean isTransient) {
		setTransient(isTransient, 0);
	}

	public boolean isTransient() {
		return this.transient_;
	}

	public int getAutoHideDelay() {
		return this.autoHideDelay_;
	}

	public void setHidden(boolean hidden, WAnimation animation) {
		if (WWebWidget.canOptimizeUpdates() && hidden == this.isHidden()) {
			return;
		}
		super.setHidden(hidden, animation);
		if (!hidden && this.anchorWidget_ != null) {
			this.positionAt(this.anchorWidget_, this.orientation_);
		}
		if (hidden) {
			this.hidden().trigger();
		} else {
			this.shown().trigger();
		}
		if (!WWebWidget.canOptimizeUpdates() || this.isRendered()) {
			if (hidden) {
				this.doJavaScript("var o = jQuery.data(" + this.getJsRef()
						+ ", 'popup');if (o) o.hidden();");
			} else {
				this.doJavaScript("var o = jQuery.data(" + this.getJsRef()
						+ ", 'popup');if (o) o.shown();");
			}
		}
	}

	public Signal hidden() {
		return this.hidden_;
	}

	public Signal shown() {
		return this.shown_;
	}

	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	protected void setParent(WObject p) {
		if (!(p != null) || p == WApplication.getInstance().getDomRoot()) {
			super.setParent(p);
		}
	}

	private WWidget anchorWidget_;
	private Orientation orientation_;
	private boolean transient_;
	private int autoHideDelay_;
	private Signal hidden_;
	private Signal shown_;
	private JSignal jsHidden_;
	private JSignal jsShown_;

	// private void create(WWidget parent) ;
	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WPopupWidget.js", wtjs1());
		StringBuilder jsObj = new StringBuilder();
		jsObj.append("new Wt3_3_0.WPopupWidget(").append(
				app.getJavaScriptClass()).append(',').append(this.getJsRef())
				.append(',').append(this.transient_).append(',').append(
						this.autoHideDelay_).append(");");
		this.setJavaScriptMember(" WPopupWidget", jsObj.toString());
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WPopupWidget",
				"function(c,a,h,i){function k(){clearTimeout(d);d=setTimeout(function(){e.hide()},i)}function l(){clearTimeout(d)}function j(){e.hide()}jQuery.data(a,\"popup\",this);var e=this,m=c.WT,d=null,f=null,g=null;this.bindShow=function(b){f=b};this.bindHide=function(b){g=b};this.shown=function(){h&&setTimeout(function(){$(document).bind(\"click\",j)},0);f&&f()};this.show=function(b,n){if(a.style.display!=\"\"){a.style.display=\"\";b&&m.positionAtWidget(a.id, b.id,n);c.emit(a,\"shown\")}};this.hidden=function(){g&&g();h&&$(document).unbind(\"click\",j)};this.hide=function(){if(a.style.display!=\"none\")a.style.display=\"none\";c.emit(a,\"hidden\");e.hidden()};i>0&&$(a).mouseleave(k).mouseenter(l)}");
	}
}
