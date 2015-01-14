package com.victor.kaiser.pendergrast.auth.setup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;
import android.util.Log;

/**
 * A class to get authentication initially setup 
 * so that later we can get OAuth tokens
 * See https://developers.google.com/accounts/docs/OAuth2ForDevices
 * for more detail
 */
public class GetDeviceCodeTask extends AsyncTask<String, Integer, Integer> {
	
	private static final String TAG = "GetDeviceCodeTask";
	
	private static final Integer SUCCESS = 1;
	private static final Integer FAILURE = 0;
	
	/**
	 * Listener to call once we're done
	 */
	private OnGetDeviceCodeListener mListener;
	
	/**
	 * The response received from the server
	 */
	private String mResponse = "";
	
	/**
	 * A simple functional interface that gets called
	 * once data is received, success depends on HTTP response
	 * code only, response is the JSON received
	 */
	public static interface OnGetDeviceCodeListener {
		public void onReceiveDeviceCode(boolean success, String response);
	}
	
	public void setListener(OnGetDeviceCodeListener listener){
		mListener = listener;
	}
	
	@Override
	protected Integer doInBackground(String... params) {

		try {
			URL urlObject = new URL("https://accounts.google.com/o/oauth2/device/code");

			HttpsURLConnection con = (HttpsURLConnection) urlObject.openConnection();
			
			con.setRequestMethod("POST");
			con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			con.setDoOutput(true);

			con.connect();
			
			// Now write the parameters:
			// Client ID and the requested scope
			OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
			
			String urlParams = "client_id="+AuthConstants.CLIENT_ID+"&scope=email%20profile";
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

				return FAILURE;
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
			return FAILURE;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		callListener(result.intValue() == SUCCESS);
	}
	
	private void callListener(boolean success){
		if(mListener != null){
			mListener.onReceiveDeviceCode(success, mResponse);
		}
	}
	
	

}
