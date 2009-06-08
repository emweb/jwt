package eu.webtoolkit.jwt;

import java.io.FileInputStream;
import java.io.IOException;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;
import eu.webtoolkit.jwt.utils.StreamUtils;

public class WFileResource extends WResource {
	public WFileResource(String mimeType, String fileName) {
		super();
		this.mimeType_ = mimeType;
		this.fileName_ = fileName;
	}

	public void setFileName(String fileName) {
		this.fileName_ = fileName;
		this.dataChanged().trigger();
	}

	public String getFileName() {
		return this.fileName_;
	}

	public String getMimeType() {
		return this.mimeType_;
	}

	public void setMimeType(String mimeType) {
		this.mimeType_ = mimeType;
		this.dataChanged().trigger();
	}

	private String mimeType_;
	private String fileName_;

	@Override
	public void handleRequest(WebRequest request, WebResponse response) {
		response.setContentType(mimeType_);

		try {
			FileInputStream fis = new FileInputStream(fileName_);
			try {
				StreamUtils.copy(fis, response.getOutputStream());
				response.getOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				StreamUtils.closeQuietly(fis);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
