package android.webkit;

public class RenderProcessGoneDetail {
    private boolean crashed;
    private int priority;

    public RenderProcessGoneDetail() {
        this.crashed = false;
        this.priority = -1;
    }

    public RenderProcessGoneDetail(boolean didCrash, int rendererPriorityAtExit) {
        this.crashed = didCrash;
        this.priority = rendererPriorityAtExit;
    }

    public boolean didCrash() { return crashed; }
    public int rendererPriorityAtExit() { return priority; }
}
