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
 * Enumeration for the role of a DOM element (for theme support).
 * <p>
 * 
 * @see WTheme#apply(WWidget widget, WWidget child, int widgetRole)
 */
public class ElementThemeRole {
	public final static int MainElementThemeRole = 0;
	public final static int ProgressBarBarRole = 100;
	public final static int ProgressBarLabelRole = 101;
}
