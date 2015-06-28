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
 * Abstract rule in a CSS style sheet.
 * <p>
 * 
 * A rule presents CSS style properties that are applied to a selected set of
 * elements.
 * <p>
 * Use {@link WCssTemplateRule} if you would like to use a widget as a template
 * for specifying (<i>and</i> updating) a style rule, using the widgets style
 * properties, or {@link WCssTextRule} if you wish to directly specify the CSS
 * declarations.
 * <p>
 * 
 * @see WCssStyleSheet
 */
public abstract class WCssRule extends WObject {
	private static Logger logger = LoggerFactory.getLogger(WCssRule.class);

	/**
	 * Destructor.
	 */
	public void remove() {
		if (this.sheet_ != null) {
			this.sheet_.removeRule(this);
		}
	}

	/**
	 * Sets the selector.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The selector can only be changed as long as the rule
	 * hasn&apos;t been rendered. </i>
	 * </p>
	 */
	public void setSelector(final String selector) {
		this.selector_ = selector;
	}

	/**
	 * Returns the selector.
	 */
	public String getSelector() {
		return this.selector_;
	}

	/**
	 * Returns the style sheet to which this rule belongs.
	 */
	public WCssStyleSheet getSheet() {
		return this.sheet_;
	}

	/**
	 * Indicates that the rule has changed and needs updating.
	 */
	public void modified() {
		if (this.sheet_ != null) {
			this.sheet_.ruleModified(this);
		}
	}

	/**
	 * Returns the declarations.
	 * <p>
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public abstract String getDeclarations();

	boolean updateDomElement(final DomElement cssRuleElement, boolean all) {
		return false;
	}

	/**
	 * Creates a new CSS rule with given selector.
	 */
	protected WCssRule(final String selector, WObject parent) {
		super(parent);
		this.selector_ = selector;
		this.sheet_ = null;
	}

	/**
	 * Creates a new CSS rule with given selector.
	 * <p>
	 * Calls {@link #WCssRule(String selector, WObject parent) this(selector,
	 * (WObject)null)}
	 */
	protected WCssRule(final String selector) {
		this(selector, (WObject) null);
	}

	private String selector_;
	WCssStyleSheet sheet_;
}
