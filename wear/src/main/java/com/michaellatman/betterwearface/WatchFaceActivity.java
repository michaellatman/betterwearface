package com.michaellatman.betterwearface;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

/**
 * Created by michael on 7/10/14.
 */
public class WatchfaceActivity extends Activity implements DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<NodeApi.GetConnectedNodesResult>, LockStateBroadcastReceiver.LockStateListener {

    protected GoogleApiClient mGoogleApiClient;
    protected Boolean connected = false;
    protected String nodeID = "";
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    private ClockListener mClockListener = new ClockListener() {
        @Override
        public void onTimeChanged(Calendar calendar) {
            wakeLock.acquire(2000);
            updateTime(calendar);
        }
    };
    private Clock mClock = new Clock(Looper.getMainLooper(), mClockListener);
    private TimeBroadcastReceiver mTimeBroadcastReceiver = new TimeBroadcastReceiver(mClockListener);
    private DataBroadcastReceiver mDataBroadcastReceiver = new DataBroadcastReceiver(this);
    private LockStateBroadcastReceiver mLockStateReceiver = new LockStateBroadcastReceiver(this);
    ImageView mLockState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("WatchFace", "onCreate();");
        super.onCreate(savedInstanceState);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);

        builder.addConnectionCallbacks(this);

        builder.addOnConnectionFailedListener(this);
        builder.addApi(Wearable.API);
        mGoogleApiClient = builder.build();
        mGoogleApiClient.connect();
        doSetup();

        mLockState = (ImageView) findViewById(R.id.lockState);
        if(mLockState!=null) mLockState.setVisibility(View.GONE);
        else Log.d("Lockstate","null");
        Calendar calendar = Calendar.getInstance();
        updateTime(calendar);
        // Start the clock.
        mClock.start();

        mTimeBroadcastReceiver.register(this);
        mDataBroadcastReceiver.register(this);
        mLockStateReceiver.register(this);
    }
    @Override
    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
        List<Node> nodes = getConnectedNodesResult.getNodes();
        if (nodes.size() > 0) {
            Log.d("SyncNodes", nodes.get(0).getId());
            nodeID = nodes.get(0).getId();
            doRestore();
        }
    }

    protected Calendar getTime(){
        return Calendar.getInstance();
    }
    protected Uri buildURI(String path){
        return new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).authority(nodeID).path(path).build();
    }
    protected void fetch(final String path){
        if(connected) {
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.getDataItem(mGoogleApiClient, buildURI(path));
            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    if (dataItemResult.getDataItem() != null) {
                        String path = dataItemResult.getDataItem().getUri().getPath();
                        onDataChange(path.substring(path.lastIndexOf('/') + 1),DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap());
                    }
                    else {
                        onDataNotExist(path);
                    }
                }
            });
        }
    }
    protected void fetchAsset(final String path, final String loc){
        if(connected) {
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.getDataItem(mGoogleApiClient, buildURI(path));
            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    if (dataItemResult.getDataItem() != null) {
                        Asset loaded = DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap().getAsset(loc);
                        if(loaded == null){

                        }
                        else {
                            loadBitmapFromAsset(loaded, new ResultCallback<DataApi.GetFdForAssetResult>() {
                                @Override
                                public void onResult(DataApi.GetFdForAssetResult getFdForAssetResult) {
                                    onAssetLoad(path, loc, BitmapFactory.decodeStream(getFdForAssetResult.getInputStream()));
                                }
                            });
                        }

                    }
                    else {
                        onDataNotExist(path);
                    }
                }
            });
        }
    }
    public void loadBitmapFromAsset(Asset asset, ResultCallback<DataApi.GetFdForAssetResult> callback ) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).setResultCallback(callback);
    }
    protected void onAssetLoad(String path, String loc, Bitmap bitmap){

    }
    protected void onDataNotExist(String path){

    }

    protected void doSetup(){

    }
    protected void updateTime(Calendar calendar){

    }
    @Override
    public void onDataChange(String node, final DataMap changed) {
        wakeLock.acquire(3000);
        Log.d("Data Change",node);
    }



    private void getRemoteNodeId(ResultCallback<NodeApi.GetConnectedNodesResult> result) {
        HashSet<String> results = new HashSet<String>();

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(result);

    }
    protected void doRestore(){

    }






    @Override
    protected void onPause() {
        //  onPause();
        Log.v("WatchFace", "onPause();");
        mClock.stop();
        super.onPause();
        //background.setImageResource(R.drawable.example_watch_background);
    }

    @Override
    protected void onResume() {
        //  onResume();
        mClock.start();
        Log.v("WatchFace", "onResume();");
        super.onResume();
        //background.setImageResource(R.drawable.example_watch_background);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimeBroadcastReceiver.unregister(this);
        mDataBroadcastReceiver.unregister(this);
        mLockStateReceiver.unregister(this);
        mGoogleApiClient.disconnect();
        connected=false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("WatchFace-GAPI","Connected");

        connected = true;
        getRemoteNodeId(this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("WatchFace-GAPI","Suspended");
        connected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("WatchFace-GAPI","Failed");
        connected = false;
        mGoogleApiClient.connect();
    }


    @Override
    public void onStateChange(boolean locked) {
        Log.d("Lol","T");
        if(mLockState!=null){
            mLockState.setVisibility(View.VISIBLE);
            if(locked)mLockState.setImageDrawable(getResources().getDrawable(R.drawable.locked));
            else mLockState.setImageDrawable(getResources().getDrawable(R.drawable.unlocked));
        }
    }
}
