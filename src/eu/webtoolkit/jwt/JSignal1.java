/*
 * Copyright (C) 2006 Pieter Libin, Leuven, Belgium.
 *
 * Licensed under the terms of the GNU General Public License,
 * see the LICENSE file for more details.
 */

package eu.webtoolkit.jwt;

import java.lang.reflect.ParameterizedType;

/**
 * A signal that may be triggered from the browser with a JavaScript call, passing 1 argument.
 * 
 * The argument type A1 must be a {@link String}, {@link WString}, {@link Integer} or
 * {@link Double}. A value of the corresponding JavaScript type can be passed to the JavaScript
 * call.
 *
 * Note that this is an abstract class. To create a JSignal, you should specialize this class, but
 * you do not need to reimplement any method. The reason for this is to circumvent limitations in
 * Java to obtain introspection in the types of the arguments, and provide suitable marshalling
 * of data from JavaScript to Java. The easiest way to instantiate an object of this class is:
 * 
 * <code>
 *   JSignal1<String> pingSignal
 *     = new JSignal1<String>(this, "pingSignal") { };
 * </code>
 */
public abstract class JSignal1<A1> extends AbstractJSignal {
	private Signal1<A1> dynamic_;

	JSignal1(WObject sender, String name, boolean collectSlotJavaScript) {
		super(sender, name);

		dynamic_ = null;
	}

	/**
	 * Creates a signal.
	 * 
	 * @param sender the object that will be identified as the sender for this signal.
	 * @param name the signal name (must be unique for each sender)
	 */
	public JSignal1(WObject sender, String name) {
		this(sender, name, false);
	}

	/**
	 * Adds a listener for this signal.
	 * 
	 * Each listener will be triggered whenever the signal is triggered.
	 * 
	 * @param listenerOwner
	 *            the enclosing object for a listener implemented using an anonymous inner class
	 * @param listener
	 *            the listener
	 * @return a connection object that may be used to control the connection
	 * 
	 * @see AbstractSignal#addListener(WObject, eu.webtoolkit.jwt.Signal.Listener)
	 */
	public Connection addListener(WObject listenerOwner, Signal1.Listener<A1> listener) {
		if (dynamic_ == null)
			dynamic_ = new Signal1<A1>(getSender());

		Connection c = dynamic_.addListener(listenerOwner, listener);
		super.listenerAdded();

		return c;
	}

	@Override
	protected int getListenerCount() {
		return super.getListenerCount() + (dynamic_ != null ? dynamic_.getListenerCount() : null);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener a listener that was previously added
	 */
	public void removeListener(Signal1.Listener<A1> listener) {
		dynamic_.removeListener(listener);

		super.listenerRemoved();
	}

	/**
	 * Triggers the signal.
	 * 
	 * The argument is passed to the listeners.
	 * 
	 * @param arg The argument.
	 */
	public void trigger(A1 arg) {
		if (dynamic_ != null)
			dynamic_.trigger(arg);

		super.trigger();
	}

	/**
	 * Returns a JavaScript statement that triggers this signal.
	 * 
	 * You can use this to trigger the signal from within generated JavaScript code.
	 * 
	 * @param arg The JavaScript argument.
	 */
	public String createCall(String arg) {
		return createUserEventCall(null, null, arg, null, null, null, null, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void processDynamic(JavaScriptEvent jsEvent) {
		ParameterizedType parameterizedType =  
		    (ParameterizedType) getClass().getGenericSuperclass();
		Class A1Class= (Class) parameterizedType.getActualTypeArguments()[0];
		A1 a1 = (A1) unMarshal(jsEvent, 0, A1Class);
		trigger(a1);
	}

	@Override
	public Connection addListener(WObject listenerOwner, final Signal.Listener listener) {
		if (dynamic_ == null)
			dynamic_ = new Signal1<A1>(getSender());

		Connection c = dynamic_.addListener(listenerOwner, listener);
		super.listenerAdded();

		return c;
	}

	@Override
	public void removeListener(Signal.Listener listener) {
		if (dynamic_ != null)
			dynamic_.removeListener(listener);
	}
}