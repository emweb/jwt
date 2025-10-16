/*
 * Copyright (C) 2017 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import eu.webtoolkit.jwt.WPoint;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageUtils {
	private static Logger logger = LoggerFactory.getLogger(ImageUtils.class);

	public static WPoint getSize(final List<Byte> data) {
		try {
			BufferedImage image = ImageIO.read(getByteArrayInputStream(data));
			return new WPoint(image.getWidth(), image.getHeight());
		} catch (java.io.IOException e) {
			logger.error("An error occurred while attempting to get the size of an image", e);
			return new WPoint();
		}
	}

	public static WPoint getSize(final String fileName) {
		try {
			BufferedImage image = ImageIO.read(new File(fileName));
			return new WPoint(image.getWidth(), image.getHeight());
		} catch (java.io.IOException e) {
			logger.error("An error occurred while attempting to get the size of image '" +
					fileName + "'", e);
			return new WPoint();
		}
	}

	public static ByteArrayInputStream getByteArrayInputStream(final List<Byte> data) {
		byte[] arr = new byte[data.size()];
		for (int i = 0; i < data.size(); ++i)
			arr[i] = data.get(i);
		return new ByteArrayInputStream(arr);
	}
}
