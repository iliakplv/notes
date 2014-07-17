package com.iliakplv.notes.notes.storage;

import android.util.Pair;

import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface NotesStorage {

	public static final Integer NOTES_FOR_ALL_LABELS = 0;


	// sort

	public boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder);


	// notes

	public AbstractNote getNote(Serializable id);
	public List<AbstractNote> getNotesForLabel(Serializable labelId); // for all notes use NOTES_FOR_ALL_LABELS
	public int getNotesForLabelCount(Serializable labelId); // for all notes use NOTES_FOR_ALL_LABELS

	public Serializable insertNote(AbstractNote note);
	public boolean updateNote(Serializable id, AbstractNote note);
	public boolean deleteNote(Serializable id);


	// labels

	public Label getLabel(Serializable id);
	public List<Label> getAllLabels();

	public Serializable insertLabel(Label label);
	public boolean updateLabel(Serializable id, Label label);
	public boolean deleteLabel(Serializable id);


	// notes_labels

	public List<Label> getLabelsForNote(Serializable noteId);
	public Set<Serializable> getLabelsIdsForNote(Serializable noteId);
	public Set<Pair<Serializable, Serializable>> getAllNotesLabelsIds();    // [not used]

	public Serializable insertLabelToNote(Serializable noteId, Serializable labelId);
	public boolean deleteLabelFromNote(Serializable noteId, Serializable labelId);


	// listeners

	public boolean addStorageListener(NotesStorageListener listener);
	public boolean removeStorageListener(NotesStorageListener listener);
	public List<NotesStorageListener> detachAllListeners();
	public void attachListeners(List<NotesStorageListener> listeners);

	// synchronization

	public void sync();

	// all data delete

	public void clear();
}
