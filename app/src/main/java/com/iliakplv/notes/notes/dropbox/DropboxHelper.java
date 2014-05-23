package com.iliakplv.notes.notes.dropbox;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.iliakplv.notes.NotesApplication;


/**
 * Temporary class for Dropbox account management
 */

public final class DropboxHelper {

	private static final int REQUEST_LINK_TO_DBX = 242424;
	private static final String APP_KEY = "cyla6oz3c3vuje3";
	private static final String APP_SECRET = "blt7jatmxpojwiz";

	private static DbxAccountManager accountManager;
	private static DbxAccount account;


	// call from activity
	public static void tryLinkAccount(Activity accountLinkActivity) {
		accountManager = DbxAccountManager.getInstance(accountLinkActivity.getApplicationContext(), APP_KEY, APP_SECRET);

		if (accountManager.hasLinkedAccount()) {
			Toast.makeText(accountLinkActivity, "Dropbox account linked", Toast.LENGTH_LONG).show();
			account = accountManager.getLinkedAccount();
		} else {
			accountManager.startLink(accountLinkActivity, REQUEST_LINK_TO_DBX);
		}
	}

	// call from activity's onActivityResult()
	public static void onAccountLinkActivityResult(Activity accountLinkActivity, int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LINK_TO_DBX) {
			if (resultCode == Activity.RESULT_OK) {
				account = accountManager.getLinkedAccount();
				Toast.makeText(accountLinkActivity, "Dropbox account has been linked", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(accountLinkActivity, "Dropbox account link failed!", Toast.LENGTH_LONG).show();
			}
		}
	}


	public static synchronized DbxAccount getAccount() {
		if (account == null) {
			if (accountManager == null) {
				accountManager = DbxAccountManager.getInstance(NotesApplication.getContext(), APP_KEY, APP_SECRET);
			}
			if (accountManager.hasLinkedAccount()) {
				account = accountManager.getLinkedAccount();
			} else {
				throw new RuntimeException("No linked dropbox account!");
			}
		}
		return account;
	}
}