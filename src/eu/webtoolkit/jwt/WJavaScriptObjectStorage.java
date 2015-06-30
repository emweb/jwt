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

class WJavaScriptObjectStorage {
	private static Logger logger = LoggerFactory.getLogger(WJavaScriptObjectStorage.class);

	public WJavaScriptObjectStorage(String jsRef) {
		this.jsRef = jsRef;
	}

	public <T extends WJavaScriptExposableObject> WJavaScriptHandle<T> addObject(T o) {
		int index = jsValues.size();
		jsValues.add(o);
		dirty.set(index, true);
		o.clientBinding_ = new WJavaScriptExposableObject.JSInfo(
				this, jsRef + ".jsValues[" + index + "]");
		return new WJavaScriptHandle<T>(index, o);
	}

	public void updateJs(StringBuilder js) {
		for (int i = 0; i < jsValues.size(); ++i) {
			if (dirty.get(i)) {
				js.append(jsValues.get(i).getJsRef()).append("=").append(jsValues.get(i).getJsValue()).append(";");
				dirty.set(i, false);
			}
		}
	}

	public int size() {
		return jsValues.size();
	}

	public final List<WJavaScriptExposableObject> jsValues = new ArrayList<WJavaScriptExposableObject>();
	public final BitSet dirty = new BitSet();
	public final String jsRef;
}
