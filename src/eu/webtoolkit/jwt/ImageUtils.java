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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImageUtils {
	private static Logger logger = LoggerFactory.getLogger(ImageUtils.class);

	public static String identifyMimeType(final List<Byte> header) {
		for (int i = 0; i < mimeTypeCount; ++i) {
			if (Utils.memcmp(header, imageHeaders[i], imageHeaderSize[i]) == 0) {
				return imageMimeTypes[i];
			}
		}
		return "";
	}

	public static String identifyMimeType(final String fileName) {
		List<Byte> header = FileUtils.fileHeader(fileName, 25);
		if (header.isEmpty()) {
			return "";
		} else {
			return identifyMimeType(header);
		}
	}

	private static final int mimeTypeCount = 10;
	private static String[] imageMimeTypes = { "image/png", "image/jpeg",
			"image/gif", "image/gif", "image/bmp", "image/bmp", "image/bmp",
			"image/bmp", "image/bmp", "image/bmp" };
	private static String[] imageHeaders = { "\211PNG\r\n\032\n",
			"\377\330\377", "GIF87a", "GIF89a", "BA", "BM", "CI", "CP", "IC",
			"PI" };
	private static int[] imageHeaderSize = { 8, 3, 6, 6, 2, 2, 2, 2, 2, 2 };

	static int toUnsigned(int c) {
		int result = c;
		if (result < 0) {
			result += 256;
		}
		return result;
	}
}
