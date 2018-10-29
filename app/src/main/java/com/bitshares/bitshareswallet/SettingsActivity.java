package com.bitshares.bitshareswallet;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.good.code.starts.here.ColorUtils;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kizitonwose.colorpreference.ColorDialog;

public class SettingsActivity extends LocalizationActivity implements ColorDialog.OnColorSelectedListener {
    private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //if(preferences.contains("locale")) setLanguage(preferences.getString("locale", "ru"));
        setContentView(R.layout.activity_settings);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        int color = ColorUtils.getMainColor(this);
        mToolbar.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorUtils.manipulateColor(color, 0.75f));
        }
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();

    }

    @Override
    public void onColorSelected(int i, String s) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("color", i).commit();
        ProcessPhoenix.triggerRebirth(this);
    }
}
