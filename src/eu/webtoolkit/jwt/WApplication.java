/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import eu.webtoolkit.jwt.utils.MathUtils;
import eu.webtoolkit.jwt.utils.StringUtils;

/**
 * A class that represents an instance of a JWt Application, corresponding to a
 * single session.
 * <p>
 * 
 * Each user session of your application has a corresponding
 * {@link WApplication} instance. This instance must be created, before creating
 * widgets, and is returned by the function
 * {@link WtServlet#createApplication(WEnvironment)}. The instance is the main
 * entry point into information pertaining to a single session, and holds a
 * reference to the {@link WApplication#getRoot()} of the widget tree.
 * <p>
 * The recipe for a JWt web application, which allocates new
 * {@link WApplication} instances for every user visiting the application is
 * thus:
 * <p>
 * Throughout the application, the instance is available through
 * {@link WApplication#getInstance()}. The application may be quited either
 * using the method {@link WApplication#quit()}, or because of a timeout (when
 * the user has closed the window, or crashed its computer or was eaten by a
 * virus -- but not because the user does not interact: keep-alive messages in
 * the background will keep the session around as long as the user has the page
 * opened).
 * <p>
 * The WApplication object provides access to:
 * <ul>
 * <li>
 * {@link WEnvironment} information through
 * {@link WApplication#getEnvironment()}, which gives details about the user,
 * start-up arguments, and user agent capabilities.</li>
 * <li>
 * inline and external style sheets using {@link WApplication#getStyleSheet()}
 * and {@link WApplication#useStyleSheet(String uri)} respectively.</li>
 * <li>
 * the top-level widget (using {@link WApplication#getRoot()} for a plain
 * application) or widgets (using
 * {@link WApplication#bindWidget(WWidget widget, String domId)} for widget set
 * mode), which contains the widget hierarchy.
 * <p></li>
 * <li>
 * localization information and message resources bundles, with
 * {@link WApplication#setLocale(Locale locale)},
 * {@link WApplication#getLocale()} and setLocalizedStrings(WLocalizedStrings).
 * <p></li>
 * <li>
 * the maximum configured request size (
 * {@link WApplication#getMaximumRequestSize()}) and a signal
 * {@link WApplication#requestTooLarge()} to react to too large requests.</li>
 * <li>
 * defining cookies using
 * {@link WApplication#setCookie(String name, String value, int maxAge, String domain, String path)}
 * to persist information across sessions. These cookies may provide context
 * across sessions, and may be inspected using
 * {@link WEnvironment#getCookie(String cookieNname)} in a future session.</li>
 * <li>
 * support for internal application paths that enable browser history (back and
 * forward buttons), and bookmarks, using the
 * {@link WApplication#setInternalPath(String path, boolean emitChange)} and
 * related methods.
 * <p></li>
 * </ul>
 */
