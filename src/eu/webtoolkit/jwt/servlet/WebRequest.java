/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import eu.webtoolkit.jwt.WResource;
import eu.webtoolkit.jwt.WtServlet;

/**
 * A WebRequest which wraps the HttpServletRequest to add support for file uploads and
 * testing.
 * <p>
 * WebRequest is used instead of HttpServletRequest inside JWt's request handling,
 * and also in {@link WResource#handleRequest(WebRequest request, WebResponse response)}.
 * It handles files being POST'ed, and treats parameters in the URL or within the
 * request body in the same way.
 * <p>
 * @see WebResponse
 */
public class WebRequest extends HttpServletRequestWrapper {
	/**
	 * Progress listener interface.
	 */
	public interface ProgressListener {
		/**
		 * Update callback method.
		 */
		public void update(WebRequest request, long pBytesRead, long pContentLength);
	}
	
	private Map<String, String[]> parameters_;
	private Map<String, List<UploadedFile>> files_;

	/**
	 * Creates a WebRequest by wrapping an HttpServletRequest
	 * @param request The request to be wrapped.
	 */
	public WebRequest(HttpServletRequest request) {
		super(request);
		parse(null);
	}
	
	/**
	 * Creates a WebRequest by wrapping an HttpServletRequest
	 * @param request The request to be wrapped.
	 * @param progressListener a progress listener implementation
	 */
	public WebRequest(HttpServletRequest request, ProgressListener progressListener) {
		super(request);
		parse(progressListener);
	}

	/**
	 * Creates a mock WebRequest given list of parameters and a list of POST'ed files.
	 * 
	 * @param parameters a list of request parameters
	 * @param files a list of POST'ed files
	 */
	public WebRequest(Map<String, String[]> parameters, Map<String, List<UploadedFile>> files) {
		super(WtServlet.getServletApi().getMockupHttpServletRequest());
		parameters_ = parameters;
		files_ = files;
	}
	
	/**
	 * Returns the request method.
	 */
	public String getRequestMethod() {
		return this.getMethod();
	}

	/**
	 * Returns the script name.
	 * <p>
	 * This returns in principle {@link #getContextPath()} + {@link #getServletPath()}, but
	 * with workaround code for corner cases and container workarounds.
	 * 
	 * @return the url at which the application is deployed
	 */
	public String getScriptName() {
		String result = getContextPath() + getServletPath();
		if (!result.startsWith("/"))
			result = "/" + result;

		// Jetty will auto-redirect in this case to .../
		// I am not sure if this is according to the servlet spec ?
		if (getServletPath().length() == 0 && !result.endsWith("/"))
			result += "/"; 

		return result;
	}

	/**
	 * Returns a header value.
	 * <p>
	 * Returns the corresponding header value, using {@link #getHeader(String)}, or
	 * the empty string (""), if the header is not present.
	 * 
	 * @param header the header name
	 * @return the header value, or an empty string if the header is not present.
	 */
	public String getHeaderValue(String header) {
		String result = getHeader(header);
		return result == null ? "" : result;
	}

	/**
	 * Returns the internal path information.
	 * <p>
	 * Returns the {@link HttpServletRequestWrapper#getPathInfo()} or the empty string
	 * if there is no internal path in the request. This method also uses workarounds
	 * for corner cases for some servlet containers.
	 * 
	 * @return the internal path information, or an empty string if there is no internal path.
	 */
	public String getPathInfo() {
		String result = super.getPathInfo();
		
		// Jetty will report "/" as an internal path. Which totally makes no sense but is according
		// to the spec
		if (getServletPath().length() == 0)
			if (result != null && result.equals("/"))
				return "";

		return result == null ? "" : result;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private void parse(final ProgressListener progressUpdate) {
		Map<String, String[]> parameterMap = super.getParameterMap();

		parameters_ = new HashMap<String, String []>(parameterMap);
		files_ = new HashMap<String, List<UploadedFile>>();

		if (FileUploadBase.isMultipartContent(this)) {
			try {
				// Create a factory for disk-based file items
				DiskFileItemFactory factory = new DiskFileItemFactory();

				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);

				if (progressUpdate != null) {
					upload.setProgressListener(new org.apache.commons.fileupload.ProgressListener(){
						@Override
						public void update(long pBytesRead, long pContentLength, int pItems) {
							progressUpdate.update(WebRequest.this, pBytesRead, pContentLength);
						}
					});
				}

				// Parse the request
				List items = upload.parseRequest(this);

				Iterator itr = items.iterator();

				FileItem fi;
				File f = null;
				while (itr.hasNext()) {
					fi = (FileItem) itr.next();

					// Check if not form field so as to only handle the file inputs
					// else condition handles the submit button input
					if (!fi.isFormField()) {
						try {
							f = File.createTempFile("jwt", "jwt");
							fi.write(f);
							fi.delete();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}

						List<UploadedFile> files = files_.get(fi.getFieldName());
						if (files == null) {
							files = new ArrayList<UploadedFile>();
							files_.put(fi.getFieldName(), files);
						}
						files.add(new UploadedFile(f.getAbsolutePath(), fi.getName(), fi.getContentType()));
					} else {
						String[] v = parameters_.get(fi.getFieldName());
						if (v == null)
							v = new String[1];
						else {
							String[] newv = new String[v.length + 1];
							for (int i = 0; i < v.length; ++i)
								newv[i] = v[i];
							v = newv;
						}
						v[v.length - 1] = fi.getString();
						parameters_.put(fi.getFieldName(), v);
					}
				}
			} catch (FileUploadException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the list of uploaded files.
	 * 
	 * @return the list of uploaded files.
	 */
	public Map<String, List<UploadedFile>> getUploadedFiles() {
		return files_;
	}

	/**
	 * Returns the parameter map.
	 * <p>
	 * The parameter map includes both the parameters from the query string, as well
	 * as parameters posted in the body.
	 */
	public Map<String, String[]> getParameterMap() {
		return parameters_;
	}

	/**
	 * Returns the parameter values for a parameter.
	 * <p>
	 * Returns an array of parameters values given for a particular parameter. When
	 * no parameter value was assigned to the parameter, an empty array is returned.
	 * 
	 * @see #getParameterMap()
	 */
	@Override
	public String[] getParameterValues(String name) {
		if (parameters_.containsKey(name))
			return parameters_.get(name);
		else
			return new String[0];
	}

	/**
	 * Returns the parameter value for a parameter's name.
	 * 
	 * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter(String name) {
		if (parameters_.containsKey(name)) {
			String[] paramList = parameters_.get(name);
			if (paramList.length > 0 && paramList[0] != null) 
				return paramList[0];
		}
		
		return null;
	}
}
