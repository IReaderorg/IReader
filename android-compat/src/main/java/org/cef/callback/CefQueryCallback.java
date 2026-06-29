package org.cef.callback;

public interface CefQueryCallback {
    void success(String response);
    void failure(int error_code, String error_message);
}
