package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/** Compact six-string, nine-fret touch surface with standard and Drop D tunings. */
final class GuitarFretboardView extends View {
    interface NoteListener {
        void onNote(int midi, String label);
    }

    private static final int[] STANDARD_MIDI = {64, 59, 55, 50, 45, 40};
    private static final int[] DROP_D_MIDI = {64, 59, 55, 50, 45, 38};
    private static final String[] NOTE_NAMES = {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private NoteListener listener;
    private boolean dropD;
    private int activeString = -1;
    private int activeFret = -1;

    GuitarFretboardView(Context context) {
        super(context);
        setBackgroundColor(Color.rgb(20, 15, 12));
        setFocusable(true);
    }

    void setDropD(boolean value) {
        dropD = value;
        invalidate();
    }

    void setNoteListener(NoteListener value) {
        listener = value;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) return;
        float labels = Math.max(dp(42), getWidth() * 0.07f);
        float fretWidth = (getWidth() - labels) / 9f;
        float stringGap = getHeight() / 6f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.MONOSPACE);
        for (int fret = 0; fret < 9; fret++) {
            paint.setColor(fret == 0 ? Color.rgb(51, 37, 26)
                    : fret % 2 == 0 ? Color.rgb(75, 48, 30) : Color.rgb(63, 41, 27));
            RectF area = new RectF(labels + fret * fretWidth, 0,
                    labels + (fret + 1) * fretWidth - 1, getHeight());
            canvas.drawRect(area, paint);
            paint.setColor(Color.rgb(230, 215, 183));
            paint.setTextSize(dp(12));
            canvas.drawText(fret == 0 ? "OPEN" : String.valueOf(fret), area.centerX(), dp(17), paint);
        }
        int[] tuning = dropD ? DROP_D_MIDI : STANDARD_MIDI;
        for (int string = 0; string < 6; string++) {
            float y = (string + 0.5f) * stringGap;
            int midi = tuning[string];
            paint.setColor(Color.rgb(255, 203, 92));
            paint.setTextSize(dp(14));
            canvas.drawText(NOTE_NAMES[midi % 12], labels / 2, y + dp(5), paint);
            paint.setStrokeWidth(dp(1) + string * 0.55f);
            paint.setColor(Color.rgb(218, 204, 176));
            canvas.drawLine(labels, y, getWidth(), y, paint);
        }
        if (activeString >= 0) {
            paint.setColor(Color.argb(170, 177, 117, 255));
            float left = labels + activeFret * fretWidth;
            float top = activeString * stringGap;
            canvas.drawRoundRect(new RectF(left + 2, top + 2, left + fretWidth - 3,
                    top + stringGap - 2), dp(6), dp(6), paint);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            float labels = Math.max(dp(42), getWidth() * 0.07f);
            float fretWidth = Math.max(1f, (getWidth() - labels) / 9f);
            int string = Math.max(0, Math.min(5, (int) (event.getY() / Math.max(1f, getHeight() / 6f))));
            int fret = event.getX() < labels ? 0
                    : Math.max(0, Math.min(8, (int) ((event.getX() - labels) / fretWidth)));
            if (string != activeString || fret != activeFret || action == MotionEvent.ACTION_DOWN) {
                activeString = string;
                activeFret = fret;
                int midi = (dropD ? DROP_D_MIDI : STANDARD_MIDI)[string] + fret;
                if (listener != null)
                    listener.onNote(midi, NOTE_NAMES[midi % 12] + (midi / 12 - 1));
                invalidate();
            }
            return true;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            activeString = -1;
            activeFret = -1;
            invalidate();
            return true;
        }
        return true;
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
