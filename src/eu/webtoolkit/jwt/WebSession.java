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

class WebSession {
	private static Logger logger = LoggerFactory.getLogger(WebSession.class);

	enum State {
		JustCreated, ExpectLoad, Loaded, Dead;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	public WebSession(WtServlet controller, final String sessionId,
			EntryPointType type, final String favicon, WebRequest request,
			WEnvironment env) {
		this.mutex_ = new ReentrantLock();
		this.type_ = type;
		this.favicon_ = favicon;
		this.state_ = WebSession.State.JustCreated;
		this.useUrlRewriting_ = true;
		this.sessionId_ = sessionId;
		this.sessionIdCookie_ = "";
		this.sessionIdChanged_ = false;
		this.sessionIdCookieChanged_ = false;
		this.sessionIdInUrl_ = false;
		this.controller_ = controller;
		this.renderer_ = new WebRenderer(this);
		this.applicationName_ = "";
		this.bookmarkUrl_ = "";
		this.basePath_ = "";
		this.absoluteBaseUrl_ = "";
		this.applicationUrl_ = "";
		this.deploymentPath_ = "";
		this.redirect_ = "";
		this.pagePathInfo_ = "";
		this.pongMessage_ = "";
		this.asyncResponse_ = null;
		this.bootStyleResponse_ = null;
		this.canWriteAsyncResponse_ = false;
		this.pollRequestsIgnored_ = 0;
		this.progressiveBoot_ = false;
		this.bootStyle_ = true;
		this.deferredRequest_ = null;
		this.deferredResponse_ = null;
		this.deferCount_ = 0;
		this.recursiveEvent_ = this.mutex_.newCondition();
		this.newRecursiveEvent_ = false;
		this.updatesPendingEvent_ = this.mutex_.newCondition();
		this.updatesPending_ = false;
		this.triggerUpdate_ = false;
		this.embeddedEnv_ = new WEnvironment(this);
		this.app_ = null;
		this.debug_ = this.controller_.getConfiguration().debug();
		this.handlers_ = new ArrayList<WebSession.Handler>();
		this.emitStack_ = new ArrayList<WObject>();
		this.recursiveEventLoop_ = null;
		this.env_ = env != null ? env : this.embeddedEnv_;
		if (request != null) {
			this.applicationUrl_ = request.getScriptName();
		} else {
			this.applicationUrl_ = "/";
		}
		this.deploymentPath_ = this.applicationUrl_;
		int slashpos = this.deploymentPath_.lastIndexOf('/');
		if (slashpos != -1) {
			this.basePath_ = this.deploymentPath_
					.substring(0, 0 + slashpos + 1);
			this.applicationName_ = this.deploymentPath_
					.substring(slashpos + 1);
		} else {
			this.basePath_ = "";
			this.applicationName_ = this.applicationUrl_;
		}
		if (this.controller_.getConfiguration().sessionIdCookie()) {
			this.sessionIdCookie_ = MathUtils.randomId();
			this.sessionIdCookieChanged_ = true;
			this.getRenderer().setCookie("Wt" + this.sessionIdCookie_, "1",
					null, "", "", false);
		}
	}

	public WebSession(WtServlet controller, final String sessionId,
			EntryPointType type, final String favicon, WebRequest request) {
		this(controller, sessionId, type, favicon, request, (WEnvironment) null);
	}

	public void destruct() {
		if (this.asyncResponse_ != null) {
			this.asyncResponse_.flush();
			this.asyncResponse_ = null;
		}
		if (this.deferredResponse_ != null) {
			this.deferredResponse_.flush();
			this.deferredResponse_ = null;
		}
		this.mutex_.lock();
		this.updatesPendingEvent_.signal();
		this.mutex_.unlock();
		this.flushBootStyleResponse();
	}

	public static WebSession getInstance() {
		WebSession.Handler handler = WebSession.Handler.getInstance();
		return handler != null ? handler.getSession() : null;
	}

	public boolean isAttachThreadToLockedHandler() {
		Handler.attachThreadToHandler(new WebSession.Handler(this, false));
		return true;
	}

