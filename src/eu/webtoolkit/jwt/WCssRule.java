/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


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
public abstract class WCssRule {
	/**
	 * Destructor.
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

	public boolean updateDomElement(DomElement cssRuleElement, boolean all) {
		return false;
	}

	/**
	 * Creates a new CSS rule with given selector.
	 */
	protected WCssRule(String selector) {
		this.selector_ = selector;
		this.sheet_ = null;
	}

	private String selector_;
	WCssStyleSheet sheet_;
}
