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

class WJavaScriptPreamble {
	public WJavaScriptPreamble(JavaScriptScope scope,
			JavaScriptObjectType type, String name, String src) {
		this.scope = scope;
		this.type = type;
		this.name = name;
		this.src = src;
	}

	public JavaScriptScope scope;
	public JavaScriptObjectType type;
	public String name;
	public String src;
}
