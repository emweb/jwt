/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates a Wt application type.
 */
public enum ApplicationType {
	/**
	 * Specifies a full-screen application.
	 * <p>
	 * A full screen application manages the entire browser window and provides
	 * its own HTML page.
	 * <p>
	 * 
	 * @see WApplication#getRoot()
	 */
	Application,
	/**
	 * Specifies an application that manages one or more widgets.
	 * <p>
	 * A widget set application is part of an existing HTML page. One or more
	 * HTML elements in that web page may be bound to widgets managed by the
	 * application.
	 * <p>
	 * A widgetset application presents itself as a JavaScript file, and
	 * therefore should be embedded in the web page using a &lt;script&gt; tag,
	 * from within the &lt;body&gt; (since it needs access to the &lt;body&gt;).
	 * <p>
	 * <p>
	 * <i><b>Note:</b>A WidgetSet application requires JavaScript support</i>
	 * </p>
	 * 
	 * @see WApplication#bindWidget(WWidget widget, String domId)
	 */
	WidgetSet;

	public int getValue() {
		return ordinal();
	}
}
