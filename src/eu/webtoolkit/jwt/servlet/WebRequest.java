package eu.webtoolkit.jwt.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public class WebRequest extends HttpServletRequestWrapper {

	public static ArrayList<String> emptyValues_ = new ArrayList<String>();

	private Map<String, List<String>> parameters_;
	private Map<String, UploadedFile> files_;

	public WebRequest(HttpServletRequest request) {
		super(request);
		parse();
	}

	public WebRequest(Map<String, List<String>> parameters, Map<String, UploadedFile> files) {
		super(null);
		parameters_ = parameters;
		files_ = files;
	}

	public String getScriptName() {
		return getContextPath() + getServletPath();
	}

	public String getHeaderValue(String header) {
		String result = getHeader(header);
		return result == null ? "" : result;
	}

	public String getPathInfo() {
		String result = getPathInfo();
		return result == null ? "" : result;
	}

	public String getUrlScheme() {
		return getScheme();
	}

	public String getEnvValue(String string) {
		return "";
	}

	@SuppressWarnings("unchecked")
	private void parse() {
		Map<String, String[]> parameterMap = super.getParameterMap();

		parameters_ = new HashMap<String, List<String>>(parameterMap.size());
		files_ = new HashMap<String, UploadedFile>();

		for (String name : parameterMap.keySet())
			parameters_.put(name, new ArrayList<String>(Arrays.asList(parameterMap.get(name))));

		if (FileUploadBase.isMultipartContent(this)) {
			try {
				// Create a factory for disk-based file items
				DiskFileItemFactory factory = new DiskFileItemFactory();

				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);

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

						files_.put(fi.getFieldName(), new UploadedFile(f.getAbsolutePath(), fi.getName(), fi.getContentType()));
					} else {
						List<String> v = parameters_.get(fi.getFieldName());
						if (v == null)
							v = new ArrayList<String>(1);
						v.add(fi.getString());
						parameters_.put(fi.getFieldName(), v);
					}
				}
			} catch (FileUploadException e) {
				e.printStackTrace();
			}
		}
	}

	public int getPostDataExceeded() {
		return 0;
	}

	public int tooLarge() {
		return getPostDataExceeded();
	}

	public Map<String, UploadedFile> getUploadedFiles() {
		return files_;
	}

	public Map<String, List<String>> getParameterMap() {
		return parameters_;
	}

	@Override
	public String[] getParameterValues(String name) {
		if (parameters_.containsKey(name))
			return parameters_.get(name).toArray(a);
		else
			return new String[0];
	}
	
	private String[] a = new String[0];
}
