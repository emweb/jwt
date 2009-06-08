package eu.webtoolkit.jwt.examples.mandelbrot;

import java.io.IOException;

import eu.webtoolkit.jwt.WResource;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

public class MandelbrotResource extends WResource {
	public MandelbrotResource(MandelbrotImage img, long x, long y, int w, int h) {
		img_ = img;
		x_ = x;
		y_ = y;
		w_ = w;
		h_ = h;
	}

	public String resourceMimeType() {
		return "image/png";
	}

	protected void handleRequest(WebRequest request, WebResponse response) {
		try {
			img_.generate(x_, y_, w_, h_, response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private MandelbrotImage img_;
	long x_, y_;
	int w_, h_;
}
