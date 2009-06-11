package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A more obvious loading indicator that grays the window
 * 
 * 
 * This loading indicator uses a gray semi-transparent overlay to darken the
 * window contents, and centers a loading icon (with some text).
 * <p>
 * Usage example:
 * <p>
 * <div align="center"> <img src="/WOverlayLoadingIndicator.png"
 * alt="The overlay loading indicator">
 * <p>
 * <strong>The overlay loading indicator</strong>
 * </p>
 * </div>
 * <p>
 * <p>
 * <i><b>Note:</b>For this loading indicator to render properly in IE, you need
 * to reset the &quot;body&quot; margin to 0. Using the inline stylesheet, this
 * could be done using:</i>
 * </p>
 * 
 * @see WApplication#setLoadingIndicator(WLoadingIndicator indicator)
 */
public class WOverlayLoadingIndicator extends WContainerWidget implements
		WLoadingIndicator {
	/**
	 * Construct the loading indicator.
	 * 
	 * @param styleClass
	 *            the style class for the central box }
	 * @param backgroundStyleClass
	 *            the style class for the &quot;background&quot; part of the
	 *            indicator }
	 * @param textStyleClass
	 *            the style class for the text that is displayed}
	 *            <p>
	 *            <i><b>Note:</b>if styleClass is not set, the central box gets
	 *            the CSS style elements <code>
               background: white; <br> 
               border: 3px solid #333333; <br> 
               z-index: 10001; visibility: visible; <br> 
               position: absolute; left: 50%; top: 50%; <br> 
               margin-left: -50px; margin-top: -40px; <br> 
               width: 100px; height: 80px; <br> 
               font-family: arial,sans-serif; <br> 
               text-align: center
  </code>
	 *            <p>
	 *            if backgroundStyleClass is not set, the background gets the
	 *            CSS style elements <code>
               background: #DDDDDD; <br> 
               height: 100%; width: 100%; <br> 
               top: 0px; left: 0px; <br> 
               z-index: 10000; <br> 
               -moz-background-clip: -moz-initial; <br> 
               -moz-background-origin: -moz-initial; <br> 
               -moz-background-inline-policy: -moz-initial; <br> 
               opacity: 0.5; filter: alpha(opacity=50); -moz-opacity:0.5; <br> 
               position: absolute;
  </code> </i>
	 *            </p>
	 */
	public WOverlayLoadingIndicator(String styleClass,
			String backgroundStyleClass, String textStyleClass) {
		super();
		this.setInline(false);
		WApplication app = WApplication.getInstance();
		this.cover_ = new WContainerWidget(this);
		this.center_ = new WContainerWidget(this);
		WImage img = new WImage(WApplication.getResourcesUrl()
				+ "ajax-loading.gif", this.center_);
		img.setMargin(new WLength(7), EnumSet.of(Side.Top, Side.Bottom));
		this.text_ = new WText("Loading...", this.center_);
		this.text_.setInline(false);
		this.text_.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		if (styleClass.length() != 0) {
			this.center_.setStyleClass(styleClass);
		}
		if (textStyleClass.length() != 0) {
			this.text_.setStyleClass(textStyleClass);
		}
		if (backgroundStyleClass.length() != 0) {
			this.cover_.setStyleClass(backgroundStyleClass);
		}
		if (app.getEnvironment().agentIsIE()) {
			app.getStyleSheet().addRule("body", "height: 100%; margin: 0;");
		}
		if (backgroundStyleClass.length() == 0) {
			app
					.getStyleSheet()
					.addRule(
							"div#" + this.cover_.getId(),
							""
									+ "background: #DDDDDD;height: 100%; width: 100%;top: 0px; left: 0px;opacity: 0.5; position: absolute;-khtml-opacity: 0.5;z-index: 10000;"
									+ (app.getEnvironment().agentIsIE() ? "filter: alpha(opacity=50);"
											: "-moz-opacity:0.5;-moz-background-clip: -moz-initial;-moz-background-origin: -moz-initial;-moz-background-inline-policy: -moz-initial;"));
		}
		if (styleClass.length() == 0) {
			app
					.getStyleSheet()
					.addRule(
							"div#" + this.center_.getId(),
							"background: white;border: 3px solid #333333;z-index: 10001; visibility: visible;position: absolute; left: 50%; top: 50%;margin-left: -50px; margin-top: -40px;width: 100px; height: 80px;font-family: arial,sans-serif;text-align: center");
		}
	}

	public WOverlayLoadingIndicator() {
		this("", "", "");
	}

	public WOverlayLoadingIndicator(String styleClass) {
		this(styleClass, "", "");
	}

	public WOverlayLoadingIndicator(String styleClass,
			String backgroundStyleClass) {
		this(styleClass, backgroundStyleClass, "");
	}

	public WWidget getWidget() {
		return this;
	}

	public void setMessage(CharSequence text) {
		this.text_.setText(text);
	}

	private WContainerWidget cover_;
	private WContainerWidget center_;
	private WText text_;
}
