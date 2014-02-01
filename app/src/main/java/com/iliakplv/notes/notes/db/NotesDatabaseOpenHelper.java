package com.iliakplv.notes.notes.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.iliakplv.notes.BuildConfig;
import com.iliakplv.notes.NotesApplication;

/**
 * Author: Ilya Kopylov
 * Date:  21.08.2013
 */
class NotesDatabaseOpenHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = NotesDatabaseOpenHelper.class.getSimpleName();

	public static final int DATABASE_VERSION_FIRST = 1;
	public static final int DATABASE_VERSION_LABELS = 2;


	NotesDatabaseOpenHelper(String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(NotesApplication.getContext(), name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(NotesDatabaseAdapter.CREATE_NOTES_TABLE);
		upgradeToLabels(db, true);
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Schema creation: " + NotesDatabaseAdapter.CREATE_NOTES_TABLE);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Upgrading version " + oldVersion + " to " + newVersion);
		}

		if (oldVersion == DATABASE_VERSION_FIRST && newVersion == DATABASE_VERSION_LABELS) {
			upgradeToLabels(db, false);
		}
	}

	private void upgradeToLabels(SQLiteDatabase db, boolean creation) {
		db.execSQL(NotesDatabaseAdapter.CREATE_LABELS_TABLE);
		db.execSQL(NotesDatabaseAdapter.CREATE_NOTES_LABELS_TABLE);
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, (creation ? "Schema creation: " : "Schema upgrading: ") + NotesDatabaseAdapter.CREATE_LABELS_TABLE);
			Log.d(LOG_TAG, (creation ? "Schema creation: " : "Schema upgrading: ") + NotesDatabaseAdapter.CREATE_NOTES_LABELS_TABLE);
		}
	}
}
