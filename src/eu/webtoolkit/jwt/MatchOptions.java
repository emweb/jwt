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
	public static MatchOptions defaultMatchOptions = new MatchOptions(MatchType.StartsWith, MatchFlag.Wrap);
	
	/**
	 * Match type enumeration.
	 */
	public enum MatchType {
		/** Same type and value */
		Exactly, 
		/** Lexical match */
		StringExactly,
		/** Match start with query */
		StartsWith,
		/** Match end with query */
		EndsWith,
		/** Regular expression match */
		RegExp, 
		/** Wildcard match */
		WildCard 
	}
	
	/**
	 * Match flag enum.
	 */
	public enum MatchFlag {
		/** Case sensitive */
	    CaseSensitive,
	    /** Wrap around whole model */
	    Wrap 
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
