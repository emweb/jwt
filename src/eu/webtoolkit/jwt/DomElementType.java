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

/**
 * Enumeration for a DOM element type.
 * <p>
 * 
 * For internal use only.
 */
public enum DomElementType {
	DomElement_A, DomElement_BR, DomElement_BUTTON, DomElement_COL, DomElement_COLGROUP, DomElement_DIV, DomElement_FIELDSET, DomElement_FORM, DomElement_H1, DomElement_H2, DomElement_H3, DomElement_H4, DomElement_H5, DomElement_H6, DomElement_IFRAME, DomElement_IMG, DomElement_INPUT, DomElement_LABEL, DomElement_LEGEND, DomElement_LI, DomElement_OL, DomElement_OPTION, DomElement_UL, DomElement_SCRIPT, DomElement_SELECT, DomElement_SPAN, DomElement_TABLE, DomElement_TBODY, DomElement_THEAD, DomElement_TFOOT, DomElement_TH, DomElement_TD, DomElement_TEXTAREA, DomElement_OPTGROUP, DomElement_TR, DomElement_P, DomElement_CANVAS, DomElement_MAP, DomElement_AREA, DomElement_STYLE, DomElement_OBJECT, DomElement_PARAM, DomElement_AUDIO, DomElement_VIDEO, DomElement_SOURCE, DomElement_B, DomElement_STRONG, DomElement_EM, DomElement_I, DomElement_HR, DomElement_UNKNOWN, DomElement_OTHER;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
