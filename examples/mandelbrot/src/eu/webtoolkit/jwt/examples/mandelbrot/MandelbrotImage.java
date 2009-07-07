/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.mandelbrot;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WResource;
import eu.webtoolkit.jwt.WVirtualImage;

class MandelbrotImage extends WVirtualImage {
	public MandelbrotImage(int width, int height, long virtualWidth, long virtualHeight, double bx1, double by1, double bx2, double by2, WContainerWidget parent) {
		super(width, height, virtualWidth, virtualHeight, 256, parent);
		bx1_ = bx1;
		by1_ = by1;
		bwidth_ = bx2 - bx1;
		bheight_ = by2 - by1;
		maxDepth_ = 50;
		bailOut2_ = 30 * 30;
		enableDragging();
		redrawAll();
		scroll(width * 2, virtualHeight / 2 - height);
	}

	public void zoomIn() {
		resizeImage(getImageWidth() * 2, getImageHeight() * 2);

		scrollTo(getCurrentTopLeftX() * 2 + getViewPortWidth() / 2, getCurrentTopLeftY() * 2 + getViewPortHeight() / 2);

	}

	public void zoomOut() {
		scrollTo(getCurrentTopLeftX() / 2 - getViewPortWidth() / 4, getCurrentTopLeftY() / 2 - getViewPortHeight() / 4);

		resizeImage(Math.max((long) getViewPortWidth(), getImageWidth() / 2), Math.max((long) getViewPortHeight(), getImageHeight() / 2));
	}

	public void generate(long x, long y, int w, int h, OutputStream out) {
		BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		System.err.println("rendering: (" + x + "," + y + ") (" + (x + w) + "," + (y + h) + ")");

		for (int i = 0; i < w; ++i)
			for (int j = 0; j < h; ++j) {
				double bx = convertPixelX(x + i);
				double by = convertPixelY(y + j);
				double d = calcPixel(bx, by);

				int lowr = 100;

				int r, g, b;
				if (d == maxDepth_)
					r = g = b = 0;
				else {
					r = lowr + (int) ((d * (255 - lowr)) / maxDepth_);
					g = 0 + (int) ((d * 255) / maxDepth_);
					b = 0;
				}

				int rgb_value = ((255 & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);

				im.setRGB(i, j, rgb_value);
			}

		try {
			ImageIO.write(im, "png", out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double currentX1() {
		return convertPixelX(getCurrentTopLeftX());
	}

	public double currentY1() {
		return convertPixelY(getCurrentTopLeftY());
	}

	public double currentX2() {
		return convertPixelX(getCurrentBottomRightX());
	}

	public double currentY2() {
		return convertPixelY(getCurrentBottomRightY());
	}

	private double bx1_, by1_, bwidth_, bheight_;
	private int maxDepth_;
	private double bailOut2_;

	protected WResource render(long x, long y, int w, int h) {
		return new MandelbrotResource(this, x, y, w, h);
	}

	private double calcPixel(double x, double y) {
		double x1 = x;
		double y1 = y;

		for (int i = 0; i < maxDepth_; ++i) {
			double xs = x1 * x1;
			double ys = y1 * y1;
			double x2 = xs - ys + x;
			double y2 = x1 * y1 * 2 + y;
			x1 = x2;
			y1 = y2;

			double z = xs + ys;

			if (xs + ys > bailOut2_)
				return (double) i + 1 - Math.log(Math.log(Math.sqrt(z))) / Math.log(2.0);
		}

		return maxDepth_;
	}

	private double convertPixelX(long x) {
		return bx1_ + ((double) (x) / getImageWidth() * bwidth_);
	}

	private double convertPixelY(long y) {
		return by1_ + ((double) (y) / getImageHeight() * bheight_);
	}
}
