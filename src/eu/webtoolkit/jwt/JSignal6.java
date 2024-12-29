/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.lang.reflect.ParameterizedType;

import eu.webtoolkit.jwt.Signal.Listener;

/**
 * A signal that may be triggered from the browser with a JavaScript call, passing 6 arguments.
 * <p>
 * The argument types A1 to A6 must be a {@link String}, {@link WString} or a wrapper class
 * of one of the Java primitive types. Values of the corresponding JavaScript types can be 
 * passed to the JavaScript call.
 * <p>
 * Note that this is an abstract class. To create a JSignal, you should specialize this class, but
 * you do not need to reimplement any method. The reason for this is to circumvent limitations in
 * Java to obtain introspection in the types of the arguments, and provide suitable marshaling
 * of data from JavaScript to Java. The easiest way to instantiate an object of this class is:
 * 
 * <pre>
 *   JSignal6&lt;String, Integer, Double, Integer, WString, Integer&gt; pingSignal
 *      = new JSignal6&lt;String, Integer, Double, Integer, WString, Integer&gt;(this, "pingSignal") { };
 * </pre>
 * 
 * For a usage example take a look the documentation of {@link JSignal2}.
 */
public abstract class JSignal6<A1, A2, A3, A4, A5, A6> extends AbstractJSignal {
	private Signal6<A1, A2, A3, A4, A5, A6> dynamic_;

	JSignal6(WObject sender, String name, boolean collectSlotJavaScript) {
		super(sender, name, collectSlotJavaScript);

		dynamic_ = null;
	}

	/**
	 * Creates a signal.
	 * 
	 * @param sender the object that will be identified as the sender for this signal.
	 * @param name the signal name (must be unique for each sender)
	 */
	public JSignal6(WObject sender, String name) {
		this(sender, name, false);
	}

	/**
	 * Adds a listener for this signal.
	 * <p>
	 * Each listener will be triggered whenever the signal is triggered.
	 * 
	 * @param listenerOwner
	 *            the enclosing object for a listener implemented using an (anonymous) inner class
	 * @param listener
	 *            the listener
	 * @return a connection object that may be used to control the connection
	 * 
	 * @see AbstractSignal#addListener(WObject, eu.webtoolkit.jwt.Signal.Listener)
	 */
	public Connection addListener(WObject listenerOwner, Signal6.Listener<A1, A2, A3, A4, A5, A6> listener) {
		if (dynamic_ == null)
			dynamic_ = new Signal6<A1, A2, A3, A4, A5, A6>(getSender());

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
	public void removeListener(Signal6.Listener<A1, A2, A3, A4, A5, A6> listener) {
		if (dynamic_ != null)
			dynamic_.removeListener(listener);

		super.listenerRemoved();
	}

	/**
	 * Triggers the signal.
	 * <p>
	 * The arguments are passed to the listeners.
	 * 
	 * @param arg1 Argument 1
	 * @param arg2 Argument 2
	 * @param arg3 Argument 3
	 * @param arg4 Argument 4
	 * @param arg5 Argument 5
	 */
	public void trigger(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6) {
		if (dynamic_ != null)
			dynamic_.trigger(arg1, arg2, arg3, arg4, arg5, arg6);

		super.trigger();
	}

	public String createEventCall(String jsObject, String jsEvent, String arg1, String arg2, String arg3, String arg4, String arg5) {
		return createUserEventCall(jsObject, jsEvent, arg1, arg2, arg3, arg4, arg5, null);
	}

	/**
	 * Returns a JavaScript statement that triggers this signal.
	 * <p>
	 * You can use this to trigger the signal from within generated JavaScript code.
	 * 
	 * @param arg1 JavaScript argument 1.
	 * @param arg2 JavaScript argument 2.
	 * @param arg3 JavaScript argument 3.
	 * @param arg4 JavaScript argument 4.
	 * @param arg5 JavaScript argument 5.
	 * @param arg6 JavaScript argument 6.
	 */
	public String createCall(String arg1, String arg2, String arg3, String arg4, String arg5, String arg6) {
		return createUserEventCall(null, null, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	public String createCall(String arg1, String arg2, String arg3, String arg4, String arg5) {
		return createCall(arg1, arg2, arg3, arg4, arg5, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	void processDynamic(JavaScriptEvent jsEvent) {
		ParameterizedType parameterizedType =  
		    (ParameterizedType) getClass().getGenericSuperclass();
		Class A1Class= (Class) parameterizedType.getActualTypeArguments()[0];
		A1 a1 = (A1) unMarshal(jsEvent, 0, A1Class);
		Class A2Class= (Class) parameterizedType.getActualTypeArguments()[1];
		A2 a2 = (A2) unMarshal(jsEvent, 1, A2Class);
		Class A3Class= (Class) parameterizedType.getActualTypeArguments()[2];
		A3 a3 = (A3) unMarshal(jsEvent, 2, A3Class);
		Class A4Class= (Class) parameterizedType.getActualTypeArguments()[3];
		A4 a4 = (A4) unMarshal(jsEvent, 3, A4Class);
		Class A5Class= (Class) parameterizedType.getActualTypeArguments()[4];
		A5 a5 = (A5) unMarshal(jsEvent, 4, A5Class);
		Class A6Class= (Class) parameterizedType.getActualTypeArguments()[5];
		A6 a6 = (A6) unMarshal(jsEvent, 5, A6Class);

		trigger(a1, a2, a3, a4, a5, a6);
	}

	@Override
	public Connection addListener(WObject listenerOwner, final Signal.Listener listener) {
		if (dynamic_ == null)
			dynamic_ = new Signal6<A1, A2, A3, A4, A5, A6>(getSender());

		Connection c = dynamic_.addListener(listenerOwner, listener);

		super.listenerAdded();

		return c;
	}

	@Override
	public void removeListener(Listener listener) {
		if (dynamic_ != null)
			dynamic_.removeListener(listener);
	}
	
	@Override
	protected int getArgumentCount() {
		return 6;
	}
}
