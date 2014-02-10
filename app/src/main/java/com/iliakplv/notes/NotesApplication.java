package com.iliakplv.notes;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Ilya Kopylov
 * Date:  21.08.2013
 */
public class NotesApplication extends Application {

	private static final String LOG_TAG = NotesApplication.class.getSimpleName();

	private static Context context;

	private static final int THREAD_KEEP_ALIVE_TIME_SECONDS = 30;
	private static ThreadPoolExecutor executor;

	@Override
	public void onCreate() {
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "onCreate() call");
		}
		super.onCreate();

		context = this.getApplicationContext();

		final int processors = Runtime.getRuntime().availableProcessors();
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Detected " + processors + " processors. Creating thread pool...");
		}
		executor = new ThreadPoolExecutor(processors,
				processors,
				THREAD_KEEP_ALIVE_TIME_SECONDS,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
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

	public static void executeInBackground(Runnable task) {
		executor.execute(task);
	}

}
