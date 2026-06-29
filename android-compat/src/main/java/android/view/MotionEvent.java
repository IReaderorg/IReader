package android.view;

public class MotionEvent {
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_CANCEL = 3;

    public int getAction() { return 0; }
    public int getActionMasked() { return 0; }
    public int getActionIndex() { return 0; }
    public float getX() { return 0; }
    public float getY() { return 0; }
    public float getX(int pointerIndex) { return 0; }
    public float getY(int pointerIndex) { return 0; }
    public int getPointerCount() { return 0; }
    public long getEventTime() { return 0; }
    public int getDeviceId() { return 0; }
    public int getEdgeFlags() { return 0; }
    public float getRawX() { return 0; }
    public float getRawY() { return 0; }
    public float getPressure() { return 0; }
    public float getPressure(int pointerIndex) { return 0; }
    public float getSize() { return 0; }
    public float getSize(int pointerIndex) { return 0; }
    public int getMetaState() { return 0; }
    public float getXPrecision() { return 0; }
    public float getYPrecision() { return 0; }
    public long getDownTime() { return 0; }

    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int metaState) {
        return new MotionEvent();
    }
    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int pointerIndex, int metaState) {
        return new MotionEvent();
    }
    public static MotionEvent obtain(MotionEvent other) {
        return new MotionEvent();
    }
    public void recycle() {}
}
