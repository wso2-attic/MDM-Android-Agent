package com.mdm.android.aidl;

import com.mdm.android.aidl.ServiceResponse;
import com.mdm.android.aidl.RegisterRequest;

/**
 * TouchDown Client exposes this service Interface. 
 */
interface IMDMAgentService {
	/**
	 * Send a command to the agent. The command is defined as an XML snippet
	 * this command can be a configuration, setPolicies, remoteWipe or a getPolicies command 
	 */
    ServiceResponse doCommand(in String commandXml);
    
    /* 
    * Send a Registration request 
    */
    ServiceResponse doRegister(in RegisterRequest req);
}
