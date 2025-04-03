/*
 * Copyright (C) 2018 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import java.time.Duration;

public class ThreadUtils {
	public static void sleep(Duration d) throws InterruptedException {
		Thread.sleep(d.toMillis(), (int)(d.toNanos() % 1000000L));
	}
}
