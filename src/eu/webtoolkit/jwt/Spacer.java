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

class Spacer extends WWebWidget {
	public Spacer() {
		super();
		this.setInline(false);
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_DIV;
	}
}
