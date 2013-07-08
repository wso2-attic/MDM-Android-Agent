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

import android.content.Context;
import android.content.Intent;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public class CommonUtilities {

    /**
     * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
     */
    static String SERVER_URL = "http://10.100.1.142:9763/mdm/api/";
    
    public static String getSERVER_URL() {
		return SERVER_URL;
	}

	public static void setSERVER_URL(String sERVER_URL) {
		SERVER_URL = "http://"+sERVER_URL+":9763/mdm/api/";
	}

	/**
     * Google API project id registered to use GCM.
     */
    static final String SENDER_ID = "427708123537";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCMDemo";

    /**
     * Intent used to display a message in the screen.
     */
    static final String DISPLAY_MESSAGE_ACTION =
            "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    static final String EXTRA_MESSAGE = "message";
    static final int MESSAGE_MODE_GCM = 1;
    static final int MESSAGE_MODE_SMS = 2;
    
    /**
     * Operation IDs
     */
    static final String OPERATION_DEVICE_INFO = "500A";
    static final String OPERATION_DEVICE_LOCATION = "501A";
    static final String OPERATION_GET_APPLICATION_LIST = "502A";
    static final String OPERATION_LOCK_DEVICE = "503A";
    static final String OPERATION_WIPE_DATA = "504A";
    static final String OPERATION_CLEAR_PASSWORD = "505A";
    static final String OPERATION_NOTIFICATION = "506A";
    static final String OPERATION_WIFI = "507A";
    static final String OPERATION_DISABLE_CAMERA = "508A";
    static final String OPERATION_INSTALL_APPLICATION = "509A";
    static final String OPERATION_UNINSTALL_APPLICATION = "510A";
    static final String OPERATION_ENCRYPT_STORAGE = "511A";
    static final String OPERATION_APN = "512A";
    static final String OPERATION_MUTE = "513A";
    static final String OPERATION_TRACK_CALLS = "514A";
    static final String OPERATION_TRACK_SMS = "515A";
    static final String OPERATION_DATA_USAGE = "516A";
    static final String OPERATION_STATUS = "517A";
    static final String OPERATION_WEBCLIP = "518A";
    static final String OPERATION_PASSWORD_POLICY = "519A";
    static final String OPERATION_EMAIL_CONFIGURATION = "520A";
    static final String OPERATION_INSTALL_GOOGLE_APP = "522A";
    static final String OPERATION_CHANGE_LOCK_CODE = "526A";
    
    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}
