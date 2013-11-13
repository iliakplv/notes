package com.iliakplv.notes.notes.backup;

import java.io.File;

/**
 * Autor: Ilya Kopylov
 * Date:  13.11.2013
 */
public interface StorageTask {

	public void start();

	public void cancel();

	public void setObserver(Observer observer);


	/*********************************************
	 *
	 *            Inner classes
	 *
	 *********************************************/

	public interface Observer {
		public void onStart();
		public void onProgressUpdate(int currentFileCount, int totalFileCount);
		public void onFinish();
		public void onError(int errorCode);
	}
}

//File root = android.os.Environment.getExternalStorageDirectory();
//
//File dir = new File (root.getAbsolutePath() + "/notes");
//dir.mkdirs();
//
//final List<NotesDatabaseEntry> notesEntries = NotesDatabaseFacade.getInstance().getAllNotes();
//for (NotesDatabaseEntry entry : notesEntries) {
//
//final String title = entry.getNote().getTitle();
//String filename = StringUtils.isBlank(title) ? "empty_note" : title;
//File file = new File(dir, filename);
//while (file.exists()) {
//		filename = filename + "_1";
//file = new File(dir, filename);
//}
//
//		byte[] noteText = entry.getNote().getBody().getBytes();
//
//try {
//		FileOutputStream f = new FileOutputStream(file);
//
//f.write(noteText);
//f.close();
//} catch (FileNotFoundException e) {
//		e.printStackTrace();
//} catch (IOException e) {
//		e.printStackTrace();
//}
//
//		}
//
//////////////////////////////////////
//
//private void checkExternalMedia(){
//		boolean mExternalStorageAvailable = false;
//boolean mExternalStorageWriteable = false;
//String state = Environment.getExternalStorageState();
//
//if (Environment.MEDIA_MOUNTED.equals(state)) {
//		// Can read and write the media
//		mExternalStorageAvailable = mExternalStorageWriteable = true;
//} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
//		// Can only read the media
//		mExternalStorageAvailable = true;
//mExternalStorageWriteable = false;
//} else {
//		// Can't read or write
//		mExternalStorageAvailable = mExternalStorageWriteable = false;
//}
//		}