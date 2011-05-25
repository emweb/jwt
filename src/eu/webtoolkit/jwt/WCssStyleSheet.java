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
 * A CSS style sheet.
 * <p>
 * 
 * @see WApplication#getStyleSheet()
 */
public class WCssStyleSheet {
	/**
	 * Creates a new empty style sheet.
	 */
	public WCssStyleSheet() {
		this.rules_ = new ArrayList<WCssRule>();
		this.rulesAdded_ = new ArrayList<WCssRule>();
		this.rulesModified_ = new HashSet<WCssRule>();
		this.rulesRemoved_ = new ArrayList<String>();
		this.defined_ = new HashSet<String>();
	}

	/**
	 * Adds a CSS rule.
	 * <p>
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
	public WCssTextRule addRule(String selector, String declarations,
			String ruleName) {
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
	public final WCssTextRule addRule(String selector, String declarations) {
		return addRule(selector, declarations, "");
	}

	/**
	 * Adds a CSS rule.
	 * <p>
	 * Add a rule using the CSS selector <code>selector</code>, with styles
	 * specified in <code>style</code>.
	 * <p>
	 * Optionally, you may give a <code>ruleName</code>, which may later be used
	 * to check if the rule was already defined.
	 * <p>
	 * 
	 * @see WCssStyleSheet#isDefined(String ruleName)
	 */
	public WCssTemplateRule addRule(String selector, WCssDecorationStyle style,
			String ruleName) {
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
	public final WCssTemplateRule addRule(String selector,
			WCssDecorationStyle style) {
		return addRule(selector, style, "");
	}

	/**
	 * Adds a CSS rule.
	 * <p>
	 * Optionally, you may give a <code>ruleName</code>, which may later be used
	 * to check if the rule was already defined.
	 * <p>
	 * 
	 * @see WCssStyleSheet#isDefined(String ruleName)
	 */
	public WCssRule addRule(WCssRule rule, String ruleName) {
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
	 * Returns whether a rule was added with the given <code>ruleName</code>.
	 * <p>
	 * 
	 * @see WCssStyleSheet#addRule(String selector, String declarations, String
	 *      ruleName)
	 */
	public boolean isDefined(String ruleName) {
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
		this.rulesModified_.add(rule);
	}

	String getCssText(boolean all) {
		String result = "";
		List<WCssRule> toProcess = all ? this.rules_ : this.rulesAdded_;
		for (int i = 0; i < toProcess.size(); ++i) {
			WCssRule rule = toProcess.get(i);
			result += rule.getSelector() + " { " + rule.getDeclarations()
					+ " }\n";
		}
		this.rulesAdded_.clear();
		if (all) {
			this.rulesModified_.clear();
		}
		return result;
	}

	public void javaScriptUpdate(WApplication app, Writer js, boolean all)
			throws IOException {
		if (!all) {
			for (int i = 0; i < this.rulesRemoved_.size(); ++i) {
				js.append("Wt3_1_10.removeCssRule(");
				DomElement.jsStringLiteral(js, this.rulesRemoved_.get(i), '\'');
				js.append(");");
			}
			this.rulesRemoved_.clear();
			for (Iterator<WCssRule> i_it = this.rulesModified_.iterator(); i_it
					.hasNext();) {
				WCssRule i = i_it.next();
				js.append("{ var d= Wt3_1_10.getCssRule(");
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
		if (!app.getEnvironment().agentIsIE()
				&& app.getEnvironment().getAgent() != WEnvironment.UserAgent.Konqueror) {
			List<WCssRule> toProcess = all ? this.rules_ : this.rulesAdded_;
			for (int i = 0; i < toProcess.size(); ++i) {
				WCssRule rule = toProcess.get(i);
				js.append("Wt3_1_10.addCss('").append(rule.getSelector())
						.append("',");
				DomElement.jsStringLiteral(js, rule.getDeclarations(), '\'');
				js.append(");").append('\n');
			}
			this.rulesAdded_.clear();
			if (all) {
				this.rulesModified_.clear();
			}
		} else {
			String text = this.getCssText(all);
			if (text.length() != 0) {
				js.append("Wt3_1_10.addCssText(");
				DomElement.jsStringLiteral(js, text, '\'');
				js.append(");").append('\n');
			}
		}
	}

	void clear() {
		while (!this.rules_.isEmpty()) {
			if (this.rules_.get(this.rules_.size() - 1) != null)
				this.rules_.get(this.rules_.size() - 1).remove();
		}
	}

	private List<WCssRule> rules_;
	private List<WCssRule> rulesAdded_;
	private Set<WCssRule> rulesModified_;
	private List<String> rulesRemoved_;
	private Set<String> defined_;
}
