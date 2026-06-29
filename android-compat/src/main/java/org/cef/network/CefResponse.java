package org.cef.network;

import java.util.HashMap;

public class CefResponse {
    public int status = 200;
    public String statusText = "OK";
    public String mimeType = "text/html";
    public HashMap<String, String> headers = new HashMap<>();

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getHeader(String headerName) { return headers.get(headerName); }
    public void setHeaderByName(String headerName, String value, boolean overwrite) { headers.put(headerName, value); }
    public void getHeaderMap(HashMap<String, String> headerMap) { headerMap.putAll(headers); }
    public boolean isReadonly() { return false; }
    public void setError(int error) {}
}
