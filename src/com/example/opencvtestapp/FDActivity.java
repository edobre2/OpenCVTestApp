 package com.example.opencvtestapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FDActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OpenCv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private CascadeClassifier      mJavaDetector;
    private static final float PI = 3.1415926f;
    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
	private HomeKeyLocker          locker;
    public FDActivity() {
    	Log.i(TAG, "Instantiated a " + this.getClass());
    }
    
    public BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
    	@Override
    	public void onManagerConnected(int status) {
    		switch(status) {
	    		case LoaderCallbackInterface.SUCCESS:
	    		{
	    			Log.i(TAG, "OpenSC loaded successfully");
	    			
	    			try {
	    				InputStream is = getResources().openRawResource(R.raw.cascade_frontal21);
	    				File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
	    				mCascadeFile = new File(cascadeDir, "cascade_frontal21.xml");
	    				FileOutputStream os = new FileOutputStream(mCascadeFile);
	    				
	    				byte[] buffer = new byte[4096];
	    				int bytesRead;
	    				while ((bytesRead = is.read(buffer)) != -1) {
	    					os.write(buffer, 0, bytesRead);
	    				}
	    				is.close();
	    				os.close();
	    				mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
	    				if (mJavaDetector.empty()) {
	    					Log.e(TAG, "Failed to load cascade classifier");
	    					mJavaDetector = null;
	    				}
	    				else 
	    					Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
	    				cascadeDir.delete();
	    			}
	    			catch( IOException e) {
	    				e.printStackTrace();
	    				Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
	    			}
	    			
	    			mOpenCvCameraView.enableView();
	    		} break;
	    		default: {
	    			super.onManagerConnected(status);
	    		} break;
    		}
    	}
    };

    @Override
    public void onResume() {
        super.onResume();
    }
    
    public void unlock(View v) {
    	Log.i(TAG, "Unlock clicked");
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8 , this, mLoaderCallback);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "Called onCreate");
    	
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        locker = new HomeKeyLocker();
        locker.lock(this);
        
 	    if(getIntent()!=null&&getIntent().hasExtra("kill")&&getIntent().getExtras().getInt("kill")==1){
       	    finish();
        }
 	   
 	    try {
 	        startService(new Intent(this,LockscreenService.class));

 	        StateListener phoneStateListener = new StateListener();
 	        TelephonyManager telephonyManager =(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
 	        telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);

 	    }
        catch (Exception e) {
        	
        }
        TextView tv = (TextView) findViewById(R.id.textView1);
        DateFormat df = DateFormat.getDateInstance();
        tv.setText(df.format(new Date()));
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    class StateListener extends PhoneStateListener{
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            super.onCallStateChanged(state, incomingNumber);
            switch(state){
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    System.out.println("call Activity off hook");
                	finish();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    };
    
    @Override
    public void onPause() {
        super.onPause();
        if(mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    
	 
	@Override
	public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();		
	}

	@Override
	public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
        mGray = inputFrame.gray();
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();

        if (mJavaDetector != null) {
        	Core.flip(mGray.t(), mGray, 0);
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mGray, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        if (facesArray.length > 0) {
        	locker.unlock();
        	finish();
        }
        return mGray;
	}
}
