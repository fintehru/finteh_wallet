package com.bitshares.bitshareswallet;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.EditText;

import com.good.code.starts.here.servers.ServersFragment;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        findPreference("currency_setting").setVisible(false);

        /*Preference preference = findPreference("currency_setting");
        preference.setOnPreferenceChangeListener((preference1, newValue) -> {
            Intent intent = new Intent();
            intent.putExtra("setting_changed", "currency_setting");
            getActivity().setResult(Activity.RESULT_OK, intent);
            return true;
        });*/

        Preference pinPreference = findPreference("pin_settings");
        pinPreference.setOnPreferenceClickListener(p -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PinSettingsFragment()).addToBackStack(null).commit();
            return true;
        });

        Preference serverSelectPreference = findPreference("full_node_api_server");
        serverSelectPreference.setOnPreferenceClickListener(p -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ServersFragment()).addToBackStack(null).commit();
            return true;
        });
    }
}
