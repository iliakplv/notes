package com.iliakplv.notes.notes.db;

import android.util.Pair;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.utils.AppLog;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class NotesDatabaseFacade {

	private static final String LOG_TAG = NotesDatabaseFacade.class.getSimpleName();
	private static NotesDatabaseFacade instance = new NotesDatabaseFacade();

	public static final int ALL_LABELS = NotesDatabaseAdapter.ALL_ENTRIES;
	private static final int INVALID_ID = -1;


	// list cache
	private List<NotesDatabaseEntry> notesListCache;
	private volatile int notesListCacheLabelId = INVALID_ID;
	private volatile boolean notesListCacheActual = false;
	private volatile int notesListCacheSize = 0;

	// list sort
	private volatile NotesUtils.NoteSortOrder notesSortOrder = NotesUtils.NoteSortOrder.Title;

	// note cache
	private NotesDatabaseEntry<AbstractNote> noteCache;
	private volatile int noteCacheNoteId = INVALID_ID;
	private volatile boolean noteCacheActual = false;

	// listeners
	private List<DatabaseChangeListener> databaseListeners;


	private NotesDatabaseFacade() {}
	public static NotesDatabaseFacade getInstance() {
		return instance;
	}


	// notes

	public boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder) {
		boolean orderChanged = this.notesSortOrder != notesSortOrder;
		if (orderChanged) {
			this.notesSortOrder = notesSortOrder;
			notesListCacheActual = false;
			notifyDatabaseListeners();
		}
		return orderChanged;
	}

	public NotesDatabaseEntry<AbstractNote> getNote(int id) {
		refreshNoteCacheIfNeeded(id);
		return noteCache;
	}

	private void refreshNoteCacheIfNeeded(int noteId) {
		final boolean needToRefresh = noteCacheNoteId != noteId || !noteCacheActual;
		AppLog.d(LOG_TAG, "Note entry refresh (id=" + noteId + "). Cached entry " +
				(needToRefresh ? "NOT " : "") + "actual");
		if (needToRefresh) {
			noteCache = (NotesDatabaseEntry<AbstractNote>) performDatabaseTransaction(TransactionType.GetNote, noteId);
			noteCacheNoteId = noteId;
			noteCacheActual = true;
		}
	}

	private void refreshNotesListCacheIfNeeded(int labelId) {
		final boolean needToRefresh = notesListCacheLabelId != labelId || !notesListCacheActual;
		AppLog.d(LOG_TAG, "Notes entries refresh (labelId=" + labelId + "). Cached entries list " +
				(needToRefresh ? "NOT " : "") + "actual");
		if (needToRefresh) {
			final TransactionType selectTransaction =
					labelId == ALL_LABELS ? TransactionType.GetAllNotes : TransactionType.GetNotesForLabel;
			notesListCache =
					(List<NotesDatabaseEntry>) performDatabaseTransaction(selectTransaction, labelId);
			notesListCacheLabelId = labelId;
			notesListCacheSize = notesListCache.size();
			notesListCacheActual = true;
		}
	}

	public List<NotesDatabaseEntry> getNotesForLabel(int labelId) {
		refreshNotesListCacheIfNeeded(labelId);
		return notesListCache;
	}

	public int getNotesForLabelCount(int labelId) {
		refreshNotesListCacheIfNeeded(labelId);
		return notesListCacheSize;
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

	public NotesDatabaseEntry<Label> getLabel(int id) {
		return (NotesDatabaseEntry<Label>) performDatabaseTransaction(TransactionType.GetLabel, id);
	}

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

	public Set<Pair<Integer, Integer>> getAllNotesLabelsIds() {
		return (Set<Pair<Integer, Integer>>) performDatabaseTransaction(TransactionType.GetAllNotesLabelsIds);
	}

	public List<NotesDatabaseEntry<Label>> getLabelsForNote(int noteId) {
		return (List<NotesDatabaseEntry<Label>>) performDatabaseTransaction(TransactionType.GetLabelsForNote, noteId);
	}

	public Set<Integer> getLabelsIdsForNote(int noteId) {
		return (Set<Integer>) performDatabaseTransaction(TransactionType.GetLabelsIdsForNote, noteId);
	}

	public synchronized int insertLabelToNote(int noteId, int labelId) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertLabelToNote, noteId, labelId);
	}

	public synchronized boolean deleteLabelFromNote(int noteId, int labelId) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteLabelFromNote, noteId, labelId);
	}


	private Object performDatabaseTransaction(TransactionType transactionType, Object... args) {
		Object result;
		int noteId = 0;
		int labelId = 0;

		final NotesDatabaseAdapter adapter = new NotesDatabaseAdapter();
		adapter.open();
		switch (transactionType) {
			case GetNote:
				noteId = (Integer) args[0];
				result = adapter.getNote(noteId);
				break;
			case GetAllNotes:
				result = adapter.getAllNotes(notesSortOrder);
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

			case GetLabel:
				labelId = (Integer) args[0];
				result = adapter.getLabel(labelId);
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

			case GetAllNotesLabelsIds:
				result = adapter.getAllNotesLabelsIds();
				break;
			case GetLabelsForNote:
				noteId = (Integer) args[0];
				result = adapter.getLabelsForNote(noteId);
				break;
			case GetLabelsIdsForNote:
				noteId = (Integer) args[0];
				result = adapter.getLabelsIdsForNote(noteId);
				break;
			case GetNotesForLabel:
				labelId = (Integer) args[0];
				result = adapter.getNotesForLabel(labelId, notesSortOrder);
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

		onTransactionPerformed(transactionType, noteId, labelId);
		return result;
	}

	private void onTransactionPerformed(TransactionType transactionType, int noteId, int labelId) {
		AppLog.d(LOG_TAG, "Database transaction (" + transactionType.name() + ") performed");

		if (databaseModificationTransaction(transactionType)) {
			notesListCacheActual = false;
			notifyDatabaseListeners();
		}

		if (noteCacheNoteId == noteId && noteModificationTransaction(transactionType)) {
			noteCacheActual = false;
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

	public boolean addDatabaseChangeListener(DatabaseChangeListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		if (databaseListeners == null) {
			databaseListeners = new LinkedList<DatabaseChangeListener>();
		}
		return databaseListeners.add(listener);
	}

	public boolean removeDatabaseChangeListener(DatabaseChangeListener listener) {
		if (databaseListeners != null) {
			return databaseListeners.remove(listener);
		}
		return false;
	}

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

	private static boolean noteModificationTransaction(TransactionType transactionType) {
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

		GetLabel,
		GetAllLabels,
		InsertLabel,
		UpdateLabel,
		DeleteLabel,

		GetAllNotesLabelsIds,
		GetLabelsForNote,
		GetLabelsIdsForNote,
		GetNotesForLabel,
		InsertLabelToNote,
		DeleteLabelFromNote
	}

	public interface DatabaseChangeListener {

		/**
		 * Callback for notes database changing
		 * Called after transaction that affects database entries (insert, update or delete) had performed
		 * Called from background thread. If you want to refresh UI in this method perform it on UI thread!
		 */
		public void onDatabaseChanged();
	}

}
