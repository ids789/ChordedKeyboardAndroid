/*
    Class for rendering chord icons
    Displays 5 dots corresponding to the fingers needed for a specific chord
 */

package com.ids789.chordedkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ChordRefView extends View {
    Paint paint = new Paint();
    int chord = 0;

    int[][] btn_coords = {{28,26}, {6,14}, {20,6}, {36,6}, {50,14}};
    int radius = 6;

    public ChordRefView(Context context) {
        super(context);
    }

    public ChordRefView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setChord(int chord) {
        this.chord = chord;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < 5; i++) {
            if ((chord & (1 << (4 - i))) == 0)
                paint.setColor(getResources().getColor(R.color.chord_btn_released));
            else
                paint.setColor(getResources().getColor(R.color.chord_btn_pressed));
            canvas.drawCircle(btn_coords[i][0], btn_coords[i][1], radius, paint);
        }
    }
}

