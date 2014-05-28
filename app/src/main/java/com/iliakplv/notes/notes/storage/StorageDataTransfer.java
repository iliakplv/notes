package com.iliakplv.notes.notes.storage;


import android.util.Pair;

import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public final class StorageDataTransfer {

	private static final NotesStorage storage = Storage.getStorage();

	private static boolean backupPerformed = false;

	private static List<AbstractNote> notesBackup;
	private static List<Label> labelsBackup;
	private static Set<Pair<Serializable, Serializable>> notesLabelsBackup;


	private static void backupFromStorage() {
		notesBackup = storage.getNotesForLabel(NotesStorage.NOTES_FOR_ALL_LABELS);
		labelsBackup = storage.getAllLabels();
		notesLabelsBackup = storage.getAllNotesLabelsIds();
		backupPerformed = true;
	}

	private static void restoreToStorage() {
		if (!backupPerformed) {
			throw new IllegalStateException("Backup not performed!");
		}

		// Ids mapping
		final HashMap<Serializable, Serializable> notesOldToNewIdsMap =
				new HashMap<Serializable, Serializable>();
		final HashMap<Serializable, Serializable> labelsOldToNewIdsMap =
				new HashMap<Serializable, Serializable>();

		// restore notes
		for (AbstractNote note : notesBackup) {
			Serializable newId = storage.insertNote(note);
			notesOldToNewIdsMap.put(note.getId(), newId);
		}

		// restore labels
		for (Label label : labelsBackup) {
			Serializable newId = storage.insertLabel(label);
			labelsOldToNewIdsMap.put(label.getId(), newId);
		}

		// restore notes_labels
		for (Pair<Serializable, Serializable> noteIdLabelId : notesLabelsBackup) {
			Serializable newNoteId = notesOldToNewIdsMap.get(noteIdLabelId.first);
			Serializable newLabelId = labelsOldToNewIdsMap.get(noteIdLabelId.second);
			storage.insertLabelToNote(newNoteId, newLabelId);
		}
	}

	private static void clearBackup() {
		notesBackup = null;
		labelsBackup = null;
		notesLabelsBackup = null;
		backupPerformed = false;
	}


	public static synchronized void transferDataFromDatabaseToDropbox() {
		if (Storage.getCurrentStorageType() != Storage.Type.Database) {
			throw new IllegalStateException("Current storage type is " + Storage.getCurrentStorageType());
		}

		backupFromStorage();
		if (!backupPerformed) {
			throw new IllegalStateException();
		}

		storage.clear();
		Storage.init(Storage.Type.Dropbox);
		restoreToStorage();
		clearBackup();
	}
}
