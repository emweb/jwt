/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
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
        img_.generate(x_, y_, w_, h_, response.getOutputStream());
    }

    private MandelbrotImage img_;
    long x_, y_;
    int w_, h_;
}
