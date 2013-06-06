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

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DevicePolicyDemoActivity extends Activity implements
		OnCheckedChangeListener {
	static final String TAG = "DevicePolicyDemoActivity";
	static final int ACTIVATION_REQUEST = 47; // identifies our request id
	DevicePolicyManager devicePolicyManager;
	ComponentName demoDeviceAdmin;
	ToggleButton toggleButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	/*	setContentView(R.layout.main);

		toggleButton = (ToggleButton) super
				.findViewById(R.id.toggle_device_admin);
		toggleButton.setOnCheckedChangeListener(this);*/

		// Initialize Device Policy Manager service and our receiver class
		devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		demoDeviceAdmin = new ComponentName(this, DemoDeviceAdminReceiver.class);
	}

	/**
	 * Called when a button is clicked on. We have Lock Device and Reset Device
	 * buttons that could invoke this method.
	 */
	public void onClick(View v) {
	/*	switch (v.getId()) {
		case R.id.button_lock_device:
			// We lock the screen
			Toast.makeText(this, "Locking device...", Toast.LENGTH_LONG).show();
			Log.d(TAG, "Locking device now");
			devicePolicyManager.lockNow();
			break;
		case R.id.button_reset_device:
			// We reset the device - this will erase entire /data partition!
			Toast.makeText(this, "Locking device...", Toast.LENGTH_LONG).show();
			Log.d(TAG,
					"RESETing device now - all user data will be ERASED to factory settings");
			devicePolicyManager.wipeData(ACTIVATION_REQUEST);
			break;
		}*/
	}

	/**
	 * Called when the state of toggle button changes. In this case, we send an
	 * intent to activate the device policy administration.
	 */
	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		if (isChecked) {
			// Activate device administration
			Intent intent = new Intent(
					DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
					demoDeviceAdmin);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					"Enable device administration");
			startActivityForResult(intent, ACTIVATION_REQUEST);
		}
		Log.d(TAG, "onCheckedChanged to: " + isChecked);
	}

	/**
	 * Called when startActivityForResult() call is completed. The result of
	 * activation could be success of failure, mostly depending on user okaying
	 * this app's request to administer the device.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVATION_REQUEST:
			if (resultCode == Activity.RESULT_OK) {
				Log.i(TAG, "Administration enabled!");
				toggleButton.setChecked(true);
			} else {
				Log.i(TAG, "Failed to enable administration!");
				toggleButton.setChecked(false);
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}