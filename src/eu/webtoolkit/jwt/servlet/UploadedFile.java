/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.servlet;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Details about a file uploaded with a request to a resource.
 * <p>
 * 
 * @see WResource#handleRequest(WebRequest request, WebResponse response)
 */
public class UploadedFile {
	private static Logger logger = LoggerFactory.getLogger(UploadedFile.class);

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
	 * file is temporarily stored. Unless you call {@link }, this file is deleted
	 * automatically.
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

	static class Impl {
		private static Logger logger = LoggerFactory.getLogger(Impl.class);

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
	static StringWriter emptyStream = new StringWriter();
}
