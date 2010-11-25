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

/**
 * A CSS rule specified directly using CSS declarations.
 * <p>
 * 
 * @see WCssStyleSheet
 */
public class WCssTextRule extends WCssRule {
	/**
	 * Creates a CSS rule with a given selector and declarations.
	 */
	public WCssTextRule(String selector, String declarations) {
		super(selector);
		this.declarations_ = declarations;
	}

	public String getDeclarations() {
		return this.declarations_;
	}

	private String declarations_;
}
