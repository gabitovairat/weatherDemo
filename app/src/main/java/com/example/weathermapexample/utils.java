package com.example.weathermapexample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.KeyEvent;

/**
 * Created by Айрат on 14.02.2016.
 */
public class utils {

    public static Boolean checkNetworkState(Context c) {
        boolean wifiConnected = false, mobileConnected = false;
        ConnectivityManager connMgr = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }

        return wifiConnected || mobileConnected;
        // return true;
    }

    public static void  showNoInternetConnection(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.activity);
        builder.setMessage(R.string.not_connected_to_internet)
                .setPositiveButton(R.string.settings_name,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.activity.startActivity(new Intent(Settings.ACTION_SETTINGS));
                            }
                        })
                .setNegativeButton(R.string.continue_without_net,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                return;
                            }
                        });

        builder.create().show();
    }
}
