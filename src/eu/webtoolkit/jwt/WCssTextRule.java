/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * Returns the declarations.
 * <p>
 * This is a semi-colon separated list of CSS declarations.
 */
public class WCssTextRule extends WCssRule {
	/**
	 * Returns the declarations.
	 * <p>
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public WCssTextRule(String selector, String declarations) {
		super(selector);
		this.declarations_ = declarations;
	}

	/**
	 * Returns the declarations.
	 * <p>
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public String getDeclarations() {
		return this.declarations_;
	}

	private String declarations_;
}
