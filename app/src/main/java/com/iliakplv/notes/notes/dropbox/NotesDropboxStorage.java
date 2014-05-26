package com.iliakplv.notes.notes.dropbox;


import android.util.Pair;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.LabelComparator;
import com.iliakplv.notes.notes.NoteComparator;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.NotesStorageListener;
import com.iliakplv.notes.utils.AppLog;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class NotesDropboxStorage implements NotesStorage {

	private static final String TAG = NotesDropboxStorage.class.getSimpleName();
	private static final String INVALID_ID = "";

	private static final DbxTable.ResolutionRule RESOLUTION_RULE = DbxTable.ResolutionRule.LOCAL;
	DbxDatastore datastore;

	// tables
	private static final String NOTES_TABLE = "notes";
	private static final String NOTES_TITLE = "title";
	private static final String NOTES_TEXT = "text";
	private static final String NOTES_CREATE_TIME = "created";
	private static final String NOTES_CHANGE_TIME = "changed";
	private static final String[] NOTES_FIELDS =
			{NOTES_TITLE, NOTES_TEXT, NOTES_CREATE_TIME, NOTES_CHANGE_TIME};

	private static final String LABELS_TABLE = "labels";
	private static final String LABELS_NAME = "name";
	private static final String LABELS_COLOR = "color";
	private static final String[] LABELS_FIELDS =
			{LABELS_NAME, LABELS_COLOR};

	private static final String NOTES_LABELS_TABLE = "notes_labels";
	private static final String NOTES_LABELS_NOTE_ID = "note_id";
	private static final String NOTES_LABELS_LABEL_ID = "label_id";
	private static final String[] NOTES_LABELS_FIELDS =
			{NOTES_LABELS_NOTE_ID, NOTES_LABELS_LABEL_ID};

	private DbxTable notesTable;
	private DbxTable labelsTable;
	private DbxTable notesLabelsTable;
	
	// cache
	private static final int CACHE_NOTE = 1;
	private static final int CACHE_NOTES_LIST = 2;
	private static final int CACHE_LABELS_LIST = 4;

	// notes list cache
	private List<AbstractNote> notesListCache;
	private volatile Serializable notesListCacheLabelId = INVALID_ID;
	private volatile boolean notesListCacheActual = false;
	private volatile int notesListCacheSize = 0;

	// list sort
	private NoteComparator noteComparator = new NoteComparator();
	private LabelComparator labelComparator = new LabelComparator();

	// note cache
	private AbstractNote noteCache;
	private volatile Serializable noteCacheNoteId = INVALID_ID;
	private volatile boolean noteCacheActual = false;

	// labels cache
	private List<Label> labelsListCache;
	private volatile boolean labelsListCacheActual = false;

	// listeners
	private List<NotesStorageListener> storageListeners;

	// TODO 'smart' delete
	// TODO 'clear storage' method

	public NotesDropboxStorage() {
		try {
			datastore = DbxDatastore.openDefault(DropboxHelper.getAccount());
		} catch (DbxException e) {
			AppLog.e(TAG, "Error opening datastore", e);
			throw new RuntimeException("Error opening datastore");
		}

		syncDatastore();
		initTables();
	}

	private void syncDatastore() {
		try {
			datastore.sync();
		} catch (DbxException e) {
			AppLog.e(TAG, "syncDatastore()", e);
			throw new RuntimeException();
		}
	}

	@Override
	public void sync() {
		// Cache invalidation, listeners notification. Sync also called from here
		onStorageContentChanged(CACHE_NOTE | CACHE_NOTES_LIST |CACHE_LABELS_LIST);
	}

	public void initTables() {
		notesTable = datastore.getTable(NOTES_TABLE);
		labelsTable = datastore.getTable(LABELS_TABLE);
		notesLabelsTable = datastore.getTable(NOTES_LABELS_TABLE);

		for (String field : NOTES_FIELDS) {
			notesTable.setResolutionRule(field, RESOLUTION_RULE);
		}
		for (String field : LABELS_FIELDS) {
			labelsTable.setResolutionRule(field, RESOLUTION_RULE);
		}
		for (String field : NOTES_LABELS_FIELDS) {
			notesLabelsTable.setResolutionRule(field, RESOLUTION_RULE);
		}
	}


	@Override
	public boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder) {
		boolean orderChanged = noteComparator.getSortOrder() != notesSortOrder;
		if (orderChanged) {
			noteComparator.setSortOrder(notesSortOrder);
			onStorageContentChanged(CACHE_NOTES_LIST);
		}
		return orderChanged;
	}

	@Override
	public AbstractNote getNote(Serializable id) {
		final String stringId = (String) id;
		if (!DbxTable.isValidId(stringId)) {
			return null;
		}
		refreshNoteCacheIfNeeded(stringId);
		return noteCache;
	}

	private void refreshNoteCacheIfNeeded(String noteId) {
		final boolean needToRefresh = !noteCacheActual || !noteCacheNoteId.equals(noteId);
		AppLog.d(TAG, "Note entry refresh (id=" + noteId + "). Cached entry " +
				(needToRefresh ? "NOT " : "") + "actual");
		if (needToRefresh) {
			try {
				noteCache = createNoteFromRecord(notesTable.get(noteId));
			} catch (DbxException e) {
				AppLog.e(TAG, "refreshNoteCacheIfNeeded()", e);
				throw new RuntimeException();
			}

			noteCacheNoteId = noteId;
			noteCacheActual = true;
		}
	}

	private void refreshNotesListCacheIfNeeded(Serializable labelId) {
		final boolean needToRefresh = !notesListCacheActual || !notesListCacheLabelId.equals(labelId);
		AppLog.d(TAG, "Notes entries refresh (labelId=" + labelId + "). Cached entries list " +
				(needToRefresh ? "NOT " : "") + "actual");
		if (needToRefresh) {
			// query all notes records
			final DbxTable.QueryResult allNotesRecords;
			try {
				allNotesRecords = notesTable.query();
			} catch (DbxException e) {
				AppLog.e(TAG, "refreshNotesListCacheIfNeeded", e);
				throw new RuntimeException();
			}

			// find notes ids for specified label
			final boolean notesForAllLabels = labelId.equals(NOTES_FOR_ALL_LABELS);
			final Set<String> noteIdsForLabel = notesForAllLabels ?
					null :
					getNotesIdsForLabel((String) labelId);

			// add required notes to cache
			if (notesListCache != null) {
				notesListCache.clear();
			} else {
				notesListCache = new ArrayList<AbstractNote>();
			}
			for (DbxRecord noteRecord : allNotesRecords) {
				if (notesForAllLabels || noteIdsForLabel.contains(noteRecord.getId())) {
					notesListCache.add(createNoteFromRecord(noteRecord));
				}
			}

			Collections.sort(notesListCache, noteComparator);

			notesListCacheLabelId = labelId;
			notesListCacheSize = notesListCache.size();
			notesListCacheActual = true;
		}
	}

	private static AbstractNote createNoteFromRecord(DbxRecord record) {
		final String title = record.getString(NOTES_TITLE);
		final String text = record.getString(NOTES_TEXT);
		final long createTime = record.getLong(NOTES_CREATE_TIME);
		final long changeTime = record.getLong(NOTES_CHANGE_TIME);

		final AbstractNote note = new TextNote(title, text);
		note.setCreateTime(new DateTime(createTime));
		note.setChangeTime(new DateTime(changeTime));
		note.setId(record.getId());
		return note;
	}

	@Override
	public List<AbstractNote> getNotesForLabel(Serializable labelId) {
		refreshNotesListCacheIfNeeded(labelId);
		return notesListCache;
	}

	@Override
	public int getNotesForLabelCount(Serializable labelId) {
		refreshNotesListCacheIfNeeded(labelId);
		return notesListCacheSize;
	}

	@Override
	public Serializable insertNote(AbstractNote note) {
		final DbxRecord temp = notesTable.insert()
				.set(NOTES_TITLE, note.getTitle())
				.set(NOTES_TEXT, note.getBody())
				.set(NOTES_CREATE_TIME, note.getCreateTime().getMillis())
				.set(NOTES_CHANGE_TIME, note.getChangeTime().getMillis());

		onStorageContentChanged(CACHE_NOTES_LIST);
		return temp.getId();
	}

	@Override
	public boolean updateNote(Serializable id, AbstractNote note) {
		final DbxRecord noteRecord;
		try {
			noteRecord = notesTable.get((String) id);
		} catch (DbxException e) {
			AppLog.e(TAG, "updateNote()", e);
			throw new RuntimeException();
		}

		final boolean existingRecord = noteRecord != null;
		if (existingRecord) {
			noteRecord
					.set(NOTES_TITLE, note.getTitle())
					.set(NOTES_TEXT, note.getBody())
					.set(NOTES_CREATE_TIME, note.getCreateTime().getMillis())
					.set(NOTES_CHANGE_TIME, note.getChangeTime().getMillis());
			onStorageContentChanged((noteCacheNoteId.equals(id) ? CACHE_NOTE : 0) | CACHE_NOTES_LIST);
		}

		return existingRecord;
	}

	@Override
	public boolean deleteNote(Serializable id) {
		boolean deleted = false;
		try {
			final DbxRecord noteRecord = notesTable.get((String) id);
			if (noteRecord != null) {
				noteRecord.deleteRecord();
				deleted = true;
				onStorageContentChanged((noteCacheNoteId.equals(id) ? CACHE_NOTE : 0) | CACHE_NOTES_LIST);
			}
		} catch (DbxException e) {
			AppLog.e(TAG, "deleteNote()", e);
			throw new RuntimeException();
		}

		return deleted;
	}

	@Override
	public Label getLabel(Serializable id) {
		final String stringId = (String) id;
		if (!DbxTable.isValidId(stringId)) {
			return null;
		}

		final DbxRecord labelRecord;
		try {
			labelRecord = labelsTable.get(stringId);
		} catch (DbxException e) {
			AppLog.e(TAG, "getLabel()", e);
			throw new RuntimeException();
		}
		return createLabelFromRecord(labelRecord);
	}

	@Override
	public List<Label> getAllLabels() {
		refreshLabelsListCacheIfNeeded();
		return labelsListCache;
	}

	private void refreshLabelsListCacheIfNeeded() {
		AppLog.d(TAG, "Labels entries refresh. Cached entries list " +
				(labelsListCacheActual ? "" : "NOT ") + "actual");
		if (!labelsListCacheActual) {
			// get all labels records
			final DbxTable.QueryResult allLabelsRecords;
			try {
				allLabelsRecords = labelsTable.query();
			} catch (DbxException e) {
				AppLog.e(TAG, "refreshLabelsListCacheIfNeeded()", e);
				throw new RuntimeException();
			}

			// fill cache
			if (labelsListCache != null) {
				labelsListCache.clear();
			} else {
				labelsListCache = new ArrayList<Label>();
			}
			for (DbxRecord labelRecord : allLabelsRecords) {
				labelsListCache.add(createLabelFromRecord(labelRecord));
			}

			Collections.sort(labelsListCache, labelComparator);

			labelsListCacheActual = true;
		}
	}

	private static Label createLabelFromRecord(DbxRecord record) {
		final String name = record.getString(LABELS_NAME);
		final int color = (int) record.getLong(LABELS_COLOR);
		final Label label = new Label(name, color);
		label.setId(record.getId());
		return label;
	}

	@Override
	public Serializable insertLabel(Label label) {
		final DbxRecord temp = labelsTable.insert()
				.set(LABELS_NAME, label.getName())
				.set(LABELS_COLOR, label.getColor());

		onStorageContentChanged(CACHE_NOTES_LIST | CACHE_LABELS_LIST);
		return temp.getId();
	}

	@Override
	public boolean updateLabel(Serializable id, Label label) {
		final DbxRecord labelRecord;
		try {
			labelRecord = labelsTable.get((String) id);
		} catch (DbxException e) {
			AppLog.e(TAG, "updateLabel()", e);
			throw new RuntimeException();
		}
		final boolean existingRecord = labelRecord != null;
		if (existingRecord) {
			labelRecord
					.set(LABELS_NAME, label.getName())
					.set(LABELS_COLOR, label.getColor());
			onStorageContentChanged(CACHE_NOTE | CACHE_NOTES_LIST | CACHE_LABELS_LIST);
		}
		return existingRecord;
	}

	@Override
	public boolean deleteLabel(Serializable id) {
		boolean deleted = false;
		try {
			final DbxRecord labelRecord = labelsTable.get((String) id);
			if (labelRecord != null) {
				labelRecord.deleteRecord();
				deleted = true;
				onStorageContentChanged(CACHE_NOTE | CACHE_NOTES_LIST | CACHE_LABELS_LIST);
			}
		} catch (DbxException e) {
			AppLog.e(TAG, "deleteLabel()", e);
			throw new RuntimeException();
		}
		return deleted;
	}

	@Override
	public List<Label> getLabelsForNote(Serializable noteId) {
		final List<Label> allLabels = getAllLabels();
		final Set<Serializable> labelsIdsForNote = getLabelsIdsForNote(noteId);

		final List<Label> result = new ArrayList<Label>();
		for (Label label : allLabels) {
			if (labelsIdsForNote.contains(label.getId())) {
				result.add(label);
			}
		}

		return result;
	}

	@Override
	public Set<Serializable> getLabelsIdsForNote(Serializable noteId) {
		final DbxFields queryParams = new DbxFields().set(NOTES_LABELS_NOTE_ID, (String) noteId);

		final DbxTable.QueryResult notesLabelsIds;
		try {
			notesLabelsIds = notesLabelsTable.query(queryParams);
		} catch (DbxException e) {
			AppLog.e(TAG, "getLabelsIdsForNote()", e);
			throw new RuntimeException();
		}

		final Set<Serializable> result = new HashSet<Serializable>();
		for (DbxRecord record : notesLabelsIds) {
			result.add(record.getString(NOTES_LABELS_LABEL_ID));
		}
		return result;
	}

	private Set<String> getNotesIdsForLabel(String labelId) {
		final DbxFields queryParams = new DbxFields().set(NOTES_LABELS_LABEL_ID, labelId);
		final DbxTable.QueryResult notesLabelsIds;

		try {
			notesLabelsIds = notesLabelsTable.query(queryParams);
		} catch (DbxException e) {
			AppLog.e(TAG, "getNotesIdsForLabel()", e);
			throw new RuntimeException();
		}

		final Set<String> result = new HashSet<String>();
		for (DbxRecord record : notesLabelsIds) {
			result.add(record.getString(NOTES_LABELS_NOTE_ID));
		}
		return result;
	}

	@Override
	public Set<Pair<Serializable, Serializable>> getAllNotesLabelsIds() {
		final DbxTable.QueryResult allNotesLabelsIds;
		try {
			allNotesLabelsIds = notesLabelsTable.query();
		} catch (DbxException e) {
			AppLog.e(TAG, "getAllNotesLabelsIds()", e);
			throw new RuntimeException();
		}
		final HashSet<Pair<Serializable, Serializable>> result =
				new HashSet<Pair<Serializable, Serializable>>();
		for (DbxRecord record : allNotesLabelsIds) {
			result.add(new Pair<Serializable, Serializable>(
					record.getString(NOTES_LABELS_NOTE_ID),
					record.getString(NOTES_LABELS_LABEL_ID)));
		}
		return result;
	}

	@Override
	public Serializable insertLabelToNote(Serializable noteId, Serializable labelId) {
		final DbxRecord temp = notesLabelsTable.insert()
				.set(NOTES_LABELS_NOTE_ID, (String) noteId)
				.set(NOTES_LABELS_LABEL_ID, (String) labelId);

		onStorageContentChanged((noteCacheNoteId.equals(noteId) ? CACHE_NOTE : 0) | CACHE_NOTES_LIST);
		return temp.getId();
	}

	@Override
	public boolean deleteLabelFromNote(Serializable noteId, Serializable labelId) {
		final DbxFields queryParams = new DbxFields()
				.set(NOTES_LABELS_NOTE_ID, (String) noteId)
				.set(NOTES_LABELS_LABEL_ID, (String) labelId);

		final DbxTable.QueryResult notesLabels;
		try {
			notesLabels = notesLabelsTable.query(queryParams);
		} catch (DbxException e) {
			AppLog.e(TAG, "deleteLabelFromNote()", e);
			throw new RuntimeException();
		}

		boolean deleted = false;
		for (DbxRecord record : notesLabels) {
			record.deleteRecord();
			deleted = true;
		}
		if (deleted) {
			onStorageContentChanged((noteCacheNoteId.equals(noteId) ? CACHE_NOTE : 0) | CACHE_NOTES_LIST);
		}
		return deleted;
	}

	// Cache control

	private void onStorageContentChanged(final int affectedCacheType) {
		syncDatastore();

		// cache invalidation
		if ((affectedCacheType & CACHE_NOTE) != 0) {
			noteCacheActual = false;
		}
		if ((affectedCacheType & CACHE_NOTES_LIST) != 0) {
			notesListCacheActual = false;
		}
		if ((affectedCacheType & CACHE_LABELS_LIST) != 0) {
			labelsListCacheActual = false;
		}

		notifyDatabaseListeners();
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
