package android.app;
import android.content.Intent;
import android.content.Context;
public class PendingIntent {
    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) { return new PendingIntent(); }
    public static final int FLAG_IMMUTABLE = 67108864;
    public static final int FLAG_MUTABLE = 33554432;
}
