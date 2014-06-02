package com.iliakplv.notes.utils;

import android.util.Log;

import com.iliakplv.notes.BuildConfig;

public class AppLog {

	private static final boolean ENABLED = BuildConfig.DEBUG;


	public static void d(String tag, String msg) {
		if (ENABLED) {
			Log.d(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (ENABLED) {
			Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		e(tag, msg, null);
	}

	public static void e(String tag, String msg, Throwable e) {
		if (ENABLED) {
			Log.e(tag, msg, e);
		}
	}

	public static void i(String tag, String msg) {
		if (ENABLED) {
			Log.i(tag, msg);
		}
	}
}