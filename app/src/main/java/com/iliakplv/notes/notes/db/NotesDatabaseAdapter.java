package com.iliakplv.notes.notes.db;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.TextNote;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Ilya Kopylov
 * Date:  21.08.2013
 */
class NotesDatabaseAdapter {

	// Database
	private static final String DATABASE_NAME = "notes.db";
	private static final int CURRENT_VERSION = NotesDatabaseOpenHelper.DATABASE_VERSION_LABELS;

	static final int ALL_ENTRIES = 0;

	// Common keys
	private static final String KEY_ID = "_id";
	private static final int KEY_ID_COLUMN = 0;

	// Tables
	// Table: Notes
	private static final String NOTES_TABLE = "notes";
	private static final String NOTES_NAME = "name";
	private static final String NOTES_BODY = "body";
	private static final String NOTES_CREATE_DATE = "create_date";
	private static final String NOTES_CHANGE_DATE = "change_date";

	private static final int NOTES_NAME_COLUMN = 1;
	private static final int NOTES_BODY_COLUMN = 2;
	private static final int NOTES_CREATE_DATE_COLUMN = 3;
	private static final int NOTES_CHANGE_DATE_COLUMN = 4;

	private static final String[] NOTES_PROJECTION = {KEY_ID,
			NOTES_NAME, NOTES_BODY, NOTES_CREATE_DATE, NOTES_CHANGE_DATE};

	// Table: Labels
	private static final String LABELS_TABLE = "labels";
	private static final String LABELS_NAME = "name";
	private static final String LABELS_COLOR = "color";

	private static final int LABELS_NAME_COLUMN = 1;
	private static final int LABELS_COLOR_COLUMN = 2;

	private static final String[] LABELS_PROJECTION = {KEY_ID,
			LABELS_NAME, LABELS_COLOR};

	// Table: NotesLabels
	private static final String NOTES_LABELS_TABLE = "notes_labels";
	private static final String NOTES_LABELS_NOTE_ID = "note_id";
	private static final String NOTES_LABELS_LABEL_ID = "label_id";


	// Schema creation
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
					NOTES_LABELS_NOTE_ID + " INTEGER, " +
					NOTES_LABELS_LABEL_ID + " INTEGER, " +
					" FOREIGN KEY (" + NOTES_LABELS_NOTE_ID + ") REFERENCES " + NOTES_TABLE + " (" + KEY_ID + ")," +
					" FOREIGN KEY (" + NOTES_LABELS_LABEL_ID + ") REFERENCES " + LABELS_TABLE + " (" + KEY_ID + "));";

	private SQLiteDatabase db;
	private NotesDatabaseOpenHelper dbHelper;


	// Constructors

	NotesDatabaseAdapter() {
		dbHelper = new NotesDatabaseOpenHelper(DATABASE_NAME, null, CURRENT_VERSION);
	}


	// notes queries

