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

	public static WPoint getSize(final String fileName) {
		List<Byte> header = FileUtils.fileHeader(fileName, 25);
		if (header.isEmpty()) {
			return new WPoint();
		} else {
			String mimeType = identifyMimeType(header);
			if (mimeType.equals("image/jpeg")) {
				return getJpegSize(fileName);
			} else {
				return getSize(header);
			}
		}
	}

	public static WPoint getSize(final List<Byte> header) {
		String mimeType = identifyMimeType(header);
		if (mimeType.equals("image/png")) {
			int width = ((toUnsigned(header.get(16)) << 8 | toUnsigned(header
					.get(17))) << 8 | toUnsigned(header.get(18))) << 8
					| toUnsigned(header.get(19));
			int height = ((toUnsigned(header.get(20)) << 8 | toUnsigned(header
					.get(21))) << 8 | toUnsigned(header.get(22))) << 8
					| toUnsigned(header.get(23));
			return new WPoint(width, height);
		} else {
			if (mimeType.equals("image/gif")) {
				int width = toUnsigned(header.get(7)) << 8
						| toUnsigned(header.get(6));
				int height = toUnsigned(header.get(9)) << 8
						| toUnsigned(header.get(8));
				return new WPoint(width, height);
			} else {
				return new WPoint();
			}
		}
	}

	public static WPoint getJpegSize(final String fileName) {
		List<Byte> header = FileUtils.fileHeader(fileName, 1000);
		int pos = 2;
		while (toUnsigned(header.get(pos)) == 0xFF) {
			if (toUnsigned(header.get(pos + 1)) == 0xC0
					|| toUnsigned(header.get(pos + 1)) == 0xC1
					|| toUnsigned(header.get(pos + 1)) == 0xC2
					|| toUnsigned(header.get(pos + 1)) == 0xC3
					|| toUnsigned(header.get(pos + 1)) == 0xC9
					|| toUnsigned(header.get(pos + 1)) == 0xCA
					|| toUnsigned(header.get(pos + 1)) == 0xCB) {
				break;
			}
			pos += 2 + (toUnsigned(header.get(pos + 2)) << 8)
					+ toUnsigned(header.get(pos + 3));
			if (pos + 12 > header.size()) {
				break;
			}
		}
		int height = toUnsigned(header.get(pos + 5) << 8)
				+ toUnsigned(header.get(pos + 6));
		int width = toUnsigned(header.get(pos + 7) << 8)
				+ toUnsigned(header.get(pos + 8));
		return new WPoint(width, height);
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
