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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ControlsWidget extends WContainerWidget {
	private static Logger logger = LoggerFactory
			.getLogger(ControlsWidget.class);

	public ControlsWidget(EventDisplayer ed, boolean hasSubMenu) {
		super();
		this.ed_ = ed;
		this.hasSubMenu_ = hasSubMenu;
	}

	public boolean hasSubMenu() {
		return this.hasSubMenu_;
	}

	public void populateSubMenu(WMenu menu) {
	}

	public void topic(String classname, WContainerWidget parent) {
		new WText(this.title(classname) + "<br/>", parent);
	}

	public void topic(String classname1, String classname2,
			WContainerWidget parent) {
		new WText(this.title(classname1) + " and " + this.title(classname2)
				+ "<br/>", parent);
	}

	public void topic(String classname1, String classname2, String classname3,
			WContainerWidget parent) {
		new WText(this.title(classname1) + ", " + this.title(classname2)
				+ " and " + this.title(classname3) + "<br/>", parent);
	}

	public void topic(String classname1, String classname2, String classname3,
			String classname4, WContainerWidget parent) {
		new WText(this.title(classname1) + ", " + this.title(classname2) + ", "
				+ this.title(classname3) + " and " + this.title(classname4)
				+ "<br/>", parent);
	}

	public EventDisplayer eventDisplayer() {
		return this.ed_;
	}

	protected EventDisplayer ed_;

	protected static WText addText(CharSequence s, WContainerWidget parent) {
		WText text = new WText(s, parent);
		boolean literal;
		literal = WString.toWString(s).isLiteral();
		if (!literal) {
			text.setInternalPathEncoding(true);
		}
		return text;
	}

	protected static final WText addText(CharSequence s) {
		return addText(s, (WContainerWidget) null);
	}

	private boolean hasSubMenu_;

	private String docAnchor(String classname) {
		StringWriter ss = new StringWriter();
		String cn = StringUtils.replaceAll(classname, "Chart::", "chart/");
		ss.append("<a href=\"http://www.webtoolkit.eu/").append(
				"jwt/latest/doc/javadoc/eu/webtoolkit/jwt/").append(classname)
				.append(".html\" target=\"_blank\">doc</a>");
		return ss.toString();
	}

	private String title(String classname) {
		String cn = "";
		cn = StringUtils.replaceAll(classname, "Chart::", "");
		return "<span class=\"title\">" + cn + "</span> "
				+ "<span class=\"doc\">[" + this.docAnchor(classname)
				+ "]</span>";
	}

	private String escape(String name) {
		StringWriter ss = new StringWriter();
		for (int i = 0; i < name.length(); ++i) {
			if (name.charAt(i) != ':') {
				ss.append(name.charAt(i));
			} else {
				ss.append("_1");
			}
		}
		return ss.toString();
	}
}
