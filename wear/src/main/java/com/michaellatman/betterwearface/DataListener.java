package com.michaellatman.betterwearface;


import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import java.util.Calendar;

/**
 * An interface to listen to the clock ticking.
 */
public interface DataListener {
    void onDataChange(String node, DataMap changed);
}