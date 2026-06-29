package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.network.CefRequest;

public interface CefResourceRequestHandler {
    boolean onBeforeResourceLoad(CefBrowser browser, CefFrame frame, CefRequest request);
    CefResourceHandler getResourceHandler(CefBrowser browser, CefFrame frame, CefRequest request);
}
