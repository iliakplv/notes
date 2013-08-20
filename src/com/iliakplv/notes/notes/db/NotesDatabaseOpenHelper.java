package com.iliakplv.notes.notes.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.iliakplv.notes.utils.Utils;

/**
 * Author: Ilya Kopylov
 * Date:  21.08.2013
 */
public class NotesDatabaseOpenHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = NotesDatabaseOpenHelper.class.getSimpleName();

	private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

	public NotesDatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(NotesDatabaseAdapter.CREATE_SCHEME_COMMAND);
		if (Utils.DEBUG) {
			Log.d(LOG_TAG, "Database created with command: " + NotesDatabaseAdapter.CREATE_SCHEME_COMMAND);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO implement
		if (Utils.DEBUG) {
			Log.d(LOG_TAG, "Upgrading version " + oldVersion + " to " + newVersion);
		}
		db.execSQL(DROP_TABLE_IF_EXISTS + NotesDatabaseAdapter.TABLE_NOTES);
		onCreate(db);
	}
}
