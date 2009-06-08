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
 * An abstract interface for a loading indicator
 * 
 * 
 * The loading indicator displays a message while a response from the server is
 * pending.
 * <p>
 * By providing a custom implementation of this interface, you may use a custom
 * loading indicator using
 * {@link WApplication#setLoadingIndicator(WLoadingIndicator indicator)},instead
 * of the default implementation ({@link WDefaultLoadingIndicator}).
 * <p>
 * 
 * @see WApplication#setLoadingIndicator(WLoadingIndicator indicator)
 */
public interface WLoadingIndicator {
	/**
	 * Returns the widget that visually represents the indicator.
	 * 
	 * You should reimplement this method to return a widget that will be shown
	 * to indicate that a response is pending. The widget should be positioned
	 * using CSS.
	 * <p>
	 * The widget will be shown and hidden using {@link WWidget#show()} and
	 * {@link WWidget#hide()}. If you want to customize this behaviour, you
	 * should reimplement the {@link WWidget#setHidden(boolean hidden)} method.
	 * Note that show() and hide() are stateless slots, and thus you need to
	 * make sure that your implementation comforms to that contract, so that it
	 * may be optimized to JavaScript (the server-side implementation will only
	 * be called during stateless slot prelearning).
	 * <p>
	 * <p>
	 * <i><b>Note:</b>The widget will not be added to the
	 * {@link WApplication#getRoot()} container. </i>
	 * </p>
	 */
	public WWidget getWidget();

	/**
	 * Change the message that you want to be displayed.
	 * 
	 * If the indicator is capable of displaying a text message, then you should
	 * reimplement this method to allow this message to be modified.
	 */
	public void setMessage(CharSequence text);
}
