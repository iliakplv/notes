package com.iliakplv.notes.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.iliakplv.notes.NotesApplication;

public class ConnectivityUtils {

    private ConnectivityUtils() {
        throw new AssertionError("Instance creation not allowed!");
    }

    public static boolean isNetworkConnected() {
        final ConnectivityManager cm = (ConnectivityManager) NotesApplication.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

}
