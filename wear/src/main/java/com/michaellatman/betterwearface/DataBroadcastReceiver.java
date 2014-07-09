package com.michaellatman.betterwearface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import java.util.Calendar;

/**
 * Created by michael on 7/8/14.
 */
public class DataBroadcastReceiver extends BroadcastReceiver{
    private DataListener mDataListener;

    public DataBroadcastReceiver(DataListener dataListener) {
        mDataListener = dataListener;
    }

    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.michaellatman.betterwearface.DATA_CHANGE");

        context.registerReceiver(this, intentFilter);
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Data Change Broadcast", "Recieved");
        mDataListener.onDataChange(intent.getStringExtra("NAME"),DataMap.fromBundle(intent.getBundleExtra("DATA")));
    }
}
