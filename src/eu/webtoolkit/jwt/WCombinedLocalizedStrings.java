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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A localized string resolver that bundles multiple string resolvers.
 * <p>
 * 
 * This class implements the localized strings interface and delegates {@link }
 * string resolution to one or more string resolvers. You will typically use
 * this class if you want to combine different methods of string resolution
 * (e.g. some from files, and other strings using a database).
 * <p>
 * 
 * @see WApplication#setLocalizedStrings(WLocalizedStrings translator)
 */
public class WCombinedLocalizedStrings extends WLocalizedStrings {
	private static Logger logger = LoggerFactory
			.getLogger(WCombinedLocalizedStrings.class);

	/**
	 * Constructor.
	 */
	public WCombinedLocalizedStrings() {
		super();
		this.localizedStrings_ = new ArrayList<WLocalizedStrings>();
	}

	/**
	 * Adds a string resolver.
	 * <p>
	 * The order in which string resolvers are added is significant: {@link }
	 * will consult each string resolver in the order they have been added,
	 * until a match is found.
	 */
	public void add(WLocalizedStrings resolver) {
		this.localizedStrings_.add(resolver);
	}

	/**
	 * Inserts a string resolver.
	 * <p>
	 * 
	 * @see WCombinedLocalizedStrings#add(WLocalizedStrings resolver)
	 */
	public void insert(int index, WLocalizedStrings resolver) {
		this.localizedStrings_.add(0 + index, resolver);
	}

	/**
	 * Removes a string resolver.
	 * <p>
	 * 
	 * @see WCombinedLocalizedStrings#add(WLocalizedStrings resolver)
	 */
	public void remove(WLocalizedStrings resolver) {
		this.localizedStrings_.remove(resolver);
	}

	/**
	 * Returns the list of resolvers.
	 * <p>
	 * 
	 * @see WCombinedLocalizedStrings#add(WLocalizedStrings resolver)
	 * @see WCombinedLocalizedStrings#remove(WLocalizedStrings resolver)
	 */
	public List<WLocalizedStrings> getItems() {
		return this.localizedStrings_;
	}

	public void refresh() {
		for (int i = 0; i < this.localizedStrings_.size(); ++i) {
			this.localizedStrings_.get(i).refresh();
		}
	}

	public void hibernate() {
		for (int i = 0; i < this.localizedStrings_.size(); ++i) {
			this.localizedStrings_.get(i).hibernate();
		}
	}

	public String resolveKey(final String key) {
		String result = null;
		for (int i = 0; i < this.localizedStrings_.size(); ++i) {
			result = this.localizedStrings_.get(i).resolveKey(key);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private List<WLocalizedStrings> localizedStrings_;
}
