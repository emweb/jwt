package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import eu.webtoolkit.jwt.servlet.WebResponse;
import eu.webtoolkit.jwt.utils.MathUtils;
import eu.webtoolkit.jwt.utils.OrderedMultiMap;
import eu.webtoolkit.jwt.utils.StringUtils;

class WebRenderer implements SlotLearnerInterface {
	public WebRenderer(WebSession session) {
		super();
		this.session_ = session;
		this.visibleOnly_ = true;
		this.twoPhaseThreshold_ = 5000;
		this.expectedAckId_ = 0;
		this.cookiesToSet_ = new ArrayList<WebRenderer.Cookie>();
		this.currentFormObjects_ = new ArrayList<WObject>();
		this.currentFormObjectsList_ = "";
		this.collectedJS1_ = new StringWriter();
		this.collectedJS2_ = new StringWriter();
		this.invisibleJS_ = new StringWriter();
		this.statelessJS_ = new StringWriter();
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

	public void needUpdate(WWidget w, boolean laterOnly) {
		if (this.session_.getEnv().hasAjax()) {
			this.updateMap_.add(w);
			if (!laterOnly) {
				this.moreUpdates_ = true;
			}
		}
	}

	public void doneUpdate(WWidget w) {
		this.updateMap_.remove(w);
	}

	public void updateFormObjects(WWebWidget source, boolean checkDescendants) {
		this.formObjectsChanged_ = true;
	}

	public enum ResponseType {
		UpdateResponse, FullResponse;

		public int getValue() {
			return ordinal();
		}
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

	public List<WObject> getFormObjects() {
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
			this.setHeaders(response, "text/plain; charset=UTF-8");
		}
		response.out().append("window.location.reload(true);");
	}

	public final void letReloadJS(WebResponse response, boolean newSession)
			throws IOException {
		letReloadJS(response, newSession, false);
	}

	public void letReloadHTML(WebResponse response, boolean newSession)
			throws IOException {
		this.setHeaders(response, "text/html; charset=UTF-8");
		response.out().append("<html><script type=\"text/javascript\">");
		this.letReloadJS(response, newSession, true);
		response.out().append("</script><body></body></html>");
	}

	public boolean isDirty() {
		return !this.updateMap_.isEmpty()
				|| this.collectedJS1_.getBuffer().length() > 0
				|| this.collectedJS2_.getBuffer().length() > 0
				|| this.invisibleJS_.getBuffer().length() > 0;
	}

	public void serveMainWidget(WebResponse response,
			WebRenderer.ResponseType responseType) throws IOException {
		switch (responseType) {
		case UpdateResponse:
			this.serveJavaScriptUpdate(response);
			break;
		case FullResponse:
			switch (this.session_.getType()) {
			case Application:
				if (this.session_.getEnv().hasAjax()) {
					this.serveMainscript(response);
				} else {
					this.serveMainpage(response);
				}
				break;
			case WidgetSet:
				this.serveWidgetSet(response);
			}
		}
	}

	public void serveBootstrap(WebResponse response) throws IOException {
		boolean xhtml = this.session_.getEnv().getContentType() == WEnvironment.ContentType.XHTML1;
		Configuration conf = this.session_.getController().getConfiguration();
		FileServe boot = new FileServe(WtServlet.Boot_html);
		boot.setVar("XMLPREAMBLE", "");
		boot.setVar("DOCTYPE", this.session_.getDocType());
		StringWriter noJsRedirectUrl = new StringWriter();
		DomElement.htmlAttributeValue(noJsRedirectUrl, this.session_
				.getBootstrapUrl(response,
						WebSession.BootstrapOption.KeepInternalPath)
				+ "&js=no");
		if (xhtml) {
			boot.setVar("HTMLATTRIBUTES",
					"xmlns=\"http://www.w3.org/1999/xhtml\"");
			boot.setVar("METACLOSE", "/>");
			boot.setVar("AUTO_REDIRECT", "");
			boot.setVar("NOSCRIPT_TEXT", conf.getRedirectMessage());
		} else {
			if (this.session_.getEnv().agentIsIE()) {
				boot
						.setVar("HTMLATTRIBUTES",
								"xmlns:v=\"urn:schemas-microsoft-com:vml\" lang=\"en\" dir=\"ltr\"");
			} else {
				boot.setVar("HTMLATTRIBUTES", "lang=\"en\" dir=\"ltr\"");
			}
			boot.setVar("METACLOSE", ">");
			boot.setVar("AUTO_REDIRECT",
					"<noscript><meta http-equiv=\"refresh\" content=\"0;url="
							+ noJsRedirectUrl.toString() + "\"></noscript>");
			boot.setVar("NOSCRIPT_TEXT", conf.getRedirectMessage());
		}
		boot.setVar("BLANK_HTML", this.session_.getBootstrapUrl(response,
				WebSession.BootstrapOption.ClearInternalPath)
				+ "&amp;resource=blank");
		boot.setVar("SELF_URL", this.session_.getBootstrapUrl(response,
				WebSession.BootstrapOption.KeepInternalPath));
		boot.setVar("REDIRECT_URL", noJsRedirectUrl.toString());
		boot.setVar("SESSION_ID", this.session_.getSessionId());
		boot.setVar("RANDOMSEED", String.valueOf(MathUtils.randomInt() + 0));
		boot.setVar("RELOAD_IS_NEWSESSION", conf.isReloadIsNewSession());
		boot
				.setVar(
						"USE_COOKIES",
						conf.getSessionTracking() == Configuration.SessionTracking.CookiesURL);
		boot.setVar("HEADDECLARATIONS", this.getHeadDeclarations());
		response.addHeader("Cache-Control", "no-cache, no-store");
		response.addHeader("Expires", "-1");
		String contentType = xhtml ? "application/xhtml+xml" : "text/html";
		contentType += "; charset=UTF-8";
		this.setHeaders(response, contentType);
		boot.stream(response.out());
	}

	public void serveError(WebResponse response, Exception e,
			WebRenderer.ResponseType responseType) throws IOException {
		this.serveError(response, e.toString(), responseType);
	}

	public void serveError(WebResponse response, String message,
			WebRenderer.ResponseType responseType) throws IOException {
		boolean js = responseType != WebRenderer.ResponseType.FullResponse
				|| this.session_.getEnv().hasAjax();
		if (!js) {
			response.setContentType("text/html");
			response.out().append("<title>Error occurred.</title>").append(
					"<h2>Error occurred.</h2>").append(
					WWebWidget.escapeText(new WString(message), true)
							.toString()).append('\n');
		} else {
			this.collectedJS1_
					.append("document.title = 'Error occurred.';document.body.innerHtml='<h2>Error occurred.</h2>' +");
			DomElement.jsStringLiteral(this.collectedJS1_, message, '\'');
			this.collectedJS1_.append(";");
		}
	}

	public void setCookie(String name, String value, int maxAge, String domain,
			String path) {
		this.cookiesToSet_.add(new WebRenderer.Cookie(name, value, path,
				domain, maxAge));
	}

	public boolean isPreLearning() {
		return this.learning_;
	}

	public void ackUpdate(int updateId) {
		if (updateId == this.expectedAckId_) {
			this.setJSSynced(false);
		}
		++this.expectedAckId_;
	}

	public void streamRedirectJS(Writer out, String redirect)
			throws IOException {
		if (this.session_.getApp() != null) {
			out.append("if (window.").append(
					this.session_.getApp().getJavaScriptClass()).append(") ")
					.append(this.session_.getApp().getJavaScriptClass())
					.append("._p_.setHash('").append(
							this.session_.getApp().getInternalPath()).append(
							".');\n");
		}
		out.append("if (window.location.replace) window.location.replace('")
				.append(redirect).append("');else window.location.href='")
				.append(redirect).append("';\n");
	}

	private static class Cookie {
		public String name;
		public String value;
		public String path;
		public String domain;
		public int maxAge;

		public Cookie(String n, String v, String p, String d, int m) {
			this.name = n;
			this.value = v;
			this.path = p;
			this.domain = d;
			this.maxAge = m;
		}
	}

	private WebSession session_;
	private boolean visibleOnly_;
	private int twoPhaseThreshold_;
	private int expectedAckId_;
	private List<WebRenderer.Cookie> cookiesToSet_;
	private List<WObject> currentFormObjects_;
	private String currentFormObjectsList_;
	private boolean formObjectsChanged_;

	private void setHeaders(WebResponse response, String mimeType) {
		for (int i = 0; i < this.cookiesToSet_.size(); ++i) {
			String cookies = "";
			String value = this.cookiesToSet_.get(i).value;
			cookies += DomElement.urlEncodeS(this.cookiesToSet_.get(i).name)
					+ "=" + DomElement.urlEncodeS(value) + "; Version=1;";
			if (this.cookiesToSet_.get(i).maxAge != -1) {
				cookies += " Max-Age="
						+ String.valueOf(this.cookiesToSet_.get(i).maxAge)
						+ ";";
			}
			if (this.cookiesToSet_.get(i).domain.length() != 0) {
				cookies += " Domain=" + this.cookiesToSet_.get(i).domain + ";";
			}
			if (this.cookiesToSet_.get(i).path.length() != 0) {
				cookies += " Path=" + this.cookiesToSet_.get(i).path + ";";
			}
			if (cookies.length() != 0) {
				response.addHeader("Set-Cookie", cookies);
			}
		}
		this.cookiesToSet_.clear();
		response.setContentType(mimeType);
	}

	private void serveJavaScriptUpdate(WebResponse response) throws IOException {
		this.setHeaders(response, "text/plain; charset=UTF-8");
		this.collectJavaScript();
		response.out().append(this.collectedJS1_.toString()).append(
				this.session_.getApp().getJavaScriptClass()).append(
				"._p_.response(").append(String.valueOf(this.expectedAckId_))
				.append(");").append(this.collectedJS2_.toString());
	}

	private void serveMainpage(WebResponse response) throws IOException {
		Configuration conf = this.session_.getController().getConfiguration();
		WApplication app = this.session_.getApp();
		String redirect = this.session_.getRedirect();
		if (redirect.length() != 0) {
			response.setRedirect(redirect);
			return;
		}
		WWebWidget mainWebWidget = app.domRoot_;
		this.visibleOnly_ = true;
		DomElement mainElement = mainWebWidget.createSDomElement(app);
		this.setJSSynced(true);
		final boolean xhtml = app.getEnvironment().getContentType() == WEnvironment.ContentType.XHTML1;
		String styleSheets = "";
		for (int i = 0; i < app.styleSheets_.size(); ++i) {
			styleSheets += "<link href='"
					+ app.fixRelativeUrl(app.styleSheets_.get(i))
					+ "' rel='stylesheet' type='text/css'"
					+ (xhtml ? "/>" : ">");
		}
		app.styleSheetsAdded_ = 0;
		app.scriptLibrariesAdded_ = 0;
		FileServe page = new FileServe(WtServlet.Plain_html);
		page.setVar("XMLPREAMBLE", "");
		page.setVar("DOCTYPE", this.session_.getDocType());
		page.setVar("SESSIONID", this.session_.getSessionId());
		if (app.getEnvironment().agentIsSpiderBot()) {
			page.setVar("WTD_SESSIONID_FIELD", "");
		} else {
			page.setVar("WTD_SESSIONID_FIELD",
					"<input name='wtd' id='wtd' type='hidden' value='"
							+ this.session_.getSessionId() + "' />");
		}
		if (xhtml) {
			page.setVar("HTMLATTRIBUTES",
					"xmlns=\"http://www.w3.org/1999/xhtml\"");
			page.setVar("METACLOSE", "/>");
		} else {
			if (this.session_.getEnv().agentIsIE()) {
				page
						.setVar("HTMLATTRIBUTES",
								"xmlns:v=\"urn:schemas-microsoft-com:vml\" lang=\"en\" dir=\"ltr\"");
			} else {
				page.setVar("HTMLATTRIBUTES", "lang=\"en\" dir=\"ltr\"");
			}
			page.setVar("METACLOSE", ">");
		}
		String url = conf.getSessionTracking() == Configuration.SessionTracking.CookiesURL
				&& this.session_.getEnv().supportsCookies() ? this.session_
				.getBookmarkUrl(app.newInternalPath_) : this.session_
				.getMostRelativeUrl(app.newInternalPath_);
		url = app.fixRelativeUrl(url);
		url = StringUtils.replace(url, '&', "&amp;");
		page.setVar("RELATIVE_URL", url);
		if (conf.isInlineCss()) {
			page.setVar("STYLESHEET", app.getStyleSheet().getCssText(true));
		} else {
			page.setVar("STYLESHEET", "");
		}
		page.setVar("STYLESHEETS", styleSheets);
		page.setVar("TITLE", WWebWidget.escapeText(app.getTitle()).toString());
		page.setVar("HEADDECLARATIONS", this.getHeadDeclarations());
		app.titleChanged_ = false;
		String contentType = xhtml ? "application/xhtml+xml" : "text/html";
		contentType += "; charset=UTF-8";
		this.setHeaders(response, contentType);
		this.formObjectsChanged_ = true;
		this.currentFormObjectsList_ = this.createFormObjectsList(app);
		page.streamUntil(response.out(), "HTML");
		List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
		{
			EscapeOStream out = new EscapeOStream(response.out());
			mainElement.asHTML(out, timeouts);
			/* delete mainElement */;
		}
		StringWriter onload = new StringWriter();
		DomElement.createTimeoutJs(onload, timeouts, app);
		int refresh = conf.getSessionTimeout() / 3;
		for (int i = 0; i < timeouts.size(); ++i) {
			refresh = Math.min(refresh, 1 + timeouts.get(i).msec / 1000);
		}
		if (app.isQuited()) {
			refresh = 100000;
		}
		page.setVar("REFRESH", String.valueOf(refresh));
		page.stream(response.out());
		app.internalPathIsChanged_ = false;
	}

	private void serveMainscript(WebResponse response) throws IOException {
		Configuration conf = this.session_.getController().getConfiguration();
		this.setHeaders(response, "text/javascript; charset=UTF-8");
		String redirect = this.session_.getRedirect();
		if (redirect.length() != 0) {
			this.streamRedirectJS(response.out(), redirect);
			return;
		}
		WApplication app = this.session_.getApp();
		final boolean xhtml = app.getEnvironment().getContentType() == WEnvironment.ContentType.XHTML1;
		this.formObjectsChanged_ = true;
		this.currentFormObjectsList_ = this.createFormObjectsList(app);
		FileServe script = new FileServe(WtServlet.Wt_js);
		script.setVar("DEBUG", conf.isDebug());
		script.setVar("WT_CLASS", "Wt2_99_2");
		script.setVar("APP_CLASS", app.getJavaScriptClass());
		script.setVar("AUTO_JAVASCRIPT", app.autoJavaScript_);
		script.setVar("STRICTLY_SERIALIZED_EVENTS", conf.isSerializedEvents());
		boolean innerHtml = !xhtml || app.getEnvironment().agentIsGecko();
		script.setVar("INNER_HTML", innerHtml);
		script.setVar("FORM_OBJECTS", '[' + this.currentFormObjectsList_ + ']');
		script.setVar("RELATIVE_URL", '"' + this.session_.getBootstrapUrl(
				response, WebSession.BootstrapOption.ClearInternalPath) + '"');
		script.setVar("KEEP_ALIVE", String
				.valueOf(conf.getSessionTimeout() / 2));
		script.setVar("INITIAL_HASH", app.getInternalPath());
		script.setVar("INDICATOR_TIMEOUT", "500");
		script
				.setVar("SERVER_PUSH_TIMEOUT",
						conf.getServerPushTimeout() * 1000);
		script.setVar("ONLOAD", "loadWidgetTree();");
		script.setVar("WT_HISTORY_PREFIX", "Wt-history");
		script.stream(response.out());
		app.autoJavaScriptChanged_ = false;
		this.streamCommJs(app, response.out());
		WWebWidget mainWebWidget = app.domRoot_;
		this.visibleOnly_ = true;
		app.loadingIndicatorWidget_.show();
		DomElement mainElement = mainWebWidget.createSDomElement(app);
		app.loadingIndicatorWidget_.hide();
		if (conf.isInlineCss()) {
			app.getStyleSheet().javaScriptUpdate(app, response.out(), true);
		}
		app.styleSheetsAdded_ = app.styleSheets_.size();
		this.loadStyleSheets(response.out(), app);
		app.scriptLibrariesAdded_ = app.scriptLibraries_.size();
		this.loadScriptLibraries(response.out(), app, true);
		response.out().append('\n').append(app.getBeforeLoadJavaScript());
		response.out().append("window.loadWidgetTree = function(){\n");
		String cvar = "";
		{
			EscapeOStream sout = new EscapeOStream(response.out());
			cvar = mainElement.asJavaScript(sout, DomElement.Priority.Create);
		}
		response.out().append("document.body.appendChild(").append(cvar)
				.append(");\n");
		{
			EscapeOStream sout = new EscapeOStream(response.out());
			mainElement.asJavaScript(sout, DomElement.Priority.Update);
		}
		/* delete mainElement */;
		this.setJSSynced(true);
		this.preLearnStateless(app, this.collectedJS1_);
		response.out().append(this.collectedJS1_.toString());
		this.collectedJS1_ = new StringWriter();
		this.updateLoadIndicator(response.out(), app, true);
		response.out().append(app.getAfterLoadJavaScript()).append(
				"{var e=null;").append(
				app.hideLoadingIndicator_.getJavaScript()).append("}").append(
				"};\n");
		response.out().append("scriptLoaded = true; if (isLoaded) onLoad();\n");
		this.loadScriptLibraries(response.out(), app, false);
	}

	private void serveWidgetSet(WebResponse response) throws IOException {
		Configuration conf = this.session_.getController().getConfiguration();
		WApplication app = this.session_.getApp();
		this.setHeaders(response, "text/javascript; charset=UTF-8");
		final boolean xhtml = app.getEnvironment().getContentType() == WEnvironment.ContentType.XHTML1;
		final boolean innerHtml = !xhtml || app.getEnvironment().agentIsGecko();
		this.formObjectsChanged_ = true;
		this.currentFormObjectsList_ = this.createFormObjectsList(app);
		FileServe script = new FileServe(WtServlet.Wt_js);
		script.setVar("DEBUG", conf.isDebug());
		script.setVar("WT_CLASS", "Wt2_99_2");
		script.setVar("APP_CLASS", app.getJavaScriptClass());
		script.setVar("AUTO_JAVASCRIPT", app.autoJavaScript_);
		script.setVar("STRICTLY_SERIALIZED_EVENTS", conf.isSerializedEvents());
		script.setVar("INNER_HTML", innerHtml);
		script.setVar("FORM_OBJECTS", '[' + this.currentFormObjectsList_ + ']');
		script.setVar("RELATIVE_URL", '"' + this.session_.getBootstrapUrl(
				response, WebSession.BootstrapOption.KeepInternalPath) + '"');
		script.setVar("KEEP_ALIVE", String
				.valueOf(conf.getSessionTimeout() / 2));
		script.setVar("INITIAL_HASH", app.getInternalPath());
		script.setVar("INDICATOR_TIMEOUT", "500");
		script
				.setVar("SERVER_PUSH_TIMEOUT",
						conf.getServerPushTimeout() * 1000);
		script.setVar("ONLOAD", "");
		script.stream(response.out());
		app.autoJavaScriptChanged_ = false;
		this.streamCommJs(app, response.out());
		WWebWidget mainWebWidget = app.domRoot_;
		this.visibleOnly_ = true;
		app.loadingIndicatorWidget_.show();
		DomElement mainElement = mainWebWidget.createSDomElement(app);
		app.loadingIndicatorWidget_.hide();
		if (conf.isInlineCss()) {
			app.getStyleSheet().javaScriptUpdate(app, response.out(), true);
		}
		app.styleSheetsAdded_ = app.styleSheets_.size();
		this.loadStyleSheets(response.out(), app);
		app.scriptLibrariesAdded_ = app.scriptLibraries_.size();
		this.loadScriptLibraries(response.out(), app, true);
		response.out().append('\n').append(app.getBeforeLoadJavaScript());
		String cvar = "";
		{
			EscapeOStream sout = new EscapeOStream(response.out());
			mainElement.asJavaScript(sout, DomElement.Priority.Create);
			cvar = mainElement.asJavaScript(sout, DomElement.Priority.Update);
		}
		/* delete mainElement */;
		response.out().append("document.body.insertBefore(").append(cvar)
				.append(",document.body.firstChild);").append("{var e=null; ")
				.append(app.hideLoadingIndicator_.getJavaScript()).append("}")
				.append('\n');
		app.domRoot2_.rootAsJavaScript(app, response.out(), true);
		this.setJSSynced(true);
		this.preLearnStateless(app, this.collectedJS1_);
		response.out().append(this.collectedJS1_.toString());
		this.collectedJS1_ = new StringWriter();
		this.updateLoadIndicator(response.out(), app, true);
		String historyE = app.getEnvironment().getParameter("Wt-history");
		if (historyE != null) {
			response.out().append("Wt2_99_2").append(".history.initialize('")
					.append(historyE.charAt(0)).append("-field', '").append(
							historyE.charAt(0)).append("-iframe');\n");
		}
		response.out().append(app.getAfterLoadJavaScript()).append(
				app.getJavaScriptClass()).append("._p_.load();\n");
		if (!(app.getTitle().length() == 0)) {
			response.out().append(app.getJavaScriptClass()).append(
					"._p_.setTitle(");
			DomElement.jsStringLiteral(response.out(), app.getTitle()
					.toString(), '\'');
			response.out().append(");\n");
		}
		app.titleChanged_ = false;
		this.loadScriptLibraries(response.out(), app, false);
	}

	private void streamCommJs(WApplication app, Writer out) throws IOException {
		Configuration conf = this.session_.getController().getConfiguration();
		FileServe js = new FileServe(
				app.getAjaxMethod() == WApplication.AjaxMethod.XMLHttpRequest ? WtServlet.CommAjax_js
						: WtServlet.CommScript_js);
		js.setVar("APP_CLASS", app.getJavaScriptClass());
		js.setVar("CLOSE_CONNECTION",
				conf.getServerType() == Configuration.ServerType.WtHttpdServer);
		js.stream(out);
	}

	private void collectJavaScript() throws IOException {
		WApplication app = this.session_.getApp();
		Configuration conf = this.session_.getController().getConfiguration();
		String redirect = this.session_.getRedirect();
		if (redirect.length() != 0) {
			this.streamRedirectJS(this.collectedJS1_, redirect);
			return;
		}
		this.collectedJS1_.append(this.invisibleJS_.toString());
		this.invisibleJS_ = new StringWriter();
		if (conf.isInlineCss()) {
			app.getStyleSheet()
					.javaScriptUpdate(app, this.collectedJS1_, false);
		}
		this.loadStyleSheets(this.collectedJS1_, app);
		this.loadScriptLibraries(this.collectedJS1_, app, true);
		this.loadScriptLibraries(this.collectedJS2_, app, false);
		this.collectedJS1_.append(app.getNewBeforeLoadJavaScript());
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
					if (!this.learning_ && this.visibleOnly_) {
						if (!w.isStubbed()) {
							w.getSDomChanges(changes, app);
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
		out.append(app.getAfterLoadJavaScript());
		if (app.isQuited()) {
			out.append(app.getJavaScriptClass()).append("._p_.quit();");
			WContainerWidget timers = app.getTimerRoot();
			DomElement d = DomElement.getForUpdate(timers,
					DomElementType.DomElement_DIV);
			d.setProperty(Property.PropertyInnerHTML, "");
			EscapeOStream sout = new EscapeOStream(out);
			d.asJavaScript(sout, DomElement.Priority.Update);
			/* delete d */;
		}
		this.updateLoadIndicator(out, app, false);
		out.append('}');
	}

	private void loadStyleSheets(Writer out, WApplication app)
			throws IOException {
		int first = app.styleSheets_.size() - app.styleSheetsAdded_;
		for (int i = first; i < app.styleSheets_.size(); ++i) {
			out.append("Wt2_99_2").append(".addStyleSheet('").append(
					app.fixRelativeUrl(app.styleSheets_.get(i)))
					.append("');\n");
		}
		app.styleSheetsAdded_ = 0;
	}

	private void loadScriptLibraries(Writer out, WApplication app, boolean start)
			throws IOException {
		int first = app.scriptLibraries_.size() - app.scriptLibrariesAdded_;
		if (start) {
			for (int i = first; i < app.scriptLibraries_.size(); ++i) {
				String uri = app
						.fixRelativeUrl(app.scriptLibraries_.get(i).uri);
				out.append(app.scriptLibraries_.get(i).beforeLoadJS).append(
						app.getJavaScriptClass()).append("._p_.loadScript('")
						.append(uri).append("',");
				DomElement.jsStringLiteral(out,
						app.scriptLibraries_.get(i).symbol, '\'');
				out.append(");\n");
				out.append(app.getJavaScriptClass()).append("._p_.onJsLoad(\"")
						.append(uri).append("\",function() {\n");
			}
		} else {
			for (int i = first; i < app.scriptLibraries_.size(); ++i) {
				out.append("});");
			}
			app.scriptLibrariesAdded_ = 0;
		}
	}

	private void updateLoadIndicator(Writer out, WApplication app, boolean all)
			throws IOException {
		if (app.showLoadingIndicator_.needUpdate() || all) {
			out.append("showLoadingIndicator = function() {var e = null;\n")
					.append(app.showLoadingIndicator_.getJavaScript()).append(
							"};\n");
			app.showLoadingIndicator_.updateOk();
		}
		if (app.hideLoadingIndicator_.needUpdate() || all) {
			out.append("hideLoadingIndicator = function() {var e = null;\n")
					.append(app.hideLoadingIndicator_.getJavaScript()).append(
							"};\n");
			app.hideLoadingIndicator_.updateOk();
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
		for (int i = 0; i < this.currentFormObjects_.size(); ++i) {
			if (i != 0) {
				result += ',';
			}
			result += "'" + this.currentFormObjects_.get(i).getFormName() + "'";
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
		Map<String, AbstractEventSignal> ss = this.session_.getApp()
				.exposedSignals();
		for (Iterator<Map.Entry<String, AbstractEventSignal>> i_it = ss
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<String, AbstractEventSignal> i = i_it.next();
			if (i.getValue().getSender() == app) {
				i.getValue().processPreLearnStateless(this);
			}
			WWidget ww = ((i.getValue().getSender()) instanceof WWidget ? (WWidget) (i
					.getValue().getSender())
					: null);
			if (ww != null && !ww.isStubbed()) {
				WWidget a = ww.getAdam();
				if (a == app.domRoot_ || a == app.domRoot2_) {
					i.getValue().processPreLearnStateless(this);
				}
			}
		}
		out.append(this.statelessJS_.toString());
		this.statelessJS_ = new StringWriter();
	}

	private StringWriter collectedJS1_;
	private StringWriter collectedJS2_;
	private StringWriter invisibleJS_;
	private StringWriter statelessJS_;

	private void collectJS(Writer js) throws IOException {
		List<DomElement> changes = new ArrayList<DomElement>();
		this.collectChanges(changes);
		if (js != null) {
			EscapeOStream sout = new EscapeOStream(js);
			for (int i = 0; i < changes.size(); ++i) {
				changes.get(i).asJavaScript(sout, DomElement.Priority.Delete);
			}
			for (int i = 0; i < changes.size(); ++i) {
				changes.get(i).asJavaScript(sout, DomElement.Priority.Update);
				/* delete changes.get(i) */;
			}
		} else {
			for (int i = 0; i < changes.size(); ++i) {
				/* delete changes.get(i) */;
			}
		}
		if (this.session_.getType() == WebSession.Type.WidgetSet) {
			return;
		}
		WApplication app = this.session_.getApp();
		if (js != null) {
			if (app.titleChanged_) {
				js.append(app.getJavaScriptClass()).append("._p_.setTitle(");
				DomElement.jsStringLiteral(js, app.getTitle().toString(), '\'');
				js.append(");\n");
			}
			if (app.internalPathIsChanged_) {
				js.append(app.getJavaScriptClass()).append("._p_.setHash('")
						.append(app.newInternalPath_).append("');\n");
			}
			js.append(app.getAfterLoadJavaScript());
		} else {
			app.getAfterLoadJavaScript();
		}
		app.internalPathIsChanged_ = false;
		app.titleChanged_ = false;
	}

	private String getHeadDeclarations() {
		if (this.session_.getFavicon().length() != 0) {
			final boolean xhtml = this.session_.getEnv().getContentType() == WEnvironment.ContentType.XHTML1;
			return "<link rel=\"icon\" type=\"image/vnd.microsoft.icon\" href=\""
					+ this.session_.getFavicon() + (xhtml ? "\"/>" : "\">");
		} else {
			return "";
		}
	}

	private Set<WWidget> UpdateMap;
	private Set<WWidget> updateMap_;
	private boolean learning_;
	private boolean moreUpdates_;

	public String learn(AbstractEventSignal.LearningListener slot)
			throws IOException {
		this.collectJS(this.statelessJS_);
		if (slot.getType() == SlotType.PreLearnStateless) {
			this.learning_ = true;
		}
		slot.trigger();
		StringWriter js = new StringWriter();
		this.collectJS(js);
		String result = js.toString();
		if (slot.getType() == SlotType.PreLearnStateless) {
			slot.undoTrigger();
			this.collectJS((Writer) null);
			this.learning_ = false;
		} else {
			this.statelessJS_.append(result);
		}
		slot.setJavaScript(result);
		return result;
	}
}
