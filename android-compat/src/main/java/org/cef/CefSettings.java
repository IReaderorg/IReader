package org.cef;

public class CefSettings {
    public String cache_path = "";
    public String user_agent = "";
    public String product_version = "";
    public boolean pack_loading_disabled = false;
    public int log_severity = 0;

    public enum LogSeverity { DEFAULT, VERBOSE, INFO, WARNING, ERROR, ERROR_REPORT, DISABLE }

    public enum MultiThreadedMessageLoop { DEFAULT, DISABLED, ENABLED }
}
