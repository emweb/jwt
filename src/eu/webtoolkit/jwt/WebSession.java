/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import eu.webtoolkit.jwt.servlet.UploadedFile;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

class WebSession {
	enum State {
		JustCreated, Loaded, Dead;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	public WebSession(WtServlet controller, String sessionId,
			ApplicationType type, String favicon, WebRequest request,
			WEnvironment env) {
		this.mutex_ = new ReentrantLock();
		this.type_ = type;
		this.favicon_ = favicon;
		this.state_ = WebSession.State.JustCreated;
		this.sessionId_ = sessionId;
		this.controller_ = controller;
		this.renderer_ = new WebRenderer(this);
		this.applicationName_ = "";
		this.bookmarkUrl_ = "";
		this.baseUrl_ = "";
		this.absoluteBaseUrl_ = "";
		this.applicationUrl_ = "";
		this.deploymentPath_ = "";
		this.redirect_ = "";
		this.pollResponse_ = null;
		this.updatesPending_ = false;
		this.embeddedEnv_ = new WEnvironment(this);
		this.app_ = null;
		this.debug_ = this.controller_.getConfiguration().isDebug();
		this.handlers_ = new ArrayList<WebSession.Handler>();
		this.emitStack_ = new ArrayList<WObject>();
		this.recursiveEventLoop_ = null;
		this.env_ = env != null ? env : this.embeddedEnv_;
		if (request != null) {
			this.deploymentPath_ = request.getScriptName();
		} else {
			this.deploymentPath_ = "/";
		}
		this.applicationUrl_ = this.deploymentPath_;
		this.applicationName_ = this.applicationUrl_;
		this.baseUrl_ = this.applicationUrl_;
		int slashpos = this.applicationName_.lastIndexOf('/');
		if (slashpos != -1) {
			this.applicationName_ = this.applicationName_
					.substring(slashpos + 1);
			this.baseUrl_ = this.baseUrl_.substring(0, 0 + slashpos + 1);
		}
		this.log("notice").append("Session created");
	}

	public WebSession(WtServlet controller, String sessionId,
			ApplicationType type, String favicon, WebRequest request) {
		this(controller, sessionId, type, favicon, request, (WEnvironment) null);
	}

	public static WebSession getInstance() {
		WebSession.Handler handler = WebSession.Handler.getInstance();
		return handler != null ? handler.getSession() : null;
	}

	public ApplicationType getType() {
		return this.type_;
	}

	public String getFavicon() {
		return this.favicon_;
	}

	public String getDocType() {
		final boolean xhtml = this.env_.getContentType() == WEnvironment.ContentType.XHTML1;
		if (xhtml) {
			return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
		} else {
			return "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
		}
	}

	public String getSessionId() {
		return this.sessionId_;
	}

	public WtServlet getController() {
		return this.controller_;
	}

	public WEnvironment getEnv() {
		return this.env_;
	}

	public WApplication getApp() {
		return this.app_;
	}

	public WebRenderer getRenderer() {
		return this.renderer_;
	}

	public boolean isDebug() {
		return this.debug_;
	}

	public void redirect(String url) {
		this.redirect_ = url;
		if (this.redirect_.length() == 0) {
			this.redirect_ = "?";
		}
	}

	public String getRedirect() {
		String result = this.redirect_;
		this.redirect_ = "";
		return result;
	}

	public void setApplication(WApplication app) {
		this.app_ = app;
	}

	public WLogEntry log(String type) {
		Configuration conf = this.controller_.getConfiguration();
		WLogEntry e = conf.getLogger().getEntry();
		return e;
	}

