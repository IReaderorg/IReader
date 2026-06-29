package org.cef.callback;

public interface CefMediaAccessCallback {
    static final int MEDIA_ACCESS_PERMISSION_NONE = 0;
    static final int MEDIA_ACCESS_PERMISSION_CAMERA = 1;
    static final int MEDIA_ACCESS_PERMISSION_MICROPHONE = 2;
    static final int MEDIA_ACCESS_PERMISSION_VIDEO = 4;
    void Continue(int allowedMediaTypes);
    void Cancel();
}
