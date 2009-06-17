package eu.webtoolkit.jwt.examples.mandelbrot;

import java.util.EnumSet;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.Signal2;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WTable;
import eu.webtoolkit.jwt.WText;

public class MandelbrotExample extends WContainerWidget {
	public MandelbrotExample(WContainerWidget parent) {
		super(parent);
		new WText(new WString("<h2>Wt Mandelbrot example</h2>" + "<p>The image below is a WVirtualImage that renders the classic Mandelbrot fractal.</p>"
				+ "<p>It is drawn as a grid of many smaller images, computed on the fly, " + "as you scroll around through the virtual image. "
				+ "You can scroll the image using the buttons, " + "or by dragging the mouse.</p>"), this);
		WTable layout = new WTable(this);
		this.mandelbrot_ = new MandelbrotImage(400, 400, 3000, 3000, -2, -1.5, 1, 1.5, layout.getElementAt(0, 0));
		WContainerWidget buttons = new WContainerWidget(layout.getElementAt(0, 0));
		buttons.resize(new WLength(400), new WLength());
		buttons.setContentAlignment(EnumSet.of(AlignmentFlag.AlignCenter));
		new WPushButton(new WString("Left"), buttons).clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				MandelbrotExample.this.moveLeft();
			}
		});
		new WPushButton(new WString("Right"), buttons).clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				MandelbrotExample.this.moveRight();
			}
		});
		new WPushButton(new WString("Up"), buttons).clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				MandelbrotExample.this.moveUp();
			}
		});
		new WPushButton(new WString("Down"), buttons).clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				MandelbrotExample.this.moveDown();
			}
		});
		new WBreak(buttons);
		new WPushButton(new WString("Zoom in"), buttons).clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				MandelbrotExample.this.zoomIn();
			}
		});
		new WPushButton(new WString("Zoom out"), buttons).clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				MandelbrotExample.this.zoomOut();
			}
		});
		this.viewPortText_ = new WText(layout.getElementAt(0, 1));
		layout.getElementAt(0, 1).setPadding(new WLength(10));
		this.updateViewPortText();
		this.mandelbrot_.viewPortChanged().addListener(this, new Signal2.Listener<Long, Long>() {
			public void trigger(Long e1, Long e2) {
				MandelbrotExample.this.updateViewPortText();
			}
		});
	}

	public MandelbrotExample() {
		this((WContainerWidget) null);
	}

	private MandelbrotImage mandelbrot_;
	private WText viewPortText_;

	private void moveLeft() {
		this.mandelbrot_.scroll(-50, 0);
	}

	private void moveRight() {
		this.mandelbrot_.scroll(50, 0);
	}

	private void moveUp() {
		this.mandelbrot_.scroll(0, -50);
	}

	private void moveDown() {
		this.mandelbrot_.scroll(0, 50);
	}

	private void zoomIn() {
		this.mandelbrot_.zoomIn();
	}

	private void zoomOut() {
		this.mandelbrot_.zoomOut();
	}

	private void updateViewPortText() {
		this.viewPortText_.setText(new WString("Current viewport: (" + String.valueOf(this.mandelbrot_.currentX1()) + ","
				+ String.valueOf(this.mandelbrot_.currentY1()) + ") to (" + String.valueOf(this.mandelbrot_.currentX2()) + ","
				+ String.valueOf(this.mandelbrot_.currentY2()) + ")"));
	}
}
