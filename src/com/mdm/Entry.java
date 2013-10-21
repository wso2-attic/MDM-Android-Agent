
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
*/package com.mdm;

import static com.mdm.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.mdm.CommonUtilities.EXTRA_MESSAGE;
import static com.mdm.CommonUtilities.SENDER_ID;
import static com.mdm.CommonUtilities.SERVER_URL;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.google.android.gcm.GCMRegistrar;
import com.mdm.models.PInfo;

import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothClass.Device;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Entry extends Activity {
	TextView mDisplay;
	AsyncTask<Void, Void, Void> mRegisterTask;
	AsyncTask<Void, Void, String> mLicenseTask;
	DeviceInfo info = null;
	String regId = "";
	boolean accessFlag = true;
	TextView errorMessage;
	Context context;
	ProgressDialog progressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry);
		checkNotNull(SERVER_URL, "SERVER_URL");
        checkNotNull(SENDER_ID, "SENDER_ID");
        info = new DeviceInfo(Entry.this);
        
        context = Entry.this;
        
        /*TouchDownConfig touchdown = new TouchDownConfig(context);
        touchdown.register();
        touchdown.performConfiguration("sdsdsd", "sdsd", "sdsdsd", "sdsdsdsd", true);
		touchdown.performPolicySet();*/
        if((info.getSdkVersion() > android.os.Build.VERSION_CODES.FROYO) && !info.isRooted()){
        	accessFlag = true;
        }else{
        	accessFlag = true;
        }
		
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this);
    //    mDisplay = (TextView) findViewById(R.id.display);
        registerReceiver(mHandleMessageReceiver,
                new IntentFilter(DISPLAY_MESSAGE_ACTION));
        //ImageView optionBtn = (ImageView) findViewById(R.id.option_button);	
		errorMessage = (TextView) findViewById(R.id.textView1);
		errorMessage.setText(getString(R.string.device_not_compatible_error));
		if(!accessFlag){
			errorMessage.setVisibility(View.VISIBLE);
		}

		/*optionBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Entry.this,DisplayDeviceInfo.class);
				intent.putExtra("from_activity_name", Entry.class.getSimpleName());
				startActivity(intent);
			}
		});*/
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if(extras.containsKey("regid")){
				regId = extras.getString("regid");
			}
		}
		if(regId == null || regId.equals("")){
			regId = GCMRegistrar.getRegistrationId(this);
		}

        Log.v("REGIDDDDD",regId);
        if (regId.equals("") || regId == null) {
            GCMRegistrar.register(getApplicationContext(), SENDER_ID);
        } else {
            if (GCMRegistrar.isRegisteredOnServer(this)) {
            	Log.v("Check is Register Func","isRegisteredOnServer");
              //  mDisplay.append(getString(R.string.already_registered) + "\n");
            } else {
                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                      //  boolean registered = ServerUtilities.register(context, regId);
                    	boolean registered = true; 
                    	if (!registered) {
                            GCMRegistrar.unregister(context);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                    	Log.v("REG IDDDD",regId);
                        mRegisterTask = null;
                    }

                };
                if(accessFlag){
                	mRegisterTask.execute(null, null, null);
                }else{
                	//Toast.makeText(getApplicationContext(), getString(R.string.device_not_compatible_error), Toast.LENGTH_LONG).show();
                }
            }
        }
        final Context context = this;
        
        mLicenseTask = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
              //  boolean registered = ServerUtilities.register(context, regId);
            	String response="";
            	try{
            		response =ServerUtilities.getEULA(context);
            	}catch(Exception e){
            		e.printStackTrace();
            	}
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
            	Log.v("REG IDDDD",regId);
            	if(result != null){
            		SharedPreferences mainPref = Entry.this.getSharedPreferences("com.mdm",
    						Context.MODE_PRIVATE);
    				Editor editor = mainPref.edit();
    				editor.putString("eula", result);
    				editor.commit();
            	}
            	mLicenseTask = null;
            }

        };

        mLicenseTask.execute();

        
        
        mRegisterTask = new AsyncTask<Void, Void, Void>() {
        	boolean state = false;
            @Override
            protected Void doInBackground(Void... params) {
            	try{
            		state =ServerUtilities.isRegistered(regId, context);
            	}catch(Exception e){
            		e.printStackTrace();
            		//HandleNetworkError(e);
            		//Toast.makeText(getApplicationContext(), "No Connection", Toast.LENGTH_LONG).show();
            	}
                return null;
            }
            
            //declare other objects as per your need
            @Override
            protected void onPreExecute()
            {
                progressDialog= ProgressDialog.show(Entry.this, "Checking Registration Info","Please wait", true);
                progressDialog.setCancelable(true);
                progressDialog.setOnCancelListener(cancelListener);
                //do initialization of required objects objects here                
            };    
            
            OnCancelListener cancelListener=new OnCancelListener(){
                @Override
                public void onCancel(DialogInterface arg0){
                	showAlert("Could not connect to server please check your internet connection and try again", "Connection Error");
                }
            };

            @Override
            protected void onPostExecute(Void result) {
            	SharedPreferences mainPref = context.getSharedPreferences(
    					"com.mdm", Context.MODE_PRIVATE);
    			String success = mainPref.getString("registered", "");
    			if(success.trim().equals("1")){
    				state = true;
    			}
    			
            	if(accessFlag){
	            	if(state){
	        			Intent intent = new Intent(Entry.this,AllReadyRegistered.class);
	        			intent.putExtra("regid", regId);
	        			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        			startActivity(intent);
	        			finish();
	        		}else{
	        			Intent intent = new Intent(Entry.this,Authentication.class);
	        			intent.putExtra("regid", regId);
	        			Log.v("REGIDDDDD",regId);
	        			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        			startActivity(intent);
	        			finish();
	        		}
            	}
                mRegisterTask = null;
                
            }

        };
        if(accessFlag){
        	mRegisterTask.execute(null, null, null);
        }else{
        	//Toast.makeText(getApplicationContext(), getString(R.string.device_not_compatible_error), Toast.LENGTH_LONG).show();
        }
        
        
       
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.entry, menu);
		return true;
	}
	
	protected void onDestroy() {
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
        
        if(mLicenseTask!=null){
        	mLicenseTask.cancel(true);
        }
        
        if(progressDialog!=null && progressDialog.isShowing()){
        	progressDialog.dismiss();
        	progressDialog = null;
        }
        try{
        unregisterReceiver(mHandleMessageReceiver);
        GCMRegistrar.onDestroy(getApplicationContext());
        }catch(Exception ex){
        	ex.printStackTrace();
        }
        super.onDestroy();
    }

    private void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(
                    getString(R.string.error_config, name));
        }
    }
    
    void HandleNetworkError(Exception e) {
    	//Toast.makeText(context, "Connecting to server failed", Toast.LENGTH_LONG).show();
    	if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
    	Intent intentIP = new Intent(Entry.this,SettingsActivity.class);
		intentIP.putExtra("from_activity_name", Authentication.class.getSimpleName());
		intentIP.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intentIP);
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
        mRegisterTask = new AsyncTask<Void, Void, Void>() {
        	boolean state = false;
            @Override
            protected Void doInBackground(Void... params) {
            	try{
            		state =ServerUtilities.isRegistered(regId, context);
            	}catch(Exception e){
            		e.printStackTrace();
            		//HandleNetworkError(e);
            		//Toast.makeText(getApplicationContext(), "No Connection", Toast.LENGTH_LONG).show();
            	}
                return null;
            }
            
            
            //declare other objects as per your need
            @Override
            protected void onPreExecute()
            {
                progressDialog= ProgressDialog.show(Entry.this, "Checking Registration Info","Please wait", true);
                progressDialog.setCancelable(true);
                progressDialog.setOnCancelListener(cancelListener);
                //do initialization of required objects objects here                
            };     

            OnCancelListener cancelListener=new OnCancelListener(){
                @Override
                public void onCancel(DialogInterface arg0){
                	showAlert("Could not connect to server please check your internet connection and try again", "Connection Error");
                    //finish();
                }
            };
            @Override
            protected void onPostExecute(Void result) {
            	SharedPreferences mainPref = context.getSharedPreferences(
    					"com.mdm", Context.MODE_PRIVATE);
    			String success = mainPref.getString("registered", "");
    			if(success.trim().equals("1")){
    				state = true;
    			}
    			
            	if(accessFlag){
	            	if(state){
	        			Intent intent = new Intent(Entry.this,AllReadyRegistered.class);
	        			intent.putExtra("regid", regId);
	        			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        			startActivity(intent);
	        			finish();
	        		}else{
	        			Intent intent = new Intent(Entry.this,Authentication.class);
	        			intent.putExtra("regid", regId);
	        			Log.v("REGIDDDDD",regId);
	        			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        			startActivity(intent);
	        			finish();
	        		}
            	}
                mRegisterTask = null;
                
            }

        };
        if(accessFlag){
        	mRegisterTask.execute(null, null, null);
        	if(progressDialog!=null && progressDialog.isShowing()){
            	progressDialog.dismiss();
            	progressDialog = null;
            }
        }else{
        	//Toast.makeText(getApplicationContext(), getString(R.string.device_not_compatible_error), Toast.LENGTH_LONG).show();
        }
    	super.onResume();
    }
    
    public void showAlert(String message, String title){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	finish();
                dialog.cancel();
            }
        });
        /*builder1.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });*/

        AlertDialog alert = builder.create();
        alert.show();
	}


    private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            mDisplay.append(newMessage + "\n");
        }
    };

}
