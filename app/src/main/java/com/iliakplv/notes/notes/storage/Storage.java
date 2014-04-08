package com.iliakplv.notes.notes.storage;

import android.util.Pair;

import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

import java.util.List;
import java.util.Set;


/**
 * Application data storage (singleton)
 * Provides access to current storage implementation.
 */

public final class Storage implements NotesStorage { // TODO ???

	private static final Storage instance = new Storage();

	private NotesStorage storageImpl;

	private Storage() {
		storageImpl = new NotesDatabaseFacade();
	}

	public static NotesStorage getStorage() {
		return instance;
	}


	/***********************************
	 *      instance methods
	 ***********************************/

	// sort

	public boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder) {
		return storageImpl.setNotesSortOrder(notesSortOrder);
	}

	// notes

	public AbstractNote getNote(int id) {
		return storageImpl.getNote(id);
	}
	public List<AbstractNote> getNotesForLabel(int labelId) {
		return storageImpl.getNotesForLabel(labelId);
	}

	public int getNotesForLabelCount(int labelId) {
		return storageImpl.getNotesForLabelCount(labelId);
	}

	public int insertNote(AbstractNote note) {
		return storageImpl.insertNote(note);
	}

	public boolean updateNote(int id, AbstractNote note) {
		return storageImpl.updateNote(id, note);
	}

	public boolean deleteNote(int id) {
		return storageImpl.deleteNote(id);
	}

	// labels

	public Label getLabel(int id) {
		return storageImpl.getLabel(id);
	}

	public List<Label> getAllLabels() {
		return storageImpl.getAllLabels();
	}

	public int insertLabel(Label label) {
		return storageImpl.insertLabel(label);
	}

	public boolean updateLabel(int id, Label label) {
		return storageImpl.updateLabel(id, label);
	}

	public boolean deleteLabel(int id) {
		return storageImpl.deleteLabel(id);
	}

	// notes_labels

	public List<Label> getLabelsForNote(int noteId) {
		return storageImpl.getLabelsForNote(noteId);
	}

	public Set<Integer> getLabelsIdsForNote(int noteId) {
		return storageImpl.getLabelsIdsForNote(noteId);
	}

	public Set<Pair<Integer, Integer>> getAllNotesLabelsIds() {
		return storageImpl.getAllNotesLabelsIds();
	}

	public int insertLabelToNote(int noteId, int labelId) {
		return storageImpl.insertLabelToNote(noteId,labelId);
	}

	public boolean deleteLabelFromNote(int noteId, int labelId) {
		return storageImpl.deleteLabelFromNote(noteId, labelId);
	}

	// listeners

	public boolean addStorageListener(NotesStorageListener listener) {
		return storageImpl.addStorageListener(listener);
	}

	public boolean removeStorageListener(NotesStorageListener listener) {
		return storageImpl.removeStorageListener(listener);
	}


	/***********************************
	 *      Inner classes
	 ***********************************/

	public static enum Type {
		Database,
		Dropbox
	}
}
