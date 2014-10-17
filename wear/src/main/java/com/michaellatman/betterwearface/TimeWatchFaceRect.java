package com.michaellatman.betterwearface;

import android.support.wearable.view.WatchViewStub;

/**
 * Created by michael on 9/20/14.
 */
public class TimeWatchFaceRect extends TimeWatchFace {
    @Override
    protected void doSetup() {
        setContentView(R.layout.main_square);

        super.doSetup();
    }

}
