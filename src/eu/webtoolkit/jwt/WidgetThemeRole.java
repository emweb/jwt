/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Enumeration for the role of a subwidget (for theme support)
 *
 * <p>
 *
 * @see WTheme#apply(WWidget widget, WWidget child, int widgetRole)
 */
public class WidgetThemeRole {
  public static final int MenuItemIconRole = 100;
  public static final int MenuItemCheckBoxRole = 101;
  public static final int MenuItemCloseRole = 102;
  public static final int DialogCoverRole = 200;
  public static final int DialogTitleBarRole = 201;
  public static final int DialogBodyRole = 202;
  public static final int DialogFooterRole = 203;
  public static final int DialogCloseIconRole = 204;
  public static final int DialogContent = 205;
  public static final int TableViewRowContainerRole = 300;
  public static final int DatePickerPopupRole = 400;
  public static final int TimePickerPopupRole = 410;
  public static final int PanelTitleBarRole = 500;
  public static final int PanelCollapseButtonRole = 501;
  public static final int PanelTitleRole = 502;
  public static final int PanelBodyRole = 503;
  public static final int AuthWidgets = 600;
  public static final int InPlaceEditingRole = 700;
  public static final int NavbarRole = 800;
  public static final int NavCollapseRole = 801;
  public static final int NavBrandRole = 802;
  public static final int NavbarSearchRole = 803;
  public static final int NavbarMenuRole = 804;
  public static final int NavbarBtn = 805;
  public static final int NavbarAlignLeftRole = 806;
  public static final int NavbarAlignRightRole = 807;
}
