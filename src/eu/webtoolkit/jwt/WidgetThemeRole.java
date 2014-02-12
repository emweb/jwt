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
 * Enumeration for the role of a subwidget (for theme support).
 * <p>
 * 
 * @see WTheme#apply(WWidget widget, WWidget child, int widgetRole)
 */
public class WidgetThemeRole {
	public final static int MenuItemIconRole = 100;
	public final static int MenuItemCheckBoxRole = 101;
	public final static int MenuItemCloseRole = 102;
	public final static int DialogCoverRole = 200;
	public final static int DialogTitleBarRole = 201;
	public final static int DialogBodyRole = 202;
	public final static int DialogFooterRole = 203;
	public final static int DialogCloseIconRole = 204;
	public final static int DialogContent = 205;
	public final static int TableViewRowContainerRole = 300;
	public final static int DatePickerPopupRole = 400;
	public final static int PanelTitleBarRole = 500;
	public final static int PanelCollapseButtonRole = 501;
	public final static int PanelTitleRole = 502;
	public final static int PanelBodyRole = 503;
	public final static int AuthWidgets = 600;
	public final static int InPlaceEditingRole = 700;
	public final static int NavbarRole = 800;
	public final static int NavCollapseRole = 801;
	public final static int NavBrandRole = 802;
	public final static int NavbarSearchRole = 803;
	public final static int NavbarMenuRole = 804;
	public final static int NavbarBtn = 805;
}
