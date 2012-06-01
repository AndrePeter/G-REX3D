package com.andredittrich.main;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.andredittrich.surface3d.OGLLayer;


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
	private int PuMVPMatrixHandle;
	private float[] mMxMatrix = new float[16];
	private float[] mMyMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mMVPLMatrix = new float[16];
    private float[] mMMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    /**
	 * Stores a copy of the model matrix specifically for the light position.
	 */
	private float[] mLightModelMatrix = new float[16];

    /** Store the accumulated rotation. */
	private final float[] mAccumulatedRotation = new float[16];
	
	/** Store the current rotation. */
	private final float[] mCurrentRotation = new float[16];
	private final float[]mTemporaryMatrix = new float[16];
    public int mWidth;
    public int mHeight;
    private OGLLayer layer;
    
    private int mNormalHandle;
	
	private int mProgram;
	private int PProgram;

	/** This will be used to pass in the light position. */
	private int mLightPosHandle;
    private int myPositionHandle;
    private int myPPositionHandle;
    /** This will be used to pass in the modelview matrix. */
	private int mMVMatrixHandle;
//	private int PmMVMatrixHandle;
//	private int PmNormalHandle;
	
    

	/**
	 * Used to hold a light centered on the origin in model space. We need a 4th
	 * coordinate so we can get translations to work when we multiply this by
	 * our transformation matrices.
	 */
	private final float[] mLightPosInModelSpace = new float[] { -5000.0f, 5000.0f,
			20000.0f, 1.0f };
	/**
	 * Used to hold the current position of the light in world space (after
	 * transformation via model matrix).
	 */
	private final float[] mLightPosInWorldSpace = new float[4];
	/**
	 * Used to hold the transformed position of the light in eye space (after
	 * transformation via modelview matrix)
	 */
	private final float[] mLightPosInEyeSpace = new float[4];
    
    
    
    private final String vertexShaderCode = "uniform mat4 uMVPMatrix;      \n" 
			+ "uniform mat4 u_MVMatrix;       \n" 
			+ "uniform vec3 u_LightPos;       \n" 
			+ "vec4 a_Color = vec4(0, 1, 1, 1.0);     \n"
			+ "attribute vec4 vPosition;     \n" 			
			+ "attribute vec3 a_Normal;       \n" 
			+ "varying vec4 v_Color;   \n"
			+ "void main()                    \n" 
			+ "{                              \n"
			+ "   vec3 modelViewVertex = vec3(u_MVMatrix * vPosition);              \n"
			+ "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
			+ "   float distance = length(u_LightPos - modelViewVertex);             \n"
			+ "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n"
			+ "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n"
			+ "   diffuse = diffuse * (1.0 / (0.0001*distance));  \n"
			+ "   v_Color = a_Color * diffuse + a_Color * vec4(0.1, 0.1, 0.1, 1.0);                                      \n"
			+ "   gl_Position = uMVPMatrix * vPosition;                            \n"
			+ "   gl_PointSize = 5.0;         \n"
			+ "}                                                                     \n";
	    
	private final String fragmentShaderCode = 
	        "precision mediump float;  \n" +
	        "varying vec4 v_Color;      \n" +		
	        "void main(){              \n" +
	        " gl_FragColor =  v_Color; \n" +
	        "}                         \n";
	                        
	
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
	    
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        // initialize the triangle vertex array
        initShapes();
//        GLES20.glEnable(GLES20.GL_CULL_FACE);
//		
//		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
     // Define a simple shader program for our point.
     		final String pointVertexShader = "uniform mat4 uMVPMatrix;      \n"
     				+ "attribute vec4 vPosition;     \n"
     				+ "void main()                    \n"
     				+ "{                              \n"
     				+ "   gl_Position = uMVPMatrix   \n"
     				+ "               * vPosition;   \n"
     				+ "   gl_PointSize = 5.0;         \n"
     				+ "}                              \n";

     		final String pointFragmentShader = "precision mediump float;       \n"
     				+ "void main()                    \n"
     				+ "{                              \n"
     				+ "   gl_FragColor = vec4(1.0,    \n"
     				+ "   1.0, 1.0, 1.0);             \n"
     				+ "}                              \n";
        
        
     		int PvertexShader = loadShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
            int PfragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        
            PProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
            GLES20.glAttachShader(PProgram, PvertexShader);   // add the vertex shader to program
            GLES20.glAttachShader(PProgram, PfragmentShader); // add the fragment shader to program
            GLES20.glLinkProgram(PProgram);
            
            myPPositionHandle = GLES20.glGetAttribLocation(PProgram, "vPosition");
            PuMVPMatrixHandle = GLES20.glGetUniformLocation(PProgram, "uMVPMatrix");
            
            GLES20.glEnableVertexAttribArray(myPPositionHandle);           
        
            
//            PmNormalHandle = GLES20.glGetAttribLocation(PProgram,
//    				"a_Normal");
//            GLES20.glEnableVertexAttribArray(mNormalHandle);
//            
//            PmMVMatrixHandle = GLES20.glGetUniformLocation(PProgram,
//    				"u_MVMatrix");
            
            
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL program executables
        
