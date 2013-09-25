package com.iliakplv.notes.notes.db;

import com.iliakplv.notes.notes.AbstractNote;

import java.util.List;

/**
 * Author: Ilya Kopylov
 * Date: 19.09.2013
 */
public class NotesDatabaseFacade {

	private static List<NotesDatabaseEntry> notesDatabaseEntries;
	private static volatile boolean entriesListActual = false;

	private NotesDatabaseFacade() {
		// only static usage allowed
	}


	public static synchronized List<NotesDatabaseEntry> getAllNotes() {
		if (!entriesListActual) {
			 notesDatabaseEntries =
					 (List<NotesDatabaseEntry>) performDatabaseTransaction(TransactionType.GetAllNotes, null);
			entriesListActual = true;
		}
		return notesDatabaseEntries;
	}

	public static synchronized long insertNote(AbstractNote note) {
		entriesListActual = false;
		return (Long) performDatabaseTransaction(TransactionType.InsertNote, note);
	}

	public static synchronized boolean updateNote(int id, AbstractNote note) {
		entriesListActual = false;
		return (Boolean) performDatabaseTransaction(TransactionType.UpdateNote, id, note);
	}

	public static synchronized boolean deleteNote(int id) {
		entriesListActual = false;
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteNote, id);
	}


	private static Object performDatabaseTransaction(TransactionType transactionType, Object... args) {
		final NotesDatabaseAdapter adapter = new NotesDatabaseAdapter();
		adapter.open();
		Object result = null;
		switch (transactionType) {
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
