package org.cef.handler;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;

import java.awt.Rectangle;
import java.nio.ByteBuffer;

public interface CefRenderHandler {
    Rectangle getViewRect(CefBrowser browser);
    void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height);
}
