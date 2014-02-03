/*
 ~ Copyright (c) 2014, WSO2 Inc. (http://wso2.com/) All Rights Reserved.
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
package com.wso2.mobile.mdm.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public class CommonUtilities {
	public static boolean DEBUG_MODE_ENABLED = true;
    /**
     * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
     */
    //public static String SERVER_IP = "mdm.wso2.com"; //Live server
	public static String SERVER_IP = "10.100.0.150"; //Kasun's
	//public static String SERVER_IP = "192.168.1.2";
   // public static String SERVER_IP = "10.100.0.150";//Gayan's
	//public static String SERVER_IP = "10.100.0.151";//Chan's
	//public static String SERVER_IP = "10.10.10.86"; //Nira's
	//public static String SERVER_IP = "192.168.18.57";//Lab machine
	//public static String SERVER_IP = "192.168.1.44";
	public static String SERVER_PORT = "9763";
	public static String SERVER_PROTOCOL = "http://";
	public static String SERVER_APP_ENDPOINT = "/mdm/api/";
	public static String SERVER_URL = SERVER_PROTOCOL+SERVER_IP+":"+SERVER_PORT+SERVER_APP_ENDPOINT;
    
    public static String getSERVER_URL() {
		return SERVER_URL;
	}

	public static void setSERVER_URL(String sERVER_URL) {
		SERVER_IP = sERVER_URL;
		SERVER_URL = SERVER_PROTOCOL+sERVER_URL+":"+SERVER_PORT+SERVER_APP_ENDPOINT;
	}

	/**
     * Google API project id registered to use GCM.
     */
	//public static String SENDER_ID = "427708123537";//Gayan's
    public static String SENDER_ID = "";//Kasun's
	//public static String SENDER_ID = "84876239920";//Live server
	
    public static String getSENDER_ID() {
		return SENDER_ID;
	}

	public static void setSENDER_ID(String sENDER_ID) {
		SENDER_ID = sENDER_ID;
	}

	/**
     * Tag used on log messages.
     */
	public static final String TAG = "WSO2MobileMDM";

    /**
     * Intent used to display a message in the screen.
     */
	public static final String DISPLAY_MESSAGE_ACTION =
            "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
	public static final String EXTRA_MESSAGE = "message";
	public static final int MESSAGE_MODE_GCM = 1;
	public static final int MESSAGE_MODE_SMS = 2;
	public static final String EULA_TITLE = "License Agreement";
	public static final String EULA_TEXT = "This End User License Agreement (“Agreement”) is a legal agreement between you (“You”) and WSO2Mobile Lanka (Pvt) Ltd, regarding the enrollment of Your personal mobile device (“Device”) in SoR’s mobile device management program, and the loading to and removal from Your Device and Your use of certain applications and any associated software and user documentation, whether provided in “online” or electronic format, used in connection with the operation of or provision of services to WSO2Mobile Lanka (Pvt) Ltd.  BY SELECTING “I ACCEPT” DURING INSTALLATION, YOU ARE ENROLLING YOUR DEVICE, AND THEREBY AUTHORIZING SOR OR ITS AGENTS TO INSTALL, UPDATE AND REMOVE THE APPS FROM YOUR DEVICE AS DESCRIBED IN THIS AGREEMENT.  YOU ARE ALSO EXPLICITLY ACKNOWLEDGING AND AGREEING THAT (1) THIS IS A BINDING CONTRACT AND (2) YOU HAVE READ AND AGREE TO THE TERMS OF THIS AGREEMENT. IF YOU DO NOT ACCEPT THESE TERMS, DO NOT ENROLL YOUR DEVICE AND DO NOT PROCEED ANY FURTHER. You agree that: (1) You understand and agree to be bound by the terms and conditions contained in this Agreement, and (2) You are at least 21 years old and have the legal capacity to enter into this Agreement as defined by the laws of Your jurisdiction.  SoR shall have the right, without prior notice, to terminate or suspend (i) this Agreement, (ii) the enrollment of Your Device, or (iii) the functioning of the Apps in the event of a violation of this Agreement or the cessation of Your relationship with SoR (including termination of Your employment if You are an employee or expiration or termination of Your applicable franchise or supply agreement if You are a franchisee of or supplier to the WSO2Mobile Lanka (Pvt) Ltd system).  SoR expressly reserves all rights not expressly granted herein.";
    public static final String TRUSTSTORE_PASSWORD = "wso2mobile123";
	/**
	 * Status codes
	 */
	public static final String REQUEST_SUCCESSFUL = "200";
	public static final String REGISTERATION_SUCCESSFUL = "201";
	
	/**
     * Operation IDs
     */
	public static final String OPERATION_DEVICE_INFO = "500A";
	public static final String OPERATION_DEVICE_LOCATION = "501A";
	public static final String OPERATION_GET_APPLICATION_LIST = "502A";
	public static final String OPERATION_LOCK_DEVICE = "503A";
	public static final String OPERATION_WIPE_DATA = "504A";
	public static final String OPERATION_CLEAR_PASSWORD = "505A";
	public static final String OPERATION_NOTIFICATION = "506A";
	public static final String OPERATION_WIFI = "507A";
	public static final String OPERATION_DISABLE_CAMERA = "508A";
	public static final String OPERATION_INSTALL_APPLICATION = "509A";
	public static final String OPERATION_INSTALL_APPLICATION_BUNDLE = "509B";
	public static final String OPERATION_UNINSTALL_APPLICATION = "510A";
	public static final String OPERATION_ENCRYPT_STORAGE = "511A";
	public static final String OPERATION_APN = "512A";
	public static final String OPERATION_MUTE = "513A";
	public static final String OPERATION_TRACK_CALLS = "514A";
	public static final String OPERATION_TRACK_SMS = "515A";
	public static final String OPERATION_DATA_USAGE = "516A";
	public static final String OPERATION_STATUS = "517A";
	public static final String OPERATION_WEBCLIP = "518A";
	public static final String OPERATION_PASSWORD_POLICY = "519A";
	public static final String OPERATION_EMAIL_CONFIGURATION = "520A";
	public static final String OPERATION_INSTALL_GOOGLE_APP = "522A";
	public static final String OPERATION_CHANGE_LOCK_CODE = "526A";
	public static final String OPERATION_POLICY_BUNDLE = "500P";
	public static final String OPERATION_POLICY_MONITOR = "501P";
	public static final String OPERATION_BLACKLIST_APPS = "528B";
	public static final String OPERATION_POLICY_REVOKE = "502P";
    
    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
	public static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}
