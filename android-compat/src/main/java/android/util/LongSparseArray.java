package android.util;

public class LongSparseArray<E> {
    public LongSparseArray() {}
    public LongSparseArray(int initialCapacity) {}
    public E get(long key) { return null; }
    public E get(long key, E valueIfKeyNotFound) { return valueIfKeyNotFound; }
    public void delete(long key) {}
    public void remove(long key) {}
    public void put(long key, E value) {}
    public int size() { return 0; }
    public long keyAt(int index) { return 0; }
    public E valueAt(int index) { return null; }
    public int indexOfKey(long key) { return -1; }
    public void clear() {}
    public void append(long key, E value) {}
}
