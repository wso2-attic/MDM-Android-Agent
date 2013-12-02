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
import com.wso2mobile.mdm.services.Operation;
import com.wso2mobile.mdm.services.WSO2MobileDeviceAdminReceiver;
import com.wso2mobile.mdm.utils.ServerUtilities;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AlreadyRegisteredActivity extends SherlockActivity {
	AsyncTask<Void, Void, Void> mRegisterTask;
	static final int ACTIVATION_REQUEST = 47; // identifies our request id
	DevicePolicyManager devicePolicyManager;
	ComponentName demoDeviceAdmin;
	String regId="";
	private Button btnUnregister;
	private TextView txtRegText;
	//private ImageView optionBtn;
	private final int TAG_BTN_UNREGISTER = 0;
	private final int TAG_BTN_OPTIONS = 1;
	private final int TAG_BTN_RE_REGISTER = 2;
	ActionBar actionbar;
	boolean unregState=false;
	boolean freshRegFlag = false;
	Operation operation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_already_registered);
		 getSupportActionBar().setDisplayShowCustomEnabled(true);
		 getSupportActionBar().setCustomView(R.layout.custom_sherlock_bar);
		 View homeIcon = findViewById(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.id.home : R.id.abs__home);
		 ((View) homeIcon.getParent()).setVisibility(View.GONE);
		 
		 devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		 demoDeviceAdmin = new ComponentName(this, WSO2MobileDeviceAdminReceiver.class);
		 operation = new Operation(AlreadyRegisteredActivity.this);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if(extras.containsKey(getResources().getString(R.string.intent_extra_regid))){
				regId = extras.getString(getResources().getString(R.string.intent_extra_regid));
			}
			
			if(extras.containsKey(getResources().getString(R.string.intent_extra_fresh_reg_flag))){
				freshRegFlag = extras.getBoolean(getResources().getString(R.string.intent_extra_fresh_reg_flag));
			}
			
		}
		if(regId == null || regId.equals("")){
			regId = GCMRegistrar.getRegistrationId(this);
		}
		
		if(freshRegFlag){
			try {
	            if (!devicePolicyManager.isAdminActive(demoDeviceAdmin)) {
	                    Intent intent1 = new Intent(
	                                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
	                    intent1.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
	                                    demoDeviceAdmin);
	                    intent1.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
	                                    getResources().getString(R.string.device_admin_enable_alert));
	                    startActivityForResult(intent1, ACTIVATION_REQUEST);
	            }
	            operation.executePolicy();
		    } catch (Exception ex) {
		            ex.printStackTrace();
		    }
		}
		txtRegText = (TextView)findViewById(R.id.txtRegText);
		
		btnUnregister = (Button)findViewById(R.id.btnUnreg);
		btnUnregister.setTag(TAG_BTN_UNREGISTER);
		btnUnregister.setOnClickListener(onClickListener_BUTTON_CLICKED);
		
		 try {
			 if(freshRegFlag){
	             SharedPreferences mainPref = AlreadyRegisteredActivity.this.getSharedPreferences(getResources().getString(R.string.shared_pref_package),
	                             Context.MODE_PRIVATE);
	             Editor editor = mainPref.edit();
	             editor.putString(getResources().getString(R.string.shared_pref_registered),"1");
	             editor.commit();
			 }
             
	     } catch (Exception e) {
	             // TODO Auto-generated catch block
	             e.printStackTrace();
	     }
		
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
				startUnRegistration();
				break;

			case TAG_BTN_OPTIONS:
				//startOptionActivity();
				break;
			case TAG_BTN_RE_REGISTER:
				Intent intent = new Intent(AlreadyRegisteredActivity.this,EntryActivity.class);
            	intent.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
            	startActivity(intent);
            	finish();
				break;

			default:
				break;
			}

		}
	};
	
	public void startUnRegistration(){
		final Context context = AlreadyRegisteredActivity.this;
		mRegisterTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
            	 Map<String, String> paramss = new HashMap<String, String>();
                 paramss.put(getResources().getString(R.string.intent_extra_regid), regId);
            	//ServerUtilities.sendToServer(context, "/UNRegister", paramss);
                 unregState=ServerUtilities.unregister(regId, context);
                return null;
            }
            
            ProgressDialog progressDialog;
            //declare other objects as per your need
            @Override
            protected void onPreExecute()
            {
                progressDialog= ProgressDialog.show(AlreadyRegisteredActivity.this, getResources().getString(R.string.dialog_message_unregistering),getResources().getString(R.string.dialog_message_please_wait), true);

                //do initialization of required objects objects here                
            };    


            @Override
            protected void onPostExecute(Void result) {
            	if(unregState){
	            	/*
	            	Intent intent = new Intent(AlreadyRegisteredActivity.this,EntryActivity.class);
	            	intent.putExtra("regid", regId);
	            	startActivity(intent);
	            	finish();*/
            		txtRegText.setText(R.string.register_text_view_text_unregister);
            		btnUnregister.setText(R.string.register_button_text);
            		btnUnregister.setTag(TAG_BTN_RE_REGISTER);
            		btnUnregister.setOnClickListener(onClickListener_BUTTON_CLICKED);
            		
            		devicePolicyManager.removeActiveAdmin(demoDeviceAdmin);
	            	try {
	        			SharedPreferences mainPref = context.getSharedPreferences(getResources().getString(R.string.shared_pref_package),
	        					Context.MODE_PRIVATE);
	        			Editor editor = mainPref.edit();
	        			editor.putString(getResources().getString(R.string.shared_pref_policy), "");
	        			editor.putString(getResources().getString(R.string.shared_pref_isagreed), "0");
	        			editor.putString(getResources().getString(R.string.shared_pref_registered),"0");	
	        			editor.putString(getResources().getString(R.string.shared_pref_ip),"");
	        			editor.commit();
	        		} catch (Exception e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}
            	}else{
            		Intent intent = new Intent(AlreadyRegisteredActivity.this,AuthenticationErrorActivity.class);
	            	intent.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
	            	intent.putExtra(getResources().getString(R.string.intent_extra_from_activity), AlreadyRegisteredActivity.class.getSimpleName());
	            	startActivity(intent);
            	}
                mRegisterTask = null;
                progressDialog.dismiss();
            }

        };
        mRegisterTask.execute(null, null, null);

	}

	public void startOptionActivity(){
		Intent intent = new Intent(AlreadyRegisteredActivity.this,AgentSettingsActivity.class);
		intent.putExtra(getResources().getString(R.string.intent_extra_from_activity), AlreadyRegisteredActivity.class.getSimpleName());
		intent.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
		startActivity(intent);
	}
	
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
    		intentOP.putExtra(getResources().getString(R.string.intent_extra_from_activity), AlreadyRegisteredActivity.class.getSimpleName());
    		intentOP.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
			startActivity(intentOP);
    		return true;
    	case R.id.info_setting:
    		Intent intentIN = new Intent(AlreadyRegisteredActivity.this,DisplayDeviceInfoActivity.class);
    		intentIN.putExtra(getResources().getString(R.string.intent_extra_from_activity), AlreadyRegisteredActivity.class.getSimpleName());
    		intentIN.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
			startActivity(intentIN);
			return true;
    	case R.id.pin_setting:
    		Intent intentPIN = new Intent(AlreadyRegisteredActivity.this,PinCodeActivity.class);
    		intentPIN.putExtra(getResources().getString(R.string.intent_extra_from_activity), AlreadyRegisteredActivity.class.getSimpleName());
			intentPIN.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
			startActivity(intentPIN);
			return true;
    	case R.id.ip_setting:
    		SharedPreferences mainPref = AlreadyRegisteredActivity.this.getSharedPreferences("com.mdm",
					Context.MODE_PRIVATE);
			Editor editor = mainPref.edit();
			editor.putString(getResources().getString(R.string.shared_pref_ip), "");
			editor.commit();
			
    		Intent intentIP = new Intent(AlreadyRegisteredActivity.this,SettingsActivity.class);
    		intentIP.putExtra(getResources().getString(R.string.intent_extra_from_activity), AlreadyRegisteredActivity.class.getSimpleName());
    		intentIP.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
			startActivity(intentIP);
			return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    } 
	
	@Override
    public void onBackPressed() {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            this.startActivity(i);
            //finish();
            super.onBackPressed();
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
