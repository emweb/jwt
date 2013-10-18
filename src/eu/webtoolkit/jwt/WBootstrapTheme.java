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
 * Theme based on the Twitter Bootstrap CSS framework.
 * <p>
 * 
 * By default, the theme will use CSS resources that are shipped together with
 * the JWt distribution.
 * <p>
 * 
 * @see WApplication#setTheme(WTheme theme)
 */
public class WBootstrapTheme extends WTheme {
	private static Logger logger = LoggerFactory
			.getLogger(WBootstrapTheme.class);

	/**
	 * Constructor.
	 */
	public WBootstrapTheme(WObject parent) {
		super(parent);
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WBootstrapTheme(WObject parent) this((WObject)null)}
	 */
	public WBootstrapTheme() {
		this((WObject) null);
	}

	public String getName() {
		return "bootstrap";
	}

	public List<WCssStyleSheet> getStyleSheets() {
		List<WCssStyleSheet> result = new ArrayList<WCssStyleSheet>();
		String themeDir = this.getResourcesUrl();
		result.add(new WCssStyleSheet(new WLink(themeDir + "bootstrap.css")));
		result.add(new WCssStyleSheet(new WLink(themeDir
				+ "bootstrap-responsive.css")));
		result.add(new WCssStyleSheet(new WLink(themeDir + "wt.css")));
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
		case WidgetThemeRole.MenuItemCloseRole: {
			WText txt = ((child) instanceof WText ? (WText) (child) : null);
			if (txt != null) {
				txt.setText("<button class='close'>&times;</button>");
			}
		}
			break;
		case WidgetThemeRole.DialogCoverRole:
			child.addStyleClass("modal-backdrop");
			break;
		case WidgetThemeRole.DialogTitleBarRole:
			child.addStyleClass("modal-header");
			break;
		case WidgetThemeRole.DialogBodyRole:
			child.addStyleClass("modal-body");
			break;
		case WidgetThemeRole.DialogFooterRole:
			child.addStyleClass("modal-footer");
			break;
		case WidgetThemeRole.DialogCloseIconRole: {
			child.addStyleClass("close");
			WText t = ((child) instanceof WText ? (WText) (child) : null);
			t.setText("&times;");
			break;
		}
		case WidgetThemeRole.TableViewRowContainerRole: {
			WAbstractItemView view = ((widget) instanceof WAbstractItemView ? (WAbstractItemView) (widget)
					: null);
			child
					.toggleStyleClass("Wt-striped", view
							.hasAlternatingRowColors());
			break;
		}
		case WidgetThemeRole.DatePickerPopupRole:
			child.addStyleClass("Wt-datepicker");
			break;
		case WidgetThemeRole.PanelTitleBarRole:
			child.addStyleClass("accordion-heading");
			break;
		case WidgetThemeRole.PanelCollapseButtonRole:
		case WidgetThemeRole.PanelTitleRole:
			child.addStyleClass("accordion-toggle");
			break;
		case WidgetThemeRole.PanelBodyRole:
			child.addStyleClass("accordion-inner");
			break;
		case WidgetThemeRole.AuthWidgets:
			WApplication app = WApplication.getInstance();
			app.getBuiltinLocalizedStrings().useBuiltin(
					WtServlet.AuthBootstrapTheme_xml);
			break;
		}
	}

	public void apply(WWidget widget, DomElement element, int elementRole) {
		boolean creating = element.getMode() == DomElement.Mode.ModeCreate;
		{
			WPopupWidget popup = ((widget) instanceof WPopupWidget ? (WPopupWidget) (widget)
					: null);
			if (popup != null) {
				WDialog dialog = ((widget) instanceof WDialog ? (WDialog) (widget)
						: null);
				if (!(dialog != null)) {
					element.addPropertyWord(Property.PropertyClass,
							"dropdown-menu");
				}
			}
		}
		switch (element.getType()) {
		case DomElement_A:
			if (creating
					&& ((widget) instanceof WPushButton ? (WPushButton) (widget)
							: null) != null) {
				element.addPropertyWord(Property.PropertyClass, "btn");
			}
			if (element.getProperty(Property.PropertyClass).indexOf(
					"dropdown-toggle") != -1) {
				WMenuItem item = ((widget.getParent()) instanceof WMenuItem ? (WMenuItem) (widget
						.getParent())
						: null);
				if (!(((item.getParentMenu()) instanceof WPopupMenu ? (WPopupMenu) (item
						.getParentMenu())
						: null) != null)) {
					DomElement b = DomElement
							.createNew(DomElementType.DomElement_B);
					b.setProperty(Property.PropertyClass, "caret");
					element.addChild(b);
				}
			}
			break;
		case DomElement_BUTTON: {
			if (creating) {
				element.addPropertyWord(Property.PropertyClass, "btn");
			}
			WPushButton button = ((widget) instanceof WPushButton ? (WPushButton) (widget)
					: null);
			if (button != null) {
				if (creating && button.isDefault()) {
					element.addPropertyWord(Property.PropertyClass,
							"btn-primary");
				}
				if (button.getMenu() != null
						&& element.getProperties().get(
								Property.PropertyInnerHTML) != null) {
					element.addPropertyWord(Property.PropertyInnerHTML,
							"<span class=\"caret\"></span>");
				}
				if (creating && !(button.getText().length() == 0)) {
					element.addPropertyWord(Property.PropertyClass,
							"with-label");
				}
				if (!button.getLink().isNull()) {
					logger
							.error(new StringWriter()
									.append(
											"Cannot use WPushButton::setLink() after the button has been rendered with WBootstrapTheme")
									.toString());
				}
			}
			break;
		}
		case DomElement_DIV: {
			WDialog dialog = ((widget) instanceof WDialog ? (WDialog) (widget)
					: null);
			if (dialog != null) {
				element.addPropertyWord(Property.PropertyClass, "modal");
				return;
			}
			WPanel panel = ((widget) instanceof WPanel ? (WPanel) (widget)
					: null);
			if (panel != null) {
				element.addPropertyWord(Property.PropertyClass,
						"accordion-group");
				return;
			}
			WProgressBar bar = ((widget) instanceof WProgressBar ? (WProgressBar) (widget)
					: null);
			if (bar != null) {
				switch (elementRole) {
				case ElementThemeRole.MainElementThemeRole:
					element.addPropertyWord(Property.PropertyClass, "progress");
					break;
				case ElementThemeRole.ProgressBarBarRole:
					element.addPropertyWord(Property.PropertyClass, "bar");
					break;
				case ElementThemeRole.ProgressBarLabelRole:
					element
							.addPropertyWord(Property.PropertyClass,
									"bar-label");
				}
				return;
			}
			WGoogleMap map = ((widget) instanceof WGoogleMap ? (WGoogleMap) (widget)
					: null);
			if (map != null) {
				element.addPropertyWord(Property.PropertyClass, "Wt-googlemap");
				return;
			}
			WAbstractItemView itemView = ((widget) instanceof WAbstractItemView ? (WAbstractItemView) (widget)
					: null);
			if (itemView != null) {
				element.addPropertyWord(Property.PropertyClass,
						"form-horizontal");
				return;
			}
		}
			break;
		case DomElement_LABEL: {
			WCheckBox cb = ((widget) instanceof WCheckBox ? (WCheckBox) (widget)
					: null);
			if (cb != null) {
				element.addPropertyWord(Property.PropertyClass, "checkbox");
				if (cb.isInline()) {
					element.addPropertyWord(Property.PropertyClass, "inline");
				}
			} else {
				WRadioButton rb = ((widget) instanceof WRadioButton ? (WRadioButton) (widget)
						: null);
				if (rb != null) {
					element.addPropertyWord(Property.PropertyClass, "radio");
					if (rb.isInline()) {
						element.addPropertyWord(Property.PropertyClass,
								"inline");
					}
				}
			}
		}
			break;
		case DomElement_LI: {
			WMenuItem item = ((widget) instanceof WMenuItem ? (WMenuItem) (widget)
					: null);
			if (item != null) {
				if (item.isSeparator()) {
					element.addPropertyWord(Property.PropertyClass, "divider");
				}
				if (item.isSectionHeader()) {
					element.addPropertyWord(Property.PropertyClass,
							"nav-header");
				}
				if (item.getMenu() != null) {
					if (((item.getParentMenu()) instanceof WPopupMenu ? (WPopupMenu) (item
							.getParentMenu())
							: null) != null) {
						element.addPropertyWord(Property.PropertyClass,
								"dropdown-submenu");
					} else {
						element.addPropertyWord(Property.PropertyClass,
								"dropdown");
					}
				}
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
		case DomElement_UL: {
			WPopupMenu popupMenu = ((widget) instanceof WPopupMenu ? (WPopupMenu) (widget)
					: null);
			if (popupMenu != null) {
				element
						.addPropertyWord(Property.PropertyClass,
								"dropdown-menu");
				if (popupMenu.getParentItem() != null
						&& ((popupMenu.getParentItem().getParentMenu()) instanceof WPopupMenu ? (WPopupMenu) (popupMenu
								.getParentItem().getParentMenu())
								: null) != null) {
					element.addPropertyWord(Property.PropertyClass, "submenu");
				}
			} else {
				WMenu menu = ((widget) instanceof WMenu ? (WMenu) (widget)
						: null);
				if (menu != null) {
					element.addPropertyWord(Property.PropertyClass, "nav");
					WTabWidget tabs = ((menu.getParent().getParent()) instanceof WTabWidget ? (WTabWidget) (menu
							.getParent().getParent())
							: null);
					if (tabs != null) {
						element.addPropertyWord(Property.PropertyClass,
								"nav-tabs");
					}
				} else {
					WSuggestionPopup suggestions = ((widget) instanceof WSuggestionPopup ? (WSuggestionPopup) (widget)
							: null);
					if (suggestions != null) {
						element.addPropertyWord(Property.PropertyClass,
								"typeahead");
					}
				}
			}
		}
		case DomElement_SPAN: {
			WInPlaceEdit inPlaceEdit = ((widget) instanceof WInPlaceEdit ? (WInPlaceEdit) (widget)
					: null);
			if (inPlaceEdit != null) {
				element.addPropertyWord(Property.PropertyClass,
						"Wt-in-place-edit");
			}
		}
			break;
		default:
			break;
		}
	}

	public String getDisabledClass() {
		return "disabled";
	}

	public String getActiveClass() {
		return "active";
	}

	public boolean isCanStyleAnchorAsButton() {
		return true;
	}

	public void applyValidationStyle(WWidget widget,
			WValidator.Result validation, EnumSet<ValidationStyleFlag> styles) {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/BootstrapValidate.js", wtjs1());
		app.loadJavaScript("js/BootstrapValidate.js", wtjs2());
		if (app.getEnvironment().hasAjax()) {
			StringBuilder js = new StringBuilder();
			js.append("Wt3_3_1.setValidationState(").append(widget.getJsRef())
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

	public boolean canBorderBoxElement(DomElement element) {
		return element.getType() != DomElementType.DomElement_INPUT;
	}

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
				"function(a,b,f,c){var e=b==1&&(c&2)!=0;c=b!=1&&(c&1)!=0;var d=$(a);d.toggleClass(\"Wt-valid\",e).toggleClass(\"Wt-invalid\",c);(d=d.closest(\".control-group\"))&&d.toggleClass(\"success\",e).toggleClass(\"error\",c);if(typeof a.defaultTT===\"undefined\")a.defaultTT=a.getAttribute(\"title\")||\"\";b?a.setAttribute(\"title\",a.defaultTT):a.setAttribute(\"title\",f)}");
	}
}
