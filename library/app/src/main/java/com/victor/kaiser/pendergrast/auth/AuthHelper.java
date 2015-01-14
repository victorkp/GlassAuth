package com.victor.kaiser.pendergrast.auth;

import com.victor.kaiser.pendergrast.auth.preference.PreferenceConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AuthHelper {

	/**
	 * Determine if the user has already signed in
	 * @param context application context, used to read Preferences
	 * @return true if the user is signed in
	 */
	public static boolean isAuthenticated(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		return !prefs.getString(PreferenceConstants.REFRESH_TOKEN, "").isEmpty();
	}
}
