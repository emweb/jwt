/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;

/**
* Except when MatchExactly, the lexical matching is done (by comparing
* string representations of the value with the query).
* This is by default case insensitive, unless MatchCaseSensitive is 
* enabled.
*/
public class MatchOptions {
	/**
	 * The default match options, MatchStartsWith and MatchWrap.
	 */
	public static MatchOptions defaultMatchOptions = new MatchOptions(MatchType.MatchStartsWith, MatchFlag.MatchWrap);
	
	/**
	 * Match type enumeration.
	 */
	public enum MatchType {
		/** Same type and value */
		MatchExactly, 
		/** Lexical match */
		MatchStringExactly,
		/** Match start with query */
		MatchStartsWith,
		/** Match end with query */
		MatchEndsWith,
		/** Regular expression match */
		MatchRegExp, 
		/** Wildcard match */
		MatchWildCard 
	}
	
	/**
	 * Match flag enum.
	 */
	public enum MatchFlag {
		/** Case sensitive */
	    MatchCaseSensitive,
	    /** Wrap around whole model */
	    MatchWrap 
	}
	
	private MatchType type;
	private EnumSet<MatchFlag> flags;
	
	/**
	 * Constructor.
	 */
	public MatchOptions(MatchType type) {
		this.type = type;
		this.flags = EnumSet.noneOf(MatchFlag.class);
	}

	/**
	 * Constructor.
	 */
	public MatchOptions(MatchType type, MatchFlag flag) {
		this.type = type;
		this.flags = EnumSet.of(flag);
	}
	
	/**
	 * Constructor.
	 */
	public MatchOptions(MatchType type, MatchFlag flag, MatchFlag flag2) {
		this.type = type;
		this.flags = EnumSet.of(flag, flag2);
	}
	
	/**
	 * Return the type.
	 */
	public MatchType getType() {
		return type;
	}

	/**
	 * Return the flags.
	 */
	public EnumSet<MatchFlag> getFlags() {
		return flags;
	}
}
