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
 * CSS-based theme support. This implements the classic JWt themes, which were
 * available before theme support was customized with the addition of the
 * {@link WTheme} class.
 * <p>
 * The following table shows which style classes are applied by this theme.
 * <p>
 * 
 * <table border="1" cellspacing="3" cellpadding="3">
 * <tr>
 * <td rowspan="12">{@link WAbstractItemView}</td>
 * <td>.Wt-itemview</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-headerdiv</td>
 * <td>the header container</td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-header</td>
 * <td>the header</td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-header .Wt-label</td>
 * <td>a header label</td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-header .Wt-label</td>
 * <td>a header label</td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-tv-rh</td>
 * <td>resize handle</td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-tv-sh</td>
 * <td>sort handle</td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-tv-sh-none</td>
 * <td>sort handle, unsorted</td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-tv-sh-down</td>
 * <td>sort handle, descending sort</td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-tv-sh-up</td>
 * <td>sort handle, ascending sort</td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-selected</td>
 * <td>selected item (or row)</td>
 * </tr>
 * <tr>
 * <td>.Wt-itemview .Wt-spacer</td>
 * <td>spacer (briefly visible during scrolling)</td>
 * </tr>
 * <tr>
 * <td>{@link WAbstractSpinBox}</td>
 * <td>.Wt-spinbox</td>
 * <td>(for the HTML4 implementation)</td>
 * </tr>
 * <tr>
 * <td rowspan="11">{@link WCalendar}</td>
 * <td>.Wt-cal</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>.Wt-cal table.d1</td>
 * <td>the table (single letter days)</td>
 * </tr>
 * <tr>
 * <td>.Wt-cal table.d3</td>
 * <td>the table (three letter days)</td>
 * </tr>
 * <tr>
 * <td>.Wt-cal table.dlong</td>
 * <td>the table (ful day names)</td>
 * </tr>
 * <tr>
 * <td>.Wt-cal th.caption</td>
 * <td>a caption cell (containing month/year navigation)</td>
 * </tr>
 * <tr>
 * <td>.Wt-cal th</td>
 * <td>week day header cell</td>
 * </tr>
 * <tr>
 * <td>.Wt-cal td</td>
 * <td>day cell</td>
 * </tr>
 * <tr>
 * <td>.Wt-cal-oom</td>
 * <td>out-of-month day</td>
 * </tr>
 * <tr>
 * <td>.Wt-cal-oom</td>
 * <td>out-of-range day (ray &lt; bottom or day &gt; top)</td>
 * </tr>
 * <tr>
 * <td>.Wt-cal-sel</td>
 * <td>selected day</td>
 * </tr>
 * <tr>
 * <td>.Wt-cal-now</td>
 * <td>today</td>
 * </tr>
 * <tr>
 * <td rowspan="2">{@link WDateEdit}</td>
 * <td>.Wt-dateedit</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>.Wt-datepicker</td>
 * <td>the popup</td>
 * </tr>
 * <tr>
 * <td>{@link WDatePicker}</td>
 * <td>.Wt-datepicker</td>
 * <td>the popup</td>
 * </tr>
 * <tr>
 * <td rowspan="5">{@link WDialog}</td>
 * <td>.Wt-dialog</td>
 * <td>the dialog</td>
 * </tr>
 * <tr>
 * <td>.Wt-dialog .closeicon</td>
 * <td>the close icon in the titlebar</td>
 * </tr>
 * <tr>
 * <td>.Wt-dialog .titlebar</td>
 * <td>the titlebar</td>
 * </tr>
 * <tr>
 * <td>.Wt-dialog .body</td>
 * <td>the dialog body</td>
 * </tr>
 * <tr>
 * <td>.Wt-dialog .footer</td>
 * <td>the dialog footer</td>
 * </tr>
 * <tr>
 * <td rowspan="8">{@link WMenuItem}</td>
 * <td>.item</td>
 * <td>an unselected item</td>
 * </tr>
 * <tr>
 * <td>.itemselected</td>
 * <td>a selected item</td>
 * </tr>
 * <tr>
 * <td>.item.Wt-closable</td>
 * <td>a closable item</td>
 * </tr>
 * <tr>
 * <td>.item.Wt-separator</td>
 * <td>a separator item</td>
 * </tr>
 * <tr>
 * <td>.item.Wt-sectheader</td>
 * <td>a section header item</td>
 * </tr>
 * <tr>
 * <td>.item .Wt-icon</td>
 * <td>the item&apos;s icon</td>
 * </tr>
 * <tr>
 * <td>.item .Wt-chkbox</td>
 * <td>the item&apos;s checkbox</td>
 * </tr>
 * <tr>
 * <td>.item .closeicon</td>
 * <td>the item&apos;s close icon</td>
 * </tr>
 * <tr>
 * <td>{@link WMessageBox}</td>
 * <td>.Wt-dialog</td>
 * <td>see supra (WDialog)</td>
 * </tr>
 * <tr>
 * <td rowspan="3">{@link WPanel}</td>
 * <td>.Wt-panel</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>.Wt-panel .titlebar</td>
 * <td>the titlebar</td>
 * </tr>
 * <tr>
 * <td>.Wt-panel .body</td>
 * <td>the body</td>
 * </tr>
 * <tr>
 * <td>{@link WPopupMenu}</td>
 * <td>.Wt-popupmenu</td>
 * <td>the popup menu; for the items, see supra (WMenuItem)</td>
 * </tr>
 * <tr>
 * <td>{@link WPopupWidget}</td>
 * <td>.Wt-outset</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td rowspan="3">{@link WProgressBar}</td>
 * <td>.Wt-progressbar</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>.Wt-progressbar .Wt-pgb-bar</td>
 * <td>the bar</td>
 * </tr>
 * <tr>
 * <td>.Wt-progressbar .Wt-pgb-label</td>
 * <td>the value label</td>
 * </tr>
 * <tr>
 * <td>{@link WPushButton}</td>
 * <td>.Wt-btn</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td rowspan="6">{@link WSlider}</td>
 * <td>.Wt-slider-h <i>or</i> .Wt-slider-v</td>
 * <td>for horizontal or vertical slider</td>
 * </tr>
 * <tr>
 * <td>.Wt-slider-[hv] .Wt-slider-bg</td>
 * <td>background element</td>
 * </tr>
 * <tr>
 * <td>.Wt-slider-[hv] .fill</td>
 * <td>fill to the current value</td>
 * </tr>
 * <tr>
 * <td>.Wt-slider-[hv] .handle</td>
 * <td>the slider handle</td>
 * </tr>
 * <tr>
 * <td>.Wt-slider-[hv] .Wt-w</td>
 * <td>an additional element for styling</td>
 * </tr>
 * <tr>
 * <td>.Wt-slider-[hv] .Wt-e</td>
 * <td>an additional element for styling</td>
 * </tr>
 * <tr>
 * <td rowspan="3">{@link WSuggestionPopup}</td>
 * <td>.Wt-suggest</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>.Wt-suggest li</td>
 * <td>an item</td>
 * </tr>
 * <tr>
 * <td>.Wt-suggest .active</td>
 * <td>an active item</td>
 * </tr>
 * <tr>
 * <td>{@link WTabWidget}</td>
 * <td>.Wt-tabs</td>
 * <td>the header, which is a {@link WMenu}</td>
 * </tr>
 * <tr>
 * <td rowspan="3">{@link WTableView}</td>
 * <td>.Wt-tableview</td>
 * <td>see supra (WAbstractItemView)</td>
 * </tr>
 * <tr>
 * <td>.Wt-tableview .Wt-contents</td>
 * <td>the contents area</td>
 * </tr>
 * <tr>
 * <td>.Wt-tableview .Wt-contents .Wt-tv-c</td>
 * <td>a contents cell</td>
 * </tr>
 * <tr>
 * <td rowspan="10">{@link WTreeNode}</td>
 * <td>.Wt-tree</td>
 * <td>a tree node</td>
 * </tr>
 * <tr>
 * <td>.Wt-tree.Wt-trunk</td>
 * <td>a trunk node</td>
 * </tr>
 * <tr>
 * <td>.Wt-tree.Wt-end</td>
 * <td>an end node (last node within parent)</td>
 * </tr>
 * <tr>
 * <td>.Wt-tree ul</td>
 * <td>children list</td>
 * </tr>
 * <tr>
 * <td>.Wt-tree .Wt-ctrl</td>
 * <td>collapse/expand control</td>
 * </tr>
 * <tr>
 * <td>.Wt-tree .Wt-ctrl.expand</td>
 * <td>expand control</td>
 * </tr>
 * <tr>
 * <td>.Wt-tree .Wt-ctrl.collapse</td>
 * <td>collapse control</td>
 * </tr>
 * <tr>
 * <td>.Wt-tree .Wt-ctrl.noexpand</td>
 * <td>an item that cannot be expanded</td>
 * </tr>
 * <tr>
 * <td>.Wt-tree .Wt-selected</td>
 * <td>a selected node</td>
 * </tr>
 * <tr>
 * <td>.Wt-tree .Wt-label</td>
 * <td>the label</td>
 * </tr>
 * <tr>
 * <td rowspan="10">{@link WTreeView}</td>
 * <td>.Wt-treeview</td>
 * <td>see supra (WAbstractItemView)</td>
 * </tr>
 * <tr>
 * <td>.Wt-treeview ul</td>
 * <td>a node</td>
 * </tr>
 * <tr>
 * <td>.Wt-treeview ul.Wt-tv-root</td>
 * <td>the root node</td>
 * </tr>
 * <tr>
 * <td>.Wt-treeview .Wt-tv-row</td>
 * <td>a row of additional cells</td>
 * </tr>
 * <tr>
 * <td>.Wt-treeview .Wt-trunk</td>
 * <td>a trunk node</td>
 * </tr>
 * <tr>
 * <td>.Wt-treeview .Wt-end</td>
 * <td>an end node (last node within parent)</td>
 * </tr>
 * <tr>
 * <td>.Wt-treeview .Wt-ctrl</td>
 * <td>collapse/expand control</td>
 * </tr>
 * <tr>
 * <td>.Wt-treeview .Wt-ctrl.expand</td>
 * <td>expand control</td>
 * </tr>
 * <tr>
 * <td>.Wt-treeview .Wt-ctrl.collapse</td>
 * <td>collapse control</td>
 * </tr>
 * <tr>
 * <td>.Wt-treeview .Wt-ctrl.noexpand</td>
 * <td>an item that cannot be expanded</td>
 * </tr>
 * </table>
 * 
 * 
 * <p>
 * <code>*</code> CSS selectors for these widgets are currently still hard-coded
 * in the widget itself.
 * <p>
 * 
 * @see WApplication#setTheme(WTheme theme)
 */
