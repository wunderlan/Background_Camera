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
	private SurfaceView surfaceview;// ��ƵԤ���ؼ�
    private LinearLayout lay; //Ը���ؼ���
    private SurfaceHolder surfaceHolder; //��surfaceView��ص�
    public static int kind=0;
    public static long time=3000;
    public static int quality;
 
    /**
     * onCreate����
     */
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.camera_video); 
        //��ʼ���ؼ�
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
     * ��ʼ���ؼ��Լ��ص�
     */
    private void init() { 
      // View view=getLayoutInflater().inflate(R.layout.camera_video, null);
        surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview); 
        lay=(LinearLayout)this.findViewById(R.id.lay_camera); 
        //lay.setVisibility(LinearLayout.INVISIBLE);
        SurfaceHolder holder = this.surfaceview.getHolder();// ȡ��holder 
     holder.addCallback(this); // holder����ص��ӿ� 
     // ����setType
     holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
         
    } 
 
  
    @Override 
    public void surfaceChanged(SurfaceHolder holder, int format, int width, 
            int height) { 
        // ��holder�����holderΪ��ʼ��oncreat����ȡ�õ�holder����������surfaceHolder 
        surfaceHolder = holder; 
        
    } 
 
    @Override 
    public void surfaceCreated(SurfaceHolder holder) { 
        // ��holder�����holderΪ��ʼ��oncreat����ȡ�õ�holder����������surfaceHolder 
        surfaceHolder = holder; 
     Log.i("process",Thread.currentThread().getName());
     //¼���̣߳���ȻҲ�����ڱ�ĵط�����������һ��Ҫ��onCreate����ִ������Լ�surfaceHolder����ֵ�Ժ�����
      RecordThread thread=new RecordThread(time,surfaceview,surfaceHolder);
      MainActivity.mRecordThread=thread;
       thread.start();
        
    } 
 
    @Override 
    public void surfaceDestroyed(SurfaceHolder holder) { 
        // surfaceDestroyed��ʱ��ͬʱ��������Ϊnull 
        surfaceview = null; 
        surfaceHolder = null;        
    } 
    
    /**
     * ¼���߳�
     * 
     * @author bcaiw
     * 
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public class RecordThread extends Thread {

     private MediaRecorder mediarecorder;// ¼����Ƶ����
     private SurfaceHolder surfaceHolder;
     private long recordTime;
     private SurfaceView surfaceview;// ��ʾ��Ƶ�Ŀؼ�
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
       * ��ʼ¼��
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
       * ������ʱ�������涨ʱ��recordTime��ִ��ֹͣ¼������
       */
      //Timer timer = new Timer();

     // timer.schedule(new TimerThread(), recordTime);
      mRunnable=new Runnable()
  	{
  		@Override
  		public void run()
  		{
  			// TODO �Զ����ɵķ������
  			stopRecord();
  		}
  	};
      mHandler=new Handler();
      mHandler.postDelayed(mRunnable, time);
      Looper.loop();
     }

     

     /**
      * ��ȡ����ͷʵ������
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
			//���֪ͨ��Ķ�����������ת��main ���Acticity   
			mNotification.setLatestEventInfo(getApplication(),
					getString(R.string.app_name), getApplication().getString(R.string.video_on_now), pt);
			mNotificationManager.notify(0, mNotification);
	    }
	    @TargetApi(Build.VERSION_CODES.HONEYCOMB) @SuppressWarnings("deprecation")
	    private void check_camera_not_null()
		{
	    	if(mCamera!=null)
			{
				 // ����camera
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
      * ��ʼ¼��
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public void startRecord() {
      mediarecorder = new MediaRecorder();// ����mediarecorder����
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
      // ����¼����ƵԴΪCamera(���)
      mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
      mediarecorder.setOrientationHint(90);
      //mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      //mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
      //mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
      //mediarecorder.setVideoSize(max.width, max.height);

      
      // ����¼���ļ���������ʽ���ֱ���֮�࣬���ȫ��������
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
	  // ������Ƶ�ļ������·��
	  mediarecorder.setOutputFile(filename);
      try {
       // ׼��¼��
       mediarecorder.prepare();
       // ��ʼ¼��
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
      * ֹͣ¼��
      */
     public void stopRecord()
     {
      if (mediarecorder != null)
      {
       // ֹͣ¼��
       mediarecorder.stop();  
       // �ͷ���Դ
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
		//���֪ͨ��Ķ�����������ת��main ���Acticity   
		mNotification.setLatestEventInfo(getApplication(),
				getString(R.string.app_name), getApplication().getString(R.string.video_stop_now), pt);
		mNotificationManager.notify(0, mNotification);
		m_wakeObj.release();
		keyguardLock.reenableKeyguard();
     }

     /**
      * ��ʱ��
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
