package com.michaellatman.betterwearface;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsSyncService extends Service {
    private Context context = this;
    private SettingsSyncService me = this;
    private LocationManager mLocationManager;
    public Location location = null;
    public SettingsSyncService() {
    }

    GoogleApiClient mGoogleApiClient = null;

    public static void startSync(Context context){
        Intent intent = new Intent();
        intent.setAction("com.michaellatman.settings_update");
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        builder.addApi(Wearable.API);
        builder.addConnectionCallbacks(mConnectedCallback);
        mGoogleApiClient = builder.build();
        mGoogleApiClient.connect();

        return START_STICKY;

    }

    private GoogleApiClient.ConnectionCallbacks mConnectedCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            if(mGoogleApiClient != null) {
                PutDataMapRequest dataMap = PutDataMapRequest.create("/settings");

                //Put settings here.
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                Toast.makeText(getApplicationContext(),preferences.getString(SettingsFragment.KEY_PREF_TEMP_FORMAT,""),Toast.LENGTH_LONG).show();

                PutDataRequest request = dataMap.asPutDataRequest();
                com.google.android.gms.common.api.PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                        .putDataItem(mGoogleApiClient, request);
                pendingResult.setResultCallback(mResultCallback);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };
    private final ResultCallback mResultCallback = new ResultCallback() {
        @Override
        public void onResult(Result result) {
            if(result.getStatus().isSuccess()){
                Log.d("SettingsSync","SUCCESS");
                mGoogleApiClient.disconnect();
            }
            else{
                Log.d("SettingsSync","Fail :(");
                mGoogleApiClient.disconnect();
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
