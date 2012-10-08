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

class WebRenderer implements SlotLearnerInterface {
	private static Logger logger = LoggerFactory.getLogger(WebRenderer.class);

	public WebRenderer(WebSession session) {
		super();
		this.session_ = session;
		this.visibleOnly_ = true;
		this.rendered_ = false;
		this.twoPhaseThreshold_ = 5000;
		this.pageId_ = 0;
		this.expectedAckId_ = 0;
		this.scriptId_ = 0;
		this.solution_ = "";
		this.cookiesToSet_ = new HashMap<String, WebRenderer.CookieValue>();
		this.currentFormObjects_ = new HashMap<String, WObject>();
		this.currentFormObjectsList_ = "";
		this.collectedJS1_ = new StringWriter();
		this.collectedJS2_ = new StringWriter();
		this.invisibleJS_ = new StringWriter();
		this.statelessJS_ = new StringWriter();
		this.beforeLoadJS_ = new StringWriter();
		this.updateMap_ = new HashSet<WWidget>();
		this.learning_ = false;
	}

	public void setTwoPhaseThreshold(int bytes) {
		this.twoPhaseThreshold_ = bytes;
	}

	public boolean isVisibleOnly() {
		return this.visibleOnly_;
	}

	public void setVisibleOnly(boolean how) {
		this.visibleOnly_ = how;
	}

	public void setRendered(boolean how) {
		this.rendered_ = how;
	}

	public void needUpdate(WWidget w, boolean laterOnly) {
		logger.debug(new StringWriter().append("needUpdate: ")
				.append(w.getId()).append(" (").append("(fixme)").append(")")
				.toString());
		this.updateMap_.add(w);
		if (!laterOnly) {
			this.moreUpdates_ = true;
		}
	}

	public void doneUpdate(WWidget w) {
		logger.debug(new StringWriter().append("doneUpdate: ")
				.append(w.getId()).append(" (").append("(fixme)").append(")")
				.toString());
		this.updateMap_.remove(w);
	}

	public void updateFormObjects(WWebWidget source, boolean checkDescendants) {
		this.formObjectsChanged_ = true;
	}

	public void updateFormObjectsList(WApplication app) {
		if (this.formObjectsChanged_) {
			this.currentFormObjects_.clear();
			app.domRoot_.getFormObjects(this.currentFormObjects_);
			if (app.domRoot2_ != null) {
				app.domRoot2_.getFormObjects(this.currentFormObjects_);
			}
		}
	}

	public Map<String, WObject> getFormObjects() {
		return this.currentFormObjects_;
	}

	public void saveChanges() throws IOException {
		this.collectJS(this.collectedJS1_);
	}

	public void discardChanges() throws IOException {
		this.collectJS((Writer) null);
	}

	public void letReloadJS(WebResponse response, boolean newSession,
			boolean embedded) throws IOException {
		if (!embedded) {
			this.setCaching(response, false);
			this.setHeaders(response, "text/javascript; charset=UTF-8");
		}
		response
				.out()
				.append(
						"if (window.Wt) window.Wt._p_.quit(); window.location.reload(true);");
	}

	public final void letReloadJS(WebResponse response, boolean newSession)
			throws IOException {
		letReloadJS(response, newSession, false);
	}

	public void letReloadHTML(WebResponse response, boolean newSession)
			throws IOException {
		this.setCaching(response, false);
		this.setHeaders(response, "text/html; charset=UTF-8");
		response.out().append("<html><script type=\"text/javascript\">");
		this.letReloadJS(response, newSession, true);
		response.out().append("</script><body></body></html>");
	}

	public boolean isDirty() {
		return !this.updateMap_.isEmpty() || this.formObjectsChanged_
				|| this.session_.getApp().afterLoadJavaScript_.length() != 0
				|| this.session_.getApp().serverPushChanged_
				|| this.session_.getApp().styleSheetsAdded_ != 0
				|| this.collectedJS1_.getBuffer().length() > 0
				|| this.collectedJS2_.getBuffer().length() > 0
				|| this.invisibleJS_.getBuffer().length() > 0;
	}

	public int getScriptId() {
		return this.scriptId_;
	}

	public int getPageId() {
		return this.pageId_;
	}

	public void serveResponse(WebResponse response) throws IOException {
		this.session_.setTriggerUpdate(false);
		switch (response.getResponseType()) {
		case Update:
			this.serveJavaScriptUpdate(response);
			break;
		case Page:
			if (this.session_.getApp() != null) {
				this.serveMainpage(response);
			} else {
				this.serveBootstrap(response);
			}
			break;
		case Script:
			this.serveMainscript(response);
			break;
		}
	}

	public void serveError(int status, WebResponse response, String message)
			throws IOException {
		boolean js = response.getResponseType() != WebRequest.ResponseType.Page;
		WApplication app = this.session_.getApp();
		if (!js || !(app != null)) {
			response.setStatus(status);
			response.setContentType("text/html");
			response.out().append("<title>Error occurred.</title>").append(
					"<h2>Error occurred.</h2>").append(
					WWebWidget.escapeText(new WString(message), true)
							.toString()).append('\n');
		} else {
			response
					.out()
					.append(app.getJavaScriptClass())
					.append(
							"._p_.quit();document.title = 'Error occurred.';document.body.innerHtml='<h2>Error occurred.</h2>' +");
			DomElement.jsStringLiteral(response.out(), message, '\'');
			response.out().append(";");
		}
	}

