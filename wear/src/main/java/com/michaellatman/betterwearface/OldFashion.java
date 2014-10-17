package com.michaellatman.betterwearface;

import android.graphics.Bitmap;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.wearable.DataMap;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by michael on 7/11/14.
 */
public class OldFashion extends WatchfaceActivity {
    ImageView background;
    AnimatedTextView hour;
    AnimatedTextView hour2;
    AnimatedTextView minute;
    AnimatedTextView minute2;
    AnimatedTextView center;
    AnimatedTextView date;
    @Override
    protected void doSetup() {
        setContentView(R.layout.oldfashion);
        background = (ImageView) findViewById(R.id.background);
        hour = (AnimatedTextView) findViewById(R.id.hour1);
        hour2 = (AnimatedTextView) findViewById(R.id.hour2);
        minute = (AnimatedTextView) findViewById(R.id.minute1);
        minute2 = (AnimatedTextView) findViewById(R.id.minute2);
        center = (AnimatedTextView) findViewById(R.id.center);
        date = (AnimatedTextView) findViewById(R.id.date);
        hour.setIn(AnimationUtils.loadAnimation(this, R.anim.fadein));
        hour2.setIn(AnimationUtils.loadAnimation(this, R.anim.fadein));
        minute.setIn(AnimationUtils.loadAnimation(this, R.anim.fadein));
        minute2.setIn(AnimationUtils.loadAnimation(this, R.anim.fadein));
        hour.setOut(AnimationUtils.loadAnimation(this, R.anim.fadeout));
        hour2.setOut(AnimationUtils.loadAnimation(this, R.anim.fadeout));
        minute.setOut(AnimationUtils.loadAnimation(this, R.anim.fadeout));
        minute2.setOut(AnimationUtils.loadAnimation(this, R.anim.fadeout));
        super.doSetup();
        startClock();
    }
    public String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }
    @Override
    protected void updateTime(Calendar calendar) {
        super.updateTime(calendar);
        String h = ""+calendar.get(Calendar.HOUR);
        if(calendar.get(Calendar.HOUR)==0)h="12";
        if(DateFormat.is24HourFormat(this)){
            h = ""+calendar.get(Calendar.HOUR_OF_DAY);


        }
        if(h.length()>1){
            hour.setPendingText(""+h.substring(0,1));
            hour2.setPendingText(""+h.substring(1,2));
        }
        else{
            hour.setPendingText("0");
            hour2.setPendingText(""+h);
        }
        String m = ""+calendar.get(Calendar.MINUTE);
        if(m.length()>1){
            minute.setPendingText(""+m.substring(0,1));
            minute2.setPendingText(""+m.substring(1,2));
        }
        else{
            minute.setPendingText("0");
            minute2.setPendingText(m);
        }

        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
// for the current Locale :
//   DateFormatSymbols symbols = new DateFormatSymbols();
        String[] dayNames = symbols.getShortWeekdays();
        String[] monthsNames = symbols.getShortMonths();
        //calendar.set(Calendar.HOUR,0);
        //calendar.set(Calendar.MINUTE,0);
        date.setPendingText(dayNames[calendar.get(Calendar.DAY_OF_WEEK)]+" "+monthsNames[calendar.get(Calendar.MONTH)]+" "+calendar.get(Calendar.DAY_OF_MONTH));

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

    @Override
    protected void doRestore(){
        fetchAsset("/settings", "background");
    }
    @Override
    protected void onAssetLoad(String path, String loc, Bitmap bitmap){
        if(loc.equals("background")){
            background.setImageBitmap(bitmap);
        }

    }
    @Override
    public void onDataChange(String node, final DataMap changed) {
        super.onDataChange(node,changed);
        Log.d("Data Change Activity", node);
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
}
