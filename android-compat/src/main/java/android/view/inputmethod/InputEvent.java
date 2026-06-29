package android.view.inputmethod;

public class InputEvent {
    public int getDeviceId() { return 0; }
    public long getEventTime() { return 0; }
    public long getDownTime() { return 0; }
    public int getMetaState() { return 0; }
    public boolean isCtrlPressed() { return false; }
    public boolean isAltPressed() { return false; }
    public boolean isShiftPressed() { return false; }
    public void recycle() {}
}
