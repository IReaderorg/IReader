package android.graphics;

public class Picture {
    public int getWidth() { return 0; }
    public int getHeight() { return 0; }
    public Canvas beginRecording(int width, int height) { return new Canvas(); }
    public void endRecording() {}
    public void draw(Canvas canvas) {}
    public Picture deepCopy() { return new Picture(); }
}
