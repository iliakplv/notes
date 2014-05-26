package com.iliakplv.notes.notes.storage;

import com.iliakplv.notes.notes.db.NotesDatabaseStorage;
import com.iliakplv.notes.notes.dropbox.NotesDropboxStorage;
import com.iliakplv.notes.utils.AppLog;

public final class Storage {

	private static final String LOG_TAG = Storage.class.getSimpleName();
	private static final Type DEFAULT = Type.Database;

	private static final StorageWrapper storageWrapper = new StorageWrapper();
	private static volatile Type storageType = null;


	public static NotesStorage getStorage() {
		checkInit();
		return storageWrapper;
	}

	private static void checkInit() {
		if (storageType == null) {
			throw new IllegalStateException("Storage must be initialized before usage!");
		}
	}

	public static Type getStorageType() {
		return storageType;
	}

	public static void init(Type newStorageType) {
		AppLog.d(LOG_TAG, "init() call. Storage type: " + newStorageType.toString());

		switch (newStorageType) {
			case Database:
				storageWrapper.setTarget(new NotesDatabaseStorage());
				break;

			case Dropbox:
				storageWrapper.setTarget(new NotesDropboxStorage());
				break;

			default:
				throw new IllegalArgumentException("Unknown storage type: "
						+ newStorageType.toString());
		}
		storageType = newStorageType;
	}

	public static void initDefault() {
		init(DEFAULT);
	}


	/**
	 * ******************************************
	 *
	 * Inner classes
	 *
	 * *******************************************
	 */


	public static enum Type {
		Database,
		Dropbox
	}
}
