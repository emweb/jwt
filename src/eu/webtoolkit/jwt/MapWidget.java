/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

class MapWidget extends WContainerWidget {
	public MapWidget() {
		super();
	}

	void updateDom(DomElement element, boolean all) {
		if (all) {
			element.setAttribute("name", this.getId());
		}
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_MAP;
	}
}
