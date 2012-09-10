package com.andredittrich.view3d;

import com.andredittrich.opengles.ARRenderer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.SeekBar;


/**
 * This class extends the SeekBar class and is designed to work vertically.
 * 
 * @author Diogo Margues <diogohomemmarques@gmail.com>
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class VerticalSeekBar extends SeekBar {

   	public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);

        super.onDraw(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//    	ARActivity.removeLocUpdates();
    	setEnabled(true);
    	ARActivity.mPreview.mSurfaceView.setVisibility(SurfaceView.INVISIBLE);
    	
    	Log.d("eventy", Float.toString(event.getY()));
    	Log.d("getmax", Float.toString(getMax()));
    	
//        if (!isEnabled()) {
//            return false;
//        }

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
        case MotionEvent.ACTION_UP:
            setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
            ARRenderer.eyeZ = getMax() - (int) (getMax() * event.getY() / getHeight());
            Log.d("eye", Float.toString(ARRenderer.eyeZ));
            ARRenderer.XX = getMax() - (int) (getMax() * event.getY() / getHeight());
//            Log.d("WERT", Float.toString(HelloOpenGLES20Renderer.eyeZ));
            onSizeChanged(getWidth(), getHeight(), 0, 0);
            
            break;

        case MotionEvent.ACTION_CANCEL:
            break;
        }
        return true;
    }
}