	public void notify(WEvent event) throws IOException {
		WebSession.Handler handler = event.handler;
		WebRequest request = handler.getRequest();
		WebResponse response = handler.getResponse();
		if (WebSession.Handler.getInstance() != handler) {
			WebSession.Handler.getInstance().setRequest(request, response);
		}
		if (event.renderOnly) {
			this.render(handler, event.responseType);
			return;
		}
		String requestE = request.getParameter("request");
		switch (this.state_) {
		case JustCreated:
			this.render(handler, event.responseType);
			break;
		case Loaded:
			if (event.responseType == WebRenderer.ResponseType.Script) {
				if (!this.env_.doesAjax_) {
					String hashE = request.getParameter("_");
					String scaleE = request.getParameter("scale");
					this.env_.doesAjax_ = true;
					this.env_.doesCookies_ = request.getHeaderValue("Cookie")
							.length() != 0;
					try {
						this.env_.dpiScale_ = scaleE != null ? Double
								.parseDouble(scaleE) : 1;
					} catch (NumberFormatException e) {
						this.env_.dpiScale_ = 1;
					}
					if (hashE != null) {
						this.env_.setInternalPath(hashE);
					}
					this.app_.enableAjax();
					if (this.env_.getInternalPath().length() > 1) {
						this.app_.changeInternalPath(this.env_
								.getInternalPath());
					}
				}
				this.render(handler, event.responseType);
			} else {
				try {
					if (0 != 0) {
						this.app_.requestTooLarge().trigger(0);
					}
				} catch (RuntimeException e) {
					this.log("error").append(
							"Exception in WApplication::requestTooLarge")
							.append(e.toString());
					throw e;
				}
				String resourceE = request.getParameter("resource");
				String signalE = this.getSignal(request, "");
				if (signalE != null) {
					this.progressiveBoot_ = false;
				}
				if (requestE != null && requestE.equals("resource")
						&& resourceE != null) {
					if (resourceE.equals("blank")) {
						handler.getResponse().setContentType("text/html");
						handler
								.getResponse()
								.out()
								.append(
										"<html><head><title>bhm</title></head><body>&#160;</body></html>");
						handler.getResponse().flush();
						handler.setRequest((WebRequest) null,
								(WebResponse) null);
					} else {
						WResource resource = this.decodeResource(resourceE);
						if (resource != null) {
							try {
								resource.handle(request, response);
								handler.setRequest((WebRequest) null,
										(WebResponse) null);
							} catch (RuntimeException e) {
								this.log("error").append(
										"Exception while streaming resource")
										.append(e.toString());
								throw e;
							}
						} else {
							handler.getResponse().setContentType("text/html");
							handler
									.getResponse()
									.out()
									.append(
											"<html><body><h1>Refusing to respond.</h1></body></html>");
							handler.getResponse().flush();
							handler.setRequest((WebRequest) null,
									(WebResponse) null);
						}
					}
				} else {
					this.env_.urlScheme_ = request.getScheme();
					if (signalE != null) {
						String ackIdE = request.getParameter("ackId");
						try {
							if (ackIdE != null) {
								this.renderer_.ackUpdate(Integer
										.parseInt(ackIdE));
							}
						} catch (NumberFormatException e) {
							this.log("error").append("Could not parse ackId: ")
									.append(ackIdE);
						}
						if (this.pollResponse_ != null) {
							this.pollResponse_.flush();
							this.pollResponse_ = null;
						}
						if (!signalE.equals("res") && !signalE.equals("poll")) {
							try {
								this.notifySignal(event);
							} catch (RuntimeException e) {
								this.log("error").append(
										"Error during event handling: ")
										.append(e.toString());
								throw e;
							}
						} else {
							if (signalE.equals("poll") && !this.updatesPending_) {
								this.pollResponse_ = handler.getResponse();
								handler.setRequest((WebRequest) null,
										(WebResponse) null);
							}
						}
					} else {
						this.log("notice").append("Refreshing session");
						if (event.responseType == WebRenderer.ResponseType.Page) {
							if (request.getPathInfo().length() != 0) {
								this.app_.changeInternalPath(request
										.getPathInfo());
							} else {
								String hashE = request.getParameter("_");
								if (hashE != null) {
									this.app_.changeInternalPath(hashE);
								}
							}
						}
						this.env_.parameters_ = handler.getRequest()
								.getParameterMap();
						this.app_.refresh();
					}
					if (handler.getResponse() != null
							&& !(this.recursiveEventLoop_ != null)) {
						this.render(handler, event.responseType);
					}
				}
			}
		}
	}

