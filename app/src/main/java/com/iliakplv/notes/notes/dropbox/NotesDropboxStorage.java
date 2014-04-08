package com.iliakplv.notes.notes.dropbox;


import android.util.Pair;

import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.NotesStorageListener;

import java.util.List;
import java.util.Set;

public class NotesDropboxStorage implements NotesStorage {


	@Override
	public boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder) {
		return false;
	}

	@Override
	public AbstractNote getNote(int id) {
		return null;
	}

	@Override
	public List<AbstractNote> getNotesForLabel(int labelId) {
		return null;
	}

	@Override
	public int getNotesForLabelCount(int labelId) {
		return 0;
	}

	@Override
	public int insertNote(AbstractNote note) {
		return 0;
	}

	@Override
	public boolean updateNote(int id, AbstractNote note) {
		return false;
	}

	@Override
	public boolean deleteNote(int id) {
		return false;
	}

	@Override
	public Label getLabel(int id) {
		return null;
	}

	@Override
	public List<Label> getAllLabels() {
		return null;
	}

	@Override
	public int insertLabel(Label label) {
		return 0;
	}

	@Override
	public boolean updateLabel(int id, Label label) {
		return false;
	}

	@Override
	public boolean deleteLabel(int id) {
		return false;
	}

	@Override
	public List<Label> getLabelsForNote(int noteId) {
		return null;
	}

	@Override
	public Set<Integer> getLabelsIdsForNote(int noteId) {
		return null;
	}

	@Override
	public Set<Pair<Integer, Integer>> getAllNotesLabelsIds() {
		return null;
	}

	@Override
	public int insertLabelToNote(int noteId, int labelId) {
		return 0;
	}

	@Override
	public boolean deleteLabelFromNote(int noteId, int labelId) {
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
