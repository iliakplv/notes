package com.iliakplv.notes.notes.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.iliakplv.notes.notes.db.NotesDatabaseStorage;
import com.iliakplv.notes.notes.dropbox.NotesDropboxStorage;
import com.iliakplv.notes.utils.AppLog;

public final class Storage {

	private static final String TAG = Storage.class.getSimpleName();

	private static final StorageWrapper storageWrapper = new StorageWrapper();

	private static final String STORAGE_TYPE = "storage_type";
	public static final Type DEFAULT_STORAGE = Type.Database;
	private static Type currentStorageType = null;
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

	public static Type getCurrentStorageType() {
		return currentStorageType;
	}

	/**
	 * Storage initialization
	 * @param newStorageType new storage type or null for last used or default storage
	 */
	public static void init(Context context, Type newStorageType) {
		AppLog.d(TAG, "init() call. Initialized: " + initialized +
				" Current storage: " + String.valueOf(currentStorageType) +
				" New storage: " + String.valueOf(newStorageType));

		if (initialized && currentStorageType == newStorageType) {
			return;
		}

		final boolean lastUsedOrDefault = newStorageType == null;
		if (lastUsedOrDefault) {
			final SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
			newStorageType = Type.valueOf(prefs.getString(STORAGE_TYPE, DEFAULT_STORAGE.toString()));
		}

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

		final SharedPreferences.Editor prefsEditor =
				context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
		prefsEditor.putString(STORAGE_TYPE, newStorageType.toString());
		prefsEditor.commit();

		currentStorageType = newStorageType;
		initialized = true;
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
