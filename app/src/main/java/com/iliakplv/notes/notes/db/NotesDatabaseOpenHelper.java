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

	private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

	NotesDatabaseOpenHelper(String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(NotesApplication.getContext(), name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(NotesDatabaseAdapter.CREATE_SCHEME_COMMAND);
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Database created with command: " + NotesDatabaseAdapter.CREATE_SCHEME_COMMAND);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO implement
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Upgrading version " + oldVersion + " to " + newVersion);
		}
		db.execSQL(DROP_TABLE_IF_EXISTS + NotesDatabaseAdapter.NOTES_TABLE);
		onCreate(db);
	}
}
