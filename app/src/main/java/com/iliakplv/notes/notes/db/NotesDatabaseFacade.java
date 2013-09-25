package com.iliakplv.notes.notes.db;

import android.util.Log;
import com.iliakplv.notes.BuildConfig;
import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.notes.AbstractNote;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: Ilya Kopylov
 * Date: 19.09.2013
 */
public class NotesDatabaseFacade {

	private static final String LOG_TAG = NotesDatabaseFacade.class.getSimpleName();
	private static NotesDatabaseFacade instance = new NotesDatabaseFacade();


	private List<NotesDatabaseEntry> notesDatabaseEntries;
	private volatile boolean entriesListActual = false;
	private List<DatabaseChangeListener> listeners;


	// Instance

	private NotesDatabaseFacade() {}

	public static NotesDatabaseFacade getInstance() {
		return instance;
	}


	// Transactions

	public synchronized List<NotesDatabaseEntry> getAllNotes() {
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Notes entries fetching. Entries list " + (entriesListActual ? "" : "NOT ") + "actual");
		}
		if (!entriesListActual) {
			final boolean emptyBeforeUpdate = notesDatabaseEntries != null && notesDatabaseEntries.isEmpty();
			notesDatabaseEntries =
					 (List<NotesDatabaseEntry>) performDatabaseTransaction(TransactionType.GetAllNotes, null);
			if (emptyBeforeUpdate && !notesDatabaseEntries.isEmpty()) {
				NotesApplication.onFirstNoteCreated();
			}
		}
		return notesDatabaseEntries;
	}

	public synchronized long insertNote(AbstractNote note) {
		return (Long) performDatabaseTransaction(TransactionType.InsertNote, note);
	}

	public synchronized boolean updateNote(int id, AbstractNote note) {
		return (Boolean) performDatabaseTransaction(TransactionType.UpdateNote, id, note);
	}

	public synchronized boolean deleteNote(int id) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteNote, id);
	}

	private Object performDatabaseTransaction(TransactionType transactionType, Object... args) {
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

		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Database transaction (" + transactionType.name() + ") performed");
		}

		entriesListActual = !isTransactionModifiesDatabase(transactionType);
		notifyListeners();

		return result;
	}


	// Listeners

	private void notifyListeners() {
		if (listeners != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (DatabaseChangeListener listener : listeners) {
						listener.onDatabaseChanged();
					}
				}
			}).start();
		}
	}

	public boolean addDatabaseChangeListener(DatabaseChangeListener listener) {
		if (listeners == null) {
			listeners = new LinkedList<DatabaseChangeListener>();
		}
		return listeners.add(listener);
	}

	public boolean removeDatabaseChangeListener(DatabaseChangeListener listener) {
		if (listeners != null) {
			return listeners.remove(listener);
		}
		return false;
	}


	// Other

	private static boolean isTransactionModifiesDatabase(TransactionType transactionType) {
		return transactionType != TransactionType.GetAllNotes;
	}


	/*********************************************
	 *
	 *            Inner classes
	 *
	 *********************************************/

	private static enum TransactionType {
		GetAllNotes,
		InsertNote,
		UpdateNote,
		DeleteNote
	}

	public interface DatabaseChangeListener {
		public void onDatabaseChanged();
	}

}
