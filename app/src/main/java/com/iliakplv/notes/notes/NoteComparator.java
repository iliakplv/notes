package com.iliakplv.notes.notes;

import java.util.Comparator;

public class NoteComparator implements Comparator<AbstractNote> {

	NotesUtils.NoteSortOrder order;


	public NoteComparator() {
		this(NotesUtils.DEFAULT_SORT_ORDER);
	}

	public NoteComparator(NotesUtils.NoteSortOrder order) {
		if (order == null) {
			throw new NullPointerException("Order is null");
		}
		this.order = order;
	}


	public NotesUtils.NoteSortOrder getSortOrder() {
		return order;
	}

	public void setSortOrder(NotesUtils.NoteSortOrder order) {
		this.order = order;
	}


	@Override
	public int compare(AbstractNote lhs, AbstractNote rhs) {
		switch (order) {
			case Title:
				return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
			case CreateDateAscending:
				return lhs.getCreateTime().compareTo(rhs.getCreateTime());
			case CreateDateDescending:
				return rhs.getCreateTime().compareTo(lhs.getCreateTime());
			case ChangeDate: // Descending
				return rhs.getChangeTime().compareTo(lhs.getChangeTime());
			default:
				throw new IllegalArgumentException("Unknown sort order type: " + order.toString());
		}
	}
}
