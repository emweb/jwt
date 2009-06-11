package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

class WebSession {
	public enum Type {
		Application, WidgetSet;

		public int getValue() {
			return ordinal();
		}
	}

	public WebSession(WtServlet controller, String sessionId,
			WebSession.Type type, String favicon, WebRequest request) {
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
		this.env_ = new WEnvironment(this);
		this.app_ = null;
		this.debug_ = false;
		this.handlers_ = new ArrayList<WebSession.Handler>();
		this.emitStack_ = new ArrayList<WObject>();
		this.deploymentPath_ = request.getScriptName();
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

	public void destroy() {
		if (this.app_ != null) {
			this.app_.finalize();
		}
		/* delete this.app_ */;
		if (this.pollResponse_ != null) {
			this.pollResponse_.flush();
		}
		this.log("notice").append("Session destroyed");
	}

	public static WebSession getInstance() {
		WebSession.Handler handler = WebSession.Handler.getInstance();
		return handler != null ? handler.getSession() : null;
	}

	public WebSession.Type getType() {
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

	public static void notify(WEvent e) throws IOException {
		WebSession session = e.getSession();
		switch (e.type) {
		case EmitSignal:
			session.notifySignal(e);
			break;
		case Refresh:
			session.app_.refresh();
			break;
		case Render:
			session.render(e.handler, e.responseType);
			break;
		case HashChange:
			session.app_.changeInternalPath(e.hash);
			break;
		}
	}

	public boolean handleRequest(WebRequest request, WebResponse response)
			throws IOException {
		this.mutex_.lock();
		try {
			WebSession.Handler handler = new WebSession.Handler(this, request,
					response);
			boolean flushed = false;
			WebRenderer.ResponseType responseType = WebRenderer.ResponseType.FullResponse;
			String wtdE = request.getParameter("wtd");
			String signalE = this.getSignal(request, "");
			String resourceE = request.getParameter("resource");
			if ((!(wtdE != null) || !wtdE.equals(this.sessionId_))
					&& this.state_ != WebSession.State.JustCreated
					&& (signalE != null || resourceE != null)) {
				handler.getResponse().setContentType("text/html");
				handler
						.getResponse()
						.out()
						.append(
								"<html><head></head><body>CSRF prevention</body></html>");
			} else {
				try {
					String requestE = request.getParameter("request");
					switch (this.state_) {
					case JustCreated: {
						switch (this.type_) {
						case Application:
							this.init(request);
							if (signalE != null && !(requestE != null)
									|| requestE != null
									&& requestE.equals("script")) {
								this
										.log("notice")
										.append(
												"Signal from dead session, sending reload.");
								this.renderer_.letReloadJS(handler
										.getResponse(), true);
								handler.killSession();
							} else {
								if (request.getParameter("resource") != null) {
									this
											.log("notice")
											.append(
													"Not serving bootstrap for resource.");
									handler.killSession();
									handler.getResponse().setContentType(
											"text/html");
									handler
											.getResponse()
											.out()
											.append(
													"<html><head></head><body></body></html>");
								} else {
									boolean forcePlain = this.env_
											.agentIsSpiderBot()
											|| !this.env_.agentSupportsAjax();
									if (forcePlain) {
										this.env_.doesJavaScript_ = false;
										this.env_.doesAjax_ = false;
										this.env_.doesCookies_ = false;
										if (!this.isStart()) {
											throw new WtException(
													"Could not start application.");
										}
										if (!this.env_.getInternalPath()
												.equals("/")) {
											this.app_.setInternalPath("/");
											this.app_
													.notify(new WEvent(
															handler,
															WEvent.EventType.HashChange,
															this.env_
																	.getInternalPath()));
										}
										this.app_
												.notify(new WEvent(
														handler,
														WEvent.EventType.Render,
														WebRenderer.ResponseType.FullResponse));
										if (this.env_.agentIsSpiderBot()) {
											handler.killSession();
										}
									} else {
										this.setState(
												WebSession.State.Bootstrap, 10);
										this.renderer_.serveBootstrap(handler
												.getResponse());
									}
								}
							}
							break;
						case WidgetSet:
							this.init(request);
							this.env_.doesJavaScript_ = true;
							this.env_.doesAjax_ = true;
							if (!this.isStart()) {
								throw new WtException(
										"Could not start application.");
							}
							this.app_.notify(new WEvent(handler,
									WEvent.EventType.Render,
									WebRenderer.ResponseType.FullResponse));
						}
						break;
					}
					case Bootstrap: {
						String jsE = request.getParameter("js");
						if (!(jsE != null)) {
							this.setState(WebSession.State.Bootstrap, 10);
							this.renderer_
									.serveBootstrap(handler.getResponse());
							break;
						}
						if (!(this.app_ != null)) {
							String ajaxE = request.getParameter("ajax");
							String hashE = request.getParameter("_");
							String scaleE = request.getParameter("scale");
							this.env_.doesJavaScript_ = jsE.equals("yes");
							this.env_.doesAjax_ = this.env_.doesJavaScript_
									&& ajaxE != null && ajaxE.equals("yes");
							this.env_.doesCookies_ = request.getHeaderValue(
									"Cookie").length() != 0;
							if (this.env_.doesAjax_
									&& request.getPathInfo().length() != 0) {
								String url = this.getBaseUrl()
										+ this.getApplicationName();
								url += '#' + this.env_.getInternalPath();
								this.redirect(url);
								this.renderer_.serveMainWidget(handler
										.getResponse(),
										WebRenderer.ResponseType.FullResponse);
								this.log("notice").append(
										"Redirecting to canonical URL: ")
										.append(url);
								handler.killSession();
								break;
							}
							try {
								this.env_.dpiScale_ = scaleE != null ? Double
										.parseDouble(scaleE) : 1;
							} catch (NumberFormatException e) {
								this.env_.dpiScale_ = 1;
							}
							if (hashE != null) {
								this.env_.setInternalPath(hashE);
							}
							if (!this.isStart()) {
								throw new WtException(
										"Could not start application.");
							}
							if (!this.env_.getInternalPath().equals("/")) {
								this.app_.setInternalPath("/");
								this.app_.notify(new WEvent(handler,
										WEvent.EventType.HashChange, this.env_
												.getInternalPath()));
							}
						} else {
							if (jsE.equals("no") && this.env_.doesAjax_) {
								handler.getResponse().setRedirect(
										this.getBaseUrl()
												+ this.getApplicationName()
												+ '#'
												+ this.env_.getInternalPath());
								handler.killSession();
								break;
							} else {
								if (request.getPathInfo().length() != 0) {
									this.app_.notify(new WEvent(handler,
											WEvent.EventType.HashChange,
											request.getPathInfo()));
								}
							}
						}
						this.app_.notify(new WEvent(handler,
								WEvent.EventType.Render,
								WebRenderer.ResponseType.FullResponse));
						break;
					}
					case Loaded: {
						responseType = signalE != null && !(requestE != null)
								&& this.env_.doesAjax_ ? WebRenderer.ResponseType.UpdateResponse
								: WebRenderer.ResponseType.FullResponse;
						try {
							if (request.getPostDataExceeded() != 0) {
								this.app_.requestTooLarge().trigger(
										request.getPostDataExceeded());
							}
						} catch (Exception e) {
							throw new WtException(
									"Exception in WApplication::requestTooLarge",
									e);
						}
						if (requestE != null && requestE.equals("script")) {
							handler.getResponse().setContentType("text/plain");
							break;
						}
						if (!(resourceE != null) && !(signalE != null)) {
							this.log("notice").append("Refreshing session");
							this.app_.notify(new WEvent(handler,
									WEvent.EventType.Refresh));
							if (this.env_.doesAjax_) {
								this.setState(WebSession.State.Bootstrap, 10);
								this.renderer_.serveBootstrap(handler
										.getResponse());
								break;
							}
						}
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
						this.env_.urlScheme_ = request.getUrlScheme();
						if (resourceE != null && resourceE.equals("blank")) {
							handler.getResponse().setContentType("text/html");
							handler
									.getResponse()
									.out()
									.append(
											"<html><head><title>bhm</title></head><body>&#160;</body></html>");
						} else {
							if (resourceE != null) {
								WResource resource = this
										.decodeResource(resourceE);
								if (resource != null) {
									try {
										resource.handle(request, response);
										flushed = true;
									} catch (Exception e) {
										throw new WtException(
												"Exception while streaming resource",
												e);
									}
								} else {
									handler.getResponse().setContentType(
											"text/html");
									handler
											.getResponse()
											.out()
											.append(
													"<html><body><h1>Session timeout.</h1></body></html>");
								}
							} else {
								if (responseType == WebRenderer.ResponseType.FullResponse
										&& !this.env_.doesAjax_
										&& !(signalE != null)) {
									this.app_.notify(new WEvent(handler,
											WEvent.EventType.HashChange,
											request.getPathInfo()));
								}
								String hashE = request.getParameter("_");
								if (signalE != null) {
									if (this.pollResponse_ != null) {
										this.pollResponse_.flush();
										this.pollResponse_ = null;
									}
									if (!signalE.equals("res")
											&& !signalE.equals("poll")) {
										try {
											this.app_
													.notify(new WEvent(
															handler,
															WEvent.EventType.EmitSignal));
										} catch (Exception e) {
											throw new WtException(
													"Error during event handling",
													e);
										}
									} else {
										if (signalE.equals("poll")
												&& !this.updatesPending_) {
											this.pollResponse_ = handler
													.getResponse();
											handler.swapRequest(
													(WebRequest) null,
													(WebResponse) null);
										}
									}
								} else {
									if (hashE != null) {
										this.app_.notify(new WEvent(handler,
												WEvent.EventType.HashChange,
												hashE));
									}
									try {
										this.app_.notify(new WEvent(handler,
												WEvent.EventType.Refresh));
									} catch (Exception e) {
										throw new WtException(
												"Exception while refreshing session",
												e);
									}
								}
								if (handler.getResponse() != null) {
									this.app_.notify(new WEvent(handler,
											WEvent.EventType.Render,
											responseType));
								}
							}
						}
					}
						break;
					case Dead:
						throw new WtException(
								"Internal error: WebSession is dead?");
					}
				} catch (WtException e) {
					this.log("fatal").append(e.toString());
					e.printStackTrace();
					handler.killSession();
					this.renderer_.serveError(handler.getResponse(), e,
							responseType);
				} catch (Exception e) {
					this.log("fatal").append(e.toString());
					e.printStackTrace();
					handler.killSession();
					this.renderer_.serveError(handler.getResponse(), e,
							responseType);
				}
			}
			if (handler.getResponse() != null && !flushed) {
				handler.getResponse().flush();
			}
			handler.destroy();
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
				this.renderer_.serveMainWidget(this.pollResponse_,
						WebRenderer.ResponseType.UpdateResponse);
				this.pollResponse_.flush();
				this.pollResponse_ = null;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void doRecursiveEventLoop(String javascript) {
		this.log("error").append(
				"Cannot do recursive event loop without threads");
	}

	public void unlockRecursiveEventLoop() {
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
		String result = WebSession.Handler.getInstance().getResponse()
				.encodeURL(url);
		if (this.env_.agentIsSpiderBot()) {
			return result;
		}
		int questionPos = result.indexOf('?');
		if (questionPos == -1) {
			return result + this.getSessionQuery();
		} else {
			if (questionPos == result.length() - 1) {
				return result + this.getSessionQuery().substring(1);
			} else {
				return result + '&' + this.getSessionQuery().substring(1);
			}
		}
	}

	public enum BootstrapOption {
		ClearInternalPath, KeepInternalPath;

		public int getValue() {
			return ordinal();
		}
	}

	public String getBootstrapUrl(WebResponse response,
			WebSession.BootstrapOption option) {
		if (response.getPathInfo().length() == 0) {
			return this.getMostRelativeUrl();
		} else {
			switch (option) {
			case KeepInternalPath:
				if (this.applicationName_.length() == 0) {
					String internalPath = this.app_ != null ? this.app_
							.getInternalPath() : this.env_.getInternalPath();
					if (internalPath.length() > 1) {
						return this.appendSessionQuery("?_="
								+ DomElement.urlEncodeS(internalPath));
					}
				}
				return this.appendSessionQuery("");
			case ClearInternalPath:
				return this.appendSessionQuery(this.baseUrl_
						+ this.applicationName_);
			default:
				assert false;
			}
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
			return request.getEnvValue(varName);
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

	public static class Handler {
		public Handler(WebSession session, WebRequest request,
				WebResponse response) {
			this.session_ = session;
			this.request_ = request;
			this.response_ = response;
			this.eventLoop_ = false;
			this.killed_ = false;
			this.init();
		}

		public Handler(WebSession session, boolean locked) {
			this.session_ = session;
			this.request_ = null;
			this.response_ = null;
			this.eventLoop_ = false;
			this.killed_ = false;
		}

		public Handler(WebSession session) {
			this(session, true);
		}

		public void destroy() {
			threadHandler_.set((WebSession.Handler) null);
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

		private void setEventLoop(boolean how) {
			this.eventLoop_ = how;
		}

		private static void attachThreadToSession(WebSession session) {
			session
					.log("error")
					.append(
							"attachThreadToSession() requires that Wt is built with threading enabled");
		}

		private boolean isSessionDead() {
			return this.killed_ || this.session_.isDone();
		}

		private boolean isEventLoop() {
			return this.eventLoop_;
		}

		private void swapRequest(WebRequest request, WebResponse response) {
			this.request_ = request;
			this.response_ = response;
		}

		private WebSession session_;
		private WebRequest request_;
		private WebResponse response_;
		private boolean eventLoop_;
		private boolean killed_;
	}

	private void setDebug(boolean debug) {
		this.debug_ = debug;
	}

	private boolean isDebug() {
		return this.debug_;
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

	private enum State {
		JustCreated, Bootstrap, Loaded, Dead;

		public int getValue() {
			return ordinal();
		}
	}

	private ReentrantLock mutex_;
	private WebSession.Type type_;
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
	private WEnvironment env_;
	private WApplication app_;
	private boolean debug_;
	private List<WebSession.Handler> handlers_;
	private List<WObject> emitStack_;

	private WebSession.Handler findEventloopHandler(int index) {
		return null;
	}

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
			WebRenderer.ResponseType type) throws IOException {
		try {
			if (!this.env_.doesJavaScript_
					&& type == WebRenderer.ResponseType.FullResponse) {
				this.checkTimers();
			}
		} catch (Exception e) {
			throw new WtException("Exception while triggering timers", e);
		}
		if (this.app_.isQuited()) {
			handler.killSession();
		}
		this.renderer_.serveMainWidget(handler.getResponse(), type);
		this.setState(WebSession.State.Loaded, this.controller_
				.getConfiguration().getSessionTimeout());
		this.updatesPending_ = false;
	}

	private enum SignalKind {
		LearnedStateless(0), AutoLearnStateless(1), Dynamic(2);

		private int value;

		SignalKind(int value) {
			this.value = value;
		}

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

	private void notifySignal(WEvent e) throws IOException {
		WebSession.Handler handler = e.handler;
		this.renderer_.saveChanges();
		for (int i = 0;; ++i) {
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
						handler.setEventLoop(true);
						for (int k = 0; k < 3; ++k) {
							WebSession.SignalKind kind = WebSession.SignalKind
									.values()[k];
							if (kind == WebSession.SignalKind.AutoLearnStateless
									&& request.getPostDataExceeded() != 0) {
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
		List<WObject> formObjects = this.renderer_.getFormObjects();
		for (int i = 0; i < formObjects.size(); ++i) {
			WObject obj = formObjects.get(i);
			if (!(request.getPostDataExceeded() != 0)) {
				obj.setFormData(getFormData(request, se + obj.getFormName()));
			} else {
				obj.requestTooLarge(request.getPostDataExceeded());
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
		if (this.getType() == WebSession.Type.WidgetSet) {
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

	private boolean isStart() throws Exception {
		try {
			this.app_ = this.controller_.doCreateApplication(this);
		} catch (Exception e) {
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
			/* delete this */;
		}
	}

	// private void generateNewSessionId() ;
	private String getSessionQuery() {
		return "?wtd=" + this.sessionId_;
	}

	private static ThreadLocal<WebSession.Handler> threadHandler_ = new ThreadLocal<WebSession.Handler>();
}
