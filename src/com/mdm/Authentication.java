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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import com.actionbarsherlock.view.Menu;

public class Authentication extends SherlockActivity {
	AsyncTask<Void, Void, Void> mRegisterTask ;
	String regId = "";
	public static final String MDM_PREFERENCES_LOGIN = "Login";
	Button authenticate;
	EditText username;
	EditText password;
	Activity activity;
	Context context;
	AsyncTask<Void, Void, String> mLicenseTask;
	private final int TAG_BTN_AUTHENTICATE = 0;
	private final int TAG_BTN_OPTIONS = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setCustomView(R.layout.custom_sherlock_bar);
		View homeIcon = findViewById(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.id.home : R.id.abs__home);
		 ((View) homeIcon.getParent()).setVisibility(View.GONE);
		 
		this.activity = Authentication.this;
		this.context = Authentication.this;
		username = (EditText)findViewById(R.id.editText1);
		password = (EditText) findViewById(R.id.editText2);
		username.setFocusable(true);
		username.requestFocus();
		Log.v("check first username",username.getText().toString());
		Log.v("check first password",password.getText().toString());
		authenticate = (Button)findViewById(R.id.btnRegister);
		authenticate.setEnabled(false);
		authenticate.setTag(TAG_BTN_AUTHENTICATE);
		authenticate.setOnClickListener(onClickListener_BUTTON_CLICKED);
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
	            		SharedPreferences mainPref = Authentication.this.getSharedPreferences("com.mdm",
	    						Context.MODE_PRIVATE);
	    				Editor editor = mainPref.edit();
	    				editor.putString("eula", result);
	    				editor.commit();
	            	}
	            	mLicenseTask = null;
	            }

	        };
	       
	       mLicenseTask.execute();
	 
		
		
		SharedPreferences mainPref = context.getSharedPreferences(
				"com.mdm", Context.MODE_PRIVATE);
		String isAgreed = mainPref.getString("isAgreed", "");
		String eula = mainPref.getString("eula", "");
		
		if(!isAgreed.equals("1")){
			if(eula != null && eula !=""){
				showAlert(eula, CommonUtilities.EULA_TITLE);
			}else{
				showAlert(CommonUtilities.EULA_TEXT, CommonUtilities.EULA_TITLE);
			}
		}
		DeviceInfo deviceInfo = new DeviceInfo(Authentication.this);
		
		/*optionBtn = (ImageView) findViewById(R.id.option_button);	
		optionBtn.setTag(TAG_BTN_OPTIONS);
		optionBtn.setOnClickListener(onClickListener_BUTTON_CLICKED);*/
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if(extras.containsKey("regid")){
				regId = extras.getString("regid");
			}
		}
		if(regId == null || regId.equals("")){
			regId = GCMRegistrar.getRegistrationId(this);
		}
		
		username.addTextChangedListener(new TextWatcher() {
		      @Override
		      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		      }

		      @Override
		      public void onTextChanged(CharSequence s, int start, int before, int count) {
		    	  enableSubmitIfReady();
		      }

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				 enableSubmitIfReady();
			}
		    });
		
		password.addTextChangedListener(new TextWatcher() {
		      @Override
		      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		      }

		      @Override
		      public void onTextChanged(CharSequence s, int start, int before, int count) {
		    	  enableSubmitIfReady();
		      }

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				 enableSubmitIfReady();
			}
		    });
		
	
    }
	
	OnClickListener onClickListener_BUTTON_CLICKED = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub

			int iTag = (Integer) view.getTag();

			switch (iTag) {

			case TAG_BTN_AUTHENTICATE:
				startAuthentication();
				break;

			case TAG_BTN_OPTIONS:
				//startOptionActivity();
				break;
			default:
				break;
			}

		}
	};
	
	public void showAlert(String message, String title){
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.custom_terms_popup);
		dialog.setTitle(CommonUtilities.EULA_TITLE);
		dialog.setCancelable(false);
		//TextView text = (TextView) dialog.findViewById(R.id.text);
		WebView web = (WebView)dialog.findViewById(R.id.webview);
		String html = "<html><body>"+message+"</body></html>";
		String mime = "text/html";
		String encoding = "utf-8";
		web.getSettings().setJavaScriptEnabled(true);
		web.loadDataWithBaseURL(null, html, mime, encoding, null);
		//text.setText(message+ "/n/n" +message +"/n/n/n"+message);
		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		Button cancelButton = (Button) dialog.findViewById(R.id.dialogButtonCancel);

		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences mainPref = Authentication.this.getSharedPreferences("com.mdm",
						Context.MODE_PRIVATE);
				Editor editor = mainPref.edit();
				editor.putString("isAgreed", "1");
				editor.commit();
				dialog.dismiss();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences mainPref = Authentication.this.getSharedPreferences("com.mdm",
						Context.MODE_PRIVATE);
				Editor editor = mainPref.edit();
				editor.putString("isAgreed", "0");
				editor.commit();
				finish();
				/*
				Intent intentIP = new Intent(Authentication.this,SettingsActivity.class);
	    		intentIP.putExtra("from_activity_name", Authentication.class.getSimpleName());
	    		intentIP.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intentIP);
				dialog.dismiss();*/
			}
		});
		
		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

		    @Override
		    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		        if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
		            return true; 
		        }else if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
		        	 return true; 
		        }
		        return false; 
		    }
		});

		dialog.show();
	}
	
	public void startAuthentication(){
		final Context context = Authentication.this;
        mRegisterTask = new AsyncTask<Void, Void, Void>() {
        	boolean state=false;
            @Override
            protected Void doInBackground(Void... params) {
            	state = ServerUtilities.isAuthenticate(username.getText().toString(), password.getText().toString(), Authentication.this);
                return null;
            }
            
            ProgressDialog progressDialog;
            //declare other objects as per your need
            @Override
            protected void onPreExecute()
            {
                progressDialog= ProgressDialog.show(Authentication.this, "Authenticating","Please wait", true);

                //do initialization of required objects objects here                
            };    

           
            protected void onPostExecute(Void result) {

				if(state){
					//String pin = null;
					/*SharedPreferences mainPref = context.getSharedPreferences(
							"com.mdm", Context.MODE_PRIVATE);
					String pinSaved = mainPref.getString("pin", "");*/
					//if(pinSaved!=null && !pinSaved.equals("")){
						Intent intent = new Intent(Authentication.this,PinCodeActivity.class);
						intent.putExtra("regid", regId);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra("email", username.getText().toString());
						startActivity(intent);
					/*}else{
						Intent intent = new Intent(Authentication.this, MainActivity.class);
						intent.putExtra("regid", regId);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra("email", username.getText().toString());
						startActivity(intent);
					}*/
				}else{
					Intent intent = new Intent(Authentication.this,AuthenticationError.class);
					intent.putExtra("from_activity_name", Authentication.class.getSimpleName());
					intent.putExtra("regid", regId);
					startActivity(intent);
				}
                mRegisterTask = null;
                progressDialog.dismiss();
            }
		
			/*protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return null;
			}*/

        };
        mRegisterTask.execute(null, null, null);
	}
	
	public void startOptionActivity(){
		Intent intent = new Intent(Authentication.this,DisplayDeviceInfo.class);
		intent.putExtra("from_activity_name", Authentication.class.getSimpleName());
		intent.putExtra("regid", regId);
		startActivity(intent);
	}
	
	public void enableSubmitIfReady() {

	    boolean isReady = false;
	    
	    if(username.getText().toString().length()>=3 && password.getText().toString().length()>=3){
	    	isReady = true;
	    }

	    if (isReady) {
	       authenticate.setEnabled(true);
	   } else {
		   authenticate.setEnabled(false);
	    }
	  }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getSupportMenuInflater().inflate(R.menu.auth_sherlock_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.ip_setting:
    		SharedPreferences mainPref = Authentication.this.getSharedPreferences("com.mdm",
					Context.MODE_PRIVATE);
			Editor editor = mainPref.edit();
			editor.putString("ip", "");
			editor.commit();
			
    		Intent intentIP = new Intent(Authentication.this,SettingsActivity.class);
    		intentIP.putExtra("from_activity_name", Authentication.class.getSimpleName());
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
	    	Intent i = new Intent();
	    	i.setAction(Intent.ACTION_MAIN);
	    	i.addCategory(Intent.CATEGORY_HOME);
	    	this.startActivity(i);
	        return true;
	    }
	    else if (keyCode == KeyEvent.KEYCODE_HOME) {
	    	/*Intent i = new Intent();
	    	i.setAction(Intent.ACTION_MAIN);
	    	i.addCategory(Intent.CATEGORY_HOME);
	    	this.startActivity(i);*/
	    	this.finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

}
