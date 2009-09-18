/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

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
	private Signal dataChanged_ = null;
	private String suggestedFileName_;
	private String currentUrl_;

	WResource(WObject parent) {
		super(parent);
		generateUrl();
		suggestedFileName_ = "";
	}

	/**
	 * Constructor.
	 */
	public WResource() {
		this(null);
	}

	/**
	 * Generates a URL for this resource.
	 * <p>
	 * The url is unique to assure that it is not cached by the web browser, and
	 * can thus be used to refer to a new "version" of the resource, which can
	 * be indicated by triggering the {@link #dataChanged()} signal.
	 * 
	 * The old urls are not invalidated by calling this method.
	 * 
	 * @return the url.
	 */
	public String generateUrl() {
		WApplication app = WApplication.getInstance();

		currentUrl_ = app.addExposedResource(this);
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
		if (suggestedFileName_.length() != 0)
			response.addHeader("Content-Disposition", "attachment;filename="
					+ suggestedFileName_);

		handleRequest(request, response);
		response.flush();
	}

	/**
	 * Signal triggered when the data presented in this resource has changed.
	 * <p>
	 * Widgets that reference the resource (such as anchors and images) will
	 * make sure the new data is rendered.
	 * 
	 * It is better to call setChanged() than to trigger this signal. setChanged
	 * generates a new URL for this resource to avoid caching problems and then
	 * emits this signal.
	 */
	public Signal dataChanged() {
		if (dataChanged_ == null)
			dataChanged_ = new Signal(this);
		return dataChanged_;
	}

	/**
	 * Write the resource to an output stream.
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
	public void write(OutputStream out, Map<String, List<String>> parameterMap,
			Map<String, UploadedFile> uploadedFiles) throws IOException {
		WebRequest request = new WebRequest(parameterMap, uploadedFiles);
		WebResponse response = new WebResponse(out);
		handleRequest(request, response);
	}

	/**
	 * Suggests a filename to the user for the data streamed by this resource.
	 * <p>
	 * For resources, intended to be downloaded by the user, suggest a name used
	 * for saving. The filename extension may also help the browser to identify
	 * the correct program for opening the resource.
	 */
	public void suggestFileName(String name) {
		suggestedFileName_ = name;
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
	 */
	public void setChanged() {
		generateUrl();
		dataChanged_.trigger();
	}

	/**
	 * Returns the current URL for this resource. Returns the url that refers to
	 * this resource.
	 */
	public String getUrl() {
		return currentUrl_;
	}
}
