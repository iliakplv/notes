package com.iliakplv.notes.analytics;

public enum Event {

	// if event name has 'Click' suffix this event tracks relevant button click

/*	Notes */

	NoteCreateClick,
	NoteShow,
	NoteInfoShow,
	NoteShareDialogShow,
	NotesSortOrderSelect,
	ShareIntentReceived,

	NoteCreate,
	NoteEdit,
	NoteDelete,

	SearchUsed,

/*	Labels */

	LabelCreateClick,
	LabelSelect,

	LabelCreate,
	LabelEdit,
	LabelDelete,
	LabelAddToNote,
	LabelRemoveFromNote,

/*	Dropbox */

	DropboxLinkAttempt,
	DropboxLinkSuccess,
	DropboxLinkFail,
	DropboxSyncAuto,

	DropboxSyncManual,
	
/*	Other */

	SettingsOpening,
	AboutOpening
}
