package org.cef;

public class CefApp {
    public static CefApp getInstance() { return new CefApp(); }
    public static CefApp getInstance(String[] args) { return new CefApp(); }
    public static CefApp getInstance(String[] args, CefSettings settings) { return new CefApp(); }

    public CefClient createClient() { return null; }

    @FunctionalInterface
    public interface CefStateListener {
        void onStateChange(CefAppState state);
    }

    public void onInitialization(CefStateListener listener) {}
    public void setState(State state) {}
    public State getState() { return State.TERMINATED; }
    public void runMessageLoop() {}
    public void quitMessageLoop() {}
    public void dispose() {}
    public void addStateListener(StateListener listener) {}
    public void removeStateListener(StateListener listener) {}

    public enum State { UNINITIALIZED, INITIALIZING, INITIALIZED, SHUTTING_DOWN, TERMINATED }
    public enum CefAppState { UNINITIALIZED, INITIALIZING, INITIALIZED, SHUTTING_DOWN, TERMINATED }

    public interface StateListener {
        void stateHasChanged(State state);
    }
}
