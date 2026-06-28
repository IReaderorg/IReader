package org.cef.network;

import java.util.Vector;

public class CefPostData {
    public static CefPostData create() { return new CefPostData(); }
    public Vector<CefPostDataElement> getElements() { return new Vector<>(); }
    public boolean addElement(CefPostDataElement element) { return true; }
    public void removeElement(CefPostDataElement element) {}
    public void removeElements() {}
    public int getElementCount() { return 0; }
    public boolean isReadOnly() { return false; }
}
