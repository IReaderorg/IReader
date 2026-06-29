package org.cef.browser;

import java.awt.Rectangle;

public interface CefBrowser {
    String getURL();
    void loadURL(String url);
    void loadURL(String url, java.util.Map<String, String> headers);
    void loadString(String content, String url);
    void loadString(String content, String url, String mimeType, String encoding, String historyUrl);
    void executeJavaScript(String code, String url, int line);
    void stopLoad();
    void reload();
    void reloadIgnoringCache();
    boolean canGoBack();
    boolean canGoForward();
    void goBack();
    void goForward();
    boolean isCloseAllowed();
    void setCloseAllowed();
    void close();
    void close(boolean forceClose);
    CefFrame getMainFrame();
    CefFrame[] getAllFrames();
    CefFrame getFocusedFrame();
    org.cef.CefClient getClient();
    Rectangle getViewRect();
    void setWindowVisibility(boolean visible);
    void setFocused(boolean focused);
    void createImmediately();
}
