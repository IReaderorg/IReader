package android.webkit;

public abstract class WebMessagePort {
    public abstract void setWebMessageCallback(WebMessagePort.WebMessageCallback callback);
    public abstract void setWebMessageCallback(Executor executor, WebMessagePort.WebMessageCallback callback);
    public abstract void postMessage(String message);
    public abstract void postMessage(WebMessage message);
    public abstract void close();
    public abstract WebMessagePort[] createChannelPair();
    public abstract void transferTo(WebView webView, String targetOrigin);

    public interface WebMessageCallback {
        default void onMessage(WebMessagePort port, WebMessage message) {}
    }
}