	public boolean handleRequest(WebRequest request, WebResponse response)
			throws IOException {
		Configuration conf = this.controller_.getConfiguration();
		this.mutex_.lock();
		try {
			WebSession.Handler handler = new WebSession.Handler(this, request,
					response);
			String wtdE = request.getParameter("wtd");
			String requestE = request.getParameter("request");
			WebRenderer.ResponseType responseType = WebRenderer.ResponseType.Page;
			if ((!(wtdE != null) || !wtdE.equals(this.sessionId_))
					&& this.state_ != WebSession.State.JustCreated
					&& (requestE != null && (requestE.equals("signal") || requestE
							.equals("resource")))) {
				handler.getResponse().setContentType("text/html");
				handler
						.getResponse()
						.out()
						.append(
								"<html><head></head><body>CSRF prevention</body></html>");
			} else {
				try {
					switch (this.state_) {
					case JustCreated: {
						switch (this.type_) {
						case Application: {
							this.init(request);
							if (requestE != null) {
								if (requestE.equals("jsupdate")
										|| requestE.equals("script")) {
									this
											.log("notice")
											.append(
													"Signal from dead session, sending reload.");
									this.renderer_.letReloadJS(handler
											.getResponse(), true);
									handler.killSession();
									break;
								} else {
									if (requestE.equals("resource")) {
										this
												.log("notice")
												.append(
														"Not serving bootstrap for resource.");
										handler.getResponse().setContentType(
												"text/html");
										handler
												.getResponse()
												.out()
												.append(
														"<html><head></head><body></body></html>");
										handler.killSession();
										break;
									}
								}
							}
							boolean forcePlain = this.env_.agentIsSpiderBot()
									|| !this.env_.agentSupportsAjax();
							this.progressiveBoot_ = !forcePlain
									&& conf.progressiveBootstrap();
							if (forcePlain || this.progressiveBoot_) {
								this.env_.doesAjax_ = false;
								this.env_.doesCookies_ = false;
								try {
									String internalPath = this.env_
											.getCookie("WtInternalPath");
									this.env_.setInternalPath(internalPath);
								} catch (RuntimeException e) {
								}
								if (!this.start()) {
									throw new WtException(
											"Could not start application.");
								}
								this.app_.notify(new WEvent(handler,
										WebRenderer.ResponseType.Page));
								this.setLoaded();
								if (this.env_.agentIsSpiderBot()) {
									handler.killSession();
								}
							} else {
								this.serveResponse(handler,
										WebRenderer.ResponseType.Page);
								this.setState(WebSession.State.Loaded, 10);
							}
							break;
						}
						case WidgetSet:
							this.init(request);
							this.env_.doesAjax_ = true;
							if (!this.start()) {
								throw new WtException(
										"Could not start application.");
							}
							this.app_.notify(new WEvent(handler,
									WebRenderer.ResponseType.Script));
							this.setLoaded();
						}
						break;
					}
					case Loaded: {
						responseType = WebRenderer.ResponseType.Page;
						if (requestE != null) {
							if (requestE.equals("jsupdate")) {
								responseType = WebRenderer.ResponseType.Update;
							} else {
								if (requestE.equals("script")) {
									responseType = WebRenderer.ResponseType.Script;
								}
							}
						}
						if (!(this.app_ != null)) {
							if (responseType == WebRenderer.ResponseType.Script) {
								String hashE = request.getParameter("_");
								String scaleE = request.getParameter("scale");
								this.env_.doesAjax_ = true;
								this.env_.doesCookies_ = request
										.getHeaderValue("Cookie").length() != 0;
								try {
									this.env_.dpiScale_ = scaleE != null ? Double
											.parseDouble(scaleE)
											: 1;
								} catch (NumberFormatException e) {
									this.env_.dpiScale_ = 1;
								}
								if (hashE != null) {
									this.env_.setInternalPath(hashE);
								}
								if (!this.start()) {
									throw new WtException(
											"Could not start application.");
								}
							} else {
								String jsE = request.getParameter("js");
								if (jsE != null && jsE.equals("no")) {
									if (!this.start()) {
										throw new WtException(
												"Could not start application.");
									}
								} else {
									if (!conf.isReloadIsNewSession()
											&& wtdE != null
											&& wtdE.equals(this.sessionId_)) {
										this.serveResponse(handler,
												WebRenderer.ResponseType.Page);
										this.setState(WebSession.State.Loaded,
												10);
									} else {
										handler.getResponse().setContentType(
												"text/html");
										handler
												.getResponse()
												.out()
												.append(
														"<html><body><h1>Refusing to respond.</h1></body></html>");
									}
									break;
								}
							}
							this.state_ = WebSession.State.JustCreated;
						}
						boolean requestForResource = requestE != null
								&& requestE.equals("resource");
						{
							this.app_.notify(new WEvent(handler, responseType));
							if (handler.getResponse() != null
									&& !requestForResource) {
								this.app_.notify(new WEvent(handler,
										responseType, true));
							}
						}
						this.setLoaded();
						break;
					}
					case Dead:
						throw new WtException(
								"Internal error: WebSession is dead?");
					}
				} catch (WtException e) {
					this.log("fatal").append(e.toString());
					e.printStackTrace();
					handler.killSession();
					if (handler.getResponse() != null) {
						this.serveError(handler, e.toString(), responseType);
					}
				} catch (RuntimeException e) {
					this.log("fatal").append(e.toString());
					e.printStackTrace();
					handler.killSession();
					if (handler.getResponse() != null) {
						this.serveError(handler, e.toString(), responseType);
					}
				}
			}
			if (handler.getResponse() != null) {
				handler.getResponse().flush();
			}
			;
			return !handler.isSessionDead();
		} finally {
			this.mutex_.unlock();
		}
	}

