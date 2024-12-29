/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import eu.webtoolkit.jwt.servlet.UploadedFile;

public class OrderedMultiMap<T_INDEX, T_VALUE> {
	private TreeMap<T_INDEX, ArrayList<T_VALUE>> _treeMap = new TreeMap<T_INDEX, ArrayList<T_VALUE>>();
	private int size_ = 0;

	public class MultiMapEntry implements Entry<T_INDEX, T_VALUE> {

		private T_INDEX key;
		private T_VALUE value;

		MultiMapEntry(T_INDEX key, T_VALUE value) {
			this.key = key;
			this.value = value;
		}

		public T_INDEX getKey() {
			return key;
		}

		public T_VALUE getValue() {
			return value;
		}

		public T_VALUE setValue(T_VALUE arg0) {
			return null;
		}

	}

	public class EntrySetIterator implements Iterator<Entry<T_INDEX, T_VALUE>> {

		private Iterator<T_INDEX> mapIterator;
		private int nextIndex;
		T_INDEX currentMapKey;

		EntrySetIterator() {
			mapIterator = _treeMap.keySet().iterator();
			currentMapKey = null;
			nextIndex = -1;
		}

		public boolean hasNext() {
			if (nextIndex == -1)
				return mapIterator.hasNext();
			else
				return true;
		}

		public Entry<T_INDEX, T_VALUE> next() {
			if (nextIndex == -1) {
				currentMapKey = mapIterator.next();
				nextIndex = 0;
			}

			ArrayList<T_VALUE> list = _treeMap.get(currentMapKey);
			T_VALUE v = list.get(nextIndex++);
			if (nextIndex == list.size())
				nextIndex = -1;

			return new MultiMapEntry(currentMapKey, v);
		}

		public void remove() {
		}

	}

	public class EntrySet extends AbstractSet<Entry<T_INDEX, T_VALUE>> {

		@Override
		public Iterator<Entry<T_INDEX, T_VALUE>> iterator() {

			return new EntrySetIterator();
		}

		@Override
		public int size() {
			return size_;
		}
	}

	public void put(T_INDEX index, T_VALUE value) {
		ArrayList<T_VALUE> list = _treeMap.get(index);
		if (list == null) {
			list = new ArrayList<T_VALUE>();
			_treeMap.put(index, list);
		}
		list.add(value);

		++size_;
	}

	public Set<Map.Entry<T_INDEX, T_VALUE>> entrySet() {
		return new EntrySet();
	}

	public void find(T_INDEX key, List<T_VALUE> files) {
		for (Map.Entry<T_INDEX, ArrayList<T_VALUE>> e : _treeMap.entrySet()) 
			if (e.getKey().equals(key))
				files.addAll(e.getValue());
	}
}
