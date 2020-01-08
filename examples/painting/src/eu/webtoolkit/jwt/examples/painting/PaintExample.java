/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.painting;

import java.util.EnumSet;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WPaintedWidget;
import eu.webtoolkit.jwt.WSlider;
import eu.webtoolkit.jwt.WText;

public class PaintExample extends WContainerWidget {
    public PaintExample(WContainerWidget root, boolean showTitle) {
        super(root);
        String text = "";
        if (showTitle)
          text += "<h2>Paint example</h2>";

        text += 
          "<p>A simple example demonstrating cross-browser vector graphics."
          + "</p>"
          + "<p>The emweb logo below is painted using the Wt WPainter API from "
          + "bezier paths, and rendered to the browser using inline SVG, inline VML "
          + "or the HTML 5 &lt;canvas&gt; element."
          + "</p>"
          + "<p>"
          + "The example also demonstrates the horizontal and vertical "
          + "<a href=\"http://www.webtoolkit.eu/wt/doc/reference/html/classWt_1_1WSlider.html\" target=\"_blank\">"
          + "WSlider</a> widgets (which are rendered using vector graphics). Here, "
          + "the events of the WSlider widgets are used to scale and rotate the "
          + "emweb logo."
          + "</p>"
          + "<p>"
          + "In non-IE browsers, a different backend is used for positive or negative "
          + "angles (SVG or HTML canvas)."
          + "</p>";

        new WText(text, this);
        WContainerWidget emweb = new WContainerWidget(this);
        emweb.setMargin(new WLength(), EnumSet.of(Side.Left, Side.Right));
        WGridLayout layout = new WGridLayout();
        emweb.setLayout(layout);
        WSlider scaleSlider = new WSlider(Orientation.Horizontal);
        scaleSlider.setMinimum(0);
        scaleSlider.setMaximum(20);
        scaleSlider.setValue(10);
        scaleSlider.setTickInterval(5);
        scaleSlider.setTickPosition(WSlider.TicksBothSides);
        scaleSlider.resize(new WLength(300), new WLength(50));
        scaleSlider.sliderMoved().addListener(this,
                new Signal1.Listener<Integer>() {
                    public void trigger(Integer e1) {
                        PaintExample.this.scaleShape(e1);
                    }
                });
        layout.addWidget(scaleSlider, 0, 1, EnumSet.of(
                AlignmentFlag.AlignCenter, AlignmentFlag.AlignMiddle));
        WSlider rotateSlider = new WSlider(Orientation.Vertical);
        rotateSlider.setMinimum(-30);
        rotateSlider.setMaximum(30);
        rotateSlider.setValue(0);
        rotateSlider.setTickInterval(10);
        rotateSlider.setTickPosition(WSlider.TicksBothSides);
        rotateSlider.resize(new WLength(50), new WLength(400));
        rotateSlider.sliderMoved().addListener(this,
                new Signal1.Listener<Integer>() {
                    public void trigger(Integer e1) {
                        PaintExample.this.rotateShape(e1);
                    }
                });
        layout.addWidget(rotateSlider, 1, 0, EnumSet.of(
                AlignmentFlag.AlignCenter, AlignmentFlag.AlignMiddle));
        this.shapes_ = new ShapesWidget();
        this.shapes_.setAngle(0.0);
        this.shapes_.setRelativeSize(0.5);
        this.shapes_.setPreferredMethod(WPaintedWidget.Method.HtmlCanvas);
        layout.addWidget(this.shapes_, 1, 1, EnumSet.of(
                AlignmentFlag.AlignCenter, AlignmentFlag.AlignMiddle));
    }

    private ShapesWidget shapes_;

    private void rotateShape(int v) {
        this.shapes_.setAngle(v / 2.0);
        this.shapes_
                .setPreferredMethod(v < 0 ? WPaintedWidget.Method.InlineSvgVml
                        : WPaintedWidget.Method.HtmlCanvas);
    }

    private void scaleShape(int v) {
        this.shapes_.setRelativeSize(0.1 + 0.9 * (v / 20.0));
    }
}
