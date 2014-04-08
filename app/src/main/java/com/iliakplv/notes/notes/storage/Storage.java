package com.iliakplv.notes.notes.storage;

import com.iliakplv.notes.notes.db.NotesDatabaseStorage;
import com.iliakplv.notes.utils.AppLog;

public final class Storage {

	private static final String LOG_TAG = Storage.class.getSimpleName();
	private static final Type DEFAULT = Type.Database;

	private static final StorageWrapper storageWrapper = new StorageWrapper();
	private static volatile boolean initialized = false;


	public static NotesStorage getStorage() {
		checkInit();
		return storageWrapper;
	}

	private static void checkInit() {
		if (!initialized) {
			throw new IllegalStateException("Storage must be initialized before usage!");
		}
	}

	public static void init(Type storageType) {
		AppLog.d(LOG_TAG, "init() call. Storage type: " + storageType.toString());
		initialized = false;
		switch (storageType) {
			case Database:
				storageWrapper.initStorage(new NotesDatabaseStorage());
				break;

			case Dropbox:
				throw new IllegalArgumentException("Not implemented");
				// break;

			default:
				throw new IllegalArgumentException("Unknown storage type: "
						+ storageType.toString());
		}
		initialized = true;
	}

	public static void initDefault() {
		init(DEFAULT);
	}


	/***********************************
	 *      Inner classes
	 ***********************************/

	public static enum Type {
		Database,
		Dropbox
	}
}
