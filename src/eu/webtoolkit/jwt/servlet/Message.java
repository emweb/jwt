/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.servlet;

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
 * An HTTP client message (request or response).
 * <p>
 * 
 * This class implements a message that is sent or received by the HTTP
 * {@link Client}.
 * <p>
 * It is not to be confused with {@link Request} and {@link Response}, which are
 * involved in the web application server handling.
 */
public class Message {
	private static Logger logger = LoggerFactory.getLogger(Message.class);

	/**
	 * An HTTP message header.
	 * <p>
	 * 
	 * An HTTP message header is a name/value pair, as defined by RFC 822.
	 */
	public static class Header {
		private static Logger logger = LoggerFactory.getLogger(Header.class);

		/**
		 * Default constructor.
		 */
		public Header() {
			this.name_ = "";
			this.value_ = "";
		}

		/**
		 * Constructs a header with a given name and value.
		 */
		public Header(String name, String value) {
			this.name_ = name;
			this.value_ = value;
		}

		/**
		 * Copy constructor.
		 */
		public Header(Message.Header other) {
			this.name_ = other.name_;
			this.value_ = other.value_;
		}

		/**
		 * Sets the header name.
		 */
		public void setName(String name) {
			this.name_ = name;
		}

		/**
		 * Returns the header name.
		 * <p>
		 * 
		 * @see Message#setName(String name)
		 */
		public String getName() {
			return this.name_;
		}

		/**
		 * Sets the header value.
		 */
		public void setValue(String value) {
			this.value_ = value;
		}

		/**
		 * Returns the header value.
		 * <p>
		 * 
		 * @see Message#setValue(String value)
		 */
		public String getValue() {
			return this.value_;
		}

		private String name_;
		private String value_;
	}

	/**
	 * Constructor.
	 * <p>
	 * This creates an empty message, with an invalid status (-1), no headers
	 * and an empty body.
	 */
	public Message() {
		this.status_ = -1;
		this.headers_ = new ArrayList<Message.Header>();
		this.body_ = new StringBuilder();
	}

	/**
	 * Sets the status code.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>This method is probably not useful to you, since for a
	 * request it is ignored, and for a response it is set by the client. </i>
	 * </p>
	 */
	public void setStatus(int status) {
		this.status_ = status;
	}

	/**
	 * Returns the status code.
	 * <p>
	 * This returns the HTTP status code of a response message. Typical values
	 * are 200 (OK) or 404 (Not found).
	 */
	public int getStatus() {
		return this.status_;
	}

	/**
	 * Sets a header value.
	 * <p>
	 * If a header with that value was already defined, it is replaced with the
	 * new value. Otherwise, the header is added.
	 */
	public void setHeader(String name, String value) {
		for (int i = 0; i < this.headers_.size(); ++i) {
			if (this.headers_.get(i).getName().equals(name)) {
				this.headers_.get(i).setValue(value);
				return;
			}
		}
		this.headers_.add(new Message.Header(name, value));
	}

	/**
	 * Returns the headers.
	 */
	public List<Message.Header> getHeaders() {
		return this.headers_;
	}

	/**
	 * Returns a header value.
	 * <p>
	 * Returns 0 if no header with that name is found.
	 */
	public String getHeader(String name) {
		for (int i = 0; i < this.headers_.size(); ++i) {
			if (this.headers_.get(i).getName().equals(name)) {
				return this.headers_.get(i).getValue();
			}
		}
		return null;
	}

	/**
	 * Concatenates body text.
	 * <p>
	 * Adds the <code>text</code> to the message body.
	 */
	public void addBodyText(String text) {
		this.body_.append(text);
	}

	/**
	 * Returns the body text.
	 * <p>
	 * Returns the body text.
	 */
	public String getBody() {
		return this.body_.toString();
	}

	private int status_;
	private List<Message.Header> headers_;
	private StringBuilder body_;
}
