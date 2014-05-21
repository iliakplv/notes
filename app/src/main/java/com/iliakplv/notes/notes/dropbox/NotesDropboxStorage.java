package com.iliakplv.notes.notes.dropbox;


import android.util.Pair;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxTable;
import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.NotesStorageListener;
import com.iliakplv.notes.utils.AppLog;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class NotesDropboxStorage implements NotesStorage {

	private static final String TAG = NotesDropboxStorage.class.getSimpleName();
	private static final String INVALID_ID = "";

	// tables
	private static final String NOTES_TITLE = "title";
	private static final String NOTES_TEXT = "text";
	private static final String NOTES_CREATE_TIME = "created";
	private static final String NOTES_CHANGE_TIME = "changed";

	private static final String LABELS_NAME = "name";
	private static final String LABELS_COLOR = "color";

	private static final String NOTE_LABELS_NOTE_ID = "note_id";
	private static final String NOTE_LABELS_LABEL_ID = "label_id";

	private DbxTable notesTable;
	private DbxTable labelsTable;
	private DbxTable notesLabelsTable;
	
	// cache
	private static final int CACHE_NOTE = 1;
	private static final int CACHE_NOTES_LIST = 2;
	private static final int CACHE_LABELS_LIST = 4;

	// notes list cache
	private List<AbstractNote> notesListCache;
	private volatile String notesListCacheLabelId = INVALID_ID;
	private volatile boolean notesListCacheActual = false;
	private volatile int notesListCacheSize = 0;

	// list sort
	private volatile NotesUtils.NoteSortOrder notesSortOrder = NotesUtils.DEFAULT_SORT_ORDER;

	// note cache
	private AbstractNote noteCache;
	private volatile String noteCacheNoteId = INVALID_ID;
	private volatile boolean noteCacheActual = false;

	// labels cache
	private List<Label> labelsListCache;
	private volatile boolean labelsListCacheActual = false;

	// listeners
	private List<NotesStorageListener> storageListeners;


	public NotesDropboxStorage() {
		final DbxAccount account = DropboxHelper.getAccount();
		DbxDatastore datastore;

		try {
			datastore = DbxDatastore.openDefault(account);
		} catch (DbxException e) {
			AppLog.e(TAG, "Error opening datastore", e);
			throw new RuntimeException("Error opening datastore");
		}

		initTables(datastore);
	}

	public void initTables(DbxDatastore datastore) {
		notesTable = datastore.getTable("notes");
		labelsTable = datastore.getTable("labels");
		notesLabelsTable = datastore.getTable("notes_labels");
	}

	@Override
	public boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder) {
		boolean orderChanged = this.notesSortOrder != notesSortOrder;
		if (orderChanged) {
			this.notesSortOrder = notesSortOrder;
			invalidateCache(CACHE_NOTES_LIST);
		}
		return orderChanged;
	}

	@Override
	public AbstractNote getNote(Serializable id) {
		refreshNoteCacheIfNeeded(id.toString());
		return noteCache;
	}

	private void refreshNoteCacheIfNeeded(String noteId) {
		final boolean needToRefresh = !noteCacheActual || !noteCacheNoteId.equals(noteId);
		AppLog.d(TAG, "Note entry refresh (id=" + noteId + "). Cached entry " +
				(needToRefresh ? "NOT " : "") + "actual");
		if (needToRefresh) {
			try {
				final String title = notesTable.get(noteId).getString(NOTES_TITLE);
				final String text = notesTable.get(noteId).getString(NOTES_TEXT);
				final long createTime = notesTable.get(noteId).getLong(NOTES_CREATE_TIME);
				final long changeTime = notesTable.get(noteId).getLong(NOTES_CHANGE_TIME);
				noteCache = new TextNote(title, text);
				noteCache.setCreateTime(new DateTime(createTime));
				noteCache.setChangeTime(new DateTime(changeTime));
			} catch (DbxException e) {
				AppLog.e(TAG, "refreshNoteCacheIfNeeded()", e);
				throw new RuntimeException();
			}
			noteCacheNoteId = noteId;
			noteCacheActual = true;
		}
	}

	private void refreshNotesListCacheIfNeeded(String labelId) {
		final boolean needToRefresh = !notesListCacheActual || !notesListCacheLabelId.equals(labelId);
		AppLog.d(TAG, "Notes entries refresh (labelId=" + labelId + "). Cached entries list " +
				(needToRefresh ? "NOT " : "") + "actual");
		if (needToRefresh) {

			// TODO implement query

			notesListCacheLabelId = labelId;
			notesListCacheSize = notesListCache.size();
			notesListCacheActual = true;
		}
	}


		@Override
	public List<AbstractNote> getNotesForLabel(Serializable labelId) {
		refreshNotesListCacheIfNeeded(labelId.toString());
		return notesListCache;
	}

	@Override
	public int getNotesForLabelCount(Serializable labelId) {
		refreshNotesListCacheIfNeeded(labelId.toString());
		return notesListCacheSize;
	}

	@Override
	public Serializable insertNote(AbstractNote note) {
		// TODO implement
		invalidateCache(CACHE_NOTES_LIST);
		return 0;
	}

	@Override
	public boolean updateNote(Serializable id, AbstractNote note) {
		// TODO implement
		invalidateCache((noteCacheNoteId.equals(id) ? CACHE_NOTE : 0) | CACHE_NOTES_LIST);
		return false;
	}

	@Override
	public boolean deleteNote(Serializable id) {
		// TODO implement
		invalidateCache((noteCacheNoteId.equals(id) ? CACHE_NOTE : 0) | CACHE_NOTES_LIST);
		return false;
	}

	@Override
	public Label getLabel(Serializable id) {
		// TODO implement

		return null;
	}

	@Override
	public List<Label> getAllLabels() {
		// TODO implement

		return null;
	}

	@Override
	public Serializable insertLabel(Label label) {
		// TODO implement
		invalidateCache(CACHE_NOTES_LIST | CACHE_LABELS_LIST);
		return 0;
	}

	@Override
	public boolean updateLabel(Serializable id, Label label) {
		// TODO implement
		invalidateCache(CACHE_NOTE | CACHE_NOTES_LIST | CACHE_LABELS_LIST);
		return false;
	}

	@Override
	public boolean deleteLabel(Serializable id) {
		// TODO implement
		invalidateCache(CACHE_NOTE | CACHE_NOTES_LIST | CACHE_LABELS_LIST);
		return false;
	}

	@Override
	public List<Label> getLabelsForNote(Serializable noteId) {
		// TODO implement

		return null;
	}

	@Override
	public Set<Serializable> getLabelsIdsForNote(Serializable noteId) {
		// TODO implement

		return null;
	}

	@Override
	public Set<Pair<Serializable, Serializable>> getAllNotesLabelsIds() {
		// TODO implement [if needed]

		return null;
	}

	@Override
	public Serializable insertLabelToNote(Serializable noteId, Serializable labelId) {
		// TODO implement
		invalidateCache((noteCacheNoteId.equals(noteId) ? CACHE_NOTE : 0) | CACHE_NOTES_LIST);
		return 0;
	}

	@Override
	public boolean deleteLabelFromNote(Serializable noteId, Serializable labelId) {
		// TODO implement
		invalidateCache((noteCacheNoteId.equals(noteId) ? CACHE_NOTE : 0) | CACHE_NOTES_LIST);
		return false;
	}

	// Cache control

	private void invalidateCache(final int cacheType) {
		if ((cacheType & CACHE_NOTE) != 0) {
			noteCacheActual = false;
		}
		if ((cacheType & CACHE_NOTES_LIST) != 0) {
			notesListCacheActual = false;
			notifyDatabaseListeners();
		}
		if ((cacheType & CACHE_LABELS_LIST) != 0) {
			labelsListCacheActual = false;
		}
	}

	// Listeners

	private void notifyDatabaseListeners() {
		if (storageListeners != null) {
			NotesApplication.executeInBackground(new Runnable() {
				@Override
				public void run() {
					for (NotesStorageListener listener : storageListeners) {
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
		if (storageListeners == null) {
			storageListeners = new LinkedList<NotesStorageListener>();
		}
		return storageListeners.add(listener);
	}

	@Override
	public boolean removeStorageListener(NotesStorageListener listener) {
		if (storageListeners != null) {
			return storageListeners.remove(listener);
		}
		return false;
	}

}
