package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

final class PianoKeyboardView extends View {
    interface NoteListener { void onNote(double frequency, String name); }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<Integer, Integer> activePointers = new HashMap<>();
    private NoteListener listener;
    private int octave = 4;
    private static final int[] WHITE_SEMITONES = {0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23};
    private static final int[] BLACK_SEMITONES = {1, 3, 6, 8, 10, 13, 15, 18, 20, 22};
    private static final float[] BLACK_POSITIONS = {1, 2, 4, 5, 6, 8, 9, 11, 12, 13};
    private static final String[] NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    PianoKeyboardView(Context context) {
        super(context);
        setBackgroundColor(Color.rgb(10, 11, 16));
    }

    void setOctave(int value) { octave = Math.max(2, Math.min(6, value)); invalidate(); }
    void setNoteListener(NoteListener value) { listener = value; }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float whiteWidth = getWidth() / 14f;
        paint.setStyle(Paint.Style.FILL);
        for (int index = 0; index < 14; index++) {
            boolean active = activePointers.containsValue(WHITE_SEMITONES[index]);
            paint.setColor(active ? Color.rgb(55, 226, 255) : Color.rgb(242, 244, 249));
            RectF key = new RectF(index * whiteWidth + 1, 1, (index + 1) * whiteWidth - 1, getHeight() - 1);
            canvas.drawRoundRect(key, 5, 5, paint);
            paint.setColor(Color.rgb(20, 22, 30));
            paint.setTextSize(Math.max(18, whiteWidth * 0.24f));
            paint.setTextAlign(Paint.Align.CENTER);
            int semitone = WHITE_SEMITONES[index];
            canvas.drawText(NAMES[semitone % 12] + (octave + semitone / 12), key.centerX(), getHeight() - 18, paint);
        }
        float blackWidth = whiteWidth * 0.62f;
        float blackHeight = getHeight() * 0.62f;
        for (int index = 0; index < BLACK_SEMITONES.length; index++) {
            float center = BLACK_POSITIONS[index] * whiteWidth;
            boolean active = activePointers.containsValue(BLACK_SEMITONES[index]);
            paint.setColor(active ? Color.rgb(164, 92, 255) : Color.rgb(18, 20, 28));
            canvas.drawRoundRect(new RectF(center - blackWidth / 2, 0, center + blackWidth / 2, blackHeight), 5, 5, paint);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            int semitone = noteAt(event.getX(pointerIndex), event.getY(pointerIndex));
            activePointers.put(pointerId, semitone);
            fire(semitone);
            invalidate();
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            for (int index = 0; index < event.getPointerCount(); index++) {
                int id = event.getPointerId(index);
                int semitone = noteAt(event.getX(index), event.getY(index));
                Integer previous = activePointers.get(id);
                if (previous == null || previous != semitone) {
                    activePointers.put(id, semitone);
                    fire(semitone);
                }
            }
            invalidate();
            return true;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
            if (action == MotionEvent.ACTION_CANCEL) activePointers.clear();
            else activePointers.remove(pointerId);
            invalidate();
            return true;
        }
        return true;
    }

    private int noteAt(float x, float y) {
        float whiteWidth = getWidth() / 14f;
        if (y < getHeight() * 0.62f) {
            float blackWidth = whiteWidth * 0.62f;
            for (int index = 0; index < BLACK_POSITIONS.length; index++) {
                float center = BLACK_POSITIONS[index] * whiteWidth;
                if (x >= center - blackWidth / 2 && x <= center + blackWidth / 2) return BLACK_SEMITONES[index];
            }
        }
        int white = Math.max(0, Math.min(13, (int) (x / whiteWidth)));
        return WHITE_SEMITONES[white];
    }

    private void fire(int semitone) {
        if (listener == null) return;
        double midi = 12 * (octave + 1) + semitone;
        double frequency = 440.0 * Math.pow(2.0, (midi - 69.0) / 12.0);
        listener.onNote(frequency, NAMES[semitone % 12] + (octave + semitone / 12));
    }
}
