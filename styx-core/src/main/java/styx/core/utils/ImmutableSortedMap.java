package styx.core.utils;

import styx.Pair;

public interface ImmutableSortedMap<K, V> extends Iterable<Pair<K, V>> {

    public boolean isEmpty();

    public boolean hasSingle();

    public boolean hasMany();

    public V get(K key);

    public ImmutableSortedMap<K, V> put(K key, V val);

    public Pair<K, V> single();

    public Pair<K, V> find(K key, boolean forward);
}
