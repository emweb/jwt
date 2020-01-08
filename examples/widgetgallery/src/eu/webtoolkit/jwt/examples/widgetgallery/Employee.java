/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

class Employee {
	public Employee(String firstName, String lastName, double pay) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.pay = pay;
	}

	public String firstName;
	public String lastName;
	public double pay;
}
