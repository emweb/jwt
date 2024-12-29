/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;

/**
 * A signal that propagates events to listeners, and is capable of passing 2 argument.
 * <p>
 * A signal implements the Observer pattern, allowing one or more listeners to listen for
 * events generated on the signal. The event may propagate 2 arguments to the listeners.
 * <p>
 * A usage example:
 * <pre>
 * {@code
 *  class MyWidget extends WContainerWidget
 *  {
 *    public MyWidget(WContainerWidget parent)
 *    {
 *      super(parent);
 *  	
 *      this.done = new Signal2<Integer, String>();
 *      ...
 *      WPushButton button = new WPushButton("Okay");
 *      button.clicked().addListener(this, new Signal.Listener() {
 *        public void trigger() {
 *          process();
 *        }
 *      });
 *    }
 *
 *    // provide an accessor for the signal
 *    Signal2<Integer, String> done() { return done; }
 *
 *    private Signal2<Integer, String> done;
 *
 *    void process() {
 *      ...
 *      done.trigger(42, "Totally done"); // trigger the signal
 *    }
 *  };
 * }
 * </pre> 
 *  This widget could then be used form another class:
 * <pre> 
 * {@code
 *  class GUIClass extends WContainerWidget
 *  {
 *    ...
 *
 *    private void init() {
 *      MyWidget widget = new MyWidget(this);
 *      widget.done().addListener(this, new Signal2.Listener<Integer, String>() {
 *        public void trigger(Integer i, String s) {
 *          whenDone(i, s);
 *        }
 *      });
 *    }
 *   
 *    void whenDone(int result, String description) {
 *      ...
 *    }
 *  };
 * }
 * </pre>
 */
public class Signal2<A1, A2> extends AbstractSignal {
	/**
	 * The listener interface.
	 * <p>
	 * This listener may be added to a {@link Signal2} or {@link JSignal2}, and
	 * its {@link #trigger(Object, Object)} method will be invoked whenever
	 * the signal is triggered.
	 */
	public static interface Listener<A1, A2> extends SignalImpl.Listener {
		/**
		 * Triggers the listener.
		 * 
		 * @param arg1 Argument 1
		 * @param arg2 Argument 2
		 */
		public void trigger(A1 arg1, A2 arg2);
	}

	/**
	 * Creates a new signal.
	 */
	public Signal2() {
	}

	Signal2(WObject sender) {
		this();
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
	public Connection addListener(WObject listenerOwner, Listener<A1, A2> listener) {
		return getImpl(true).addListener(listenerOwner, listener);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener a listener that was previously added
	 */
	public void removeListener(Listener<A1, A2> listener) {
		getImpl(false).removeListener(listener);
	}

	/**
	 * Triggers the signal.
	 * <p>
	 * The arguments are passed to the listeners.
	 * 
	 * @param arg1 Argument 1.
	 * @param arg2 Argument 2.
	 */
	@SuppressWarnings("unchecked")
	public void trigger(A1 arg1, A2 arg2) {
		SignalImpl impl = getImpl(false);
		if (impl == null)
			return;

		ArrayList<SignalImpl.Listener> listeners = impl.getListeners();

		for (SignalImpl.Listener listener : listeners)
			((Listener) (listener)).trigger(arg1, arg2);
	}

	@Override
	public Connection addListener(WObject listenerOwner, final Signal.Listener listener) {
		Signal2.Listener<A1, A2> l = new Signal2.Listener<A1, A2>() {
			public void trigger(A1 a1, A2 a2) {
				listener.trigger();
			}
		};

		return getImpl(true).addWrappedListener(listenerOwner, l, listener);
	}

	@Override
	public void removeListener(Signal.Listener listener) {
		getImpl(true).removeWrappedListener(listener);
	}
	
	@Override
	protected int getArgumentCount() {
		return 2;
	}
}
