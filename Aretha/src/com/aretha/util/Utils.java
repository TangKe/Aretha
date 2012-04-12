package com.aretha.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;

public class Utils {
	private static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

	/**
	 * Check whether the network connection is available
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnect(Context context) {
		ConnectivityManager connectionManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectionManager != null) {
			NetworkInfo info = connectionManager.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Generate a {@link UUID}
	 * 
	 * @param text
	 * @return
	 */
	public static UUID getUUID(String text) {
		if (text == null) {
			return UUID.randomUUID();
		}

		try {
			return UUID.fromString(text);
		} catch (IllegalArgumentException e) {
			return UUID.nameUUIDFromBytes(text.getBytes());
		}
	}

	public static String getMD5(String str) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}

		return md5StrBuff.toString();
	}

	public static void addShortcut(Context context, Class<?> launchActivity,
			int shortcutRes, int shortcutTitleRes) {
		Intent addShortcut = new Intent(ACTION_ADD_SHORTCUT);
		Parcelable icon = Intent.ShortcutIconResource.fromContext(context,
				shortcutRes);
		Intent intent = new Intent(context, launchActivity);

		addShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				context.getString(shortcutTitleRes));
		addShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		addShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		context.sendBroadcast(addShortcut);
	}
}
