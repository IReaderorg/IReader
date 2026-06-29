package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCallback;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;

public interface CefResourceHandler {
    boolean processRequest(CefRequest request, CefCallback callback);
    void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl);
    boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefCallback callback);
    void cancel();
}
