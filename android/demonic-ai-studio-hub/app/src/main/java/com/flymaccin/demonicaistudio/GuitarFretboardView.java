package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

final class GuitarFretboardView extends View {
    interface NoteListener { void onNote(double frequency, String label); }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] standardMidi = {64, 59, 55, 50, 45, 40};
    private final int[] dropDMidi = {64, 59, 55, 50, 45, 38};
    private boolean dropD;
    private NoteListener listener;
    private int activeString = -1;
    private int activeFret = -1;
    private static final String[] NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    GuitarFretboardView(Context context) {
        super(context);
        setBackgroundColor(Color.rgb(21, 16, 12));
    }

    void setDropD(boolean value) { dropD = value; invalidate(); }
    void setNoteListener(NoteListener value) { listener = value; }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float labelWidth = Math.max(58, getWidth() * 0.07f);
        float fretWidth = (getWidth() - labelWidth) / 9f;
        float stringGap = getHeight() / 6f;
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.MONOSPACE);

        for (int fret = 0; fret < 9; fret++) {
            paint.setColor(fret == 0 ? Color.rgb(48, 36, 26) : (fret % 2 == 0 ? Color.rgb(74, 48, 29) : Color.rgb(65, 42, 26)));
            RectF area = new RectF(labelWidth + fret * fretWidth, 0, labelWidth + (fret + 1) * fretWidth - 2, getHeight());
            canvas.drawRect(area, paint);
            paint.setColor(Color.rgb(235, 218, 180));
            paint.setTextSize(15);
            canvas.drawText(fret == 0 ? "OPEN" : String.valueOf(fret), area.centerX(), 18, paint);
        }
        for (int string = 0; string < 6; string++) {
            float y = (string + 0.5f) * stringGap;
            int midi = tuning()[string];
            paint.setColor(Color.rgb(255, 206, 84));
            paint.setTextSize(18);
            canvas.drawText(NAMES[midi % 12], labelWidth / 2, y + 6, paint);
            paint.setStrokeWidth(1.5f + string * 0.55f);
            paint.setColor(Color.rgb(220, 206, 176));
            canvas.drawLine(labelWidth, y, getWidth(), y, paint);
        }
        if (activeString >= 0) {
            paint.setColor(Color.argb(190, 164, 92, 255));
            float left = labelWidth + activeFret * fretWidth;
            float top = activeString * stringGap;
            canvas.drawRoundRect(new RectF(left + 3, top + 3, left + fretWidth - 5, top + stringGap - 3), 8, 8, paint);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            float labelWidth = Math.max(58, getWidth() * 0.07f);
            float fretWidth = (getWidth() - labelWidth) / 9f;
            int string = Math.max(0, Math.min(5, (int) (event.getY() / (getHeight() / 6f))));
            int fret = Math.max(0, Math.min(8, (int) ((event.getX() - labelWidth) / fretWidth)));
            if (event.getX() < labelWidth) fret = 0;
            if (string != activeString || fret != activeFret || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                activeString = string;
                activeFret = fret;
                int midi = tuning()[string] + fret;
                double frequency = 440.0 * Math.pow(2.0, (midi - 69.0) / 12.0);
                if (listener != null) listener.onNote(frequency, NAMES[midi % 12] + (midi / 12 - 1));
                invalidate();
            }
            return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            activeString = -1;
            activeFret = -1;
            invalidate();
            return true;
        }
        return true;
    }

    private int[] tuning() { return dropD ? dropDMidi : standardMidi; }
}
