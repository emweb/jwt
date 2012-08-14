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

	static final int mimeTypeCount = 10;
	static String[] imageMimeTypes = { "image/png", "image/jpeg", "image/gif",
			"image/gif", "image/bmp", "image/bmp", "image/bmp", "image/bmp",
			"image/bmp", "image/bmp" };
	static String[] imageHeaders = { "\211PNG\r\n\032\n", "\377\330\377",
			"GIF87a", "GIF89a", "BA", "BM", "CI", "CP", "IC", "PI" };
	static int[] imageHeaderSize = { 8, 3, 6, 6, 2, 2, 2, 2, 2, 2 };

	public static String identifyImageFileMimeType(String fileName) {
		List<Integer> header = FileUtils.fileHeader(fileName, 25);
		if (header.size() == 0) {
			return "";
		}
		return identifyImageMimeType(header);
	}

	public static String identifyImageMimeType(List<Integer> header) {
		for (int i = 0; i < mimeTypeCount; ++i) {
			if (Utils.memcmp(header, imageHeaders[i], imageHeaderSize[i]) == 0) {
				return imageMimeTypes[i];
			}
		}
		return "";
	}
}
