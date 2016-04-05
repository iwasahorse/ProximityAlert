package com.homework.pavement.proximityalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name");
        boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false); // getBooleanExtra(String name, boolean defaultValue)
        if(isEntering)
            Toast.makeText(context, String.format("%s 에 접근중입니다.", name), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context,  String.format("%s 에서 벗어납니다.", name), Toast.LENGTH_LONG).show();
    }
}
