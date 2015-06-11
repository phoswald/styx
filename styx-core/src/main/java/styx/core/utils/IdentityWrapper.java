package styx.core.utils;

public final class IdentityWrapper <T> {

    private final T obj;

    public IdentityWrapper(T obj) {
        this.obj = obj;
    }

    public T unwrap() {
        return obj;
    }

    @Override
    public boolean equals(Object other) {
        return other != null &&
               other instanceof IdentityWrapper &&
               obj == ((IdentityWrapper<?>) other).obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(obj);
    }
}
