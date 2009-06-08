package eu.webtoolkit.jwt;

import java.io.IOException;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

public class WMemoryResource extends WResource {

	public WMemoryResource(WObject parent) {
		super(parent);
		this.mimeType_ = "";
		this.data_ = null;
	}

	public WMemoryResource() {
		this((WObject) null);
	}

	public WMemoryResource(String mimeType, WObject parent) {
		this(parent);
		this.mimeType_ = mimeType;
	}

	public WMemoryResource(String mimeType) {
		this(mimeType, (WObject) null);
	}

	public void setData(byte[] data) {
		this.data_ = data;
		this.dataChanged().trigger();
	}

	public void setData(char[] data) {
		this.data_ = new byte[data.length];
		for (int i = 0; i < data.length; ++i)
			data_[i] = (byte) data[i];
	}
	
	public String getMimeType() {
		return this.mimeType_;
	}

	public void setMimeType(String mimeType) {
		this.mimeType_ = mimeType;
		this.dataChanged().trigger();
	}

	private String mimeType_;
	private byte[] data_;

	public byte[] getData() {
		return data_;
	}

	@Override
	protected void handleRequest(WebRequest request, WebResponse response) throws IOException {
		response.setContentType(mimeType_);
		response.getOutputStream().write(data_);
	}
}
