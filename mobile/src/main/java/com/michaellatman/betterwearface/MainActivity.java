package com.michaellatman.betterwearface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.soundcloud.android.crop.Crop;
import com.soundcloud.android.crop.CropImageActivity;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;


public class MainActivity extends Activity  {

    private boolean gAPIConnected;
    private GoogleApiClient mGoogleAppiClient;
    private LocationManager mLocationManager;
    private Button mPickBackground;
    private Button mClearBackground;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Random rand  =new Random();

        Intent intent = new Intent();
        intent.setAction("com.michaellatman.boot");
        sendBroadcast(intent);
        mPickBackground = (Button)findViewById(R.id.pickBackground);
        mPickBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
        mClearBackground = (Button)findViewById(R.id.clearBackground);
        mClearBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("backgroundSet", false);
                editor.commit();
                SettingsSyncService.startSync(view.getContext());
            }
        });
    }
    public static void saveFile(Context context, Bitmap b, String picName){
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        }
        catch (FileNotFoundException e) {
            Log.d("file", "file not found");
            e.printStackTrace();
        }
        catch (IOException e) {
            Log.d("file", "io exception");
            e.printStackTrace();
        }

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
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Uri outputUri = Uri.fromFile(new File(getCacheDir(), "cropped"));
        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    Log.d("Image","Selected!");
                    /*
                    */


                    new Crop(imageReturnedIntent.getData()).output(outputUri).withMaxSize(312, 312).asSquare().start(this);
                    //imageView.setImageBitmap(bitmap);
                }
                break;
            case Crop.REQUEST_CROP:
                if(resultCode == RESULT_OK) {
                    Log.d("Image", "Cropped!");
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();

                    InputStream stream = null;
                    Bitmap bitmap = null;
                    try {


                            stream = getContentResolver().openInputStream(
                                    Uri.parse(outputUri.toString()));
                            bitmap = BitmapFactory.decodeStream(stream);
                            saveFile(this,bitmap,"background");
                            stream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(bitmap!=null){
                        editor.putBoolean("backgroundSet", true);
                    }
                    else{
                        editor.putBoolean("backgroundSet", false);
                    }
                    bitmap.recycle();
                    editor.commit();
                    SettingsSyncService.startSync(this);
                }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this,SettingsActivity.class);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
