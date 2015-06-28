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

class TopicTemplate extends WTemplate {
	private static Logger logger = LoggerFactory.getLogger(TopicTemplate.class);

	public TopicTemplate(String trKey) {
		super(tr(trKey));
		this.setInternalPathEncoding(true);
		this.addFunction("tr", Functions.tr);
		this.setCondition("if:cpp", false);
		this.setCondition("if:java", true);
		this
				.bindString("doc-url",
						"http://www.webtoolkit.eu/jwt/latest/doc/javadoc/eu/webtoolkit/jwt/");
	}

	public void resolveString(final String varName, final List<WString> args,
			final Writer result) throws IOException {
		if (varName.equals("doc-link")) {
			String className = args.get(0).toString();
			className = StringUtils.replaceAll(className, "Render-", "render.");
			result.append("<a href=\"").append(this.docUrl(className)).append(
					"\" target=\"_blank\">");
			className = StringUtils.replaceAll(className, "render.", "");
			result.append(className).append("</a>");
		} else {
			if (varName.equals("src")) {
				String exampleName = args.get(0).toString();
				result.append("<fieldset class=\"src\">").append(
						"<legend>source</legend>").append(
						tr("src-" + exampleName).toString()).append(
						"</fieldset>");
			} else {
				super.resolveString(varName, args, result);
			}
		}
	}

	private String docUrl(final String className) {
		StringBuilder ss = new StringBuilder();
		String cn = className;
		cn = StringUtils.replaceAll(cn, ".", "/");
		ss.append(this.getString("doc-url")).append(cn).append(".html");
		return ss.toString();
	}

	private String getString(final String varName) {
		try {
			StringWriter ss = new StringWriter();
			List<WString> args = new ArrayList<WString>();
			this.resolveString(varName, args, ss);
			return ss.toString();
		} catch (IOException ie) {
			ie.printStackTrace();
			return null;
		}
	}

	private static String escape(final String name) {
		StringBuilder ss = new StringBuilder();
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
