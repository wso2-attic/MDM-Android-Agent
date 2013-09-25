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

import static com.mdm.CommonUtilities.SERVER_URL;
import static com.mdm.CommonUtilities.TAG;
import com.google.android.gcm.GCMRegistrar;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.*;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	public static boolean isAuthenticate(String username, String password,
			Context context) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("password", password);
		String response = sendWithTimeWait("users/authenticate", params,
				"POST", context).get("response");
		try {
			if (response.trim().contains("200")) {
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
		String status = response.get("status");
		try {
			Log.v("Register State", response.get("response"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (response.get("response").trim().equals("registered") || status.trim().equals("200")) {
			return true;
		} else {
			return false;
		}
	}

	public static String register(String regId, Context context) {
		DeviceInfo deviceInfo = new DeviceInfo(context);
		JSONObject jsObject = new JSONObject();
		String osVersion = "";
		try {
			osVersion = deviceInfo.getOsVersion();
			jsObject.put("device", deviceInfo.getDevice());
			jsObject.put("imei", deviceInfo.getDeviceId());
			jsObject.put("imsi", deviceInfo.getIMSINumber());
			jsObject.put("model", deviceInfo.getDeviceModel());
			jsObject.put("email", deviceInfo.getEmail());
			// jsObject.put("sdkversion", deviceInfo.getSdkVersion());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, String> params = new HashMap<String, String>();
		params.put("regid", regId);
		params.put("properties", jsObject.toString());
		params.put("email", deviceInfo.getEmail());
		params.put("osversion", osVersion);
		params.put("platform", "Android");
		params.put("vendor", deviceInfo.getDeviceManufacturer());

		// Calls the function "sendTimeWait" to do a HTTP post to our server
		// using Android HTTPUrlConnection API
		String response = sendWithTimeWait("devices/register", params, "POST",
				context).get("response");
		return response;
	}

	public static String unregister(String regId, Context context) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("regid", regId);
		String response = sendWithTimeWait("devices/unregister", params,
				"POST", context).get("response");
		return response;
	}

	public static String pushData(Map<String, String> params, Context context) {
		String response = sendWithTimeWait("notifications", params, "POST",
				context).get("response");
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
		String[] allowHost = {"my.ultra.com", "your.ultra.com", "ours.ultra.com"}; 
		
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

	public final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
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
			endpoint = "http://"+ipSaved+":9763/mdm/api/"+ epPostFix;
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

				// trustAllHosts();
				trustIFNetServer(context);
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
				Log.v("Response Statussss", status + "");
				InputStream inStream = conn.getInputStream();
				response = inputStreamAsString(inStream);
				response_params.put("response",response);
				Log.v("Response Messageeeee", response);
				response_params.put("status", String.valueOf(status));
			} else {
				status = 200;
			}
			if (status != 200 && status != 201) {
				throw new IOException("Post failed with error code " + status);
			}
		} finally {
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

			String keyPassword = "0772356583";

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
				return null;
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
				builder.append("\n"); // appende a new line
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
