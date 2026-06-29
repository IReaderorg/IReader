package android.content.res;

import android.util.DisplayMetrics;

public class TypedArray {
    public int getIndexCount() { return 0; }
    public int getIndex(int at) { return 0; }
    public int getInt(int index, int defValue) { return defValue; }
    public String getString(int index) { return null; }
    public int getResourceId(int index, int defValue) { return defValue; }
    public void recycle() {}
    public boolean hasValue(int index) { return false; }
    public float getFloat(int index, float defValue) { return defValue; }
}
