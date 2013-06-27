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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.mdm.models.PInfo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class Operation {

	Context context = null;
	DevicePolicyManager devicePolicyManager;
	ApplicationManager appList;
	DeviceInfo deviceInfo;
	TrackCallSMS conversations;
	PhoneState deviceState;
	String code = null;
	String token = null;
	String data = "";
	GPSTracker gps;
	String recepient = "";
	int mode = 1;
	private static final String TAG = "Operation Handler";
	static final int ACTIVATION_REQUEST = 47;
	Intent intent;
	Map<String, String> params = new HashMap<String, String>();
	SmsManager smsManager;

	public Operation(Context context, int mode, Intent intent) {
		this.context = context;
		this.intent = intent;
		this.mode = mode;
		code = intent.getStringExtra("message").trim();
		Log.v("Code", code);

		token = intent.getStringExtra("token").trim();
		Log.v("Token", token);

		if (intent.getStringExtra("data") != null) {
			data = intent.getStringExtra("data");
		}
		doTask();

	}

	public Operation(Context context, int mode, Map<String, String> params,
			String recepient) {
		this.context = context;
		this.mode = mode;
		this.params = params;
		this.recepient = recepient;

		if (params.get("code") != null) {
			code = params.get("code");
			Log.v("PRINTING CODE : ", code);
		}

		if (params.get("data") != null) {
			data = params.get("data");
		}
		doTask();

	}

	@SuppressWarnings("static-access")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void doTask() {

		String notification = "";
		String ssid = "";
		String password = "";

		devicePolicyManager = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		appList = new ApplicationManager(context);
		deviceInfo = new DeviceInfo(context);
		gps = new GPSTracker(context);
		smsManager = SmsManager.getDefault();
		conversations = new TrackCallSMS(context);
		deviceState = new PhoneState(context);

		if (code.equals(CommonUtilities.OPERATION_DEVICE_INFO)) {

			PhoneState phoneState = new PhoneState(context);
			JSONObject obj = new JSONObject();
			JSONObject battery_obj = new JSONObject();
			JSONObject inmemory_obj = new JSONObject();
			JSONObject exmemory_obj = new JSONObject();
			JSONObject location_obj = new JSONObject();
			double latitude = 0;
			double longitude = 0;
			try {
				latitude = gps.getLatitude();
				longitude = gps.getLongitude();
				// obj.put("ip",phoneState.getIpAddress());
				// obj.put("battery_scale",battery.getScale()+"");
				battery_obj.put("level", phoneState.getBatteryLevel());
				// obj.put("battery_voltage",battery.getVoltage()+"");
				// obj.put("battery_temp", battery.getTemp()+"");
				inmemory_obj.put("total",
						deviceInfo.getTotalInternalMemorySize());
				inmemory_obj.put("available",
						deviceInfo.getAvailableInternalMemorySize());
				exmemory_obj.put("total",
						deviceInfo.getTotalExternalMemorySize());
				exmemory_obj.put("available",
						deviceInfo.getAvailableExternalMemorySize());
				location_obj.put("latitude", latitude);
				location_obj.put("longitude", longitude);

				obj.put("battery", battery_obj);
				obj.put("internal_memory", inmemory_obj);
				obj.put("external_memory", exmemory_obj);
				obj.put("location_obj", location_obj);
				obj.put("operator", deviceInfo.getNetworkOperatorName());

				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);
				params.put("status", "200");
				params.put("data", obj.toString());

				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager
							.sendTextMessage(
									recepient,
									null,
									"Battery Level : "
											+ phoneState.getBatteryLevel()
											+ ", Total Memory : "
											+ deviceInfo.formatSize(deviceInfo
													.getTotalInternalMemorySize()
													+ deviceInfo
															.getTotalExternalMemorySize())
											+ ", Available Memory : "
											+ deviceInfo.formatSize(deviceInfo
													.getAvailableInternalMemorySize()
													+ deviceInfo
															.getAvailableExternalMemorySize()),
									null, null);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_DEVICE_LOCATION)) {

			LocationServices ls = new LocationServices(context);
			Log.v("Latitude", ls.getLatitude());
			double latitude = 0;
			double longitude = 0;
			JSONObject obj = new JSONObject();
			try {
				latitude = gps.getLatitude();
				longitude = gps.getLongitude();
				obj.put("latitude", latitude);
				obj.put("longitude", longitude);
				/*
				 * obj.put("latitude",ls.getLatitude());
				 * obj.put("longitude",ls.getLongitude());
				 */

				Map<String, String> params = new HashMap<String, String>();
				params.put("code", CommonUtilities.OPERATION_DEVICE_LOCATION);
				params.put("msgID", token);
				params.put("status", "200");
				params.put("data", obj.toString());
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager
							.sendTextMessage(recepient, null, "Longitude : "
									+ longitude + ",Latitude : " + latitude,
									null, null);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_GET_APPLICATION_LIST)) {
			ArrayList<PInfo> apps = appList.getInstalledApps(false); /*
																	 * false =
																	 * no system
																	 * packages
																	 */
			// String apps[] = appList.getApplicationListasArray();
			JSONArray jsonArray = new JSONArray();
			int max = apps.size();
			if (max > 10) {
				max = 10;
			}
			String apz = "";
			Log.e("APP TOTAL : ", "" + max);
			for (int i = 0; i < max; i++) {
				JSONObject jsonObj = new JSONObject();
				try {
					jsonObj.put("name", apps.get(i).appname);
					jsonObj.put("package", apps.get(i).pname);
					jsonObj.put("icon", apps.get(i).icon);
					apz += apps.get(i).appname + " ,";
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				jsonArray.add(jsonObj);
			}
			/*
			 * for(int i=0;i<apps.length;i++){ jsonArray.add(apps[i]); }
			 */
			JSONObject appsObj = new JSONObject();
			try {
				appsObj.put("apps", jsonArray);

				Map<String, String> params = new HashMap<String, String>();

				params.put("code",
						CommonUtilities.OPERATION_GET_APPLICATION_LIST);
				params.put("msgID", token);
				params.put("status", "200");
				params.put("data", jsonArray.toString());
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager
							.sendTextMessage(recepient, null, apz, null, null);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_LOCK_DEVICE)) {

			// Toast.makeText(this, "Locking device...",
			// Toast.LENGTH_LONG).show();
			Log.d(TAG, "Locking device now");
			try {
				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);
				params.put("status", "200");
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							"Device Locked Successfully", null, null);
				}

				devicePolicyManager.lockNow();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_WIPE_DATA)) {

			// Toast.makeText(this, "Locking device...",
			// Toast.LENGTH_LONG).show();
			Log.d(TAG,
					"RESETing device now - all user data will be ERASED to factory settings");
			String pin = null;
			SharedPreferences mainPref = context.getSharedPreferences(
					"com.mdm", Context.MODE_PRIVATE);
			String pinSaved = mainPref.getString("pin", "");

			try {
				JSONObject jobj = new JSONObject(data);
				pin = (String) jobj.get("pin");
				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);

				if (pin.trim().equals(pinSaved.trim())) {
					params.put("status", "200");
				} else {
					params.put("status", "400");
				}

				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					if (pin.trim().equals(pinSaved.trim())) {
						smsManager.sendTextMessage(recepient, null,
								"Device Wiped Successfully", null, null);
					} else {
						smsManager.sendTextMessage(recepient, null,
								"Wrong PIN", null, null);
					}
				}
				if (pin.trim().equals(pinSaved.trim())) {
					Toast.makeText(context, "Device is being wiped",
							Toast.LENGTH_LONG).show();
					devicePolicyManager.wipeData(ACTIVATION_REQUEST);
				} else {
					Toast.makeText(context,
							"Device wipe failed due to wrong PIN",
							Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_CHANGE_LOCK_CODE)) {
			ComponentName demoDeviceAdmin = new ComponentName(context,
					DemoDeviceAdminReceiver.class);
			devicePolicyManager.setPasswordMinimumLength(demoDeviceAdmin, 3);
			String pass = "123";
			// data = intent.getStringExtra("data");
			JSONParser jp = new JSONParser();
			try {
				JSONObject jobj = new JSONObject(data);
				pass = (String) jobj.get("password");

				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);
				params.put("status", "200");
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							"Lock code changed Successfully", null, null);
				}

				devicePolicyManager.resetPassword(pass,
						DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
				devicePolicyManager.lockNow();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_NOTIFICATION)) {

			JSONParser jp = new JSONParser();
			try {
				JSONObject jobj = new JSONObject(data);
				if (jobj.get("notification").toString() != null
						|| jobj.get("notification").toString().equals("")) {
					notification = jobj.get("notification").toString();
				} else if (jobj.get("Notification").toString() != null
						|| jobj.get("Notification").toString().equals("")) {
					notification = jobj.get("Notification").toString();
				} else {
					notification = "";
				}

				Log.v("Notification", notification);
				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);
				params.put("status", "200");
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							"Notification Receieved Successfully", null, null);
				}
				generateNotification(context, notification);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_WIFI)) {

			// data = intent.getStringExtra("data");
			JSONParser jp = new JSONParser();
			try {
				JSONObject jobj = new JSONObject(data);
				ssid = (String) jobj.get("ssid");
				password = (String) jobj.get("password");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<String, String> params = new HashMap<String, String>();
			params.put("code", code);
			params.put("msgID", token);
			params.put("status", "200");
			if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
				ServerUtilities.pushData(params, context);
			} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
				smsManager.sendTextMessage(recepient, null,
						"WiFi Configured Successfully", null, null);
			}
			try {
				setWifi(ssid, password);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_DISABLE_CAMERA)) {

			boolean camFunc = true;
			// data = intent.getStringExtra("data");
			JSONParser jp = new JSONParser();
			try {
				JSONObject jobj = new JSONObject(data);
				if (jobj.get("function").toString().equals("Enable")) {
					camFunc = false;
				} else if (jobj.get("function").toString().equals("Disable")) {
					camFunc = true;
				} else {
					camFunc = Boolean.parseBoolean(jobj.get("function")
							.toString());
				}

				ComponentName cameraAdmin = new ComponentName(context,
						DemoDeviceAdminReceiver.class);
				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);
				params.put("status", "200");
				String cammode = "Disabled";
				if (camFunc) {
					cammode = "Disabled";
				} else {
					cammode = "Enabled";
				}
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null, "Camera "
							+ cammode + " Successfully", null, null);
				}

				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					devicePolicyManager.setCameraDisabled(cameraAdmin, camFunc);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_INSTALL_APPLICATION)) {

			String appUrl = "";
			// data = intent.getStringExtra("data");
			JSONParser jp = new JSONParser();
			try {
				JSONObject jobj = new JSONObject(data);
				appUrl = (String) jobj.get("url");

				Log.v("App URL : ", appUrl);
				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);
				params.put("status", "200");
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							"Application installed Successfully", null, null);
				}
				appList.installApp(appUrl);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_UNINSTALL_APPLICATION)) {

			String packageName = "";
			// data = intent.getStringExtra("data");
			JSONParser jp = new JSONParser();
			try {
				JSONObject jobj = new JSONObject(data);
				packageName = (String) jobj.get("package");

				Log.v("Package Name : ", packageName);
				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);
				params.put("status", "200");
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							"Application uninstalled Successfully", null, null);
				}
				appList.unInstallApplication(packageName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_ENCRYPT_STORAGE)) {
			boolean encryptFunc = true;
			String pass = "";
			// data = intent.getStringExtra("data");
			JSONParser jp = new JSONParser();
			try {
				JSONObject jobj = new JSONObject(data);
				//pass = (String)jobj.get("password");
				if (jobj.get("function").toString().equalsIgnoreCase("encrypt")) {
					encryptFunc = true;
				} else if (jobj.get("function").toString()
						.equalsIgnoreCase("decrypt")) {
					encryptFunc = false;
				} else {
					encryptFunc = Boolean.parseBoolean(jobj.get("function")
							.toString());
				}

				// ComponentName cameraAdmin = new ComponentName(this,
				// DemoDeviceAdminReceiver.class);
				ComponentName admin = new ComponentName(context,
						DemoDeviceAdminReceiver.class);
				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);
				if(devicePolicyManager.getStorageEncryptionStatus() != devicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED){
					params.put("status", "200");
				}else{
					params.put("status", "400");
				}
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							"Storage Encrypted Successfully", null, null);
				}
				if (encryptFunc
						&& devicePolicyManager.getStorageEncryptionStatus() != devicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED) {
					if (devicePolicyManager.getStorageEncryptionStatus() == devicePolicyManager.ENCRYPTION_STATUS_INACTIVE) {
						//devicePolicyManager.resetPassword(pass,
								//DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
						if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
							devicePolicyManager.setStorageEncryption(admin,
									encryptFunc);
							Intent intent = new Intent(DevicePolicyManager.ACTION_START_ENCRYPTION);
							((Activity) context).startActivityForResult(intent, 1);
						}
					}
				} else if (!encryptFunc
						&& devicePolicyManager.getStorageEncryptionStatus() != devicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED) {
					if (devicePolicyManager.getStorageEncryptionStatus() == devicePolicyManager.ENCRYPTION_STATUS_ACTIVE
							|| devicePolicyManager.getStorageEncryptionStatus() == devicePolicyManager.ENCRYPTION_STATUS_ACTIVATING) {
						if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
							devicePolicyManager.setStorageEncryption(admin,
									encryptFunc);
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (code.equals(CommonUtilities.OPERATION_MUTE)) {

			Log.d(TAG, "Muting Device");
			try {
				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);
				params.put("status", "200");
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							"Device Muted Successfully", null, null);
				}

				muteDevice();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (code.equals(CommonUtilities.OPERATION_TRACK_CALLS)) {
			try {
				Map<String, String> params = new HashMap<String, String>();

				params.put("code", CommonUtilities.OPERATION_TRACK_CALLS);
				params.put("msgID", token);
				params.put("status", "200");
				params.put("data", conversations.getCallDetails().toString());
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null, conversations
							.getCallDetails().toString(), null, null);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (code.equals(CommonUtilities.OPERATION_TRACK_SMS)) {
			int MESSAGE_TYPE_INBOX = 1;
			int MESSAGE_TYPE_SENT = 2;
			JSONObject smsObj = new JSONObject();

			try {
				smsObj.put("inbox", conversations.getSMS(MESSAGE_TYPE_INBOX));
				smsObj.put("sent", conversations.getSMS(MESSAGE_TYPE_SENT));

				Map<String, String> params = new HashMap<String, String>();

				params.put("code", CommonUtilities.OPERATION_TRACK_SMS);
				params.put("msgID", token);
				params.put("status", "200");
				params.put("data", smsObj.toString());
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							smsObj.toString(), null, null);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (code.equals(CommonUtilities.OPERATION_DATA_USAGE)) {
			JSONObject dataObj = new JSONObject();

			try {
				dataObj.put("upload", deviceState.getDataUploadUsage());
				dataObj.put("download", deviceState.getDataDownloadUsage());

				Map<String, String> params = new HashMap<String, String>();

				params.put("code", CommonUtilities.OPERATION_DATA_USAGE);
				params.put("msgID", token);
				params.put("status", "200");
				params.put("data", dataObj.toString());
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							dataObj.toString(), null, null);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (code.equals(CommonUtilities.OPERATION_STATUS)) {
			boolean encryptStatus = false;
			boolean passCodeStatus = false;
			try {
				if (devicePolicyManager.getStorageEncryptionStatus() != devicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED) {
					if (devicePolicyManager.getStorageEncryptionStatus() == devicePolicyManager.ENCRYPTION_STATUS_ACTIVE
							|| devicePolicyManager.getStorageEncryptionStatus() == devicePolicyManager.ENCRYPTION_STATUS_ACTIVATING) {
						encryptStatus = true;
					} else {
						encryptStatus = false;
					}
				}
				if (devicePolicyManager.isActivePasswordSufficient()) {
					passCodeStatus = true;
				} else {
					passCodeStatus = false;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				passCodeStatus = false;
			}
			JSONObject dataObj = new JSONObject();

			try {
				dataObj.put("encryption", encryptStatus);
				dataObj.put("passcode", passCodeStatus);

				Map<String, String> params = new HashMap<String, String>();

				params.put("code", code);
				params.put("msgID", token);
				params.put("status", "200");
				params.put("data", dataObj.toString());
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							dataObj.toString(), null, null);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		} else if (code.equals(CommonUtilities.OPERATION_WEBCLIP)) {
			String appUrl = "";
			String title = "";
			// data = intent.getStringExtra("data");
			JSONParser jp = new JSONParser();
			try {
				JSONObject jobj = new JSONObject(data);
				appUrl = (String) jobj.get("url");
				title = (String) jobj.get("title");
				Log.v("Web App URL : ", appUrl);
				Map<String, String> params = new HashMap<String, String>();
				params.put("code", code);
				params.put("msgID", token);
				params.put("status", "200");
				if (mode == CommonUtilities.MESSAGE_MODE_GCM) {
					ServerUtilities.pushData(params, context);
				} else if (mode == CommonUtilities.MESSAGE_MODE_SMS) {
					smsManager.sendTextMessage(recepient, null,
							"WebClip created Successfully", null, null);
				}
				appList.createWebAppBookmark(appUrl, title);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set WiFi
	 */
	public void setWifi(String SSID, String password) {

		WifiConfiguration wc = new WifiConfiguration();

		wc.SSID = "\"{SSID}\"".replace("{SSID}", SSID);
		wc.preSharedKey = "\"{PRESHAREDKEY}\"".replace("{PRESHAREDKEY}",
				password);

		wc.status = WifiConfiguration.Status.ENABLED;
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		int netId = wifi.addNetwork(wc);
		wifi.enableNetwork(netId, true);
	}

	/**
	 * Mute the device
	 */
	private void muteDevice() {
		Log.v("MUTING THE DEVICE : ", "MUTING");
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		Log.v("VOLUME : ",
				"" + audioManager.getStreamVolume(AudioManager.STREAM_RING));
		audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
		Log.v("VOLUME AFTER: ",
				"" + audioManager.getStreamVolume(AudioManager.STREAM_RING));

	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_stat_gcm;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);
		String title = context.getString(R.string.app_name);
		Intent notificationIntent = new Intent(context, NotifyActivity.class);
		notificationIntent.putExtra("notification", message);
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notificationManager.notify(0, notification);
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
}
