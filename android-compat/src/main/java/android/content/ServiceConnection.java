package android.content;
import android.os.IBinder;
public interface ServiceConnection {
    void onServiceConnected(ComponentName name, IBinder service);
    void onServiceDisconnected(ComponentName name);
}
