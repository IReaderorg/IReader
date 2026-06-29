package org.cef.network;

import java.util.HashMap;
import java.util.Map;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;

public class CefRequest {
    public String url = "";
    public String method = "GET";
    public HashMap<String, String> headerMap = new HashMap<>();
    public CefPostData postData;

    public String getURL() { return url; }
    public void setURL(String url) { this.url = url; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public HashMap<String, String> getHeaderMap() { return headerMap; }
    public void getHeaderMap(Map<String, String> headerMap) { headerMap.putAll(this.headerMap); }
    public void setHeaderByName(String name, String value, boolean overwrite) { headerMap.put(name, value); }
    public CefPostData getPostData() { return postData; }
    public void setPostData(CefPostData postData) { this.postData = postData; }
    public String getInitiator() { return ""; }
    public boolean isReadOnly() { return false; }

    public enum TransitionType {
        TT_EXPLICIT,
        TT_AUTO_SUBFRAME,
        TT_MANUAL_SUBFRAME,
        TT_GENERATED,
        TT_AUTO_TOPLEVEL,
        TT_FORM_SUBMIT,
        TT_RELOAD,
        TT_KEYWORD,
        TT_KEYWORD_GENERATED;
    }

    public enum TransitionFlags {
        TF_NONE,
        TF_BLOCKED_ADVERTISEMENT,
        TF_BLOCKED_REDIRECT,
    }
}
