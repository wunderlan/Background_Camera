// Use of this source code is governed by a BSD-style license
// that can be found in the License file.
//
// Author: ZeehMn (https://github.com/ZeehMn)
package com.Zeeh.myapptest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;



public class Camera_pic_Activity extends Activity
{
	public static final String CAMERA_KIND="Camera_kind";
	public static final String CAMERA_TIME="Camera_time";
	public static final String CAMERA_NUM="Camera_num";
	public int kind=0,time=0;
	public long num=1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
     //mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    // showNotification();
    	super.onCreate(savedInstanceState); 
        setContentView(R.layout.camera_video); 
        if(MainActivity.mCamera_pic_Thread==null)
        {
        	kind=getIntent().getIntExtra(CAMERA_KIND, 0);
            time=getIntent().getIntExtra(CAMERA_TIME, 0);
            num=getIntent().getLongExtra(CAMERA_NUM, 0);
            MainActivity.mCamera_pic_Thread=new Camera_pic_Thread(kind, time, num, this);
            MainActivity.mCamera_pic_Thread.start();
        }
        else 
        	MainActivity.mCamera_pic_Thread.mActivity=this;
     
    }
    @Override 
    public void onResume() {
    	super.onResume();
    	}
    @Override 
    public void onPause() {
    	super.onPause();
    	}
      //0为后置摄像头
     //1为前置摄像头
    public class Camera_pic_Thread extends Thread
    {
    	public int kind=0,time=0;
    	public long num=1;
        private Camera camera=null;
        public Handler handler;
        //private NotificationManager mNM;
        public long totaltime=0;
        public Runnable runnable;
        //private int NOTIFICATION = R.string.service_started;
        public OrientationEventListener mOrientationListener;
        public int orientations;
        public Activity mActivity=null;
        private KeyguardLock keyguardLock;
        private WakeLock m_wakeObj;
        public Camera_pic_Thread(int mkind,int mtime,long mnum,Activity activity)
        {
        	kind=mkind;
        	time=mtime;
        	num=mnum;
        	mActivity=activity;
        	handler = new Handler();
        }
		@Override
		public void run()
		{
			// TODO 自动生成的方法存根
			PowerManager pm = (PowerManager)getApplication().getSystemService(Context.POWER_SERVICE); 
     	    m_wakeObj = (WakeLock)pm.newWakeLock(PowerManager.FULL_WAKE_LOCK 
     	               | PowerManager.ACQUIRE_CAUSES_WAKEUP 
     	               | PowerManager.ON_AFTER_RELEASE, "Camera_video_Activity"); 
        	 m_wakeObj.acquire(); 
            KeyguardManager keyguardManager = (KeyguardManager)getApplication().getSystemService(KEYGUARD_SERVICE); 
   	     	keyguardLock = keyguardManager.newKeyguardLock("LOCK_TAG"); 
   		    keyguardLock.disableKeyguard();
			Looper.prepare();
			Toast.makeText(getApplication(), getApplication().getString(R.string.service_started), Toast.LENGTH_SHORT)
		     .show();
			MainActivity.mCamera_pic_Thread=this;
		     mOrientationListener = new OrientationEventListener(getApplication()){  
		         @Override  
		         public void onOrientationChanged(int orientation) {  
		             orientations =orientation;  
		         }  
		     }; 
		     if(mOrientationListener!=null){//先判断下防止出现空指针异常  
		         mOrientationListener.enable();  
		     }  
		     onStartCommand();
		}
        
    @SuppressWarnings("deprecation")
	private void check_camera_not_null()
	{
    	if(camera!=null)
    	{
			Parameters mParameters= camera.getParameters(); 
			long j=0;
			for (int i = 0; i < 5000; i++)
			{
				j=j+2*i;
			}
			List<Size> mList=null;
			if(mParameters!=null)
         	    mList=mParameters.getSupportedPictureSizes();
			j=0;
			for (int i = 0; i < 5000; i++)
			{
				j=j+2*i;
			}
         	Size max=null;
         	if (mList!=null)
			{
				max = mList.get(0);
				for (Size size : mList)
				{
					if (size.width > max.width)
						max = size;
				}
			}
			if (max!=null&&mParameters!=null)
			{
				mParameters.setPictureSize(max.width, max.height);
				camera.setParameters(mParameters);
			}
			if (mActivity==null)
    		{
    			Intent intent = new Intent(getApplication(),
    					Camera_pic_Activity.class);
    			startActivity(intent);
    		}
    		SurfaceView dummy = (SurfaceView) mActivity.findViewById(R.id.surfaceview); 
             try {
              camera.setPreviewDisplay(dummy.getHolder());
             } catch (IOException e) {
              e.printStackTrace();
             }
             camera.startPreview();
             //mActivity.finish();
    	}
	}
    private void init()
    {
    	if(kind==0)
           camera = openFacingBackCamera();
    	else
    		camera=openFacingFrontCamera();
         //IntentFilter filter = new IntentFilter();
    	if(camera!=null)
    		check_camera_not_null();
		else
		{
			long j=0;
			for (int i = 0; i < 50000; i++)
			{
				j=j+2*i;
			}
			check_camera_not_null();
		}
        
    }

    public void onStartCommand()
    {
     runnable=new Runnable()
     {
         @SuppressWarnings("deprecation")
		@Override
         public void run()
         {
        	 init();
             camera.autoFocus(new AutoFocusCallback()
             {
                 @Override
                 public void onAutoFocus(boolean success, Camera camera)
                  {
                	camera.setPreviewCallback(null);
                    camera.takePicture(new ShutterCallback()
					{
						@Override
						public void onShutter()
						{
							m_wakeObj.release();
					        keyguardLock.reenableKeyguard();
					         if(mActivity!=null)
					     		mActivity.finish();
				        	 m_wakeObj.acquire(); 
				   		     keyguardLock.disableKeyguard(); 
						}
					}, null, new PhotoHandler(getApplication()));
                  }
             });
         }
     };
    // for(int i=0;i<num;i++)
    // {
      handler.postDelayed(runnable,300);//+time*i);
    // }
    }

    public void onDestroy()
    {
    	MainActivity.mCamera_pic_Thread=null;
    	if(mActivity!=null)
    		mActivity.finish();
     //mNM.cancel(NOTIFICATION);
     if (camera != null) 
     {
         camera.release();
         camera = null;
     }
     if(mOrientationListener!=null){  
         mOrientationListener.disable();  
     }  
     Toast.makeText(getApplication(), getApplication().getString(R.string.service_stopped), Toast.LENGTH_LONG)
         .show();
    m_wakeObj.release();
    keyguardLock.reenableKeyguard();
    }

    /**
     * Show a notification while this service is running.
     */
   /* @TargetApi(Build.VERSION_CODES.HONEYCOMB) @SuppressLint("NewApi") 
    private void showNotification() {
     CharSequence text = getText(R.string.service_started);

     Notification notification = new Notification.Builder(getApplication()).setContentText(text)
    		 .setWhen(System.currentTimeMillis()).build();

     PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
       new Intent(this, MainActivity.class), 0);

     //notification.setLatestEventInfo(this,
       //getText(R.string.local_service_label), text, contentIntent);

     mNM.notify(NOTIFICATION, notification);
    }*/

     /*public void TakePic(Context context, Intent intent) {
       if (camera != null) {
        SurfaceView dummy = new SurfaceView(getBaseContext());
        try {
         camera.setPreviewDisplay(dummy.getHolder());
        } catch (IOException e) {
         e.printStackTrace();
        }
        camera.startPreview();

        camera.takePicture(null, null, new PhotoHandler(
          getApplicationContext()));
       }
      }*/

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private Camera openFacingBackCamera() {
     Camera cam = null;
     Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
     for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
      Camera.getCameraInfo(camIdx, cameraInfo);
      if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
       try {
        cam = Camera.open(camIdx);
        break;
       } catch (RuntimeException e) {
        e.printStackTrace();
       }
      }
     }
     return cam;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD) @SuppressLint("NewApi") 
    private Camera openFacingFrontCamera()
	{
    	Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++)
        {
           Camera.getCameraInfo(camIdx, cameraInfo);
           if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
           {
             try
             {
              cam = Camera.open(camIdx);
              break;
             }
             catch (RuntimeException e)
             {
               e.printStackTrace();
             }
	       }
        }
        return cam;
	}
	public class PhotoHandler implements PictureCallback
	{

	    private final Context context;

	    public PhotoHandler(Context context) {
	        this.context = context;
	    }

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera)
	    {
	    	 camera.stopPreview();
	         camera.release();
	         camera = null;
	       /* m_wakeObj.release();
	        keyguardLock.reenableKeyguard();
	         if(mActivity!=null)
	     		mActivity.finish();
        	 m_wakeObj.acquire(); 
   		     keyguardLock.disableKeyguard(); */
	         BitmapFactory.Options opts = new BitmapFactory.Options();  
             opts.inPreferredConfig = Config.RGB_565;  
             //opts.inSampleSize =4;   
             Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length,opts);  
             Matrix matrixs = new Matrix();  
                 if(orientations > 325 || orientations <= 45){  
                     Log.v("time", "Surface.ROTATION_0;"+orientations); 
                     if(kind==0)
                        matrixs.setRotate(90);
                     else matrixs.setRotate(270); 
                 }else if(orientations > 45 && orientations <= 135){  
                     Log.v("time", " Surface.ROTATION_270"+orientations); 
                     if(kind==0)  matrixs.setRotate(180); 
                     else matrixs.setRotate(0);
                 }else if(orientations > 135 && orientations < 225){  
                     Log.v("time", "Surface.ROTATION_180;"+orientations);  
                     if(kind==0) matrixs.setRotate(270);  
                     else matrixs.setRotate(90);
                 }else {  
                     Log.v("time", "Surface.ROTATION_90"+orientations);  
                    if(kind==0) matrixs.setRotate(0);  
                    else matrixs.setRotate(180);
                 }  
             bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrixs, true);  

	      //在SD卡上创建文件夹
            File pictureFileDir = new File(Environment.getExternalStorageDirectory()
                            + "/Zeeh/pic/");

	        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

	            Toast.makeText(context, "Can't create directory to save image.",
	                    Toast.LENGTH_LONG).show();
	            return;

	        }

	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
	        String date = dateFormat.format(new Date());
	        String photoFile = "Pic_" + date + ".jpg";

	        String filename = pictureFileDir.getPath()+"/" + photoFile;

	        File pictureFile = new File(filename);

	        try {
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            ByteArrayOutputStream mStream=new ByteArrayOutputStream();
	            bmp.compress(Bitmap.CompressFormat.JPEG, 75, mStream);
	            fos.write(mStream.toByteArray());
	            fos.close();
	            Toast.makeText(getApplication(), getApplication().getString(R.string.camera_pic_save),
	                    Toast.LENGTH_LONG).show();
	        } catch (Exception error) {
	            Toast.makeText(context, "Image could not be saved.",
	                    Toast.LENGTH_LONG).show();
	        }
	        long nexttime=3000+time;
	        totaltime=totaltime+time;
	        if(totaltime+3000<num)
	        {
   	            handler.postDelayed(runnable, nexttime);
	        }
	        else 
	        {
				 onDestroy();
			}
	    }
	}
    }
   }
