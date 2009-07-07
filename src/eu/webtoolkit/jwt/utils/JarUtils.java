/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
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
