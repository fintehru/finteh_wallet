package com.bitshares.bitshareswallet;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceManager;

public class PinSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_pin, rootKey);


        SwitchPreference switchPreference = (SwitchPreference) findPreference("pin_switch");
        Preference preference = findPreference("pin_set");

        boolean status = PreferenceManager.getDefaultSharedPreferences(getActivity()).contains("val");
        switchPreference.setChecked(status);
        preference.setVisible(status);

        switchPreference.setOnPreferenceChangeListener((pref, newValue) -> {
            boolean enabled = (Boolean) newValue;
            preference.setVisible(enabled);

            if(enabled) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, PinSetFragment.newInstance(PinSetFragment.SET)).addToBackStack(null).commit();
            } else {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, PinSetFragment.newInstance(PinSetFragment.REMOVE)).addToBackStack(null).commit();
            }

            return true;
        });

        preference.setOnPreferenceClickListener(pref -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, PinSetFragment.newInstance(PinSetFragment.CHANGE)).addToBackStack(null).commit();
            return true;
        });
    }
}
