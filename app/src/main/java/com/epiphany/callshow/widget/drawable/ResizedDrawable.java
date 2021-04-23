package com.epiphany.callshow.widget.drawable;

import android.graphics.Canvas;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;


public class ResizedDrawable extends LayerDrawable {

    private Paint paint;
    private int width;
    private int height;
    private Drawable target;


    public ResizedDrawable(Drawable target, int width, int height) {
        super(new Drawable[] { target });
        this.width = width;
        this.height = height;
        this.target = target;
        target.setBounds(0, 0, target.getIntrinsicWidth(), target.getIntrinsicHeight());
        paint = new Paint();
        paint.setAntiAlias(true);
    }


    public ResizedDrawable(Drawable target, int size) {
        this(target, size, size);
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.getSaveCount();
        canvas.save();
        super.draw(canvas);
        canvas.getMatrix().setRectToRect(
                new RectF(0, 0, target.getIntrinsicWidth(), target.getIntrinsicHeight()),
                new RectF(0, 0, width, height),
                ScaleToFit.CENTER);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

}