public class WCssTheme extends WTheme {
	private static Logger logger = LoggerFactory.getLogger(WCssTheme.class);

	/**
	 * Constructor.
	 * <p>
	 * 
	 * Creates a classic JWt theme. JWt comes with two CSS themes:
	 * &quot;polished&quot; and default. For the bootstrap theme, use
	 * {@link WBootstrapTheme}.
	 */
	public WCssTheme(final String name, WObject parent) {
		super(parent);
		this.name_ = name;
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WCssTheme(String name, WObject parent) this(name,
	 * (WObject)null)}
	 */
	public WCssTheme(final String name) {
		this(name, (WObject) null);
	}

	public String getName() {
		return this.name_;
	}

	/**
	 * Returns the stylesheets.
	 * <p>
	 * 
	 * Returns wt.css, plus on IE wt_ie.css, plus on IE6 wt_ie6.css. The style
	 * sheets are located in the theme directory in the resources folder.
	 */
	public List<WCssStyleSheet> getStyleSheets() {
		List<WCssStyleSheet> result = new ArrayList<WCssStyleSheet>();
		if (this.name_.length() != 0) {
			String themeDir = this.getResourcesUrl();
			WApplication app = WApplication.getInstance();
			result.add(new WCssStyleSheet(new WLink(themeDir + "wt.css")));
			if (app.getEnvironment().agentIsIElt(9)) {
				result.add(new WCssStyleSheet(new WLink(themeDir + "wt_ie.css")));
			}
			if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
				result.add(new WCssStyleSheet(
						new WLink(themeDir + "wt_ie6.css")));
			}
		}
		return result;
	}

	public void apply(WWidget widget, WWidget child, int widgetRole) {
		if (!widget.isThemeStyleEnabled()) {
			return;
		}
		switch (widgetRole) {
		case WidgetThemeRole.MenuItemIconRole:
			child.addStyleClass("Wt-icon");
			break;
		case WidgetThemeRole.MenuItemCheckBoxRole:
			child.addStyleClass("Wt-chkbox");
			break;
		case WidgetThemeRole.MenuItemCloseRole:
			widget.addStyleClass("Wt-closable");
			child.addStyleClass("closeicon");
			break;
		case WidgetThemeRole.DialogCoverRole:
			child.setStyleClass("Wt-dialogcover in");
			break;
		case WidgetThemeRole.DialogTitleBarRole:
			child.addStyleClass("titlebar");
			break;
		case WidgetThemeRole.DialogBodyRole:
			child.addStyleClass("body");
			break;
		case WidgetThemeRole.DialogFooterRole:
			child.addStyleClass("footer");
			break;
		case WidgetThemeRole.DialogCloseIconRole:
			child.addStyleClass("closeicon");
			break;
		case WidgetThemeRole.TableViewRowContainerRole: {
			WAbstractItemView view = ((widget) instanceof WAbstractItemView ? (WAbstractItemView) (widget)
					: null);
			String backgroundImage = "";
			if (view.hasAlternatingRowColors()) {
				backgroundImage = "stripes/stripe-";
			} else {
				backgroundImage = "no-stripes/no-stripe-";
			}
			backgroundImage = this.getResourcesUrl() + backgroundImage
					+ String.valueOf((int) view.getRowHeight().toPixels())
					+ "px.gif";
			child.getDecorationStyle().setBackgroundImage(
					new WLink(backgroundImage));
			break;
		}
		case WidgetThemeRole.DatePickerPopupRole:
			child.addStyleClass("Wt-datepicker");
			break;
		case WidgetThemeRole.PanelTitleBarRole:
			child.addStyleClass("titlebar");
			break;
		case WidgetThemeRole.PanelBodyRole:
			child.addStyleClass("body");
			break;
		case WidgetThemeRole.AuthWidgets:
			WApplication app = WApplication.getInstance();
			app.useStyleSheet(new WLink(WApplication.getRelativeResourcesUrl()
					+ "form.css"));
			app.getBuiltinLocalizedStrings().useBuiltin(
					WtServlet.AuthCssTheme_xml);
			break;
		}
	}

	public void apply(WWidget widget, final DomElement element, int elementRole) {
		if (!widget.isThemeStyleEnabled()) {
			return;
		}
		boolean creating = element.getMode() == DomElement.Mode.ModeCreate;
		{
			WPopupWidget popup = ((widget) instanceof WPopupWidget ? (WPopupWidget) (widget)
					: null);
			if (popup != null) {
				element.addPropertyWord(Property.PropertyClass, "Wt-outset");
			}
		}
		switch (element.getType()) {
		case DomElement_BUTTON:
			if (creating) {
				element.addPropertyWord(Property.PropertyClass, "Wt-btn");
				WPushButton b = ((widget) instanceof WPushButton ? (WPushButton) (widget)
						: null);
				if (b != null) {
					if (b.isDefault()) {
						element.addPropertyWord(Property.PropertyClass,
								"Wt-btn-default");
					}
					if (!(b.getText().length() == 0)) {
						element.addPropertyWord(Property.PropertyClass,
								"with-label");
					}
				}
			}
			break;
		case DomElement_UL:
			if (((widget) instanceof WPopupMenu ? (WPopupMenu) (widget) : null) != null) {
				element.addPropertyWord(Property.PropertyClass,
						"Wt-popupmenu Wt-outset");
			} else {
				WTabWidget tabs = ((widget.getParent().getParent()) instanceof WTabWidget ? (WTabWidget) (widget
						.getParent().getParent()) : null);
				if (tabs != null) {
					element.addPropertyWord(Property.PropertyClass, "Wt-tabs");
				} else {
					WSuggestionPopup suggestions = ((widget) instanceof WSuggestionPopup ? (WSuggestionPopup) (widget)
							: null);
					if (suggestions != null) {
						element.addPropertyWord(Property.PropertyClass,
								"Wt-suggest");
					}
				}
			}
			break;
		case DomElement_LI: {
			WMenuItem item = ((widget) instanceof WMenuItem ? (WMenuItem) (widget)
					: null);
			if (item != null) {
				if (item.isSeparator()) {
					element.addPropertyWord(Property.PropertyClass,
							"Wt-separator");
				}
				if (item.isSectionHeader()) {
					element.addPropertyWord(Property.PropertyClass,
							"Wt-sectheader");
				}
				if (item.getMenu() != null) {
					element.addPropertyWord(Property.PropertyClass, "submenu");
				}
			}
		}
			break;
		case DomElement_DIV: {
			WDialog dialog = ((widget) instanceof WDialog ? (WDialog) (widget)
					: null);
			if (dialog != null) {
				element.addPropertyWord(Property.PropertyClass, "Wt-dialog");
				return;
			}
			WPanel panel = ((widget) instanceof WPanel ? (WPanel) (widget)
					: null);
			if (panel != null) {
				element.addPropertyWord(Property.PropertyClass,
						"Wt-panel Wt-outset");
				return;
			}
			WProgressBar bar = ((widget) instanceof WProgressBar ? (WProgressBar) (widget)
					: null);
			if (bar != null) {
				switch (elementRole) {
				case ElementThemeRole.MainElementThemeRole:
					element.addPropertyWord(Property.PropertyClass,
							"Wt-progressbar");
					break;
				case ElementThemeRole.ProgressBarBarRole:
					element.addPropertyWord(Property.PropertyClass,
							"Wt-pgb-bar");
					break;
				case ElementThemeRole.ProgressBarLabelRole:
					element.addPropertyWord(Property.PropertyClass,
							"Wt-pgb-label");
				}
				return;
			}
		}
			break;
		case DomElement_INPUT: {
			WAbstractSpinBox spinBox = ((widget) instanceof WAbstractSpinBox ? (WAbstractSpinBox) (widget)
					: null);
			if (spinBox != null) {
				element.addPropertyWord(Property.PropertyClass, "Wt-spinbox");
				return;
			}
			WDateEdit dateEdit = ((widget) instanceof WDateEdit ? (WDateEdit) (widget)
					: null);
			if (dateEdit != null) {
				element.addPropertyWord(Property.PropertyClass, "Wt-dateedit");
				return;
			}
			WTimeEdit timeEdit = ((widget) instanceof WTimeEdit ? (WTimeEdit) (widget)
					: null);
			if (timeEdit != null) {
				element.addPropertyWord(Property.PropertyClass, "Wt-timeedit");
				return;
			}
		}
			break;
		default:
			break;
		}
	}

	/**
	 * Returns a generic CSS class name for a disabled element.
	 * <p>
	 * 
	 * The CSS class Wt-disabled is applied to disabled classes.
	 */
	public String getDisabledClass() {
		return "Wt-disabled";
	}

	/**
	 * Returns a generic CSS class name for an active element.
	 * <p>
	 * 
	 * The CSS class Wt-selected is applied to active classes.
	 */
	public String getActiveClass() {
		return "Wt-selected";
	}

	public String utilityCssClass(int utilityCssClassRole) {
		switch (utilityCssClassRole) {
		case UtilityCssClassRole.ToolTipOuter:
			return "Wt-tooltip";
		default:
			return "";
		}
	}

	/**
	 * Returns whether the theme allows for an anchor to be styled as a button.
	 * <p>
	 * 
	 * Returns false.
	 */
	public boolean isCanStyleAnchorAsButton() {
		return false;
	}

	public void applyValidationStyle(WWidget widget,
			final WValidator.Result validation,
			EnumSet<ValidationStyleFlag> styles) {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/CssThemeValidate.js", wtjs1());
		app.loadJavaScript("js/CssThemeValidate.js", wtjs2());
		if (app.getEnvironment().hasAjax()) {
			StringBuilder js = new StringBuilder();
			js.append("Wt3_4_2.setValidationState(")
					.append(widget.getJsRef())
					.append(",")
					.append(validation.getState() == WValidator.State.Valid ? 1
							: 0)
					.append(",")
					.append(WString.toWString(validation.getMessage())
							.getJsStringLiteral()).append(",")
					.append(EnumUtils.valueOf(styles)).append(");");
			widget.doJavaScript(js.toString());
		} else {
			boolean validStyle = validation.getState() == WValidator.State.Valid
					&& !EnumUtils.mask(styles,
							ValidationStyleFlag.ValidationValidStyle).isEmpty();
			boolean invalidStyle = validation.getState() != WValidator.State.Valid
					&& !EnumUtils.mask(styles,
							ValidationStyleFlag.ValidationInvalidStyle)
							.isEmpty();
			widget.toggleStyleClass("Wt-valid", validStyle);
			widget.toggleStyleClass("Wt-invalid", invalidStyle);
		}
	}

	public boolean canBorderBoxElement(final DomElement element) {
		return true;
	}

	private String name_;

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"validate",
				"function(a){var b;b=a.options?a.options.item(a.selectedIndex).text:a.value;b=a.wtValidate.validate(b);this.setValidationState(a,b.valid,b.message,1)}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"setValidationState",
				"function(a,b,d,c){var e=b==1&&(c&2)!=0;c=b!=1&&(c&1)!=0;$(a).toggleClass(\"Wt-valid\",e).toggleClass(\"Wt-invalid\",c);if(typeof a.defaultTT===\"undefined\")a.defaultTT=a.getAttribute(\"title\")||\"\";b?a.setAttribute(\"title\",a.defaultTT):a.setAttribute(\"title\",d)}");
	}
}
