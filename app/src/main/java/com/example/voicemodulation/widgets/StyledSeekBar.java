package com.example.voicemodulation.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.voicemodulation.R;

public class StyledSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {
    public StyledSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setProgress(5);
        setRotation(270);
        //setMax(10);
        setBackgroundColor(Color.BLACK);
        Drawable t = context.getDrawable(R.drawable.seekbar_thumb);
        setThumb(t);
    }

    @Override
    protected void onMeasure(int width, int height) {
        float p = Resources.getSystem().getDisplayMetrics().density;
        int h = (int) (100 * p);
        int w = (int) (90 * p);
        super.onMeasure(w, h);
        setMeasuredDimension(w, h);
    }
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        DisplayMetrics display = Resources.getSystem().getDisplayMetrics();
        int dp = (int) display.density;
        paint.setStrokeWidth(5);
        int height = getHeight();
        int width = getWidth();
        int spacing = width / getMax();
        int tickwidth = dp;
        if (getMax() > 2) {
            for (int i = 1; i < getMax(); i++) {
                width -= spacing;
                canvas.drawLine((width) + (tickwidth / 2), height / 2 - 80, (width) - (tickwidth / 2), height / 2 - 80, paint);
                canvas.drawLine((width) + (tickwidth / 2), height / 2 + 80, (width) - (tickwidth / 2), height / 2 + 80, paint);
            }
        }
    }
}
