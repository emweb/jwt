/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
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
  public static final int MenuItemIcon = 100;
  public static final int MenuItemCheckBox = 101;
  public static final int MenuItemClose = 102;
  public static final int DialogCoverWidget = 200;
  public static final int DialogTitleBar = 201;
  public static final int DialogBody = 202;
  public static final int DialogFooter = 203;
  public static final int DialogCloseIcon = 204;
  public static final int DialogContent = 205;
  public static final int TableViewRowContainer = 300;
  public static final int DatePickerPopup = 400;
  public static final int DatePickerIcon = 401;
  public static final int TimePickerPopup = 410;
  public static final int TimePickerPopupContent = 411;
  public static final int PanelTitleBar = 500;
  public static final int PanelCollapseButton = 501;
  public static final int PanelTitle = 502;
  public static final int PanelBody = 503;
  public static final int PanelBodyContent = 504;
  public static final int AuthWidgets = 600;
  public static final int InPlaceEditing = 700;
  public static final int InPlaceEditingButtonsContainer = 701;
  public static final int InPlaceEditingButton = 702;
  public static final int Navbar = 800;
  public static final int NavCollapse = 801;
  public static final int NavBrand = 802;
  public static final int NavbarForm = 803;
  public static final int NavbarSearchForm = 804;
  public static final int NavbarSearchInput = 805;
  public static final int NavbarMenu = 806;
  public static final int NavbarBtn = 807;
  public static final int NavbarAlignLeft = 808;
  public static final int NavbarAlignRight = 809;
}
