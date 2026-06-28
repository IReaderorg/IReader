package org.cef.browser;

public interface CefFrame {
    String getURL();
    void loadURL(String url);
    void executeJavaScript(String code, String url, int line);
    boolean isValid();
    String getName();
    boolean isMain();
}
