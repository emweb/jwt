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

	public static String identifyMimeType(List<Byte> header) {
		for (int i = 0; i < mimeTypeCount; ++i) {
			if (Utils.memcmp(header, imageHeaders[i], imageHeaderSize[i]) == 0) {
				return imageMimeTypes[i];
			}
		}
		return "";
	}

	public static String identifyMimeType(String fileName) {
		List<Byte> header = FileUtils.fileHeader(fileName, 25);
		if (header.isEmpty()) {
			return "";
		} else {
			return identifyMimeType(header);
		}
	}

	public static WPoint getSize(String fileName) {
		List<Byte> header = FileUtils.fileHeader(fileName, 25);
		if (header.isEmpty()) {
			return new WPoint();
		} else {
			return getSize(header);
		}
	}

	public static WPoint getSize(List<Byte> header) {
		String mimeType = identifyMimeType(header);
		if (mimeType.equals("image/png")) {
			int width = (((int) header.get(16) << 8 | (int) header.get(17)) << 8 | (int) header
					.get(18)) << 8
					| (int) header.get(19);
			int height = (((int) header.get(20) << 8 | (int) header.get(21)) << 8 | (int) header
					.get(22)) << 8
					| (int) header.get(23);
			return new WPoint(width, height);
		} else {
			if (mimeType.equals("image/gif")) {
				int width = (int) header.get(7) << 8 | (int) header.get(6);
				int height = (int) header.get(9) << 8 | (int) header.get(8);
				return new WPoint(width, height);
			} else {
				return new WPoint();
			}
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
}
