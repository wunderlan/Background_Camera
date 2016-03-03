// Use of this source code is governed by a BSD-style license
// that can be found in the License file.
//
// Author: ZeehMn (https://github.com/ZeehMn)
package com.Zeeh.myapptest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.Toast;


public class Camera_video_Activity extends Activity implements SurfaceHolder.Callback
{
	public static final String CAMERA_V_KIND="Camera_V_kind";
	public static final String CAMERA_V_TIME="Camera_V_time";
	public static final String CAMERA_V_QUALITY="Camera_V_quality";
	private SurfaceView surfaceview;// 视频预览控件
    private LinearLayout lay; //愿揽控件的
    private SurfaceHolder surfaceHolder; //和surfaceView相关的
    public static int kind=0;
    public static long time=3000;
    public static int quality;
 
    /**
     * onCreate方法
     */
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.camera_video); 
        //初始化控件
        kind=getIntent().getIntExtra(CAMERA_V_KIND, 0);
        time=getIntent().getLongExtra(CAMERA_V_TIME, 3000);
        quality=getIntent().getIntExtra(CAMERA_V_QUALITY, 3);
        init(); 
    } 
    @Override 
    public void onResume() {
    	super.onResume();
    	}
    @Override 
    public void onPause() {
    	super.onPause();
    	}
    /**
     * 初始化控件以及回调
     */
    private void init() { 
      // View view=getLayoutInflater().inflate(R.layout.camera_video, null);
        surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview); 
        lay=(LinearLayout)this.findViewById(R.id.lay_camera); 
        //lay.setVisibility(LinearLayout.INVISIBLE);
        SurfaceHolder holder = this.surfaceview.getHolder();// 取得holder 
     holder.addCallback(this); // holder加入回调接口 
     // 设置setType
     holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
         
    } 
 
  
    @Override 
    public void surfaceChanged(SurfaceHolder holder, int format, int width, 
            int height) { 
        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder 
        surfaceHolder = holder; 
        
    } 
 
    @Override 
    public void surfaceCreated(SurfaceHolder holder) { 
        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder 
        surfaceHolder = holder; 
     Log.i("process",Thread.currentThread().getName());
     //录像线程，当然也可以在别的地方启动，但是一定要在onCreate方法执行完成以及surfaceHolder被赋值以后启动
      RecordThread thread=new RecordThread(time,surfaceview,surfaceHolder);
      MainActivity.mRecordThread=thread;
       thread.start();
        
    } 
 
    @Override 
    public void surfaceDestroyed(SurfaceHolder holder) { 
        // surfaceDestroyed的时候同时对象设置为null 
        surfaceview = null; 
        surfaceHolder = null;        
    } 
    
    /**
     * 录像线程
     * 
     * @author bcaiw
     * 
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public class RecordThread extends Thread {

     private MediaRecorder mediarecorder;// 录制视频的类
     private SurfaceHolder surfaceHolder;
     private long recordTime;
     private SurfaceView surfaceview;// 显示视频的控件
     private Camera mCamera;
     public Handler mHandler;
     public Runnable mRunnable;
     public NotificationManager mNotificationManager;
     private KeyguardLock keyguardLock;
     private WakeLock m_wakeObj;
     Size max=null,mid=null;
     
     public RecordThread(long recordTime, SurfaceView surfaceview,
       SurfaceHolder surfaceHolder) {
      this.recordTime = recordTime;
      this.surfaceview = surfaceview;
      this.surfaceHolder = surfaceHolder;
      mNotificationManager=(NotificationManager)getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
     }

     @Override
     public void run() {

      /**
       * 开始录像
       */
    	PowerManager pm = (PowerManager)getApplication().getSystemService(Context.POWER_SERVICE); 
  	    m_wakeObj = (WakeLock)pm.newWakeLock(PowerManager.FULL_WAKE_LOCK 
  	               | PowerManager.ACQUIRE_CAUSES_WAKEUP 
  	               | PowerManager.ON_AFTER_RELEASE, "Camera_video_Activity"); 
     	 m_wakeObj.acquire(); 
     	KeyguardManager keyguardManager = (KeyguardManager)getApplication().getSystemService(KEYGUARD_SERVICE); 
		keyguardLock = keyguardManager.newKeyguardLock("LOCK_TAG"); 
		keyguardLock.disableKeyguard(); 
         startRecord();

      finish();
      showNotification();
      Looper.prepare();
      /**
       * 启动定时器，到规定时间recordTime后执行停止录像任务
       */
      //Timer timer = new Timer();

     // timer.schedule(new TimerThread(), recordTime);
      mRunnable=new Runnable()
  	{
  		@Override
  		public void run()
  		{
  			// TODO 自动生成的方法存根
  			stopRecord();
  		}
  	};
      mHandler=new Handler();
      mHandler.postDelayed(mRunnable, time);
      Looper.loop();
     }

     

     /**
      * 获取摄像头实例对象
      * 
      * @return
      */
     @TargetApi(Build.VERSION_CODES.GINGERBREAD) @SuppressLint("NewApi")
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

	    @SuppressWarnings("deprecation")
		private void showNotification()
	    {
			@SuppressWarnings("deprecation")
			Notification mNotification = new Notification(
					R.drawable.ic_launcher, getApplication().getString(R.string.video_on_now), Calendar.getInstance()
							.getTimeInMillis());
			//mNotification.defaults=Notification.DEFAULT_ALL;   
			PendingIntent pt = PendingIntent.getActivity(getApplication(), 0, new Intent(
					getApplication(), MainActivity.class), 0);
			//点击通知后的动作，这里是转回main 这个Acticity   
			mNotification.setLatestEventInfo(getApplication(),
					getString(R.string.app_name), getApplication().getString(R.string.video_on_now), pt);
			mNotificationManager.notify(0, mNotification);
	    }
	    @TargetApi(Build.VERSION_CODES.HONEYCOMB) @SuppressWarnings("deprecation")
	    private void check_camera_not_null()
		{
	    	if(mCamera!=null)
			{
				 // 解锁camera
			      Parameters params = mCamera.getParameters();
			      params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
				{
					List<Size> mList=null;
					mList= params.getSupportedVideoSizes();
					long j=0;
					for (int i = 0; i < 10000; i++)
					{
						j=j+2*i;
					}
					if (mList!=null)
					{
						max = mList.get(0);
						mid = mList.get(0);
						for (int i = 0; i < mList.size(); i++)
						{
							if (mList.get(i).height > max.height)
								max = mList.get(i);
							if (i <= mList.size() / 2
									&& mList.get(i).height > mid.height)
								mid = mList.get(i);
						}
					}
				}
				mCamera.setParameters(params);
			      mCamera.unlock();
			}
		}
     /**
      * 开始录像
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public void startRecord() {
      mediarecorder = new MediaRecorder();// 创建mediarecorder对象
      if(kind==0)
  		mCamera = openFacingBackCamera();
  	  else if(kind==1)
  		mCamera=openFacingFrontCamera();
      if(mCamera!=null)
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
      if(mCamera==null)
    	  return;
      mediarecorder.setCamera(mCamera);
      // 设置录制视频源为Camera(相机)
      mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
      mediarecorder.setOrientationHint(90);
      //mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      //mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
      //mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
      //mediarecorder.setVideoSize(max.width, max.height);

      
      // 设置录制文件质量，格式，分辨率之类，这个全部包括了
      switch (quality)
	{
	case 1:
		if(Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB||mid==null)
		    mediarecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		else 
		{
			  mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		      mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
		      mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		      mediarecorder.setVideoSize(mid.width, mid.height);
		}
		break;
	case 2:
		mediarecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		break;
	case 3:
		if(Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB||max==null)
		    mediarecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		else 
		{
		      mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		      mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
		      mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		      mediarecorder.setVideoSize(max.width, max.height);
		}
		break;
	default:
		mediarecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		break;
	}
      mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
      
      File pictureFileDir = new File(Environment.getExternalStorageDirectory()
              + "/Zeeh/video/");
      if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) 
      {
       Toast.makeText(getApplication(), "Can't create directory to save image.",
          Toast.LENGTH_LONG).show();
        return;
      }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String date = dateFormat.format(new Date());
        String videoFile = "video_" + date + ".3gp";
        String filename = pictureFileDir.getPath()+"/" + videoFile;
	  // 设置视频文件输出的路径
	  mediarecorder.setOutputFile(filename);
      try {
       // 准备录制
       mediarecorder.prepare();
       // 开始录制
       mediarecorder.start();
      } catch (IllegalStateException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
      } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
      }
     }

     /**
      * 停止录制
      */
     public void stopRecord()
     {
      if (mediarecorder != null)
      {
       // 停止录制
       mediarecorder.stop();  
       // 释放资源
       mediarecorder.release();
       mediarecorder = null;
       if(mCamera!=null)
       {
        mCamera.release();
        mCamera = null;
       }   
      }
      MainActivity.mRecordThread=null;
      Toast.makeText(getApplication(), getApplication().getString(R.string.service_video_stopped), Toast.LENGTH_LONG)
		 .show();
     Toast.makeText(getApplication(), getApplication().getString(R.string.camera_video_save), Toast.LENGTH_LONG)
		 .show();
     
     Notification mNotification = new Notification(
				R.drawable.ic_launcher, getApplication().getString(R.string.video_stop_now), Calendar.getInstance()
						.getTimeInMillis());
		//mNotification.defaults=Notification.DEFAULT_ALL;   
		PendingIntent pt = PendingIntent.getActivity(getApplication(), 0, new Intent(
				getApplication(), MainActivity.class), 0);
		//点击通知后的动作，这里是转回main 这个Acticity   
		mNotification.setLatestEventInfo(getApplication(),
				getString(R.string.app_name), getApplication().getString(R.string.video_stop_now), pt);
		mNotificationManager.notify(0, mNotification);
		m_wakeObj.release();
		keyguardLock.reenableKeyguard();
     }

     /**
      * 定时器
      * @author bcaiw
      *
      */
    /* class TimerThread extends TimerTask {
      @Override
      public void run() {
       stopRecord();
       this.cancel();
      }
     }*/
    }
}
