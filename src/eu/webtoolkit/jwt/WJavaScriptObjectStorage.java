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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

class WJavaScriptObjectStorage {
	private static Logger logger = LoggerFactory.getLogger(WJavaScriptObjectStorage.class);

	public WJavaScriptObjectStorage(WWidget widget) {
		this.widget = widget;
	}

	public <T extends WJavaScriptExposableObject> WJavaScriptHandle<T> addObject(T o) {
		int index = jsValues.size();
		jsValues.add(o);
		dirty.set(index, true);
		o.clientBinding_ = new WJavaScriptExposableObject.JSInfo(
				this, getJsRef() + ".jsValues[" + index + "]");
		return new WJavaScriptHandle<T>(index, o);
	}

	public void updateJs(StringBuilder js) {
		for (int i = 0; i < jsValues.size(); ++i) {
			if (dirty.get(i)) {
				js.append(getJsRef()).append(".setJsValue(").append(i).append(",");
				js.append(jsValues.get(i).getJsValue()).append(");");
				dirty.set(i, false);
			}
		}
	}

	public int size() {
		return jsValues.size();
	}

	public void assignFromJSON(String json) {
		try {
			JsonElement result = new JsonParser().parse(json);
			JsonObject o = result.getAsJsonObject();

			if (jsValues.size() < o.entrySet().size())
				throw new IllegalStateException("JSON array length is larger than number of jsValues");

			for (Map.Entry<String,JsonElement> i : o.entrySet()) {
				int idx = Integer.parseInt(i.getKey(), 10);
				JsonElement value = i.getValue();
				if (idx >= jsValues.size())
					throw new IllegalStateException("JSON value index is outside of bounds");
				if (!dirty.get(idx))
					jsValues.get(idx).assignFromJSON(value);
			}
		} catch (JsonParseException e) {
			logger.error("Failed to parse JSON", e);
		} catch (IllegalStateException e) {
			logger.error("Failed to assign value from JSON", e);
		} catch (NumberFormatException e) {
			logger.error("Failed to assign value from JSON, couldn't cast index", e);
		}
	}

	public String getJsRef() {
		return "jQuery.data(" + widget.getJsRef() + ",'jsobj')";
	}

	final List<WJavaScriptExposableObject> jsValues = new ArrayList<WJavaScriptExposableObject>();
	final BitSet dirty = new BitSet();
	private final WWidget widget;
}
