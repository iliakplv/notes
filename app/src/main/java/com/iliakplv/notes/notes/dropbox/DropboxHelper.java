package com.iliakplv.notes.notes.dropbox;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.notes.storage.Storage;


/**
 * Temporary class for Dropbox account management
 */

public final class DropboxHelper {

    private static final String APP_KEY = "cyla6oz3c3vuje3";
    private static final String APP_SECRET = "blt7jatmxpojwiz";

    private static DbxAccountManager accountManager = null;
    private static DbxAccount account = null;

    private static ConnectivityReceiver connectivityReceiver;


    private static void initAccountManagerIfNeeded(Context context) {
        if (accountManager == null) {
            accountManager = DbxAccountManager.getInstance(context, APP_KEY, APP_SECRET);
        }
    }

    public static synchronized boolean hasLinkedAccount() {
        if (account != null) {
            return true;
        } else {
            initAccountManagerIfNeeded(NotesApplication.getContext());
            return accountManager.hasLinkedAccount();
        }
    }

    public static synchronized DbxAccount getAccount() {
        if (account == null) {
            initAccountManagerIfNeeded(NotesApplication.getContext());
            if (accountManager.hasLinkedAccount()) {
                account = accountManager.getLinkedAccount();
            }
        }
        return account;
    }

    public static void initSynchronization() {
        if (connectivityReceiver == null &&
                Storage.getCurrentStorageType() == Storage.Type.Dropbox &&
                hasLinkedAccount()) {

            connectivityReceiver = new ConnectivityReceiver();
            NotesApplication.getContext().registerReceiver(connectivityReceiver,
                    new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    public static void disableSynchronization() {
        if (connectivityReceiver != null) {
            NotesApplication.getContext().unregisterReceiver(connectivityReceiver);
            connectivityReceiver = null;
        }
    }


    /**
     * ***************** Inner classes *******************
     */

    private static final class ConnectivityReceiver extends BroadcastReceiver {

        private static final int AUTO_SYNC_INTERVAL_MILLIS = 1000;
        private static final String LAST_AUTO_SYNC_TIME = "last_auto_sync_time";

        private final SharedPreferences sharedPreferences;

        private ConnectivityReceiver() {
            sharedPreferences = NotesApplication.getContext()
                    .getSharedPreferences(ConnectivityReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.isConnected()) {
                if (isSyncIntervalExceeded()) {
                    Storage.getStorage().sync();
                    updateLastSyncTime();
                }
            }
        }

        private void updateLastSyncTime() {
            sharedPreferences.edit().putLong(LAST_AUTO_SYNC_TIME, System.currentTimeMillis()).apply();
        }

        private boolean isSyncIntervalExceeded() {
            return System.currentTimeMillis() - sharedPreferences.getLong(LAST_AUTO_SYNC_TIME, 0)
                    >= AUTO_SYNC_INTERVAL_MILLIS;
        }
    }
}
