package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import eu.webtoolkit.jwt.servlet.UploadedFile;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

public abstract class WResource extends WObject {
	private Signal dataChanged_ = null;
	private String suggestedFileName_;

	public WResource(WObject parent) {
		super(parent);
		suggestedFileName_ = "";
	}

	public WResource() {
		this(null);
	}

	public String generateUrl() {
		WApplication app = WApplication.instance();

		return app.addExposedResource(this);
	}

	protected void beingDeleted() {
	}

	abstract protected void handleRequest(WebRequest request, WebResponse response) throws IOException;

	public void handle(WebRequest request, WebResponse response) throws IOException {
		handleRequest(request, response);
		response.flush();
	}

	public Signal dataChanged() {
		if (dataChanged_ == null)
			dataChanged_ = new Signal(this);
		return dataChanged_;
	}

	public void write(OutputStream out, Map<String, List<String>> parameterMap, Map<String, UploadedFile> uploadedFiles) throws IOException {
		WebRequest request = new WebRequest(parameterMap, uploadedFiles); // XXX
		WebResponse response = new WebResponse(out); // XXX
		handleRequest(request, response);
	}

	public void suggestFileName(String name) {
		suggestedFileName_ = name;
	}

	public String getSuggestedFileName() {
		return suggestedFileName_;
	}

}
