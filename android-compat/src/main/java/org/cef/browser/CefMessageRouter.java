package org.cef.browser;

import org.cef.handler.CefMessageRouterHandler;

public class CefMessageRouter {
    public static CefMessageRouter create() { return new CefMessageRouter(); }
    public static CefMessageRouter create(CefMessageRouterConfig config) { return new CefMessageRouter(); }
    public static CefMessageRouter create(CefMessageRouterConfig config, CefMessageRouterHandler handler) { return new CefMessageRouter(); }

    public static class CefMessageRouterConfig {
        public String jsQueryFunction = "cefQuery";
        public String jsCancelFunction = "cefQueryCancel";
    }
}
