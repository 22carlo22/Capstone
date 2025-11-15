package com.example.myapplication.ui.calendar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class CircleDrawable extends Drawable {
    private final Paint paint;

    private final int num;

    public CircleDrawable(int num) {

        if(num > 4) this.num = 4;
        else this.num = num;

        this.paint = new Paint();
        paint.setColor(Color.GRAY);

    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(40,30,num*7,paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}