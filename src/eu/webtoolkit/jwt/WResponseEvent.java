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

class WResponseEvent implements WAbstractEvent {
	public WResponseEvent() {
		super();
		this.jsEvent_ = new JavaScriptEvent();
	}

	public String getResponse() {
		return this.jsEvent_.response;
	}

	public WAbstractEvent createFromJSEvent(JavaScriptEvent jsEvent) {
		return new WResponseEvent(jsEvent);
	}

	private JavaScriptEvent jsEvent_;

	private WResponseEvent(JavaScriptEvent jsEvent) {
		super();
		this.jsEvent_ = jsEvent;
	}
}
