package com.iliakplv.notes.notes.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.utils.AppLog;

/* package */ class NotesDatabaseOpenHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = NotesDatabaseOpenHelper.class.getSimpleName();

	static final int DATABASE_VERSION_FIRST = 1;     // Only (notes)
	static final int DATABASE_VERSION_LABELS = 2;    // Added: (labels), (notes_labels)


	NotesDatabaseOpenHelper(String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(NotesApplication.getContext(), name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createFirstVersion(db);
		upgradeToLabels(db, true);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		AppLog.d(LOG_TAG, "Upgrading version " + oldVersion + " to " + newVersion);

		if (oldVersion == DATABASE_VERSION_FIRST && newVersion == DATABASE_VERSION_LABELS) {
			upgradeToLabels(db, false);
		}
	}

	private void createFirstVersion(SQLiteDatabase db) {
		db.execSQL(NotesDatabaseAdapter.CREATE_NOTES_TABLE);
		AppLog.d(LOG_TAG, "Schema creation: " + NotesDatabaseAdapter.CREATE_NOTES_TABLE);
	}

	private void upgradeToLabels(SQLiteDatabase db, boolean creation) {
		db.execSQL(NotesDatabaseAdapter.CREATE_LABELS_TABLE);
		db.execSQL(NotesDatabaseAdapter.CREATE_NOTES_LABELS_TABLE);
		AppLog.d(LOG_TAG, (creation ? "Schema creation: " : "Schema upgrading: ") + NotesDatabaseAdapter.CREATE_LABELS_TABLE);
		AppLog.d(LOG_TAG, (creation ? "Schema creation: " : "Schema upgrading: ") + NotesDatabaseAdapter.CREATE_NOTES_LABELS_TABLE);
	}
}
