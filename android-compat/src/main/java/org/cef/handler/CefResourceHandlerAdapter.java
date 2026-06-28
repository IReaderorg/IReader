package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCallback;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;

public class CefResourceHandlerAdapter implements CefResourceHandler {
    public boolean processRequest(CefRequest request, CefCallback callback) { return false; }
    public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl) {}
    public boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefCallback callback) { return false; }
    public void cancel() {}
}
