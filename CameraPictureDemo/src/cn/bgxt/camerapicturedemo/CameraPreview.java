package cn.bgxt.camerapicturedemo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 瀹氫箟涓�釜棰勮绫�
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,Camera.PreviewCallback  {
    private static final String TAG = "main";
	private SurfaceHolder mHolder;
    private Camera mCamera;
    private FileOutputStream fileStream = null;
    Context mContext;
    CameraGLSurfaceView glSurfaceView = null;
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mContext = context;
        mCamera = camera;

        // 閫氳繃SurfaceView鑾峰緱SurfaceHolder
        mHolder = getHolder();
        // 涓篠urfaceHolder鎸囧畾鍥炶皟
        mHolder.addCallback(this);
        // 璁剧疆Surface涓嶇淮鎶よ嚜宸辩殑缂撳啿鍖猴紝鑰屾槸绛夊緟灞忓箷鐨勬覆鏌撳紩鎿庡皢鍐呭鎺ㄩ�鍒扮晫闈�鍦ˋndroid3.0涔嬪悗寮冪敤
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    public void SetGLSurfaceView(CameraGLSurfaceView view)
    {
    	glSurfaceView = view;
    }
    public void surfaceCreated(SurfaceHolder holder) {
        // 褰揝urface琚垱寤轰箣鍚庯紝寮�Camera鐨勯瑙�
        try {
        	mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "棰勮澶辫触");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Surface鍙戠敓鏀瑰彉鐨勬椂鍊欏皢琚皟鐢紝绗竴娆℃樉绀哄埌鐣岄潰鐨勬椂鍊欎篃浼氳璋冪敤
        if (mHolder.getSurface() == null){
          // 濡傛灉Surface涓虹┖锛屼笉缁х画鎿嶄綔
          return;
        }

        // 鍋滄Camera鐨勯瑙�
        try {
            mCamera.stopPreview();
        } catch (Exception e){
        	Log.d(TAG, "褰揝urface鏀瑰彉鍚庯紝鍋滄棰勮鍑洪敊");
        }

        // 鍦ㄩ瑙堝墠鍙互鎸囧畾Camera鐨勫悇椤瑰弬鏁�

        // 閲嶆柊寮�棰勮
        try {
        	Camera.Parameters params = mCamera.getParameters();
            mCamera.setPreviewDisplay(mHolder);
            params.setPreviewFormat(ImageFormat.YV12);
            params.setPreviewSize(320, 240);
            params.setRotation(90);
            mCamera.setParameters(params);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
            setCameraDisplayOrientation((Activity)mContext,1,mCamera);
        } catch (Exception e){
            Log.d(TAG, "棰勮Camera鍑洪敊");
        }
    }
    @Override
    public void onPreviewFrame(byte[] b, Camera c) {
		Log.d(TAG,"onPreviewFrame");
//		writeFileToSD(b);
		WriteData(b);
	}
    private Size getBestSupportedSize(List<Size> sizes) {  
        // 鍙栬兘閫傜敤鐨勬渶澶х殑SIZE  
        Size largestSize = sizes.get(0);  
        int largestArea = sizes.get(0).height * sizes.get(0).width;  
        for (Size s : sizes) {  
            int area = s.width * s.height;
            if (area > largestArea) {  
                largestArea = area;  
                largestSize = s;  
            }  
        }  
        return largestSize;  
    } 
    private void WriteData(byte[] buf) { 
    	Camera.Parameters params = mCamera.getParameters();
    	((MainActivity)mContext).ShowData(buf,params.getPreviewSize().width,params.getPreviewSize().height);
//    	if(glSurfaceView!=null)
//    		glSurfaceView.RenderData(buf,params.getPreviewSize().width,params.getPreviewSize().height);
    }
    private void writeFileToSD(byte[] buf) {  
        String sdStatus = Environment.getExternalStorageState();  
        if(!sdStatus.equals(Environment.MEDIA_MOUNTED)) {  
            Log.d(TAG, "SD card is not avaiable/writeable right now.");  
            return;  
        }  
        try { 
        	
        	if(fileStream==null){
            String pathName="/storage/sdcard0/Download/";  
            String fileName="test.yuv";  
            File path = new File(pathName);  
            File file = new File(pathName + fileName);  
            if( !path.exists()) {  
                Log.d(TAG, "Create the path:" + pathName);  
                path.mkdir();  
            }  
            if( !file.exists()) {  
                Log.d(TAG, "Create the file:" + fileName);  
                file.createNewFile();  
            }  
            fileStream = new FileOutputStream(file); 
        	}
        	else{
        		Log.d(TAG,"WriteFrame");
        		fileStream.write(buf); 
        		
        	}
              
        } catch(Exception e) {  
            Log.e(TAG, "Error on writeFilToSD.");  
            e.printStackTrace();  
        }  
    }
    public void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) {
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	 }
}
