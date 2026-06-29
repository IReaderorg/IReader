package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefMediaAccessCallback;

public class CefPermissionHandlerAdapter implements CefPermissionHandler {
    public boolean onRequestMediaAccessPermission(CefBrowser browser, CefFrame frame, String requestingUrl, int requestedPermissions, CefMediaAccessCallback callback) { return false; }
}
