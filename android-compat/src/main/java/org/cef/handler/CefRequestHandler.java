package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;

public interface CefRequestHandler {
    CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator, BoolRef disableDefaultHandling);
    void onRenderProcessTerminated(CefBrowser browser, TerminationStatus status, int errorCode, String errorString);
    boolean getAuthCredentials(CefBrowser browser, CefFrame frame, boolean isProxy, String host, int port, String realm, String scheme, org.cef.callback.CefCallback callback);
    boolean onCertificateError(CefBrowser browser, int errorCode, String requestUrl, org.cef.callback.CefCallback callback);

    public enum TerminationStatus {
        TS_PROCESS_ABNORMAL_TERMINATION,
        TS_PROCESS_WAS_KILLED,
        TS_PROCESS_CRASHED,
        TS_PROCESS_OOM,
        TS_PROCESS_NOT_RESPONDING
    }
}
