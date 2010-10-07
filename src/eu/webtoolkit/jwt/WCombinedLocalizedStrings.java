/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;

/**
 * A localized string resolver that bundles multiple string resolvers.
 * <p>
 * 
 * This class implements the localized strings interface and delegates
 * {@link WString#tr(String key) WString#tr()} string resolution to one or more
 * string resolvers. You will typically use this class if you want to combine
 * different methods of string resolution (e.g. some from files, and other
 * strings using a database).
 * <p>
 * 
 * @see WApplication#setLocalizedStrings(WLocalizedStrings translator)
 */
public class WCombinedLocalizedStrings extends WLocalizedStrings {
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
	 * {@link WCombinedLocalizedStrings#resolveKey(String key) resolveKey()}
	 * will consult each string resolver in the order they have been added.
	 */
	public void add(WLocalizedStrings localizedStrings) {
		this.localizedStrings_.add(localizedStrings);
	}

	/**
	 * Returns all string resolver.
	 * <p>
	 * Returns the list of all string resolvers that were added by a call to
	 * {@link WCombinedLocalizedStrings#add(WLocalizedStrings localizedStrings)
	 * add()}.
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

	public String resolveKey(String key) {
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
