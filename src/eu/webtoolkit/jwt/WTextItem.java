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

class WTextItem {
	public WTextItem(CharSequence text, double width) {
		this.text_ = WString.toWString(text);
		this.width_ = width;
	}

	public WString getText() {
		return this.text_;
	}

	public double getWidth() {
		return this.width_;
	}

	private WString text_;
	private double width_;
}
