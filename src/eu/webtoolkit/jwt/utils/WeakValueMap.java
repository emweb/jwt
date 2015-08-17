package eu.webtoolkit.jwt.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WeakValueMap<K, V> implements Map<K, V> {
	private ReferenceQueue<V> referenceQueue = new ReferenceQueue<V>();

	private static class KeyedWeakReference<K, V> extends WeakReference<V> {
		private K key;
		
		KeyedWeakReference(K key, V value, ReferenceQueue<V> referenceQueue) {
			super(value, referenceQueue);
			this.key = key;
		}
		
		K getKey() { return key; }
	}

	private HashMap<K, KeyedWeakReference<K, V>> storage = new HashMap<K, KeyedWeakReference<K, V>>();
	
	public int size() {
		prune();
		return storage.size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean containsKey(Object key) {
		prune();
		return storage.containsKey(key);
	}

	public boolean containsValue(Object value) {
		for (KeyedWeakReference<K, V> v : storage.values()) {
			V vv = v.get();
			if (vv != null)
				if (value.equals(vv))
					return true;
		}
		return false;
	}

	public V get(Object key) {
		prune();
		WeakReference<V> v = storage.get(key);
		if (v != null)
			return v.get();
		else
			return null;
	}

	public V put(K key, V value) {
		storage.put(key, new KeyedWeakReference<K, V>(key, value, referenceQueue));
		return value;
	}

	public V remove(Object key) {
		KeyedWeakReference<K, V> k = storage.get(key);
		if (k != null) {
			V result = k.get();
			storage.remove(key);
			return result;
		} else
			return null;
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		for (K k : m.keySet())
			put(k, m.get(k));
	}

	public void clear() {
		storage.clear();
	}

	public Set<K> keySet() {
		prune();
		return storage.keySet();
	}

	public Collection<V> values() {
		Set<V> result = new HashSet<V>();
		
		for (KeyedWeakReference<K, V> v : storage.values()) {
			V vv = v.get();
			if (vv != null)
				result.add(vv);
		}
		
		return result;
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		prune();

		@SuppressWarnings("unused")
		Set<java.util.Map.Entry<K, V>> result = new HashSet<Map.Entry<K,V>>();
		
		for (final java.util.Map.Entry<K, KeyedWeakReference<K, V>> i : storage.entrySet()) {
			final V vv = i.getValue().get();
			if (vv != null)
				result.add(new Entry<K, V>() {
					@Override
					public K getKey() {
						return i.getKey();
					}

					@Override
					public V getValue() {
						return vv;
					}

					@Override
					public V setValue(V value) {
						throw new RuntimeException("Not implemented");
					}
				});
		}
		
		return result;
	}

	private void prune() {
		//System.gc();
		//Runtime.getRuntime().runFinalization();
		for (;;) {
			@SuppressWarnings("unchecked")
			KeyedWeakReference<K, V> ref = (KeyedWeakReference<K, V>) referenceQueue.poll();
			if (ref != null) {
				storage.remove(ref.getKey());
			} else
				break;
		}
	}	
}
