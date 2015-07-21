package com.estar.scanner;

import com.estar.net.Bluetooch;
import com.estar.net.Constant;
import com.estar.net.ScannerClient;
import com.estar.net.ScannerServer;
import com.estar.net.WifiAdmin;
//import com.estar.scanner.R;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

public class ClientActivity extends Activity implements ScannerLister{

	private static String TAG = "SCANNER";
	SurfaceView mSurfaceView;
	WifiAdmin wifiAdmin;
	CameraOption co;
	
	Button btntak;
	TextView tex1;
	TextView tex2;
	
	ScannerClient sct;
	
	int num = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//常亮 	
		setContentView(R.layout.activity_client);

		mSurfaceView = (SurfaceView)findViewById(R.id.surface);
		tex1 = (TextView)findViewById(R.id.textView1);
		tex1.setTextColor(0xFFFF0000);
		tex2 = (TextView)findViewById(R.id.textView2);
		tex2.setTextColor(0xFFFF0000);
		
		co = new CameraOption(this,mSurfaceView);
		co.setLinster(this);
		
		//开启自动连接指定wifi
		wifiAdmin = new WifiAdmin(ClientActivity.this);
		wifiAdmin.setLister(this);
		wifiAdmin.connectToServer();
		
		sct = new ScannerClient();
		sct.setLister(ClientActivity.this);
		
		
		btntak = (Button)findViewById(R.id.takepic);
		btntak.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				sct.nativeSendMessage(132,num,0);
				num++;
			}
		});

		infohandr.sendEmptyMessage(0);
		
	}
	
	Handler infohandr=new Handler(){
		@Override
		public void handleMessage(Message msg){
			
			String  str ;
			if(sct.getServerStatus()){
				str = "已连接至服务器";	
			}else{
				str = "正在连接...";	
			}
			tex1.setText(str);	

			sendEmptyMessageDelayed(0, 300);
		}
	};
	
	
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {//按手机上的返回键，退出
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			sct.nativeStop();
			wifiAdmin.stop();
			if(co != null) co.stop();
			System.exit(0);
		}		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onMessage(Message msg) {
		// TODO Auto-generated method stub
		int cmd = msg.what;
		Log.d(TAG,"CMD"+cmd);
		switch(cmd){
		
		case Constant.MSG_WIFI_CONNECTED:
			//连接到指定的wifi
			Log.d(TAG,"MSG_WIFI_CONNECTED:"+msg.arg1);
			
			int addr = msg.arg1;
			
			if(addr == 0){
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				// 取得WifiManager对象
				WifiManager mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
				// 取得WifiInfo对象
				WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
				addr = mWifiInfo.getIpAddress();
				Log.v(TAG, "getIpAddress = " + addr);
				
			}
			
			
			//根据当前IP得到服务器IP
			int ip = getServerIP(addr);
			Log.d(TAG,"SERVER IP:"+intToIp(ip));
//			handr.sendEmptyMessage(msg.what);
			//初始化客户端，尝试与服务端建立连接
			sct.nativeInit(intToIp(ip));
			Log.d(TAG,"MSG_init");
			break;
			
		case Constant.MSG_CLIENT_CONNECTED:
//			handr.sendEmptyMessage(msg.what);
			Log.d(TAG,"MSG_CLIENT_CONNECTED");
			break;
		case Constant.MSG_WIFI_CONNECT_FAILED:
			
			wifiAdmin.connectToServer();
			
			break;
		case Constant.MSG_CLIENT_TAKEN:
			//收到服务端命令，拍摄照片
			Log.d(TAG,"拍摄照片");
			if(co != null){
				co.takePic(0, msg.arg1);
			}
			break;	
		case Constant.MSG_CAMERA_ONTAKEN:
			Log.d(TAG,"本机拍摄照片完成，通知服务端并传输图像给服务端");
			//本机拍摄照片完成，通知服务端并传输图像给服务端
			sct.nativeSendMessage(Constant.MSG_CLIENT_ONTAKEN, msg.arg1, 0);
			break;
			
			
		default:
			
			break;
		
		}
	}

	
	

	private String intToIp(int i)  {
		return "" + (i & 0xFF) + "."+ ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) +"."+((i >> 24 ) & 0xFF);
	} 
	
	
	public static int bytesToInt(byte[] bytes) {
        int addr = bytes[3] & 0xFF;
        addr |= ((bytes[2] << 8) & 0xFF00);
        addr |= ((bytes[1] << 16) & 0xFF0000);
        addr |= ((bytes[0] << 24) & 0xFF000000);
        return addr;
    }
	
	private int getServerIP(int i){
		
		int tmp = i & 0x00FFFFFF;
		Log.d(TAG,"IP1:"+intToIp(tmp));
		int add = 1;
		tmp |= ((add << 24) & 0xFF000000);
		Log.d(TAG,"IP2:"+intToIp(tmp));
		return tmp;
	}
	
	
}
