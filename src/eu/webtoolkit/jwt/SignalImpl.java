/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import eu.webtoolkit.jwt.AbstractSignal.Connection;

class SignalImpl {
	static interface Listener {
	}
	
	static class ListenerSignalPair {
		public ListenerSignalPair(Listener listener, SignalImpl signal) {
			this.listener = listener;
			this.signal = signal;
		}

		Listener listener;
		SignalImpl signal;
	}

	private final ArrayList<Listener> observerCatalog = new ArrayList<Listener>();
	private final ArrayList<WeakReference<Listener>> weakObserverCatalog = new ArrayList<WeakReference<Listener>>();
	
	private Map<Listener, Object> wrappedListeners_ = null;
	private Map<WeakReference<Listener>, WeakReference<Object>> weakWrappedListeners_ = null;

	private static ArrayList<Listener> emptyList = new ArrayList<Listener>();
	private boolean blocked = false;

	public Connection addListener(WObject listenerOwner, Listener listener) {
		if (listenerOwner != null) {
			weakObserverCatalog.add(new WeakReference<Listener>(listener));
			if (listenerOwner.listenerSignalsPairs == null)
				listenerOwner.listenerSignalsPairs = new ArrayList<ListenerSignalPair>();
			listenerOwner.listenerSignalsPairs.add(new ListenerSignalPair(listener, this));
		} else
			observerCatalog.add(listener);

		return new Connection(this, listener);
	}

	public Connection addWrappedListener(WObject listenerOwner, Listener listener, Object wrappedListener) {
		if (listenerOwner != null) {
			if (weakWrappedListeners_ == null)
				weakWrappedListeners_ = new HashMap<WeakReference<Listener>, WeakReference<Object>>();

			weakWrappedListeners_.put(new WeakReference<Listener>(listener),
									  new WeakReference<Object>(wrappedListener));
		} else {
			if (wrappedListeners_ == null)
				wrappedListeners_ = new HashMap<Listener, Object>();
			wrappedListeners_.put(listener, wrappedListener);
		}

		return addListener(listenerOwner, listener);
	}

	public void removeListener(Listener listener) {
		for (WeakReference<Listener> weakObserver : weakObserverCatalog) {
			if (listener == weakObserver.get()) {
				weakObserverCatalog.remove(weakObserver);
				break;
			}
		}
		observerCatalog.remove(listener);
	}
	
	@SuppressWarnings("unchecked")
	protected ArrayList<Listener> getListeners() {
		if (blocked)
			return emptyList;

		ArrayList<Listener> result = (ArrayList<Listener>) observerCatalog.clone();

		Iterator<WeakReference<Listener>> i = weakObserverCatalog.iterator();
		while (i.hasNext()) {
			WeakReference<Listener> ref = i.next();
			Listener listener = ref.get();
			if (listener != null)
				result.add(listener);
			else {
				i.remove();
			}
		}

		return result;
	}

	protected int getListenerCount() {
		return observerCatalog.size() + weakObserverCatalog.size();
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean b) {
		blocked = b;
	}

	public boolean hasListener(Listener listener) {
		for (WeakReference<Listener> weakObserver : weakObserverCatalog) {
			if (listener == weakObserver.get())
				return true;
		}

		for (Listener observer : observerCatalog) {
			if (listener == observer)
				return true;
		}

		return false;
	}

	public void removeWrappedListener(Listener listener) {
		removeListener(listener);

		if (wrappedListeners_ != null)
			wrappedListeners_.remove(listener);
		
		if (weakWrappedListeners_ != null)
			weakWrappedListeners_.remove(new WeakReference<Listener>(listener));
	}
}
