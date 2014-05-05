package com.iliakplv.notes.notes.dropbox;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;

public final class DropboxHelper {

	private static final int REQUEST_LINK_TO_DBX = 42;
	private static final String APP_KEY = "cyla6oz3c3vuje3";
	private static final String APP_SECRET = "blt7jatmxpojwiz";

	private DbxAccountManager mAccountManager;
	private DbxAccount mAccount;


	private void tryLinkAccount(Activity accountLinkActivity) {
		mAccountManager = DbxAccountManager.getInstance(accountLinkActivity.getApplicationContext(), APP_KEY, APP_SECRET);

		if (mAccountManager.hasLinkedAccount()) {
			Toast.makeText(accountLinkActivity, "Account already linked", Toast.LENGTH_LONG).show();
		} else {
			mAccountManager.startLink(accountLinkActivity, REQUEST_LINK_TO_DBX);
		}
	}

	public void onAccountLinkActivityResult(Activity accountLinkActivity, int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LINK_TO_DBX) {
			if (resultCode == Activity.RESULT_OK) {
				mAccount = mAccountManager.getLinkedAccount();
				Toast.makeText(accountLinkActivity, "Account has been linked", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(accountLinkActivity, "Account link failed!", Toast.LENGTH_LONG).show();
			}
		}
	}

}
