package eu.webtoolkit.jwt;

import java.util.EnumSet;

/**
 * Flags that specify how to match two values.
 * 
 * Except when MatchExactly, the lexical matching is done (by comparing string
 * representations of the value with the query). This is by default case
 * insensitive, unless MatchCaseSensitive is OR&apos;ed.
 */
public enum MatchFlag {
	/**
	 * Same type and value.
	 */
	MatchExactly,
	/**
	 * Lexical match.
	 */
	MatchStringExactly,
	/**
	 * Match start with query.
	 */
	MatchStartsWith,
	/**
	 * Match end with query.
	 */
	MatchEndsWith,
	/**
	 * Regular expression match.
	 */
	MatchRegExp,
	/**
	 * Wildcard match.
	 */
	MatchWildCard,
	/**
	 * Case sensitive.
	 */
	MatchCaseSensitive,
	/**
	 * Wrap around whole model.
	 */
	MatchWrap;

	public int getValue() {
		return ordinal();
	}

	public static final EnumSet<MatchFlag> MatchTypeMask = EnumSet.of(
			MatchFlag.MatchExactly, MatchFlag.MatchStringExactly,
			MatchFlag.MatchStartsWith, MatchFlag.MatchEndsWith,
			MatchFlag.MatchRegExp, MatchFlag.MatchWildCard);
}
