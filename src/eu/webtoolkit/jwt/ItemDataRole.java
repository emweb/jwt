/*
 * Copyright (C) 2018 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration that indicates a role for a data item.
 * <p>
 * 
 * A single data item can have data associated with it corresponding to
 * different roles. Each role may be used by the corresponding view class in a
 * different way.
 * <p>
 * 
 * @see WModelIndex#getData(ItemDataRole role)
 */
public final class ItemDataRole implements Comparable<ItemDataRole> {
	private static Logger logger = LoggerFactory.getLogger(ItemDataRole.class);

	/**
	 * Create a new role with a certain int value.
	 * <p>
	 * JWt's roles are &lt; 32. User defined roles should be &gte; 32.
	 */
	public ItemDataRole(int role) {
		this.role_ = role;
	}

	/**
	 * Returns the canonical ItemDataRole for the given numeric value.
	 * <p>
	 * If the role is one of the predefined roles, that instance is returned,
	 * otherwise a new role is created, so the following is true for all predefined roles:
	 * <pre>{@code
	 * ItemDataRole.of(0).equals(ItemDataRole.Display) &&
	 * ItemDataRole.of(0) == ItemDataRole.Display
	 * }</pre>
	 */
	public static ItemDataRole of(int role) {
		if (role < predefined_.length &&
				predefined_[role] != null)
			return predefined_[role];
		else
			return new ItemDataRole(role);
	}

	/**
	 * Returns the underlying int of this role.
	 */
	public int getValue() {
		return this.role_;
	}

	@Override
	public boolean equals(Object rhs) {
		if (this == rhs)
			return true;
		else if (rhs instanceof ItemDataRole)
			return this.role_ == ((ItemDataRole) rhs).role_;
		else
			return false;
	}

	@Override
	public int compareTo(final ItemDataRole rhs) {
		return Integer.compare(role_, rhs.role_);
	}

	@Override
	public String toString() {
		if (this.role_ < names_.length &&
				names_[this.role_] != null)
			return names_[this.role_];
		else
			return "ItemDataRole (" + this.role_ + ")";
	}

	@Override
	public int hashCode() {
		return this.role_;
	}

	private static ItemDataRole[] predefined_ = new ItemDataRole[32];
	private static String[] names_ = new String[32];

	/**
	 * Role for textual representation (0).
	 */
	public static final ItemDataRole Display = defineRole(0, "Display");
	/**
	 * Role for the url of an icon (1).
	 */
	public static final ItemDataRole Decoration = defineRole(1, "Decoration");
	/**
	 * Role for the edited value (2).
	 */
	public static final ItemDataRole Edit = defineRole(2, "Edit");
	/**
	 * Role for the style class (3).
	 */
	public static final ItemDataRole StyleClass = defineRole(3, "StyleClass");
	/**
	 * Role that indicates the check state (4).
	 * <p>
	 * Data for this role should be a <code>bool</code>. When the
	 * Wt::ItemIsTristate flag is set for the item, data for this role should be
	 * of type {@link CheckState}.
	 */
	public static final ItemDataRole Checked = defineRole(4, "Checked");
	/**
	 * Role for a (plain) tooltip (5).
	 */
	public static final ItemDataRole ToolTip = defineRole(5, "ToolTip");
	/**
	 * Role for a link (6).
	 */
	public static final ItemDataRole Link = defineRole(6, "Link");
	/**
	 * Role for mime type information (7).
	 */
	public static final ItemDataRole MimeType = defineRole(7, "MimeType");
	/**
	 * Level in aggregation, for header data (8).
	 */
	public static final ItemDataRole Level = defineRole(8, "Level");
	/**
	 * Marker pen color (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) (16).
	 */
	public static final ItemDataRole MarkerPenColor = defineRole(16, "MarkerPenColor");
	/**
	 * Marker brush color (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) (17).
	 */
	public static final ItemDataRole MarkerBrushColor = defineRole(17, "MarkerBrushColor");
	/**
	 * Marker size (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) (20).
	 */
	public static final ItemDataRole MarkerScaleFactor = defineRole(20, "MarkerScaleFactor");
	/**
	 * Marker type (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) (21).
	 */
	public static final ItemDataRole MarkerType = defineRole(21, "MarkerType");
	/**
	 * Bar pen color (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) (18).
	 */
	public static final ItemDataRole BarPenColor = defineRole(18, "BarPenColor");
	/**
	 * Bar brush color (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) (19).
	 */
	public static final ItemDataRole BarBrushColor = defineRole(19, "BarBrushColor");
	/**
	 * First role reserved for user purposes (32).
	 */
	public static final ItemDataRole User = new ItemDataRole(32);

	private static ItemDataRole defineRole(int i, String name) {
		ItemDataRole role = new ItemDataRole(i);
		predefined_[i] = role;
		names_[i] = "ItemDataRole::" + name + " (" + i + ")";
		return role;
	}

	private int role_;
}
