/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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
 * A CSS style sheet.
 * <p>
 * 
 * @see WApplication#getStyleSheet()
 */
public class WCssStyleSheet {
	private static Logger logger = LoggerFactory
			.getLogger(WCssStyleSheet.class);

	/**
	 * Creates a new (internal) style sheet.
	 */
	public WCssStyleSheet() {
		this.link_ = new WLink();
		this.media_ = "";
		this.rules_ = new ArrayList<WCssRule>();
		this.rulesAdded_ = new ArrayList<WCssRule>();
		this.rulesModified_ = new HashSet<WCssRule>();
		this.rulesRemoved_ = new ArrayList<String>();
		this.defined_ = new HashSet<String>();
	}

	/**
	 * Creates a new (external) style sheet reference.
	 */
	public WCssStyleSheet(final WLink link, final String media) {
		this.link_ = link;
		this.media_ = media;
		this.rules_ = new ArrayList<WCssRule>();
		this.rulesAdded_ = new ArrayList<WCssRule>();
		this.rulesModified_ = new HashSet<WCssRule>();
		this.rulesRemoved_ = new ArrayList<String>();
		this.defined_ = new HashSet<String>();
	}

	/**
	 * Creates a new (external) style sheet reference.
	 * <p>
	 * Calls {@link #WCssStyleSheet(WLink link, String media) this(link, "all")}
	 */
	public WCssStyleSheet(final WLink link) {
		this(link, "all");
	}

	public WLink getLink() {
		return this.link_;
	}

	public String getMedia() {
		return this.media_;
	}

	/**
	 * Adds a CSS rule.
	 * <p>
	 * 
	 * Add a rule using the CSS selector <code>selector</code>, with CSS
	 * declarations in <code>declarations</code>. These declarations must be a
	 * list separated by semi-colons (;).
	 * <p>
	 * Optionally, you may give a <code>ruleName</code>, which may later be used
	 * to check if the rule was already defined.
	 * <p>
	 * 
	 * @see WCssStyleSheet#isDefined(String ruleName)
	 */
	public WCssTextRule addRule(final String selector,
			final String declarations, final String ruleName) {
		WCssTextRule result = new WCssTextRule(selector, declarations);
		this.addRule(result, ruleName);
		return result;
	}

	/**
	 * Adds a CSS rule.
	 * <p>
	 * Returns
	 * {@link #addRule(String selector, String declarations, String ruleName)
	 * addRule(selector, declarations, "")}
	 */
	public final WCssTextRule addRule(final String selector,
			final String declarations) {
		return addRule(selector, declarations, "");
	}

	/**
	 * Adds a CSS rule.
	 * <p>
	 * 
	 * Add a rule using the CSS selector <code>selector</code>, with styles
	 * specified in <code>style</code>.
	 * <p>
	 * Optionally, you may give a <code>ruleName</code>, which may later be used
	 * to check if the rule was already defined.
	 * <p>
	 * 
	 * @see WCssStyleSheet#isDefined(String ruleName)
	 */
	public WCssTemplateRule addRule(final String selector,
			final WCssDecorationStyle style, final String ruleName) {
		WCssTemplateRule result = new WCssTemplateRule(selector);
		result.getTemplateWidget().setDecorationStyle(style);
		this.addRule(result, ruleName);
		return result;
	}

	/**
	 * Adds a CSS rule.
	 * <p>
	 * Returns
	 * {@link #addRule(String selector, WCssDecorationStyle style, String ruleName)
	 * addRule(selector, style, "")}
	 */
	public final WCssTemplateRule addRule(final String selector,
			final WCssDecorationStyle style) {
		return addRule(selector, style, "");
	}

	/**
	 * Adds a CSS rule.
	 * <p>
	 * 
	 * Optionally, you may give a <code>ruleName</code>, which may later be used
	 * to check if the rule was already defined. Note: you may not pass the same
	 * rule to 2 diffrent applications.
	 * <p>
	 * 
	 * @see WCssStyleSheet#isDefined(String ruleName)
	 */
	public WCssRule addRule(WCssRule rule, final String ruleName) {
		this.rules_.add(rule);
		this.rulesAdded_.add(rule);
		rule.sheet_ = this;
		if (ruleName.length() != 0) {
			this.defined_.add(ruleName);
		}
		return rule;
	}

	/**
	 * Adds a CSS rule.
	 * <p>
	 * Returns {@link #addRule(WCssRule rule, String ruleName) addRule(rule,
	 * "")}
	 */
	public final WCssRule addRule(WCssRule rule) {
		return addRule(rule, "");
	}

