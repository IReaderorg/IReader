package android.webkit;

import java.util.concurrent.Executor;

public interface WebViewRenderProcessClient {
    void onRenderProcessUnresponsive(WebView view, WebViewRenderProcess renderProcess);
    void onRenderProcessResponsive(WebView view, WebViewRenderProcess renderProcess);
}
