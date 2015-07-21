package com.estar.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import com.estar.scanner.ScannerLister;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;



public class BlueClient {
	private  String TAG = "SCANNER";
	//
	private  ScannerLister lister;
	private  BluetoothSocket mmSocket;
	private  BluetoothDevice mmDevice;
	private static final UUID UUID_COM = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	boolean mStop;
	InputStream mmInStream;
	OutputStream mmOutStream;
	private static OutputStream outstream =null;
	BluetoothAdapter adapter = null ;  	  
	private static BluetoothDevice device = null;
		 
		    
	public BlueClient(){
			 		  		  			 
	}  
		 		
	public void nativeInit(BluetoothDevice device1){
		
		Log.d(TAG, "nativeInit");
		BluetoothSocket tmp = null;		 
    	BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (!adapter.isEnabled()) {
				adapter.enable();
	    }
		byte[] pin = {1,2,3,4};
		
	//	boolean ret = device1.setPin(pin);
	//	Log.d(TAG,"set pin:"+ret);
		Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		enable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 5000); 	
		Log.d(TAG, "adapter"+adapter);
		Log.d(TAG, "device1"+device1);
		adapter.cancelDiscovery();
		mmDevice = device1;    
		new Thread(recServer).start();      
	
	}
	
	
	Runnable recServer = new Runnable() {  
        @Override  
        public void run() {        	  		
        	BluetoothSocket tmp = null;	
          try {
        	  mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));			
        	  mmSocket.connect();
        	  Log.d(TAG,"connect success!");      	 
//        	  onMessage(Constant.MSG_BLUE_CONNECT,0,0);        
          } catch (IOException e) {
        	  Log.d(TAG,e.toString());
        	  e.printStackTrace();  
        	  bluetoochstop();
          	}       									 			          		            		 
	        try {	
	        	if (mmSocket != null) {	
	        		mmOutStream = mmSocket.getOutputStream();
	        		outstream = mmOutStream;
	        		 Log.d(TAG,"get outputstream success!");    
	        		onMessage(100,0,0);
/*	        		while(!mStop)	{
	        		sendmsg();
	        		try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}*/
	        	} 
	        } catch (IOException e) {		        	
	        	Log.d(TAG,e.toString());	  
	        	e.printStackTrace(); 
	        	bluetoochstop();
	        }
	       	
//			}
	               
        }    
        };       
        public  void sendmsg(int cmd,int arg){	   		
        	
        	if(outstream == null)    {
        		Log.d(TAG, "outstream:"+outstream);
        		return;
        	}
        	
    		Log.d(TAG,"cmd："+cmd+"arg:"+arg);
    		Log.d(TAG,"服务器发送蓝牙指令");   		   		
 	        	byte bytes = 1;	        		       
 		        try {
 //		        	Log.d(TAG, "bluetooch send message start...");
 		        	outstream.write(bytes);
 		        	outstream.flush();
 		        	Log.d(TAG,"bluetooch send message success"+arg);
 		        	   		
 	        } catch (IOException e) {		        	
 	        	Log.d(TAG,e.toString());	  
 	        	e.printStackTrace(); 
 	        	bluetoochstop();
 	        }
    	   
    	}
	    public void bluetoochstop() {
	        try {
	        	if(mmSocket!=null){
	        		mmSocket.close();
	        		mStop=true;
	          }
	        } catch (IOException e) { 
	        	e.printStackTrace();
	        }    
	    }
	
	    public  void  onMessage(int type,int arg1 ,int arg2){
			if(lister != null){
				Message msg = new Message();
				msg.what = type;
				msg.arg1 = arg1;
				msg.arg2 = arg2;			
				lister.onMessage(msg);
			}
			Log.d(TAG,"sendMessage java server:"+type+" "+arg1 +" " + arg2);
		}
		public  void setLister(ScannerLister  ll){
			lister  = ll;
		}

		public void stop() {
			// TODO Auto-generated method stub
			try {
				mmSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		public boolean  isConnected(){
			if(mmSocket == null)  
				return false;
			
			
			return mmSocket.isConnected();
		}
		
}
