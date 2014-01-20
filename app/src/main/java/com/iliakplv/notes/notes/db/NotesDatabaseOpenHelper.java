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
		// TODO test
		db.execSQL(NotesDatabaseAdapter.CREATE_NOTES_TABLE);
		db.execSQL(NotesDatabaseAdapter.CREATE_LABELS_TABLE);
		db.execSQL(NotesDatabaseAdapter.CREATE_NOTES_LABELS_TABLE);
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Schema creation: " + NotesDatabaseAdapter.CREATE_NOTES_TABLE);
			Log.d(LOG_TAG, "Schema creation: " + NotesDatabaseAdapter.CREATE_LABELS_TABLE);
			Log.d(LOG_TAG, "Schema creation: " + NotesDatabaseAdapter.CREATE_NOTES_LABELS_TABLE);
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
