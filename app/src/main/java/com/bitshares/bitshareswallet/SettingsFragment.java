package com.bitshares.bitshareswallet;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference preference = findPreference("currency_setting");
        preference.setOnPreferenceChangeListener((preference1, newValue) -> {
            Intent intent = new Intent();
            intent.putExtra("setting_changed", "currency_setting");
            getActivity().setResult(Activity.RESULT_OK, intent);
            return true;
        });

        Preference pinPreference = findPreference("pin_settings");
        pinPreference.setOnPreferenceClickListener(p -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PinSettingsFragment()).addToBackStack(null).commit();
            return true;
        });
    }
}