	public EntryPointType getType() {
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
			return "<!DOCTYPE html>";
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

	public boolean isUseUrlRewriting() {
		return this.useUrlRewriting_;
	}

	public boolean isDebug() {
		return this.debug_;
	}

	public void redirect(final String url) {
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

	public void notify(final WEvent event) throws IOException {
		final WebSession.Handler handler = event.impl_.handler;
		final WebRequest request = handler.getRequest();
		final WebResponse response = handler.getResponse();
		if (WebSession.Handler.getInstance() != handler) {
			WebSession.Handler.getInstance().setRequest(request, response);
		}
		if (event.impl_.renderOnly) {
			this.render(handler);
			return;
		}
		String requestE = request.getParameter("request");
		String pageIdE = request.getParameter("pageId");
		if (pageIdE != null
				&& !pageIdE.equals(String.valueOf(this.renderer_.getPageId()))) {
			handler.getResponse().setContentType(
					"text/javascript; charset=UTF-8");
			handler.getResponse().out().append("{}");
			handler.flushResponse();
			return;
		}
		switch (this.state_) {
		case JustCreated:
			this.render(handler);
			break;
		case ExpectLoad:
		case Loaded:
			if ((!(requestE != null) || !requestE.equals("resource"))
					&& handler.getResponse().getResponseType() == WebRequest.ResponseType.Page) {
				if (!this.env_.agentIsIE()) {
					if (!str(handler.getRequest().getHeaderValue("User-Agent"))
							.equals(this.env_.getUserAgent())) {
						logger.warn(new StringWriter().append("secure:")
								.append("change of user-agent not allowed.")
								.toString());
						logger.info(new StringWriter().append(
								"old user agent: ").append(
								this.env_.getUserAgent()).toString());
						logger.info(new StringWriter().append(
								"new user agent: ").append(
								str(handler.getRequest().getHeaderValue(
										"User-Agent"))).toString());
						this.serveError(403, handler, "Forbidden");
						return;
					}
				}
				String ca = WEnvironment.getClientAddress(handler.getRequest(),
						this.controller_.getConfiguration());
				if (!ca.equals(this.env_.getClientAddress())) {
					boolean isInvalid = this.sessionIdCookie_.length() == 0;
					if (!isInvalid) {
						String cookie = str(request.getHeaderValue("Cookie"));
						if (cookie.indexOf("Wt" + this.sessionIdCookie_) == -1) {
							isInvalid = true;
						}
					}
					if (isInvalid) {
						logger.warn(new StringWriter().append("secure:")
								.append("change of IP address (").append(
										this.env_.getClientAddress()).append(
										" -> ").append(ca).append(
										") not allowed.").toString());
						this.serveError(403, handler, "Forbidden");
						return;
					}
				}
			}
			if (this.sessionIdCookieChanged_) {
				String cookie = str(request.getHeaderValue("Cookie"));
				if (cookie.indexOf("Wt" + this.sessionIdCookie_) == -1) {
					this.sessionIdCookie_ = "";
					logger.info(new StringWriter().append(
							"session id cookie not working").toString());
				}
				this.sessionIdCookieChanged_ = false;
			}
			if (handler.getResponse().getResponseType() == WebRequest.ResponseType.Script) {
				String sidE = request.getParameter("sid");
				if (!(sidE != null)
						|| !sidE.equals(String.valueOf(this.renderer_
								.getScriptId()))) {
					throw new WException("Script id mismatch");
				}
				if (!(request.getParameter("skeleton") != null)) {
					if (!this.env_.hasAjax()) {
						this.env_.enableAjax(request);
						this.app_.enableAjax();
						if (this.env_.getInternalPath().length() > 1) {
							this.changeInternalPath(
									this.env_.getInternalPath(), handler
											.getResponse());
						}
					} else {
						String hashE = request.getParameter("_");
						if (hashE != null) {
							this.changeInternalPath(hashE, handler
									.getResponse());
						}
					}
				}
				this.render(handler);
			} else {
				try {
					if (0L != 0) {
						this.app_.requestTooLarge().trigger(0L);
					}
				} catch (final RuntimeException e) {
					logger.error(new StringWriter().append(
							"Exception in WApplication::requestTooLarge")
							.append(e.toString()).toString());
					throw e;
				}
				String hashE = request.getParameter("_");
				WResource resource = null;
				if (!(requestE != null)) {
					if (request.getPathInfo().length() != 0) {
						resource = this.app_.decodeExposedResource("/path/"
								+ StringUtils.prepend(request.getPathInfo(),
										'/'));
					}
					if (!(resource != null) && hashE != null) {
						resource = this.app_.decodeExposedResource("/path/"
								+ hashE);
					}
				}
				String resourceE = request.getParameter("resource");
				String signalE = this.getSignal(request, "");
				if (signalE != null) {
					this.progressiveBoot_ = false;
				}
				if (resource != null || requestE != null
						&& requestE.equals("resource") && resourceE != null) {
					if (resourceE != null && resourceE.equals("blank")) {
						handler.getResponse().setContentType("text/html");
						handler
								.getResponse()
								.out()
								.append(
										"<html><head><title>bhm</title></head><body> </body></html>");
						handler.flushResponse();
					} else {
						if (!(resource != null)) {
							resource = this.app_
									.decodeExposedResource(resourceE);
						}
						if (resource != null) {
							try {
								resource.handle(request, response);
								handler.setRequest((WebRequest) null,
										(WebResponse) null);
							} catch (final RuntimeException e) {
								logger.error(new StringWriter().append(
										"Exception while streaming resource")
										.append(e.toString()).toString());
								throw e;
							}
						} else {
							logger.error(new StringWriter().append(
									"decodeResource(): resource '").append(
									resourceE).append("' not exposed")
									.toString());
							handler.getResponse().setContentType("text/html");
							handler
									.getResponse()
									.out()
									.append(
											"<html><body><h1>Nothing to say about that.</h1></body></html>");
							handler.flushResponse();
						}
					}
				} else {
					this.env_.urlScheme_ = str(request.getScheme());
					if (signalE != null) {
						String ackIdE = request.getParameter("ackId");
						boolean invalidAckId = this.env_.hasAjax()
								&& !request.isWebSocketMessage();
						if (invalidAckId && ackIdE != null) {
							try {
								if (this.renderer_.ackUpdate(Integer
										.parseInt(ackIdE))) {
									invalidAckId = false;
								}
							} catch (final NumberFormatException e) {
							}
						}
						if (invalidAckId) {
							if (!(ackIdE != null)) {
								logger.warn(new StringWriter()
										.append("secure:").append(
												"missing ackId").toString());
							} else {
								logger.warn(new StringWriter()
										.append("secure:").append(
												"invalid ackId").toString());
							}
							this.serveError(403, handler, "Forbidden");
							return;
						}
						if (this.asyncResponse_ != null
								&& !this.asyncResponse_.isWebSocketRequest()) {
							this.asyncResponse_.flush();
							this.asyncResponse_ = null;
							this.canWriteAsyncResponse_ = false;
						}
						if (signalE.equals("poll")) {
							if (!WtServlet.isAsyncSupported()) {
								this.updatesPendingEvent_.signal();
								if (!this.updatesPending_) {
									try {
										this.updatesPendingEvent_
												.await(
														this.controller_
																.getConfiguration()
																.getServerPushTimeout() * 2,
														java.util.concurrent.TimeUnit.SECONDS);
									} catch (final InterruptedException e) {
									}
								}
								if (!this.updatesPending_) {
									handler.flushResponse();
									return;
								}
							}
							if (!this.updatesPending_) {
								if (!(this.asyncResponse_ != null)
										|| this.pollRequestsIgnored_ == 2) {
									if (this.asyncResponse_ != null) {
										logger
												.info(new StringWriter()
														.append(
																"discarding broken asyncResponse, (ws: ")
														.append(
																String
																		.valueOf(this.asyncResponse_
																				.isWebSocketRequest()))
														.toString());
										this.asyncResponse_.flush();
										this.asyncResponse_ = null;
									}
									this.pollRequestsIgnored_ = 0;
									this.asyncResponse_ = handler.getResponse();
									this.canWriteAsyncResponse_ = true;
									handler.setRequest((WebRequest) null,
											(WebResponse) null);
								} else {
									++this.pollRequestsIgnored_;
									logger
											.debug(new StringWriter()
													.append(
															"ignored poll request (#")
													.append(
															String
																	.valueOf(this.pollRequestsIgnored_))
													.append(")").toString());
								}
							} else {
								this.pollRequestsIgnored_ = 0;
							}
						} else {
							if (!WtServlet.isAsyncSupported()) {
								this.updatesPending_ = false;
								this.updatesPendingEvent_.signal();
							}
						}
						if (handler.getRequest() != null) {
							logger.debug(new StringWriter().append("signal: ")
									.append(signalE).toString());
							try {
								handler.nextSignal = -1;
								this.notifySignal(event);
							} catch (final RuntimeException e) {
								logger.error(new StringWriter().append(
										"error during event handling: ")
										.append(e.toString()).toString());
								throw e;
							}
						}
					}
					if (handler.getResponse() != null
							&& handler.getResponse().getResponseType() == WebRequest.ResponseType.Page
							&& (!this.env_.hasAjax() || !this.controller_
									.getConfiguration().reloadIsNewSession())) {
						this.app_.getDomRoot().setRendered(false);
						this.env_.parameters_ = handler.getRequest()
								.getParameterMap();
						if (hashE != null) {
							this.changeInternalPath(hashE, handler
									.getResponse());
						} else {
							if (handler.getRequest().getPathInfo().length() != 0) {
								this.changeInternalPath(handler.getRequest()
										.getPathInfo(), handler.getResponse());
							} else {
								this.changeInternalPath("", handler
										.getResponse());
							}
						}
					}
					if (!(signalE != null)) {
						if (this.getType() == EntryPointType.WidgetSet) {
							logger
									.error(new StringWriter()
											.append(
													"bogus request: missing signal, discarding")
											.toString());
							handler.flushResponse();
							return;
						}
						logger.info(new StringWriter().append(
								"refreshing session").toString());
						this.flushBootStyleResponse();
						if (handler.getRequest() != null) {
							this.env_.parameters_ = handler.getRequest()
									.getParameterMap();
						}
						this.app_.refresh();
					}
					if (handler.getResponse() != null
							&& !(this.recursiveEventLoop_ != null)) {
						this.render(handler);
					}
				}
			}
		case Dead:
			break;
		}
	}

	public void pushUpdates() {
		try {
			this.triggerUpdate_ = false;
			if (!(this.app_ != null) || !this.renderer_.isDirty()) {
				logger.debug(new StringWriter().append(
						"pushUpdates(): nothing to do").toString());
				return;
			}
			this.updatesPending_ = true;
			if (this.canWriteAsyncResponse_) {
				if (this.asyncResponse_.isWebSocketRequest()
						&& this.asyncResponse_.isWebSocketMessagePending()) {
					logger.debug(new StringWriter().append(
							"pushUpdates(): web socket message pending")
							.toString());
					return;
				}
				if (this.asyncResponse_.isWebSocketRequest()) {
				} else {
					this.asyncResponse_
							.setResponseType(WebRequest.ResponseType.Update);
					this.renderer_.serveResponse(this.asyncResponse_);
				}
				this.updatesPending_ = false;
				if (!this.asyncResponse_.isWebSocketRequest()) {
					this.asyncResponse_.flush();
					this.asyncResponse_ = null;
					this.canWriteAsyncResponse_ = false;
				}
			} else {
				this.updatesPendingEvent_.signal();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void doRecursiveEventLoop() {
		try {
			WebSession.Handler handler = WebSession.Handler.getInstance();
			if (handler.getRequest() != null && !WtServlet.isAsyncSupported()) {
				throw new WException(
						"Recursive eventloop requires a Servlet 3.0 enabled servlet container and an application with async-supported enabled.");
			}
			if (handler.getRequest() != null) {
				handler.getSession().notifySignal(
						new WEvent(new WEvent.Impl(handler)));
			} else {
				if (this.app_.isUpdatesEnabled()) {
					this.app_.triggerUpdate();
				}
			}
			if (handler.getResponse() != null) {
				handler.getSession().render(handler);
			}
			if (this.state_ == WebSession.State.Dead) {
				this.recursiveEventLoop_ = null;
				throw new WException(
						"doRecursiveEventLoop(): session was killed");
			}
			WebSession.Handler prevRecursiveEventLoop = this.recursiveEventLoop_;
			this.recursiveEventLoop_ = handler;
			this.newRecursiveEvent_ = false;
			while (!this.newRecursiveEvent_) {
				this.recursiveEvent_.awaitUninterruptibly();
			}
			if (this.state_ == WebSession.State.Dead) {
				this.recursiveEventLoop_ = null;
				throw new WException(
						"doRecursiveEventLoop(): session was killed");
			}
			this.setLoaded();
			this.app_.notify(new WEvent(new WEvent.Impl(handler)));
			this.recursiveEventLoop_ = prevRecursiveEventLoop;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void deferRendering() {
		if (!(this.deferredRequest_ != null)) {
			WebSession.Handler handler = WebSession.Handler.getInstance();
			this.deferredRequest_ = handler.getRequest();
			this.deferredResponse_ = handler.getResponse();
			handler.setRequest((WebRequest) null, (WebResponse) null);
		}
		++this.deferCount_;
	}

	public void resumeRendering() {
		if (--this.deferCount_ == 0) {
			WebSession.Handler handler = WebSession.Handler.getInstance();
			handler.setRequest(this.deferredRequest_, this.deferredResponse_);
			this.deferredRequest_ = null;
			this.deferredResponse_ = null;
		}
	}

	public void setTriggerUpdate(boolean update) {
		this.triggerUpdate_ = update;
	}

	public void expire() {
		this.kill();
	}

	public boolean isUnlockRecursiveEventLoop() {
		if (!(this.recursiveEventLoop_ != null)) {
			return false;
		}
		WebSession.Handler handler = WebSession.Handler.getInstance();
		this.recursiveEventLoop_.setRequest(handler.getRequest(), handler
				.getResponse());
		handler.setRequest((WebRequest) null, (WebResponse) null);
		this.newRecursiveEvent_ = true;
		this.recursiveEvent_.signal();
		return true;
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

	public boolean isDead() {
		return this.state_ == WebSession.State.Dead;
	}

	public WebSession.State getState() {
		return this.state_;
	}

	public void kill() {
		this.state_ = WebSession.State.Dead;
		this.isUnlockRecursiveEventLoop();
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

	public boolean hasSessionIdInUrl() {
		return this.sessionIdInUrl_;
	}

	public void setSessionIdInUrl(boolean value) {
		this.sessionIdInUrl_ = value;
	}

	public boolean isUseUglyInternalPaths() {
		return false;
	}

	public void setPagePathInfo(final String path) {
		if (!this.isUseUglyInternalPaths()) {
			this.pagePathInfo_ = path;
		}
	}

	public String getPagePathInfo() {
		return this.pagePathInfo_;
	}

	public String getMostRelativeUrl(final String internalPath) {
		return this.appendSessionQuery(this.getBookmarkUrl(internalPath));
	}

	public final String getMostRelativeUrl() {
		return getMostRelativeUrl("");
	}

	public String appendInternalPath(final String baseUrl,
			final String internalPath) {
		if (internalPath.length() == 0 || internalPath.equals("/")) {
			if (baseUrl.length() == 0) {
				return "?";
			} else {
				return baseUrl;
			}
		} else {
			if (this.isUseUglyInternalPaths()) {
				return baseUrl + "?_="
						+ DomElement.urlEncodeS(internalPath, "#");
			} else {
				if (this.applicationName_.length() == 0) {
					return baseUrl
							+ DomElement.urlEncodeS(internalPath.substring(1),
									"#");
				} else {
					return baseUrl + DomElement.urlEncodeS(internalPath, "#");
				}
			}
		}
	}

	public String appendSessionQuery(final String url) {
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
		if (WebSession.Handler.getInstance().getResponse() != null) {
			return WebSession.Handler.getInstance().getResponse().encodeURL(
					result);
		}
		return url;
	}

	public String ajaxCanonicalUrl(final WebResponse request) {
		String hashE = null;
		if (this.applicationName_.length() == 0) {
			hashE = request.getParameter("_");
		}
		if (this.pagePathInfo_.length() != 0 || hashE != null
				&& hashE.length() > 1) {
			String url = "";
			if (this.applicationName_.length() == 0) {
				url = this.fixRelativeUrl("?");
				url = url.substring(0, 0 + url.length() - 1);
			} else {
				url = this.fixRelativeUrl(this.applicationName_);
			}
			boolean firstParameter = true;
			for (Iterator<Map.Entry<String, String[]>> i_it = request
					.getParameterMap().entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<String, String[]> i = i_it.next();
				if (!i.getKey().equals("_")) {
					url += (firstParameter ? '?' : '&')
							+ Utils.urlEncode(i.getKey()) + '='
							+ Utils.urlEncode(i.getValue()[0]);
					firstParameter = false;
				}
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

	public String getBootstrapUrl(final WebResponse response,
			WebSession.BootstrapOption option) {
		switch (option) {
		case KeepInternalPath: {
			String url = "";
			String internalPath = this.app_ != null ? this.app_
					.getInternalPath() : this.env_.getInternalPath();
			if (this.isUseUglyInternalPaths()) {
				if (internalPath.length() > 1) {
					url = "?_=" + DomElement.urlEncodeS(internalPath, "#");
				}
				if (isAbsoluteUrl(this.applicationUrl_)) {
					url = this.applicationUrl_ + url;
				}
			} else {
				if (!isAbsoluteUrl(this.applicationUrl_)) {
					if (internalPath.length() > 1) {
						String lastPart = internalPath.substring(internalPath
								.lastIndexOf('/') + 1);
						url = "";
					} else {
						url = this.applicationName_;
					}
				} else {
					if (this.applicationName_.length() == 0
							&& internalPath.length() > 1) {
						internalPath = internalPath.substring(1);
					}
					url = this.applicationUrl_ + internalPath;
				}
			}
			return this.appendSessionQuery(url);
		}
		case ClearInternalPath: {
			String url = "";
			if (this.applicationName_.length() == 0) {
				url = this.fixRelativeUrl(".");
				url = url.substring(0, 0 + url.length() - 1);
			} else {
				url = this.fixRelativeUrl(this.applicationName_);
			}
			return this.appendSessionQuery(url);
		}
		default:
			assert false;
		}
		return "";
	}

	public String fixRelativeUrl(final String url) {
		if (isAbsoluteUrl(url)) {
			return url;
		}
		if (url.length() > 0 && url.charAt(0) == '#') {
			if (!isAbsoluteUrl(this.applicationUrl_)) {
				return url;
			} else {
				return this.applicationName_ + url;
			}
		}
		if (!isAbsoluteUrl(this.applicationUrl_)) {
			if (url.length() == 0 || url.charAt(0) == '/') {
				return url;
			} else {
				if (this.env_.publicDeploymentPath_.length() != 0) {
					String dp = this.env_.publicDeploymentPath_;
					if (url.charAt(0) != '?') {
						int s = dp.lastIndexOf('/');
						dp = dp.substring(0, 0 + s + 1);
					}
					return dp + url;
				} else {
					if (this.env_.hashInternalPaths()) {
						return url;
					} else {
						String rel = "";
						String pi = this.pagePathInfo_;
						for (int i = 0; i < pi.length(); ++i) {
							if (pi.charAt(i) == '/') {
								rel += "../";
							}
						}
						return rel + url;
					}
				}
			}
		} else {
			return this.makeAbsoluteUrl(url);
		}
	}

	public String makeAbsoluteUrl(final String url) {
		if (isAbsoluteUrl(url)) {
			return url;
		} else {
			if (url.length() == 0 || url.charAt(0) != '/') {
				return this.absoluteBaseUrl_ + url;
			} else {
				return host(this.absoluteBaseUrl_) + url;
			}
		}
	}

	public String getBookmarkUrl(final String internalPath) {
		String result = this.bookmarkUrl_;
		return this.appendInternalPath(result, internalPath);
	}

	public String getBookmarkUrl() {
		if (this.app_ != null) {
			return this.getBookmarkUrl(this.app_.getInternalPath());
		} else {
			return this.getBookmarkUrl(this.env_.getInternalPath());
		}
	}

	public String getCgiValue(final String varName) {
		WebRequest request = WebSession.Handler.getInstance().getRequest();
		if (request != null) {
			return str("");
		} else {
			return "";
		}
	}

	public String getCgiHeader(final String headerName) {
		WebRequest request = WebSession.Handler.getInstance().getRequest();
		if (request != null) {
			return str(request.getHeaderValue(headerName));
		} else {
			return "";
		}
	}

	public EventType getEventType(final WEvent event) {
		if (event.impl_.handler == null) {
			return EventType.OtherEvent;
		}
		final WebSession.Handler handler = event.impl_.handler;
		final WebRequest request = handler.getRequest();
		if (event.impl_.renderOnly || !(handler.getRequest() != null)) {
			return EventType.OtherEvent;
		}
		String requestE = request.getParameter("request");
		String pageIdE = handler.getRequest().getParameter("pageId");
		if (pageIdE != null
				&& !pageIdE.equals(String.valueOf(this.renderer_.getPageId()))) {
			return EventType.OtherEvent;
		}
		switch (this.state_) {
		case ExpectLoad:
		case Loaded:
			if (handler.getResponse().getResponseType() == WebRequest.ResponseType.Script) {
				return EventType.OtherEvent;
			} else {
				WResource resource = null;
				if (!(requestE != null) && request.getPathInfo().length() != 0) {
					resource = this.app_.decodeExposedResource("/path/"
							+ request.getPathInfo());
				}
				String resourceE = request.getParameter("resource");
				String signalE = this.getSignal(request, "");
				if (resource != null || requestE != null
						&& requestE.equals("resource") && resourceE != null) {
					return EventType.ResourceEvent;
				} else {
					if (signalE != null) {
						if (signalE.equals("none") || signalE.equals("load")
								|| signalE.equals("hash")
								|| signalE.equals("poll")) {
							return EventType.OtherEvent;
						} else {
							List<Integer> signalOrder = this
									.getSignalProcessingOrder(event);
							int timerSignals = 0;
							for (int i = 0; i < signalOrder.size(); ++i) {
								int signalI = signalOrder.get(i);
								String se = signalI > 0 ? 'e' + String
										.valueOf(signalI) : "";
								String s = this.getSignal(request, se);
								if (!(s != null)) {
									break;
								} else {
									if (signalE.equals("user")) {
										return EventType.UserEvent;
									} else {
										AbstractEventSignal esb = this
												.decodeSignal(s, false);
										if (!(esb != null)) {
											continue;
										}
										WTimerWidget t = ((esb.getSender()) instanceof WTimerWidget ? (WTimerWidget) (esb
												.getSender())
												: null);
										if (t != null) {
											++timerSignals;
										} else {
											return EventType.UserEvent;
										}
									}
								}
							}
							if (timerSignals != 0) {
								return EventType.TimerEvent;
							}
						}
					} else {
						return EventType.OtherEvent;
					}
				}
			}
		default:
			return EventType.OtherEvent;
		}
	}

	public void setState(WebSession.State state, int timeout) {
		if (this.state_ != WebSession.State.Dead) {
			this.state_ = state;
			logger.debug(new StringWriter().append("Setting to expire in ")
					.append(String.valueOf(timeout)).append("s").toString());
		}
	}

	static class Handler {
		private static Logger logger = LoggerFactory.getLogger(Handler.class);

		public Handler() {
			this.nextSignal = -1;
			this.signalOrder = new ArrayList<Integer>();
			this.prevHandler_ = null;
			this.session_ = null;
			this.request_ = null;
			this.response_ = null;
			this.killed_ = false;
			this.init();
		}

		public Handler(WebSession session, final WebRequest request,
				final WebResponse response) {
			this.nextSignal = -1;
			this.signalOrder = new ArrayList<Integer>();
			this.prevHandler_ = null;
			this.session_ = session;
			this.request_ = request;
			this.response_ = response;
			this.killed_ = false;
			session.getMutex().lock();
			this.init();
		}

		public Handler(WebSession session, boolean takeLock) {
			this.nextSignal = -1;
			this.signalOrder = new ArrayList<Integer>();
			this.prevHandler_ = null;
			this.session_ = session;
			this.request_ = null;
			this.response_ = null;
			this.killed_ = false;
			if (takeLock) {
				session.getMutex().lock();
			}
			this.init();
		}

		public Handler(WebSession session) {
			this.nextSignal = -1;
			this.signalOrder = new ArrayList<Integer>();
			this.prevHandler_ = null;
			this.session_ = session;
			this.request_ = null;
			this.response_ = null;
			this.killed_ = false;
			session.getMutex().lock();
			this.init();
		}

		public void release() {
			if (this.isHaveLock()) {
				if (this.session_.triggerUpdate_) {
					this.session_.pushUpdates();
				}
				this.session_.getMutex().unlock();
			}
			attachThreadToHandler(this.prevHandler_);
		}

		public static WebSession.Handler getInstance() {
			return threadHandler_.get();
		}

		public boolean isHaveLock() {
			return this.session_.getMutex().isHeldByCurrentThread();
		}

		public void flushResponse() {
			if (this.response_ != null) {
				this.response_.flush();
				this.setRequest((WebRequest) null, (WebResponse) null);
			}
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

		public void setRequest(WebRequest request, WebResponse response) {
			this.request_ = request;
			this.response_ = response;
		}

		public int nextSignal;
		public List<Integer> signalOrder;

		static void attachThreadToSession(WebSession session) {
			attachThreadToHandler((WebSession.Handler) null);
			if (!(session != null)) {
				return;
			}
			if (session.state_ == WebSession.State.Dead) {
				logger.warn(new StringWriter().append(
						"attaching to dead session?").toString());
			}
			if (!session.isAttachThreadToLockedHandler()) {
				logger
						.warn(new StringWriter()
								.append(
										"attachThread(): no thread is holding this application's lock ?")
								.toString());
				WebSession.Handler
						.attachThreadToHandler(new WebSession.Handler(session,
								false));
			}
		}

		public static WebSession.Handler attachThreadToHandler(
				WebSession.Handler handler) {
			WebSession.Handler result;
			result = threadHandler_.get();
			threadHandler_.set(handler);
			return result;
		}

		private void init() {
			this.prevHandler_ = attachThreadToHandler(this);
		}

		private WebSession.Handler prevHandler_;
		private WebSession session_;
		private WebRequest request_;
		private WebResponse response_;
		private boolean killed_;
	}

	public void handleRequest(final WebSession.Handler handler)
			throws IOException {
		try {
			final WebRequest request = handler.getRequest();
			String wtdE = request.getParameter("wtd");
			String origin = request.getHeaderValue("Origin");
			if (origin != null) {
				if (wtdE != null && wtdE.equals(this.sessionId_)
						|| this.state_ == WebSession.State.JustCreated) {
					if (isEqual(origin, "null")) {
						origin = "*";
					}
					handler.getResponse().addHeader(
							"Access-Control-Allow-Origin", origin);
					handler.getResponse().addHeader(
							"Access-Control-Allow-Credentials", "true");
					if (isEqual(request.getRequestMethod(), "OPTIONS")) {
						WebResponse response = handler.getResponse();
						response.setStatus(200);
						response.addHeader("Access-Control-Allow-Methods",
								"POST, OPTIONS");
						response.addHeader("Access-Control-Max-Age", "1728000");
						response.flush();
						return;
					}
				} else {
					if (request.isWebSocketRequest()) {
						handler.flushResponse();
						return;
					}
				}
			}
			String requestE = request.getParameter("request");
			if (requestE != null && requestE.equals("ws")
					&& !request.isWebSocketRequest()) {
				logger.error(new StringWriter().append(
						"invalid WebSocket request, ignoring").toString());
				logger.info(new StringWriter().append("Connection: ").append(
						str(request.getHeaderValue("Connection"))).toString());
				logger.info(new StringWriter().append("Upgrade: ").append(
						str(request.getHeaderValue("Upgrade"))).toString());
				logger
						.info(new StringWriter()
								.append("Sec-WebSocket-Version: ")
								.append(
										str(request
												.getHeaderValue("Sec-WebSocket-Version")))
								.toString());
				handler.flushResponse();
				return;
			}
			if (request.isWebSocketRequest()) {
				if (this.state_ != WebSession.State.JustCreated) {
					this.handleWebSocketRequest(handler);
					return;
				} else {
					handler.flushResponse();
					this.kill();
					return;
				}
			}
			final Configuration conf = this.controller_.getConfiguration();
			handler.getResponse().setResponseType(WebRequest.ResponseType.Page);
			if (!(requestE != null && requestE.equals("resource")
					|| isEqual(request.getRequestMethod(), "POST") || isEqual(
					request.getRequestMethod(), "GET"))) {
				handler.getResponse().setStatus(400);
				handler.flushResponse();
				return;
			}
			if ((!(wtdE != null) || !wtdE.equals(this.sessionId_))
					&& this.state_ != WebSession.State.JustCreated
					&& (requestE != null && (requestE.equals("jsupdate") || requestE
							.equals("resource")))) {
				logger.debug(new StringWriter().append("CSRF: ").append(
						wtdE != null ? wtdE : "no wtd").append(" != ").append(
						this.sessionId_).append(", requestE: ").append(
						requestE != null ? requestE : "none").toString());
				logger.warn(new StringWriter().append("secure:").append(
						"CSRF prevention kicked in.").toString());
				this.serveError(403, handler, "Forbidden");
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
									handler.getResponse().setResponseType(
											WebRequest.ResponseType.Update);
									logger
											.info(new StringWriter()
													.append(
															"signal from dead session, sending reload.")
													.toString());
									this.renderer_.letReloadJS(handler
											.getResponse(), true);
									this.kill();
									break;
								} else {
									if (!requestE.equals("page")) {
										logger
												.info(new StringWriter()
														.append(
																"not serving this.")
														.toString());
										handler.getResponse().setContentType(
												"text/html");
										handler
												.getResponse()
												.out()
												.append(
														"<html><head></head><body></body></html>");
										this.kill();
										break;
									}
								}
							}
							try {
								String internalPath = this.env_
										.getCookie("WtInternalPath");
								this.env_.setInternalPath(internalPath);
							} catch (final RuntimeException e) {
							}
							boolean forcePlain = this.env_.agentIsSpiderBot()
									|| !this.env_.agentSupportsAjax();
							this.progressiveBoot_ = !forcePlain
									&& conf.progressiveBootstrap(this.env_
											.getInternalPath());
							if (forcePlain || this.progressiveBoot_) {
								if (!this.start(handler.getResponse())) {
									throw new WException(
											"Could not start application.");
								}
								this.app_.notify(new WEvent(new WEvent.Impl(
										handler)));
								if (this.env_.agentIsSpiderBot()) {
									this.kill();
								} else {
									if (this.controller_
											.limitPlainHtmlSessions()) {
										logger
												.warn(new StringWriter()
														.append("secure:")
														.append(
																"DoS: plain HTML sessions being limited")
														.toString());
										if (forcePlain) {
											this.kill();
										} else {
											this.setState(
													WebSession.State.Loaded,
													conf.getBootstrapTimeout());
										}
									} else {
										this.setLoaded();
									}
								}
							} else {
								this.serveResponse(handler);
								this.setState(WebSession.State.Loaded, conf
										.getBootstrapTimeout());
							}
							break;
						}
						case WidgetSet:
							if (requestE != null && requestE.equals("resource")) {
								String resourceE = request
										.getParameter("resource");
								if (resourceE != null
										&& resourceE.equals("blank")) {
									handler.getResponse().setContentType(
											"text/html");
									handler
											.getResponse()
											.out()
											.append(
													"<html><head><title>bhm</title></head><body> </body></html>");
								} else {
									logger
											.info(new StringWriter()
													.append(
															"not starting session for resource.")
													.toString());
									handler.getResponse().setContentType(
											"text/html");
									handler
											.getResponse()
											.out()
											.append(
													"<html><head></head><body></body></html>");
								}
								this.kill();
							} else {
								handler.getResponse().setResponseType(
										WebRequest.ResponseType.Script);
								this.init(request);
								this.env_.enableAjax(request);
								if (!this.start(handler.getResponse())) {
									throw new WException(
											"Could not start application.");
								}
								this.app_.notify(new WEvent(new WEvent.Impl(
										handler)));
								this.setExpectLoad();
							}
							break;
						default:
							assert false;
						}
						break;
					}
					case ExpectLoad:
					case Loaded: {
						if (requestE != null) {
							if (requestE.equals("jsupdate")) {
								handler.getResponse().setResponseType(
										WebRequest.ResponseType.Update);
							} else {
								if (requestE.equals("script")) {
									handler.getResponse().setResponseType(
											WebRequest.ResponseType.Script);
									if (this.state_ == WebSession.State.Loaded) {
										this.setExpectLoad();
									}
								} else {
									if (requestE.equals("style")) {
										this.flushBootStyleResponse();
										boolean ios5 = this.env_
												.agentIsMobileWebKit()
												&& (this.env_.getUserAgent()
														.indexOf("OS 5_") != -1
														|| this.env_
																.getUserAgent()
																.indexOf(
																		"OS 6_") != -1
														|| this.env_
																.getUserAgent()
																.indexOf(
																		"OS 7_") != -1 || this.env_
														.getUserAgent()
														.indexOf("OS 8_") != -1);
										String jsE = request.getParameter("js");
										boolean nojs = jsE != null
												&& jsE.equals("no");
										this.bootStyle_ = this.bootStyle_
												&& (this.app_ != null || !ios5
														&& !nojs);
										if (!this.bootStyle_) {
											handler.getResponse()
													.setContentType("text/css");
											handler.flushResponse();
										} else {
											int i = 0;
											final int MAX_TRIES = 1000;
											while (!(this.app_ != null)
													&& i < MAX_TRIES) {
												this.mutex_.unlock();
												Thread.sleep(5);
												this.mutex_.lock();
												++i;
											}
											if (i < MAX_TRIES) {
												this.renderer_
														.serveLinkedCss(handler
																.getResponse());
											}
											handler.flushResponse();
										}
										break;
									}
								}
							}
						}
						boolean requestForResource = requestE != null
								&& requestE.equals("resource");
						if (!(this.app_ != null)) {
							String resourceE = request.getParameter("resource");
							if (handler.getResponse().getResponseType() == WebRequest.ResponseType.Script) {
								if (!(request.getParameter("skeleton") != null)) {
									this.env_.enableAjax(request);
									if (!this.start(handler.getResponse())) {
										throw new WException(
												"Could not start application.");
									}
								} else {
									this.serveResponse(handler);
									return;
								}
							} else {
								if (requestForResource && resourceE != null
										&& resourceE.equals("blank")) {
									handler.getResponse().setContentType(
											"text/html");
									handler
											.getResponse()
											.out()
											.append(
													"<html><head><title>bhm</title></head><body> </body></html>");
									break;
								} else {
									String jsE = request.getParameter("js");
									if (jsE != null && jsE.equals("no")) {
										if (!this.start(handler.getResponse())) {
											throw new WException(
													"Could not start application.");
										}
										if (this.controller_
												.limitPlainHtmlSessions()) {
											logger
													.warn(new StringWriter()
															.append("secure:")
															.append(
																	"DoS: plain HTML sessions being limited")
															.toString());
											this.kill();
										}
									} else {
										if (!conf.reloadIsNewSession()
												&& wtdE != null
												&& wtdE.equals(this.sessionId_)) {
											this.serveResponse(handler);
											this.setState(
													WebSession.State.Loaded,
													conf.getBootstrapTimeout());
										} else {
											handler
													.getResponse()
													.setContentType("text/html");
											handler
													.getResponse()
													.out()
													.append(
															"<html><body><h1>Refusing to respond.</h1></body></html>");
										}
										break;
									}
								}
							}
						}
						boolean doNotify = false;
						if (handler.getRequest() != null) {
							String signalE = handler.getRequest().getParameter(
									"signal");
							boolean isPoll = signalE != null
									&& signalE.equals("poll");
							if (requestForResource || isPoll
									|| !this.isUnlockRecursiveEventLoop()) {
								doNotify = true;
								if (this.env_.hasAjax()) {
									if (this.state_ != WebSession.State.ExpectLoad
											&& handler.getResponse()
													.getResponseType() == WebRequest.ResponseType.Update) {
										this.setLoaded();
									}
								} else {
									if (this.state_ != WebSession.State.ExpectLoad
											&& !this.controller_
													.limitPlainHtmlSessions()) {
										this.setLoaded();
									}
								}
							}
						} else {
							doNotify = false;
						}
						if (doNotify) {
							this.app_.notify(new WEvent(
									new WEvent.Impl(handler)));
							if (handler.getResponse() != null
									&& !requestForResource) {
								this.app_.notify(new WEvent(new WEvent.Impl(
										handler, true)));
							}
						}
						break;
					}
					case Dead:
						logger
								.info(new StringWriter().append(
										"request to dead session, ignoring")
										.toString());
						break;
					}
				} catch (final WException e) {
					logger.error(new StringWriter().append("fatal error: ")
							.append(e.toString()).toString());
					e.printStackTrace();
					this.kill();
					if (handler.getResponse() != null) {
						this.serveError(500, handler, e.toString());
					}
				} catch (final RuntimeException e) {
					logger.error(new StringWriter().append("fatal error: ")
							.append(e.toString()).toString());
					e.printStackTrace();
					this.kill();
					if (handler.getResponse() != null) {
						this.serveError(500, handler, e.toString());
					}
				}
			}
			if (handler.getResponse() != null) {
				handler.flushResponse();
			}
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	public ReentrantLock getMutex() {
		return this.mutex_;
	}

	public void setExpectLoad() {
		if (this.controller_.getConfiguration().ajaxPuzzle()) {
			this.setState(WebSession.State.ExpectLoad, this.controller_
					.getConfiguration().getBootstrapTimeout());
		} else {
			this.setLoaded();
		}
	}

	public void setLoaded() {
		this.setState(WebSession.State.Loaded, this.controller_
				.getConfiguration().getSessionTimeout());
	}

	// public void generateNewSessionId() ;
	private void handleWebSocketRequest(final WebSession.Handler handler) {
	}

	private static void handleWebSocketMessage(WebSession session,
			WebReadEvent event) {
	}

	private static void webSocketReady(WebSession session, WebWriteEvent event) {
		logger.debug(new StringWriter().append("webSocketReady()").toString());
	}

	private void checkTimers() {
		WContainerWidget timers = this.app_.getTimerRoot();
		final List<WWidget> timerWidgets = timers.getChildren();
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
		if (this.app_ != null && this.app_.localizedStrings_ != null) {
			this.app_.localizedStrings_.hibernate();
		}
	}

	private ReentrantLock mutex_;
	private static ThreadLocal<WebSession.Handler> threadHandler_ = new ThreadLocal<WebSession.Handler>();
	private EntryPointType type_;
	private String favicon_;
	private WebSession.State state_;
	private boolean useUrlRewriting_;
	private String sessionId_;
	private String sessionIdCookie_;
	boolean sessionIdChanged_;
	private boolean sessionIdCookieChanged_;
	private boolean sessionIdInUrl_;
	private WtServlet controller_;
	private WebRenderer renderer_;
	private String applicationName_;
	private String bookmarkUrl_;
	private String basePath_;
	private String absoluteBaseUrl_;
	private String applicationUrl_;
	private String deploymentPath_;
	private String redirect_;
	String pagePathInfo_;
	private String pongMessage_;
	private WebResponse asyncResponse_;
	private WebResponse bootStyleResponse_;
	private boolean canWriteAsyncResponse_;
	private int pollRequestsIgnored_;
	private boolean progressiveBoot_;
	private boolean bootStyle_;
	private WebRequest deferredRequest_;
	private WebResponse deferredResponse_;
	private int deferCount_;
	private java.util.concurrent.locks.Condition recursiveEvent_;
	private boolean newRecursiveEvent_;
	private java.util.concurrent.locks.Condition updatesPendingEvent_;
	private boolean updatesPending_;
	private boolean triggerUpdate_;
	private WEnvironment embeddedEnv_;
	private WEnvironment env_;
	private WApplication app_;
	private boolean debug_;
	private List<WebSession.Handler> handlers_;
	private List<WObject> emitStack_;
	private WebSession.Handler recursiveEventLoop_;

	// private WResource decodeResource(final String resourceId) ;
	private AbstractEventSignal decodeSignal(final String signalId,
			boolean checkExposed) {
		AbstractEventSignal result = this.app_.decodeExposedSignal(signalId);
		if (result != null && checkExposed) {
			WWidget w = ((result.getSender()) instanceof WWidget ? (WWidget) (result
					.getSender())
					: null);
			if (w != null && !this.app_.isExposed(w)) {
				result = null;
			}
		}
		if (!(result != null) && checkExposed) {
			logger.error(new StringWriter().append("decodeSignal(): signal '")
					.append(signalId).append("' not exposed").toString());
		}
		return result;
	}

	private AbstractEventSignal decodeSignal(final String objectId,
			final String name, boolean checkExposed) {
		AbstractEventSignal result = this.app_.decodeExposedSignal(objectId,
				name);
		if (result != null && checkExposed) {
			WWidget w = ((result.getSender()) instanceof WWidget ? (WWidget) (result
					.getSender())
					: null);
			if (w != null && !this.app_.isExposed(w) && !name.equals("resized")) {
				result = null;
			}
		}
		if (!(result != null) && checkExposed) {
			logger.error(new StringWriter().append("decodeSignal(): signal '")
					.append(objectId).append('.').append(name).append(
							"' not exposed").toString());
		}
		return result;
	}

	private static WObject.FormData getFormData(final WebRequest request,
			final String name) {
		List<UploadedFile> files = new ArrayList<UploadedFile>();
		CollectionUtils.findInMultimap(request.getUploadedFiles(), name, files);
		return new WObject.FormData(request.getParameterValues(name), files);
	}

	private void render(final WebSession.Handler handler) throws IOException {
		try {
			if (!this.env_.hasAjax()) {
				try {
					this.checkTimers();
				} catch (final RuntimeException e) {
					logger.error(new StringWriter().append(
							"Exception while triggering timers").append(
							e.toString()).toString());
					throw e;
				}
			}
			if (this.app_ != null && this.app_.isQuited()) {
				this.kill();
			}
			if (handler.getResponse() != null) {
				this.serveResponse(handler);
			}
		} catch (final RuntimeException e) {
			handler.flushResponse();
			throw e;
		}
		this.updatesPending_ = false;
	}

	private void serveError(int status, final WebSession.Handler handler,
			final String e) throws IOException {
		this.renderer_.serveError(status, handler.getResponse(), e);
		handler.flushResponse();
	}

	private void serveResponse(final WebSession.Handler handler)
			throws IOException {
		if (handler.getResponse().getResponseType() == WebRequest.ResponseType.Page) {
			this.pagePathInfo_ = handler.getRequest().getPathInfo();
			String wtdE = handler.getRequest().getParameter("wtd");
			if (wtdE != null && wtdE.equals(this.sessionId_)) {
				this.sessionIdInUrl_ = true;
			} else {
				this.sessionIdInUrl_ = false;
			}
		}
		if (!handler.getRequest().isWebSocketMessage()) {
			if (handler.getResponse().getResponseType() == WebRequest.ResponseType.Script
					&& !(handler.getRequest().getParameter("skeleton") != null)) {
				this.mutex_.unlock();
				try {
					Thread.sleep(1);
				} catch (final InterruptedException e) {
				}
				this.mutex_.lock();
			}
			this.renderer_.serveResponse(handler.getResponse());
		}
		handler.flushResponse();
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

	private void processSignal(AbstractEventSignal s, final String se,
			final WebRequest request, WebSession.SignalKind kind) {
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

	private List<Integer> getSignalProcessingOrder(final WEvent e) {
		final WebSession.Handler handler = e.impl_.handler;
		List<Integer> highPriority = new ArrayList<Integer>();
		List<Integer> normalPriority = new ArrayList<Integer>();
		for (int i = 0;; ++i) {
			final WebRequest request = handler.getRequest();
			String se = i > 0 ? 'e' + String.valueOf(i) : "";
			String signalE = this.getSignal(request, se);
			if (!(signalE != null)) {
				break;
			}
			if (!signalE.equals("user") && !signalE.equals("hash")
					&& !signalE.equals("none") && !signalE.equals("poll")
					&& !signalE.equals("load")) {
				AbstractEventSignal signal = this.decodeSignal(signalE, true);
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

	private void notifySignal(final WEvent e) throws IOException {
		final WebSession.Handler handler = e.impl_.handler;
		if (handler.nextSignal == -1) {
			Utils.copyList(this.getSignalProcessingOrder(e),
					handler.signalOrder);
			handler.nextSignal = 0;
		}
		for (int i = handler.nextSignal; i < handler.signalOrder.size(); ++i) {
			if (!(handler.getRequest() != null)) {
				return;
			}
			final WebRequest request = handler.getRequest();
			int signalI = handler.signalOrder.get(i);
			String se = signalI > 0 ? 'e' + String.valueOf(signalI) : "";
			String signalE = this.getSignal(request, se);
			if (!(signalE != null)) {
				return;
			}
			this.renderer_.setRendered(true);
			logger.debug(new StringWriter().append("signal: ").append(signalE)
					.toString());
			if (signalE.equals("none") || signalE.equals("load")) {
				if (signalE.equals("load")) {
					if (this.getType() == EntryPointType.WidgetSet) {
						this.renderer_.setRendered(false);
					}
					if (!this.renderer_.checkResponsePuzzle(request)) {
						this.app_.quit();
					} else {
						this.setLoaded();
					}
				}
				this.renderer_.setVisibleOnly(false);
			} else {
				if (!signalE.equals("poll")) {
					this.propagateFormValues(e, se);
					boolean discardStateless = !request.isWebSocketMessage()
							&& i == 0;
					if (discardStateless) {
						this.renderer_.saveChanges();
					}
					handler.nextSignal = i + 1;
					if (signalE.equals("hash")) {
						String hashE = request.getParameter(se + "_");
						if (hashE != null) {
							this.changeInternalPath(hashE, handler
									.getResponse());
							this.app_.doJavaScript("Wt3_3_2.scrollIntoView("
									+ WWebWidget.jsStringLiteral(hashE) + ");");
						} else {
							this.changeInternalPath("", handler.getResponse());
						}
					} else {
						for (int k = 0; k < 3; ++k) {
							WebSession.SignalKind kind = WebSession.SignalKind
									.values()[k];
							if (kind == WebSession.SignalKind.AutoLearnStateless
									&& 0L != 0) {
								break;
							}
							AbstractEventSignal s;
							if (signalE.equals("user")) {
								String idE = request.getParameter(se + "id");
								String nameE = request
										.getParameter(se + "name");
								if (!(idE != null) || !(nameE != null)) {
									break;
								}
								s = this.decodeSignal(idE, nameE, k == 0);
							} else {
								s = this.decodeSignal(signalE, k == 0);
							}
							this.processSignal(s, se, request, kind);
							if (kind == WebSession.SignalKind.LearnedStateless
									&& discardStateless) {
								this.renderer_.discardChanges();
							}
						}
					}
				}
			}
		}
	}

	private void propagateFormValues(final WEvent e, final String se) {
		final WebRequest request = e.impl_.handler.getRequest();
		this.renderer_.updateFormObjectsList(this.app_);
		Map<String, WObject> formObjects = this.renderer_.getFormObjects();
		String focus = request.getParameter(se + "focus");
		if (focus != null) {
			int selectionStart = -1;
			int selectionEnd = -1;
			try {
				String selStart = request.getParameter(se + "selstart");
				if (selStart != null) {
					selectionStart = Integer.parseInt(selStart);
				}
				String selEnd = request.getParameter(se + "selend");
				if (selEnd != null) {
					selectionEnd = Integer.parseInt(selEnd);
				}
			} catch (final NumberFormatException ee) {
				logger.error(new StringWriter().append(
						"Could not lexical cast selection range").toString());
			}
			this.app_.setFocus(focus, selectionStart, selectionEnd);
		} else {
			this.app_.setFocus("", -1, -1);
		}
		for (Iterator<Map.Entry<String, WObject>> i_it = formObjects.entrySet()
				.iterator(); i_it.hasNext();) {
			Map.Entry<String, WObject> i = i_it.next();
			String formName = i.getKey();
			WObject obj = i.getValue();
			if (!(0L != 0)) {
				obj.setFormData(getFormData(request, se + formName));
			} else {
				obj.setRequestTooLarge(0L);
			}
		}
	}

	private String getSignal(final WebRequest request, final String se) {
		String signalE = request.getParameter(se + "signal");
		if (!(signalE != null)) {
			final int signalLength = 7 + se.length();
			final Map<String, String[]> entries = request.getParameterMap();
			for (Iterator<Map.Entry<String, String[]>> i_it = entries
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<String, String[]> i = i_it.next();
				if (i.getKey().length() > (int) signalLength
						&& i.getKey().substring(0, 0 + signalLength).equals(
								se + "signal=")) {
					signalE = i.getValue()[0];
					String v = i.getKey().substring(signalLength);
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

	private void init(final WebRequest request) {
		this.env_.init(request);
		final Configuration conf = this.controller_.getConfiguration();
		if (conf.getSessionTracking() == Configuration.SessionTracking.CookiesURL
				&& this.env_.supportsCookies()) {
			this.useUrlRewriting_ = false;
		}
		String hashE = request.getParameter("_");
		this.absoluteBaseUrl_ = this.env_.getUrlScheme() + "://"
				+ this.env_.getHostName() + this.basePath_;
		boolean useAbsoluteUrls;
		String absoluteBaseUrl = this.app_.readConfigurationProperty("baseURL",
				this.absoluteBaseUrl_);
		if (absoluteBaseUrl != this.absoluteBaseUrl_) {
			this.absoluteBaseUrl_ = absoluteBaseUrl;
			useAbsoluteUrls = true;
		} else {
			useAbsoluteUrls = false;
		}
		if (useAbsoluteUrls) {
			int slashpos = this.absoluteBaseUrl_.lastIndexOf('/');
			if (slashpos != -1
					&& slashpos != this.absoluteBaseUrl_.length() - 1) {
				this.absoluteBaseUrl_ = this.absoluteBaseUrl_.substring(0,
						0 + slashpos + 1);
			}
			slashpos = this.absoluteBaseUrl_.indexOf("://");
			if (slashpos != -1) {
				slashpos = this.absoluteBaseUrl_.indexOf("/", slashpos + 3);
				if (slashpos != -1) {
					this.deploymentPath_ = this.absoluteBaseUrl_
							.substring(slashpos)
							+ this.applicationName_;
				}
			}
		}
		this.bookmarkUrl_ = this.applicationName_;
		if (this.getType() == EntryPointType.WidgetSet || useAbsoluteUrls) {
			this.applicationUrl_ = this.absoluteBaseUrl_
					+ this.applicationName_;
			this.bookmarkUrl_ = this.applicationUrl_;
		}
		String path = request.getPathInfo();
		if (path.length() == 0 && hashE != null) {
			path = hashE;
		}
		this.env_.setInternalPath(path);
		this.pagePathInfo_ = request.getPathInfo();
	}

	private boolean start(WebResponse response) {
		try {
			this.app_ = this.controller_.doCreateApplication(this);
			if (!this.app_.internalPathValid_) {
				if (response.getResponseType() == WebRequest.ResponseType.Page) {
					response.setStatus(404);
				}
			}
		} catch (final RuntimeException e) {
			this.app_ = null;
			this.kill();
			throw e;
		}
		return this.app_ != null;
	}

	private String getSessionQuery() {
		return "?wtd=" + DomElement.urlEncodeS(this.sessionId_);
	}

	private void flushBootStyleResponse() {
		if (this.bootStyleResponse_ != null) {
			this.bootStyleResponse_.flush();
			this.bootStyleResponse_ = null;
		}
	}

	private void changeInternalPath(final String path, WebResponse response) {
		if (!this.app_.internalPathIsChanged_) {
			if (!this.app_.changedInternalPath(path)) {
				if (response.getResponseType() == WebRequest.ResponseType.Page) {
					response.setStatus(404);
				}
			}
		}
	}

	private static UploadedFile uf;

	static boolean isAbsoluteUrl(final String url) {
		return url.indexOf(":") != -1;
	}

	static String host(final String url) {
		int pos = 0;
		for (int i = 0; i < 3; ++i) {
			pos = url.indexOf('/', pos);
			if (pos == -1) {
				return url;
			} else {
				++pos;
			}
		}
		return url.substring(0, 0 + pos - 1);
	}

	static String str(String v) {
		return v != null ? v : "";
	}

	static boolean isEqual(String s1, String s2) {
		return s1 == s2;
	}
}
