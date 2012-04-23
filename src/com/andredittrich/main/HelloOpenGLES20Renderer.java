package com.andredittrich.main;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public class HelloOpenGLES20Renderer implements Renderer {

	ArrayList<FloatBuffer> tris = new ArrayList<FloatBuffer>();
//	private FloatBuffer triangleVB;
	public float mAngleY =1f;
	public float mAngleX =1f;
	public float mdY;
	public float mdX;
	public float scale = 1.f;
	private int muMVPMatrixHandle;
	private float[] mMxMatrix = new float[16];
	private float[] mMyMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mMMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    /** Store the accumulated rotation. */
	private final float[] mAccumulatedRotation = new float[16];
	
	/** Store the current rotation. */
	private final float[] mCurrentRotation = new float[16];
	private final float[]mTemporaryMatrix = new float[16];
    public int mWidth;
    public int mHeight;
    
	
	
	private int mProgram;
    private int myPositionHandle;
    
	private final String vertexShaderCode = 
	        // This matrix member variable provides a hook to manipulate
	        // the coordinates of the objects that use this vertex shader
	        "uniform mat4 uMVPMatrix;   \n" +
	        
	        "attribute vec4 vPosition;  \n" +
	        "void main(){               \n" +
	        
	        // the matrix must be included as a modifier of gl_Position
	        " gl_Position = uMVPMatrix * vPosition; \n" +
	        
	        "}  \n";
	    
	    private final String fragmentShaderCode1 = 
	        "precision mediump float;  \n" +
	        "void main(){              \n" +
	        " gl_FragColor = vec4 (0.4, 0.3, 0.5, 1.0); \n" +
	        "}                         \n";
	    private final String fragmentShaderCode = 
		        "precision mediump float;  \n" +
		        "void main(){              \n" +
		        " gl_FragColor = vec4 (0.9, 0.76953125, 0.22265625, 1.0); \n" +
		        "}                         \n";
	
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
	    
        // Set the background frame color
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        
        // initialize the triangle vertex array
        initShapes();
//        GLES20.glEnable(GLES20.GL_CULL_FACE);
//		
//		// Enable depth testing
//		GLES20.glEnable(GLES20.GL_DEPTH_TEST);	
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        int fragmentShader1 = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode1);
        
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader1); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL program executables
        
        // get handle to the vertex shader's vPosition member
        myPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        Matrix.setIdentityM(mAccumulatedRotation, 0);
    }
    
    public void onDrawFrame(GL10 unused) {
    	Log.d("draw", "now");
    
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
     // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
//        Log.d("anzahl", Integer.toString(tris.size()));
        for (FloatBuffer triangleVB : tris) {
			// Prepare the triangle data
			GLES20.glVertexAttribPointer(myPositionHandle, 3, GLES20.GL_FLOAT,
					false, 12, triangleVB);
			GLES20.glEnableVertexAttribArray(myPositionHandle);
			
			
			// Create a rotation for the triangle
//	        long time = SystemClock.uptimeMillis() % 4000L;
//	        float angle = 0.090f * ((int) time);
			
			Matrix.setIdentityM(mMMatrix, 0);//.setRotateEulerM(mMMatrix, 0, mAngleY, mAngleX, 0);
			Matrix.translateM(mMMatrix, 0, -mdX, -mdY, 0);
			Matrix.scaleM(mMMatrix, 0, scale, scale, scale);
			Matrix.setIdentityM(mCurrentRotation, 0); 
			Matrix.rotateM(mCurrentRotation, 0, mAngleX, 0.0f, 1.0f, 0.0f);
			Matrix.rotateM(mCurrentRotation, 0, mAngleY, 1.0f, 0.0f, 0.0f);
			
			mAngleX = 0.0f;
			mAngleY = 0.0f;
	    	    	
	    	// Multiply the current rotation by the accumulated rotation, and then set the accumulated rotation to the result.
	    	Matrix.multiplyMM(mTemporaryMatrix, 0, mCurrentRotation, 0, mAccumulatedRotation, 0);
	    	System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotation, 0, 16);
	    	    	
	        // Rotate the cube taking the overall rotation into account.     	
	    	Matrix.multiplyMM(mTemporaryMatrix, 0, mMMatrix, 0, mAccumulatedRotation, 0);
	    	System.arraycopy(mTemporaryMatrix, 0, mMMatrix, 0, 16); 
	    	
			
//	        Matrix.setRotateM(mMxMatrix, 0, mAngleY, 1.0f, 0.0f, 0.0f);
//	        Matrix.setRotateM(mMyMatrix, 0, mAngleX, 0.0f, 1.0f, 0.0f);
//	        Matrix.multiplyMM(mMMatrix, 0, mMxMatrix, 0, mMyMatrix, 0);
			
//			Matrix.setLookAtM(mVMatrix, 0, mdX, mdX, -5f, mdX, mdY, 0f, 0f, 1.0f, 0.0f);
	    	Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
	        
//	        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
	        
	        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
			System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);
			// Apply a ModelView Projection transformation
			//Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
	        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
			// Draw the triangle
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		}
    }
    
    public void onSurfaceChanged(GL10 unused, int width, int height) {
    	GLES20.glViewport(0, 0, width, height);
        
    	mWidth = width;
    	mHeight = height;
    	Log.d("height", Integer.toString(height));
    	Log.d("width", Integer.toString(width));
        float ratio = (float) width / height;
        
        // this projection matrix is applied to object coodinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1f, 30);
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }
    
    private void initShapes(){
        
    	FloatBuffer triangleVB;
    	
        	float triangleCoords[] = {
                    // X, Y, Z
                    -1f, -1f, 0f,
                    1f, -1f, 0f,
                     1f,  1f, 0f,
                     -1f, -1f, 0f,
                     1f,  1f, 0f,
                     -1f, 1f, 0f};
//                    -1f, -0.5f, 0.3f,
//                    0.0f,  1f, 0.4f,
//                    -1f, 0.5f, 0.1f
//                }; 
                
                // initialize vertex Buffer for triangle  
                ByteBuffer vbb = ByteBuffer.allocateDirect(
                        // (# of coordinate values * 4 bytes per float)
                        triangleCoords.length * 4); 
                vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
                triangleVB = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
                triangleVB.put(triangleCoords);    // add the coordinates to the FloatBuffer
                
                triangleVB.position(0);            // set the buffer to read the first coordinate
                
                tris.add(triangleVB);   
    }
    
    private int loadShader(int type, String shaderCode){
        
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type); 
        
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        
        return shader;
    }

}
