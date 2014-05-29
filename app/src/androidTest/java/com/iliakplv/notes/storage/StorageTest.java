package com.iliakplv.notes.storage;

import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.Storage;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class StorageTest extends TestCase {

	private static final int ALL_LABELS = NotesStorage.NOTES_FOR_ALL_LABELS;

	List<Serializable> notesIds;
	List<Serializable> labelsIds;
	NotesStorage storage;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		storage = Storage.getStorage();
		notesIds = new ArrayList<Serializable>();
		labelsIds = new ArrayList<Serializable>();

		notesIds.add(storage.insertNote(new TextNote("title0", "body0")));
		notesIds.add(storage.insertNote(new TextNote("title1", "body1")));
		notesIds.add(storage.insertNote(new TextNote("title2", "body2")));

		labelsIds.add(storage.insertLabel(new Label("label0", 1)));
		labelsIds.add(storage.insertLabel(new Label("label1", 6)));
		labelsIds.add(storage.insertLabel(new Label("label2", 0)));

		storage.insertLabelToNote(notesIds.get(0), labelsIds.get(0));
		storage.insertLabelToNote(notesIds.get(0), labelsIds.get(1));
		storage.insertLabelToNote(notesIds.get(1), labelsIds.get(1));

	}

	public void testDatabase() {

		// title0, title1, title2
		// label0, label1, label2

		// title0 -- label0, label1
		// title1 -- label1
		// title2 -- (no labels)

		Assert.assertEquals(notesIds.size(), storage.getNotesForLabelCount(ALL_LABELS));
		Assert.assertEquals(notesIds.size(), storage.getNotesForLabel(ALL_LABELS).size());
		Assert.assertEquals(labelsIds.size(), storage.getAllLabels().size());

		Assert.assertEquals(2, storage.getLabelsForNote(notesIds.get(0)).size());
		Assert.assertEquals(1, storage.getLabelsForNote(notesIds.get(1)).size());
		Assert.assertEquals(0, storage.getLabelsForNote(notesIds.get(2)).size());

		Assert.assertEquals(1, storage.getNotesForLabel(labelsIds.get(0)).size());
		Assert.assertEquals(2, storage.getNotesForLabel(labelsIds.get(1)).size());
		Assert.assertEquals(0, storage.getNotesForLabel(labelsIds.get(2)).size());



		// Change something...

		storage.deleteLabel(labelsIds.get(1));
		labelsIds.remove(1);
		storage.deleteLabel(labelsIds.get(1));
		labelsIds.remove(1);

		// title0, title1, title2
		// label0

		// title0 -- label0
		// title1 -- (no labels)
		// title2 -- (no labels)

		Assert.assertEquals(notesIds.size(), storage.getNotesForLabelCount(ALL_LABELS));
		Assert.assertEquals(notesIds.size(), storage.getNotesForLabel(ALL_LABELS).size());
		Assert.assertEquals(labelsIds.size(), storage.getAllLabels().size());

		Assert.assertEquals(1, storage.getLabelsForNote(notesIds.get(0)).size());
		Assert.assertEquals(0, storage.getLabelsForNote(notesIds.get(1)).size());
		Assert.assertEquals(0, storage.getLabelsForNote(notesIds.get(2)).size());

		Assert.assertEquals(1, storage.getNotesForLabel(labelsIds.get(0)).size());



		// Change something...

		storage.deleteNote(notesIds.get(0));
		storage.deleteNote(notesIds.get(1));
		storage.deleteNote(notesIds.get(2));
		final Serializable removedId = notesIds.get(0);
		notesIds.clear();

		// (no notes)
		// label0

		Assert.assertEquals(notesIds.size(), storage.getNotesForLabelCount(ALL_LABELS));
		Assert.assertEquals(notesIds.size(), storage.getNotesForLabel(ALL_LABELS).size());
		Assert.assertEquals(labelsIds.size(), storage.getAllLabels().size());

		Assert.assertEquals(0, storage.getNotesForLabel(labelsIds.get(0)).size());
		Assert.assertNull(storage.getNote(removedId));


		// ...finally remove label (label0)

		storage.deleteLabel(labelsIds.get(0));
	}
}
