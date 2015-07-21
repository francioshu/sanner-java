package com.estar.net;

import java.io.DataOutputStream;
import java.io.IOException;

import android.util.Log;

public class ScannerMessage {
	private static String TAG = "SCANNER";
	public  int  type;
	public  int cmd;
	public int  id;
	public int  size;
	byte[] data;
	DataOutputStream dotStream;
	
	public ScannerMessage(){
		
		
	}
	
	public void write(DataOutputStream stream){
		dotStream = stream;
		if(type == Constant.MSG_TYPE_CMD){
			
				writeCommand();
		}else{
			
				writePicture();
		}

		
	}
	
	private void writeCommand(){
		Log.d(TAG, "writeCommand:"+id);
		
		try {
			dotStream.writeInt(type);
			dotStream.writeInt(cmd);
			dotStream.writeInt(id);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void writePicture(){
		
	}
	
}
