package com.michaellatman.betterwearface;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class SettingsSyncService extends Service {
    private Context context = this;
    private SettingsSyncService me = this;
    private LocationManager mLocationManager;
    public Location location = null;
    public Intent mIntent;
    public Boolean loud = true;
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
        mIntent = intent;
        loud = intent.getBooleanExtra("loud",true);
        mGoogleApiClient = builder.build();
        mGoogleApiClient.connect();


        return START_REDELIVER_INTENT;

    }
    public static Bitmap loadBitmap(Context context, String picName){
        Bitmap b = null;
        FileInputStream fis;
        try {
            fis = context.openFileInput(picName);
            b = BitmapFactory.decodeStream(fis);
            fis.close();

        }
        catch (FileNotFoundException e) {
            Log.d("file", "file not found");
            e.printStackTrace();
        }
        catch (IOException e) {
            Log.d("file", "io exception");
            e.printStackTrace();
        }
        return b;
    }
    private GoogleApiClient.ConnectionCallbacks mConnectedCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            if(mGoogleApiClient != null) {
                PutDataMapRequest dataMap = PutDataMapRequest.create("/settings");

                //Put settings here.
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                //Toast.makeText(getApplicationContext(),"Syncing Settings",Toast.LENGTH_LONG).show();
                //dataMap.getDataMap().putInt("someRandom",new Random().nextInt(30));





                if (preferences.getBoolean("backgroundSet",false)) {
                    if(loud)Toast.makeText(getApplicationContext(),"Syncing Background",Toast.LENGTH_LONG).show();
                    Bitmap bitmap = loadBitmap(getApplicationContext(),"background");
                    ByteArrayOutputStream s = new ByteArrayOutputStream();
                    bitmap = Bitmap.createScaledBitmap(bitmap, 312, 312, true);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, s);
                    Log.d("Sync","Add background");
                    byte[] byteArray = s.toByteArray();
                    dataMap.getDataMap().putBoolean("customBackground", true);
                    dataMap.getDataMap().putAsset("background", Asset.createFromBytes(byteArray));
                } else {
                    if(loud)Toast.makeText(getApplicationContext(),"Removed Background",Toast.LENGTH_LONG).show();
                    Log.d("Sync","Remove background");
                    dataMap.getDataMap().putBoolean("customBackground", false);
                }

                if(dataMap.getDataMap().size()>0) {
                    PutDataRequest request = dataMap.asPutDataRequest();
                    com.google.android.gms.common.api.PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                            .putDataItem(mGoogleApiClient, request);
                    pendingResult.setResultCallback(mResultCallback);
                }
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
