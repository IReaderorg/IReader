package org.cef.browser;

import javax.swing.JPanel;
import org.cef.handler.CefRenderHandler;

public enum CefRendering {
    DEFAULT,
    SOFTWARE,
    HARDWARE;

    public static class CefRenderingWithHandler {
        public final CefRenderHandler handler;
        public final JPanel panel;

        public CefRenderingWithHandler(CefRenderHandler handler, JPanel panel) {
            this.handler = handler;
            this.panel = panel;
        }
    }
}
