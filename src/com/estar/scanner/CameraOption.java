package com.estar.scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.estar.net.Constant;


@SuppressLint("NewApi")public class CameraOption implements SurfaceHolder.Callback{

	private static String TAG = "SCANNER";

	private int previewWidth = 1280;  //预览分辨率
	private int previewHeight = 720;

	int pictureWidth = 4160;
	int pictureHeight = 3120;

	int angle = 0;

	int isR=0;
	Camera mCamera = null;	
	int defaultCameraId = 0;

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;

	ScannerLister  mLister;
	int mPicNO;
	public int mType;

	public CameraOption(Context context ,SurfaceView surface ){
		Log.e(TAG,"mCamera CameraOption");
		mSurfaceView = surface;
		//   mSurfaceView = new SurfaceView(context);

		//     addView(mSurfaceView);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public  String WBalance = Parameters.WHITE_BALANCE_SHADE;//WHITE_BALANCE_WARM_FLUORESCENT
	private  boolean  needResetPara = false;
	public void setWhiteBalance(String value){
		Log.e(TAG,"setWhiteBalance "+value);
		WBalance = value;
		mCamera.stopPreview();
		setPara();
		mCamera.startPreview();


	}


	public void setAngle(int an)
	{
		angle = an;
		if(angle==180){
			isR=1;}

	}
	public void setPara(){
		if(mCamera == null)  return;

		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewFpsRange(15, 15);
		parameters.setPreviewSize(previewWidth,previewHeight);
		parameters.setExposureCompensation(2);
		parameters.setWhiteBalance(WBalance) ;
		//		parameters.setSceneMode(parameters.SCENE_MODE_NIGHT);
		parameters.setPictureSize(pictureWidth, pictureHeight);
		parameters.setPictureFormat(PixelFormat.JPEG);
		Log.d(TAG,"setPara angle"+angle);
		parameters.setRotation(angle);
		mCamera.setParameters(parameters); 

	}


	public void stop(){
		Log.d(TAG,"mCamera stop");
		if(mCamera != null){
			Log.d(TAG,"mCamera stopPreview");
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}


	String savePath = "/mnt/sdcard/calib/";

	public void saveJpeg(byte[] data){
		File folder = new File(savePath);
		if(!folder.exists()) 
		{
			folder.mkdir();
		}

		String RL="R";

		if(isR ==1){
			RL="L";
		}

		String jpegName = savePath + "calib-" +RL+ mPicNO +".jpg";

		//File jpegFile = new File(jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			fout.write(data);
			fout.flush();
			fout.close();
			Log.i(TAG, "saveJpeg:"+jpegName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "savejpeg error!!");
			e.printStackTrace();
		}
	}
	PictureCallback myJpegCallback = new PictureCallback() 
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub


			saveJpeg(data);

			mCamera.startPreview();

			Message msg = new Message();
			msg.what = Constant.MSG_CAMERA_ONTAKEN;
			msg.arg1 = mPicNO;


			mLister.onMessage(msg);
		}
	};
	
	boolean mState = false;//记录是否有对焦
	//boolean flag = true;//判断是否对焦
	private AutoFocusCallback autoFocusCallback = new AutoFocusCallback();
	
	/**
	 * 拍照
	 * @param type
	 * @param num
	 * @return
	 */
	
	boolean flag = true;
	public boolean takePic(int type,int num){

		mType = type;
		mPicNO = num;
		//		Log.d(TAG,"takePic type:"+mType+":"+mPicNO);
		if(mCamera != null){

			
			if (flag) {
				mCamera.autoFocus(autoFocusCallback);
			}else {
				mCamera.takePicture(myShutterCallback, null, myJpegCallback);
			}
			
			return true;
		}
		return false;

	}
	
	
	/**
	 * 建立Hanlder用于拍照时的中环站
	 */
	
	private Hanlder mHanlder = new Hanlder();
	private static final int FOCUS_SUCCESS = 0x12598;//用于标记对焦状态
	
	@SuppressLint("HandlerLeak")
	private class Hanlder extends android.os.Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case FOCUS_SUCCESS:
				mCamera.takePicture(myShutterCallback, null, myJpegCallback);
				flag = false;
				break;

			}
		}
	}
	
	
	

	/**
	 * 使用子线程等待第一次对焦ok
	 */
	private class AutoFocusCallback implements android.hardware.Camera.AutoFocusCallback{

		@Override
		public void onAutoFocus(boolean success, Camera mCamera) {
			// TODO Auto-generated method stub
			if (success) {

			}else {

			}
			mHanlder.sendEmptyMessage(FOCUS_SUCCESS);

		}

	}

	ShutterCallback myShutterCallback = new ShutterCallback() 
	{
		public void onShutter() {//快门
		}
	};


	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.

		mCamera = Camera.open(defaultCameraId);


		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);//设置预览显示
			}
		} catch (IOException exception) {
			Log.d(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(previewWidth,previewHeight);
		//   parameters.setPictureSize(4160, 3120);
		parameters.setExposureCompensation(0);
		//       requestLayout();//要求布局
		Log.d(TAG,"surfaceChanged in");
		mCamera.cancelAutoFocus() ;
		//parameters.setZoom(0);
		//mCamera.setParameters(parameters);
		//mCamera.stopSmoothZoom();
		//    mCamera.setFaceDetectionListener(this);
		//    mCamera.startFaceDetection();
		//   mCamera.setPreviewCallback(cb);
		parameters.setPictureSize(pictureWidth, pictureHeight);
		Log.d(TAG,"setPara angle"+angle);
		parameters.setRotation(angle);
		mCamera.setParameters(parameters);
		Log.e(TAG, "Setparam");
		mCamera.startPreview();//开始预览
		Log.e(TAG,"surfaceChanged  out");
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}


	public void setLinster(ScannerLister ll) {
		// TODO Auto-generated method stub
		mLister = ll;
	}
}
