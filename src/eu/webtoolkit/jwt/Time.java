/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

class Time {
	public Time() {
		time = System.currentTimeMillis();
	}

	public Time(Time other) {
		this.time = other.time;
	}

	public Time add(int msec) {
		Time t = new Time(this);
		t.addToThis(msec);
		return t;
	}

	public Time addToThis(int msec) {
		this.time += msec;
		return this;
	}

	public int subtract(Time other) {
		return (int) (this.time - other.time);
	}

	private long time;
}
