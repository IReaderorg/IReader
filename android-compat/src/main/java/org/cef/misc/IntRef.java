package org.cef.misc;

public class IntRef {
    public int myVal;
    public IntRef() {}
    public IntRef(int val) { myVal = val; }
    public void set(int val) { myVal = val; }
    public int get() { return myVal; }
}
