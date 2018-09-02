package com.bitshares.bitshareswallet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.bitshares.bitshareswallet.util.Safe;
import com.bitshares.bitshareswallet.wallet.BitsharesWalletWraper;
import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;

public class LockActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BitsharesWalletWraper bitsharesWalletWraper = BitsharesWalletWraper.getInstance();

        if (bitsharesWalletWraper.load_wallet_file() != 0 || bitsharesWalletWraper.is_new()){
            Intent intent = new Intent(this, SignUpButtonActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        } else {
            preferences = getSharedPreferences("data", Context.MODE_PRIVATE);
            if(!preferences.contains("val")) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            } else {
                setContentView(R.layout.activity_lock);

                String strName = bitsharesWalletWraper.get_account().name;

                sha256_object.encoder encoder = new sha256_object.encoder();
                encoder.write(strName.getBytes());

                WebView webViewFrom = findViewById(R.id.webViewAvatarFrom);
                loadWebView(webViewFrom, 85, encoder.result().toString());

                PinLockView mPinLockView = findViewById(R.id.pin_lock_view);
                IndicatorDots mIndicatorDots = findViewById(R.id.indicator_dots);

                mPinLockView.attachIndicatorDots(mIndicatorDots);
                mPinLockView.setPinLockListener(new PinLockListener() {
                    @Override
                    public void onComplete(String pin) {
                        if (checkPin(pin)) {
                            Intent intent = new Intent(LockActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            mPinLockView.resetPinLockView();
                            Toast.makeText(LockActivity.this, R.string.incorrect_pin, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onEmpty() { }

                    @Override
                    public void onPinChange(int pinLength, String intermediatePin) { }
                });

                mPinLockView.setPinLength(6);
                mPinLockView.setShowDeleteButton(true);
                mPinLockView.setTextColor(ContextCompat.getColor(this, R.color.white));

                mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FIXED);
            }
        }
    }

    private boolean checkPin(String pin) {
        String val = preferences.getString("val", null);
        if(val != null) {
            return Safe.encryptDecrypt(val, new char[]{'B','I','T','S','H','A','R','E'}).equals(pin);
        }
        return false;
    }

    private void loadWebView(WebView webView, int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html {margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }

}
