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
package com.wso2mobile.mdm.utils;

import static com.wso2mobile.mdm.utils.CommonUtilities.TAG;

import com.google.android.gcm.GCMRegistrar;
import com.wso2mobile.mdm.R;
import com.wso2mobile.mdm.api.DeviceInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

	private static final int MAX_ATTEMPTS = 2;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	public static boolean isAuthenticate(String username, String password,
			Context context) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("password", password);
		String response = "";
		try {
			response = sendWithTimeWait("users/authenticate", params,
					"POST", context).get("response");
			if (response.trim().contains(CommonUtilities.REQUEST_SUCCESSFUL)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean isRegistered(String regId, Context context) {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> response = new HashMap<String, String>();
		params.put("regid", regId);
		response = sendWithTimeWait("devices/isregistered", params,
				"POST", context);
		String status="";
		try {
			status = response.get("status");
			Log.v("Register State", response.get("response"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (response.get("response").trim().equals("registered") || status.trim().equals(CommonUtilities.REQUEST_SUCCESSFUL)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String getEULA(Context context) {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> response = new HashMap<String, String>();
		String res="";
		params.put("", null);
		response = getRequest("devices/license", context);
		String status = "";
		try {
			status = response.get("status");
			Log.v("EULA RESPONSE", response.get("response"));
			res =  response.get("response");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (status.trim().equals(CommonUtilities.REQUEST_SUCCESSFUL)) {
			return res;
			
		} else {
			return null;
		}
	}
	
	public static HttpClient getNewHttpClient() {
	     try {
	         KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	         trustStore.load(null, null);

	         SSLSocketFactory sf = new WSO2MobileSSLSocketFactory(trustStore);
	         sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	         HttpParams params = new BasicHttpParams();
	         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	         HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	         SchemeRegistry registry = new SchemeRegistry();
	         registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	         registry.register(new Scheme("https", sf, 443));

	         ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	         return new DefaultHttpClient(ccm, params);
	     } catch (Exception e) {
	         return new DefaultHttpClient();
	     }
	}
	
	public static Map<String, String> getRequest(String url, Context context){
		HttpResponse response = null;
		Map<String, String> response_params = new HashMap<String, String>();
		String _response ="";
		try {        
			String endpoint = CommonUtilities.SERVER_URL + url;
			
			SharedPreferences mainPref = context.getSharedPreferences(
					"com.mdm", Context.MODE_PRIVATE);
			String ipSaved = mainPref.getString("ip", "");
			
			if(ipSaved != null && ipSaved != ""){
				endpoint = "http://"+ipSaved+":"+CommonUtilities.SERVER_PORT+"/mdm/api/"+ url;
			}
			
		        HttpClient client = getNewHttpClient();
		        HttpGet request = new HttpGet();
		        request.setURI(new URI(endpoint));
		        response = client.execute(request);
		        _response = convertStreamToString(response.getEntity().getContent());
		        response_params.put("response",_response);
				response_params.put("status", String.valueOf(response.getStatusLine().getStatusCode()));
			Log.e("RESPONSE : ",_response);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response_params;
	}
	
	public static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

	public static boolean register(String regId, Context context) {
		DeviceInfo deviceInfo = new DeviceInfo(context);
		JSONObject jsObject = new JSONObject();
		String osVersion = "";
		String response = "";
		boolean state=false;
		try {
			osVersion = deviceInfo.getOsVersion();
			jsObject.put("device", deviceInfo.getDevice());
			jsObject.put("imei", deviceInfo.getDeviceId());
			jsObject.put("imsi", deviceInfo.getIMSINumber());
			jsObject.put("model", deviceInfo.getDeviceModel());
			jsObject.put("email", deviceInfo.getEmail());
			// jsObject.put("sdkversion", deviceInfo.getSdkVersion());

		

		Map<String, String> params = new HashMap<String, String>();
		params.put("regid", regId);
		params.put("properties", jsObject.toString());
		params.put("email", deviceInfo.getEmail());
		params.put("osversion", osVersion);
		params.put("platform", "Android");
		params.put("vendor", deviceInfo.getDeviceManufacturer());

		// Calls the function "sendTimeWait" to do a HTTP post to our server
		// using Android HTTPUrlConnection API
		response = sendWithTimeWait("devices/register", params, "POST",
				context).get("status");

		if(response.equals(CommonUtilities.REGISTERATION_SUCCESSFUL)){
			state = true;
		}else{
			state = false;
		}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return state;
	}

	public static boolean unregister(String regId, Context context) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("regid", regId);
		String response = "";
		boolean state=false;
		try{
		response = sendWithTimeWait("devices/unregister", params,
				"POST", context).get("status");
		if(response.equals(CommonUtilities.REQUEST_SUCCESSFUL)){
			state = true;
		}else{
			state = false;
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return state;
	}

	public static String pushData(Map<String, String> params, Context context) {
		String response="";
		try{
		response = sendWithTimeWait("notifications", params, "POST",
				context).get("response");
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	public static Map<String, String> sendWithTimeWait(String epPostFix,
		Map<String, String> params, String option, Context context) {
		Map<String, String> response = null;
		Map<String, String> responseFinal = null;
		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			Log.d(TAG, "Attempt #" + i + " to register");
			try {
				response = sendToServer(epPostFix, params, option, context);
				if (response != null && !response.equals(null)) {
					responseFinal = response;
				}
				GCMRegistrar.setRegisteredOnServer(context, true);
				String message = context.getString(R.string.server_registered);
				Log.v("Check Reg Success", message.toString());
				// displayMessage(context, message);
				return responseFinal;
			} catch (IOException e) {
				Log.e(TAG, "Failed to register on attempt " + i, e);
				if (i == MAX_ATTEMPTS) {
					break;
				}
				/*
				 * try { Log.d(TAG, "Sleeping for " + backoff +
				 * " ms before retry"); Thread.sleep(backoff); } catch
				 * (InterruptedException e1) { // Activity finished before we
				 * complete - exit. Log.d(TAG,
				 * "Thread interrupted: abort remaining retries!");
				 * Thread.currentThread().interrupt(); return null; } //
				 * increase backoff exponentially backoff *= 2;
				 */
				return responseFinal;
			}
		}
		String message = context.getString(R.string.server_register_error,
				MAX_ATTEMPTS);
		// CommonUtilities.displayMessage(context, message);
		return responseFinal;
	}
	
	public final static HostnameVerifier WSO2MOBILE_HOST = new HostnameVerifier() {
		String[] allowHost = {"192.168.18.57", "204.13.81.82", "ours.ultra.com"}; 
		
		public boolean verify(String hostname, SSLSession session) {
			boolean status = false;
			try{
			for (int i=0; i < allowHost.length; i++) {
	             if (hostname == allowHost[i])
	                status = true;
	        }
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return status;
		}
		
	};

	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	public static Map<String, String> sendToServer(String epPostFix, Map<String, String> params,
			String option, Context context) throws IOException {
		String response = null;
		Map<String, String> response_params = new HashMap<String, String>();
		String endpoint = CommonUtilities.SERVER_URL + epPostFix;
		
		SharedPreferences mainPref = context.getSharedPreferences(
				"com.mdm", Context.MODE_PRIVATE);
		String ipSaved = mainPref.getString("ip", "");
		
		if(ipSaved != null && ipSaved != ""){
			endpoint = "http://"+ipSaved+":"+CommonUtilities.SERVER_PORT+"/mdm/api/"+ epPostFix;
		}
		
		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
					.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		Log.v(TAG, "Posting '" + body + "' to " + url);
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		HttpsURLConnection sConn = null;
		try {

			if (url.getProtocol().toLowerCase().equals("https")) {
				//trustIFNetServer(context);
				//sConn.setHostnameVerifier(DO_NOT_VERIFY);
				trustAllHosts();
				sConn = (HttpsURLConnection) url.openConnection();
				sConn.setHostnameVerifier(DO_NOT_VERIFY);
				conn = sConn;

			} else {
				conn = (HttpURLConnection) url.openConnection();
			}
			// conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod(option);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			// conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Connection", "close");
			// post the request
			int status = 0;
			Log.v("Check verb", option);
			if (!option.equals("DELETE")) {
				OutputStream out = conn.getOutputStream();
				out.write(bytes);
				out.close();
				// handle the response
				status = conn.getResponseCode();
				Log.v("Response Status", status + "");
				InputStream inStream = conn.getInputStream();
				response = inputStreamAsString(inStream);
				response_params.put("response",response);
				Log.v("Response Message", response);
				response_params.put("status", String.valueOf(status));
			} else {
				status = Integer.valueOf(CommonUtilities.REQUEST_SUCCESSFUL);
			}
			if (status != Integer.valueOf(CommonUtilities.REQUEST_SUCCESSFUL) && status != Integer.valueOf(CommonUtilities.REGISTERATION_SUCCESSFUL)) {
				throw new IOException("Post failed with error code " + status);
			}
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return response_params;
	}

	private static void trustIFNetServer(Context context) {

		try {
			/*TrustManagerFactory tmf = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());*/
			
			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance("PKIX");
			KeyStore ks = KeyStore.getInstance("BKS");

			InputStream in = context.getResources().openRawResource(
					R.raw.truststore);

			String keyPassword = "";

			ks.load(in, keyPassword.toCharArray());

			in.close();

			tmf.init(ks);

			TrustManager[] tms = tmf.getTrustManagers();

			SSLContext sc = SSLContext.getInstance("TLS");

			sc.init(null, tms, new java.security.SecureRandom());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void trustAllHosts() {

		X509TrustManager easyTrustManager = new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub

			}

		};

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { easyTrustManager };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");

			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String inputStreamAsString(InputStream in) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder builder = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append("\n"); // append a new line
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// System.out.println(builder.toString());
		return builder.toString();
	}

}
