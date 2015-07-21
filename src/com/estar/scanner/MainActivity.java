package com.estar.scanner;

import com.estar.net.ScannerClient;
import com.estar.net.ScannerServer;
import com.estar.net.WifiAdmin;
import com.estar.net.WifiApAdmin;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import java.io.File;

public class MainActivity extends Activity{
	
	static{
		System.loadLibrary("scanner");
	}
	
	private static String TAG = "SCANNER";

	CameraOption co;
	
	Button  btnserver;
	Button  btnclient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnserver = (Button)findViewById(R.id.server);
		btnserver.setOnClickListener(serverLis);
		

		btnclient = (Button)findViewById(R.id.client);
		btnclient.setOnClickListener(clientLis);
	
	}
	
	String savePath = "/mnt/sdcard/calib/";
	File folder = new File(savePath);
	private void deleteFile(File folder){ 
		   if(folder.exists()){                    //判断文件是否存在
		    if(folder.isFile()){                    //判断是否是文件
		    	folder.delete();                     
		    }else if(folder.isDirectory()){              //否则如果它是一个目录
		     File files[] = folder.listFiles();               //声明目录下所有的文件 files[];
		     for(int i=0;i<files.length;i++){            //遍历目录下所有的文件
		      this.deleteFile(files[i]);             //把每个文件 用这个方法进行迭代
		     } 
		    } 
		    folder.delete(); 
		   }else{ 
		    System.out.println("所删除的文件不存在！"+'\n'); 
		   } 
		}
	
	
	
	
	View.OnClickListener serverLis = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			startActivity(new Intent (MainActivity.this, ServerActivity.class) );  
		//	deleteFile(folder);
			
		}
	};

	View.OnClickListener clientLis = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
	//		deleteFile(folder);
			startActivity(new Intent (MainActivity.this, ClientActivity.class) );  
		
		}
	};

	

	
}
