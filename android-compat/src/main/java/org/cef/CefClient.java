package org.cef;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefRendering;
import org.cef.handler.CefDisplayHandler;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefPermissionHandler;
import org.cef.handler.CefRequestHandler;

import java.util.HashMap;

public class CefClient {
    public CefBrowser createBrowser(String url, boolean useOSR, boolean transparent) { return null; }
    public CefBrowser createBrowser(String url, boolean useOSR, boolean transparent, HashMap<String, String> requestContext) { return null; }
    public CefBrowser createBrowser(String url, CefRendering.CefRenderingWithHandler rendering, boolean transparent) { return null; }
    public CefBrowser createBrowser(String url, CefRendering rendering, boolean transparent) { return null; }
    public void setDisplayHandler(CefDisplayHandler handler) {}
    public void setLoadHandler(CefLoadHandler handler) {}
    public void setRequestHandler(CefRequestHandler handler) {}
    public void setPermissionHandler(CefPermissionHandler handler) {}
    public void addDisplayHandler(CefDisplayHandler handler) {}
    public void addLoadHandler(CefLoadHandler handler) {}
    public void addRequestHandler(CefRequestHandler handler) {}
    public void addPermissionHandler(CefPermissionHandler handler) {}
    public void addMessageRouter(org.cef.browser.CefMessageRouter router) {}
    public void removeMessageRouter(org.cef.browser.CefMessageRouter router) {}
    public void dispose() {}
}
