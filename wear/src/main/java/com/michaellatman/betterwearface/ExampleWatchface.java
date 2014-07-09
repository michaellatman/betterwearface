package com.michaellatman.betterwearface;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.wearable.view.CircledImageView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * Created by jeremy on 7/4/14.
 */
public class ExampleWatchface extends Activity implements DataListener {

    ImageView background;
    AnimatedTextView hours;
    AnimatedTextView tens;
    AnimatedTextView minutes;
    TextView dayView;
    TextView date;
    Animation out;
    Animation in;
    private GoogleApiClient mGoogleAppiClient;
    private Handler handler = new Handler();
    private CircledImageView weather;
    private TextView weatherText;

    private String numberToText(int number){
        switch (number){
            case 1:
                return "One";
            case 2:
                return "Two";
            case 3:
                return "Three";
            case 4:
                return "Four";
            case 5:
                return "Five";
            case 6:
                return "Six";
            case 7:
                return "Seven";
            case 8:
                return "Eight";
            case 9:
                return "Nine";
            case 10:
                return "Ten";
            case 11:
                return "Eleven";
            case 12:
                return "Twelve";
            case 13:
                return "Thirteen";
            case 14:
                return "Fourteen";
            case 15:
                return "Fifteen";
            case 16:
                return "Sixteen";
            case 17:
                return "Seventeen";
            case 18:
                return "Eighteen";
            case 19:
                return "Nineteen";
            case 20:
                return "Twenty";
            case 30:
                return "Thirty";
            case 40:
                return "Forty";
            case 50:
                return "Fifty";
        }
        return "";
    }

