package android.webkit;

public abstract class WebViewRenderProcess {
    public abstract void terminate();
    public abstract int getRenderProcessPid();
    public abstract String getRenderProcessGoneDetail();
}
