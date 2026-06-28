package org.cef.handler;

import org.cef.browser.CefBrowser;

import java.awt.Rectangle;
import java.nio.ByteBuffer;

public class CefRenderHandlerAdapter implements CefRenderHandler {
    public Rectangle getViewRect(CefBrowser browser) { return new Rectangle(0, 0, 1280, 720); }
    public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height) {}
}
