package com.iliakplv.notes.notes.db;

import com.iliakplv.notes.notes.AbstractNote;

import java.util.List;

/**
 * Author: Ilya Kopylov
 * Date: 19.09.2013
 */
public class NotesDatabaseFacade {

	private NotesDatabaseFacade() {
		// only static usage allowed
	}


	public static List<NotesDatabaseEntry> getAllNotes() {
		return (List<NotesDatabaseEntry>) performDatabaseTransaction(TransactionType.GetAllNotes, null);
	}

	public static long insertNote(AbstractNote note) {
		return (Long) performDatabaseTransaction(TransactionType.InsertNote, note);
	}

	public static boolean updateNote(int id, AbstractNote note) {
		return (Boolean) performDatabaseTransaction(TransactionType.UpdateNote, id, note);
	}

	public static boolean deleteNote(int id) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteNote, id);
	}


	private static Object performDatabaseTransaction(TransactionType type, Object... args) {
		final NotesDatabaseAdapter adapter = new NotesDatabaseAdapter();
		adapter.open();
		Object result = null;
		switch (type) {
			case GetAllNotes:
				result = adapter.getAllNotes();
				break;
			case InsertNote:
				result = adapter.insertNote((AbstractNote) args[0]);
				break;
			case UpdateNote:
				result = adapter.updateNote((Integer) args[0], (AbstractNote) args[1]);
				break;
			case DeleteNote:
				result = adapter.deleteNote((Integer) args[0]);
				break;
			default:
				throw new IllegalArgumentException("Wrong DB transaction type");
		}
		adapter.close();
		return result;
	}


	private static enum TransactionType {
		GetAllNotes,
		InsertNote,
		UpdateNote,
		DeleteNote
	}

}
