package com.michaellatman.betterwearface;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataChangeReceiver extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        final List events = FreezableUtils
                .freezeIterable(dataEvents);

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);
        Log.e("Data", "!!");
        if (!connectionResult.isSuccess()) {
            Log.e("Data", "Failed to connect to GoogleApiClient.");
            return;
        }

        // Loop through the events and send a message
        // to the node that created the data item.
        /*NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Updated")
                        .setContentText("Now");

// Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

// Build the notification and issues it with notification manager.
        notificationManager.notify(1, notificationBuilder.build());*/

        for (DataEvent event : (List<DataEvent>)events) {
            Uri uri = event.getDataItem().getUri();

            // Get the node id from the host value of the URI
            String nodeId = uri.getHost();
            String path = uri.getPath();
            String idStr = path.substring(path.lastIndexOf('/') + 1);


            DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());
            Intent intent = new Intent("com.michaellatman.betterwearface.DATA_CHANGE");
            intent.putExtra("DATA", item.getDataMap().toBundle());
            intent.putExtra("NAME", idStr);
            this.sendBroadcast(intent);
            Log.d("SOMETHING","Text: "+ item.getDataMap().getString("text",""));

        }
    }
}
