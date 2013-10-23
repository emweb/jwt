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

class SimpleSelectorImpl implements SimpleSelector {
	private static Logger logger = LoggerFactory
			.getLogger(SimpleSelectorImpl.class);

	public SimpleSelectorImpl() {
		super();
		this.elementName_ = "";
		this.classes_ = new ArrayList<String>();
		this.hashid_ = "";
	}

	public String getElementName() {
		return this.elementName_;
	}

	public DomElementType getElementType() {
		return this.elementType_;
	}

	public String getHashId() {
		return this.hashid_;
	}

	public List<String> getClasses() {
		return this.classes_;
	}

	public void setElementName(final String name) {
		this.elementName_ = name;
		this.elementType_ = DomElement.parseTagName(this.elementName_);
	}

	public void addClass(final String id) {
		this.classes_.add(id);
	}

	public void setHash(final String id) {
		if (!(this.hashid_.length() != 0)) {
			this.hashid_ = id;
		}
	}

	public String elementName_;
	public DomElementType elementType_;
	public List<String> classes_;
	public String hashid_;
}
