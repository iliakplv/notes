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

	NotesDatabaseOpenHelper(String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(NotesApplication.getContext(), name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(NotesDatabaseAdapter.CREATE_SCHEME_COMMAND);
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Database created by command: " + NotesDatabaseAdapter.CREATE_SCHEME_COMMAND);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "[empty] Upgrading version " + oldVersion + " to " + newVersion);
		}
//		TODO implement in case of schema changing
//		db.execSQL("DROP TABLE IF EXISTS " + NotesDatabaseAdapter.NOTES_TABLE);
//		onCreate(db);
	}
}
