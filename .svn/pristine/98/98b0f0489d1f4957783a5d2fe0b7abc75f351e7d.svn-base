/*
 ~ Copyright (c) 2013, WSO2Mobile Inc. (http://www.wso2mobile.com) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
*/
package com.mdm;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.util.Log;

public class PhoneState {
	Context context = null;
	private long mStartRX = 0;
	private long mStartTX = 0;
	
	public PhoneState(Context context){
		this.context = context;
	}
	/**
	*Returns the device IP address
	*/
	public String getIpAddress(){
		WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
	}
	/**
	*Format the integer IP address and return it as a String
	*@param i - IP address should be passed in as an Integer
	*/
	public String intToIp(int i) {
       /* return ((i >> 24 ) & 0xFF ) + "." +
                    ((i >> 16 ) & 0xFF) + "." +
                    ((i >> 8 ) & 0xFF) + "." +
                    ( i & 0xFF) ;*/
        return (i & 0xFF) + "." +
        ((i >> 8 ) & 0xFF) + "." +
        ((i >> 16 ) & 0xFF) + "." +
        ((i >> 24 ) & 0xFF );
    }
	/**
	*Returns the available amount of memory in the device
	*/
	public String getAvailableMemory(){
		ActivityManager activityManager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);
        MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        Log.v("Available Memory", memoryInfo.availMem+"");
        String availMemory = memoryInfo.availMem+"";
        return availMemory; 
	}
	/**
	*Returns the amount of uploaded data in bytes
	*/
	public long getDataUploadUsage(){
		mStartTX = TrafficStats.getTotalTxBytes();
		return mStartTX;
	}
	/**
	*Returns the amount of downloaded data in bytes
	*/
	public long getDataDownloadUsage(){
		mStartRX = TrafficStats.getTotalRxBytes();
		return mStartRX;
	}
	/**
	*Returns the device battery information
	*/
	public float getBatteryLevel(){
		Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

	    // Error checking that probably isn't needed but I added just in case.
	    if(level == -1 || scale == -1) {
	        return 50.0f;
	    }

	    return ((float)level / (float)scale) * 100.0f;
		/*BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
            int scale = -1;
            int level = -1;
            int voltage = -1;
            int temp = -1;
            @Override
            public void onReceive(Context context, Intent intent) {
                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                Log.v("Battery Level", "level is "+level+"/"+scale+", temp is "+temp+", voltage is "+voltage);
                
                Battery battery = Battery.getInstance();
                battery.setLevel(level);
                battery.setScale(scale);
                battery.setTemp(temp);
                battery.setVoltage(voltage);
                
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(batteryReceiver, filter);
		return Battery.getInstance();*/
	}
}
