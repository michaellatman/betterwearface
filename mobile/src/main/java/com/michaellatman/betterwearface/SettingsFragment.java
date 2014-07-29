package com.michaellatman.betterwearface;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.michaellatman.betterwearface.R;
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_ALWAYS_ANIMATE = "setting_always_animate";
    public static final String KEY_PREF_TEMP_FORMAT = "setting_temp_format";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        updateSmrs(getPreferenceScreen().getSharedPreferences());

    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onStop() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
    public void updateSmrs(SharedPreferences sharedPreferences){
        Preference connectionPref = findPreference(KEY_PREF_ALWAYS_ANIMATE);
        if(sharedPreferences.getBoolean(KEY_PREF_ALWAYS_ANIMATE, true))
            connectionPref.setSummary(R.string.SETTING_ALWAYS_ANIMATE_TRUE);
        else
            connectionPref.setSummary(R.string.SETTING_ALWAYS_ANIMATE_FALSE);

        connectionPref = findPreference(KEY_PREF_TEMP_FORMAT);
        if(sharedPreferences.getString(KEY_PREF_TEMP_FORMAT, "Fahrenheit").equals("Fahrenheit"))
            connectionPref.setSummary(R.string.SETTING_TEMP_FORMAT_F);
        else
            connectionPref.setSummary(R.string.SETTING_TEMP_FORMAT_C);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        updateSmrs(sharedPreferences);
        Preference connectionPref = findPreference(key);
        if(key.equals(KEY_PREF_ALWAYS_ANIMATE)){

        }
        if(key.equals(KEY_PREF_TEMP_FORMAT)){
            SettingsSyncService.startSync(getActivity());
            UpdateWeatherService.update(getActivity());

        }
        if(key.equals("weather")){
            SettingsSyncService.startSync(getActivity());
            UpdateWeatherService.update(getActivity());
        }
            //Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            //connectionPref.setSummary(sharedPreferences.getBoolean(key, ));
        Toast.makeText(getActivity(),"Test "+key,Toast.LENGTH_LONG).show();

    }

}