package com.bitshares.bitshareswallet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.good.code.starts.here.ColorUtils;

public class SignUpButtonActivity extends LocalizationActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //if(preferences.contains("locale")) setLanguage(preferences.getString("locale", "ru"));
        setContentView(R.layout.activity_sign_up_button);

        TextView button = findViewById(R.id.sign_up_button);

        int color = ColorUtils.getMainColor(this);
        button.getRootView().setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorUtils.manipulateColor(color, 0.75f));
        }

        button.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpButtonActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.buttonLogin).setOnClickListener(v -> {
            Intent intent = new Intent(SignUpButtonActivity.this, ModelSelectActivity.class);
            startActivity(intent);
        });
    }
}
