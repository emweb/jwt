package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

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
