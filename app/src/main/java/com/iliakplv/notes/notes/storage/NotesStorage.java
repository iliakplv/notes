package com.iliakplv.notes.notes.storage;

import android.util.Pair;

import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;

import java.util.List;
import java.util.Set;

public interface NotesStorage {

	public static final int NOTES_FOR_ALL_LABELS = 0;

	// sort

	public boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder);


	// notes

	public NotesDatabaseEntry<AbstractNote> getNote(int id);
	public List<NotesDatabaseEntry> getNotesForLabel(int labelId);
	public int getNotesForLabelCount(int labelId);

	public int insertNote(AbstractNote note);
	public boolean updateNote(int id, AbstractNote note);
	public boolean deleteNote(int id);


	// labels

	public NotesDatabaseEntry<Label> getLabel(int id);
	public List<NotesDatabaseEntry<Label>> getAllLabels();

	public int insertLabel(Label label);
	public boolean updateLabel(int id, Label label);
	public boolean deleteLabel(int id);


	// notes_labels

	public List<NotesDatabaseEntry<Label>> getLabelsForNote(int noteId);
	public Set<Integer> getLabelsIdsForNote(int noteId);
	public Set<Pair<Integer, Integer>> getAllNotesLabelsIds();

	public int insertLabelToNote(int noteId, int labelId);
	public boolean deleteLabelFromNote(int noteId, int labelId);


	// listeners
	public boolean addStorageListener(NotesStorageListener listener);
	public boolean removeStorageListener(NotesStorageListener listener);
}
