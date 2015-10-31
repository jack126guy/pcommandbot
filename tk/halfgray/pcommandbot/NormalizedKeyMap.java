/* Copyright (c) 2015 Jack126Guy. Refer to /LICENSE.txt for details. */
package tk.halfgray.pcommandbot;

import java.util.Map;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;

/**
 * {@link Map} that normalizes keys on insertion and retrieval.
 */
public class NormalizedKeyMap<K, V> extends AbstractMap<K, V> {
	/**
	 * Utility for normalizing keys
	 */
	public interface Normalizer<K> {
		/**
		 * Normalize a key. Note that the given key is an {@link Object},
		 * so this function must be prepared to normalize objects that
		 * are not of type {@code K}. One strategy may be to return
		 * {@code null}.
		 */
		K normalize(Object key);
	}

	/**
	 * Map responsible for holding the entries
	 */
	private Map<K, V> backingmap;

	/**
	 * Normalizer for this map
	 */
	private Normalizer<? extends K> norm;

	/**
	 * Create a new empty map.
	 * @param normalizer Normalizer to use for the keys in this map
	 */
	public NormalizedKeyMap(Normalizer<? extends K> normalizer) {
		this(Collections.<K, V>emptyMap(), normalizer);
	}

	/**
	 * Create a new map with the same entries as the given map.
	 * The keys from the map are normalized before inserting
	 * into this map.
	 * @param map Map of entries to copy
	 * @param normalizer Normalizer to use for the keys in this map
	 */
	public NormalizedKeyMap(Map<? extends K, ? extends V> map, Normalizer<? extends K> normalizer) {
		backingmap = new HashMap<K, V>();
		norm = normalizer;
		//AbstractMap.putAll() will call our put()
		putAll(map);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return backingmap.entrySet();
	}

	@Override
	public boolean containsKey(Object key) {
		return backingmap.containsKey(norm.normalize(key));
	}

	@Override
	public V get(Object key) {
		return backingmap.get(norm.normalize(key));
	}

	@Override
	public V put(K key, V value) {
		return backingmap.put(norm.normalize(key), value);
	}

	@Override
	public V remove(Object key) {
		return backingmap.remove(norm.normalize(key));
	}
}
