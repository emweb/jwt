/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Term {
	private static Logger logger = LoggerFactory.getLogger(Term.class);

	enum Type {
		Font, Length, Angle, Time, Frequency, OtherNumber, QuotedString, Identifier, Hash, Uri, Invalid;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	enum Unit {
		Em, Ex, Px, Cm, Mm, In, Pt, Pc, Deg, Rad, Grad, Ms, Seconds, Hz, Khz, Percentage, InvalidUnit;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	public Term() {
		this.quotedString_ = "";
		this.identifier_ = "";
		this.hash_ = "";
		this.uri_ = "";
		this.unit_ = Term.Unit.InvalidUnit;
	}

	public void setUnit(Term.Unit u) {
		this.unit_ = u;
		if (u.getValue() <= Term.Unit.Ex.getValue()) {
			this.type_ = Term.Type.Font;
		} else {
			if (u.getValue() <= Term.Unit.Pc.getValue()) {
				this.type_ = Term.Type.Length;
			} else {
				if (u.getValue() <= Term.Unit.Grad.getValue()) {
					this.type_ = Term.Type.Angle;
				} else {
					if (u.getValue() <= Term.Unit.Seconds.getValue()) {
						this.type_ = Term.Type.Time;
					} else {
						if (u.getValue() <= Term.Unit.Khz.getValue()) {
							this.type_ = Term.Type.Frequency;
						} else {
							if (u.getValue() <= Term.Unit.Percentage.getValue()) {
								this.type_ = Term.Type.OtherNumber;
							} else {
								this.type_ = Term.Type.Invalid;
							}
						}
					}
				}
			}
		}
	}

	public void setQuotedString(String s) {
		this.quotedString_ = s;
		this.type_ = Term.Type.QuotedString;
	}

	public void setIdentifier(String id) {
		this.identifier_ = id;
		this.type_ = Term.Type.Identifier;
	}

	public void setHash(String hash) {
		this.hash_ = hash;
		this.type_ = Term.Type.Hash;
	}

	public void setUri(String uri) {
		this.uri_ = uri;
		this.type_ = Term.Type.Uri;
	}

	// public Term.Type getType() ;
	public double value_;
	public String quotedString_;
	public String identifier_;
	public String hash_;
	public String uri_;
	public Term.Unit unit_;
	public Term.Type type_;
}
