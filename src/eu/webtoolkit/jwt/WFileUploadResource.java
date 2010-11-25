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

class WFileUploadResource extends WResource {
	public WFileUploadResource(WFileUpload fileUpload) {
		super(fileUpload);
		this.fileUpload_ = fileUpload;
	}

	protected void handleRequest(WebRequest request, WebResponse response)
			throws IOException {
		boolean triggerUpdate = false;
		List<UploadedFile> files = new ArrayList<UploadedFile>();
		CollectionUtils.findInMultimap(request.getUploadedFiles(), "data",
				files);
		if (!(0 != 0)) {
			if (!files.isEmpty() || request.getParameter("data") != null) {
				triggerUpdate = true;
			}
		}
		response.setContentType("text/html; charset=utf-8");
		response.addHeader("Expires", "Sun, 14 Jun 2020 00:00:00 GMT");
		response.addHeader("Cache-Control", "max-age=315360000");
		Writer o = response.out();
		o
				.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html lang=\"en\" dir=\"ltr\">\n<head><title></title>\n<script type=\"text/javascript\">\nfunction load() { ");
		if (triggerUpdate || 0 != 0) {
			o.append("if (window.parent.").append(
					WApplication.getInstance().getJavaScriptClass()).append(
					") ");
			if (triggerUpdate) {
				o.append("window.parent.").append(
						WApplication.getInstance().getJavaScriptClass())
						.append("._p_.update(null, '").append(
								this.fileUpload_.uploaded().encodeCmd())
						.append("', null, true);");
			} else {
				if (0 != 0) {
					o.append("window.parent.").append(
							WApplication.getInstance().getJavaScriptClass())
							.append("._p_.update(null, '").append(
									this.fileUpload_.fileTooLargeImpl()
											.encodeCmd()).append(
									"', null, true);");
				}
			}
		}
		o
				.append("}\n</script></head><body onload=\"load();\"style=\"margin:0;padding:0;\">");
		o.append("</body></html>");
		if (0 != 0) {
			this.fileUpload_.tooLargeSize_ = 0;
		} else {
			if (!files.isEmpty()) {
				this.fileUpload_.setFiles(files);
			}
		}
	}

	private WFileUpload fileUpload_;
	private static UploadedFile uploaded;
}
