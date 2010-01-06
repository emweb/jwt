/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

class WSignalMapper2<T, E extends WAbstractEvent> extends WObject {
	private Signal2<T, E> mapped_;

	WSignalMapper2(WObject parent) {
		super(parent);
		mapped_ = new Signal2<T, E>();
	}

	WSignalMapper2() {
		this(null);
	}

	AbstractSignal.Connection mapConnect1(EventSignal1<E> signal, final T value) {
		return signal.addListener(this, new Signal1.Listener<E>() {

			public void trigger(E e) {
				mapped_.trigger(value, e);
			}
		});
	}

	public Signal2<T, E> mapped() {
		return mapped_;
	}

	public void removeMapping(T value) {
	}
}
