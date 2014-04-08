package com.iliakplv.notes.notes.db;

import android.util.Pair;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.NotesStorageListener;
import com.iliakplv.notes.utils.AppLog;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class NotesDatabaseStorage implements NotesStorage {

	private static final String LOG_TAG = NotesDatabaseStorage.class.getSimpleName();
	private static final int INVALID_ID = -1;


	// list cache
	private List<AbstractNote> notesListCache;
	private volatile int notesListCacheLabelId = INVALID_ID;
	private volatile boolean notesListCacheActual = false;
	private volatile int notesListCacheSize = 0;

	// list sort
	private volatile NotesUtils.NoteSortOrder notesSortOrder = NotesUtils.NoteSortOrder.Title;

	// note cache
	private AbstractNote noteCache;
	private volatile int noteCacheNoteId = INVALID_ID;
	private volatile boolean noteCacheActual = false;

	// listeners
	private List<NotesStorageListener> databaseListeners;


	// notes

	@Override
	public boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder) {
		boolean orderChanged = this.notesSortOrder != notesSortOrder;
		if (orderChanged) {
			this.notesSortOrder = notesSortOrder;
			notesListCacheActual = false;
			notifyDatabaseListeners();
		}
		return orderChanged;
	}

	@Override
	public AbstractNote getNote(int id) {
		refreshNoteCacheIfNeeded(id);
		return noteCache;
	}

	private void refreshNoteCacheIfNeeded(int noteId) {
		final boolean needToRefresh = noteCacheNoteId != noteId || !noteCacheActual;
		AppLog.d(LOG_TAG, "Note entry refresh (id=" + noteId + "). Cached entry " +
				(needToRefresh ? "NOT " : "") + "actual");
		if (needToRefresh) {
			noteCache = (AbstractNote) performDatabaseTransaction(TransactionType.GetNote, noteId);
			noteCacheNoteId = noteId;
			noteCacheActual = true;
		}
	}

	private void refreshNotesListCacheIfNeeded(int labelId) {
		final boolean needToRefresh = notesListCacheLabelId != labelId || !notesListCacheActual;
		AppLog.d(LOG_TAG, "Notes entries refresh (labelId=" + labelId + "). Cached entries list " +
				(needToRefresh ? "NOT " : "") + "actual");
		if (needToRefresh) {
			final TransactionType selectTransaction = labelId == NOTES_FOR_ALL_LABELS ?
							TransactionType.GetAllNotes :
							TransactionType.GetNotesForLabel;
			notesListCache =
					(List<AbstractNote>) performDatabaseTransaction(selectTransaction, labelId);
			notesListCacheLabelId = labelId;
			notesListCacheSize = notesListCache.size();
			notesListCacheActual = true;
		}
	}

	@Override
	public List<AbstractNote> getNotesForLabel(int labelId) {
		refreshNotesListCacheIfNeeded(labelId);
		return notesListCache;
	}

	@Override
	public int getNotesForLabelCount(int labelId) {
		refreshNotesListCacheIfNeeded(labelId);
		return notesListCacheSize;
	}

	@Override
	public synchronized int insertNote(AbstractNote note) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertNote, note);
	}

	@Override
	public synchronized boolean updateNote(int id, AbstractNote note) {
		return (Boolean) performDatabaseTransaction(TransactionType.UpdateNote, id, note);
	}

	@Override
	public synchronized boolean deleteNote(int id) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteNote, id);
	}


	// labels

	@Override
	public Label getLabel(int id) {
		return (Label) performDatabaseTransaction(TransactionType.GetLabel, id);
	}

	@Override
	public List<Label> getAllLabels() {
		return (List<Label>) performDatabaseTransaction(TransactionType.GetAllLabels);
	}

	@Override
	public synchronized int insertLabel(Label label) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertLabel, label);
	}

	@Override
	public synchronized boolean updateLabel(int id, Label label) {
		return (Boolean) performDatabaseTransaction(TransactionType.UpdateLabel, id, label);
	}

	@Override
	public synchronized boolean deleteLabel(int id) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteLabel, id);
	}


	// notes_labels

	@Override
	public Set<Pair<Integer, Integer>> getAllNotesLabelsIds() {
		return (Set<Pair<Integer, Integer>>) performDatabaseTransaction(TransactionType.GetAllNotesLabelsIds);
	}

	@Override
	public List<Label> getLabelsForNote(int noteId) {
		return (List<Label>) performDatabaseTransaction(TransactionType.GetLabelsForNote, noteId);
	}

	@Override
	public Set<Integer> getLabelsIdsForNote(int noteId) {
		return (Set<Integer>) performDatabaseTransaction(TransactionType.GetLabelsIdsForNote, noteId);
	}

	@Override
	public synchronized int insertLabelToNote(int noteId, int labelId) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertLabelToNote, noteId, labelId);
	}

	@Override
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
					for (NotesStorageListener listener : databaseListeners) {
						listener.onContentChanged();
					}
				}
			});
		}
	}

	@Override
	public boolean addStorageListener(NotesStorageListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		if (databaseListeners == null) {
			databaseListeners = new LinkedList<NotesStorageListener>();
		}
		return databaseListeners.add(listener);
	}

	@Override
	public boolean removeStorageListener(NotesStorageListener listener) {
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

}
