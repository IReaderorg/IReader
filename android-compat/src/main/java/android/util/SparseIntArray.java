package android.util;
public class SparseIntArray {
    public SparseIntArray() {}
    public void put(int key, int value) {}
    public int get(int key) { return get(key, 0); }
    public int get(int key, int valueIfKeyNotFound) { return valueIfKeyNotFound; }
    public int size() { return 0; }
    public int keyAt(int index) { return 0; }
    public int valueAt(int index) { return 0; }
    public int indexOfKey(int key) { return -1; }
}
