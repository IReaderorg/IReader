package android.util;
public abstract class Property<T, V> {
    public Property(Class<V> type, String name) {}
    public abstract V get(T object);
    public abstract void set(T object, V value);
}
