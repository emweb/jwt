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
import org.apache.commons.io.*;

/**
 * Represents an application instance for a single session.
 * <p>
 * 
 * Each user session of your application has a corresponding WApplication
 * instance. You need to create a new instance and return it as the result of
 * {@link WtServlet#createApplication(WEnvironment)}. The instance is the main
 * entry point to session information, and holds a reference to the
 * {@link WApplication#getRoot() getRoot()} of the widget tree.
 * <p>
 * The recipe for a JWt web application, which allocates new
 * {@link WApplication} instances for every user visiting the application is
 * thus:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * public class HelloServlet extends WtServlet {
 * 	public HelloServlet() {
 * 		super();
 * 	}
 * 
 * 	public WApplication createApplication(WEnvironment env) {
 * 		// In practice, you will specialize WApplication and simply
 * 		// return a new instance.
 * 		WApplication app = new WApplication(env);
 * 		app.getRoot().addWidget(new WText(&quot;Hello world.&quot;));
 * 		return app;
 * 	}
 * }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * Throughout the session, the instance is available through the static method
 * {@link WApplication#getInstance() getInstance()}, which uses thread-specific
 * storage to keep track of the current session. The application may be quited
 * either using the method {@link WApplication#quit() quit()}, or because of a
 * timeout after the user has closed the window, but not because the user does
 * not interact: keep-alive messages in the background will keep the session
 * around as long as the user has the page opened.
 * <p>
 * The WApplication object provides access to session-wide settings, including:
 * <p>
 * <ul>
 * <li>circumstancial information through {@link WApplication#getEnvironment()
 * getEnvironment()}, which gives details about the user, start-up arguments,
 * and user agent capabilities.</li>
 * <li>the application title with
 * {@link WApplication#setTitle(CharSequence title) setTitle()}.</li>
 * <li>inline and external style sheets using
 * {@link WApplication#getStyleSheet() getStyleSheet()} and
 * {@link WApplication#useStyleSheet(String uri) useStyleSheet()}.</li>
 * <li>inline and external JavaScript using
 * {@link WApplication#doJavaScript(String javascript, boolean afterLoaded)
 * doJavaScript()} and {@link WApplication#require(String uri, String symbol)
 * require()}.</li>
 * <li>the top-level widget in {@link WApplication#getRoot() getRoot()},
 * representing the entire browser window, or multiple top-level widgets using
 * {@link WApplication#bindWidget(WWidget widget, String domId) bindWidget()}
 * when deployed in WidgetSet mode to manage a number of widgets within a 3rd
 * party page.</li>
 * <li>definition of cookies using
 * {@link WApplication#setCookie(String name, String value, int maxAge, String domain, String path)
 * setCookie()} to persist information across sessions, which may be read using
 * {@link WEnvironment#getCookie(String cookieNname) WEnvironment#getCookie()}
 * in a future session.</li>
 * <li>management of the internal path (that enables browser history and
 * bookmarks) using
 * {@link WApplication#setInternalPath(String path, boolean emitChange)
 * setInternalPath()} and related methods.</li>
 * <li>support for server-initiated updates with
 * {@link WApplication#enableUpdates(boolean enabled) enableUpdates()}</li>
 * </ul>
 * <p>
 * <ul>
 * <li>localization information and message resources bundles, with
 * {@link WApplication#setLocale(Locale locale) setLocale()} and
 * {@link WApplication#setLocalizedStrings(WLocalizedStrings translator)
 * setLocalizedStrings()}</li>
 * </ul>
 */
