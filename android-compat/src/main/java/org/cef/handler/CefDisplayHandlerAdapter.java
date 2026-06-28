package org.cef.handler;

import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;

public class CefDisplayHandlerAdapter implements CefDisplayHandler {
    public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity level, String message, String source, int line) { return false; }
    public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {}
    public void onStatusMessage(CefBrowser browser, String value) {}
    public void onTitleChange(CefBrowser browser, String title) {}
    public void onFullscreenModeChange(CefBrowser browser, boolean fullscreen) {}
    public boolean onTooltip(CefBrowser browser, String text) { return false; }
}
