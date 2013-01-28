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

class TopicWidget extends WContainerWidget {
	private static Logger logger = LoggerFactory.getLogger(TopicWidget.class);

	public TopicWidget() {
		super();
	}

	public void populateSubMenu(WMenu menu) {
	}

	// public void topic(String classname, WContainerWidget parent) ;
	// public void topic(String classname1, String classname2, WContainerWidget
	// parent) ;
	// public void topic(String classname1, String classname2, String
	// classname3, WContainerWidget parent) ;
	// public void topic(String classname1, String classname2, String
	// classname3, String classname4, WContainerWidget parent) ;
	public static WString reindent(CharSequence text) {
		List<String> lines = new ArrayList<String>();
		String s = text.toString();
		lines = new ArrayList<String>(Arrays.asList(s.split("\n")));
		String result = "";
		int indent = -1;
		int newlines = 0;
		for (int i = 0; i < lines.size(); ++i) {
			String line = lines.get(i);
			if (line.length() == 0) {
				++newlines;
			} else {
				if (indent == -1) {
					indent = countSpaces(line);
				} else {
					for (int j = 0; j < newlines; ++j) {
						result += '\n';
					}
				}
				newlines = 0;
				if (result.length() != 0) {
					result += '\n';
				}
				result += skipSpaces(line, indent);
			}
		}
		return new WString(result);
	}

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

	private String docAnchor(String classname) {
		StringWriter ss = new StringWriter();
		String cn = StringUtils.replaceAll(classname, "Chart::", "chart/");
		ss.append("<a href=\"http://www.webtoolkit.eu/").append(
				"jwt/latest/doc/javadoc/eu/webtoolkit/jwt/").append(classname)
				.append(".html\" target=\"_blank\">doc</a>");
		return ss.toString();
	}

	// private String title(String classname) ;
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

	static int countSpaces(String line) {
		for (int pos = 0; pos < line.length(); ++pos) {
			if (line.charAt(pos) != ' ') {
				return pos;
			}
		}
		return line.length();
	}

	static String skipSpaces(String line, int count) {
		if (line.length() >= count) {
			return line.substring(count);
		} else {
			return "";
		}
	}
}
