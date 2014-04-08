package com.iliakplv.notes.notes.storage;


import android.util.Pair;

import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;

import java.util.List;
import java.util.Set;

/* package */ final class StorageWrapper implements NotesStorage {

	private NotesStorage target;

	/* package */ void setTarget(NotesStorage target) {
		this.target = target;
	}


	// sort

	public boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder) {
		return target.setNotesSortOrder(notesSortOrder);
	}

	// notes

	public AbstractNote getNote(int id) {
		return target.getNote(id);
	}
	public List<AbstractNote> getNotesForLabel(int labelId) {
		return target.getNotesForLabel(labelId);
	}

	public int getNotesForLabelCount(int labelId) {
		return target.getNotesForLabelCount(labelId);
	}

	public int insertNote(AbstractNote note) {
		return target.insertNote(note);
	}

	public boolean updateNote(int id, AbstractNote note) {
		return target.updateNote(id, note);
	}

	public boolean deleteNote(int id) {
		return target.deleteNote(id);
	}

	// labels

	public Label getLabel(int id) {
		return target.getLabel(id);
	}

	public List<Label> getAllLabels() {
		return target.getAllLabels();
	}

	public int insertLabel(Label label) {
		return target.insertLabel(label);
	}

	public boolean updateLabel(int id, Label label) {
		return target.updateLabel(id, label);
	}

	public boolean deleteLabel(int id) {
		return target.deleteLabel(id);
	}

	// notes_labels

	public List<Label> getLabelsForNote(int noteId) {
		return target.getLabelsForNote(noteId);
	}

	public Set<Integer> getLabelsIdsForNote(int noteId) {
		return target.getLabelsIdsForNote(noteId);
	}

	public Set<Pair<Integer, Integer>> getAllNotesLabelsIds() {
		return target.getAllNotesLabelsIds();
	}

	public int insertLabelToNote(int noteId, int labelId) {
		return target.insertLabelToNote(noteId,labelId);
	}

	public boolean deleteLabelFromNote(int noteId, int labelId) {
		return target.deleteLabelFromNote(noteId, labelId);
	}

	// listeners

	public boolean addStorageListener(NotesStorageListener listener) {
		return target.addStorageListener(listener);
	}

	public boolean removeStorageListener(NotesStorageListener listener) {
		return target.removeStorageListener(listener);
	}
}
