package com.bitshares.bitshareswallet;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bitshares.bitshareswallet.room.BitsharesBalanceAsset;
import com.good.code.starts.here.ColorUtils;
import com.good.code.starts.here.dialog.hide.TokenHideAdapter;
import com.good.code.starts.here.servers.ServersFragment;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kizitonwose.colorpreference.ColorDialog;
import com.kizitonwose.colorpreference.ColorPreference;
import com.kizitonwose.colorpreference.ColorShape;
import com.kizitonwose.colorpreferencecompat.ColorPreferenceCompat;

import java.util.ArrayList;
import java.util.List;


public class SettingsFragment extends PreferenceFragmentCompat {

    private boolean loaded = false;

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

        ColorPreferenceCompat colorPreference = (ColorPreferenceCompat) findPreference("color");
        colorPreference.setOnPreferenceClickListener(p -> {
            new ColorDialog.Builder((SettingsActivity) getActivity())
                    .setColorShape(ColorShape.CIRCLE)
                    .setColorChoices(R.array.color_choices)
                    .setNumColumns(5)
                    .setSelectedColor(ColorUtils.getMainColor(getActivity()))
                    .show();
            return true;
        });

        Preference hidePreference = findPreference("hide");
        hidePreference.setOnPreferenceClickListener(p -> {

            ProgressDialog progressDialog = ProgressDialog.show(getActivity(),"Loading assets", "");
            BitsharesApplication.getInstance().getBitsharesDatabase().getBitsharesDao().queryAvaliableBalances("USD").observe(this, bitsharesBalanceAssets -> {

                if(!loaded) {
                    loaded = true;
                    List<String> symbolList = new ArrayList<>();
                    symbolList.add("FINTEH");
                    for (BitsharesBalanceAsset bitsharesBalanceAsset : bitsharesBalanceAssets) {
                        if (!bitsharesBalanceAsset.quote.equals("FINTEH"))
                            symbolList.add(bitsharesBalanceAsset.quote);
                    }

                    RecyclerView recyclerView = new RecyclerView(getActivity());

                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    TokenHideAdapter adapter = new TokenHideAdapter(getActivity(), symbolList);
                    recyclerView.setAdapter(adapter);

                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Hide")
                            .setView(recyclerView)
                            .setPositiveButton("Save", (dialog1, which) -> {
                                adapter.save();
                                loaded = false;
                            })
                            .setNegativeButton("Cancel", ((dialog2, which) -> {
                                loaded = false;
                            } ))
                            .create();

                    progressDialog.dismiss();
                    dialog.show();
                }
            });
            return true;
        });

    }
}
