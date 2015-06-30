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
 * JavaScript exposable object.
 */
public abstract class WJavaScriptExposableObject {
	private static Logger logger = LoggerFactory
			.getLogger(WJavaScriptExposableObject.class);

	public WJavaScriptExposableObject() {
		this.clientBinding_ = null;
	}

	public WJavaScriptExposableObject(final WJavaScriptExposableObject other) {
		this.clientBinding_ = other.clientBinding_;
	}

	public abstract WJavaScriptExposableObject clone();

	/**
	 * Returns whether this object is JavaScript bound.
	 * <p>
	 * A JavaScript bound object (as opposed to being mostly a simple value
	 * class) has an equivalent representation in JavaScript. Its value can
	 * usually only be modified through a WJavaScriptHandle.
	 */
	public boolean isJavaScriptBound() {
		return this.clientBinding_ != null;
	}

	public abstract String getJsValue();

	public String getJsRef() {
		if (this.clientBinding_ != null) {
			return this.clientBinding_.jsRef_;
		} else {
			return this.getJsValue();
		}
	}

	protected boolean sameBindingAs(final WJavaScriptExposableObject rhs) {
		if (!(this.clientBinding_ != null) && !(rhs.clientBinding_ != null)) {
			return true;
		} else {
			if (this.clientBinding_ != null && rhs.clientBinding_ != null) {
				return this.clientBinding_.equals(rhs.clientBinding_);
			} else {
				return false;
			}
		}
	}

	protected boolean sameContextAs(final WJavaScriptExposableObject rhs) {
		if (!(this.clientBinding_ != null) && !(rhs.clientBinding_ != null)) {
			return true;
		} else {
			if (this.clientBinding_ != null && rhs.clientBinding_ != null) {
				return this.clientBinding_.context_ == rhs.clientBinding_.context_;
			} else {
				return false;
			}
		}
	}

	protected void assignBinding(final WJavaScriptExposableObject rhs) {
		assert rhs.clientBinding_ != null;
		if (rhs != this) {
			if (this.clientBinding_ != null) {
				;
			}
			this.clientBinding_ = rhs.clientBinding_;
		}
	}

	protected void assignBinding(final WJavaScriptExposableObject rhs,
			final String jsRef) {
		assert rhs.clientBinding_ != null;
		if (rhs != this) {
			if (this.clientBinding_ != null) {
				;
			}
			this.clientBinding_ = new WJavaScriptExposableObject.JSInfo(
					rhs.clientBinding_);
		}
		this.clientBinding_.jsRef_ = jsRef;
	}

	static class JSInfo {
		private static Logger logger = LoggerFactory.getLogger(JSInfo.class);

		public JSInfo(WJavaScriptObjectStorage context, final String jsRef) {
			this.context_ = context;
			this.jsRef_ = jsRef;
		}

		public JSInfo(final WJavaScriptExposableObject.JSInfo other) {
			this.context_ = other.context_;
			this.jsRef_ = other.jsRef_;
		}

		public boolean equals(final WJavaScriptExposableObject.JSInfo rhs) {
			return this.context_ == rhs.context_
					&& this.jsRef_.equals(rhs.jsRef_);
		}

		public WJavaScriptObjectStorage context_;
		public String jsRef_;
	}

	WJavaScriptExposableObject.JSInfo clientBinding_;
}
