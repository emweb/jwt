/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;
import eu.webtoolkit.jwt.utils.StreamUtils;

/**
 * A resource which streams data from a local file.
 * <p>
 * To update the resource, either use setFileName() to point it to a
 * new file, or trigger the {@link #dataChanged()} signal when only the
 * file contents has changed, but not the filename.
 */
public class WFileResource extends WResource {
	private static final Logger logger = LoggerFactory.getLogger(WFileResource.class);
	
	/**
	 * Creates a new resource with given mime-type for a file.
	 * 
	 * @param mimeType the mime type of the file.
	 * @param fileName the file name.
	 */
	public WFileResource(String mimeType, String fileName) {
		this.mimeType_ = mimeType;
		this.fileName_ = fileName;
	}
	
	/**
	 * Changes the file name.
	 * <p>
	 * This causes the resource to be refreshed in the browser by triggering {@link #dataChanged()}.
	 * 
	 * @param fileName the new filename.
	 */
	public void setFileName(String fileName) {
		boolean replacing = (fileName_ != null && fileName_.length() > 0);

		this.fileName_ = fileName;

		if (replacing)
			setChanged();
	}

	/**
	 * Returns the file name.
	 * 
	 * @return the file name.
	 */
	public String getFileName() {
		return this.fileName_;
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
		
		setChanged();
	}

	private String mimeType_;
	private String fileName_;

	@Override
	public void handleRequest(WebRequest request, WebResponse response) {
		response.setContentType(mimeType_);

		try {
			File f = new File(fileName_);
			FileInputStream fis = new FileInputStream(f);
			try {
				StreamUtils.copy(fis, response.getOutputStream());
				response.getOutputStream().flush();
			} catch (IOException e) {
				logger.info("IOException, {}", fileName_, e);
			} finally {
				StreamUtils.closeQuietly(fis);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file: " + fileName_);
			response.setStatus(404);
		}
	}
}
