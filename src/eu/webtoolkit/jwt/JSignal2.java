/*
 * Copyright (C) 2006 Pieter Libin, Leuven, Belgium.
 *
 * Licensed under the terms of the GNU General Public License,
 * see the LICENSE file for more details.
 */

package eu.webtoolkit.jwt;

import java.lang.reflect.ParameterizedType;

import eu.webtoolkit.jwt.Signal.Listener;

/**
 * A signal that may be triggered from the browser with a JavaScript call, passing 2 arguments.
 * 
 * The argument types A1 and A2 must be a {@link String}, {@link WString}, {@link Integer} or
 * {@link Double}. Values of the corresponding JavaScript types can be passed to the JavaScript
 * call.
 *
 * Note that this is an abstract class. To create a JSignal, you should specialize this class, but
 * you do not need to reimplement any method. The reason for this is to circumvent limitations in
 * Java to obtain introspection in the types of the arguments, and provide suitable marshalling
 * of data from JavaScript to Java. The easiest way to instantiate an object of this class is:
 * 
 * <code>
 *   JSignal2<String, Integer> pingSignal
 *     = new JSignal2<String, Integer>(this, "pingSignal") { };
 * </code>
 */
public abstract class JSignal2<A1, A2> extends AbstractJSignal {
	private Signal2<A1, A2> dynamic_;

	JSignal2(WObject sender, String name, boolean collectSlotJavaScript) {
		super(sender, name);

		dynamic_ = null;
	}
	
	/**
	 * Creates a signal.
	 * 
	 * @param sender the object that will be identified as the sender for this signal.
	 * @param name the signal name (must be unique for each sender)
	 */
	public JSignal2(WObject sender, String name) {
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
	public Connection addListener(WObject listenerOwner, Signal2.Listener<A1, A2> listener) {
		if (dynamic_ == null)
			dynamic_ = new Signal2<A1, A2>(getSender());

		Connection c = dynamic_.addListener(listenerOwner, listener);

		super.listenerAdded();

		return c;
	}

	@Override
	protected int getListenerCount() {
		return super.getListenerCount() + (dynamic_ != null ? dynamic_.getListenerCount() : 0);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener a listener that was previously added
	 */
	public void removeListener(Signal2.Listener<A1, A2> listener) {
		if (dynamic_ != null)
			dynamic_.removeListener(listener);

		super.listenerRemoved();
	}

	/**
	 * Triggers the signal.
	 * 
	 * The arguments are passed to the listeners.
	 * 
	 * @param arg1 Argument 1
	 * @param arg2 Argument 2
	 */
	public void trigger(A1 arg1, A2 arg2) {
		if (dynamic_ != null)
			dynamic_.trigger(arg1, arg2);

		super.trigger();
	}

	/**
	 * Returns a JavaScript statement that triggers this signal.
	 * 
	 * You can use this to trigger the signal from within generated JavaScript code.
	 * 
	 * @param arg1 JavaScript argument 1.
	 * @param arg2 JavaScript argument 2.
	 */
	public String createCall(String arg1, String arg2) {
		return createUserEventCall(null, null, arg1, arg2, null, null, null, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void processDynamic(JavaScriptEvent jsEvent) {
		ParameterizedType parameterizedType =  
		    (ParameterizedType) getClass().getGenericSuperclass();
		Class A1Class= (Class) parameterizedType.getActualTypeArguments()[0];
		A1 a1 = (A1) unMarshal(jsEvent, 0, A1Class);
		Class A2Class= (Class) parameterizedType.getActualTypeArguments()[1];
		A2 a2 = (A2) unMarshal(jsEvent, 1, A2Class);
		trigger(a1, a2);
	}

	@Override
	public Connection addListener(WObject listenerOwner, final Signal.Listener listener) {
		if (dynamic_ == null)
			dynamic_ = new Signal2<A1, A2>(getSender());

		Connection c = dynamic_.addListener(listenerOwner, listener);

		super.listenerAdded();

		return c;
	}

	@Override
	public void removeListener(Listener listener) {
		if (dynamic_ != null)
			dynamic_.removeListener(listener);
	}
}