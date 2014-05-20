package com.iliakplv.notes.notes.dropbox;


import android.util.Pair;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxTable;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.NotesStorageListener;
import com.iliakplv.notes.utils.AppLog;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class NotesDropboxStorage implements NotesStorage {

	private static final String TAG = NotesDropboxStorage.class.getSimpleName();


	private static final String NOTES_TITLE = "title";
	private static final String NOTES_TEXT = "text";
	private static final String NOTES_CREATE_TIME = "created";
	private static final String NOTES_CHANGE_TIME = "changed";

	private DbxTable notesTable;
	private DbxTable labelsTable;
	private DbxTable notesLabelsTable;


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
		return false;
	}

	@Override
	public AbstractNote getNote(Serializable id) {
		AbstractNote note;
		try {
			final String title = notesTable.get(id.toString()).getString(NOTES_TITLE);
			final String text = notesTable.get(id.toString()).getString(NOTES_TEXT);
			final long createTime = notesTable.get(id.toString()).getLong(NOTES_CREATE_TIME);
			final long changeTime = notesTable.get(id.toString()).getLong(NOTES_CHANGE_TIME);
			note = new TextNote(title, text);
			note.setCreateTime(new DateTime(createTime));
			note.setChangeTime(new DateTime(changeTime));
		} catch (DbxException e) {
			AppLog.e(TAG, "getNote()", e);
			throw new RuntimeException();
		}
		return note;
	}

	@Override
	public List<AbstractNote> getNotesForLabel(Serializable labelId) {
		return null;
	}

	@Override
	public int getNotesForLabelCount(Serializable labelId) {
		return 0;
	}

	@Override
	public Serializable insertNote(AbstractNote note) {
		return 0;
	}

	@Override
	public boolean updateNote(Serializable id, AbstractNote note) {
		return false;
	}

	@Override
	public boolean deleteNote(Serializable id) {
		return false;
	}

	@Override
	public Label getLabel(Serializable id) {
		return null;
	}

	@Override
	public List<Label> getAllLabels() {
		return null;
	}

	@Override
	public Serializable insertLabel(Label label) {
		return 0;
	}

	@Override
	public boolean updateLabel(Serializable id, Label label) {
		return false;
	}

	@Override
	public boolean deleteLabel(Serializable id) {
		return false;
	}

	@Override
	public List<Label> getLabelsForNote(Serializable noteId) {
		return null;
	}

	@Override
	public Set<Serializable> getLabelsIdsForNote(Serializable noteId) {
		return null;
	}

	@Override
	public Set<Pair<Serializable, Serializable>> getAllNotesLabelsIds() {
		return null;
	}

	@Override
	public Serializable insertLabelToNote(Serializable noteId, Serializable labelId) {
		return 0;
	}

	@Override
	public boolean deleteLabelFromNote(Serializable noteId, Serializable labelId) {
		return false;
	}

	@Override
	public boolean addStorageListener(NotesStorageListener listener) {
		return false;
	}

	@Override
	public boolean removeStorageListener(NotesStorageListener listener) {
		return false;
	}
}
