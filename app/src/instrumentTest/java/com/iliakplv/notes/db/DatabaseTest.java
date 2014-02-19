package com.iliakplv.notes.db;

import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;


public class DatabaseTest extends TestCase {
	
	private static final int ALL_LABELS = NotesDatabaseFacade.ALL_LABELS;

	List<Integer> notesIds;
	List<Integer> labelsIds;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		notesIds = new ArrayList<Integer>();
		labelsIds = new ArrayList<Integer>();

		notesIds.add(NotesDatabaseFacade.getInstance().insertNote(new TextNote("title0", "body0")));
		notesIds.add(NotesDatabaseFacade.getInstance().insertNote(new TextNote("title1", "body1")));
		notesIds.add(NotesDatabaseFacade.getInstance().insertNote(new TextNote("title2", "body2")));

		labelsIds.add(NotesDatabaseFacade.getInstance().insertLabel(new Label("label0", 1)));
		labelsIds.add(NotesDatabaseFacade.getInstance().insertLabel(new Label("label1", 6)));
		labelsIds.add(NotesDatabaseFacade.getInstance().insertLabel(new Label("label2", 0)));

		NotesDatabaseFacade.getInstance().insertLabelToNote(notesIds.get(0), labelsIds.get(0));
		NotesDatabaseFacade.getInstance().insertLabelToNote(notesIds.get(0), labelsIds.get(1));
		NotesDatabaseFacade.getInstance().insertLabelToNote(notesIds.get(1), labelsIds.get(1));

	}

	public void testDatabase() {

		// title0, title1, title2
		// label0, label1, label2

		// title0 -- label0, label1
		// title1 -- label1
		// title2 -- (no labels)

		Assert.assertEquals(notesIds.size(), NotesDatabaseFacade.getInstance().getNotesForLabelCount(ALL_LABELS));
		Assert.assertEquals(notesIds.size(), NotesDatabaseFacade.getInstance().getNotesForLabel(ALL_LABELS).size());
		Assert.assertEquals(labelsIds.size(), NotesDatabaseFacade.getInstance().getAllLabels().size());

		Assert.assertEquals(2, NotesDatabaseFacade.getInstance().getLabelsForNote(notesIds.get(0)).size());
		Assert.assertEquals(1, NotesDatabaseFacade.getInstance().getLabelsForNote(notesIds.get(1)).size());
		Assert.assertEquals(0, NotesDatabaseFacade.getInstance().getLabelsForNote(notesIds.get(2)).size());

		Assert.assertEquals(1, NotesDatabaseFacade.getInstance().getNotesForLabel(labelsIds.get(0)).size());
		Assert.assertEquals(2, NotesDatabaseFacade.getInstance().getNotesForLabel(labelsIds.get(1)).size());
		Assert.assertEquals(0, NotesDatabaseFacade.getInstance().getNotesForLabel(labelsIds.get(2)).size());



		// Change something...

		NotesDatabaseFacade.getInstance().deleteLabel(labelsIds.get(1));
		labelsIds.remove(1);
		NotesDatabaseFacade.getInstance().deleteLabel(labelsIds.get(1));
		labelsIds.remove(1);

		// title0, title1, title2
		// label0

		// title0 -- label0
		// title1 -- (no labels)
		// title2 -- (no labels)

		Assert.assertEquals(notesIds.size(), NotesDatabaseFacade.getInstance().getNotesForLabelCount(ALL_LABELS));
		Assert.assertEquals(notesIds.size(), NotesDatabaseFacade.getInstance().getNotesForLabel(ALL_LABELS).size());
		Assert.assertEquals(labelsIds.size(), NotesDatabaseFacade.getInstance().getAllLabels().size());

		Assert.assertEquals(1, NotesDatabaseFacade.getInstance().getLabelsForNote(notesIds.get(0)).size());
		Assert.assertEquals(0, NotesDatabaseFacade.getInstance().getLabelsForNote(notesIds.get(1)).size());
		Assert.assertEquals(0, NotesDatabaseFacade.getInstance().getLabelsForNote(notesIds.get(2)).size());

		Assert.assertEquals(1, NotesDatabaseFacade.getInstance().getNotesForLabel(labelsIds.get(0)).size());



		// Change something...

		NotesDatabaseFacade.getInstance().deleteNote(notesIds.get(0));
		NotesDatabaseFacade.getInstance().deleteNote(notesIds.get(1));
		NotesDatabaseFacade.getInstance().deleteNote(notesIds.get(2));
		final int removedId = notesIds.get(0);
		notesIds.clear();

		// (no notes)
		// label0

		Assert.assertEquals(notesIds.size(), NotesDatabaseFacade.getInstance().getNotesForLabelCount(ALL_LABELS));
		Assert.assertEquals(notesIds.size(), NotesDatabaseFacade.getInstance().getNotesForLabel(ALL_LABELS).size());
		Assert.assertEquals(labelsIds.size(), NotesDatabaseFacade.getInstance().getAllLabels().size());

		Assert.assertEquals(0, NotesDatabaseFacade.getInstance().getNotesForLabel(labelsIds.get(0)).size());
		Assert.assertNull(NotesDatabaseFacade.getInstance().getNote(removedId));


		// ...finally remove label (label0)

		NotesDatabaseFacade.getInstance().deleteLabel(labelsIds.get(0));
	}
}
