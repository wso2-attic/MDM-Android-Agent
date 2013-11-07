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
package com.wso2mobile.mdm;

import java.util.HashMap;
import java.util.Map;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.google.android.gcm.GCMRegistrar;
import com.wso2mobile.mdm.services.WSO2MobileDeviceAdminReceiver;
import com.wso2mobile.mdm.utils.ServerUtilities;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.KeyEvent;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AlreadyRegisteredActivity extends SherlockActivity {
	AsyncTask<Void, Void, Void> mRegisterTask;
	static final int ACTIVATION_REQUEST = 47; // identifies our request id
	DevicePolicyManager devicePolicyManager;
	ComponentName demoDeviceAdmin;
	String regId="";
	private Button btnUnregister;
	//private ImageView optionBtn;
	private final int TAG_BTN_UNREGISTER = 0;
	private final int TAG_BTN_OPTIONS = 1;
	ActionBar actionbar;
	boolean unregState=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_ready_registered);
		 getSupportActionBar().setDisplayShowCustomEnabled(true);
		 getSupportActionBar().setCustomView(R.layout.custom_sherlock_bar);
		 View homeIcon = findViewById(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.id.home : R.id.abs__home);
		 ((View) homeIcon.getParent()).setVisibility(View.GONE);
		 
		 devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		 demoDeviceAdmin = new ComponentName(this, WSO2MobileDeviceAdminReceiver.class);
		 
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if(extras.containsKey("regid")){
				regId = extras.getString("regid");
			}
		}
		if(regId == null || regId.equals("")){
			regId = GCMRegistrar.getRegistrationId(this);
		}
		btnUnregister = (Button)findViewById(R.id.btnUnreg);
		btnUnregister.setTag(TAG_BTN_UNREGISTER);
		btnUnregister.setOnClickListener(onClickListener_BUTTON_CLICKED);
		
		//optionBtn = (ImageView) findViewById(R.id.option_button);	
		//optionBtn.setTag(TAG_BTN_OPTIONS);
		//optionBtn.setOnClickListener(onClickListener_BUTTON_CLICKED);
	}
	
	OnClickListener onClickListener_BUTTON_CLICKED = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub

			int iTag = (Integer) view.getTag();

			switch (iTag) {

			case TAG_BTN_UNREGISTER:
				startRegistration();
				break;

			case TAG_BTN_OPTIONS:
				//startOptionActivity();
				break;

			default:
				break;
			}

		}
	};
	
	public void startRegistration(){
		final Context context = AlreadyRegisteredActivity.this;
		mRegisterTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
            	 Map<String, String> paramss = new HashMap<String, String>();
                 paramss.put("regid", regId);
            	//ServerUtilities.sendToServer(context, "/UNRegister", paramss);
                 unregState=ServerUtilities.unregister(regId, context);
                return null;
            }
            
            ProgressDialog progressDialog;
            //declare other objects as per your need
            @Override
            protected void onPreExecute()
            {
                progressDialog= ProgressDialog.show(AlreadyRegisteredActivity.this, "Unregistering Device","Please wait", true);

                //do initialization of required objects objects here                
            };    


            @Override
            protected void onPostExecute(Void result) {
            	if(unregState){
	            	devicePolicyManager.removeActiveAdmin(demoDeviceAdmin);
	            	Intent intent = new Intent(AlreadyRegisteredActivity.this,EntryActivity.class);
	            	intent.putExtra("regid", regId);
	            	startActivity(intent);
	            	finish();
            	}else{
            		Intent intent = new Intent(AlreadyRegisteredActivity.this,AuthenticationErrorActivity.class);
	            	intent.putExtra("regid", regId);
	            	intent.putExtra("from_activity_name", AlreadyRegisteredActivity.class.getSimpleName());
	            	startActivity(intent);
            	}
                mRegisterTask = null;
                progressDialog.dismiss();
            }

        };
        mRegisterTask.execute(null, null, null);
        
        try {
			SharedPreferences mainPref = context.getSharedPreferences("com.mdm",
					Context.MODE_PRIVATE);
			Editor editor = mainPref.edit();
			editor.putString("policy", "");
			editor.commit();
			
			SharedPreferences mainPref2 = AlreadyRegisteredActivity.this.getSharedPreferences("com.mdm",
					Context.MODE_PRIVATE);
			Editor editor2 = mainPref.edit();
			editor.putString("isAgreed", "0");
			editor.commit();
			
			editor.putString("registered","0");
			editor.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startOptionActivity(){
		Intent intent = new Intent(AlreadyRegisteredActivity.this,AgentSettingsActivity.class);
		intent.putExtra("from_activity_name", AlreadyRegisteredActivity.class.getSimpleName());
		intent.putExtra("regid", regId);
		startActivity(intent);
	}
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.all_ready_registered, menu);
		return true;
		MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.options_menu, menu);
        return true;
	}*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.sherlock_menu, menu);
        return true;
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.operation_setting:
    		Intent intentOP = new Intent(AlreadyRegisteredActivity.this,AvailableOperationsActivity.class);
    		intentOP.putExtra("from_activity_name", AlreadyRegisteredActivity.class.getSimpleName());
    		intentOP.putExtra("regid", regId);
			startActivity(intentOP);
    		return true;
    	case R.id.info_setting:
    		Intent intentIN = new Intent(AlreadyRegisteredActivity.this,DisplayDeviceInfoActivity.class);
    		intentIN.putExtra("from_activity_name", AlreadyRegisteredActivity.class.getSimpleName());
    		intentIN.putExtra("regid", regId);
			startActivity(intentIN);
			return true;
    	case R.id.pin_setting:
    		Intent intentPIN = new Intent(AlreadyRegisteredActivity.this,PinCodeActivity.class);
    		intentPIN.putExtra("from_activity_name", AlreadyRegisteredActivity.class.getSimpleName());
			intentPIN.putExtra("regid", regId);
			startActivity(intentPIN);
			return true;
    	case R.id.ip_setting:
    		SharedPreferences mainPref = AlreadyRegisteredActivity.this.getSharedPreferences("com.mdm",
					Context.MODE_PRIVATE);
			Editor editor = mainPref.edit();
			editor.putString("ip", "");
			editor.commit();
			
    		Intent intentIP = new Intent(AlreadyRegisteredActivity.this,SettingsActivity.class);
    		intentIP.putExtra("from_activity_name", AlreadyRegisteredActivity.class.getSimpleName());
    		intentIP.putExtra("regid", regId);
			startActivity(intentIP);
			return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    } 
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	//finish();
	    	Intent i = new Intent();
	    	i.setAction(Intent.ACTION_MAIN);
	    	i.addCategory(Intent.CATEGORY_HOME);
	    	this.startActivity(i);
	        return true;
	    }
	    else if (keyCode == KeyEvent.KEYCODE_HOME) {
	    	Intent i = new Intent();
	    	i.setAction(Intent.ACTION_MAIN);
	    	i.addCategory(Intent.CATEGORY_HOME);
	    	this.startActivity(i);
	    	//finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	 /*public boolean onOptionsItemSelected(MenuItem item) {
	    	switch (item.getItemId()) {
	    	case R.id.info:
	    		Intent intent = new Intent(AllReadyRegistered.this,AgentSettingsActivity.class);
	    		intent.putExtra("from_activity_name", AllReadyRegistered.class.getSimpleName());
	    		intent.putExtra("regid", regId);
	    		startActivity(intent);
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    	}
	    }   */

}
