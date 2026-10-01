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

/** Two-octave multitouch keyboard; reports MIDI pitch rather than encoded frequency strings. */
final class PianoKeyboardView extends View {
    interface NoteListener {
        void onNote(int midi, String name);
    }

    private static final int[] WHITE_KEYS = {0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23};
    private static final int[] BLACK_KEYS = {1, 3, 6, 8, 10, 13, 15, 18, 20, 22};
    private static final float[] BLACK_CENTERS = {1, 2, 4, 5, 6, 8, 9, 11, 12, 13};
    private static final String[] NOTE_NAMES = {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<Integer, Integer> activePointers = new HashMap<>();
    private NoteListener listener;
    private int octave = 4;

    PianoKeyboardView(Context context) {
        super(context);
        setBackgroundColor(Color.rgb(11, 13, 19));
        setFocusable(true);
    }

    void setOctave(int value) {
        octave = Math.max(2, Math.min(6, value));
        invalidate();
    }

    void setNoteListener(NoteListener value) {
        listener = value;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) return;
        float whiteWidth = getWidth() / 14f;
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        for (int index = 0; index < WHITE_KEYS.length; index++) {
            int semitone = WHITE_KEYS[index];
            paint.setColor(activePointers.containsValue(semitone)
                    ? Color.rgb(68, 221, 243) : Color.rgb(241, 243, 248));
            RectF key = new RectF(index * whiteWidth + 1, 1,
                    (index + 1) * whiteWidth - 1, getHeight() - 1);
            canvas.drawRoundRect(key, 5, 5, paint);
            paint.setColor(Color.rgb(23, 26, 34));
            paint.setTextSize(Math.max(12, Math.min(20, whiteWidth * 0.24f)));
            canvas.drawText(NOTE_NAMES[semitone % 12] + (octave + semitone / 12),
                    key.centerX(), getHeight() - 12, paint);
        }
        float blackWidth = whiteWidth * 0.6f;
        float blackHeight = getHeight() * 0.58f;
        for (int index = 0; index < BLACK_KEYS.length; index++) {
            int semitone = BLACK_KEYS[index];
            float center = BLACK_CENTERS[index] * whiteWidth;
            paint.setColor(activePointers.containsValue(semitone)
                    ? Color.rgb(174, 119, 255) : Color.rgb(20, 22, 30));
            canvas.drawRoundRect(new RectF(center - blackWidth / 2, 0,
                    center + blackWidth / 2, blackHeight), 5, 5, paint);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            press(event.getPointerId(pointerIndex), event.getX(pointerIndex), event.getY(pointerIndex));
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            for (int index = 0; index < event.getPointerCount(); index++) {
                int pointer = event.getPointerId(index);
                Integer previous = activePointers.get(pointer);
                int next = noteAt(event.getX(index), event.getY(index));
                if (previous == null || previous != next)
                    press(pointer, event.getX(index), event.getY(index));
            }
            return true;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            activePointers.remove(event.getPointerId(pointerIndex));
            invalidate();
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            activePointers.clear();
            invalidate();
            return true;
        }
        return true;
    }

    private void press(int pointer, float x, float y) {
        int semitone = noteAt(x, y);
        activePointers.put(pointer, semitone);
        if (listener != null) {
            int midi = 12 * (octave + 1) + semitone;
            listener.onNote(midi, NOTE_NAMES[semitone % 12] + (octave + semitone / 12));
        }
        invalidate();
    }

    private int noteAt(float x, float y) {
        float whiteWidth = Math.max(1f, getWidth() / 14f);
        if (y < getHeight() * 0.58f) {
            float blackWidth = whiteWidth * 0.6f;
            for (int index = 0; index < BLACK_CENTERS.length; index++) {
                float center = BLACK_CENTERS[index] * whiteWidth;
                if (x >= center - blackWidth / 2 && x <= center + blackWidth / 2)
                    return BLACK_KEYS[index];
            }
        }
        int white = Math.max(0, Math.min(13, (int) (x / whiteWidth)));
        return WHITE_KEYS[white];
    }
}
