package android.webkit;

public class WebMessage {
    public WebMessage(String data) {}
    public WebMessage(String data, WebMessagePort[] ports) {}
    public String getData() { return null; }
    public WebMessagePort[] getPorts() { return new WebMessagePort[0]; }
}
