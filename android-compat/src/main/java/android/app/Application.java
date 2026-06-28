package android.app;

public class Application extends android.content.ContextWrapper {
    public Application() { super(null); }
    public void onCreate() {}
    public final void attach(android.content.Context context) { attachBaseContext(context); }
}
