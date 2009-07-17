/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.Writer;
import eu.webtoolkit.jwt.servlet.UploadedFile;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

class WFileUploadResource extends WResource {
	public WFileUploadResource(WFileUpload fileUpload) {
		super(fileUpload);
		this.fileUpload_ = fileUpload;
	}

	protected void handleRequest(WebRequest request, WebResponse response)
			throws IOException {
		boolean triggerUpdate = false;
		UploadedFile p = null;
		if (!(request.tooLarge() != 0)) {
			UploadedFile i = request.getUploadedFiles().get("data");
			if (i != null) {
				p = i;
				triggerUpdate = true;
			}
		}
		response.setContentType("text/html; charset=utf-8");
		response.addHeader("Expires", "Sun, 14 Jun 2020 00:00:00 GMT");
		response.addHeader("Cache-Control", "max-age=315360000");
		Writer o = response.out();
		o
				.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html lang=\"en\" dir=\"ltr\">\n<head><title></title>\n<script type=\"text/javascript\">\nfunction load() { ");
		if (triggerUpdate) {
			o.append("window.parent.").append(
					WApplication.getInstance().getJavaScriptClass()).append(
					"._p_.update(null, '").append(
					this.fileUpload_.uploaded().encodeCmd()).append(
					"', null, true);");
		}
		o
				.append("}\n</script></head><body onload=\"load();\"style=\"margin:0;padding:0;\">");
		o.append("</body></html>");
		if (request.tooLarge() != 0) {
			this.fileUpload_.requestTooLarge(request.tooLarge());
		} else {
			if (p != null) {
				this.fileUpload_.setFormData(p);
			}
		}
	}

	private WFileUpload fileUpload_;
}
