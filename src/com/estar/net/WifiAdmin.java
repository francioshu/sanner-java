package com.estar.net;

import java.util.List;
import com.estar.scanner.ScannerLister;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Message;
import android.util.Log;


public class WifiAdmin {
	
	private static final String TAG = "SCANNER";
	
	private WifiManager mWifiManager;
	private WifiInfo mWifiInfo;
	// 扫描出的网络连接列表
	private List<ScanResult> mWifiList;
	private List<WifiConfiguration> mWifiConfiguration;

	private WifiLock mWifiLock;
	
	private String mPasswd = "hw123456";
	private String mSSID = "HotSpot1";
	
	int mNetId ;
	
	public static final int TYPE_NO_PASSWD = 0x11;
	public static final int TYPE_WEP = 0x12;
	public static final int TYPE_WPA = 0x13;
	
	private Context mContext = null;

	boolean mIsConnected = false;
	boolean mIsConnecting = false;
	boolean mIsResReceiver = false;
	ScannerLister mLister;
	
	public WifiAdmin(Context context) {
		
		mContext = context;
		
		// 取得WifiManager对象
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		// 取得WifiInfo对象
		mWifiInfo = mWifiManager.getConnectionInfo();
		Log.v(TAG, "getIpAddress = " + mWifiInfo.getIpAddress());
		
	}

	public void connectToServer(){
		Log.d(TAG, "connectToServer");
;
		
		openWifi();
		
		sanWork.start(20,1000);//10 times, 1000ms
		
	}
	
	
	public void setLister(ScannerLister ll ){
		
		mLister = ll;
	}
	
	// 添加一个网络并连接
	public void addNetwork(WifiConfiguration wcg) {
		
	//	register();
		
		WifiApAdmin.closeWifiAp(mContext);
		
		int wcgID = mWifiManager.addNetwork(wcg);
		boolean b = mWifiManager.enableNetwork(wcgID, true);
	}
	
	MyTimerCheck  sanWork = new MyTimerCheck() {
		@Override
		public void doTimerCheckWork() {
			// TODO Auto-generated method stub
			
			startScan();
			
			if(mIsConnecting && isWifiContected(mContext) == WIFI_CONNECTED){

				Log.d(TAG, " doTimerCheckWork:WIFI_CONNECTED!! ");
				mIsConnected = true;
				
				mWifiInfo = mWifiManager.getConnectionInfo();
				mNetId = mWifiInfo.getNetworkId();
				int ip = mWifiInfo.getIpAddress();
				Log.d(TAG,"IP:"+intToIp(ip));
				
				Message msg = new Message();
				msg.what = Constant.MSG_WIFI_CONNECTED;
				msg.arg1 = ip;
				
				mLister.onMessage(msg);
				
				sanWork.exit();
				
			}
			
			
			if(lookUpScan() && !mIsConnected && !mIsConnecting){
				
				mIsConnecting = true;
				Log.d(TAG,"try to connect...");
//				if(!mIsResReceiver){
//					mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
//					mIsResReceiver = true;
//				}
				WifiConfiguration wcg = createWifiInfo(mSSID, mPasswd, TYPE_WPA);
				addNetwork(wcg);
			}
			
		}
		
		@Override
		public void doTimeOutWork() {
			// TODO Auto-generated method stub
			if(!mIsConnected){
				Log.d(TAG,"doTimeOutWork 1");
			
			}
			Log.d(TAG,"doTimeOutWork2");
			this.exit();
		}
	};
	
	// 打开WIFI
	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	
	// 关闭WIFI
	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	public void startScan() {
		mWifiManager.startScan();
		mWifiList = mWifiManager.getScanResults();
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
	}
	
	
	
	// 查看扫描结果
	public boolean lookUpScan() {

		for (int i = 0; i < mWifiList.size(); i++) {
			// 匹配SSID，如果找到指定的wifi，返回true
	
			ScanResult res = mWifiList.get(i);
			
			if((res.SSID).equalsIgnoreCase(mSSID)){
				return true;
			}
			Log.d(TAG,res.SSID);

		}
		return false;
	}

	// 得到MAC地址
	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// 得到接入点的BSSID
	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	// 得到IP地址
	public int getIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}
	
	public static final int WIFI_CONNECTED = 0x01;
	public static final int WIFI_CONNECT_FAILED = 0x02;
	public static final int WIFI_CONNECTING = 0x03;
	public static final int OBTAINING_IPADDR = 0x04;
	/**
	 * 判断wifi是否连接成功,不是network
	 * 
	 * @param context
	 * @return
	 */
	public int isWifiContected(Context context) {
		
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		Log.d(TAG,"isWifiContected:"+wifiNetworkInfo.getDetailedState());
		
		if ( wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTING) {
			return WIFI_CONNECTING;
		} else if (wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTED || wifiNetworkInfo.getDetailedState() == DetailedState.CAPTIVE_PORTAL_CHECK) {
			return WIFI_CONNECTED;
		} else if(wifiNetworkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR) {
			return OBTAINING_IPADDR;
		}else{
			return WIFI_CONNECT_FAILED;
		}
		
	}
	
	private String intToIp(int i)  {
			return "" + (i & 0xFF) + "."+ ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) +"."+((i >> 24 ) & 0xFF);
	} 
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			
			
			if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
				Log.d(TAG, "RSSI changed");
				
				//有可能是正在获取，或者已经获取了
				Log.d(TAG, " intent is " + WifiManager.RSSI_CHANGED_ACTION);
				
				if (isWifiContected(mContext) == WIFI_CONNECTED) {
					Log.d(TAG, " WIFI_CONNECTED!! ");
					mIsConnected = true;
					
					mWifiInfo = mWifiManager.getConnectionInfo();
					mNetId = mWifiInfo.getNetworkId();
					int ip = mWifiInfo.getIpAddress();
					Log.d(TAG,"IP:"+intToIp(ip));
					
					Message msg = new Message();
					msg.what = Constant.MSG_WIFI_CONNECTED;
					msg.arg1 = ip;
					
					mLister.onMessage(msg);
					
					sanWork.exit();
					
				} else if (isWifiContected(mContext) == WIFI_CONNECT_FAILED) {
					Log.d(TAG, " WIFI_CONNECT_FAILED ");
					
					Message msg = new Message();
					msg.what = Constant.MSG_WIFI_CONNECT_FAILED;
					msg.arg1 = 0;
					
					mLister.onMessage(msg);
					
					sanWork.exit();
				} else if (isWifiContected(mContext) == WIFI_CONNECTING) {
					Log.d(TAG, " WIFI_CONNECTING ");
					
				}
			}
		}
	};
	
	public WifiConfiguration createWifiInfo(String SSID, String password, int type) {
		
		Log.v(TAG, "SSID = " + SSID + "## Password = " + password + "## Type = " + type);
		
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		
		// 分为三种情况：1没有密码2用wep加密3用wpa加密
		if (type == TYPE_NO_PASSWD) {// WIFICIPHER_NOPASS
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
			
		} else if (type == TYPE_WEP) {  //  WIFICIPHER_WEP 
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == TYPE_WPA) {   // WIFICIPHER_WPA
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		} 
		
		return config;
	}

	public void stop() {
		// TODO Auto-generated method stub
		if(mIsResReceiver){
			Log.d(TAG,"unregisterReceiver mBroadcastReceiver");
			mContext.unregisterReceiver(mBroadcastReceiver);
			mIsResReceiver = false;
		}
		mWifiManager.removeNetwork(mNetId);
	}
	
	
	
}
