package com.iliakplv.notes.notes.storage;

import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

public final class Storage {

	private static NotesStorage currentStorage = new NotesDatabaseFacade();


	public static NotesStorage getStorage() {
		return currentStorage;
	}


	public static boolean addStorageListener(NotesStorageListener listener) {
		return currentStorage.addStorageListener(listener);
	}

	public static boolean removeStorageListener(NotesStorageListener listener) {
		return currentStorage.removeStorageListener(listener);
	}
}
