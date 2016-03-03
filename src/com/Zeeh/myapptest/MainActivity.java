// Use of this source code is governed by a BSD-style license
// that can be found in the License file.
//
// Author: ZeehMn (https://github.com/ZeehMn)
package com.Zeeh.myapptest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.Zeeh.myapptest.Camera_pic_Activity.Camera_pic_Thread;
import com.Zeeh.myapptest.Camera_video_Activity.RecordThread;


public class MainActivity extends ActionBarActivity {
	
	public static Camera_pic_Thread mCamera_pic_Thread=null;
	public static RecordThread mRecordThread=null;
	public Handler mHandler=new Handler();
	public Button mButton1;
	public Button mButton2;
	public boolean is_video=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton1=(Button)findViewById(R.id.button1);
        mButton2=(Button)findViewById(R.id.button2);
        mButton1.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO 自动生成的方法存根
				mButton1.setClickable(false);
				take_pic();
				mHandler.postDelayed(new Runnable()
				{
					
					@Override
					public void run()
					{
						// TODO 自动生成的方法存根
						mButton1.setClickable(true);
					}
				}, 8000);
			}
		});
        mButton2.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO 自动生成的方法存根
				if(!is_video)
				{
					is_video=true;
					take_video();
					mButton2.setText("停止录像");
				}
				else
				{
					is_video=false;
					take_video_now();
					mButton2.setText("开始录像");
				}

			}
		});
    }

    
    private void take_pic()
    {
    	Intent intent = new Intent(this, Camera_pic_Activity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.putExtra(Camera_pic_Activity.CAMERA_KIND,0);//0 for back,1 for front
    	intent.putExtra(Camera_pic_Activity.CAMERA_TIME, 1000*10);
    	intent.putExtra(Camera_pic_Activity.CAMERA_NUM, 1);
    	Toast.makeText(getApplicationContext(),
    			getString(R.string.service_started), Toast.LENGTH_SHORT)
    			.show();
    	startActivity(intent);

    }
    private void take_video()
    {
    	Intent intent = new Intent(this, Camera_video_Activity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.putExtra(Camera_video_Activity.CAMERA_V_KIND,0);
    	intent.putExtra(Camera_video_Activity.CAMERA_V_TIME,
    			(long) (1000 * 60 * 5));
    	intent.putExtra(Camera_video_Activity.CAMERA_V_QUALITY, 2);
    	startActivity(intent);
    	Toast.makeText(getApplicationContext(),
    			getString(R.string.service_video_started),
    			Toast.LENGTH_SHORT).show();

    }
    
    private void take_video_now()
    {
    	if(MainActivity.mRecordThread!=null)
    	{
    		MainActivity.mRecordThread.mHandler.removeCallbacks(MainActivity.mRecordThread.mRunnable);
    		MainActivity.mRecordThread.mHandler.post(MainActivity.mRecordThread.mRunnable);
    	}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
