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


import com.wso2mobile.mdm.services.RegisterSuccessfulActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AgentSettingsActivity extends Activity {
	private String FROM_ACTIVITY = null;
	private String REG_ID = "";
	static final String[] OP_NAME = new String[] { "Operations", "Phone Info", "Change PIN code", "Change IP address"};
	private ArrayAdapter<String> listAdapter ; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agent_settings);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if(extras.containsKey("from_activity_name")){
				FROM_ACTIVITY = extras.getString("from_activity_name");
			}
			
			if(extras.containsKey("regid")){
				REG_ID = extras.getString("regid");
			}
		}
		
		listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow ,OP_NAME);
		 
		ListView listView = (ListView) findViewById(R.id.listview);  
		listView.setAdapter(listAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			    // When clicked, show a toast with the TextView text
				if(position == 0){
					Intent intent = new Intent(AgentSettingsActivity.this,AvailableOperationsActivity.class);
					intent.putExtra("from_activity_name", FROM_ACTIVITY);
					intent.putExtra("regid", REG_ID);
					startActivity(intent);
				}else if(position == 1){
					Intent intent = new Intent(AgentSettingsActivity.this,DisplayDeviceInfoActivity.class);
					intent.putExtra("from_activity_name", FROM_ACTIVITY);
					intent.putExtra("regid", REG_ID);
					startActivity(intent);
				}else if(position == 2){
					Intent intent = new Intent(AgentSettingsActivity.this,PinCodeActivity.class);
					intent.putExtra("from_activity_name", AgentSettingsActivity.class.getSimpleName());
					intent.putExtra("main_activity_name", FROM_ACTIVITY);
					intent.putExtra("regid", REG_ID);
					startActivity(intent);
				}else if(position == 3){
					Intent intent = new Intent(AgentSettingsActivity.this,SettingsActivity.class);
					intent.putExtra("from_activity_name", FROM_ACTIVITY);
					intent.putExtra("regid", REG_ID);
					startActivity(intent);
				}
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if(FROM_ACTIVITY != null && FROM_ACTIVITY.equals(EntryActivity.class.getSimpleName())){
	    		Intent intent = new Intent(AgentSettingsActivity.this,EntryActivity.class);
	    		startActivity(intent);
	    	}
	    	else if(FROM_ACTIVITY != null && FROM_ACTIVITY.equals(AuthenticationActivity.class.getSimpleName())){
	    		Intent intent = new Intent(AgentSettingsActivity.this,AuthenticationActivity.class);
	    		intent.putExtra("regid", REG_ID);
	    		startActivity(intent);
	    	}else if(FROM_ACTIVITY != null && FROM_ACTIVITY.equals(AlreadyRegisteredActivity.class.getSimpleName())){
	    		Intent intent = new Intent(AgentSettingsActivity.this,AlreadyRegisteredActivity.class);
	    		intent.putExtra("regid", REG_ID);
	    		startActivity(intent);
	    	}else if(FROM_ACTIVITY != null && FROM_ACTIVITY.equals(MainActivity.class.getSimpleName())){
	    		Intent intent = new Intent(AgentSettingsActivity.this,MainActivity.class);
	    		intent.putExtra("regid", REG_ID);
	    		startActivity(intent);
	    	}else if(FROM_ACTIVITY != null && FROM_ACTIVITY.equals(RegisterSuccessfulActivity.class.getSimpleName())){
	    		Intent intent = new Intent(AgentSettingsActivity.this,RegisterSuccessfulActivity.class);
	    		startActivity(intent);
	    	}else{
	    		finish();
	    	}
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
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.agent_settings, menu);
		return true;
	}

}
