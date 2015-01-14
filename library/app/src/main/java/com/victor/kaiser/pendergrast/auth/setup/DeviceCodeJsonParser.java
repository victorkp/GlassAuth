package com.victor.kaiser.pendergrast.auth.setup;

import org.json.JSONObject;

import android.content.SharedPreferences;

import com.victor.kaiser.pendergrast.auth.preference.PreferenceConstants;

public class DeviceCodeJsonParser {
	private static final String TAG = "DeviceCodeJsonParser";

	/**
	 * The fields that are expected in the JSON response
	 */
	private static class JSONFields {
		public static final String DEVICE_CODE = "device_code";
		public static final String USER_CODE = "user_code";
		public static final String VERIFICATION_URL = "verification_url";
		public static final String EXPIRES_IN = "expires_in";
		public static final String INTERVAL = "interval";
	}

	
	private String mDeviceCode;
	private String mUserCode;
	private String mUrl;
	private long mExpiration;
	private int mInterval;

	/**
	 * Decode JSON into Device Code, User Code, Verification URL,
	 * Expiration Time, and Min Request Interval
	 */
	public DeviceCodeJsonParser(String jsonResponse) {

		try {
			JSONObject obj = new JSONObject(jsonResponse);
			
			mDeviceCode = obj.getString(JSONFields.DEVICE_CODE);
			mUserCode = obj.getString(JSONFields.USER_CODE);
			mUrl = obj.getString(JSONFields.VERIFICATION_URL);
			mExpiration = obj.getLong(JSONFields.EXPIRES_IN);
			mInterval = obj.getInt(JSONFields.INTERVAL);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the Device Code to SharedPreferences
	 */
	public void writeToPreferences(SharedPreferences prefs){
		prefs.edit()
			.putString(PreferenceConstants.DEVICE_CODE, mDeviceCode)
			.commit();
	}
	
	public String getDeviceCode(){
		return mDeviceCode;
	}
	
	public String getUserCode(){
		return mUserCode;
	}
	
	public String getUrl(){
		return mUrl;
	}
	
	public long getExpirationTime(){
		return mExpiration;
	}
	
	public int getRequestInteral(){
		return mInterval;
	}

}
