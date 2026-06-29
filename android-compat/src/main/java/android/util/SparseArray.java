package android.util;

public class SparseArray<E> {
    public SparseArray() {}
    public SparseArray(int initialCapacity) {}
    public E get(int key) { return null; }
    public E get(int key, E valueIfKeyNotFound) { return valueIfKeyNotFound; }
    public void delete(int key) {}
    public void remove(int key) {}
    public void removeAt(int index) {}
    public void put(int key, E value) {}
    public int size() { return 0; }
    public int keyAt(int index) { return 0; }
    public E valueAt(int index) { return null; }
    public int indexOfKey(int key) { return -1; }
    public int indexOfValue(E value) { return -1; }
    public void clear() {}
    public void append(int key, E value) {}
    public int growingCount() { return 0; }
}
