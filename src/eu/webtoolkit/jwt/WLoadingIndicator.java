/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

/**
 * An abstract interface for a loading indicator.
 * <p>
 * 
 * The loading indicator displays a message while a response from the server is
 * pending.
 * <p>
 * By providing a custom implementation of this interface, you may use a custom
 * loading indicator using
 * {@link WApplication#setLoadingIndicator(WLoadingIndicator indicator)
 * WApplication#setLoadingIndicator()},instead of the default implementation (
 * {@link WDefaultLoadingIndicator}).
 * <p>
 * 
 * @see WApplication#setLoadingIndicator(WLoadingIndicator indicator)
 */
public interface WLoadingIndicator {
	/**
	 * Returns the widget that visually represents the indicator.
	 * <p>
	 * You should reimplement this method to return a widget that will be shown
	 * to indicate that a response is pending. The widget should be positioned
	 * using CSS.
	 * <p>
	 * The widget will be shown and hidden using {@link WWidget#show()
	 * WWidget#show()} and {@link WWidget#hide() WWidget#hide()}. If you want to
	 * customize this behaviour, you should reimplement the
	 * {@link WWidget#setHidden(boolean hidden) WWidget#setHidden()} method.
	 * Note that show() and hide() are stateless slots, and thus you need to
	 * make sure that your implementation comforms to that contract, so that it
	 * may be optimized to JavaScript (the server-side implementation will only
	 * be called during stateless slot prelearning).
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The widget will not be added to the
	 * {@link WApplication#getRoot() WApplication#getRoot()} container. </i>
	 * </p>
	 */
	public WWidget getWidget();

	/**
	 * Sets the message that you want to be displayed.
	 * <p>
	 * If the indicator is capable of displaying a text message, then you should
	 * reimplement this method to allow this message to be modified.
	 */
	public void setMessage(CharSequence text);
}
