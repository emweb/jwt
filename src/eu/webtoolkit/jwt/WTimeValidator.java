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
 * A time validator.
 * <p>
 * 
 * @see WTimeEdit
 * @see WTime
 * @see WTimePicker
 */
public class WTimeValidator extends WRegExpValidator {
	private static Logger logger = LoggerFactory
			.getLogger(WTimeValidator.class);

	/**
	 * Creates a new {@link WTimeValidator}.
	 */
	public WTimeValidator(WObject parent) {
		super(parent);
		this.format_ = "";
		this.setFormat(WTime.getDefaultFormat());
	}

	/**
	 * Creates a new {@link WTimeValidator}.
	 * <p>
	 * Calls {@link #WTimeValidator(WObject parent) this((WObject)null)}
	 */
	public WTimeValidator() {
		this((WObject) null);
	}

	/**
	 * Creates a new {@link WTimeValidator}.
	 */
	public WTimeValidator(final String format, WObject parent) {
		super(parent);
		this.format_ = "";
		this.setFormat(format);
	}

	/**
	 * Creates a new {@link WTimeValidator}.
	 * <p>
	 * Calls {@link #WTimeValidator(String format, WObject parent) this(format,
	 * (WObject)null)}
	 */
	public WTimeValidator(final String format) {
		this(format, (WObject) null);
	}

	/**
	 * sets the validator format
	 * <p>
	 */
	public void setFormat(final String format) {
		if (!this.format_.equals(format)) {
			this.format_ = format;
			this.setRegExp(WTime.formatToRegExp(format).regexp);
			this.repaint();
		}
	}

	/**
	 * Returns the Validator current format.
	 */
	public String getFormat() {
		return this.format_;
	}

	private String format_;
}