public class WApplication extends WObject {
	/**
	 * Enumeration that indicates the method for dynamic (AJAX-alike) updates
	 * ((<b>deprecated</b>).
	 * <p>
	 * 
	 * @see WApplication#setAjaxMethod(WApplication.AjaxMethod method)
	 */
	public enum AjaxMethod {
		/**
		 * Using the XMLHttpRequest object (real AJAX).
		 */
		XMLHttpRequest,
		/**
		 * Using dynamic script tags (for cross-domain AJAX).
		 */
		DynamicScriptTag;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Creates a new application instance.
	 * <p>
	 * The <code>environment</code> provides information on the initial request,
	 * user agent, and deployment-related information.
	 */
	public WApplication(WEnvironment env) {
		super();
		this.requestTooLarge_ = new Signal1<Integer>();
		this.session_ = env.session_;
		this.title_ = new WString();
		this.closeMessage_ = new WString();
		this.titleChanged_ = false;
		this.closeMessageChanged_ = false;
		this.styleSheet_ = new WCssStyleSheet();
		this.localizedStrings_ = null;
		this.locale_ = new Locale("");
		this.oldInternalPath_ = "";
		this.newInternalPath_ = "";
		this.internalPathChanged_ = new Signal1<String>(this);
		this.serverPush_ = 0;
		this.modifiedWithoutEvent_ = false;
		this.javaScriptClass_ = "Wt";
		this.dialogCover_ = null;
		this.quited_ = false;
		this.onePixelGifUrl_ = "";
		this.internalPathsEnabled_ = false;
		this.exposedOnly_ = null;
		this.loadingIndicator_ = null;
		this.connected_ = true;
		this.htmlClass_ = "";
		this.bodyClass_ = "";
		this.bodyHtmlClassChanged_ = false;
		this.enableAjax_ = false;
		this.initialized_ = false;
		this.focusId_ = "";
		this.selectionStart_ = -1;
		this.selectionEnd_ = -1;
		this.layoutDirection_ = LayoutDirection.LeftToRight;
		this.scriptLibraries_ = new ArrayList<WApplication.ScriptLibrary>();
		this.scriptLibrariesAdded_ = 0;
		this.theme_ = "default";
		this.styleSheets_ = new ArrayList<WApplication.StyleSheet>();
		this.styleSheetsAdded_ = 0;
		this.metaHeaders_ = new ArrayList<WApplication.MetaHeader>();
		this.exposedSignals_ = new HashMap<String, WeakReference<AbstractEventSignal>>();
		this.exposedResources_ = new HashMap<String, WResource>();
		this.encodedObjects_ = new HashMap<String, WObject>();
		this.exposeSignals_ = true;
		this.afterLoadJavaScript_ = "";
		this.beforeLoadJavaScript_ = "";
		this.newBeforeLoadJavaScript_ = "";
		this.autoJavaScript_ = "";
		this.javaScriptLoaded_ = new HashSet<String>();
		this.autoJavaScriptChanged_ = false;
		this.showLoadingIndicator_ = new EventSignal("showload", this);
		this.hideLoadingIndicator_ = new EventSignal("hideload", this);
		this.unloaded_ = new JSignal(this, "Wt-unload");
		this.soundManager_ = null;
		this.showLoadJS = new JSlot();
		this.hideLoadJS = new JSlot();
		this.session_.setApplication(this);
		this.locale_ = this.getEnvironment().getLocale();
		this.newInternalPath_ = this.getEnvironment().getInternalPath();
		this.internalPathIsChanged_ = false;
		this.setLocalizedStrings((WLocalizedStrings) null);
		if (this.getEnvironment().agentIsIElt(9)) {
			this.addMetaHeader(MetaHeaderType.MetaHttpHeader,
					"X-UA-Compatible", "IE=7");
		}
		this.domRoot_ = new WContainerWidget();
		this.domRoot_.setStyleClass("Wt-domRoot");
		this.domRoot_.load();
		if (this.session_.getType() == EntryPointType.Application) {
			this.domRoot_.resize(WLength.Auto, new WLength(100,
					WLength.Unit.Percentage));
		}
		this.timerRoot_ = new WContainerWidget(this.domRoot_);
		this.timerRoot_.setId("Wt-timers");
		this.timerRoot_.resize(WLength.Auto, new WLength(0));
		this.timerRoot_.setPositionScheme(PositionScheme.Absolute);
		if (this.session_.getType() == EntryPointType.Application) {
			this.ajaxMethod_ = WApplication.AjaxMethod.XMLHttpRequest;
			this.domRoot2_ = null;
			this.widgetRoot_ = new WContainerWidget(this.domRoot_);
			;
			this.widgetRoot_.resize(WLength.Auto, new WLength(100,
					WLength.Unit.Percentage));
		} else {
			this.ajaxMethod_ = WApplication.AjaxMethod.DynamicScriptTag;
			this.domRoot2_ = new WContainerWidget();
			this.domRoot2_.load();
			this.widgetRoot_ = null;
		}
		this.styleSheet_.addRule("table",
				"border-collapse: collapse; border: 0px");
		this.styleSheet_.addRule("div, td, img",
				"margin: 0px; padding: 0px; border: 0px");
		this.styleSheet_.addRule("td", "vertical-align: top;");
		this.styleSheet_.addRule(".Wt-ltr td", "text-align: left;");
		this.styleSheet_.addRule(".Wt-rtl td", "text-align: right;");
		this.styleSheet_.addRule("button", "white-space: nowrap");
		this.styleSheet_.addRule("video", "display: block");
		if (this.getEnvironment().getContentType() == WEnvironment.ContentType.XHTML1) {
			this.styleSheet_.addRule("button", "display: inline");
		}
		if (this.getEnvironment().agentIsGecko()) {
			this.styleSheet_.addRule("html", "overflow: auto;");
		}
		this.styleSheet_.addRule("iframe.Wt-resource",
				"width: 0px; height: 0px; border: 0px;");
		if (this.getEnvironment().agentIsIElt(9)) {
			this.styleSheet_
					.addRule(
							"iframe.Wt-shim",
							"position: absolute; top: -1px; left: -1px; z-index: -1;opacity: 0; filter: alpha(opacity=0);border: none; margin: 0; padding: 0;");
		}
		this.styleSheet_
				.addRule(
						".Wt-wrap",
						"border: 0px;margin: 0px;padding: 0px;font-size: inherit; pointer: hand; cursor: pointer; cursor: hand;background: transparent;text-decoration: none;color: inherit;");
		this.styleSheet_.addRule(".Wt-ltr .Wt-wrap", "text-align: left;");
		this.styleSheet_.addRule(".Wt-rtl .Wt-wrap", "text-align: right;");
		if (this.getEnvironment().agentIsIE()) {
			this.styleSheet_.addRule(".Wt-wrap", "margin: -1px 0px -3px;");
		}
		this.styleSheet_.addRule(".Wt-invalid", "background-color: #f79a9a;");
		this.styleSheet_.addRule("span.Wt-disabled", "color: gray;");
		this.styleSheet_.addRule("fieldset.Wt-disabled legend", "color: gray;");
		this.styleSheet_
				.addRule(".unselectable",
						"-moz-user-select:-moz-none;-khtml-user-select: none;user-select: none;");
		this.styleSheet_
				.addRule(".selectable",
						"-moz-user-select: text;-khtml-user-select: normal;user-select: text;");
		this.styleSheet_
				.addRule(".Wt-sbspacer",
						"float: right; width: 16px; height: 1px;border: 0px; display: none;");
		this.styleSheet_.addRule(".Wt-domRoot", "position: relative;");
		this.styleSheet_
				.addRule(
						"body.Wt-layout",
						""
								+ "height: 100%; width: 100%;margin: 0px; padding: 0px; border: none;"
								+ (this.getEnvironment().hasJavaScript() ? "overflow:hidden"
										: ""));
		this.styleSheet_
				.addRule(
						"html.Wt-layout",
						""
								+ "height: 100%; width: 100%;margin: 0px; padding: 0px; border: none;"
								+ (this.getEnvironment().hasJavaScript() ? "overflow:hidden"
										: ""));
		if (this.getEnvironment().agentIsOpera()) {
			if (this.getEnvironment().getUserAgent().indexOf("Mac OS X") != -1) {
				this.styleSheet_.addRule("img.Wt-indeterminate",
						"margin: 4px 1px -3px 2px;");
			} else {
				this.styleSheet_.addRule("img.Wt-indeterminate",
						"margin: 4px 2px -3px 0px;");
			}
		} else {
			if (this.getEnvironment().getUserAgent().indexOf("Mac OS X") != -1) {
				this.styleSheet_.addRule("img.Wt-indeterminate",
						"margin: 4px 3px 0px 4px;");
			} else {
				this.styleSheet_.addRule("img.Wt-indeterminate",
						"margin: 3px 3px 0px 4px;");
			}
		}
		this.setLoadingIndicator(new WDefaultLoadingIndicator());
		this.unloaded_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WApplication.this.unload();
			}
		});
	}

	/**
	 * Returns the current application instance.
	 * <p>
	 * This method uses thread-specific storage to fetch the current session.
	 */
	public static WApplication getInstance() {
		WebSession session = WebSession.getInstance();
		return session != null ? session.getApp() : null;
	}

	/**
	 * Returns the environment information.
	 * <p>
	 * This method returns the environment object that was used when
	 * constructing the application. The environment provides information on the
	 * initial request, user agent, and deployment-related information.
	 * <p>
	 * 
	 * @see WApplication#getUrl()
	 * @see WApplication#getSessionId()
	 */
	public WEnvironment getEnvironment() {
		return this.session_.getEnv();
	}

	/**
	 * Returns the root container.
	 * <p>
	 * This is the top-level widget container of the application, and
	 * corresponds to entire browser window. The user interface of your
	 * application is represented by the content of this container.
	 * <p>
	 * The {@link WApplication#getRoot() getRoot()} widget is only defined when
	 * the application manages the entire window. When deployed as a
	 * {@link EntryPointType#WidgetSet WidgetSet} application, there is no
	 * root() container, and <code>null</code> is returned. Instead, use
	 * {@link WApplication#bindWidget(WWidget widget, String domId)
	 * bindWidget()} to bind one or more root widgets to existing HTML
	 * &lt;div&gt; (or other) elements on the page.
	 */
	public WContainerWidget getRoot() {
		return this.widgetRoot_;
	}

	/**
	 * Returns a reference to the inline style sheet.
	 * <p>
	 * Widgets may allow configuration of their look and feel through style
	 * classes. These may be defined in this inline stylesheet, or in external
	 * style sheets.
	 * <p>
	 * It is usually preferable to use external stylesheets (and consider more
	 * accessible). Still, the internal stylesheet has as benefit that style
	 * rules may be dynamically updated, and it is easier to manage
	 * logistically.
	 * <p>
	 * 
	 * @see WApplication#useStyleSheet(String uri)
	 * @see WWidget#setStyleClass(String styleClass)
	 */
	public WCssStyleSheet getStyleSheet() {
		return this.styleSheet_;
	}

	/**
	 * Adds an external style sheet.
	 * <p>
	 * Widgets may allow configuration of their look and feel through style
	 * classes. These may be defined in an inline stylesheet, or in external
	 * style sheets.
	 * <p>
	 * The <code>url</code> indicates a relative or absolute URL to the
	 * stylesheet.
	 * <p>
	 * External stylesheets are inserted after the internal style sheet, and can
	 * therefore override default styles set by widgets in the internal style
	 * sheet.
	 * <p>
	 * 
	 * @see WApplication#getStyleSheet()
	 * @see WWidget#setStyleClass(String styleClass)
	 */
	public void useStyleSheet(String uri) {
		for (int i = 0; i < this.styleSheets_.size(); ++i) {
			if (this.styleSheets_.get(i).uri.equals(uri)) {
				return;
			}
		}
		this.styleSheets_.add(new WApplication.StyleSheet(uri, ""));
		++this.styleSheetsAdded_;
	}

	/**
	 * Adds an external style sheet, constrained with conditions.
	 * <p>
	 * If not empty, <code>condition</code> is a string that is used to apply
	 * the stylesheet to specific versions of IE. Only a limited subset of the
	 * IE conditional comments syntax is supported (since these are in fact
	 * interpreted server-side instead of client-side). Examples are:
	 * <p>
	 * <ul>
	 * <li>&quot;IE gte 6&quot;: only for IE version 6 or later.</li>
	 * <li>&quot;!IE gte 6&quot;: only for IE versions prior to IE6.</li>
	 * <li>&quot;IE lte 7&quot;: only for IE versions prior to IE7.</li>
	 * </ul>
	 * <p>
	 * The <code>media</code> indicates the CSS media to which this stylesheet
	 * applies. This may be a comma separated list of media. The default value
	 * is &quot;all&quot; indicating all media.
	 * <p>
	 * The <code>url</code> indicates a relative or absolute URL to the
	 * stylesheet.
	 * <p>
	 * 
	 * @see WApplication#useStyleSheet(String uri)
	 */
	public void useStyleSheet(String uri, String condition, String media) {
		boolean display = true;
		if (condition.length() != 0) {
			display = false;
			if (this.getEnvironment().agentIsIE()) {
				int thisVersion = 4;
				switch (this.getEnvironment().getAgent()) {
				case IEMobile:
					thisVersion = 5;
					break;
				case IE6:
					thisVersion = 6;
					break;
				case IE7:
					thisVersion = 7;
					break;
				case IE8:
					thisVersion = 8;
					break;
				default:
					thisVersion = 9;
					break;
				}
				final int lte = 0;
				final int lt = 1;
				final int eq = 2;
				final int gt = 3;
				final int gte = 4;
				int cond = eq;
				boolean invert = false;
				String r = condition;
				while (r.length() != 0) {
					if (r.length() >= 3 && r.substring(0, 0 + 3).equals("IE ")) {
						r = r.substring(3);
					} else {
						if (r.charAt(0) == '!') {
							r = r.substring(1);
							invert = !invert;
						} else {
							if (r.length() >= 4
									&& r.substring(0, 0 + 4).equals("lte ")) {
								r = r.substring(4);
								cond = lte;
							} else {
								if (r.length() >= 3
										&& r.substring(0, 0 + 3).equals("lt ")) {
									r = r.substring(3);
									cond = lt;
								} else {
									if (r.length() >= 3
											&& r.substring(0, 0 + 3).equals(
													"gt ")) {
										r = r.substring(3);
										cond = gt;
									} else {
										if (r.length() >= 4
												&& r.substring(0, 0 + 4)
														.equals("gte ")) {
											r = r.substring(4);
											cond = gte;
										} else {
											try {
												int version = Integer
														.parseInt(r);
												switch (cond) {
												case eq:
													display = thisVersion == version;
													break;
												case lte:
													display = thisVersion <= version;
													break;
												case lt:
													display = thisVersion < version;
													break;
												case gte:
													display = thisVersion >= version;
													break;
												case gt:
													display = thisVersion > version;
													break;
												}
												if (invert) {
													display = !display;
												}
											} catch (RuntimeException e) {
												this
														.log("error")
														.append(
																"Could not parse condition: '")
														.append(condition)
														.append("'");
											}
											r = "";
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if (display) {
			this.styleSheets_.add(new WApplication.StyleSheet(uri, media));
			++this.styleSheetsAdded_;
		}
	}

	/**
	 * Adds an external style sheet, constrained with conditions.
	 * <p>
	 * Calls {@link #useStyleSheet(String uri, String condition, String media)
	 * useStyleSheet(uri, condition, "all")}
	 */
	public final void useStyleSheet(String uri, String condition) {
		useStyleSheet(uri, condition, "all");
	}

	/**
	 * Sets the theme.
	 * <p>
	 * The theme provides the look and feel of several built-in widgets, using
	 * CSS style rules. Rules for each theme are defined in the
	 * <code>resources/themes/</code><i>theme</i><code>/</code> folder.
	 * <p>
	 * The default theme is &quot;default&quot;. When setting &quot;&quot;, the
	 * external style sheets related to the theme are not loaded.
	 */
	public void setCssTheme(String theme) {
		this.theme_ = theme;
	}

	/**
	 * Returns the theme.
	 * <p>
	 * 
	 * @see WApplication#setCssTheme(String theme)
	 */
	public String getCssTheme() {
		return this.theme_;
	}

	/**
	 * Sets the layout direction.
	 * <p>
	 * The default direction is LeftToRight.
	 * <p>
	 * This sets the language text direction, which by itself sets the default
	 * text alignment and reverse the column orders of &lt;table&gt; elements.
	 * <p>
	 * In addition, JWt will take this setting into account in
	 * {@link WTableView} and {@link WTreeView} (so that columns are reverted),
	 * and swap the behaviour of {@link WWidget#setFloatSide(Side s)
	 * WWidget#setFloatSide()} and
	 * {@link WWidget#setOffsets(WLength offset, EnumSet sides)
	 * WWidget#setOffsets()} for RightToLeft languages. Note that CSS settings
	 * themselves are not affected by this setting, and thus for example
	 * <code>&quot;float: right&quot;</code> will move a box to the right,
	 * irrespective of the layout direction.
	 * <p>
	 * The library sets <code>&quot;Wt-ltr&quot;</code> or
	 * <code>&quot;Wt-rtl&quot;</code> as style classes for the document body.
	 * You may use this if to override certain style rules for a Right-to-Left
	 * document.
	 * <p>
	 * For example: <blockquote>
	 * 
	 * <pre>
	 * body.Wt-ltr .sidebar { float: right; }
	 *    body.Wt-rtl .sidebar { float: left; }
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The layout direction can be set only at application
	 * startup and does not have the effect of rerendering the entire UI. </i>
	 * </p>
	 */
	public void setLayoutDirection(LayoutDirection direction) {
		if (direction != this.layoutDirection_) {
			this.layoutDirection_ = direction;
			this.bodyHtmlClassChanged_ = true;
		}
	}

	/**
	 * Returns the layout direction.
	 * <p>
	 * 
	 * @see WApplication#setLayoutDirection(LayoutDirection direction)
	 */
	public LayoutDirection getLayoutDirection() {
		return this.layoutDirection_;
	}

	/**
	 * Sets a style class to the entire page &lt;body&gt;.
	 * <p>
	 * 
	 * @see WApplication#setHtmlClass(String styleClass)
	 */
	public void setBodyClass(String styleClass) {
		this.bodyClass_ = styleClass;
		this.bodyHtmlClassChanged_ = true;
	}

	/**
	 * Returns the style class set for the entire page &lt;body&gt;.
	 * <p>
	 * 
	 * @see WApplication#setBodyClass(String styleClass)
	 */
	public String getBodyClass() {
		return this.bodyClass_;
	}

	/**
	 * Sets a style class to the entire page &lt;html&gt;.
	 * <p>
	 * 
	 * @see WApplication#setBodyClass(String styleClass)
	 */
	public void setHtmlClass(String styleClass) {
		this.htmlClass_ = styleClass;
		this.bodyHtmlClassChanged_ = true;
	}

	/**
	 * Returns the style class set for the entire page &lt;html&gt;.
	 * <p>
	 * 
	 * @see WApplication#setHtmlClass(String styleClass)
	 */
	public String getHtmlClass() {
		return this.htmlClass_;
	}

	/**
	 * Sets the window title.
	 * <p>
	 * Sets the browser window title to <code>title</code>.
	 * <p>
	 * The default title is &quot;&quot;.
	 * <p>
	 * 
	 * @see WApplication#getTitle()
	 */
	public void setTitle(CharSequence title) {
		if (this.session_.getRenderer().isPreLearning()
				|| !this.title_.equals(title)) {
			this.title_ = WString.toWString(title);
			this.titleChanged_ = true;
		}
	}

	/**
	 * Returns the window title.
	 * <p>
	 * 
	 * @see WApplication#setTitle(CharSequence title)
	 */
	public WString getTitle() {
		return this.title_;
	}

	/**
	 * Returns the close message.
	 */
	public WString getCloseMessage() {
		return this.closeMessage_;
	}

	/**
	 * Returns the resource object that provides localized strings.
	 * <p>
	 * This returns the object previously set using
	 * {@link WApplication#setLocalizedStrings(WLocalizedStrings translator)
	 * setLocalizedStrings()}.
	 * <p>
	 * {@link WString#tr(String key) WString#tr()} is used to create localized
	 * strings, whose localized translation is looked up through this object,
	 * using a key.
	 * <p>
	 * 
	 * @see WString#tr(String key)
	 */
	public WLocalizedStrings getLocalizedStrings() {
		return this.localizedStrings_.getItems().get(0);
	}

	/**
	 * Sets the resource object that provides localized strings.
	 * <p>
	 * The <code>translator</code> resolves localized strings within the current
	 * application locale.
	 * <p>
	 * 
	 * @see WApplication#getLocalizedStrings()
	 * @see WString#tr(String key)
	 */
	public void setLocalizedStrings(WLocalizedStrings translator) {
		;
		this.localizedStrings_ = new WCombinedLocalizedStrings();
		if (translator != null) {
			this.localizedStrings_.add(translator);
		}
		WStdLocalizedStrings defaultMessages = new WStdLocalizedStrings();
		defaultMessages.useBuiltin(WtServlet.WtMessages_xml);
		this.localizedStrings_.add(defaultMessages);
	}

	/**
	 * Changes the locale.
	 * <p>
	 * The locale is used by the localized strings resource to resolve localized
	 * strings.
	 * <p>
	 * By passing an empty <code>locale</code>, the default locale is chosen.
	 * <p>
	 * When the locale is changed, {@link WApplication#refresh() refresh()} is
	 * called, which will resolve the strings of the current user-interface in
	 * the new locale.
	 * <p>
	 * At construction, the locale is copied from the environment (
	 * {@link WEnvironment#getLocale() WEnvironment#getLocale()}), and this is
	 * the locale that was configured by the user in his browser preferences,
	 * and passed using an HTTP request header.
	 * <p>
	 * 
	 * @see WApplication#getLocalizedStrings()
	 * @see WString#tr(String key)
	 */
	public void setLocale(Locale locale) {
		this.locale_ = locale;
		this.refresh();
	}

	/**
	 * Returns the current locale.
	 * <p>
	 */
	public Locale getLocale() {
		return this.locale_;
	}

	/**
	 * Refreshes the application.
	 * <p>
	 * This lets the application to refresh its data, including strings from
	 * message-resource bundles. This done by propagating
	 * {@link WWidget#refresh() WWidget#refresh()} through the widget hierarchy.
	 * <p>
	 * This method is also called when the user hits the refresh (or reload)
	 * button, if this can be caught within the current session.
	 * <p>
	 * The reload button may only be caught when cookies for session tracking
	 * are configured in the servlet container.
	 * <p>
	 * 
	 * @see WWidget#refresh()
	 */
	public void refresh() {
		if (this.localizedStrings_ != null) {
			this.localizedStrings_.refresh();
		}
		if (this.domRoot2_ != null) {
			this.domRoot2_.refresh();
		} else {
			this.widgetRoot_.refresh();
		}
		if (this.title_.refresh()) {
			this.titleChanged_ = true;
		}
		if (this.closeMessage_.refresh()) {
			this.closeMessageChanged_ = true;
		}
	}

	/**
	 * Binds a top-level widget for a WidgetSet deployment.
	 * <p>
	 * This method binds a <code>widget</code> to an existing element with DOM
	 * id <code>domId</code> on the page. The element type should correspond
	 * with the widget type (e.g. it should be a &lt;div&gt; for a
	 * {@link WContainerWidget}, or a &lt;table&gt; for a {@link WTable}).
	 * <p>
	 * 
	 * @see WApplication#getRoot()
	 * @see EntryPointType#WidgetSet
	 */
	public void bindWidget(WWidget widget, String domId) {
		if (this.session_.getType() != EntryPointType.WidgetSet) {
			throw new WtException(
					"WApplication::bind() can be used only in WidgetSet mode.");
		}
		widget.setId(domId);
		this.domRoot2_.addWidget(widget);
	}

	/**
	 * Returns a URL for the current session.
	 * <p>
	 * Returns the (relative) URL for this application session (including the
	 * session ID if necessary). The URL includes the full application path, and
	 * is expanded by the browser into a full URL.
	 * <p>
	 * For example, for an application deployed at <blockquote>
	 * 
	 * <pre>
	 * http://www.mydomain.com/stuff/app.wt
	 * </pre>
	 * 
	 * </blockquote> this method would return
	 * <code>&quot;/stuff/app.wt?wtd=AbCdEf&quot;</code>. Additional query
	 * parameters can be appended in the form of
	 * <code>&quot;&amp;param1=value&amp;param2=value&quot;</code>.
	 * <p>
	 * To obtain a URL that is suitable for bookmarking the current application
	 * state, to be used across sessions, use
	 * {@link WApplication#getBookmarkUrl() getBookmarkUrl()} instead.
	 * <p>
	 * 
	 * @see WApplication#redirect(String url)
	 * @see WEnvironment#getHostName()
	 * @see WEnvironment#getUrlScheme()
	 * @see WApplication#getBookmarkUrl()
	 */
	public String getUrl() {
		return this.fixRelativeUrl(this.session_.getApplicationUrl());
	}

	/**
	 * Makes an absolute URL.
	 * <p>
	 * Returns an absolute URL for a given (relative url) by including the
	 * schema, hostname, and deployment path.
	 * <p>
	 * If <code>url</code> is &quot;&quot;, then the absolute base URL is
	 * returned. This is the absolute URL at which the application is deployed,
	 * up to the last &apos;/&apos;.
	 */
	public String makeAbsoluteUrl(String url) {
		if (url.indexOf("://") != -1) {
			return url;
		} else {
			if (url.length() != 0 && url.charAt(0) == '/') {
				return this.getEnvironment().getUrlScheme() + "://"
						+ this.getEnvironment().getHostName() + url;
			} else {
				return this.session_.getAbsoluteBaseUrl() + url;
			}
		}
	}

	/**
	 * Returns a bookmarkable URL for the current internal path.
	 * <p>
	 * Is equivalent to
	 * <code>bookmarkUrl({@link WApplication#getInternalPath() getInternalPath()})</code>
	 * , see {@link WApplication#getBookmarkUrl(String internalPath)
	 * getBookmarkUrl()}.
	 * <p>
	 * To obtain a URL that is refers to the current session of the application,
	 * use {@link WApplication#getUrl() getUrl()} instead.
	 * <p>
	 * 
	 * @see WApplication#getUrl()
	 * @see WApplication#getBookmarkUrl(String internalPath)
	 */
	public String getBookmarkUrl() {
		return this.getBookmarkUrl(this.newInternalPath_);
	}

	/**
	 * Returns a bookmarkable URL for a given internal path.
	 * <p>
	 * Returns the (relative) URL for this application that includes the
	 * internal path <code>internalPath</code>, usable across sessions. The URL
	 * is relative and expanded into a full URL by the browser.
	 * <p>
	 * For example, for an application with current URL: <blockquote>
	 * 
	 * <pre>
	 * http://www.mydomain.com/stuff/app.wt#/project/internal/
	 * </pre>
	 * 
	 * </blockquote> when called with <code>&quot;/project/external&quot;</code>
	 * , this method would return:
	 * <ul>
	 * <li><code>&quot;app.wt/project/external/&quot;</code> when JavaScript is
	 * available, or the agent is a web spider, or</li>
	 * <li><code>&quot;app.wt/project/external/?wtd=AbCdEf&quot;</code> when no
	 * JavaScript is available and URL rewriting is used for session-tracking</li>
	 * </ul>
	 * <p>
	 * When the application is deployed at a folder (ending with &apos;/&apos;),
	 * this style of URLs is not possible, and URLs are of the form:
	 * <ul>
	 * <li><code>&quot;?_=/project/external/&quot;</code> when JavaScript is
	 * available, or the agent is a web spider, or</li>
	 * <li><code>&quot;?_=/project/external/&amp;wtd=AbCdEf&quot;</code> when no
	 * JavaScript is available and URL rewriting is used for session-tracking.</li>
	 * </ul>
	 * <p>
	 * You can use {@link WApplication#getBookmarkUrl() getBookmarkUrl()} as the
	 * destination for a {@link WAnchor}, and listen to a click event is
	 * attached to a slot that switches to the internal path
	 * <code>internalPath</code> (see
	 * {@link WAnchor#setRefInternalPath(String path)
	 * WAnchor#setRefInternalPath()}). In this way, an anchor can be used to
	 * switch between internal paths within an application regardless of the
	 * situation (browser with or without Ajax support, or a web spider bot),
	 * but still generates suitable URLs across sessions, which can be used for
	 * bookmarking, opening in a new window/tab, or indexing.
	 * <p>
	 * To obtain a URL that refers to the current session of the application,
	 * use {@link WApplication#getUrl() getUrl()} instead.
	 * <p>
	 * 
	 * @see WApplication#getUrl()
	 * @see WApplication#getBookmarkUrl()
	 */
	public String getBookmarkUrl(String internalPath) {
		return this.session_.getBookmarkUrl(internalPath);
	}

	/**
	 * Change the internal path.
	 * <p>
	 * A JWt application may manage multiple virtual paths. The virtual path is
	 * appended to the application URL. Depending on the situation, the path is
	 * directly appended to the application URL or it is appended using a name
	 * anchor (#).
	 * <p>
	 * For example, for an application deployed at: <blockquote>
	 * 
	 * <pre>
	 * http://www.mydomain.com/stuff/app.wt
	 * </pre>
	 * 
	 * </blockquote> for which an <code>internalPath</code>
	 * <code>&quot;/project/z3cbc/details/&quot;</code> is set, the two forms
	 * for the application URL are:
	 * <ul>
	 * <li>
	 * in an AJAX session: <blockquote>
	 * 
	 * <pre>
	 * http://www.mydomain.com/stuff/app.wt#/project/z3cbc/details/
	 * </pre>
	 * 
	 * </blockquote></li>
	 * <li>
	 * in a plain HTML session: <blockquote>
	 * 
	 * <pre>
	 * http://www.mydomain.com/stuff/app.wt/project/z3cbc/details/
	 * </pre>
	 * 
	 * </blockquote> This has as major consequence that from the browser stand
	 * point, the application now serves many different URLs. As a consequence,
	 * relative URLs will break. Still, you can specify relative URLs within
	 * your application (in for example {@link WAnchor#setRef(String url)
	 * WAnchor#setRef()} or {@link WImage#setImageRef(String ref)
	 * WImage#setImageRef()}) since JWt will transform them to absolute URLs
	 * when needed. But, this in turn may break deployments behind reverse
	 * proxies when the context paths differ. For the same reason, you will need
	 * to use absolute URLs in any XHTML or CSS you write manually. <br>
	 * This type of URLs are only used when the your application is deployed at
	 * a location that does not end with a &apos;/&apos;. Otherwise, JWt will
	 * generate URLS like: <blockquote>
	 * 
	 * <pre>
	 * http://www.mydomain.com/stuff/?_=/project/z3cbc/details/
	 * </pre>
	 * 
	 * </blockquote></li>
	 * </ul>
	 * <p>
	 * When the internal path is changed, an entry is added to the browser
	 * history. When the user navigates back and forward through this history
	 * (using the browser back/forward buttons), an
	 * {@link WApplication#internalPathChanged() internalPathChanged()} event is
	 * emitted. You should listen to this signal to switch the application to
	 * the corresponding state. When <code>emitChange</code> is
	 * <code>true</code>, this signal is also emitted by setting the path.
	 * <p>
	 * A url that includes the internal path may be obtained using
	 * {@link WApplication#getBookmarkUrl() getBookmarkUrl()}.
	 * <p>
	 * The <code>internalPath</code> must start with a &apos;/&apos;. In this
	 * way, you can still use normal anchors in your HTML. Internal path changes
	 * initiated in the browser to paths that do not start with a &apos;/&apos;
	 * are ignored.
	 * <p>
	 * 
	 * @see WApplication#getBookmarkUrl()
	 * @see WApplication#getInternalPath()
	 * @see WApplication#internalPathChanged()
	 */
	public void setInternalPath(String path, boolean emitChange) {
		this.enableInternalPaths();
		if (!this.internalPathIsChanged_) {
			this.oldInternalPath_ = this.newInternalPath_;
		}
		if (!this.session_.getRenderer().isPreLearning() && emitChange) {
			this.changeInternalPath(path);
		} else {
			this.newInternalPath_ = path;
		}
		this.internalPathIsChanged_ = true;
	}

	/**
	 * Change the internal path.
	 * <p>
	 * Calls {@link #setInternalPath(String path, boolean emitChange)
	 * setInternalPath(path, false)}
	 */
	public final void setInternalPath(String path) {
		setInternalPath(path, false);
	}

	/**
	 * Returns the current internal path.
	 * <p>
	 * When the application is just created, this is equal to
	 * {@link WEnvironment#getInternalPath() WEnvironment#getInternalPath()}.
	 * <p>
	 * 
	 * @see WApplication#setInternalPath(String path, boolean emitChange)
	 * @see WApplication#getInternalPathNextPart(String path)
	 * @see WApplication#internalPathMatches(String path)
	 */
	public String getInternalPath() {
		return this.newInternalPath_;
	}

	/**
	 * Returns a part of the current internal path.
	 * <p>
	 * This is a convenience method which returns the next <code>folder</code>
	 * in the internal path, after the given <code>path</code>.
	 * <p>
	 * For example, when the current internal path is
	 * <code>&quot;/project/z3cbc/details&quot;</code>, this method returns
	 * <code>&quot;details&quot;</code> when called with
	 * <code>&quot;/project/z3cbc/&quot;</code> as <code>path</code> argument.
	 * <p>
	 * The <code>path</code> must start with a &apos;/&apos;, and
	 * {@link WApplication#internalPathMatches(String path)
	 * internalPathMatches()} should evaluate to <code>true</code> for the given
	 * <code>path</code>. If not, an empty string is returned and an error
	 * message is logged.
	 * <p>
	 * 
	 * @see WApplication#getInternalPath()
	 * @see WApplication#internalPathChanged()
	 */
	public String getInternalPathNextPart(String path) {
		String current = StringUtils.terminate(this.newInternalPath_, '/');
		if (!pathMatches(current, path)) {
			this.log("warn").append("WApplication::internalPath(): path '")
					.append(path).append("' not within current path '").append(
							this.newInternalPath_).append("'");
			return "";
		}
		int startPos = path.length();
		int t = current.indexOf('/', startPos);
		String result = "";
		if (t == -1) {
			result = current.substring(startPos);
		} else {
			result = current.substring(startPos, startPos + t - startPos);
		}
		return result;
	}

	/**
	 * Checks if the internal path matches a given path.
	 * <p>
	 * Returns whether the current {@link WApplication#getInternalPath()
	 * getInternalPath()} starts with <code>path</code> (or is equal to
	 * <code>path</code>). You will typically use this method within a slot
	 * conneted to the {@link WApplication#internalPathChanged()
	 * internalPathChanged()} signal, to check that an internal path change
	 * affects the widget. It may also be useful before changing
	 * <code>path</code> using
	 * {@link WApplication#setInternalPath(String path, boolean emitChange)
	 * setInternalPath()} if you do not intend to remove sub paths when the
	 * current internal path already matches <code>path</code>.
	 * <p>
	 * The <code>path</code> must start with a &apos;/&apos;.
	 * <p>
	 * 
	 * @see WApplication#setInternalPath(String path, boolean emitChange)
	 * @see WApplication#getInternalPath()
	 */
	public boolean internalPathMatches(String path) {
		if (this.session_.getRenderer().isPreLearning()) {
			return false;
		} else {
			return pathMatches(StringUtils
					.terminate(this.newInternalPath_, '/'), path);
		}
	}

	/**
	 * Signal which indicates that the user changes the internal path.
	 * <p>
	 * This signal indicates a change to the internal path, which is usually
	 * triggered by the user using the browser back/forward buttons.
	 * <p>
	 * The argument contains the new internal path.
	 * <p>
	 * 
	 * @see WApplication#setInternalPath(String path, boolean emitChange)
	 */
	public Signal1<String> internalPathChanged() {
		this.enableInternalPaths();
		return this.internalPathChanged_;
	}

	/**
	 * Redirects the application to another location.
	 * <p>
	 * The client will be redirected to a new location identified by
	 * <code>url</code>. Use this in conjunction with
	 * {@link WApplication#quit() quit()} if you want to the application to be
	 * terminated as well.
	 * <p>
	 * Calling redirect() does not imply quit() since it may be useful to switch
	 * between a non-secure and secure (SSL) transport connection.
	 */
	public void redirect(String url) {
		this.session_.redirect(url);
	}

	/**
	 * Returns the URL at which the resources are deployed.
	 */
	public static String getResourcesUrl() {
		String path = WebSession.getInstance().getController()
				.getConfiguration().getProperty(WApplication.RESOURCES_URL);
		if (path == "/wt-resources/") {
			String result = WApplication.getInstance().getEnvironment()
					.getDeploymentPath();
			if (result.length() != 0
					&& result.charAt(result.length() - 1) == '/') {
				return result + path.substring(1);
			} else {
				return result + path;
			}
		} else {
			return path;
		}
	}

	/**
	 * Returns the unique identifier for the current session.
	 * <p>
	 * The session id is a string that uniquely identifies the current session.
	 * Note that the actual contents has no particular meaning and client
	 * applications should in no way try to interpret its value.
	 */
	public String getSessionId() {
		return this.session_.getSessionId();
	}

	WebSession getSession() {
		return this.session_;
	}

	/**
	 * Enables server-initiated updates.
	 * <p>
	 * By default, updates to the user interface are possible only at startup,
	 * during any event (in a slot), or at regular time points using
	 * {@link WTimer}. This is the normal JWt event loop.
	 * <p>
	 * In some cases, one may want to modify the user interface from a second
	 * thread, outside the event loop. While this may be worked around by the
	 * {@link WTimer}, in some cases, there are bandwidth and processing
	 * overheads associated which may be unnecessary, and which create a
	 * trade-off with time resolution of the updates.
	 * <p>
	 * When <code>enabled</code> is <code>true</code>, this enables &quot;server
	 * push&quot; (what is called &apos;comet&apos; in AJAX terminology).
	 * Widgets may then be modified, created or deleted outside of the event
	 * loop (e.g. in response to execution of another thread), and these changes
	 * are propagated by calling {@link WApplication#triggerUpdate()
	 * triggerUpdate()}.
	 * <p>
	 * Note that you need to grab the application&apos;s update lock to avoid
	 * concurrency problems, whenever you modify the application&apos;s state
	 * from another thread.
	 * <p>
	 * An example of how to modify the widget tree outside the event loop and
	 * propagate changes is:
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * // You need to have a reference to the application whose state
	 *    // you are about to manipulate.
	 *    WApplication app = ...;
	 *   
	 *    // Grab the application lock
	 *    WApplication.UpdateLock lock = app.getUpdateLock();
	 *   
	 *    try {
	 *      // We now have exclusive access to the application:
	 *      // we can safely modify the widget tree for example.
	 *      app.getRoot().addWidget(new WText(&quot;Something happened!&quot;));
	 *   
	 *      // Push the changes to the browser
	 *      app.triggerUpdate();
	 *    } finally {
	 *      lock.release();
	 *    }
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * This works only if your servlet container supports the Servlet 3.0 API.
	 * If you try to invoke this function on a servlet container with no such
	 * support, and exception will be thrown.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>This works only if JavaScript is available on the
	 * client.</i>
	 * </p>
	 * 
	 * @see WApplication#triggerUpdate()
	 */
	public void enableUpdates(boolean enabled) {
		if (enabled) {
			++this.serverPush_;
		} else {
			--this.serverPush_;
		}
		if (enabled && this.serverPush_ == 1 || !enabled
				&& this.serverPush_ == 0) {
			this.doJavaScript(this.javaScriptClass_ + "._p_.setServerPush("
					+ (enabled ? "true" : "false") + ");");
		}
	}

	/**
	 * Enables server-initiated updates.
	 * <p>
	 * Calls {@link #enableUpdates(boolean enabled) enableUpdates(true)}
	 */
	public final void enableUpdates() {
		enableUpdates(true);
	}

	/**
	 * Returns whether server-initiated updates are enabled.
	 * <p>
	 * 
	 * @see WApplication#enableUpdates(boolean enabled)
	 */
	public boolean isUpdatesEnabled() {
		return this.serverPush_ > 0;
	}

	/**
	 * Propagates server-initiated updates.
	 * <p>
	 * Propagate changes made to the user interface outside of the main event
	 * loop. This is only possible after a call to
	 * {@link WApplication#enableUpdates(boolean enabled) enableUpdates()}, and
	 * must be done while holding the {@link UpdateLock}.
	 * <p>
	 * 
	 * @see WApplication#enableUpdates(boolean enabled)
	 * @see UpdateLock
	 */
	public void triggerUpdate() {
		if (!WtServlet.isAsyncSupported()) {
			throw new WtException(
					"Server push requires a Servlet 3.0 enabled servlet container and an application with async-supported enabled.");
		}
		if (!this.modifiedWithoutEvent_) {
			return;
		}
		if (this.serverPush_ > 0) {
			this.session_.pushUpdates();
		} else {
			throw new WtException(
					"WApplication::triggerUpdate() called but server-triggered updates not enabled using WApplication::enableUpdates()");
		}
	}

	/**
	 * A synchronization lock for manipulating and updating the application and
	 * its widgets outside of the event loop.
	 * <p>
	 * 
	 * You need to take this lock only when you want to manipulate widgets
	 * outside of the event loop. Inside the event loop, this lock is already
	 * held by the library itself.
	 * <p>
	 * 
	 * @see WApplication#getUpdateLock()
	 */
	public static class UpdateLock {
		/**
		 * Releases the lock.
		 */
		public void release() {
			System.err.append("Releasing update lock").append('\n');
			if (WApplication.getInstance().modifiedWithoutEvent_) {
				System.err.append("Releasing handler").append('\n');
				WApplication.getInstance().modifiedWithoutEvent_ = false;
				WebSession.Handler.getInstance().release();
			}
		}

		private UpdateLock(WApplication app) {
			System.err.append("Grabbing update lock").append('\n');
			WebSession.Handler handler = WebSession.Handler.getInstance();
			if (handler != null && handler.isHaveLock()
					&& handler.getSession() == app.session_) {
				return;
			}
			System.err.append("Creating new handler for app: app.sessionId()")
					.append('\n');
			new WebSession.Handler(app.session_, true);
			app.modifiedWithoutEvent_ = true;
		}
	}

	/**
	 * Grabs and returns the lock for manipulating widgets outside the event
	 * loop. java.
	 * <p>
	 * cpp
	 * <p>
	 * You need to keep this lock in scope while manipulating widgets outside of
	 * the event loop. In normal cases, inside the JWt event loop, you do not
	 * need to care about it.
	 * <p>
	 * 
	 * @see WApplication#enableUpdates(boolean enabled)
	 * @see WApplication#triggerUpdate() cpp
	 */
	public WApplication.UpdateLock getUpdateLock() {
		return new WApplication.UpdateLock(this);
	}

	/**
	 * Attach an auxiliary thread to this application.
	 * <p>
	 * In a multi-threaded environment, {@link WApplication#getInstance()
	 * getInstance()} uses thread-local data to retrieve the application object
	 * that corresponds to the session currently being handled by the thread.
	 * This is set automatically by the library whenever an event is delivered
	 * to the application, or when you use the {@link UpdateLock} to modify the
	 * application from an auxiliary thread outside the normal event loop.
	 * <p>
	 * When you want to manipulate the widget tree inside the main event loop,
	 * but from within an auxiliary thread, then you cannot use the
	 * {@link UpdateLock} since this will create an immediate dead lock.
	 * Instead, you may attach the auxiliary thread to the application, by
	 * calling this method from the auxiliary thread, and in this way you can
	 * modify the application from within that thread without needing the update
	 * lock.
	 * <p>
	 * Calling {@link WApplication#attachThread(boolean attach) attachThread()}
	 * with <code>attach</code> = <code>false</code>, detaches the current
	 * thread.
	 */
	public void attachThread(boolean attach) {
		if (attach) {
			WebSession.Handler.attachThreadToSession(this.session_);
		} else {
			WebSession.Handler.attachThreadToSession((WebSession) null);
		}
	}

	/**
	 * Attach an auxiliary thread to this application.
	 * <p>
	 * Calls {@link #attachThread(boolean attach) attachThread(true)}
	 */
	public final void attachThread() {
		attachThread(true);
	}

	/**
	 * Executes some JavaScript code.
	 * <p>
	 * This method may be used to call some custom <code>javaScript</code> code
	 * as part of an event response.
	 * <p>
	 * This function does not wait until the JavaScript is run, but returns
	 * immediately. The JavaScript will be run after the normal event handling,
	 * unless <code>afterLoaded</code> is set to <code>false</code>.
	 * <p>
	 * 
	 * @see WApplication#addAutoJavaScript(String javascript)
	 * @see WApplication#declareJavaScriptFunction(String name, String function)
	 */
	public void doJavaScript(String javascript, boolean afterLoaded) {
		if (afterLoaded) {
			this.afterLoadJavaScript_ += javascript;
			this.afterLoadJavaScript_ += '\n';
		} else {
			this.beforeLoadJavaScript_ += javascript;
			this.beforeLoadJavaScript_ += '\n';
			this.newBeforeLoadJavaScript_ += javascript;
			this.newBeforeLoadJavaScript_ += '\n';
		}
	}

	/**
	 * Executes some JavaScript code.
	 * <p>
	 * Calls {@link #doJavaScript(String javascript, boolean afterLoaded)
	 * doJavaScript(javascript, true)}
	 */
	public final void doJavaScript(String javascript) {
		doJavaScript(javascript, true);
	}

	/**
	 * Adds JavaScript statements that should be run continuously.
	 * <p>
	 * This is an internal method.
	 * <p>
	 * It is used by for example layout managers to adjust the layout whenever
	 * the DOM tree is manipulated.
	 * <p>
	 * 
	 * @see WApplication#doJavaScript(String javascript, boolean afterLoaded)
	 */
	public void addAutoJavaScript(String javascript) {
		this.autoJavaScript_ += javascript;
		this.autoJavaScriptChanged_ = true;
	}

	/**
	 * Declares an application-wide JavaScript function.
	 * <p>
	 * This is an internal method.
	 */
	public void declareJavaScriptFunction(String name, String function) {
		this.doJavaScript(this.javaScriptClass_ + '.' + name + '=' + function
				+ ';', false);
	}

	/**
	 * Loads a JavaScript library.
	 * <p>
	 * Loads a JavaScript library located at the URL <code>url</code>. JWt keeps
	 * track of libraries (with the same URL) that already have been loaded, and
	 * will load a library only once. In addition, you may provide a
	 * <code>symbol</code> which if already defined will also indicate that the
	 * library was already loaded (possibly outside of JWt when in WidgetSet
	 * mode).
	 * <p>
	 * This method returns <code>true</code> only when the library is loaded for
	 * the first time.
	 * <p>
	 * JavaScript libraries may be loaded at any point in time. Any JavaScript
	 * code is deferred until the library is loaded, except for JavaScript that
	 * was defined to load before, passing <code>false</code> as second
	 * parameter to
	 * {@link WApplication#doJavaScript(String javascript, boolean afterLoaded)
	 * doJavaScript()}.
	 */
	public boolean require(String uri, String symbol) {
		WApplication.ScriptLibrary sl = new WApplication.ScriptLibrary(uri,
				symbol);
		if (this.scriptLibraries_.indexOf(sl) == -1) {
			sl.beforeLoadJS = this.newBeforeLoadJavaScript_;
			this.newBeforeLoadJavaScript_ = "";
			this.scriptLibraries_.add(sl);
			++this.scriptLibrariesAdded_;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Loads a JavaScript library.
	 * <p>
	 * Returns {@link #require(String uri, String symbol) require(uri, "")}
	 */
	public final boolean require(String uri) {
		return require(uri, "");
	}

	/**
	 * Sets the name of the application JavaScript class.
	 * <p>
	 * This should be called right after construction of the application, and
	 * changing the JavaScript class is only supported for WidgetSet mode
	 * applications. The <code>className</code> should be a valid JavaScript
	 * identifier, and should also be unique in a single page.
	 */
	public void setJavaScriptClass(String javaScriptClass) {
		if (this.session_.getType() != EntryPointType.Application) {
			this.javaScriptClass_ = javaScriptClass;
		}
	}

	/**
	 * Returns the name of the application JavaScript class.
	 * <p>
	 * This JavaScript class encapsulates all JavaScript methods specific to
	 * this application instance. The method is foreseen to allow multiple
	 * applications to run simultaneously on the same page in Wt::WidgtSet mode,
	 * without interfering.
	 */
	public String getJavaScriptClass() {
		return this.javaScriptClass_;
	}

	/**
	 * Processes UI events.
	 * <p>
	 * You may call this method during a long operation to:
	 * <ul>
	 * <li>propagate widget changes to the client.</li>
	 * <li>process UI events.</li>
	 * </ul>
	 * <p>
	 * This method starts a recursive event loop, blocking the current thread,
	 * and resumes when all pending user interface events have been processed.
	 * <p>
	 * Because a thread is blocked, this may affect your application
	 * scalability.
	 */
	public void processEvents() {
		this.doJavaScript("setTimeout(\"" + this.javaScriptClass_
				+ "._p_.update(null,'none',null,false);\",0);");
		this.session_.doRecursiveEventLoop();
	}

	/**
	 * Reads a configuration property.
	 * <p>
	 * Tries to read a configured value for the property <code>name</code>. If
	 * no value was configured, the default <code>value</code> is returned.
	 */
	public static String readConfigurationProperty(String name, String value) {
		String property = WebSession.getInstance().getController()
				.getConfiguration().getProperty(name);
		if (property != null) {
			return property;
		} else {
			return value;
		}
	}

	/**
	 * Sets the Ajax communication method (<b>deprecated</b>).
	 * <p>
	 * This method has no effect.
	 * <p>
	 * Since JWt 3.1.8, a communication method that works is detected at run
	 * time. For widget set mode, cross-domain Ajax is chosen if available.
	 * <p>
	 * 
	 * @deprecated this setting is no longer needed.
	 */
	public void setAjaxMethod(WApplication.AjaxMethod method) {
		this.ajaxMethod_ = method;
	}

	/**
	 * Returns the Ajax communication method (<b>deprecated</b>).
	 * <p>
	 * 
	 * @see WApplication#setAjaxMethod(WApplication.AjaxMethod method)
	 */
	public WApplication.AjaxMethod getAjaxMethod() {
		return this.ajaxMethod_;
	}

	WContainerWidget getDomRoot() {
		return this.domRoot_;
	}

	WContainerWidget getDomRoot2() {
		return this.domRoot2_;
	}

	String encodeObject(WObject object) {
		String result = "w" + object.getUniqueId();
		this.encodedObjects_.put(result, object);
		return result;
	}

	WObject decodeObject(String objectId) {
		WObject i = this.encodedObjects_.get(objectId);
		if (i != null) {
			return i;
		} else {
			return null;
		}
	}

	String fixRelativeUrl(String url) {
		if (url.indexOf("://") != -1) {
			return url;
		}
		if (url.length() > 0 && url.charAt(0) == '#') {
			return url;
		}
		if (this.session_.getType() == EntryPointType.Application) {
			if (!this.getEnvironment().hasJavaScript()
					&& WebSession.Handler.getInstance().getRequest()
							.getPathInfo().length() != 0) {
				if (url.length() != 0 && url.charAt(0) == '/') {
					return url;
				} else {
					return this.session_.getBaseUrl() + url;
				}
			} else {
				return url;
			}
		} else {
			return this.makeAbsoluteUrl(url);
		}
	}

	/**
	 * Initializes the application, post-construction.
	 * <p>
	 * This method is invoked by the JWt library after construction of a new
	 * application. You may reimplement this method to do additional
	 * initialization that is not possible from the constructor (e.g. which uses
	 * virtual methods).
	 */
	public void initialize() {
	}

	/**
	 * Changes the threshold for two-phase rendering.
	 * <p>
	 * This changes the threshold for the <code>size</code> of a JavaScript
	 * response (in bytes) to render invisible changes in one go. If the
	 * bandwidth for rendering the invisible changes exceed the threshold, they
	 * will be fetched in a second communication, after the visible changes have
	 * been rendered.
	 * <p>
	 * The value is a trade-off: setting it smaller will always use two-phase
	 * rendering, increasing the total render time but reducing the latency for
	 * the visible changes. Setting it too large will increase the latency to
	 * render the visible changes, since first also all invisible changes need
	 * to be computed and received in the browser.
	 */
	public void setTwoPhaseRenderingThreshold(int bytes) {
		this.session_.getRenderer().setTwoPhaseThreshold(bytes);
	}

	/**
	 * Sets a new cookie.
	 * <p>
	 * Use cookies to transfer information across different sessions (e.g. a
	 * user name). In a subsequent session you will be able to read this cookie
	 * using {@link WEnvironment#getCookie(String cookieNname)
	 * WEnvironment#getCookie()}. You cannot use a cookie to store information
	 * in the current session.
	 * <p>
	 * The name must be a valid cookie name (of type &apos;token&apos;: no
	 * special characters or separators, see RFC2616 page 16). The value may be
	 * anything. Specify the maximum age (in seconds) after which the client
	 * must discard the cookie. To delete a cookie, use a value of
	 * &apos;0&apos;.
	 * <p>
	 * By default the cookie only applies to the current path on the current
	 * domain. To set a proper value for domain, see also RFC2109.
	 * <p>
	 * 
	 * @see WEnvironment#supportsCookies()
	 * @see WEnvironment#getCookie(String cookieNname)
	 */
	public void setCookie(String name, String value, int maxAge, String domain,
			String path) {
		this.session_.getRenderer()
				.setCookie(name, value, maxAge, domain, path);
	}

	/**
	 * Sets a new cookie.
	 * <p>
	 * Calls
	 * {@link #setCookie(String name, String value, int maxAge, String domain, String path)
	 * setCookie(name, value, maxAge, "", "")}
	 */
	public final void setCookie(String name, String value, int maxAge) {
		setCookie(name, value, maxAge, "", "");
	}

	/**
	 * Sets a new cookie.
	 * <p>
	 * Calls
	 * {@link #setCookie(String name, String value, int maxAge, String domain, String path)
	 * setCookie(name, value, maxAge, domain, "")}
	 */
	public final void setCookie(String name, String value, int maxAge,
			String domain) {
		setCookie(name, value, maxAge, domain, "");
	}

	/**
	 * Adds an HTML meta header.
	 * <p>
	 * A meta header can only be added in the following situations:
	 * <p>
	 * <ul>
	 * <li>when a plain HTML session is used (including when the user agent is a
	 * bot), you can add meta headers at any time.</li>
	 * </ul>
	 * <p>
	 * <ul>
	 * <li>or, when DOCREF<a class="el"
	 * href="overview.html#progressive_bootstrap">progressive bootstrap</a> is
	 * used, you can set meta headers for any type of session, from within the
	 * application constructor (which corresponds to the initial request).</li>
	 * </ul>
	 * <p>
	 * <ul>
	 * <li>but never for a {@link EntryPointType#WidgetSet} mode application
	 * since then the application is hosted within a foreign HTML page.</li>
	 * </ul>
	 * <p>
	 * These situations coincide with {@link WEnvironment#hasAjax()
	 * WEnvironment#hasAjax()} returning <code>false</code> (see
	 * {@link WApplication#getEnvironment() getEnvironment()}).
	 */
	public void addMetaHeader(String name, CharSequence content, String lang) {
		this.addMetaHeader(MetaHeaderType.MetaName, name, content, lang);
	}

	/**
	 * Adds an HTML meta header.
	 * <p>
	 * Calls {@link #addMetaHeader(String name, CharSequence content, String lang)
	 * addMetaHeader(name, content, "")}
	 */
	public final void addMetaHeader(String name, CharSequence content) {
		addMetaHeader(name, content, "");
	}

	/**
	 * Adds an HTML meta header.
	 * <p>
	 * This overloaded method allows to define both &quot;name&quot; meta
	 * headers, relating to document properties as well as
	 * &quot;http-equiv&quot; meta headers, which define HTTP headers.
	 * <p>
	 * 
	 * @see WApplication#addMetaHeader(String name, CharSequence content, String
	 *      lang)
	 */
	public void addMetaHeader(MetaHeaderType type, String name,
			CharSequence content, String lang) {
		if (this.getEnvironment().hasJavaScript()) {
			this.log("warn").append(
					"WApplication::addMetaHeader() with no effect");
		}
		if (type == MetaHeaderType.MetaHttpHeader) {
			for (int i = 0; i < this.metaHeaders_.size(); ++i) {
				WApplication.MetaHeader m = this.metaHeaders_.get(i);
				if (m.type == MetaHeaderType.MetaHttpHeader
						&& m.name.equals(name)) {
					m.content = WString.toWString(content);
					return;
				}
			}
		}
		this.metaHeaders_.add(new WApplication.MetaHeader(type, name, content,
				lang));
	}

	/**
	 * Adds an HTML meta header.
	 * <p>
	 * Calls
	 * {@link #addMetaHeader(MetaHeaderType type, String name, CharSequence content, String lang)
	 * addMetaHeader(type, name, content, "")}
	 */
	public final void addMetaHeader(MetaHeaderType type, String name,
			CharSequence content) {
		addMetaHeader(type, name, content, "");
	}

	/**
	 * Adds an entry to the application log.
	 * <p>
	 * Starts a new log entry of the given <code>type</code> in the JWt
	 * application log file. This method returns a stream-like object to which
	 * the message may be streamed.
	 */
	public WLogEntry log(String type) {
		return this.session_.log(type);
	}

	/**
	 * Sets the loading indicator.
	 * <p>
	 * The loading indicator is shown to indicate that a response from the
	 * server is pending or JavaScript is being evaluated.
	 * <p>
	 * The default loading indicator is a {@link WDefaultLoadingIndicator}.
	 * <p>
	 * When setting a new loading indicator, the previous one is deleted.
	 */
	public void setLoadingIndicator(WLoadingIndicator indicator) {
		if (!(this.loadingIndicator_ != null)) {
			this.showLoadingIndicator_.addListener(this.showLoadJS);
			this.hideLoadingIndicator_.addListener(this.hideLoadJS);
		}
		;
		this.loadingIndicator_ = indicator;
		if (this.loadingIndicator_ != null) {
			this.loadingIndicatorWidget_ = indicator.getWidget();
			this.domRoot_.addWidget(this.loadingIndicatorWidget_);
			this.showLoadJS.setJavaScript("function(o,e) {Wt3_1_8.inline('"
					+ this.loadingIndicatorWidget_.getId() + "');}");
			this.hideLoadJS.setJavaScript("function(o,e) {Wt3_1_8.hide('"
					+ this.loadingIndicatorWidget_.getId() + "');}");
			this.loadingIndicatorWidget_.hide();
		}
	}

	/**
	 * Returns the loading indicator.
	 * <p>
	 * 
	 * @see WApplication#setLoadingIndicator(WLoadingIndicator indicator)
	 */
	public WLoadingIndicator getLoadingIndicator() {
		return this.loadingIndicator_;
	}

	String getOnePixelGifUrl() {
		if (this.onePixelGifUrl_.length() == 0) {
			WMemoryResource w = new WMemoryResource("image/gif", this);
			w.setData(gifData);
			this.onePixelGifUrl_ = w.getUrl();
		}
		return this.onePixelGifUrl_;
	}

	String getDocType() {
		return this.session_.getDocType();
	}

	/**
	 * Quits the application.
	 * <p>
	 * The method returns immediately, but has as effect that the application
	 * will be terminated after the current event is completed.
	 * <p>
	 * The current widget tree (including any modifications still pending and
	 * applied during the current event handling) will still be rendered, after
	 * which the application is terminated.
	 * <p>
	 * You might want to make sure no more events can be received from the user,
	 * by not having anything clickable, for example by displaying only text.
	 * Even better is to {@link WApplication#redirect(String url) redirect()}
	 * the user to another, static, page in conjunction with quit().
	 * <p>
	 * 
	 * @see WApplication#redirect(String url)
	 */
	public void quit() {
		this.quited_ = true;
	}

	/**
	 * Returns whether the application is quited.
	 * <p>
	 * 
	 * @see WApplication#quit()
	 */
	public boolean isQuited() {
		return this.quited_;
	}

	/**
	 * Returns the current maximum size of a request to the application.
	 * <p>
	 * 
	 * @see WApplication#requestTooLarge()
	 */
	public long getMaximumRequestSize() {
		return this.session_.getController().getConfiguration()
				.getMaxRequestSize() * 1024;
	}

	/**
	 * Signal which indicates that too a large request was received.
	 * <p>
	 * The integer parameter is the request size that was received in bytes.
	 */
	public Signal1<Integer> requestTooLarge() {
		return this.requestTooLarge_;
	}

	void redirectToSession(String newSessionId) {
		Configuration conf = this.session_.getController().getConfiguration();
		String redirectUrl = this.getBookmarkUrl();
		if (conf.getSessionTracking() == Configuration.SessionTracking.CookiesURL
				&& this.getEnvironment().supportsCookies()) {
			String cookieName = this.getEnvironment().getDeploymentPath();
			this.setCookie(cookieName, newSessionId, -1);
		} else {
			redirectUrl += "?wtd=" + newSessionId;
		}
		this.redirect(redirectUrl);
	}

	boolean isConnected() {
		return this.connected_;
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The application receives key events when no widget currently has focus.
	 * Otherwise, key events are handled by the widget in focus, and its
	 * ancestors.
	 * <p>
	 * 
	 * @see WInteractWidget#keyWentDown()
	 */
	public EventSignal1<WKeyEvent> globalKeyWentDown() {
		return this.domRoot_.keyWentDown();
	}

	/**
	 * Event signal emitted when a &quot;character&quot; was entered.
	 * <p>
	 * The application receives key events when no widget currently has focus.
	 * Otherwise, key events are handled by the widget in focus, and its
	 * ancestors.
	 * <p>
	 * 
	 * @see WInteractWidget#keyPressed()
	 */
	public EventSignal1<WKeyEvent> globalKeyPressed() {
		return this.domRoot_.keyPressed();
	}

	/**
	 * Event signal emitted when a keyboard key is released.
	 * <p>
	 * The application receives key events when no widget currently has focus.
	 * Otherwise, key events are handled by the widget in focus, and its
	 * ancestors.
	 * <p>
	 * 
	 * @see WInteractWidget#keyWentUp()
	 */
	public EventSignal1<WKeyEvent> globalKeyWentUp() {
		return this.domRoot_.keyWentUp();
	}

	/**
	 * Event signal emitted when enter was pressed.
	 * <p>
	 * The application receives key events when no widget currently has focus.
	 * Otherwise, key events are handled by the widget in focus, and its
	 * ancestors.
	 * <p>
	 * 
	 * @see WInteractWidget#enterPressed()
	 */
	public EventSignal globalEnterPressed() {
		return this.domRoot_.enterPressed();
	}

	/**
	 * Event signal emitted when escape was pressed.
	 * <p>
	 * The application receives key events when no widget currently has focus.
	 * Otherwise, key events are handled by the widget in focus, and its
	 * ancestors.
	 * <p>
	 * 
	 * @see WInteractWidget#escapePressed()
	 */
	public EventSignal globalEscapePressed() {
		return this.domRoot_.escapePressed();
	}

	boolean isDebug() {
		return this.session_.isDebug();
	}

	public void setFocus(String id, int selectionStart, int selectionEnd) {
		this.focusId_ = id;
		this.selectionStart_ = selectionStart;
		this.selectionEnd_ = selectionEnd;
	}

	boolean isJavaScriptLoaded(String jsFile) {
		return this.javaScriptLoaded_.contains(jsFile) != false;
	}

	void setJavaScriptLoaded(String jsFile) {
		this.javaScriptLoaded_.add(jsFile);
	}

	/**
	 * Sets the message for the user to confirm closing of the application
	 * window/tab.
	 * <p>
	 * If the message is empty, then the user may navigate away from the page
	 * without confirmation.
	 * <p>
	 * Otherwise the user will be prompted with a browser-specific dialog asking
	 * him to confirm leaving the page. This <code>message</code> is added to
	 * the page.
	 * <p>
	 * 
	 * @see WApplication#unload()
	 */
	public void setConfirmCloseMessage(CharSequence message) {
		if (!message.equals(this.closeMessage_)) {
			this.closeMessage_ = WString.toWString(message);
			this.closeMessageChanged_ = true;
		}
	}

	/**
	 * Notifies an event to the application.
	 * <p>
	 * This method is called by the event loop for propagating an event to the
	 * application. It provides a single point of entry for events to the
	 * application, besides the application constructor.
	 * <p>
	 * You may want to reimplement this method for two reasons:
	 * <p>
	 * <ul>
	 * <li>for having a single point for exception handling: while you may want
	 * to catch recoverable exceptions in a more appropriate place, general
	 * (usually fatal) exceptions may be caught here. You will in probably also
	 * want to catch the same exceptions in the application constructor in the
	 * same way.</li>
	 * <li>you want to manage resource usage during requests. For example, at
	 * the end of request handling, you want to return a database session back
	 * to the pool. Since notify() is also used for rendering right after the
	 * application is created, this will also clean up resources after
	 * application construction.</li>
	 * </ul>
	 * <p>
	 * In either case, you will need to call the base class implementation of
	 * notify(), as otherwise no events will be delivered to your application.
	 * <p>
	 * The following shows a generic template for reimplementhing this method
	 * for both managing request resources and generic exception handling.
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * void notify(WEvent event) {
	 * 	// Grab resources for during request handling
	 * 	try {
	 * 		super.notify(event);
	 * 	} catch (MyException exception) {
	 * 		// handle this exception in a central place
	 * 	}
	 * 	// Free resources used during request handling
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * Note that any uncaught exception throw during event handling terminates
	 * the session.
	 */
	protected void notify(WEvent e) throws IOException {
		this.session_.notify(e);
	}

	/**
	 * Returns whether a widget is exposed in the interface.
	 * <p>
	 * The default implementation simply returns <code>true</code>, unless a
	 * modal dialog is active, in which case it returns <code>true</code> only
	 * for widgets that are inside the dialog.
	 * <p>
	 * You may want to reimplement this method if you wish to disallow events
	 * from certain widgets even when they are inserted in the widget hierachy.
	 */
	protected boolean isExposed(WWidget w) {
		if (w != this.domRoot_ && this.exposedOnly_ != null) {
			for (WWidget p = w; p != null; p = p.getParent()) {
				if (p == this.exposedOnly_ || p == this.timerRoot_) {
					return true;
				}
			}
			return false;
		} else {
			WWidget p = w.getAdam();
			return p == this.domRoot_ || p == this.domRoot2_;
		}
	}

	/**
	 * Progresses to an Ajax-enabled user interface.
	 * <p>
	 * This method is called when the progressive bootstrap method is used, and
	 * support for AJAX has been detected. The default behavior will propagate
	 * the {@link WWidget#enableAjax() WWidget#enableAjax()} method through the
	 * widget hierarchy.
	 * <p>
	 * You may want to reimplement this method if you want to make changes to
	 * the user-interface when AJAX is enabled. You should always call the base
	 * implementation.
	 * <p>
	 * 
	 * @see WWidget#enableAjax()
	 */
	protected void enableAjax() {
		this.enableAjax_ = true;
		this.session_.getRenderer().beforeLoadJS_
				.append(this.newBeforeLoadJavaScript_);
		this.newBeforeLoadJavaScript_ = "";
		this.session_.getRenderer().beforeLoadJS_
				.append(this.afterLoadJavaScript_);
		this.afterLoadJavaScript_ = "";
		this.domRoot_.enableAjax();
		if (this.domRoot2_ != null) {
			this.domRoot2_.enableAjax();
		}
	}

	/**
	 * Handles a browser unload event.
	 * <p>
	 * The browser unloads the application when the user navigates away or when
	 * he closes the window or tab.
	 * <p>
	 * When <code>reload-is-new-session</code> is set to <code>true</code>, then
	 * the default implementation of this method terminates this session by
	 * calling {@link WApplication#quit() quit()}.
	 * <p>
	 * You may want to reimplement this if you want to keep the application
	 * running until it times out (as was the behaviour before JWt 3.1.6).
	 */
	protected void unload() {
		this.quit();
	}

	private Signal1<Integer> requestTooLarge_;

	static class ScriptLibrary {
		public ScriptLibrary(String anUri, String aSymbol) {
			this.uri = anUri;
			this.symbol = aSymbol;
			this.beforeLoadJS = "";
		}

		public String uri;
		public String symbol;
		public String beforeLoadJS;

		public boolean equals(WApplication.ScriptLibrary other) {
			return this.uri.equals(other.uri);
		}
	}

	static class MetaHeader {
		public MetaHeader(MetaHeaderType aType, String aName,
				CharSequence aContent, String aLang) {
			this.type = aType;
			this.name = aName;
			this.lang = aLang;
			this.content = WString.toWString(aContent);
		}

		public MetaHeaderType type;
		public String name;
		public String lang;
		public WString content;
	}

	private WebSession session_;
	private WString title_;
	private WString closeMessage_;
	boolean titleChanged_;
	boolean closeMessageChanged_;
	private WContainerWidget widgetRoot_;
	WContainerWidget domRoot_;
	WContainerWidget domRoot2_;
	private WContainerWidget timerRoot_;
	private WCssStyleSheet styleSheet_;
	WCombinedLocalizedStrings localizedStrings_;
	private Locale locale_;
	String oldInternalPath_;
	String newInternalPath_;
	Signal1<String> internalPathChanged_;
	boolean internalPathIsChanged_;
	private int serverPush_;
	boolean modifiedWithoutEvent_;
	private String javaScriptClass_;
	private WApplication.AjaxMethod ajaxMethod_;
	private WContainerWidget dialogCover_;
	private boolean quited_;
	private String onePixelGifUrl_;
	private boolean internalPathsEnabled_;
	private WWidget exposedOnly_;
	private WLoadingIndicator loadingIndicator_;
	WWidget loadingIndicatorWidget_;
	private boolean connected_;
	String htmlClass_;
	String bodyClass_;
	boolean bodyHtmlClassChanged_;
	boolean enableAjax_;
	boolean initialized_;
	private String focusId_;
	private int selectionStart_;
	private int selectionEnd_;
	private LayoutDirection layoutDirection_;
	List<WApplication.ScriptLibrary> scriptLibraries_;
	int scriptLibrariesAdded_;

	static class StyleSheet {
		public String uri;
		public String media;

		public StyleSheet(String anUri, String aMedia) {
			this.uri = anUri;
			this.media = aMedia;
		}
	}

	private String theme_;
	List<WApplication.StyleSheet> styleSheets_;
	int styleSheetsAdded_;
	List<WApplication.MetaHeader> metaHeaders_;
	private Map<String, WeakReference<AbstractEventSignal>> exposedSignals_;
	private Map<String, WResource> exposedResources_;
	private Map<String, WObject> encodedObjects_;
	private boolean exposeSignals_;
	String afterLoadJavaScript_;
	String beforeLoadJavaScript_;
	String newBeforeLoadJavaScript_;
	String autoJavaScript_;
	private Set<String> javaScriptLoaded_;
	boolean autoJavaScriptChanged_;
	EventSignal showLoadingIndicator_;
	EventSignal hideLoadingIndicator_;
	private JSignal unloaded_;

	WContainerWidget getTimerRoot() {
		return this.timerRoot_;
	}

	WContainerWidget getDialogCover(boolean create) {
		if (this.dialogCover_ == null && create && this.timerRoot_ != null) {
			this.dialogCover_ = new WContainerWidget(this.domRoot_);
			this.dialogCover_.setStyleClass("Wt-dialogcover");
			this.dialogCover_.hide();
		}
		return this.dialogCover_;
	}

	final WContainerWidget getDialogCover() {
		return getDialogCover(true);
	}

	WEnvironment getEnv() {
		return this.session_.getEnv();
	}

	void addExposedSignal(AbstractEventSignal signal) {
		String s = signal.encodeCmd();
		this.exposedSignals_.put(s, new WeakReference<AbstractEventSignal>(
				signal));
	}

	void removeExposedSignal(AbstractEventSignal signal) {
		String s = signal.encodeCmd();
		if (this.exposedSignals_.remove(s) != null) {
		} else {
			System.err.append(
					" WApplication::removeExposedSignal of non-exposed ")
					.append(s).append("??").append('\n');
		}
	}

	AbstractEventSignal decodeExposedSignal(String signalName) {
		WeakReference<AbstractEventSignal> i = this.exposedSignals_
				.get(signalName);
		if (i != null) {
			AbstractEventSignal esb = i.get();
			if (!(esb != null)) {
				return null;
			}
			WWidget w = ((i.get().getSender()) instanceof WWidget ? (WWidget) (i
					.get().getSender())
					: null);
			if (!(w != null) || this.isExposed(w)
					|| signalName.endsWith(".resized")) {
				return i.get();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	AbstractEventSignal decodeExposedSignal(String objectId, String name) {
		String signalName = (objectId.equals("app") ? this.getId() : objectId)
				+ '.' + name;
		return this.decodeExposedSignal(signalName);
	}

	Map<String, WeakReference<AbstractEventSignal>> exposedSignals() {
		return this.exposedSignals_;
	}

	private String resourceMapKey(WResource resource) {
		return resource.getInternalPath().length() == 0 ? resource.getId()
				: "/path/" + resource.getInternalPath();
	}

	String addExposedResource(WResource resource, String internalPath) {
		this.exposedResources_.put(this.resourceMapKey(resource), resource);
		String fn = resource.getSuggestedFileName().toString();
		if (fn.length() != 0 && fn.charAt(0) != '/') {
			fn = '/' + fn;
		}
		if (resource.getInternalPath().length() == 0) {
			return this.session_.getMostRelativeUrl(fn)
					+ "&request=resource&resource="
					+ DomElement.urlEncodeS(resource.getId()) + "&rand="
					+ String.valueOf(seq++);
		} else {
			fn = resource.getInternalPath() + fn;
			if (this.session_.getApplicationName().length() != 0
					&& fn.charAt(0) != '/') {
				fn = '/' + fn;
			}
			return this.session_.getMostRelativeUrl(fn);
		}
	}

	void removeExposedResource(WResource resource) {
		this.exposedResources_.remove(this.resourceMapKey(resource));
	}

	WResource decodeExposedResource(String resourceKey) {
		WResource i = this.exposedResources_.get(resourceKey);
		if (i != null) {
			return i;
		} else {
			int j = resourceKey.lastIndexOf('/');
			if (j != -1 && j > 1) {
				return this.decodeExposedResource(resourceKey.substring(0,
						0 + j));
			} else {
				return null;
			}
		}
	}

	private void enableInternalPaths() {
		if (!this.internalPathsEnabled_) {
			this.internalPathsEnabled_ = true;
			this.doJavaScript(
					this.getJavaScriptClass()
							+ "._p_.enableInternalPaths("
							+ WWebWidget
									.jsStringLiteral(this.getInternalPath())
							+ ");", false);
			if (this.session_.getApplicationName().length() == 0) {
				this
						.log("warn")
						.append(
								"Deploy-path ends with '/', using /?_= for internal paths");
			}
		}
	}

	void changeInternalPath(String aPath) {
		String path = aPath;
		if (path.length() == 0 || path.charAt(0) == '/') {
			if (!path.equals(this.newInternalPath_)) {
				String v = "";
				this.newInternalPath_ = path;
				this.internalPathChanged_.trigger(this.newInternalPath_);
			}
		}
	}

	String getAfterLoadJavaScript() {
		String result = this.afterLoadJavaScript_;
		this.afterLoadJavaScript_ = "";
		return result;
	}

	String getBeforeLoadJavaScript() {
		this.newBeforeLoadJavaScript_ = "";
		return this.beforeLoadJavaScript_;
	}

	String getNewBeforeLoadJavaScript() {
		String result = this.newBeforeLoadJavaScript_;
		this.newBeforeLoadJavaScript_ = "";
		return result;
	}

	void setExposeSignals(boolean how) {
		this.exposeSignals_ = how;
	}

	boolean isExposeSignals() {
		return this.exposeSignals_;
	}

	void constrainExposed(WWidget w) {
		this.exposedOnly_ = w;
	}

	WWidget getExposeConstraint() {
		return this.exposedOnly_;
	}

	private static boolean pathMatches(String path, String query) {
		if (query.equals(path)
				|| path.length() > query.length()
				&& path.substring(0, 0 + query.length()).equals(query)
				&& (query.charAt(query.length() - 1) == '/' || path
						.charAt(query.length()) == '/')) {
			return true;
		} else {
			return false;
		}
	}

	String getFocus() {
		return this.focusId_;
	}

	int getSelectionStart() {
		return this.selectionStart_;
	}

	int getSelectionEnd() {
		return this.selectionEnd_;
	}

	SoundManager getSoundManager() {
		if (!(this.soundManager_ != null)) {
			this.soundManager_ = new SoundManager(this);
		}
		return this.soundManager_;
	}

	private SoundManager soundManager_;
	private JSlot showLoadJS;
	private JSlot hideLoadJS;
	private static char[] gifData = { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01,
			0x00, 0x01, 0x00, 0x80, 0x00, 0x00, 0xdb, 0xdf, 0xef, 0x00, 0x00,
			0x00, 0x21, 0xf9, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00, 0x2c, 0x00,
			0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x02, 0x02, 0x44,
			0x01, 0x00, 0x3b };
	private static int seq = 0;
	static String RESOURCES_URL = "resourcesURL";
}
