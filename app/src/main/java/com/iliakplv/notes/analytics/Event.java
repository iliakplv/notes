package com.iliakplv.notes.analytics;

public enum Event {

	// if event name has 'Click' suffix this event tracks relevant button click

	// TODO create events for use-cases those can be started from different points
	// notes labels dialog, note share, label creation from note details
	// TODO disable event tracking during data transfer !!!
	// TODO test


/*	Notes */

	// tracked from UI
	NoteCreateClick,
	NoteShow,
	// TODO tracked from storage
	NoteEdit,
	NoteDelete,
	// tracked from UI
	NoteInfoShow,
	NoteShareDialogShow,
	NotesSortOrderSelect,

/*	Labels */

	// tracked from UI
	LabelCreateClick,
	LabelSelect,
	// tracked from storage
	LabelEdit,
	LabelDelete,
	LabelAddToNote,
	LabelRemoveFromNote,

/*	Dropbox */

	// tracked from dropbox class
	DropboxLinkAttempt,
	DropboxLinkSuccess,
	DropboxLinkFail,
	DropboxSyncAuto,
	// tracked from UI
	DropboxSyncManual,
	
/*	Other */

	SettingsOpening
}
