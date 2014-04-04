package com.iliakplv.notes;

import android.app.Application;
import android.content.Context;

import com.iliakplv.notes.utils.AppLog;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NotesApplication extends Application {

	private static final String LOG_TAG = NotesApplication.class.getSimpleName();

	private static Context context;

	private static final int NON_CORE_THREADS_KEEP_ALIVE_TIME_SECONDS = 1;
	private static ThreadPoolExecutor executor;

	@Override
	public void onCreate() {
		AppLog.d(LOG_TAG, "onCreate() call");
		super.onCreate();

		context = this.getApplicationContext();

		final int processors = Runtime.getRuntime().availableProcessors();
		AppLog.d(LOG_TAG, "Detected " + processors + " processors. Creating thread pool...");
		executor = new ThreadPoolExecutor(processors,
				processors,
				NON_CORE_THREADS_KEEP_ALIVE_TIME_SECONDS,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	@Override
	public void onTerminate() {
		AppLog.d(LOG_TAG, "onTerminate() call");
		super.onTerminate();
	}


	public static Context getContext() {
		return context;
	}

	public static void executeInBackground(Runnable task) {
		executor.execute(task);
	}

}
