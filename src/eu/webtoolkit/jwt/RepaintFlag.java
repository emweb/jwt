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

enum RepaintFlag {
	RepaintPropertyIEMobile(0x1 << 12), RepaintPropertyAttribute(0x1 << 13), RepaintInnerHtml(
			0x1 << 14), RepaintToAjax(0x1 << 15);

	private int value;

	RepaintFlag(int value) {
		this.value = value;
	}

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return value;
	}

	public static final EnumSet<RepaintFlag> RepaintAll = EnumSet.of(
			RepaintFlag.RepaintPropertyIEMobile,
			RepaintFlag.RepaintPropertyAttribute, RepaintFlag.RepaintInnerHtml);
}