public class WApplication extends WObject {
	/**
	 * Enumeration that indicates the Ajax communication method.
	 * <p>
	 * 
	 * @see WApplication#setAjaxMethod(WApplication.AjaxMethod method)
	 */
	public enum AjaxMethod {
		/**
		 * Use the XMLHttpRequest object (real AJAX).
		 */
		XMLHttpRequest,
		/**
		 * Use dynamic script tags (for cross-domain AJAX).
		 */
		DynamicScriptTag;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Construct a {@link WApplication}.
	 */
	public WApplication(WEnvironment env) {
		super();
		this.requestTooLarge_ = new Signal1<Integer>();
		this.session_ = env.session_;
		this.title_ = new WString();
		this.titleChanged_ = false;
		this.styleSheet_ = new WCssStyleSheet();
		this.locale_ = new Locale("");
		this.oldInternalPath_ = "";
		this.newInternalPath_ = "";
		this.internalPathChanged_ = new Signal1<String>(this);
		this.javaScriptClass_ = "Wt";
		this.dialogCover_ = null;
		this.quited_ = false;
		this.onePixelGifUrl_ = "";
		this.rshLoaded_ = false;
		this.exposedOnly_ = null;
		this.loadingIndicator_ = null;
		this.connected_ = true;
		this.htmlClass_ = "";
		this.bodyClass_ = "";
		this.bodyHtmlClassChanged_ = false;
		this.scriptLibraries_ = new ArrayList<WApplication.ScriptLibrary>();
		this.scriptLibrariesAdded_ = 0;
		this.styleSheets_ = new ArrayList<String>();
		this.styleSheetsAdded_ = 0;
		this.exposedSignals_ = new HashMap<String, AbstractEventSignal>();
		this.exposedResources_ = new HashMap<String, WResource>();
		this.encodedObjects_ = new HashMap<String, WObject>();
		this.exposeSignals_ = true;
		this.afterLoadJavaScript_ = "";
		this.beforeLoadJavaScript_ = "";
		this.newBeforeLoadJavaScript_ = "";
		this.autoJavaScript_ = "";
		this.autoJavaScriptChanged_ = false;
		this.soundManager_ = null;
		this.session_.setApplication(this);
		this.locale_ = this.getEnvironment().getLocale();
		this.newInternalPath_ = this.getEnvironment().getInternalPath();
		this.internalPathIsChanged_ = false;
		this.localizedStrings_ = null;
		this.domRoot_ = new WContainerWidget();
		;
		this.domRoot_.load();
		if (this.session_.getType() == ApplicationType.Application) {
			this.domRoot_.resize(WLength.Auto, new WLength(100,
					WLength.Unit.Percentage));
		}
		this.timerRoot_ = new WContainerWidget(this.domRoot_);
		;
		this.timerRoot_.resize(WLength.Auto, new WLength(0));
		this.timerRoot_.setPositionScheme(PositionScheme.Absolute);
		if (this.session_.getType() == ApplicationType.Application) {
			this.ajaxMethod_ = WApplication.AjaxMethod.XMLHttpRequest;
			this.domRoot2_ = null;
			this.widgetRoot_ = new WContainerWidget(this.domRoot_);
			;
			this.widgetRoot_.resize(new WLength(100, WLength.Unit.Percentage),
					new WLength(100, WLength.Unit.Percentage));
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
		this.styleSheet_
				.addRule("td", "vertical-align: top; text-align: left;");
		this.styleSheet_.addRule("button", "white-space: nowrap");
		if (this.getEnvironment().getContentType() == WEnvironment.ContentType.XHTML1) {
			this.styleSheet_.addRule("button", "display: inline");
		}
		if (this.getEnvironment().agentIsIE()) {
			this.styleSheet_.addRule("html, body", "overflow: auto;");
		} else {
			if (this.getEnvironment().agentIsGecko()) {
				this.styleSheet_.addRule("html", "overflow: auto;");
			}
		}
		this.styleSheet_.addRule("iframe.Wt-resource",
				"width: 0px; height: 0px; border: 0px;");
		if (this.getEnvironment().agentIsIE()) {
			this.styleSheet_
					.addRule(
							"iframe.Wt-shim",
							"position: absolute; top: -1px; left: -1px; z-index: -1;opacity: 0; filter: alpha(opacity=0);border: none; margin: 0; padding: 0;");
		}
		this.styleSheet_
				.addRule(
						"button.Wt-wrap",
						"border: 0px !important;text-align: left;margin: 0px !important;padding: 0px !important;font-size: inherit; pointer: hand; cursor: pointer; cursor: hand;background-color: transparent;color: inherit;");
		this.styleSheet_.addRule("a.Wt-wrap", "text-decoration: none;");
		this.styleSheet_.addRule(".Wt-invalid", "background-color: #f79a9a;");
		this.styleSheet_
				.addRule(".unselectable",
						"-moz-user-select:-moz-none;-khtml-user-select: none;user-select: none;");
		this.styleSheet_
				.addRule(".selectable",
						"-moz-user-select: text;-khtml-user-select: normal;user-select: text;");
		this.styleSheet_
				.addRule(".Wt-sbspacer",
						"float: right; width: 16px; height: 1px;border: 0px; display: none;");
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
								+ (this.getEnvironment().hasJavaScript()
										&& this.getEnvironment().getAgent() != WEnvironment.UserAgent.IE6 ? "overflow:hidden"
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
		this.javaScriptResponse_ = new EventSignal1<WResponseEvent>("response",
				this, new WResponseEvent());
		this.javaScriptResponse_.addListener(this,
				new Signal1.Listener<WResponseEvent>() {
					public void trigger(WResponseEvent e1) {
						WApplication.this.handleJavaScriptResponse(e1);
					}
				});
		this.showLoadingIndicator_ = new EventSignal("showload", this);
		this.hideLoadingIndicator_ = new EventSignal("hideload", this);
		this.setLoadingIndicator(new WDefaultLoadingIndicator());
	}

	/**
	 * Destructor.
	 * <p>
	 * The destructor deletes the {@link WApplication#getRoot()} container, and
	 * as a consequence the entire widget tree.
	 */
	public void destroy() {
		/* delete this.javaScriptResponse_ */;
		/* delete this.showLoadingIndicator_ */;
		/* delete this.hideLoadingIndicator_ */;
		this.dialogCover_ = null;
		WContainerWidget tmp = this.domRoot_;
		this.domRoot_ = null;
		if (tmp != null)
			tmp.remove();
		tmp = this.domRoot2_;
		this.domRoot2_ = null;
		if (tmp != null)
			tmp.remove();
		/* delete this.localizedStrings_ */;
		this.styleSheet_.clear();
		this.session_.setApplication((WApplication) null);
	}

	/**
	 * Returns the current application instance.
	 * <p>
	 * In a multi-threaded server, it returns the thread-specific application
	 * instance (using thread-specific storage).
	 */
	public static WApplication getInstance() {
		WebSession session = WebSession.getInstance();
		return session != null ? session.getApp() : null;
	}

	/**
	 * Returns the application environment.
	 * <p>
	 * This is the environment that was used when constructing the application.
	 * The environment contains all settings that constrain the application from
	 * outside.
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
	 * <p>
	 * <i><b>Note:</b>The {@link WApplication#getRoot()} is only defined in
	 * normal Application mode. For WidgetSet mode there is no
	 * {@link WApplication#getRoot()} container, and null is returned. Instead,
	 * use {@link WApplication#bindWidget(WWidget widget, String domId)} to bind
	 * root widgets to existing HTML &lt;div&gt; elements on the page. </i>
	 * </p>
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
	 * 
	 * @see WApplication#useStyleSheet(String uri)
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
	 * The <i>uri</i> indicates a relative or absolute URL to the stylesheet.
	 * <p>
	 * External stylesheets are inserted after the internal style sheet, and can
	 * therefore override default styles set by widgets in the internal style
	 * sheet.
	 * <p>
	 * 
	 * @see WApplication#getStyleSheet()
	 * @see WApplication#useStyleSheet(String uri, String condition)
	 */
	public void useStyleSheet(String uri) {
		this.styleSheets_.add(uri);
		++this.styleSheetsAdded_;
	}

	/**
	 * Adds an external style sheet, conditional for IE.
	 * <p>
	 * <i>condition</i> is a string that is used to apply the stylesheet to
	 * specific versions of IE. Only a limited subset of the IE conditional
	 * comments syntax is supported. Examples are:
	 * <ul>
	 * <li>&quot;IE gte 6&quot;: only for IE version 6 or later.</li>
	 * <li>&quot;!IE gte 6&quot;: only for IE versions prior to IE6.</li>
	 * <li>&quot;IE lte 7&quot;: only for IE versions prior to IE7.</li>
	 * </ul>
	 * <p>
	 * The <i>uri</i> indicates a relative or absolute URL to the stylesheet.
	 * <p>
	 * 
	 * @see WApplication#useStyleSheet(String uri)
	 */
	public void useStyleSheet(String uri, String condition) {
		if (this.getEnvironment().agentIsIE()) {
			boolean display = false;
			int thisVersion = 4;
			switch (this.getEnvironment().getAgent()) {
			case IEMobile:
				thisVersion = 5;
				break;
			case IE6:
				thisVersion = 6;
				break;
			default:
				thisVersion = 7;
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
										&& r.substring(0, 0 + 3).equals("gt ")) {
									r = r.substring(3);
									cond = gt;
								} else {
									if (r.length() >= 3
											&& r.substring(0, 0 + 3).equals(
													"gte ")) {
										r = r.substring(4);
										cond = gte;
									} else {
										try {
											int version = Integer.parseInt(r);
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
										} catch (Exception e) {
											this
													.log("error")
													.append(
															"Could not parse condition: '")
													.append(condition).append(
															"'");
										}
										r = "";
									}
								}
							}
						}
					}
				}
			}
			if (display) {
				this.useStyleSheet(uri);
			}
		}
	}

	/**
	 * Set the title.
	 * <p>
	 * Set the title that appears as the browser window title.
	 * <p>
	 * The default title is &quot;&quot;.
	 * <p>
	 * 
	 * @see WApplication#getTitle()
	 */
	public void setTitle(CharSequence title) {
		if (this.getSession().getRenderer().isPreLearning()
				|| !this.title_.equals(title)) {
			this.title_ = WString.toWString(title);
			this.titleChanged_ = true;
		}
	}

	/**
	 * Returns the title.
	 * <p>
	 * 
	 * @see WApplication#setTitle(CharSequence title)
	 */
	public WString getTitle() {
		return this.title_;
	}

	/**
	 * Returns the object that provides localized strings.
	 * <p>
	 * You can set a class implementing {@link WLocalizedStrings} using
	 * {@link WApplication#setLocalizedStrings(WLocalizedStrings translator)}.
	 * <p>
	 * 
	 * @see WString#tr(String key)
	 */
	public WLocalizedStrings getLocalizedStrings() {
		return this.localizedStrings_;
	}

	/**
	 * Set the string translator.
	 * <p>
	 * The string translator resolves localized strings in the current locale.
	 * The previous string translator is deleted, and ownership is transferred
	 * to the application.
	 * <p>
	 * 
	 * @see WApplication#getLocalizedStrings()
	 * @see WString#tr(String key)
	 */
	public void setLocalizedStrings(WLocalizedStrings translator) {
		/* delete this.localizedStrings_ */;
		this.localizedStrings_ = translator;
	}

	/**
	 * Changes the locale.
	 * <p>
	 * By passing a <i>locale</i> that is an empty string, the default locale is
	 * chosen. The locale is used by the string translator to resolve
	 * internationalized strings.
	 * <p>
	 * When the locale gets changed, {@link WApplication#refresh()} is called,
	 * which will resolve the strings in the new locale.
	 * <p>
	 * The default locale is copied from the environment (
	 * {@link WEnvironment#getLocale()}), and is the locale that was configured
	 * by the user in his browser preferences.
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
	 * Returns the currently used locale.
	 * <p>
	 */
	public Locale getLocale() {
		return this.locale_;
	}

	/**
	 * Refresh the application.
	 * <p>
	 * Causes the application to refresh its data, including messages from
	 * message-resource bundles. This done by propagating
	 * {@link WWidget#refresh()} through the widget hierarchy.
	 * <p>
	 * This method is also called when the user hits the refresh (or reload)
	 * button, in case the application is configured to not create a new session
	 * in response.
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
	}

	/**
	 * Bind a top-level widget for a WidgetSet deployment.
	 * <p>
	 * This method binds a widget to an existing element on the page. The
	 * element type should correspond with the widget type (e.g. it should be a
	 * &lt;div&gt; for a {@link WContainerWidget}, or a &lt;table&gt; for a
	 * {@link WTable}).
	 * <p>
	 * 
	 * @see WApplication#getRoot()
	 * @see ApplicationType#WidgetSet
	 */
	public void bindWidget(WWidget widget, String domId) {
		if (this.session_.getType() != ApplicationType.WidgetSet) {
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
	 * For example, for an application deployed at <code>
   http://www.mydomain.com/stuff/app.wt
  </code>
	 * this method would return
	 * <code>&quot;/stuff/app.wt?wtd=AbCdEf&quot;</code>, when using URL
	 * rewriting for session-tracking or
	 * <code>&quot;/stuff/app.wt?a=a&quot;</code> when using cookies for
	 * session-tracking
	 * <p>
	 * . As in each case, a query is appended at the end of the URL, additional
	 * query parameters can be appended in the form of
	 * <code>&quot;&amp;param1=value&amp;param2=value&quot;</code>.
	 * <p>
	 * To obtain a URL that is suitable for bookmarking the current application
	 * state, to be used across sessions, use
	 * {@link WApplication#getBookmarkUrl()} instead.
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
	 * Returns a bookmarkable URL, including the internal path.
	 * <p>
	 * Is equivalent to
	 * <code>bookmarkUrl({@link WApplication#getInternalPath()})</code>, see
	 * {@link WApplication#getBookmarkUrl(String internalPath)}.
	 * <p>
	 * To obtain a URL that is refers to the current session of the application,
	 * use {@link WApplication#getUrl()} instead.
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
	 * internal path <i>internalPath</i>, usable across sessions. The URL is
	 * relative and expanded into a full URL by the browser.
	 * <p>
	 * For example, for an application with current URL: <code>
   http://www.mydomain.com/stuff/app.wt#/project/internal/
  </code>
	 * when called with <code>&quot;/project/external&quot;</code>, this method
	 * would return:
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
	 * You can use {@link WApplication#getBookmarkUrl()} as the destination for
	 * a {@link WAnchor}, and listen to a click event is attached to a slot that
	 * switches to the internal path <i>internalPath</i> (see
	 * {@link WAnchor#setRefInternalPath(String path)}). In this way, an anchor
	 * can be used to switch between internal paths within an application
	 * regardless of the situation (browser with or without Ajax support, or a
	 * web spider bot), but still generates suitable URLs across sessions, which
	 * can be used for bookmarking, opening in a new window/tab, or indexing.
	 * <p>
	 * To obtain a URL that refers to the current session of the application,
	 * use {@link WApplication#getUrl()} instead.
	 * <p>
	 * 
	 * @see WApplication#getUrl()
	 * @see WApplication#getBookmarkUrl()
	 */
	public String getBookmarkUrl(String internalPath) {
		if (!this.getEnvironment().hasJavaScript()) {
			if (this.getEnvironment().agentIsSpiderBot()) {
				return this.session_.getBookmarkUrl(internalPath);
			} else {
				return this.session_.getMostRelativeUrl(internalPath);
			}
		} else {
			return this.session_.getBookmarkUrl(internalPath);
		}
	}

	/**
	 * Change the internal path.
	 * <p>
	 * A JWt application may manage multiple virtual paths. The virtual path is
	 * appended to the application URL. Depending on the situation, the path is
	 * directly appended to the application URL or it is appended using a name
	 * anchor (#).
	 * <p>
	 * For example, for an application deployed at: <code>
   http://www.mydomain.com/stuff/app.wt
  </code>
	 * for which an <i>internalPath</i>
	 * <code>&quot;/project/z3cbc/details/&quot;</code> is set, the two forms
	 * for the application URL are:
	 * <ul>
	 * <li>
	 * in a browser with AJAX: <code>
   http://www.mydomain.com/stuff/app.wt#/project/z3cbc/details/
  </code>
	 * </li>
	 * <li>
	 * or in other situations (no JavaScript): <code>
   http://www.mydomain.com/stuff/app.wt/project/z3cbc/details/
  </code>
	 * This has as major consequence that from the browser stand point, the
	 * application now serves many different URLs. As a consequence, relative
	 * URLs will break. Still, you can specify relative URLs within your
	 * application (in for example {@link WAnchor#setRef(String ref)} or
	 * {@link WImage#setImageRef(String ref)}) since JWt will transform them to
	 * absolute URLs when needed. But, this in turn may break deployments behind
	 * reverse proxies when the context paths differ. For the same reason, you
	 * will need to use absolute URLs in any XHTML or CSS you write manually. <br>
	 * This type of URLs are only used when the your application is deployed at
	 * a location that does not end with a &apos;/&apos;. Otherwise, JWt will
	 * generate URLS like: <code>
   http://www.mydomain.com/stuff/?_=/project/z3cbc/details/
  </code>
	 * </li>
	 * </ul>
	 * <p>
	 * When the internal path is changed, an entry is added to the browser
	 * history. When the user navigates back and forward through this history
	 * (using the browser back/forward buttons), an
	 * {@link WApplication#internalPathChanged()} event is emitted. You should
	 * listen to this signal to switch the application to the corresponding
	 * state. When <i>emitChange</i> is true, this signal is also emitted by
	 * setting the path.
	 * <p>
	 * A url that includes the internal path may be obtained using
	 * {@link WApplication#getBookmarkUrl()}.
	 * <p>
	 * The <i>internalPath</i> must start with a &apos;/&apos;. In this way, you
	 * can still use normal anchors in your HTML. Internal path changes
	 * initiated in the browser to paths that do not start with a &apos;/&apos;
	 * are ignored.
	 * <p>
	 * 
	 * @see WApplication#getBookmarkUrl()
	 * @see WApplication#getInternalPath()
	 * @see WApplication#internalPathChanged()
	 */
	public void setInternalPath(String path, boolean emitChange) {
		this.isLoadRsh();
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
	 * {@link WEnvironment#getInternalPath()}.
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
	 * Returns part of the current internal path.
	 * <p>
	 * This is a convenience method which returns the next folder in the
	 * internal path, after the given <i>path</i>.
	 * <p>
	 * For example, when the current internal path is
	 * <code>&quot;/project/z3cbc/details&quot;</code>, this method returns
	 * <code>&quot;details&quot;</code> when called with
	 * <code>&quot;/project/z3cbc/&quot;</code> as <i>path</i> argument.
	 * <p>
	 * The <i>path</i> must start with a &apos;/&apos;, and
	 * {@link WApplication#internalPathMatches(String path)} should evaluate to
	 * <i>true</i> for the given <i>path</i>. If not, an empty string is
	 * returned and an error message is logged.
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
	 * Returns whether the current {@link WApplication#getInternalPath()} starts
	 * with <i>path</i> (or is equal to <i>path</i>). You will typically use
	 * this method within a slot conneted to the
	 * {@link WApplication#internalPathChanged()} signal, to check that an
	 * internal path change affects the widget. It may also be useful before
	 * changing <i>path</i> using
	 * {@link WApplication#setInternalPath(String path, boolean emitChange)} if
	 * you do not intend to remove sub paths when the current internal path
	 * already matches <i>path</i>.
	 * <p>
	 * The <i>path</i> must start with a &apos;/&apos;.
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
		return this.internalPathChanged_;
	}

	/**
	 * Redirects the application to another location.
	 * <p>
	 * The client will be redirected to a new location. Use this in conjunction
	 * with {@link WApplication#quit()} if you want to the application to be
	 * terminated as well.
	 * <p>
	 * Calling {@link WApplication#redirect(String url)} does not imply
	 * {@link WApplication#quit()} since it may be useful to switch between a
	 * non-secure and secure (SSL) transport connection.
	 */
	public void redirect(String url) {
		this.session_.redirect(url);
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

	public WebSession getSession() {
		return this.session_;
	}

	/**
	 * Attach an auxiliary thread to this application.
	 * <p>
	 * In a multi-threaded environment, {@link WApplication#getInstance()} uses
	 * thread-local data to retrieve the application object that corresponds to
	 * the session currently being handled by the thread. This is set
	 * automatically by the library whenever an event is delivered to the
	 * application, or when you use the getUpdateLock() to modify the
	 * application from an auxiliary thread outside the normal event loop.
	 * <p>
	 * When you want to manipulate the widget tree inside the main event loop,
	 * but from within an auxiliary thread, then you cannot use the
	 * getUpdateLock() since this will create an immediate dead lock. Instead,
	 * you may attach the auxiliary thread to the application, by calling this
	 * method from the auxiliary thread, and in this way you can modify the
	 * application from within that thread without needing the update lock.
	 */
	public void attachThread() {
		WebSession.Handler.attachThreadToSession(this.session_);
	}

	/**
	 * Executes some JavaScript code.
	 * <p>
	 * This method may be used to call some custom JavaScript code as part of an
	 * event response.
	 * <p>
	 * This function does not wait until the JavaScript is run, but returns
	 * immediately. The JavaScript will be run after the normal event handling,
	 * unless <i>afterLoaded</i> is set to false.
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

	public void addAutoJavaScript(String javascript) {
		this.autoJavaScript_ += javascript;
		this.autoJavaScriptChanged_ = true;
	}

	public void declareJavaScriptFunction(String name, String function) {
		this.doJavaScript(this.javaScriptClass_ + '.' + name + '=' + function
				+ ';', false);
	}

	/**
	 * Load a JavaScript library.
	 * <p>
	 * Attempt to load a JavaScript library. When <i>symbol</i> is not empty,
	 * the library is only inserted in the page if the given symbol is not yet
	 * defined.
	 * <p>
	 * Returns true when the library was not yet loaded for this application.
	 * <p>
	 * JavaScript libraries may be loaded at any point in time. They will be
	 * loaded before evaluating the normal event handling code, but after
	 * javaScript that has been executed using doJavaScript(..., false).
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
	 * Load a JavaScript library.
	 * <p>
	 * Returns {@link #require(String uri, String symbol) require(uri, "")}
	 */
	public final boolean require(String uri) {
		return require(uri, "");
	}

	/**
	 * Process UI events.
	 * <p>
	 * You may call this method during a long operation to:
	 * <ul>
	 * <li>
	 * Propagate widget changes to the client.</li>
	 * <li>
	 * Process UI events.</li>
	 * </ul>
	 * <p>
	 * This method starts a recursive event loop, blocking the current thread,
	 * and resumes when all events have been processed.
	 */
	public void processEvents() {
		this.session_
				.doRecursiveEventLoop("setTimeout(\"" + this.javaScriptClass_
						+ "._p_.update(null,'"
						+ this.javaScriptResponse_.encodeCmd()
						+ "',null,false);\",0);");
	}

	public static String readConfigurationProperty(String name, String value) {
		String property = WApplication.getInstance().session_.getController()
				.getConfiguration().getProperty(name);
		if (property != null) {
			return property;
		} else {
			return value;
		}
	}

	/**
	 * Set the Ajax communication method.
	 * <p>
	 * You may change the communication method only from within the application
	 * constructor.
	 * <p>
	 * The default method depends on your application deployment type.
	 * <p>
	 * For plain applications, {@link WApplication.AjaxMethod#XMLHttpRequest
	 * XMLHttpRequest} is used, while for WidgetSet applications,
	 * {@link WApplication.AjaxMethod#DynamicScriptTag DynamicScriptTag} is
	 * used. The latter is less efficient, but has the benefit to allow serving
	 * the application from a different server than the page that hosts the
	 * embedded widgets.
	 */
	public void setAjaxMethod(WApplication.AjaxMethod method) {
		this.ajaxMethod_ = method;
	}

	/**
	 * Returns the Ajax communication method.
	 * <p>
	 * 
	 * @see WApplication#setAjaxMethod(WApplication.AjaxMethod method)
	 */
	public WApplication.AjaxMethod getAjaxMethod() {
		return this.ajaxMethod_;
	}

	public String getJavaScriptClass() {
		return this.javaScriptClass_;
	}

	public WContainerWidget getDomRoot() {
		return this.domRoot_;
	}

	public WContainerWidget getDomRoot2() {
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

	public String fixRelativeUrl(String url) {
		if (url.indexOf("://") != -1) {
			return url;
		}
		if (url.length() > 0 && url.charAt(0) == '#') {
			return url;
		}
		if (this.ajaxMethod_ == WApplication.AjaxMethod.XMLHttpRequest) {
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
			if (url.length() != 0) {
				if (url.charAt(0) != '/') {
					return this.session_.getAbsoluteBaseUrl() + url;
				} else {
					return this.getEnvironment().getUrlScheme() + "://"
							+ this.getEnvironment().getHostName() + url;
				}
			} else {
				return url;
			}
		}
	}

	public static String getResourcesUrl() {
		String path = "/wt-resources/";
		readConfigurationProperty("resourcesURL", path);
		String result = WApplication.getInstance().getEnvironment()
				.getDeploymentPath();
		if (result.length() != 0 && result.charAt(result.length() - 1) == '/') {
			return result + path.substring(1);
		} else {
			return result + path;
		}
	}

	/**
	 * Initialize the application, post-construction.
	 * <p>
	 * This method is invoked by the JWt library after construction of a new
	 * application. You may reimplement this method to do additional
	 * initialization that is not possible from the constructor (e.g. which uses
	 * virtual methods).
	 */
	public void initialize() {
	}

	/**
	 * Finalize the application, pre-destruction.
	 * <p>
	 * This method is invoked by the JWt library before destruction of a new
	 * application. You may reimplement this method to do additional
	 * finalization that is not possible from the destructor (e.g. which uses
	 * virtual methods).
	 */
	public void finalize() {
	}

	/**
	 * Change the threshold for two-phase rendering.
	 * <p>
	 * This changes the threshold for the communication size (in bytes) to
	 * render invisible changes in one go. If the bandwidth for rendering the
	 * invisible changes exceed the threshold, they will be fetched in a second
	 * communication, after the visible changes have been rendered.
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
	 * Set a new cookie.
	 * <p>
	 * Use cookies to transfer information across different sessions (e.g. a
	 * user name). In a subsequent session you will be able to read this cookie
	 * using {@link WEnvironment#getCookie(String cookieNname)}. You cannot use
	 * a cookie to store information in the current session.
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
	 * <p>
	 * <i><b>Note:</b>Wt provides session tracking automatically (and may be
	 * configured to use a cookie for this). You only need this method if you
	 * want to remember information <i>across</i> sessions.</i>
	 * </p>
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
	 * Set a new cookie.
	 * <p>
	 * Calls
	 * {@link #setCookie(String name, String value, int maxAge, String domain, String path)
	 * setCookie(name, value, maxAge, "", "")}
	 */
	public final void setCookie(String name, String value, int maxAge) {
		setCookie(name, value, maxAge, "", "");
	}

	/**
	 * Set a new cookie.
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
	 * Returns the current maximum size of a request to the application.
	 * <p>
	 * 
	 * @see WApplication#requestTooLarge()
	 */
	public int getMaximumRequestSize() {
		return this.session_.getController().getConfiguration()
				.getMaxRequestSize() * 1024;
	}

	/**
	 * Add an entry to the application log.
	 * <p>
	 * Starts a new log entry of the given <i>type</i> in the JWt application
	 * log file. This method returns a stream-like object to which the message
	 * may be streamed.
	 */
	public WLogEntry log(String type) {
		return this.session_.log(type);
	}

	/**
	 * Set the loading indicator.
	 * <p>
	 * The loading indicator is shown to indicate that a response from the
	 * server is pending or JavaScript is being evaluated.
	 * <p>
	 * The default loading indicator is a {@link WDefaultLoadingIndicator}.
	 * <p>
	 * When setting a new loading indicator, the previous one is deleted.
	 */
	public void setLoadingIndicator(WLoadingIndicator indicator) {
		/* delete this.loadingIndicator_ */;
		this.loadingIndicator_ = indicator;
		if (this.loadingIndicator_ != null) {
			this.loadingIndicatorWidget_ = indicator.getWidget();
			this.domRoot_.addWidget(this.loadingIndicatorWidget_);
			JSlot showLoadJS = new JSlot();
			showLoadJS.setJavaScript("function(obj, e) {Wt2_99_4.inline('"
					+ this.loadingIndicatorWidget_.getId() + "');}");
			this.showLoadingIndicator_.addListener(showLoadJS);
			JSlot hideLoadJS = new JSlot();
			hideLoadJS.setJavaScript("function(obj, e) {Wt2_99_4.hide('"
					+ this.loadingIndicatorWidget_.getId() + "');}");
			this.hideLoadingIndicator_.addListener(hideLoadJS);
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

	public String getOnePixelGifUrl() {
		if (this.onePixelGifUrl_.length() == 0) {
			WMemoryResource w = new WMemoryResource("image/gif", this);
			w.setData(gifData);
			this.onePixelGifUrl_ = w.generateUrl();
		}
		return this.onePixelGifUrl_;
	}

	public String getDocType() {
		return this.session_.getDocType();
	}

	/**
	 * Quit the application.
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
	 * Even better is to {@link WApplication#redirect(String url)} the user to
	 * another, static, page in conjunction with {@link WApplication#quit()}.
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
	 * Signal which indicates that too a large POST was received.
	 * <p>
	 * The integer parameter is the request that was received in bytes.
	 */
	public Signal1<Integer> requestTooLarge() {
		return this.requestTooLarge_;
	}

	public void redirectToSession(String newSessionId) {
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

	public boolean isConnected() {
		return this.connected_;
	}

	public void setBodyClass(String styleClass) {
		this.bodyClass_ = styleClass;
		this.bodyHtmlClassChanged_ = true;
	}

	public void setHtmlClass(String styleClass) {
		this.htmlClass_ = styleClass;
		this.bodyHtmlClassChanged_ = true;
	}

	public boolean isDebug() {
		return this.session_.isDebug();
	}

	/**
	 * Notifies an event to the application.
	 * <p>
	 * This method is called by the event loop for propagating an event to the
	 * application. It provides a single point of entry for events to the
	 * application.
	 * <p>
	 * You may want to reimplement this method for two reasons:
	 * <ul>
	 * <li>for having a single point for exception handling (you may want to
	 * catch specialized exceptions at specific points, but general (fatal)
	 * exceptions may be caught here.</li>
	 * <li>you want to manage resource usage during requests. For example, at
	 * the end of request handling, you want to return a database session back
	 * to the pool.</li>
	 * </ul>
	 * <p>
	 * In either case, you will need to call the base class implementation
	 * {@link WApplication#notify(WEvent e)}, as otherwise no events will be
	 * delivered to your application.
	 */
	protected void notify(WEvent e) throws IOException {
		e.getSession().notify(e);
	}

	/**
	 * Returns whether a widget is exposed in the interface.
	 * <p>
	 * The default implementation simply returns true, unless a modal dialog is
	 * active, in which case it returns true only for widgets that are inside
	 * the dialog.
	 * <p>
	 * You may want to reimplement this method if you wish to disallow events
	 * from certain widgets even when they are inserted in the widget hierachy.
	 */
	protected boolean isExposed(WWidget w) {
		if (this.exposedOnly_ != null) {
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
	 * Progress to an Ajax-enabled user interface.
	 * <p>
	 * This method is called when the progressive bootstrap method is used, and
	 * support for AJAX has been detected. The default behavior will propagate
	 * the {@link WWidget#enableAjax()} method through the widget hierarchy.
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
		this.domRoot_.enableAjax();
		if (this.domRoot2_ != null) {
			this.domRoot2_.enableAjax();
		}
	}

	protected void exposeOnly(WWidget w) {
		this.exposedOnly_ = w;
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

	private Map<String, AbstractEventSignal> SignalMap;
	private Map<String, WResource> ResourceMap;
	private Map<String, WObject> ObjectMap;
	private WebSession session_;
	private WString title_;
	boolean titleChanged_;
	private WContainerWidget widgetRoot_;
	WContainerWidget domRoot_;
	WContainerWidget domRoot2_;
	private WContainerWidget timerRoot_;
	private WCssStyleSheet styleSheet_;
	private WLocalizedStrings localizedStrings_;
	private Locale locale_;
	String oldInternalPath_;
	String newInternalPath_;
	Signal1<String> internalPathChanged_;
	boolean internalPathIsChanged_;
	private String javaScriptClass_;
	private WApplication.AjaxMethod ajaxMethod_;
	private WContainerWidget dialogCover_;
	private boolean quited_;
	private String onePixelGifUrl_;
	private boolean rshLoaded_;
	private WWidget exposedOnly_;
	private WLoadingIndicator loadingIndicator_;
	WWidget loadingIndicatorWidget_;
	private boolean connected_;
	String htmlClass_;
	String bodyClass_;
	boolean bodyHtmlClassChanged_;
	boolean enableAjax_;
	List<WApplication.ScriptLibrary> scriptLibraries_;
	int scriptLibrariesAdded_;
	List<String> styleSheets_;
	int styleSheetsAdded_;
	private Map<String, AbstractEventSignal> exposedSignals_;
	private Map<String, WResource> exposedResources_;
	private Map<String, WObject> encodedObjects_;
	private boolean exposeSignals_;
	private String afterLoadJavaScript_;
	String beforeLoadJavaScript_;
	String newBeforeLoadJavaScript_;
	String autoJavaScript_;
	boolean autoJavaScriptChanged_;
	private EventSignal1<WResponseEvent> javaScriptResponse_;
	EventSignal showLoadingIndicator_;
	EventSignal hideLoadingIndicator_;

	WContainerWidget getTimerRoot() {
		return this.timerRoot_;
	}

	WContainerWidget getDialogCover(boolean create) {
		if (this.dialogCover_ == null && create) {
			this.dialogCover_ = new WContainerWidget(this.domRoot_);
			this.dialogCover_.setStyleClass("Wt-dialogcover");
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
		this.exposedSignals_.put(s, signal);
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
		AbstractEventSignal i = this.exposedSignals_.get(signalName);
		if (i != null) {
			WWidget w = ((i.getSender()) instanceof WWidget ? (WWidget) (i
					.getSender()) : null);
			if (!(w != null) || this.isExposed(w)) {
				return i;
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

	Map<String, AbstractEventSignal> exposedSignals() {
		return this.exposedSignals_;
	}

	String addExposedResource(WResource resource) {
		this.exposedResources_.put(resource.getId(), resource);
		String fn = resource.getSuggestedFileName();
		if (fn.length() != 0 && fn.charAt(0) != '/') {
			fn = '/' + fn;
		}
		return this.session_.getMostRelativeUrl(fn)
				+ "&request=resource&resource="
				+ DomElement.urlEncodeS(resource.getId()) + "&rand="
				+ String.valueOf(MathUtils.randomInt());
	}

	void removeExposedResource(WResource resource) {
		this.exposedResources_.remove(resource.getId());
	}

	WResource decodeExposedResource(String resourceName) {
		WResource i = this.exposedResources_.get(resourceName);
		if (i != null) {
			return i;
		} else {
			return null;
		}
	}

	private boolean isLoadRsh() {
		if (!this.rshLoaded_) {
			this.rshLoaded_ = true;
			if (this.session_.getApplicationName().length() == 0) {
				this
						.log("warn")
						.append(
								"Deploy-path ends with '/', using /?_= for internal paths");
			}
			return true;
		} else {
			return false;
		}
	}

	void changeInternalPath(String aPath) {
		String path = aPath;
		if (!path.equals(this.newInternalPath_)
				&& (path.length() == 0 || path.charAt(0) == '/')) {
			String v = "";
			this.newInternalPath_ = path;
			this.internalPathChanged().trigger(this.newInternalPath_);
		}
	}

	private void handleJavaScriptResponse(WResponseEvent event) {
		this.session_.unlockRecursiveEventLoop();
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

	private static boolean pathMatches(String path, String query) {
		if (query.length() <= path.length()
				&& path.substring(0, 0 + query.length()).equals(query)) {
			return true;
		} else {
			return false;
		}
	}

	SoundManager getSoundManager() {
		if (!(this.soundManager_ != null)) {
			this.soundManager_ = new SoundManager(this);
		}
		return this.soundManager_;
	}

	private SoundManager soundManager_;
	private static char[] gifData = { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01,
			0x00, 0x01, 0x00, 0x80, 0x00, 0x00, 0xdb, 0xdf, 0xef, 0x00, 0x00,
			0x00, 0x21, 0xf9, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00, 0x2c, 0x00,
			0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x02, 0x02, 0x44,
			0x01, 0x00, 0x3b };
}
