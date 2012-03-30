/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.webtoolkit.jwt.servlet.UploadedFile;

/**
 * A simple base class for many JWt classes.
 * <p>
 * The class provides unique string IDs, which may be made identifiable using {@link #setObjectName(String)}.
 * <p>
 * The class also provides object life-time information for signal listeners (see {@link Signal.Listener}) that
 * are implemented as inner classes, helping to avoid the typical memory leak problem related when an object
 * is only reachable through an inner class listener object connected to a signal. <br>
 * By storing the signal listener within the WObject and using
 * a weak reference from within the {@link Signal} class, the object and listener will be reclaimed when only
 * referenced (using a weak reference) from the listener.
 * 
 * @see Signal#addListener(WObject, eu.webtoolkit.jwt.Signal.Listener)
 */
public class WObject {
	static class FormData {
		public FormData(String[] parameters, List<UploadedFile> uploadedFiles) {
			values = parameters;
			files = uploadedFiles;
		}

		String[] values;
		List<UploadedFile> files;
	}

	private WObject parent_;
	private int id_;
	private String objectName_;
	private static int nextObjId_ = 0;

	ArrayList<SignalImpl.ListenerSignalPair> listenerSignalsPairs;

	/**
	 * Default constructor.
	 */
	public WObject() {
		this(null);
	}

	protected WObject(WObject parent) {
		id_ = nextObjId_++;
		parent_ = parent;
		objectName_ = "";
	}

	int getRawUniqueId() {
		return id_;
	}

	void setParent(WObject parent) {
		parent_ = parent;
	}

	WObject getParent() {
		return parent_;
	}
	
	boolean hasParent() {
		return parent_ != null;
	}

	protected void addChild(WObject child) {
		child.setParent(this);
	}

	void removeChild(WObject child) {
		child.setParent(null);
	}

	/**
	 * Sets an object name.
	 * <p>
	 * The object name can be used to easily identify a type of object in the DOM, and does not need to be unique. It
	 * will usually reflect the widget type or role. The object name is prepended to the auto-generated object {@link #getId()}.
	 * <p>
	 * The default object name is empty.
	 * 
	 * @param name the object name.
	 */
	public void setObjectName(String name) {
		objectName_ = name;
	}

	/**
	 * Returns the object name.
	 * 
	 * @return the object name.
	 */
	public String getObjectName() {
		return objectName_;
	}

	final int getInternalId() {
		return id_;
	}

	final String getUniqueId() {
		return String.format("o%x", id_);
	}

	/**
	 * Returns the (unique) identifier for this object
	 * <p>
	 * For a {@link WWidget}, this corresponds to the id of the DOM element that represents the widget. This is not entirely
	 * unique, since a {@link WCompositeWidget} shares the same id as its implementation.
	 * <p>
	 * By default, the id is auto-generated, unless a custom id is set for a widget using {@link WWidget#setId(String)}. The
	 * auto-generated id is created by concatenating {@link #getObjectName()} with a unique number.
	 */
	public String getId() {
		String result = getObjectName();

		if (result != "")
			return result + '_' + getUniqueId();
		else
			return getUniqueId();
	}

	void setFormData(FormData formData) {
	}

	void setRequestTooLarge(long size) {
	}

	void formDataSet() {
	}

	void resetLearnedSlots() {
	}
	
	void signalConnectionsChanged() {
	}

	/**
	 * Creates a localized string.
	 * 
	 * This is a convenience method for {@link WString#tr(String)}.
	 */
	public static WString tr(String intlKey) {
		return WString.tr(intlKey);
	}
	
        /**
	 * Disconnects listeners owned by the object from signals.
	 *
	 * This method provides automatic connection management, forcing signal
	 * disconnection. Note that signals would be automatically disconnected
	 * when the object is garbage collected (and signals use weak references
	 * to allow garbage collection of an object which is otherwise no
	 * longer referenced but is still connected by signals).
	 */
	public void remove() {
		if (listenerSignalsPairs != null) 
			for (SignalImpl.ListenerSignalPair lsp : listenerSignalsPairs)
				lsp.signal.removeListener(lsp.listener);
	}
	
	static void seedId(int id) {
	  nextObjId_ = id;
	}
}
