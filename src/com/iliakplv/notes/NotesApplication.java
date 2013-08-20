package com.iliakplv.notes;


import android.app.Application;
import android.content.Context;

/**
 * Author: Ilya Kopylov
 * Date:  21.08.2013
 */
public class NotesApplication extends Application {

	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
	}

	public static Context getContext() {
		return context;
	}

}
