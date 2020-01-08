/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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

class StyleSheetImpl implements StyleSheet {
	private static Logger logger = LoggerFactory
			.getLogger(StyleSheetImpl.class);

	public StyleSheetImpl() {
		super();
		this.rulesetArray_ = new ArrayList<RulesetImpl>();
	}

	public int getRulesetSize() {
		return this.rulesetArray_.size();
	}

	public Ruleset rulesetAt(int i) {
		return this.rulesetArray_.get(i);
	}

	public List<RulesetImpl> rulesetArray_;
}
