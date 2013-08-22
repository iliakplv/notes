package com.iliakplv.notes.notes.db;

import com.iliakplv.notes.notes.AbstractNote;

/**
 * Autor: Ilya Kopylov
 * Date:  22.08.2013
 */
public class DatabaseEntry {

	private AbstractNote note;
	private int id;


	public DatabaseEntry(AbstractNote note, int id) {
		setNote(note);
		setId(id);
	}


	public AbstractNote getNote() {
		return note;
	}

	public void setNote(AbstractNote note) {
		if (note == null) {
			throw new NullPointerException("Note is null");
		}
		this.note = note;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		if (id < 1) {
			throw new IllegalArgumentException("Wrong id value");
		}
		this.id = id;
	}

}
