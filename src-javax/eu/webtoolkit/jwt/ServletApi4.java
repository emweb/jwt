/*
 * Copyright (C) 2023 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

import javax.servlet.*;

import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServletApi4 extends ServletApi {
	private static Logger logger = LoggerFactory.getLogger(ServletApi4.class);

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	public void configureRequestEncoding(ServletContext context, boolean contextIsInitializing) {
		if (contextIsInitializing) {
			context.setRequestCharacterEncoding("UTF-8");
			logger.info("Set default request character encoding to UTF-8");
		} else {
			if (Objects.equals(context.getRequestCharacterEncoding(), "UTF-8")) {
				logger.info("Request character encoding is UTF-8: excellent!");
			} else {
				logger.error("Request character encoding is not set to UTF-8 (is {}), this will not work well:\n" +
					" - either configure request character encoding directly in your web.xml:\n" +
					"\n" +
					"  <request-character-encoding>UTF-8</request-character-encoding>\n" +
					"\n" +
					" - OR, configure eu.webtoolkit.jwt.ServletInit as a listener in your web.xml:\n" +
					"\n" +
					"  <listener>\n" +
					"    <listener-class>eu.webtoolkit.jwt.ServletInit</listener-class>\n" +
					"  </listener>\n", context.getRequestCharacterEncoding());
			}
		}
	}
}
