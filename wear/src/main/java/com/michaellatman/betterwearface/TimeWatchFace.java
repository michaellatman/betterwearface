package com.michaellatman.betterwearface;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
public class TimeWatchFace extends WatchfaceActivity {


    ImageView background;
    AnimatedTextView hours;
    AnimatedTextView tens;
    AnimatedTextView minutes;
    TextView dayView;
    TextView date;
    Animation out;
    Animation in;
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

    public String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }
    @Override
    protected void updateTime(Calendar calendar){

        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
// for the current Locale :
//   DateFormatSymbols symbols = new DateFormatSymbols();
        String[] dayNames = symbols.getShortWeekdays();
        String[] monthsNames = symbols.getShortMonths();
        //calendar.set(Calendar.HOUR,0);
        //calendar.set(Calendar.MINUTE,0);
        dayView.setText(dayNames[calendar.get(Calendar.DAY_OF_WEEK)]+" "+monthsNames[calendar.get(Calendar.MONTH)]+" "+calendar.get(Calendar.DAY_OF_MONTH));
        //dayView.setText(dayNames[calendar.get(Calendar.DAY_OF_WEEK)]);
        if(calendar.get(Calendar.MINUTE)>20){

                tens.setPendingText(numberToText((Integer.valueOf(Integer.toString(calendar.get(Calendar.MINUTE)).substring(0, 1) + "0"))));
                minutes.setPendingText(numberToText((Integer.valueOf(Integer.toString(calendar.get(Calendar.MINUTE)).substring(1, 2)))));

        }
        else{

            if(calendar.get(Calendar.MINUTE) == 0){
                tens.setPendingText("o'clock");
            }
            else if(calendar.get(Calendar.MINUTE)<10){
                tens.setPendingText("O'"+numberToText(calendar.get(Calendar.MINUTE)));
            }
            else  tens.setPendingText(numberToText(calendar.get(Calendar.MINUTE)));
            minutes.setPendingText("");
        }

        if(calendar.get(Calendar.HOUR)!=0) hours.setPendingText(numberToText(calendar.get(Calendar.HOUR)));
        else hours.setPendingText("Twelve");
    }



    @Override
    protected void doSetup(){
        setContentView(R.layout.main);
        background = (ImageView) findViewById(R.id.background);
        hours = (AnimatedTextView) findViewById(R.id.hoursView);
        tens = (AnimatedTextView) findViewById(R.id.tensView);
        minutes = (AnimatedTextView) findViewById(R.id.minView);
        weather = (CircledImageView) findViewById(R.id.weatherIcon);
        weatherText = (TextView) findViewById(R.id.weatherText);
        dayView = (TextView) findViewById(R.id.dayView);

        weather.setCircleRadius(40);


        in = AnimationUtils.loadAnimation(this, R.anim.slidein);
        out = AnimationUtils.loadAnimation(this, R.anim.slideout);
    }
    @Override
    protected void doRestore(){
        fetch("/weather");
        fetchAsset("/settings", "background");
    }

    @Override
    protected void onDataNotExist(String path){
        if(path.equals("/weather")){
            weatherText.setVisibility(View.INVISIBLE);
            weather.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        background.setVisibility(View.INVISIBLE);
    }
    @Override
    protected void onResume(){
        super.onResume();
        background.setVisibility(View.VISIBLE);
    }

    private void updateSettings(DataMap dataMap) {
        if(dataMap!=null) {
            if (dataMap.getAsset("image") != null) {

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
            } else if (condition>=800&&condition<=804) {
                if(getTime().get(Calendar.AM_PM) == Calendar.AM){
                    if(condition==800){
                        weather.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                        weather.setImageDrawable(getResources().getDrawable(R.drawable.mostlycloudy));
                    }
                    if(condition==802)  weather.setImageDrawable(getResources().getDrawable(R.drawable.cloud));
                    else
                        weather.setImageDrawable(getResources().getDrawable(R.drawable.mostlycloudy));
                }
                else{
                    weather.setImageDrawable(getResources().getDrawable(R.drawable.cloud));
                }
            }
            else if (condition >= 300 && condition <= 531) {
                weather.setImageDrawable(getResources().getDrawable(R.drawable.rain));
            } else if (condition >= 600 && condition <= 622) {
                weather.setImageDrawable(getResources().getDrawable(R.drawable.snow));
            }
            else if (condition >= 200 && condition <= 232) {
                weather.setImageDrawable(getResources().getDrawable(R.drawable.thunder));
            }
        }
        else {
            weatherText.setVisibility(View.INVISIBLE);
            weather.setVisibility(View.INVISIBLE);
        }
    }




    @Override
    public void onDataChange(String node, final DataMap changed) {
        super.onDataChange(node,changed);
        Log.d("Data Change Activity",node);
        if(node.equals("weather")){

            updateWeather(changed);
        }
        if(node.equals("settings")){
            if(changed.getBoolean("customBackground",false)){
                Log.d("CustomBackground","Yes");
                fetchAsset("/settings","background");
            }
            else{
                if(background.getDrawable() != null){
                    /*
                    Notification noti = new Notification.Builder(this)
                            .setContentTitle("Background Removed")
                            .setContentText("If you removed the background, ignore. If this is an error. email mlatman@gmail.com")
                            .setSmallIcon(R.drawable.go_to_phone_00156)
                            .build();
                    NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    //call notify method of NotificationManager to add this notification to android notification drawer..
                    notificationmanager.notify(0, noti);
                    */

                }
                Log.d("CustomBackground","No");
                background.setImageDrawable(null);
            }
        }
    }
    @Override
    protected void onAssetLoad(String path, String loc, Bitmap bitmap){
        if(loc.equals("background")){
            background.setImageBitmap(bitmap);
        }

    }
}
