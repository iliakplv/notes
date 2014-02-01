package com.iliakplv.notes.notes.db;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.TextNote;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ilya Kopylov
 * Date:  21.08.2013
 */
class NotesDatabaseAdapter {

	// Database
	private static final String DATABASE_NAME = "notes.db";

	private static final int CURRENT_VERSION = NotesDatabaseOpenHelper.DATABASE_VERSION_LABELS;

	private static final int ALL_NOTES = 0;

	// Common keys
	private static final String KEY_ID = "_id";
	private static final int KEY_ID_COLUMN = 0;

	// Tables
	// Table: Notes
	private static final String NOTES_TABLE = "notes";
	private static final int NOTES_NAME_COLUMN = 1;
	private static final String NOTES_NAME = "name";
	private static final int NOTES_BODY_COLUMN = 2;
	private static final String NOTES_BODY = "body";
	private static final int NOTES_CREATE_DATE_COLUMN = 3;
	private static final String NOTES_CREATE_DATE = "create_date";
	private static final int NOTES_CHANGE_DATE_COLUMN = 4;
	private static final String NOTES_CHANGE_DATE = "change_date";

	// Table: Labels
	private static final String LABELS_TABLE = "labels";
	private static final int LABELS_NAME_COLUMN = 1;
	private static final String LABELS_NAME = "name";
	private static final int LABELS_COLOR_COLUMN = 2;
	private static final String LABELS_COLOR = "color";

	// Table: NotesLabels
	private static final String NOTES_LABELS_TABLE = "notes_labels";
	private static final int NOTES_LABELS_NOTE_COLUMN = 1;
	private static final String NOTES_LABELS_NOTE = "note";
	private static final int NOTES_LABELS_LABEL_COLUMN = 2;
	private static final String NOTES_LABELS_LABEL = "label";


	// Scheme creation
	static final String CREATE_NOTES_TABLE =
			"CREATE TABLE " + NOTES_TABLE +
					" (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					NOTES_NAME + " TEXT NOT NULL, " +
					NOTES_BODY + " TEXT NOT NULL, " +
					NOTES_CREATE_DATE + " LONG, " +
					NOTES_CHANGE_DATE + " LONG);";

	static final String CREATE_LABELS_TABLE =
			"CREATE TABLE " + LABELS_TABLE +
					" (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					LABELS_NAME + " TEXT NOT NULL, " +
					LABELS_COLOR + " INTEGER);";

	static final String CREATE_NOTES_LABELS_TABLE =
			"CREATE TABLE " + NOTES_LABELS_TABLE +
					" (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					NOTES_LABELS_NOTE + " INTEGER, " +
					NOTES_LABELS_LABEL + " INTEGER, " +
					" FOREIGN KEY (" + NOTES_LABELS_NOTE + ") REFERENCES " + NOTES_TABLE + " (" + KEY_ID + ")," +
					" FOREIGN KEY (" + NOTES_LABELS_LABEL + ") REFERENCES " + LABELS_TABLE + " (" + KEY_ID + "));";

	private SQLiteDatabase db;
	private NotesDatabaseOpenHelper dbHelper;


	// Constructors

	NotesDatabaseAdapter() {
		dbHelper = new NotesDatabaseOpenHelper(DATABASE_NAME, null, CURRENT_VERSION);
	}


	// Queries

	NotesDatabaseEntry getNote(int id) {
		final List<NotesDatabaseEntry> list = getNotes(id);
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	List<NotesDatabaseEntry> getAllNotes() {
		return getNotes(ALL_NOTES);
	}

	private List<NotesDatabaseEntry> getNotes(int id) {
		Cursor cursor = db.query(NOTES_TABLE,
				new String[]{KEY_ID, NOTES_NAME, NOTES_BODY, NOTES_CREATE_DATE, NOTES_CHANGE_DATE},
				whereClauseForId(id), null, null, null, null);

		List<NotesDatabaseEntry> result = new ArrayList<NotesDatabaseEntry>();

		if (cursor.moveToFirst()) {
			do {
				AbstractNote note = new TextNote(cursor.getString(NOTES_NAME_COLUMN),
						cursor.getString(NOTES_BODY_COLUMN));
				note.setCreateTime(new DateTime(cursor.getLong(NOTES_CREATE_DATE_COLUMN)));
				note.setChangeTime(new DateTime(cursor.getLong(NOTES_CHANGE_DATE_COLUMN)));
				NotesDatabaseEntry entry = new NotesDatabaseEntry(note, cursor.getInt(KEY_ID_COLUMN));
				result.add(entry);
			} while (cursor.moveToNext());
		}

		return result;
	}


	// Data modification

	int insertNote(AbstractNote note) {
		return (int) db.insert(NOTES_TABLE, null, contentValuesForNote(note));
	}

	boolean updateNote(int id, AbstractNote note) {
		return db.update(NOTES_TABLE, contentValuesForNote(note), whereClauseForId(id), null) > 0;
	}

	boolean deleteNote(int id) {
		return db.delete(NOTES_TABLE, whereClauseForId(id), null) > 0;
	}


	// Util methods

	private static ContentValues contentValuesForNote(AbstractNote note) {
		ContentValues cv = new ContentValues();
		cv.put(NOTES_NAME, note.getTitle());
		cv.put(NOTES_BODY, note.getBody());
		cv.put(NOTES_CREATE_DATE, note.getCreateTime().getMillis());
		cv.put(NOTES_CHANGE_DATE, note.getChangeTime().getMillis());
		return cv;
	}

	private static String whereClauseForId(int id) {
		if (id == ALL_NOTES) {
			return null;
		} else if (id >= 1) {
			return KEY_ID + "=" + id;
		}
		throw new IllegalArgumentException("Wrong id value: " + id);
	}


	// Database open and close

	void open() {
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			db = dbHelper.getReadableDatabase();
		}
	}

	void close() {
		if (db != null) {
			db.close();
		}
	}

}