	public void serveLinkedCss(WebResponse response) throws IOException {
		WApplication app = this.session_.getApp();
		response.setContentType("text/css");
		if (app.getCssTheme().length() != 0) {
			response.out().append("@import url(\"").append(
					WApplication.getResourcesUrl()).append("themes/").append(
					app.getCssTheme()).append("/wt.css\");\n");
			if (app.getEnvironment().agentIsIE()) {
				response.out().append("@import url(\"").append(
						WApplication.getResourcesUrl()).append("themes/")
						.append(app.getCssTheme()).append("/wt_ie.css\");\n");
			}
			if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
				response.out().append("@import url(\"").append(
						WApplication.getResourcesUrl()).append("themes/")
						.append(app.getCssTheme()).append("/wt_ie6.css\");\n");
			}
		}
		for (int i = 0; i < app.styleSheets_.size(); ++i) {
			String url = app.styleSheets_.get(i).uri;
			response.out().append("@import url(\"").append(url).append("\")");
			if (app.styleSheets_.get(i).media.length() != 0
					&& !app.styleSheets_.get(i).media.equals("all")) {
				response.out().append(' ')
						.append(app.styleSheets_.get(i).media);
			}
			response.out().append(";\n");
		}
		app.styleSheetsAdded_ = 0;
	}

	public void setCookie(final String name, final String value, WDate expires,
			final String domain, final String path, boolean secure) {
		this.cookiesToSet_.put(name, new WebRenderer.CookieValue(value, path,
				domain, expires, secure));
	}

	public boolean isPreLearning() {
		return this.learning_;
	}

	public void learningIncomplete() {
		this.learningIncomplete_ = true;
	}

	public boolean ackUpdate(int updateId) {
		if (updateId == this.expectedAckId_) {
			this.setJSSynced(false);
			++this.expectedAckId_;
			return true;
		} else {
			if (updateId < this.expectedAckId_
					&& this.expectedAckId_ - updateId < 5
					|| this.expectedAckId_ - 5 < updateId) {
				return true;
			} else {
				return false;
			}
		}
	}

	public void streamRedirectJS(Writer out, String redirect)
			throws IOException {
		if (this.session_.getApp() != null
				&& this.session_.getApp().internalPathIsChanged_) {
			out
					.append("if (window.")
					.append(this.session_.getApp().getJavaScriptClass())
					.append(") ")
					.append(this.session_.getApp().getJavaScriptClass())
					.append("._p_.setHash(")
					.append(
							WWebWidget
									.jsStringLiteral(this.session_.getApp().newInternalPath_))
					.append(");\n");
		}
		out.append("if (window.location.replace) window.location.replace('")
				.append(redirect).append("');else window.location.href='")
				.append(redirect).append("';\n");
	}

	public boolean checkResponsePuzzle(WebRequest request) {
		if (this.solution_.length() != 0) {
			String ackPuzzleE = request.getParameter("ackPuzzle");
			if (!(ackPuzzleE != null)) {
				logger.warn(new StringWriter().append("secure:").append(
						"Ajax puzzle fail: solution missing").toString());
				return false;
			}
			String ackPuzzle = ackPuzzleE;
			List<String> answer = new ArrayList<String>();
			List<String> solution = new ArrayList<String>();
			solution = new ArrayList<String>(Arrays.asList(this.solution_
					.split(",")));
			answer = new ArrayList<String>(Arrays.asList(ackPuzzle.split(",")));
			int j = 0;
			boolean fail = false;
			for (int i = 0; i < solution.size(); ++i) {
				for (; j < answer.size(); ++j) {
					if (solution.get(i).equals(answer.get(j))) {
						break;
					}
				}
				if (j == answer.size()) {
					fail = true;
					break;
				}
			}
			if (j < answer.size() - 1) {
				fail = true;
			}
			if (fail) {
				logger.warn(new StringWriter().append("secure:").append(
						"Ajax puzzle fail: '").append(ackPuzzle).append(
						"' vs '").append(this.solution_).append('\'')
						.toString());
				this.solution_ = "";
				return false;
			} else {
				this.solution_ = "";
				return true;
			}
		} else {
			return true;
		}
	}

	static class CookieValue {
		private static Logger logger = LoggerFactory
				.getLogger(CookieValue.class);

		public String value;
		public String path;
		public String domain;
		public WDate expires;
		public boolean secure;

		public CookieValue() {
			this.value = "";
			this.path = "";
			this.domain = "";
			this.expires = null;
			this.secure = false;
		}

		public CookieValue(String v, String p, String d, WDate e, boolean s) {
			this.value = v;
			this.path = p;
			this.domain = d;
			this.expires = e;
			this.secure = s;
		}
	}

	private WebSession session_;
	private boolean visibleOnly_;
	private boolean rendered_;
	private int twoPhaseThreshold_;
	private int pageId_;
	private int expectedAckId_;
	private int scriptId_;
	private String solution_;
	private Map<String, WebRenderer.CookieValue> cookiesToSet_;
	private Map<String, WObject> currentFormObjects_;
	private String currentFormObjectsList_;
	private boolean formObjectsChanged_;

	private void setHeaders(WebResponse response, final String mimeType) {
		for (Iterator<Map.Entry<String, WebRenderer.CookieValue>> i_it = this.cookiesToSet_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<String, WebRenderer.CookieValue> i = i_it.next();
			WebRenderer.CookieValue cookie = i.getValue();
			StringBuilder header = new StringBuilder();
			String value = cookie.value;
			if (value.length() == 0) {
				value = "deleted";
			}
			header.append(Utils.urlEncode(i.getKey())).append('=').append(
					Utils.urlEncode(value)).append("; Version=1;");
			if (!(cookie.expires == null)) {
			}
			if (cookie.domain.length() != 0) {
				header.append(" Domain=").append(cookie.domain).append(';');
			}
			if (cookie.path.length() == 0) {
				header.append(" Path=").append(
						this.session_.getEnv().getDeploymentPath()).append(';');
			} else {
				header.append(" Path=").append(cookie.path).append(';');
			}
			if (!response.isWebSocketMessage()) {
				header.append(" httponly;");
			}
			if (cookie.secure) {
				header.append(" secure;");
			}
			response.addHeader("Set-Cookie", header.toString());
		}
		this.cookiesToSet_.clear();
		response.setContentType(mimeType);
	}

	private void setCaching(WebResponse response, boolean allowCache) {
		if (allowCache) {
			response.addHeader("Cache-Control", "max-age=2592000,private");
		} else {
			response.addHeader("Cache-Control",
					"no-cache, no-store, must-revalidate");
			response.addHeader("Pragma", "no-cache");
			response.addHeader("Expires", "0");
		}
	}

	private void serveJavaScriptUpdate(WebResponse response) throws IOException {
		this.setCaching(response, false);
		this.setHeaders(response, "text/javascript; charset=UTF-8");
		if (this.session_.sessionIdChanged_) {
			this.collectedJS1_.append(
					this.session_.getApp().getJavaScriptClass()).append(
					"._p_.setSessionUrl(").append(
					WWebWidget.jsStringLiteral(this.getSessionUrl())).append(
					");");
			this.session_.sessionIdChanged_ = false;
		}
		if (!this.rendered_) {
			this.serveMainAjax(response);
		} else {
			this.collectJavaScript();
			logger.debug(new StringWriter().append("js: ").append(
					this.collectedJS1_.toString()).append(
					this.collectedJS2_.toString()).toString());
			this.addResponseAckPuzzle(response.out());
			this.renderSetServerPush(response.out());
			response.out().append(this.collectedJS1_.toString()).append(
					this.collectedJS2_.toString());
			if (response.isWebSocketRequest() || response.isWebSocketMessage()) {
				this.setJSSynced(false);
			}
		}
	}

	private void serveMainscript(WebResponse response) throws IOException {
		Configuration conf = this.session_.getController().getConfiguration();
		boolean widgetset = this.session_.getType() == EntryPointType.WidgetSet;
		boolean serveSkeletons = !conf.splitScript()
				|| response.getParameter("skeleton") != null;
		boolean serveRest = !conf.splitScript() || !serveSkeletons;
		this.session_.sessionIdChanged_ = false;
		this.setCaching(response, conf.splitScript() && serveSkeletons);
		this.setHeaders(response, "text/javascript; charset=UTF-8");
		if (!widgetset) {
			String redirect = this.session_.getRedirect();
			if (redirect.length() != 0) {
				this.streamRedirectJS(response.out(), redirect);
				return;
			}
		} else {
			this.expectedAckId_ = this.scriptId_ = MathUtils.randomInt();
		}
		WApplication app = this.session_.getApp();
		final boolean xhtml = this.session_.getEnv().getContentType() == WEnvironment.ContentType.XHTML1;
		final boolean innerHtml = !xhtml
				|| this.session_.getEnv().agentIsGecko();
		if (serveSkeletons) {
			boolean haveJQuery = app.isCustomJQuery();
			if (!haveJQuery) {
				response.out().append("if (typeof window.$ === 'undefined') {");
				response.out().append(WtServlet.JQuery_js);
				response.out().append("}");
			}
			List<String> parts = new ArrayList<String>();
			String Wt_js_combined = "";
			if (parts.size() > 1) {
				for (int i = 0; i < parts.size(); ++i) {
					Wt_js_combined += parts.get(i);
				}
			}
			FileServe script = new FileServe(parts.size() > 1 ? Wt_js_combined
					: WtServlet.Wt_js);
			script
					.setCondition(
							"CATCH_ERROR",
							conf.getErrorReporting() != Configuration.ErrorReporting.NoErrors);
			script
					.setCondition(
							"SHOW_STACK",
							conf.getErrorReporting() == Configuration.ErrorReporting.ErrorMessageWithStack);
			script.setCondition("UGLY_INTERNAL_PATHS", this.session_
					.isUseUglyInternalPaths());
			script.setCondition("DYNAMIC_JS", false);
			script.setVar("WT_CLASS", "Wt3_2_3");
			script.setVar("APP_CLASS", app.getJavaScriptClass());
			script.setCondition("STRICTLY_SERIALIZED_EVENTS", conf
					.serializedEvents());
			script.setCondition("WEB_SOCKETS", conf.webSockets());
			script.setVar("INNER_HTML", innerHtml);
			script.setVar("ACK_UPDATE_ID", this.expectedAckId_);
			script.setVar("SESSION_URL", WWebWidget.jsStringLiteral(this
					.getSessionUrl()));
			String deployPath = this.session_.getEnv().publicDeploymentPath_;
			if (deployPath.length() == 0) {
				deployPath = this.session_.getDeploymentPath();
			}
			script
					.setVar("DEPLOY_PATH", WWebWidget
							.jsStringLiteral(deployPath));
			int keepAlive;
			if (conf.getSessionTimeout() == -1) {
				keepAlive = 1000000;
			} else {
				keepAlive = conf.getSessionTimeout() / 2;
			}
			script.setVar("KEEP_ALIVE", String.valueOf(keepAlive));
			script.setVar("INDICATOR_TIMEOUT", conf.getIndicatorTimeout());
			script.setVar("SERVER_PUSH_TIMEOUT",
					conf.getServerPushTimeout() * 1000);
			script.setVar("CLOSE_CONNECTION", false);
			String params = "";
			if (this.session_.getType() == EntryPointType.WidgetSet) {
				Map<String, String[]> m = this.session_.getEnv()
						.getParameterMap();
				for (Iterator<Map.Entry<String, String[]>> i_it = m.entrySet()
						.iterator(); i_it.hasNext();) {
					Map.Entry<String, String[]> i = i_it.next();
					if (params.length() != 0) {
						params += '&';
					}
					params += Utils.urlEncode(i.getKey()) + '='
							+ Utils.urlEncode(i.getValue()[0]);
				}
			}
			script.setVar("PARAMS", params);
			script.stream(response.out());
		}
		if (!serveRest) {
			return;
		}
		response.out().append(app.getJavaScriptClass()).append("._p_.setPage(")
				.append(String.valueOf(this.pageId_)).append(");");
		this.formObjectsChanged_ = true;
		app.autoJavaScriptChanged_ = true;
		if (this.session_.getType() == EntryPointType.WidgetSet) {
			response.out().append("$(document).ready(function() { ").append(
					app.getJavaScriptClass()).append(
					"._p_.update(null, 'load', null, false);});\n");
		} else {
			if (!this.rendered_) {
				this.serveMainAjax(response);
			} else {
				boolean enabledAjax = app.enableAjax_;
				if (app.enableAjax_) {
					this.collectedJS1_
							.append(
									"var form = Wt3_2_3.getElement('Wt-form'); if (form) {")
							.append(this.beforeLoadJS_.toString());
					this.beforeLoadJS_ = new StringWriter();
					this.collectedJS1_
							.append("var domRoot = ")
							.append(app.domRoot_.getJsRef())
							.append(
									";domRoot.style.display = form.style.display;document.body.replaceChild(domRoot, form);");
					int librariesLoaded = this.loadScriptLibraries(
							this.collectedJS1_, app);
					app.streamBeforeLoadJavaScript(this.collectedJS1_, false);
					this.collectedJS2_.append(
							"Wt3_2_3.resolveRelativeAnchors();").append(
							"domRoot.style.visibility = 'visible';").append(
							app.getJavaScriptClass()).append(
							"._p_.doAutoJavaScript();");
					this.loadScriptLibraries(this.collectedJS2_, app,
							librariesLoaded);
					this.collectedJS2_.append("}");
					app.enableAjax_ = false;
				} else {
					app.streamBeforeLoadJavaScript(response.out(), true);
				}
				response.out().append("window.").append(
						app.getJavaScriptClass()).append(
						"LoadWidgetTree = function(){\n");
				this.visibleOnly_ = false;
				this.formObjectsChanged_ = true;
				this.currentFormObjectsList_ = "";
				this.collectJavaScript();
				this.updateLoadIndicator(this.collectedJS1_, app, true);
				logger.debug(new StringWriter().append("js: ").append(
						this.collectedJS1_.toString()).append(
						this.collectedJS2_.toString()).toString());
				response.out().append(this.collectedJS1_.toString());
				this.addResponseAckPuzzle(response.out());
				response.out().append(app.getJavaScriptClass()).append(
						"._p_.setHash(").append(
						WWebWidget.jsStringLiteral(app.newInternalPath_))
						.append(");\n");
				if (!app.getEnvironment().hashInternalPaths()) {
					this.session_.setPagePathInfo(app.newInternalPath_);
				}
				response.out().append(app.getJavaScriptClass()).append(
						"._p_.update(null, 'load', null, false);").append(
						this.collectedJS2_.toString()).append("};");
				this.session_.getApp().serverPushChanged_ = true;
				this.renderSetServerPush(response.out());
				if (enabledAjax) {
					response
							.out()
							.append(
									"\nif (typeof document.readyState === 'undefined')")
							.append(" setTimeout(function() { ").append(
									app.getJavaScriptClass()).append(
									"._p_.load(true);").append("}, 400);")
							.append("else ");
				}
				response.out().append("$(document).ready(function() { ")
						.append(app.getJavaScriptClass()).append(
								"._p_.load(true);});\n");
			}
		}
	}

	private void serveBootstrap(WebResponse response) throws IOException {
		boolean xhtml = this.session_.getEnv().getContentType() == WEnvironment.ContentType.XHTML1;
		Configuration conf = this.session_.getController().getConfiguration();
		FileServe boot = new FileServe(WtServlet.Boot_html);
		this.setPageVars(boot);
		StringWriter noJsRedirectUrl = new StringWriter();
		DomElement.htmlAttributeValue(noJsRedirectUrl, this.session_
				.getBootstrapUrl(response,
						WebSession.BootstrapOption.KeepInternalPath)
				+ "&js=no");
		boot.setVar("REDIRECT_URL", noJsRedirectUrl.toString());
		if (xhtml) {
			boot.setVar("AUTO_REDIRECT", "");
			boot.setVar("NOSCRIPT_TEXT", conf.getRedirectMessage());
		} else {
			boot.setVar("AUTO_REDIRECT",
					"<noscript><meta http-equiv=\"refresh\" content=\"0; url="
							+ noJsRedirectUrl.toString() + "\"></noscript>");
			boot.setVar("NOSCRIPT_TEXT", conf.getRedirectMessage());
		}
		StringWriter bootStyleUrl = new StringWriter();
		DomElement.htmlAttributeValue(bootStyleUrl, this.session_
				.getBootstrapUrl(response,
						WebSession.BootstrapOption.ClearInternalPath)
				+ "&request=style");
		boot.setVar("BOOT_STYLE_URL", bootStyleUrl.toString());
		this.setCaching(response, false);
		String contentType = xhtml ? "application/xhtml+xml" : "text/html";
		contentType += "; charset=UTF-8";
		this.setHeaders(response, contentType);
		this.streamBootContent(response, boot, false);
		boot.stream(response.out());
		this.rendered_ = false;
	}

	private void serveMainpage(WebResponse response) throws IOException {
		++this.expectedAckId_;
		++this.pageId_;
		this.session_.sessionIdChanged_ = false;
		Configuration conf = this.session_.getController().getConfiguration();
		WApplication app = this.session_.getApp();
		if (!app.getEnvironment().hasAjax()
				&& (app.internalPathIsChanged_ && !app.oldInternalPath_
						.equals(app.newInternalPath_))) {
			app.oldInternalPath_ = app.newInternalPath_;
			this.session_.redirect(this.session_.fixRelativeUrl(this.session_
					.getMostRelativeUrl(app.newInternalPath_)));
		}
		String redirect = this.session_.getRedirect();
		if (redirect.length() != 0) {
			response.setStatus(302);
			response.sendRedirect(redirect);
			return;
		}
		WWebWidget mainWebWidget = app.domRoot_;
		this.visibleOnly_ = true;
		DomElement mainElement = mainWebWidget.createSDomElement(app);
		this.rendered_ = true;
		this.setJSSynced(true);
		final boolean xhtml = app.getEnvironment().getContentType() == WEnvironment.ContentType.XHTML1;
		String styleSheets = "";
		if (app.getCssTheme().length() != 0) {
			styleSheets += "<link href=\"" + WApplication.getResourcesUrl()
					+ "themes/" + app.getCssTheme()
					+ "/wt.css\" rel=\"stylesheet\" type=\"text/css\""
					+ (xhtml ? "/>" : ">") + "\n";
			if (app.getEnvironment().agentIsIE()) {
				styleSheets += "<link href=\"" + WApplication.getResourcesUrl()
						+ "themes/" + app.getCssTheme()
						+ "/wt_ie.css\" rel=\"stylesheet\" type=\"text/css\""
						+ (xhtml ? "/>" : ">") + "\n";
			}
			if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
				styleSheets += "<link href=\"" + WApplication.getResourcesUrl()
						+ "themes/" + app.getCssTheme()
						+ "/wt_ie6.css\" rel=\"stylesheet\" type=\"text/css\""
						+ (xhtml ? "/>" : ">") + "\n";
			}
		}
		for (int i = 0; i < app.styleSheets_.size(); ++i) {
			String url = app.styleSheets_.get(i).uri;
			url = StringUtils.replace(url, '&', "&amp;");
			styleSheets += "<link href=\"" + this.session_.fixRelativeUrl(url)
					+ "\" rel=\"stylesheet\" type=\"text/css\"";
			if (app.styleSheets_.get(i).media.length() != 0
					&& !app.styleSheets_.get(i).media.equals("all")) {
				styleSheets += " media=\"" + app.styleSheets_.get(i).media
						+ '"';
			}
			styleSheets += xhtml ? "/>" : ">";
			styleSheets += "\n";
		}
		app.styleSheetsAdded_ = 0;
		this.beforeLoadJS_ = new StringWriter();
		for (int i = 0; i < app.scriptLibraries_.size(); ++i) {
			String url = app.scriptLibraries_.get(i).uri;
			url = StringUtils.replace(url, '&', "&amp;");
			styleSheets += "<script src='" + this.session_.fixRelativeUrl(url)
					+ "'></script>\n";
			this.beforeLoadJS_.append(app.scriptLibraries_.get(i).beforeLoadJS);
		}
		app.scriptLibrariesAdded_ = 0;
		app.newBeforeLoadJavaScript_ = app.beforeLoadJavaScript_.length();
		boolean hybridPage = this.session_.isProgressiveBoot()
				|| this.session_.getEnv().hasAjax();
		FileServe page = new FileServe(hybridPage ? WtServlet.Hybrid_html
				: WtServlet.Plain_html);
		this.setPageVars(page);
		page.setVar("SESSION_ID", this.session_.getSessionId());
		String url = app.getEnvironment().agentIsSpiderBot()
				|| !this.session_.isUseUrlRewriting() ? this.session_
				.getBookmarkUrl(app.newInternalPath_) : this.session_
				.getMostRelativeUrl(app.newInternalPath_);
		url = this.session_.fixRelativeUrl(url);
		url = StringUtils.replace(url, '&', "&amp;");
		page.setVar("RELATIVE_URL", url);
		if (conf.isInlineCss()) {
			page.setVar("STYLESHEET", app.getStyleSheet().getCssText(true));
		} else {
			page.setVar("STYLESHEET", "");
		}
		page.setVar("STYLESHEETS", styleSheets);
		page.setVar("TITLE", WWebWidget.escapeText(app.getTitle()).toString());
		app.titleChanged_ = false;
		String contentType = xhtml ? "application/xhtml+xml" : "text/html";
		contentType += "; charset=UTF-8";
		this.setCaching(response, false);
		this.setHeaders(response, contentType);
		this.currentFormObjectsList_ = this.createFormObjectsList(app);
		if (hybridPage) {
			this.streamBootContent(response, page, true);
		}
		page.streamUntil(response.out(), "HTML");
		List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
		{
			EscapeOStream js = new EscapeOStream();
			EscapeOStream out = new EscapeOStream(response.out());
			mainElement.asHTML(out, js, timeouts);
			app.afterLoadJavaScript_ = js.toString() + app.afterLoadJavaScript_;
			;
			app.domRoot_.doneRerender();
		}
		int refresh;
		if (app.getEnvironment().hasAjax()) {
			StringWriter str = new StringWriter();
			DomElement.createTimeoutJs(str, timeouts, app);
			app.doJavaScript(str.toString());
			refresh = 1000000;
		} else {
			if (app.isQuited() || conf.getSessionTimeout() == -1) {
				refresh = 1000000;
			} else {
				refresh = conf.getSessionTimeout() / 3;
				for (int i = 0; i < timeouts.size(); ++i) {
					refresh = Math
							.min(refresh, 1 + timeouts.get(i).msec / 1000);
				}
			}
		}
		page.setVar("REFRESH", String.valueOf(refresh));
		page.stream(response.out());
		app.internalPathIsChanged_ = false;
	}

	private void serveMainAjax(WebResponse response) throws IOException {
		Configuration conf = this.session_.getController().getConfiguration();
		boolean widgetset = this.session_.getType() == EntryPointType.WidgetSet;
		WApplication app = this.session_.getApp();
		WWebWidget mainWebWidget = app.domRoot_;
		this.visibleOnly_ = true;
		app.loadingIndicatorWidget_.show();
		DomElement mainElement = mainWebWidget.createSDomElement(app);
		app.loadingIndicatorWidget_.hide();
		app.scriptLibrariesAdded_ = app.scriptLibraries_.size();
		int librariesLoaded = this.loadScriptLibraries(response.out(), app);
		response.out().append(app.getJavaScriptClass()).append(
				"._p_.autoJavaScript=function(){").append(app.autoJavaScript_)
				.append("};\n");
		app.autoJavaScriptChanged_ = false;
		this.currentFormObjectsList_ = this.createFormObjectsList(app);
		response.out().append(app.getJavaScriptClass()).append(
				"._p_.setFormObjects([").append(this.currentFormObjectsList_)
				.append("]);");
		this.formObjectsChanged_ = false;
		response.out().append("\n");
		app.streamBeforeLoadJavaScript(response.out(), true);
		if (!widgetset) {
			response.out().append("window.").append(app.getJavaScriptClass())
					.append("LoadWidgetTree = function(){\n");
		}
		if (widgetset || !this.session_.isBootStyleResponse()) {
			if (app.getCssTheme().length() != 0) {
				response.out().append("Wt3_2_3").append(".addStyleSheet('")
						.append(WApplication.getResourcesUrl()).append(
								"themes/").append(app.getCssTheme()).append(
								"/wt.css', 'all');");
				if (app.getEnvironment().agentIsIE()) {
					response.out().append("Wt3_2_3").append(".addStyleSheet('")
							.append(WApplication.getResourcesUrl()).append(
									"themes/").append(app.getCssTheme())
							.append("/wt_ie.css', 'all');");
				}
				if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
					response.out().append("Wt3_2_3").append(".addStyleSheet('")
							.append(WApplication.getResourcesUrl()).append(
									"themes/").append(app.getCssTheme())
							.append("/wt_ie6.css', 'all');");
				}
			}
			app.styleSheetsAdded_ = app.styleSheets_.size();
			this.loadStyleSheets(response.out(), app);
		}
		if (conf.isInlineCss()) {
			app.getStyleSheet().javaScriptUpdate(app, response.out(), true);
		}
		if (app.bodyHtmlClassChanged_) {
			response
					.out()
					.append("document.body.parentNode.className='")
					.append(app.htmlClass_)
					.append("';")
					.append("document.body.className='")
					.append(this.getBodyClassRtl())
					.append("';")
					.append("document.body.setAttribute('dir', '")
					.append(
							app.getLayoutDirection() == LayoutDirection.LeftToRight ? "LTR"
									: "RTL").append("');");
		}
		StringWriter s = new StringWriter();
		mainElement.addToParent(s, "document.body", widgetset ? 0 : -1, app);
		;
		this.addResponseAckPuzzle(s);
		if (app.isQuited()) {
			s.append(app.getJavaScriptClass()).append("._p_.quit();");
		}
		if (widgetset) {
			app.domRoot2_.rootAsJavaScript(app, s, true);
		}
		logger.debug(new StringWriter().append("js: ").append(s.toString())
				.toString());
		response.out().append(s.toString());
		this.setJSSynced(true);
		this.preLearnStateless(app, this.collectedJS1_);
		logger.debug(new StringWriter().append("js: ").append(
				this.collectedJS1_.toString()).toString());
		response.out().append(this.collectedJS1_.toString());
		this.collectedJS1_ = new StringWriter();
		this.updateLoadIndicator(response.out(), app, true);
		if (widgetset) {
			String historyE = app.getEnvironment().getParameter("Wt-history");
			if (historyE != null) {
				response.out().append("Wt3_2_3")
						.append(".history.initialize('").append(
								historyE.charAt(0)).append("-field', '")
						.append(historyE.charAt(0)).append("-iframe', '');\n");
			}
		}
		app.streamAfterLoadJavaScript(response.out());
		response.out().append("{var o=null,e=null;").append(
				app.hideLoadingIndicator_.getJavaScript()).append("}");
		if (!widgetset) {
			if (!app.isQuited()) {
				response.out().append(
						this.session_.getApp().getJavaScriptClass()).append(
						"._p_.update(null, 'load', null, false);\n");
			}
			response.out().append("};\n");
		}
		this.renderSetServerPush(response.out());
		response.out().append("$(document).ready(function() { ").append(
				app.getJavaScriptClass()).append("._p_.load(").append(
				widgetset ? "false" : "true").append(");});\n");
		this.loadScriptLibraries(response.out(), app, librariesLoaded);
	}

	// private void serveWidgetSet(WebResponse request) ;
	private void collectJavaScript() throws IOException {
		WApplication app = this.session_.getApp();
		Configuration conf = this.session_.getController().getConfiguration();
		this.collectedJS1_.append(this.invisibleJS_.toString());
		this.invisibleJS_ = new StringWriter();
		if (conf.isInlineCss()) {
			app.getStyleSheet()
					.javaScriptUpdate(app, this.collectedJS1_, false);
		}
		this.loadStyleSheets(this.collectedJS1_, app);
		if (app.bodyHtmlClassChanged_) {
			this.collectedJS1_
					.append("document.body.parentNode.className='")
					.append(app.htmlClass_)
					.append("';")
					.append("document.body.className='")
					.append(this.getBodyClassRtl())
					.append("';")
					.append("document.body.setAttribute('dir', '")
					.append(
							app.getLayoutDirection() == LayoutDirection.LeftToRight ? "LTR"
									: "RTL").append("');");
		}
		int librariesLoaded = this.loadScriptLibraries(this.collectedJS1_, app);
		this.loadScriptLibraries(this.collectedJS2_, app, librariesLoaded);
		app.streamBeforeLoadJavaScript(this.collectedJS1_, false);
		if (app.domRoot2_ != null) {
			app.domRoot2_.rootAsJavaScript(app, this.collectedJS1_, false);
		}
		this.collectJavaScriptUpdate(this.collectedJS1_);
		if (this.visibleOnly_) {
			boolean needFetchInvisible = false;
			if (!this.updateMap_.isEmpty()) {
				needFetchInvisible = true;
				if (this.twoPhaseThreshold_ > 0) {
					this.visibleOnly_ = false;
					this.collectJavaScriptUpdate(this.invisibleJS_);
					if (this.invisibleJS_.getBuffer().length() < (int) this.twoPhaseThreshold_) {
						this.collectedJS1_.append(this.invisibleJS_.toString());
						this.invisibleJS_ = new StringWriter();
						needFetchInvisible = false;
					}
					this.visibleOnly_ = true;
				}
			}
			if (needFetchInvisible) {
				this.collectedJS1_.append(app.getJavaScriptClass()).append(
						"._p_.update(null, 'none', null, false);");
			}
		}
		if (app.autoJavaScriptChanged_) {
			this.collectedJS1_.append(app.getJavaScriptClass()).append(
					"._p_.autoJavaScript=function(){").append(
					app.autoJavaScript_).append("};");
			app.autoJavaScriptChanged_ = false;
		}
		this.visibleOnly_ = true;
		app.domRoot_.doneRerender();
		if (app.domRoot2_ != null) {
			app.domRoot2_.doneRerender();
		}
		String redirect = this.session_.getRedirect();
		if (redirect.length() != 0) {
			this.streamRedirectJS(this.collectedJS1_, redirect);
		}
	}

	private void collectChanges(List<DomElement> changes) {
		WApplication app = this.session_.getApp();
		do {
			this.moreUpdates_ = false;
			OrderedMultiMap<Integer, WWidget> depthOrder = new OrderedMultiMap<Integer, WWidget>();
			for (Iterator<WWidget> i_it = this.updateMap_.iterator(); i_it
					.hasNext();) {
				WWidget i = i_it.next();
				int depth = 1;
				WWidget ww = i;
				WWidget w = ww;
				for (; w.getParent() != null; ++depth) {
					w = w.getParent();
				}
				if (w != app.domRoot_ && w != app.domRoot2_) {
					logger.debug(new StringWriter().append("ignoring: ")
							.append(ww.getId()).append(" (").append("(fixme)")
							.append(") ").append(w.getId()).append(" (")
							.append("(fixme)").append(")").toString());
					depth = 0;
				}
				depthOrder.put(depth, ww);
			}
			for (Iterator<Map.Entry<Integer, WWidget>> i_it = depthOrder
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<Integer, WWidget> i = i_it.next();
				boolean j = this.updateMap_.contains(i.getValue());
				if (j != false) {
					WWidget w = i.getValue();
					if (i.getKey() == 0) {
						w.getWebWidget().propagateRenderOk();
						continue;
					}
					logger.debug(new StringWriter().append("updating: ")
							.append(w.getId()).append(" (").append("(fixme)")
							.append(")").toString());
					if (!this.learning_ && this.visibleOnly_) {
						if (w.isRendered()) {
							w.getSDomChanges(changes, app);
						} else {
							logger.debug(new StringWriter()
									.append("Ignoring: ").append(w.getId())
									.toString());
						}
					} else {
						w.getSDomChanges(changes, app);
					}
				}
			}
		} while (!this.learning_ && this.moreUpdates_);
	}

	private void collectJavaScriptUpdate(Writer out) throws IOException {
		WApplication app = this.session_.getApp();
		out.append('{');
		this.collectJS(out);
		this.preLearnStateless(app, out);
		if (this.formObjectsChanged_) {
			String formObjectsList = this.createFormObjectsList(app);
			if (!formObjectsList.equals(this.currentFormObjectsList_)) {
				this.currentFormObjectsList_ = formObjectsList;
				out.append(app.getJavaScriptClass()).append(
						"._p_.setFormObjects([").append(
						this.currentFormObjectsList_).append("]);");
			}
		}
		app.streamAfterLoadJavaScript(out);
		if (app.isQuited()) {
			out.append(app.getJavaScriptClass()).append("._p_.quit();");
		}
		this.updateLoadIndicator(out, app, false);
		out.append('}');
	}

	private void loadStyleSheets(Writer out, WApplication app)
			throws IOException {
		int first = app.styleSheets_.size() - app.styleSheetsAdded_;
		for (int i = first; i < app.styleSheets_.size(); ++i) {
			out.append("Wt3_2_3").append(".addStyleSheet('").append(
					this.session_.fixRelativeUrl(app.styleSheets_.get(i).uri))
					.append("', '").append(app.styleSheets_.get(i).media)
					.append("');\n");
		}
		app.styleSheetsAdded_ = 0;
	}

	private int loadScriptLibraries(Writer out, WApplication app, int count)
			throws IOException {
		if (count == -1) {
			int first = app.scriptLibraries_.size() - app.scriptLibrariesAdded_;
			for (int i = first; i < app.scriptLibraries_.size(); ++i) {
				String uri = this.session_.fixRelativeUrl(app.scriptLibraries_
						.get(i).uri);
				out.append(app.scriptLibraries_.get(i).beforeLoadJS).append(
						app.getJavaScriptClass()).append("._p_.loadScript('")
						.append(uri).append("',");
				DomElement.jsStringLiteral(out,
						app.scriptLibraries_.get(i).symbol, '\'');
				out.append(");\n");
				out.append(app.getJavaScriptClass()).append("._p_.onJsLoad(\"")
						.append(uri).append("\",function() {\n");
			}
			count = app.scriptLibrariesAdded_;
			app.scriptLibrariesAdded_ = 0;
			return count;
		} else {
			if (count != 0) {
				out.append(app.getJavaScriptClass()).append(
						"._p_.doAutoJavaScript();");
				for (int i = 0; i < count; ++i) {
					out.append("});");
				}
			}
			return 0;
		}
	}

	private final int loadScriptLibraries(Writer out, WApplication app)
			throws IOException {
		return loadScriptLibraries(out, app, -1);
	}

	private void updateLoadIndicator(Writer out, WApplication app, boolean all)
			throws IOException {
		if (app.showLoadingIndicator_.needsUpdate(all)) {
			out.append(
					"showLoadingIndicator = function() {var o=null,e=null;\n")
					.append(app.showLoadingIndicator_.getJavaScript()).append(
							"};\n");
			app.showLoadingIndicator_.updateOk();
		}
		if (app.hideLoadingIndicator_.needsUpdate(all)) {
			out.append(
					"hideLoadingIndicator = function() {var o=null,e=null;\n")
					.append(app.hideLoadingIndicator_.getJavaScript()).append(
							"};\n");
			app.hideLoadingIndicator_.updateOk();
		}
	}

	private void renderSetServerPush(Writer out) {
		try {
			if (this.session_.getApp().serverPushChanged_) {
				out.append(this.session_.getApp().getJavaScriptClass()).append(
						"._p_.setServerPush(").append(
						this.session_.getApp().isUpdatesEnabled() ? "true"
								: "false").append(");");
				this.session_.getApp().serverPushChanged_ = false;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void setJSSynced(boolean invisibleToo) {
		this.collectedJS1_ = new StringWriter();
		this.collectedJS2_ = new StringWriter();
		if (!invisibleToo) {
			this.collectedJS1_.append(this.invisibleJS_.toString());
		}
		this.invisibleJS_ = new StringWriter();
	}

	private String createFormObjectsList(WApplication app) {
		this.updateFormObjectsList(app);
		String result = "";
		for (Iterator<Map.Entry<String, WObject>> i_it = this.currentFormObjects_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<String, WObject> i = i_it.next();
			if (result.length() != 0) {
				result += ',';
			}
			result += "'" + i.getKey() + "'";
		}
		this.formObjectsChanged_ = false;
		return result;
	}

	private void preLearnStateless(WApplication app, Writer out)
			throws IOException {
		boolean isIEMobile = app.getEnvironment().agentIsIEMobile();
		if (isIEMobile || !this.session_.getEnv().hasAjax()) {
			return;
		}
		this.collectJS(out);
		Map<String, WeakReference<AbstractEventSignal>> ss = this.session_
				.getApp().exposedSignals();
		for (Iterator<Map.Entry<String, WeakReference<AbstractEventSignal>>> i_it = ss
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<String, WeakReference<AbstractEventSignal>> i = i_it
					.next();
			AbstractEventSignal s = i.getValue().get();
			if (!(s != null)) {
				i_it.remove();
				continue;
			}
			if (s.getSender() == app) {
				s.processPreLearnStateless(this);
			}
			WWidget ww = ((s.getSender()) instanceof WWidget ? (WWidget) (s
					.getSender()) : null);
			if (ww != null && ww.isRendered()) {
				s.processPreLearnStateless(this);
			}
		}
		out.append(this.statelessJS_.toString());
		this.statelessJS_ = new StringWriter();
	}

	private StringWriter collectedJS1_;
	private StringWriter collectedJS2_;
	private StringWriter invisibleJS_;
	private StringWriter statelessJS_;
	StringWriter beforeLoadJS_;

	private void collectJS(Writer js) throws IOException {
		List<DomElement> changes = new ArrayList<DomElement>();
		this.collectChanges(changes);
		WApplication app = this.session_.getApp();
		if (js != null) {
			if (!this.isPreLearning()) {
				app.streamBeforeLoadJavaScript(js, false);
			}
			EscapeOStream sout = new EscapeOStream(js);
			for (int i = 0; i < changes.size(); ++i) {
				changes.get(i).asJavaScript(sout, DomElement.Priority.Delete);
			}
			for (int i = 0; i < changes.size(); ++i) {
				changes.get(i).asJavaScript(sout, DomElement.Priority.Update);
				;
			}
		} else {
			for (int i = 0; i < changes.size(); ++i) {
				;
			}
		}
		if (js != null) {
			if (app.titleChanged_) {
				js.append(app.getJavaScriptClass()).append("._p_.setTitle(")
						.append(
								WString.toWString(app.getTitle())
										.getJsStringLiteral()).append(");\n");
			}
			if (app.closeMessageChanged_) {
				js.append(app.getJavaScriptClass()).append(
						"._p_.setCloseMessage(").append(
						WString.toWString(app.getCloseMessage())
								.getJsStringLiteral()).append(");\n");
			}
		}
		app.titleChanged_ = false;
		app.closeMessageChanged_ = false;
		if (js != null) {
			int librariesLoaded = this.loadScriptLibraries(js, app);
			app.streamAfterLoadJavaScript(js);
			if (app.internalPathIsChanged_) {
				js.append(app.getJavaScriptClass()).append("._p_.setHash(")
						.append(
								WWebWidget
										.jsStringLiteral(app.newInternalPath_))
						.append(");\n");
				if (!this.isPreLearning()
						&& !app.getEnvironment().hashInternalPaths()) {
					this.session_.setPagePathInfo(app.newInternalPath_);
				}
			}
			this.loadScriptLibraries(js, app, librariesLoaded);
		} else {
			app.afterLoadJavaScript_ = "";
		}
		app.internalPathIsChanged_ = false;
	}

	private void setPageVars(FileServe page) {
		boolean xhtml = this.session_.getEnv().getContentType() == WEnvironment.ContentType.XHTML1;
		WApplication app = this.session_.getApp();
		page.setVar("DOCTYPE", this.session_.getDocType());
		String htmlAttr = "";
		if (app != null && app.htmlClass_.length() != 0) {
			htmlAttr = " class=\"" + app.htmlClass_ + "\"";
		}
		if (xhtml) {
			page.setVar("HTMLATTRIBUTES",
					"xmlns=\"http://www.w3.org/1999/xhtml\"" + htmlAttr);
			page.setVar("METACLOSE", "/>");
		} else {
			if (this.session_.getEnv().agentIsIE()) {
				page.setVar("HTMLATTRIBUTES",
						"xmlns:v=\"urn:schemas-microsoft-com:vml\" lang=\"en\" dir=\"ltr\""
								+ htmlAttr);
			} else {
				page.setVar("HTMLATTRIBUTES", "lang=\"en\" dir=\"ltr\""
						+ htmlAttr);
			}
			page.setVar("METACLOSE", ">");
		}
		String attr = this.getBodyClassRtl();
		if (attr.length() != 0) {
			attr = " class=\"" + attr + "\"";
		}
		if (app != null
				&& app.getLayoutDirection() == LayoutDirection.RightToLeft) {
			attr += " dir=\"RTL\"";
		}
		page.setVar("BODYATTRIBUTES", attr);
		page.setVar("HEADDECLARATIONS", this.getHeadDeclarations());
		page.setCondition("FORM", !this.session_.getEnv().agentIsSpiderBot()
				&& !this.session_.getEnv().hasAjax());
	}

	private void streamBootContent(WebResponse response, FileServe boot,
			boolean hybrid) throws IOException {
		Configuration conf = this.session_.getController().getConfiguration();
		FileServe bootJs = new FileServe(WtServlet.Boot_js);
		boot.setVar("BLANK_HTML", this.session_.getBootstrapUrl(response,
				WebSession.BootstrapOption.ClearInternalPath)
				+ "&amp;request=resource&amp;resource=blank");
		boot.setVar("SESSION_ID", this.session_.getSessionId());
		boot.setVar("APP_CLASS", "Wt");
		bootJs.setVar("SELF_URL", this.safeJsStringLiteral(this.session_
				.getBootstrapUrl(response,
						WebSession.BootstrapOption.ClearInternalPath)));
		bootJs.setVar("SESSION_ID", this.session_.getSessionId());
		this.expectedAckId_ = this.scriptId_ = MathUtils.randomInt();
		bootJs.setVar("SCRIPT_ID", this.scriptId_);
		bootJs.setVar("RANDOMSEED", MathUtils.randomInt());
		bootJs.setVar("RELOAD_IS_NEWSESSION", conf.reloadIsNewSession());
		bootJs
				.setVar(
						"USE_COOKIES",
						conf.getSessionTracking() == Configuration.SessionTracking.CookiesURL);
		bootJs.setVar("AJAX_CANONICAL_URL", this
				.safeJsStringLiteral(this.session_.ajaxCanonicalUrl(response)));
		bootJs.setVar("APP_CLASS", "Wt");
		bootJs.setVar("PATH_INFO", WWebWidget.jsStringLiteral(this.session_
				.getEnv().pathInfo_));
		bootJs.setCondition("SPLIT_SCRIPT", conf.splitScript());
		bootJs.setCondition("HYBRID", hybrid);
		boolean xhtml = this.session_.getEnv().getContentType() == WEnvironment.ContentType.XHTML1;
		bootJs.setCondition("DEFER_SCRIPT", !xhtml);
		String internalPath = hybrid ? this.session_.getApp().getInternalPath()
				: this.session_.getEnv().getInternalPath();
		bootJs.setVar("INTERNAL_PATH", this.safeJsStringLiteral(internalPath));
		boot.streamUntil(response.out(), "BOOT_JS");
		bootJs.stream(response.out());
	}

	private void addResponseAckPuzzle(Writer out) throws IOException {
		String puzzle = "";
		Configuration conf = this.session_.getController().getConfiguration();
		if (conf.ajaxPuzzle() && this.expectedAckId_ == this.scriptId_) {
			List<WContainerWidget> widgets = new ArrayList<WContainerWidget>();
			WApplication app = this.session_.getApp();
			this.addContainerWidgets(app.domRoot_, widgets);
			if (app.domRoot2_ != null) {
				this.addContainerWidgets(app.domRoot2_, widgets);
			}
			int r = MathUtils.randomInt() % widgets.size();
			WContainerWidget wc = widgets.get(r);
			puzzle = '"' + wc.getId() + '"';
			String l = "";
			for (WWidget w = wc.getParent(); w != null; w = w.getParent()) {
				if (w.getId().length() == 0) {
					continue;
				}
				if (w.getId().equals(l)) {
					continue;
				}
				l = w.getId();
				if (this.solution_.length() != 0) {
					this.solution_ += ',';
				}
				this.solution_ += l;
			}
		}
		out.append(this.session_.getApp().getJavaScriptClass()).append(
				"._p_.response(").append(String.valueOf(this.expectedAckId_));
		if (puzzle.length() != 0) {
			out.append(",").append(puzzle);
		}
		out.append(");");
	}

	private void addContainerWidgets(WWebWidget w, List<WContainerWidget> result) {
		for (int i = 0; i < w.getChildren().size(); ++i) {
			WWidget c = w.getChildren().get(i);
			if (!c.isRendered()) {
				return;
			}
			if (!c.isHidden()) {
				this.addContainerWidgets(c.getWebWidget(), result);
			}
			WContainerWidget wc = ((c) instanceof WContainerWidget ? (WContainerWidget) (c)
					: null);
			if (wc != null) {
				result.add(wc);
			}
		}
	}

	private String getHeadDeclarations() {
		final boolean xhtml = this.session_.getEnv().getContentType() == WEnvironment.ContentType.XHTML1;
		EscapeOStream result = new EscapeOStream();
		if (this.session_.getApp() != null) {
			for (int i = 0; i < this.session_.getApp().metaHeaders_.size(); ++i) {
				WApplication.MetaHeader m = this.session_.getApp().metaHeaders_
						.get(i);
				result.append("<meta");
				if (m.name.length() != 0) {
					appendAttribute(result,
							m.type == MetaHeaderType.MetaName ? "name"
									: "http-equiv", m.name);
				}
				if (m.lang.length() != 0) {
					appendAttribute(result, "lang", m.lang);
				}
				appendAttribute(result, "content", m.content.toString());
				result.append(xhtml ? "/>" : ">");
			}
			for (int i = 0; i < this.session_.getApp().metaLinks_.size(); ++i) {
				WApplication.MetaLink ml = this.session_.getApp().metaLinks_
						.get(i);
				EscapeOStream link = new EscapeOStream();
				link.append("<link");
				appendAttribute(link, "href", ml.href);
				appendAttribute(link, "rel", ml.rel);
				if (ml.media.length() != 0) {
					appendAttribute(link, "media", ml.media);
				}
				if (ml.hreflang.length() != 0) {
					appendAttribute(link, "hreflang", ml.hreflang);
				}
				if (ml.type.length() != 0) {
					appendAttribute(link, "type", ml.type);
				}
				if (ml.sizes.length() != 0) {
					appendAttribute(link, "sizes", ml.sizes);
				}
				if (ml.disabled) {
					appendAttribute(link, "disabled", "");
				}
				link.append(xhtml ? "/>" : ">");
				result.append(link.toString());
			}
		} else {
			if (this.session_.getEnv().agentIsIElt(9)) {
				result.append(
						"<meta http-equiv=\"X-UA-Compatible\" content=\"IE=7")
						.append(xhtml ? "\"/>" : "\">").append('\n');
			} else {
				if (this.session_.getEnv().getAgent() == WEnvironment.UserAgent.IE9) {
					result
							.append(
									"<meta http-equiv=\"X-UA-Compatible\" content=\"IE=9")
							.append(xhtml ? "\"/>" : "\">").append('\n');
				}
			}
		}
		if (this.session_.getFavicon().length() != 0) {
			result
					.append(
							"<link rel=\"icon\" type=\"image/vnd.microsoft.icon\" href=\"")
					.append(this.session_.getFavicon()).append(
							xhtml ? "\"/>" : "\">");
		}
		String baseUrl = "";
		baseUrl = WApplication.readConfigurationProperty("baseURL", baseUrl);
		if (baseUrl.length() != 0) {
			result.append("<base href=\"").append(baseUrl).append(
					xhtml ? "\"/>" : "\">");
		}
		return result.toString();
	}

	private String getBodyClassRtl() {
		if (this.session_.getApp() != null) {
			String s = this.session_.getApp().bodyClass_;
			if (s.length() != 0) {
				s += ' ';
			}
			s += this.session_.getApp().getLayoutDirection() == LayoutDirection.LeftToRight ? "Wt-ltr"
					: "Wt-rtl";
			this.session_.getApp().bodyHtmlClassChanged_ = false;
			return s;
		} else {
			return "";
		}
	}

	private String getSessionUrl() {
		String result = this.session_.getApplicationUrl();
		if (isAbsoluteUrl(result)) {
			return this.session_.appendSessionQuery(result);
		} else {
			return this.session_.appendSessionQuery(".").substring(1);
		}
	}

	private Set<WWidget> updateMap_;
	private boolean learning_;
	private boolean learningIncomplete_;
	private boolean moreUpdates_;

	private String safeJsStringLiteral(String value) {
		String s = WWebWidget.jsStringLiteral(value);
		return StringUtils.replace(s, "<", "<'+'");
	}

	public String learn(AbstractEventSignal.LearningListener slot)
			throws IOException {
		if (slot.getType() == SlotType.PreLearnStateless) {
			this.learning_ = true;
		}
		this.learningIncomplete_ = false;
		slot.trigger();
		StringWriter js = new StringWriter();
		this.collectJS(js);
		String result = js.toString();
		logger.debug(new StringWriter().append("learned: ").append(result)
				.toString());
		if (slot.getType() == SlotType.PreLearnStateless) {
			slot.undoTrigger();
			this.collectJS((Writer) null);
			this.learning_ = false;
		} else {
			this.statelessJS_.append(result);
		}
		if (!this.learningIncomplete_) {
			slot.setJavaScript(result);
		}
		this.collectJS(this.statelessJS_);
		return result;
	}

	static boolean isAbsoluteUrl(String url) {
		return url.indexOf("://") != -1;
	}

	static void appendAttribute(EscapeOStream eos, String name, String value) {
		eos.append(" ").append(name).append("=\"");
		eos.pushEscape(EscapeOStream.RuleSet.HtmlAttribute);
		eos.append(value);
		eos.popEscape();
		eos.append('"');
	}
}
