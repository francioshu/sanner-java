package com.estar.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.estar.scanner.ScannerLister;

import android.os.Message;
import android.util.Log;

public class ScannerServer {
	private static String TAG = "SCANNER";
	private static ScannerLister lister;
	 ServerSocket sSocket;
	 
	 Thread accept_thread;
	 Thread client_thread;
	 
	 boolean accept_thread_quit = false;
	 boolean recv_thread_quit = false;
	 
		Socket socket;
		DataInputStream diStream;
		DataOutputStream dotStream;
	  
	public ScannerServer(){
		
	
	}
	
	public  void nativeInit() {
		// TODO Auto-generated method stub

		try {
			sSocket=new ServerSocket(8877);
			Log.d(TAG,"监听8877接口......");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		accept_thread_quit = false;
		accept_thread = new Thread(server_accept);
		accept_thread.start();
	}
	
	Runnable  server_accept = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try
			{

				while(!accept_thread_quit)
				{

						Socket sct =sSocket.accept();
						
						if(  socket == null  ||  !socket.isConnected()){
							socket = sct;
							diStream=new DataInputStream(socket.getInputStream());
							dotStream=new DataOutputStream(socket.getOutputStream());
							
							Log.d(TAG, "server_accept:"+socket);
							recv_thread_quit = false;
							client_thread = new Thread(client_recv);
							client_thread.start();
						}

				}
				

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
	
	
	Runnable client_recv = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG,"start   client_recv!!");
			try
			{
				while(!recv_thread_quit)
				{
					int type = diStream.readInt();
					if(type == Constant.MSG_TYPE_CMD){
						Log.d(TAG, "MSG_TYPE_CMD");
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
	
	
	public boolean getClientStatus(){
		
		if(  socket != null ){

			return socket.isConnected();
			
		}

		
		return false;
		
	}
	
	
	public  int sendToClient(int cmd,int id){
		
		Log.d(TAG, "sendToClient:"+cmd+" :"+id);
		
		ScannerMessage msg = new ScannerMessage();
		msg.type = Constant.MSG_TYPE_CMD;
		msg.cmd = cmd;
		msg.id = id;
		
		try {
			dotStream=new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "socket dead!!!");
			e.printStackTrace();
		}
		msg.write(dotStream);

		return 0;
	}
	
	public  void setLister(ScannerLister  ll){
		lister  = ll;
		
	}
	
	
	public  void  onMessage(int cmd,int id ,int arg2){
		Log.d(TAG,"sendMessage java server:"+cmd+" "+id +" " + arg2);
		if(lister != null){
			Message msg = new Message();
			msg.what = cmd;
			msg.arg1 = id;
			msg.arg2 = arg2;
			
			lister.onMessage(msg);
		}
		
	}

	public  void nativeStop() {
		// TODO Auto-generated method stub
		accept_thread_quit = true;
		recv_thread_quit = true;

		
	}



	
}
