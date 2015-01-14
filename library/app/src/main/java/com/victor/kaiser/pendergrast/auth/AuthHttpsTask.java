package com.victor.kaiser.pendergrast.auth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.victor.kaiser.pendergrast.auth.preference.PreferenceConstants;
import com.victor.kaiser.pendergrast.auth.setup.AuthConstants;
import com.victor.kaiser.pendergrast.auth.setup.AuthTokenJsonParser;

/**
 * An AsyncTask to issue HTTPS requests with an Auth Token.
 * This AsyncTask handles retrieving an Auth Token, making the user's request (with the token),
 * and calling back through an OnResponseListener
 */
public class AuthHttpsTask extends AsyncTask<Void, Void, Integer> {

	private static final String TAG = "PutNotesTask";

	// Integers to indicate success and various failures possible
	private static final int SUCCESS = 0;
	private static final int FAILURE_UNKNOWN = 1;
	private static final int FAILURE_NO_AUTH_TOKEN = 2;
	private static final int FAILURE_NO_JSON = 3;
	private static final int FAILURE_BAD_RESPONSE = 4;
	private static final int FAILURE_BAD_AUTH_TOKEN_RESPONSE = 5;
	private static final int FAILURE_NO_REFRESH_TOKEN = 6;

	/**
	 * A listener for HTTP responses once the 
	 * AuthHttpsTask is executed
	 */
	public static interface OnResponseListener {
		/**
		 * Called when there is an
		 * @param success true if the server responded with HTTP 200
		 * @param response the response from the server
		 */
		public void onResponse(boolean success, String response);
	}

	/**
	 * The response received from the server
	 */
	private String mResponse = "";

	/**
	 * The request method to use: "GET", "POST", etc.
	 */
	private String mRequestMethod = "GET";
	
	/**
	 * The URL to connect to
	 */
	private String mUrl = "";

	/**
	 * The JSON to send to the server
	 */
	private String mJSON = "";
	
	/**
	 * The Auth Token to use for this request
	 */
	private String mToken = "";
	
	/**
	 * The refresh token retrieved
	 */
	private String mRefreshToken = "";

	/**
	 * Listener to call once we're done
	 */
	private OnResponseListener mListener;
	
	/**
	 * Instantiate an AuthHttpsTask
	 * @param context the application context
	 * @param url the URL to send request to
	 * @param listener a listener to get the response
	 */
	public AuthHttpsTask(Context context, String url,  OnResponseListener listener){
		mRefreshToken = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceConstants.REFRESH_TOKEN, "");
		mUrl = url;
		mListener = listener;
	}

	/**
	 * Instantiate an AuthHttpsTask
	 * @param context the application context
	 * @param url the URL to send request to
	 * @param json additional json to send along with the request
	 * @param listener an OnResponseListener to get the response
	 */
	public AuthHttpsTask(Context context, String url, String json, OnResponseListener listener){
		this(context, url, listener);
		mJSON = json;
	}
	
	/**
	 * Instantiate an AuthHttpsTask
	 * @param context the application context
	 * @param url the URL to send request to
	 * @param json additional json to send along with the request
	 * @param requestMethod "GET", "PUT", "POST", etc.
	 * @param listener an OnResponseListener to get the response
	 */
	public AuthHttpsTask(Context context, String url, String json, String requestMethod, OnResponseListener listener){
		this(context, url, json, listener);
		mRequestMethod = requestMethod;
	}
	
	/**
	 * Set the listener that will be called once the
	 * notes are put to the server
	 */
	public void setListener(OnResponseListener listener) {
		mListener = listener;
	}

	/**
	 * Set the JSON to send to the server
	 */
	public void setJSON(String json){
		mJSON = json;
	}
	
	/**
	 * Set the request method: "GET", "PUT", "POST", etc.
	 */
	public void setRequestMethod(String method){
		mRequestMethod = method;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		
		if(mRefreshToken.isEmpty()){
			return FAILURE_NO_REFRESH_TOKEN;
		}

		/*
		 * Step 1: Retrieve an Auth Token
		 * Post Client ID, Secret, and Refresh Token to Google's OAuth service
		 * Receive JSON with Access Token, Token Type, Expiration, and Refresh Token
		 */
		try {
			URL urlObject = new URL("https://accounts.google.com/o/oauth2/token");

			HttpsURLConnection con = (HttpsURLConnection) urlObject.openConnection();
			
			con.setRequestMethod("POST");
			con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			con.setDoOutput(true);
			con.connect();

			// Now write the parameters:
			// Client ID, Client Secret, Device Code, and what kind of Grant
			OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
			
			String urlParams = "client_id=" + AuthConstants.CLIENT_ID + 
								"&client_secret=" + AuthConstants.CLIENT_SECRET + 
								"&refresh_token=" + mRefreshToken + 
								"&grant_type=refresh_token";
			out.write(urlParams);
			
			out.flush();
			out.close();
			

			int serverCode = con.getResponseCode();

			Log.i(TAG, "HttpURLConnection response: " + serverCode);

			if (serverCode != 200) {
				// Response is bad
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));

				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					Log.e(TAG, line);
				}

				return FAILURE_NO_AUTH_TOKEN;
			} else {
				// Response is good
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

				String authResponse = "";	
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					authResponse += line;
				}
				
				AuthTokenJsonParser parser = new AuthTokenJsonParser(authResponse);
				if(parser.hasError()){
					return FAILURE_BAD_AUTH_TOKEN_RESPONSE;
				}
				
				mToken = parser.getAuthToken();
			}
		} catch (Exception e){
			return FAILURE_UNKNOWN;
		}
		
		
		if (mToken.isEmpty()) {
			return FAILURE_NO_AUTH_TOKEN;
		}

		if(mJSON.isEmpty()){
			return FAILURE_NO_JSON;
		}

		/*
		 * Step 2: Make Request with Auth Token
		 * Use specified Request Method, additional JSON, and URL to make request
		 */
		try {
			URL urlObject = new URL(mUrl);

			HttpsURLConnection con = (HttpsURLConnection) urlObject.openConnection();

			con.setRequestMethod(mRequestMethod);
			con.setRequestProperty("Authorization", "Bearer " + mToken);
			con.setRequestProperty("Content-Type", "application/json");
			
			con.setDoOutput(true);

			con.connect();

			// Write the JSON
			OutputStreamWriter output = new OutputStreamWriter(con.getOutputStream());
			output.write(mJSON);
			output.flush();
			output.close();

			int serverCode = con.getResponseCode();

			Log.i(TAG, "HttpURLConnection response: " + serverCode);

			if (serverCode != 200) {
				// Response is bad
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));

				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					Log.e(TAG, line);
				}

				return FAILURE_BAD_RESPONSE;
			} else {
				// Response is good
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					mResponse += line;
				}

				Log.i(TAG, "Response: \"\n" + mResponse + "\n\"");

				return SUCCESS;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return FAILURE_UNKNOWN;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		/*
		 * Step 3: Call Listener
		 */
		if(mListener != null) {
			mListener.onResponse(result.intValue() == SUCCESS, mResponse);
		}
	}

}
