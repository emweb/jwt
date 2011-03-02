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

class SpinBoxValidator extends WValidator {
	public SpinBoxValidator(WAbstractSpinBox spinBox) {
		super();
		this.spinBox_ = spinBox;
	}

	public WValidator.State validate(String input) {
		return this.spinBox_.parseValue(input) ? WValidator.State.Valid
				: WValidator.State.Invalid;
	}

	public String getJavaScriptValidate() {
		return "jQuery.data(" + this.spinBox_.getJsRef() + ", 'obj');";
	}

	private WAbstractSpinBox spinBox_;
}
