package com.iliakplv.notes.notes;

import java.util.Comparator;

public class LabelComparator implements Comparator<Label> {

	@Override
	public int compare(Label lhs, Label rhs) {
		return lhs.getName().compareTo(rhs.getName());
	}

}
