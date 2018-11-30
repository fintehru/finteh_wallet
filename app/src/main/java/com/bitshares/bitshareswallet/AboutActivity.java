package com.bitshares.bitshareswallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;
import com.good.code.starts.here.ColorUtils;

import java.util.Locale;

import de.bitsharesmunich.graphenej.models.backup.WalletBackup;

public class AboutActivity extends LocalizationActivity {
    private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //if(preferences.contains("locale")) setLanguage(preferences.getString("locale", "ru"));
        setContentView(R.layout.activity_about);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        int color = ColorUtils.getMainColor(this);
        mToolbar.setBackgroundColor(color);
        findViewById(R.id.appBar).setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorUtils.manipulateColor(color, 0.75f));
        }
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textViewAccountName = (TextView)findViewById(R.id.textViewAccountName);
        final String strName = "finteh";
        textViewAccountName.setText(strName);

        sha256_object.encoder encoder = new sha256_object.encoder();
        encoder.write(strName.getBytes());

        WebView webView = (WebView)findViewById(R.id.webViewAvatar);
        loadWebView(webView, 70, encoder.result().toString());

        findViewById(R.id.textViewCopyAccount).setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("account name", strName);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(AboutActivity.this, R.string.copy_success, Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_donate).setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, MainActivity.class);
            intent.putExtra("action", "donate");
            intent.putExtra("name", strName);
            intent.putExtra("amount", "10");
            intent.putExtra("unit", "BTS");
            startActivity(intent);
        });

        TextView textViewVersion = (TextView)findViewById(R.id.textViewVersion);
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
            String strVersion = getString(R.string.about_activity_version);
            textViewVersion.setText(strVersion + " " + packInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadWebView(WebView webView, int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html { margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }
}
