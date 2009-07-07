/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.IOException;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

/**
 * A resource which streams data from memory.
 * <p>
 * This is suitable for relatively small resources, which still require some
 * computation.
 * <p>
 * If creating the data requires computation which you would like to
 * post-pone until the resource is served, then you may want to
 * directly reimplement {@link WResource} instead and compute the data on the
 * fly while streaming.
 */
public class WMemoryResource extends WResource {
	/**
	 * Create a new resource.
	 * <p>
	 * You must call {@link #setMimeType(String)} and {@link #setData(byte[])} before using the resource.
	 */
	public WMemoryResource() {
		this("", (WObject) null);
	}

	WMemoryResource(String mimeType, WObject parent) {
		super(parent);
		this.data_ = null;
		this.mimeType_ = mimeType;
	}

	/**
	 * Create a new resource.
	 * <p>
	 * You must call {@link #setData(byte[])} before using the resource.
	 */
	public WMemoryResource(String mimeType) {
		this(mimeType, (WObject) null);
	}

	/**
	 * Set data.
	 * 
	 * The data is specified as bytes. If you want to set character data,
	 * you will need to encode the characters into bytes using the appropriate
	 * encoding.
	 * 
	 * @param data
	 */
	public void setData(byte[] data) {
		this.data_ = data;
		this.dataChanged().trigger();
	}

	void setData(char[] data) {
		this.data_ = new byte[data.length];
		for (int i = 0; i < data.length; ++i)
			data_[i] = (byte) data[i];
	}
	
	/**
	 * Returns the mime type.
	 * 
	 * @return the mime type.
	 */
	public String getMimeType() {
		return this.mimeType_;
	}

	/**
	 * Changes the mime type.
	 * <p>
	 * This causes the resource to be refreshed in the browser by triggering {@link #dataChanged()}.
	 * 
	 * @param mimeType
	 */
	public void setMimeType(String mimeType) {
		this.mimeType_ = mimeType;
		this.dataChanged().trigger();
	}

	private String mimeType_;
	private byte[] data_;

	/**
	 * Returns the data.
	 * 
	 * @return the data.
	 */
	public byte[] getData() {
		return data_;
	}

	@Override
	protected void handleRequest(WebRequest request, WebResponse response) throws IOException {
		response.setContentType(mimeType_);
		response.getOutputStream().write(data_);
	}
}
