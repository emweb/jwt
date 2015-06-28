/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

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

class CombinedStyleSheet implements StyleSheet {
	private static Logger logger = LoggerFactory
			.getLogger(CombinedStyleSheet.class);

	public CombinedStyleSheet() {
		super();
		this.sheets_ = new ArrayList<StyleSheet>();
		this.sheets_not_owned_ = new HashSet<StyleSheet>();
	}

	public void use(StyleSheet sh, boolean noFree) {
		this.sheets_.add(sh);
		if (noFree) {
			this.sheets_not_owned_.add(sh);
		}
	}

	public final void use(StyleSheet sh) {
		use(sh, false);
	}

	public int getRulesetSize() {
		int result = 0;
		for (int i = 0; i < this.sheets_.size(); ++i) {
			result += this.sheets_.get(i).getRulesetSize();
		}
		return result;
	}

	public Ruleset rulesetAt(int j) {
		for (int i = 0; i < this.sheets_.size(); ++i) {
			if ((int) j < this.sheets_.get(i).getRulesetSize()) {
				return this.sheets_.get(i).rulesetAt(j);
			}
			j -= this.sheets_.get(i).getRulesetSize();
		}
		return this.sheets_.get(0).rulesetAt(0);
	}

	private List<StyleSheet> sheets_;
	private Set<StyleSheet> sheets_not_owned_;
}
