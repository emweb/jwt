/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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

class AgeFormModel extends WFormModel {
	private static Logger logger = LoggerFactory.getLogger(AgeFormModel.class);

	public static String AgeField = "age";

	public AgeFormModel(WObject parent) {
		super(parent);
		this.addField(AgeField);
		this.setValidator(AgeField, this.getCreateAgeValidator());
		this.setValue(AgeField, "");
	}

	public AgeFormModel() {
		this((WObject) null);
	}

	private WValidator getCreateAgeValidator() {
		WIntValidator v = new WIntValidator(0, 150);
		return v;
	}
}
