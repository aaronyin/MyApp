package com.buaair.carsmart.utils;

import android.content.Context;
import android.widget.Toast;

public class Util {

	public static void showToast(Context mContext, String text) {
		Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public static void showToast(Context mContext, int resId) {
		Toast toast = Toast.makeText(mContext, resId, Toast.LENGTH_SHORT);
		toast.show();
	}
}
