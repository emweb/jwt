/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public abstract class WResource extends WObject {
	/**
	 * Values for the disposition type in the Content-Disposition header
	 */
	public enum DispositionType {
		/**
		 * Do not specify a disposition type
		 */
		NoDisposition,
		/**
		 * Open with a helper application or show 'Save As' dialog
		 */
		Attachment,
		/**
		 * View with a browser plugin
		 */
		Inline
	};
	
	private Signal dataChanged_ = new Signal(this);

	private String suggestedFileName_;
	private String currentUrl_;
	private String internalPath_;
	private String botUrl_ = "";
	private boolean trackUploadProgress_;
	private DispositionType dispositionType_;
	private int version_ = 0;
	private boolean invalidAfterChanged_ = false;

	/**
	 * Constructor.
	 */
	public WResource() {
		suggestedFileName_ = "";
		internalPath_ = "";
		dispositionType_ = DispositionType.NoDisposition;

		generateUrl();
	}

	/**
	 * Generates a URL for this resource.
	 * <p>
	 * The url is unique to assure that it is not cached by the web browser, and
	 * can thus be used to refer to a new "version" of the resource, which can
	 * be indicated by triggering the {@link #dataChanged()} signal.
	 * 
	 * The old urls are not invalidated by calling this method, unless
	 * you enable setInvalidAfterChanged().
	 * 
	 * @return the url.
	 */
	public String generateUrl() {
		WApplication app = WApplication.getInstance();
		
		if (app != null) {
			if (!botUrl_.isEmpty() && app.getEnvironment().agentIsSpiderBot()) {
				currentUrl_ = botUrl_;
			} else {
				if (currentUrl_ != null && trackUploadProgress_) {
					WtServlet c = WebSession.getInstance().getController();
					c.removeUploadProgressUrl(getUrl());
				}
				currentUrl_ = app.addExposedResource(this);
				if (trackUploadProgress_) {
					WtServlet c = WebSession.getInstance().getController();
					c.addUploadProgressUrl(getUrl());
				}
			}
		} else {
			currentUrl_ = internalPath_;
		}
		return currentUrl_;
	}

	void beingDeleted() {
	}

	/**
	 * Handles a request.
	 * <p>
	 * Reimplement this method so that a proper response is generated for the
	 * given request. From the <i>request</i> object you can access request
	 * parameters and whether the request is a continuation request. In the
	 * <i>response</i> object, you should set the mime type and stream the
	 * output data.
	 * 
	 * @param request
	 *            The request information
	 * @param response
	 *            The response object
	 * @throws IOException
	 */
	abstract protected void handleRequest(WebRequest request,
			WebResponse response) throws IOException;

	void handle(WebRequest request, WebResponse response) throws IOException {

		Handler handler = WebSession.Handler.getInstance();
		if (!takesUpdateLock() && handler != null)
			WebSession.Handler.getInstance().unlock();

		if (dispositionType_ != DispositionType.NoDisposition
				|| suggestedFileName_.length() != 0) {
			String theDisposition;
			switch (dispositionType_) {
			default:
			case Inline:
				theDisposition = "inline";
				break;
			case Attachment:
				theDisposition = "attachment";
				break;
			}
			if (suggestedFileName_.length() != 0) {
				if (dispositionType_ == DispositionType.NoDisposition) {
					// backward compatibility-ish with older Wt versions
					theDisposition = "attachment";
				}
				// Browser incompatibility hell: internationalized filename
				// suggestions
				// First filename is for browsers that don't support RFC 5987
				// Second filename is for browsers that do support RFC 5987
				String fileField;
				// We cannot query wApp here, because wApp doesn't exist for
				// static resources.
				String userAgent = request.getUserAgent();
				boolean isIE = userAgent.contains("MSIE");
				boolean isChrome = userAgent.contains("Chrome");
				if (isIE || isChrome) {
					// filename="foo-%c3%a4-%e2%82%ac.html"
					// Note: IE never converts %20 back to space, so avoid escaping
					// IE will also not url decode the filename if the file has no ASCII
					// extension (e.g. .txt)
					fileField = "filename=\""
							+ StringUtils.urlEncode(suggestedFileName_, " ")
							+ "\";";
				} else {
					// Binary UTF-8 sequence: for FF3, Safari, Chrome, Chrome9
					fileField = "filename=\"" + suggestedFileName_ + "\";";
				}
				// Next will be picked by RFC 5987 in favour of the
				// one without specified encoding (Chrome9,
				fileField += StringUtils.encodeHttpHeaderField("filename",
						suggestedFileName_);
				response.addHeader("Content-Disposition", theDisposition + ";"
						+ fileField);
			} else {
				response.addHeader("Content-Disposition", theDisposition);
			}
		}

		handleRequest(request, response);
		response.flush();
	}

	/**
	 * Signal triggered when the data presented in this resource has changed.
	 * <p>
	 * Widgets that reference the resource (such as anchors and images) will
	 * make sure the new data is rendered.
	 * 
	 * It is better to call {@link #setChanged()} than to trigger this signal
	 * since that method generates a new URL for this resource to avoid caching problems
	 * before emitting the signal.
	 */
	public Signal dataChanged() {
		return dataChanged_;
	}

	/**
	 * Writes the resource to an output stream.
	 * <p>
	 * This is a utility method that allows you to write the resource to an
	 * output stream, by using {@link #handleRequest(WebRequest, WebResponse)}.
	 * 
	 * @param out
	 *            The output stream.
	 * @param parameterMap
	 *            A map with parameters that are made available in the
	 *            {@link WebRequest}.
	 * @param uploadedFiles
	 *            A list of uploaded files that are made available in the
	 *            {@link WebRequest}.
	 * @throws IOException
	 */
	public void write(OutputStream out, Map<String, String[]> parameterMap,
			Map<String, List<UploadedFile>> uploadedFiles) throws IOException {
		WebRequest request = new WebRequest(parameterMap, uploadedFiles);
		WebResponse response = new WebResponse(out);
		handleRequest(request, response);
		response.flush();
	}
	
	/**
	 * Writes the resource to an output stream.
	 * <p>
	 * This is a utility method that allows you to write the resource to an
	 * output stream, by using {@link #handleRequest(WebRequest, WebResponse)}.
	 * 
	 * @param out
	 *            The output stream.
	 * @throws IOException
	 */
	public void write(OutputStream out) throws IOException {
		write(out, new HashMap<String, String[]>(), new HashMap<String, List<UploadedFile>>());
	}

	/**
	 * Suggests a filename to the user for the data streamed by this resource.
	 * <p>
	 * For resources, intended to be downloaded by the user, suggest a name used
	 * for saving. The filename extension may also help the browser to identify
	 * the correct program for opening the resource.
	 * 
	 * The disposition type determines if the resource is intended to
	 * be opened by a plugin in the browser (DispositionType#Inline), or to be saved to disk
	 * (DispositionType#Attachment). DispositionType#NoDisposition is not a valid Content-Disposition when a
	 * filename is suggested; this will be rendered as DispositionType#Attachment.
	 *
	 * @see WResource#setDispositionType(DispositionType dispositionType)
	 */
	public void suggestFileName(String name, DispositionType dispositionType) {
		suggestedFileName_ = name;
		dispositionType_ = dispositionType;
		
		generateUrl();
	}
	
	/**
	 * Suggests a filename to the user for the data streamed by this resource.
	 * <p>
	 * For resources, intended to be downloaded by the user, suggest a name used
	 * for saving. The filename extension may also help the browser to identify
	 * the correct program for opening the resource.
	 * 
	 * The disposition type determines if the resource is intended to
	 * be opened by a plugin in the browser (DispositionType#Inline), or to be saved to disk
	 * (DispositionType#Attachment). DispositionType#NoDisposition is not a valid Content-Disposition when a
	 * filename is suggested; this will be rendered as DispositionType#Attachment.
	 *
	 * @see WResource#setDispositionType(DispositionType dispositionType)
	 * 
	 * Calls {@link #suggestFileName(String name, DispositionType dispositionType)
	 * suggestFileName(name, DispositionType.Attachment) }
	 */
	public void suggestFileName(String name) {
		suggestFileName(name, DispositionType.Attachment);
	}

	/**
	 * Returns the suggested file name.
	 * 
	 * @see #suggestFileName(String)
	 */
	public String getSuggestedFileName() {
		return suggestedFileName_;
	}

	/**
	 * Generates a new URL for this resource and emits the changed signal
	 *
	 * @see #setInvalidAfterChanged(boolean)
	 */
	public void setChanged() {
		generateUrl();

		dataChanged_.trigger();
	}

	/**
	 * Return "page not found" for prior resource URLs after change
	 *
	 * This option invalidates earlier versions of the resource url prior to
	 * the last call of setChanged() or generateUrl(). The default value is false.
	 *
	 * This does not work when the resource is deployed at an internal path using
	 * setInternalPath().
	 *
	 * @see #setChanged()
	 * @see #generateUrl()
	 */
	public void setInvalidAfterChanged(boolean enabled) {
		invalidAfterChanged_ = enabled;
	}

	/**
	 * Should "page not found" be returned for outdated resource URLs
	 *
	 * @see #setInvalidAfterChanged()
	 */
	public boolean isInvalidAfterChanged() {
		return invalidAfterChanged_;
	}

	/**
	 * Configures the Content-Disposition header
	 * 
	 * The Content-Disposition header allows to instruct the browser that a
	 * resource should be shown inline or as attachment. This function enables
	 * you to set this property.
	 * 
	 * This is often used in combination with
	 * {@link #suggestFileName(String, DispositionType)}. The
	 * Content-Disposition must not be DispositionType#NoDisposition
	 * when a filename is given; if this case is encountered, None will be
	 * rendered as DispositionType#Attachment.
	 * 
	 * @see #suggestFileName(String, DispositionType)
	 */
	public void setDispositionType(DispositionType dispositionType) {
		dispositionType_ = dispositionType;
	}

	/**
	 * Returns the currently configured content disposition
	 * 
	 * @see #setDispositionType(DispositionType cd)
	 */
	public DispositionType getDispositionType() {
		return dispositionType_;
	}

	/**
	 * Returns the current URL for this resource. Returns the url that refers to
	 * this resource.
	 */
	public String getUrl() {
		return currentUrl_;
	}

	/**
	 * Returns the internal path.
	 * 
	 * @see #setInternalPath(String)
	 */
	public String getInternalPath() {
		return internalPath_;
	}

	/** Sets an internal path for this resource.
	 *
	 * Using this method you can deploy the resource at a fixed path. Unless
	 * you deploy using cookies for session tracking (not recommended), a
	 * session identifier will be appended to the path.
	 *
	 * You should use internal paths that are different from internal paths 
	 * handled by your application ({@link WApplication#setInternalPath(String)}), if you
	 * do not want a conflict between these two when the browser does not use
	 * AJAX (and thus url fragments for its internal paths).
	 *
	 * The default value is empty. By default the URL for a resource is
	 * unspecified and a URL will be generated by the library. 
	 */
	public void setInternalPath(String internalPath) {
		this.internalPath_ = internalPath;
		
		generateUrl();
	}

	/** Sets an alternative URL, given to bots, for this resource.
	 *
	 * If <code>url</code> is not empty, this URL will be used instead of
	 * the regular URL when the request comes from a bot (e.g., a web
	 * crawler).
	 *
	 * As bots have their session terminated after sending a reply,
	 * the {@link WApplication} linked to it, and thus also its resources
	 * are removed. This means functionality such as continuations,
	 * handling resource or application changes are not available.
	 *
	 * Only public resources can be accessed by bots (see
	 * {@link WServer#addResource()}). Private resources (linked to the
	 * {@link WApplication}) cannot be accessed by bots.
	 *
	 * By default, this is empty.
	 *
	 * <p><i><b>Note: </b> If this is not empty, the resource will not be
	 * accessible to bots.
	 */
	public void setAlternativeBotUrl(String url) {
		WApplication app = WApplication.getInstance();
		boolean wasEmpty = botUrl_.isEmpty();
		this.botUrl_ = url;

		if (app != null && app.getEnvironment().agentIsSpiderBot()) {
			if (wasEmpty) {
				app.removeExposedResource(this);
			}
			generateUrl();
		}
	}

	public String getAlternativeBotUrl() {
		return botUrl_;
	}

	/**
	 * Indicate interest in upload progress.
	 * 
	 * When supported, you can track upload progress using this signal. While
	 * data is being received, and before {@link #handleRequest(WebRequest
	 * request, WebResponse response)} is called, progress information is
	 * indicated using {@link #dataReceived()}.
	 * 
	 * The default value is false.
	 */
	public void setUploadProgress(boolean enabled) {
		if (trackUploadProgress_ != enabled) {
			trackUploadProgress_ = enabled;

			WtServlet c = WebSession.getInstance().getController();
			if (enabled)
				c.addUploadProgressUrl(getUrl());
			else
				c.removeUploadProgressUrl(getUrl());
		}
	}

	private boolean takesUpdateLock_ = true;

	/**
	 * Set whether this resource takes the WApplication's update lock.
	 * <p>
	 * By default, WResource takes the WApplication's update lock,
	 * so handleRequest() is performed in the WApplication's event loop.
	 * <p>
	 * If necessary this can be disabled by setting this option to false.
	 * This will make it so that handleRequest() does not block the WApplication,
	 * and multiple handleRequest() calls can be performed concurrently.
	 * <p>
	 * This option has no effect on static resources, since there is no WApplication
	 * in that case.
	 */
	public void setTakesUpdateLock(boolean enabled) {
		takesUpdateLock_ = enabled;
	}

	/**
	 * Get whether this resource takes the WApplication's update lock
	 *
	 * @see setTakesUpdateLock(boolean enabled)
	 */
	public boolean takesUpdateLock() {
		return takesUpdateLock_;
	}

	public int getVersion() {
		return version_;
	}

	public void incrementVersion() {
		version_++;
	}

	/**
	 * Signal emitted when data has been received for this resource.
	 * 
	 * When this signal is emitted, you have the update lock to modify the
	 * application. Because there is however no corresponding request from the
	 * browser, any update to the user interface is not immediately reflected in
	 * the client. To update the client interface, you need to use a
	 * {@link WTimer}.
	 * 
	 * @see #setUploadProgress(boolean enabled)
	 */
	public Signal2<Long, Long> dataReceived() {
		return dataReceived_;
	}

	private Signal2<Long, Long> dataReceived_ = new Signal2<Long, Long>();

	public Signal1<Long> dataExceeded() {
		return dataExceeded_;
	}

	private Signal1<Long> dataExceeded_ = new Signal1<Long>();
}
