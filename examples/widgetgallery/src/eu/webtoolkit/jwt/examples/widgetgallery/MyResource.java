/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MyResource extends WResource {
	private static Logger logger = LoggerFactory.getLogger(MyResource.class);

	public MyResource(WObject parent) {
		super(parent);
		this.suggestFileName("data.txt");
	}

	public MyResource() {
		this((WObject) null);
	}

	public void handleRequest(final WebRequest request,
			final WebResponse response) throws IOException {
		response.setContentType("plain/text");
		response.out().append("I am a text file.").append('\n');
	}
}
