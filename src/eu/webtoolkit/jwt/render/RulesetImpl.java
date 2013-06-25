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

class RulesetImpl implements Ruleset {
	private static Logger logger = LoggerFactory.getLogger(RulesetImpl.class);

	public RulesetImpl() {
		super();
		this.selector_ = new SelectorImpl();
		this.block_ = new DeclarationBlockImpl();
	}

	public Selector getSelector() {
		return this.selector_;
	}

	public DeclarationBlock getDeclarationBlock() {
		return this.block_;
	}

	public void setBlock(DeclarationBlockImpl b) {
		this.block_ = b;
	}

	public SelectorImpl selector_;
	public DeclarationBlockImpl block_;
}
