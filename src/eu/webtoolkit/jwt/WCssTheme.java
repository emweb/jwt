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
 * <p>
 * 
 * CSS-based theme support. This implements the classic JWt themes, which were
 * available before theme support was customized with the addition of the
 * {@link WTheme} class.
 * <p>
 * 
 * @see WApplication#setTheme(WTheme theme)
 */
public class WCssTheme extends WTheme {
	private static Logger logger = LoggerFactory.getLogger(WCssTheme.class);

	/**
	 * Constructor.
	 * <p>
	 * Creates a classic JWt theme (&quot;polished&quot; or
	 * &quot;default&quot;).
	 */
	public WCssTheme(String name, WObject parent) {
		super(parent);
		this.name_ = name;
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WCssTheme(String name, WObject parent) this(name,
	 * (WObject)null)}
	 */
	public WCssTheme(String name) {
		this(name, (WObject) null);
	}

	public String getName() {
		return this.name_;
	}

	public List<WCssStyleSheet> getStyleSheets() {
		List<WCssStyleSheet> result = new ArrayList<WCssStyleSheet>();
		if (this.name_.length() != 0) {
			String themeDir = WApplication.getResourcesUrl() + "themes/"
					+ this.name_;
			WApplication app = WApplication.getInstance();
			result.add(new WCssStyleSheet(new WLink(themeDir + "/wt.css")));
			if (app.getEnvironment().agentIsIE()) {
				result.add(new WCssStyleSheet(
						new WLink(themeDir + "/wt_ie.css")));
			}
			if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
				result.add(new WCssStyleSheet(new WLink(themeDir
						+ "/wt_ie6.css")));
			}
		}
		return result;
	}

	public void apply(WWidget widget, WWidget child, int widgetRole) {
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
			child.setStyleClass("Wt-dialogcover");
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
			child.addStyleClass("Wt-outset Wt-datepicker");
			break;
		case WidgetThemeRole.PanelTitleBarRole:
			child.addStyleClass("titlebar");
			break;
		case WidgetThemeRole.PanelBodyRole:
			child.addStyleClass("body");
			break;
		case WidgetThemeRole.AuthWidgets:
			WApplication app = WApplication.getInstance();
			app.useStyleSheet(new WLink(WApplication.getResourcesUrl()
					+ "form.css"));
			app.getBuiltinLocalizedStrings().useBuiltin(
					WtServlet.AuthCssTheme_xml);
			break;
		}
	}

	public void apply(WWidget widget, DomElement element, int elementRole) {
		switch (element.getType()) {
		case DomElement_BUTTON:
			element.addPropertyWord(Property.PropertyClass, "Wt-btn");
			break;
		case DomElement_UL:
			if (((widget) instanceof WPopupMenu ? (WPopupMenu) (widget) : null) != null) {
				element.addPropertyWord(Property.PropertyClass,
						"Wt-popupmenu Wt-outset");
			} else {
				WTabWidget tabs = ((widget.getParent().getParent()) instanceof WTabWidget ? (WTabWidget) (widget
						.getParent().getParent())
						: null);
				if (tabs != null) {
					element.addPropertyWord(Property.PropertyClass, "Wt-tabs");
				} else {
					WSuggestionPopup suggestions = ((widget) instanceof WSuggestionPopup ? (WSuggestionPopup) (widget)
							: null);
					if (suggestions != null) {
						element.addPropertyWord(Property.PropertyClass,
								"Wt-suggest Wt-outset");
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
			}
		}
			break;
		case DomElement_DIV: {
			WDialog dialog = ((widget) instanceof WDialog ? (WDialog) (widget)
					: null);
			if (dialog != null) {
				element.addPropertyWord(Property.PropertyClass,
						"Wt-dialog Wt-outset");
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
		}
			break;
		default:
			break;
		}
	}

	public String getDisabledClass() {
		return "Wt-disabled";
	}

	public String getActiveClass() {
		return "Wt-selected";
	}

	public boolean isCanStyleAnchorAsButton() {
		return false;
	}

	public void applyValidationStyle(WWidget widget,
			WValidator.Result validation, EnumSet<ValidationStyleFlag> styles) {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/CssThemeValidate.js", wtjs1());
		app.loadJavaScript("js/CssThemeValidate.js", wtjs2());
		if (app.getEnvironment().hasAjax()) {
			StringBuilder js = new StringBuilder();
			js.append("Wt3_3_0.setValidationState(").append(widget.getJsRef())
					.append(",").append(
							validation.getState() == WValidator.State.Valid ? 1
									: 0).append(",").append(
							WString.toWString(validation.getMessage())
									.getJsStringLiteral()).append(",").append(
							EnumUtils.valueOf(styles)).append(");");
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
				"function(a,b,d,c){var e=b==1&&(c&2)!=0;c=b!=1&&(c&1)!=0;$(a).toggleClass(\"Wt-valid\",e).toggleClass(\"Wt-invalid\",c);a.defaultTT=typeof a.defaultTT===\"undefined\"?a.getAttribute(\"title\")||\"\":\"\";b?a.setAttribute(\"title\",a.defaultTT):a.setAttribute(\"title\",d)}");
	}
}
