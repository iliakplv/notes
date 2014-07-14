package com.iliakplv.notes.analytics;

public enum Event {

	// if event name has 'Click' suffix this event tracks relevant button click

	// TODO create events for use-cases those can be started from different points
	// notes labels dialog, note share, label creation from note details
	// TODO track note/label real save/delete !!!
	// TODO do not track note temp save/delete !!!
	// TODO disable event tracking during data transfer !!!


//	Notes

	NoteCreateClick,
	NoteShow,
	NoteEdit,
	NoteDelete,
	NoteInfoShow,
	NoteShareDialogShow,
	NotesSortOrderSelect,

//	Labels

	LabelCreateClick,
	LabelSelect,
	LabelEdit,
	LabelDelete,
	LabelAddToNote,
	LabelRemoveFromNote,

//	Dropbox
	
	DropboxLinkAttempt,
	DropboxLinkSuccess,
	DropboxLinkFail,
	DropboxSyncAuto,
	DropboxSyncManual,
	
//	Other

	SettingsOpening
}
