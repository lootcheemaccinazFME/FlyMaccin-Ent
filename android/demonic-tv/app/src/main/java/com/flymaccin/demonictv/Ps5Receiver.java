package com.flymaccin.demonictv;

import android.content.Context;
import android.view.Surface;

/**
 * Clean integration boundary for a PS5 Remote Play-compatible receiver.
 * No third-party copyleft implementation code is embedded here.
 */
public final class Ps5Receiver {
    public enum State { IDLE, DISCOVERING, PAIRING, CONNECTING, STREAMING, SUSPENDED, ERROR }
    public interface Listener { void onState(State state, String detail); }
    private State state = State.IDLE;
    private Listener listener;
    private Surface videoSurface;
    private boolean controllerLocked = true;
    private boolean backgroundAudio = true;
    private boolean captureAllowed = false;
    public Ps5Receiver(Context context) {}
    public void setListener(Listener l) { listener = l; }
    public State getState() { return state; }
    public void attachSurface(Surface surface) { videoSurface = surface; }
    public void detachSurface() { videoSurface = null; }
    public void discover() { transition(State.DISCOVERING, "Searching for PS5"); }
    public void beginPairing() { transition(State.PAIRING, "Pairing flow ready"); }
    public void connect() { transition(State.CONNECTING, "Receiver transport not yet implemented"); }
    public void disconnect() { transition(State.IDLE, "Disconnected"); }
    public void setControllerLocked(boolean value) { controllerLocked = value; }
    public boolean isControllerLocked() { return controllerLocked; }
    public void setBackgroundAudio(boolean value) { backgroundAudio = value; }
    public boolean isBackgroundAudio() { return backgroundAudio; }
    public boolean isCaptureAllowed() { return captureAllowed; }
    public void setCaptureAllowed(boolean allowed) { captureAllowed = allowed; }
    public boolean sendControllerEvent(android.view.KeyEvent event) {
        return state == State.STREAMING && controllerLocked;
    }
    private void transition(State s, String d) {
        state = s;
        if (listener != null) listener.onState(s, d);
    }
}
