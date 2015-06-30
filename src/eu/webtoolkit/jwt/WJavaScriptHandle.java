/*
 * Copyright (C) 2015 Emweb bvba, Herent, Belgium.
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

// TODO(Roel): Keep this up-to-date with C++ version
public class WJavaScriptHandle<T extends WJavaScriptExposableObject> {
	private static Logger logger = LoggerFactory.getLogger(WJavaScriptHandle.class);

	public WJavaScriptHandle() { }

	public WJavaScriptHandle(int id, T value) {
		assert value.clientBinding_ != null;
		value_ = value;
		id_ = id;
	}

	public String getJsRef() {
		return value_.clientBinding_.jsRef_;
	}

	// TODO(Roel): Java can probably get away with a lot less copying!
	public void setValue(T value) {
		// Rescue the binding
		WJavaScriptExposableObject.JSInfo binding = value_.clientBinding_;
		value_ = (T)value.clone();
		value_.clientBinding_ = binding;
		binding.context_.jsValues.set(id_, value_);
		binding.context_.dirty.set(id_, true);
	}

	public T getValue() {
		if (value_ == null) return null;
		return (T)value_.clone();
	}

	private T value_ = null;
	private int id_ = 0;
}
