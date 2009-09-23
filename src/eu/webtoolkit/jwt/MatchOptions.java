/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
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
		MatchExactly, /** Same type and value */
		MatchStringExactly, /** Lexical match */
		MatchStartsWith, /** Match start with query */
		MatchEndsWith, /** Match end with query */
		MatchRegExp, /** Regular expression match */
		MatchWildCard /** Wildcard match */
	}
	
	/**
	 * Match flag enum.
	 */
	public enum MatchFlag {
	    MatchCaseSensitive, /** Case sensitive */
	    MatchWrap /** Wrap around whole model */
	}
	
	private MatchType type;
	private EnumSet<MatchFlag> flags;
	
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
