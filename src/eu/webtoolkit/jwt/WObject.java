/*
 * Copyright (C) 2006 Pieter Libin, Leuven, Belgium.
 *
 * Licensed under the terms of the GNU General Public License,
 * see the LICENSE file for more details.
 */

package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.webtoolkit.jwt.servlet.UploadedFile;

public class WObject {
	static class FormData {
		public FormData(String[] parameters, UploadedFile uploadedFile) {
			if (parameters != null)
				values = Arrays.asList(parameters);
			else
				values = Collections.emptyList();
			file = uploadedFile;
		}

		List<String> values;
		UploadedFile file;
	}

	private WObject parent_;
	private int id_;
	private String objectName_;
	private static int nextObjId_;

	ArrayList<SignalImpl.Listener> listeners;

	public WObject() {
		this(null);
	}

	public WObject(WObject parent) {
		id_ = nextObjId_++;
		parent_ = parent;
		objectName_ = "";
	}

	protected int getRawUniqueId() {
		return id_;
	}

	public void setParent(WObject parent) {
		parent_ = parent;
	}

	public WObject getParent() {
		return parent_;
	}

	public void addChild(WObject child) {
		child.setParent(this);
	}

	public void setObjectName(String name) {
		objectName_ = name;
	}

	public String getObjectName() {
		return objectName_;
	}
	
	protected void destroy() {
	}

	final int getInternalId() {
		return id_;
	}

	final String getUniqueId() {
		return String.format("o%x", id_);
	}

	public String getFormName() {
		StringBuilder result = new StringBuilder(objectName_);

		if (objectName_ != "")
			result.append("-");
		
		result.append(getUniqueId());
		
		return result.toString();
	}

	final String getId() {
		return getFormName();
	}

	void setFormData(FormData formData) {
	}

	void requestTooLarge(int size) {
	}

	void formDataSet() {
	}

	public void resetLearnedSlots() {
	}
	
	void signalConnectionsChanged() {
	}

	public static WString tr(String intlKey) {
		return WString.tr(intlKey);
	}
}