	public void pushUpdates() {
		try {
			if (!this.renderer_.isDirty()) {
				return;
			}
			this.updatesPending_ = true;
			if (this.pollResponse_ != null) {
				this.renderer_.serveResponse(this.pollResponse_,
						WebRenderer.ResponseType.Update);
				this.pollResponse_.flush();
				this.pollResponse_ = null;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void doRecursiveEventLoop() {
		this.log("error").append(
				"Cannot do recursive event loop without threads");
	}

	public void pushEmitStack(WObject o) {
		this.emitStack_.add(o);
	}

	public void popEmitStack() {
		this.emitStack_.remove(this.emitStack_.size() - 1);
	}

	public WObject getEmitStackTop() {
		if (!this.emitStack_.isEmpty()) {
			return this.emitStack_.get(this.emitStack_.size() - 1);
		} else {
			return null;
		}
	}

	public boolean isDone() {
		return this.state_ == WebSession.State.Dead;
	}

	public WebSession.State getState() {
		return this.state_;
	}

	public boolean isProgressiveBoot() {
		return this.progressiveBoot_;
	}

	public String getApplicationName() {
		return this.applicationName_;
	}

	public String getApplicationUrl() {
		return this.applicationUrl_ + this.getSessionQuery();
	}

	public String getDeploymentPath() {
		return this.deploymentPath_;
	}

	public String getMostRelativeUrl(String internalPath) {
		return this.appendSessionQuery(this.getBookmarkUrl(internalPath));
	}

	public final String getMostRelativeUrl() {
		return getMostRelativeUrl("");
	}

	public String appendInternalPath(String baseUrl, String internalPath) {
		if (internalPath.length() == 0 || internalPath.equals("/")) {
			if (baseUrl.length() == 0) {
				return "?";
			} else {
				return baseUrl;
			}
		} else {
			if (this.applicationName_.length() == 0) {
				return baseUrl + "?_=" + DomElement.urlEncodeS(internalPath);
			} else {
				return baseUrl + DomElement.urlEncodeS(internalPath);
			}
		}
	}

	public String appendSessionQuery(String url) {
		String result = url;
		if (this.env_.agentIsSpiderBot()) {
			return result;
		}
		int questionPos = result.indexOf('?');
		if (questionPos == -1) {
			result += this.getSessionQuery();
		} else {
			if (questionPos == result.length() - 1) {
				result += this.getSessionQuery().substring(1);
			} else {
				result += '&' + this.getSessionQuery().substring(1);
			}
		}
		if (result.startsWith("?")) {
			result = this.applicationUrl_ + result;
		}
		return WebSession.Handler.getInstance().getResponse().encodeURL(result);
	}

	public String ajaxCanonicalUrl(WebResponse request) {
		String hashE = null;
		if (this.applicationName_.length() == 0) {
			hashE = request.getParameter("_");
		}
		if (request.getPathInfo().length() != 0 || hashE != null
				&& hashE.length() > 1) {
			String url = "";
			if (request.getPathInfo().length() != 0) {
				String pi = request.getPathInfo();
				for (int t = pi.indexOf('/'); t != -1; t = pi.indexOf('/',
						t + 1)) {
					url += "../";
				}
				url += this.getApplicationName();
			} else {
				url = this.getBaseUrl() + this.getApplicationName();
			}
			url += '#' + (this.app_ != null ? this.app_.getInternalPath()
					: this.env_.getInternalPath());
			return url;
		} else {
			return "";
		}
	}

	enum BootstrapOption {
		ClearInternalPath, KeepInternalPath;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	public String getBootstrapUrl(WebResponse response,
			WebSession.BootstrapOption option) {
		switch (option) {
		case KeepInternalPath: {
			String internalPath = "";
			if (this.applicationName_.length() == 0) {
				internalPath = this.app_ != null ? this.app_.getInternalPath()
						: this.env_.getInternalPath();
				if (internalPath.length() > 1) {
					return this.appendSessionQuery("?_="
							+ DomElement.urlEncodeS(internalPath));
				} else {
					return this.appendSessionQuery("");
				}
			} else {
				internalPath = WebSession.Handler.getInstance().getRequest()
						.getPathInfo();
			}
			if (internalPath.length() > 1) {
				String lastPart = internalPath.substring(internalPath
						.lastIndexOf('/') + 1);
				return this.appendSessionQuery(lastPart);
			} else {
				return this.appendSessionQuery(this.applicationName_);
			}
		}
		case ClearInternalPath:
			if (WebSession.Handler.getInstance().getRequest().getPathInfo()
					.length() > 1) {
				return this.appendSessionQuery(this.baseUrl_
						+ this.applicationName_);
			} else {
				return this.appendSessionQuery(this.applicationName_);
			}
		default:
			assert false;
		}
		return "";
	}

	public String getBookmarkUrl(String internalPath) {
		String result = this.bookmarkUrl_;
		if (!this.env_.hasAjax()
				&& result.indexOf("://") == -1
				&& (this.env_.getInternalPath().length() > 1 || internalPath
						.length() > 1)) {
			result = this.baseUrl_ + this.applicationName_;
		}
		return this.appendInternalPath(result, internalPath);
	}

	public String getBookmarkUrl() {
		if (this.app_ != null) {
			return this.getBookmarkUrl(this.app_.getInternalPath());
		} else {
			return this.getBookmarkUrl(this.env_.getInternalPath());
		}
	}

	public String getAbsoluteBaseUrl() {
		return this.absoluteBaseUrl_;
	}

	public String getBaseUrl() {
		return this.baseUrl_;
	}

	public String getCgiValue(String varName) {
		WebRequest request = WebSession.Handler.getInstance().getRequest();
		if (request != null) {
			return "";
		} else {
			return "";
		}
	}

	public String getCgiHeader(String headerName) {
		WebRequest request = WebSession.Handler.getInstance().getRequest();
		if (request != null) {
			return request.getHeaderValue(headerName);
		} else {
			return "";
		}
	}

	static class Handler {
		public Handler(WebSession session, WebRequest request,
				WebResponse response) {
			this.session_ = session;
			this.request_ = request;
			this.response_ = response;
			this.killed_ = false;
			this.init();
		}

		public Handler(WebSession session, boolean takeLock) {
			this.session_ = session;
			this.request_ = null;
			this.response_ = null;
			this.killed_ = false;
			this.init();
		}

		public static WebSession.Handler getInstance() {
			return threadHandler_.get();
		}

		public WebResponse getResponse() {
			return this.response_;
		}

		public WebRequest getRequest() {
			return this.request_;
		}

		public WebSession getSession() {
			return this.session_;
		}

		public void killSession() {
			this.killed_ = true;
			this.session_.state_ = WebSession.State.Dead;
		}

		private void init() {
			threadHandler_.set(this);
		}

		static void attachThreadToSession(WebSession session) {
			threadHandler_.set(new WebSession.Handler(session, false));
		}

		private boolean isSessionDead() {
			return this.killed_ || this.session_.isDone();
		}

		private void setRequest(WebRequest request, WebResponse response) {
			this.request_ = request;
			this.response_ = response;
		}

		private WebSession session_;
		private WebRequest request_;
		private WebResponse response_;
		private boolean killed_;
	}

	public void setLoaded() {
		this.setState(WebSession.State.Loaded, this.controller_
				.getConfiguration().getSessionTimeout());
	}

	private void checkTimers() {
		WContainerWidget timers = this.app_.getTimerRoot();
		List<WWidget> timerWidgets = timers.getChildren();
		List<WTimerWidget> expired = new ArrayList<WTimerWidget>();
		for (int i = 0; i < timerWidgets.size(); ++i) {
			WTimerWidget wti = ((timerWidgets.get(i)) instanceof WTimerWidget ? (WTimerWidget) (timerWidgets
					.get(i))
					: null);
			if (wti.isTimerExpired()) {
				expired.add(wti);
			}
		}
		WMouseEvent dummy = new WMouseEvent();
		for (int i = 0; i < expired.size(); ++i) {
			expired.get(i).clicked().trigger(dummy);
		}
	}

	private void hibernate() {
		if (this.app_ != null && this.app_.getLocalizedStrings() != null) {
			this.app_.getLocalizedStrings().hibernate();
		}
	}

	private ReentrantLock mutex_;
	private ApplicationType type_;
	private String favicon_;
	private WebSession.State state_;
	private String sessionId_;
	private WtServlet controller_;
	private WebRenderer renderer_;
	private String applicationName_;
	private String bookmarkUrl_;
	private String baseUrl_;
	private String absoluteBaseUrl_;
	private String applicationUrl_;
	private String deploymentPath_;
	private String redirect_;
	private WebResponse pollResponse_;
	private boolean updatesPending_;
	private boolean progressiveBoot_;
	private WEnvironment embeddedEnv_;
	private WEnvironment env_;
	private WApplication app_;
	private boolean debug_;
	private List<WebSession.Handler> handlers_;
	private List<WObject> emitStack_;
	private WebSession.Handler recursiveEventLoop_;

	private WResource decodeResource(String resourceId) {
		WResource resource = this.app_.decodeExposedResource(resourceId);
		if (resource != null) {
			return resource;
		} else {
			this.log("error").append("decodeResource(): resource '").append(
					resourceId).append("' not exposed");
			return null;
		}
	}

	private AbstractEventSignal decodeSignal(String signalId) {
		AbstractEventSignal result = this.app_.decodeExposedSignal(signalId);
		if (result != null) {
			return result;
		} else {
			this.log("error").append("decodeSignal(): signal '").append(
					signalId).append("' not exposed");
			return null;
		}
	}

	private AbstractEventSignal decodeSignal(String objectId, String name) {
		AbstractEventSignal result = this.app_.decodeExposedSignal(objectId,
				name);
		if (result != null) {
			return result;
		} else {
			this.log("error").append("decodeSignal(): signal '").append(
					objectId).append('.').append(name).append("' not exposed");
			return null;
		}
	}

	private static WObject.FormData getFormData(WebRequest request, String name) {
		UploadedFile file = request.getUploadedFiles().get(name);
		return new WObject.FormData(request.getParameterValues(name),
				file != null ? file : null);
	}

	private void render(WebSession.Handler handler,
			WebRenderer.ResponseType responseType) throws IOException {
		try {
			if (!this.env_.doesAjax_) {
				try {
					this.checkTimers();
				} catch (RuntimeException e) {
					this.log("error").append(
							"Exception while triggering timers").append(
							e.toString());
					throw e;
				}
			}
			if (this.app_.isQuited()) {
				handler.killSession();
			}
			this.serveResponse(handler, responseType);
		} catch (RuntimeException e) {
			handler.getResponse().flush();
			handler.setRequest((WebRequest) null, (WebResponse) null);
			throw e;
		}
		this.updatesPending_ = false;
	}

	private void serveError(WebSession.Handler handler, String e,
			WebRenderer.ResponseType responseType) throws IOException {
		this.renderer_.serveError(handler.getResponse(), e, responseType);
		handler.getResponse().flush();
		handler.setRequest((WebRequest) null, (WebResponse) null);
	}

	private void serveResponse(WebSession.Handler handler,
			WebRenderer.ResponseType responseType) throws IOException {
		this.renderer_.serveResponse(handler.getResponse(), responseType);
		handler.getResponse().flush();
		handler.setRequest((WebRequest) null, (WebResponse) null);
	}

	enum SignalKind {
		LearnedStateless(0), AutoLearnStateless(1), Dynamic(2);

		private int value;

		SignalKind(int value) {
			this.value = value;
		}

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return value;
		}
	}

	private void processSignal(AbstractEventSignal s, String se,
			WebRequest request, WebSession.SignalKind kind) {
		if (!(s != null)) {
			return;
		}
		switch (kind) {
		case LearnedStateless:
			s.processLearnedStateless();
			break;
		case AutoLearnStateless:
			s.processAutoLearnStateless(this.renderer_);
			break;
		case Dynamic:
			JavaScriptEvent jsEvent = new JavaScriptEvent();
			jsEvent.get(request, se);
			s.processDynamic(jsEvent);
		}
	}

	private List<Integer> getSignalProcessingOrder(WEvent e) {
		WebSession.Handler handler = e.handler;
		List<Integer> highPriority = new ArrayList<Integer>();
		List<Integer> normalPriority = new ArrayList<Integer>();
		for (int i = 0;; ++i) {
			WebRequest request = handler.getRequest();
			String se = i > 0 ? 'e' + String.valueOf(i) : "";
			String signalE = this.getSignal(request, se);
			if (!(signalE != null)) {
				break;
			}
			if (!signalE.equals("user") && !signalE.equals("hash")
					&& !signalE.equals("res") && !signalE.equals("none")
					&& !signalE.equals("load")) {
				AbstractEventSignal signal = this.decodeSignal(signalE);
				if (!(signal != null)) {
				} else {
					if (signal.getName() == WFormWidget.CHANGE_SIGNAL) {
						highPriority.add(i);
					} else {
						normalPriority.add(i);
					}
				}
			} else {
				normalPriority.add(i);
			}
		}
		highPriority.addAll(normalPriority);
		return highPriority;
	}

	private void notifySignal(WEvent e) throws IOException {
		WebSession.Handler handler = e.handler;
		this.renderer_.saveChanges();
		List<Integer> order = this.getSignalProcessingOrder(e);
		for (int i = 0; i < order.size(); ++i) {
			if (!(handler.getRequest() != null)) {
				return;
			}
			WebRequest request = handler.getRequest();
			String se = i > 0 ? 'e' + String.valueOf(i) : "";
			String signalE = this.getSignal(request, se);
			if (!(signalE != null)) {
				return;
			}
			this.propagateFormValues(e, se);
			if (signalE.equals("hash")) {
				String hashE = request.getParameter(se + "_");
				if (hashE != null) {
					this.app_.changeInternalPath(hashE);
				}
			} else {
				if (signalE.equals("none") || signalE.equals("load")) {
					this.renderer_.setVisibleOnly(false);
				} else {
					if (!signalE.equals("res")) {
						for (int k = 0; k < 3; ++k) {
							WebSession.SignalKind kind = WebSession.SignalKind
									.values()[k];
							if (kind == WebSession.SignalKind.AutoLearnStateless
									&& 0 != 0) {
								break;
							}
							if (signalE.equals("user")) {
								String idE = request.getParameter(se + "id");
								String nameE = request
										.getParameter(se + "name");
								if (!(idE != null) || !(nameE != null)) {
									break;
								}
								this.processSignal(this
										.decodeSignal(idE, nameE), se, request,
										kind);
							} else {
								this.processSignal(this.decodeSignal(signalE),
										se, request, kind);
							}
							if (kind == WebSession.SignalKind.LearnedStateless
									&& i == 0) {
								this.renderer_.discardChanges();
							}
						}
					}
				}
			}
		}
	}

	private void propagateFormValues(WEvent e, String se) {
		WebRequest request = e.handler.getRequest();
		this.renderer_.updateFormObjectsList(this.app_);
		Map<String, WObject> formObjects = this.renderer_.getFormObjects();
		for (Iterator<Map.Entry<String, WObject>> i_it = formObjects.entrySet()
				.iterator(); i_it.hasNext();) {
			Map.Entry<String, WObject> i = i_it.next();
			String formName = i.getKey();
			WObject obj = i.getValue();
			if (!(0 != 0)) {
				obj.setFormData(getFormData(request, se + formName));
			} else {
				obj.requestTooLarge(0);
			}
		}
	}

	private String getSignal(WebRequest request, String se) {
		String signalE = request.getParameter(se + "signal");
		if (!(signalE != null)) {
			Map<String, List<String>> entries = request.getParameterMap();
			for (Iterator<Map.Entry<String, List<String>>> i_it = entries
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<String, List<String>> i = i_it.next();
				if (i.getKey().indexOf(se + "signal=") == 0) {
					signalE = i.getValue().get(0);
					String v = i.getKey().substring(7 + se.length());
					if (v.length() >= 2) {
						String e = v.substring(v.length() - 2);
						if (e.equals(".x") || e.equals(".y")) {
							v = v.substring(0, 0 + v.length() - 2);
						}
					}
					(signalE) = v;
					break;
				}
			}
		}
		return signalE;
	}

	private void setState(WebSession.State state, int timeout) {
		if (this.state_ != WebSession.State.Dead) {
			this.state_ = state;
		}
	}

	private void init(WebRequest request) {
		this.env_.init(request);
		String hashE = request.getParameter("_");
		this.absoluteBaseUrl_ = this.env_.getUrlScheme() + "://"
				+ this.env_.getHostName() + this.baseUrl_;
		this.bookmarkUrl_ = this.applicationName_;
		if (this.applicationName_.length() == 0) {
			this.bookmarkUrl_ = this.baseUrl_ + this.applicationName_;
		}
		if (this.getType() == ApplicationType.WidgetSet) {
			this.applicationUrl_ = this.env_.getUrlScheme() + "://"
					+ this.env_.getHostName() + this.applicationUrl_;
			this.bookmarkUrl_ = this.absoluteBaseUrl_ + this.bookmarkUrl_;
		}
		String path = request.getPathInfo();
		if (path.length() == 0 && hashE != null) {
			path = hashE;
		}
		this.env_.setInternalPath(path);
	}

	private boolean start() {
		try {
			this.app_ = this.controller_.doCreateApplication(this);
		} catch (RuntimeException e) {
			this.app_ = null;
			this.kill();
			throw e;
		}
		if (this.app_ != null) {
			this.app_.initialize();
		}
		return this.app_ != null;
	}

	private void kill() {
		this.state_ = WebSession.State.Dead;
		if (this.handlers_.isEmpty()) {
			;
		}
	}

	// private void generateNewSessionId() ;
	private String getSessionQuery() {
		return "?wtd=" + this.sessionId_;
	}

	private static ThreadLocal<WebSession.Handler> threadHandler_ = new ThreadLocal<WebSession.Handler>();
}
