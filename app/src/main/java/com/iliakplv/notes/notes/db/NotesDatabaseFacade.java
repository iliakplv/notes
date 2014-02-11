package com.iliakplv.notes.notes.db;

import android.util.Log;
import com.iliakplv.notes.BuildConfig;
import com.iliakplv.notes.NotesApplication;
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

	public static final int ALL_LABELS = NotesDatabaseAdapter.ALL_ENTRIES;

	private static final int INVALID_ID = -1;

	// list cache
	private List<NotesDatabaseEntry> lastFetchedNotesList; // List<NotesDatabaseEntry<AbstractNote>>
	private volatile int lastFetchedNotesListLabelId = INVALID_ID;
	private volatile boolean lastFetchedNotesListActual = false;
	private volatile int lastFetchedNotesListSize = 0;

	// note cache
	private NotesDatabaseEntry<AbstractNote> lastFetchedNoteEntry;
	private volatile int lastFetchedNoteEntryId = INVALID_ID;
	private volatile boolean lastFetchedNoteEntryActual = false;

	// listeners
	private List<DatabaseChangeListener> databaseListeners;
	private List<NoteChangeListener> noteListeners;


	private NotesDatabaseFacade() {}

	public static NotesDatabaseFacade getInstance() {
		return instance;
	}


	// notes

	public NotesDatabaseEntry<AbstractNote> getNote(int id) {
		final boolean needToRefresh = lastFetchedNoteEntryId != id || !lastFetchedNoteEntryActual;
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Note entry fetching (id=" + id + "). Cached entry " +
					(needToRefresh ? "NOT " : "") + "actual");
		}
		if (needToRefresh) {
			lastFetchedNoteEntry = (NotesDatabaseEntry<AbstractNote>) performDatabaseTransaction(TransactionType.GetNote, id);
			lastFetchedNoteEntryId = id;
			lastFetchedNoteEntryActual = true;
		}
		return lastFetchedNoteEntry;
	}

	public List<NotesDatabaseEntry> getNotesForLabel(int labelId) {
		final boolean needToRefresh = lastFetchedNotesListLabelId != labelId || !lastFetchedNotesListActual;
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, "Notes entries fetching (labelId=" + labelId + "). Cached entries list " +
					(needToRefresh ? "NOT " : "") + "actual");
		}
		if (needToRefresh) {
			final TransactionType selectTransaction =
					labelId == ALL_LABELS ? TransactionType.GetAllNotes : TransactionType.GetNotesForLabel;
			lastFetchedNotesList =
					(List<NotesDatabaseEntry>) performDatabaseTransaction(selectTransaction, labelId);
			lastFetchedNotesListLabelId = labelId;
			lastFetchedNotesListSize = lastFetchedNotesList.size();
			lastFetchedNotesListActual = true;
		}
		return lastFetchedNotesList;
	}

	public int getNotesForLabelCount(int labelId) {
		final boolean needToRefresh = lastFetchedNotesListLabelId != labelId || !lastFetchedNotesListActual;
		if (needToRefresh) {
			getNotesForLabel(labelId);
		}
		return lastFetchedNotesListSize;
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

	public synchronized int insertLabelToNote(int noteId, int labelId) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertLabelToNote, noteId, labelId);
	}

	public synchronized boolean deleteLabelFromNote(int noteId, int labelId) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteLabelFromNote, noteId, labelId);
	}


	private Object performDatabaseTransaction(TransactionType transactionType, Object... args) {
		final NotesDatabaseAdapter adapter = new NotesDatabaseAdapter();
		adapter.open();

		Object result;
		int noteId = INVALID_ID;
		int labelId;

		switch (transactionType) {
			case GetNote:
				noteId = (Integer) args[0];
				result = adapter.getNote(noteId);
				break;
			case GetAllNotes:
				result = adapter.getAllNotes();
				break;
			case InsertNote:
				result = adapter.insertNote((AbstractNote) args[0]);
				break;
			case UpdateNote:
				noteId = (Integer) args[0];
				result = adapter.updateNote(noteId, (AbstractNote) args[1]);
				break;
			case DeleteNote:
				noteId = (Integer) args[0];
				adapter.deleteNoteLabelsForNote(noteId);
				result = adapter.deleteNote(noteId);
				break;

			case GetAllLabels:
				result = adapter.getAllLabels();
				break;
			case InsertLabel:
				result = adapter.insertLabel((Label) args[0]);
				break;
			case UpdateLabel:
				labelId = (Integer) args[0];
				result = adapter.updateLabel(labelId, (Label) args[1]);
				break;
			case DeleteLabel:
				labelId = (Integer) args[0];
				adapter.deleteNoteLabelsForLabel(labelId);
				result = adapter.deleteLabel(labelId);
				break;

			case GetLabelsForNote:
				noteId = (Integer) args[0];
				result = adapter.getLabelsForNote(noteId);
				break;
			case GetNotesForLabel:
				labelId = (Integer) args[0];
				result = adapter.getNotesForLabel(labelId);
				break;
			case InsertLabelToNote:
				noteId = (Integer) args[0];
				labelId = (Integer) args[1];
				result = adapter.insertNoteLabel(noteId, labelId);
				break;
			case DeleteLabelFromNote:
				noteId = (Integer) args[0];
				labelId = (Integer) args[1];
				result = adapter.deleteNoteLabel(noteId, labelId);
				break;

			default:
				throw new IllegalArgumentException("Wrong transaction type: " + transactionType.name());
		}
		adapter.close();
		onTransactionPerformed(transactionType, noteId);
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
			if (changedNoteId != INVALID_ID) {
				lastFetchedNoteEntryActual = lastFetchedNoteEntryActual &&
						lastFetchedNoteEntryId != changedNoteId;
			}
			notifyNoteListeners(changedNoteId);
		}
		if (databaseModificationTransaction(transactionType)) {
			lastFetchedNotesListActual = false;
			notifyDatabaseListeners();
		}

	}

	// Listeners

	private void notifyDatabaseListeners() {
		if (databaseListeners != null) {
			NotesApplication.executeInBackground(new Runnable() {
				@Override
				public void run() {
					for (DatabaseChangeListener listener : databaseListeners) {
						listener.onDatabaseChanged();
					}
				}
			});
		}
	}

	private void notifyNoteListeners(final int changedNoteId) {
		if (noteListeners != null) {
			NotesApplication.executeInBackground(new Runnable() {
				@Override
				public void run() {
					for (NoteChangeListener listener : noteListeners) {
						if (changedNoteId == INVALID_ID || listener.getNoteId() == changedNoteId) {
							listener.onNoteChanged();
						}
					}
				}
			});
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
			case InsertNote:
			case UpdateNote:
			case DeleteNote:

			case InsertLabel:
			case UpdateLabel:
			case DeleteLabel:

			case InsertLabelToNote:
			case DeleteLabelFromNote:
				return true;
		}
		return false;
	}

	private static boolean existingNoteModificationTransaction(TransactionType transactionType) {
		switch (transactionType) {
			case UpdateNote:
			case DeleteNote:

			case UpdateLabel:
			case DeleteLabel:

			case InsertLabelToNote:
			case DeleteLabelFromNote:
				return true;
		}
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
