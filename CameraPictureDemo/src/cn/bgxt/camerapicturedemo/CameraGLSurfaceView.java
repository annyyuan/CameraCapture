package cn.bgxt.camerapicturedemo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.graphics.SurfaceTexture;  
import android.opengl.GLES11Ext;  
import android.opengl.GLES20;  
import android.opengl.GLU;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 瀹氫箟涓�釜棰勮绫�
 */
public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer{
    private static final String TAG = "main";
	Context mContext;  
    OpenGLDraw mOpenGLDrawer = null; 
    ByteBuffer mBuf;
    int mWidth,mHeight;
    boolean bNeedRender = false;
    Object     mRenderLock = new Object();
    float[]    projectMatrix = new float[16];
    
    public CameraGLSurfaceView(Context context) {
    	super(context);
    	mContext = context;  
        setEGLContextClientVersion(2);  
        setRenderer(this);  
        setRenderMode(RENDERMODE_WHEN_DIRTY); 
        mBuf = ByteBuffer.allocate(409600); 
        Matrix.setIdentityM(projectMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {  
        // TODO Auto-generated method stub  
        Log.i(TAG, "onSurfaceCreated...");
        checkGlError("SurfaceCreated");
        GLES20.glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
        if(mOpenGLDrawer==null)
        	mOpenGLDrawer = new OpenGLDraw();  
    }

    @Override  
    public void onSurfaceChanged(GL10 gl, int width, int height) {  
        // TODO Auto-generated method stub  
        Log.i(TAG, "onSurfaceChanged...");  
        checkGlError("SurfaceChanged");
        gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
//		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
//		gl.glLoadIdentity(); 					//Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
//		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
//        gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
//        gl.glLoadIdentity(); 
//        Matrix.orthoM(projectMatrix, 0, 0, width, 0, height, -1, 1);
        mOpenGLDrawer.setRotation(270.0f);
    }
    
    @Override  
    public void onDrawFrame(GL10 gl) {  
        // TODO Auto-generated method stub  
        Log.i(TAG, "onDrawFrame...");  
        checkGlError("onDrawFrame");
        //GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);  
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);  

        if(bNeedRender)
        	mOpenGLDrawer.Render(mBuf,mWidth,mHeight, projectMatrix);
        
        synchronized(mRenderLock) {
            bNeedRender = false;
        }
    }
    
	public void RenderData(byte[]data, int width, int height)
	{	
		Log.i(TAG,"RenderData");
		mWidth = width;
		mHeight = height;
		mBuf.put(data);
		mBuf.flip ();
		
		synchronized(mRenderLock) {
		    bNeedRender = true;
		}
		checkGlError("RenderData");
		this.requestRender();
		//		mOpenGLDrawer.Render(data,width,height);		 
	}
	private void checkGlError(String op) {
        int error;
        
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("opengl","***** " + op + ": glError " + error);
//            throw new RuntimeException(op + ": glError " + error);
        }
    }
	
}
