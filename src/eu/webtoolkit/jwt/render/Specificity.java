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

class Specificity {
	private static Logger logger = LoggerFactory.getLogger(Specificity.class);

	public Specificity(boolean valid) {
		this.value_ = "     ";
		this.setA(0);
		this.setB(0);
		this.setC(0);
		this.setD(0);
		this.setValid(valid);
	}

	public Specificity() {
		this(true);
	}

	public Specificity(int a, int b, int c, int d) {
		this.value_ = "     ";
		this.setA(a);
		this.setB(b);
		this.setC(c);
		this.setD(d);
		this.setValid(true);
	}

	public void setValid(boolean b) {
		this.value_ = StringUtils.put(this.value_, 0, b ? (char) 1 : (char) 0);
	}

	public void setA(int a) {
		this.value_ = StringUtils.put(this.value_, 1, (char) (a % 256));
	}

	public void setB(int b) {
		this.value_ = StringUtils.put(this.value_, 2, (char) (b % 256));
	}

	public void setC(int c) {
		this.value_ = StringUtils.put(this.value_, 3, (char) (c % 256));
	}

	public void setD(int d) {
		this.value_ = StringUtils.put(this.value_, 4, (char) (d % 256));
	}

	public boolean isValid() {
		return this.value_.charAt(0) == (char) 1;
	}

	public boolean isSmallerThen(final Specificity other) {
		return this.value_.compareTo(other.value_) < 0;
	}

	public boolean isGreaterThen(final Specificity other) {
		return this.value_.compareTo(other.value_) > 0;
	}

	public boolean isSmallerOrEqualThen(final Specificity other) {
		return !this.isGreaterThen(other);
	}

	public boolean isGreaterOrEqualThen(final Specificity other) {
		return !this.isSmallerThen(other);
	}

	private String value_;
}
