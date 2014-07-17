package com.iliakplv.notes.notes.storage;


import android.util.Pair;

import com.iliakplv.notes.analytics.EventTracker;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.utils.AppLog;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public final class StorageDataTransfer {

	private static final String TAG = StorageDataTransfer.class.getSimpleName();

	private static final NotesStorage storage = Storage.getStorage();


	private static List<AbstractNote> notesBackup;
	private static List<Label> labelsBackup;
	private static Set<Pair<Serializable, Serializable>> notesLabelsBackup;
	private static boolean backupPerformed = false;


	private static void backupCurrentStorage() {
		notesBackup = storage.getNotesForLabel(NotesStorage.NOTES_FOR_ALL_LABELS);
		labelsBackup = storage.getAllLabels();
		notesLabelsBackup = storage.getAllNotesLabelsIds();
		backupPerformed = true;
	}

	private static void restoreBackup() {
		checkBackupPerformed();

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

	private static void checkBackupPerformed() {
		if (!backupPerformed) {
			throw new IllegalStateException("Backup not performed!");
		}
	}

	private static void clearCurrentStorage() {
		checkBackupPerformed();
		storage.clear();
	}

	private static void clearBackup() {
		notesBackup = null;
		labelsBackup = null;
		notesLabelsBackup = null;
		backupPerformed = false;
	}


	public static synchronized void changeStorageType(Storage.Type newStorageType, boolean clearCurrentStorage) {
		if (newStorageType == null) {
			throw new NullPointerException("New storage type is null");
		}
		if (Storage.getCurrentStorageType() == newStorageType) {
			return;
		}

		// event tracking state backup
		final boolean eventTrackingWasEnabled = EventTracker.isEnabled();
		EventTracker.setEnabled(false);
		// listeners backup
		final List<NotesStorageListener> listeners = Storage.getStorage().detachAllListeners();

// data transfer start

		backupCurrentStorage();
		if (clearCurrentStorage) {
			clearCurrentStorage();
		}

		boolean newStorageInitialized = false;
		try {
			Storage.init(newStorageType);
			newStorageInitialized = true;
		} catch (Exception e) {
			AppLog.e(TAG, "Exception during storage initialization", e);
		}

		// restore data to new initialized storage or
		// to old storage if new storage has not been initialized and old has been cleared
		if (newStorageInitialized || clearCurrentStorage) {
			restoreBackup();
		}

		clearBackup();

// data transfer end

		// listeners restore
		Storage.getStorage().attachListeners(listeners);
		// event tracking state restore
		EventTracker.setEnabled(eventTrackingWasEnabled);
	}
}
