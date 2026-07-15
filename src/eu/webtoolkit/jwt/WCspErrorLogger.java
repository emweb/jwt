/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.OutputStream;
import java.text.Normalizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.WebSession.Handler;
import eu.webtoolkit.jwt.servlet.UploadedFile;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

/**
 * An object which can be rendered in the HTTP protocol.
 *
 * <h3>Usage</h3>
 *
 * Besides the main page, other objects may be rendered as additional resources,
 * for example documents or images. Classes such as {@link WAnchor} or
 * {@link WImage} can use a resource instead of a URL to provide their contents.
 * Whenever the resource has changed, you should call the setChanged() method.
 * setChanged() will make sure that the browser will use a new version of the
 * resource by generating a new URL, and emits the dataChanged() signal to make
 * those that refer to the resource aware that they should update their
 * references to the new URL.
 * <p>
 * You can help the browser to start a suitable helper application to handle the
 * resource, or suggest to the user a suitable filename for saving the resource,
 * by setting an appropriate file name using {@link #suggestFileName(String)}.
 * <p>
 * To serve resources that you create on the fly, you need to specialize this
 * class and reimplement {@link #handleRequest(WebRequest, WebResponse)}.
 *
 * <h3>Concurrency issues</h3>
 *
 * Because of the nature of the web, a resource may be requested one time or
 * multiple times at the discretion of the browser, and therefore your resource
 * should in general not have any side-effects except for what is needed to
 * render its own contents. Unlike event notifications to a JWt application,
 * resource requests are not serialized, but are handled concurrently. Therefore
 * you are not allowed to access or modify widget state from within the
 * resource, unless you provide your own locking mechanism for it.
 *
 * @see WAnchor
 * @see WImage
 */
public class WCspErrorLogger extends WResource {

	private static Logger logger = LoggerFactory.getLogger(WCspErrorLogger.class);

	private int maxReportSize_ = 1024 * 50;

	/**
	 * Construct a CSP error logger.
	 */
	public WCspErrorLogger() {}

	/**
	 * Construct a CSP error logger with the given maximum report size.
	 */
	public WCspErrorLogger(int maxReportSize) {
		this.maxReportSize_ = maxReportSize;
	}


	protected void handleRequest(WebRequest request,
															 WebResponse response) throws IOException {
		int len = request.getContentLength();
		String type = request.getContentType();

		if (type == null ||
				(!type.equals("application/reports+json") &&
				!type.equals("application/csp-report"))) {
			logger.info("Ignoring CSP report with unexpected content type: {}", (type == null ? "(null)" : type));
			return;
		}

		if (len > maxReportSize_) {
			logger.info("Ignoring CSP report that exceeds maximum size of {} bytes", maxReportSize_);
			return;
		}

		if (len == 0) {
			logger.info("Ignoring empty CSP report");
			return;
		}

		byte[] buf = new byte[len];
		request.getInputStream().read(buf, 0, len);

		String body = new String(buf, "UTF-8");
		logReport(body);
	}

	protected void logReport(final String report)
	{
		logger.error("CSP report:\n{}", report);
	}


}
