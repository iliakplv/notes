package com.iliakplv.notes.notes.db;

import android.util.Pair;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.NotesStorageListener;
import com.iliakplv.notes.utils.AppLog;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class NotesDatabaseStorage implements NotesStorage {

	private static final String LOG_TAG = NotesDatabaseStorage.class.getSimpleName();
	private static final Integer INVALID_ID = -1;


	// list cache
	private List<AbstractNote> notesListCache;
	private volatile Integer notesListCacheLabelId = INVALID_ID;
	private volatile boolean notesListCacheActual = false;
	private volatile int notesListCacheSize = 0;

	// list sort
	private volatile NotesUtils.NoteSortOrder notesSortOrder = NotesUtils.DEFAULT_SORT_ORDER;

	// note cache
	private AbstractNote noteCache;
	private volatile Integer noteCacheNoteId = INVALID_ID;
	private volatile boolean noteCacheActual = false;

	// labels cache
	private List<Label> labelsListCache;
	private volatile boolean labelsListCacheActual = false;

	// listeners
	private List<NotesStorageListener> databaseListeners;


	// notes

	@Override
	public boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder) {
		boolean orderChanged = this.notesSortOrder != notesSortOrder;
		if (orderChanged) {
			this.notesSortOrder = notesSortOrder;
			notesListCacheActual = false;
			notifyListeners();
		}
		return orderChanged;
	}

	@Override
	public AbstractNote getNote(Serializable id) {
		refreshNoteCacheIfNeeded((Integer) id);
		return noteCache;
	}

	private void refreshNoteCacheIfNeeded(Integer noteId) {
		final boolean needToRefresh = !noteCacheActual || !noteCacheNoteId.equals(noteId);
		AppLog.d(LOG_TAG, "Note entry refresh (id=" + noteId + "). Cached entry " +
				(needToRefresh ? "NOT " : "") + "actual");
		if (needToRefresh) {
			noteCache = (AbstractNote) performDatabaseTransaction(TransactionType.GetNote, noteId);
			noteCacheNoteId = noteId;
			noteCacheActual = true;
		}
	}

	private void refreshNotesListCacheIfNeeded(Integer labelId) {
		final boolean needToRefresh = !notesListCacheActual || !notesListCacheLabelId.equals(labelId);
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
	public List<AbstractNote> getNotesForLabel(Serializable labelId) {
		refreshNotesListCacheIfNeeded((Integer) labelId);
		return notesListCache;
	}

	@Override
	public int getNotesForLabelCount(Serializable labelId) {
		refreshNotesListCacheIfNeeded((Integer) labelId);
		return notesListCacheSize;
	}

	@Override
	public synchronized Serializable insertNote(AbstractNote note) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertNote, note);
	}

	@Override
	public synchronized boolean updateNote(Serializable id, AbstractNote note) {
		return (Boolean) performDatabaseTransaction(TransactionType.UpdateNote, id, note);
	}

	@Override
	public synchronized boolean deleteNote(Serializable id) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteNote, id);
	}


	// labels

	@Override
	public Label getLabel(Serializable id) {
		return (Label) performDatabaseTransaction(TransactionType.GetLabel, id);
	}

	@Override
	public List<Label> getAllLabels() {
		refreshLabelsListCacheIfNeeded();
		return labelsListCache;
	}

	private void refreshLabelsListCacheIfNeeded() {
		AppLog.d(LOG_TAG, "Labels entries refresh. Cached entries list " +
				(labelsListCacheActual ? "" : "NOT ") + "actual");
		if (!labelsListCacheActual) {
			labelsListCache = (List<Label>) performDatabaseTransaction(TransactionType.GetAllLabels);
			labelsListCacheActual = true;
		}
	}

	@Override
	public synchronized Serializable insertLabel(Label label) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertLabel, label);
	}

	@Override
	public synchronized boolean updateLabel(Serializable id, Label label) {
		return (Boolean) performDatabaseTransaction(TransactionType.UpdateLabel, id, label);
	}

	@Override
	public synchronized boolean deleteLabel(Serializable id) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteLabel, id);
	}


	// notes_labels

	@Override
	public List<Label> getLabelsForNote(Serializable noteId) {
		return (List<Label>) performDatabaseTransaction(TransactionType.GetLabelsForNote, noteId);
	}

	@Override
	public Set<Serializable> getLabelsIdsForNote(Serializable noteId) {
		return (Set<Serializable>) performDatabaseTransaction(TransactionType.GetLabelsIdsForNote, noteId);
	}

	@Override
	public Set<Pair<Serializable, Serializable>> getAllNotesLabelsIds() {
		return (Set<Pair<Serializable, Serializable>>) performDatabaseTransaction(TransactionType.GetAllNotesLabelsIds);
	}

	@Override
	public synchronized Serializable insertLabelToNote(Serializable noteId, Serializable labelId) {
		return (Integer) performDatabaseTransaction(TransactionType.InsertLabelToNote, noteId, labelId);
	}

	@Override
	public synchronized boolean deleteLabelFromNote(Serializable noteId, Serializable labelId) {
		return (Boolean) performDatabaseTransaction(TransactionType.DeleteLabelFromNote, noteId, labelId);
	}


	private Object performDatabaseTransaction(TransactionType transactionType, Object... args) {
		Object result;
		Integer noteId = 0;
		Integer labelId = 0;

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

			case DeleteAllData:
				adapter.deleteAllData();
				result = null;
				break;

			default:
				throw new IllegalArgumentException("Wrong transaction type: " + transactionType.name());
		}
		adapter.close();

		onTransactionPerformed(transactionType, noteId, labelId);
		return result;
	}

	private void onTransactionPerformed(TransactionType transactionType, Integer noteId, Integer labelId) {
		AppLog.d(LOG_TAG, "Database transaction (" + transactionType.name() + ") performed");

		if (noteCacheNoteId.equals(noteId) && noteModificationTransaction(transactionType)) {
			noteCacheActual = false;
		}
		if (labelsModificationTransaction(transactionType)) {
			labelsListCacheActual = false;
		}
		if (databaseModificationTransaction(transactionType)) {
			notesListCacheActual = false;
			notifyListeners();
		}
	}

	// Listeners

	private void notifyListeners() {
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

	@Override
	public List<NotesStorageListener> detachAllListeners() {
		if (databaseListeners == null) {
			databaseListeners = new LinkedList<NotesStorageListener>();
		}
		final List<NotesStorageListener> listeners = databaseListeners;
		databaseListeners = null;
		return listeners;
	}

	@Override
	public void attachListeners(List<NotesStorageListener> listeners) {
		if (listeners == null) {
			throw new NullPointerException();
		}
		if (databaseListeners == null) {
			databaseListeners = new LinkedList<NotesStorageListener>();
		}
		databaseListeners.addAll(listeners);
		notifyListeners();
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

			case DeleteAllData:
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

			case DeleteAllData:
				return true;
		}
		return false;
	}

	private static boolean labelsModificationTransaction(TransactionType transactionType) {
		switch (transactionType) {
			case InsertLabel:
			case UpdateLabel:
			case DeleteLabel:

			case DeleteAllData:
				return true;
		}
		return false;
	}


	@Override
	public void sync() {
		// do nothing
	}

	@Override
	public void clear() {
		performDatabaseTransaction(TransactionType.DeleteAllData);
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
		DeleteLabelFromNote,

		DeleteAllData
	}

}
