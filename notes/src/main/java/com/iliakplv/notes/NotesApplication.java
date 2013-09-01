package com.iliakplv.notes;


import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.db.NotesDatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

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
		context = this;

		fillDatabase();
	}

	@Override
	public void onTerminate() {
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "onTerminate() call");
		}
		super.onTerminate();
	}

	// TODO test
	private void fillDatabase() {
		List<AbstractNote> notes = new ArrayList<AbstractNote>();
		notes.add(new TextNote("First", "First things first"));
		notes.add(new TextNote("One more", "Read me"));
		notes.add(new TextNote("Yet another", "DO NOT read me"));
		notes.add(new TextNote("Just note", "Very important text"));

		NotesDatabaseAdapter dbAdapter = new NotesDatabaseAdapter(this);
		dbAdapter.open();
		for (AbstractNote note : notes) {
			dbAdapter.insertNote(note);
		}
		dbAdapter.close();

	}

	public static Context getContext() {
		return context;
	}

}
