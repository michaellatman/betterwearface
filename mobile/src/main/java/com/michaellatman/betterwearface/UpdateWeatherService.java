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

import com.google.android.gms.common.ConnectionResult;
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

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class UpdateWeatherService extends Service {
    private Context context = this;
    private UpdateWeatherService me = this;
    private LocationManager mLocationManager;
    public Location location = null;
    public UpdateWeatherService() {
    }

    GoogleApiClient mGoogleApiClient = null;

    public static void update(Context context){
        Intent intent = new Intent();
        intent.setAction("com.michaellatman.weatherupdate");
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        builder.addApi(Wearable.API);
        builder.addConnectionCallbacks(mConnectedCallback);
        mGoogleApiClient = builder.build();
        Log.d("Service","Start!");
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1,
                1, mLocationListener);
        return START_STICKY;

    }

    private GoogleApiClient.ConnectionCallbacks mConnectedCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            if(mGoogleApiClient != null){


                RequestParams params = new RequestParams();
                params.add("q", location.getLatitude() + "," + location.getLongitude());
                WeatherClient.get(params, new JsonHttpResponseHandler(){

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {


                            JSONObject current = response.getJSONObject("data").getJSONArray("current_condition").getJSONObject(0);
                            String condition = current.getString("weatherCode");
                            PutDataMapRequest dataMap = PutDataMapRequest.create("/weather");
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                            if(preferences.getString(SettingsFragment.KEY_PREF_TEMP_FORMAT,"Fahrenheit").equals("Fahrenheit")){
                                dataMap.getDataMap().putString("text", current.getString("temp_F")+"˚ F");
                            }
                            else
                                dataMap.getDataMap().putString("text", current.getString("temp_C")+"˚ C");

                            dataMap.getDataMap().putString("condition", condition);
                            //dataMap.getDataMap().putString("random", ""+new Random().nextInt(20));


                            PutDataRequest request = dataMap.asPutDataRequest();
                            com.google.android.gms.common.api.PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                                    .putDataItem(mGoogleApiClient, request);
                            pendingResult.setResultCallback(mResultCallback);





                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("Response",response.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d("Response","failure");
                    }
                });
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
                Log.d("Weather","SUCCESS");
            }
            else{
                Log.d("Weather","Fail :(");
            }
        }
    };
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here

            mLocationManager.removeUpdates(this);
                    me.location = location;
                    mGoogleApiClient.connect();


                    Log.d("Loc", "Changed");

                }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
