package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.network.CefRequest;

public class CefResourceRequestHandlerAdapter implements CefResourceRequestHandler {
    public boolean onBeforeResourceLoad(CefBrowser browser, CefFrame frame, CefRequest request) { return false; }
    public CefResourceHandler getResourceHandler(CefBrowser browser, CefFrame frame, CefRequest request) { return null; }
}
