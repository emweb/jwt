/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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

class EmwebLoadingIndicator extends WContainerWidget implements
		WLoadingIndicator {
	public EmwebLoadingIndicator() {
		super();
		this.setInline(false);
		WApplication app = WApplication.getInstance();
		this.cover_ = new WContainerWidget(this);
		this.center_ = new WContainerWidget(this);
		WImage img = new WImage(new WLink("icons/emweb.jpg"), this.center_);
		img.setMargin(new WLength(7), EnumSet.of(Side.Top, Side.Bottom));
		this.text_ = new WText("Loading...", this.center_);
		this.text_.setInline(false);
		this.text_.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		if (app.getEnvironment().agentIsIE()) {
			app.getStyleSheet().addRule("body", "height: 100%; margin: 0;");
		}
		app
				.getStyleSheet()
				.addRule(
						"div#" + this.cover_.getId(),
						""
								+ "background: #DDDDDD;height: 100%; width: 100%;top: 0px; left: 0px;opacity: 0.5; position: absolute;-khtml-opacity: 0.5;z-index: 10000;"
								+ (app.getEnvironment().agentIsIE() ? "filter: alpha(opacity=50);"
										: "-moz-opacity:0.5;-moz-background-clip: -moz-initial;-moz-background-origin: -moz-initial;-moz-background-inline-policy: -moz-initial;"));
		app
				.getStyleSheet()
				.addRule(
						"div#" + this.center_.getId(),
						"background: white;border: 3px solid #333333;z-index: 10001; visibility: visible;position: absolute; left: 50%; top: 50%;margin-left: -120px; margin-top: -60px;width: 240px; height: 120px;font-family: arial,sans-serif;text-align: center");
	}

	public WWidget getWidget() {
		return this;
	}

	public void setMessage(CharSequence text) {
		this.text_.setText(text);
	}

	private WContainerWidget cover_;
	private WContainerWidget center_;
	private WText text_;
}