	NotesDatabaseEntry<AbstractNote> getNote(int id) {
		final List<NotesDatabaseEntry<AbstractNote>> list = notesQuery(id);
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	List<NotesDatabaseEntry<AbstractNote>> getAllNotes() {
		return notesQuery(ALL_ENTRIES);
	}

	private List<NotesDatabaseEntry<AbstractNote>> notesQuery(int id) {
		Cursor cursor = db.query(NOTES_TABLE, NOTES_PROJECTION,
				whereClauseForId(id), null, null, null, null);

		List<NotesDatabaseEntry<AbstractNote>> result = new ArrayList<NotesDatabaseEntry<AbstractNote>>();

		if (cursor.moveToFirst()) {
			do {
				AbstractNote note = new TextNote(cursor.getString(NOTES_NAME_COLUMN),
						cursor.getString(NOTES_BODY_COLUMN));
				note.setCreateTime(new DateTime(cursor.getLong(NOTES_CREATE_DATE_COLUMN)));
				note.setChangeTime(new DateTime(cursor.getLong(NOTES_CHANGE_DATE_COLUMN)));
				NotesDatabaseEntry<AbstractNote> entry = new NotesDatabaseEntry<AbstractNote>(note, cursor.getInt(KEY_ID_COLUMN));
				result.add(entry);
			} while (cursor.moveToNext());
		}

		return result;
	}


	// notes data modification

	int insertNote(AbstractNote note) {
		return (int) db.insert(NOTES_TABLE, null, contentValuesForNote(note));
	}

	boolean updateNote(int id, AbstractNote note) {
		return db.update(NOTES_TABLE, contentValuesForNote(note), whereClauseForId(id), null) > 0;
	}

	boolean deleteNote(int id) {
		return db.delete(NOTES_TABLE, whereClauseForId(id), null) > 0;
	}


	// labels queries

	List<NotesDatabaseEntry<Label>> getAllLabels() {
		return labelsQuery(ALL_ENTRIES);
	}

	private List<NotesDatabaseEntry<Label>> labelsQuery(int id) {
		Cursor cursor = db.query(LABELS_TABLE, LABELS_PROJECTION,
				whereClauseForId(id), null, null, null, null);

		List<NotesDatabaseEntry<Label>> result = new ArrayList<NotesDatabaseEntry<Label>>();

		if (cursor.moveToFirst()) {
			do {
				Label label = new Label(cursor.getString(LABELS_NAME_COLUMN), cursor.getInt(LABELS_COLOR_COLUMN));
				NotesDatabaseEntry<Label> entry = new NotesDatabaseEntry<Label>(label, cursor.getInt(KEY_ID_COLUMN));
				result.add(entry);
			} while (cursor.moveToNext());
		}

		return result;
	}


	// labels data modification

	int insertLabel(Label label) {
		return (int) db.insert(LABELS_TABLE, null, contentValuesForLabel(label));
	}

	boolean updateLabel(int id, Label label) {
		return db.update(LABELS_TABLE, contentValuesForLabel(label), whereClauseForId(id), null) > 0;
	}

	boolean deleteLabel(int id) {
		return db.delete(LABELS_TABLE, whereClauseForId(id), null) > 0;
	}


	// notes_labels queries

	List<NotesDatabaseEntry<Label>> getLabelsForNote(int noteId) {
		final Cursor cursor = getLabelsForNoteCursor(noteId);
		List<NotesDatabaseEntry<Label>> result = new ArrayList<NotesDatabaseEntry<Label>>();
		if (cursor.moveToFirst()) {
			do {
				Label label = new Label(cursor.getString(LABELS_NAME_COLUMN), cursor.getInt(LABELS_COLOR_COLUMN));
				NotesDatabaseEntry<Label> entry = new NotesDatabaseEntry<Label>(label, cursor.getInt(KEY_ID_COLUMN));
				result.add(entry);
			} while (cursor.moveToNext());
		}
		return result;
	}

	Set<Integer> getLabelsIdsForNote(int noteId) {
		final Cursor cursor = getLabelsForNoteCursor(noteId);
		Set<Integer> result = new HashSet<Integer>();
		if (cursor.moveToFirst()) {
			do {
				result.add(cursor.getInt(KEY_ID_COLUMN));
			} while (cursor.moveToNext());
		}
		return result;
	}

	private Cursor getLabelsForNoteCursor(int noteId) {
		final String query = "SELECT " + projectionToString(LABELS_PROJECTION) +
				" FROM " + LABELS_TABLE + " WHERE " + KEY_ID +
				" IN (SELECT " + NOTES_LABELS_LABEL_ID + " FROM " + NOTES_LABELS_TABLE +
				" WHERE " + whereClause(NOTES_LABELS_NOTE_ID, noteId) + ")" +
				" ORDER BY " + KEY_ID + " DESC;";
		return db.rawQuery(query, null);
	}

	List<NotesDatabaseEntry<AbstractNote>> getNotesForLabel(int labelId) {
		final String query = "SELECT " + projectionToString(NOTES_PROJECTION) +
				" FROM " + NOTES_TABLE + " WHERE " + KEY_ID +
				" IN (SELECT " + NOTES_LABELS_NOTE_ID + " FROM " + NOTES_LABELS_TABLE +
				" WHERE " + whereClause(NOTES_LABELS_LABEL_ID, labelId) + ")" +
				" ORDER BY " + KEY_ID + " ASC;";
		Cursor cursor = db.rawQuery(query, null);

		List<NotesDatabaseEntry<AbstractNote>> result = new ArrayList<NotesDatabaseEntry<AbstractNote>>();

		if (cursor.moveToFirst()) {
			do {
				AbstractNote note = new TextNote(cursor.getString(NOTES_NAME_COLUMN),
						cursor.getString(NOTES_BODY_COLUMN));
				note.setCreateTime(new DateTime(cursor.getLong(NOTES_CREATE_DATE_COLUMN)));
				note.setChangeTime(new DateTime(cursor.getLong(NOTES_CHANGE_DATE_COLUMN)));
				NotesDatabaseEntry<AbstractNote> entry = new NotesDatabaseEntry<AbstractNote>(note, cursor.getInt(KEY_ID_COLUMN));
				result.add(entry);
			} while (cursor.moveToNext());
		}

		return result;
	}


	// notes_labels data modification
	// (no updates for current values, only insert and delete)

	int insertNoteLabel(int noteId, int labelId) {
		return (int) db.insert(NOTES_LABELS_TABLE, null, contentValuesForNoteLabel(noteId, labelId));
	}

	boolean deleteNoteLabel(int noteId, int labelId) {
		return db.delete(NOTES_LABELS_TABLE,
				whereClause(NOTES_LABELS_NOTE_ID, noteId) + " AND " + whereClause(NOTES_LABELS_LABEL_ID, labelId),
				null) > 0;
	}

	boolean deleteNoteLabelsForNote(int noteId) {
		return db.delete(NOTES_LABELS_TABLE,
				whereClause(NOTES_LABELS_NOTE_ID, noteId),
				null) > 0;
	}

	boolean deleteNoteLabelsForLabel(int labelId) {
		return db.delete(NOTES_LABELS_TABLE,
				whereClause(NOTES_LABELS_LABEL_ID, labelId),
				null) > 0;
	}


	// Util methods

	private static ContentValues contentValuesForNote(AbstractNote note) {
		final ContentValues cv = new ContentValues();
		cv.put(NOTES_NAME, note.getTitle());
		cv.put(NOTES_BODY, note.getBody());
		cv.put(NOTES_CREATE_DATE, note.getCreateTime().getMillis());
		cv.put(NOTES_CHANGE_DATE, note.getChangeTime().getMillis());
		return cv;
	}

	private static ContentValues contentValuesForLabel(Label label) {
		final ContentValues cv = new ContentValues();
		cv.put(LABELS_NAME, label.getName());
		cv.put(LABELS_COLOR, label.getColor());
		return cv;
	}

	private static ContentValues contentValuesForNoteLabel(int noteId, int labelId) {
		final ContentValues cv = new ContentValues();
		cv.put(NOTES_LABELS_NOTE_ID, noteId);
		cv.put(NOTES_LABELS_LABEL_ID, labelId);
		return cv;
	}


	private static String whereClause(String column, int id) {
		if (id == ALL_ENTRIES) {
			return null;
		} else if (id >= 1) {
			return column + "=" + id;
		}
		throw new IllegalArgumentException("Wrong id value: " + id);
	}

	private static String whereClauseForId(int id) {
		return whereClause(KEY_ID, id);
	}

	private static String projectionToString(String[] projection) {
		if (projection != null && projection.length > 0) {
			final int elements = projection.length;

			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < elements - 1; i++) {
				sb.append(projection[i]);
				sb.append(", ");
			}
			sb.append(projection[elements - 1]);

			return sb.toString();
		}
		return "";
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
