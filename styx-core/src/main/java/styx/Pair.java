package styx;

public final class Pair<K, V> {

    private final K key;
    private final V val;

    public Pair(K key, V val) {
        this.key = key;
        this.val = val;
    }

    public K key() {
        return this.key;
    }

    public V val() {
        return this.val;
    }
}
