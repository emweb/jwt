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

class WCssTemplateWidget extends WWebWidget {
	private static Logger logger = LoggerFactory
			.getLogger(WCssTemplateWidget.class);

	public WCssTemplateWidget(WCssTemplateRule rule) {
		super();
		this.rule_ = rule;
	}

	public void setPositionScheme(PositionScheme scheme) {
		super.setPositionScheme(scheme);
		this.rule_.modified();
	}

	public void setOffsets(WLength offset, EnumSet<Side> sides) {
		super.setOffsets(offset, sides);
		this.rule_.modified();
	}

	public void resize(WLength width, WLength height) {
		super.resize(width, height);
		this.rule_.modified();
	}

	public void setMinimumSize(WLength width, WLength height) {
		super.setMinimumSize(width, height);
		this.rule_.modified();
	}

	public void setMaximumSize(WLength width, WLength height) {
		super.setMaximumSize(width, height);
		this.rule_.modified();
	}

	public void setLineHeight(WLength height) {
		super.setLineHeight(height);
		this.rule_.modified();
	}

	public void setFloatSide(Side s) {
		super.setFloatSide(s);
		this.rule_.modified();
	}

	public void setClearSides(EnumSet<Side> sides) {
		super.setClearSides(sides);
		this.rule_.modified();
	}

	public void setMargin(WLength margin, EnumSet<Side> sides) {
		super.setMargin(margin, sides);
		this.rule_.modified();
	}

	public void setHidden(boolean hidden, WAnimation animation) {
		super.setHidden(hidden, animation);
		this.rule_.modified();
	}

	public void setPopup(boolean popup) {
		super.setPopup(popup);
		this.rule_.modified();
	}

	public void setInline(boolean isinline) {
		super.setInline(isinline);
		this.rule_.modified();
	}

	public WCssDecorationStyle getDecorationStyle() {
		this.rule_.modified();
		return super.getDecorationStyle();
	}

	public void setVerticalAlignment(AlignmentFlag alignment, WLength length) {
		super.setVerticalAlignment(alignment, length);
		this.rule_.modified();
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_SPAN;
	}

	private WCssTemplateRule rule_;
}
