/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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

class NumericItem extends WStandardItem {
	private static Logger logger = LoggerFactory.getLogger(NumericItem.class);

	public NumericItem clone() {
		return new NumericItem();
	}

	public void setData(final Object data, int role) {
		if (role == ItemDataRole.EditRole) {
			Object dt = new Object();
			double d = StringUtils.asNumber(data);
			if (d != d) {
				dt = data;
			} else {
				dt = d;
			}
			super.setData(dt, role);
		} else {
			super.setData(data, role);
		}
	}
}
