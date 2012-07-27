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
 * Base class for exceptions thrown by Wt.
 */
public class WException extends RuntimeException {
	private static Logger logger = LoggerFactory.getLogger(WException.class);

	/**
	 * Creates an exception.
	 */
	public WException(String what) {
		super();
		this.what_ = what;
	}

	/**
	 * Creates an exception.
	 */
	public WException(String what, RuntimeException wrapped) {
		super();
		this.what_ = what + "\nCaused by exception: " + wrapped.toString();
	}

	/**
	 * Returns the message.
	 */
	public String toString() {
		return this.what_;
	}

	/**
	 * Sets the message.
	 */
	public void setMessage(String message) {
		this.what_ = message;
	}

	private String what_;
}
