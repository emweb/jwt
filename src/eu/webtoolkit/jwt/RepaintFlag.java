/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;

enum RepaintFlag {
	RepaintPropertyIEMobile(0x1 << 12), RepaintPropertyAttribute(0x1 << 13), RepaintInnerHtml(
			0x1 << 14);

	private int value;

	RepaintFlag(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static final EnumSet<RepaintFlag> RepaintAll = EnumSet.of(
			RepaintFlag.RepaintPropertyIEMobile,
			RepaintFlag.RepaintPropertyAttribute, RepaintFlag.RepaintInnerHtml);
}
