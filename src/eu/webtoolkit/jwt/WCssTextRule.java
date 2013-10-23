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

/**
 * A CSS rule specified directly using CSS declarations.
 * <p>
 * 
 * @see WCssStyleSheet
 */
public class WCssTextRule extends WCssRule {
	private static Logger logger = LoggerFactory.getLogger(WCssTextRule.class);

	/**
	 * Creates a CSS rule with a given selector and declarations.
	 */
	public WCssTextRule(final String selector, final String declarations,
			WObject parent) {
		super(selector, parent);
		this.declarations_ = declarations;
	}

	/**
	 * Creates a CSS rule with a given selector and declarations.
	 * <p>
	 * Calls
	 * {@link #WCssTextRule(String selector, String declarations, WObject parent)
	 * this(selector, declarations, (WObject)null)}
	 */
	public WCssTextRule(final String selector, final String declarations) {
		this(selector, declarations, (WObject) null);
	}

	public String getDeclarations() {
		return this.declarations_;
	}

	private String declarations_;
}
