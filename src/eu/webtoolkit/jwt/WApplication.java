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
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	public class HelloServlet extends WtServlet {
 * 		public HelloServlet() {
 * 			super();
 * 		}
 * 
 * 		public WApplication createApplication(WEnvironment env) {
 * 			// In practice, you will specialize WApplication and simply
 * 			// return a new instance.
 * 			WApplication app = new WApplication(env);
 * 			app.getRoot().addWidget(new WText(&quot;Hello world.&quot;));
 * 			return app;
 * 		}
 * 	}
 * }
 * </pre>
 * <p>
 * Throughout the session, the instance is available through the static method
 * {@link WApplication#getInstance() getInstance()}, which uses thread-specific
 * storage to keep track of the current session. The application may be exited
 * either using the method {@link WApplication#quit() quit()}, or because of a
 * timeout after the user has closed the window, but not because the user does
 * not interact: keep-alive messages in the background will keep the session
 * around as long as the user has the page opened.
 * <p>
 * The WApplication object provides access to session-wide settings, including:
 * <p>
 * <ul>
 * <li>circumstantial information through {@link WApplication#getEnvironment()
 * getEnvironment()}, which gives details about the user, start-up arguments,
 * and user agent capabilities.</li>
 * <li>the application title with
 * {@link WApplication#setTitle(CharSequence title) setTitle()}.</li>
 * <li>inline and external style sheets using
 * {@link WApplication#getStyleSheet() getStyleSheet()} and
 * {@link WApplication#useStyleSheet(WLink link, String media) useStyleSheet()}.
 * </li>
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
 * {@link WApplication#setCookie(String name, String value, int maxAge, String domain, String path, boolean secure)
 * setCookie()} to persist information across sessions, which may be read using
 * {@link WEnvironment#getCookie(String cookieName) WEnvironment#getCookie()} in
 * a future session.</li>
 * <li>management of the internal path (that enables browser history and
 * bookmarks) using {@link WApplication#getBookmarkUrl() getBookmarkUrl()} and
 * related methods.</li>
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
	private static Logger logger = LoggerFactory.getLogger(WApplication.class);

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
	public WApplication(final WEnvironment env) {
		super();
		this.requestTooLarge_ = new Signal1<Long>();
		this.session_ = env.session_;
		this.title_ = new WString();
		this.closeMessage_ = new WString();
		this.titleChanged_ = false;
		this.closeMessageChanged_ = false;
		this.localeChanged_ = false;
		this.styleSheet_ = new WCssStyleSheet();
		this.localizedStrings_ = null;
		this.locale_ = new Locale("");
		this.renderedInternalPath_ = "";
		this.newInternalPath_ = "";
		this.internalPathChanged_ = new Signal1<String>(this);
		this.internalPathInvalid_ = new Signal1<String>();
		this.serverPush_ = 0;
		this.serverPushChanged_ = true;
		this.javaScriptClass_ = "Wt";
		this.quitted_ = false;
		this.quittedMessage_ = new WString();
		this.onePixelGifR_ = null;
		this.internalPathsEnabled_ = false;
		this.exposedOnly_ = null;
		this.loadingIndicator_ = null;
		this.connected_ = true;
		this.htmlClass_ = "";
		this.bodyClass_ = "";
		this.bodyHtmlClassChanged_ = true;
		this.enableAjax_ = false;
		this.focusId_ = "";
		this.selectionStart_ = -1;
		this.selectionEnd_ = -1;
		this.layoutDirection_ = LayoutDirection.LeftToRight;
		this.scriptLibraries_ = new ArrayList<WApplication.ScriptLibrary>();
		this.scriptLibrariesAdded_ = 0;
		this.theme_ = null;
		this.styleSheets_ = new ArrayList<WCssStyleSheet>();
		this.styleSheetsToRemove_ = new ArrayList<WCssStyleSheet>();
		this.styleSheetsAdded_ = 0;
		this.metaHeaders_ = new ArrayList<MetaHeader>();
		this.metaLinks_ = new ArrayList<WApplication.MetaLink>();
		this.exposedSignals_ = new WeakValueMap<String, AbstractEventSignal>();
		this.exposedResources_ = new WeakValueMap<String, WResource>();
		this.encodedObjects_ = new HashMap<String, WObject>();
		this.justRemovedSignals_ = new HashSet<String>();
		this.exposeSignals_ = true;
		this.afterLoadJavaScript_ = "";
		this.beforeLoadJavaScript_ = "";
		this.newBeforeLoadJavaScript_ = 0;
		this.autoJavaScript_ = "";
		this.autoJavaScriptChanged_ = false;
		this.javaScriptPreamble_ = new ArrayList<WJavaScriptPreamble>();
		this.newJavaScriptPreamble_ = 0;
		this.javaScriptLoaded_ = new HashSet<String>();
		this.customJQuery_ = false;
		this.showLoadingIndicator_ = new EventSignal("showload", this);
		this.hideLoadingIndicator_ = new EventSignal("hideload", this);
		this.unloaded_ = new JSignal(this, "Wt-unload");
		this.objectStore_ = new HashMap<String, Object>();
		this.soundManager_ = null;
		this.showLoadJS = new JSlot();
		this.hideLoadJS = new JSlot();
		this.session_.setApplication(this);
		this.locale_ = this.getEnvironment().getLocale();
		this.renderedInternalPath_ = this.newInternalPath_ = this
				.getEnvironment().getInternalPath();
		this.internalPathIsChanged_ = false;
		this.internalPathDefaultValid_ = true;
		this.internalPathValid_ = true;
		this.theme_ = new WCssTheme("default", this);
		this.setLocalizedStrings((WLocalizedStrings) null);
		if (!this.getEnvironment().hasJavaScript()
				&& this.getEnvironment().agentIsIE()) {
			if (this.getEnvironment().getAgent().getValue() < WEnvironment.UserAgent.IE9
					.getValue()) {
				final Configuration conf = this.getEnvironment().getServer()
						.getConfiguration();
				boolean selectIE7 = conf.getUaCompatible().indexOf("IE8=IE7") != -1;
				if (selectIE7) {
					this.addMetaHeader(MetaHeaderType.MetaHttpHeader,
							"X-UA-Compatible", "IE=7");
				}
			} else {
				if (this.getEnvironment().getAgent() == WEnvironment.UserAgent.IE9) {
					this.addMetaHeader(MetaHeaderType.MetaHttpHeader,
							"X-UA-Compatible", "IE=9");
				} else {
					if (this.getEnvironment().getAgent() == WEnvironment.UserAgent.IE10) {
						this.addMetaHeader(MetaHeaderType.MetaHttpHeader,
								"X-UA-Compatible", "IE=10");
					} else {
						this.addMetaHeader(MetaHeaderType.MetaHttpHeader,
								"X-UA-Compatible", "IE=11");
					}
				}
			}
		}
		this.domRoot_ = new WContainerWidget();
		this.domRoot_.setGlobalUnfocused(true);
		this.domRoot_.setStyleClass("Wt-domRoot");
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
			this.widgetRoot_.resize(WLength.Auto, new WLength(100,
					WLength.Unit.Percentage));
		} else {
			this.ajaxMethod_ = WApplication.AjaxMethod.DynamicScriptTag;
			this.domRoot2_ = new WContainerWidget();
			this.widgetRoot_ = null;
		}
		this.styleSheet_.addRule("table",
				"border-collapse: collapse; border: 0px;border-spacing: 0px");
		this.styleSheet_.addRule("div, td, img",
				"margin: 0px; padding: 0px; border: 0px");
		this.styleSheet_.addRule("td", "vertical-align: top;");
		this.styleSheet_.addRule("td", "text-align: left;");
		this.styleSheet_.addRule(".Wt-rtl td", "text-align: right;");
		this.styleSheet_.addRule("button", "white-space: nowrap;");
		this.styleSheet_.addRule("video", "display: block");
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
						"border: 0px;margin: 0px;padding: 0px;font: inherit; cursor: pointer; cursor: hand;background: transparent;text-decoration: none;color: inherit;");
		this.styleSheet_.addRule(".Wt-wrap", "text-align: left;");
		this.styleSheet_.addRule(".Wt-rtl .Wt-wrap", "text-align: right;");
		this.styleSheet_.addRule("div.Wt-chwrap", "width: 100%; height: 100%");
		if (this.getEnvironment().agentIsIE()) {
			this.styleSheet_.addRule(".Wt-wrap", "margin: -1px 0px -3px;");
		}
		this.styleSheet_
				.addRule(
						".unselectable",
						"-moz-user-select:-moz-none;-khtml-user-select: none;-webkit-user-select: none;user-select: none;");
		this.styleSheet_
				.addRule(
						".selectable",
						"-moz-user-select: text;-khtml-user-select: normal;-webkit-user-select: text;user-select: text;");
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
		if (this.getEnvironment().supportsCss3Animations()) {
			String prefix = "";
			if (this.getEnvironment().agentIsWebKit()) {
				prefix = "webkit-";
			} else {
				if (this.getEnvironment().agentIsGecko()) {
					prefix = "moz-";
				}
			}
			this.useStyleSheet(new WLink(WApplication.getRelativeResourcesUrl()
					+ prefix + "transitions.css"));
		}
		this.setLoadingIndicator(new WDefaultLoadingIndicator());
		this.unloaded_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WApplication.this.doUnload();
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
	 * @see WApplication#url(String internalPath)
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
	 * Finds a widget by name.
	 * <p>
	 * This finds a widget in the application&apos;s widget hierarchy. It does
	 * not only consider widgets in the {@link WApplication#getRoot() getRoot()}
	 * , but also widgets that are placed outside this root, such as in dialogs,
	 * or other &quot;roots&quot; such as all the bound widgets in a widgetset
	 * application.
	 * <p>
	 * 
	 * @see WWidget#setObjectName(String name)
	 * @see WWidget#find(String name)
	 */
	public WWidget findWidget(final String name) {
		WWidget result = this.domRoot_.find(name);
		if (!(result != null) && this.domRoot2_ != null) {
			result = this.domRoot2_.find(name);
		}
		return result;
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
	 * @see WApplication#useStyleSheet(WLink link, String media)
	 * @see WWidget#setStyleClass(String styleClass)
	 */
	public WCssStyleSheet getStyleSheet() {
		return this.styleSheet_;
	}

	/**
	 * Adds an external style sheet.
	 * <p>
	 * The <code>link</code> is a link to a stylesheet.
	 * <p>
	 * The <code>media</code> indicates the CSS media to which this stylesheet
	 * applies. This may be a comma separated list of media. The default value
	 * is &quot;all&quot; indicating all media.
	 * <p>
	 * This is an overloaded method for convenience, equivalent to:
	 * 
	 * <pre>
	 *   {@code
	 *    useStyleSheet(Wt::WCssStyleSheet(link, media))
	 *   }
	 * </pre>
	 */
	public void useStyleSheet(final WLink link, final String media) {
		this.useStyleSheet(new WCssStyleSheet(link, media));
	}

	/**
	 * Adds an external style sheet.
	 * <p>
	 * Calls {@link #useStyleSheet(WLink link, String media) useStyleSheet(link,
	 * "all")}
	 */
	public final void useStyleSheet(final WLink link) {
		useStyleSheet(link, "all");
	}

	/**
	 * Conditionally adds an external style sheet.
	 * <p>
	 * This is an overloaded method for convenience, equivalent to:
	 * 
	 * <pre>
	 *   {@code
	 *    useStyleSheet(Wt::WCssStyleSheet(link, media), condition)
	 *   }
	 * </pre>
	 */
	public void useStyleSheet(final WLink link, final String condition,
			final String media) {
		this.useStyleSheet(new WCssStyleSheet(link, media), condition);
	}

	/**
	 * Adds an external stylesheet.
	 * <p>
	 * Widgets may allow configuration of their look and feel through style
	 * classes. These may be defined in an inline stylesheet, or in external
	 * style sheets.
	 * <p>
	 * External stylesheets are inserted after the internal style sheet, and can
	 * therefore override default styles set by widgets in the internal style
	 * sheet. External stylesheets must have valid link.
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
	 * 
	 * @see WApplication#getStyleSheet()
	 * @see WApplication#removeStyleSheet(WLink link)
	 * @see WWidget#setStyleClass(String styleClass)
	 */
	public void useStyleSheet(final WCssStyleSheet styleSheet,
			final String condition) {
		if (styleSheet.getLink().isNull()) {
			throw new WException(
					"WApplication::useStyleSheet stylesheet must have valid link!");
		}
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
											} catch (final RuntimeException e) {
												logger.error(new StringWriter()
														.append("Could not parse condition: '")
														.append(condition)
														.append("'").toString());
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
			for (int i = 0; i < this.styleSheets_.size(); ++i) {
				if (this.styleSheets_.get(i).getLink()
						.equals(styleSheet.getLink())
						&& this.styleSheets_.get(i).getMedia()
								.equals(styleSheet.getMedia())) {
					return;
				}
			}
			this.styleSheets_.add(styleSheet);
			++this.styleSheetsAdded_;
		}
	}

	/**
	 * Adds an external stylesheet.
	 * <p>
	 * Calls {@link #useStyleSheet(WCssStyleSheet styleSheet, String condition)
	 * useStyleSheet(styleSheet, "")}
	 */
	public final void useStyleSheet(final WCssStyleSheet styleSheet) {
		useStyleSheet(styleSheet, "");
	}

	/**
	 * Removes an external stylesheet.
	 * <p>
	 * 
	 * @see WApplication#getStyleSheet()
	 * @see WWidget#setStyleClass(String styleClass)
	 */
	public void removeStyleSheet(final WLink link) {
		for (int i = (int) this.styleSheets_.size() - 1; i > -1; --i) {
			if (this.styleSheets_.get(i).getLink().equals(link)) {
				final WCssStyleSheet sheet = this.styleSheets_.get(i);
				this.styleSheetsToRemove_.add(sheet);
				if (i > (int) this.styleSheets_.size() + this.styleSheetsAdded_
						- 1) {
					this.styleSheetsAdded_--;
				}
				this.styleSheets_.remove(0 + i);
				break;
			}
		}
	}

	/**
	 * Sets the theme.
	 * <p>
	 * The theme provides the look and feel of several built-in widgets, using
	 * CSS style rules. Rules for each theme are defined in the
	 * <code>resources/themes/</code><i>theme</i><code>/</code> folder.
	 * <p>
	 * The default theme is &quot;default&quot; CSS theme.
	 */
	public void setTheme(WTheme theme) {
		this.theme_ = theme;
	}

	/**
	 * Returns the theme.
	 */
	public WTheme getTheme() {
		return this.theme_;
	}

	/**
	 * Sets a CSS theme.
	 * <p>
	 * This sets a {@link WCssTheme} as theme.
	 * <p>
	 * The theme provides the look and feel of several built-in widgets, using
	 * CSS style rules. Rules for each CSS theme are defined in the
	 * <code>resources/themes/</code><i>name</i><code>/</code> folder.
	 * <p>
	 * The default theme is &quot;default&quot;. Setting an empty theme
	 * &quot;&quot; will result in a stub CSS theme that does not load any
	 * stylesheets.
	 */
	public void setCssTheme(final String theme) {
		this.setTheme(new WCssTheme(theme, this));
	}

	/**
	 * Sets the layout direction.
	 * <p>
	 * The default direction is LeftToRight.
	 * <p>
	 * This sets the language text direction, which by itself sets the default
	 * text alignment and reverse the column orders of &lt;table&gt; elements.
	 * <p>
	 * In addition, JWt will take this setting into account in {@link WTextEdit}, {@link WTableView} and {@link WTreeView} (so that columns are
	 * reverted), and swap the behaviour of {@link WWidget#setFloatSide(Side s)
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
	 * For example:
	 * 
	 * <pre>
	 *   {@code
	 *    body        .sidebar { float: right; }
	 *    body.Wt-rtl .sidebar { float: left; }
	 *   }
	 * </pre>
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
	public void setBodyClass(final String styleClass) {
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
	public void setHtmlClass(final String styleClass) {
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
	public void setTitle(final CharSequence title) {
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
	 * <p>
	 * 
	 * @see WApplication#setConfirmCloseMessage(CharSequence message)
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
		if (this.localizedStrings_.getItems().size() > 1) {
			return this.localizedStrings_.getItems().get(0);
		} else {
			return null;
		}
	}

	/**
	 * Accesses the built-in resource bundle.
	 * <p>
	 * This is an internal function and should not be called directly.
	 * <p>
	 * 
	 * @see WApplication#getLocalizedStrings()
	 */
	public WXmlLocalizedStrings getBuiltinLocalizedStrings() {
		return (((this.localizedStrings_.getItems().get(this.localizedStrings_
				.getItems().size() - 1)) instanceof WXmlLocalizedStrings ? (WXmlLocalizedStrings) (this.localizedStrings_
				.getItems().get(this.localizedStrings_.getItems().size() - 1))
				: null));
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
		if (!(this.localizedStrings_ != null)) {
			this.localizedStrings_ = new WCombinedLocalizedStrings();
			WXmlLocalizedStrings defaultMessages = new WXmlLocalizedStrings();
			defaultMessages.useBuiltin(WtServlet.Wt_xml);
			this.localizedStrings_.add(defaultMessages);
		}
		if (this.localizedStrings_.getItems().size() > 1) {
			WLocalizedStrings previous = this.localizedStrings_.getItems().get(
					0);
			this.localizedStrings_.remove(previous);
			;
		}
		if (translator != null) {
			this.localizedStrings_.insert(0, translator);
		}
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
	public void setLocale(final Locale locale) {
		this.locale_ = locale;
		this.localeChanged_ = true;
		this.refresh();
	}

	/**
	 * Returns the current locale.
	 * <p>
	 * 
	 * @see WApplication#setLocale(Locale locale)
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
			this.domRoot_.refresh();
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
	public void bindWidget(WWidget widget, final String domId) {
		if (this.session_.getType() != EntryPointType.WidgetSet) {
			throw new WException(
					"WApplication::bindWidget() can be used only in WidgetSet mode.");
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
	 * For example, for an application deployed at
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt 
	 *   }
	 * </pre>
	 * 
	 * this method might return
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
	public String url(final String internalPath) {
		return this.resolveRelativeUrl(this.session_
				.getMostRelativeUrl(internalPath));
	}

	/**
	 * Returns a URL for the current session.
	 * <p>
	 * Returns {@link #url(String internalPath) url("")}
	 */
	public final String url() {
		return url("");
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
	 * <p>
	 * The default implementation is not complete: it does not handle relative
	 * URL path segments with &apos;..&apos;. It just handles the cases that are
	 * necessary for {@link }.
	 * <p>
	 * This is not used in the library, except when a public URL is needed, e.g.
	 * for inclusion in an email.
	 * <p>
	 * You may want to reimplement this method when the application is hosted
	 * behind a reverse proxy or in general the public URL of the application
	 * cannot be guessed correctly by the application.
	 */
	public String makeAbsoluteUrl(final String url) {
		return this.session_.makeAbsoluteUrl(url);
	}

	/**
	 * &quot;Resolves&quot; a relative URL taking into account internal paths.
	 * <p>
	 * Using HTML5 History API or in a plain HTML session (without ugly internal
	 * paths), the internal path is present as a full part of the URL. This has
	 * a consequence that relative URLs, if not dealt with, would be resolved
	 * against the last &apos;folder&apos; name of the internal path, rather
	 * than against the application deployment path (which is what you probably
	 * want).
	 * <p>
	 * When using a widgetset mode deployment, or when configuring a baseURL
	 * property in the configuration, this method will make an absolute URL so
	 * that the property is fetched from the right server.
	 * <p>
	 * Otherwise, this method will fixup a relative URL so that it resolves
	 * correctly against the base path of an application. This does not
	 * necessarily mean that the URL is resolved into an absolute URL. In fact,
	 * JWt will simply prepend a sequence of &quot;../&quot; path elements to
	 * correct for the internal path. When passed an absolute URL (i.e. starting
	 * with &apos;/&apos;), the url is returned unchanged.
	 * <p>
	 * For URLs passed to the JWt API (and of which the library knows it is
	 * represents a URL) this method is called internally by the library. But it
	 * may be useful for URLs which are set e.g. inside a {@link WTemplate}.
	 */
	public String resolveRelativeUrl(final String url) {
		return this.session_.fixRelativeUrl(url);
	}

	/**
	 * Returns a URL for the current session.
	 * <p>
	 * Returns the (relative) URL for this application session (including the
	 * session ID if necessary). The URL includes the full application path, and
	 * is expanded by the browser into a full URL.
	 * <p>
	 * For example, for an application deployed at
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt 
	 *   }
	 * </pre>
	 * 
	 * this method might return
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
	public String getBookmarkUrl() {
		return this.getBookmarkUrl(this.newInternalPath_);
	}

	/**
	 * Returns a URL for the current session.
	 * <p>
	 * Returns the (relative) URL for this application session (including the
	 * session ID if necessary). The URL includes the full application path, and
	 * is expanded by the browser into a full URL.
	 * <p>
	 * For example, for an application deployed at
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt 
	 *   }
	 * </pre>
	 * 
	 * this method might return
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
	public String getBookmarkUrl(final String internalPath) {
		return this.session_.getBookmarkUrl(internalPath);
	}

	/**
	 * Changes the internal path.
	 * <p>
	 * A JWt application may manage multiple virtual paths. The virtual path is
	 * appended to the application URL. Depending on the situation, the path is
	 * directly appended to the application URL or it is appended using a name
	 * anchor (#).
	 * <p>
	 * For example, for an application deployed at:
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt
	 *   }
	 * </pre>
	 * 
	 * for which an <code>internalPath</code>
	 * <code>&quot;/project/z3cbc/details/&quot;</code> is set, the two forms
	 * for the application URL are:
	 * <ul>
	 * <li>
	 * in an AJAX session (HTML5):
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt/project/z3cbc/details/
	 *   }
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * in an AJAX session (HTML4):
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt#/project/z3cbc/details/
	 *   }
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * </li>
	 * <li>
	 * in a plain HTML session:
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt/project/z3cbc/details/
	 *   }
	 * </pre>
	 * 
	 * </li>
	 * </ul>
	 * <p>
	 * Note, since JWt 3.1.9, the actual form of the URL no longer affects
	 * relative URL resolution, since now JWt includes an HTML
	 * <code>meta base</code> tag which points to the deployment path,
	 * regardless of the current internal path. This does break deployments
	 * behind a reverse proxy which changes paths.
	 * <p>
	 * When the internal path is changed, an entry is added to the browser
	 * history. When the user navigates back and forward through this history
	 * (using the browser back/forward buttons), an
	 * {@link WApplication#internalPathChanged() internalPathChanged()} event is
	 * emitted. You should listen to this signal to switch the application to
	 * the corresponding state. When <code>emitChange</code> is
	 * <code>true</code>, this signal is also emitted by setting the path (but
	 * only if the path is actually changed).
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
	 * @see WApplication#getBookmarkUrl()
	 * @see WApplication#internalPathChanged()
	 */
	public void setInternalPath(final String path, boolean emitChange) {
		this.enableInternalPaths();
		if (!this.session_.getRenderer().isPreLearning() && emitChange) {
			this.changeInternalPath(path);
		} else {
			this.newInternalPath_ = path;
		}
		this.internalPathValid_ = true;
		this.internalPathIsChanged_ = true;
	}

	/**
	 * Changes the internal path.
	 * <p>
	 * Calls {@link #setInternalPath(String path, boolean emitChange)
	 * setInternalPath(path, false)}
	 */
	public final void setInternalPath(final String path) {
		setInternalPath(path, false);
	}

	/**
	 * Sets whether an internal path is valid by default.
	 * <p>
	 * This configures how you treat (invalid) internal paths. If an internal
	 * path is treated valid by default then you need to call
	 * setInternalPath(false) for an invalid path. If on the other hand you
	 * treat an internal path as invalid by default, then you need to call
	 * setInternalPath(true) for a valid path.
	 * <p>
	 * A user which opens an invalid internal path will receive a HTTP 404-Not
	 * Found response code (if sent an HTML response).
	 * <p>
	 * The default value is <code>true</code>.
	 */
	public void setInternalPathDefaultValid(boolean valid) {
		this.internalPathDefaultValid_ = valid;
	}

	/**
	 * Returns a URL for the current session.
	 * <p>
	 * Returns the (relative) URL for this application session (including the
	 * session ID if necessary). The URL includes the full application path, and
	 * is expanded by the browser into a full URL.
	 * <p>
	 * For example, for an application deployed at
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt 
	 *   }
	 * </pre>
	 * 
	 * this method might return
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
	public boolean isInternalPathDefaultValid() {
		return this.internalPathDefaultValid_;
	}

	/**
	 * Sets whether the current internal path is valid.
	 * <p>
	 * You can use this function in response to an internal path change event
	 * (or at application startup) to indicate whether the new (or initial)
	 * internal path is valid. This has only an effect on plain HTML sessions,
	 * or on the first response in an application deployed with progressive
	 * bootstrap settings, as this generates then a 404 Not-Found response.
	 * <p>
	 * 
	 * @see WApplication#internalPathChanged()
	 * @see WApplication#getBookmarkUrl()
	 */
	public void setInternalPathValid(boolean valid) {
		this.internalPathValid_ = valid;
	}

	/**
	 * Returns a URL for the current session.
	 * <p>
	 * Returns the (relative) URL for this application session (including the
	 * session ID if necessary). The URL includes the full application path, and
	 * is expanded by the browser into a full URL.
	 * <p>
	 * For example, for an application deployed at
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt 
	 *   }
	 * </pre>
	 * 
	 * this method might return
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
	public boolean isInternalPathValid() {
		return this.internalPathValid_;
	}

	/**
	 * Returns a URL for the current session.
	 * <p>
	 * Returns the (relative) URL for this application session (including the
	 * session ID if necessary). The URL includes the full application path, and
	 * is expanded by the browser into a full URL.
	 * <p>
	 * For example, for an application deployed at
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt 
	 *   }
	 * </pre>
	 * 
	 * this method might return
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
	public String getInternalPath() {
		return StringUtils.prepend(this.newInternalPath_, '/');
	}

	/**
	 * Returns a URL for the current session.
	 * <p>
	 * Returns the (relative) URL for this application session (including the
	 * session ID if necessary). The URL includes the full application path, and
	 * is expanded by the browser into a full URL.
	 * <p>
	 * For example, for an application deployed at
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt 
	 *   }
	 * </pre>
	 * 
	 * this method might return
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
	public String getInternalPathNextPart(final String path) {
		String subPath = this.internalSubPath(path);
		int t = subPath.indexOf('/');
		if (t == -1) {
			return subPath;
		} else {
			return subPath.substring(0, 0 + t);
		}
	}

	/**
	 * Returns a URL for the current session.
	 * <p>
	 * Returns the (relative) URL for this application session (including the
	 * session ID if necessary). The URL includes the full application path, and
	 * is expanded by the browser into a full URL.
	 * <p>
	 * For example, for an application deployed at
	 * 
	 * <pre>
	 *   {@code
	 *    http://www.mydomain.com/stuff/app.wt 
	 *   }
	 * </pre>
	 * 
	 * this method might return
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
	public String internalSubPath(final String path) {
		String current = StringUtils.append(this.newInternalPath_, '/');
		if (!pathMatches(current, path)) {
			logger.warn(new StringWriter().append("internalPath(): path '")
					.append(path).append("' not within current path '")
					.append(this.getInternalPath()).append("'").toString());
			return "";
		}
		return current.substring(path.length());
	}

	/**
	 * Checks if the internal path matches a given path.
	 * <p>
	 * Returns whether the current {@link WApplication#getBookmarkUrl()
	 * getBookmarkUrl()} starts with <code>path</code> (or is equal to
	 * <code>path</code>). You will typically use this method within a slot
	 * conneted to the {@link WApplication#internalPathChanged()
	 * internalPathChanged()} signal, to check that an internal path change
	 * affects the widget. It may also be useful before changing
	 * <code>path</code> using {@link WApplication#getBookmarkUrl()
	 * getBookmarkUrl()} if you do not intend to remove sub paths when the
	 * current internal path already matches <code>path</code>.
	 * <p>
	 * The <code>path</code> must start with a &apos;/&apos;.
	 * <p>
	 * 
	 * @see WApplication#getBookmarkUrl()
	 * @see WApplication#getBookmarkUrl()
	 */
	public boolean internalPathMatches(final String path) {
		if (this.session_.getRenderer().isPreLearning()) {
			return false;
		} else {
			return pathMatches(StringUtils.append(this.newInternalPath_, '/'),
					path);
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
	 * @see WApplication#getBookmarkUrl()
	 */
	public Signal1<String> internalPathChanged() {
		this.enableInternalPaths();
		return this.internalPathChanged_;
	}

	/**
	 * Signal which indicates that an invalid internal path is navigated.
	 */
	public Signal1<String> internalPathInvalid() {
		return this.internalPathInvalid_;
	}

	/**
	 * Redirects the application to another location.
	 * <p>
	 * The client will be redirected to a new location identified by
	 * <code>url</code>. Use this in conjunction with
	 * {@link WApplication#quit() quit()} if you want the application to be
	 * terminated as well.
	 * <p>
	 * Calling redirect() does not imply quit() since it may be useful to switch
	 * between a non-secure and secure (SSL) transport connection.
	 */
	public void redirect(final String url) {
		this.session_.redirect(url);
	}

	/**
	 * Returns the URL at which the resources are deployed.
	 * <p>
	 * Returns resolveRelativeUrl({@link WApplication#getRelativeResourcesUrl()
	 * getRelativeResourcesUrl()})
	 */
	public static String getResourcesUrl() {
		return WApplication.getInstance().resolveRelativeUrl(
				WApplication.getRelativeResourcesUrl());
	}

	/**
	 * Returns the URL at which the resources are deployed.
	 * <p>
	 * 
	 * @see WApplication#resolveRelativeUrl(String url)
	 */
	public static String getRelativeResourcesUrl() {
		WApplication app = WApplication.getInstance();
		final Configuration conf = app.getEnvironment().getServer()
				.getConfiguration();
		String path = conf.getProperty(WApplication.RESOURCES_URL);
		if (path == "/wt-resources/") {
			String result = app.getEnvironment().getDeploymentPath();
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
	 * 
	 * <pre>
	 *   {@code
	 *    // You need to have a reference to the application whose state
	 *    // you are about to manipulate.
	 *    WApplication app = ...;
	 *   
	 *    // Grab the application lock
	 *    WApplication.UpdateLock lock = app.getUpdateLock();
	 *   
	 *    try {
	 *      // We now have exclusive access to the application:
	 *      // we can safely modify the widget tree for example.
	 *      app.getRoot().addWidget(new WText("Something happened!"));
	 *   
	 *      // Push the changes to the browser
	 *      app.triggerUpdate();
	 *    } finally {
	 *      lock.release();
	 *    }
	 *   }
	 * </pre>
	 * <p>
	 * This works only if your servlet container supports the Servlet 3.0 API.
	 * If you try to invoke this function on a servlet container with no such
	 * support, an exception will be thrown.
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
			if (this.serverPush_ == 0
					&& !(WebSession.Handler.getInstance().getRequest() != null)) {
				logger.warn(new StringWriter()
						.append("WApplication::enableUpdates(true): should be called from within event loop")
						.toString());
			}
			++this.serverPush_;
		} else {
			--this.serverPush_;
		}
		if (enabled && this.serverPush_ == 1 || !enabled
				&& this.serverPush_ == 0) {
			this.serverPushChanged_ = true;
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
	 * When the lock to the application is released, changes will propagate to
	 * the user interface. This call only has an effect after updates have been
	 * enabled from within the normal event loop using
	 * {@link WApplication#enableUpdates(boolean enabled) enableUpdates()}.
	 * <p>
	 * This is typically used only outside of the main event loop, e.g. from
	 * another thread or from within a method posted to an application using
	 * WServer::post(), since changes always propagate within the event loop at
	 * the end of the event.
	 * <p>
	 * The update is not immediate, and thus changes that happen after this call
	 * will equally be pushed to the client.
	 * <p>
	 * 
	 * @see WApplication#enableUpdates(boolean enabled)
	 */
	public void triggerUpdate() {
		if (WebSession.Handler.getInstance().getRequest() != null) {
			return;
		}
		if (!(this.serverPush_ != 0)) {
			logger.warn(new StringWriter().append(
					"WApplication::triggerUpdate(): updates not enabled?")
					.toString());
		}
		this.session_.setTriggerUpdate(true);
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
		private static Logger logger = LoggerFactory
				.getLogger(UpdateLock.class);

		/**
		 * Releases the lock.
		 */
		public void release() {
			if (this.createdHandler_) {
				WebSession.Handler.getInstance().release();
			}
		}

		private UpdateLock(WApplication app) {
			WebSession.Handler handler = WebSession.Handler.getInstance();
			this.createdHandler_ = false;
			if (handler != null && handler.isHaveLock()
					&& handler.getSession() == app.session_) {
				return;
			}
			new WebSession.Handler(app.session_,
					WebSession.Handler.LockOption.TakeLock);
			this.createdHandler_ = true;
		}

		private boolean createdHandler_;
	}

	/**
	 * Grabs and returns the lock for manipulating widgets outside the event
	 * loop.
	 * <p>
	 * You need to keep this lock in scope while manipulating widgets outside of
	 * the event loop. In normal cases, inside the JWt event loop, you do not
	 * need to care about it.
	 * <p>
	 * 
	 * @see WApplication#enableUpdates(boolean enabled)
	 * @see WApplication#triggerUpdate()
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
	 * In most situations, it&apos;s more robust to use
	 * {@link WWidget#doJavaScript(String js) WWidget#doJavaScript()} however.
	 * <p>
	 * 
	 * @see WWidget#doJavaScript(String js)
	 * @see WApplication#declareJavaScriptFunction(String name, String function)
	 */
	public void doJavaScript(final String javascript, boolean afterLoaded) {
		if (afterLoaded) {
			this.afterLoadJavaScript_ += javascript;
			this.afterLoadJavaScript_ += '\n';
		} else {
			this.beforeLoadJavaScript_ += javascript;
			this.beforeLoadJavaScript_ += '\n';
			this.newBeforeLoadJavaScript_ += javascript.length() + 1;
		}
	}

	/**
	 * Executes some JavaScript code.
	 * <p>
	 * Calls {@link #doJavaScript(String javascript, boolean afterLoaded)
	 * doJavaScript(javascript, true)}
	 */
	public final void doJavaScript(final String javascript) {
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
	public void addAutoJavaScript(final String javascript) {
		this.autoJavaScript_ += javascript;
		this.autoJavaScriptChanged_ = true;
	}

	/**
	 * Declares an application-wide JavaScript function.
	 * <p>
	 * The function is stored in {@link WApplication#getJavaScriptClass()
	 * getJavaScriptClass()}.
	 * <p>
	 * The next code snippet declares and invokes function foo:
	 */
	public void declareJavaScriptFunction(final String name,
			final String function) {
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
	 * <p>
	 * Although JWt includes an off-the-shelf JQuery version (which can also be
	 * used by your own JavaScript code), you can override the one used by JWt
	 * and load another JQuery version instead, but this needs to be done using
	 * {@link WApplication#requireJQuery(String uri) requireJQuery()}.
	 */
	public boolean require(final String uri, final String symbol) {
		WApplication.ScriptLibrary sl = new WApplication.ScriptLibrary(uri,
				symbol);
		if (this.scriptLibraries_.indexOf(sl) == -1) {
			StringBuilder ss = new StringBuilder();
			this.streamBeforeLoadJavaScript(ss, false);
			sl.beforeLoadJS = ss.toString();
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
	public final boolean require(final String uri) {
		return require(uri, "");
	}

	/**
	 * Loads a custom JQuery library.
	 * <p>
	 * Wt ships with a rather old version of JQuery (1.4.1) which is sufficient
	 * for its needs and is many times smaller than more recent JQuery releases
	 * (about 50% smaller).
	 * <p>
	 * Using this function, you can replace Wt&apos;s JQuery version with
	 * another version of JQuery.
	 * <p>
	 * 
	 * <pre>
	 *   {@code
	 *    requireJQuery("jquery/jquery-1.7.2.min.js");
	 *   }
	 * </pre>
	 */
	public boolean requireJQuery(final String uri) {
		this.customJQuery_ = true;
		return this.require(uri);
	}

	/**
	 * Returns whether a custom JQuery library is used.
	 * <p>
	 * 
	 * @see WApplication#requireJQuery(String uri)
	 */
	public boolean isCustomJQuery() {
		return this.customJQuery_;
	}

	/**
	 * Sets the name of the application JavaScript class.
	 * <p>
	 * This should be called right after construction of the application, and
	 * changing the JavaScript class is only supported for WidgetSet mode
	 * applications. The <code>className</code> should be a valid JavaScript
	 * identifier, and should also be unique in a single page.
	 */
	public void setJavaScriptClass(final String javaScriptClass) {
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
				+ "._p_.update(null,'none',null,true);\",0);");
		this.waitForEvent();
	}

	/**
	 * Blocks the thread, waiting for an UI event.
	 * <p>
	 * This function is used by functions like
	 * {@link WDialog#exec(WAnimation animation) WDialog#exec()} or
	 * WPopupMenu::exec(), to block the current thread waiting for a new event.
	 * <p>
	 * This requires that at least one additional thread is available to process
	 * incoming requests, and is not scalable when working with a fixed size
	 * thread pools.
	 */
	public void waitForEvent() {
		if (!this.getEnvironment().isTest()) {
			this.session_.doRecursiveEventLoop();
		}
	}

	/**
	 * Reads a configuration property.
	 * <p>
	 * Tries to read a configured value for the property <code>name</code>. If
	 * no value was configured, the default <code>value</code> is returned.
	 */
	public static String readConfigurationProperty(final String name,
			final String value) {
		WebSession session = WebSession.getInstance();
		if (session != null) {
			return session.getEnv().getServer()
					.readConfigurationProperty(name, value);
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

	WObject decodeObject(final String objectId) {
		WObject i = this.encodedObjects_.get(objectId);
		if (i != null) {
			return i;
		} else {
			return null;
		}
	}

	/**
	 * Destroys the application session.
	 * <p>
	 * The application is destroyed when the session is invalidated. You should
	 * put here any logic which is needed to cleanup the application session.
	 * <p>
	 * The default implementation does nothing.
	 */
	public void destroy() {
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
	 * using {@link WEnvironment#getCookie(String cookieName)
	 * WEnvironment#getCookie()}. You cannot use a cookie to store information
	 * in the current session.
	 * <p>
	 * The name must be a valid cookie name (of type &apos;token&apos;: no
	 * special characters or separators, see RFC2616 page 16). The value may be
	 * anything. Specify the maximum age (in seconds) after which the client
	 * must discard the cookie. To delete a cookie, use a value of
	 * &apos;0&apos;.
	 * <p>
	 * By default the cookie only applies to the application deployment path (
	 * {@link WEnvironment#getDeploymentPath() WEnvironment#getDeploymentPath()}
	 * ) in the current domain. To set a proper value for domain, see also
	 * RFC2109.
	 * <p>
	 * 
	 * @see WEnvironment#supportsCookies()
	 * @see WEnvironment#getCookie(String cookieName)
	 */
	public void setCookie(final String name, final String value, int maxAge,
			final String domain, final String path, boolean secure) {
		WDate expires = WDate.getCurrentDate();
		expires = expires.addSeconds(maxAge);
		this.session_.getRenderer().setCookie(name, value, expires, domain,
				path, secure);
	}

	/**
	 * Sets a new cookie.
	 * <p>
	 * Calls
	 * {@link #setCookie(String name, String value, int maxAge, String domain, String path, boolean secure)
	 * setCookie(name, value, maxAge, "", "", false)}
	 */
	public final void setCookie(final String name, final String value,
			int maxAge) {
		setCookie(name, value, maxAge, "", "", false);
	}

	/**
	 * Sets a new cookie.
	 * <p>
	 * Calls
	 * {@link #setCookie(String name, String value, int maxAge, String domain, String path, boolean secure)
	 * setCookie(name, value, maxAge, domain, "", false)}
	 */
	public final void setCookie(final String name, final String value,
			int maxAge, final String domain) {
		setCookie(name, value, maxAge, domain, "", false);
	}

	/**
	 * Sets a new cookie.
	 * <p>
	 * Calls
	 * {@link #setCookie(String name, String value, int maxAge, String domain, String path, boolean secure)
	 * setCookie(name, value, maxAge, domain, path, false)}
	 */
	public final void setCookie(final String name, final String value,
			int maxAge, final String domain, final String path) {
		setCookie(name, value, maxAge, domain, path, false);
	}

	/**
	 * Removes a cookie.
	 * <p>
	 * 
	 * @see WApplication#setCookie(String name, String value, int maxAge, String
	 *      domain, String path, boolean secure)
	 */
	public void removeCookie(final String name, final String domain,
			final String path) {
		this.session_.getRenderer().setCookie(name, "", new WDate(1970, 1, 1),
				domain, path, false);
	}

	/**
	 * Removes a cookie.
	 * <p>
	 * Calls {@link #removeCookie(String name, String domain, String path)
	 * removeCookie(name, "", "")}
	 */
	public final void removeCookie(final String name) {
		removeCookie(name, "", "");
	}

	/**
	 * Removes a cookie.
	 * <p>
	 * Calls {@link #removeCookie(String name, String domain, String path)
	 * removeCookie(name, domain, "")}
	 */
	public final void removeCookie(final String name, final String domain) {
		removeCookie(name, domain, "");
	}

	/**
	 * Adds an HTML meta link.
	 * <p>
	 * When a link was previously set for the same <code>href</code>, its
	 * contents are replaced. When an empty string is used for the arguments
	 * <code>media</code>, <code>hreflang</code>, <code>type</code> or
	 * <code>sizes</code>, they will be ignored.
	 * <p>
	 * 
	 * @see WApplication#removeMetaLink(String href)
	 */
	public void addMetaLink(final String href, final String rel,
			final String media, final String hreflang, final String type,
			final String sizes, boolean disabled) {
		if (this.getEnvironment().hasJavaScript()) {
			logger.warn(new StringWriter().append(
					"WApplication::addMetaLink() with no effect").toString());
		}
		if (href.length() == 0) {
			throw new WException(
					"WApplication::addMetaLink() href cannot be empty!");
		}
		if (rel.length() == 0) {
			throw new WException(
					"WApplication::addMetaLink() rel cannot be empty!");
		}
		for (int i = 0; i < this.metaLinks_.size(); ++i) {
			final WApplication.MetaLink ml = this.metaLinks_.get(i);
			if (ml.href.equals(href)) {
				ml.rel = rel;
				ml.media = media;
				ml.hreflang = hreflang;
				ml.type = type;
				ml.sizes = sizes;
				ml.disabled = disabled;
				return;
			}
		}
		WApplication.MetaLink ml = new WApplication.MetaLink(href, rel, media,
				hreflang, type, sizes, disabled);
		this.metaLinks_.add(ml);
	}

	/**
	 * Removes the HTML meta link.
	 * <p>
	 * 
	 * @see WApplication#addMetaLink(String href, String rel, String media,
	 *      String hreflang, String type, String sizes, boolean disabled)
	 */
	public void removeMetaLink(final String href) {
		for (int i = 0; i < this.metaLinks_.size(); ++i) {
			final WApplication.MetaLink ml = this.metaLinks_.get(i);
			if (ml.href.equals(href)) {
				this.metaLinks_.remove(0 + i);
				return;
			}
		}
	}

	/**
	 * Adds a &quot;name&quot; HTML meta header.
	 * <p>
	 * 
	 * @see WApplication#addMetaHeader(MetaHeaderType type, String name,
	 *      CharSequence content, String lang)
	 */
	public void addMetaHeader(final String name, final CharSequence content,
			final String lang) {
		this.addMetaHeader(MetaHeaderType.MetaName, name, content, lang);
	}

	/**
	 * Adds a &quot;name&quot; HTML meta header.
	 * <p>
	 * Calls {@link #addMetaHeader(String name, CharSequence content, String lang)
	 * addMetaHeader(name, content, "")}
	 */
	public final void addMetaHeader(final String name,
			final CharSequence content) {
		addMetaHeader(name, content, "");
	}

	/**
	 * Adds an HTML meta header.
	 * <p>
	 * This method sets either a &quot;name&quot; meta headers, which configures
	 * a document property, or a &quot;http-equiv&quot; meta headers, which
	 * defines a HTTP headers (but these latter headers are being deprecated).
	 * <p>
	 * A meta header can however only be added in the following situations:
	 * <p>
	 * <ul>
	 * <li>when a plain HTML session is used (including when the user agent is a
	 * bot), you can add meta headers at any time.</li>
	 * <li>or, when DOCREF<a class="el"
	 * href="overview.html#progressive_bootstrap">progressive bootstrap</a> is
	 * used, you can set meta headers for any type of session, from within the
	 * application constructor (which corresponds to the initial request).</li>
	 * <li>but never for a {@link EntryPointType#WidgetSet} mode application
	 * since then the application is hosted within a foreign HTML page.</li>
	 * </ul>
	 * <p>
	 * These situations coincide with {@link WEnvironment#hasAjax()
	 * WEnvironment#hasAjax()} returning <code>false</code> (see
	 * {@link WApplication#getEnvironment() getEnvironment()}). The reason that
	 * it other cases the HTML page has already been rendered, and will not be
	 * rerendered since all updates are done dynamically.
	 * <p>
	 * As an alternative, you can use the &lt;meta-headers&gt; configuration
	 * property in the configuration file, which will be applied in all
	 * circumstances.
	 * <p>
	 * 
	 * @see WApplication#removeMetaHeader(MetaHeaderType type, String name)
	 */
	public void addMetaHeader(MetaHeaderType type, final String name,
			final CharSequence content, final String lang) {
		if (this.getEnvironment().hasJavaScript()) {
			logger.warn(new StringWriter().append(
					"WApplication::addMetaHeader() with no effect").toString());
		}
		for (int i = 0; i < this.metaHeaders_.size(); ++i) {
			final MetaHeader m = this.metaHeaders_.get(i);
			if (m.type == type && m.name.equals(name)) {
				if ((content.length() == 0)) {
					this.metaHeaders_.remove(0 + i);
				} else {
					m.content = WString.toWString(content);
				}
				return;
			}
		}
		if (!(content.length() == 0)) {
			this.metaHeaders_
					.add(new MetaHeader(type, name, content, lang, ""));
		}
	}

	/**
	 * Adds an HTML meta header.
	 * <p>
	 * Calls
	 * {@link #addMetaHeader(MetaHeaderType type, String name, CharSequence content, String lang)
	 * addMetaHeader(type, name, content, "")}
	 */
	public final void addMetaHeader(MetaHeaderType type, final String name,
			final CharSequence content) {
		addMetaHeader(type, name, content, "");
	}

	/**
	 * Returns a meta header value.
	 * <p>
	 * 
	 * @see WApplication#addMetaHeader(String name, CharSequence content, String
	 *      lang)
	 */
	public WString metaHeader(MetaHeaderType type, final String name) {
		for (int i = 0; i < this.metaHeaders_.size(); ++i) {
			final MetaHeader m = this.metaHeaders_.get(i);
			if (m.type == type && m.name.equals(name)) {
				return m.content;
			}
		}
		return WString.Empty;
	}

	/**
	 * Removes one or all meta headers.
	 * <p>
	 * Removes the meta header with given type and name (if it is present). If
	 * name is empty, all meta headers of the given type are removed.
	 * <p>
	 * 
	 * @see WApplication#addMetaHeader(String name, CharSequence content, String
	 *      lang)
	 */
	public void removeMetaHeader(MetaHeaderType type, final String name) {
		if (this.getEnvironment().hasJavaScript()) {
			logger.warn(new StringWriter().append(
					"removeMetaHeader() with no effect").toString());
		}
		for (int i = 0; i < this.metaHeaders_.size(); ++i) {
			final MetaHeader m = this.metaHeaders_.get(i);
			if (m.type == type && (name.length() == 0 || m.name.equals(name))) {
				this.metaHeaders_.remove(0 + i);
				if (name.length() == 0) {
					--i;
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Removes one or all meta headers.
	 * <p>
	 * Calls {@link #removeMetaHeader(MetaHeaderType type, String name)
	 * removeMetaHeader(type, "")}
	 */
	public final void removeMetaHeader(MetaHeaderType type) {
		removeMetaHeader(type, "");
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
			this.showLoadJS.setJavaScript("function(o,e) {Wt3_3_6.inline('"
					+ this.loadingIndicatorWidget_.getId() + "');}");
			this.hideLoadJS.setJavaScript("function(o,e) {Wt3_3_6.hide('"
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
		if (this.getEnvironment().agentIsIElt(7)) {
			if (!(this.onePixelGifR_ != null)) {
				WMemoryResource w = new WMemoryResource("image/gif", this);
				w.setData(gifData);
				this.onePixelGifR_ = w;
			}
			return this.onePixelGifR_.getUrl();
		} else {
			return "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
		}
	}

	String getDocType() {
		return this.session_.getDocType();
	}

	/**
	 * Quits the application.
	 * <p>
	 * This quits the application with a default restart message resolved as
	 * {@link WString#tr(String key) WString#tr()}
	 * (&quot;Wt.QuittedMessage&quot;).
	 * <p>
	 * 
	 * @see WApplication#quit(CharSequence restartMessage)
	 */
	public void quit() {
		this.quit(WString.tr("Wt.QuittedMessage"));
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
	 * If the restart message is not empty, then the user will be offered to
	 * restart the application (using the provided message) when further
	 * interacting with the application.
	 * <p>
	 * 
	 * @see WApplication#redirect(String url)
	 */
	public void quit(final CharSequence restartMessage) {
		this.quitted_ = true;
		this.quittedMessage_ = WString.toWString(restartMessage);
	}

	/**
	 * Returns whether the application has quit. (<b>deprecated</b>).
	 * <p>
	 * 
	 * @see WApplication#quit()
	 * @deprecated {@link WApplication#hasQuit() hasQuit()} is proper English
	 */
	public boolean isQuited() {
		return this.quitted_;
	}

	/**
	 * Returns whether the application has quit.
	 * <p>
	 * 
	 * @see WApplication#quit()
	 */
	public boolean hasQuit() {
		return this.quitted_;
	}

	/**
	 * Returns the current maximum size of a request to the application.
	 * <p>
	 * The returned value is the maximum request size in bytes.
	 * <p>
	 * 
	 * @see WApplication#requestTooLarge()
	 */
	public long getMaximumRequestSize() {
		return this.getEnvironment().getServer().getConfiguration()
				.getMaxRequestSize();
	}

	/**
	 * Signal which indicates that too a large request was received.
	 * <p>
	 * The integer parameter is the request size that was received in bytes.
	 */
	public Signal1<Long> requestTooLarge() {
		return this.requestTooLarge_;
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

	public void setFocus(final String id, int selectionStart, int selectionEnd) {
		this.focusId_ = id;
		this.selectionStart_ = selectionStart;
		this.selectionEnd_ = selectionEnd;
	}

	/**
	 * Loads an internal JavaScript file.
	 * <p>
	 * This is an internal function and should not be called directly.
	 * <p>
	 * 
	 * @see WApplication#require(String uri, String symbol)
	 * @see WApplication#doJavaScript(String javascript, boolean afterLoaded)
	 */
	public void loadJavaScript(String jsFile, final WJavaScriptPreamble preamble) {
		if (!this.isJavaScriptLoaded(preamble.name)) {
			this.javaScriptLoaded_.add(jsFile);
			this.javaScriptLoaded_.add(preamble.name);
			this.javaScriptPreamble_.add(preamble);
			++this.newJavaScriptPreamble_;
		}
	}

	boolean isJavaScriptLoaded(String jsFile) {
		return this.javaScriptLoaded_.contains(jsFile) != false;
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
	public void setConfirmCloseMessage(final CharSequence message) {
		if (!message.equals(this.closeMessage_)) {
			this.closeMessage_ = WString.toWString(message);
			this.closeMessageChanged_ = true;
		}
	}

	/**
	 * Sets the message for the user when the application was .
	 */
	void enableInternalPaths() {
		if (!this.internalPathsEnabled_) {
			this.internalPathsEnabled_ = true;
			this.doJavaScript(this.getJavaScriptClass()
					+ "._p_.enableInternalPaths("
					+ WWebWidget.jsStringLiteral(this.renderedInternalPath_)
					+ ");");
			if (this.session_.isUseUglyInternalPaths()) {
				logger.warn(new StringWriter()
						.append("Deploy-path ends with '/', using /?_= for internal paths")
						.toString());
			}
		}
	}

	/**
	 * Utility function to check if one path falls under another path.
	 * <p>
	 * This returns whether the <code>query</code> path matches the given
	 * <code>path</code>, meaning that it is equal to that path or it specifies
	 * a more specific sub path of that path.
	 */
	public static boolean pathMatches(final String path, final String query) {
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

	/**
	 * Encodes an untrusted URL to prevent referer leaks.
	 * <p>
	 * This encodes an URL so that in case the session ID is present in the
	 * current URL, this session ID does not leak to the refenced URL.
	 * <p>
	 * Wt will safely handle URLs in the API (in {@link WImage} and
	 * {@link WAnchor}) but you may want to use this function to encode URLs
	 * which you use in {@link WTemplate} texts.
	 */
	public String encodeUntrustedUrl(final String url) {
		boolean needRedirect = (url.indexOf("://") != -1 || url
				.startsWith("//")) && this.session_.hasSessionIdInUrl();
		if (needRedirect) {
			WtServlet c = this.session_.getController();
			return "?request=redirect&url=" + Utils.urlEncode(url) + "&hash="
					+ Utils.urlEncode(c.computeRedirectHash(url));
		} else {
			return url;
		}
	}

	/**
	 * Pushes a (modal) widget onto the expose stack.
	 * <p>
	 * This defines a new context of widgets that are currently visible.
	 */
	void pushExposedConstraint(WWidget w) {
		this.exposedOnly_ = w;
	}

	void popExposedConstraint(WWidget w) {
		assert this.exposedOnly_ == w;
		this.exposedOnly_ = null;
	}

	public void addGlobalWidget(WWidget w) {
		this.domRoot_.addWidget(w);
	}

	public void removeGlobalWidget(WWidget anon1) {
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
	 * 
	 * <pre>
	 *   {@code
	 *    void notify(WEvent event) {
	 *        // Grab resources for during request handling
	 *        try {
	 *          super.notify(event);
	 *        }  catch (MyException exception) {
	 *          // handle this exception in a central place
	 *        }
	 *        // Free resources used during request handling
	 *    }
	 *   }
	 * </pre>
	 * <p>
	 * Note that any uncaught exception throw during event handling terminates
	 * the session.
	 */
	protected void notify(final WEvent e) throws IOException {
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
		if (w == this.domRoot_) {
			return true;
		}
		if (w.getParent() == this.timerRoot_) {
			return true;
		}
		if (this.exposedOnly_ != null) {
			return this.exposedOnly_.isExposed(w);
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
		this.streamBeforeLoadJavaScript(
				this.session_.getRenderer().beforeLoadJS_, false);
		this.streamAfterLoadJavaScript(this.session_.getRenderer().beforeLoadJS_);
		this.domRoot_.enableAjax();
		if (this.domRoot2_ != null) {
			this.domRoot2_.enableAjax();
		}
		this.doJavaScript("Wt3_3_6.ajaxInternalPaths("
				+ WWebWidget.jsStringLiteral(this.resolveRelativeUrl(this
						.getBookmarkUrl("/"))) + ");");
	}

	/**
	 * Handles a browser unload event.
	 * <p>
	 * The browser unloads the application when the user navigates away or when
	 * he closes the window or tab.
	 * <p>
	 * When <code>reload-is-new-session</code> is set to <code>true</code>, then
	 * the default implementation of this method terminates this session by
	 * calling {@link WApplication#quit() quit()}, otherwise the session is
	 * scheduled to expire within seconds (since it may be a refresh).
	 * <p>
	 * You may want to reimplement this if you want to keep the application
	 * running until it times out (as was the behaviour before JWt 3.1.6).
	 */
	protected void unload() {
		this.quit();
	}

	/**
	 * handleJavaScriptError print javaScript errors to log file. You may want
	 * to overwrite it to render error page for example.
	 * <p>
	 * 
	 * @param errorText
	 *            the error will usually be in json format. }
	 */
	protected void handleJavaScriptError(final String errorText) {
		logger.error(new StringWriter().append("JavaScript error: ")
				.append(errorText).toString());
		this.quit();
	}

	private Signal1<Long> requestTooLarge_;

	static class ScriptLibrary {
		private static Logger logger = LoggerFactory
				.getLogger(ScriptLibrary.class);

		public ScriptLibrary(final String anUri, final String aSymbol) {
			this.uri = anUri;
			this.symbol = aSymbol;
			this.beforeLoadJS = "";
		}

		public String uri;
		public String symbol;
		public String beforeLoadJS;

		public boolean equals(final WApplication.ScriptLibrary other) {
			return this.uri.equals(other.uri);
		}
	}

	static class MetaLink {
		private static Logger logger = LoggerFactory.getLogger(MetaLink.class);

		public MetaLink(final String aHref, final String aRel,
				final String aMedia, final String aHreflang,
				final String aType, final String aSizes, boolean aDisabled) {
			this.href = aHref;
			this.rel = aRel;
			this.media = aMedia;
			this.hreflang = aHreflang;
			this.type = aType;
			this.sizes = aSizes;
			this.disabled = aDisabled;
		}

		public String href;
		public String rel;
		public String media;
		public String hreflang;
		public String type;
		public String sizes;
		public boolean disabled;
	}

	private WebSession session_;
	private WString title_;
	private WString closeMessage_;
	boolean titleChanged_;
	boolean closeMessageChanged_;
	boolean localeChanged_;
	private WContainerWidget widgetRoot_;
	WContainerWidget domRoot_;
	WContainerWidget domRoot2_;
	private WContainerWidget timerRoot_;
	private WCssStyleSheet styleSheet_;
	WCombinedLocalizedStrings localizedStrings_;
	private Locale locale_;
	String renderedInternalPath_;
	String newInternalPath_;
	Signal1<String> internalPathChanged_;
	private Signal1<String> internalPathInvalid_;
	boolean internalPathIsChanged_;
	private boolean internalPathDefaultValid_;
	boolean internalPathValid_;
	private int serverPush_;
	boolean serverPushChanged_;
	private String javaScriptClass_;
	private WApplication.AjaxMethod ajaxMethod_;
	private boolean quitted_;
	WString quittedMessage_;
	private WResource onePixelGifR_;
	boolean internalPathsEnabled_;
	private WWidget exposedOnly_;
	private WLoadingIndicator loadingIndicator_;
	WWidget loadingIndicatorWidget_;
	private boolean connected_;
	String htmlClass_;
	String bodyClass_;
	boolean bodyHtmlClassChanged_;
	boolean enableAjax_;
	private String focusId_;
	private int selectionStart_;
	private int selectionEnd_;
	private LayoutDirection layoutDirection_;
	List<WApplication.ScriptLibrary> scriptLibraries_;
	int scriptLibrariesAdded_;
	private WTheme theme_;
	List<WCssStyleSheet> styleSheets_;
	List<WCssStyleSheet> styleSheetsToRemove_;
	int styleSheetsAdded_;
	List<MetaHeader> metaHeaders_;
	List<WApplication.MetaLink> metaLinks_;
	private WeakValueMap<String, AbstractEventSignal> exposedSignals_;
	private WeakValueMap<String, WResource> exposedResources_;
	private Map<String, WObject> encodedObjects_;
	private Set<String> justRemovedSignals_;
	private boolean exposeSignals_;
	String afterLoadJavaScript_;
	String beforeLoadJavaScript_;
	int newBeforeLoadJavaScript_;
	String autoJavaScript_;
	boolean autoJavaScriptChanged_;
	private List<WJavaScriptPreamble> javaScriptPreamble_;
	private int newJavaScriptPreamble_;
	private Set<String> javaScriptLoaded_;
	private boolean customJQuery_;
	EventSignal showLoadingIndicator_;
	EventSignal hideLoadingIndicator_;
	private JSignal unloaded_;
	private Map<String, Object> objectStore_;

	WContainerWidget getTimerRoot() {
		return this.timerRoot_;
	}

	WEnvironment getEnv() {
		return this.session_.getEnv();
	}

	void addExposedSignal(AbstractEventSignal signal) {
		String s = signal.encodeCmd();
		this.exposedSignals_.put(s, signal);
		logger.debug(new StringWriter().append("addExposedSignal: ").append(s)
				.toString());
	}

	void removeExposedSignal(AbstractEventSignal signal) {
		String s = signal.encodeCmd();
		if (this.exposedSignals_.remove(s) != null) {
			this.justRemovedSignals_.add(s);
			logger.debug(new StringWriter().append("removeExposedSignal: ")
					.append(s).toString());
		} else {
			logger.debug(new StringWriter()
					.append("removeExposedSignal of non-exposed ").append(s)
					.append("??").toString());
		}
	}

	AbstractEventSignal decodeExposedSignal(final String signalName) {
		AbstractEventSignal i = this.exposedSignals_.get(signalName);
		if (i != null) {
			return i;
		} else {
			return null;
		}
	}

	String encodeSignal(final String objectId, final String name) {
		return objectId + '.' + name;
	}

	WeakValueMap<String, AbstractEventSignal> exposedSignals() {
		return this.exposedSignals_;
	}

	Set<String> getJustRemovedSignals() {
		return this.justRemovedSignals_;
	}

	private String resourceMapKey(WResource resource) {
		return resource.getInternalPath().length() == 0 ? resource.getId()
				: "/path/" + resource.getInternalPath();
	}

	String addExposedResource(WResource resource) {
		this.exposedResources_.put(this.resourceMapKey(resource), resource);
		String fn = resource.getSuggestedFileName().toString();
		if (fn.length() != 0 && fn.charAt(0) != '/') {
			fn = '/' + fn;
		}
		if (resource.getInternalPath().length() == 0) {
			return this.session_.getMostRelativeUrl(fn)
					+ "&request=resource&resource="
					+ Utils.urlEncode(resource.getId()) + "&rand="
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

	private boolean removeExposedResource(WResource resource) {
		String key = this.resourceMapKey(resource);
		WResource i = this.exposedResources_.get(key);
		if (i != null && i == resource) {
			this.exposedResources_.remove(key);
			return true;
		} else {
			return false;
		}
	}

	WResource decodeExposedResource(final String resourceKey) {
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

	private boolean changeInternalPath(final String aPath) {
		String path = StringUtils.prepend(aPath, '/');
		if (!path.equals(this.getInternalPath())) {
			this.renderedInternalPath_ = this.newInternalPath_ = path;
			this.internalPathValid_ = this.internalPathDefaultValid_;
			this.internalPathChanged_.trigger(this.newInternalPath_);
			if (!this.internalPathValid_) {
				this.internalPathInvalid_.trigger(this.newInternalPath_);
			}
		}
		return this.internalPathValid_;
	}

	boolean changedInternalPath(final String path) {
		if (!this.getEnvironment().isInternalPathUsingFragments()) {
			this.session_.setPagePathInfo(path);
		}
		return this.changeInternalPath(path);
	}

	void streamAfterLoadJavaScript(final StringBuilder out) {
		out.append(this.afterLoadJavaScript_);
		this.afterLoadJavaScript_ = "";
	}

	void streamBeforeLoadJavaScript(final StringBuilder out, boolean all) {
		this.streamJavaScriptPreamble(out, all);
		if (!all) {
			if (this.newBeforeLoadJavaScript_ != 0) {
				out.append(this.beforeLoadJavaScript_
						.substring(this.beforeLoadJavaScript_.length()
								- this.newBeforeLoadJavaScript_));
			}
		} else {
			out.append(this.beforeLoadJavaScript_);
		}
		this.newBeforeLoadJavaScript_ = 0;
	}

	private void streamJavaScriptPreamble(final StringBuilder out, boolean all) {
		if (all) {
			this.newJavaScriptPreamble_ = this.javaScriptPreamble_.size();
		}
		for (int i = this.javaScriptPreamble_.size()
				- this.newJavaScriptPreamble_; i < this.javaScriptPreamble_
				.size(); ++i) {
			final WJavaScriptPreamble preamble = this.javaScriptPreamble_
					.get(i);
			String scope = preamble.scope == JavaScriptScope.ApplicationScope ? this
					.getJavaScriptClass() : "Wt3_3_6";
			if (preamble.type == JavaScriptObjectType.JavaScriptFunction) {
				out.append(scope).append('.').append(preamble.name)
						.append(" = function() { return (")
						.append(preamble.src).append(").apply(").append(scope)
						.append(", arguments) };");
			} else {
				out.append(scope).append('.').append(preamble.name)
						.append(" = ").append(preamble.src).append('\n');
			}
		}
		this.newJavaScriptPreamble_ = 0;
	}

	void setExposeSignals(boolean how) {
		this.exposeSignals_ = how;
	}

	boolean isExposeSignals() {
		return this.exposeSignals_;
	}

	private void doUnload() {
		final Configuration conf = this.getEnvironment().getServer()
				.getConfiguration();
		if (conf.reloadIsNewSession()) {
			this.unload();
		} else {
			this.session_.setState(WebSession.State.Loaded, 5);
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
			this.soundManager_ = new SoundManager(this.getDomRoot());
		}
		return this.soundManager_;
	}

	private SoundManager soundManager_;
	static String RESOURCES_URL = "resourcesURL";
	private JSlot showLoadJS;
	private JSlot hideLoadJS;
	private static char[] gifData = { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01,
			0x00, 0x01, 0x00, 0x80, 0x00, 0x00, 0xdb, 0xdf, 0xef, 0x00, 0x00,
			0x00, 0x21, 0xf9, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00, 0x2c, 0x00,
			0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x02, 0x02, 0x44,
			0x01, 0x00, 0x3b };
	private static int seq = 0;
}
