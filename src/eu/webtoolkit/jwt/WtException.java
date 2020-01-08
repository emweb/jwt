/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

/**
 * JWt runtime exception.
 */
public class WtException extends RuntimeException {
	/**
	 * Constructor.
	 */
	public WtException(String msg) {
		super(msg);
	}

	/**
	 * Constructor.
	 */
	public WtException(String msg, Exception wrapped) {
		super(msg, wrapped);
	}
}
