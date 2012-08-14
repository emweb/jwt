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
 * An environment for testing purposes
 */
public class WTestEnvironment extends WEnvironment {
	private static Logger logger = LoggerFactory
			.getLogger(WTestEnvironment.class);

	public WTestEnvironment(Configuration configuration, EntryPointType type) {
		super();
		this.theSession_ = null;
		this.dialogExecuted_ = new Signal1<WDialog>();
		this.popupExecuted_ = new Signal1<WPopupMenu>();
		List<String> dummy = new ArrayList<String>();
		this.configuration_ = configuration;
		this.controller_ = new TestController(configuration);
		this.init(type);
	}

	public WTestEnvironment(Configuration configuration) {
		this(configuration, EntryPointType.Application);
	}

	public void setParameterMap(Map<String, String[]> parameters) {
		this.parameters_ = parameters;
	}

	public void setCookies(Map<String, String> cookies) {
		this.cookies_ = cookies;
	}

	public void setHeaderValue(String value) {
	}

	public void setSupportsCookies(boolean enabled) {
		this.doesCookies_ = enabled;
	}

	public void setAjax(boolean enabled) {
		this.doesAjax_ = enabled;
	}

	public void setDpiScale(double dpiScale) {
		this.dpiScale_ = dpiScale;
	}

	public void setLocale(Locale locale) {
		this.locale_ = locale;
	}

	public void setHostName(String hostName) {
		this.host_ = hostName;
	}

	public void setUrlScheme(String scheme) {
		this.urlScheme_ = scheme;
	}

	void setUserAgent(String userAgent) {
		super.setUserAgent(userAgent);
	}

	public void setReferer(String referer) {
		this.referer_ = referer;
	}

	public void setAccept(String accept) {
		this.accept_ = accept;
	}

	public void setServerSignature(String signature) {
		this.serverSignature_ = signature;
	}

	public void setServerSoftware(String software) {
		this.serverSignature_ = software;
	}

	public void setServerAdmin(String serverAdmin) {
		this.serverAdmin_ = serverAdmin;
	}

	public void setClientAddress(String clientAddress) {
		this.clientAddress_ = clientAddress;
	}

	public void setInternalPath(String internalPath) {
		super.setInternalPath(internalPath);
	}

	public void setContentType(WEnvironment.ContentType contentType) {
		this.contentType_ = contentType;
	}

	Signal1<WDialog> dialogExecuted() {
		return this.dialogExecuted_;
	}

	Signal1<WPopupMenu> popupExecuted() {
		return this.popupExecuted_;
	}

	public void endRequest() {
		;
	}

	public void startRequest() {
		new WebSession.Handler(this.theSession_, true);
	}

	private WebSession theSession_;
	private Configuration configuration_;
	private WtServlet controller_;
	private Signal1<WDialog> dialogExecuted_;
	private Signal1<WPopupMenu> popupExecuted_;

	public boolean isTest() {
		return true;
	}

	private void init(EntryPointType type) {
		this.session_ = new WebSession(this.controller_, "testwtd", type, "",
				(WebRequest) null, this);
		this.theSession_ = this.session_;
		new WebSession.Handler(this.theSession_, true);
		this.doesAjax_ = true;
		this.doesCookies_ = true;
		this.dpiScale_ = 1;
		this.contentType_ = WEnvironment.ContentType.XHTML1;
		this.urlScheme_ = "http";
		this.referer_ = "";
		this.accept_ = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
		this.serverSignature_ = "None (WTestEnvironment)";
		this.serverSoftware_ = this.serverSignature_;
		this.serverAdmin_ = "your@onyourown.here";
		this.pathInfo_ = "";
		this
				.setUserAgent("Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.11) Gecko/2009060309 Ubuntu/9.04 (jaunty) Firefox/3.0.11");
		this.host_ = "localhost";
		this.clientAddress_ = "127.0.0.1";
		this.locale_ = new Locale("en");
	}
}
