package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.network.CefRequest;

public interface CefLoadHandler {
    void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward);
    void onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType);
    void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl);
    void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode);

    public enum ErrorCode {
        ERR_NONE, ERR_FAILED, ERR_ABORTED, ERR_INVALID_ARGUMENT, ERR_INVALID_HANDLE,
        ERR_FILE_NOT_FOUND, ERR_TIMED_OUT, ERR_CONNECTION_CLOSED, ERR_CONNECTION_RESET,
        ERR_CONNECTION_REFUSED, ERR_CONNECTION_ABORTED, ERR_CONNECTION_FAILED,
        ERR_NAME_NOT_RESOLVED, ERR_INTERNET_DISCONNECTED, ERR_SSL_PROTOCOL_ERROR,
        ERR_ADDRESS_INVALID, ERR_ADDRESS_UNREACHABLE, ERR_UNKNOWN_URL_SCHEME,
        ERR_UNEXPECTED, ERR_ACCESS_DENIED, ERR_NOT_IMPLEMENTED,
        ERR_INSUFFICIENT_RESOURCES, ERR_OUT_OF_MEMORY, ERR_MAX
    }
}
