package eu.webtoolkit.jwt.examples.planner.captcha;

import java.util.Random;

import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPointF;
import eu.webtoolkit.jwt.WText;

/**
 * A captcha using different shapes with a particular color, 
 * the user is required to select a specific shape visible in the 
 * widget as annotated in the captcha's text field
 * 
 * @author pieter
 */
public class ShapesCaptchaWidget extends WContainerWidget {
	private Signal completed = new Signal();
	private WText captchaMessage;
	private ShapesWidget shapesWidget;

	public ShapesCaptchaWidget(WContainerWidget parent, final int width, final int height) {
		super(parent);

		// set the css style class
		setStyleClass("captcha");

		captchaMessage = new WText(this);

		// construct the ShapesWidget, this widget will render all shapes
		shapesWidget = new ShapesWidget(this);
		shapesWidget.resize(width, height);

		// connect an anonymous listener to the shapesWidget's clicked() signal
		// when the user clicked on the required shape, the completed signal is triggered
		shapesWidget.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
			public void trigger(WMouseEvent me) {
				if (shapesWidget.correctlyClicked(me)) 
					completed.trigger();
				else
					regenerate();
			}
		});
		
		regenerate();
	}
	
	private void regenerate() {
		// initialize the ShapesWidget
		shapesWidget.initShapes();
		shapesWidget.update();
		
		// set the text, so the user knows which shape requires his selection
		captchaMessage.setText(tr("captcha.message")
				.arg(shapesWidget.getSelectedColor())
				.arg(shapesWidget.getSelectedShape()));
	}
	
	public Signal completed() {
		return completed;
	}
}
