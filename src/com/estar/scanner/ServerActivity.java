package com.estar.scanner;

import com.estar.net.Bluetooch;
import com.estar.net.Constant;
import com.estar.net.ScannerServer;
import com.estar.net.WifiApAdmin;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ServerActivity extends Activity implements ScannerLister{

	private static String TAG = "SCANNER";
	SurfaceView mSurfaceView;
	
	Button btntakePic;
	Button btntaketest;
	TextView tex1;
	TextView tex2;
	CameraOption co;
	boolean serverOnTaken = true;
	boolean clientOnTaken = true;
	int  mCurrNum = 0;
	WifiApAdmin wifiAp;
	Bluetooch  btd;
	
	ScannerServer ssr;
	
	int  TOTALOFPIC =  360;
	
	private String mPasswd = "hw123456";
	private String mSSID = "HotSpot1";
	
	int testnum = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//常亮 
		Log.d(TAG,"ServerActivity");
		
		mSurfaceView = (SurfaceView)findViewById(R.id.surface);
		
		btntakePic = (Button)findViewById(R.id.takepic);
		tex1 = (TextView)findViewById(R.id.textView1);
		tex1.setTextColor(0xFFFF0000);
		tex2 = (TextView)findViewById(R.id.textView2);
		tex2.setTextColor(0xFFFF0000);
		
		btntakePic.setOnClickListener(takePicLL);
	//	btntakePic.setClickable(false);
		mCurrNum = 0;
		
		co = new CameraOption(this,mSurfaceView);
		co.setAngle(180);
		co.setLinster(this);
		
		
		//开启wifi热点
		wifiAp = new WifiApAdmin(this);
		wifiAp.setLister(this);
		wifiAp.startWifiAp(mSSID, mPasswd);
		
		ssr = new ScannerServer();
		ssr.setLister(this);
		
		btd = new Bluetooch(this);
		btd.setLister(this);
		btd.search();
		
		infohandr.sendEmptyMessage(0);
		
		
		btntaketest  = (Button)findViewById(R.id.taketest);
		btntaketest.setOnClickListener(new  View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ssr.sendToClient(132, testnum++);
			}
		});
		
	}
	
	Handler infohandr=new Handler(){
		@Override
		public void handleMessage(Message msg){
			
			String  str ;
			if(ssr.getClientStatus()){
				str = "client client 已连接";	
			}else{
				str = "client client 未连接";	
			}
			tex1.setText(str);	

			if(btd.isConnected()){
				str = "蓝牙已连接";
			}else{
				str = "蓝牙未连接";
			}
			tex2.setText(str);	
			sendEmptyMessageDelayed(0, 300);
		}
	};
	
	
	
	View.OnClickListener takePicLL = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//只有当本机拍摄完一张 而且 客户端也拍摄完成才能进行 下一次拍摄
			mCurrNum = 0;
			takepicHandler.sendEmptyMessage(0);
			

		}
	};
	

	
	Handler takepicHandler  = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			
			if(mCurrNum >= TOTALOFPIC){
				super.handleMessage(msg);
				return;
			}
			if (co != null && serverOnTaken && clientOnTaken){
				serverOnTaken = false;
				clientOnTaken = false;
			
				btd.sendMessage();
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//通知客户端进行拍摄
				Log.d(TAG,"通知客户端进行拍摄");
				ssr.sendToClient(Constant.MSG_CLIENT_TAKEN, mCurrNum);
			    
			    co.takePic(0, mCurrNum);
				 mCurrNum++;
		//	    btntakePic.setClickable(false);
			}
			super.handleMessage(msg);
		}
	};
	
	
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {//按手机上的返回键，退出
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			//关闭wifi热点
			wifiAp.closeWifiAp(ServerActivity.this);
			ssr.nativeStop();
			//关闭摄像头预览
			if(co != null) co.stop();
	//		wifiAdmin.stop();
			System.exit(0);
		}		
		return super.onKeyDown(keyCode, event);
	}

@Override
	public void onMessage(Message msg) {
		// TODO Auto-generated method stub
	   
		int cmd = msg.what;
		switch(cmd){
		
		case Constant.MSG_WIFIAP_SETUPED:
			//wifi热点建立成功
			infohandr.sendEmptyMessage(msg.what);

			//初始化服务器
			ssr.nativeInit();
			Log.d(TAG,"MSG_WIFIAP_SETUPED");
			break;
		case Constant.MSG_CLIENT_CONNECTED:
			infohandr.sendEmptyMessage(msg.what);
			Log.d(TAG,"客户端连接到服务器，可以开始拍照");
			//客户端连接到服务器，可以开始拍照
			clientOnTaken = true;
			btntakePic.setClickable(true);
			break;
		case Constant.MSG_CAMERA_ONTAKEN:
			Log.d(TAG,"本机拍摄完一张照片");
			//本机拍摄完一张照片
			serverOnTaken = true;
			takepicHandler.sendEmptyMessage(0);
			break;
		case Constant.MSG_CLIENT_ONTAKEN:
			Log.d(TAG,"客户端拍摄完图片:"+msg.arg1);
			//客户端拍摄完一张图片
			clientOnTaken = true;
			takepicHandler.sendEmptyMessage(0);
			break;
			
		case Constant.MSG_CLIENT_FILEREV:
			Log.d(TAG,"收到客户端传输的图片");
			//收到客户端传输的图片
			Log.d(TAG,"MSG_CLIENT_FILEREV");
			
			
			break;	
		default:
			
			break;
		
		}
	}


	
}