    private Handler mHandler;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    public String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }
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
    private void updateTime(Calendar calendar){
        date.setText(getMonth(calendar.get(Calendar.MONTH))+" "+calendar.get(Calendar.DAY_OF_MONTH));
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
// for the current Locale :
//   DateFormatSymbols symbols = new DateFormatSymbols();
        String[] dayNames = symbols.getWeekdays();
        //calendar.set(Calendar.HOUR,0);
        //calendar.set(Calendar.MINUTE,0);
        dayView.setText(dayNames[calendar.get(Calendar.DAY_OF_WEEK)]);
        if(calendar.get(Calendar.MINUTE)>20){

                tens.setPendingText(numberToText((Integer.valueOf(Integer.toString(calendar.get(Calendar.MINUTE)).substring(0, 1) + "0"))));
                minutes.setPendingText(numberToText((Integer.valueOf(Integer.toString(calendar.get(Calendar.MINUTE)).substring(1, 2)))));

        }
        else{

            if(calendar.get(Calendar.MINUTE) == 0){
                tens.setPendingText("o'clock");
            }
            else {
                tens.setPendingText(numberToText(calendar.get(Calendar.MINUTE)));
            }
            minutes.setPendingText("");
        }

        if(calendar.get(Calendar.HOUR)!=0) hours.setPendingText(numberToText(calendar.get(Calendar.HOUR)));
        else hours.setPendingText("Twelve");
    }


    private void getRemoteNodeId(ResultCallback<NodeApi.GetConnectedNodesResult> result) {
        HashSet<String> results = new HashSet<String>();

        Wearable.NodeApi.getConnectedNodes(mGoogleAppiClient).setResultCallback(result);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("WatchFace", "onCreate();");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        background = (ImageView) findViewById(R.id.background);
        hours = (AnimatedTextView) findViewById(R.id.hoursView);
        tens = (AnimatedTextView) findViewById(R.id.tensView);
        minutes = (AnimatedTextView) findViewById(R.id.minView);
        weather = (CircledImageView) findViewById(R.id.weatherIcon);
        weatherText = (TextView) findViewById(R.id.weatherText);
        dayView = (TextView) findViewById(R.id.dayView);
        date = (TextView) findViewById(R.id.dateView);
        weather.setCircleRadius(40);
        weather.setCircleColor(Color.BLACK);
        weather.setCircleBorderColor(Color.WHITE);

        in = AnimationUtils.loadAnimation(this, R.anim.slidein);
        out = AnimationUtils.loadAnimation(this, R.anim.slideout);
        Calendar calendar = Calendar.getInstance();
        updateTime(calendar);
        // Start the clock.
        mClock.start();

        mTimeBroadcastReceiver.register(this);
        mDataBroadcastReceiver.register(this);

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);

        builder.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle connectionHint) {
                Log.d("GAPI", "onConnected: " + connectionHint);
                getRemoteNodeId(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                   /* @Override
                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                        Uri lol = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).authority(getLocalNodeResult.getNode().getId()).path("/weather").build();
                        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.getDataItem(mGoogleAppiClient, lol);
                        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(DataApi.DataItemResult dataItemResult) {
                                updateWeather(DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap());
                            }
                        });
                    }*/

                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        List<Node> nodes = getConnectedNodesResult.getNodes();
                        if (nodes.size() > 0) {
                            Log.d("Nodes",nodes.get(0).getId());
                            Uri lol = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).authority(nodes.get(0).getId()).path("/weather").build();
                            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.getDataItem(mGoogleAppiClient, lol);
                            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                                @Override
                                public void onResult(DataApi.DataItemResult dataItemResult) {
                                    if(dataItemResult.getDataItem() != null) {
                                        updateWeather(DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap());
                                    }
                                }
                            });

                        }

                    }
                });

            }

            @Override
            public void onConnectionSuspended(int cause) {
                Log.d("GAPI", "onConnectionSuspended: " + cause);

            }
        });

        builder.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Log.d("GAPI", "onConnectionFailed: " + result);

            }
        });
        builder.addApi(Wearable.API);
        mGoogleAppiClient = builder.build();
        mGoogleAppiClient.connect();
    }

    private void updateSettings(DataMap dataMap) {
        if(dataMap!=null) {
            if (dataMap.getAsset("image") != null) {
                //Log.d("image", dataMap.getByteArray("image").toString());
                //Bitmap b = BitmapFactory.decodeByteArray(, 0, dataMap.getAsset("image").getData().length)
                /*
                loadBitmapFromAsset( dataMap.getAsset("image"),new ResultCallback<DataApi.GetFdForAssetResult>(){

                    @Override
                    public void onResult(DataApi.GetFdForAssetResult getFdForAssetResult) {
                        if (getFdForAssetResult.getInputStream() == null) {
                            Log.w("image", "Requested an unknown Asset.");

                        }
                        // decode the stream into a bitmap
                        //background.setImageBitmap(BitmapFactory.decodeStream(getFdForAssetResult.getInputStream()));
                    }
                });
                */

            }
        }
    }


    private void updateWeather(DataMap changed) {
        if(changed.getString("condition") != null) {
            weatherText.setVisibility(View.VISIBLE);
            weather.setVisibility(View.VISIBLE);
            weatherText.setText(changed.getString("text"));
            int condition = Integer.valueOf(changed.getString("condition"));
            if (condition == 113) {
                weather.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
            } else if (condition == 116) {
                weather.setImageDrawable(getResources().getDrawable(R.drawable.mostlycloudy));
            } else if (condition == 119) {
                weather.setImageDrawable(getResources().getDrawable(R.drawable.cloud));
            } else if (condition == 122) {
                weather.setImageDrawable(getResources().getDrawable(R.drawable.cloud));
            } else if (condition >= 293 && condition <= 308) {
                weather.setImageDrawable(getResources().getDrawable(R.drawable.rain));
            } else if (condition >= 323 && condition <= 395) {
                weather.setImageDrawable(getResources().getDrawable(R.drawable.snow));
            }
        }
        else {
            weatherText.setVisibility(View.INVISIBLE);
            weather.setVisibility(View.INVISIBLE);
        }
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
    }

    @Override
    public void onDataChange(String node, DataMap changed) {
        Log.d("Data Change",node);
        if(node.equals("weather")){
            wakeLock.acquire(1000);
            updateWeather(changed);
        }
    }
}
