/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.servlet;

import java.io.File;

/**
 * Details about a file uploaded with a (POST) request
 */
public class UploadedFile {
	UploadedFile(String spoolName, String clientFileName, String contentType) {
		this.fileInfo_ = null;
		this.fileInfo_ = new UploadedFile.Impl();
		this.fileInfo_.spoolFileName = spoolName;
		this.fileInfo_.clientFileName = clientFileName;
		this.fileInfo_.contentType = contentType;
		this.fileInfo_.isStolen = false;
	}

	/**
	 * Return the spool file name.
	 * <p>
	 * This is the location on the local (server) filesystem where the uploaded
	 * file is temporarily stored. Unless you call
	 * {@link UploadedFile#stealSpoolFile() stealSpoolFile()}, this file is
	 * deleted automatically.
	 */
	public String getSpoolFileName() {
		return this.fileInfo_.spoolFileName;
	}

	/**
	 * Returns the client file name.
	 * <p>
	 * This is the location that was indicated by the browser. Depending on the
	 * browser this is an absolute path or only the file name.
	 */
	public String getClientFileName() {
		return this.fileInfo_.clientFileName;
	}

	/**
	 * Returns the file content type.
	 * <p>
	 * Returns the content mime-type that was sent along with the uploaded file.
	 */
	public String getContentType() {
		return this.fileInfo_.contentType;
	}

	/**
	 * Steals the uploaded spool file.
	 * <p>
	 * By stealing the spooled file, it is no longer automatically deleted by
	 * Wt.
	 */
	public void stealSpoolFile() {
		this.fileInfo_.isStolen = true;
	}

	private static class Impl {
		public String spoolFileName;
		public String clientFileName;
		public String contentType;
		public boolean isStolen;

		public void finalize() {
			if (!this.isStolen) {
				new File(this.spoolFileName).delete();
			}
		}
	}

	private UploadedFile.Impl fileInfo_;
}
