package eu.webtoolkit.jwt;

import java.util.EnumSet;

/**
 * Enumeration that indicates a relative location.
 * <p>
 * Values of CenterX, CenterY, and CenterXY are only valid for
 * {@link WCssDecorationStyle#setBackgroundImage(String image, WCssDecorationStyle.Repeat repeat, EnumSet sides)}
 * <p>
 * 
 * @see WWidget#setMargin(WLength margin, EnumSet sides)
 * @see WWidget#setOffsets(WLength offset, EnumSet sides)
 * @see WWidget#setFloatSide(Side s)
 * @see WWidget#setClearSides(EnumSet sides)
 * @see WContainerWidget#setPadding(WLength length, EnumSet sides)
 * @see WCssDecorationStyle#setBackgroundImage(String image,
 *      WCssDecorationStyle.Repeat repeat, EnumSet sides)
 */
public enum Side {
	/**
	 * Top side.
	 */
	Top,
	/**
	 * Bottom side.
	 */
	Bottom,
	/**
	 * Left side.
	 */
	Left,
	/**
	 * Right side.
	 */
	Right,
	/**
	 * Center horiziontally.
	 */
	CenterX,
	/**
	 * Center vertically.
	 */
	CenterY;

	public int getValue() {
		return ordinal();
	}

	public static final EnumSet<Side> None = EnumSet.noneOf(Side.class);
	public static final EnumSet<Side> CenterXY = EnumSet.of(Side.CenterX,
			Side.CenterY);
	public static final EnumSet<Side> Horizontals = EnumSet.of(Side.Left,
			Side.Right);
	public static final EnumSet<Side> Verticals = EnumSet.of(Side.Top,
			Side.Bottom);
	public static final EnumSet<Side> All = EnumSet.of(Side.Top, Side.Bottom,
			Side.Left, Side.Right);
}
