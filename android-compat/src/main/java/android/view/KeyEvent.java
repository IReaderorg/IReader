package android.view;

public class KeyEvent {
    public interface Callback {
        boolean onKeyDown(int keyCode, KeyEvent event);
        boolean onKeyLongPress(int keyCode, KeyEvent event);
        boolean onKeyUp(int keyCode, KeyEvent event);
        boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event);
    }
}
