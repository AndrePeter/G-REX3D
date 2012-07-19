package com.andredittrich.main;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

class HelloOpenGLES20SurfaceView extends GLSurfaceView {

	private static final int INVALID_POINTER_ID = -1;
    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;
   
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
	private final float TOUCH_SCALE_FACTOR = 180.0f / 360;
    private HelloOpenGLES20Renderer mRenderer;
    private float mPreviousX;
    private float mPreviousY;
//    private float mdX;
//    private float mdY;
    private float xstart;
    private float ystart;
    public float mDensity = 1f;
    private float ysum = 0;
    private float yrealsum = 0;
    private float xsum = 0;
    private float xrealsum = 0;
    public static float dy = 0;
    public static float dx = 0;
	
	public HelloOpenGLES20SurfaceView(Context context) {
		
		super(context);

		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);
		setZOrderMediaOverlay(true);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		

		// set the mRenderer member
		mRenderer = new HelloOpenGLES20Renderer();
		setRenderer(mRenderer);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
//		getHolder().setFormat(PixelFormat.TRANSLUCENT);		
		// Render the view only when there is a change
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//.RENDERMODE_WHEN_DIRTY);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*
		 * only Handle Touch events if the Controls3D Button is linked
		 */
		
		mScaleDetector.onTouchEvent(event);
	        
	        final int action = event.getAction();
	        switch (action & MotionEvent.ACTION_MASK) {
	        case MotionEvent.ACTION_DOWN: {
	        	GREX3DActivity.myZoomBar.setEnabled(false);
	            final float x = event.getX();
	            final float y = event.getY();
                
	            xstart = x;
	            ystart = y;
//	            Log.d("yDown",Float.toString(ystart));
//	            Log.d("xDown",Float.toString(xstart));
	            mActivePointerId = event.getPointerId(0);
	            break;
	        }
	            
	        case MotionEvent.ACTION_MOVE: {
	            final int pointerIndex = event.findPointerIndex(mActivePointerId);
	            final float x = event.getX(pointerIndex);
	            final float y = event.getY(pointerIndex);
//	            Log.d("yMove",Float.toString(y));
//	            Log.d("xMove",Float.toString(x));
	            // Only move if the ScaleGestureDetector isn't processing a gesture.
	            if (!mScaleDetector.isInProgress()) {
	                float dx = x - xstart;
	                float dy = y - ystart;
//	                // reverse direction of rotation above the mid-line
//                if (y > getHeight() / 2) {
//                  dx = dx * -1 ;
//                }
//    
//                // reverse direction of rotation to left of the mid-line
//                if (x < getWidth() / 2) {
//                  dy = dy * -1 ;
//                }
//              
                mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;
                mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;
                
               	                
//	                float realx = (1.21f)*dx/(mRenderer.mHeight/2);
//	                float realy = (1.21f)*dy/(mRenderer.mHeight/2);
//	                
//	                Log.d("realdy", Float.toString(realy));
//	                Log.d("realdx", Float.toString(realx));
	                
//	                mRenderer.mdX += realx;
//	                mRenderer.mdY += realy;
	                mRenderer.mdX += (dx/mDensity/2.5f) * 0.008* HelloOpenGLES20Renderer.xExtent;
	                mRenderer.mdY += (dy/mDensity/2.5f) * 0.008* HelloOpenGLES20Renderer.xExtent;
	                
	                
//	                ysum += dy;
//	                yrealsum += realy;
//	                Log.d("ysum",Float.toString(ysum));
//	                Log.d("yrealsum",Float.toString(yrealsum));
//	                xsum += dx;
//	                xrealsum += realx;
//	                Log.d("xsum",Float.toString(xsum));
//	                Log.d("xrealsum",Float.toString(xrealsum));
	            }
	            requestRender();
	            xstart = x;
	            ystart = y;

	            break;
	        }
	            
	        case MotionEvent.ACTION_UP: {
	            mActivePointerId = INVALID_POINTER_ID;
	            break;
	        }
	            
	        case MotionEvent.ACTION_CANCEL: {
	            mActivePointerId = INVALID_POINTER_ID;
	            break;
	        }
	        
	        case MotionEvent.ACTION_POINTER_UP: {
	            final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
	                    >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
	            final int pointerId = event.getPointerId(pointerIndex);
	            if (pointerId == mActivePointerId) {
	                // This was our active pointer going up. Choose a new
	                // active pointer and adjust accordingly.
	                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
	                xstart = event.getX(newPointerIndex);
	                ystart = event.getY(newPointerIndex);
	                mActivePointerId = event.getPointerId(newPointerIndex);
	            }
	            break;
	        }
	        }
	        
	        return true;
	    }
	
	@Override
	public void requestRender() {
		// TODO Auto-generated method stub
		super.requestRender();
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
        	if (!HelloOpenGLES20Renderer.AR) {
            mScaleFactor *= detector.getScaleFactor();            
            
            // Don't let the object get too small or too large.
//            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
            mRenderer.scale = mScaleFactor;
            requestRender();
            return true;
        	} else {
        		return false;
        	}
        	
        }
    }


}