	/**
	 * Returns if a rule was already defined in this style sheet.
	 * <p>
	 * 
	 * Returns whether a rule was added with the given <code>ruleName</code>.
	 * <p>
	 * 
	 * @see WCssStyleSheet#addRule(String selector, String declarations, String
	 *      ruleName)
	 */
	public boolean isDefined(final String ruleName) {
		boolean i = this.defined_.contains(ruleName);
		return i != false;
	}

	/**
	 * Removes a rule.
	 */
	public void removeRule(WCssRule rule) {
		if (this.rules_.remove(rule)) {
			if (!this.rulesAdded_.remove(rule)) {
				this.rulesRemoved_.add(rule.getSelector());
			}
			this.rulesModified_.remove(rule);
		}
	}

	void ruleModified(WCssRule rule) {
		if (this.rulesAdded_.indexOf(rule) == -1) {
			this.rulesModified_.add(rule);
		}
	}

	public void cssText(final StringBuilder out, boolean all) {
		if (this.link_.isNull()) {
			final List<WCssRule> toProcess = all ? this.rules_
					: this.rulesAdded_;
			for (int i = 0; i < toProcess.size(); ++i) {
				WCssRule rule = toProcess.get(i);
				out.append(rule.getSelector()).append(" { ")
						.append(rule.getDeclarations()).append(" }\n");
			}
			this.rulesAdded_.clear();
			if (all) {
				this.rulesModified_.clear();
			}
		} else {
			WApplication app = WApplication.getInstance();
			out.append("@import url(\"").append(this.link_.resolveUrl(app))
					.append("\")");
			if (this.media_.length() != 0 && !this.media_.equals("all")) {
				out.append(" ").append(this.media_);
			}
			out.append(";\n");
		}
	}

	public void javaScriptUpdate(WApplication app, final StringBuilder js,
			boolean all) {
		if (!all) {
			for (int i = 0; i < this.rulesRemoved_.size(); ++i) {
				js.append("Wt3_5_0.removeCssRule(");
				DomElement.jsStringLiteral(js, this.rulesRemoved_.get(i), '\'');
				js.append(");");
			}
			this.rulesRemoved_.clear();
			for (Iterator<WCssRule> i_it = this.rulesModified_.iterator(); i_it
					.hasNext();) {
				WCssRule i = i_it.next();
				js.append("{ var d= Wt3_5_0.getCssRule(");
				DomElement.jsStringLiteral(js, i.getSelector(), '\'');
				js.append(");if(d){");
				DomElement d = DomElement.updateGiven("d",
						DomElementType.DomElement_SPAN);
				if (i.updateDomElement(d, false)) {
					EscapeOStream sout = new EscapeOStream(js);
					d.asJavaScript(sout, DomElement.Priority.Update);
				}
				;
				js.append("}}");
			}
			this.rulesModified_.clear();
		}
		if (!app.getEnvironment().agentIsIElt(9)
				&& app.getEnvironment().getAgent() != WEnvironment.UserAgent.Konqueror) {
			final List<WCssRule> toProcess = all ? this.rules_
					: this.rulesAdded_;
			for (int i = 0; i < toProcess.size(); ++i) {
				WCssRule rule = toProcess.get(i);
				js.append("Wt3_5_0.addCss('").append(rule.getSelector())
						.append("',");
				DomElement.jsStringLiteral(js, rule.getDeclarations(), '\'');
				js.append(");\n");
			}
			this.rulesAdded_.clear();
			if (all) {
				this.rulesModified_.clear();
			}
		} else {
			StringBuilder css = new StringBuilder();
			this.cssText(css, all);
			if (!(css.length() == 0)) {
				js.append("Wt3_5_0.addCssText(");
				DomElement.jsStringLiteral(js, css.toString(), '\'');
				js.append(");\n");
			}
		}
	}

	void clear() {
		while (!this.rules_.isEmpty()) {
			if (this.rules_.get(this.rules_.size() - 1) != null)
				this.rules_.get(this.rules_.size() - 1).remove();
		}
	}

	private WLink link_;
	private String media_;
	private List<WCssRule> rules_;
	private List<WCssRule> rulesAdded_;
	private Set<WCssRule> rulesModified_;
	private List<String> rulesRemoved_;
	private Set<String> defined_;

	boolean isDirty() {
		return !this.rulesAdded_.isEmpty() || !this.rulesModified_.isEmpty()
				|| !this.rulesRemoved_.isEmpty();
	}
}
