package org.cef.network;

public class CefPostDataElement {
    public static final int PDE_TYPE_EMPTY = 0;
    public static final int PDE_TYPE_BYTES = 1;
    public static final int PDE_TYPE_FILE = 2;

    private byte[] bytes;
    private String file;
    private int type = PDE_TYPE_EMPTY;

    public static CefPostDataElement create() { return new CefPostDataElement(); }
    public int getType() { return type; }
    public String getFile() { return file; }
    public void setFile(String fileName) { this.file = fileName; this.type = PDE_TYPE_FILE; }
    public byte[] getBytes() { return bytes; }
    public void setToBytes(int size, byte[] bytes) { this.bytes = bytes; this.type = PDE_TYPE_BYTES; }
    public void setToBytes(byte[] bytes) { this.bytes = bytes; this.type = PDE_TYPE_BYTES; }
    public void setToEmpty() { this.type = PDE_TYPE_EMPTY; }
    public int getBytesCount() { return bytes != null ? bytes.length : 0; }
}
