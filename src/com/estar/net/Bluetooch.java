package com.estar.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.estar.scanner.ScannerLister;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

		
public class Bluetooch {
		
		private static final String TAG = "SCANNER";
		protected static final String CONNECT_FAILED = null;
		protected static final String EXTRA_DEVICE_ADDRESS = "device_address";
		private static final Intent data = null;
		private static final String REQUEST_DISCOVERABLE = null;
		private static final String REQUEST_DISCOVERY = null;			
	   private List<String> devices;
	   private List<BluetoothDevice> deviceList; 
	   private final String lockName = "TurnTable";
//	   private final String lockName = "aigo MH01";
//	   private final String lockName = "PENGLONGYAO";
	   private BluetoothAdapter adapter;
	   private Context mContext = null;
	   public BluetoothDevice device1;
	   BlueClient  blueclient;
	   boolean mStop;	 
		InputStream mmInStream;
		OutputStream mmOutStream;
		private  ScannerLister lister;
	   
		
		static BluetoothDevice  remoteDevice;
		
	   public Bluetooch(Context context){
		   mContext=context;
	     deviceList = new ArrayList<BluetoothDevice>();
		  devices = new ArrayList<String>();
		  blueclient = new BlueClient();
	   }
	  
	   
		public  void search() {
			Log.d(TAG, "search");
			adapter = BluetoothAdapter.getDefaultAdapter();
			if (!adapter.isEnabled()) {
				adapter.enable();
	        }	    
	        Intent searchIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);	
	        searchIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500000);            
	       
	        
	        //扫描附近设备
	        if(adapter.isDiscovering()){	        	
	        	adapter.cancelDiscovery();
	        }
	        adapter.startDiscovery();//搜索附近设备
	        Log.d(TAG, "搜索附近设备");
	        
	        
	        //注册扫描结果广播
	        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	        mContext.registerReceiver(mBluetoothReceiver, filter);
	        Log.d(TAG, "注册扫描结果广播,ACTION_FOUND");
	        // mContext 是传进来的activity 

	        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	        mContext.registerReceiver(mBluetoothReceiver, filter);
	        Log.d(TAG, "注册扫描结果广播,ACTION_DISCOVERY_FINISHED");
	        
	                 
	        // 获取已保存的配对设备
	       Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();	       
			if (pairedDevices.size() > 0) {	           
	            for (BluetoothDevice device : pairedDevices) {
	            	devices.add(device.getName() + "\n" + device.getAddress());
	            }
	        } else {
	            String noDevices = "No devices have been paired";
	            devices.add(noDevices);
	            Log.d(TAG, "devices"+devices);
	        }
					
//			onDestroy();
		}
		
	

		public void onDestroy() {		 
	//	        super.onDestroy();
		        // Make sure we're not doing discovery anymore
		        if (adapter != null) {
		        	adapter.cancelDiscovery();
		        }
		        // Unregister broadcast listeners
		        mContext.unregisterReceiver(mBluetoothReceiver);
		    }
				 

	    // 当发现新蓝牙设备后扫描完成广播
	   	 
		private  BroadcastReceiver mBluetoothReceiver= new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            Log.d(TAG, "进入广播:"+action.toString());
	            
	            //发现设备
	            if (BluetoothDevice.ACTION_FOUND.equals(action)) {	            	
	                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	                Log.d(TAG, "发现设备");	  
	                devices.add(device.getName() + "\n" + device.getAddress());
            		Log.d(TAG, "devices:"+device.getName()+"["+device.getAddress()+"]");
            		
	             // 添加非配对设备
	                if (isLock(device)) {           
	                		String address = device.getAddress();

	                		
	                		
	                		devices.add(device.getName() + "\n" + device.getAddress());
	                		Log.d(TAG, "devices"+devices);
	                		deviceList.add(device);
	                		Log.d(TAG, "deviceList"+deviceList);		
	 	                	Log.d(TAG, "address"+address);		
	 	                	device1 = adapter.getRemoteDevice(address);	             
	 	                	Log.d(TAG, "device1:"+device1);		
	 	                	blueclient.nativeInit(device);	 	     
	 	                	         	
	                }              
	                }	               	            
	        }		
	    };
	    
	   /* private BroadcastReceiver _discoveryReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) 
			{
				 卸载注册的接收器 
				unregisterReceiver(mBluetoothReceiver);
				unregisterReceiver(this);
				
			}
	    };
*/
			
	
	    public  void  onMessage(int type,int arg1 ,int arg2){
	 			
				if(lister != null){
	 				Message msg = new Message();
	 				msg.what = type;
	 				msg.arg1 = arg1;
	 				msg.arg2 = arg2;			
	 				lister.onMessage(msg);
	 			}
	 			Log.d(TAG,"sendMessage  server:"+type+" "+arg1 +" " + arg2);
	 		}
	    
		public  void setLister(ScannerLister  ll){
			lister  = ll;
			
		}
	
    
	    private boolean isLock(BluetoothDevice device) {
	        boolean isLockName = (lockName.equals( device.getName()));
//	        boolean isSingleDevice = devices.indexOf(device.getName()) == -1;
//	        return isLockName && isSingleDevice;
	        return isLockName;
	    }

	    public void  sendMessage(){
	    	blueclient.sendmsg(0, 0);
	    	
	    }


		public void stop() {
			// TODO Auto-generated method stub
			blueclient.stop();
		}
		
		
		public boolean  isConnected(){
			
			return blueclient.isConnected();
		}
		
	   	    
	}	    
	    
	    
	    
	    
	    
	    
	    
	    
