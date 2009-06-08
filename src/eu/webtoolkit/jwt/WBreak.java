package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A widget that provides a line break between inline widgets
 * 
 * 
 * This is an {@link WWidget#setInline(boolean inlined) inline} widget that
 * provides a line break inbetween its sibling widgets (such as {@link WText}).
 * <p>
 * The widget corresponds to the HTML <code>&lt;br /&gt;</code> tag.
 */
public class WBreak extends WWebWidget {
	/**
	 * Construct a line break.
	 */
	public WBreak(WContainerWidget parent) {
		super(parent);
	}

	public WBreak() {
		this((WContainerWidget) null);
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_BR;
	}
}
