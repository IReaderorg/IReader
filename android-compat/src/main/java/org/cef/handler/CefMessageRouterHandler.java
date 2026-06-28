package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;

public interface CefMessageRouterHandler {
    boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback);
    void onQueryCanceled(CefBrowser browser, CefFrame frame, long queryId);
}
