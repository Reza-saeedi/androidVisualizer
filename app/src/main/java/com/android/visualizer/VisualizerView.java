package com.android.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView extends View {

    private byte[] mBytes;
    private float[] mPoints;
    private Rect mRect = new Rect();
    private Paint mPaint = new Paint();
    int min;

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBytes = null;
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorVisualizer));
    }

    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBytes == null) {
            return;
        }
        int some = 0;

        for (int i = 0; i < mBytes.length - 1; i++) {
            some += Math.abs(mBytes[i]);
        }
        if (min == 0)
            min = some;
        if(min>some)
            min=some;
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (some-min)/100, mPaint);

    }

}
