package org.cef.handler;

public interface CefResourceHeaderAccessor {
    void setHeader(String name, String value, boolean overwrite);
}
