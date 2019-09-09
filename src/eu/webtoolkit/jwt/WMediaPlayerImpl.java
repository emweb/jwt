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

class WMediaPlayerImpl extends WTemplate {
	private static Logger logger = LoggerFactory
			.getLogger(WMediaPlayerImpl.class);

	public WMediaPlayerImpl(WMediaPlayer player, final CharSequence text) {
		super(text);
		this.player_ = player;
		this.setFormObject(true);
	}

	String renderRemoveJs(boolean recursive) {
		if (this.isRendered()) {
			String result = this.player_.getJsPlayerRef()
					+ ".jPlayer('destroy');";
			if (!recursive) {
				result += "Wt3_4_1.remove('" + this.getId() + "');";
			}
			return result;
		} else {
			return super.renderRemoveJs(recursive);
		}
	}

	protected void setFormData(final WObject.FormData formData) {
		this.player_.setFormData(formData);
	}

	private WMediaPlayer player_;
}
