/*
 * Copyright (C) 2015 Emweb bv, Herent, Belgium.
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
 *  A handle to a JavaScript representation of an object.
 *  <p>
 *
 *  A WJavaScriptHandle allows to access and modify an object in JavaScript.
 *  This is useful to avoid server roundtrips when frequently updating something,
 *  e.g. to interact with and animate a WPaintedWidget.
 *	<p>
 *  You can use the {@link #getValue value} of a WJavaScriptHandle just as you would normally,
 *  with the exception that it will be {@link WJavaScriptExposableObject#isJavaScriptBound
 *  JavaScript bound} and so will any copies you make of it. You should
 *  not modify a JavaScript bound object, as this will not change its client side
 *  representation. Use the handle's {@link #setValue} method instead.
 *	<p>
 *  You can access (and modify) the value of a handle on the client side using {@link #getJsRef}.
 *  <p>
 *  You can update the value from the server with {@link #setValue}. Currently, changes
 *  made to the value on the client side are not synced back to the server, but
 *  support may be added for this in the future.
 *  <p>
 *  Currently, only WPaintedWidget allows the use of {@link WJavaScriptExposableObject
 *  JavaScript exposable objects}.
 *  <p>
 *
 *  @see WJavaScriptExposableObject
 *  @see WPaintedWidget
 */
public class WJavaScriptHandle<T extends WJavaScriptExposableObject> {
	private static Logger logger = LoggerFactory.getLogger(WJavaScriptHandle.class);

	WJavaScriptHandle(int id, T value) {
		assert value != null && value.clientBinding_ != null;
		value_ = value;
		id_ = id;
	}

	/**
	 * Returns the JavaScript representation of the object.
	 * <p>
	 *
	 * You can access and modify the value of this handle through its {@link #getJsRef jsRef}.
	 * <p>
	 * Changes on the client side are currently not synced back to the
	 * server, but support for this may be added in the future.
	 */
	public String getJsRef() {
		assert value_ != null;
		return value_.clientBinding_.jsRef_;
	}

    /**
	 * Set the value for this handle.
     * <p>
	 *
     * The value may not be JavaScript bound, i.e. related to another WJavaScriptHandle.
	 * <p>
     * The change to the value will be synced to the client side equivalent.
	 * <p>
	 * A defensive copy will be made of given value.
     *
	 * @throws NullPointerException The given value is null
     * @throws WException Trying to assign a JavaScript bound value
     */
	public void setValue(T value) {
		if (value.isJavaScriptBound()) {
			throw new WException("Can not assign a JavaScript bound value to a WJavaScriptHandle!");
		}
		// Rescue the binding
		WJavaScriptExposableObject.JSInfo binding = value_.clientBinding_;
		value_ = (T)value.clone();
		value_.clientBinding_ = binding;
		binding.context_.jsValues.set(id_, value_);
		binding.context_.dirty.set(id_, true);
	}

    /** 
	 * Get the value for this handle.
	 * <p>
	 *
	 * @return A copy of the value.
	 * <p>
     * <p>
     * <i><b>Warning:</b> You should not modify this value or any copy of it on the server side,
     *	     because this will not be synced to the client side. Use {@link #setValue} instead.</i>
	 * </p>
     */
	public T getValue() {
		assert value_ != null;
		return (T)value_.clone();
	}

	private T value_;
	private final int id_;
}
