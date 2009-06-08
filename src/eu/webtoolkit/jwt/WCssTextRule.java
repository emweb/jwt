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
 * Returns the declarations.
 * 
 * This is a semi-colon separated list of CSS declarations.
 */
public class WCssTextRule extends WCssRule {
	/**
	 * Returns the declarations.
	 * 
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public WCssTextRule(String selector, String declarations) {
		super(selector);
		this.declarations_ = declarations;
	}

	/**
	 * Returns the declarations.
	 * 
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public String getDeclarations() {
		return this.declarations_;
	}

	private String declarations_;
}
