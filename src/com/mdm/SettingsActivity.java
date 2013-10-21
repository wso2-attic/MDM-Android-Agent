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

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	TextView ip;
	Button optionBtn;
	private String FROM_ACTIVITY = null;
	private String REG_ID = "";
	Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		context = SettingsActivity.this;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if(extras.containsKey("from_activity_name")){
				FROM_ACTIVITY = extras.getString("from_activity_name");
			}
			
			if(extras.containsKey("regid")){
				REG_ID = extras.getString("regid");
			}
		}
		
		
		ip = (TextView)findViewById(R.id.editText1);
		SharedPreferences mainPref = context.getSharedPreferences(
				"com.mdm", Context.MODE_PRIVATE);
		String ipSaved = mainPref.getString("ip", "");
		
		if(ipSaved != null && ipSaved != ""){
			ip.setText(ipSaved);
			Intent intent = new Intent(SettingsActivity.this,Entry.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);	
		}else{
			ip.setText(CommonUtilities.SERVER_IP);
		}
		optionBtn = (Button) findViewById(R.id.button1);	
		
		optionBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!ip.getText().toString().trim().equals("")){
					SharedPreferences mainPref = SettingsActivity.this.getSharedPreferences("com.mdm",
							Context.MODE_PRIVATE);
					Editor editor = mainPref.edit();
					editor.putString("ip", ip.getText().toString().trim());
					editor.commit();
					
					CommonUtilities.setSERVER_URL(ip.getText().toString().trim());
					Intent intent = new Intent(SettingsActivity.this,Entry.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    			startActivity(intent);	
				}else{
					Toast.makeText(context, "Please enter Server Address, i.e : www.abc.com", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && FROM_ACTIVITY != null && FROM_ACTIVITY.equals(AllReadyRegistered.class.getSimpleName())) {
    		Intent intent = new Intent(SettingsActivity.this,AllReadyRegistered.class);
    		intent.putExtra("from_activity_name", SettingsActivity.class.getSimpleName());
    		intent.putExtra("regid", REG_ID);
    		startActivity(intent);
    		return true;
	    }else if (keyCode == KeyEvent.KEYCODE_BACK && FROM_ACTIVITY != null && FROM_ACTIVITY.equals(RegisterSuccessful.class.getSimpleName())) {
    		Intent intent = new Intent(SettingsActivity.this,RegisterSuccessful.class);
    		intent.putExtra("from_activity_name", SettingsActivity.class.getSimpleName());
    		intent.putExtra("regid", REG_ID);
    		startActivity(intent);
    		return true;
	    }else if (keyCode == KeyEvent.KEYCODE_BACK && FROM_ACTIVITY != null && FROM_ACTIVITY.equals(Authentication.class.getSimpleName())) {
    		Intent intent = new Intent(SettingsActivity.this,Authentication.class);
    		intent.putExtra("from_activity_name", SettingsActivity.class.getSimpleName());
    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		intent.putExtra("regid", REG_ID);
    		startActivity(intent);
    		return true;
	    }else if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	Intent i = new Intent();
	    	i.setAction(Intent.ACTION_MAIN);
	    	i.addCategory(Intent.CATEGORY_HOME);
	    	this.startActivity(i);
	    	this.finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
