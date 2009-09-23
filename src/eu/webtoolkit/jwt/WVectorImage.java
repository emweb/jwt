/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * An abstract paint device for rendering using native vector graphics.
 */
public interface WVectorImage extends WPaintDevice {
	/**
	 * Internal method.
	 */
	public String getRendered();
}