//        GLES20.glBindAttribLocation(mProgram, 0, "vPosition");
//        GLES20.glBindAttribLocation(mProgram, 1, "a_Normal");        
        
        // get handle to the vertex shader's vPosition member
        myPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");        
        Matrix.setIdentityM(mAccumulatedRotation, 0);
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glEnableVertexAttribArray(myPositionHandle);
        mNormalHandle = GLES20.glGetAttribLocation(mProgram,
				"a_Normal");
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram,
				"u_MVMatrix");
		mLightPosHandle = GLES20.glGetUniformLocation(mProgram,
				"u_LightPos");
        

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, 150f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
       
              
    }
    
    public void onDrawFrame(GL10 unused) {
    	Log.d("draw", "now");
    
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
			// Prepare the triangle data
        layer.getVertexBuffer().position(0);       
        layer.getNormalBuffer().position(0);
        
		GLES20.glVertexAttribPointer(mNormalHandle, 3,
				GLES20.GL_FLOAT, false, 0, layer.getNormalBuffer());
        
//        layer.getVertexBuffer().position(0);       

			GLES20.glVertexAttribPointer(myPositionHandle, 3, GLES20.GL_FLOAT,
					false, 0, layer.getVertexBuffer());
			Log.d("groesse",Integer.toString(layer.getVertexBuffer().capacity()));
			
//			Matrix.setIdentityM(mLightModelMatrix, 0);
//			Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0,
//					mLightPosInModelSpace, 0);
//			Matrix.multiplyMV(mLightPosInEyeSpace, 0, mVMatrix, 0,
//					mLightPosInWorldSpace, 0);
			
			
			
			Matrix.setIdentityM(mMMatrix, 0);//.setRotateEulerM(mMMatrix, 0, mAngleY, mAngleX, 0);
//			Matrix.translateM(mMMatrix, 0, -mdX, -mdY, 0);
			Matrix.scaleM(mMMatrix, 0, scale, scale, scale);
			Matrix.setIdentityM(mCurrentRotation, 0); 
			
			
			
			// RICHTIG DREHEN !!!!
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
			// RICHTIG DREHEN !!!!
			
			
			
//	        Matrix.setRotateM(mMxMatrix, 0, mAngleY, 1.0f, 0.0f, 0.0f);
//	        Matrix.setRotateM(mMyMatrix, 0, mAngleX, 0.0f, 1.0f, 0.0f);
//	        Matrix.multiplyMM(mMMatrix, 0, mMxMatrix, 0, mMyMatrix, 0);
			
//			Matrix.setLookAtM(mVMatrix, 0, mdX, mdY, -120f, mdX, mdY, 0f, 0f, 1.0f, 0.0f);
	    	
	    	
	    	
	    	Matrix.multiplyMM(mMVPLMatrix, 0, mVMatrix, 0, mMMatrix, 0);
	    	
//	    	GLES20.glUniformMatrix4fv(PmMVMatrixHandle, 1, false, mMVPMatrix, 0);
//	        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
	    	
	    	Matrix.setIdentityM(mLightModelMatrix, 0);
			Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0,
					mLightPosInModelSpace, 0);
			Matrix.multiplyMV(mLightPosInEyeSpace, 0, mMVPLMatrix, 0,
					mLightPosInWorldSpace, 0);
			GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],
					mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
			Log.d("lightPosxyz", Float.toString(mLightPosInEyeSpace[0]));
			
			
			GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPLMatrix, 0);
	        
	        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjMatrix, 0, mMVPLMatrix, 0);
			System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);
			// Apply a ModelView Projection transformation
			//Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
	        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
			// Draw the triangle
	       
	       
	        /*
			 * do not draw triangles if the object should not be filled.
			 * then only draw the vertices.
			 */
//	        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, layer.getVertexBuffer().capacity());
	        GLES20.glDrawElements(GLES20.GL_TRIANGLES, layer.getIndexBuffer().capacity(),
			GLES20.GL_UNSIGNED_SHORT, layer.getIndexBuffer());
//	        
	        
	        GLES20.glUseProgram(PProgram);
			drawPoints();

    }
    
    private void drawPoints() {
		// Pass in the position information
    	layer.getVertexBuffer().position(0);
    	layer.getLineBuffer().position(0);
		GLES20.glVertexAttribPointer(myPPositionHandle, 3, GLES20.GL_FLOAT,
				false, 0, layer.getVertexBuffer());

		GLES20.glEnableVertexAttribArray(myPPositionHandle);
		GLES20.glUniformMatrix4fv(PuMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		
//        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, layer.getVertexBuffer().capacity()/3);
//		GLES20.glDrawElements(GLES20.GL_POINTS, layer.getVertexBuffer().capacity(),
//    			GLES20.GL_UNSIGNED_SHORT, layer.getVertexBuffer());
		
//        GLES20.glDrawElements(GLES20.GL_LINES, layer.getLineBuffer().capacity(),
//    			GLES20.GL_UNSIGNED_SHORT, layer.getLineBuffer());
        
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
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1f, 200);
        
    }
    
    private void initShapes(){
    	layer = GREX3DActivity.tsobj;
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
