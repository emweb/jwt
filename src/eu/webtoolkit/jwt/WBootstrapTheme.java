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
 * This theme implements support for building a JWt web application that uses
 * Twitter Bootstrap as a theme for its (layout and) styling. The theme comes
 * with CSS from Bootstrap version 2.2.2 or version 3.1. Only the CSS components
 * of twitter bootstrap are used, but not the JavaScript (i.e. the functional
 * parts), since the functionality is already built-in to the widgets from the
 * library.
 * <p>
 * Using this theme, various widgets provided by the library are rendered using
 * markup that is compatible with Twitter Bootstrap. The bootstrap theme is also
 * extended with a proper (compatible) styling of widgets for which bootstrap
 * does not provide styling (table views, tree views, sliders, etc...).
 * <p>
 * By default, the theme will use CSS resources that are shipped together with
 * the JWt distribution, but since the Twitter Bootstrap CSS API is a popular
 * API for custom themes, you can easily replace the CSS with custom-built CSS
 * (by reimplementing {@link WBootstrapTheme#getStyleSheets() getStyleSheets()}
 * ).
 * <p>
 * Although this theme facilitates the use of Twitter Bootstrap with Wt, it is
 * still important to understand how Bootstrap expects markup to be, especially
 * related to layout using its grid system, for which we refer to the official
 * bootstrap documentation, see <a
 * href="http://getbootstrap.com">http://getbootstrap.com</a>
 * <p>
 * 
 * @see WApplication#setTheme(WTheme theme)
 */
public class WBootstrapTheme extends WTheme {
	private static Logger logger = LoggerFactory
			.getLogger(WBootstrapTheme.class);

	/**
	 * Enumeration to select a bootstrap version.
	 * <p>
	 * 
	 * @see WBootstrapTheme#setVersion(WBootstrapTheme.Version version)
	 */
	public enum Version {
		/**
		 * Bootstrap 2.
		 */
		Version2(2),
		/**
		 * Bootstrap 3.
		 */
		Version3(3);

		private int value;

		Version(int value) {
			this.value = value;
		}

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return value;
		}
	}

	/**
	 * Constructor.
	 */
	public WBootstrapTheme(WObject parent) {
		super(parent);
		this.version_ = WBootstrapTheme.Version.Version2;
		this.responsive_ = false;
		this.formControlStyle_ = true;
		WApplication app = WApplication.getInstance();
		if (app != null) {
			app.getBuiltinLocalizedStrings().useBuiltin(
					WtServlet.BootstrapTheme_xml);
		}
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WBootstrapTheme(WObject parent) this((WObject)null)}
	 */
	public WBootstrapTheme() {
		this((WObject) null);
	}

	/**
	 * <p>
	 * Enables responsive features.
	 * <p>
	 * Responsive features can be enabled only at application startup. For
	 * bootstrap 3, you need to use the progressive bootstrap feature of JWt
	 * (see see DOCREF<a class="el" href="overview.html#config_general">10.2
	 * General application settings (wt_config.xml)</a>) as it requires setting
	 * HTML meta flags.
	 * <p>
	 * Responsive features are disabled by default.
	 */
	public void setResponsive(boolean enabled) {
		this.responsive_ = enabled;
	}

	/**
	 * Returns whether responsive features are enabled.
	 * <p>
	 * 
	 * @see WBootstrapTheme#setResponsive(boolean enabled)
	 */
	public boolean isResponsive() {
		return this.responsive_;
	}

	/**
	 * Sets the bootstrap version.
	 * <p>
	 * The default bootstrap version is 2 (but this may change in the future and
	 * thus we recommend setting the version).
	 * <p>
	 * Since Twitter Bootstrap breaks its API with a major version change, the
	 * version has a big impact on how how the markup is done for various
	 * widgets.
	 * <p>
	 * Note that the two Bootstrap versions have a different license: Apache 2.0
	 * for Bootstrap version 2.2.2, and MIT for version 3.1. See these licenses
	 * for details.
	 */
	public void setVersion(WBootstrapTheme.Version version) {
		this.version_ = version;
		if (this.version_.getValue() >= WBootstrapTheme.Version.Version3
				.getValue()) {
			WApplication app = WApplication.getInstance();
			if (app != null) {
				app.getBuiltinLocalizedStrings().useBuiltin(
						WtServlet.Bootstrap3Theme_xml);
			}
		}
	}

	/**
	 * Returns the bootstrap version.
	 * <p>
	 * 
	 * @see WBootstrapTheme#setVersion(WBootstrapTheme.Version version)
	 */
	public WBootstrapTheme.Version getVersion() {
		return this.version_;
	}

	/**
	 * Enables form-control on all applicable form widgets.
	 * <p>
	 * This is relevant only for bootstrap 3.
	 * <p>
	 * By applying &quot;form-control&quot; on form widgets, they will become
	 * block level elements that take the size of the parent (which is in
	 * bootstrap&apos;s philosphy a grid layout).
	 * <p>
	 * The default value is <code>true</code>.
	 */
	public void setFormControlStyleEnabled(boolean enabled) {
		this.formControlStyle_ = enabled;
	}

	public String getName() {
		return "bootstrap";
	}

	public List<WCssStyleSheet> getStyleSheets() {
		List<WCssStyleSheet> result = new ArrayList<WCssStyleSheet>();
		String themeDir = this.getResourcesUrl();
		StringWriter themeVersionDir = new StringWriter();
		themeVersionDir.append(themeDir)
				.append(String.valueOf(this.version_.getValue())).append("/");
		result.add(new WCssStyleSheet(new WLink(themeVersionDir.toString()
				+ "bootstrap.css")));
		WApplication app = WApplication.getInstance();
		if (this.responsive_) {
			if (this.version_.getValue() < WBootstrapTheme.Version.Version3
					.getValue()) {
				result.add(new WCssStyleSheet(new WLink(themeVersionDir
						.toString() + "bootstrap-responsive.css")));
			} else {
				if (app != null) {
					WString v = app.metaHeader(MetaHeaderType.MetaName,
							"viewport");
					if ((v.length() == 0)) {
						app.addMetaHeader("viewport",
								"width=device-width, initial-scale=1");
					}
				}
			}
		}
		result.add(new WCssStyleSheet(new WLink(themeVersionDir.toString()
				+ "wt.css")));
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
			child.setStyleClass("Wt-chkbox");
			((WFormWidget) child).getLabel().addStyleClass("checkbox-inline");
			break;
		case WidgetThemeRole.MenuItemCloseRole: {
			WText txt = ((child) instanceof WText ? (WText) (child) : null);
			if (txt != null) {
				txt.setText("<button class='close'>&times;</button>");
			}
		}
			break;
		case WidgetThemeRole.DialogContent:
			if (this.version_ == WBootstrapTheme.Version.Version3) {
				child.addStyleClass("modal-content");
			}
			break;
		case WidgetThemeRole.DialogCoverRole:
			if (this.version_ == WBootstrapTheme.Version.Version3) {
				child.addStyleClass("modal-backdrop in");
			} else {
				child.addStyleClass("modal-backdrop Wt-bootstrap2");
			}
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
			child.toggleStyleClass("Wt-striped", view.hasAlternatingRowColors());
			break;
		}
		case WidgetThemeRole.DatePickerPopupRole:
			child.addStyleClass("Wt-datepicker");
			break;
		case WidgetThemeRole.TimePickerPopupRole:
			child.addStyleClass("Wt-timepicker");
			break;
		case WidgetThemeRole.PanelTitleBarRole:
			child.addStyleClass(this.getClassAccordionHeading());
			break;
		case WidgetThemeRole.PanelCollapseButtonRole:
		case WidgetThemeRole.PanelTitleRole:
			child.addStyleClass("accordion-toggle");
			break;
		case WidgetThemeRole.PanelBodyRole:
			child.addStyleClass(this.getClassAccordionInner());
			break;
		case WidgetThemeRole.InPlaceEditingRole:
			if (this.version_ == WBootstrapTheme.Version.Version2) {
				child.addStyleClass("input-append");
			} else {
				child.addStyleClass("input-group");
			}
			break;
		case WidgetThemeRole.NavCollapseRole:
			child.addStyleClass(this.getClassNavCollapse());
			break;
		case WidgetThemeRole.NavBrandRole:
			child.addStyleClass(this.getClassBrand());
			break;
		case WidgetThemeRole.NavbarSearchRole:
			child.addStyleClass(this.getClassNavbarSearch());
			break;
		case WidgetThemeRole.NavbarAlignLeftRole:
			child.addStyleClass(this.getClassNavbarLeft());
			break;
		case WidgetThemeRole.NavbarAlignRightRole:
			child.addStyleClass(this.getClassNavbarRight());
			break;
		case WidgetThemeRole.NavbarMenuRole:
			child.addStyleClass(this.getClassNavbarMenu());
			break;
		case WidgetThemeRole.NavbarBtn:
			child.addStyleClass(this.getClassNavbarBtn());
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
				WDialog dialog = ((widget) instanceof WDialog ? (WDialog) (widget)
						: null);
				if (!(dialog != null)) {
					element.addPropertyWord(Property.PropertyClass,
							"dropdown-menu");
				}
			}
		}
		switch (element.getType()) {
		case DomElement_A: {
			if (creating
					&& ((widget) instanceof WPushButton ? (WPushButton) (widget)
							: null) != null) {
				element.addPropertyWord(Property.PropertyClass,
						this.classBtn(widget));
			}
			WPushButton btn = ((widget) instanceof WPushButton ? (WPushButton) (widget)
					: null);
			if (creating && btn != null && btn.isDefault()) {
				element.addPropertyWord(Property.PropertyClass, "btn-primary");
			}
			if (element.getProperty(Property.PropertyClass).indexOf(
					"dropdown-toggle") != -1) {
				WMenuItem item = ((widget.getParent()) instanceof WMenuItem ? (WMenuItem) (widget
						.getParent()) : null);
				if (!(((item.getParentMenu()) instanceof WPopupMenu ? (WPopupMenu) (item
						.getParentMenu()) : null) != null)) {
					DomElement b = DomElement
							.createNew(DomElementType.DomElement_B);
					b.setProperty(Property.PropertyClass, "caret");
					element.addChild(b);
				}
			}
			break;
		}
		case DomElement_BUTTON: {
			if (creating && !widget.hasStyleClass("list-group-item")) {
				element.addPropertyWord(Property.PropertyClass,
						this.classBtn(widget));
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
					logger.error(new StringWriter()
							.append("Cannot use WPushButton::setLink() after the button has been rendered with WBootstrapTheme")
							.toString());
				}
			}
			break;
		}
		case DomElement_DIV: {
			WDialog dialog = ((widget) instanceof WDialog ? (WDialog) (widget)
					: null);
			if (dialog != null) {
				if (this.version_ == WBootstrapTheme.Version.Version2) {
					element.addPropertyWord(Property.PropertyClass, "modal");
				} else {
					element.addPropertyWord(Property.PropertyClass,
							"modal-dialog Wt-dialog");
				}
				return;
			}
			WPanel panel = ((widget) instanceof WPanel ? (WPanel) (widget)
					: null);
			if (panel != null) {
				element.addPropertyWord(Property.PropertyClass,
						this.getClassAccordionGroup());
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
					element.addPropertyWord(Property.PropertyClass,
							this.getClassBar());
					break;
				case ElementThemeRole.ProgressBarLabelRole:
					element.addPropertyWord(Property.PropertyClass, "bar-label");
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
				element.addPropertyWord(Property.PropertyClass, "form-inline");
				return;
			}
			WNavigationBar navBar = ((widget) instanceof WNavigationBar ? (WNavigationBar) (widget)
					: null);
			if (navBar != null) {
				element.addPropertyWord(Property.PropertyClass,
						this.getClassNavbar());
				return;
			}
		}
			break;
		case DomElement_LABEL: {
			if (elementRole == 1) {
				if (this.version_ == WBootstrapTheme.Version.Version3) {
					WCheckBox cb = ((widget) instanceof WCheckBox ? (WCheckBox) (widget)
							: null);
					WRadioButton rb = null;
					if (cb != null) {
						element.addPropertyWord(Property.PropertyClass, widget
								.isInline() ? "checkbox-inline" : "checkbox");
					} else {
						rb = ((widget) instanceof WRadioButton ? (WRadioButton) (widget)
								: null);
						if (rb != null) {
							element.addPropertyWord(Property.PropertyClass,
									widget.isInline() ? "radio-inline"
											: "radio");
						}
					}
					if ((cb != null || rb != null) && !widget.isInline()) {
						element.setType(DomElementType.DomElement_DIV);
					}
				} else {
					WCheckBox cb = ((widget) instanceof WCheckBox ? (WCheckBox) (widget)
							: null);
					WRadioButton rb = null;
					if (cb != null) {
						element.addPropertyWord(Property.PropertyClass,
								"checkbox");
					} else {
						rb = ((widget) instanceof WRadioButton ? (WRadioButton) (widget)
								: null);
						if (rb != null) {
							element.addPropertyWord(Property.PropertyClass,
									"radio");
						}
					}
					if ((cb != null || rb != null) && widget.isInline()) {
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
							.getParentMenu()) : null) != null) {
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
			if (this.version_ == WBootstrapTheme.Version.Version3
					&& this.formControlStyle_) {
				WAbstractToggleButton tb = ((widget) instanceof WAbstractToggleButton ? (WAbstractToggleButton) (widget)
						: null);
				if (!(tb != null)) {
					element.addPropertyWord(Property.PropertyClass,
							"form-control");
				}
			}
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
		case DomElement_TEXTAREA:
		case DomElement_SELECT:
			if (this.version_ == WBootstrapTheme.Version.Version3
					&& this.formControlStyle_) {
				element.addPropertyWord(Property.PropertyClass, "form-control");
			}
			break;
		case DomElement_UL: {
			WPopupMenu popupMenu = ((widget) instanceof WPopupMenu ? (WPopupMenu) (widget)
					: null);
			if (popupMenu != null) {
				element.addPropertyWord(Property.PropertyClass, "dropdown-menu");
				if (popupMenu.getParentItem() != null
						&& ((popupMenu.getParentItem().getParentMenu()) instanceof WPopupMenu ? (WPopupMenu) (popupMenu
								.getParentItem().getParentMenu()) : null) != null) {
					element.addPropertyWord(Property.PropertyClass, "submenu");
				}
			} else {
				WMenu menu = ((widget) instanceof WMenu ? (WMenu) (widget)
						: null);
				if (menu != null) {
					element.addPropertyWord(Property.PropertyClass, "nav");
					WTabWidget tabs = ((menu.getParent().getParent()) instanceof WTabWidget ? (WTabWidget) (menu
							.getParent().getParent()) : null);
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
			} else {
				WDatePicker picker = ((widget) instanceof WDatePicker ? (WDatePicker) (widget)
						: null);
				if (picker != null) {
					element.addPropertyWord(Property.PropertyClass,
							"Wt-datepicker");
				}
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

	public String utilityCssClass(int utilityCssClassRole) {
		switch (utilityCssClassRole) {
		case UtilityCssClassRole.ToolTipInner:
			return "tooltip-inner";
		case UtilityCssClassRole.ToolTipOuter:
			return "tooltip fade top in";
		default:
			return "";
		}
	}

	public boolean isCanStyleAnchorAsButton() {
		return true;
	}

	public void applyValidationStyle(WWidget widget,
			final WValidator.Result validation,
			EnumSet<ValidationStyleFlag> styles) {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/BootstrapValidate.js", wtjs1());
		app.loadJavaScript("js/BootstrapValidate.js", wtjs2());
		if (app.getEnvironment().hasAjax()) {
			StringBuilder js = new StringBuilder();
			js.append("Wt3_3_8.setValidationState(")
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
		return element.getType() != DomElementType.DomElement_INPUT;
	}

	private WBootstrapTheme.Version version_;
	private boolean responsive_;
	private boolean formControlStyle_;

	private String classBtn(WWidget widget) {
		WPushButton button = ((widget) instanceof WPushButton ? (WPushButton) (widget)
				: null);
		return this.version_ == WBootstrapTheme.Version.Version2
				|| this.hasButtonStyleClass(widget) || button != null
				&& button.isDefault() ? "btn" : "btn btn-default";
	}

	private String getClassBar() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "bar"
				: "progress-bar";
	}

	private String getClassAccordion() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "accordion"
				: "panel-group";
	}

	private String getClassAccordionGroup() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "accordion-group"
				: "panel panel-default";
	}

	private String getClassAccordionHeading() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "accordion-heading"
				: "panel-heading";
	}

	private String getClassAccordionBody() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "accordion-body"
				: "panel-collapse";
	}

	private String getClassAccordionInner() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "accordion-inner"
				: "panel-body";
	}

	private String getClassNavCollapse() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "nav-collapse"
				: "navbar-collapse";
	}

	private String getClassNavbar() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "navbar"
				: "navbar navbar-default";
	}

	private String getClassBrand() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "brand"
				: "navbar-brand";
	}

	private String getClassNavbarSearch() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "search-query"
				: "navbar-search";
	}

	private String getClassNavbarLeft() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "pull-left"
				: "navbar-left";
	}

	private String getClassNavbarRight() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "pull-right"
				: "navbar-right";
	}

	private String getClassNavbarMenu() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "navbar-nav"
				: "navbar-nav";
	}

	private String getClassNavbarBtn() {
		return this.version_ == WBootstrapTheme.Version.Version2 ? "btn-navbar"
				: "navbar-toggle";
	}

	private boolean hasButtonStyleClass(WWidget widget) {
		int size = btnClasses.length;
		for (int i = 0; i < size; ++i) {
			if (widget.hasStyleClass(btnClasses[i])) {
				return true;
			}
		}
		return false;
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"validate",
				"function(a){var b;b=a.options?a.options.item(a.selectedIndex)==null?\"\":a.options.item(a.selectedIndex).text:a.value;b=a.wtValidate.validate(b);this.setValidationState(a,b.valid,b.message,1)}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"setValidationState",
				"function(a,b,h,e){var i=b==1&&(e&2)!=0;e=b!=1&&(e&1)!=0;var d=$(a);d.toggleClass(\"Wt-valid\",i).toggleClass(\"Wt-invalid\",e);var c,f,g;c=d.closest(\".control-group\");if(c.length>0){f=\"success\";g=\"error\"}else{c=d.closest(\".form-group\");if(c.length>0){f=\"has-success\";g=\"has-error\"}}if(c.length>0){if(d=c.find(\".Wt-validation-message\"))b?d.text(a.defaultTT):d.text(h);c.toggleClass(f,i).toggleClass(g,e)}if(typeof a.defaultTT===\"undefined\")a.defaultTT= a.getAttribute(\"title\")||\"\";b?a.setAttribute(\"title\",a.defaultTT):a.setAttribute(\"title\",h)}");
	}

	private static String[] btnClasses = { "btn-default", "btn-primary",
			"btn-success", "btn-info", "btn-warning", "btn-danger", "btn-link" };
}
