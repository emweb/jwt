/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import org.slf4j.Logger;

public class StreamUtils {
	public static String readFile(String fname) {
		FileInputStream s = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			s = new FileInputStream(fname);
			copy(s, baos);
			return baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not read file: " + fname, e);
		} catch (IOException e) {
			throw new RuntimeException("Could not read file: " + fname, e);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					// Ignore that
				}
			}
		}
	}
	
	public static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = is.read(buffer))) {
			os.write(buffer, 0, n);
		}
	}

	public static void closeQuietly(InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (IOException ioe) {
			// shhhht!
		}
	}

	public static class ErrorSuppressingOutputStream extends ServletOutputStream {
		public ErrorSuppressingOutputStream() {
			this.valid = false;
		}

		public ErrorSuppressingOutputStream(ServletOutputStream stream, Logger logger) {
			this.stream = stream;
			this.logger = logger;
		}

		@Override
		public void write(int b) {
			if (!valid)
				return;
			try {
				stream.write(b);
			} catch (IOException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("IOException occurred while writing to ServletOutputStream", e);
				} else {
					logger.info("IOException occurred while writing to ServletOutputStream: {}, " +
							"enable debug logging for more details", e.getMessage());
				}
				valid = false;
			}
		}

		@Override
		public void write(byte[] b) {
			if (!valid)
				return;
			try {
				stream.write(b);
			} catch (IOException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("IOException occurred while writing to ServletOutputStream", e);
				} else {
					logger.info("IOException occurred while writing to ServletOutputStream: {}, " +
							"enable debug logging for more details", e.getMessage());
				}
				valid = false;
			}
		}

		@Override
		public void write(byte[] b, int off, int len) {
			if (!valid)
				return;
			try {
				stream.write(b, off, len);
			} catch (IOException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("IOException occurred while writing to ServletOutputStream", e);
				} else {
					logger.info("IOException occurred while writing to ServletOutputStream: {}, " +
							"enable debug logging for more details", e.getMessage());
				}
				valid = false;
			}
		}

		@Override
		public void flush() {
			if (!valid)
				return;
			try {
				stream.flush();
			} catch (IOException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("IOException occurred while flushing ServletOutputStream", e);
				} else {
					logger.info("IOException occurred while flushing ServletOutputStream: {}, " +
							"enable debug logging for more details", e.getMessage());
				}
				valid = false;
			}
		}

		@Override
		public void close() {
			if (!valid)
				return;
			try {
				stream.close();
			} catch (IOException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("IOException occurred when closing ServletOutputStream", e);
				} else {
					logger.info("IOException occurred when closing ServletOutputStream: {}, " +
							"enable debug logging for more details", e.getMessage());
				}
				valid = false;
			}
		}

		@Override
		public boolean isReady() {
			if (!valid) {
				// If not valid, we will just discard everything that is written,
				// so we say we're "ready"
				return true;
			}
			return stream.isReady();
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			if (stream != null) {
				stream.setWriteListener(writeListener);
			}
		}

		private boolean valid = true;
		private ServletOutputStream stream;
		private Logger logger;
	}
}
