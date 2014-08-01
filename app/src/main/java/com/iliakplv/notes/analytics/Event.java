package com.iliakplv.notes.analytics;

public enum Event {

	// if event name has 'Click' suffix this event tracks relevant button click

/*	Notes */

	// tracked from UI
	NoteCreateClick,
	NoteShow,
	NoteInfoShow,
	NoteShareDialogShow,
	NotesSortOrderSelect,
	// tracked from storage
	NoteCreate,
	NoteEdit,
	NoteDelete,
	// tracked from search implementation
	SearchUsed,

/*	Labels */

	// tracked from UI
	LabelCreateClick,
	LabelSelect,
	// tracked from storage
	LabelCreate,
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

	SettingsOpening,
	AboutOpening
}
