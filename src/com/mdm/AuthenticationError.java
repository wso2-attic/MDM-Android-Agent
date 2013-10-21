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

import com.google.android.gcm.GCMRegistrar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AuthenticationError extends Activity {
    String regId = "";
    private Button btnTryAgain;
    private final int TAG_BTN_TRY_AGAIN = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication_error);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if(extras.containsKey("regid")){
				regId = extras.getString("regid");
			}
		}
		if(regId == null || regId.equals("")){
			regId = GCMRegistrar.getRegistrationId(this);
		}
		btnTryAgain = (Button)findViewById(R.id.btnTryAgain);
		btnTryAgain.setTag(TAG_BTN_TRY_AGAIN);
		btnTryAgain.setOnClickListener(onClickListener_BUTTON_CLICKED);
		
	}

	OnClickListener onClickListener_BUTTON_CLICKED = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub

			int iTag = (Integer) view.getTag();

			switch (iTag) {

			case TAG_BTN_TRY_AGAIN:
				tryAgain();
				break;

			default:
				break;
			}

		}
	};
	
	public void tryAgain(){
		Intent intent = new Intent(AuthenticationError.this,Authentication.class);
		intent.putExtra("from_activity_name", Authentication.class.getSimpleName());
		intent.putExtra("regid", regId);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.authentication_error, menu);
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	Intent intent2 = new Intent(AuthenticationError.this,Authentication.class);
	    	intent2.putExtra("from_activity_name", Authentication.class.getSimpleName());
	    	intent2.putExtra("regid", regId);
			startActivity(intent2);
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

}
