package com.iliakplv.notes.notes.storage;

import android.util.Pair;

import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface NotesStorage {

    Integer NOTES_FOR_ALL_LABELS = 0;
    List<AbstractNote> EMPTY_NOTES_LIST = new ArrayList<>(0);


    // sort

    boolean setNotesSortOrder(NotesUtils.NoteSortOrder notesSortOrder);


    // notes

    AbstractNote getNote(Serializable id);

    List<AbstractNote> getNotesForLabel(Serializable labelId); // for all notes use NOTES_FOR_ALL_LABELS

    List<AbstractNote> getNotesForQuery(String searchQuery); // ignores case, spaces and empty strings

    Serializable insertNote(AbstractNote note);

    boolean updateNote(Serializable id, AbstractNote note);

    boolean deleteNote(Serializable id);


    // labels

    Label getLabel(Serializable id);

    List<Label> getAllLabels();

    Serializable insertLabel(Label label);

    boolean updateLabel(Serializable id, Label label);

    boolean deleteLabel(Serializable id);


    // notes_labels

    List<Label> getLabelsForNote(Serializable noteId);

    Set<Serializable> getLabelsIdsForNote(Serializable noteId);

    Set<Pair<Serializable, Serializable>> getAllNotesLabelsIds();    // [not used]

    Serializable insertLabelToNote(Serializable noteId, Serializable labelId);

    boolean deleteLabelFromNote(Serializable noteId, Serializable labelId);


    // listeners

    boolean addStorageListener(NotesStorageListener listener);

    boolean removeStorageListener(NotesStorageListener listener);

    List<NotesStorageListener> detachAllListeners();

    void attachListeners(List<NotesStorageListener> listeners);

    // synchronization

    void sync();

    // all data delete

    void clear();
}
