package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * Abstract rule in a CSS style sheet.
 * 
 * 
 * A rule presents CSS style properties that are applied to a selected set of
 * elements.
 * <p>
 * Use {@link WCssRule#getDeclarations()} if you would like to use a widget as a
 * template for specifying (<i>and</i> updating) a style rule, using the widgets
 * style properties, or {@link WCssRule#getDeclarations()} if you wish to
 * directly specify the CSS declarations.
 * <p>
 * 
 * @see WCssStyleSheet
 */
public abstract class WCssRule {
	/**
	 * Delete a CSS rule.
	 */
	public void remove() {
		if (this.sheet_ != null) {
			this.sheet_.removeRule(this);
		}
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
	 * Indicate that the rule has changed and needs updating.
	 */
	public void modified() {
		if (this.sheet_ != null) {
			this.sheet_.ruleModified(this);
		}
	}

	/**
	 * Returns the declarations.
	 * 
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public abstract String getDeclarations();

	/**
	 * Returns the declarations.
	 * 
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public boolean updateDomElement(DomElement cssRuleElement, boolean all) {
		return false;
	}

	/**
	 * Returns the declarations.
	 * 
	 * This is a semi-colon separated list of CSS declarations.
	 */
	protected WCssRule(String selector) {
		this.selector_ = selector;
		this.sheet_ = null;
	}

	/**
	 * Returns the declarations.
	 * 
	 * This is a semi-colon separated list of CSS declarations.
	 */
	private String selector_;
	/**
	 * Returns the declarations.
	 * 
	 * This is a semi-colon separated list of CSS declarations.
	 */
	WCssStyleSheet sheet_;
}
