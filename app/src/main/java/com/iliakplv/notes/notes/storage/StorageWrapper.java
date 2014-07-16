package com.iliakplv.notes.notes.storage;


import android.util.Pair;

import com.iliakplv.notes.analytics.Event;
import com.iliakplv.notes.analytics.EventTracker;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;

import java.io.Serializable;
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

	public AbstractNote getNote(Serializable id) {
		return target.getNote(id);
	}
	public List<AbstractNote> getNotesForLabel(Serializable labelId) {
		return target.getNotesForLabel(labelId);
	}

	public int getNotesForLabelCount(Serializable labelId) {
		return target.getNotesForLabelCount(labelId);
	}

	public Serializable insertNote(AbstractNote note) {
		return target.insertNote(note);
	}

	public boolean updateNote(Serializable id, AbstractNote note) {
		// track event
		return target.updateNote(id, note);
	}

	public boolean deleteNote(Serializable id) {
		// track event
		return target.deleteNote(id);
	}

	// labels

	public Label getLabel(Serializable id) {
		return target.getLabel(id);
	}

	public List<Label> getAllLabels() {
		return target.getAllLabels();
	}

	public Serializable insertLabel(Label label) {
		return target.insertLabel(label);
	}

	public boolean updateLabel(Serializable id, Label label) {
		EventTracker.track(Event.LabelEdit);
		return target.updateLabel(id, label);
	}

	public boolean deleteLabel(Serializable id) {
		EventTracker.track(Event.LabelDelete);
		return target.deleteLabel(id);
	}

	// notes_labels

	public List<Label> getLabelsForNote(Serializable noteId) {
		return target.getLabelsForNote(noteId);
	}

	public Set<Serializable> getLabelsIdsForNote(Serializable noteId) {
		return target.getLabelsIdsForNote(noteId);
	}

	public Set<Pair<Serializable, Serializable>> getAllNotesLabelsIds() {
		return target.getAllNotesLabelsIds();
	}

	public Serializable insertLabelToNote(Serializable noteId, Serializable labelId) {
		EventTracker.track(Event.LabelAddToNote);
		return target.insertLabelToNote(noteId,labelId);
	}

	public boolean deleteLabelFromNote(Serializable noteId, Serializable labelId) {
		EventTracker.track(Event.LabelRemoveFromNote);
		return target.deleteLabelFromNote(noteId, labelId);
	}

	// listeners

	public boolean addStorageListener(NotesStorageListener listener) {
		return target.addStorageListener(listener);
	}

	public boolean removeStorageListener(NotesStorageListener listener) {
		return target.removeStorageListener(listener);
	}

	// sync

	public void sync() {
		target.sync();
	}

	// clear

	public void clear() {
		target.clear();
	}
}
