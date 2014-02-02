package com.iliakplv.notes.notes.db;

import android.util.Log;
import com.iliakplv.notes.BuildConfig;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: Ilya Kopylov
 * Date: 19.09.2013
 */
public class NotesDatabaseFacade {

	private static final String LOG_TAG = NotesDatabaseFacade.class.getSimpleName();
	private static NotesDatabaseFacade instance = new NotesDatabaseFacade();


	private List<NotesDatabaseEntry<AbstractNote>> notesDatabaseEntries;
	private volatile boolean entriesListActual = false;
	private volatile int entriesListSize = -1;

	private NotesDatabaseEntry<AbstractNote> lastFetchedEntry;
	private volatile int lastFetchedEntryId = 0;
	private volatile boolean lastFetchedEntryActual = false;

	private List<DatabaseChangeListener> databaseListeners;
	private List<NoteChangeListener> noteListeners;


	// Instance

	private NotesDatabaseFacade() {}

	public static NotesDatabaseFacade getInstance() {
		return instance;
	}


	// notes

	public NotesDatabaseEntry<AbstractNote> getNote(int id) {
		final boolean needToRefresh = lastFetchedEntryId != id || !lastFetchedEntryActual;
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Note entry fetching (id=" + id + "). Cached entry " +
					(needToRefresh ? "NOT " : "") + "actual");
		}
		if (needToRefresh) {
			lastFetchedEntry = (NotesDatabaseEntry<AbstractNote>) performDatabaseTransaction(TransactionType.GetNote, id);
			lastFetchedEntryId = id;
			lastFetchedEntryActual = true;
		}
		return lastFetchedEntry;
	}

	public List<NotesDatabaseEntry<AbstractNote>> getAllNotes() {
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Notes entries fetching. Cached entries list " +
					(entriesListActual ? "" : "NOT ") + "actual");
		}
		if (!entriesListActual) {
			notesDatabaseEntries =
					 (List<NotesDatabaseEntry<AbstractNote>>) performDatabaseTransaction(TransactionType.GetAllNotes, null);
			entriesListSize = notesDatabaseEntries.size();
			entriesListActual = true;
		}
		return notesDatabaseEntries;
	}

	public int getNotesCount() {
		if (entriesListSize < 0) {
			getAllNotes();
		}
		return entriesListSize;
	}

	public synchronized int insertNote(AbstractNote note) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertNote, note);
	}

	public synchronized boolean updateNote(int id, AbstractNote note) {
		return (Boolean) performDatabaseTransaction(TransactionType.UpdateNote, id, note);
	}

	public synchronized boolean deleteNote(int id) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteNote, id);
	}


	// labels

	public List<NotesDatabaseEntry<Label>> getAllLabels() {
		return (List<NotesDatabaseEntry<Label>>) performDatabaseTransaction(TransactionType.GetAllLabels);
	}

	public synchronized int insertLabel(Label label) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertLabel, label);
	}

	public synchronized boolean updateLabel(int id, Label label) {
		return (Boolean) performDatabaseTransaction(TransactionType.UpdateLabel, id, label);
	}

	public synchronized boolean deleteLabel(int id) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteLabel, id);
	}


	// notes_labels

	public List<NotesDatabaseEntry<Label>> getLabelsForNote(int noteId) {
		return (List<NotesDatabaseEntry<Label>>) performDatabaseTransaction(TransactionType.GetLabelsForNote, noteId);
	}

	public List<NotesDatabaseEntry<AbstractNote>> getNotesForLabel(int labelId) {
		return (List<NotesDatabaseEntry<AbstractNote>>) performDatabaseTransaction(TransactionType.GetNotesForLabel, labelId);
	}

	public synchronized int insertLabelToNote(int noteId, int labelId) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertLabelToNote, noteId, labelId);
	}

	public synchronized boolean deleteLabelFromNote(int id) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteLabelFromNote, id);
	}


	private Object performDatabaseTransaction(TransactionType transactionType, Object... args) {
		final NotesDatabaseAdapter adapter = new NotesDatabaseAdapter();
		adapter.open();
		Object result = null;
		int id = 0;
		switch (transactionType) {
			case GetNote:
				id = (Integer) args[0];
				result = adapter.getNote(id);
				break;
			case GetAllNotes:
				result = adapter.getAllNotes();
				break;
			case InsertNote:
				result = adapter.insertNote((AbstractNote) args[0]);
				entriesListSize++;
				break;
			case UpdateNote:
				id = (Integer) args[0];
				result = adapter.updateNote(id, (AbstractNote) args[1]);
				break;
			case DeleteNote:
				id = (Integer) args[0];
				final List<NotesDatabaseEntry<Label>> labelsForNote = adapter.getLabelsForNote(id);
				for (NotesDatabaseEntry<Label> entry : labelsForNote) {
					adapter.deleteNoteLabel(entry.getId());
				}
				result = adapter.deleteNote(id);
				entriesListSize--;
				break;

			case GetAllLabels:
				result = adapter.getAllLabels();
				break;
			case InsertLabel:
				result = adapter.insertLabel((Label) args[0]);
				break;
			case UpdateLabel:
				id = (Integer) args[0];
				result = adapter.updateLabel(id, (Label) args[1]);
				break;
			case DeleteLabel:
				id = (Integer) args[0];
				final List<NotesDatabaseEntry<AbstractNote>> notesForLabel = adapter.getNotesForLabel(id);
				for (NotesDatabaseEntry<AbstractNote> entry : notesForLabel) {
					adapter.deleteNoteLabel(entry.getId());
				}
				result = adapter.deleteLabel(id);
				break;

			case GetLabelsForNote:
				id = (Integer) args[0];
				result = adapter.getLabelsForNote(id);
				break;
			case GetNotesForLabel:
				id = (Integer) args[0];
				result = adapter.getNotesForLabel(id);
				break;
			case InsertLabelToNote:
				int noteId = (Integer) args[0];
				int labelId = (Integer) args[1];
				result = adapter.insertNoteLabel(noteId, labelId);
				break;
			case DeleteLabelFromNote:
				id = (Integer) args[0];
				result = adapter.deleteNoteLabel(id);
				break;

			default:
				throw new IllegalArgumentException("Wrong transaction type: " + transactionType.name());
		}
		adapter.close();
		onTransactionPerformed(transactionType, id);
		return result;
	}

	private void onTransactionPerformed(TransactionType transactionType, int changedNoteId) {
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Database transaction (" + transactionType.name() +") performed");
		}
		if (existingNoteModificationTransaction(transactionType)) {
			if (BuildConfig.DEBUG) {
				Log.d(LOG_TAG, "Changed note id=" + changedNoteId);
			}
			lastFetchedEntryActual = lastFetchedEntryId != changedNoteId;
			notifyNoteListeners(changedNoteId);
		}
		if (databaseModificationTransaction(transactionType)) {
			entriesListActual = false;
			notifyDatabaseListeners();
		}

	}

	// Listeners

	private void notifyDatabaseListeners() {
		if (databaseListeners != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (DatabaseChangeListener listener : databaseListeners) {
						listener.onDatabaseChanged();
					}
				}
			}).start();
		}
	}

	private void notifyNoteListeners(final int changedNoteId) {
		if (noteListeners != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (NoteChangeListener listener : noteListeners) {
						if (listener.getNoteId() == changedNoteId) {
							listener.onNoteChanged();
						}
					}
				}
			}).start();
		}
	}

	public boolean addDatabaseChangeListener(DatabaseChangeListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		if (databaseListeners == null) {
			databaseListeners = new LinkedList<DatabaseChangeListener>();
		}
		return databaseListeners.add(listener);
	}

	public boolean addNoteChangeListener(NoteChangeListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		if (noteListeners == null) {
			noteListeners = new LinkedList<NoteChangeListener>();
		}
		return noteListeners.add(listener);
	}

	public boolean removeDatabaseChangeListener(DatabaseChangeListener listener) {
		if (databaseListeners != null) {
			return databaseListeners.remove(listener);
		}
		return false;
	}

	public boolean removeNoteChangeListener(NoteChangeListener listener) {
		if (noteListeners != null) {
			return noteListeners.remove(listener);
		}
		return false;
	}


	// Other

	private static boolean databaseModificationTransaction(TransactionType transactionType) {
		switch (transactionType) {
			case GetNote:
			case GetAllNotes:
				return false;

			case InsertNote:
			case UpdateNote:
			case DeleteNote:
				return true;
		}
//		throw new IllegalArgumentException("Unknown transaction type: " + transactionType.name());
		return false;
	}

	private static boolean existingNoteModificationTransaction(TransactionType transactionType) {
		switch (transactionType) {
			case GetNote:
			case GetAllNotes:
			case InsertNote:
				return false;

			case UpdateNote:
			case DeleteNote:
				return true;
		}
//		throw new IllegalArgumentException("Unknown transaction type: " + transactionType.name());
		return false;
	}


	/*********************************************
	 *
	 *            Inner classes
	 *
	 *********************************************/

	private static enum TransactionType {
		GetNote,
		GetAllNotes,
		InsertNote,
		UpdateNote,
		DeleteNote,

		GetAllLabels,
		InsertLabel,
		UpdateLabel,
		DeleteLabel,

		GetLabelsForNote,
		GetNotesForLabel,
		InsertLabelToNote,
		DeleteLabelFromNote
	}

	public interface DatabaseChangeListener {

		/**
		 * Callback for notes database changing
		 * Called after transaction that affects database entries (insert, update or delete) was performed
		 * Called from background thread. If you want to refresh UI in this method do it on UI thread!
		 */
		public void onDatabaseChanged();
	}

	public interface NoteChangeListener {

		/**
		 * Callback for existing note changing
		 * Called after changing note that this listener watching
		 * Called from background thread. If you want to refresh UI in this method do it on UI thread!
		 */
		public void onNoteChanged();

		/**
		 * @return id of note which this listener watching
		 */
		public int getNoteId();
	}

}
