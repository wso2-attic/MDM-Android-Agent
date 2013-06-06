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

import static com.mdm.CommonUtilities.SENDER_ID;
import static com.mdm.CommonUtilities.displayMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.MemoryInfo;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	DevicePolicyManager devicePolicyManager;
	ApplicationManager appList;
	static final int ACTIVATION_REQUEST = 47; 
	ProcessMessage processMsg = null;
		
    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
       // displayMessage(context, getString(R.string.gcm_registered));
       // ServerUtilities.register(context, registrationId);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("regId", registrationId);
        editor.commit();
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
    //    displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
          //  ServerUtilities.unregister(context, registrationId);
        	
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }
    
	@Override
    protected void onMessage(Context context, Intent intent) {
		String code = intent.getStringExtra("message").trim();
        Config.context = this;
        //devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    	processMsg = new ProcessMessage(Config.context, CommonUtilities.MESSAGE_MODE_GCM, intent);
    	
        /*Log.i(TAG, "Received message");
        String notification = "";
        String data = "";
        String ssid = "";
        String password = "";
 //       String message = getString(R.string.gcm_message);
        String code = intent.getStringExtra("message").trim();
        Log.v("Code",  code );
        
        String token = intent.getStringExtra("token").trim();
        Log.v("Token",  token );
        
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        appList = new ApplicationManager(GCMIntentService.this);
        
        
	        if(code.equals(CommonUtilities.OPERATION_DEVICE_INFO)){
	        	
	        	PhoneState phoneState = new PhoneState(GCMIntentService.this);
	        	Battery battery = phoneState.getBattery();
	            JSONObject obj=new JSONObject();
	      	  	try {
					obj.put("ip",phoneState.getIpAddress());
		      	  	obj.put("scale",battery.getScale()+"");
		      	  	obj.put("level",battery.getLevel()+"");
		      	  	obj.put("voltage",battery.getVoltage()+"");
		      	  	obj.put("temp", battery.getTemp()+"");
		      	  	obj.put("avail_mem", phoneState.getAvailableMemory());
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	      	    Map<String, String> params = new HashMap<String, String>();
	            params.put("code", code);
	            params.put("token", token);
	            params.put("info",obj.toString());
	            ServerUtilities.pushData(params, context);
	            
	        }else if(code.equals(CommonUtilities.OPERATION_DEVICE_LOCATION)){
	        	
	        	LocationServices ls = new LocationServices(context);
	        	Log.v("Latitude",ls.getLatitude());
	        	 JSONObject obj=new JSONObject();
	       	  	try {
	 				obj.put("latitude",ls.getLatitude());
	 	      	  	obj.put("longitude",ls.getLongitude());
	 	      	  	
	 			} catch (JSONException e1) {
	 				// TODO Auto-generated catch block
	 				e1.printStackTrace();
	 			}
	       	  	Map<String, String> params = new HashMap<String, String>();
	            params.put("code",CommonUtilities.OPERATION_DEVICE_LOCATION);
	            params.put("token",token);
	            params.put("info",obj.toString());
	            ServerUtilities.pushData(params, context); 
	            
	        }else if(code.equals(CommonUtilities.OPERATION_GET_APPLICATION_LIST)){
	        	  
	        	String apps[] = appList.getApplicationListasArray();
	        	JSONArray jsonArray = new JSONArray();
	        	for(int i=0;i<apps.length;i++){
	        		jsonArray.add(apps[i]);
	        	}
	        	JSONObject appsObj = new JSONObject();
	        	try {
					appsObj.put("apps", jsonArray);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        	
	            Map<String, String> params = new HashMap<String, String>();
	                
	            params.put("code",CommonUtilities.OPERATION_GET_APPLICATION_LIST);
	            params.put("token", token);  
	            params.put("info",jsonArray.toString());
	            ServerUtilities.pushData(params, context); 
	            
	        }else if(code.equals(CommonUtilities.OPERATION_LOCK_DEVICE)){
	        	
	        	Toast.makeText(this, "Locking device...", Toast.LENGTH_LONG).show();
				Log.d(TAG, "Locking device now");
				Map<String, String> params = new HashMap<String, String>();
	            params.put("code", code);
	            params.put("token", token);
	            ServerUtilities.pushData(params, context);
				devicePolicyManager.lockNow();
				
	        }else if(code.equals(CommonUtilities.OPERATION_WIPE_DATA)){
	
	        	Toast.makeText(this, "Locking device...", Toast.LENGTH_LONG).show();
	        	Log.d(TAG,"RESETing device now - all user data will be ERASED to factory settings");
	        	Map<String, String> params = new HashMap<String, String>();
	            params.put("code", code);
	            params.put("token", token);
	            ServerUtilities.pushData(params, context);
	        	devicePolicyManager.wipeData(ACTIVATION_REQUEST);
	        	
	        }else if(code.equals(CommonUtilities.OPERATION_CHANGE_LOCK_CODE)){
	
	        	ComponentName demoDeviceAdmin = new ComponentName(this, DemoDeviceAdminReceiver.class);
	        	devicePolicyManager.setPasswordMinimumLength(demoDeviceAdmin, 7);
	        	String pass = intent.getStringExtra("password");
	        	Map<String, String> params = new HashMap<String, String>();
	            params.put("code", code);
	            params.put("token", token);
	            ServerUtilities.pushData(params, context);
	        	devicePolicyManager.resetPassword(pass, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
	        	
	        }else if(code.equals(CommonUtilities.OPERATION_NOTIFICATION)){
	        	
	        	notification = intent.getStringExtra("notification");
	        	Log.v("Notification",notification);
	        	Map<String, String> params = new HashMap<String, String>();
	            params.put("code", code);
	            params.put("token", token);
	            ServerUtilities.pushData(params, context);
	        	generateNotification(this,notification);
	        	
	        }else if(code.equals(CommonUtilities.OPERATION_WIFI)){
	        	
	        	data = intent.getStringExtra("data");
	        	JSONParser jp = new JSONParser();
	        	try {
					JSONObject jobj = (JSONObject)jp.parse(data);
					ssid = (String)jobj.get("ssid");
					password = (String)jobj.get("password");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	Map<String, String> params = new HashMap<String, String>();
	            params.put("code", code);
	            params.put("token", token);
	            ServerUtilities.pushData(params, context);
	        	setWifi(ssid,password);
	        	
	        }else if(code.equals(CommonUtilities.OPERATION_DISABLE_CAMERA)){
	        	
	        	boolean camFunc = intent.getBooleanExtra("function", true);
	        	ComponentName cameraAdmin = new ComponentName(this, DemoDeviceAdminReceiver.class);
	        	Map<String, String> params = new HashMap<String, String>();
	            params.put("code", code);
	            params.put("token", token);
	            ServerUtilities.pushData(params, context);
	        	devicePolicyManager.setCameraDisabled(cameraAdmin, camFunc);
	        	
	        }else if(code.equals(CommonUtilities.OPERATION_INSTALL_APPLICATION)){
	        	
	        	String appUrl = intent.getStringExtra("url");
	        	Log.v("App URL : ",appUrl);
	        	Map<String, String> params = new HashMap<String, String>();
	            params.put("code", code);
	            params.put("token", token);
	            ServerUtilities.pushData(params, context);
	        	appList.installApp(appUrl);
	        	
	        }else if(code.equals(CommonUtilities.OPERATION_UNINSTALL_APPLICATION)){
	        	
	        	String packageName = intent.getStringExtra("package");
	        	Log.v("Package Name : ",packageName);
	        	Map<String, String> params = new HashMap<String, String>();
	            params.put("code", code);
	            params.put("token", token);
	            ServerUtilities.pushData(params, context);
	        	appList.unInstallApplication(packageName);
	        	
	        }*/
        
        //displayMessage(context, message);
        // notifies user
        //generateNotification(context, message);           	
       }
	    public void  setWifi(String SSID, String password){
	       
	       
	        WifiConfiguration wc=new WifiConfiguration();
	       
	        wc.SSID = "\"{SSID}\"".replace("{SSID}", SSID);
	        wc.preSharedKey = "\"{PRESHAREDKEY}\"".replace("{PRESHAREDKEY}",password)  ;
	   
	        wc.status = WifiConfiguration.Status.ENABLED;
	        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	
	        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	         
	        int netId=wifi.addNetwork(wc);
	        wifi.enableNetwork(netId, true);     
	    }

        
    

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
      //  displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
      //  displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
     //   displayMessage(context, getString(R.string.gcm_recoverable_error,errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, NotifyActivity.class);
        notificationIntent.putExtra("notification", message);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }
}
