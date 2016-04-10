package cn.bgxt.camerapicturedemo;

import java.io.File;
import java.io.FileOutputStream;



import java.nio.ByteBuffer;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;

public class MainActivity extends Activity {
	protected static final String TAG = "main";
	private Camera mCamera;
	private CameraPreview mPreview;
	private CameraGLSurfaceView mPreview2;
	
//	private GLSurfaceView glSurfaceView;	
//	private GLFrameRenderer glRenderer;
	public ByteBuffer[] yuvPlanes;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mCamera = getCameraInstance();
		// 鍒涘缓棰勮绫伙紝骞朵笌Camera鍏宠仈锛屾渶鍚庢坊鍔犲埌鐣岄潰甯冨眬涓�
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);		
		
		mPreview2 = new CameraGLSurfaceView(this);
//		glSurfaceView = new GLFrameSurface(this);
//        glSurfaceView.setEGLContextClientVersion(2);  
        // 
//        glRenderer = new GLFrameRenderer(null, glSurfaceView, getDM(this));
        // set our renderer to be the main renderer with
        // the current activity context
//        glSurfaceView.setRenderer(glRenderer);
        
		FrameLayout preview2 = (FrameLayout) findViewById(R.id.camera_preview2);
		preview2.addView(mPreview2);
		
//		mPreview.SetGLSurfaceView(mPreview2);
		Button beatyButton = (Button) findViewById(R.id.button_capture);
		beatyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 鍦ㄦ崟鑾峰浘鐗囧墠杩涜鑷姩瀵圭劍
/*				mCamera.autoFocus(new AutoFocusCallback() {
					
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						// 浠嶤amera鎹曡幏鍥剧墖
						mCamera.takePicture(null, null, mPicture);
					}
				});	*/			
			}
		});
	}

	/** 妫�祴璁惧鏄惁瀛樺湪Camera纭欢 */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// 瀛樺湪
			return true;
		} else {
			// 涓嶅瓨鍦�
			return false;
		}
	}

	/** 鎵撳紑涓�釜Camera */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
//			c.setDisplayOrientation(90);
		} catch (Exception e) {
			Log.d(TAG, "鎵撳紑Camera澶辫触澶辫触");
		}
		return c; 
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// 鑾峰彇Jpeg鍥剧墖锛屽苟淇濆瓨鍦╯d鍗′笂
			File pictureFile = new File("/sdcard/" + System.currentTimeMillis()
					+ ".jpg");
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (Exception e) {
				Log.d(TAG, "淇濆瓨鍥剧墖澶辫触");
			}
		}
	};

	
	@Override
	protected void onDestroy() {
		// 鍥炴敹Camera璧勬簮
		if(mCamera!=null){
			mCamera.stopPreview();
			mCamera.release();
			mCamera=null;
		}
		super.onDestroy();
	}
	public DisplayMetrics getDM(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		return outMetrics;
	}
	
	public void ShowData(byte[]data, int width, int height)
	{
		mPreview2.RenderData(data,width,height);
/*		glRenderer.update(320, 240);
		copyFrom(data, 320, 240);
		
        byte[] y = new byte[yuvPlanes[0].remaining()];
        yuvPlanes[0].get(y, 0, y.length);
        
        byte[] u = new byte[yuvPlanes[1].remaining()];
        yuvPlanes[1].get(u, 0, u.length);
        
        byte[] v = new byte[yuvPlanes[2].remaining()];
        yuvPlanes[2].get(v, 0, v.length);

        
        glRenderer.update(y, v, u);*/
	}

    
    public void copyFrom(byte[] yuvData, int width, int height) {
    	
    	int[] yuvStrides = { width, width / 2, width / 2};
    	
    	if (yuvPlanes == null) {
            yuvPlanes = new ByteBuffer[3];
            yuvPlanes[0] = ByteBuffer.allocateDirect(yuvStrides[0] * height);
            yuvPlanes[1] = ByteBuffer.allocateDirect(yuvStrides[1] * height / 2);
            yuvPlanes[2] = ByteBuffer.allocateDirect(yuvStrides[2] * height / 2);
    	}
    	
        if (yuvData.length < width * height * 3 / 2) {
          throw new RuntimeException("Wrong arrays size: " + yuvData.length);
        }
        
        int planeSize = width * height;
        
        ByteBuffer[] planes = new ByteBuffer[3];
        
        planes[0] = ByteBuffer.wrap(yuvData, 0, planeSize);
        planes[1] = ByteBuffer.wrap(yuvData, planeSize, planeSize / 4);
        planes[2] = ByteBuffer.wrap(yuvData, planeSize + planeSize / 4, planeSize / 4);
        
        for (int i = 0; i < 3; i++) {
        	yuvPlanes[i].position(0);
        	yuvPlanes[i].put(planes[i]);
        	yuvPlanes[i].position(0);
        	yuvPlanes[i].limit(yuvPlanes[i].capacity());
        }
	} 
}
