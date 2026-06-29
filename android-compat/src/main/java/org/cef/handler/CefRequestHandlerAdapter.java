package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;

public class CefRequestHandlerAdapter implements CefRequestHandler {
    public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator, BoolRef disableDefaultHandling) { return null; }
    public void onRenderProcessTerminated(CefBrowser browser, CefRequestHandler.TerminationStatus status, int errorCode, String errorString) {}
    public boolean onBeforeBrowse(CefBrowser browser, CefFrame frame, CefRequest request, boolean user_gesture, boolean is_redirect) { return false; }
    public boolean onOpenURLFromTab(CefBrowser browser, CefFrame frame, String targetUrl, boolean user_gesture) { return false; }
    public boolean getAuthCredentials(CefBrowser browser, CefFrame frame, boolean isProxy, String host, int port, String realm, String scheme, org.cef.callback.CefCallback callback) { return false; }
    public boolean onCertificateError(CefBrowser browser, int errorCode, String requestUrl, org.cef.callback.CefCallback callback) { return false; }
}
