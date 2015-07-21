package com.estar.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import com.estar.scanner.ScannerLister;

import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class ScannerClient {
	private static String TAG = "SCANNER";
	private static ScannerLister lister;
	
	Socket socket;
	DataInputStream diStream;
	DataOutputStream dotStream;
	
	String serverIp = "192.168.1.1";
	Thread recv_thread;
	 boolean recv_thread_quit = false;
	 
	 
	public ScannerClient(){
		
		


    
	}

	
	public    int nativeInit(String ip ){
		serverIp = ip;
		Log.d(TAG,"nativeInit IP:"+serverIp);
			
		
			
			
			
		recv_thread_quit = false;
		recv_thread = new Thread(client_recv);
		recv_thread.setName("client_recv_thread ");
		recv_thread.start();
    	Log.d(TAG,"nativeInit  finished!!");
		return 0;
	}
	
	Runnable client_recv = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG,"start   client_recv!!");
			try
			{
		   		 socket=new Socket(serverIp,8877);
		   		 diStream=new DataInputStream(socket.getInputStream());
		   		 dotStream=new DataOutputStream(socket.getOutputStream());
		   		 
		     	Log.d(TAG, "socket:"+socket);
		    	Log.d(TAG, "diStream:"+diStream);
		    	Log.d(TAG, "dotStream:"+dotStream);
		    	
				while(!recv_thread_quit)
				{
					
					int type = diStream.readInt();
					if(type == Constant.MSG_TYPE_CMD){
						int cmd = diStream.readInt();
						int id = diStream.readInt();
						Log.d(TAG, "MSG_TYPE_CMD"+cmd+"-"+id);
						onMessage(cmd,id,0);
						
					}
				}
				
					Log.d(TAG, "client recv quit");
					diStream.close();
					dotStream.close();
					socket.close();

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	};
	
	
	public    int nativeStop(){
		
		recv_thread_quit = true;
		
		try {
			diStream.close();
			dotStream.close();
			socket.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		try {
			recv_thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "nativeStop");
		return 0;
	}
	public     int nativeSendMessage(int cmd,int id,int arg){
		Log.d(TAG,  "nativeSendMessage msg:"+cmd + " " + id);
		ScannerMessage msg = new ScannerMessage();
		msg.type = Constant.MSG_TYPE_CMD;
		msg.cmd = cmd;
		msg.id = id;
		
		msg.write(dotStream);
		
		return 0;
	}
	
	public  void  onMessage(int type,int arg1 ,int arg2){
		Log.d(TAG,"onMessage java  ScannerClient:"+type+" "+arg1 +" " + arg2);
		if(lister != null){
			Message msg = new Message();
			msg.what = type;
			msg.arg1 = arg1;
			msg.arg2 = arg2;
			
			lister.onMessage(msg);
		}
		
	}
	
	
	public  void setLister(ScannerLister ll) {
		// TODO Auto-generated method stub
		lister = ll;
	}


	public boolean getServerStatus() {
		// TODO Auto-generated method stub
		
		if(socket == null  || !socket.isConnected()){
			return false;
		}else{
			return true;
		}
	}
	
}
