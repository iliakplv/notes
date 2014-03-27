package com.iliakplv.notes.notes.db;

public class NotesDatabaseEntry<T> {

	private T entry;
	private int id;


	public NotesDatabaseEntry(T entry, int id) {
		setEntry(entry);
		setId(id);
	}


	public T getEntry() {
		return entry;
	}

	public void setEntry(T entry) {
		if (entry == null) {
			throw new NullPointerException("Entry is null");
		}
		this.entry = entry;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		if (id < 1) {
			throw new IllegalArgumentException("Wrong id value:" + id);
		}
		this.id = id;
	}

}
