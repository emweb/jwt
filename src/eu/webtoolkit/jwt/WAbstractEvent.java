/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * An internal interface for providing details for an event
 */
public interface WAbstractEvent {
	/**
	 * Internal method.
	 */
	public WAbstractEvent createFromJSEvent(JavaScriptEvent jsEvent);
}
