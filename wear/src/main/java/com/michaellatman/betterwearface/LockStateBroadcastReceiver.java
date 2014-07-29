package com.michaellatman.betterwearface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by michael on 7/29/14.
 */
public class LockStateBroadcastReceiver extends BroadcastReceiver {
    LockStateListener listener = null;
    public  LockStateBroadcastReceiver( LockStateListener listener){
        this.listener = listener;

    }
    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.michaellatman.wearlock.STATE_UNLOCK");
        intentFilter.addAction("com.michaellatman.wearlock.STATE_LOCK");

        context.registerReceiver(this, intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("com.michaellatman.wearlock.STATE_UNLOCK")){
            listener.onStateChange(false);
        }
        else{
            listener.onStateChange(true);
        }
    }
    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }

    public interface LockStateListener{
        public void onStateChange(boolean locked);
    }
}
