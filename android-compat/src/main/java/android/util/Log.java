package android.util;

public final class Log {
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;

    public static int v(String tag, String msg) { System.out.println("V/" + tag + ": " + msg); return 0; }
    public static int v(String tag, String msg, Throwable tr) { System.out.println("V/" + tag + ": " + msg); return 0; }
    public static int d(String tag, String msg) { System.out.println("D/" + tag + ": " + msg); return 0; }
    public static int d(String tag, String msg, Throwable tr) { System.out.println("D/" + tag + ": " + msg); return 0; }
    public static int i(String tag, String msg) { System.out.println("I/" + tag + ": " + msg); return 0; }
    public static int i(String tag, String msg, Throwable tr) { System.out.println("I/" + tag + ": " + msg); return 0; }
    public static int w(String tag, String msg) { System.err.println("W/" + tag + ": " + msg); return 0; }
    public static int w(String tag, String msg, Throwable tr) { System.err.println("W/" + tag + ": " + msg); return 0; }
    public static int w(String tag, Throwable tr) { System.err.println("W/" + tag); return 0; }
    public static int e(String tag, String msg) { System.err.println("E/" + tag + ": " + msg); return 0; }
    public static int e(String tag, String msg, Throwable tr) { System.err.println("E/" + tag + ": " + msg); return 0; }
    public static boolean isLoggable(String tag, int level) { return true; }
    public static int println(int priority, String tag, String msg) { return 0; }
    public static String getStackTraceString(Throwable tr) { return tr.toString(); }
}
