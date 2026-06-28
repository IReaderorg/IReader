package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.network.CefRequest;

public interface CefDisplayHandler {
    boolean onConsoleMessage(CefBrowser browser, org.cef.CefSettings.LogSeverity level, String message, String source, int line);
    void onAddressChange(CefBrowser browser, CefFrame frame, String url);
    void onStatusMessage(CefBrowser browser, String value);
    void onTitleChange(CefBrowser browser, String title);
    void onFullscreenModeChange(CefBrowser browser, boolean fullscreen);
    boolean onTooltip(CefBrowser browser, String text);
}
