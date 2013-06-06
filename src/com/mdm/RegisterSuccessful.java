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

import java.util.HashMap;
import java.util.Map;

import com.google.android.gcm.GCMRegistrar;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class RegisterSuccessful extends Activity {
	AsyncTask<Void, Void, Void> mRegisterTask;
	static final int ACTIVATION_REQUEST = 47; // identifies our request id
	DevicePolicyManager devicePolicyManager;
	ComponentName demoDeviceAdmin;
	String regId="";
	private Button btnUnregister;
	private ImageView optionBtn;
	private final int TAG_BTN_UNREGISTER = 0;
	private final int TAG_BTN_OPTIONS = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_successful);
		
		devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		demoDeviceAdmin = new ComponentName(this, DemoDeviceAdminReceiver.class);
		
		//Starting device admin
    	try{
	    		if(!devicePolicyManager.isAdminActive(demoDeviceAdmin)){
		        	Intent intent1 = new Intent(
		    				DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		    		intent1.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
		    				demoDeviceAdmin);
		    		intent1.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
		    				"This will enable device administration");
		    		startActivityForResult(intent1, ACTIVATION_REQUEST);
	    		}
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}
    	
    	
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if(extras.containsKey("regid")){
				regId = extras.getString("regid");
			}
		}
		if(regId == null || regId.equals("")){
			regId = GCMRegistrar.getRegistrationId(this);
		}
		btnUnregister = (Button)findViewById(R.id.btnUnregister);
		btnUnregister.setTag(TAG_BTN_UNREGISTER);
		btnUnregister.setOnClickListener(onClickListener_BUTTON_CLICKED);
		
		optionBtn = (ImageView) findViewById(R.id.option_button);	
		optionBtn.setTag(TAG_BTN_OPTIONS);
		optionBtn.setOnClickListener(onClickListener_BUTTON_CLICKED);
		
		
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
				startOptionActivity();
				break;

			default:
				break;
			}

		}
	};
	
	public void startOptionActivity(){
		Intent intent = new Intent(RegisterSuccessful.this,AgentSettingsActivity.class);
		intent.putExtra("from_activity_name", RegisterSuccessful.class.getSimpleName());
		intent.putExtra("regid", regId);
		startActivity(intent);
	}
	
	public void startUnRegistration(){
		final Context context = RegisterSuccessful.this;
		mRegisterTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
            	 Map<String, String> paramss = new HashMap<String, String>();
                 paramss.put("regid", regId);
            	ServerUtilities.unregister(regId,context);
                return null;
            }
            
            ProgressDialog progressDialog;
            //declare other objects as per your need
            @Override
            protected void onPreExecute()
            {
                progressDialog= ProgressDialog.show(RegisterSuccessful.this, "Unregistering Device","Please wait", true);

                //do initialization of required objects objects here                
            };  

            @Override
            protected void onPostExecute(Void result) {
            	devicePolicyManager.removeActiveAdmin(demoDeviceAdmin);
            	Intent intent = new Intent(RegisterSuccessful.this,Entry.class);
            	startActivity(intent);
            	finish();
                mRegisterTask = null;
                progressDialog.dismiss();
            }

        };
        mRegisterTask.execute(null, null, null);
	}
	
	@Override
	 public void onBackPressed() {
		Intent i = new Intent();
    	i.setAction(Intent.ACTION_MAIN);
    	i.addCategory(Intent.CATEGORY_HOME);
    	this.startActivity(i);
        super.onBackPressed();
    }
	
	  @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
		    if (keyCode == KeyEvent.KEYCODE_BACK) {
		    	Intent i = new Intent();
		    	i.setAction(Intent.ACTION_MAIN);
		    	i.addCategory(Intent.CATEGORY_HOME);
		    	this.startActivity(i);
		    	finish();
		        return true;
		    }
		    else if (keyCode == KeyEvent.KEYCODE_HOME) {
		    	/*Intent i = new Intent();
		    	i.setAction(Intent.ACTION_MAIN);
		    	i.addCategory(Intent.CATEGORY_HOME);
		    	this.startActivity(i);*/
		    	finish();
		        return true;
		    }
		    return super.onKeyDown(keyCode, event);
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*getMenuInflater().inflate(R.menu.register_successful, menu);
		return true;*/
		MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.options_menu, menu);
        return true;
	}
	 public boolean onOptionsItemSelected(MenuItem item) {
	    	switch (item.getItemId()) {
	    	case R.id.info:
	    		Intent intent = new Intent(RegisterSuccessful.this,AgentSettingsActivity.class);
	    		intent.putExtra("from_activity_name", RegisterSuccessful.class.getSimpleName());
	    		intent.putExtra("regid", regId);
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    	}
	    }   

}
