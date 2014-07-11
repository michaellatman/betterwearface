package com.michaellatman.betterwearface;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReciever extends BroadcastReceiver {
    public BootReciever() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, UpdateWeatherService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, 15*60*1000,pi); // Millisec * Second * Minute

        Intent settings = new Intent(context, SettingsSyncService.class);
        settings.putExtra("loud",false);
        PendingIntent psettings = PendingIntent.getService(context, 0, settings, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, 30*60*1000,psettings);
    }
}
