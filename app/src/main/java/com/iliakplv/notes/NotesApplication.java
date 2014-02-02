package com.iliakplv.notes;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Author: Ilya Kopylov
 * Date:  21.08.2013
 */
public class NotesApplication extends Application {

	private static final String LOG_TAG = NotesApplication.class.getSimpleName();

	private static Context context;

	@Override
	public void onCreate() {
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "onCreate() call");
		}
		super.onCreate();
		context = this.getApplicationContext();
	}

	@Override
	public void onTerminate() {
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "onTerminate() call");
		}
		super.onTerminate();
	}


	public static Context getContext() {
		return context;
	}

}
