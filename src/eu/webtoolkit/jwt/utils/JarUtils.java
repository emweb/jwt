/*
 * Copyright (C) 2006 Pieter Libin, Leuven, Belgium.
 *
 * Licensed under the terms of the GNU General Public License,
 * see the LICENSE file for more details.
 */

package eu.webtoolkit.jwt.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JarUtils {
	public static JarUtils getInstance() {
		return new JarUtils();
	}

	public String readTextFromJar(String s) {
		String thisLine;
		StringBuilder builder = new StringBuilder();
		try {
			InputStream is = this.getClass().getResourceAsStream(s);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while ((thisLine = br.readLine()) != null) {
				builder.append(thisLine + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return builder.toString();
	}

	public static void main(String[] args) {
		String a = getInstance().readTextFromJar("Boot.html");
		System.err.println(a);
	}
}
