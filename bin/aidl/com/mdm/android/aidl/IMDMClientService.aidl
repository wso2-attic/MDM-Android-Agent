package com.mdm.android.aidl;

import com.mdm.android.aidl.RegisterRequest;
import com.mdm.android.aidl.ServiceResponse;

/**
 * 
 */
interface IMDMClientService {
	/**
	* A registration request from TouchDown to the MDMClient
	 */
    ServiceResponse doRegister(in RegisterRequest request);
    ServiceResponse configComplete(in String ConfigCorrelationID);
    ServiceResponse doCommand(in String cmd);
    ServiceResponse requestConfig();
}
